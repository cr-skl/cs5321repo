package LogicalOperator;

import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

public class LogicalSelectOp extends LogicalOperator {
  private Expression exp;

  public Expression getExpression() {
    return exp;
  }

  public LogicalSelectOp(Expression exp, Map<String, Table> aliasMap) {
    this.exp = exp;
  }
}
