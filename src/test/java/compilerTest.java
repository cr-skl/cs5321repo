import compiler.Compiler;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class compilerTest {

  @Test
  public void testCompilerMain() throws URISyntaxException {
    String[] args = {"samples/input", "samples/output"};
    Compiler.main(args);
  }
}
