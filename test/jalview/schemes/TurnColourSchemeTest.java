package jalview.schemes;

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class TurnColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.turn A = 0.66, R
   * = 0.95, N = 1.56, D = 1.46... min = 0.47 max = 1.56
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (c, 1-c, 1-c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new TurnColourScheme();

    float min = 0.47f;
    float max = 1.56f;
    float a = (0.66f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null),
            new Color(a, 1 - a, 1 - a));

    float d = (1.46f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null),
            new Color(d, 1 - d, 1 - d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
