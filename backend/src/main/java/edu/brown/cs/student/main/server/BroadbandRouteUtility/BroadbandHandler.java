package edu.brown.cs.student.main.Server.BroadbandRouteUtility;

import edu.brown.cs.student.main.Server.CSVRouteUtility.CSVResponse;
import edu.brown.cs.student.main.Server.Exceptions.DatasourceException;
import edu.brown.cs.student.main.Server.Exceptions.InvalidArgsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

/** The BroadbandHandler class handles HTTP requests related to broadband usage data retrieval. */
public class BroadbandHandler implements Route {

  // Data source for retrieving broadband usage information
  private final ACSAPISource state;

  /**
   * Constructs a BroadbandHandler instance with a data source for ACS API broadband data.
   *
   * @throws DatasourceException If there is an issue with the data source.
   */
  public BroadbandHandler(ACSAPISource state) throws DatasourceException {
    this.state = state;
  }

  /**
   * Handles an HTTP request to retrieve broadband usage data for a specified state and county.
   *
   * @param request The HTTP request object.
   * @param response The HTTP response object.
   * @return A serialized response containing broadband usage data.
   */
  @Override
  public Object handle(Request request, Response response) throws InvalidArgsException {
    Map<String, Object> result = new HashMap<>();
    Set<String> params = request.queryParams();

    try {
      if (!params.contains("state") || !params.contains("county")) {
        result.put("result", "error_bad_request");
        result.put("error_message", "Please provide both a state and county in your request.");
      } else {
        String state = request.queryParams("state").toLowerCase();
        String county = request.queryParams("county").toLowerCase();
        Map<String, String> broadbandUsage = this.state.getBroadbandUsage(state, county);
        result.put("time", broadbandUsage.get("time"));
        String data = broadbandUsage.get("data");
        String[] rows = data.split("], \\[");
        List<List<String>> formattedData = new ArrayList<>();
        String[] headerValues = rows[0].substring(2).split(", ");
        List<String> header = new ArrayList<>();
        for (String value : headerValues) {
          header.add("\"" + value + "\"");
        }
        formattedData.add(header);
        for (int i = 1; i < rows.length; i++) {
          String row = rows[i];
          if (i == rows.length - 1) {
            row = row.substring(0, row.length() - 1);
          }
          String[] values = row.substring(0, row.length() - 1).split(", ");
          List<String> formattedRow = new ArrayList<>();
          formattedRow.add("\"" + values[0] + ", " + values[1] + "\"");
          for (int j = 2; j < values.length; j++) {
            formattedRow.add("\"" + values[j] + "\"");
          }
          formattedData.add(formattedRow);
        }
        result.put("data", formattedData);
        result.put("result", "success");
        result.put("state", state);
        result.put("county", county);
      }
    } catch (DatasourceException e) {
      result.put("result", "error_datasource");
      result.put("error_message", e.getMessage());
    } catch (InvalidArgsException e) {
      result.put("result", "error_bad_request");
      result.put("error_message", e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
    CSVResponse CSVResponse = new CSVResponse(result);
    return CSVResponse.serialize();
  }
}
