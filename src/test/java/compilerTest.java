import compiler.Compiler;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class compilerTest {

  @Test
  public void testCompilerMain() throws URISyntaxException, IOException {
    String[] args = {
      "E:\\5321tmp\\samples-p2\\input",
      "E:\\5321tmp\\samples-p2\\output",
      "E:\\5321tmp\\samples-p2\\temp"
    };
    Compiler.main(args);
  }
}
