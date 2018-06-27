package tech.upstream.excel.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SurfaceResponse {
  public List<String> cells;
  public List<List<Object>> values;
}
