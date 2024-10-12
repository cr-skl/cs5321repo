package LogicalOperator;

import common.DBCatalog;
import net.sf.jsqlparser.schema.Table;

public class LogicalScanOp extends LogicalOperator {
  private String tableName;
  private Table table;

  public Table getTable() {
    return table;
  }

  public LogicalScanOp(String tName, Table table) {
    tableName = tName;
    this.table = table;
    this.setOutputSchema(DBCatalog.getInstance().getSchema().get(tName));
  }

  public String getTableName() {
    return tableName;
  }
}
