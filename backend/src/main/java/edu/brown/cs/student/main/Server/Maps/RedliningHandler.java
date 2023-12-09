package edu.brown.cs.student.main.Server.Maps;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class RedliningHandler implements Route {
  private final JsonAdapter<Map<String, Object>> geoJsonAdapter;

  public RedliningHandler() {
    // Initialize Moshi for parsing GeoJSON
    Moshi moshi = new Moshi.Builder().build();
    // Define the type for parsing the GeoJSON
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    geoJsonAdapter = moshi.adapter(type);
  }

  @Override
  public Object handle(Request request, Response response) {
    if (request.queryParams().size() != 4) {
      response.status(400);
      return "Error: bad request";
    }

    try {
      // Parse the GeoJSON data from a JSON file or a string (wherever your data is stored)
      File currentDir = new File(System.getProperty("user.dir"));
      String filePath = currentDir + "/data/redlining/data.json";
      File jsonFile = new File(filePath);
      String geoJsonData = "";

      try {
        // Read the content of the JSON file
        byte[] jsonData = Files.readAllBytes(jsonFile.toPath());
        geoJsonData = new String(jsonData, StandardCharsets.UTF_8);

        // Rest of your code for parsing and processing GeoJSON
      } catch (IOException e) {
        e.printStackTrace();
        // Handle any exceptions that occur while reading the file
      }

      Map<String, Object> geoJsonMap = geoJsonAdapter.fromJson(geoJsonData);

      // Extract the "features" array from the GeoJSON
      List<Map<String, Object>> features = (List<Map<String, Object>>) geoJsonMap.get("features");

      // Parse the bounding box parameters from the request
      double minLat = Double.parseDouble(request.queryParams("minLat"));
      double maxLat = Double.parseDouble(request.queryParams("maxLat"));
      double minLon = Double.parseDouble(request.queryParams("minLon"));
      double maxLon = Double.parseDouble(request.queryParams("maxLon"));

      // Initialize a list to store filtered features
      List<Map<String, Object>> filteredFeatures = new ArrayList<>();

      // Loop through the features and filter based on the bounding box
      for (Map<String, Object> feature : features) {
        try {
          Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
          if (geometry == null) {
            // Handle the case where "geometry" is null
            continue; // Skip this feature and continue with the next one
          }
          List<List<List<List<Double>>>> coordinates =
              (List<List<List<List<Double>>>>) geometry.get("coordinates");

          // Assuming coordinates are in the format: [[[lon, lat], [lon, lat], ...]]
          // Check if any coordinate in the feature is outside the bounding box
          boolean addFeature = true;

          for (List<List<List<Double>>> polygon : coordinates) {
            for (List<List<Double>> points : polygon) {
              for (List<Double> point : points) {
                double lon = point.get(0); // Extract longitude from the first index
                double lat = point.get(1); // Extract latitude from the second index

                if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) {
                  addFeature = false;
                  break; // Exit the innermost loop
                }
              }
              if (!addFeature) {
                break; // Exit the middle loop
              }
            }
            if (!addFeature) {
              break; // Exit the outermost loop
            }
          }

          if (addFeature) {
            filteredFeatures.add(feature);
          }
        } catch (Exception e) {
          // Handle exceptions for the current feature here
          e.printStackTrace(); // You can print the stack trace for debugging
        }
      }

      // Create a new GeoJSON object with filtered features
      Map<String, Object> filteredGeoJson = new HashMap<>();
      filteredGeoJson.put("type", "FeatureCollection");
      filteredGeoJson.put("features", filteredFeatures);

      // Serialize the filtered GeoJSON to a JSON string and return it
      String filteredGeoJsonString = geoJsonAdapter.toJson(filteredGeoJson);
      response.type("application/json");
      return filteredGeoJsonString;
    } catch (IOException e) {
      e.printStackTrace();
      response.status(500);
      return "Internal Server Error";
    }
  }
}
