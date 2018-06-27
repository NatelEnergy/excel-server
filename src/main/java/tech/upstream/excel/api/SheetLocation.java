package tech.upstream.excel.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class SheetLocation {
  @ApiModelProperty(
    value = "Where is the sheet stored", 
    example = "gs|assets|etc",
    required = true
  )
  public String source;
  
  @ApiModelProperty(
    value = "path within storage", 
    example = "path/in/storage.xlsx",
    required = true
  )
  public String path;

  @ApiModelProperty(
    value = "Name of the sheet within the workspace", 
    example = "Sheet2",
    required = false
  )
  public String defaultSheet;
}
