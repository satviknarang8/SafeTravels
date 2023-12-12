package edu.brown.cs.student.main.Server.SafeTravels;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Private.APIkeys;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import okio.Buffer;
import java.net.URLEncoder;

public class GeocodingAPIClient {
  private final APIkeys apiKeys = new APIkeys();

  public GeocodingAPIClient() throws DatasourceException {
  }
  private static final String base_url = "https://maps.googleapis.com/maps/api/geocode/json?";

  /**
   * Method to connect to the URL which we want
   * @param requestURL the URL to connect to
   * @return The successful connection
   * @throws IOException Throws if there is an error in connecting to the URL
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    try {
      URLConnection urlConnection = requestURL.openConnection();
      if (!(urlConnection instanceof HttpURLConnection)) {
        throw new DatasourceException("unexpected: result of connection wasn't HTTP");
      }
      HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
      clientConnection.connect(); // GET
      if (clientConnection.getResponseCode() != 200 && clientConnection.getResponseCode() != 204) {
        throw new DatasourceException(
            "unexpected: API connection not success status "
                + clientConnection.getResponseMessage());
      }
      if (clientConnection.getResponseCode() == 204) {
        throw new DatasourceException("no content found");
      }
      return clientConnection;
    } catch (Exception e) {
      throw new DatasourceException(e.getMessage());
    }
  }
  public List<Double> getCoordinates(String address) throws DatasourceException {
    try {
      String encoded = URLEncoder.encode(address, "UTF-8");
      URL url = new URL(base_url + "address=" + encoded + "&key=" + apiKeys.geocodingKey);
      HttpURLConnection clientConnection = connect(url);
      Moshi moshi = new Moshi.Builder().build();

      // Define a JsonAdapter to parse the JSON into a Map
      JsonAdapter<Map<String, Object>> adapter =
          moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));

      // Parse the JSON response into a Map
      Map<String, Object> response =
          adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

      if (response == null || !response.containsKey("results")) {
        throw new DatasourceException("Invalid response from Geocoding API");
      }

      // Extract the location data
      List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
      if (results.isEmpty()) {
        throw new DatasourceException("No results found");
      }

      Map<String, Object> geometry = (Map<String, Object>) results.get(0).get("geometry");
      Map<String, Double> location = (Map<String, Double>) geometry.get("location");
      Double lat = location.get("lat");
      Double lng = location.get("lng");

      return List.of(lat, lng);

    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    } catch (Exception e) {
      throw new DatasourceException(e.getMessage());
    }
  }
  public Map<String, Object> getSafetyRatings(double north, double south, double east, double west, String accessToken) throws DatasourceException {
    try {
      // Construct the URL for the Amadeus API
      String urlStr = String.format(
              "https://test.api.amadeus.com/v1/safety/safety-rated-locations/by-square?north=%f&west=%f&south=%f&east=%f",
              north, west, south, east
      );
      String urler = "https://test.api.amadeus.com/v1/safety/safety-rated-locations/by-square?north="+north+"&west="+west+"&south="+south+"&east="+east;

      URL url = new URL(urler);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", "Bearer " + accessToken);

      // Check response code
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new DatasourceException("Failed to fetch safety ratings: " + connection.getResponseMessage());
      }

      // Parse the JSON response
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<Map<String, Object>> adapter =
              moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
      Map<String, Object> response = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

      connection.disconnect();

      if (response == null) {
        throw new DatasourceException("Invalid response from Amadeus API");
      }
      System.out.println(response);
      return response;

    } catch (IOException e) {
      throw new DatasourceException("IOException while fetching safety ratings: " + e.getMessage());
    }
  }
}
