package common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import operator.DedupOperator;
import operator.JoinOperator;
import operator.Operator;
import operator.ProjectOperator;
import operator.ScanOperator;
import operator.SelectOperator;
import operator.SortOperator;
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

    //        String firstTableName = fromItem.getAlias() != null ? fromItem.getAlias().getName() :
    // fromItem.getName();

    // Get "WHERE"
    Expression eval = body.getWhere();
    // Get "SELECT"
    List<SelectItem> selectItems = body.getSelectItems();
    // Get "JOIN" "ON"
    List<Join> joins = body.getJoins();
    // Get "ORDER BY"
    List<OrderByElement> orderByElements = body.getOrderByElements();

    buildAliasMap(aliasMap, fromItem, joins);
    // Build operator tree, return the top
    BuildOpVisitor treeBuilder = new BuildOpVisitor();

    // parsing WHERE condition for following JOIN
    Map<String, List<Expression>> selectCond = new HashMap<>();
    Map<String, List<Expression>> joinCond = new HashMap<>();
    if (eval != null) {
      // Classify it into two groups:
      //                              selectCond   :    after scan ,  can be done immediately
      //                              joinCond     :    be done when joining
      List<Expression> expressions = ConditionParser(eval);
      ClassifyExpVisitor classifier = new ClassifyExpVisitor(selectCond, joinCond, aliasMap);
      for (Expression e : expressions) {
        e.accept(classifier);
      }
      // turn alias into table_name
      processAlias(selectCond, aliasMap);
      processAlias(joinCond, aliasMap);
    }
    // FROM ... The first table as source    must use the table's name  , not alias
    treeBuilder.visit(new ScanOperator(fromItem.getName()));
    // self-scan first
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

        // use left-table (the already composite one)  to join (may conditioned) right-table
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
    if (body.getDistinct() != null) {
      if (orderByElements == null) {
        treeBuilder.visit(new SortOperator(new ArrayList<>(), aliasMap));
      }
      treeBuilder.visit(new DedupOperator());
    }
    return treeBuilder.getRoot();
  }

  private void processAlias(Map<String, List<Expression>> map, Map<String, Table> aliasMap) {
    if (map.size() == 0) return;
    //
    Collection<List<Expression>> exprss = map.values();
    AliasExpVisitor aliasExpVisitor = new AliasExpVisitor(aliasMap);
    for (List<Expression> exprs : exprss) {
      for (Expression expr : exprs) {
        expr.accept(aliasExpVisitor);
      }
    }
  }

  //    private void buildAliasMap(Map<String, String> aliasMap, Table fromItem, List<Join> joins) {
  //        // for the first
  //        if (fromItem.getAlias() != null) {
  //            aliasMap.put(fromItem.getAlias().getName(), fromItem.getName());
  //        }
  //        // for the rest
  //        if (joins != null) {
  //            for (Join j : joins) {
  //                Table rightTable = (Table) j.getRightItem();
  //                if (rightTable.getAlias() != null) {
  //                    aliasMap.put(rightTable.getAlias().getName(), rightTable.getName());
  //                }
  //            }
  //        }
  //
  //    }
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

  private List<Expression> ConditionParser(Expression expr) {
    List<Expression> res = new ArrayList<>();
    parseHelper(expr, res);
    return res;
  }

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
