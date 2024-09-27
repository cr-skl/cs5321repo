package operator;

import common.Tuple;

public class DedupOperator extends Operator {
  private Operator child;
  private Tuple prevTuple;

  /** Resets cursor on the operator to the beginning */
  @Override
  public void reset() {
    child.reset();
    prevTuple = null;
  }

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    Tuple nextTuple;
    while ((nextTuple = child.getNextTuple()) != null) {
      if (prevTuple == null || !nextTuple.equals(prevTuple)) {
        prevTuple = nextTuple;
        return nextTuple;
      }
    }
    return null;
  }
  /**
   * set the child, called on .visit()
   * @param child c
   */
  public void setChild(Operator child) {
    this.child = child;
  }
}
