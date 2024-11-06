package tools.IO;

import common.entity.Tuple;

public interface TupleReader {
  Tuple readNextTuple();

  void reset();

  void close();
}
