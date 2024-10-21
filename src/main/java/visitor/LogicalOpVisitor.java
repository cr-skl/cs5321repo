package visitor;

import LogicalOperator.*;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/**
 * top comment: calling the visit, will make current root to the left child of the current operator,
 * set the operator's schema based on all its child's schema then set the operator as current root
 * p.s : all the schema is expected to be only Column using real tableName, not alias
 */
public class LogicalOpVisitor {

  /** ALL LOGIC COPIED FROM THE CLASS BuildOpVisitor */
  private LogicalOperator root;

  public LogicalOpVisitor() {
    this.root = null;
  }

  /**
   * return final result
   *
   * @return r
   */
  public LogicalOperator getRoot() {
    return root;
  }

  /**
   * see top comment
   *
   * @param operator op
   */
  public void visit(LogicalScanOp operator) {
    if (root == null) root = operator;
  }

  /**
   * see top comment
   *
   * @param operator op
   */
  public void visit(LogicalSelectOp operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      operator.setOutputSchema(root.getOutputSchema());
      root = operator;
    }
  }

  /**
   * see top comment
   *
   * @param operator op
   */
  public void visit(LogicalProjectOp operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      root = operator;
    }
  }

  /**
   * see the top comment
   *
   * @param operator op
   */
  public void visit(LogicalJoinOp operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setLeftChild(root);
      ArrayList<Column> leftSchema = operator.getLeftChild().getOutputSchema();
      ArrayList<Column> rightSchema = operator.getRightChild().getOutputSchema();
      ArrayList<Column> topSchema = new ArrayList<>();
      topSchema.addAll(leftSchema);
      topSchema.addAll(rightSchema);
      operator.setOutputSchema(topSchema);
      root = operator;
    }
  }

  /**
   * see top comment
   *
   * @param operator op
   */
  public void visit(LogicalSortOp operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      operator.setOutputSchema(root.getOutputSchema());
      root = operator;
    }
  }

  /**
   * see top comment
   *
   * @param operator op
   */
  public void visit(LogicalDedupOp operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      operator.setOutputSchema(root.getOutputSchema());
      root = operator;
    }
  }
}
