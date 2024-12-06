import compiler.Compiler;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class compilerTest {

  @Test
  public void testCompilerMain() throws URISyntaxException, IOException {
    String[] args = {"E:\\5321tmp\\samples-p1\\input", "E:\\5321tmp\\samples-p1\\output"};
    Compiler.main(args);
  }
}
