import PhysicalOperator.Operator;
import PhysicalOperator.ScanOperator;
import common.DBCatalog;
import common.Tuple;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

public class OperatorTests {
  private ArrayList<Column> tmp = new ArrayList<>();

  @Test
  public void testScanOperator() throws Exception {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();

    URI resourceUri = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();

    Path resourcePath = Paths.get(resourceUri);
    Path dbPath = resourcePath.resolve("db");
    DBCatalog.getInstance().setDataDirectory(dbPath.toString());

    Operator s0 = new ScanOperator("Boats");
    // test getNextTuple()
    Tuple t;
    while ((t = s0.getNextTuple()) != null) {
      System.out.println(t);
    }
    System.out.println();
    // test reset()
    s0 = new ScanOperator("Boats");
    System.out.println(s0.getNextTuple());
    System.out.println(s0.getNextTuple());
    s0.reset();
    System.out.println(s0.getNextTuple());
    System.out.println();
    // test top-level  read from sql
    /*
    * • while more queries remain, parse the next query in the queries file
      • construct a ScanOperator for the table in the fromItem
      • call dump() on your ScanOperator to send the results somewhere helpful, like a file or your console
     */
    URI queriesUri =
        Objects.requireNonNull(classLoader.getResource("samples/input/queries.sql")).toURI();
    Path queriesFilePath = Paths.get(queriesUri);
    Statements statements = CCJSqlParserUtil.parseStatements(Files.readString(queriesFilePath));
    List<Statement> statementList = statements.getStatements();

    for (Statement stmt : statementList) {
      Select sql = (Select) stmt;
      PlainSelect body = (PlainSelect) sql.getSelectBody();
      Table fromItem = (Table) body.getFromItem();
      Alias alias = fromItem.getAlias();
      String name = fromItem.getName();
      ScanOperator s1 = new ScanOperator(name);
      s1.dump(System.out);
      s1.close();
      System.out.println();
    }
  }
}
