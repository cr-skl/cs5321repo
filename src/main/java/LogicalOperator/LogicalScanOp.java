package LogicalOperator;

import common.DBCatalog;

public class LogicalScanOp extends LogicalOperator {
  private String tableName;

  public LogicalScanOp(String tName) {
    tableName = tName;
    this.setOutputSchema(DBCatalog.getInstance().getSchema().get(tName));
  }

  public String getTableName() {
    return tableName;
  }
}
