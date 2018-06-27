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

//  @ApiModelProperty(
//    value = "Increment value from min to max", 
//    example = "0.1",
//    required = false
//  )
//  public Double step;

  @ApiModelProperty(
    value = "Rather than passing the exact increment, calculate it", 
    example = "50",
    required = false
  )
  public Integer steps;
  
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("min: ").append(min).append(",");
    str.append("max: ").append(max).append(",");
    str.append("steps: ").append(steps);
    return str.toString();
  }
}
