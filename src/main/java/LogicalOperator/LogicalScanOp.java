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

    for (Column column : originalSchema) {

      Table updatedTable = new Table();
      if (tableEntity.getAlias() != null) {
        updatedTable.setName(tableEntity.getAlias().getName());
      } else {
        updatedTable.setName(tableEntity.getName());
      }

      Column updatedColumn = new Column(updatedTable, column.getColumnName());
      updatedSchema.add(updatedColumn);
    }
    return updatedSchema;
  }
}
