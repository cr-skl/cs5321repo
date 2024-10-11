package LogicalOperator;

import net.sf.jsqlparser.expression.Expression;

public class LogicalSelectOp extends LogicalOperator {
  private Expression exp;

  public Expression getExpression() {
    return exp;
  }

  public LogicalSelectOp(Expression exp) {
    this.exp = exp;
  }
}
