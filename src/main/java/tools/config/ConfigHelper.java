package tools.config;

import common.DBCatalog;
import common.entity.*;
import net.sf.jsqlparser.schema.Column;
import org.apache.logging.log4j.LogManager;
import tools.IO.IndexWriter;
import tools.IO.IndexWriterBinImpl;
import tools.IO.TupleReaderBinImpl;
import visitor.PhysicalPlanBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigHelper {
  public int joinType, joinBufPages, sortType, sortBufPages, useIndex, evalQuery;
  public Path sqlPath, dbPath, schemaPath, index_info_Path, index_Path;
  public ConfigHelper(URI inputURI) throws URISyntaxException {

    readConfig(inputURI);
  }

  public void readConfig(URI inputURI) throws URISyntaxException {
    ClassLoader classLoader = PhysicalPlanBuilder.class.getClassLoader();
    URI InputURI = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path input = Paths.get(InputURI);
    Path config = input.resolve("plan_builder_config.txt");
    dbPath = input.resolve("db");
    sqlPath = input.resolve("testqueries.sql");
    schemaPath = dbPath.resolve("schema.txt");
    index_info_Path = dbPath.resolve("index_info.txt");
    index_Path = dbPath.resolve("indexes");
    try {
        // read config from plan_builder_config.txt file
        BufferedReader br = new BufferedReader(new FileReader(config.toString()));
        // read JOIN params
        String[] params = br.readLine().split("\\s");
        joinType = Integer.valueOf(params[0]);
        joinBufPages = 0;
        if (joinType == 1) joinBufPages = Integer.valueOf(params[1]);
        else if (joinType != 0 && joinType != 2)
            throw new IllegalArgumentException("Join type must be 0, 1, or 2");
        // read SORT params
        params = br.readLine().split("\\s");
        sortType = Integer.valueOf(params[0]);
        sortBufPages = 0;
        if (sortType == 1) sortBufPages = Integer.valueOf(params[1]);
        else if (sortType != 0) throw new IllegalArgumentException("Sort type must be 0 or 1");
        // read INDEX param
        params = br.readLine().split("\\s");
        useIndex = Integer.valueOf(params[0]);
        // read QUERY param
        params = br.readLine().split("\\s");
        evalQuery = Integer.valueOf(params[0]);
        br.close();
    } catch (IOException e) {
        LogManager.getLogger().error(e.getMessage());
    }
  }

  public void buildIndex() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(index_info_Path.toString()));
    String line = null;
    while ((line = br.readLine()) != null) {
      String[] tokens  = line.split("\\s");
      String  table    = tokens[0];
      String  key      = tokens[1];
      boolean type     = Integer.valueOf(tokens[2]) == 1 ? true:false;
      Integer order    = Integer.valueOf(tokens[3]);
      buildIndexOne(table, key, type, order);
    }
  }
  private void buildIndexOne(String table, String key, boolean clustered, int d) {
    // get the key's mapping position for indexing
    Map<String, ArrayList<Column>> schema = DBCatalog.getInstance().getSchema();
    ArrayList<Column> columns = schema.get(table);
    int idx = 0;
    for (int i = 0; i < columns.size(); i++) {
      if (key.equals(columns.get(i).getColumnName())) {
        idx = i;
        break;
      }
    }
    final int keyIdx = idx;
    TupleReaderBinImpl tupleReader = new TupleReaderBinImpl(DBCatalog.getInstance().getFileForTable(table));
    // extract all the tuples in this table
    List<metaTuple> whole = new ArrayList<>();
    Tuple t = null;
    while((t = tupleReader.readNextTuple()) != null) {
      int[] meta = tupleReader.getMeta();
      whole.add(new metaTuple(t, meta[0], meta[1]));
    }
    // sort all the tuple, with its info of pid and tid
    Collections.sort(whole, Comparator.comparingInt(a -> a.getElementAtIndex(keyIdx)));
    metaTuple mt = null;
    Integer entryKeyVal = null;
    if (!clustered) {
      // generate all entryQueue
      // put them in an entryList, for preparation of generating leafNode
      Queue<Entry> entryQueue = new LinkedList<>();
      for (int j = 0; j < whole.size();) {
        mt = whole.get(j);
        entryKeyVal = mt.getElementAtIndex(keyIdx);
        Entry entry = new Entry(entryKeyVal);
        entry.addRecord(new Rid(mt.getPageID(), mt.getTupleID()));

        // put all same key record into the same entry;
        metaTuple next = null;
        int k = j+1;
        for (; k < whole.size() && mt.getElementAtIndex(keyIdx) == (next = whole.get(k)).getElementAtIndex(keyIdx); k++) {
          entry.addRecord(new Rid(next.getPageID(), next.getTupleID()));
        }
        entryQueue.offer(entry);
        j = k;
      }
      String fileName = table+","+key;
      // open file, with placeholder for header page
      IndexWriter indexWriter = new IndexWriterBinImpl(new File(index_Path.toString(), fileName), d);
      // pageCnt always points to the last processed node
      int pageCnt = 0;
      // build the layer of leaf node
      Queue<LeafNode> leafNodeQueue = new LinkedList<>();
      while(entryQueue.size() >= 3*d) {
        LeafNode leafNode = new LeafNode();
        for (int i = 0; i < 2*d; i++) {
          leafNode.addNode(entryQueue.poll());
        }
        leafNodeQueue.offer(leafNode);
        indexWriter.writeNode(leafNode);
        pageCnt++;
      }
      // 2d < rem < 3d
      if (entryQueue.size() > 2*d) {
        LeafNode l1 = new LeafNode();
        LeafNode l2 = new LeafNode();
        int rem = entryQueue.size();
        for (int i = 0; i < rem/2; i++) {
          l1.addNode(entryQueue.poll());
        }
        while(!entryQueue.isEmpty()) {
          l2.addNode(entryQueue.poll());
        }
        leafNodeQueue.offer(l1);
        leafNodeQueue.offer(l2);
        indexWriter.writeNode(l1);
        pageCnt++;
        indexWriter.writeNode(l2);
        pageCnt++;
      }
      // rem <= 2d
      else {
        LeafNode leafNode = new LeafNode();
        while(!entryQueue.isEmpty()) {
          leafNode.addNode(entryQueue.poll());
        }
        leafNodeQueue.offer(leafNode);
        indexWriter.writeNode(leafNode);
        pageCnt++;
      }


      int leafCnt = pageCnt;

      // childPtr simulate the child's page number, starting from 1
      // since page 0  is header , will be written in the last
      
      // build First layer index page
      int childPtr = 1;
      int[] pgCnt = new int[1];
      int[] cdCnt = new int[1];
      pgCnt[0] = pageCnt;
      cdCnt[0] = childPtr;

      Queue<Node> baseQueue = new LinkedList<>(leafNodeQueue);
      Queue<Node> nodeQueue = buildIndexPerLayer(indexWriter, baseQueue, d, cdCnt, pgCnt);
      // following layer is built from index
      while(nodeQueue.size() > 1) {
        Queue<Node> res = buildIndexPerLayer(indexWriter, nodeQueue, d, cdCnt, pgCnt);
        nodeQueue = res;
      }
      int rootAddr = pgCnt[0];

      // write Header
      indexWriter.writeHeader(leafCnt, rootAddr);
    }
  }
  private Queue<Node> buildIndexPerLayer(IndexWriter indexWriter,
                     Queue<Node> childNodeQueue,
                     int d,
                     int[] childPtr,
                     int[] pageCnt
                     ) {
    Queue<Node> nodeQueue = new LinkedList<>();
    while(childNodeQueue.size() >= 3*d+2) {
      IndexNode indexNode = new IndexNode();
      for (int i = 0; i < 2*d+1; i++) {
        indexNode.addNode(childNodeQueue.poll(), childPtr[0]++);
      }
      nodeQueue.offer(indexNode);
      indexWriter.writeNode(indexNode);
      pageCnt[0]++;
    }
    if (childNodeQueue.size() > 2*d + 1) {
      IndexNode l1 = new IndexNode();
      IndexNode l2 = new IndexNode();
      int rem = childNodeQueue.size();
      for (int i = 0; i < rem/2; i++) {
        l1.addNode(childNodeQueue.poll(), childPtr[0]++);
      }
      while(!childNodeQueue.isEmpty()) {
        l2.addNode(childNodeQueue.poll(), childPtr[0]++);
      }
      nodeQueue.offer(l1);
      indexWriter.writeNode(l1);
      pageCnt[0]++;
      nodeQueue.offer(l2);
      indexWriter.writeNode(l2);
      pageCnt[0]++;
    }
    else {
      IndexNode indexNode = new IndexNode();
      while(!childNodeQueue.isEmpty()) {
        indexNode.addNode(childNodeQueue.poll(), childPtr[0]++);
      }
      nodeQueue.offer(indexNode);
      indexWriter.writeNode(indexNode);
      pageCnt[0]++;
    }
    return nodeQueue;
  }
}