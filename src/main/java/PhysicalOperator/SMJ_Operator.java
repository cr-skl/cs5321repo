package PhysicalOperator;

import common.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;

public class SMJ_Operator extends JoinOperator {
  public SMJ_Operator(PhysicalOperator leftChild, PhysicalOperator rightChild, Expression eval) {
    super(leftChild, rightChild, eval);
  }

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    return null;
  }
}
