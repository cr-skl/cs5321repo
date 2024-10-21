package LogicalOperator;

import java.util.Map;
import net.sf.jsqlparser.schema.Table;
import visitor.PhysicalPlanBuilder;

public class LogicalDedupOp extends LogicalOperator {
  @Override
  public void accept(PhysicalPlanBuilder visitor, Map<String, Table> aliasMap) {
    visitor.visit(this, aliasMap);
  }
  // this class is just here to be a placeholder in the logical query plan
}
