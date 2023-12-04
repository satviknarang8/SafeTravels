package edu.brown.cs.student.main.parser;

import edu.brown.cs.student.main.creator.CreatorFromRow;
import edu.brown.cs.student.main.exceptions.ParserException;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The Parser class is responsible for parsing data from a given Reader and transforming it into a
 * list of rows represented by objects. It uses a provided Creator to produce lists of objects from
 * the parsed rows.
 *
 * @param <T> The type of objects to be created and stored in rows.
 */
public class Parser<T> {

  /** This is the regex Pattern to separate the rows into comma separated values. */
  private static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  /** A buffered reader to wrap the generic reader passed in to allow reading line by line. */
  private final BufferedReader bufferedReader;

  /** A CreatorFromRow interface instance to be created in the constructor. */
  private final CreatorFromRow<T> creator;

  private final boolean hasHeaders;

  /**
   * Constructor for Parser object with the parameters described below.
   *
   * @param reader A generic reader to read the data from the source provided
   * @param creator A creator to produce list of objects from the rows in the data
   */
  public Parser(Reader reader, CreatorFromRow<T> creator, boolean hasHeaders) {
    this.bufferedReader = new BufferedReader(reader);
    this.creator = creator;
    this.hasHeaders = hasHeaders;
  }

  /**
   * Parses the data from the Reader and returns a list of rows represented by objects.
   *
   * @return A list of lists of objects where each List of objects represents a parsed row
   * @throws ParserException if there is an exception in parsing the file
   */
  public List<T> parse() throws ParserException {
    List<T> result = new ArrayList<>();
    String currentLine;
    int count = 0;
    try {
      currentLine = this.bufferedReader.readLine();
      while (currentLine != null) {
        if (count == 0 && !this.hasHeaders) {
          currentLine = this.bufferedReader.readLine();
          count += 1;
          continue;
        }
        count += 1;
        List<String> splitRow = Arrays.asList(regexSplitCSVRow.split(currentLine));
        T createdRow = this.creator.create(splitRow);
        result.add(createdRow);
        currentLine = this.bufferedReader.readLine();
      }
    } catch (Throwable e) {
      throw new ParserException(e);
    }
    return result;
  }
}
