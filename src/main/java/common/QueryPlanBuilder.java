package common;

import java.util.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import operator.*;
import visitor.AliasExpVisitor;
import visitor.BuildOpVisitor;
import visitor.ClassifyExpVisitor;

/**
 * Class to translate a JSQLParser statement into a relational algebra query plan. For now only
 * works for Statements that are Selects, and specifically PlainSelects. Could implement the visitor
 * pattern on the statement, but doesn't for simplicity as we do not handle nesting or other complex
 * query features.
 *
 * <p>Query plan fixes join order to the order found in the from clause and uses a left deep tree
 * join. Maximally pushes selections on individual relations and evaluates join conditions as early
 * as possible in the join tree. Projections (if any) are not pushed and evaluated in a single
 * projection operator after the last join. Finally, sorting and duplicate elimination are added if
 * needed.
 *
 * <p>For the subset of SQL which is supported as well as assumptions on semantics, see the Project
 * 2 student instructions, Section 2.1
 */
public class QueryPlanBuilder {

  public QueryPlanBuilder() {}

  /**
   * Top level method to translate statement to query plan
   *
   * @param stmt statement to be translated
   * @return the root of the query plan
   * @precondition stmt is a Select having a body that is a PlainSelect
   */
  public Operator buildPlan(Statement stmt) {
    // alias -> name
    Map<String, Table> aliasMap = new HashMap<>();
    Select sql = (Select) stmt;
    PlainSelect body = (PlainSelect) sql.getSelectBody();
    // Get  "FROM"
    Table fromItem = (Table) body.getFromItem();
    String firstTableName = fromItem.getName();
    // Get "WHERE"
    Expression eval = body.getWhere();
    // Get "SELECT"
    List<SelectItem> selectItems = body.getSelectItems();
    // Get "JOIN" "ON"
    List<Join> joins = body.getJoins();
    // Get "ORDER BY"
    List<OrderByElement> orderByElements = body.getOrderByElements();
    // map each alias to a table instance
    buildAliasMap(aliasMap, fromItem, joins);
    // Build operator tree, return the top
    BuildOpVisitor treeBuilder = new BuildOpVisitor();

    // parsing WHERE condition for following JOINs
    Map<String, List<Expression>> selectCond = new HashMap<>();
    Map<String, List<Expression>> joinCond = new HashMap<>();
    if (eval != null) {
      // Classify it into two groups:
      //                              selectCond   :    after scan ,  can be done immediately
      //                              joinCond     :    be done when joining two tables
      List<Expression> expressions = ConditionParser(eval);
      ClassifyExpVisitor classifier = new ClassifyExpVisitor(selectCond, joinCond, aliasMap);
      for (Expression e : expressions) {
        e.accept(classifier);
      }
      // turn alias into table_name
      processAlias(selectCond, aliasMap);
      processAlias(joinCond, aliasMap);
    }

    // Building the operator tree

    // FROM ...
    treeBuilder.visit(new ScanOperator(fromItem.getName()));
    // self-selection
    if (selectCond.containsKey(firstTableName)) {
      for (Expression e : selectCond.get(firstTableName)) {
        treeBuilder.visit(new SelectOperator(e));
      }
    }

    // JOIN ...
    // a set to record the tables that already joined the current
    Set<String> joined_tables = new HashSet<>();
    joined_tables.add(firstTableName);
    if (joins != null) {
      for (Join join : joins) {
        // get the right table info
        BuildOpVisitor subTreeBuilder = new BuildOpVisitor();
        Table rightTable = (Table) join.getRightItem();
        String rightName = rightTable.getName();
        // scan right table    must use the table's name  , not alias
        subTreeBuilder.visit(new ScanOperator(rightTable.getName()));
        // do self-selection
        if (selectCond.containsKey(rightName)) {
          for (Expression e : selectCond.get(rightName)) {
            subTreeBuilder.visit(new SelectOperator(e));
          }
        }

        // use left-table (the already composite one)  to join (if conditioned) right-table
        for (String leftName : joined_tables) {
          String combi1 = leftName + "," + rightName;
          String combi2 = rightName + "," + leftName;
          if (joinCond.containsKey(combi1)) {
            for (Expression e : joinCond.get(combi1)) {
              treeBuilder.visit(new JoinOperator(subTreeBuilder.getRoot(), e));
            }
          } else if (joinCond.containsKey(combi2)) {
            for (Expression e : joinCond.get(combi2)) {
              treeBuilder.visit(new JoinOperator(subTreeBuilder.getRoot(), e));
            }
          } else {
            treeBuilder.visit(new JoinOperator(subTreeBuilder.getRoot(), null));
          }
        }
        joined_tables.add(rightName);
      }
    }
    // SELECT .... projection
    if (selectItems.size() > 0) {
      treeBuilder.visit(new ProjectOperator(selectItems, aliasMap));
    }
    // ORDER...BY
    if (orderByElements != null) {
      treeBuilder.visit(new SortOperator(orderByElements, aliasMap));
    }
    // DISTINCT
    if (body.getDistinct() != null) {
      if (orderByElements == null) {
        treeBuilder.visit(new SortOperator(new ArrayList<>(), aliasMap));
      }
      treeBuilder.visit(new DedupOperator());
    }
    return treeBuilder.getRoot();
  }

  /**
   * For all the tables refered by the SQL,
   * if it has alias, map the alias String to it
   * @param aliasMap   the passed in aliasMap, expected to be empty first
   * @param fromItem   the first table in FROM clause
   * @param joins      the remaining table in FROM clause
   */
  private void buildAliasMap(Map<String, Table> aliasMap, Table fromItem, List<Join> joins) {
    // project the fromTable onto its Table
    if (fromItem.getAlias() != null) {
      aliasMap.put(fromItem.getAlias().getName(), fromItem);
    }

    // project all rightTable in the join
    if (joins != null) {
      for (Join j : joins) {
        Table rightTable = (Table) j.getRightItem();
        if (rightTable.getAlias() != null) {
          aliasMap.put(rightTable.getAlias().getName(), rightTable);
        }
      }
    }
  }

  /**
   *  Process a Map, whose val is Expression. For each of the Expressions,
   *  turn their alias Column into real Name .
   *  e.g: Expression S.A < R.H   --> Expression Sailors.A < Reserves.H
   * @param map
   * @param aliasMap
   */
  private void processAlias(Map<String, List<Expression>> map, Map<String, Table> aliasMap) {
    // deal with case that don't use any alias
    if (map.isEmpty()) return;
    Collection<List<Expression>> exprss = map.values();
    AliasExpVisitor aliasExpVisitor = new AliasExpVisitor(aliasMap);
    for (List<Expression> exprs : exprss) {
      for (Expression expr : exprs) {
        expr.accept(aliasExpVisitor);
      }
    }
  }

  /**
   * Parse the whole expression in the WHERE into List<Expression> that are
   * delimited by AND
   * Return the List
   * p.s : cannot deal with nested AND
   * @param expr long expression to be parsed
   * @return the list result
   */
  private List<Expression> ConditionParser(Expression expr) {
    List<Expression> res = new ArrayList<>();
    parseHelper(expr, res);
    return res;
  }

  /**
   * Recursion helper for the method ConditionParser
   * @param expr expression  maybe leaf root
   * @param res result list to be added
   */
  private void parseHelper(Expression expr, List<Expression> res) {
    if (!(expr instanceof AndExpression)) {
      res.add(expr);
    } else {
      AndExpression Aexpr = (AndExpression) expr;
      parseHelper(Aexpr.getLeftExpression(), res);
      parseHelper(Aexpr.getRightExpression(), res);
    }
  }
}
