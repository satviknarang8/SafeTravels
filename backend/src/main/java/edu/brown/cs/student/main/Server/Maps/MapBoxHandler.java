// package edu.brown.cs.student.main.Server.Maps;

// import edu.brown.cs.student.main.Server.Server;
// import java.io.BufferedReader;
// import java.io.InputStreamReader;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import java.util.Set;
// import spark.Request;
// import spark.Response;
// import spark.Route;

// public class MapBoxHandler implements Route {
//   /** Constructs a ViewCSVHandler instance. */
//   public MapBoxHandler() {}

//   /**
//    * Handles an HTTP request to view loaded CSV data.
//    *
//    * @param request The HTTP request object.
//    * @param response The HTTP response object.
//    * @return A serialized response containing the loaded CSV data or an error message.
//    */
//   @Override
//   public Object handle(Request request, Response response) {
//     Set<String> params = request.queryParams();
//     if (params.size() != 2) {
//       response.status(400);
//       return "Error: bad request";
//     }
//     String place = request.queryParams("place");
//     String token = request.queryParams("accessToken");

//     try {
//       String url =
//           "https://api.mapbox.com/geocoding/v5/mapbox.places/"
//               + place
//               + ".json?access_token="
//               + token;
//       Server.setHistory(place);
//       System.err.println(Server.getHistory());

//       URL apiUrl = new URL(url);
//       HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
//       connection.setRequestMethod("GET");

//       int responseCode = connection.getResponseCode();
//       if (responseCode == HttpURLConnection.HTTP_OK) {
//         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//         String inputLine;
//         StringBuilder res = new StringBuilder();

//         while ((inputLine = in.readLine()) != null) {
//           res.append(inputLine);
//         }
//         in.close();
//         return res;
//       } else {
//         System.out.println("Error: " + responseCode);
//         return null;
//       }
//     } catch (Exception e) {
//       e.printStackTrace();
//       return null;
//     }
//   }
// }

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
  public MapBoxHandler() {}

  @Override
  public Object handle(Request request, Response response) {
    Set<String> params = request.queryParams();
    if (params.size() != 4) {  // Expecting 4 parameters: startPlace, startToken, endPlace, endToken
      response.status(400);
      return "Error: bad request";
    }

    String startPlace = request.queryParams("startPlace");
    String startToken = request.queryParams("startAccessToken");
    String endPlace = request.queryParams("endPlace");
    String endToken = request.queryParams("endAccessToken");

    try {
      String startUrl = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
              + startPlace
              + ".json?access_token="
              + startToken;

      String endUrl = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
              + endPlace
              + ".json?access_token="
              + endToken;

      // Fetching start location data
      String startResult = fetchDataFromMapbox(startUrl);
      
      // Fetching end location data
      String endResult = fetchDataFromMapbox(endUrl);

      // Combine and return results
      return startResult + "\n" + endResult;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private String fetchDataFromMapbox(String url) throws Exception {
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
      return res.toString();
    } else {
      System.out.println("Error: " + responseCode);
      return null;
    }
  }

//   @Override
//   public Object handle(Request request, Response response) {
//     Double startLat = Double.parseDouble(request.queryParams("startLat"));
//     Double startLng = Double.parseDouble(request.queryParams("startLng"));
//     String startToken = request.queryParams("startAccessToken");
    
//     Double endLat = Double.parseDouble(request.queryParams("endLat"));
//     Double endLng = Double.parseDouble(request.queryParams("endLng"));
//     String endToken = request.queryParams("endAccessToken");

//     try {
//       // Fetching start location data
//       String startResult = fetchDataFromMapbox(startLat, startLng, startToken);
      
//       // Fetching end location data
//       String endResult = fetchDataFromMapbox(endLat, endLng, endToken);

//       // Combine and return results
//       return startResult + "\n" + endResult;

//     } catch (Exception e) {
//       e.printStackTrace();
//       return null;
//     }
//   }

//   private String fetchDataFromMapbox(Double lat, Double lng, String token) throws Exception {
//     String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
//               + lng + ","
//               + lat
//               + ".json?access_token="
//               + token;
// }

