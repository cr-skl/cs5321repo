package tools.IO;

import common.entity.Node;

public interface IndexWriter {

  void writeNode(Node node);

  void writeHeader(int leafCnt, int rootAddr);

  void close();
}
