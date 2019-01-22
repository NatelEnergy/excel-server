package tech.upstream.excel;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import tech.upstream.excel.ExcelServerApplication;
import tech.upstream.excel.ExcelServerConfiguration;
import tech.upstream.excel.api.EvaluateRequest;
import tech.upstream.excel.api.EvaluateResponse;
import tech.upstream.excel.api.EvaluationResult;
import tech.upstream.excel.resources.ExampleResource;

import org.apache.poi.hssf.util.CellReference;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

  private static final String TMP_FILE = createTempFile();
  private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");

  @ClassRule
  public static final DropwizardAppRule<ExcelServerConfiguration> RULE = 
    new DropwizardAppRule<>(
        ExcelServerApplication.class, CONFIG_PATH
       // ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE)
    );
  
  private static ExampleResource EXAMPLES;

  @BeforeClass
  public static void migrateDb() throws Exception {
    // RULE.getApplication().run("db", "migrate", CONFIG_PATH);
    //LOCATOR = new WorkbookLocator(Files.createTempDirectory("xls-test").toFile(), RULE.getObjectMapper() );
    EXAMPLES = new ExampleResource(null, RULE.getObjectMapper());
  }

  private static String createTempFile() {
    try {
      return File.createTempFile("test-example", null).getAbsolutePath();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void testEvaluationRuns() throws Exception {
    CellReference ref = new CellReference("sheet!B12");
    assertThat(ref.getSheetName()).isEqualTo("sheet");
    assertThat(ref.getRow()).isEqualTo(11);
    assertThat(ref.getCol()).isEqualTo((short)1);
    
    final EvaluateRequest req = EXAMPLES.getEvaluateRequest(); //.loadAssetRequestExample("hydro/slh_01.json");
    System.out.println( "REQ: " + RULE.getObjectMapper().writeValueAsString(req));
    
    final EvaluateResponse rsp = postRequest(req);
    
    EvaluationResult res = rsp.evaluations.get(0);
    
    // Setting a formula works
    assertThat(res.values.get( "D4" )).isEqualTo(11.0);
    
    // Simple math
    assertThat(res.values.get( "D5" )).isEqualTo(6.0);
    
    System.out.println( "RSP: "+ RULE.getObjectMapper().writeValueAsString(rsp));
  }

  private EvaluateResponse postRequest(EvaluateRequest req) {
    return RULE.client()
        .target("http://localhost:" + RULE.getLocalPort() + "/sheet/evaluate")
        .request()
        .post(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
        .readEntity(EvaluateResponse.class);
  }

  @Test
  public void testLogFileWritten() throws IOException {
    // The log file is using a size and time based policy, which used to silently
    // fail (and not write to a log file). This test ensures not only that the
    // log file exists, but also contains the log line that jetty prints on startup
    final Path log = Paths.get("./logs/application.log");
    assertThat(log).exists();
    final String actual = new String(Files.readAllBytes(log), UTF_8);
    assertThat(actual).contains("0.0.0.0:" + RULE.getLocalPort());
  }
}
