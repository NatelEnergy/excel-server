package tech.upstream.excel.api;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class Evaluation {
  @ApiModelProperty(
    value = "an optional ID that is returned in the results", 
    example = "YourUniqueID",
    required = false
  )
  public Object refId;
  
  @ApiModelProperty(
    value = "Write each value before returning  { CellRef: value }",
    required = false
  )
  public HashMap<String, Object> write;

  @ApiModelProperty(
    value = "List of cell valus to return",
    required = true
  )
  public List<String> read;
}
