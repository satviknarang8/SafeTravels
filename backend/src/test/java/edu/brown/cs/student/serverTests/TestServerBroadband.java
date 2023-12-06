package edu.brown.cs.student.serverTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spark.Spark.after;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Server.BroadbandRouteUtility.BroadbandHandler;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.serverTests.mocks.MockedACSAPIBroadbandSource;
import edu.brown.cs.student.serverTests.mocks.MockedDatasourceErrorACSAPIBroadbandSource;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
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

public class TestServerBroadband {

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

    data.put("result", "success");
    data.put(
        "data",
        "[[NAME, S2802_C03_022E, state, county], [Fairfax County, Virginia, 94.6, 51, 059]]");
    data.put("county", "fairfax");
    data.put("time", "2023/09/28 22:06:48");
    data.put("state", "virginia");

    Spark.get("broadband", (Route) new BroadbandHandler(new MockedACSAPIBroadbandSource(data)));
    Spark.get(
        "broadbandDatasourceError",
        (Route) new BroadbandHandler(new MockedDatasourceErrorACSAPIBroadbandSource()));
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + Spark.port());

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void teardownAfter() {
    Spark.unmap("/broadband");
    Spark.unmap("/broadbandDatasourceError");
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
    HttpURLConnection clientConnection = tryRequest("broadband?state=A&&county=B");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection.disconnect();
  }

  @Test
  public void testBroadbandUsage() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Virginia&&county=Fairfax");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("success", body.get("result"));
    assertEquals("virginia", body.get("state"));
    assertEquals("fairfax", body.get("county"));
    assertEquals(
        "[[\"NAME\", \"S2802_C03_022E\", \"state\", \"county\"], [\"Fairfax County, Virginia\", \"94.6\", \"51\", \"059\"]]",
        body.get("data").toString());
  }

  @Test
  public void testInvalidInput() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals(
        "Please provide both a state and county in your request.", body.get("error_message"));
  }

  @Test
  public void testDatasourceError() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("broadbandDatasourceError?state=Virginia&&county=Fairfax");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    System.out.println(body);
    assertEquals("error_datasource", body.get("result"));
    assertEquals("The Census API is unable to process your request.", body.get("error_message"));
  }
}
