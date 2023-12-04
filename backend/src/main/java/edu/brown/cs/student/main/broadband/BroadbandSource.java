package edu.brown.cs.student.main.broadband;

import static spark.Spark.connect;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.acsdatasource.OLD_ACSDatasource;
import edu.brown.cs.student.main.acsdatasource.OLD_CensusAPIClient;
import edu.brown.cs.student.main.exceptions.DatasourceException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import okio.Buffer;

/**
 * The BroadbandSource class does the majority of the querying from the actual dataset. It
 * implements the OLD_ACSDatasource interface and querys the ACS dataset to get the values we need
 */
public class BroadbandSource implements OLD_ACSDatasource {

  /** The year for which we want the dataset from */
  private static final String ACS_YEAR = "2021"; // Change this to the desired ACS year
  /** The specific dataset we awnt */
  private static final String ACS_DATASET = "acs1/subject";
  /** The specific variable we want to search for */
  private static final String ACS_VARIABLE = "S2802_C03_022E";
  /** The state param for the url */
  private static final String ACS_STATE_PARAM = "state";
  /** The county param for the url */
  private static final String ACS_COUNTY_PARAM = "county";

  /** An empty constructor for this class */
  public BroadbandSource() {}

  /**
   * A function that allows us to connect to the URl we want
   *
   * @param requestURL The URL that we are trying to connect to
   * @return The successfull connection to the URL
   * @throws DatasourceException Thrown if there is an error in getting the datasource
   * @throws IOException Thrown if there is an error in connecting to the URL
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
   * This is a function from the ACSDataSource interface. It gets the stateCodes and countyCodes and
   * querys the URL based on the recieved codes. It then returns the data recieved from the query
   *
   * @param state The state for which we want to query in
   * @param county The county for which we want to query in
   * @return A list of list of strings representing the queried data
   */
  @Override
  public List<List<String>> getBroadbandData(String state, String county)
      throws DatasourceException {
    try {
      OLD_CensusAPIClient censusApiClient = new OLD_CensusAPIClient();
      String stateCode = censusApiClient.getStateCode(state);
      String countyCode = "*";
      if (!county.equals("*")) {
        countyCode = censusApiClient.getCountyCode(stateCode, county);
      }
      String baseUrl = "https://api.census.gov/data/";
      String endpoint = ACS_YEAR + "/" + "acs/" + ACS_DATASET;
      String query =
          "/variables"
              + "?get=NAME,"
              + ACS_VARIABLE
              + "&for="
              + ACS_COUNTY_PARAM
              + ":"
              + countyCode
              + "&in="
              + ACS_STATE_PARAM
              + ":"
              + stateCode;
      URL apiUrl = new URL(baseUrl + endpoint + query);
      HttpURLConnection clientConnection = connect(apiUrl);
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<List<List<String>>> adapter =
          moshi.adapter(
              Types.newParameterizedType(
                  List.class, Types.newParameterizedType(List.class, String.class)));
      List<List<String>> broadbandData =
          adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();
      if (broadbandData == null) {
        throw new DatasourceException("Malformed response from ACS API");
      }
      return broadbandData;

    } catch (DatasourceException | IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }
}
