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

public class BuildOpVisitor implements OpVisitor {
  private Operator root;

  public BuildOpVisitor() {
    this.root = null;
  }

  public Operator getRoot() {
    return root;
  }

  @Override
  public void visit(ScanOperator operator) {
    if (root == null) root = operator;
  }

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

  @Override
  public void visit(ProjectOperator operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      root = operator;
    }
  }

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

  public void visit(SortOperator operator) {
    if (root == null) {
      root = operator;
    } else {
      operator.setChild(root);
      operator.setOutputSchema(root.getOutputSchema());
      root = operator;
    }
  }

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
