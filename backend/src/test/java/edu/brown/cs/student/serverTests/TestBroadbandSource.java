package edu.brown.cs.student.serverTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spark.Spark.after;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Server.BroadbandRouteUtility.ACSAPIBroadbandSource;
import edu.brown.cs.student.main.Server.BroadbandRouteUtility.BroadbandHandler;
import edu.brown.cs.student.main.Server.BroadbandRouteUtility.CachedACSAPIBroadbandSource;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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

public class TestBroadbandSource {

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

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

    Spark.get(
        "broadband",
        (Route)
            new BroadbandHandler(
                new CachedACSAPIBroadbandSource(new ACSAPIBroadbandSource(), 10, 5)));
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + Spark.port());

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void teardownAfter() {
    Spark.unmap("/broadband");
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
    HttpURLConnection clientConnection = tryRequest("broadband?state=Florida&&county=Miami-Dade");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("success", body.get("result"));
    assertEquals("florida", body.get("state"));
    assertEquals(
        "[[\"NAME\", \"S2802_C03_022E\", \"state\", \"county\"], [\"Miami-Dade County, Florida\", \"85.0\", \"12\", \"086\"]]",
        body.get("data").toString());
    clientConnection.disconnect();
  }

  @Test
  public void testCachedBroadbandUsage() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Florida&&county=Miami-Dade");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    HttpURLConnection newClientConnection =
        tryRequest("broadband?state=Florida&&county=Miami-Dade");
    assertEquals(200, newClientConnection.getResponseCode());

    Map<String, Object> newBody =
        adapter.fromJson(new Buffer().readFrom(newClientConnection.getInputStream()));

    assertEquals(body.get("time"), newBody.get("time"));

    clientConnection.disconnect();
    newClientConnection.disconnect();
  }

  @Test
  public void testInvalidState() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Flo&&county=Miami-Dade");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("flo is not a valid state", body.get("error_message"));
    clientConnection.disconnect();
  }

  @Test
  public void testInvalidCounty() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Florida&&county=X");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("x county does not exist in the state of florida", body.get("error_message"));
    clientConnection.disconnect();
  }

  @Test
  public void testInvalidInputs() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Florida");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals(
        "Please provide both a state and county in your request.", body.get("error_message"));
  }
}
