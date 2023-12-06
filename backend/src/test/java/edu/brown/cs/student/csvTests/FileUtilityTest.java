package edu.brown.cs.student.csvTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.brown.cs.student.main.csv.Creator.defaultSearchCreator;
import edu.brown.cs.student.main.csv.FileUtility.csvFileUtility;
import edu.brown.cs.student.main.csv.Parser.ParserException;
import edu.brown.cs.student.main.csv.Parser.csvParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import org.junit.jupiter.api.Test;

/** This class is responsible for testing the csvFileUtility across various user test cases. */
public class FileUtilityTest {

  /**
   * Tests the helper method isNum to determine if it is able to properly determine which columns
   * represent a valid column index.
   *
   * @throws FileNotFoundException If the file used by the parser is not found
   */
  @Test
  public void testIsNum() throws FileNotFoundException, ParserException {
    FileReader input = new FileReader("data/census/income_by_race_edited.csv");
    csvParser<List<String>> parser = new csvParser<List<String>>(input, new defaultSearchCreator());
    csvFileUtility utility = new csvFileUtility(parser, Boolean.TRUE);
    assertFalse(utility.isNum("-1"));
    assertFalse(utility.isNum("100"));
    assertTrue(utility.isNum("4"));
    assertTrue(utility.isNum("0"));
    assertFalse(utility.isNum("9"));
    assertFalse(utility.isNum("Star"));
  }

  /**
   * Tests various use cases of executing a search. This includes specifying a column to search in,
   * searching across the entire csv, looking for invalid data, looking for data that is in a
   * different column, searching by column index, and searching with no header present.
   *
   * @throws FileNotFoundException If the csv file used by parser is not found
   */
  @Test
  public void testInitExecuteSearch() throws FileNotFoundException, ParserException {
    FileReader input = new FileReader("data/census/income_by_race_edited.csv");
    csvFileUtility utility =
        new csvFileUtility(
            new csvParser<List<String>>(input, new defaultSearchCreator()), Boolean.TRUE);

    List<List<String>> result = utility.initSearch("Total", "Race");
    assertEquals(utility.initSearch("Total").size(), result.size());
    assertEquals("Total", result.get(15).get(1));

    assertEquals(0, utility.initSearch("Total", "Year").size());
    assertEquals(0, utility.initSearch("Pizza", "ID Geography").size());
    assertEquals(0, utility.initSearch("Bristol County, RI").size());

    result = utility.initSearch("bristol-county-ri");
    assertEquals(utility.initSearch("bristol-county-ri", "8").size(), result.size());
    assertEquals(utility.initSearch("bristol-county-ri", "Slug Geography").size(), result.size());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          utility.initSearch("RI", "9999");
        });
  }
}
