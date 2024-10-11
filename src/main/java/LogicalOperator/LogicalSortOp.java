package LogicalOperator;

import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class LogicalSortOp extends LogicalOperator {
  private List<OrderByElement> orderByElements;

  public List<OrderByElement> getOrderByElements() {
    return orderByElements;
  }

  public LogicalSortOp(List<OrderByElement> orderByElements) {
    this.orderByElements = orderByElements;
  }
}
