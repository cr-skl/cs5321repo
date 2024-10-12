package LogicalOperator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

import java.util.Map;

public class LogicalSelectOp extends LogicalOperator {
  private Expression exp;

  public Expression getExpression() {
    return exp;
  }

  public LogicalSelectOp(Expression exp, Map<String, Table> aliasMap) {
    this.exp = exp;
  }
}
