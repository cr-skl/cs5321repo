package visitor;

import operator.ProjectOperator;
import operator.ScanOperator;
import operator.SelectOperator;

interface OpVisitor {
  void visit(ScanOperator operator);

  void visit(SelectOperator operator);

  void visit(ProjectOperator operator);
}
