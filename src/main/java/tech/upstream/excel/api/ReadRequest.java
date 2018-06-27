package tech.upstream.excel.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class ReadRequest extends BaseRequest {

  @ApiModelProperty(
    value = "Area Ref. May also be a whole row range (e.g. \"3:5\"), or a whole column range (e.g. \"C:F\")", 
    example = "B1:D8",
    required = true
  )
  public String range;
}
