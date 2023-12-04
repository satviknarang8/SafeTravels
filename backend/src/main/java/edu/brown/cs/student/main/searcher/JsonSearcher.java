package edu.brown.cs.student.main.searcher;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.FeatureCollection.GeoJSONFeatureCollection.Feature;
import edu.brown.cs.student.main.api.handlers.RedliningHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class JsonSearcher implements Route {

  private RedliningHandler redliningHandler;

  private String filepath = "data/geodata/fullDownload.json";

  public JsonSearcher() {

    this.redliningHandler = new RedliningHandler();
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String keyword = request.queryParams("keyword");
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Map<String, Object>> adapter =
        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
    if (keyword == null) {
      Map<String, Object> errorMap = new HashMap<>();
      errorMap.put("result", "error_bad_request");
      errorMap.put("details", "no inputted keyword. please re-try inputting");
      return adapter.toJson(errorMap);
    }
    try {
      List<Feature> keywordSearched = this.redliningHandler.getKeywordData(keyword);
      System.out.println("hi");
      if (keywordSearched.isEmpty()) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("result", "error_datasource");
        errorMap.put("details", "No data found for inputted variables");
        return adapter.toJson(errorMap);
      }
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("result", "success");
      responseMap.put("data", keywordSearched);
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      Map<String, Object> errorMap = new HashMap<>();
      errorMap.put("result", "error_datasource");
      errorMap.put("details", e.getMessage());
      return adapter.toJson(errorMap);
    }
  }
}
