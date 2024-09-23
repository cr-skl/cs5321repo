package common;

import net.sf.jsqlparser.expression.Expression;
import operator.Operator;

public class JoinOperator extends Operator {
    private Operator leftChild;
    private Operator rightChild;
    private Expression eval;
    /**
     * Resets cursor on the operator to the beginning
     */
    @Override
    public void reset() {

    }

    /**
     * Get next tuple from operator
     *
     * @return next Tuple, or null if we are at the end
     */
    @Override
    public Tuple getNextTuple() {
        Tuple LeftNextTuple = null;
        while(true) {
            LeftNextTuple = leftChild.getNextTuple();
            rightChild.reset();
            while(true) {

            }
        }
    }
}
