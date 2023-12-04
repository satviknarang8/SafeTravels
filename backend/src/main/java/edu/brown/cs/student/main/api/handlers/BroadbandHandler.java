package edu.brown.cs.student.main.api.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.acsdatasource.OLD_ACSDatasource;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the broadband endpoint, which sends back the broadband data from the American Community
 * Survey (ACS).
 */
public class BroadbandHandler implements Route {
  private final OLD_ACSDatasource acsDatasource;

  public BroadbandHandler(OLD_ACSDatasource acsDatasource) {
    this.acsDatasource = acsDatasource;
  }

  /**
   * Gets the names of the state and county for which broadband data is requested from the query
   * parameters. Then it creates a BroadbandSource, which implements the OLD_ACSDatasource interface and
   * is responsible for using the ACS API to return results. In its getBroadbandData() method, the
   * BroadbandSource creates an instance of the OLD_CensusAPIClient class, which is responsible for
   * using the names of the state and county to retrieve the state code and county code from the ACS
   * API. Also, serializes the response and returns a Json object.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    try {
      Moshi moshi = new Moshi.Builder().build();
      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
      Map<String, Object> responseMap = new HashMap<>();
      if (state == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "bad_request");
        responseMap.put("details", "State was not given in query");
        responseMap.put("type", "fail");
        responseMap.put("date_time", LocalDateTime.now().toString());
        return adapter.toJson(responseMap);
      }
      if (county == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "bad_request");
        responseMap.put("details", "County was not given in query");
        responseMap.put("type", "fail");
        responseMap.put("date_time", LocalDateTime.now().toString());
        return adapter.toJson(responseMap);
      }
      List<List<String>> broadbandData = this.acsDatasource.getBroadbandData(state, county);
      if (broadbandData == null || broadbandData.size() == 0) {
        responseMap.put("broadband_data", "does not exist");
      } else {
        responseMap.put("broadband_data", broadbandData);
      }
      responseMap.put("type", "success");
      responseMap.put("date_time", LocalDateTime.now().toString());
      responseMap.put("state", state);
      responseMap.put("county", county);
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      Moshi moshi = new Moshi.Builder().build();
      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
      Map<String, Object> responseMap = new HashMap<>();
      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
      responseMap.put("type", "error");
      responseMap.put("error_type", "datasource");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    }
  }
}
