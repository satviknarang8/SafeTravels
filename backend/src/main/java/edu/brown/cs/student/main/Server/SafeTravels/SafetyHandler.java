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

import static java.awt.geom.Point2D.distance;
import static java.lang.Math.max;

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

      List<Double> midpoint = calculateMidpoint(startCoordinates.get(0), startCoordinates.get(1), endCoordinates.get(0), endCoordinates.get(1));

// Calculate radius
      int radius = (int) max(distance(midpoint.get(0), midpoint.get(1), startCoordinates.get(0), startCoordinates.get(1)),
              distance(midpoint.get(0), midpoint.get(1), endCoordinates.get(0), endCoordinates.get(1)));

// Use the midpoint and radius for the Amadeus API call
      Map<String, Object> safetyRatings = geocodingClient.getSafetyRatings(midpoint.get(0), midpoint.get(1), radius);

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
  private List<Double> calculateMidpoint(double lat1, double lon1, double lat2, double lon2) {
    double midLat = (lat1 + lat2) / 2.0;
    double midLon = (lon1 + lon2) / 2.0;
    return List.of(midLat, midLon);
  }

}