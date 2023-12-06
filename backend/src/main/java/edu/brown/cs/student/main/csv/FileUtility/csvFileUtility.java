package edu.brown.cs.student.main.csv.FileUtility;

import edu.brown.cs.student.main.csv.Parser.ParserException;
import edu.brown.cs.student.main.csv.Parser.csvParser;
import java.util.ArrayList;
import java.util.List;

/**
 * The csvFileUtility class is used to search a csv. It makes the assumption that the data from the
 * csv is stored as a list of a list of strings. An important design choice in the implementation of
 * this class is that if a valid number is entered by a user it will be prioritized as a column
 * index rather the column name.
 */
public class csvFileUtility {

  public List<List<String>> rows;
  csvParser<List<String>> parser;

  /**
   * Initializes instance variables passed into the constructor
   *
   * @param parser csvParser that is used to read data from the csv file and populate 'rows'
   * @param hasHeaders a boolean arg passed by the user specifying if the file has a header
   */
  public csvFileUtility(csvParser<List<String>> parser, Boolean hasHeaders) throws ParserException {
    this.parser = parser;
    this.rows = parser.readRows(hasHeaders);
  }

  public csvFileUtility(csvParser<List<String>> parser, List<List<String>> rows) {
    this.parser = parser;
    this.rows = rows;
  }

  /**
   * This method is called by both of the initSearch methods below to perform the query that the
   * user requested. It performs a search across the rows based on a target column, if specified.
   *
   * @param query The search string
   * @param targetColumn The column to search in (-1 if not specified by user)
   * @return A list of rows containing the query
   */
  public List<List<String>> executeSearch(String query, Integer targetColumn) {
    List<List<String>> searchResult = new ArrayList<>();
    for (List<String> row : rows) {
      if ((targetColumn >= 0 && row.get(targetColumn).equals(query))
          || (targetColumn == -1 && row.contains(query))) {
        searchResult.add(row);
      }
    }
    return searchResult;
  }

  /**
   * This method calls executeSearch to determine which rows contain the query. Further, it
   * determines if the column the user passed in is a valid column to search in and handles it
   * accordingly.
   *
   * @param query The search string
   * @param column The column to search in
   * @return A list of rows containing the query
   */
  public List<List<String>> initSearch(String query, String column)
      throws IllegalArgumentException {
    if (!isNum(column) && this.parser.getColumnToIndexMap(column) == -1) {
      throw new IllegalArgumentException(
          "You provided an invalid column to search on: "
              + column
              + ". These are the available columns: "
              + this.parser.getColumnNames());
    } else if (isNum(column)) {
      return executeSearch(query, Integer.valueOf(column));
    } else {
      return executeSearch(query, this.parser.getColumnToIndexMap(column));
    }
  }

  /**
   * This version if initSearch is called when the user does not specify a column in which case it
   * default to -1 to indicate it has not been specified allowing a full search of the rows.
   *
   * @param query The search string
   * @return A list of rows containing the query
   */
  public List<List<String>> initSearch(String query) {
    return this.executeSearch(query, -1);
  }

  /**
   * A helper method to determine if the string corresponding to the column passed in is a valid
   * number in regard to the number of columns in the csv.
   *
   * @param column The column name or number
   * @return A boolean indicating if column is a valid entry by the user
   */
  public boolean isNum(String column) {
    try {
      int val = Integer.parseInt(column);
      return val >= 0 && val < this.rows.get(0).size();
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
