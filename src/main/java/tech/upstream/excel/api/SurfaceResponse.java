package tech.upstream.excel.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SurfaceResponse {
  public double[] x;
  public double[] y;
  public List<List<Object>> z;
  public List<String> cells;
}
