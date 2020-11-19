/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.gui;

import static org.junit.Assert.assertNotEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import jalview.api.FeatureColourI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.project.Jalview2xmlTests;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.FeatureColour;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.JalviewColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.util.MessageManager;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SplitFrameTest
{
  AlignFrame dnaAf, proteinAf;

  SplitFrame testSplitFrame;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    setUpJvOptionPane();
    /*
     * use read-only test properties file
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Jalview.main(new String[] { "-nonews" });
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    Desktop.instance.closeAll_actionPerformed(null);
  }

  /**
   * configure (read-only) properties for test to ensure Consensus is computed
   * for colour Above PID testing
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.TRUE.toString());
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/testdata/MN908947.jvp", DataSourceType.FILE);

    /*
     * wait for Consensus thread to complete
     */
    synchronized (this)
    {
      while (af.getViewport().getConsensusSeq() == null)
      {
        try
        {
          wait(50);
        } catch (InterruptedException e)
        {
        }
      }
    }
    testSplitFrame = (SplitFrame) af.getSplitViewContainer();
    proteinAf=af.getViewport().getAlignment().isNucleotide() ? testSplitFrame.getComplementAlignFrame(af) : af;
    dnaAf=testSplitFrame.getComplementAlignFrame(proteinAf);
  }

  public static void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups= {"Functional"})
  public void testAlignAsSplitFrame()
  {

    /*
     * If alignment was requested from one half of a SplitFrame, show in a
     * SplitFrame with the other pane similarly aligned.
     */
    AlignFrame requestedBy = proteinAf;
    AlignmentI wholeProteinAl = proteinAf.getViewport().getAlignment();
    SequenceI[] sel = wholeProteinAl.getSequencesArray();
    // Select 3 sequences, from columns 3-7 inclusive
    SequenceGroup selRegion = new SequenceGroup(
            Arrays.asList(sel[0], sel[1], sel[3]));
    selRegion.setStartRes(3);
    selRegion.setEndRes(7);
    proteinAf.getViewport().setSelectionGroup(selRegion);
    proteinAf.getViewport().sendSelection();
    assertNotNull(dnaAf.getViewport().getSelectionGroup());
    AlignmentView inputView = proteinAf.gatherSequencesForAlignment();
    assertEquals(inputView.getSequences().length, 3);
    assertEquals(inputView.getWidth(), 5);
    assertNotNull(inputView.getComplementView());

    Object alAndHidden[] = inputView.getAlignmentAndHiddenColumns(
            proteinAf.getViewport().getGapCharacter());
    AlignmentI result = new Alignment((SequenceI[]) alAndHidden[0]);
    result.setHiddenColumns((HiddenColumns) alAndHidden[1]);
    // check we are referring to the expected alignment
    assertEquals(
            requestedBy.getSplitViewContainer().getComplement(requestedBy),
            dnaAf.getCurrentView().getAlignment());
    // and that datasets are consistent (if not, there's a problem elsewhere in
    // splitframe construction
    AlignmentI complementDs = requestedBy.getSplitViewContainer()
            .getComplement(requestedBy).getDataset();
    assertTrue(complementDs == dnaAf
            .getViewport().getAlignment().getDataset());
    assertTrue(complementDs == proteinAf.getViewport().getAlignment()
            .getDataset());

    char gc = requestedBy.getSplitViewContainer().getComplement(requestedBy)
            .getGapCharacter();

    AlignmentI complement = inputView.getComplementView()
            .getVisibleAlignment(gc);
    String complementTitle = requestedBy.getSplitViewContainer()
            .getComplementTitle(requestedBy);
    // becomes null if the alignment window was closed before the alignment
    // job finished.
    AlignmentI copyComplement = new Alignment(complement);
    // todo should this be done by copy constructor?
    copyComplement.setGapCharacter(complement.getGapCharacter());
    // share the same dataset (and the mappings it holds)
    copyComplement.setDataset(complementDs);
    copyComplement.alignAs(result);
    // check shape is as expected
    assertEquals(copyComplement.getWidth(), result.getWidth() * 3);
    assertEquals(copyComplement.getHeight(), result.getHeight());
    // specific bug with this set - see same CDS for all distinct products
    assertTrue(
            !copyComplement.getSequenceAt(0).getSequenceAsString().equals(
                    copyComplement.getSequenceAt(1).getSequenceAsString()),
            "Didn't reconstruct CDS correctly");
    // now get the result again, do some edits and reconstruct again
    alAndHidden = inputView.getAlignmentAndHiddenColumns(
            proteinAf.getViewport().getGapCharacter());
    AlignmentI newresult = new Alignment((SequenceI[]) alAndHidden[0]);
    newresult.setHiddenColumns((HiddenColumns) alAndHidden[1]);
    newresult.setDataset(complementDs);
    newresult.getSequenceAt(0).insertCharAt(3, 3, '-');
    newresult.getSequenceAt(1).insertCharAt(0, 3, '-');
    newresult.padGaps();
    AlignmentI newcomplement = inputView.getComplementView()
            .getVisibleAlignment('-');
    newcomplement.alignAs(newresult);
    assertEquals(newcomplement.getWidth(), newresult.getWidth() * 3);
    assertEquals(newcomplement.getHeight(), newresult.getHeight());
    // if reconstruction worked, the first sequence should not equal the first
    // sequence in the original CDS 'alignAs'
    for (int sq = 0; sq < 3; sq++)
    {
      // check same CDS in same position
      assertTrue(newcomplement.getSequenceAt(sq)
              .getDatasetSequence() == newcomplement.getSequenceAt(sq)
                      .getDatasetSequence());
      // verify that sequence strings are different
      assertTrue(!newcomplement.getSequenceAt(sq).getSequenceAsString()
              .equals(copyComplement.getSequenceAt(sq)
                      .getSequenceAsString()));
    }
    // JAL-3748 bug manifests as duplicated CDS sequence content, so need to
    // also check each CDS is distinct.
    assertTrue(
            !newcomplement.getSequenceAt(0).getSequenceAsString().equals(
                    newcomplement.getSequenceAt(1).getSequenceAsString()),
            "Didn't reconstruct CDS correctly");

  }
}
