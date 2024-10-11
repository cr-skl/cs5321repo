package tools.IO;

import common.Tuple;
import java.io.*;
import tools.debug.FileLogger;

public class TupleReaderFileImpl implements TupleReader {
  private FileLogger logger = FileLogger.getInstance();
  private File file;
  private BufferedReader reader;

  public TupleReaderFileImpl(File file) {
    this.file = file;
    try {
      this.reader = new BufferedReader(new FileReader(this.file));
    } catch (Exception e) {
      logger.log("Cannot read File" + e.getMessage());
    }
  }

  @Override
  public Tuple readNextTuple() {
    try {
      String curLine = reader.readLine();
      if (curLine != null) {
        return new Tuple(curLine);
      }
    } catch (Exception e) {
      logger.log("Cannot readLine " + e.getMessage());
    }
    return null;
  }

  @Override
  public void reset() {
    try {
      reader.close();
      reader = new BufferedReader(new FileReader(file));
    } catch (IOException e) {
      logger.log("Cannot close reader" + e.getMessage());
    }
  }

  @Override
  public void close() {
    try {
      if (reader != null) reader.close();
    } catch (IOException e) {
      logger.log("Cannot close reader " + e.getMessage());
    }
  }
}
