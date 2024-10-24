package PhysicalOperator;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.List;
import java.util.Map;

public abstract class SortOperator extends PhysicalOperator{
    protected List<OrderByElement> orderByElements;
    protected Map<String, Table> aliasMap;
    protected PhysicalOperator child;

    public SortOperator(List<OrderByElement> orderByElements, Map<String, Table> aliasMap) {
        this.orderByElements = orderByElements;
        this.aliasMap = aliasMap;
    }

    /**
     * set the child, called on .visit()
     *
     * @param child c
     */
    public void setChild(PhysicalOperator child) {
        this.child = child;
    }
}
