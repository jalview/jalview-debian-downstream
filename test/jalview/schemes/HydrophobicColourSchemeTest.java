package jalview.schemes;

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class HydrophobicColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.hyd A = 1.8, R =
   * -4.5, N = -3.5, D = -3.5... min = -3.9 max = 4.5
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (c, 0, 1-c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new HydrophobicColourScheme();

    float min = -3.9f;
    float max = 4.5f;
    float a = (1.8f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null),
 new Color(a, 0, 1 - a));

    float d = (-3.5f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null),
 new Color(d, 0, 1 - d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
