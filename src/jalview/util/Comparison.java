/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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

import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.List;

/**
 * Assorted methods for analysing or comparing sequences.
 */
public class Comparison
{
  private static final int EIGHTY_FIVE = 85;

  private static final int TO_UPPER_CASE = 'a' - 'A';

  private static final char GAP_SPACE = ' ';

  private static final char GAP_DOT = '.';

  private static final char GAP_DASH = '-';

  public static final String GapChars = new String(new char[] { GAP_SPACE,
      GAP_DOT, GAP_DASH });

  /**
   * DOCUMENT ME!
   * 
   * @param ii
   *          DOCUMENT ME!
   * @param jj
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static final float compare(SequenceI ii, SequenceI jj)
  {
    return Comparison.compare(ii, jj, 0, ii.getLength() - 1);
  }

  /**
   * this was supposed to be an ungapped pid calculation
   * 
   * @param ii
   *          SequenceI
   * @param jj
   *          SequenceI
   * @param start
   *          int
   * @param end
   *          int
   * @return float
   */
  public static float compare(SequenceI ii, SequenceI jj, int start, int end)
  {
    String si = ii.getSequenceAsString();
    String sj = jj.getSequenceAsString();

    int ilen = si.length() - 1;
    int jlen = sj.length() - 1;

    while (Comparison.isGap(si.charAt(start + ilen)))
    {
      ilen--;
    }

    while (Comparison.isGap(sj.charAt(start + jlen)))
    {
      jlen--;
    }

    int count = 0;
    int match = 0;
    float pid = -1;

    if (ilen > jlen)
    {
      for (int j = 0; j < jlen; j++)
      {
        if (si.substring(start + j, start + j + 1).equals(
                sj.substring(start + j, start + j + 1)))
        {
          match++;
        }

        count++;
      }

      pid = (float) match / (float) ilen * 100;
    }
    else
    {
      for (int j = 0; j < jlen; j++)
      {
        if (si.substring(start + j, start + j + 1).equals(
                sj.substring(start + j, start + j + 1)))
        {
          match++;
        }

        count++;
      }

      pid = (float) match / (float) jlen * 100;
    }

    return pid;
  }

  /**
   * this is a gapped PID calculation
   * 
   * @param s1
   *          SequenceI
   * @param s2
   *          SequenceI
   * @return float
   */
  public final static float PID(String seq1, String seq2)
  {
    return PID(seq1, seq2, 0, seq1.length());
  }

  static final int caseShift = 'a' - 'A';

  // Another pid with region specification
  public final static float PID(String seq1, String seq2, int start, int end)
  {
    return PID(seq1, seq2, start, end, true, false);
  }

  /**
   * Calculate percent identity for a pair of sequences over a particular range,
   * with different options for ignoring gaps.
   * 
   * @param seq1
   * @param seq2
   * @param start
   *          - position in seqs
   * @param end
   *          - position in seqs
   * @param wcGaps
   *          - if true - gaps match any character, if false, do not match
   *          anything
   * @param ungappedOnly
   *          - if true - only count PID over ungapped columns
   * @return
   */
  public final static float PID(String seq1, String seq2, int start,
          int end, boolean wcGaps, boolean ungappedOnly)
  {
    int s1len = seq1.length();
    int s2len = seq2.length();

    int len = Math.min(s1len, s2len);

    if (end < len)
    {
      len = end;
    }

    if (len < start)
    {
      start = len - 1; // we just use a single residue for the difference
    }

    int elen = len - start, bad = 0;
    char chr1;
    char chr2;
    boolean agap;
    for (int i = start; i < len; i++)
    {
      chr1 = seq1.charAt(i);

      chr2 = seq2.charAt(i);
      agap = isGap(chr1) || isGap(chr2);
      if ('a' <= chr1 && chr1 <= 'z')
      {
        // TO UPPERCASE !!!
        // Faster than toUpperCase
        chr1 -= caseShift;
      }
      if ('a' <= chr2 && chr2 <= 'z')
      {
        // TO UPPERCASE !!!
        // Faster than toUpperCase
        chr2 -= caseShift;
      }

      if (chr1 != chr2)
      {
        if (agap)
        {
          if (ungappedOnly)
          {
            elen--;
          }
          else if (!wcGaps)
          {
            bad++;
          }
        }
        else
        {
          bad++;
        }
      }

    }
    if (elen < 1)
    {
      return 0f;
    }
    return ((float) 100 * (elen - bad)) / elen;
  }

  /**
   * Answers true if the supplied character is a recognised gap character, else
   * false. Currently hard-coded to recognise '-', '-' or ' ' (hyphen / dot /
   * space).
   * 
   * @param c
   * 
   * @return
   */
  public static final boolean isGap(char c)
  {
    return (c == GAP_DASH || c == GAP_DOT || c == GAP_SPACE) ? true : false;
  }

  /**
   * Answers true if more than 85% of the sequence residues (ignoring gaps) are
   * A, G, C, T or U, else false. This is just a heuristic guess and may give a
   * wrong answer (as AGCT are also amino acid codes).
   * 
   * @param seqs
   * @return
   */
  public static final boolean isNucleotide(SequenceI[] seqs)
  {
    if (seqs == null)
    {
      return false;
    }
    int ntCount = 0;
    int aaCount = 0;
    for (SequenceI seq : seqs)
    {
      if (seq == null)
      {
        continue;
      }
      // TODO could possibly make an informed guess just from the first sequence
      // to save a lengthy calculation
      for (char c : seq.getSequence())
      {
        if ('a' <= c && c <= 'z')
        {
          c -= TO_UPPER_CASE;
        }

        if (c == 'A' || c == 'G' || c == 'C' || c == 'T' || c == 'U')
        {
          ntCount++;
        }
        else if (!Comparison.isGap(c))
        {
          aaCount++;
        }
      }
    }

    /*
     * Check for nucleotide count > 85% of total count (in a form that evades
     * int / float conversion or divide by zero).
     */
    if (ntCount * 100 > EIGHTY_FIVE * (ntCount + aaCount))
    {
      return true;
    }
    else
    {
      return false;
    }

  }

  /**
   * Convenience overload of isNucleotide
   * 
   * @param seqs
   * @return
   */
  public static boolean isNucleotide(SequenceI[][] seqs)
  {
    if (seqs == null)
    {
      return false;
    }
    List<SequenceI> flattened = new ArrayList<SequenceI>();
    for (SequenceI[] ss : seqs)
    {
      for (SequenceI s : ss)
      {
        flattened.add(s);
      }
    }
    final SequenceI[] oneDArray = flattened.toArray(new SequenceI[flattened
            .size()]);
    return isNucleotide(oneDArray);
  }
}
