package edu.brown.cs.student.main.Server.BroadbandRouteUtility;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okio.Buffer;

/**
 * The ACSAPIBroadbandSource class implements the ACSAPISource interface for fetching broadband
 * usage data from the Census API.
 */
public class ACSAPIBroadbandSource implements ACSAPISource {

  // A map to store state names and their corresponding IDs fetched from the Census API
  private final Map<String, String> stateIDMap;

  /**
   * Constructs an instance of ACSAPIBroadbandSource and fetches the state ID map from the Census
   * API.
   *
   * @throws DatasourceException If there is an issue with the data source.
   */
  public ACSAPIBroadbandSource() throws DatasourceException {
    this.stateIDMap = fetchStateIDMap();
  }

  /**
   * Fetches the state ID map from the Census API and stores it locally.
   *
   * @return A Map containing state names as keys and state IDs as values.
   * @throws DatasourceException If there is an issue with the data source.
   */
  public Map<String, String> fetchStateIDMap() throws DatasourceException {
    Map<String, String> map = new HashMap<>();

    try {
      List<List<String>> stateDataList =
          deserializeCensusData("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");

      if (stateDataList == null) {
        throw new DatasourceException(
            "Unable to retrieve states and corresponding IDs from Census API");
      } else {
        for (List<String> stateData : stateDataList) {
          if (stateData.size() >= 2) {
            String stateName = stateData.get(0).toLowerCase();
            String stateID = stateData.get(1);
            map.put(stateName, stateID);
          }
        }
        map.remove("name");
      }

    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
    return map;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getBroadbandUsage(String state, String county)
      throws DatasourceException, InvalidArgsException {
    String stateID = stateIDMap.getOrDefault(state, "invalid");
    if (stateID.equals("invalid")) {
      throw new InvalidArgsException(state + " is not a valid state");
    }

    try {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
      // Check if county is * meaning we return all counties
      if (county.equals("*")) {
        return Map.of(
            "data", broadbandStatistics(stateID, "*"),
            "time", dtf.format(LocalDateTime.now()));
      }

      List<List<String>> countyDataList =
          deserializeCensusData(
              "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateID);

      if (countyDataList == null) {
        throw new DatasourceException(
            "Unable to retrieve states and corresponding IDs from Census API");
      }

      String countyID = "invalid";
      for (List<String> list : countyDataList) {
        int indexToSubstr = list.get(0).indexOf("County") - 1;
        if (indexToSubstr > 0 && list.get(0).substring(0, indexToSubstr).equalsIgnoreCase(county)) {
          countyID = list.get(2);
        }
      }

      if (!countyID.equals("invalid")) {
        return Map.of(
            "data", broadbandStatistics(stateID, countyID),
            "time", dtf.format(LocalDateTime.now()));
      } else {
        throw new InvalidArgsException(county + " county does not exist in the state of " + state);
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  /**
   * Fetches broadband statistics data for a specified state and county from the Census API.
   *
   * @param stateID The ID of the state.
   * @param countyID The ID of the county.
   * @return A string representing the broadband statistics data.
   * @throws DatasourceException If there is an issue with the data source.
   */
  public String broadbandStatistics(String stateID, String countyID) throws DatasourceException {
    try {
      List<List<String>> broadbandDataList =
          deserializeCensusData(
              "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                  + countyID
                  + "&in=state:"
                  + stateID);

      // Check if the response is valid and contains data
      if (broadbandDataList == null) {
        throw new DatasourceException(
            "No broadband statistics data available for the specified county and state");
      } else {
        return broadbandDataList.toString();
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  /**
   * Private helper method for establishing an HTTP connection to the given URL.
   *
   * @param requestURL The URL to connect to.
   * @return An HTTP connection object.
   * @throws DatasourceException If there is an issue with the data source.
   * @throws IOException If there is an issue with the network connection.
   */
  public static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection)) {
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    }
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect();
    if (clientConnection.getResponseCode() != 200) {
      throw new DatasourceException(
          "unexpected: API connection not successful, status "
              + clientConnection.getResponseMessage());
    }
    return clientConnection;
  }

  /**
   * Deserializes Census data from the specified URL.
   *
   * @param url The URL containing the Census data.
   * @return A list of lists representing the deserialized data.
   * @throws IOException If there is an issue with the network connection.
   * @throws DatasourceException If there is an issue with the data source.
   */
  public static List<List<String>> deserializeCensusData(String url)
      throws IOException, DatasourceException {

    // Create a URL object
    URL requestURL = new URL(url);
    HttpURLConnection connection = connect(requestURL);

    // Create a Moshi object
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<List<List<String>>> adapter =
        moshi.adapter(Types.newParameterizedType(List.class, List.class, String.class));
    List<List<String>> responseObj =
        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    connection.disconnect();
    return responseObj;
  }
}
