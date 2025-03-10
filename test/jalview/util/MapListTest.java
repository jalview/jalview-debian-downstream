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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.gui.JvOptionPane;

public class MapListTest
{
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Cache.initLogger();
  }

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testSomething()
  {
    MapList ml = new MapList(new int[] { 1, 5, 10, 15, 25, 20 },
            new int[]
            { 51, 1 }, 1, 3);
    MapList ml1 = new MapList(new int[] { 1, 3, 17, 4 },
            new int[]
            { 51, 1 }, 1, 3);
    MapList ml2 = new MapList(new int[] { 1, 60 }, new int[] { 1, 20 }, 3,
            1);
    // test internal consistency
    int to[] = new int[51];
    testMap(ml, 1, 60);
    MapList mldna = new MapList(new int[] { 2, 2, 6, 8, 12, 16 },
            new int[]
            { 1, 3 }, 3, 1);
    int[] frm = mldna.locateInFrom(1, 1);
    testLocateFrom(mldna, 1, 1, new int[] { 2, 2, 6, 7 });
    testMap(mldna, 1, 3);
    /*
     * for (int from=1; from<=51; from++) { int[] too=ml.shiftTo(from); int[]
     * toofrom=ml.shiftFrom(too[0]);
     * System.out.println("ShiftFrom("+from+")=="+too[0]+" %
     * "+too[1]+"\t+-+\tShiftTo("+too[0]+")=="+toofrom[0]+" % "+toofrom[1]); }
     */
  }

  private static void testLocateFrom(MapList mldna, int i, int j, int[] ks)
  {
    int[] frm = mldna.locateInFrom(i, j);
    assertEquals("Failed test locate from " + i + " to " + j,
            Arrays.toString(frm), Arrays.toString(ks));
  }

  /**
   * test routine. not incremental.
   * 
   * @param ml
   * @param fromS
   * @param fromE
   */
  private void testMap(MapList ml, int fromS, int fromE)
  {
    // todo convert to JUnit style tests
    for (int from = 1; from <= 25; from++)
    {
      int[] too = ml.shiftFrom(from);
      System.out.print("ShiftFrom(" + from + ")==");
      if (too == null)
      {
        System.out.print("NaN\n");
      }
      else
      {
        System.out.print(too[0] + " % " + too[1] + " (" + too[2] + ")");
        System.out.print("\t+--+\t");
        int[] toofrom = ml.shiftTo(too[0]);
        if (toofrom != null)
        {
          if (toofrom[0] != from)
          {
            System.err.println("Mapping not reflexive:" + from + " "
                    + too[0] + "->" + toofrom[0]);
          }
          System.out.println("ShiftTo(" + too[0] + ")==" + toofrom[0]
                  + " % " + toofrom[1] + " (" + toofrom[2] + ")");
        }
        else
        {
          System.out.println("ShiftTo(" + too[0] + ")=="
                  + "NaN! - not Bijective Mapping!");
        }
      }
    }
    int mmap[][] = ml.makeFromMap();
    System.out.println("FromMap : (" + mmap[0][0] + " " + mmap[0][1] + " "
            + mmap[0][2] + " " + mmap[0][3] + " ");
    for (int i = 1; i <= mmap[1].length; i++)
    {
      if (mmap[1][i - 1] == -1)
      {
        System.out.print(i + "=XXX");

      }
      else
      {
        System.out.print(i + "=" + (mmap[0][2] + mmap[1][i - 1]));
      }
      if (i % 20 == 0)
      {
        System.out.print("\n");
      }
      else
      {
        System.out.print(",");
      }
    }
    // test range function
    System.out.print("\nTest locateInFrom\n");
    {
      int f = mmap[0][2], t = mmap[0][3];
      while (f <= t)
      {
        System.out.println("Range " + f + " to " + t);
        int rng[] = ml.locateInFrom(f, t);
        if (rng != null)
        {
          for (int i = 0; i < rng.length; i++)
          {
            System.out.print(rng[i] + ((i % 2 == 0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        System.out.print("\nReversed\n");
        rng = ml.locateInFrom(t, f);
        if (rng != null)
        {
          for (int i = 0; i < rng.length; i++)
          {
            System.out.print(rng[i] + ((i % 2 == 0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        System.out.print("\n");
        f++;
        t--;
      }
    }
    System.out.print("\n");
    mmap = ml.makeToMap();
    System.out.println("ToMap : (" + mmap[0][0] + " " + mmap[0][1] + " "
            + mmap[0][2] + " " + mmap[0][3] + " ");
    for (int i = 1; i <= mmap[1].length; i++)
    {
      if (mmap[1][i - 1] == -1)
      {
        System.out.print(i + "=XXX");

      }
      else
      {
        System.out.print(i + "=" + (mmap[0][2] + mmap[1][i - 1]));
      }
      if (i % 20 == 0)
      {
        System.out.print("\n");
      }
      else
      {
        System.out.print(",");
      }
    }
    System.out.print("\n");
    // test range function
    System.out.print("\nTest locateInTo\n");
    {
      int f = mmap[0][2], t = mmap[0][3];
      while (f <= t)
      {
        System.out.println("Range " + f + " to " + t);
        int rng[] = ml.locateInTo(f, t);
        if (rng != null)
        {
          for (int i = 0; i < rng.length; i++)
          {
            System.out.print(rng[i] + ((i % 2 == 0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        System.out.print("\nReversed\n");
        rng = ml.locateInTo(t, f);
        if (rng != null)
        {
          for (int i = 0; i < rng.length; i++)
          {
            System.out.print(rng[i] + ((i % 2 == 0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        f++;
        t--;
        System.out.print("\n");
      }
    }
  }

  /**
   * Tests for method that locates ranges in the 'from' map for given range in
   * the 'to' map.
   */
  @Test(groups = { "Functional" })
  public void testLocateInFrom_noIntrons()
  {
    /*
     * Simple mapping with no introns
     */
    int[] codons = new int[] { 1, 12 };
    int[] protein = new int[] { 1, 4 };
    MapList ml = new MapList(codons, protein, 3, 1);
    assertEquals("[1, 3]", Arrays.toString(ml.locateInFrom(1, 1)));
    assertEquals("[4, 6]", Arrays.toString(ml.locateInFrom(2, 2)));
    assertEquals("[7, 9]", Arrays.toString(ml.locateInFrom(3, 3)));
    assertEquals("[10, 12]", Arrays.toString(ml.locateInFrom(4, 4)));
    assertEquals("[1, 6]", Arrays.toString(ml.locateInFrom(1, 2)));
    assertEquals("[1, 9]", Arrays.toString(ml.locateInFrom(1, 3)));
    assertEquals("[1, 12]", Arrays.toString(ml.locateInFrom(1, 4)));
    assertEquals("[4, 9]", Arrays.toString(ml.locateInFrom(2, 3)));
    assertEquals("[4, 12]", Arrays.toString(ml.locateInFrom(2, 4)));
    assertEquals("[7, 12]", Arrays.toString(ml.locateInFrom(3, 4)));
    assertEquals("[10, 12]", Arrays.toString(ml.locateInFrom(4, 4)));

    assertNull(ml.locateInFrom(0, 0));
    assertNull(ml.locateInFrom(1, 5));
    assertNull(ml.locateInFrom(-1, 1));
  }

  /**
   * Tests for method that locates ranges in the 'from' map for given range in
   * the 'to' map.
   */
  @Test(groups = { "Functional" })
  public void testLocateInFrom_withIntrons()
  {
    /*
     * Exons at positions [2, 3, 5] [6, 7, 9] [10, 12, 14] [16, 17, 18] i.e.
     * 2-3, 5-7, 9-10, 12-12, 14-14, 16-18
     */
    int[] codons = { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18 };
    int[] protein = { 1, 4 };
    MapList ml = new MapList(codons, protein, 3, 1);
    assertEquals("[2, 3, 5, 5]", Arrays.toString(ml.locateInFrom(1, 1)));
    assertEquals("[6, 7, 9, 9]", Arrays.toString(ml.locateInFrom(2, 2)));
    assertEquals("[10, 10, 12, 12, 14, 14]",
            Arrays.toString(ml.locateInFrom(3, 3)));
    assertEquals("[16, 18]", Arrays.toString(ml.locateInFrom(4, 4)));
  }

  /**
   * Tests for method that locates the overlap of the ranges in the 'from' map
   * for given range in the 'to' map
   */
  @Test(groups = { "Functional" })
  public void testGetOverlapsInFrom_withIntrons()
  {
    /*
     * Exons at positions [2, 3, 5] [6, 7, 9] [10, 12, 14] [16, 17, 18] i.e.
     * 2-3, 5-7, 9-10, 12-12, 14-14, 16-18
     */
    int[] codons = { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18 };
    int[] protein = { 11, 14 };
    MapList ml = new MapList(codons, protein, 3, 1);

    assertEquals("[2, 3, 5, 5]",
            Arrays.toString(ml.getOverlapsInFrom(11, 11)));
    assertEquals("[2, 3, 5, 7, 9, 9]",
            Arrays.toString(ml.getOverlapsInFrom(11, 12)));
    // out of range 5' :
    assertEquals("[2, 3, 5, 7, 9, 9]",
            Arrays.toString(ml.getOverlapsInFrom(8, 12)));
    // out of range 3' :
    assertEquals("[10, 10, 12, 12, 14, 14, 16, 18]",
            Arrays.toString(ml.getOverlapsInFrom(13, 16)));
    // out of range both :
    assertEquals("[2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18]",
            Arrays.toString(ml.getOverlapsInFrom(1, 16)));
    // no overlap:
    assertNull(ml.getOverlapsInFrom(20, 25));
  }

  /**
   * Tests for method that locates the overlap of the ranges in the 'to' map
   * for given range in the 'from' map
   */
  @Test(groups = { "Functional" })
  public void testGetOverlapsInTo_withIntrons()
  {
    /*
     * Exons at positions [2, 3, 5] [6, 7, 9] [10, 12, 14] [17, 18, 19] i.e.
     * 2-3, 5-7, 9-10, 12-12, 14-14, 17-19
     */
    int[] codons = { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 17, 19 };
    /*
     * Mapped proteins at positions 1, 3, 4, 6 in the sequence
     */
    int[] protein = { 1, 1, 3, 4, 6, 6 };
    MapList ml = new MapList(codons, protein, 3, 1);

    /*
     * Can't map from an unmapped position
     */
    assertNull(ml.getOverlapsInTo(1, 1));
    assertNull(ml.getOverlapsInTo(4, 4));
    assertNull(ml.getOverlapsInTo(15, 16));
    
    /*
     * nor from a range that includes no mapped position (exon)
     */
    assertNull(ml.getOverlapsInTo(15, 16));

    // end of codon 1 maps to first peptide
    assertEquals("[1, 1]", Arrays.toString(ml.getOverlapsInTo(2, 2)));
    // end of codon 1 and start of codon 2 maps to first 2 peptides
    assertEquals("[1, 1, 3, 3]", Arrays.toString(ml.getOverlapsInTo(3, 7)));

    // range overlaps 5' end of dna:
    assertEquals("[1, 1, 3, 3]", Arrays.toString(ml.getOverlapsInTo(1, 6)));
   assertEquals("[1, 1, 3, 3]", Arrays.toString(ml.getOverlapsInTo(1, 8)));

    // range overlaps 3' end of dna:
    assertEquals("[6, 6]", Arrays.toString(ml.getOverlapsInTo(17, 24)));
    assertEquals("[6, 6]", Arrays.toString(ml.getOverlapsInTo(16, 24)));
    
    // dna positions 8, 11 are intron but include end of exon 2 and start of exon 3
    assertEquals("[3, 4]", Arrays.toString(ml.getOverlapsInTo(8, 11)));
  }

  /**
   * Tests for method that locates ranges in the 'to' map for given range in the
   * 'from' map.
   */
  @Test(groups = { "Functional" })
  public void testLocateInTo_noIntrons()
  {
    /*
     * Simple mapping with no introns
     */
    int[] codons = new int[] { 1, 12 };
    int[] protein = new int[] { 1, 4 };
    MapList ml = new MapList(codons, protein, 3, 1);
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(1, 3)));
    assertEquals("[2, 2]", Arrays.toString(ml.locateInTo(4, 6)));
    assertEquals("[3, 3]", Arrays.toString(ml.locateInTo(7, 9)));
    assertEquals("[4, 4]", Arrays.toString(ml.locateInTo(10, 12)));
    assertEquals("[1, 2]", Arrays.toString(ml.locateInTo(1, 6)));
    assertEquals("[1, 3]", Arrays.toString(ml.locateInTo(1, 9)));
    assertEquals("[1, 4]", Arrays.toString(ml.locateInTo(1, 12)));
    assertEquals("[2, 2]", Arrays.toString(ml.locateInTo(4, 6)));
    assertEquals("[2, 4]", Arrays.toString(ml.locateInTo(4, 12)));

    /*
     * A part codon is treated as if a whole one.
     */
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(1, 1)));
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(1, 2)));
    assertEquals("[1, 2]", Arrays.toString(ml.locateInTo(1, 4)));
    assertEquals("[1, 3]", Arrays.toString(ml.locateInTo(2, 8)));
    assertEquals("[1, 4]", Arrays.toString(ml.locateInTo(3, 11)));
    assertEquals("[2, 4]", Arrays.toString(ml.locateInTo(5, 11)));

    assertNull(ml.locateInTo(0, 0));
    assertNull(ml.locateInTo(1, 13));
    assertNull(ml.locateInTo(-1, 1));
  }

  /**
   * Tests for method that locates ranges in the 'to' map for given range in the
   * 'from' map.
   */
  @Test(groups = { "Functional" })
  public void testLocateInTo_withIntrons()
  {
    /*
     * Exons at positions [2, 3, 5] [6, 7, 9] [10, 12, 14] [16, 17, 18] i.e.
     * 2-3, 5-7, 9-10, 12-12, 14-14, 16-18
     */
    int[] codons = { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18 };
    /*
     * Mapped proteins at positions 1, 3, 4, 6 in the sequence
     */
    int[] protein = { 1, 1, 3, 4, 6, 6 };
    MapList ml = new MapList(codons, protein, 3, 1);

    /*
     * Can't map from an unmapped position
     */
    assertNull(ml.locateInTo(1, 2));
    assertNull(ml.locateInTo(2, 4));
    assertNull(ml.locateInTo(4, 4));

    /*
     * Valid range or subrange of codon1 maps to protein1.
     */
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(2, 2)));
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(3, 3)));
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(3, 5)));
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(2, 3)));
    assertEquals("[1, 1]", Arrays.toString(ml.locateInTo(2, 5)));

    // codon position 6 starts the next protein:
    assertEquals("[1, 1, 3, 3]", Arrays.toString(ml.locateInTo(3, 6)));

    // codon positions 7 to 17 (part) cover proteins 2/3/4 at positions 3/4/6
    assertEquals("[3, 4, 6, 6]", Arrays.toString(ml.locateInTo(7, 17)));

  }

  /**
   * Test equals method.
   */
  @Test(groups = { "Functional" })
  public void testEquals()
  {
    int[] codons = new int[] { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18 };
    int[] protein = new int[] { 1, 4 };
    MapList ml = new MapList(codons, protein, 3, 1);
    MapList ml1 = new MapList(codons, protein, 3, 1); // same values
    MapList ml2 = new MapList(codons, protein, 2, 1); // fromRatio differs
    MapList ml3 = new MapList(codons, protein, 3, 2); // toRatio differs
    codons[2] = 4;
    MapList ml6 = new MapList(codons, protein, 3, 1); // fromShifts differ
    protein[1] = 3;
    MapList ml7 = new MapList(codons, protein, 3, 1); // toShifts differ

    assertTrue(ml.equals(ml));
    assertEquals(ml.hashCode(), ml.hashCode());
    assertTrue(ml.equals(ml1));
    assertEquals(ml.hashCode(), ml1.hashCode());
    assertTrue(ml1.equals(ml));

    assertFalse(ml.equals(null));
    assertFalse(ml.equals("hello"));
    assertFalse(ml.equals(ml2));
    assertFalse(ml.equals(ml3));
    assertFalse(ml.equals(ml6));
    assertFalse(ml.equals(ml7));
    assertFalse(ml6.equals(ml7));

    try
    {
      MapList ml4 = new MapList(codons, null, 3, 1); // toShifts null
      assertFalse(ml.equals(ml4));
    } catch (NullPointerException e)
    {
      // actually thrown by constructor before equals can be called
    }
    try
    {
      MapList ml5 = new MapList(null, protein, 3, 1); // fromShifts null
      assertFalse(ml.equals(ml5));
    } catch (NullPointerException e)
    {
      // actually thrown by constructor before equals can be called
    }
  }

  /**
   * Test for the method that flattens a list of ranges into a single array.
   */
  @Test(groups = { "Functional" })
  public void testGetRanges()
  {
    List<int[]> ranges = new ArrayList<>();
    ranges.add(new int[] { 2, 3 });
    ranges.add(new int[] { 5, 6 });
    assertEquals("[2, 3, 5, 6]",
            Arrays.toString(MapList.getRanges(ranges)));
  }

  /**
   * Check state after construction
   */
  @Test(groups = { "Functional" })
  public void testConstructor()
  {
    int[] codons = { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18 };
    int[] protein = { 1, 1, 3, 4, 6, 6 };
    MapList ml = new MapList(codons, protein, 3, 1);
    assertEquals(3, ml.getFromRatio());
    assertEquals(2, ml.getFromLowest());
    assertEquals(18, ml.getFromHighest());
    assertEquals(1, ml.getToLowest());
    assertEquals(6, ml.getToHighest());
    assertEquals("{[2, 3], [5, 7], [9, 10], [12, 12], [14, 14], [16, 18]}",
            prettyPrint(ml.getFromRanges()));
    assertEquals("{[1, 1], [3, 4], [6, 6]}", prettyPrint(ml.getToRanges()));

    /*
     * Also copy constructor
     */
    MapList ml2 = new MapList(ml);
    assertEquals(3, ml2.getFromRatio());
    assertEquals(2, ml2.getFromLowest());
    assertEquals(18, ml2.getFromHighest());
    assertEquals(1, ml2.getToLowest());
    assertEquals(6, ml2.getToHighest());
    assertEquals("{[2, 3], [5, 7], [9, 10], [12, 12], [14, 14], [16, 18]}",
            prettyPrint(ml2.getFromRanges()));
    assertEquals("{[1, 1], [3, 4], [6, 6]}",
            prettyPrint(ml2.getToRanges()));

    /*
     * reverse direction
     */
    codons = new int[] { 9, 6 };
    protein = new int[] { 100, 91, 80, 79 };
    ml = new MapList(codons, protein, 3, 1);
    assertEquals(6, ml.getFromLowest());
    assertEquals(9, ml.getFromHighest());
    assertEquals(79, ml.getToLowest());
    assertEquals(100, ml.getToHighest());
  }

  /**
   * Test constructor used to merge consecutive ranges but now just leaves them
   * as supplied (JAL-3751)
   */
  @Test(groups = { "Functional" })
  public void testConstructor_mergeRanges()
  {
    int[] codons = { 2, 3, 3, 7, 9, 10, 12, 12, 13, 14, 16, 17 };
    int[] protein = { 1, 1, 2, 3, 6, 6 };
    MapList ml = new MapList(codons, protein, 3, 1);
    assertEquals(3, ml.getFromRatio());
    assertEquals(2, ml.getFromLowest());
    assertEquals(17, ml.getFromHighest());
    assertEquals(1, ml.getToLowest());
    assertEquals(6, ml.getToHighest());
    assertEquals("{[2, 3], [3, 7], [9, 10], [12, 12], [13, 14], [16, 17]}",
            prettyPrint(ml.getFromRanges()));
    assertEquals("{[1, 1], [2, 3], [6, 6]}", prettyPrint(ml.getToRanges()));
  }

  /**
   * Convert a List of {[i, j], [k, l], ...} to "[[i, j], [k, l], ...]"
   * 
   * @param ranges
   * @return
   */
  private String prettyPrint(List<int[]> ranges)
  {
    StringBuilder sb = new StringBuilder(ranges.size() * 5);
    boolean first = true;
    sb.append("{");
    for (int[] range : ranges)
    {
      if (!first)
      {
        sb.append(", ");
      }
      sb.append(Arrays.toString(range));
      first = false;
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Test the method that creates an inverse mapping
   */
  @Test(groups = { "Functional" })
  public void testGetInverse()
  {
    int[] codons = { 2, 3, 5, 7, 9, 10, 12, 12, 14, 14, 16, 18 };
    int[] protein = { 1, 1, 3, 4, 6, 6 };

    MapList ml = new MapList(codons, protein, 3, 1);
    MapList ml2 = ml.getInverse();
    assertEquals(ml.getFromRatio(), ml2.getToRatio());
    assertEquals(ml.getFromRatio(), ml2.getToRatio());
    assertEquals(ml.getToHighest(), ml2.getFromHighest());
    assertEquals(ml.getFromHighest(), ml2.getToHighest());
    assertEquals(prettyPrint(ml.getFromRanges()),
            prettyPrint(ml2.getToRanges()));
    assertEquals(prettyPrint(ml.getToRanges()),
            prettyPrint(ml2.getFromRanges()));
  }

  @Test(groups = { "Functional" })
  public void testToString()
  {
    MapList ml = new MapList(new int[] { 1, 5, 10, 15, 25, 20 },
            new int[]
            { 51, 1 }, 1, 3);
    String s = ml.toString();
    assertEquals("[ [1, 5] [10, 15] [25, 20] ] 1:3 to [ [51, 1] ]", s);
  }

  @Test(groups = { "Functional" })
  public void testAddMapList()
  {
    MapList ml = new MapList(new int[] { 11, 15, 20, 25, 35, 30 },
            new int[]
            { 72, 22 }, 1, 3);
    assertEquals(11, ml.getFromLowest());
    assertEquals(35, ml.getFromHighest());
    assertEquals(22, ml.getToLowest());
    assertEquals(72, ml.getToHighest());

    MapList ml2 = new MapList(new int[] { 2, 4, 37, 40 },
            new int[]
            { 12, 17, 78, 83, 88, 96 }, 1, 3);
    ml.addMapList(ml2);
    assertEquals(2, ml.getFromLowest());
    assertEquals(40, ml.getFromHighest());
    assertEquals(12, ml.getToLowest());
    assertEquals(96, ml.getToHighest());

    String s = ml.toString();
    assertEquals(
            "[ [11, 15] [20, 25] [35, 30] [2, 4] [37, 40] ] 1:3 to [ [72, 22] [12, 17] [78, 83] [88, 96] ]",
            s);
  }

  /**
   * Test that confirms adding a map twice does nothing
   */
  @Test(groups = { "Functional" })
  public void testAddMapList_sameMap()
  {
    MapList ml = new MapList(new int[] { 11, 15, 20, 25, 35, 30 },
            new int[]
            { 72, 22 }, 1, 3);
    String before = ml.toString();
    ml.addMapList(ml);
    assertEquals(before, ml.toString());
    ml.addMapList(new MapList(ml));
    assertEquals(before, ml.toString());
  }

  @Test(groups = { "Functional" })
  public void testAddMapList_contiguous()
  {
    MapList ml = new MapList(new int[] { 11, 15 }, new int[] { 72, 58 }, 1,
            3);

    MapList ml2 = new MapList(new int[] { 15, 16 }, new int[] { 58, 53 }, 1,
            3);
    ml.addMapList(ml2);
    assertEquals("[ [11, 16] ] 1:3 to [ [72, 53] ]", ml.toString());
  }

  @Test(groups = "Functional")
  public void testAddRange()
  {
    int[] range = { 1, 5 };
    List<int[]> ranges = new ArrayList<>();

    // add to empty list:
    MapList.addRange(range, ranges);
    assertEquals(1, ranges.size());
    assertSame(range, ranges.get(0));

    // extend contiguous (same position):
    MapList.addRange(new int[] { 5, 10 }, ranges);
    assertEquals(1, ranges.size());
    assertEquals(1, ranges.get(0)[0]);
    assertEquals(10, ranges.get(0)[1]);

    // extend contiguous (next position):
    MapList.addRange(new int[] { 11, 15 }, ranges);
    assertEquals(1, ranges.size());
    assertEquals(1, ranges.get(0)[0]);
    assertEquals(15, ranges.get(0)[1]);

    // change direction: range is not merged:
    MapList.addRange(new int[] { 16, 10 }, ranges);
    assertEquals(2, ranges.size());
    assertEquals(16, ranges.get(1)[0]);
    assertEquals(10, ranges.get(1)[1]);

    // extend reverse contiguous (same position):
    MapList.addRange(new int[] { 10, 8 }, ranges);
    assertEquals(2, ranges.size());
    assertEquals(16, ranges.get(1)[0]);
    assertEquals(8, ranges.get(1)[1]);

    // extend reverse contiguous (next position):
    MapList.addRange(new int[] { 7, 6 }, ranges);
    assertEquals(2, ranges.size());
    assertEquals(16, ranges.get(1)[0]);
    assertEquals(6, ranges.get(1)[1]);

    // change direction: range is not merged:
    MapList.addRange(new int[] { 6, 9 }, ranges);
    assertEquals(3, ranges.size());
    assertEquals(6, ranges.get(2)[0]);
    assertEquals(9, ranges.get(2)[1]);

    // not contiguous: not merged
    MapList.addRange(new int[] { 11, 12 }, ranges);
    assertEquals(4, ranges.size());
    assertEquals(11, ranges.get(3)[0]);
    assertEquals(12, ranges.get(3)[1]);
  }

  /**
   * Check state after construction
   */
  @Test(groups = { "Functional" })
  public void testConstructor_withLists()
  {
    /*
     * reverse direction
     */
    int[][] codons = new int[][] { { 9, 6 } };
    int[][] protein = new int[][] { { 100, 91 }, { 80, 79 } };
    MapList ml = new MapList(Arrays.asList(codons), Arrays.asList(protein),
            3, 1);
    assertEquals(6, ml.getFromLowest());
    assertEquals(9, ml.getFromHighest());
    assertEquals(79, ml.getToLowest());
    assertEquals(100, ml.getToHighest());
  }

  /**
   * Test that method that inspects for the (first) forward or reverse from
   * range. Single position ranges are ignored.
   */
  @Test(groups = { "Functional" })
  public void testIsFromForwardStrand()
  {
    // [3-9] declares forward strand
    MapList ml = new MapList(new int[] { 2, 2, 3, 9, 12, 11 },
            new int[]
            { 20, 11 }, 1, 1);
    assertTrue(ml.isFromForwardStrand());

    // [11-5] declares reverse strand ([13-14] is ignored)
    ml = new MapList(new int[] { 2, 2, 11, 5, 13, 14 },
            new int[]
            { 20, 11 }, 1, 1);
    assertFalse(ml.isFromForwardStrand());

    // all single position ranges - defaults to forward strand
    ml = new MapList(new int[] { 2, 2, 4, 4, 6, 6 }, new int[] { 3, 1 }, 1,
            1);
    assertTrue(ml.isFromForwardStrand());
  }

  /**
   * Test the method that merges contiguous ranges
   */
  @Test(groups = { "Functional" })
  public void testCoalesceRanges()
  {
    assertNull(MapList.coalesceRanges(null));
    List<int[]> ranges = new ArrayList<>();
    assertSame(ranges, MapList.coalesceRanges(ranges));
    ranges.add(new int[] { 1, 3 });
    assertSame(ranges, MapList.coalesceRanges(ranges));

    // add non-contiguous range:
    ranges.add(new int[] { 5, 6 });
    assertSame(ranges, MapList.coalesceRanges(ranges));

    // 'contiguous' range in opposite direction is not merged:
    ranges.add(new int[] { 7, 6 });
    assertSame(ranges, MapList.coalesceRanges(ranges));

    // merging in forward direction:
    ranges.clear();
    ranges.add(new int[] { 1, 3 });
    ranges.add(new int[] { 4, 5 }); // contiguous
    ranges.add(new int[] { 5, 5 }); // overlap!
    ranges.add(new int[] { 6, 7 }); // contiguous
    List<int[]> merged = MapList.coalesceRanges(ranges);
    assertEquals(2, merged.size());
    assertArrayEquals(new int[] { 1, 5 }, merged.get(0));
    assertArrayEquals(new int[] { 5, 7 }, merged.get(1));
    // verify input list is unchanged
    assertEquals(4, ranges.size());
    assertArrayEquals(new int[] { 1, 3 }, ranges.get(0));
    assertArrayEquals(new int[] { 4, 5 }, ranges.get(1));
    assertArrayEquals(new int[] { 5, 5 }, ranges.get(2));
    assertArrayEquals(new int[] { 6, 7 }, ranges.get(3));

    // merging in reverse direction:
    ranges.clear();
    ranges.add(new int[] { 7, 5 });
    ranges.add(new int[] { 5, 4 }); // overlap
    ranges.add(new int[] { 4, 4 }); // overlap
    ranges.add(new int[] { 3, 1 }); // contiguous
    merged = MapList.coalesceRanges(ranges);
    assertEquals(3, merged.size());
    assertArrayEquals(new int[] { 7, 5 }, merged.get(0));
    assertArrayEquals(new int[] { 5, 4 }, merged.get(1));
    assertArrayEquals(new int[] { 4, 1 }, merged.get(2));

    // merging with switches of direction:
    ranges.clear();
    ranges.add(new int[] { 1, 3 });
    ranges.add(new int[] { 4, 5 }); // contiguous
    ranges.add(new int[] { 5, 5 }); // overlap
    ranges.add(new int[] { 6, 6 }); // contiguous
    ranges.add(new int[] { 12, 10 });
    ranges.add(new int[] { 9, 8 }); // contiguous
    ranges.add(new int[] { 8, 8 }); // overlap
    ranges.add(new int[] { 7, 7 }); // contiguous
    merged = MapList.coalesceRanges(ranges);
    assertEquals(4, merged.size());
    assertArrayEquals(new int[] { 1, 5 }, merged.get(0));
    assertArrayEquals(new int[] { 5, 6 }, merged.get(1));
    assertArrayEquals(new int[] { 12, 8 }, merged.get(2));
    assertArrayEquals(new int[] { 8, 7 }, merged.get(3));
    
    // 'subsumed' ranges are preserved
    ranges.clear();
    ranges.add(new int[] { 10, 30 });
    ranges.add(new int[] { 15, 25 }); 
    merged = MapList.coalesceRanges(ranges);
    assertEquals(2, merged.size());
    assertArrayEquals(new int[] { 10, 30 }, merged.get(0));
    assertArrayEquals(new int[] { 15, 25 }, merged.get(1));
  }

  /**
   * Test the method that compounds ('traverses') two mappings
   */
  @Test(groups = "Functional")
  public void testTraverse()
  {
    /*
     * simple 1:1 plus 1:1 forwards
     */
    MapList ml1 = new MapList(new int[] { 3, 4, 8, 12 },
            new int[]
            { 5, 8, 11, 13 }, 1, 1);
    assertEquals("{[3, 4], [8, 12]}", prettyPrint(ml1.getFromRanges()));
    assertEquals("{[5, 8], [11, 13]}", prettyPrint(ml1.getToRanges()));

    MapList ml2 = new MapList(new int[] { 1, 50 },
            new int[]
            { 40, 45, 70, 75, 90, 127 }, 1, 1);
    assertEquals("{[1, 50]}", prettyPrint(ml2.getFromRanges()));
    assertEquals("{[40, 45], [70, 75], [90, 127]}",
            prettyPrint(ml2.getToRanges()));

    MapList compound = ml1.traverse(ml2);

    assertEquals(1, compound.getFromRatio());
    assertEquals(1, compound.getToRatio());
    List<int[]> fromRanges = compound.getFromRanges();
    assertEquals(2, fromRanges.size());
    assertArrayEquals(new int[] { 3, 4 }, fromRanges.get(0));
    assertArrayEquals(new int[] { 8, 12 }, fromRanges.get(1));
    List<int[]> toRanges = compound.getToRanges();
    assertEquals(4, toRanges.size());
    // 5-8 maps to 44-45,70-71
    // 11-13 maps to 74-75,90
    assertArrayEquals(new int[] { 44, 45 }, toRanges.get(0));
    assertArrayEquals(new int[] { 70, 71 }, toRanges.get(1));
    assertArrayEquals(new int[] { 74, 75 }, toRanges.get(2));
    assertArrayEquals(new int[] { 90, 90 }, toRanges.get(3));

    /*
     * 1:1 over 1:1 backwards ('reverse strand')
     */
    ml1 = new MapList(new int[] { 1, 50 }, new int[] { 70, 119 }, 1, 1);
    ml2 = new MapList(new int[] { 1, 500 },
            new int[]
            { 1000, 901, 600, 201 }, 1, 1);
    compound = ml1.traverse(ml2);

    assertEquals(1, compound.getFromRatio());
    assertEquals(1, compound.getToRatio());
    fromRanges = compound.getFromRanges();
    assertEquals(1, fromRanges.size());
    assertArrayEquals(new int[] { 1, 50 }, fromRanges.get(0));
    toRanges = compound.getToRanges();
    assertEquals(2, toRanges.size());
    assertArrayEquals(new int[] { 931, 901 }, toRanges.get(0));
    assertArrayEquals(new int[] { 600, 582 }, toRanges.get(1));

    /*
     * 1:1 plus 1:3 should result in 1:3
     */
    ml1 = new MapList(new int[] { 1, 30 }, new int[] { 11, 40 }, 1, 1);
    ml2 = new MapList(new int[] { 1, 100 }, new int[] { 1, 50, 91, 340 }, 1,
            3);
    compound = ml1.traverse(ml2);

    assertEquals(1, compound.getFromRatio());
    assertEquals(3, compound.getToRatio());
    fromRanges = compound.getFromRanges();
    assertEquals(1, fromRanges.size());
    assertArrayEquals(new int[] { 1, 30 }, fromRanges.get(0));
    // 11-40 maps to 31-50,91-160
    toRanges = compound.getToRanges();
    assertEquals(2, toRanges.size());
    assertArrayEquals(new int[] { 31, 50 }, toRanges.get(0));
    assertArrayEquals(new int[] { 91, 160 }, toRanges.get(1));

    /*
     * 3:1 plus 1:1 should result in 3:1
     */
    ml1 = new MapList(new int[] { 1, 30 }, new int[] { 11, 20 }, 3, 1);
    ml2 = new MapList(new int[] { 1, 100 }, new int[] { 1, 15, 91, 175 }, 1,
            1);
    compound = ml1.traverse(ml2);

    assertEquals(3, compound.getFromRatio());
    assertEquals(1, compound.getToRatio());
    fromRanges = compound.getFromRanges();
    assertEquals(1, fromRanges.size());
    assertArrayEquals(new int[] { 1, 30 }, fromRanges.get(0));
    // 11-20 maps to 11-15, 91-95
    toRanges = compound.getToRanges();
    assertEquals(2, toRanges.size());
    assertArrayEquals(new int[] { 11, 15 }, toRanges.get(0));
    assertArrayEquals(new int[] { 91, 95 }, toRanges.get(1));

    /*
     * 1:3 plus 3:1 should result in 1:1
     */
    ml1 = new MapList(new int[] { 21, 40 }, new int[] { 13, 72 }, 1, 3);
    ml2 = new MapList(new int[] { 1, 300 }, new int[] { 51, 70, 121, 200 },
            3, 1);
    compound = ml1.traverse(ml2);

    assertEquals(1, compound.getFromRatio());
    assertEquals(1, compound.getToRatio());
    fromRanges = compound.getFromRanges();
    assertEquals(1, fromRanges.size());
    assertArrayEquals(new int[] { 21, 40 }, fromRanges.get(0));
    // 13-72 maps 3:1 to 55-70, 121-124
    toRanges = compound.getToRanges();
    assertEquals(2, toRanges.size());
    assertArrayEquals(new int[] { 55, 70 }, toRanges.get(0));
    assertArrayEquals(new int[] { 121, 124 }, toRanges.get(1));

    /*
     * 3:1 plus 1:3 should result in 1:1
     */
    ml1 = new MapList(new int[] { 31, 90 }, new int[] { 13, 32 }, 3, 1);
    ml2 = new MapList(new int[] { 11, 40 }, new int[] { 41, 50, 71, 150 },
            1, 3);
    compound = ml1.traverse(ml2);

    assertEquals(1, compound.getFromRatio());
    assertEquals(1, compound.getToRatio());
    fromRanges = compound.getFromRanges();
    assertEquals(1, fromRanges.size());
    assertArrayEquals(new int[] { 31, 90 }, fromRanges.get(0));
    // 13-32 maps to 47-50,71-126
    toRanges = compound.getToRanges();
    assertEquals(2, toRanges.size());
    assertArrayEquals(new int[] { 47, 50 }, toRanges.get(0));
    assertArrayEquals(new int[] { 71, 126 }, toRanges.get(1));

    /*
     * method returns null if not all regions are mapped through
     */
    ml1 = new MapList(new int[] { 1, 50 }, new int[] { 101, 150 }, 1, 1);
    ml2 = new MapList(new int[] { 131, 180 }, new int[] { 201, 250 }, 1, 3);
    compound = ml1.traverse(ml2);
    assertNull(compound);
  }

  /**
   * Test that method that inspects for the (first) forward or reverse 'to'
   * range. Single position ranges are ignored.
   */
  @Test(groups = { "Functional" })
  public void testIsToForwardsStrand()
  {
    // [3-9] declares forward strand
    MapList ml = new MapList(new int[] { 20, 11 },
            new int[]
            { 2, 2, 3, 9, 12, 11 }, 1, 1);
    assertTrue(ml.isToForwardStrand());

    // [11-5] declares reverse strand ([13-14] is ignored)
    ml = new MapList(new int[] { 20, 11 },
            new int[]
            { 2, 2, 11, 5, 13, 14 }, 1, 1);
    assertFalse(ml.isToForwardStrand());

    // all single position ranges - defaults to forward strand
    ml = new MapList(new int[] { 3, 1 }, new int[] { 2, 2, 4, 4, 6, 6 }, 1,
            1);
    assertTrue(ml.isToForwardStrand());
  }
}
