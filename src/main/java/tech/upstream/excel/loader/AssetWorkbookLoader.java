package tech.upstream.excel.loader;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AssetWorkbookLoader implements WorkbookLoader {

  @Override
  public String name() {
    return "assets";
  }

  @Override
  public Workbook load(String path) throws IOException {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream stream = classloader.getResourceAsStream("assets/"+ path);
    return new XSSFWorkbook(stream);
  }

}
