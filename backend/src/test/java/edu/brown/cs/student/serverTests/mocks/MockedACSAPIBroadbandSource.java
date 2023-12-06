package edu.brown.cs.student.serverTests.mocks;

import edu.brown.cs.student.main.Server.BroadbandRouteUtility.ACSAPISource;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import java.util.Map;

public class MockedACSAPIBroadbandSource implements ACSAPISource {

  private final Map<String, String> constantData;

  public MockedACSAPIBroadbandSource(Map<String, String> data) {
    this.constantData = data;
  }

  /**
   * Retrieves broadband usage information for a specified state and county.
   *
   * @param state The name of the state for which to retrieve broadband usage information.
   * @param county The name of the county within the state for which to retrieve broadband usage
   *     information.
   * @return A Map containing broadband usage data.
   * @throws DatasourceException If there is an issue with the data source.
   * @throws InvalidArgsException If the provided state or county is invalid or not found.
   */
  @Override
  public Map<String, String> getBroadbandUsage(String state, String county)
      throws DatasourceException, InvalidArgsException {
    return constantData;
  }
}
