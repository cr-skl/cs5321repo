package Comparator;

import common.Tuple;
import java.util.*;
import java.util.Comparator;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import tools.AliasTool;

public class TupleComparator implements Comparator<Tuple> {
  private Map<String, Integer> indexMap;
  private Map<String, Table> aliasMap;

  private Map<String, Boolean> orderAscMap;
  private List<Column> orderColumns;
  private List<Column> remainingColumns;
  private ArrayList<Column> schema;

  public TupleComparator(
      List<OrderByElement> orderByElements, ArrayList<Column> schema, Map<String, Table> aliasMap) {
    this.indexMap = new HashMap<>();
    this.aliasMap = aliasMap;
    this.schema = schema;
    this.orderAscMap = new HashMap<>();
    this.orderColumns = new ArrayList<>();
    this.remainingColumns = new ArrayList<>();
    for (int i = 0; i < schema.size(); i++) {
      Column curColumn = schema.get(i);
      // if in required
      boolean inRequired = false;
      int j = 0;
      for (; j < orderByElements.size(); j++) {
        Column target = (Column) orderByElements.get(j).getExpression();
        String targetAliasOrName = target.getTable().getName();
        if (AliasTool.isAlias(targetAliasOrName, aliasMap)) {
          target.setTable(aliasMap.get(targetAliasOrName));
        }
        if (columnEqual(curColumn, target)) {
          orderColumns.add(target);
          orderAscMap.put(target.toString(), orderByElements.get(j).isAsc());
          inRequired = true;
          break;
        }
      }
      // if not in required
      if (!inRequired) remainingColumns.add(curColumn);
      indexMap.put(curColumn.toString(), i);
    }
  }

  @Override
  public int compare(Tuple t1, Tuple t2) {
    for (Column col : orderColumns) {
      int columnIndex = indexMap.get(col.toString());
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

  private boolean columnEqual(Column c1, Column c2) {
    return c1.getTable().getName().equalsIgnoreCase(c2.getTable().getName())
        && c1.getColumnName().equalsIgnoreCase(c2.getColumnName());
  }
}
