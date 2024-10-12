package LogicalOperator;

import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class LogicalSortOp extends LogicalOperator {
  private List<OrderByElement> orderByElements;

  public List<OrderByElement> getOrderByElements() {
    return orderByElements;
  }

  public LogicalSortOp(List<OrderByElement> orderByElements, Map<String, Table> aliasMap) {
    this.orderByElements = orderByElements;
  }
}
