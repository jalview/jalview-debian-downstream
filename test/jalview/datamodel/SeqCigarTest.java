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
package jalview.datamodel;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import jalview.gui.JvOptionPane;
import jalview.util.Comparison;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for SeqCigar
 */
public class SeqCigarTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testFindPosition()
  {
    SequenceI oseq = new Sequence("MySeq", "ASD---ASD---ASD", 37, 45);
    oseq.createDatasetSequence();
    SeqCigar cs = new SeqCigar(oseq);
    assertEquals(oseq.getSequenceAsString(), cs.getSequenceString('-'));
    for (int c = 0, cLen = oseq.getLength(); c < cLen; c++)
    {
      int os_p = oseq.findPosition(c);
      int cigar_p = cs.findPosition(c);
      if (Comparison.isGap(oseq.getCharAt(c)))
      {
        assertEquals("Expected gap at position " + os_p + " column " + c,
                -1, cigar_p);
      }
      else
      {
        assertEquals("Positions don't match for at column " + c, os_p,
                cigar_p);
      }
    }
  }

  @Test(groups= {"Functional"})
  public void testReconstructSeq()
  {
    String o_seq = "asdfktryasdtqwrtsaslldddptyipqqwaslchvhttt";
    SequenceI s = new Sequence("MySeq", o_seq, 39, 80);
    String orig_gapped = "----asdf------ktryas---dtqwrtsasll----dddptyipqqwa----slchvhttt";
    // name of sequence in a particular alignment should be recovered
    SequenceI s_gapped = new Sequence("MySeqAlign", orig_gapped, 39, 80);
    s_gapped.setDatasetSequence(s);
    SeqCigar cg_sgapped = new SeqCigar(s_gapped);
    assertTrue(testSeqRecovery(cg_sgapped,s_gapped,true));
    SequenceI subseq_gapped = s_gapped.getSubSequence(44, 60);
    SeqCigar subseq_cg_range=new SeqCigar(s_gapped,44,59);
    assertTrue(testSeqRecovery(subseq_cg_range, subseq_gapped, true),"SeqCigar created on range of sequence failed");

    // test another way of reconstructing a sequence from seqCigar
    SequenceI[] sqs=SeqCigar.createAlignmentSequences(new SeqCigar[] {subseq_cg_range}, '-', new HiddenColumns(), null);
    assertTrue(testSeqRecovery(subseq_cg_range, sqs[0], true),"createAlignmentSequences didn't reconstruct same sequence as for SeqCigar created on range of sequence failed (used by AlignmentView for selections)");

    subseq_gapped.setName("SubSeqMySeqAlign"); // name of sequence in a particular alignment should be recovered
    SeqCigar subseq_cg = new SeqCigar(subseq_gapped);
    assertTrue(testSeqRecovery(subseq_cg,subseq_gapped,true));
  }
  /*
   * refactored 'as is' from main method
   * 
   * TODO: split into separate tests
   */
  @Test(groups = { "Functional" })
  public void testSomething() throws Exception
  {
    String o_seq = "asdfktryasdtqwrtsaslldddptyipqqwaslchvhttt";
    Sequence s = new Sequence("MySeq", o_seq, 39, 80);
    String orig_gapped = "----asdf------ktryas---dtqwrtsasll----dddptyipqqwa----slchvhttt";
    Sequence s_gapped = new Sequence("MySeq", orig_gapped, 39, 80);
    String ex_cs_gapped = "4I4M6I6M3I11M4I12M4I9M";
    s_gapped.setDatasetSequence(s);
    String sub_gapped_s = "------ktryas---dtqwrtsasll----dddptyipqqwa----slchvh";
    Sequence s_subsequence_gapped = new Sequence("MySeq", sub_gapped_s, 43,
            77);
    s_subsequence_gapped.setDatasetSequence(s);

    SeqCigar c_null = new SeqCigar(s);
    String cs_null = c_null.getCigarstring();
    assertEquals("Failed to recover ungapped sequence cigar operations",
            "42M", cs_null);
    testCigar_string(s_gapped, ex_cs_gapped);
    SeqCigar gen_sgapped = SeqCigar.parseCigar(s, ex_cs_gapped);
    assertEquals("Failed parseCigar", ex_cs_gapped,
            gen_sgapped.getCigarstring());

    assertTrue(testSeqRecovery(gen_sgapped, s_gapped,true));

    /*
     * Test dataset resolution
     */
    SeqCigar sub_gapped = new SeqCigar(s_subsequence_gapped);
    assertTrue(testSeqRecovery(sub_gapped, s_subsequence_gapped,true));

    /*
     * Test width functions
     */
    assertEquals("Failed getWidth", sub_gapped_s.length(),
            sub_gapped.getWidth());

    sub_gapped.getFullWidth();
    assertFalse("hasDeletedRegions is incorrect",
            sub_gapped.hasDeletedRegions());

    // Test start-end region SeqCigar
    SeqCigar sub_se_gp = new SeqCigar(s_subsequence_gapped, 8, 48);
    assertEquals(
            "SeqCigar(seq, start, end) not properly clipped alignsequence",
            41, sub_se_gp.getWidth());

    /*
     * TODO: can we add assertions to the sysouts that follow?
     */
    System.out.println("\nOriginal sequence align:\n" + sub_gapped_s
            + "\nReconstructed window from 8 to 48\n" + "XXXXXXXX"
            + sub_se_gp.getSequenceString('-') + "..." + "\nCigar String:"
            + sub_se_gp.getCigarstring() + "\n");
    SequenceI ssgp = sub_se_gp.getSeq('-');
    System.out.println("\t " + ssgp.getSequenceAsString());
    for (int r = 0; r < 10; r++)
    {
      sub_se_gp = new SeqCigar(s_subsequence_gapped, 8, 48);
      int sl = sub_se_gp.getWidth();
      int st = sl - 1 - r;
      for (int rs = 0; rs < 10; rs++)
      {
        int e = st + rs;
        sub_se_gp.deleteRange(st, e);
        String ssgapedseq = sub_se_gp.getSeq('-').getSequenceAsString();
        System.out.println(st + "," + e + "\t:" + ssgapedseq);
        st -= 3;
      }
    }

    SeqCigar[] set = new SeqCigar[] { new SeqCigar(s),
        new SeqCigar(s_subsequence_gapped, 8, 48), new SeqCigar(s_gapped) };
    Alignment al = new Alignment(set);
    for (int i = 0; i < al.getHeight(); i++)
    {
      System.out.println("" + al.getSequenceAt(i).getName() + "\t"
              + al.getSequenceAt(i).getStart() + "\t"
              + al.getSequenceAt(i).getEnd() + "\t"
              + al.getSequenceAt(i).getSequenceAsString());
    }

    System.out.println("Gapped.");
    set = new SeqCigar[] { new SeqCigar(s),
        new SeqCigar(s_subsequence_gapped, 8, 48), new SeqCigar(s_gapped) };
    set[0].deleteRange(20, 25);
    al = new Alignment(set);
    for (int i = 0; i < al.getHeight(); i++)
    {
      System.out.println("" + al.getSequenceAt(i).getName() + "\t"
              + al.getSequenceAt(i).getStart() + "\t"
              + al.getSequenceAt(i).getEnd() + "\t"
              + al.getSequenceAt(i).getSequenceAsString());
    }

    // if (!ssgapedseq.equals("ryas---dtqqwa----slchvh"))
    // System.err.println("Subseqgaped\n------ktryas---dtqwrtsasll----dddptyipqqwa----slchvhryas---dtqwrtsasll--qwa----slchvh\n"+ssgapedseq+"\n"+sub_se_gp.getCigarstring());
  }

  /**
   * non rigorous testing
   * 
   * @param seq
   *          Sequence
   * @param ex_cs_gapped
   *          String
   * @return String
   */

  protected void testCigar_string(Sequence seq, String ex_cs_gapped)
  {
    SeqCigar c_sgapped = new SeqCigar(seq);
    String cs_gapped = c_sgapped.getCigarstring();
    assertEquals("Failed getCigarstring", ex_cs_gapped, cs_gapped);
  }

  protected boolean testSeqRecovery(SeqCigar gen_sgapped, SequenceI s_gapped,boolean startEndCheck)
  {
    // this is non-rigorous - start and end recovery is not tested.
    SequenceI gen_sgapped_s = gen_sgapped.getSeq('-');
    // assertEquals("Couldn't reconstruct sequence", s_gapped.getSequence(),
    // gen_sgapped_s);
    if (!gen_sgapped_s.getSequenceAsString().equals(
            s_gapped.getSequenceAsString()))
    {
      // TODO: investigate errors reported here, to allow full conversion to
      // passing JUnit assertion form
      System.err.println("Couldn't reconstruct sequence.\n"
              + gen_sgapped_s.getSequenceAsString() + "\n"
              + s_gapped.getSequenceAsString());
      return false;
    }
    if (startEndCheck)
    {
      assertEquals("Start not conserved in reconstructed sequence",s_gapped.getStart(),gen_sgapped_s.getStart());
      assertEquals("End not conserved in reconstructed sequence",s_gapped.getEnd(),gen_sgapped_s.getEnd());
    }
    return true;
  }

}
