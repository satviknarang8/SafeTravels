package edu.brown.cs.student.main.Server.SafeTravels;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SafetyHandler implements Route {

  public SafetyHandler() {
  }

  /**
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    return null;


//    String startLoc = request.queryParams("start");
//    String endLoc = request.queryParams("end");
//
//    // add more parameters such as safety priorities
//
//    try {
//      Moshi moshi = new Moshi.Builder().build();
//      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
//      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
//      Map<String, Object> responseMap = new HashMap<>();
//      if (startLoc == null){
//        responseMap.put("type", "error");
//        responseMap.put("error_type", "bad_request");
//        responseMap.put("details", "Starting location was not given in query");
//        responseMap.put("type", "fail");
//        responseMap.put("date_time", LocalDateTime.now().toString());
//        return adapter.toJson(responseMap);
//      }
//      if (endLoc == null){
//        responseMap.put("type", "error");
//        responseMap.put("error_type", "bad_request");
//        responseMap.put("details", "Destination was not given in query");
//        responseMap.put("type", "fail");
//        responseMap.put("date_time", LocalDateTime.now().toString());
//        return adapter.toJson(responseMap);
//      }
//
//
//
//
//
//
//      List<List<String>> broadbandData = this.acsDatasource.getBroadbandData(state, county);
//      if (broadbandData == null || broadbandData.size() == 0){
//        responseMap.put("broadband_data", "does not exist");
//      } else {
//        responseMap.put("broadband_data", broadbandData);
//      }
//      responseMap.put("type", "success");
//      responseMap.put("date_time", LocalDateTime.now().toString());
//      responseMap.put("state", state);
//      responseMap.put("county", county);
//      return adapter.toJson(responseMap);
//    } catch (Exception e) {
//      Moshi moshi = new Moshi.Builder().build();
//      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
//      Map<String, Object> responseMap = new HashMap<>();
//      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
//      responseMap.put("type", "error");
//      responseMap.put("error_type", "datasource");
//      responseMap.put("details", e.getMessage());
//      return adapter.toJson(responseMap);
//    }
  }
}