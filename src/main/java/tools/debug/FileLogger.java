package tools.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileLogger {
  private static FileLogger instance;
  private PrintWriter writer;

  public FileLogger() {
    try {
      // true meaning write to the end of existing file, not overwritting it
      FileWriter fileWriter = new FileWriter("logfile.txt", true);

      writer = new PrintWriter(fileWriter, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * get the Instance for singleton
   *
   * @return
   */
  public static FileLogger getInstance() {
    if (instance == null) {
      instance = new FileLogger();
    }
    return instance;
  }

  // write in the log
  public void log(String message) {
    writer.println(message);
  }

  // close writing I/O
  public void close() {
    writer.close();
  }
}
/** usage : Filelogger.getInstance().log() */
