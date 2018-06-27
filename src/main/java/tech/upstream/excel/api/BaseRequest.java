package tech.upstream.excel.api;

import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class BaseRequest {
  public SheetLocation sheet;
  
  @ApiModelProperty(
    value = "Cell values set when the sheet is loaded.  { CellRef: value }"
  )
  public HashMap<String, Object> setup;
}
