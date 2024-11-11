package PhysicalOperator;// package PhysicalOperator;
//
// import common.entity.Tuple;
// import net.sf.jsqlparser.schema.Table;
// import net.sf.jsqlparser.statement.select.OrderByElement;
// import tools.IO.TupleReader;
// import tools.IO.TupleReaderHumanImpl;
// import tools.IO.TupleWriter;
// import tools.IO.TupleWriterHumanImpl;
//
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.PriorityQueue;
//
// public class ExtSortOperator extends SortOperator {
//    private String tempPathRoot;
//    private File sortedFile;
//    private TupleReader reader;
//    private int bufferSize;
//
//    // 用于存放临时文件
//    private List<File> tempFiles = new ArrayList<>();
//
//    public ExtSortOperator(List<OrderByElement> orderByElements, Map<String, Table> aliasMap,
// String tempPath, int bufferSize) {
//        super(orderByElements, aliasMap);
//        this.tempPathRoot = tempPath;
//        this.bufferSize = bufferSize;
//    }
//
//    /**
//     * Resets cursor on the operator to the beginning
//     */
//    @Override
//    public void reset() {
//        if (reader != null) {
//            reader.reset();
//        } else {
//            throw new IllegalStateException("Reader is not initialized yet");
//        }
//    }
//
//    /**
//     * Get next tuple from operator
//     *
//     * @return next Tuple, or null if we are at the end
//     */
//    @Override
//    public Tuple getNextTuple() {
//        if (reader == null) {
//            // 执行排序并生成归并结果文件
//            executeExternalSort();
//        }
//        return reader.readNextTuple();
//    }
//
//    /**
//     * 执行外部排序过程，包括划分初始块和归并阶段
//     */
//    private void executeExternalSort() {
//        // 1. 初始排序阶段：生成多个已排序的临时块文件
//        partitionAndSort();
//
//        // 2. 归并阶段：对所有临时块文件进行多路归并
//        mergeSortedRuns();
//
//        // 3. 初始化读取器以获取排序结果
//        this.reader = new TupleReaderHumanImpl(sortedFile);
//    }
//
//    /**
//     * 划分初始块，并对每块进行排序然后存入临时文件
//     */
//    private void partitionAndSort() {
//        List<Tuple> buffer = new ArrayList<>();
//        Tuple tuple;
//        while ((tuple = child.getNextTuple()) != null) {
//            buffer.add(tuple);
//            if (buffer.size() == bufferSize) {
//                sortAndWriteToTempFile(buffer);
//                buffer.clear();
//            }
//        }
//        if (!buffer.isEmpty()) {
//            sortAndWriteToTempFile(buffer);
//        }
//    }
//
//    /**
//     * 对缓冲区内的元组进行排序，并将排序结果写入临时文件
//     *
//     * @param buffer 待排序的元组列表
//     */
//    private void sortAndWriteToTempFile(List<Tuple> buffer) {
//        Collections.sort(buffer, new TupleComparator(orderByElements, this.getOutputSchema(),
// this.aliasMap));
//        File tempFile;
//        try {
//            tempFile = File.createTempFile("sort_run", ".tmp", new File(tempPathRoot));
//            tempFiles.add(tempFile);
//            TupleWriter writer = new TupleWriterHumanImpl(tempFile);
//            for (Tuple t : buffer) {
//                writer.writeTuple(t);
//            }
//
//        } catch (IOException e) {
//            throw new IllegalStateException("Error creating/writing temp file", e);
//        }
//    }
//
//    /**
//     * 对所有的已排序临时文件进行多路归并
//     */
//    private void mergeSortedRuns() {
//        List<TupleReader> readers = new ArrayList<>();
//        try {
//            for (File file : tempFiles) {
//                readers.add(new TupleReaderHumanImpl(file));
//            }
//            sortedFile = new File(tempPathRoot, "sorted_result.tmp");
//            TupleWriter writer = new TupleWriterHumanImpl(sortedFile);
//            PriorityQueue<ReaderTuplePair> pq = new PriorityQueue<>(Comparator.comparing(pair ->
// pair.tuple, new TupleComparator(orderByElements, this.getOutputSchema(), this.aliasMap)));
//            // 初始化优先队列
//            for (TupleReader reader : readers) {
//                if (reader.hasNext()) {
//                    pq.add(new ReaderTuplePair(reader, reader.readNextTuple()));
//                }
//            }
//            // 归并排序
//            while (!pq.isEmpty()) {
//                ReaderTuplePair minPair = pq.poll();
//                writer.writeTuple(minPair.tuple);
//                if (minPair.reader.hasNext()) {
//                    pq.add(new ReaderTuplePair(minPair.reader, minPair.reader.readNextTuple()));
//                }
//            }
//        } catch (IOException e) {
//            throw new IllegalStateException("Error during merging sorted runs", e);
//        }
//    }
//
//    /**
//     * 清理所有的临时文件，包括最终的排序结果文件
//     */
//    public void cleanup() {
//        // 删除中间临时文件
//        for (File file : tempFiles) {
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//        // 删除最终的排序结果文件
//        if (sortedFile != null && sortedFile.exists()) {
//            sortedFile.delete();
//        }
//    }
//
//    /**
//     * 用于存储TupleReader和对应的当前元组
//     */
//    private static class ReaderTuplePair {
//        TupleReader reader;
//        Tuple tuple;
//
//        ReaderTuplePair(TupleReader reader, Tuple tuple) {
//            this.reader = reader;
//            this.tuple = tuple;
//        }
//    }
// }
//
