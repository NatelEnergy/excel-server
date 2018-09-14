package tech.upstream.excel.resources;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/info", tags="System Info")
public class InfoResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private final ObjectMapper mapper;
  
  public InfoResource(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  
  public Map<String,Object> loadGit() throws Exception {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream stream = classLoader.getResourceAsStream("git.json");
    return mapper.readValue(stream, HashMap.class);
  }
  
  @GET
  @Path("build")
  @ApiOperation( value="show the build info", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Response git() throws Exception {
    return Response.ok(loadGit()).build();
  }

  public String getGitDescription() {
    try {
      Map<String,Object> git = loadGit();
      String time = (String)git.get("git.build.time");
      String ref = (String)git.get("git.commit.id.abbrev");
      String ver = (String)git.get("git.build.version");
      return ver + " (" + ref + ") " + time;
    }
    catch(Exception ex) {}
    return "<unknown>";
  }
}
