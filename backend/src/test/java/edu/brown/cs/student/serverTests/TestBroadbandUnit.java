package edu.brown.cs.student.serverTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.Server.BroadbandRouteUtility.ACSAPIBroadbandSource;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestBroadbandUnit {
  ACSAPIBroadbandSource broadbandSource;

  @BeforeEach
  public void setupBefore() throws DatasourceException {
    broadbandSource = new ACSAPIBroadbandSource();
  }

  @Test
  public void fetchStateIDMap() {
    try {
      assertEquals(
          "{florida=12, arkansas=05, nebraska=31, ohio=39, texas=48, missouri=29, georgia=13, alaska=02, delaware=10, massachusetts=25, california=06, oklahoma=40, new hampshire=33, north carolina=37, alabama=01, louisiana=22, kansas=20, puerto rico=72, pennsylvania=42, south carolina=45, utah=49, minnesota=27, oregon=41, virginia=51, washington=53, district of columbia=11, iowa=19, arizona=04, maryland=24, illinois=17, rhode island=44, tennessee=47, new jersey=34, west virginia=54, montana=30, idaho=16, kentucky=21, wisconsin=55, maine=23, nevada=32, hawaii=15, michigan=26, connecticut=09, colorado=08, new york=36, north dakota=38, wyoming=56, south dakota=46, vermont=50, mississippi=28, new mexico=35, indiana=18}",
          broadbandSource.fetchStateIDMap().toString());
    } catch (DatasourceException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void getBroadbandUsage() throws InvalidArgsException {
    try {
      String body = broadbandSource.getBroadbandUsage("florida", "Miami-Dade").toString();
      if (body.contains("[Miami-Dade County, Florida, 85.0, 12, 086]")) {
        assertEquals(1, 1);
      } else {
        assertEquals(1, 0);
      }
    } catch (DatasourceException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void getBroadbandStatistics() throws InvalidArgsException {
    try {
      assertEquals(
          "[[NAME, S2802_C03_022E, state, county], [Brevard County, Florida, 89.3, 12, 009]]",
          broadbandSource.broadbandStatistics("12", "009"));
    } catch (DatasourceException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void connect() throws DatasourceException, IOException {
    try {
      HttpURLConnection clientConnection =
          broadbandSource.connect(
              new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*"));
      assertEquals(200, clientConnection.getResponseCode());
    } catch (DatasourceException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void deserializeCensusData() throws IOException, DatasourceException {
    assertEquals(
        "[[NAME, state], [Alabama, 01], [Alaska, 02], [Arizona, 04], [Arkansas, 05], [California, 06], [Louisiana, 22], [Kentucky, 21], [Colorado, 08], [Connecticut, 09], [Delaware, 10], [District of Columbia, 11], [Florida, 12], [Georgia, 13], [Hawaii, 15], [Idaho, 16], [Illinois, 17], [Indiana, 18], [Iowa, 19], [Kansas, 20], [Maine, 23], [Maryland, 24], [Massachusetts, 25], [Michigan, 26], [Minnesota, 27], [Mississippi, 28], [Missouri, 29], [Montana, 30], [Nebraska, 31], [Nevada, 32], [New Hampshire, 33], [New Jersey, 34], [New Mexico, 35], [New York, 36], [North Carolina, 37], [North Dakota, 38], [Ohio, 39], [Oklahoma, 40], [Oregon, 41], [Pennsylvania, 42], [Rhode Island, 44], [South Carolina, 45], [South Dakota, 46], [Tennessee, 47], [Texas, 48], [Utah, 49], [Vermont, 50], [Virginia, 51], [Washington, 53], [West Virginia, 54], [Wisconsin, 55], [Wyoming, 56], [Puerto Rico, 72]]",
        ACSAPIBroadbandSource.deserializeCensusData(
                "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*")
            .toString());
  }
}
