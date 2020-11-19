package jalview.util.matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Locale;

import org.testng.annotations.Test;

public class ConditionTest
{
  @Test(groups = "Functional")
  public void testToString()
  {
    Locale.setDefault(Locale.UK);
    assertEquals(Condition.Contains.toString(), "Contains");
    assertEquals(Condition.NotContains.toString(), "Does not contain");
    assertEquals(Condition.Matches.toString(), "Matches");
    assertEquals(Condition.NotMatches.toString(), "Does not match");
    assertEquals(Condition.Present.toString(), "Is present");
    assertEquals(Condition.NotPresent.toString(), "Is not present");
    assertEquals(Condition.LT.toString(), "<");
    assertEquals(Condition.LE.toString(), "<=");
    assertEquals(Condition.GT.toString(), ">");
    assertEquals(Condition.GE.toString(), ">=");
    assertEquals(Condition.EQ.toString(), "=");
    assertEquals(Condition.NE.toString(), "not =");

    /*
     * repeat call to get coverage of value caching
     */
    assertEquals(Condition.NE.toString(), "not =");
  }

  @Test(groups = "Functional")
  public void testGetStableName()
  {
    assertEquals(Condition.Contains.getStableName(), "Contains");
    assertEquals(Condition.NotContains.getStableName(), "NotContains");
    assertEquals(Condition.Matches.getStableName(), "Matches");
    assertEquals(Condition.NotMatches.getStableName(), "NotMatches");
    assertEquals(Condition.Present.getStableName(), "Present");
    assertEquals(Condition.NotPresent.getStableName(), "NotPresent");
    assertEquals(Condition.LT.getStableName(), "LT");
    assertEquals(Condition.LE.getStableName(), "LE");
    assertEquals(Condition.GT.getStableName(), "GT");
    assertEquals(Condition.GE.getStableName(), "GE");
    assertEquals(Condition.EQ.getStableName(), "EQ");
    assertEquals(Condition.NE.getStableName(), "NE");
  }

  @Test(groups = "Functional")
  public void testFromString()
  {
    assertEquals(Condition.fromString("Contains"), Condition.Contains);
    // not case sensitive
    assertEquals(Condition.fromString("contains"), Condition.Contains);
    assertEquals(Condition.fromString("CONTAINS"), Condition.Contains);
    assertEquals(Condition.fromString("NotContains"),
            Condition.NotContains);
    assertEquals(Condition.fromString("Matches"), Condition.Matches);
    assertEquals(Condition.fromString("NotMatches"), Condition.NotMatches);
    assertEquals(Condition.fromString("Present"), Condition.Present);
    assertEquals(Condition.fromString("NotPresent"), Condition.NotPresent);
    assertEquals(Condition.fromString("LT"), Condition.LT);
    assertEquals(Condition.fromString("LE"), Condition.LE);
    assertEquals(Condition.fromString("GT"), Condition.GT);
    assertEquals(Condition.fromString("GE"), Condition.GE);
    assertEquals(Condition.fromString("EQ"), Condition.EQ);
    assertEquals(Condition.fromString("NE"), Condition.NE);

    assertNull(Condition.fromString("Equals"));
    assertNull(Condition.fromString(""));
    assertNull(Condition.fromString(null));
  }
}
