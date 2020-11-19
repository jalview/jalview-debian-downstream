package jalview.ext.android;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class SparseDoubleArrayTest
{

  @Test
  public void testConstructor()
  {
    double[] d = new double[] { 0d, 0d, 1.2d, 0d, 0d, 3.4d };
    SparseDoubleArray s = new SparseDoubleArray(d);
    for (int i = 0; i < d.length; i++)
    {
      assertEquals(s.get(i), d[i], "At [" + i + "]");
    }
  }

  @Test
  public void testAdd()
  {
    double[] d = new double[] { 0d, 0d, 1.2d, 0d, 0d, 3.4d };
    SparseDoubleArray s = new SparseDoubleArray(d);
    // add to zero (absent)
    s.add(0, 3.2d);
    assertEquals(s.get(0), 3.2d);
    // add to non-zero
    s.add(0, 2.5d);
    assertEquals(s.get(0), 5.7d);
    // add negative value
    s.add(2, -5.3d);
    assertEquals(s.get(2), -4.1d);
    // add to unset value
    s.add(12, 9.8d);
    assertEquals(s.get(12), 9.8d);
  }

  @Test
  public void testDivide()
  {
    double delta = 1.0e-10;
    double[] d = new double[] { 0d, 2.4d, 1.2d, 0d, -4.8d, -3.6d };
    SparseDoubleArray s = new SparseDoubleArray(d);
    assertEquals(s.divide(0, 1d), 0d); // no such entry
    assertEquals(s.divide(2, 0d), 0d); // zero divisor
    assertEquals(s.divide(1, 2d), 1.2d, delta); // + / +
    assertEquals(s.divide(2, -2d), -0.6d, delta); // + / -
    assertEquals(s.divide(4, 3d), -1.6d, delta); // - / +
    assertEquals(s.divide(5, -3d), 1.2d, delta); // - / -
  }
}
