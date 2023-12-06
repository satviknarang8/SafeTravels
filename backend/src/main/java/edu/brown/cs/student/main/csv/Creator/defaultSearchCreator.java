package edu.brown.cs.student.main.csv.Creator;

import java.util.List;

/** Implementation of CreatorFromRow that turns rows of data in csv into a list of strings. */
public class defaultSearchCreator implements CreatorFromRow<List<String>> {

  /**
   * The create method implementation which is used by our csvParser to handle csv data as a list of
   * strings.
   *
   * @param row The list of strings corresponding to a single row in the csv
   * @return A list of strings corresponding to the data in the row
   * @throws FactoryFailureException Determines if the list of string output is successfully created
   */
  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    return row;
  }
}
