package edu.brown.cs.student.serverTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spark.Spark.after;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Server.CSVRouteUtility.LoadCSVHandler;
import edu.brown.cs.student.main.Server.CSVRouteUtility.SearchCSVHandler;
import edu.brown.cs.student.main.Server.CSVRouteUtility.ViewCSVHandler;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Server;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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

public class TestServerCSV {

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

    Spark.get("loadcsv", (Route) new LoadCSVHandler());
    Spark.get("viewcsv", (Route) new ViewCSVHandler());
    Spark.get("searchcsv", (Route) new SearchCSVHandler());

    Server.setFileLoaded(false);

    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + Spark.port());

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void teardownAfter() {
    Spark.unmap("/loadcsv");
    Spark.unmap("/viewcsv");
    Spark.unmap("/searchcsv");
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
  public void testEndpoints() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("viewcsv");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("searchcsv");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection.disconnect();
  }

  @Test
  public void testViewCSVNoLoad() throws IOException {
    HttpURLConnection clientConnection = tryRequest("viewcsv");

    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("You must load a file to view it.", body.get("error_message"));
    clientConnection.disconnect();
  }

  @Test
  public void testSearchCSVNoLoad() throws IOException {
    HttpURLConnection clientConnection = tryRequest("searchcsv?query=Newport&&columnToSearch=0");

    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("You must load a file to search it.", body.get("error_message"));
    clientConnection.disconnect();
  }

  @Test
  public void testLoadCSVValid() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filePath=income/ri_income.csv&&hasHeader=true");

    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("success", body.get("result"));
    assertEquals("income/ri_income.csv", body.get("filepath"));
    clientConnection.disconnect();
  }

  @Test
  public void testLoadCSVInvalid() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filePath=income/ri_income&&hasHeader=true");

    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals(
        "Please choose a valid CSV file. income/ri_income is not present in the \"data\" folder.",
        body.get("error_message"));
    clientConnection.disconnect();
  }

  @Test
  public void testLoadCSVEmpty() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filePath=income/empty_income.csv&&hasHeader=false");

    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("success", body.get("result"));
    assertEquals("income/empty_income.csv", body.get("filepath"));
    clientConnection.disconnect();
  }

  @Test
  public void testLoadAndViewCSV() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filePath=income/empty_income.csv&&hasHeader=false");

    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("viewcsv");

    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("success", body.get("result"));
    assertEquals(new ArrayList<>(), body.get("data"));
    clientConnection.disconnect();
  }

  @Test
  public void testLoadViewAndSearchCSV() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filePath=income/ri_income.csv&hasHeader=True");

    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("viewcsv");

    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("searchcsv?query=Providence");

    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    System.out.println(body);
    assertEquals("success", body.get("result"));
    assertEquals("Providence", body.get("query"));
    assertEquals(
        "[[Providence, \"55,787.00\", \"65,461.00\", \"31,757.00\"]]", body.get("data").toString());
    clientConnection.disconnect();
  }

  @Test
  public void testLoadViewAndSearchCSVNoValidQuery() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filePath=income/ri_income.csv&hasHeader=True");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("viewcsv");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("searchcsv?query=Prov");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("success", body.get("result"));
    assertEquals("Prov", body.get("query"));
    assertEquals("[]", body.get("data").toString());
    clientConnection.disconnect();
  }

  @Test
  public void testInvalidInputs() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("error_bad_request", body.get("result"));
    assertEquals(
        "Please provide a filePath and hasHeader arguments in your request.",
        body.get("error_message"));

    clientConnection = tryRequest("loadcsv?filePath=income/ri_income.csv&&hasHeader=true");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = tryRequest("searchcsv?columnToSearch=City/Town");
    assertEquals(200, clientConnection.getResponseCode());

    body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    System.out.println(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Please include a query.", body.get("error_message"));
  }
}
