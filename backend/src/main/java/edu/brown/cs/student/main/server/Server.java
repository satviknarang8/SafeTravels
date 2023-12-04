package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import edu.brown.cs.student.main.acsdatasource.OLD_ACSDatasource;
import edu.brown.cs.student.main.api.handlers.BroadbandHandler;
import edu.brown.cs.student.main.api.handlers.RedliningHandler;
import edu.brown.cs.student.main.api.handlers.SafePlaceHandler;
import edu.brown.cs.student.main.broadband.BroadbandSource;
import edu.brown.cs.student.main.searcher.JsonSearcher;
import java.util.HashMap;
import java.util.Map;
import spark.Spark;

/** Our program's main top-level class. */
public class Server {

  /**
   * Creates the shared state, starts Spark, and runs the 4 handlers LoadCSVHandler, ViewCSVHandler,
   * SearchCSVHandler, and BroadbandHandler.
   */
  public static void main(String[] args) {
    int port = 3233;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Map<String, Object> sharedState = new HashMap<>();
    OLD_ACSDatasource broadbandSource = new BroadbandSource();

//    Spark.get("/broadband", new BroadbandHandler(broadbandSource));
//    Spark.get("/redlining", new RedliningHandler());
//    Spark.get("/search", new JsonSearcher());
    Spark.get("/safetymap", new SafePlaceHandler());

    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }
}
