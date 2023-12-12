package edu.brown.cs.student.main.Server.SafeTravels;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import spark.Request;
import spark.Response;
import spark.Route;

public class SafetyHandler implements Route {

  public SafetyHandler() {
  }

  /**
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    GeocodingAPIClient geocodingClient = new GeocodingAPIClient();
    String startLoc = request.queryParams("start");
    String endLoc = request.queryParams("end");

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();

    // Validate start and end locations
    if (startLoc == null || endLoc == null) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "bad_request");
      responseMap.put("details", "Starting or ending location was not provided");
      return adapter.toJson(responseMap);
    }

    try {
      // Get coordinates for start and end locations
      List<Double> startCoordinates = geocodingClient.getCoordinates(startLoc);
      List<Double> endCoordinates = geocodingClient.getCoordinates(endLoc);

      // Create bounding box and call Amadeus API (pseudo-code)
      // BoundingBox bbox = createBoundingBox(startCoordinates, endCoordinates);
      // List<SafetyRating> safetyRatings = callAmadeusSafePlaceApi(bbox, apiKey);

      // Add the safety ratings to the response
      // responseMap.put("safety_ratings", safetyRatings);
    } catch (DatasourceException e) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "datasource");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    }

    responseMap.put("type", "success");
    responseMap.put("date_time", LocalDateTime.now().toString());
    return adapter.toJson(responseMap);
  }
}