package LogicalOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import visitor.PhysicalPlanBuilder;

public class LogicalProjectOp extends LogicalOperator {
  private List<SelectItem> selectItemList;
  private ArrayList<Column> requiredList;

  public List<SelectItem> getSelectItemList() {
    return selectItemList;
  }

  public void setChild(LogicalOperator child) {
    this.child = child;
    setOutputSchema(requiredList == null ? child.getOutputSchema() : requiredList);
  }

  @Override
  public void accept(PhysicalPlanBuilder visitor, Map<String, Table> aliasMap) {
    visitor.visit(this, aliasMap);
  }

  public LogicalProjectOp(List<SelectItem> selectItemList, Map<String, Table> aliasMap) {
    this.selectItemList = selectItemList;
    requiredList = new ArrayList<>();
    for (SelectItem e : selectItemList) {
      if (e instanceof AllColumns) {
        requiredList = null;
      }
      if (e instanceof SelectExpressionItem) {
        SelectExpressionItem expressionItem = (SelectExpressionItem) e;
        Column aliasCol = (Column) expressionItem.getExpression();
        requiredList.add(aliasCol);
      }
    }
  }
}
