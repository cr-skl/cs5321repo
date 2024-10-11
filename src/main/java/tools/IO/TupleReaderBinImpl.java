package tools.IO;

import common.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import tools.debug.FileLogger;

public class TupleReaderBinImpl implements TupleReader {
  private File file;
  private FileLogger logger = FileLogger.getInstance();
  private FileChannel fileChannel;
  private ByteBuffer buffer;
  private int pageSize = 4096;
  private boolean endOfFile = false;
  private int tupleSize;
  private int tupleCount;
  private int tuplePtr;

  public TupleReaderBinImpl(File file) {
    this.endOfFile = false;
    this.tupleSize = tupleSize;
    this.file = file;
    try {
      this.fileChannel = new FileInputStream(file).getChannel();
      this.buffer = ByteBuffer.allocate(pageSize);
      loadNextPage();
    } catch (FileNotFoundException e) {
      logger.log("Error in creating Input Stream for file: " + e.getMessage());
    } catch (IOException e) {
      logger.log("Error in reading file to a buffer: " + e.getMessage());
    }
  }

  private void loadNextPage() throws IOException {
    // clear buffer,  buffer now as a target (for write
    buffer.clear();
    // from file to buffer, write the page
    int bytesRead = fileChannel.read(buffer);
    if (bytesRead == -1) {
      endOfFile = true;
    } else {
      // buffer now as a source ( for write
      buffer.flip();
      // write buffer to metaData
      tupleSize = buffer.getInt();
      tupleCount = buffer.getInt();
      // reset the tuplePointer to the
      tuplePtr = 0;
    }
  }

  @Override
  public Tuple readNextTuple() {
    if (endOfFile) {
      return null;
    }

    if (tuplePtr >= tupleCount) {
      try {
        loadNextPage();
        if (endOfFile) {
          return null;
        }
      } catch (IOException e) {
        logger.log("Error in loading next page: " + e.getMessage());
        return null;
      }
    }

    ArrayList<Integer> tupleData = new ArrayList<>();
    for (int i = 0; i < tupleSize; i++) {
      tupleData.add(buffer.getInt());
    }

    tuplePtr++;
    return new Tuple(tupleData);
  }

  @Override
  public void reset() {
    try {
      fileChannel.position(0);
      loadNextPage();
      endOfFile = false;
    } catch (IOException e) {
      logger.log("Error in resetting reader: " + e.getMessage());
    }
  }

  @Override
  public void close() {
    try {
      if (fileChannel != null) {
        fileChannel.close();
      }
    } catch (IOException e) {
      logger.log("Error in closing FileChannel: " + e.getMessage());
    }
  }
}
