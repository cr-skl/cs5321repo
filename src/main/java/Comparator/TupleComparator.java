package Comparator;

import common.Tuple;
import java.util.*;
import java.util.Comparator;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class TupleComparator implements Comparator<Tuple> {
  private Map<String, Integer> indexMap;
  private Map<String, Table> aliasMap;

  private Map<String, Boolean> orderAscMap;
  private List<Column> orderColumns;
  private List<Column> remainingColumns;
  private ArrayList<Column> schema;

  /**
   * Given the Priors of orderByElements, specialize its comparator for the Tuple For each column in
   * schema, decide it is in orderByElements or not generate the orderColumns, orderAscMap,
   * remainingColumns which will be of great use in compare() method
   *
   * @param orderByElements elements that are metrics for sorting
   * @param schema schema is the output schema of all these tuples, which helps mapping Column into
   *     Tuple[i]
   * @param aliasMap aliasMap for dealing alias
   */
  public TupleComparator(
      List<OrderByElement> orderByElements, ArrayList<Column> schema, Map<String, Table> aliasMap) {
    this.indexMap = new HashMap<>();
    this.aliasMap = aliasMap;
    this.schema = schema;
    this.orderAscMap = new HashMap<>();
    this.orderColumns = new ArrayList<>();
    this.remainingColumns = new ArrayList<>();
    // record order columns
    Set<String> usedColumns = new HashSet<>();

    // by OrderElements
    for (OrderByElement orderByElement : orderByElements) {
      Column target = (Column) orderByElement.getExpression();

      // find corresponding Column in schema
      for (int i = 0; i < schema.size(); i++) {
        Column curColumn = schema.get(i);
        if (columnEqual(curColumn, target)) {
          orderColumns.add(curColumn);
          orderAscMap.put(curColumn.toString(), orderByElement.isAsc());
          usedColumns.add(curColumn.toString());
          indexMap.put(curColumn.toString(), i);
          break;
        }
      }
    }

    // put all remaining columns
    for (int i = 0; i < schema.size(); i++) {
      Column curColumn = schema.get(i);
      if (!usedColumns.contains(curColumn.toString())) {
        remainingColumns.add(curColumn);
        indexMap.put(curColumn.toString(), i);
      }
    }

    //    // for each column in schema, decide it is in orderByElements or not
    //    for (int i = 0; i < schema.size(); i++) {
    //      Column curColumn = schema.get(i);
    //      // sign for recording it is a required Column in orderByElements or not
    //      boolean inRequired = false;
    //      for (int j = 0; j < orderByElements.size(); j++) {
    //        Column target = (Column) orderByElements.get(j).getExpression();
    //        String targetAliasOrName = target.getTable().getName();
    //        // Deal with Alias : like S.A ,  convert it to Sailors.A
    //        if (AliasTool.isAlias(targetAliasOrName, aliasMap)) {
    //          target.setTable(aliasMap.get(targetAliasOrName));
    //        }
    //        if (columnEqual(curColumn, target)) {
    //          // is in required, need to record asc/desc as well
    //          orderColumns.add(curColumn);
    //          orderAscMap.put(curColumn.toString(), orderByElements.get(j).isAsc());
    //          inRequired = true;
    //          break;
    //        }
    //      }
    //      // if not in required
    //      if (!inRequired) remainingColumns.add(curColumn);
    //      indexMap.put(curColumn.toString(), i);
    //    }
  }

  /**
   * compare method for comapring tuples compare with orderColumns first then compare with
   * remainingColumns
   *
   * @param t1 the first object to be compared.
   * @param t2 the second object to be compared.
   * @return the comparing integer result
   */
  @Override
  public int compare(Tuple t1, Tuple t2) {
    for (Column col : orderColumns) {
      // get the index for specific column schema
      int columnIndex = indexMap.get(col.toString());
      // get the corresponding val of that tuple and make comparison , return result
      int comparisonResult =
          Integer.compare(t1.getElementAtIndex(columnIndex), t2.getElementAtIndex(columnIndex));
      // asc / desc
      if (comparisonResult != 0) {
        return orderAscMap.get(col.toString()) ? comparisonResult : -comparisonResult;
      }
    }
    // remaining cols  default :  rising order asc
    for (Column rem : remainingColumns) {
      int columnIndex = indexMap.get(rem.toString());
      int comparisonResult =
          Integer.compare(t1.getElementAtIndex(columnIndex), t2.getElementAtIndex(columnIndex));
      if (comparisonResult != 0) {
        return comparisonResult;
      }
    }
    return 0;
  }

  /**
   * Decide if two Column are the same using both tableName and colName
   *
   * @param c1 column
   * @param c2 column
   * @return boolean res
   */
  private boolean columnEqual(Column c1, Column c2) {
    return c1.getTable().getName().equalsIgnoreCase(c2.getTable().getName())
        && c1.getColumnName().equalsIgnoreCase(c2.getColumnName());
  }
}
