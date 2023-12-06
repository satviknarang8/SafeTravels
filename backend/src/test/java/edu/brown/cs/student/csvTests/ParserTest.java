package edu.brown.cs.student.csvTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.csv.Creator.defaultSearchCreator;
import edu.brown.cs.student.main.csv.Parser.ParserException;
import edu.brown.cs.student.main.csv.Parser.csvParser;
import edu.brown.cs.student.main.csv.Stars.Star;
import edu.brown.cs.student.main.csv.Stars.StarCreator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * This class is responsible for testing the csvParser across various csv Files and implementations
 * of CreatorFromRow
 */
public class ParserTest {

  /**
   * This test check if the readHeader method is able to properly turn a header row into a mapping
   * which can be used by our fileUtility later on.
   */
  @Test
  public void testReadHeader() {
    csvParser<List<String>> parser =
        new csvParser<List<String>>(new StringReader(""), new defaultSearchCreator());
    List<String> lst = new ArrayList<>(List.of("Color", "Size", "Shape"));
    parser.readHeader(lst);
    assertEquals(0, parser.getColumnToIndexMap("Color"));
    assertEquals(2, parser.getColumnToIndexMap("Shape"));
    assertEquals(-1, parser.getColumnToIndexMap("pizza"));
  }

  /** This test check if we are able to parse data when there is no header row present. */
  @Test
  public void testReadRowsWithoutHeaders() throws ParserException {
    StringReader input =
        new StringReader(
            "RI,White,\" $1,058.47 \",395773.6521, $1.00 ,75%\n"
                + "RI,Black, $770.26 ,30424.80376, $0.73 ,6%\n"
                + "RI,Native American/American Indian, $471.07 ,2315.505646, $0.45 ,0%\n"
                + "RI,Asian-Pacific Islander,\" $1,080.09 \",18956.71657, $1.02 ,4%\n"
                + "RI,Hispanic/Latino, $673.14 ,74596.18851, $0.64 ,14%\n"
                + "RI,Multiracial, $971.89 ,8883.049171, $0.92 ,2%");
    csvParser<List<String>> parser = new csvParser<List<String>>(input, new defaultSearchCreator());
    List<List<String>> rows = parser.readRows(Boolean.FALSE);
    assertEquals(6, rows.size());
    assertEquals(
        List.of("RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%"), rows.get(0));
    assertEquals("Multiracial", rows.get(5).get(1));
    assertEquals(-1, parser.getColumnToIndexMap("Race"));
  }

  /**
   * This test checks if we are able to parse data when there is a header present.
   *
   * @throws FileNotFoundException If an invalid csv file is passed to parser
   */
  @Test
  public void testReadRowsWithHeaders() throws FileNotFoundException, ParserException {
    FileReader input = new FileReader("data/census/income_by_race_edited.csv");
    csvParser<List<String>> parser = new csvParser<List<String>>(input, new defaultSearchCreator());
    List<List<String>> rows = parser.readRows(Boolean.TRUE);
    assertEquals(323, rows.size());
    assertEquals("85413", rows.get(0).get(4));
    assertEquals(1, parser.getColumnToIndexMap("Race"));
    assertEquals(
        List.of(
            "0",
            "Total",
            "2020",
            "2020",
            "75857",
            "2022",
            "\"Kent County, RI\"",
            "05000US44003",
            "kent-county-ri"),
        rows.get(1));
    assertEquals(-1, parser.getColumnToIndexMap("Name"));
  }

  /**
   * This tests a non-default implementation of CreatorFromRow which turns rows into Stars. It
   * ensures we achieve the same outcomes that we did with our defaultSearchCreator.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void testReadRowsWithStarData() throws FileNotFoundException, ParserException {
    FileReader input = new FileReader("data/stars/ten-star.csv");
    csvParser<Star> parser = new csvParser<Star>(input, new StarCreator());
    List<Star> rows = parser.readRows(Boolean.TRUE);
    assertEquals(10, rows.size());
    assertEquals(70667, rows.get(5).id());
    assertEquals(1, parser.getColumnToIndexMap("ProperName"));
  }
}
