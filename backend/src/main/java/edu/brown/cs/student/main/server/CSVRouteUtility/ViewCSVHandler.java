package edu.brown.cs.student.main.Server.CSVRouteUtility;

import edu.brown.cs.student.main.Server.Server;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/** The ViewCSVHandler class handles HTTP requests for viewing loaded CSV data. */
public class ViewCSVHandler implements Route {

  /** Constructs a ViewCSVHandler instance. */
  public ViewCSVHandler() {}

  /**
   * Handles an HTTP request to view loaded CSV data.
   *
   * @param request The HTTP request object.
   * @param response The HTTP response object.
   * @return A serialized response containing the loaded CSV data or an error message.
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> result = new HashMap<>();
    if (!Server.getFileLoaded()) {
      result.put("result", "error_bad_request");
      result.put("error_message", "You must load a file to view it.");
    } else {
      result.put("result", "success");
      result.put("data", Server.getDefensiveRows());
    }

    CSVResponse CSVResponse = new CSVResponse(result);
    return CSVResponse.serialize();
  }
}
