package jalview.io.cache;

import java.util.LinkedHashSet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AppCacheTest
{
  private AppCache appCache;

  private static final String TEST_CACHE_KEY = "CACHE.UNIT_TEST";

  private static final String TEST_FAKE_CACHE_KEY = "CACHE.UNIT_TEST_FAKE";

  @BeforeClass(alwaysRun = true)
  public void setUpCache()
  {
    appCache = AppCache.getInstance();
  }

  public void generateTestCacheItems()
  {
    LinkedHashSet<String> testCacheItems = new LinkedHashSet<String>();
    for (int x = 0; x < 10; x++)
    {
      testCacheItems.add("TestCache" + x);
    }
    appCache.putCache(TEST_CACHE_KEY, testCacheItems);
    appCache.persistCache(TEST_CACHE_KEY);
  }

  @Test(groups = { "Functional" })
  public void appCacheTest()
  {
    LinkedHashSet<String> cacheItems = appCache
            .getAllCachedItemsFor(TEST_FAKE_CACHE_KEY);
    Assert.assertEquals(cacheItems.size(), 0);
    generateTestCacheItems();
    cacheItems = appCache.getAllCachedItemsFor(TEST_CACHE_KEY);
    Assert.assertEquals(cacheItems.size(), 10);
    appCache.deleteCacheItems(TEST_CACHE_KEY);
    cacheItems = appCache.getAllCachedItemsFor(TEST_CACHE_KEY);
    Assert.assertEquals(cacheItems.size(), 0);
  }

  @Test(groups = { "Functional" })
  public void appCacheLimitTest()
  {
    String limit = appCache.getCacheLimit(TEST_CACHE_KEY);
    Assert.assertEquals(limit, "99");
    limit = String.valueOf(appCache.updateCacheLimit(TEST_CACHE_KEY, 20));
    Assert.assertEquals(limit, "20");
    limit = appCache.getCacheLimit(TEST_CACHE_KEY);
    Assert.assertEquals(limit, "20");
    appCache.updateCacheLimit(TEST_CACHE_KEY, 99);
  }


}
