package edu.brown.cs.student.main.csv.Stars;

import edu.brown.cs.student.main.csv.Creator.CreatorFromRow;
import edu.brown.cs.student.main.csv.Creator.FactoryFailureException;
import java.util.List;

/**
 * A class implementing CreatorFromRow to parse data from csv files that conform to the star record.
 */
public class StarCreator implements CreatorFromRow<Star> {

  /**
   * Converts a row into a Star
   *
   * @param row The list of strings corresponding to a single row in the csv
   * @return A star that is made from the data in the row
   * @throws FactoryFailureException Handles an error with creating the Star
   */
  @Override
  public Star create(List<String> row) throws FactoryFailureException {
    try {
      return new Star(
          Integer.valueOf(row.get(0)),
          row.get(1),
          Double.valueOf(row.get(2)),
          Double.valueOf(row.get(3)),
          Double.valueOf(row.get(4)));
    } catch (IllegalArgumentException e) {
      throw new FactoryFailureException(
          "There was an issue with parsing the CSV. Please check the following row: " + row, row);
    }
  }
}
