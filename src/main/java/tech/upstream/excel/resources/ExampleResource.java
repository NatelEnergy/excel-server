package tech.upstream.excel.resources;

import tech.upstream.excel.api.EvaluateRequest;
import tech.upstream.excel.api.EvaluateResponse;
import tech.upstream.excel.api.ReadRequest;
import tech.upstream.excel.api.ReadResponse;
import tech.upstream.excel.api.SurfaceRequest;
import tech.upstream.excel.api.SurfaceResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import io.swagger.annotations.*;

@Path("/example")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/example", tags="Examples")
public class ExampleResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public final SheetResource eval;
  final ObjectMapper objectMapper;

  public ExampleResource(SheetResource eval, ObjectMapper objectMapper) {
    this.eval = eval;
    this.objectMapper = objectMapper;
  }

  public EvaluateRequest loadAssetRequestExample(String path) throws IOException
  {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream stream = classloader.getResourceAsStream("assets/"+path);
    if(stream==null) {
      throw new IllegalArgumentException("No resource at: "+path);
    }
    return objectMapper.readValue(stream, EvaluateRequest.class);
  }
  public SurfaceRequest loadAssetSurfaceRequestExample(String path) throws IOException
  {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream stream = classloader.getResourceAsStream("assets/"+path);
    if(stream==null) {
      throw new IllegalArgumentException("No resource at: "+path);
    }
    return objectMapper.readValue(stream, SurfaceRequest.class);
  }
  public ReadRequest loadAssetReadRequestExample(String path) throws IOException
  {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream stream = classloader.getResourceAsStream("assets/"+path);
    if(stream==null) {
      throw new IllegalArgumentException("No resource at: "+path);
    }
    return objectMapper.readValue(stream, ReadRequest.class);
  }

  @GET
  @Path("/read-request")
  public ReadRequest getReadRequest() throws IOException {
    return loadAssetReadRequestExample("req/simple_read.json");
  }
  
  @GET
  @Path("/read-response")
  public ReadResponse getReadResponse() throws IOException {
    return eval.read(getReadRequest());
  }

  @GET
  @Path("/evaluate-request")
  public EvaluateRequest getEvaluateRequest() throws IOException {
    return loadAssetRequestExample("req/simple_eval.json");
  }
  
  @GET
  @Path("/evaluate-response")
  public EvaluateResponse getEvaluateResponse() throws IOException {
    return eval.evaluate(getEvaluateRequest());
  }

  @GET
  @Path("/evaluate-response.xslt")
  public Response getEvaluateResponseXSLT() throws IOException {
    return eval.evaluateAndGetWorkbook(getEvaluateRequest());
  }
  

  @GET
  @Path("/surface-request")
  public SurfaceRequest getSurfaceRequest() throws IOException {
    return loadAssetSurfaceRequestExample("req/simple_surface.json");
    //return loadAssetSurfaceRequestExample("req/slh_surface_single.json");
  }
  
  @GET
  @Path("/surface-response")
  public SurfaceResponse getSurfaceeResponse() throws IOException {
    return eval.surface(getSurfaceRequest());
  }

  @GET
  @Path("/surface-response.xslt")
  public Response getSurfaceResponseXSLT() throws IOException {
    return eval.surfaceAndGetWorkbook(getSurfaceRequest());
  }

  @GET
  @Path("/surface-plot.html")
  @Produces(MediaType.TEXT_HTML)
  public Response getSurfacePlot() throws Exception {
    return eval.surfacePlot(getSurfaceRequest());
  }
  
  @GET
  @Path("/dump")
  @ApiOperation( value="dump", hidden=true )
  @Produces(MediaType.TEXT_PLAIN)
  public String dumpSheet(@QueryParam("path") String path, @QueryParam("path") String range) {
    StringBuilder str = new StringBuilder();
    str.append("TODO: "+ path);
    return str.toString();
  }
}
