package tech.upstream.excel;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import tech.upstream.excel.api.SheetLocation;
import tech.upstream.excel.loader.WorkbookLoader;


@ThreadSafe
public class WorkbookLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  final Map<String, WorkbookLoader> loaders;
  
  public WorkbookLocator() {
    loaders = new ConcurrentHashMap<>();
  }
  
  public void register(WorkbookLoader loader)
  {
    loaders.put(loader.name(), loader);
  }
//  
//  public Workbook load(String key) throws Exception {
//    File f = new File(cache, key);
//    if(!f.exists()) {
//      int idx = key.indexOf('@');
//      if(idx <= 0 ) {
//        throw new IllegalArgumentException("Key should include src & path: (gcloud@path/from/configured/bucket)");
//      }
//      String src = key.substring(0, idx);
//      String path = key.substring(idx+1);
//      
//      if("assets".equals(src)) {
//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        f.getParentFile().mkdirs();
//        InputStream stream = classloader.getResourceAsStream("assets/"+ path);
//        LOGGER.info("writing: "+key + " TO "+f.getAbsolutePath());
//        FileUtils.copyInputStreamToFile(stream, f);
//      }
//      else if("gcloud".equals(src)) {
//        throw new IllegalArgumentException("TODO! implement gcloud fetch");
//      }
//      else {
//        throw new IllegalArgumentException("Unknown workbook source: "+src);
//      }
//    }
//    // Read from disk every time.  This will force them to be thread safe
//    return new XSSFWorkbook(f);
//  }
  
  public Workbook getWorkbook(SheetLocation location) {
    WorkbookLoader loader = loaders.get(location.source);
    if(loader==null) {
      throw new IllegalArgumentException("Unknown loader: "+location.source);
    }
    
    try {
      Workbook wb = loader.load(location.path);
      if(wb==null) {
        throw new IllegalArgumentException("Unable to find workbook: "+location.path);
      }
      return wb;
    } 
    catch (Exception e) {
      if(e.getCause() instanceof IllegalArgumentException) {
        throw (IllegalArgumentException)e.getCause();
      }
      throw new IllegalArgumentException("Unable to find workbook: "+location.path, e);
    }
  }

  public Sheet getSheet(SheetLocation location) {
    Workbook wb = getWorkbook(location);
    Sheet sheet = wb.getSheetAt(0);
    if(!Strings.isNullOrEmpty(location.defaultSheet)) {
      sheet = wb.getSheet(location.defaultSheet);
      if(sheet==null) {
        StringBuilder str = new StringBuilder();
        for(int i=0; i<wb.getNumberOfSheets(); i++) {
          if(str.length()>0) {
            str.append(", ");
          }
          str.append(wb.getSheetName(i));
        }
        str.append("]");
        throw new IllegalArgumentException("unknown sheet: "+location.defaultSheet + "  ["+str);
      }
    }
    wb.setActiveSheet(wb.getSheetIndex(sheet));
    return sheet;
  }
}
