package edu.brown.cs.student.main.Server;

import static spark.Spark.after;

import edu.brown.cs.student.main.Server.BroadbandRouteUtility.ACSAPIBroadbandSource;
import edu.brown.cs.student.main.Server.BroadbandRouteUtility.BroadbandHandler;
import edu.brown.cs.student.main.Server.BroadbandRouteUtility.CachedACSAPIBroadbandSource;
import edu.brown.cs.student.main.Server.CSVRouteUtility.LoadCSVHandler;
import edu.brown.cs.student.main.Server.CSVRouteUtility.SearchCSVHandler;
import edu.brown.cs.student.main.Server.CSVRouteUtility.ViewCSVHandler;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.LoginHandler.LoginManager;
import edu.brown.cs.student.main.Server.Maps.MapBoxHandler;
import edu.brown.cs.student.main.Server.Maps.RedliningHandler;
import edu.brown.cs.student.main.Server.SafeTravels.SafetyHandler;
import edu.brown.cs.student.main.csv.Parser.csvParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import spark.Route;
import spark.Spark;

/** The Server class represents a server application for handling CSV data. */
public class Server {

  // CSV parser used to parse the CSV data
  private static csvParser<List<String>> parser = null;

  // List to store CSV data rows
  private static List<List<String>> rows = new ArrayList<>();

  // Defensive copy of CSV data rows to ensure immutability
  private static List<List<String>> defensiveRows = null;

  // Flag to indicate if the CSV file has been loaded
  private static Boolean fileLoaded = Boolean.FALSE;

  // Persistent search history
  private static ArrayList<String> searchHistory = new ArrayList<String>();

  // Set search history
  public static void setHistory(String h) {
    searchHistory.add(h);
  }

  // Get search history
  public static ArrayList<String> getHistory() {
    return searchHistory;
  }

  /**
   * Get the defensive copy of CSV data rows.
   *
   * @return An unmodifiable list containing CSV data rows.
   */
  public static List<List<String>> getDefensiveRows() {
    if (defensiveRows == null) {
      defensiveRows = Collections.unmodifiableList(rows);
    }
    return defensiveRows;
  }

  /**
   * Set the CSV parser used to parse the CSV data.
   *
   * @param p The CSV parser to be set.
   */
  public static void setParser(csvParser<List<String>> p) {
    parser = p;
  }

  /**
   * Get the CSV parser used to parse the CSV data.
   *
   * @return The CSV parser.
   */
  public static csvParser<List<String>> getParser() {
    return parser;
  }

  /**
   * Set the flag indicating whether the CSV file has been loaded.
   *
   * @param b The value to set for the fileLoaded flag.
   */
  public static void setFileLoaded(Boolean b) {
    fileLoaded = b;
  }

  /**
   * Get the flag indicating whether the CSV file has been loaded.
   *
   * @return true if the CSV file is loaded, false otherwise.
   */
  public static Boolean getFileLoaded() {
    return fileLoaded;
  }

  /**
   * Set the CSV data rows with updated rows.
   *
   * @param updatedRows The updated CSV data rows to set.
   */
  public static void setRows(List<List<String>> updatedRows) {
    rows.clear();
    rows.addAll(updatedRows);
  }

  /**
   * The main method to start the server application.
   *
   * @param args Command-line arguments (not used).
   * @throws DatasourceException If there is an issue with the data source.
   */
  public static void main(String[] args) throws DatasourceException {
    int port = 3232;

    Spark.staticFiles.location("/public");

    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Spark.get("loadcsv", new LoadCSVHandler());
    Spark.get("viewcsv", new ViewCSVHandler());
    Spark.get("searchcsv", new SearchCSVHandler());
//    Spark.get(
//        "broadband",
//        (Route)
//            new BroadbandHandler(
//                new CachedACSAPIBroadbandSource(new ACSAPIBroadbandSource(), 10, 5)));
    Spark.get("redlining", new RedliningHandler());
    Spark.get("mapbox", new MapBoxHandler());
    Spark.get("login", new LoginManager());
    Spark.get("register", new LoginManager());
    Spark.get("safestroute", new SafetyHandler());


    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }
}
