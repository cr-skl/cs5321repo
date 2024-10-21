package visitor;

import PhysicalOperator.ProjectOperator;
import PhysicalOperator.ScanOperator;
import PhysicalOperator.SelectOperator;

interface OpVisitor {
  void visit(ScanOperator operator);

  void visit(SelectOperator operator);

  void visit(ProjectOperator operator);
}
