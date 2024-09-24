import static org.junit.jupiter.api.Assertions.assertEquals;

import common.DBCatalog;
import common.Tuple;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;
import visitor.ExpVisitor;

public class ExpVisitorTest {
  // test the function between Long and Long
  @Test
  public void testExpVisitor1() throws URISyntaxException {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();
    URI resourceUri = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(resourceUri);
    Path dbPath = resourcePath.resolve("db");
    DBCatalog.getInstance().setDataDirectory(dbPath.toString());

    ExpVisitor t1 = new ExpVisitor(new Tuple("101,2,3"));
    GreaterThan exp = new GreaterThan();
    // expr: 1 > 3
    exp.setLeftExpression(new LongValue(1));
    exp.setRightExpression(new LongValue(3));
    exp.accept(t1);
    assertEquals(t1.getResult(), false);
    // expr: 3 > 1
    exp.setLeftExpression(new LongValue(3));
    exp.setRightExpression(new LongValue(1));
    exp.accept(t1);
    assertEquals(t1.getResult(), true);
  }

  // test the function between Column and Column
  @Test
  public void testExpVisitor2() throws URISyntaxException {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();
    URI resourceUri = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(resourceUri);
    Path dbPath = resourcePath.resolve("db");
    DBCatalog.getInstance().setDataDirectory(dbPath.toString());

    ExpVisitor t1 = new ExpVisitor(new Tuple("101,2,3"));
    Table table = new Table();
    table.setName("Boats");
    Column column0 = new Column();
    column0.setTable(table);
    column0.setColumnName("D");
    Column column1 = new Column();
    column1.setTable(table);
    column1.setColumnName("E");

    GreaterThan exp = new GreaterThan();
    exp.setLeftExpression(new LongValue(100));
    exp.setRightExpression(column0);
    // expr: 100 > tuple[0]:101
    exp.accept(t1);
    assertEquals(t1.getResult(), false);
    // expr: tuple[0]:103 > 101
    exp.setLeftExpression(column0);
    exp.setRightExpression(new LongValue(100));
    exp.accept(t1);
    assertEquals(t1.getResult(), true);
    // expr: tuple[0]:103  > tuple[1]:2
    exp.setLeftExpression(column0);
    exp.setRightExpression(column1);
    exp.accept(t1);
    assertEquals(t1.getResult(), true);
  }

  // test the function with AndExpression
  @Test
  public void testExpVisitor3() throws URISyntaxException {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();
    URI resourceUri = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(resourceUri);
    Path dbPath = resourcePath.resolve("db");
    DBCatalog.getInstance().setDataDirectory(dbPath.toString());

    ExpVisitor t1 = new ExpVisitor(new Tuple("101,2,3"));
    Table table = new Table();
    table.setName("Boats");
    Column column0 = new Column();
    column0.setTable(table);
    column0.setColumnName("D");
    Column column1 = new Column();
    column1.setTable(table);
    column1.setColumnName("E");

    // expr: 100 > tuple[0]:101 false   AND    expr: tuple[0]:103 > 101 true
    GreaterThan exp1 = new GreaterThan();
    exp1.setLeftExpression(new LongValue(100));
    exp1.setRightExpression(column0);
    GreaterThan exp2 = new GreaterThan();
    exp2.setLeftExpression(column0);
    exp2.setRightExpression(new LongValue(100));

    AndExpression and1 = new AndExpression();
    and1.setLeftExpression(exp1);
    and1.setRightExpression(exp2);
    and1.accept(t1);
    assertEquals(t1.getResult(), false);

    // expr : 1 < 2 AND  tuple[0]:103 > 101 true
    MinorThan exp3 = new MinorThan();
    exp3.setLeftExpression(new LongValue(1));
    exp3.setRightExpression(new LongValue(2));
    and1.setLeftExpression(exp3);
    and1.setRightExpression(exp2);
    and1.accept(t1);
    assertEquals(t1.getResult(), true);

    // expr : 1 < 2 AND  tuple[0]:103 > 101 true  AND  expr: 100 > tuple[0]:101 false
    AndExpression and2 = new AndExpression(and1, exp1);
    and2.accept(t1);
    assertEquals(t1.getResult(), false);
  }
}
