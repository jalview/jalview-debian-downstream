package jalview.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import jalview.analysis.scoremodels.ScoreModels;
import jalview.api.analysis.ScoreModelI;
import jalview.bin.Cache;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CalculationChooserTest
{
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    // read-only Jalview properties
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("BLOSUM62_PCA_FOR_NUCLEOTIDE",
            Boolean.FALSE.toString());
  }

  @Test(groups = "Functional")
  public void testGetApplicableScoreModels()
  {
    ScoreModels models = ScoreModels.getInstance();
    ScoreModelI blosum62 = models.getBlosum62();
    ScoreModelI pam250 = models.getPam250();
    ScoreModelI dna = models.getDefaultModel(false);

    /*
     * peptide models for PCA
     */
    List<ScoreModelI> filtered = CalculationChooser
            .getApplicableScoreModels(false, true);
    assertEquals(filtered.size(), 4);
    assertSame(filtered.get(0), blosum62);
    assertSame(filtered.get(1), pam250);
    assertEquals(filtered.get(2).getName(), "PID");
    assertEquals(filtered.get(3).getName(), "Sequence Feature Similarity");

    /*
     * peptide models for Tree are the same
     */
    filtered = CalculationChooser.getApplicableScoreModels(false, false);
    assertEquals(filtered.size(), 4);
    assertSame(filtered.get(0), blosum62);
    assertSame(filtered.get(1), pam250);
    assertEquals(filtered.get(2).getName(), "PID");
    assertEquals(filtered.get(3).getName(), "Sequence Feature Similarity");

    /*
     * nucleotide models for PCA
     */
    filtered = CalculationChooser.getApplicableScoreModels(true, true);
    assertEquals(filtered.size(), 3);
    assertSame(filtered.get(0), dna);
    assertEquals(filtered.get(1).getName(), "PID");
    assertEquals(filtered.get(2).getName(), "Sequence Feature Similarity");

    /*
     * nucleotide models for Tree are the same
     */
    filtered = CalculationChooser.getApplicableScoreModels(true, false);
    assertEquals(filtered.size(), 3);
    assertSame(filtered.get(0), dna);
    assertEquals(filtered.get(1).getName(), "PID");
    assertEquals(filtered.get(2).getName(), "Sequence Feature Similarity");

    /*
     * enable inclusion of BLOSUM62 for nucleotide PCA (JAL-2962)
     */
    Cache.applicationProperties.setProperty("BLOSUM62_PCA_FOR_NUCLEOTIDE",
            Boolean.TRUE.toString());

    /*
     * nucleotide models for Tree are unchanged
     */
    filtered = CalculationChooser.getApplicableScoreModels(true, false);
    assertEquals(filtered.size(), 3);
    assertSame(filtered.get(0), dna);
    assertEquals(filtered.get(1).getName(), "PID");
    assertEquals(filtered.get(2).getName(), "Sequence Feature Similarity");

    /*
     * nucleotide models for PCA add BLOSUM62 as last option
     */
    filtered = CalculationChooser.getApplicableScoreModels(true, true);
    assertEquals(filtered.size(), 4);
    assertSame(filtered.get(0), dna);
    assertEquals(filtered.get(1).getName(), "PID");
    assertEquals(filtered.get(2).getName(), "Sequence Feature Similarity");
    assertSame(filtered.get(3), blosum62);
  }
}
