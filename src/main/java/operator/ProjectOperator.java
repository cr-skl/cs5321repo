package operator;

import common.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import tools.AliasTool;

public class ProjectOperator extends Operator {
  // either a selectOperator or a scan Operator
  // depends on whether has the where clause

  // either a new tuple or the original tuple
  // depends on what in the SELECT Field
  private Operator child;
  private ArrayList<Column> requiredList;

  public ProjectOperator(List<SelectItem> selectItemList, Map<String, Table> aliasMap) {
    requiredList = new ArrayList<>();
    for (SelectItem e : selectItemList) {
      if (e instanceof AllColumns) {
        requiredList = null;
      }
      if (e instanceof SelectExpressionItem) {
        SelectExpressionItem expressionItem = (SelectExpressionItem) e;
        Column aliasCol = (Column) expressionItem.getExpression();
        // if the Column is alias, set it to tableName
          if (AliasTool.isAlias(aliasCol, aliasMap)) {
          Table table = aliasCol.getTable();
          table.setName(AliasTool.getOriginalName(aliasCol, aliasMap));
        }
        requiredList.add(aliasCol);
      }
    }
  }

  public void setChild(Operator child) {
    this.child = child;
    this.setOutputSchema(requiredList == null ? child.getOutputSchema() : requiredList);
  }

  /** Resets cursor on the operator to the beginning */
  @Override
  public void reset() {
    child.reset();
  }

  /**
   * Get next tuple from operator, with the selected Columns
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    Tuple nextTuple = null;
    ArrayList<Integer> temp = null;
    ArrayList<Column> tupleSchema = child.getOutputSchema();
    Map<String, Integer> tupleMap = new HashMap<>();

    while (true) {
      nextTuple = child.getNextTuple();
      // towards the end
      if (nextTuple == null) return null;
      ArrayList<Integer> tupleVal = nextTuple.getAllElements();
      for (int i = 0; i < tupleSchema.size(); i++) {
        tupleMap.put(tupleSchema.get(i).toString(), tupleVal.get(i));
      }
      temp = new ArrayList<>();
      // requirement is AllColumns
      if (requiredList == null) {
        temp = new ArrayList<>(tupleVal);
        break;
      } else {
        // requirement is List<Column>
        // since output is on accord with the order of requiredList
        // requiredList is the one to be iterated
        for (int i = 0; i < requiredList.size(); i++) {
          String cName = requiredList.get(i).toString();
          temp.add(tupleMap.get(cName.toString()));
        }
        break;
      }
    }
    return new Tuple(temp);
  }
}
