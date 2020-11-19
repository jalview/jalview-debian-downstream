package jalview.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class MessageManagerTest
{
  @Test(groups = "Functional")
  public void testFormatMessage_invalid()
  {
    String msg = MessageManager.formatMessage("label.rubbish", "goodbye",
            "world");
    assertEquals(msg, "[missing key] label.rubbish 'goodbye' 'world'");
  }

  @Test(groups = "Functional")
  public void testGetString_invalid()
  {
    String msg = MessageManager.getString("label.rubbish");
    assertEquals(msg, "[missing key] label.rubbish");
  }

  @Test(groups = "Functional")
  public void testGetStringOrReturn()
  {
    String msg = MessageManager.getStringOrReturn("label.rubbish",
            "rubbishdefault");
    assertEquals(msg, "rubbishdefault");
  }
}
