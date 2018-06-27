package tech.upstream.excel.loader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob;


public class CloudStorageLoader implements WorkbookLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
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
    if(!Files.exists(local)) {
      LOGGER.info("Download: "+path + " from: "+name());
      local.getParent().toFile().mkdirs();
      Storage storage = StorageOptions.getDefaultInstance().getService();
      Blob blob = storage.get(cfg.bucket, path);
      blob.downloadTo(local);
    }
    File f = local.toFile();
    if(f.length()<10) {
      f.delete();
      throw new IOException("Invalid downloaded file! try again");
    }
    return new XSSFWorkbook(f);
  }
}
