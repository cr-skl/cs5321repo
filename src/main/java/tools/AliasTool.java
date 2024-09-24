package tools;

import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class AliasTool {
  // define if a table name is Alias or not
  public static boolean isAlias(Column column, Map<String, Table> aliasMap) {
    String aliasOrName = column.getTable().getName();
    return isAlias(aliasOrName, aliasMap);
  }

  public static boolean isAlias(String name, Map<String, Table> aliasMap) {
    return aliasMap.containsKey(name);
  }

  // if Column wrapped with alias,  then return alias
  public static String getAlias(Column column, Map<String, Table> aliasMap) {
    Table table = column.getTable();
    String aliasName = table.getName();
    if (aliasMap.containsKey(aliasName)) {
      return aliasMap.get(aliasName).getAlias().getName();
    }
    return null;
  }

  public static String getOriginalName(Column column, Map<String, Table> aliasMap) {
    Table table = column.getTable();
    String alias = table.getName();
    if (aliasMap.containsKey(alias)) {
      Table tb = aliasMap.get(alias);
      String tb_name = tb.getName();
      return tb_name;
    }
    return null;
  }

  public static void aliasToName(Column target, Map<String, Table> aliasMap) {
    if (isAlias(target, aliasMap)) {
      Table table = target.getTable();
      table.setName(getOriginalName(target, aliasMap));
    }
  }

  public static void aliasToName(Expression expr, Map<String, Table> aliasMap) {
    if (!(expr instanceof Column)) return;
    Column col = (Column) expr;
    aliasToName(col, aliasMap);
  }
}
