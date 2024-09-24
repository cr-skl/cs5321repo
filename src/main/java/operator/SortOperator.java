package operator;

import Comparator.TupleComparator;
import common.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class SortOperator extends Operator {
  private Boolean sorted;
  private Operator child;
  private List<Tuple> save = new ArrayList<>();
  private List<OrderByElement> orderByElements;
  private Map<String, Table> aliasMap;
  private int pointer;

  public void setChild(Operator child) {
    this.child = child;
  }

  public SortOperator(List<OrderByElement> orderByElements, Map<String, Table> aliasMap) {
    this.sorted = false;
    this.orderByElements = orderByElements;
    this.aliasMap = aliasMap;
    this.pointer = 0;
  }

  @Override
  public void reset() {
    pointer = 0;
  }

  @Override
  public Tuple getNextTuple() {
    if (!sorted) {
      fetchAndSort();
    }
    if (pointer < save.size()) {
      return save.get(pointer++);
    } else {
      return null;
    }
  }

  private void fetchAndSort() {
    if (child == null) {
      throw new IllegalStateException("Child operator is not set");
    }
    // get all the input of child
    Tuple tuple = null;
    int i = 0;
    while ((tuple = child.getNextTuple()) != null) {
      save.add(tuple);
      i++;
    }
    // get it sorted
    Collections.sort(
        save, new TupleComparator(orderByElements, this.getOutputSchema(), this.aliasMap));
    this.sorted = true;
  }
}
