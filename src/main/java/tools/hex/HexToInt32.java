package tools.hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HexToInt32 {

  public static void main(String[] args) {
    File file = new File("E:\\cs5321\\p2\\samples\\input\\db\\data\\Boats");

    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[4];

      while (fis.read(buffer) != -1) {
        int intValue = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();

        System.out.println("Int32 value: " + intValue);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
