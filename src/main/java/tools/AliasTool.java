package tools;

import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class AliasTool {
  /**
   * Given a Column
   * Decide if it has Alias
   * @param column col
   * @param aliasMap aMap
   * @return bool
   */
  public static boolean isAlias(Column column, Map<String, Table> aliasMap) {
    String aliasOrName = column.getTable().getName();
    return isAlias(aliasOrName, aliasMap);
  }

  /**
   * Given a name
   * Decide if a String is alias
   * @param name name
   * @param aliasMap aMap
   * @return bool
   */
  public static boolean isAlias(String name, Map<String, Table> aliasMap) {
    return aliasMap.containsKey(name);
  }

  /**
   * Given the column
   * return its tableName
   * @param column col
   * @param aliasMap aMap
   * @return str of tableName
   */
  public static String getOriginalName(Column column, Map<String, Table> aliasMap) {
    Table table = column.getTable();
    String alias = table.getName();
    if (aliasMap.containsKey(alias)) {
      Table tb = aliasMap.get(alias);
      return tb.getName();
    }
    return null;
  }

  /**
   * Given the Column
   * set its Column's tableName by its tableName
   * @param column column
   * @param aliasMap aMap
   */
  public static void aliasToName(Column column, Map<String, Table> aliasMap) {
    if (isAlias(column, aliasMap)) {
      Table table = column.getTable();
      table.setName(getOriginalName(column, aliasMap));
    }
  }

  /**
   * Given the Expression
   * Cast is as Column
   * set its Column's tableName by its tableName
   * @param expr expression
   * @param aliasMap aMap
   */
  public static void aliasToName(Expression expr, Map<String, Table> aliasMap) {
    if (!(expr instanceof Column)) return;
    Column col = (Column) expr;
    aliasToName(col, aliasMap);
  }
}
