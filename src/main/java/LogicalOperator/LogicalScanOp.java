package LogicalOperator;

import common.DBCatalog;
import java.util.ArrayList;
import java.util.Map;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class LogicalScanOp extends LogicalOperator {
  private String tableName;
  private Table table;

  public Table getTable() {
    return table;
  }

  public LogicalScanOp(String tName, Table table, Map<String, Table> aliasMap) {
    tableName = tName;
    this.table = table;
    // get Original Schema with only Table Name
    ArrayList<Column> originalSchema = DBCatalog.getInstance().getSchema().get(tName);
    ArrayList<Column> updatedSchema = getColumns(table, originalSchema);

    // 设置输出的 schema
    this.setOutputSchema(updatedSchema);
  }

  public String getTableName() {
    return tableName;
  }

  private static ArrayList<Column> getColumns(Table tableEntity, ArrayList<Column> originalSchema) {
    ArrayList<Column> updatedSchema = new ArrayList<>();

    // 更新列的表信息以包含别名
    for (Column column : originalSchema) {
      // 创建一个新的 Table 实例，保持原始表名和别名
      Table updatedTable = new Table();
      if (tableEntity.getAlias() != null) {
        // 如果有别名，使用别名
        updatedTable.setName(tableEntity.getAlias().getName());
      } else {
        // 如果没有别名，使用原始表名
        updatedTable.setName(tableEntity.getName());
      }

      // 创建一个新的 Column 并设置表信息
      Column updatedColumn = new Column(updatedTable, column.getColumnName());
      updatedSchema.add(updatedColumn);
    }
    return updatedSchema;
  }
}
