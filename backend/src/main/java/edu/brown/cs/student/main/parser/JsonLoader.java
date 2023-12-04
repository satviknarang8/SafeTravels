package edu.brown.cs.student.main.parser;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.FeatureCollection.GeoJSONFeatureCollection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JsonLoader {
  private final Moshi moshi;

  public JsonLoader() {
    this.moshi = new Moshi.Builder().build();
  }

  public GeoJSONFeatureCollection loadJsonDataAsFeatureCollection(String filePath)
      throws IOException {
    StringBuilder jsonBuilder = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        jsonBuilder.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    String json = jsonBuilder.toString();

    // Create a custom JSON adapter for GeoJSONFeatureCollection.
    JsonAdapter<GeoJSONFeatureCollection> jsonAdapter =
        moshi.adapter(GeoJSONFeatureCollection.class);
    // Parse the JSON and return it as a GeoJSONFeatureCollection.
    return jsonAdapter.fromJson(json);
  }
}

// import com.squareup.moshi.JsonAdapter;
// import com.squareup.moshi.Moshi;
// import com.squareup.moshi.Types;
// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.IOException;
// import java.util.Map;
//
/// **
// * The JsonLoader class is responsible for loading JSON data from a file and converting it into a
// * Map structure.
// * It uses the Moshi library for JSON parsing and deserialization.
// */
// public class JsonLoader {
//
//  private final Moshi moshi;
//
//  /**
//   * Constructs a new JsonLoader instance.
//   * It initializes the Moshi instance for JSON parsing.
//   */
//  public JsonLoader() {
//    this.moshi = new Moshi.Builder().build();
//  }
//
//  /**
//   * Load JSON data from a file and convert it into a Map of key-value pairs.
//   *
//   * @param filePath The path to the JSON file to be loaded.
//   * @return A Map representing the JSON data, where keys are strings and values can be of various
//   * types.
//   * @throws IOException If there is an issue reading the JSON file.
//   */
//  public Map<String, Object> loadJsonDataAsMap(String filePath) throws IOException {
//    StringBuilder jsonBuilder = new StringBuilder();
//
//    // Read the JSON data from the specified file.
//    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//      String line;
//      while ((line = reader.readLine()) != null) {
//        jsonBuilder.append(line);
//      }
//    }
//
//    // Convert the JSON data into a string.
//    String json = jsonBuilder.toString();
//
//    // Create a Moshi JSON adapter to parse the JSON string into a Map.
//    JsonAdapter<Map<String, Object>> jsonAdapter =
//        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
//
//    // Parse the JSON and return it as a Map.
//    return jsonAdapter.fromJson(json);
//  }
// }
