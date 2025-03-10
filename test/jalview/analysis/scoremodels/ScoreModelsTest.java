package jalview.analysis.scoremodels;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jalview.api.analysis.PairwiseScoreModelI;
import jalview.api.analysis.ScoreModelI;

import java.util.Iterator;

import org.testng.annotations.Test;

public class ScoreModelsTest
{
  /**
   * Verify that the singleton constructor successfully loads Jalview's built-in
   * score models
   */
  @Test(groups = "Functional")
  public void testConstructor()
  {
    Iterator<ScoreModelI> models = ScoreModels.getInstance().getModels()
            .iterator();
    assertTrue(models.hasNext());

    /*
     * models are served in order of addition
     */
    ScoreModelI sm = models.next();
    assertTrue(sm instanceof SimilarityScoreModel);
    assertTrue(sm instanceof PairwiseScoreModelI);
    assertFalse(sm instanceof DistanceScoreModel);
    assertEquals(sm.getName(), "BLOSUM62");
    assertEquals(((PairwiseScoreModelI) sm).getPairwiseScore('I', 'R'), -3f);

    sm = models.next();
    assertTrue(sm instanceof SimilarityScoreModel);
    assertTrue(sm instanceof PairwiseScoreModelI);
    assertFalse(sm instanceof DistanceScoreModel);
    assertEquals(sm.getName(), "PAM250");
    assertEquals(((PairwiseScoreModelI) sm).getPairwiseScore('R', 'C'), -4f);

    sm = models.next();
    assertTrue(sm instanceof SimilarityScoreModel);
    assertTrue(sm instanceof PairwiseScoreModelI);
    assertFalse(sm instanceof DistanceScoreModel);
    assertEquals(sm.getName(), "DNA");
    assertEquals(((PairwiseScoreModelI) sm).getPairwiseScore('c', 'x'), 1f);

    sm = models.next();
    assertTrue(sm instanceof SimilarityScoreModel);
    assertTrue(sm instanceof PairwiseScoreModelI);
    assertFalse(sm instanceof DistanceScoreModel);
    assertEquals(sm.getName(), "PID");
    assertEquals(((PairwiseScoreModelI) sm).getPairwiseScore('R', 'C'), 0f);
    assertEquals(((PairwiseScoreModelI) sm).getPairwiseScore('R', 'r'), 1f);

    sm = models.next();
    assertFalse(sm instanceof SimilarityScoreModel);
    assertFalse(sm instanceof PairwiseScoreModelI);
    assertTrue(sm instanceof DistanceScoreModel);
    assertEquals(sm.getName(), "Sequence Feature Similarity");
  }

  /**
   * 'Test' that prints out score matrices in tab-delimited format. This test is
   * intentionally not assigned to any group so would not be run as part of a
   * suite. It makes no assertions and is just provided as a utility method for
   * printing out matrices. Relocated here from ScoreMatrixPrinter.
   */
  @Test(groups = "none")
  public void printAllMatrices_tabDelimited()
  {
    printAllMatrices(false);
  }

  /**
   * 'Test' that prints out score matrices in html format. This test is
   * intentionally not assigned to any group so would not be run as part of a
   * suite. It makes no assertions and is just provided as a utility method for
   * printing out matrices. Relocated here from ScoreMatrixPrinter.
   */
  @Test(groups = "none")
  public void printAllMatrices_asHtml()
  {
    printAllMatrices(true);
  }

  /**
   * Print all registered ScoreMatrix as plain or html tables
   * 
   * @param asHtml
   */
  protected void printAllMatrices(boolean asHtml)
  {
    for (ScoreModelI sm : ScoreModels.getInstance().getModels())
    {
      if (sm instanceof ScoreMatrix)
      {
        System.out.println(((ScoreMatrix) sm).outputMatrix(asHtml));
      }
    }
  }
}
