package tech.upstream.excel;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.hssf.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.repackaged.com.google.common.base.Throwables;

import tech.upstream.excel.api.CellValueRange;
import tech.upstream.excel.api.EvaluateResponse;
import tech.upstream.excel.api.Evaluation;
import tech.upstream.excel.api.EvaluationResult;
import tech.upstream.excel.api.ReadRequest;
import tech.upstream.excel.api.ReadResponse;
import tech.upstream.excel.api.SurfaceRequest;
import tech.upstream.excel.api.SurfaceResponse;

/**
 * Loads a workbook and performs evaluations
 */
@NotThreadSafe
public class WorkbookEvaluator {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public final Sheet sheet;
  public final FormulaEvaluator eval;
  
  // Useful so we can hang on to the sheet
  public EvaluateResponse evaluations;
  public SurfaceResponse surface;
  public ReadResponse read;
  
  CreationHelper factory = null;
  Drawing<?> drawing = null;
  
  public WorkbookEvaluator(Sheet sheet) {
    this.sheet = sheet;
    eval = this.sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
  }

  //---------------------------------------------------------------------------
  // Helper Methods
  //---------------------------------------------------------------------------
  
  public Cell getCell(CellReference ref) {
    Sheet s = sheet;
    if(ref.getSheetName()!=null) {
      s = sheet.getWorkbook().getSheet(ref.getSheetName());
    }
    Row row = s.getRow(ref.getRow());
    if(row==null) {
      return null;
    }
    return row.getCell(ref.getCol());
  }
  
  public Object getCellValue(Cell cell) {
    if(cell==null) {
      return null;
    }
    CellValue val = eval.evaluate(cell);
    switch(val.getCellTypeEnum()) {
      case FORMULA:
      case NUMERIC: return val.getNumberValue();
      case BOOLEAN: return val.getBooleanValue();
      case BLANK:   return null;
      default:
    }
    return val.getStringValue();
  }

  public double getNumericValue(Object v) {
    if(v instanceof Number) {
      return ((Number)v).doubleValue();
    }
    Cell cell = getCell(new CellReference(v.toString()));
    Object out = getCellValue(cell);
    if(out instanceof Number) {
      return ((Number)out).doubleValue();
    }
    throw new IllegalArgumentException("Can not get numeric value from: "+v);
  }
  
  public boolean isNumeric(CellReference ref) {
    return getCell(ref).getCellTypeEnum() == CellType.NUMERIC;
  }
  
  private void setCellValue(Cell cell, Object v) {
    // If the string references another cell, try to parse it
    if(v instanceof String) {
      Cell source = null;
      try {
        source = getCell( new CellReference((String)v) );
      }
      catch(Exception ex) { } // Ignore
      if(source!=null) {
        switch(source.getCellTypeEnum()) {
        case NUMERIC: v = source.getNumericCellValue(); break;
        case STRING: v = source.getStringCellValue(); break;
        default:
          throw new IllegalArgumentException("Unable to copy value from: "+source + " // " + source.getCellTypeEnum() +" // " + v );
        }
      }
    }
    
    switch(cell.getCellTypeEnum()) {
    case NUMERIC: {
      if(v instanceof Number) {
        cell.setCellValue(((Number)v).doubleValue()); 
      }
      else if(v instanceof String) {
        cell.setCellValue(Double.parseDouble(v.toString()));
      }
      else {
        throw new IllegalArgumentException("Can not convert: "+v +" to double cell // " + cell );        
      }
      break;
    }
    case BLANK:
    case STRING: cell.setCellValue(v.toString()); break;
    default:
      throw new IllegalArgumentException("Don't know how to write: "+cell.getCellTypeEnum() +" cells // " + cell + " (set:"+v+")" );
    }
  }

  private void setComment(Cell cell, String text)
  {
    if(factory==null) {
      Workbook wb = sheet.getWorkbook();
      factory = wb.getCreationHelper();
      drawing = sheet.createDrawingPatriarch();
    }
    
    Comment old = cell.getCellComment();
    if(old != null) {
      RichTextString v = old.getString();
      old.setString(factory.createRichTextString( v.getString() + " & " 
          + text.substring(text.lastIndexOf(':')+1).trim() ));
      return;
    }
    
    // When the comment box is visible, have it show in a 1x5 space
    ClientAnchor anchor = factory.createClientAnchor();
    anchor.setCol1(cell.getColumnIndex()+1);
    anchor.setCol2(cell.getColumnIndex()+6);
    anchor.setRow1(cell.getRow().getRowNum());
    anchor.setRow2(cell.getRow().getRowNum()+1);

    // Create the comment and set the text+author
    Comment comment = drawing.createCellComment(anchor);
    RichTextString str = factory.createRichTextString(text);
    comment.setString(str);
    comment.setAuthor("wOS");
    comment.setVisible(true);

    // Assign the comment to the cell
    cell.setCellComment(comment);
  }
  
  //---------------------------------------------------------------------------
  // Calculators
  //---------------------------------------------------------------------------
  
  public void write(HashMap<String, Object> cells, String annotate) {
    if(cells != null) {
      for(Map.Entry<String,Object> entry : cells.entrySet()) {
        try {
          Cell cell = this.getCell(new CellReference(entry.getKey()));
          if(cell == null) {
            throw new IllegalArgumentException("Can not write cell: "+entry.getKey());
          }
          setCellValue(cell,entry.getValue());
          if(annotate!=null) {
            setComment(cell, "Write["+annotate+"] "+entry.getKey()+"="+entry.getValue());
          }
        }
        catch(Exception ex) {
          if(annotate!=null) {
            LOGGER.warn("TODO, add error annotatoin?", ex);
          }
          else {
            Throwables.propagate(ex);
          }
        }
      }
    }
  }
  
