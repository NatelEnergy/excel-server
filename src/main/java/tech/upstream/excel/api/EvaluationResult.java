package tech.upstream.excel.api;

import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class EvaluationResult {
  public Object refId;
  public HashMap<String, Object> values;
}
