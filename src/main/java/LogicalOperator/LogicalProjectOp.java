package LogicalOperator;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class LogicalProjectOp extends LogicalOperator {
  private List<SelectItem> selectItemList;
  private ArrayList<Column> requiredList;

  public List<SelectItem> getSelectItemList() {
    return selectItemList;
  }

  public void setChild(LogicalOperator child){
    this.child = child;
    setOutputSchema(requiredList == null ? child.getOutputSchema() : requiredList);
  }

  public LogicalProjectOp(List<SelectItem> selectItemList) {
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
