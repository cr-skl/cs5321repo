package common.entity;

public class metaTuple extends Tuple {
  /**
   * Creates a tuple using an ArrayList of integers.
   *
   * @param elements ArrayList with elements of the tuple, in order
   */
  int pageID;

  int tupleID;

  public int getPageID() {
    return pageID;
  }

  public int getTupleID() {
    return tupleID;
  }

  public metaTuple(Tuple t, int pageID, int tupleID) {
    super(t.getAllElements());
    this.pageID = pageID;
    this.tupleID = tupleID;
  }
}
