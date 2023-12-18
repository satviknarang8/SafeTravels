package edu.brown.cs.student.main.Server.SafeTravels;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Private.APIkeys;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
  public Map<String, Object> getSafetyRatings(double lat, double lon, int radius) throws DatasourceException {
    try {
      // Step 1: Get a new access token
      String tokenUrl = "https://test.api.amadeus.com/v1/security/oauth2/token";
      URL tokenEndpoint = new URL(tokenUrl);
      HttpURLConnection tokenConnection = (HttpURLConnection) tokenEndpoint.openConnection();
      tokenConnection.setRequestMethod("POST");
      tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      tokenConnection.setDoOutput(true);

      // Client credentials
      APIkeys apIkeys= new APIkeys();
      String clientId = apIkeys.safePlaceKey;
      String clientSecret = apiKeys.safePlaceSecret;
      String urlParameters = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;

      // Send the request
      try (DataOutputStream wr = new DataOutputStream(tokenConnection.getOutputStream())) {
        wr.writeBytes(urlParameters);
        wr.flush();
      }

      // Check response and retrieve token
      if (tokenConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new DatasourceException("Failed to obtain token: " + tokenConnection.getResponseMessage());
      }

      // Parse the token response
      BufferedReader in = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
      String inputLine;
      StringBuilder content = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();
      tokenConnection.disconnect();

      // Extract token from JSON response
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<Map<String, String>> tokenAdapter =
              moshi.adapter(Types.newParameterizedType(Map.class, String.class, String.class));
      Map<String, String> tokenResponse = tokenAdapter.fromJson(content.toString());
      String accessToken = tokenResponse.get("access_token");
      String baseUrl = "https://test.api.amadeus.com/v1/safety/safety-rated-locations?";

      // Step 2: Use the new token to get safety ratings
      String latitude = Double.toString(lat);
      String longitude = Double.toString(lon);
      String rad = Integer.toString(radius);

// Use the string variables in the URL

      String alt = "https://test.api.amadeus.com/v1/safety/safety-rated-locations?latitude=41.397158&longitude=2.160873&page%5Boffset%5D=0&radius=2";
      System.out.println(alt.equals(baseUrl+"latitude="+latitude+"&longitude="+longitude+"&page%5Boffset%5D=0&radius="+rad));
      System.out.println(baseUrl+"latitude="+latitude+"&longitude="+longitude+"&page%5Boffset%5D=0&radius="+rad);

      URL url = new URL(baseUrl+"latitude="+latitude+"&longitude="+longitude+"&page%5Boffset%5D=0&radius="+rad);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", "Bearer " + accessToken);

      // Check response code
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new DatasourceException("Failed to fetch safety ratings: " + connection.getResponseMessage());
      }

      // Parse the JSON response for safety ratings
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
      throw new DatasourceException("IOException while fetching safety ratings or obtaining token: " + e.getMessage());
    }
  }

}
