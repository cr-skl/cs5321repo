package PhysicalOperator;

import common.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;
import visitor.ConditionVisitor;

public abstract class JoinOperator extends PhysicalOperator {
  protected boolean initial;
  protected Tuple leftTuple;
  protected Tuple rightTuple;
  protected PhysicalOperator leftChild;
  protected PhysicalOperator rightChild;
  protected Expression eval;

  public JoinOperator(PhysicalOperator leftChild, PhysicalOperator rightChild, Expression eval) {
    this.leftChild = leftChild;
    this.rightChild = rightChild;
    this.eval = eval;
    this.leftTuple = null;
    this.rightTuple = null;
    this.initial = false;
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

  /** Resets cursor on the operator to the beginning */
  @Override
  public void reset() {
    leftChild.reset();
    rightChild.reset();
    initial = false;
  }

  /**
   * Return if the given tuple left and right satisfy the eval expression
   *
   * @param left tuple
   * @param right tuple
   * @return boolean res
   */
  protected boolean evalMatches(Tuple left, Tuple right) {
    ConditionVisitor expVisitor =
        new ConditionVisitor(
            leftChild.getOutputSchema(), rightChild.getOutputSchema(), left, right);
    eval.accept(expVisitor);
    return expVisitor.getResult();
  }
}
