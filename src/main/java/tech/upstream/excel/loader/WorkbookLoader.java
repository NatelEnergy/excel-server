package tech.upstream.excel.loader;

import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

@ThreadSafe
public interface WorkbookLoader {
  /**
   * Get the name of this loader
   */
  public String name();
  
  /**
   * Returns a workbook that can be manipulated without any side affects
   * @throws IOException 
   * @throws InvalidFormatException 
   */
  public Workbook load(String path) throws IOException, InvalidFormatException;
}
