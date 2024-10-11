package tools.alias;

import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class AliasTool {
  /**
   * Given a Column Decide if it has Alias
   *
   * @param column col
   * @param aliasMap aMap
   * @return bool
   */
  public static boolean isAlias(Column column, Map<String, Table> aliasMap) {
    String aliasOrName = column.getTable().getName();
    return isAlias(aliasOrName, aliasMap);
  }

  /**
   * Given a name Decide if a String is alias
   *
   * @param name name
   * @param aliasMap aMap
   * @return bool
   */
  public static boolean isAlias(String name, Map<String, Table> aliasMap) {
    return aliasMap.containsKey(name);
  }

  // Return its
  // non-alias:  originalTablName + columnName
  // alias   :  aliasName + columnName
  public static String getColumnKey(Column column) {
    String tableName = (column.getTable().getAlias() != null)
            ? column.getTable().getAlias().getName()
            : column.getTable().getName();
    return tableName + "," + column.getColumnName();
  }
  //  non-alias: originalTableName
  //  alias: aliasName
  public static String getAliasOrName(Table table, Map<String, Table> aliasMap) {
    return table.getAlias() != null ? table.getAlias().getName() : table.getName();
  }
  public static String getTableName(Column column, Map<String, Table> aliasMap) {
    String aliasOrName = column.getTable().getName();
    if (isAlias(aliasOrName, aliasMap)) {
      Table tb = aliasMap.get(aliasOrName);
      return tb.getName();
    }
    return aliasOrName;
  }
}
