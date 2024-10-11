package LogicalOperator;

import net.sf.jsqlparser.expression.Expression;

public class LogicalJoinOp extends LogicalOperator {
  private LogicalOperator leftChild;
  private LogicalOperator rightChild;
  private Expression eval;

  /**
   * set the child, called on .visit()
   *
   * @param leftChild lc
   */
  public void setLeftChild(LogicalOperator leftChild) {
    this.leftChild = leftChild;
  }

  /**
   * get left Child
   *
   * @return c
   */
  public LogicalOperator getLeftChild() {
    return leftChild;
  }

  /**
   * get right Child
   *
   * @return c
   */
  public LogicalOperator getRightChild() {
    return rightChild;
  }

  public Expression getExpression() {
    return eval;
  }

  /**
   * init
   *
   * @param rightChild rc
   * @param eval e
   */
  public LogicalJoinOp(LogicalOperator rightChild, Expression eval) {
    this.rightChild = rightChild;
    this.eval = eval;
  }
}
