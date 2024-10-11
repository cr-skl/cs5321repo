package tools.IO;

import common.Tuple;
import java.io.*;
import java.nio.ByteBuffer;
import tools.debug.FileLogger;

public class TupleWriterBinImpl implements TupleWriter {
  // constant
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;

  private FileLogger logger = FileLogger.getInstance();
  // metaData
  private int tupleSize = -1;
  private int tupleCount = 0;
  private int maxTuplesPerPage = -1;
  private int pageSize = PAGE_SIZE;
  // IO attribute
  private DataOutputStream out;
  private ByteBuffer buffer;

  public TupleWriterBinImpl(File outputFile) {
    try {
      this.out = new DataOutputStream(new FileOutputStream(outputFile));
      this.buffer = ByteBuffer.allocate(pageSize);

    } catch (FileNotFoundException e) {
      logger.log("Cannot open BIN outputStream for file" + e.getMessage());
    }
  }

  @Override
  public void writeTuple(Tuple tuple) {
    // when writing the first two elements in a page
    // writes metaData
    if (tupleSize == -1) {
      tupleSize = tuple.getAllElements().size();
      maxTuplesPerPage = (pageSize - 2 * INT_SIZE) / (tupleSize * INT_SIZE);
      buffer.putInt(tupleSize);
      buffer.putInt(0); // placeholder,  will be editted for pageDone
    }
    for (int e : tuple.getAllElements()) {
      buffer.putInt(e);
    }
    tupleCount++;
    // page full
    if (tupleCount == maxTuplesPerPage) {
      try {
        flushPage();
      } catch (IOException e) {
        logger.log("Cannot write flush pageBuffer to file" + e.getMessage());
      }
    }
  }

  private void flushPage() throws IOException {
    // write the current page's metaData  tupleCnt of whole page
    buffer.putInt(INT_SIZE, tupleCount);
    while (buffer.remaining() > 0) {
      buffer.putInt(0);
    }
    out.write(buffer.array());
    //    // fill the page with zeros
    //    buffer.clear();
    //    for (int i = 0; i < pageSize / INT_SIZE; i++) {
    //      buffer.putInt(0);
    //    }
    // reset the page
    buffer.clear();
    tupleCount = 0;
    // write metaData for next Page
    buffer.putInt(tupleSize);
    buffer.putInt(0);
  }

  @Override
  public void close() {
    if (tupleCount > 0) {
      try {
        flushPage();
      } catch (IOException e) {
        logger.log("Cannot flush pageBuffer to File" + e.getMessage());
      }
    }
    try {
      out.close();
    } catch (IOException e) {
      logger.log("Cannot close the DataOutputStream" + e.getMessage());
    }
  }
}
