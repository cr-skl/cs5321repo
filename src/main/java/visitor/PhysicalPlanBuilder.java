package visitor;

import LogicalOperator.*;
import PhysicalOperator.*;
import java.net.URISyntaxException;
import java.util.Map;
import net.sf.jsqlparser.schema.Table;
import tools.config.ConfigHelper;

/** class to turn logical query plans into physical query plans */
public class PhysicalPlanBuilder {

  private int joinType, joinBufPages, sortType, sortBufPages;
  private String tempDir;

  /** Read config file and make a physical plan builder */
  public PhysicalPlanBuilder(ConfigHelper configHelper) throws URISyntaxException {
    joinType = configHelper.joinType;
    joinBufPages = configHelper.joinBufPages;
    sortType = configHelper.sortType;
    sortBufPages = configHelper.sortBufPages;
    tempDir = configHelper.tempDir;
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
  public PhysicalOperator buildPlan(LogicalOperator logicalPlan, Map<String, Table> aliasMap) {

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
      PhysicalOperator l = buildPlan(lOp.getLeftChild(), aliasMap);
      PhysicalOperator r = buildPlan(lOp.getRightChild(), aliasMap);
      JoinOperator op = null;
      if (joinType == 0) op = new TNLJ_Operator(l, r, lOp.getExpression()); // TNLJ
      else if (joinType == 1)
        op = new BNLJ_Operator(l, r, lOp.getExpression(), joinBufPages); // TODO: BNLJ
      else if (joinType == 2)
        ; // TODO: SMJ
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
            new IntSortOperator(
                ((LogicalSortOp) curr).getOrderByElements(), aliasMap); // in-memory sort
      else if (sortType == 1)
        op =
            new ExtSortOperator(
                ((LogicalSortOp) curr).getOrderByElements(),
                aliasMap,
                tempDir,
                sortBufPages,
                false); // TODO: external sort
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
