package edu.brown.cs.student.main.api.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.searcher.Search;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This is our SearchCSVHandler which takes care of the searchcsv endpoint and implements the Route
 * interface from the Spark class. It is in charge of finding the values that the user searches for
 * in the currently loaded file
 */
public class SearchCSVHandler implements Route {

  /**
   * A private Map variable that keeps track of the data parsed in order to pass it on to the other
   * handlers
   */
  private final Map<String, Object> sharedState;

  /**
   * Constructor for SearchCSVHandler
   *
   * @param sharedState Takes in a sharedState map object to keep track of data in
   */
  public SearchCSVHandler(Map<String, Object> sharedState) {
    this.sharedState = sharedState;
  }

  /**
   * Handle method from the Route Interface which we override to handle our specific case. Gets the
   * value to search for and the other optional parameters (hasHeaders & columnIdentifiers) and
   * searches for the value in the loaded CSV. If not found, prints appropiate message
   *
   * @param request Request objects passed into handle method containing our query params
   * @param response Response object passed into handle which we do not use
   * @return Returns the serialized Response of a map containing the results of the search
   * @throws Exception If exception occurs, we catch it and convert it to Json
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      List<List<String>> csvData = (List<List<String>>) sharedState.get("csvData");
      String filepath = (String) this.sharedState.get("filepath");
      if (csvData == null || csvData.isEmpty()) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("result", "error_datasource");
        errorResponse.put("error_message", "File has not been loaded");
        return serializeResponse(errorResponse);
      }
      Map<String, Object> result = new HashMap<>();
      String value_to_search_for = request.queryParams("value_to_search_for");
      if (value_to_search_for == null || value_to_search_for.isEmpty()) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("result", "error_bad_request");
        errorResponse.put("error_message", "Value to search for was not included");
        return serializeResponse(errorResponse);
      }
      String columnIdentifier = request.queryParams("columnIdentifier");
      if (columnIdentifier == null) {
        columnIdentifier = "";
      }
      String headersPresent = request.queryParams("hasHeaders");
      boolean hasHeaders;
      if (headersPresent == null || headersPresent.isEmpty()) {
        hasHeaders = false;
      } else {
        hasHeaders = Boolean.parseBoolean(request.queryParams("hasHeaders"));
      }
      Search search = new Search(csvData, value_to_search_for, hasHeaders, columnIdentifier);
      List<List<String>> search_result = search.search();
      result.put("filepath", filepath);
      result.put("value_to_search_for", value_to_search_for);
      result.put("hasHeaders", hasHeaders);
      result.put("columnIdentifier", columnIdentifier);
      List<List<String>> empty_result = new ArrayList<>();
      List<String> empty = Arrays.asList("Value was not found");
      empty_result.add(empty);
      ArrayList<String> errorList = new ArrayList<String>(Arrays.asList("ERROR: "));
      if (search_result.size() == 0) {
        result.put("result", "result_size_0");
        result.put("error_message", empty_result);
        result.put("search_results", empty_result);
        return serializeResponse(result);
      } else {
        result.put("result", "success");
        result.put("search_results", search_result);
      }
      if (search_result.get(0).equals(errorList)) {
        result.put("reason", search_result.subList(1, search_result.size()));
      }
      String serializedResult = serializeResponse(result);
      return serializedResult;
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("result", "error_bad_request");
      errorResponse.put("error_message", e.getMessage());
      return serializeResponse(errorResponse);
    }
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
