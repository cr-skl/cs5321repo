package visitor;

import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

@Deprecated
/**
 * originally designed to deal with WHERE clause for a single table now considering the JOIN
 * operation relating with several tables it is of no use now
 */
public class ExpVisitor extends ExpressionVisitorAdapter {
  private Tuple tuple;
  private Boolean result;
  private Long data;
  private Boolean dataVisited;
  private Map<String, ArrayList<Column>> schema;

  // Map<table, List<Col>>
  public ExpVisitor(Tuple tuple) {
    result = false;
    dataVisited = false;
    this.tuple = tuple;
    this.schema = DBCatalog.getInstance().getSchema();
  }

  public Boolean getResult() {
    return result;
  }

  @Override
  public void visit(LongValue value) {
    dataVisited = true;
    data = value.getValue();
    result = true;
  }

  @Override
  public void visit(AndExpression expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Boolean resLeft = result;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Boolean resRight = result;
    result = resLeft && resRight;
  }

  @Override
  public void visit(EqualsTo expr) {
    dataVisited = false;
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = dataVisited ? data : null;

    dataVisited = false;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = dataVisited ? data : null;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft == resRight;
  }

  @Override
  public void visit(NotEqualsTo expr) {
    dataVisited = false;
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = dataVisited ? data : null;

    dataVisited = false;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = dataVisited ? data : null;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft != resRight;
  }

  @Override
  public void visit(GreaterThan expr) {
    dataVisited = false;
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = dataVisited ? data : null;

    dataVisited = false;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = dataVisited ? data : null;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft > resRight;
  }

  @Override
  public void visit(GreaterThanEquals expr) {
    dataVisited = false;
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = dataVisited ? data : null;

    dataVisited = false;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = dataVisited ? data : null;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft >= resRight;
  }

  @Override
  public void visit(MinorThan expr) {
    dataVisited = false;
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = dataVisited ? data : null;

    dataVisited = false;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = dataVisited ? data : null;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft < resRight;
  }

  @Override
  public void visit(MinorThanEquals expr) {
    dataVisited = false;
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = dataVisited ? data : null;

    dataVisited = false;
    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = dataVisited ? data : null;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft <= resRight;
  }

  @Override
  public void visit(Column column) {
    // From expr:  column
    String tableName = column.getTable().getName();
    String columnName = column.getColumnName();
    // Build the map for specific table
    ArrayList<Column> columns = schema.get(tableName);
    Map<String, Integer> columnMap = new HashMap<>();
    for (int i = 0; i < columns.size(); i++) {
      String cName = columns.get(i).getColumnName();
      columnMap.put(cName, i);
    }
    // return corresponding column Value
    if (columnMap.containsKey(columnName)) {
      Integer tmp = tuple.getElementAtIndex(columnMap.get(columnName));
      data = tmp.longValue();
      dataVisited = true;
    } else {
      throw new RuntimeException("Column not found in tuple");
    }
  }
}
