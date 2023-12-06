package edu.brown.cs.student.main.Server.CSVRouteUtility;

import edu.brown.cs.student.main.Server.Server;
import edu.brown.cs.student.main.csv.FileUtility.csvFileUtility;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

/** The SearchCSVHandler class handles HTTP requests for searching within loaded CSV data. */
public class SearchCSVHandler implements Route {

  /** Constructs a SearchCSVHandler instance. */
  public SearchCSVHandler() {}

  /**
   * Handles an HTTP request to search within loaded CSV data.
   *
   * @param request The HTTP request object.
   * @param response The HTTP response object.
   * @return A serialized response containing the search results or an error message.
   */
  @Override
  public Object handle(Request request, Response response) {
    Set<String> params = request.queryParams();
    String query = request.queryParams("query");
    String columnToSearch = request.queryParams("columnToSearch");
    Map<String, Object> result = new HashMap<>();
    List<List<String>> matchedRows;
    csvFileUtility searcher = new csvFileUtility(Server.getParser(), Server.getDefensiveRows());

    if (!Server.getFileLoaded()) {
      result.put("result", "error_bad_request");
      result.put("error_message", "You must load a file to search it.");
    } else if (!params.contains("query")) {
      result.put("result", "error_bad_request");
      result.put("error_message", "Please include a query.");
    } else {
      try {
        if (!params.contains("columnToSearch")) {
          matchedRows = searcher.initSearch(query);
        } else {
          matchedRows = searcher.initSearch(query, columnToSearch);
        }
        result.put("result", "success");
        result.put("data", matchedRows);
        result.put("query", query);
        result.put("columnToSearch", columnToSearch);
      } catch (IllegalArgumentException e) {
        result.put("result", "error_bad_request");
        result.put("error_message", e.getMessage());
      }
    }

    CSVResponse CSVResponse = new CSVResponse(result);
    return CSVResponse.serialize();
  }
}
