// compiler for test
/*******************************************************************************************/

/****************************************************************************************/
// compiler for jar
/****************************************************************************************/
package compiler;

import LogicalOperator.LogicalOperator;
import PhysicalOperator.PhysicalOperator;
import common.DBCatalog;
import common.QueryPlanBuilder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.apache.logging.log4j.*;
import tools.IO.TupleWriter;
import tools.IO.TupleWriterBinImpl;
import tools.config.ConfigHelper;
import visitor.PhysicalPlanBuilder;

/**
 * Top level harness class; reads queries from an input file one at a time, processes them and sends
 * output to file or to System depending on flag.
 */
public class Compiler {
  private static final Logger logger = LogManager.getLogger();

  private static String outputDir;
  private static String inputDir;
  private static String tempDir;
  private static final boolean outputToFiles = true; // true = output to

  // files, false = output
  // to System.out

  /**
   * Reads statements from queriesFile one at a time, builds query plan and evaluates, dumping
   * results to files or console as desired.
   *
   * <p>If dumping to files result of ith query is in file named queryi, indexed stating at 1.
   */
  public static void main(String[] args) {

    inputDir = args[0];
    outputDir = args[1];
    tempDir = args[2];
    DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
    ConfigHelper configHelper = new ConfigHelper(inputDir, tempDir);
    try {
      String str = Files.readString(Paths.get(inputDir + "/queries.sql"));

      Statements statements = CCJSqlParserUtil.parseStatements(str);
      QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();
      PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder(configHelper);

      // clear the ouput
      if (outputToFiles) {
        for (File file : (new File(outputDir).listFiles())) file.delete();
      }

      int counter = 1; // for numbering output files
      for (Statement statement : statements.getStatements()) {

        logger.info("Processing query: " + statement);
        clearDirectory(tempDir);
        try {
          LogicalOperator lPlan = queryPlanBuilder.buildPlan(statement);
          PhysicalOperator plan =
              physicalPlanBuilder.buildPlan(lPlan, queryPlanBuilder.getAliasMap());
          TupleWriter tp;
          if (outputToFiles) {
            File outfile = new File(outputDir + "/query" + counter);
            //            tp = new TupleWriterHumanImpl(outfile);
            //            plan.dump(tp);
            //            File outfile = new File(outputDir + "/queryBin" + counter);
            tp = new TupleWriterBinImpl(outfile);
            plan.dump(tp);
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

  public static void clearDirectory(String folderPath) {
    File folder = new File(folderPath);

    // check valid
    if (!folder.exists() || !folder.isDirectory()) {
      throw new IllegalArgumentException(
          "The provided path is not a valid directory: " + folderPath);
    }

    // recursively delete all the files
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        deleteRecursively(file);
      }
    }
  }

  private static void deleteRecursively(File file) {
    // if directory
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    // if file, just delete
    if (!file.delete()) {
      throw new IllegalStateException(
          "Failed to delete file or directory: " + file.getAbsolutePath());
    }
  }
}
