package jalview.ext.ensembl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

public class SpeciesTest
{
  @Test
  public void testGetModelOrganisms()
  {
    Set<Species> models = Species.getModelOrganisms();
    assertTrue(models.contains(Species.human));
    assertFalse(models.contains(Species.horse));
    for (Species s : Species.values())
    {
      if (s.isModelOrganism())
      {
        assertTrue(models.contains(s));
      }
      else
      {
        assertFalse(models.contains(s));
      }
    }
  }
}
