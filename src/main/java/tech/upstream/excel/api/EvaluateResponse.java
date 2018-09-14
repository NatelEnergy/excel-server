package tech.upstream.excel.api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class EvaluateResponse {
  public List<EvaluationResult> evaluations;
  public Map<String,Object> results;
}
