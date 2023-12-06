package edu.brown.cs.student.serverTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Spark.after;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Maps.RedliningHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Route;
import spark.Spark;

public class TestRedlining {

  private int NUM_TRIALS = 100;

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  Map<String, String> data = new HashMap<>();

  @BeforeAll
  public static void beforeAll() {
    Spark.port(32);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  @BeforeEach
  public void setupBefore() throws DatasourceException {
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Spark.get("redlining", (Route) new RedliningHandler());
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + Spark.port());

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void teardownAfter() {
    Spark.unmap("/redlining");
    Spark.awaitStop();
  }

  @AfterAll
  public static void afterAll() throws InterruptedException {
    Spark.stop();
    Thread.sleep(3000);
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testEndpoint() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("redlining?minLat=-90&maxLat=90&minLon=-180&maxLon=180");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection.disconnect();
  }

  @Test
  public void testRedliningFiltering() throws IOException {
    getRedlineData(-90, 90, -180, 180);

    for (int i = 0; i < NUM_TRIALS; i++) {
      double minLat = -90 + Math.random() * (90 - (-90));
      double maxLat = -90 + Math.random() * (90 - (-90));
      double minLon = -180 + Math.random() * (180 - (-180));
      double maxLon = -180 + Math.random() * (180 - (-180));

      getRedlineData(minLat, maxLat, minLon, maxLon);
    }
  }

  private void getRedlineData(double minLat, double maxLat, double minLon, double maxLon)
      throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "redlining?minLat="
                + minLat
                + "&maxLat="
                + maxLat
                + "&minLon="
                + minLon
                + "&maxLon="
                + maxLon);
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertTrue(body.containsKey("features"));
    assertTrue(body.get("features") instanceof List);

    List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");
    System.err.println("Features: " + features.size());
    for (Map<String, Object> feature : features) {
      assertTrue(feature.containsKey("geometry"));
      Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
      assertTrue(geometry.containsKey("coordinates"));

      List<List<List<List<Double>>>> coordinates =
          (List<List<List<List<Double>>>>) geometry.get("coordinates");

      validateCoordinatesInRange(coordinates, minLat, maxLat, minLon, maxLon);
    }
  }

  private void validateCoordinatesInRange(
      List<List<List<List<Double>>>> coordinates,
      double minLat,
      double maxLat,
      double minLon,
      double maxLon) {
    for (List<List<List<Double>>> polygon : coordinates) {
      for (List<List<Double>> points : polygon) {
        for (List<Double> point : points) {
          double lon = point.get(0);
          double lat = point.get(1);

          assertTrue(lat >= minLat && lat <= maxLat);
          assertTrue(lon >= minLon && lon <= maxLon);
        }
      }
    }
  }
}
