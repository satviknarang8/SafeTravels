package edu.brown.cs.student.main.api.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This is our ViewCSVHandler class which takes care of the viewcsv endpoint and implements the
 * Route interface from the Spark class. It is in charge of viewing the currently loaded CSV file
 * and presenting it to the user
 */
public class ViewCSVHandler implements Route {
  /**
   * A private Map variable that keeps track of the data parsed in order to pass it on to the other
   * handlers
   */
  private final Map<String, Object> sharedState;

  /**
   * Constructor for ViewCSVHandler
   *
   * @param sharedState Takes in a sharedState map object to keep track of data in
   */
  public ViewCSVHandler(Map<String, Object> sharedState) {
    this.sharedState = sharedState;
  }

  /**
   * Handle method from the Route Interface which we override to handle our specific case. Views the
   * csv currently in the sharedState and converts it to JSON
   *
   * @param request Request objects passed into handle method containing our query params (if any)
   * @param response Response object passed into handle which we do not use
   * @return Returns the serialized Response of a map containing the results of the search
   * @throws Exception If exception occurs, we catch it and convert it to Json
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    List<List<String>> csvData = (List<List<String>>) this.sharedState.get("csvData");
    if (csvData == null || csvData.isEmpty()) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("result", "error_datasource");
      errorResponse.put("error_message", "csv file has not been loaded");
      return serializeResponse(errorResponse);
    }
    Map<String, Object> resultResponse = new HashMap<>();
    resultResponse.put("result", "success");
    resultResponse.put("data", csvData);
    return serializeResponse(resultResponse);
  }

  /**
   * Takes in a response map and serializes it using the JSONAdapter to convert it into a JSON
   * format
   *
   * @param responseMap The Map to seralize into a JSON string
   * @return Returns the JSON String
   */
  private String serializeResponse(Map<String, Object> responseMap) {
    try {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter((Type) Map.class);
      return jsonAdapter.toJson(responseMap);
    } catch (Exception e) {
      Moshi moshi = new Moshi.Builder().build();
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("result", "error_bad_json");
      errorResponse.put("error_message", e.getMessage());
      JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter((Type) Map.class);
      return jsonAdapter.toJson(errorResponse);
    }
  }
}
