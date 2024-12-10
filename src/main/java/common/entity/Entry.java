package common.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import tools.serialize.Seri;

public class Entry implements Seri {
  private static final int INT_SIZE = 4;
  int entryKey;
  List<Rid> records;

  public Entry(int entryKey) {
    this.entryKey = entryKey;
    records = new ArrayList<>();
  }

  public void addRecord(Rid rid) {
    records.add(rid);
  }

  @Override
  public byte[] toByteArray() {
    // key + size + (pair)*size
    int total_len = (2 + 2 * records.size()) * INT_SIZE;
    ByteBuffer buffer = ByteBuffer.allocate(total_len);
    buffer.putInt(entryKey);
    buffer.putInt(records.size());
    for (Rid rid : records) {
      buffer.putInt(rid.pageID);
      buffer.putInt(rid.tupleID);
    }
    return buffer.array();
  }
}
