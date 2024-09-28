package visitor;

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
import net.sf.jsqlparser.schema.Table;
import tools.AliasTool;

public class AliasExpVisitor extends ExpressionVisitorAdapter {
  private Map<String, Table> aliasMap;

  public AliasExpVisitor(Map<String, Table> aliasMap) {
    this.aliasMap = aliasMap;
  }

  /**
   * Change the Column from S.A into Sailor.A, change the attribute of Column of the expression
   *
   * @param expr e
   */
  private void helper(Expression expr) {
    Expression leftExpression = ((BinaryExpression) expr).getLeftExpression();
    Expression rightExpression = ((BinaryExpression) expr).getRightExpression();
    AliasTool.aliasToName(leftExpression, aliasMap);
    AliasTool.aliasToName(rightExpression, aliasMap);
  }

  /**
   * change both sides of expr (if Column has alias) into Column with tableName
   *
   * @param expr e
   */
  @Override
  public void visit(EqualsTo expr) {
    helper(expr);
  }

  /**
   * change both sides of expr (if Column has alias) into Column with tableName
   *
   * @param expr e
   */
  @Override
  public void visit(NotEqualsTo expr) {
    helper(expr);
  }

  /**
   * change both sides of expr (if Column has alias) into Column with tableName
   *
   * @param expr e
   */
  @Override
  public void visit(GreaterThan expr) {
    helper(expr);
  }

  /**
   * change both sides of expr (if Column has alias) into Column with tableName
   *
   * @param expr e
   */
  @Override
  public void visit(GreaterThanEquals expr) {
    helper(expr);
  }

  /**
   * change both sides of expr (if Column has alias) into Column with tableName
   *
   * @param expr e
   */
  @Override
  public void visit(MinorThan expr) {
    helper(expr);
  }

  /**
   * change both sides of expr (if Column has alias) into Column with tableName
   *
   * @param expr e
   */
  @Override
  public void visit(MinorThanEquals expr) {
    helper(expr);
  }
}
