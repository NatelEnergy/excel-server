package tech.upstream.excel.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob;


public class CloudStorageLoader implements WorkbookLoader {
  public final CloudStorageConfig cfg;
  public final Path root;
  
  public CloudStorageLoader(CloudStorageConfig cfg, File cache)
  {
    this.cfg = cfg;
    this.root = cache.toPath().resolve(cfg.name);
    
  }

  @Override
  public String name() {
    return cfg.name;
  }

  @Override
  public Workbook load(String path) throws IOException, InvalidFormatException {
    Path local = root.resolve(path);
    if(Files.exists(local)) {
      local.getParent().toFile().mkdirs();
      Storage storage = StorageOptions.getDefaultInstance().getService();
      Blob blob = storage.get(cfg.bucket, path);
      blob.downloadTo(local);
    }
    return new XSSFWorkbook(local.toFile());
  }
}
