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
package jalview.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ComparisonTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testIsGap()
  {
    assertTrue(Comparison.isGap('-'));
    assertTrue(Comparison.isGap('.'));
    assertTrue(Comparison.isGap(' '));
    assertFalse(Comparison.isGap('X'));
    assertFalse(Comparison.isGap('x'));
    assertFalse(Comparison.isGap('*'));
    assertFalse(Comparison.isGap('G'));
  }

  /**
   * Test for isNucleotide is that sequences in a dataset are more than 85%
   * AGCTU. Test is not case-sensitive and ignores gaps.
   */
  @Test(groups = { "Functional" })
  public void testIsNucleotide_sequences()
  {
    SequenceI seq = new Sequence("eightypercent", "agctuAGCPV");
    assertFalse(Comparison.isNucleotide(new SequenceI[] { seq }));
    assertFalse(Comparison.isNucleotide(new SequenceI[][] { new SequenceI[]
    { seq } }));

    seq = new Sequence("eightyfivepercent", "agctuAGCPVagctuAGCUV");
    assertFalse(Comparison.isNucleotide(new SequenceI[] { seq }));

    seq = new Sequence("nineypercent", "agctuAGCgVagctuAGCUV");
    assertTrue(Comparison.isNucleotide(new SequenceI[] { seq }));

    seq = new Sequence("eightyfivepercentgapped",
            "--agc--tuA--GCPV-a---gct-uA-GC---UV");
    assertFalse(Comparison.isNucleotide(new SequenceI[] { seq }));

    seq = new Sequence("nineypercentgapped",
            "ag--ct-u-A---GC---g----Vag--c---tuAGCUV");
    assertTrue(Comparison.isNucleotide(new SequenceI[] { seq }));

    seq = new Sequence("allgap", "---------");
    assertFalse(Comparison.isNucleotide(new SequenceI[] { seq }));

    seq = new Sequence("DNA", "ACTugGCCAG");
    SequenceI seq2 = new Sequence("Protein", "FLIMVSPTYW");
    /*
     * 90% DNA but one protein sequence - expect false
     */
    assertFalse(Comparison.isNucleotide(new SequenceI[] { seq, seq, seq,
        seq, seq, seq, seq, seq, seq, seq2 }));
    assertFalse(Comparison.isNucleotide(new SequenceI[][] {
        new SequenceI[] { seq }, new SequenceI[] { seq, seq, seq },
        new SequenceI[] { seq, seq, seq, seq, seq, seq2 } }));
    /*
     * 80% DNA but one protein sequence - Expect false
     */
    assertFalse(Comparison.isNucleotide(new SequenceI[] { seq, seq, seq,
        seq, seq, seq, seq, seq, seq2, seq2 }));
    assertFalse(Comparison.isNucleotide(new SequenceI[][] { new SequenceI[]
    { seq }, new SequenceI[] { seq, seq, seq },
        new SequenceI[] { seq, seq, seq, seq, seq2, seq2, null } }));

    seq = new Sequence("ProteinThatLooksLikeDNA", "WYATGCCTGAgtcgt");
    // 12/14 = 85.7%
    assertTrue(Comparison.isNucleotide(new SequenceI[] { seq }));

    assertFalse(Comparison.isNucleotide((SequenceI[]) null));
    assertFalse(Comparison.isNucleotide((SequenceI[][]) null));
  }

  /**
   * Test the percentage identity calculation for two sequences
   */
  @Test(groups = { "Functional" })
  public void testPID_includingGaps()
  {
    String seq1 = "ABCDEFG"; // extra length here is ignored
    String seq2 = "abcdef";
    assertEquals("identical", 100f, Comparison.PID(seq1, seq2), 0.001f);

    // comparison range defaults to length of first sequence
    seq2 = "abcdefghijklmnopqrstuvwxyz";
    assertEquals("identical", 100f, Comparison.PID(seq1, seq2), 0.001f);

    // 5 identical, 2 gap-gap, 2 gap-residue, 1 mismatch
    seq1 = "a--b-cdefh";
    seq2 = "a---bcdefg";
    int length = seq1.length();

    // match gap-residue, match gap-gap: 9/10 identical
    // TODO should gap-gap be included in a PID score? JAL-791
    assertEquals(90f, Comparison.PID(seq1, seq2, 0, length, true, false),
            0.001f);
    // overloaded version of the method signature above:
    assertEquals(90f, Comparison.PID(seq1, seq2), 0.001f);

    // don't match gap-residue, match gap-gap: 7/10 identical
    // TODO should gap-gap be included in a PID score?
    assertEquals(70f, Comparison.PID(seq1, seq2, 0, length, false, false),
            0.001f);
  }

  @Test(groups = { "Functional" })
  public void testIsNucleotide()
  {
    assertTrue(Comparison.isNucleotide('a'));
    assertTrue(Comparison.isNucleotide('A'));
    assertTrue(Comparison.isNucleotide('c'));
    assertTrue(Comparison.isNucleotide('C'));
    assertTrue(Comparison.isNucleotide('g'));
    assertTrue(Comparison.isNucleotide('G'));
    assertTrue(Comparison.isNucleotide('t'));
    assertTrue(Comparison.isNucleotide('T'));
    assertTrue(Comparison.isNucleotide('u'));
    assertTrue(Comparison.isNucleotide('U'));
    assertFalse(Comparison.isNucleotide('-'));
    assertFalse(Comparison.isNucleotide('P'));
  }

  /**
   * Test the percentage identity calculation for two sequences
   */
  @Test(groups = { "Functional" })
  public void testPID_ungappedOnly()
  {
    // 5 identical, 2 gap-gap, 2 gap-residue, 1 mismatch
    // the extra length of seq1 is ignored
    String seq1 = "a--b-cdefhr";
    String seq2 = "a---bcdefg";
    int length = seq1.length();

    /*
     * As currently coded, 'ungappedOnly' ignores gap-residue but counts
     * gap-gap. Is this a bug - should gap-gap also be ignored, giving a PID of
     * 5/6?
     * 
     * Note also there is no variant of the calculation that penalises
     * gap-residue i.e. counts it as a mismatch. This would give a score of 5/8
     * (if we ignore gap-gap) or 5/10 (if we count gap-gap as a match).
     */
    // match gap-residue, match gap-gap: 7/8 identical
    assertEquals(87.5f, Comparison.PID(seq1, seq2, 0, length, true, true),
            0.001f);

    // don't match gap-residue with 'ungapped only' - same as above
    assertEquals(87.5f, Comparison.PID(seq1, seq2, 0, length, false, true),
            0.001f);
  }

  @Test(groups = { "Functional" })
  public void testIsNucleotideSequence()
  {
    assertFalse(Comparison.isNucleotideSequence(null, true));
    assertTrue(Comparison.isNucleotideSequence("", true));
    assertTrue(Comparison.isNucleotideSequence("aAgGcCtTuU", true));
    assertTrue(Comparison.isNucleotideSequence("aAgGcCtTuU", false));
    assertFalse(Comparison.isNucleotideSequence("xAgGcCtTuU", false));
    assertFalse(Comparison.isNucleotideSequence("aAgGcCtTuUx", false));
    assertTrue(Comparison.isNucleotideSequence("a A-g.GcCtTuU", true));
    assertFalse(Comparison.isNucleotideSequence("a A-g.GcCtTuU", false));
  }

  @Test(groups = { "Functional" })
  public void testIsSameResidue()
  {
    assertTrue(Comparison.isSameResidue('a', 'a', false));
    assertTrue(Comparison.isSameResidue('a', 'a', true));
    assertTrue(Comparison.isSameResidue('A', 'a', false));
    assertTrue(Comparison.isSameResidue('a', 'A', false));

    assertFalse(Comparison.isSameResidue('a', 'A', true));
    assertFalse(Comparison.isSameResidue('A', 'a', true));
  }
}
