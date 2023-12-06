package edu.brown.cs.student.jsonTests;

import static org.junit.jupiter.api.Assertions.*;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.json.JSONData;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JSONTest {
  private JSONData jsonData;

  @BeforeEach
  public void setUp() {
    jsonData = JSONData.getInstance();
  }

  @Test
  public void testLoadJsonFromFile() {
    String filePath = "data/json/valid_json_file.json";
    String invalidFilePath = "non_existent_file.json";

    assertDoesNotThrow(() -> jsonData.loadJsonFromFile(filePath, "validData"));
    assertThrows(
        IOException.class, () -> jsonData.loadJsonFromFile(invalidFilePath, "invalidData"));

    Object json = jsonData.getJsonData("validData");
    if (json != null) {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<Object> jsonAdapter = moshi.adapter(Object.class);

      try {
        String jsonString = jsonAdapter.toJson(json);
        System.out.println("JSON String: " + jsonString);
      } catch (JsonDataException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void testLoadJsonFromString() {
    String jsonString = "{\"key\": \"value\"}";
    assertDoesNotThrow(() -> jsonData.loadJsonFromString(jsonString, "stringData"));
    assertThrows(
        IOException.class, () -> jsonData.loadJsonFromString("invalidJson", "invalidStringData"));
  }

  @Test
  public void testGetJsonData() {
    String filePath = "data/json/valid_json_file.json";
    String jsonString = "{\"key\": \"value\"}";
    assertDoesNotThrow(() -> jsonData.loadJsonFromFile(filePath, "validData"));
    assertDoesNotThrow(() -> jsonData.loadJsonFromString(jsonString, "stringData"));
    assertNotNull(jsonData.getJsonData("validData"));
    assertNotNull(jsonData.getJsonData("stringData"));
    assertNull(jsonData.getJsonData("nonExistentData"));
  }
}
