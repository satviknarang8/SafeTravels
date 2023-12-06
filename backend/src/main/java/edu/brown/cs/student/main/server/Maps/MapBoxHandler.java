package edu.brown.cs.student.main.Server.Maps;

import edu.brown.cs.student.main.Server.Server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class MapBoxHandler implements Route {
  /** Constructs a ViewCSVHandler instance. */
  public MapBoxHandler() {}

  /**
   * Handles an HTTP request to view loaded CSV data.
   *
   * @param request The HTTP request object.
   * @param response The HTTP response object.
   * @return A serialized response containing the loaded CSV data or an error message.
   */
  @Override
  public Object handle(Request request, Response response) {
    Set<String> params = request.queryParams();
    if (params.size() != 2) {
      response.status(400);
      return "Error: bad request";
    }
    String place = request.queryParams("place");
    String token = request.queryParams("accessToken");

    try {
      String url =
          "https://api.mapbox.com/geocoding/v5/mapbox.places/"
              + place
              + ".json?access_token="
              + token;
      Server.setHistory(place);
      System.err.println(Server.getHistory());

      URL apiUrl = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
      connection.setRequestMethod("GET");

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder res = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
          res.append(inputLine);
        }
        in.close();
        return res;
      } else {
        System.out.println("Error: " + responseCode);
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
