package edu.brown.cs.student.main.Server.BroadbandRouteUtility;

import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import java.util.Map;

/**
 * The ACSAPISource interface defines a contract for data sources that provide broadband usage
 * information from the Census API.
 */
public interface ACSAPISource {

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
  Map<String, String> getBroadbandUsage(String state, String county)
      throws DatasourceException, InvalidArgsException;
}
