package PhysicalOperator;

import common.entity.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;

public class TNLJ_Operator extends JoinOperator {

  /**
   * init
   *
   * @param rightChild rc
   * @param eval e
   */
  public TNLJ_Operator(PhysicalOperator leftChild, PhysicalOperator rightChild, Expression eval) {
    super(leftChild, rightChild, eval);
  }

  /**
   * Get next tuple from operator
   *
   * <p>for i in leftTable for j in rightTable if (match)
   *
   * @return next Tuple, or null if we are at the end
   */
  public Tuple getNextTuple() {
    // init()
    if (!initial) {
      initial = true;
      // j
      rightTuple = rightChild.getNextTuple();
      // table empty
      if (rightTuple == null) {
        return null;
      }
      // i
      leftTuple = leftChild.getNextTuple();
      // table empty
      if (leftTuple == null) {
        return null;
      }
    }

    // the real getNextTuple()
    while (true) {
      // j outOfBound
      if (rightTuple == null) {
        // i++
        leftTuple = leftChild.getNextTuple();
        // i outOfBound
        if (leftTuple == null) {
          return null;
        }
        // j = 0
        rightChild.reset();
        rightTuple = rightChild.getNextTuple();
      }

      // make sure left and right are not null
      if (eval == null || evalMatches(leftTuple, rightTuple)) {
        ArrayList<Integer> leftArray = leftTuple.getAllElements();
        ArrayList<Integer> rightArray = rightTuple.getAllElements();
        ArrayList<Integer> res = new ArrayList<>();
        res.addAll(leftArray);
        res.addAll(rightArray);

        rightTuple = rightChild.getNextTuple();
        return new Tuple(res);
      } else {
        rightTuple = rightChild.getNextTuple();
      }
    }
  }
}
/************************************************************************************/
