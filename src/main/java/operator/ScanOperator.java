package operator;

import common.DBCatalog;
import common.Tuple;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScanOperator extends Operator {
  private final Logger logger = LogManager.getLogger();
  private File file;
  private BufferedReader reader;

  // Upon initialization, it opens a file scan on the appropriate data file

  public ScanOperator(String tName) {
    // load schema and file reader
    try {
      this.setOutputSchema(DBCatalog.getInstance().getSchema().get(tName));
      this.file = DBCatalog.getInstance().getFileForTable(tName);
      this.reader = new BufferedReader(new FileReader(this.file));
    } catch (Exception e) {
      logger.info("Cannot read File" + e.getMessage());
    }
  }
  /**
   * Resets cursor on the operator to the beginning
   */
  @Override
  public void reset() {
    try {
      reader.close();
      reader = new BufferedReader(new FileReader(file));
    } catch (IOException e) {
      logger.info("Cannot close reader" + e.getMessage());
    }
  }

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    try {
      String curLine = reader.readLine();
      if (curLine != null) {
        return new Tuple(curLine);
      }
    } catch (Exception e) {
      logger.info("Cannot readLine " + e.getMessage());
    }
    return null;
  }

  /**
   * close the resource
   */
  public void close() {
    try {
      if (reader != null) reader.close();
    } catch (IOException e) {
      logger.info("Cannot close reader " + e.getMessage());
    }
  }
}
