package LogicalOperator;

import common.PhysicalPlanBuilder;
import java.util.Map;
import net.sf.jsqlparser.schema.Table;

public class LogicalDedupOp extends LogicalOperator {
  @Override
  public void accept(PhysicalPlanBuilder visitor, Map<String, Table> aliasMap) {
    visitor.visit(this, aliasMap);
  }
  // this class is just here to be a placeholder in the logical query plan
}
