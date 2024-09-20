package operator;

import common.Tuple;

public class ProjectOperator extends Operator {
    // either a selectOperator or a scan Operator
    // depends on whether has the where clause

    // either a new tuple or the original tuple
    // depends on what in the SELECT Field
    private Operator child;

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
        return null;
    }
}
