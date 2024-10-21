import PhysicalOperator.PhysicalOperator;
import common.Tuple;
import java.util.ArrayList;
import java.util.List;

public class HelperMethods {
  public static List<Tuple> collectAllTuples(PhysicalOperator operator) {
    Tuple tuple;
    List<Tuple> tuples = new ArrayList<>();
    while ((tuple = operator.getNextTuple()) != null) {
      tuples.add(tuple);
    }

    return tuples;
  }
}
