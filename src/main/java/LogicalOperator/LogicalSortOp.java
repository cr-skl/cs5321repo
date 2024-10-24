package LogicalOperator;

import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import common.PhysicalPlanBuilder;

public class LogicalSortOp extends LogicalOperator {
  private List<OrderByElement> orderByElements;

  public List<OrderByElement> getOrderByElements() {
    return orderByElements;
  }

  public LogicalSortOp(List<OrderByElement> orderByElements, Map<String, Table> aliasMap) {
    this.orderByElements = orderByElements;
  }

  @Override
  public void accept(PhysicalPlanBuilder visitor, Map<String, Table> aliasMap) {
    visitor.visit(this, aliasMap);
  }
}
