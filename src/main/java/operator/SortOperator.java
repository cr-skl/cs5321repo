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

  /**
   * set the child, called on .visit()
   *
   * @param child c
   */
  public void setChild(Operator child) {
    this.child = child;
  }

  /**
   * During initialization, no data been stored, the sign sorted marking as false
   *
   * @param orderByElements objects
   * @param aliasMap aMap
   */
  public SortOperator(List<OrderByElement> orderByElements, Map<String, Table> aliasMap) {
    this.sorted = false;
    this.orderByElements = orderByElements;
    this.aliasMap = aliasMap;
    this.pointer = 0;
  }

  /** reset pointer to 0 for the save list */
  @Override
  public void reset() {
    pointer = 0;
  }

  /**
   * After calling the first getNextTuple, try fetch and sort all results in the save list then
   * return the next of the save list
   *
   * @return the nextTuple needed to be returned (after sorting)
   */
  @Override
  public Tuple getNextTuple() {
    // only executed once
    if (!sorted) {
      fetchAndSort();
    }
    if (pointer < save.size()) {
      return save.get(pointer++);
    } else {
      return null;
    }
  }

  /**
   * Called on the first getNextTuple, generate all the result from its child's output and save them
   * in the save list, then get them sorted with specific priors
   */
  private void fetchAndSort() {
    if (child == null) {
      throw new IllegalStateException("Child operator is not set");
    }
    // get all the input of child
    Tuple tuple = null;
    while ((tuple = child.getNextTuple()) != null) {
      save.add(tuple);
    }
    // get it sorted
    Collections.sort(
        save, new TupleComparator(orderByElements, this.getOutputSchema(), this.aliasMap));
    this.sorted = true;
  }
}
