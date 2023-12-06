package edu.brown.cs.student.serverTests;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class FuzzTest {
  static final int NUM_TRIALS = 100;
  static String previousSerializedData = null;

  @Test
  public void fuzzTestSerialize() {
    for (int counter = 0; counter < NUM_TRIALS; counter++) {
      try {
        Map<String, Object> testData = generateRandomData();

        String serializedData = serializeData(testData);

        if (previousSerializedData != null) {
          System.out.println("Previous: " + previousSerializedData);
          assertNotEquals(previousSerializedData, serializedData);
        }

        previousSerializedData = serializedData;
        System.out.println("Current: " + serializedData);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Map<String, Object> generateRandomData() {
    Map<String, Object> data = new HashMap<>();
    Random rand = new Random();
    data.put("randint", rand.nextInt());
    data.put("randfloat", rand.nextFloat());
    return data;
  }

  private String serializeData(Map<String, Object> data) {
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);
    return adapter.toJson(data);
  }
}
