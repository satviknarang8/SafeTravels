package edu.brown.cs.student.main.json;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okio.BufferedSource;
import okio.Okio;

public class JSONData {
  private static JSONData instance;
  private Map<String, Object> jsonMap;
  private Moshi moshi;

  private JSONData() {
    jsonMap = new HashMap<>();
    moshi = new Moshi.Builder().build();
  }

  public static JSONData getInstance() {
    if (instance == null) {
      instance = new JSONData();
    }
    return instance;
  }

  public void loadJsonFromFile(String jsonFilePath, String key) throws IOException {
    File jsonFile = new File(jsonFilePath);
    if (jsonFile.exists() && jsonFile.isFile() && jsonFile.canRead()) {
      try {
        JsonAdapter<Object> jsonAdapter = moshi.adapter(Object.class);
        BufferedSource source = Okio.buffer(Okio.source(jsonFile));
        Object jsonData = jsonAdapter.fromJson(source);
        jsonMap.put(key, jsonData);
        source.close();
      } catch (IOException e) {
        throw new IOException("Failed to read JSON file: " + jsonFilePath, e);
      } catch (JsonDataException e) {
        throw new IOException("Invalid JSON format in file: " + jsonFilePath, e);
      }
    } else {
      throw new IOException("Invalid or unreadable JSON file: " + jsonFilePath);
    }
  }

  public void loadJsonFromString(String jsonString, String key) throws IOException {
    try {
      JsonAdapter<Object> jsonAdapter = moshi.adapter(Object.class);
      Object jsonData = jsonAdapter.fromJson(jsonString);
      jsonMap.put(key, jsonData);
    } catch (IOException e) {
      throw new IOException("Failed to parse JSON string.", e);
    } catch (JsonDataException e) {
      throw new IOException("Invalid JSON format in the provided string.", e);
    }
  }

  public Object getJsonData(String key) {
    return jsonMap.get(key);
  }
}
