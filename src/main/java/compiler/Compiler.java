// compiler for test
/*******************************************************************************************/
package compiler;

import common.DBCatalog;
import common.QueryPlanBuilder;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import PhysicalOperator.Operator;
import LogicalOperator.LogicalOperator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.IO.TupleWriter;
import tools.IO.TupleWriterBinImpl;
import visitor.PhysicalPlanBuilder;


/**
 * Top level harness class; reads queries from an input file one at a time, processes them and
 * sends output to file or to System depending on flag.
 */

 public class Compiler {

  private static final Logger logger = LogManager.getLogger();
  private static final boolean outputToFiles = true; // true = output to
  private static String outputDir;
  private static String inputDir;

  // files, false = output
  // to System.out

  /**
   * Reads statements from queriesFile one at a time, builds query plan and evaluates, dumping
   * results to files or console as desired.
   *
   * <p>If dumping to files result of ith query is in file named queryi, indexed stating at 1.
   */
  public static void main(String[] args) throws URISyntaxException {

    inputDir = args[0];
    outputDir = args[1];
    ClassLoader classLoader = Compiler.class.getClassLoader();
    URI InputURI = Objects.requireNonNull(classLoader.getResource(inputDir)).toURI();
    URI OutputURI = Objects.requireNonNull(classLoader.getResource(outputDir)).toURI();
    DBCatalog.getInstance().setDataDirectory(Paths.get(InputURI).resolve("db").toString());
    logger.info("schema Directory:" + inputDir + "/db");
    String query = "testqueries.sql";
    logger.info("sql Directory:" + inputDir + "/testqueries.sql");
    try {
      //      Statements statements =
      //
      CCJSqlParserUtil.parseStatements(
          Files.readString(Paths.get(InputURI).resolve("testqueries.sql")));
      Statements statements =
          CCJSqlParserUtil.parseStatements(
              Files.readString(Paths.get(InputURI).resolve("testqueries.sql")));
      QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();
      PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder(inputDir);

      if (outputToFiles) {
        // directory
        File[] files = new File(OutputURI).listFiles();
        if (files != null) {
          for (File file : files) {
            if (file != null && file.isFile()) {
              file.delete();
            }
          }
        } else {
          logger.warn("Output directory is empty or cannot be read: " + outputDir);
        }
      }

      int counter = 1; // for numbering output files
      for (Statement statement : statements.getStatements()) {

        logger.info("Processing query: " + statement);
        PrintStream out = null;
        try {
          LogicalOperator lPlan = queryPlanBuilder.buildPlan(statement);
          Operator plan = physicalPlanBuilder.buildPlan(lPlan, queryPlanBuilder.getAliasMap());

          if (outputToFiles) {
            // human
            //            File outfile = new File(Paths.get(OutputURI).resolve("query" +
            // counter).toString());
            //            TupleWriter writer = new TupleWriterHumanImpl(outfile);
            File outfile = new File(Paths.get(OutputURI).resolve("queryBin" + counter).toString());
            // binary
            TupleWriter writer = new TupleWriterBinImpl(outfile);
            plan.dump(writer);
          } else {
            plan.dump(System.out);
          }
        } catch (Exception e) {
          logger.error(e.getMessage());
        }

        ++counter;
      }
    } catch (Exception e) {
      System.err.println("Exception occurred in interpreter");
      logger.error(e.getMessage());
    }
  }
 }
/****************************************************************************************/
// compiler for jar
/****************************************************************************************/
//package compiler;
//
//import LogicalOperator.LogicalOperator;
//import PhysicalOperator.Operator;
//import common.DBCatalog;
//import common.QueryPlanBuilder;
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.Statements;
//import org.apache.logging.log4j.*;
//import tools.IO.TupleWriter;
//import tools.IO.TupleWriterBinImpl;
//import visitor.PhysicalPlanBuilder;
//
///**
// * Top level harness class; reads queries from an input file one at a time, processes them and sends
// * output to file or to System depending on flag.
// */
//public class Compiler {
//  private static final Logger logger = LogManager.getLogger();
//
//  private static String outputDir;
//  private static String inputDir;
//  private static final boolean outputToFiles = true; // true = output to
//
//  // files, false = output
//  // to System.out
//
//  /**
//   * Reads statements from queriesFile one at a time, builds query plan and evaluates, dumping
//   * results to files or console as desired.
//   *
//   * <p>If dumping to files result of ith query is in file named queryi, indexed stating at 1.
//   */
//  public static void main(String[] args) {
//
//    inputDir = args[0];
//    outputDir = args[1];
//    DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
//    try {
//      String str = Files.readString(Paths.get(inputDir + "/queries.sql"));
//      //      String str = Files.readString(Paths.get(inputDir + "/testqueries.sql"));
//
//      Statements statements = CCJSqlParserUtil.parseStatements(str);
//      QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();
//      PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder(inputDir);
//
//      if (outputToFiles) {
//        for (File file : (new File(outputDir).listFiles())) file.delete();
//      }
//
//      int counter = 1; // for numbering output files
//      for (Statement statement : statements.getStatements()) {
//
//        logger.info("Processing query: " + statement);
//
//        try {
//          LogicalOperator lPlan = queryPlanBuilder.buildPlan(statement);
//          Operator plan = physicalPlanBuilder.buildPlan(lPlan, queryPlanBuilder.getAliasMap());
//
//          if (outputToFiles) {
//            File outfile = new File(outputDir + "/query" + counter);
//            TupleWriter writer = new TupleWriterBinImpl(outfile);
//            plan.dump(writer);
//          } else {
//            plan.dump(System.out);
//          }
//        } catch (Exception e) {
//          logger.error(e.getMessage());
//        }
//
//        ++counter;
//      }
//    } catch (Exception e) {
//      System.err.println("Exception occurred in interpreter");
//      logger.error(e.getMessage());
//    }
//  }
//}
