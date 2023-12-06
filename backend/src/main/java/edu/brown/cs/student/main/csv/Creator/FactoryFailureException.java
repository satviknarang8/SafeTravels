package edu.brown.cs.student.main.csv.Creator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an error provided to catch any error that may occur when you create an object from a row.
 * Feel free to expand or supplement or use it for other purposes.
 */
public class FactoryFailureException extends Exception {
  final List<String> row;

  /**
   * An exception used by the CreatorFromRow interface to handle issues with creating data objects
   * from rows in the csv.
   *
   * @param message Descriptive error message of Factory Failure
   * @param row The row causing factory failure
   */
  public FactoryFailureException(String message, List<String> row) {
    super(message);
    this.row = new ArrayList<>(row);
  }
}
