package tools.IO;

import common.Tuple;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import tools.debug.FileLogger;

public class TupleWriterHumanImpl implements TupleWriter {
  private FileLogger logger = FileLogger.getInstance();
  private PrintStream out;

  public TupleWriterHumanImpl(File file) {
    try {
      this.out = new PrintStream(file);
    } catch (FileNotFoundException e) {
      logger.log("Cannot open printStream for file" + e.getMessage());
    }
  }

  @Override
  public void writeTuple(Tuple tuple) {
    out.println(tuple);
  }

  @Override
  public void close() {
    out.close();
  }
}
