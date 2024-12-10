package common.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IndexNode extends Node {
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;
  // in range d+1 .... 2d+1
  private List<Node> childList;
  private List<Integer> childPageList;
  // in range d ... 2d
  private List<Integer> keyList;

  public IndexNode() {
    childList = new ArrayList<>();
    childPageList = new ArrayList<>();
    keyList = new ArrayList<>();
  }

  public void addNode(Node child, int childPage) {
    childPageList.add(childPage);
    if (leftMostKey == null) leftMostKey = child.leftMostKey;
    // the first adding child will not create key
    if (!childList.isEmpty()) {
      keyList.add(child.leftMostKey);
    }
    childList.add(child);
  }

  public byte[] toByteArray() {
    // tag 1,
    // number of keys
    // actual keys
    // addr of children node
    int total_len = (1 + 1 + keyList.size() + childPageList.size()) * INT_SIZE;
    ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
    // tag
    buffer.putInt(1);
    // number of keys
    buffer.putInt(keyList.size());
    for (Integer key : keyList) {
      buffer.putInt(key);
    }
    // addr of child node
    for (Integer child_page : childPageList) {
      buffer.putInt(child_page);
    }
    while (buffer.position() < PAGE_SIZE) {
      buffer.putInt(0);
    }
    return buffer.array();
  }
}
