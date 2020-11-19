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
package jalview.analysis;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.io.PrintStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the alignment -> Mapping routines
 * 
 * @author jimp
 * 
 */
public class TestAlignSeq
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  SequenceI s1, s2, s3;

  /**
   * @throws java.lang.Exception
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    s1 = new Sequence("Seq1", "ASDFAQQQRRRSSS");
    s1.setStart(3);
    s1.setEnd(18);
    s2 = new Sequence("Seq2", "ASDFA");
    s2.setStart(5);
    s2.setEnd(9);
    s3 = new Sequence("Seq3", "SDFAQQQSSS");

  }

  @Test(groups = { "Functional" })
  /**
   * simple test that mapping from alignment corresponds identical positions.
   */
  public void testGetMappingForS1()
  {
    AlignSeq as = AlignSeq.doGlobalNWAlignment(s1, s2, AlignSeq.PEP);
    System.out.println("s1: " + as.getAStr1());
    System.out.println("s2: " + as.getAStr2());

    // aligned results match
    assertEquals("ASDFA", as.getAStr1());
    assertEquals(as.getAStr1(), as.getAStr2());

    Mapping s1tos2 = as.getMappingFromS1(false);
    System.out.println(s1tos2.getMap().toString());
    for (int i = s2.getStart(); i < s2.getEnd(); i++)
    {
      System.out.println("Position in s2: " + i
              + " maps to position in s1: " + s1tos2.getPosition(i));
      // TODO fails: getCharAt doesn't allow for the start position??
      // assertEquals(String.valueOf(s2.getCharAt(i)),
      // String.valueOf(s1.getCharAt(s1tos2.getPosition(i))));
    }
  }

  @Test(groups = { "Functional" })
  public void testExtractGaps()
  {
    assertNull(AlignSeq.extractGaps(null, null));
    assertNull(AlignSeq.extractGaps(". -", null));
    assertNull(AlignSeq.extractGaps(null, "AB-C"));

    assertEquals("ABCD", AlignSeq.extractGaps(" .-", ". -A-B.C D."));
  }

  @Test(groups = { "Functional" })
  public void testPrintAlignment()
  {
    AlignSeq as = AlignSeq.doGlobalNWAlignment(s1, s3, AlignSeq.PEP);
    final StringBuilder baos = new StringBuilder();
    PrintStream ps = new PrintStream(System.out)
    {
      @Override
      public void print(String x)
      {
        baos.append(x);
      }

      @Override
      public void println()
      {
        baos.append("\n");
      }
    };

    as.printAlignment(ps);
    String expected = "Score = 320.0\nLength of alignment = 10\nSequence Seq1/4-13 (Sequence length = 14)\nSequence Seq3/1-10 (Sequence length = 10)\n\n"
            + "Seq1/4-13 SDFAQQQRRR\n"
            + "          |||||||   \n"
            + "Seq3/1-10 SDFAQQQSSS\n\n" + "Percentage ID = 70.00\n\n";
    assertEquals(expected, baos.toString());
  }
}
