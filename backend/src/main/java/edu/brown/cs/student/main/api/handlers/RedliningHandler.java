package edu.brown.cs.student.main.api.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.FeatureCollection.GeoJSONFeatureCollection;
import edu.brown.cs.student.main.FeatureCollection.GeoJSONFeatureCollection.Feature;
import edu.brown.cs.student.main.FeatureCollection.GeoJSONFeatureCollection.Geometry;
import edu.brown.cs.student.main.parser.JsonLoader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class RedliningHandler implements Route {

  private final String filepath = "data/geodata/fullDownload.json";
  private JsonLoader loader;
  private GeoJSONFeatureCollection featureCollection;

  public RedliningHandler() {
    this.loader = new JsonLoader();
  }

  /**
   * Gets the names of the state and county for which broadband data is requested from the query
   * parameters. Then it creates a BroadbandSource, which implements the OLD_ACSDatasource interface and
   * is responsible for using the ACS API to return results. In its getBroadbandData() method, the
   * BroadbandSource creates an instance of the OLD_CensusAPIClient class, which is responsible for
   * using the names of the state and county to retrieve the state code and county code from the ACS
   * API. Also, serializes the response and returns a Json object.
   *
   * @return
   */
  private void loadFeatureCollection() {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<GeoJSONFeatureCollection> jsonAdapter =
        moshi.adapter(GeoJSONFeatureCollection.class);
    try {
      this.featureCollection = this.loader.loadJsonDataAsFeatureCollection(filepath);
    } catch (IOException e) {
      e.printStackTrace();
      // Handle the exception or log the error as needed
    }
  }

  private void loadFeatures() {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<GeoJSONFeatureCollection> jsonAdapter =
        moshi.adapter(GeoJSONFeatureCollection.class);
    try {
      this.featureCollection = this.loader.loadJsonDataAsFeatureCollection(filepath);
    } catch (IOException e) {
      e.printStackTrace();
      // Handle the exception or log the error as needed
    }
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      Moshi moshi = new Moshi.Builder().build();
      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
      Map<String, Object> responseMap = new HashMap<>();
      Moshi moshi2 = new Moshi.Builder().build();
      JsonAdapter<GeoJSONFeatureCollection> jsonAdapter =
          moshi2.adapter(GeoJSONFeatureCollection.class);
      try {
        this.featureCollection = this.loader.loadJsonDataAsFeatureCollection(filepath);
      } catch (IOException e) {
        responseMap.put("ERROR", "FILE NOT FOUND");
        return adapter.toJson(responseMap);
      }
      String minLat = request.queryParams("minlat");
      String maxLat = request.queryParams("maxlat");
      String minLong = request.queryParams("minlong");
      String maxLong = request.queryParams("maxlong");

      if (minLat == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "bad_request");
        responseMap.put("details", "Min. Lat. was not given in query");
        responseMap.put("result", "fail");
        responseMap.put("date_time", LocalDateTime.now().toString());
        return adapter.toJson(responseMap);
      } else if (minLat.equals("**")) {
        return jsonAdapter.toJson(this.featureCollection);
      } else if (maxLat == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "bad_request");
        responseMap.put("details", "Max Lat. was not given in query");
        responseMap.put("result", "fail");
        responseMap.put("date_time", LocalDateTime.now().toString());
        return adapter.toJson(responseMap);
      } else if (minLong == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "bad_request");
        responseMap.put("details", "Min. Long. was not given in query");
        responseMap.put("result", "fail");
        responseMap.put("date_time", LocalDateTime.now().toString());
        return adapter.toJson(responseMap);
      } else if (maxLong == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "bad_request");
        responseMap.put("details", "Max Long. was not given in query");
        responseMap.put("result", "fail");
        responseMap.put("date_time", LocalDateTime.now().toString());
        return adapter.toJson(responseMap);
      }

      double minimumLat = Double.parseDouble(minLat);
      double minimumLon = Double.parseDouble(minLong);
      double maximumLat = Double.parseDouble(maxLat);
      double maximumLon = Double.parseDouble(maxLong);
      this.loader.loadJsonDataAsFeatureCollection(this.filepath);
      List<Feature> filteredFeatures =
          this.getFilteredData(minimumLat, minimumLon, maximumLat, maximumLon);

      if (filteredFeatures.isEmpty()) {
        responseMap.put("result", "error_result");
        responseMap.put(
            "details", "No data found for inputted parameters. Please enter valid ones.");
      } else {
        responseMap.put("result", "success");
        responseMap.put("data", filteredFeatures);
      }

      return adapter.toJson(responseMap);

    } catch (Exception e) {
      response.status(400);
      return e.getMessage();
    }
  }

  public List<Feature> getFilteredData(double minLat, double minLon, double maxLat, double maxLon) {
    this.loadFeatureCollection();
    List<Feature> filteredFeatures = new ArrayList<>();
    System.out.println(this.featureCollection.features().size());
    for (Feature feature : this.featureCollection.features()) {
      if (featureIntersectsBoundingBox(feature, minLat, minLon, maxLat, maxLon)) {
        filteredFeatures.add(feature);
      }
    }
    return filteredFeatures;
  }

  private boolean featureIntersectsBoundingBox(
      Feature feature, double minLat, double minLon, double maxLat, double maxLon) {
    Geometry geometry = feature.geometry();
    if (geometry == null) {
      return false;
    }
    for (List<List<List<Double>>> polygon : geometry.coordinates()) {
      for (List<List<Double>> ring : polygon) {
        for (List<Double> point : ring) {
          double lon = point.get(0);
          double lat = point.get(1);
          if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public List<GeoJSONFeatureCollection.Feature> getKeywordData(String keyword) {
    this.loadFeatureCollection();
    List<GeoJSONFeatureCollection.Feature> keywordFeatures = new ArrayList<>();
    for (GeoJSONFeatureCollection.Feature feature : this.featureCollection.features()) {
      if (keywordExistsInProperty(feature, keyword)) {
        keywordFeatures.add(feature);
      }
    }

    return keywordFeatures;
  }

  private boolean keywordExistsInProperty(
      GeoJSONFeatureCollection.Feature feature, String keyword) {
    Map<String, Object> objectMap = feature.properties();
    if (objectMap == null) {
      return false;
    }
    for (Object value : objectMap.values()) {
      if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
        return true;
      }
    }
    return false;
  }
}
