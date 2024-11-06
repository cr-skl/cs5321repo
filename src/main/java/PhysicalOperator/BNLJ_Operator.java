package PhysicalOperator;

import common.entity.Tuple;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import visitor.ConditionVisitor;

public class BNLJ_Operator extends TNLJ_Operator {

  private List<Tuple> leftBlock;
  private Iterator<Tuple> leftBlockIterator;
  private Tuple rightTuple;
  private Tuple leftTuple;
  private int maxTuplesInBlock;
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;
  private boolean initial = false;

  /**
   * init
   *
   * @param rightChild rc
   * @param eval e
   */
  public BNLJ_Operator(
      PhysicalOperator leftChild, PhysicalOperator rightChild, Expression eval, int joinBufPages) {
    super(leftChild, rightChild, eval);
    int tupleSize = INT_SIZE * leftChild.getOutputSchema().size();
    int tuplesPerPage = (PAGE_SIZE - 2 * INT_SIZE) / tupleSize;
    this.maxTuplesInBlock = joinBufPages * tuplesPerPage;
    this.leftBlock = new ArrayList<>();
    this.leftBlockIterator = null;
    this.rightTuple = null;
  }

  @Override
  public Tuple getNextTuple() {
    while (true) {
      /*********Step1: Prepare the rightTuple *******/
      // init or readThrough the rightTable, reset it
      // meanwhile, update the leftBlock
      if (rightTuple == null) {
        rightChild.reset();
        rightTuple = rightChild.getNextTuple();
        // TODO: check if the empty constraint is needed
        // if (rightTuple == null)
        //    return null;
        /****** Step2: Update the leftBlock ************/
        leftBlock = fetchNextLeftBlock(leftChild.getOutputSchema().size());
        // All leftBlocks already iterated, match over
        if (leftBlock.isEmpty()) {
          return null;
        }
        leftBlockIterator = leftBlock.iterator();
      }
      /******* Step3: scan through the leftBlock  ************/
      while (leftBlockIterator.hasNext()) {
        leftTuple = leftBlockIterator.next();
        if (eval == null || evalMatches(leftTuple, rightTuple)) {
          ArrayList<Integer> leftArray = leftTuple.getAllElements();
          ArrayList<Integer> rightArray = rightTuple.getAllElements();
          ArrayList<Integer> res = new ArrayList<>();
          res.addAll(leftArray);
          res.addAll(rightArray);
          return new Tuple(res);
        }
      }
      rightTuple = rightChild.getNextTuple();
    }
  }

  private List<Tuple> fetchNextLeftBlock(int attrs) {

    List<Tuple> block = new ArrayList<>();
    Tuple nextTuple = leftChild.getNextTuple();
    while (nextTuple != null && block.size() < maxTuplesInBlock) {
      block.add(nextTuple);
      nextTuple = leftChild.getNextTuple();
    }
    return block;
  }

  /**
   * Return if the given tuple left and right satisfy the eval expression
   *
   * @param left tuple
   * @param right tuple
   * @return boolean res
   */
  private boolean evalMatches(Tuple left, Tuple right) {
    ConditionVisitor expVisitor =
        new ConditionVisitor(
            leftChild.getOutputSchema(), rightChild.getOutputSchema(), left, right);
    eval.accept(expVisitor);
    return expVisitor.getResult();
  }

  /**
   * set the child, called on .visit()
   *
   * @param leftChild lc
   */
  public void setLeftChild(PhysicalOperator leftChild) {
    this.leftChild = leftChild;
  }

  /**
   * get left Child
   *
   * @return c
   */
  public PhysicalOperator getLeftChild() {
    return leftChild;
  }

  /**
   * get right Child
   *
   * @return c
   */
  public PhysicalOperator getRightChild() {
    return rightChild;
  }
}
