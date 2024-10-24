package LogicalOperator;

import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import common.PhysicalPlanBuilder;

public class LogicalSelectOp extends LogicalOperator {
  private Expression exp;

  public Expression getExpression() {
    return exp;
  }

  public LogicalSelectOp(Expression exp, Map<String, Table> aliasMap) {
    this.exp = exp;
  }

  @Override
  public void accept(PhysicalPlanBuilder visitor, Map<String, Table> aliasMap) {
    visitor.visit(this, aliasMap);
  }
}
