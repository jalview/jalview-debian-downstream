package jalview.io.cache;

import java.util.LinkedHashSet;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JvCacheableInputBoxTest
{

  private AppCache appCache;

  private static final String TEST_CACHE_KEY = "CACHE.UNIT_TEST";

  private JvCacheableInputBox<String> cacheBox = new JvCacheableInputBox<>(
          TEST_CACHE_KEY, 20);

  @BeforeClass(alwaysRun = true)
  private void setUpCache()
  {
    appCache = AppCache.getInstance();
  }

  @Test(groups = { "Functional" })
  public void getUserInputTest()
  {
    String userInput = cacheBox.getUserInput();
    Assert.assertEquals("", userInput);

    String testInput = "TestInput";
    cacheBox.addItem(testInput);
    cacheBox.setSelectedItem(testInput);

    try
    {
      // This 1ms delay is essential to prevent the
      // assertion below from executing before
      // swing thread finishes updating the combo-box
      Thread.sleep(100);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    userInput = cacheBox.getUserInput();
    Assert.assertEquals(testInput, userInput);
  }

  @Test(groups = { "Functional" })
  public void updateCacheTest()
  {
    String testInput = "TestInput";
    cacheBox.addItem(testInput);
    cacheBox.setSelectedItem(testInput);
    cacheBox.updateCache();
    try
    {
      // This delay is to let
      // cacheBox.updateCache() finish updating the cache
      Thread.sleep(200);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    LinkedHashSet<String> foundCache = appCache
            .getAllCachedItemsFor(TEST_CACHE_KEY);
    Assert.assertTrue(foundCache.contains(testInput));
  }
}
