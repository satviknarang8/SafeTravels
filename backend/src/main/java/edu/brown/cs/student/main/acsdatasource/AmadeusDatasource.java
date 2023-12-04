package edu.brown.cs.student.main.acsdatasource;

import edu.brown.cs.student.main.exceptions.DatasourceException;
import java.util.List;

public interface AmadeusDatasource {

  List<List<String>> getBroadbandData(String state, String county) throws DatasourceException;



}
