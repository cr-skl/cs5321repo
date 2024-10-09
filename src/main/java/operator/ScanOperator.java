package operator;

import common.DBCatalog;
import common.Tuple;
import tools.IO.TupleReader;
// import tools.IO.TupleReaderBinImpl;
import tools.IO.TupleReaderBinImpl;

public class ScanOperator extends Operator {
  private TupleReader tupleReader;

  public ScanOperator(String tName) {
    this.setOutputSchema(DBCatalog.getInstance().getSchema().get(tName));
    // load schema and file reader
    //    this.tupleReader = new
    // TupleReaderFileImpl(DBCatalog.getInstance().getFileForTable(tName));
    this.tupleReader = new TupleReaderBinImpl(DBCatalog.getInstance().getFileForTable(tName));
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
