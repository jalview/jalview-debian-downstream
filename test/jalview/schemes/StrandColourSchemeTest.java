package jalview.schemes;

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class StrandColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.strand A = 0.83,
   * R = 0.93, N = 0.89, D = 0.54... min = 0.37 max = 1.7
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (c, c, 1-c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new StrandColourScheme();

    float min = 0.37f;
    float max = 1.7f;
    float a = (0.83f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null),
 new Color(a, a, 1 - a));

    float d = (0.54f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null),
 new Color(d, d, 1 - d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
