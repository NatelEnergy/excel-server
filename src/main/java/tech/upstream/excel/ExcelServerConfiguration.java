package tech.upstream.excel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import tech.upstream.excel.loader.CloudStorageConfig;

public class ExcelServerConfiguration extends Configuration {
  @JsonProperty
  public CloudStorageConfig googleCloudStorage;
  
  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;
}
