package edu.brown.cs.student.main.acsdatasource;

import static spark.Spark.connect;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.exceptions.DatasourceException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okio.Buffer;

/** A class to get the StateCodes and County Codes from the census api */
public class OLD_CensusAPIClient {

  /** The base url for the census api */
  private static final String base_url = "https://api.census.gov/data/";

  /** The dataset for which we want the codes from */
  private static final String dataset = "2010/dec/sf1";

  /** The map containing all of the stateCodes */
  private Map<String, String> stateCodes = new HashMap<>();
  /** The map containing all of the countyCodes */
  private Map<String, String> countyCodes = new HashMap<>();

  /** An empty constructor for the OLD_CensusAPIClient */
  public OLD_CensusAPIClient() {}

  /**
   * Method to connect to the URL which we want
   *
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

  /**
   * A method to get the StateCodes from the API census (we automatically get it if it is already in
   * the map)
   *
   * @param stateName The state for which we want the state code
   * @return The state code of the state
   * @throws DatasourceException if there is an error in getting the state code
   */
  public String getStateCode(String stateName) throws DatasourceException {
    try {
      if (stateCodes.containsKey(stateName)) {
        return stateCodes.get(stateName);
      }
      try {
        String query = "?get=NAME&for=state:*";
        URL url = new URL(base_url + dataset + query);
        HttpURLConnection clientConnection = connect(url);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<List<List<String>>> adapter =
            moshi.adapter(
                Types.newParameterizedType(
                    List.class, Types.newParameterizedType(List.class, String.class)));
        List<List<String>> states =
            adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        if (states == null) {
          return "County was not found";
        }
        boolean isPresent = false;
        for (List<String> entry : states) {
          String name = entry.get(0);
          String code = entry.get(1);
          stateCodes.put(name, code);
          if (name.equals(stateName)) {
            isPresent = true;
          }
        }
        if (isPresent) {
          return stateCodes.get(stateName);
        } else {
          throw new DatasourceException("States were not found");
        }

      } catch (IOException e) {
        throw new DatasourceException(e.getMessage());
      }
    } catch (Exception e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  /**
   * A method to get the countyCodes from the API census (we automatically get it if it is already
   * in the map)
   *
   * @param stateCode The statecode in which we are searching the county in
   * @param countyName Countyname for which we want the countyCode
   * @return The countyCode of the county
   * @throws DatasourceException if there is an error in getting the county codes
   */
  public String getCountyCode(String stateCode, String countyName) throws DatasourceException {
    if (countyCodes.containsKey(countyName)) {
      return countyCodes.get(countyName);
    }
    try {
      String query = "?get=NAME&for=county:*&in=state:" + stateCode;
      URL apiUrl = new URL(base_url + dataset + query);
      HttpURLConnection clientConnection = connect(apiUrl);
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<List<List<String>>> adapter =
          moshi.adapter(
              Types.newParameterizedType(
                  List.class, Types.newParameterizedType(List.class, String.class)));
      List<List<String>> countyData =
          adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();
      if (countyData == null) {
        throw new DatasourceException("County not found");
      }
      boolean isPresent = false;
      for (List<String> entry : countyData) {
        String name = entry.get(0);
        String countyCode = entry.get(2);
        countyCodes.put(name, countyCode);
        if (name.equals(countyName)) {
          isPresent = true;
        }
      }
      if (isPresent) {
        return countyCodes.get(countyName);
      } else {
        throw new DatasourceException("County was not found");
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }
}
