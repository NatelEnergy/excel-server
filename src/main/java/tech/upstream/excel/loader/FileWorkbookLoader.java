package tech.upstream.excel.loader;

import java.io.File;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileWorkbookLoader implements WorkbookLoader {
  @Override
  public String name() {
    return "file";
  }

  @Override
  public Workbook load(String path) throws IOException, InvalidFormatException {
    return new XSSFWorkbook(new File(path));
  }
}
