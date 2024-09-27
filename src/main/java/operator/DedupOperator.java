package operator;

import common.Tuple;

public class DedupOperator extends Operator {
  private Operator child;
  private Tuple prevTuple;

  public DedupOperator() {
    prevTuple = null;
  }

  /**
   * set the child, called on .visit()
   * @param child c
   */
  public void setChild(Operator child) {
    this.child = child;
  }

  /**
   * Reset its pointer to null, and the child pointer
   */
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
      // init or not the same
      // move prevTuple to nextTuple and return it
      if (prevTuple == null || !nextTuple.equals(prevTuple)) {
        prevTuple = nextTuple;
        return prevTuple;
      }
    }
    return null;
  }

}
