package tools.debug;

import PhysicalOperator.JoinOperator;
import common.Tuple;
import tools.IO.TupleWriter;
import tools.IO.TupleWriterHumanImpl;

import java.io.File;

/**
 * Utility class for saving intermediate results of join operations.
 */
public class IntermediateResultSaver {

    /**
     * Save intermediate result of a join operation to a file.
     *
     * @param op The join operator whose result is to be saved
     */
    public static void saveIntermediateResult(JoinOperator op, int depth) {
        String fileName = generateUniqueFileName(depth);
        File file = new File(fileName);
        TupleWriter writer = new TupleWriterHumanImpl(file);

        Tuple tuple;
        while ((tuple = op.getNextTuple()) != null) {
            writer.writeTuple(tuple);
        }
        op.reset();

        writer.close();
    }

    /**
     * Generate a unique file name for saving intermediate results.
     *
     * @return A unique file name string
     */
    private static String generateUniqueFileName(int depth) {
        return "intermediate_result_depth_" + depth + "_" + System.currentTimeMillis() + ".txt";
    }
}