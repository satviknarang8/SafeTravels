package edu.brown.cs.student.main.Server.CSVRouteUtility;

import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import edu.brown.cs.student.main.Server.Server;
import edu.brown.cs.student.main.csv.Creator.defaultSearchCreator;
import edu.brown.cs.student.main.csv.Parser.ParserException;
import edu.brown.cs.student.main.csv.Parser.csvParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import spark.Request;
import spark.Response;
import spark.Route;

/** The LoadCSVHandler class handles HTTP requests for loading CSV files and parsing them. */
public class LoadCSVHandler implements Route {

  /** Constructs a LoadCSVHandler instance. */
  public LoadCSVHandler() {}

  /**
   * Handles an HTTP request to load and parse a CSV file.
   *
   * @param request The HTTP request object.
   * @param response The HTTP response object.
   * @return A serialized response indicating the success or failure of the operation.
   * @throws Exception If there is an issue with the file handling or parsing.
   */
  @Override
  public Object handle(Request request, Response response) throws InvalidArgsException {
    Map<String, Object> result = new HashMap<>();
    Set<String> params = request.queryParams();
    if (!params.contains("filePath") || !params.contains("hasHeader")) {
      result.put("result", "error_bad_request");
      result.put(
          "error_message", "Please provide a filePath and hasHeader arguments in your request.");
    } else {
      String filePath = request.queryParams("filePath");
      String hasHeader = request.queryParams("hasHeader");

      File currentDir = new File(System.getProperty("user.dir"));
      try (FileReader input = new FileReader(currentDir + "/data/" + filePath)) {
        csvParser<List<String>> parser =
            new csvParser<List<String>>(input, new defaultSearchCreator());
        Server.setRows(parser.readRows(Boolean.valueOf(hasHeader)));
        Server.setFileLoaded(Boolean.TRUE);
        Server.setParser(parser);
        result.put("result", "success");
        result.put("filepath", filePath);
        if (Boolean.valueOf(hasHeader)) {
          Set<String> columnNames = parser.getColumnNames();
          Map<Integer, String> orderedColumns = new TreeMap<>();

          for (String columnName : columnNames) {
            int index = parser.getColumnToIndexMap(columnName);
            orderedColumns.put(index, columnName);
          }

          List<String> orderedColumnList = new ArrayList<>(orderedColumns.values());
          result.put("header", orderedColumnList);
        } else {
          result.put("header", new ArrayList<>());
        }
      } catch (IOException | ParserException e) {
        result.put("result", "error_bad_request");
        result.put(
            "error_message",
            "Please choose a valid CSV file. "
                + filePath
                + " is not present in the \"data\" folder.");
      }
    }
    CSVResponse CSVResponse = new CSVResponse(result);
    return CSVResponse.serialize();
  }
}
