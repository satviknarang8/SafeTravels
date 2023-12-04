package edu.brown.cs.student.main.exceptions;

/** A general exception class for any errors in getting the DataSource */
public class DatasourceException extends Exception {

  /** The cause for the exception */
  private final Throwable cause;

  /**
   * Printing the message of the error
   *
   * @param message The message we want printed
   */
  public DatasourceException(String message) {
    super(message);
    this.cause = null;
  }

  /**
   * Throws an error specific to cause and message
   *
   * @param message Message we want to print
   * @param cause Cause of error
   */
  public DatasourceException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }
}
