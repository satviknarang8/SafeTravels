package edu.brown.cs.student.main.csv.Stars;

/**
 * A record modeling a Star based on data provided in the stardata and ten-star csv files
 *
 * @param id StarID
 * @param name ProperName
 * @param x X
 * @param y Y
 * @param z Z
 */
public record Star(Integer id, String name, Double x, Double y, Double z) {
  public Star {
    if (id == null || name == null || x == null || y == null || z == null)
      throw new IllegalArgumentException("Star cannot have null arguments!");
  }
}
