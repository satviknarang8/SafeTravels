package edu.brown.cs.student.geocodetests;
import edu.brown.cs.student.main.Server.SafeTravels.GeocodingAPIClient;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class GeocodingAPIClientTest {
  @Test
  public void testGetCoordinates() throws DatasourceException {
    GeocodingAPIClient client = new GeocodingAPIClient();
    List<Double> coordinates = client.getCoordinates("24 Sussex Dr, Ottawa, ON K1M 1M4, Canada");

    assertNotNull(coordinates, "Coordinates should not be null");
    assertEquals(2, coordinates.size(), "There should be two elements in the coordinates list");

    // Assuming you know the expected latitude and longitude
    double expectedLat = 45.4444101;
    double expectedLng = -75.69387789999999;

    assertEquals(expectedLat, coordinates.get(0), 0.0001, "Latitude should match the expected value");
    assertEquals(expectedLng, coordinates.get(1), 0.0001, "Longitude should match the expected value");
  }
}
