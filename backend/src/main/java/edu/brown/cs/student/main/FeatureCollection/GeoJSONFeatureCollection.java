package edu.brown.cs.student.main.FeatureCollection;

import java.util.List;
import java.util.Map;

public record GeoJSONFeatureCollection(String type, List<Feature> features) {

  public static record Feature(String type, Geometry geometry, Map<String, Object> properties) {}

  public static record Geometry(String type, List<List<List<List<Double>>>> coordinates) {}
}
