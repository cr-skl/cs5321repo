package PhysicalOperator;

import common.Tuple;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import visitor.ExpVisitor;

public class SelectOperator extends Operator {

  private Operator child;
  private Expression exp;
  private Map<String, Table> aliasMap;

  /**
   * child setter called on .visit()
   *
   * @param child c
   */
  public void setChild(Operator child) {
    this.child = child;
  }

  public SelectOperator(Expression exp, Map<String, Table> aliasMap) {
    this.exp = exp;
    this.aliasMap = aliasMap;
  }

  /** Resets cursor on the operator to the beginning */
  @Override
  public void reset() {
    child.reset();
  }

  /**
   * Get next tuple from child scan operator See if the tuple satisfies the expression condition
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    Tuple nextTuple;
    while (true) {
      nextTuple = child.getNextTuple();
      // if at the end, return null
      if (nextTuple == null) return null;
      ExpVisitor ev = new ExpVisitor(nextTuple, aliasMap);
      exp.accept(ev);
      // if finding the required tuple, return this tuple
      if (ev.getResult()) {
        break;
      }
    }
    return nextTuple;
  }
}
