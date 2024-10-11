package operator;

import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import java.util.Map;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import tools.IO.TupleReader;
// import tools.IO.TupleReaderBinImpl;
import tools.IO.TupleReaderFileImpl;

public class ScanOperator extends Operator {
  private TupleReader tupleReader;

  public ScanOperator(String tName, Table tableEntity, Map<String, Table> aliasMap) {
    this.tupleReader = new TupleReaderFileImpl(DBCatalog.getInstance().getFileForTable(tName));
    //    this.tupleReader = new TupleReaderBinImpl(DBCatalog.getInstance().getFileForTable(tName));

    //    this.setOutputSchema(DBCatalog.getInstance().getSchema().get(tName));

    // get Original Schema with only Table Name
    ArrayList<Column> originalSchema = DBCatalog.getInstance().getSchema().get(tName);
    ArrayList<Column> updatedSchema = getColumns(tableEntity, originalSchema);

    // 设置输出的 schema
    this.setOutputSchema(updatedSchema);
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

  /** Resets cursor on the operator to the beginning */
  @Override
  public void reset() {
    tupleReader.reset();
  }

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    return tupleReader.readNextTuple();
  }

  /** close the resource */
  public void close() {
    tupleReader.close();
  }
}
