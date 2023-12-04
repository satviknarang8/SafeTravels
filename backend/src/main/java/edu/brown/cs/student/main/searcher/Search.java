package edu.brown.cs.student.main.searcher;

import edu.brown.cs.student.main.creator.CreatorFromRow;
import edu.brown.cs.student.main.exceptions.ParserException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Search class provides methods for searching data within a CSV file. It allows searching for a
 * specific value in a specified column of the CSV file.
 */
public class Search {

  /** The name of the file to search within. */
  // private final String filename;

  private final List<List<String>> rowList;
  /** The value we need to search for in the file. */
  private String value;

  /** The columnIdentifier given by the user (can be nothing as well). */
  private String columnIdentifier = "";

  /**
   * An instance of the CreatorFromRow interface which uses a lambda function to simply return the
   * row that is passed in as a List of Strings.
   */
  private final CreatorFromRow<List<String>> creator = row -> row;

  /** A boolean flag telling us whether the file has headers or not. */
  private final Boolean hasHeaders;

  /**
   * Constructs a new instance of the Search class with the provided parameters.
   *
   * @param rowList the data from the csv file
   * @param value The value to search for within the specified file.
   * @param hasHeaders A boolean flag indicating whether the file has headers or not.
   * @param columnIdentifier The identifier for the specific column in the file to perform the
   *     search on.
   */
  public Search(
      List<List<String>> rowList, String value, Boolean hasHeaders, String columnIdentifier) {
    this.rowList = rowList;
    this.value = value.toLowerCase();
    this.columnIdentifier = columnIdentifier;
    this.hasHeaders = hasHeaders;
  }

  /**
   * The search function searches all the rows in a given file using the parse method and searches
   * each row to find the value searched for by the user, and if it is present, it adds the row to
   * an arraylist and at the end, returns an ArrayList of all rows.
   *
   * @return An ArrayList of rows represented as Strings that contain the value passed into the
   *     constructor
   * @throws ParserException Throws an exception if there is an issue parsing the CSV file
   * @throws FileNotFoundException Throws an exception if the filename is invalid
   */
  public ArrayList<List<String>> search() throws ParserException, FileNotFoundException {
    ArrayList<String> errorList =
        new ArrayList<String>(
            Arrays.asList("ERROR: ")); // Flag to check if there was an error in the searching
    ArrayList<List<String>> result = new ArrayList<>(); // ArrayList to store results
    try {
      if (rowList.isEmpty()) {
        result.add(errorList);
        result.add(Arrays.asList("The csv file is empty"));
        return result;
      }

      // Checking if headers are present, and if so, adding them to the result arraylist
      ArrayList<String> headers = new ArrayList<String>();
      if (this.hasHeaders) {
        headers = new ArrayList<>(rowList.get(0));
        result.add(headers);
      }

      // Converting the given columnIdentifier (if any) into a columnIndex
      int columnIndex = -1;
      boolean columnIdentifierIsNumber = false;
      if (!this.columnIdentifier.isEmpty()) {
        if (!headers.isEmpty()) {
          for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).trim().equalsIgnoreCase(columnIdentifier)) {
              columnIndex = i;
              break;
            }
          }
        }

        /*
        If we reached this part, then the columnIdentifier isn't in the headers so it should be a
        number
        */
        if (columnIndex == -1) {
          try {
            columnIndex = Integer.parseInt(this.columnIdentifier);
            if (columnIndex >= rowList.get(0).size() || columnIndex < 0) {
              result.clear();
              result.add(errorList);
              result.add(
                  Arrays.asList("Please enter a valid number within the bounds of the csv file"));
              return result;
            }
            columnIdentifierIsNumber = true;
          } catch (NumberFormatException e) {

            // If we reached here, the columnIdentifier isn't in the headings and isn't a number
            result.clear();
            result.add(errorList);
            result.add(
                Arrays.asList(
                    "Your heading was not found in the headers provided and it isn't a number."
                        + " Please ensure to "
                        + "input a number or input a valid heading"));
            return result;
          }
        }
      }

      int i = 0;

      // If there are headers, then skip one row to prevent searching for the value in the headers
      if (this.hasHeaders) {
        i += 1;
      }

      // Regex to check for keywords strategically
      Pattern regex =
          Pattern.compile("(?:(?<=\\W)|^)" + Pattern.quote(value.toLowerCase()) + "(?=(?:\\W|$))");

      // Boolean flag to check whether the value is present in any other columns apart from the
      // specified one
      boolean presentInOtherColumns = false;
      ArrayList<String> otherColumns =
          new ArrayList<>(); // Arraylist to keep track of other columns data can be found in
      ArrayList<Integer> columnIndexes =
          new ArrayList<>(); // Arraylist to keep track of other columns data can be found in

      if (columnIndex == -1) {
        // If we reached here, then that means no columnIdentifer was given and we are searching all
        // columns
        for (; i < rowList.size(); i++) {
          for (String data : rowList.get(i)) {
            data = data.toLowerCase();
            this.value = value.toLowerCase();

            Matcher matcher = regex.matcher(data);
            boolean containsPattern =
                matcher.find(); // Flag to check whether or not the word contains the regex pattern
            if (data.equalsIgnoreCase(this.value) || containsPattern) {
              result.add(rowList.get(i));
              break;
            }
          }
        }
      } else {
        // This part means that we were given a valid columnIdentifier
        for (; i < rowList.size(); i++) {
          List<String> data = rowList.get(i);
          for (int j = 0; j < data.size(); j++) {
            String currData = data.get(j);
            currData = currData.toLowerCase();
            Matcher matcher = regex.matcher(currData);
            boolean containsPattern = matcher.find();
            if (currData.trim().equalsIgnoreCase(this.value) || containsPattern) {
              if (j == columnIndex) {
                result.add(data);
              } else {
                // This means that the data is present in another column so we change the flag to
                // True
                presentInOtherColumns = true;
                if ((headers.isEmpty() || columnIdentifierIsNumber) && !columnIndexes.contains(j)) {
                  columnIndexes.add(j);
                } else if (!otherColumns.contains(headers.get(j))) {
                  otherColumns.add(headers.get(j));
                }
              }
            }
          }
        }
      }

      if (((!this.hasHeaders && result.isEmpty()) || (this.hasHeaders && result.size() == 1))
          && presentInOtherColumns) {
        // This means that the value was not found in the requested column but was found in other
        // columns
        result.clear();
        result.add(errorList);
        if (headers.isEmpty() || columnIdentifierIsNumber) {
          // This means the columnIdentifier was an integer
          result.add(
              Arrays.asList(
                  "Your data was not found in your requested column but it was found "
                      + "at the following column indexes (where the leftmost column is 0) "
                      + columnIndexes));
        } else {
          result.add(
              Arrays.asList(
                  "Your data was not found in your requested column but it was found "
                      + "at the following columns "
                      + otherColumns));
        }
        result.add(Arrays.asList("Do you want to try searching again with other criteria?"));
        return result;
      }

      // Condition to check if there is any relevant data. If it just has headers, clear the list
      if (result.size() == 1 && !headers.isEmpty()) {
        result.clear();
      }
      return result;
    } catch (Exception e) {
      result.add(errorList);
      result.add(Arrays.asList("Your value was not found in the file"));
      return result;
    }
  }
}
