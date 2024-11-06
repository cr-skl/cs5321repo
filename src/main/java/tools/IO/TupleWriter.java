package tools.IO;

import common.entity.Tuple;

public interface TupleWriter {

  void writeTuple(Tuple tuple);

  void close();
}
