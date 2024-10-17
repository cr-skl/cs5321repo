package visitor;

import LogicalOperator.*;
import PhysicalOperator.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import net.sf.jsqlparser.schema.Table;
import org.apache.logging.log4j.LogManager;

/** class to turn logical query plans into physical query plans */
public class PhysicalPlanBuilder {

  private int joinType, joinBufPages, sortType, sortBufPages;

  /** Read config file and make a physical plan builder */
  public PhysicalPlanBuilder(String path) throws URISyntaxException {
    // ClassLoader classLoader = PhysicalPlanBuilder.class.getClassLoader();
    // URI InputURI = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();

    // Path config = Paths.get(InputURI).resolve("plan_builder_config.txt");
    Path config = Paths.get(path + "/plan_builder_config.txt");
    try {
      // read config from txt file
      BufferedReader br = new BufferedReader(new FileReader(config.toString()));
      String[] params = br.readLine().split("\\s");
      joinType = Integer.valueOf(params[0]);
      joinBufPages = 0;
      if (joinType == 1) joinBufPages = Integer.valueOf(params[1]);
      else if (joinType != 0 && joinType != 2)
        throw new IllegalArgumentException("Join type must be 0, 1, or 2");
      params = br.readLine().split("\\s");
      sortType = Integer.valueOf(params[0]);
      sortBufPages = 0;
      if (sortType == 1) sortBufPages = Integer.valueOf(params[1]);
      else if (sortType != 0) throw new IllegalArgumentException("Sort type must be 0 or 1");
      br.close();
    } catch (IOException e) {
      LogManager.getLogger().error(e.getMessage());
    }
  }

  /**
   * Take in the root of a tree representing a logical query plan and the alias map and return the
   * root of a tree representing an appropriate physical query plan. Iterates over the logical query
   * plan and turns it into a physical query plan. This function recursively calls itself to build
   * the plan for children of a node.
   *
   * @param logicalPlan the root of a tree representing a logical query plan
   * @param aliasMap The alias map for this query
   * @return The root of a tree representing a physical query plan. Null if the input tree is
   *     empty/null
   */
  public Operator buildPlan(LogicalOperator logicalPlan, Map<String, Table> aliasMap) {

    LogicalOperator curr = logicalPlan;
    if (curr instanceof LogicalScanOp) { // scan
      // leaf node, so we don't need to set children or output schema
      return new ScanOperator(
          ((LogicalScanOp) curr).getTableName(), ((LogicalScanOp) curr).getTable(), aliasMap);
    } else if (curr instanceof LogicalSelectOp) { // select
      SelectOperator op = new SelectOperator(((LogicalSelectOp) curr).getExpression(), aliasMap);
      op.setChild(buildPlan(curr.getChild(), aliasMap));
      op.setOutputSchema(curr.getOutputSchema());
      return op;
    } else if (curr instanceof LogicalJoinOp) { // join
      LogicalJoinOp lOp = (LogicalJoinOp) curr;
      Operator r = buildPlan(lOp.getRightChild(), aliasMap);
      Operator l = buildPlan(lOp.getLeftChild(), aliasMap);
      JoinOperator op = null;
      if (joinType == 0) op = new JoinOperator(r, lOp.getExpression()); // TNLJ
      else if (joinType == 1)
        ; // TODO: BNLJ
      else if (joinType == 2)
        ; // TODO: SMJ
      op.setLeftChild(l);
      op.setOutputSchema(lOp.getOutputSchema());
      return op;
    } else if (curr instanceof LogicalProjectOp) { // project
      ProjectOperator op =
          new ProjectOperator(((LogicalProjectOp) curr).getSelectItemList(), aliasMap);
      op.setChild(buildPlan(curr.getChild(), aliasMap));
      return op;
    } else if (curr instanceof LogicalSortOp) { // sort
      SortOperator op = null;
      if (sortType == 0)
        op =
            new SortOperator(
                ((LogicalSortOp) curr).getOrderByElements(), aliasMap); // in-memory sort
      else if (sortType == 1)
        ; // TODO: external sort
      // should never throw a null pointer exception since if sortType is not 0 or 1,
      // an exception will be thrown in the constructor for the plan builder
      op.setChild(buildPlan(curr.getChild(), aliasMap));
      op.setOutputSchema(curr.getOutputSchema());
      return op;
    } else if (curr instanceof LogicalDedupOp) { // distinct
      DedupOperator op = new DedupOperator();
      op.setChild(buildPlan(curr.getChild(), aliasMap));
      op.setOutputSchema(curr.getOutputSchema());
      return op;
    } else return null;
  }
}
