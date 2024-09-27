package operator;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import visitor.ConditionVisitor;

public class JoinOperator extends Operator {
  private Operator leftChild;
  private Operator rightChild;
  private Tuple leftNextTuple = null;
  private Tuple rightNextTuple = null;
  private Expression eval;

  public void setLeftChild(Operator leftChild) {
    this.leftChild = leftChild;
  }

  public Operator getLeftChild() {
    return leftChild;
  }

  public Operator getRightChild() {
    return rightChild;
  }

  public JoinOperator(Operator rightChild, Expression eval) {
    this.rightChild = rightChild;
    this.eval = eval;
  }

  /** Resets cursor on the operator to the beginning */
  @Override
  public void reset() {
    leftChild.reset();
    rightChild.reset();
  }

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    // initialization, both are null, set left to the first
    if (leftNextTuple == null && rightNextTuple == null) {
      leftNextTuple = leftChild.getNextTuple();
    }
    // get the next right (not null)
    // if next right is null, update the left (if left is null then join ends) and reset rightTable, then get the next right
    rightNextTuple = rightChild.getNextTuple();
    if (rightNextTuple == null) {
      rightChild.reset();
      leftNextTuple = leftChild.getNextTuple();
      if (leftNextTuple == null) return null;
      rightNextTuple = rightChild.getNextTuple();
    }
    if (eval == null || evalMatches(leftNextTuple, rightNextTuple)) {
      ArrayList<Integer> leftArray = leftNextTuple.getAllElements();
      ArrayList<Integer> rightArray = rightNextTuple.getAllElements();
      ArrayList<Integer> res = new ArrayList<>();
      res.addAll(leftArray);
      res.addAll(rightArray);
      return new Tuple(res);
    }
    // the given left and right don't satisfy, get the next as result
    return getNextTuple();
  }

  /**
   * Return if the given tuple left and right  satisfy the eval expression
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
}
