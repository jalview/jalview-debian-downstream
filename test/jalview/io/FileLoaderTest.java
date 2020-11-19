package jalview.io;

import org.junit.Assert;
import org.testng.annotations.Test;

public class FileLoaderTest
{

  @Test(groups = { "Network" })
  public void testDownloadStructuresIfInputFromURL()
  {
    String urlFile = "http://www.jalview.org/builds/develop/examples/3W5V.pdb";
    FileLoader fileLoader = new FileLoader();
    fileLoader.LoadFileWaitTillLoaded(urlFile, DataSourceType.URL,
            FileFormat.PDB);
    Assert.assertNotNull(fileLoader.file);
    // The FileLoader's file is expected to be same as the original URL.
    Assert.assertEquals(urlFile, fileLoader.file);
    // Data source type expected to be DataSourceType.URL
    Assert.assertEquals(DataSourceType.URL, fileLoader.protocol);
  }
}
