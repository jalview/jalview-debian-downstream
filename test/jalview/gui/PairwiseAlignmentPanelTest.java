package jalview.gui;

import static org.testng.Assert.assertEquals;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceGroup;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import javax.swing.JTextArea;

import junit.extensions.PA;

import org.testng.annotations.Test;

public class PairwiseAlignmentPanelTest
{
  @Test(groups = "Functional")
  public void testConstructor_withSelectionGroup()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewport viewport = af.getViewport();
    AlignmentI al = viewport.getAlignment();

    /*
     * select columns 29-36 of sequences 4 and 5 for alignment
     * Q93XJ9_SOLTU/23-29 L-KAISNV
     * FER1_PEA/26-32     V-TTTKAF
     */
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(al.getSequenceAt(3), false);
    sg.addSequence(al.getSequenceAt(4), false);
    sg.setStartRes(28);
    sg.setEndRes(35);
    viewport.setSelectionGroup(sg);

    PairwiseAlignPanel testee = new PairwiseAlignPanel(viewport);

    String text = ((JTextArea) PA.getValue(testee, "textarea")).getText();
    String expected = "Score = 80.0\n" + "Length of alignment = 4\n"
            + "Sequence     FER1_PEA/29-32 (Sequence length = 7)\n"
            + "Sequence Q93XJ9_SOLTU/23-26 (Sequence length = 7)\n\n"
            + "    FER1_PEA/29-32 TKAF\n" + "                    ||.\n"
            + "Q93XJ9_SOLTU/23-26 LKAI\n\n" + "Percentage ID = 50.00\n\n";
    assertEquals(text, expected);
  }

  /**
   * This test aligns the same sequences as testConstructor_withSelectionGroup
   * but as a complete alignment (no selection). Note that in fact the user is
   * currently required to make a selection in order to calculate pairwise
   * alignments, so this case does not arise.
   */
  @Test(groups = "Functional")
  public void testConstructor_noSelectionGroup()
  {
    String seqs = ">Q93XJ9_SOLTU/23-29\nL-KAISNV\n>FER1_PEA/26-32\nV-TTTKAF\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqs,
            DataSourceType.PASTE);
    AlignViewport viewport = af.getViewport();

    PairwiseAlignPanel testee = new PairwiseAlignPanel(viewport);

    String text = ((JTextArea) PA.getValue(testee, "textarea")).getText();
    String expected = "Score = 80.0\n" + "Length of alignment = 4\n"
            + "Sequence     FER1_PEA/29-32 (Sequence length = 7)\n"
            + "Sequence Q93XJ9_SOLTU/23-26 (Sequence length = 7)\n\n"
            + "    FER1_PEA/29-32 TKAF\n" + "                    ||.\n"
            + "Q93XJ9_SOLTU/23-26 LKAI\n\n" + "Percentage ID = 50.00\n\n";
    assertEquals(text, expected);
  }
}
