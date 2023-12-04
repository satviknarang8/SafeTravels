package edu.brown.cs.student.main.api.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.creator.CreatorFromRow;
import edu.brown.cs.student.main.exceptions.ParserException;
import edu.brown.cs.student.main.parser.Parser;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This is our LoadCSVHandler which takes care of the loadCSV endpoint and implements the Route
 * interface from the Spark class
 */
public class LoadCSVHandler implements Route {

  /**
   * A private Map variable that keeps track of the data parsed in this class in order to pass it on
   * to the other handlers
   */
  private final Map<String, Object> sharedState;

  /**
   * A simple instance of the CreatorFromRow interface that returns the row it takes in as a List of
   * Strings
   */
  private final CreatorFromRow<List<String>> creator = row -> row;

  /**
   * The constructor for the loadCSV handler
   *
   * @param sharedState Takes in a sharedState map object to keep track of data in
   */
  public LoadCSVHandler(Map<String, Object> sharedState) {
    this.sharedState = sharedState;
  }

  /**
   * Handle method from the Route Interface which we override to handle our specific case. Gets the
   * filename and hasHeaders from queryParameters and then proceeds to load it into the sharedState
   *
   * @param request Request objects passed into handle method containing our query params
   * @param response Response object passed into handle which we do not use
   * @return Returns the serialized Response of a map containing the result and filepath
   * @throws Exception If exception occurs, we catch it and convert it to Json
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      String filepath = request.queryParams("filename");
      boolean hasHeaders = Boolean.parseBoolean(request.queryParams("hasHeaders"));
      List<List<String>> csvData = getCSV(filepath, hasHeaders);
      if (csvData == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("result", "error_datasource");
        errorResponse.put("reason", "File not found");
        return serializeResponse(errorResponse);
      }
      this.sharedState.put("filepath", filepath);
      this.sharedState.put("csvData", csvData);
      Map<String, Object> loadResponse = new HashMap<>();
      loadResponse.put("result", "success");
      loadResponse.put("filepath", filepath);
      return serializeResponse(loadResponse);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("result", "error_datasource");
      errorResponse.put("reason", e.getMessage());
      return serializeResponse(errorResponse);
    }
  }

  /**
   * Gets all the data from the CSV File using the Parser Class from the CSV project
   *
   * @param filepath Filepath to get data from
   * @param hasHeaders Boolean to check for the presence of headers in the file
   * @return Returns a List of List of strings of all the rows in the csv file
   * @throws ParserException Throws this exception if there is an error in parsing the file
   */
  private List<List<String>> getCSV(String filepath, boolean hasHeaders) throws ParserException {
    try {
      FileReader fileReader = new FileReader(filepath);
      Parser<List<String>> parser = new Parser<>(fileReader, this.creator, hasHeaders);
      return parser.parse();
    } catch (Exception e) {
      return null;
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
