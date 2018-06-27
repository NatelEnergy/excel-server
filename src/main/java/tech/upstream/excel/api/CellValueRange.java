package tech.upstream.excel.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class CellValueRange {
  @ApiModelProperty(
    value = "Cell Reference", 
    example = "B22 or Sheet2!G55",
    required = true
  )
  public String cell;
  
  @ApiModelProperty(
    value = "Minimum Value.  Number or Cell Reference", 
    example = "1.23 or Sheet2!G55",
    required = true
  )
  public Object min;

  @ApiModelProperty(
    value = "Maximum Value.  Number or Cell Reference", 
    example = "1.23 or Sheet2!G55",
    required = true
  )
  public Object max;

  @ApiModelProperty(
    value = "Rather than passing the exact increment, calculate it", 
    example = "50",
    required = false
  )
  public Integer steps;

  @ApiModelProperty(
    value = "Optional label used in plot", 
    required = false
  )
  public String label;
  
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("min: ").append(min).append(",");
    str.append("max: ").append(max).append(",");
    str.append("steps: ").append(steps);
    return str.toString();
  }
}
