package common.entity;

import tools.serialize.Seri;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LeafNode extends Node implements Seri {
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;
  List<Entry> childEntryLst;
  public LeafNode() {
    childEntryLst = new ArrayList<>();
  }
  public void addNode(Entry e) {
    if (leftMostKey == null)
      leftMostKey = e.entryKey;
    childEntryLst.add(e);
  }

  @Override
  public byte[] toByteArray() {
    Queue<byte[]> entrySeri = new LinkedList<>();
    int total_len = 2*INT_SIZE;
    for (Entry e : childEntryLst) {
      byte[] eS = e.toByteArray();
      total_len += eS.length;
      entrySeri.offer(eS);
    }
    ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
    buffer.putInt(0);
    buffer.putInt(entrySeri.size());
    while(!entrySeri.isEmpty()) {
      buffer.put(entrySeri.poll());
    }
    while(buffer.position() < PAGE_SIZE) {
      buffer.putInt(0);
    }
    return buffer.array();
  }
}
