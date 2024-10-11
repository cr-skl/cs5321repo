package tools.IO;

import common.Tuple;

public interface TupleReader {
  Tuple readNextTuple();

  void reset();

  void close();
}
