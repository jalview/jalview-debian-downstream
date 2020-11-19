package jalview.schemes;

import static org.testng.Assert.assertEquals;

import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import java.awt.Color;

import org.testng.annotations.Test;

public class PIDColourSchemeTest
{
  static final Color white = Color.white;

  static final Color over40 = new Color(204, 204, 255);

  static final Color over60 = new Color(153, 153, 255);

  static final Color over80 = new Color(100, 100, 255);

  /**
   * Test findColour for cases:
   * <ul>
   * <li>gap: white</li>
   * <li>no match to consensus: white</li>
   * <li>match consensus with pid > 80%: 100,100,255</li>
   * <li>match consensus with pid > 60%: 153, 153, 255</li>
   * <li>match consensus with pid > 40%: 204, 204, 255</li>
   * <li>match consensus with pid <= 40%: white</li>
   * <li>joint consensus matching</li>
   * <li>case insensitive matching</li>
   * <ul>
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ColourSchemeI scheme = new PIDColourScheme();

    /*
     * doesn't use column or sequence
     * we assume consensus residue is computed as upper case
     */
    assertEquals(scheme.findColour('A', 0, null, "A", 0f), white);
    assertEquals(scheme.findColour('A', 0, null, "A", 40f), white);
    assertEquals(scheme.findColour('A', 0, null, "A", 40.1f), over40);
    assertEquals(scheme.findColour('A', 0, null, "A", 60f), over40);
    assertEquals(scheme.findColour('A', 0, null, "A", 60.1f), over60);
    assertEquals(scheme.findColour('A', 0, null, "A", 80f), over60);
    assertEquals(scheme.findColour('A', 0, null, "A", 80.1f), over80);
    assertEquals(scheme.findColour('A', 0, null, "A", 100f), over80);
    assertEquals(scheme.findColour('A', 0, null, "KFV", 100f), white);

    assertEquals(scheme.findColour('a', 0, null, "A", 80f), over60);
    assertEquals(scheme.findColour('A', 0, null, "AC", 80f), over60);
    assertEquals(scheme.findColour('A', 0, null, "KCA", 80f), over60);
  }

  /**
   * Test that changing the 'ignore gaps in consensus' in the viewport (an
   * option on the annotation label popup menu) results in a change to the
   * colouring
   */
  @Test(groups = "Functional")
  public void testFindColour_ignoreGaps()
  {
    /*
     * AAAAA
     * AAAAA
     * -CCCC
     * FFFFF
     * 
     * first column consensus is A
     * first column PID is 50%, or 67% ignoring gaps
     */
    String seqs = ">seq1\nAAAAA\n>seq2\nAAAAA\n>seq3\n-CCCC\n>seq4\nFFFFF\n";

    /*
     * load data and wait for consensus to be computed
     */
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqs,
            DataSourceType.PASTE);
    AlignViewport viewport = af.getViewport();
    viewport.setIgnoreGapsConsensus(false, af.alignPanel);
    while (viewport.getConsensusSeq() == null)
    {
      synchronized (this)
      {
        try
        {
          wait(50);
        } catch (InterruptedException e)
        {
        }
      }
    }
    af.changeColour_actionPerformed(JalviewColourScheme.PID.toString());

    SequenceI seq = viewport.getAlignment().getSequenceAt(0);

    /*
     * including gaps, A should be coloured for 50% consensus
     */
    Color c = viewport
            .getResidueShading().findColour('A', 0, seq);
    assertEquals(c, over40);

    /*
     * now choose to ignore gaps; colour should be for 67%
     */
    viewport.setIgnoreGapsConsensus(true, af.alignPanel);
    c = viewport
            .getResidueShading().findColour('A', 0, seq);
    assertEquals(c, over60);
  }
}
