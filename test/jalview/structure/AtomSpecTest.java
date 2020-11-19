package jalview.structure;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

public class AtomSpecTest
{
  @Test
  public void testFromChimeraAtomSpec()
  {
    AtomSpec as = AtomSpec.fromChimeraAtomspec("#1:12.B");
    assertEquals(as.getModelNumber(), 1);
    assertEquals(as.getPdbResNum(), 12);
    assertEquals(as.getChain(), "B");
    assertNull(as.getPdbFile());

    // no model - default to zero
    as = AtomSpec.fromChimeraAtomspec(":13.C");
    assertEquals(as.getModelNumber(), 0);
    assertEquals(as.getPdbResNum(), 13);
    assertEquals(as.getChain(), "C");
    assertNull(as.getPdbFile());

    // model.submodel
    as = AtomSpec.fromChimeraAtomspec("#3.2:15");
    assertEquals(as.getModelNumber(), 3);
    assertEquals(as.getPdbResNum(), 15);
    assertEquals(as.getChain(), "");
    assertNull(as.getPdbFile());

    String spec = "3:12.B";
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = "#3:12-14.B";
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = "";
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = null;
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (NullPointerException e)
    {
      // ok
    }
  }
}
