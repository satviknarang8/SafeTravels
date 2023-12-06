package edu.brown.cs.student.main.csv;

import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Server;
import edu.brown.cs.student.main.csv.Creator.defaultSearchCreator;
import edu.brown.cs.student.main.csv.FileUtility.csvFileUtility;
import edu.brown.cs.student.main.csv.Parser.ParserException;
import edu.brown.cs.student.main.csv.Parser.csvParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/** The Main class of our project. This is where execution begins. */
public final class Main {

  /**
   * The main method allows the user to pass in arguments including the file to read from and
   * information about the query they would like to execute. The query is executed and printed out
   * to the user or an informative error message is displayed.
   *
   * @param args An array of command line arguments (file name, whether the file has header, search
   *     query, column name (optional)
   * @throws DatasourceException
   */
  public static void main(String[] args) throws DatasourceException {
    if (args.length == 0) {
      Server.main(args);
      return;
    }

    if (args.length < 3 || args.length > 4) {
      System.err.println(
          "Please enter the following parameters in order - file name, whether or not the file has "
              + "headers ('True' or 'False'), search query, column name/index (optional).");
      System.exit(0);
    }

    try (FileReader input = new FileReader(args[0])) {
      csvParser<List<String>> parser =
          new csvParser<List<String>>(input, new defaultSearchCreator());

      csvFileUtility searcher = new csvFileUtility(parser, Boolean.valueOf(args[1]));
      System.out.println(
          (args.length == 3)
              ? searcher.initSearch(args[2])
              : searcher.initSearch(args[2], args[3]));

    } catch (FileNotFoundException e) {
      System.err.println("Unable to find the file.");
    } catch (IOException e) {
      System.err.println("We had trouble reading the file. Please try again.");
    } catch (ParserException e) {
      System.err.println("We are unable to parse the file you provided.");
    }
  }
}
