package jalview.ext.rbvi.chimera;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class AtomSpecModelTest
{
  @Test(groups = "Functional")
  public void testGetAtomSpec()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(model.getAtomSpec(), "");
    model.addRange(1, 2, 4, "A");
    assertEquals(model.getAtomSpec(), "#1:2-4.A");
    model.addRange(1, 8, 8, "A");
    assertEquals(model.getAtomSpec(), "#1:2-4.A,8.A");
    model.addRange(1, 5, 7, "B");
    assertEquals(model.getAtomSpec(), "#1:2-4.A,8.A,5-7.B");
    model.addRange(1, 3, 5, "A");
    assertEquals(model.getAtomSpec(), "#1:2-5.A,8.A,5-7.B");
    model.addRange(0, 1, 4, "B");
    assertEquals(model.getAtomSpec(), "#0:1-4.B|#1:2-5.A,8.A,5-7.B");
    model.addRange(0, 5, 9, "C");
    assertEquals(model.getAtomSpec(), "#0:1-4.B,5-9.C|#1:2-5.A,8.A,5-7.B");
    model.addRange(1, 8, 10, "B");
    assertEquals(model.getAtomSpec(), "#0:1-4.B,5-9.C|#1:2-5.A,8.A,5-10.B");
    model.addRange(1, 8, 9, "B");
    assertEquals(model.getAtomSpec(), "#0:1-4.B,5-9.C|#1:2-5.A,8.A,5-10.B");
    model.addRange(0, 3, 10, "C"); // subsumes 5-9
    assertEquals(model.getAtomSpec(), "#0:1-4.B,3-10.C|#1:2-5.A,8.A,5-10.B");
    model.addRange(5, 25, 35, " "); // empty chain code - e.g. from homology
                                    // modelling
    assertEquals(model.getAtomSpec(),
            "#0:1-4.B,3-10.C|#1:2-5.A,8.A,5-10.B|#5:25-35.");

  }

}
