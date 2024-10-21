package LogicalOperator;

import java.util.ArrayList;
import java.util.Map;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import visitor.PhysicalPlanBuilder;

/**
 * Abstract class to represent logical operators. Every operator has a reference to an outputSchema
 * which represents the schema of the output tuples from the operator. This is a list of Column
 * objects. Each Column has an embedded Table object with the name and alias (if required) fields
 * set appropriately.
 */
public abstract class LogicalOperator {
  protected LogicalOperator child = null;

  protected ArrayList<Column> outputSchema;

  //  public Operator(ArrayList<Column> outputSchema) {
  //    this.outputSchema = outputSchema;
  //  }

  public void setOutputSchema(ArrayList<Column> outputSchema) {
    this.outputSchema = outputSchema;
  }

  public void setChild(LogicalOperator child) {
    this.child = child;
  }

  public LogicalOperator getChild() {
    return this.child;
  }

  public ArrayList<Column> getOutputSchema() {
    return outputSchema;
  }

  public abstract void accept(PhysicalPlanBuilder visitor, Map<String, Table> aliasMap);
}
