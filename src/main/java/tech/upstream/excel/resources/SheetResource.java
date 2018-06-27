package tech.upstream.excel.resources;

import com.codahale.metrics.annotation.Timed;
import tech.upstream.excel.WorkbookEvaluator;
import tech.upstream.excel.WorkbookLocator;
import tech.upstream.excel.api.BaseRequest;
import tech.upstream.excel.api.EvaluateRequest;
import tech.upstream.excel.api.EvaluateResponse;
import tech.upstream.excel.api.Evaluation;
import tech.upstream.excel.api.ReadRequest;
import tech.upstream.excel.api.ReadResponse;
import tech.upstream.excel.api.SurfaceRequest;
import tech.upstream.excel.api.SurfaceResponse;

import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import io.swagger.annotations.*;

@Path("/sheet")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/sheet", tags="Sheet")
public class SheetResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private static AtomicLong counter = new AtomicLong(5000);
  
  public final WorkbookLocator locator;

  public SheetResource(WorkbookLocator locator) {
    this.locator = locator;
  }

  public WorkbookEvaluator run(BaseRequest req, boolean annotate) {
    Sheet sheet = locator.getSheet(req.sheet);
    WorkbookEvaluator eval = new WorkbookEvaluator(sheet);
    
    if(req.setup != null) {
      eval.write(req.setup, annotate ? "Setup" : null);
    }
    
    if(req instanceof EvaluateRequest) {
      EvaluateRequest ereq = (EvaluateRequest)req;
      if(ereq.evaluations == null || ereq.evaluations.size()==0) {
        throw new IllegalArgumentException("request is missing evaluations");
      }
      Set<Object> ids = new HashSet<>();
      eval.evaluations = new EvaluateResponse();
      eval.evaluations.evaluations = new ArrayList<>(ereq.evaluations.size()+1);
      for(Evaluation r : ereq.evaluations) {
        counter.incrementAndGet();
        if(r.refId == null) {
          counter.get();
        }
        if(!ids.add(r.refId)) {
          throw new IllegalArgumentException("Duplicate Evaluatoin RefIDs found: "+ids.toString());
        }
        eval.evaluations.evaluations.add( eval.evaluate(r, annotate?r.refId.toString():null) );
      }
    }
    else if(req instanceof SurfaceRequest) {
      SurfaceRequest sreq = (SurfaceRequest)req;
      eval.surface = eval.calculateSurface( sreq, annotate ? "Surface" : null);
    }
    else if(req instanceof ReadRequest) {
      ReadRequest sreq = (ReadRequest)req;
      eval.read = eval.read(sreq);
    }
    return eval;
  }

  @POST
  @Timed(name = "read-requests")
  @ApiOperation(value = "Read a Spreadsheet", 
    notes = "Simple read for spreadsheet"
  )
  @Path("/read")
  public ReadResponse read(ReadRequest req) {
    return run(req, false).read;
  }
  
  @POST
  @Timed(name = "evaluate-requests")
  @ApiOperation(value = "Evaluate a Spreadsheet", 
    notes = "Run a list of evaluations against the selected sheet"
  )
  @Path("/evaluate")
  public EvaluateResponse evaluate(EvaluateRequest req) {
    return run(req, false).evaluations;
  }

  @POST
  @Path("/evaluate.xslx")
  @ApiOperation(value = "Get annotated workbook", 
    notes = "Returns workbook with cell annotations based on the request parameters"
  )
  @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" )
  public Response evaluateAndGetWorkbook(EvaluateRequest req) {
    final WorkbookEvaluator ex = run(req, true);
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
          try {
              ex.sheet.getWorkbook().write(output);
          } catch (Exception e) {
              throw new WebApplicationException(e);
          }
      }
    };
    return Response.ok(stream).header("content-disposition","attachment; filename = evaluate.xlsx").build();
  }
  
  @POST
  @Timed(name = "surface-requests")
  @ApiOperation(value = "Generate a surface from sheet", 
    notes = "Calculate multiple permutations of input values"
  )
  @Path("/surface")
  public SurfaceResponse surface(SurfaceRequest req) {
    return run(req, false).surface;
  }

  @POST
  @Path("/surface.xslx")
  @ApiOperation(value = "Get annotated surface workbook", 
    notes = "Returns workbook with cell annotations based on the request parameters"
  )
  @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" )
  public Response surfaceAndGetWorkbook(SurfaceRequest req) {
    final WorkbookEvaluator ex = run(req, true);
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
          try {
              ex.sheet.getWorkbook().write(output);
          } catch (Exception e) {
              throw new WebApplicationException(e);
          }
      }
    };
    return Response.ok(stream).header("content-disposition","attachment; filename = evaluate.xlsx").build();
  }
}
