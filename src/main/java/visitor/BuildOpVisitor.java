package visitor;

import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;
import operator.DedupOperator;
import operator.JoinOperator;
import operator.Operator;
import operator.ProjectOperator;
import operator.ScanOperator;
import operator.SelectOperator;
import operator.SortOperator;

/**
 *  top comment:
 *  calling the visit,
 *  will make current root to the left child of the current operator,
 *  set the operator's schema based on all its child's schema
 *  then set the operator as current root
 *
 *  p.s :  all the schema is expected to be only Column using real tableName, not alias
 */
public class BuildOpVisitor implements OpVisitor {
  private Operator root;

  public BuildOpVisitor() {
    this.root = null;
  }

  public Operator getRoot() {
    return root;
  }

  /**
   * see top comment
   * @param operator
   */
  @Override
  public void visit(ScanOperator operator) {
    if (root == null) root = operator;
  }

  /**
   * see top comment
   * @param operator
   */
  @Override
  public void visit(SelectOperator operator) {
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
   * @param operator
   */
  @Override
  public void visit(ProjectOperator operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      root = operator;
    }
  }

  /**
   * see the top comment
   * @param operator
   */
  public void visit(JoinOperator operator) {
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
   * @param operator
   */
  public void visit(SortOperator operator) {
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
   * @param operator
   */
  public void visit(DedupOperator operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      operator.setOutputSchema(root.getOutputSchema());
      root = operator;
    }
  }
}
