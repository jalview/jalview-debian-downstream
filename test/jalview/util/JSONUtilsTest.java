package jalview.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.testng.annotations.Test;

public class JSONUtilsTest
{
  @Test(groups = "Functional")
  public void testArrayToList() throws JSONException
  {
    assertNull(JSONUtils.arrayToList(null));

    JSONArray ja = new JSONArray();
    assertNull(JSONUtils.arrayToList(null));

    ja.add("hello");
    assertEquals(JSONUtils.arrayToList(ja), "hello");

    ja.add("world");
    assertEquals(JSONUtils.arrayToList(ja), "hello,world");
  }
}
