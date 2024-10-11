package visitor;

import common.DBCatalog;
import common.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import tools.alias.AliasTool;

public class ExpVisitor extends ExpressionVisitorAdapter {
  private Tuple tuple;
  private Boolean result;
  private Long data;
  private Map<String, ArrayList<Column>> schema;
  private Map<String, Table> aliasMap;

  // Map<table, List<Col>>
  public ExpVisitor(Tuple tuple, Map<String, Table> aliasMap) {
    result = false;
    this.tuple = tuple;
    this.aliasMap = aliasMap;
    this.schema = DBCatalog.getInstance().getSchema();
  }

  public Boolean getResult() {
    return result;
  }

  @Override
  public void visit(LongValue value) {
    data = value.getValue();
    result = true;
  }

  @Override
  public void visit(Column column) {
    // map to original tableName
    String tableName = AliasTool.getTableName(column, aliasMap);
    // get ColumnName
    ArrayList<Column> columns = schema.get(tableName);
    // Map ColumnName to Data
    Map<String, Integer> dataMap = new HashMap<>();
    for (int i = 0; i < columns.size(); i++) {
      String cName = columns.get(i).getColumnName();
      dataMap.put(cName, i);
    }

    String columnName = column.getColumnName();
    if (dataMap.containsKey(columnName)) {
      Integer tmp = tuple.getElementAtIndex(dataMap.get(columnName));
      data = tmp.longValue();
    } else {
      data = null;
    }
  }

  @Override
  public void visit(EqualsTo expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = data;

    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = data;
    if (resLeft == null || resRight == null) {
      result = false; // If either side is null, the result is false
    } else {
      result = resLeft.equals(resRight);
    }
  }

  @Override
  public void visit(NotEqualsTo expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = data;

    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = data;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft != resRight;
  }

  @Override
  public void visit(GreaterThan expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = data;

    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = data;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft > resRight;
  }

  @Override
  public void visit(GreaterThanEquals expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = data;

    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = data;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft >= resRight;
  }

  @Override
  public void visit(MinorThan expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = data;

    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = data;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft < resRight;
  }

  @Override
  public void visit(MinorThanEquals expr) {
    Expression leftExp = expr.getLeftExpression();
    leftExp.accept(this);
    Long resLeft = data;

    Expression rightExp = expr.getRightExpression();
    rightExp.accept(this);
    Long resRight = data;

    if (resLeft == null || resRight == null) result = false;
    else result = resLeft <= resRight;
  }
}
