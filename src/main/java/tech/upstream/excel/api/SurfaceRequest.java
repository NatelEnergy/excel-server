package tech.upstream.excel.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class SurfaceRequest extends BaseRequest {
  @ApiModelProperty(
    value = "Calculate all permutations of these cells",
    required = true
  )
  public List<CellValueRange> sweep;
  
  @ApiModelProperty(
    value = "List of cell valus to return",
    required = true
  )
  public List<String> read;
}
