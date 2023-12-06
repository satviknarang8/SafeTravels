package edu.brown.cs.student.main.csv.Parser;

import edu.brown.cs.student.main.csv.Creator.CreatorFromRow;
import edu.brown.cs.student.main.csv.Creator.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The csvParser class is designed to be able to generically parse CSVs based on a provided strategy
 * pattern.
 *
 * @param <T> A generic T to support the handling of rows into custom data objects.
 */
public class csvParser<T> {

  Reader reader;
  HashMap<String, Integer> columnToIndex;
  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  List<T> rows;

  CreatorFromRow<T> creator;

  /**
   * Initializes the instance variables
   *
   * @param reader A file or string to be parsed
   * @param creator An implementation of CreatorFromRow to customize handling of csv data
   */
  public csvParser(Reader reader, CreatorFromRow<T> creator) {
    this.reader = reader;
    this.creator = creator;
    this.columnToIndex = new HashMap<>();
  }

  /**
   * This method creates a mapping for each of the titles in the header to the corresponding column.
   *
   * @param headerRow A list of strings indicating column titles
   */
  public void readHeader(List<String> headerRow) {
    for (int i = 0; i < headerRow.size(); i++) {
      if (this.columnToIndex.containsKey(headerRow.get(i))) {
        System.err.println(
            "Your CSV cannot have two columns with identical names. \""
                + headerRow.get(i)
                + "\" is repeated");
        System.exit(0);
      }
      this.columnToIndex.put(headerRow.get(i), i);
    }
  }

  /**
   * Returns the index of the column corresponding the column name or -1 if not found
   *
   * @param col The name of the column to search for in our mapping
   * @return Column index corresponding to the column name of -1 for not found
   */
  public Integer getColumnToIndexMap(String col) {
    return this.columnToIndex.getOrDefault(col, -1);
  }

  public Set<String> getColumnNames() {
    return this.columnToIndex.keySet();
  }

  /**
   * This method reads in all the rows from the CSV and converts each row into an object specified
   * by the CreatorFromRow implementation passed into the parser. The program prints an error
   * message and exits in the case of an exception.
   *
   * @param hasHeader A boolean indicating if the first row is the header row
   * @return A list of rows corresponding to the csv data
   */
  public List<T> readRows(Boolean hasHeader) throws ParserException {
    List<T> rows = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(this.reader);
      if (hasHeader) {
        this.readHeader(List.of(regexSplitCSVRow.split(in.readLine())));
      }
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        rows.add(this.creator.create(List.of(regexSplitCSVRow.split(line))));
      }
    } catch (IOException | FactoryFailureException e) {
      throw new ParserException(e.getMessage());
    }
    this.rows = rows;
    return rows;
  }
}
