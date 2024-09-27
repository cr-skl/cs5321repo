package visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import tools.AliasTool;

/**
 *  Visitor for expression, determine if the given expression is a select-cond or a join-cond,
 *  and classify it to the corresponding list
 *  Map<String, List<Expression>>  the Key must be the table name, not alias
 */
public class ClassifyExpVisitor extends ExpressionVisitorAdapter {
  private Map<String, List<Expression>> selectCond;
  private Map<String, List<Expression>> joinCond;
  private Map<String, Table> aliasMap;

  public ClassifyExpVisitor(
      Map<String, List<Expression>> selectCond,
      Map<String, List<Expression>> joinCond,
      Map<String, Table> aliasMap) {
    this.selectCond = selectCond;
    this.joinCond = joinCond;
    this.aliasMap = aliasMap;
  }

  @Override
  public void visit(EqualsTo expr) {
    helper(expr);
  }

  @Override
  public void visit(NotEqualsTo expr) {
    helper(expr);
  }

  @Override
  public void visit(GreaterThan expr) {
    helper(expr);
  }

  @Override
  public void visit(GreaterThanEquals expr) {
    helper(expr);
  }

  @Override
  public void visit(MinorThan expr) {
    helper(expr);
  }

  @Override
  public void visit(MinorThanEquals expr) {
    helper(expr);
  }

  /**
   *  Given a expression,
   *  eval its both side:
   *                    one of them not Column, but LongVal, then must be select-cond
   *                    both are column:
   *                                      case1:  both are alias
   *                                                select / join
   *                                      case2:  both are table
   *                                                select / join
   *  classify it to corresponding map
   * @param expr expr
   */
  private void helper(Expression expr) {
    Expression leftExpression = ((BinaryExpression) expr).getLeftExpression();
    Expression rightExpression = ((BinaryExpression) expr).getRightExpression();
    // One of them not Column , then must be selection of the other Column
    if (!(leftExpression instanceof Column) || !(rightExpression instanceof Column)) {
      if (!(leftExpression instanceof Column)) {
        Column rightColumn = (Column) rightExpression;
        putInSelection(rightColumn, expr);
      } else {
        Column leftColumn = (Column) leftExpression;
        putInSelection(leftColumn, expr);
      }
    } else {
      // both are Column,  can be selection or join
      Column leftColumn = (Column) leftExpression;
      Column rightColumn = (Column) rightExpression;
      JoinOrSelect(leftColumn, rightColumn, expr);
    }
  }

  private void putInSelection(Column column, Expression expr) {
    String aliasOrName = column.getTable().getName();
    String tableName = null;
    // if is alias, cast to its original name
    // else it is  just the exact original name
    if (AliasTool.isAlias(aliasOrName, aliasMap)) {
      String alias = aliasOrName;
      tableName = aliasMap.get(alias).getName();
    } else {
      tableName = aliasOrName;
    }
    if (!selectCond.containsKey(tableName)) {
      List<Expression> lst = new ArrayList<>();
      selectCond.put(tableName, lst);
    }
    selectCond.get(tableName).add(expr);
  }

  private void JoinOrSelect(Column leftCol, Column rightCol, Expression expr) {
    String leftAliasOrName = leftCol.getTable().getName();
    String rightAliasOrName = rightCol.getTable().getName();
    // both are alias  can be join or selection
    if (AliasTool.isAlias(leftAliasOrName, aliasMap)
        && AliasTool.isAlias(rightAliasOrName, aliasMap)) {
//      String leftAlias = AliasTool.getAlias(leftCol, aliasMap);
//      String rightAlias = AliasTool.getAlias(rightCol, aliasMap);
      String leftAlias = leftAliasOrName;
      String rightAlias = rightAliasOrName;
      String leftTableName = aliasMap.get(leftAlias).getName();
      String rightTableName = aliasMap.get(rightAlias).getName();
      // alias equals,   select-cond
      if (leftAlias.equalsIgnoreCase(rightAlias)) {
        if (!selectCond.containsKey(leftTableName)) {
          List<Expression> lst = new ArrayList<>();
          selectCond.put(leftTableName, lst);
        }
        selectCond.get(leftTableName).add(expr);
      } else {
        // alias not equal,   join-cond
        String combineKey = leftTableName + "," + rightTableName;
        if (!joinCond.containsKey(combineKey)) {
          List<Expression> lst = new ArrayList<>();
          joinCond.put(combineKey, lst);
        }
        joinCond.get(combineKey).add(expr);
      }
    } else {
      // both are not aliases, can be selection or join
      String leftTableName = leftAliasOrName;
      String rightTableName = rightAliasOrName;
      // name equals,  select-cond
      if (leftTableName.equalsIgnoreCase(rightTableName)) {
        if (!selectCond.containsKey(leftTableName)) {
          List<Expression> lst = new ArrayList<>();
          selectCond.put(leftTableName, lst);
        }
        selectCond.get(leftTableName).add(expr);
      } else {
        // name not equals, join-cond
        String combineKey = leftTableName + "," + rightTableName;
        if (!joinCond.containsKey(combineKey)) {
          List<Expression> lst = new ArrayList<>();
          joinCond.put(combineKey, lst);
        }
        joinCond.get(combineKey).add(expr);
      }
    }
  }
}
