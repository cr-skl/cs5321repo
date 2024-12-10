package PhysicalOperator;

import Comparator.TupleComparator;
import common.entity.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import tools.IO.*;

import java.io.File;
import java.util.*;

public class ExtSortOperator extends SortOperator {
  private static final int PAGE_SIZE = 4096;
  private static final int INT_SIZE = 4;
  private int pageSize = PAGE_SIZE;
  private static int counter;
  private TupleReader reader;
  private int buffPages;

  private String tempPath;
  private File rootDir;
  private boolean useBin;

  public ExtSortOperator(List<OrderByElement> orderByElements, Map<String, Table> aliasMap, String tempPath, int buffPages, boolean useBin) {
    super(orderByElements, aliasMap);
    this.buffPages = buffPages;
    this.tempPath = tempPath;
    String rootDirName = "ExtSort_"+ counter++;
    this.rootDir = createDir(tempPath, rootDirName);
  }

  /**
   * Resets cursor on the operator to the beginning
   */
  @Override
  public void reset() {
    reader.reset();
  }

  /**
   * Get next tuple from operator
   *
   * @return next Tuple, or null if we are at the end
   */
  @Override
  public Tuple getNextTuple() {
    if (!sorted) {
      File res = sortIt(child);
      reader = new TupleReaderHumanImpl(res);
      sorted = true;
    }
    return reader.readNextTuple();
  }
  private File createDir(String parentFile, String fileName){
    File dir = new File(parentFile, fileName);
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
    }
    return dir;
  }
  private File createDir(File parentDir, String fileName) {
    File dir = new File(parentDir, fileName);
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
    }
    return dir;
  }
  private File sortIt(PhysicalOperator child) {
    File firstRun = firstRun(child);
    File finalOne = kRun(firstRun, buffPages, 1);
    return finalOne;
  }

  private File firstRun(PhysicalOperator child) {
    File first = createDir(rootDir, "0-run");
    boolean flag = true;
    int maxTuplesPerPage = (pageSize - 2 * INT_SIZE) / (child.getOutputSchema().size() * INT_SIZE);
    int ptr = 0;
    TupleWriter w = null;
    try {
      while(flag) {
        List<Tuple> lst = new LinkedList<>();
        File outputFile = new File(first, "gen-" + ptr);
        w = new TupleWriterHumanImpl(outputFile);
        Tuple t = null;
        for (int i = 0; i < maxTuplesPerPage && (t = child.getNextTuple()) != null; i++) {
          lst.add(t);
        }
        if (t == null)
          flag = false;
        Collections.sort(lst, new TupleComparator(orderByElements, this.getOutputSchema(), this.aliasMap));
        for (Tuple e : lst) {
          w.writeTuple(e);
        }
        ptr++;
      }
    } finally {
      if (w != null)
        w.close();
    }
    return first;
  }
  private File kRun(File lastRun, int B, int depth) {
    File[] files = lastRun.listFiles();
    if (files.length == 1)
      return lastRun;
    File nextRun = createDir(lastRun, depth + "-run");
    List<Tuple> res = new LinkedList<>();
    PriorityQueue<Node> pq = new PriorityQueue<>(new NodeComparator(orderByElements, getOutputSchema(), aliasMap));
    TupleReader r = null;
    TupleWriter w = null;
    List<TupleReader> lst = null;
    try {
      for (int i = 0, ptr = 0; i < files.length; i += B - 1, ptr++) {
        lst = new LinkedList<>();
        for (int j = i; j < Math.min(i + B -1, files.length); j++) {
          r = new TupleReaderHumanImpl(files[i]);
          lst.add(r);
          pq.offer(new Node(r));
        }

        while (!pq.isEmpty()) {
          Node node = pq.poll();
          Tuple lastE = node.top;
          if (node.renewTop()) {
            pq.offer(node);
          }
          res.add(lastE);
        }
        File outputFile = new File(nextRun, "gen-" + ptr);
        try {
          w = new TupleWriterHumanImpl(outputFile);
          for (Tuple t : res) {
            w.writeTuple(t);
          }
        } finally {
          w.close();
        }
      }
    } finally {
      if (lst != null) {
        for (TupleReader rd : lst)
          if (rd != null)
            rd.close();
      }
    }
    return kRun(nextRun, B, depth+1);
  }
  private class NodeComparator implements Comparator<Node> {
    TupleComparator tCmp;
    public NodeComparator(List<OrderByElement> orderByElements, ArrayList<Column> cols, Map<String, Table> aliasMap) {
      tCmp = new TupleComparator(orderByElements, cols, aliasMap);
    }
    @Override
    public int compare(Node o1, Node o2) {
      return tCmp.compare(o1.top, o2.top);
    }

  }
  private class Node {

    TupleReader reader;
    Tuple top;
    public Node(TupleReader reader) {
      this.reader = reader;
      renewTop();
    }
    private boolean renewTop() {
      top = reader.readNextTuple();
      if (top == null)
          return false;
      return true;
    }
  }
}