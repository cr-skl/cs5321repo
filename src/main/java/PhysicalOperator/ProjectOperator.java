package PhysicalOperator;

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
import tools.alias.AliasTool;

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
        requiredList.add(aliasCol);
      }
    }
  }

  /**
   * set its child, called on .visit()
   *
   * @param child c
   */
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
      /**
       * imp1 use Column.toString as Key, not good for self-join since Column.toString() =>
       * tableName.columnName which cannot differentiate different tableAlias with same table entity
       */
      //      for (int i = 0; i < tupleSchema.size(); i++) {
      //        tupleMap.put(tupleSchema.get(i).toString(), tupleVal.get(i));
      //      }
      for (int i = 0; i < tupleSchema.size(); i++) {
        Column col = tupleSchema.get(i);
        String key = AliasTool.getColumnKey(col);
        tupleMap.put(key, tupleVal.get(i));
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
          Column col = requiredList.get(i);
          String key = AliasTool.getColumnKey(col);
          temp.add(tupleMap.get(key));
        }
        break;
      }
    }
    return new Tuple(temp);
  }
}
