package jalview.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Sequence;

import org.testng.annotations.Test;

public class AnnotationLabelsTest
{
  @Test(groups = "Functional")
  public void testGetTooltip()
  {
    assertNull(AnnotationLabels.getTooltip(null));

    /*
     * simple description only
     */
    AlignmentAnnotation ann = new AlignmentAnnotation("thelabel", "thedesc",
            null);
    String expected = "<html>thedesc</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), expected);

    /*
     * description needing html encoding
     * (no idea why '<' is encoded but '>' is not)
     */
    ann.description = "TCoffee scores < 56 and > 28";
    expected = "<html>TCoffee scores &lt; 56 and > 28</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), expected);

    /*
     * description already html formatted
     */
    ann.description = "<html>hello world</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), ann.description);

    /*
     * simple description and score
     */
    ann.description = "hello world";
    ann.setScore(2.34d);
    expected = "<html>hello world<br/> Score: 2.34</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), expected);

    /*
     * html description and score
     */
    ann.description = "<html>hello world</html>";
    ann.setScore(2.34d);
    expected = "<html>hello world<br/> Score: 2.34</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), expected);

    /*
     * score, no description
     */
    ann.description = " ";
    assertEquals(AnnotationLabels.getTooltip(ann),
            "<html> Score: 2.34</html>");
    ann.description = null;
    assertEquals(AnnotationLabels.getTooltip(ann),
            "<html> Score: 2.34</html>");
    
    /*
     * sequenceref, simple description
     */
    ann.description = "Count < 12";
    ann.sequenceRef = new Sequence("Seq1", "MLRIQST");
    ann.hasScore = false;
    ann.score = Double.NaN;
    expected = "<html>Seq1 : Count &lt; 12</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), expected);

    /*
     * sequenceref, html description, score
     */
    ann.description = "<html>Score < 4.8</html>";
    ann.sequenceRef = new Sequence("Seq1", "MLRIQST");
    ann.setScore(-2.1D);
    expected = "<html>Seq1 : Score < 4.8<br/> Score: -2.1</html>";
    assertEquals(AnnotationLabels.getTooltip(ann), expected);

    /*
     * no score, null description
     */
    ann.description = null;
    ann.hasScore = false;
    ann.score = Double.NaN;
    assertNull(AnnotationLabels.getTooltip(ann));

    /*
     * no score, empty description, sequenceRef
     */
    ann.description = "";
    assertEquals(AnnotationLabels.getTooltip(ann), "<html>Seq1 :</html>");

    /*
     * no score, empty description, no sequenceRef
     */
    ann.sequenceRef = null;
    assertNull(AnnotationLabels.getTooltip(ann));
  }

  @Test(groups = "Functional")
  public void testGetStatusMessage()
  {
    assertNull(AnnotationLabels.getStatusMessage(null, null));

    /*
     * simple label
     */
    AlignmentAnnotation aa = new AlignmentAnnotation("IUPredWS Short",
            "Protein disorder", null);
    assertEquals(AnnotationLabels.getStatusMessage(aa, null),
            "IUPredWS Short");

    /*
     * with sequence ref
     */
    aa.setSequenceRef(new Sequence("FER_CAPAA", "MIGRKQL"));
    assertEquals(AnnotationLabels.getStatusMessage(aa, null),
            "FER_CAPAA : IUPredWS Short");

    /*
     * with graph group (degenerate, one annotation only)
     */
    aa.graphGroup = 1;
    AlignmentAnnotation aa2 = new AlignmentAnnotation("IUPredWS Long",
            "Protein disorder", null);
    assertEquals(
            AnnotationLabels.getStatusMessage(aa, new AlignmentAnnotation[]
            { aa, aa2 }), "FER_CAPAA : IUPredWS Short");

    /*
     * graph group with two members; note labels are appended in
     * reverse order (matching rendering order on screen)
     */
    aa2.graphGroup = 1;
    assertEquals(
            AnnotationLabels.getStatusMessage(aa, new AlignmentAnnotation[]
            { aa, aa2 }), "FER_CAPAA : IUPredWS Long, IUPredWS Short");

    /*
     * graph group with no sequence ref
     */
    aa.sequenceRef = null;
    assertEquals(
            AnnotationLabels.getStatusMessage(aa, new AlignmentAnnotation[]
            { aa, aa2 }), "IUPredWS Long, IUPredWS Short");
  }
}
