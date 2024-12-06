package tools.IO;

import common.entity.IndexNode;
import common.entity.LeafNode;
import common.entity.Node;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import tools.debug.FileLogger;

public class IndexWriterBinImpl implements IndexWriter {
  private FileLogger logger = FileLogger.getInstance();
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;
  private File outputFile;
  private DataOutputStream out;
  private ByteBuffer buffer;
  private int order;

  public IndexWriterBinImpl(File outputFile, int order) {
    try {
      this.order = order;
      this.outputFile = outputFile;
      this.out = new DataOutputStream(new FileOutputStream(outputFile));
      // write blank page
      out.write(new byte[PAGE_SIZE]);
    } catch (Exception e) {
      logger.log("Cannot open BIN outputStream for file" + e.getMessage());
    }
  }

  @Override
  public void writeNode(Node node) {
    if (node instanceof LeafNode) {
      writeLeafNode((LeafNode) node);
    } else {
      writeIndexNode((IndexNode) node);
    }
  }

  private void writeLeafNode(LeafNode leafNode) {
    buffer = ByteBuffer.allocate(PAGE_SIZE);
    buffer.put(leafNode.toByteArray());
  }

  private void writeIndexNode(IndexNode indexNode) {
    buffer = ByteBuffer.allocate(PAGE_SIZE);
    buffer.put(indexNode.toByteArray());
  }

  @Override
  public void writeHeader(int leafCnt, int rootAddr) {
    //    out.flush();
    try {
      this.out = new DataOutputStream(new FileOutputStream(outputFile));
      buffer = ByteBuffer.allocate(3 * INT_SIZE);
      buffer.putInt(rootAddr);
      buffer.putInt(leafCnt);
      buffer.putInt(order);
      out.write(buffer.array(), 0, 3 * INT_SIZE);
    } catch (Exception e) {
      logger.log("cannot write header" + e.getMessage());
    }
  }

  @Override
  public void close() {}
}