  public HashMap<String, Object> read(List<String> cells, String annotate) {
    HashMap<String, Object> values = new HashMap<>();
    if(cells != null) {
      for(String ref : cells) {
        try {
          Cell cell = this.getCell(new CellReference(ref));
          if(cell == null) {
            throw new IllegalArgumentException("Can not write cell: "+ref);
          }
          values.put(ref, getCellValue(cell));
          if(annotate!=null) {
            setComment(cell, "Read["+annotate+"]");
          }
        }
        catch(Exception ex) {
          if(annotate!=null) {
            LOGGER.warn("TODO, add error annotatoin?", ex);
          }
          else {
            Throwables.propagate(ex);
          }
        }
      }
    }
    return values;
  }
  
  public ReadResponse read(ReadRequest req) {
    ReadResponse rsp = new ReadResponse();
    CellRangeAddress address = CellRangeAddress.valueOf(req.range);
    rsp.cols = new ArrayList<>(address.getLastColumn()-address.getLastColumn());
    rsp.rows = new ArrayList<>(address.getLastRow()-address.getFirstRow());
    boolean first = true;
    for(int r=address.getFirstRow(); r<address.getLastRow(); r++) {
      Row row = sheet.getRow(r);
      List<Object> vals = new ArrayList<>(rsp.cols.size());
      for(int c=address.getFirstColumn(); c<address.getLastColumn(); c++) {
        Cell cell = row.getCell(c, MissingCellPolicy.RETURN_NULL_AND_BLANK);
        vals.add(getCellValue(cell));
        if(first) {
          rsp.cols.add( new org.apache.poi.ss.util.CellReference(cell).formatAsString());
        }
      }
      rsp.rows.add(vals);
      first = false;
    }
    return rsp;
  }

  public EvaluationResult evaluate(Evaluation req, String annotate) {
    if(req.read==null || req.read.size()==0) {
      throw new IllegalArgumentException("Evaluation requires at least one 'read' cell");
    }
    
    write(req.write, annotate);
    eval.clearAllCachedResultValues();
    
    EvaluationResult res = new EvaluationResult();
    res.refId = req.refId;
    res.values = read(req.read, annotate);
    return res;
  }
  
  public static class SurfaceSweeper {
    public Cell cell;
    public double[] values;
    public int index = 0;
    public double step = 0;
  }

  public SurfaceSweeper prepare(CellValueRange cfg, String annotate) {
    SurfaceSweeper sweeper = new SurfaceSweeper();
    sweeper.cell = this.getCell(new CellReference(cfg.cell));
    if(annotate!=null) {
      this.setComment(sweeper.cell, annotate + ": "+ cfg.toString());
    }
    
    double _min = this.getNumericValue(cfg.min);
    double _max = this.getNumericValue(cfg.max);
    double diff = _max-_min;
    if(cfg.steps==null) {
      cfg.steps = 25;
    }
    sweeper.step = diff / (double)cfg.steps;
    
    int len = (int)(Math.floor(diff / sweeper.step))+1;
    sweeper.values = new double[len];
    double val = _min;
    for(int i=0; i<len; i++) {
      sweeper.values[i] = val;
      val += sweeper.step;
    }
    sweeper.index = 0;
    return sweeper;
  }
  
  public SurfaceResponse calculateSurface(SurfaceRequest req, String annotate)
  { 
    eval.clearAllCachedResultValues();

    final SurfaceResponse rsp = new SurfaceResponse();
    final List<Cell> output = new ArrayList<>(req.read.size());
    final SurfaceSweeper xvals = prepare(req.x, annotate+" X");
    final SurfaceSweeper yvals = prepare(req.y, annotate+" Y");
    rsp.x = xvals.values;
    rsp.y = yvals.values;
    rsp.z = new ArrayList<List<Object>>(yvals.values.length+1);
    
    rsp.cells = new ArrayList<>(output.size());
    rsp.cells.addAll(req.read);
    for(String ref : req.read) {
      Cell cell = this.getCell(new CellReference(ref));
      output.add( cell );
      if(annotate != null) {
        this.setComment(cell, "read");
      }
    }
    
    double threshold = Double.MIN_VALUE;
    if(req.nullValueIfBelow != null) {
      threshold = req.nullValueIfBelow.doubleValue();
    }
    
    for(double y : yvals.values) {
      setCellValue(yvals.cell, y);
      List<Object> row = new ArrayList<Object>(xvals.values.length);
      for(double x : xvals.values) {
        setCellValue(xvals.cell, x);
        eval.clearAllCachedResultValues();
        if(output.size()==1) {
          row.add(getSurfaceValue(threshold,getCellValue(output.get(0))));
        }
        else {
          List<Object> values = new ArrayList<Object>(output.size());
          for(Cell cell : output) {
            values.add(getSurfaceValue(threshold,getCellValue(cell)));
          }
          row.addAll(values);
        }
      }
      rsp.z.add(row);
    }
    return rsp;
  }
  
  private Object getSurfaceValue(double threshold, Object v) {
    if(v instanceof Number) {
      if(((Number)v).doubleValue() < threshold) {
        return null;
      }
    }
    return v;
  }
}
