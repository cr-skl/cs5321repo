package tools.IO;

import common.Tuple;

public interface TupleWriter {

  void writeTuple(Tuple tuple);

  void close();
}
