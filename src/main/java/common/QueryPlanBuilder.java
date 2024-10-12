package common;

import LogicalOperator.*;
import java.util.*;

import PhysicalOperator.SelectOperator;
import PhysicalOperator.SortOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import tools.alias.AliasTool;
import visitor.ClassifyExpVisitor;
import visitor.LogicalOpVisitor;

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

  public QueryPlanBuilder() {
    aliasMap = new HashMap<>();
  }

  private Map<String, Table> aliasMap;

  public Map<String, Table> getAliasMap() {
    return aliasMap;
  }

  /**
   * Top level method to translate statement to LOGICAL query plan. Also generates the alias map
   *
   * @param stmt statement to be translated
   * @return the root of the logical query plan
   */
  public LogicalOperator buildPlan(Statement stmt) {
    // alias -> name
    aliasMap = new HashMap<>();
    Select sql = (Select) stmt;
    PlainSelect body = (PlainSelect) sql.getSelectBody();
    // Get  "FROM"
    Table fromItem = (Table) body.getFromItem();
    // Get "JOIN" "ON"
    List<Join> joins = body.getJoins();
    // map each alias to a table instance
    buildAliasMap(aliasMap, fromItem, joins);
    // Get "WHERE"
    Expression eval = body.getWhere();
    // Get "SELECT"
    List<SelectItem> selectItems = body.getSelectItems();
    // Get "ORDER BY"
    List<OrderByElement> orderByElements = body.getOrderByElements();

    String firstAliasOrName = AliasTool.getAliasOrName(fromItem, aliasMap);

    // Build operator tree, return the top
    LogicalOpVisitor treeBuilder = new LogicalOpVisitor();

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
    }

    // Building the operator tree

    // FROM ...
    treeBuilder.visit(new LogicalScanOp(fromItem.getName(), fromItem));
    // self-selection
    if (selectCond.containsKey(firstAliasOrName)) {
      for (Expression e : selectCond.get(firstAliasOrName)) {
        treeBuilder.visit(new LogicalSelectOp(e));
      }
    }

    // JOIN ...
    // a set to record the tables that already joined the current
    Set<String> joined_tables = new HashSet<>();
    joined_tables.add(firstAliasOrName);
    if (joins != null) {
      for (Join join : joins) {
        // get the right table info
        LogicalOpVisitor subTreeBuilder = new LogicalOpVisitor();
        Table rightTable = (Table) join.getRightItem();
        String rightAliasOrName = AliasTool.getAliasOrName(rightTable, aliasMap);
        // scan right table    must use the table's name  , not alias
        subTreeBuilder.visit(new LogicalScanOp(rightTable.getName(), rightTable));
        // do self-selection
        if (selectCond.containsKey(rightAliasOrName)) {
          for (Expression e : selectCond.get(rightAliasOrName)) {
            subTreeBuilder.visit(new LogicalSelectOp(e));
          }
        }

        // use left-table (the already composite one)  to join (if conditioned) right-table
        // imp2
        // Combine join conditions
        Expression combinedCondition = null;
        for (String leftAliasOrName : joined_tables) {
          String combi1 = leftAliasOrName + "," + rightAliasOrName;
          String combi2 = rightAliasOrName + "," + leftAliasOrName;
          if (joinCond.containsKey(combi1) || joinCond.containsKey(combi2)) {
            List<Expression> conditions = joinCond.getOrDefault(combi1, joinCond.get(combi2));
            for (Expression e : conditions) {
              if (combinedCondition == null) {
                combinedCondition = e;
              } else {
                combinedCondition = new AndExpression(combinedCondition, e);
              }
            }
          }
        }

        // Create a single join operator with the combined condition
        if (combinedCondition != null) {
          treeBuilder.visit(new LogicalJoinOp(subTreeBuilder.getRoot(), combinedCondition));
        } else {
          // If no specific condition, just join without conditions
          treeBuilder.visit(new LogicalJoinOp(subTreeBuilder.getRoot(), null));
        }
        joined_tables.add(rightAliasOrName);
      }
    }
    // SELECT .... projection
    if (selectItems.size() > 0) {
      treeBuilder.visit(new LogicalProjectOp(selectItems));
    }
    // ORDER...BY
    if (orderByElements != null) {
      treeBuilder.visit(new LogicalSortOp(orderByElements));
    }
    // DISTINCT
    if (body.getDistinct() != null) {
      if (orderByElements == null) {
        treeBuilder.visit(new LogicalSortOp(new ArrayList<>()));
      }
      treeBuilder.visit(new LogicalDedupOp());
    }
    return treeBuilder.getRoot();
  }

  /**
   * For all the tables refered by the SQL, if it has alias, map the alias String to it
   *
   * @param aliasMap the passed in aliasMap, expected to be empty first
   * @param fromItem the first table in FROM clause
   * @param joins the remaining table in FROM clause
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
   * Parse the whole expression in the WHERE into List<Expression> that are delimited by AND Return
   * the List p.s : cannot deal with nested AND
   *
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
   *
   * @param expr expression maybe leaf root
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
