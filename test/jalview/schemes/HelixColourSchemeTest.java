package jalview.schemes;

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class HelixColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.helix A = 1.42, R
   * = 0.98, N = 0.67, D = 1.01... min = 0.57 max = 1.51
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (c, 1-c, c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new HelixColourScheme();

    float min = 0.57f;
    float max = 1.51f;
    float a = (1.42f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null), new Color(a, 1 - a, a));

    float d = (1.01f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null), new Color(d, 1 - d, d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
