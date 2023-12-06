package edu.brown.cs.student.main.csv.Creator;

import java.util.List;

/**
 * This interface defines a method that allows your CSV parser to convert each row into an object of
 * some arbitrary passed type.
 *
 * <p>Your parser class constructor should take a second parameter of this generic interface type.
 */
public interface CreatorFromRow<T> {

  /**
   * The create method turns a single row in the csv into a generic T chosen by the developer.
   *
   * @param row The list of strings corresponding to a single row in the csv
   * @return The generic T that the list of strings is turned into, such as a custom object
   * @throws FactoryFailureException Occurs when there is an issue with creating the passed generic
   */
  T create(List<String> row) throws FactoryFailureException;
}
