package edu.brown.cs.student.main.acsdatasource;

import edu.brown.cs.student.main.exceptions.DatasourceException;
import java.util.List;

/** The OLD_ACSDatasource is an interface implementing the getBroadbandData function */
public interface OLD_ACSDatasource {

  /**
   * A function to get the broadband data for a state and county
   *
   * @param state The state for which we want the broadband data from
   * @param county The county for which we want the broadband data from
   * @return The results from the api
   * @throws DatasourceException if there is an error in connecting to the acs datasource
   */
  List<List<String>> getBroadbandData(String state, String county) throws DatasourceException;
}
