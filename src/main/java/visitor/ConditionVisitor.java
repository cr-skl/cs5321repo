package visitor;

import common.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

/**
 *  Upon initialization, map the schema with the tuple val for both left and right
 *  evaluate the expression with the val of the map
 *  p.s :
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
    ArrayList<Integer> leftElems = leftTuple.getAllElements();
    ArrayList<Integer> rightElems = rightTuple.getAllElements();
    leftMap = new HashMap<>();
    for (int i = 0; i < leftElems.size(); i++) {
      String tName = leftSchema.get(i).getTable().getName();
      String cName = leftSchema.get(i).getColumnName();
      leftMap.put(tName + "," + cName, leftElems.get(i));
    }
    rightMap = new HashMap<>();
    for (int i = 0; i < rightElems.size(); i++) {
      String tName = rightSchema.get(i).getTable().getName();
      String cName = rightSchema.get(i).getColumnName();
      rightMap.put(tName + "," + cName, rightElems.get(i));
    }
  }

  public Boolean getResult() {
    return result;
  }

  @Override
  public void visit(EqualsTo expr) {
    Column leftCol = (Column) expr.getLeftExpression();
    Column rightCol = (Column) expr.getRightExpression();
    String leftKey = leftCol.getTable().getName() + "," + leftCol.getColumnName();
    String rightKey = rightCol.getTable().getName() + "," + rightCol.getColumnName();
    if (leftMap.containsKey(leftKey))
      result = leftMap.get(leftKey) == rightMap.get(rightKey) ? true : false;
    else result = rightMap.get(leftKey) == leftMap.get(rightKey) ? true : false;
  }

  @Override
  public void visit(NotEqualsTo expr) {
    Column leftCol = (Column) expr.getLeftExpression();
    Column rightCol = (Column) expr.getRightExpression();
    String leftKey = leftCol.getTable().getName() + "," + leftCol.getColumnName();
    String rightKey = rightCol.getTable().getName() + "," + rightCol.getColumnName();
    if (leftMap.containsKey(leftKey))
      result = leftMap.get(leftKey) != rightMap.get(rightKey) ? true : false;
    else result = rightMap.get(leftKey) != leftMap.get(rightKey) ? true : false;
  }

  @Override
  public void visit(GreaterThan expr) {
    Column leftCol = (Column) expr.getLeftExpression();
    Column rightCol = (Column) expr.getRightExpression();
    String leftKey = leftCol.getTable().getName() + "," + leftCol.getColumnName();
    String rightKey = rightCol.getTable().getName() + "," + rightCol.getColumnName();
    if (leftMap.containsKey(leftKey))
      result = leftMap.get(leftKey) > rightMap.get(rightKey) ? true : false;
    else result = rightMap.get(leftKey) > leftMap.get(rightKey) ? true : false;
  }

  @Override
  public void visit(GreaterThanEquals expr) {
    Column leftCol = (Column) expr.getLeftExpression();
    Column rightCol = (Column) expr.getRightExpression();
    String leftKey = leftCol.getTable().getName() + "," + leftCol.getColumnName();
    String rightKey = rightCol.getTable().getName() + "," + rightCol.getColumnName();
    if (leftMap.containsKey(leftKey))
      result = leftMap.get(leftKey) >= rightMap.get(rightKey) ? true : false;
    else result = rightMap.get(leftKey) >= leftMap.get(rightKey) ? true : false;
  }

  @Override
  public void visit(MinorThan expr) {
    Column leftCol = (Column) expr.getLeftExpression();
    Column rightCol = (Column) expr.getRightExpression();
    String leftKey = leftCol.getTable().getName() + "," + leftCol.getColumnName();
    String rightKey = rightCol.getTable().getName() + "," + rightCol.getColumnName();
    if (leftMap.containsKey(leftKey))
      result = leftMap.get(leftKey) < rightMap.get(rightKey) ? true : false;
    else result = rightMap.get(leftKey) < leftMap.get(rightKey) ? true : false;
  }

  @Override
  public void visit(MinorThanEquals expr) {
    Column leftCol = (Column) expr.getLeftExpression();
    Column rightCol = (Column) expr.getRightExpression();
    String leftKey = leftCol.getTable().getName() + "," + leftCol.getColumnName();
    String rightKey = rightCol.getTable().getName() + "," + rightCol.getColumnName();
    if (leftMap.containsKey(leftKey))
      result = leftMap.get(leftKey) <= rightMap.get(rightKey) ? true : false;
    else result = rightMap.get(leftKey) <= leftMap.get(rightKey) ? true : false;
  }
}
