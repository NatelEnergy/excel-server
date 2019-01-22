package tech.upstream.excel;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.*;
import tech.upstream.excel.health.SimpleHealthCheck;
import tech.upstream.excel.loader.AssetWorkbookLoader;
import tech.upstream.excel.loader.CloudStorageLoader;
import tech.upstream.excel.loader.FileWorkbookLoader;
import tech.upstream.excel.resources.SheetResource;
import tech.upstream.excel.resources.ExampleResource;
import tech.upstream.excel.resources.InfoResource;
import tech.upstream.excel.tasks.EchoTask;
import tech.upstream.excel.util.IllegalArgumentExceptionMapper;

import java.io.File;
import java.lang.invoke.MethodHandles;

import javax.servlet.FilterRegistration;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bazaarvoice.dropwizard.redirect.PathRedirect;
import com.bazaarvoice.dropwizard.redirect.RedirectBundle;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class ExcelServerApplication extends Application<ExcelServerConfiguration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public static void main(String[] args) throws Exception {
    new ExcelServerApplication().run(args);
  }

  @Override
  public String getName() {
    return "upstream-excel";
  }

  @Override
  public void initialize(Bootstrap<ExcelServerConfiguration> bootstrap) {
    bootstrap.addBundle(new SwaggerBundle<ExcelServerConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ExcelServerConfiguration configuration) {
        return configuration.swaggerBundleConfiguration;
      }
    });
    
//    // Custom Jackson stuff
//    SimpleModule testModule = new SimpleModule("UpstreamModule", new Version(1, 0, 0, null, null, null));
//    testModule.addSerializer(CellReference.class, new CellReferenceSerializer());
//    testModule.addDeserializer(CellReference.class, new CellReferenceDeserializer());
//   
//    ObjectMapper mapper = Jackson.newObjectMapper();
//    mapper.registerModule(testModule);
//    bootstrap.setObjectMapper(mapper);
    
    // Enable variable substitution with environment variables
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor(false)
        )
    );
    bootstrap.addBundle(new AssetsBundle());
    
    // Redirect the home to swagger
    bootstrap.addBundle(new RedirectBundle(
      new PathRedirect("/", "/swagger#/Sheet")
    ));
  }

  @Override
  public void run(ExcelServerConfiguration configuration, Environment environment) {
    // Enable CORS headers
    final FilterRegistration.Dynamic cors =
        environment.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Configure CORS parameters
    cors.setInitParameter("allowedOrigins", "*");
    cors.setInitParameter("allowedHeaders", "*"); //X-Requested-With,Content-Type,Content-Length,Accept,Origin,Authorization");
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,PATCH,HEAD");

    // Add URL mapping
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    
    // Workbook locator
    final File f = new File("cache");
    WorkbookLocator locator = new WorkbookLocator();
    locator.register(new AssetWorkbookLoader());
    locator.register(new FileWorkbookLoader());
    if(configuration.googleCloudStorage!=null) {
      locator.register(new CloudStorageLoader(configuration.googleCloudStorage, f));
    }
    SheetResource eval = new SheetResource(locator, environment.getObjectMapper());
    
    environment.healthChecks().register("simple", new SimpleHealthCheck());
    environment.admin().addTask(new EchoTask());
    environment.jersey().register(IllegalArgumentExceptionMapper.class);
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(eval);
    environment.jersey().register(new ExampleResource(eval, environment.getObjectMapper()));
    environment.jersey().register(new InfoResource(environment.getObjectMapper()));
  }
}
