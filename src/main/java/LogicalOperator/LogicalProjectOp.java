package LogicalOperator;

import java.util.List;

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectItem;

public class LogicalProjectOp extends LogicalOperator {
  private List<SelectItem> selectItemList;

  private boolean requireAllColumns;

  public List<SelectItem> getSelectItemList() {
    return selectItemList;
  }

  public void setChild(LogicalOperator child){
    this.child = child;
    setOutputSchema(requireAllColumns ? child.getOutputSchema() : null);
  }

  public LogicalProjectOp(List<SelectItem> itemList) {
    selectItemList = itemList;
    requireAllColumns = false;
    for (SelectItem e : selectItemList) {
      if (e instanceof AllColumns) {
        requireAllColumns = true;
        return;
      }
    }
  }
}
