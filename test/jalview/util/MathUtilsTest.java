package jalview.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class MathUtilsTest
{
  @Test(groups = "Functional")
  public void testGcd()
  {
    assertEquals(MathUtils.gcd(0, 0), 0);
    assertEquals(MathUtils.gcd(0, 1), 1);
    assertEquals(MathUtils.gcd(1, 0), 1);
    assertEquals(MathUtils.gcd(1, 1), 1);
    assertEquals(MathUtils.gcd(1, -1), 1);
    assertEquals(MathUtils.gcd(-1, 1), 1);
    assertEquals(MathUtils.gcd(2, 3), 1);
    assertEquals(MathUtils.gcd(4, 2), 2);
    assertEquals(MathUtils.gcd(2, 4), 2);
    assertEquals(MathUtils.gcd(2, -4), 2);
    assertEquals(MathUtils.gcd(-2, 4), 2);
    assertEquals(MathUtils.gcd(-2, -4), 2);
    assertEquals(MathUtils.gcd(2 * 3 * 5 * 7 * 11, 3 * 7 * 13 * 17), 3 * 7);
  }
}
