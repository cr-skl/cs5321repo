package PhysicalOperator;

import common.entity.Tuple;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

public class BNLJ_Operator extends JoinOperator {

  private List<Tuple> leftBlock;
  private int leftBlockPtr;

  private int tuplesPerBlock;
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;

  /**
   * init
   *
   * @param rightChild rc
   * @param eval e
   */
  public BNLJ_Operator(
      PhysicalOperator leftChild, PhysicalOperator rightChild, Expression eval, int joinBufPages) {
    super(leftChild, rightChild, eval);
    int attrs = leftChild.getOutputSchema().size();
    int tupleSize = INT_SIZE * attrs;
    int tuplesPerPage = (PAGE_SIZE - 2 * INT_SIZE) / tupleSize;
    this.tuplesPerBlock = joinBufPages * tuplesPerPage;
    this.leftBlock = new ArrayList<>();
    this.leftBlockPtr = 0;
  }

  /**
   * for i meaning the Block of leftTable for j meaning the row of rightTable for k meaning the row
   * of leftCurrentBlock
   *
   * @return
   */
  @Override
  public Tuple getNextTuple() {
    // init()
    if (!initial) {
      initial = true;
      // k
      leftBlockPtr = 0;
      // j
      rightTuple = rightChild.getNextTuple();
      // special case: rightTable is Empty
      if (rightTuple == null) return null;
      // i
      leftBlock = fetchNextLeftBlock();
      // special case: leftTable is Empty
      if (leftBlock.isEmpty()) return null;
    }

    while (true) {
      // k outOfBound
      if (leftBlockPtr >= leftBlock.size()) {
        // j++
        rightTuple = rightChild.getNextTuple();
        // j outOfBound
        if (rightTuple == null) {
          // i++
          leftBlock = fetchNextLeftBlock();
          // i outOfBound
          if (leftBlock.isEmpty()) {
            return null;
          }
          // j = 0
          rightChild.reset();
          rightTuple = rightChild.getNextTuple();
        }
        // k = 0
        leftBlockPtr = 0;
      }
      // make sure left and right both not null
      // k++
      leftTuple = leftBlock.get(leftBlockPtr);
      leftBlockPtr++;
      if (eval == null || evalMatches(leftTuple, rightTuple)) {
        ArrayList<Integer> leftArray = leftTuple.getAllElements();
        ArrayList<Integer> rightArray = rightTuple.getAllElements();
        ArrayList<Integer> res = new ArrayList<>();
        res.addAll(leftArray);
        res.addAll(rightArray);
        return new Tuple(res);
      }
    }
  }

  //  private List<Tuple> fetchNextLeftBlock() {
  //    List<Tuple> block = new ArrayList<>();
  //    Tuple nextTuple = leftChild.getNextTuple();
  //    while (nextTuple != null && block.size() < tuplesPerBlock) {
  //      block.add(nextTuple);
  //      nextTuple = leftChild.getNextTuple();
  //    }
  //    return block;
  //  }
  private List<Tuple> fetchNextLeftBlock() {
    List<Tuple> block = new ArrayList<>();
    Tuple nextTuple = leftChild.getNextTuple();
    while (nextTuple != null) {
      block.add(nextTuple);
      if (block.size() >= tuplesPerBlock) {
        break;
      }
      nextTuple = leftChild.getNextTuple();
    }
    return block;
  }
}
