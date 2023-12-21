package edu.brown.cs.student.geocodetests;
import edu.brown.cs.student.main.Server.SafeTravels.APIClient;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;


public class APIClientTest {
  // Test method to verify fetching coordinates
  @Test
  public void testGetCoordinates() throws DatasourceException {
    APIClient client = new APIClient();
    List<Double> coordinates = client.getCoordinates("24 Sussex Dr, Ottawa, ON K1M 1M4, Canada");

    assertNotNull(coordinates, "Coordinates should not be null");
    assertEquals(2, coordinates.size(), "There should be two elements in the coordinates list");

    // Assuming you know the expected latitude and longitude
    double expectedLat = 45.4444101;
    double expectedLng = -75.69387789999999;

    assertEquals(expectedLat, coordinates.get(0), 0.0001, "Latitude should match the expected value");
    assertEquals(expectedLng, coordinates.get(1), 0.0001, "Longitude should match the expected value");
  }

  // Test method to verify fetching coordinates with another valid address
  @Test
  public void testGetCoordinatesWithAnotherValidAddress() throws DatasourceException {
    List<Double> coordinates = client.getCoordinates("1600 Amphitheatre Parkway, Mountain View, CA");

    assertNotNull(coordinates, "Coordinates should not be null");
    assertEquals(2, coordinates.size(), "There should be two elements in the coordinates list");

    double expectedLat = 37.4224764;
    double expectedLng = -122.0842499;

    assertEquals(expectedLat, coordinates.get(0), 0.0001, "Latitude should match the expected value");
    assertEquals(expectedLng, coordinates.get(1), 0.0001, "Longitude should match the expected value");
  }
  @Test
  public void testGetCoordinatesWithIncorrectAddress() {
    Exception exception = assertThrows(DatasourceException.class, () -> {
      client.getCoordinates("1234 Unknown Street, Imaginary City");
    });

    String expectedMessage = "No results found";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }



  @Test
  public void testGetCoordinatesWithEmptyAddress() {
    Exception exception = assertThrows(DatasourceException.class, () -> {
      client.getCoordinates("");
    });

    String expectedMessage = "API connection not success status Bad Request";
    System.out.println(exception.getMessage());
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
  private APIClient client;
  private HttpURLConnection mockConnection;

  // Method to set up before each test
  @BeforeEach
  public void setUp() throws DatasourceException {
    client = new APIClient();
    mockConnection = mock(HttpURLConnection.class);
    // Set up other necessary mock behaviors
  }

  // Test method to verify successful API call
  @Test
  public void testSuccessfulApiCall() throws Exception {

    Map<String, Object> result = client.getSafetyRatings(41.397158, 2.160873, 2);
    assertNotNull(result);
    assertTrue(result.containsKey("data"));
    List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
    Map<String, Object> firstEntry = dataList.get(0);
    assertTrue(firstEntry.containsKey("type"), "Entry should contain type key");
    assertTrue(firstEntry.containsKey("id"), "Entry should contain id key");
    assertTrue(firstEntry.containsKey("geoCode"), "Entry should contain geoCode key");
    assertTrue(firstEntry.containsKey("safetyScores"), "Entry should contain safetyScores key");

    // Validate geoCode and safetyScores
    Map<String, Double> geoCode = (Map<String, Double>) firstEntry.get("geoCode");
    Map<String, Double> safetyScores = (Map<String, Double>) firstEntry.get("safetyScores");

    assertNotNull(geoCode, "geoCode should not be null");
    assertNotNull(safetyScores, "safetyScores should not be null");

    // Validate specific safety scores
    assertTrue(safetyScores.containsKey("overall"), "safetyScores should contain overall key");
    assertTrue(safetyScores.containsKey("theft"), "safetyScores should contain theft key");
  }

  // Test method for zero radius
  @Test
  public void testZeroRadius() throws DatasourceException {
    Map<String, Object> result = client.getSafetyRatings(41.397158, 2.160873, 0);
    assertNotNull(result);
    assertFalse(result.containsKey("data"));
  }


  // Test method for wrong coordinates
  @Test
  public void testWrongCoord() throws DatasourceException {
    Map<String, Object> result = client.getSafetyRatings(-1018439, -10909209, 0);
    assertNotNull(result);
    assertFalse(result.containsKey("data"));
  }




//  @Test
//  public void testApiCallWithInvalidCoordinates() {
//    // Mock behavior for invalid coordinates
//    // Assertions to check for correct exception or error handling
//  }
//
//  @Test
//  public void testApiCallWithInvalidToken() {
//    // Mock behavior for invalid token
//    // Assertions to check for correct exception or error handling
//  }
//
//  @Test(expected = DatasourceException.class)
//  public void testIOExceptionHandling() throws Exception {
//    when(mockConnection.getInputStream()).thenThrow(new IOException());
//    client.getSafetyRatings(40.7128, 40.706, -74.0060, -74.010, "validToken");
//  }
}
