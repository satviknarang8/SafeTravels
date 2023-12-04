package edu.brown.cs.student.main.exceptions;

/**
 * This is a class provided to catch any error that may occur when you are parsing the rows from a
 * given CSV.
 */
public class ParserException extends Exception {

  /**
   * Constructs a new ParserException with the specified cause.
   *
   * @param cause The cause of the exception
   */
  public ParserException(Throwable cause) {
    super(cause);
  }
}
