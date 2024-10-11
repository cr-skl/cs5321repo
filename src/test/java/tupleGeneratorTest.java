import common.Tuple;
import org.junit.jupiter.api.Test;
import tools.generator.TupleGenerator;

public class tupleGeneratorTest {
  @Test
  public void test1() {
    Tuple tuple = TupleGenerator.randomTuple(2, 10, 5);
    System.out.println(tuple.toString());
  }
}
