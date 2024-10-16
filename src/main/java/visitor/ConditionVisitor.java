package visitor;

import common.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

/**
 * Upon initialization, map the schema with the tuple val for both left and right evaluate the
 * expression with the val of the map p.s : names are tableName+","+colName
 */
public class ConditionVisitor extends ExpressionVisitorAdapter {
  private Map<String, Integer> leftMap;
  private Map<String, Integer> rightMap;
  private Boolean result;

  public ConditionVisitor(
      ArrayList<Column> leftSchema,
      ArrayList<Column> rightSchema,
      Tuple leftTuple,
      Tuple rightTuple) {
    leftMap = new HashMap<>();
    rightMap = new HashMap<>();
    mapSchemaToTuple(leftSchema, leftTuple, leftMap);
    mapSchemaToTuple(rightSchema, rightTuple, rightMap);
  }

  private void mapSchemaToTuple(ArrayList<Column> schema, Tuple tuple, Map<String, Integer> map) {
    ArrayList<Integer> elements = tuple.getAllElements();
    for (int i = 0; i < elements.size(); i++) {
      String tableName = schema.get(i).getTable().getName();
      String columnName = schema.get(i).getColumnName();
      map.put(tableName + "," + columnName, elements.get(i));
    }
  }

  private String getKey(Expression expr) {
    if (expr instanceof Column) {
      Column col = (Column) expr;
      return col.getTable().getName() + "," + col.getColumnName();
    }
    return "";
  }

  public void visit(AndExpression expr) {

    boolean leftResult = false;
    boolean rightResult = false;

    expr.getLeftExpression().accept(this);
    leftResult = this.result;

    expr.getRightExpression().accept(this);
    rightResult = this.result;

    this.result = leftResult && rightResult;
  }

  /**
   * get the final result
   *
   * @return r
   */
  public Boolean getResult() {
    return result;
  }

  /**
   * eval the expr
   *
   * @param expr e
   */
  @Override
  public void visit(EqualsTo expr) {
    String leftKey = getKey(expr.getLeftExpression());
    String rightKey = getKey(expr.getRightExpression());
    if (leftMap.containsKey(leftKey) && rightMap.containsKey(rightKey)) {
      result = leftMap.get(leftKey).equals(rightMap.get(rightKey));
      //      result = leftMap.get(leftKey) == rightMap.get(rightKey);
    } else if (rightMap.containsKey(leftKey) && leftMap.containsKey(rightKey)) {
      result = rightMap.get(leftKey).equals(leftMap.get(rightKey));
      //      result = rightMap.get(leftKey) == leftMap.get(rightKey);
    }
  }

  /**
   * eval the expr
   *
   * @param expr e
   */
  @Override
  public void visit(NotEqualsTo expr) {
    String leftKey = getKey(expr.getLeftExpression());
    String rightKey = getKey(expr.getRightExpression());
    if (leftMap.containsKey(leftKey)) result = leftMap.get(leftKey) != rightMap.get(rightKey);
    else result = rightMap.get(leftKey) != leftMap.get(rightKey);
  }

  /**
   * eval the expr
   *
   * @param expr e
   */
  @Override
  public void visit(GreaterThan expr) {
    String leftKey = getKey(expr.getLeftExpression());
    String rightKey = getKey(expr.getRightExpression());
    if (leftMap.containsKey(leftKey)) result = leftMap.get(leftKey) > rightMap.get(rightKey);
    else result = rightMap.get(leftKey) > leftMap.get(rightKey);
  }

  /**
   * eval the expr
   *
   * @param expr e
   */
  @Override
  public void visit(GreaterThanEquals expr) {
    String leftKey = getKey(expr.getLeftExpression());
    String rightKey = getKey(expr.getRightExpression());
    if (leftMap.containsKey(leftKey)) result = leftMap.get(leftKey) >= rightMap.get(rightKey);
    else result = rightMap.get(leftKey) >= leftMap.get(rightKey);
  }

  /**
   * eval the expr
   *
   * @param expr e
   */
  @Override
  public void visit(MinorThan expr) {
    String leftKey = getKey(expr.getLeftExpression());
    String rightKey = getKey(expr.getRightExpression());
    if (leftMap.containsKey(leftKey)) result = leftMap.get(leftKey) < rightMap.get(rightKey);
    else result = rightMap.get(leftKey) < leftMap.get(rightKey);
  }

  /**
   * eval the expr
   *
   * @param expr e
   */
  @Override
  public void visit(MinorThanEquals expr) {
    String leftKey = getKey(expr.getLeftExpression());
    String rightKey = getKey(expr.getRightExpression());
    if (leftMap.containsKey(leftKey)) result = leftMap.get(leftKey) <= rightMap.get(rightKey);
    else result = rightMap.get(leftKey) <= leftMap.get(rightKey);
  }
}
