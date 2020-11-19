package jalview.schemes;

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class BuriedColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.buried A = 1.7, R
   * = 0.1, N = 0.4, D = 0.4... min = 0.05 max = 4.6
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (0, 1-c, c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new BuriedColourScheme();

    float min = 0.05f;
    float max = 4.6f;
    float a = (1.7f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null), new Color(0, 1 - a, a));

    float d = (0.4f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null), new Color(0, 1 - d, d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
