package tools.generator;

import common.Tuple;
import java.util.ArrayList;
import java.util.Random;

/** With specified range, col_num of tuple Generate a random tuple */
public class TupleGenerator {
  public static Tuple randomTuple(int lo, int hi, int col_num) {
    ArrayList<Integer> lst = new ArrayList<>();
    for (int i = 0; i < col_num; i++) {
      Random rand = new Random();
      int randomInt = rand.nextInt(hi - lo + 1) + lo;
      lst.add(randomInt);
    }
    Tuple tuple = new Tuple(lst);
    return tuple;
  }
}
