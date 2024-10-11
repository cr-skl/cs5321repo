package PhysicalOperator;

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

  /**
   * set the child, called on .visit()
   *
   * @param leftChild lc
   */
  public void setLeftChild(Operator leftChild) {
    this.leftChild = leftChild;
  }

  /**
   * get left Child
   *
   * @return c
   */
  public Operator getLeftChild() {
    return leftChild;
  }

  /**
   * get right Child
   *
   * @return c
   */
  public Operator getRightChild() {
    return rightChild;
  }

  /**
   * init
   *
   * @param rightChild rc
   * @param eval e
   */
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
  //  public Tuple getNextTuple() {
  //    // initialization, both are null, set left to the first
  //    if (leftNextTuple == null && rightNextTuple == null) {
  //      leftNextTuple = leftChild.getNextTuple();
  //    }
  //    // get the next right (not null)
  //    // if next right is null, update the left (if left is null then join ends) and reset
  // rightTable,
  //    // then get the next right
  //    rightNextTuple = rightChild.getNextTuple();
  //    if (rightNextTuple == null) {
  //      rightChild.reset();
  //      leftNextTuple = leftChild.getNextTuple();
  //      if (leftNextTuple == null) return null;
  //      rightNextTuple = rightChild.getNextTuple();
  //    }
  //    if (eval == null || evalMatches(leftNextTuple, rightNextTuple)) {
  //      ArrayList<Integer> leftArray = leftNextTuple.getAllElements();
  //      ArrayList<Integer> rightArray = rightNextTuple.getAllElements();
  //      ArrayList<Integer> res = new ArrayList<>();
  //      res.addAll(leftArray);
  //      res.addAll(rightArray);
  //      return new Tuple(res);
  //    }
  //    // the given left and right don't satisfy, get the next as result
  //    return getNextTuple();
  //  }
  public Tuple getNextTuple() {
    // 初始化时，leftNextTuple 和 rightNextTuple 都为 null，将 leftNextTuple 设置为第一个
    if (leftNextTuple == null && rightNextTuple == null) {
      leftNextTuple = leftChild.getNextTuple();
      if (leftNextTuple == null) {
        // 如果 leftChild 已经没有任何元素，则直接返回 null，表示 join 完成
        return null;
      }
    }
    int cnt = 0;
    while (true) {
      // 尝试获取下一个 right 元组
      cnt++;
      rightNextTuple = rightChild.getNextTuple();

      // 如果 right 元组为空，意味着需要重置 right 并且获取下一个 left 元组
      if (rightNextTuple == null) {
        rightChild.reset();
        leftNextTuple = leftChild.getNextTuple();

        // 如果 left 元组也为空，说明所有的组合都已经处理完毕
        if (leftNextTuple == null) {
          return null;
        }

        // 获取重置后的 right 元组
        rightNextTuple = rightChild.getNextTuple();
        if (rightNextTuple == null) {
          // 如果 right 元组仍然为空，继续下一个循环
          continue;
        }
      }

      // 如果 eval 为空或者当前的 left 和 right 匹配
      if (eval == null || evalMatches(leftNextTuple, rightNextTuple)) {
        ArrayList<Integer> leftArray = leftNextTuple.getAllElements();
        ArrayList<Integer> rightArray = rightNextTuple.getAllElements();
        ArrayList<Integer> res = new ArrayList<>();
        res.addAll(leftArray);
        res.addAll(rightArray);
        return new Tuple(res);
      }

      // 如果不满足匹配条件，循环继续，获取下一个 right 元组
    }
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
}
