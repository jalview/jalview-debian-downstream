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

  public static final char GAP_SPACE = ' ';

  public static final char GAP_DOT = '.';

  public static final char GAP_DASH = '-';

  public static final String GapChars = new String(
          new char[]
          { GAP_SPACE, GAP_DOT, GAP_DASH });

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
  public static float compare(SequenceI ii, SequenceI jj, int start,
          int end)
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
        if (si.substring(start + j, start + j + 1)
                .equals(sj.substring(start + j, start + j + 1)))
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
        if (si.substring(start + j, start + j + 1)
                .equals(sj.substring(start + j, start + j + 1)))
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
   * @deprecated use PIDModel.computePID()
   */
  @Deprecated
  public final static float PID(String seq1, String seq2)
  {
    return PID(seq1, seq2, 0, seq1.length());
  }

  static final int caseShift = 'a' - 'A';

  // Another pid with region specification
  /**
   * @deprecated use PIDModel.computePID()
   */
  @Deprecated
  public final static float PID(String seq1, String seq2, int start,
          int end)
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
   * @deprecated use PIDModel.computePID()
   */
  @Deprecated
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
   * Overloaded method signature to test whether a single sequence is nucleotide
   * (that is, more than 85% CGTA)
   * 
   * @param seq
   * @return
   */
  public static final boolean isNucleotide(SequenceI seq)
  {
    if (seq==null)
    {
      return false;
    }
    long ntCount = 0;
    long aaCount = 0;

    int len = seq.getLength();
    for (int i = 0; i < len; i++)
    {
      char c = seq.getCharAt(i);
      if (isNucleotide(c))
      {
        ntCount++;
      }
      else if (!isGap(c))
      {
        aaCount++;
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
    // true if we have seen a nucleotide sequence
    boolean na=false;
    for (SequenceI seq : seqs)
    {
      if (seq == null)
      {
        continue;
      }
      na=true;
      // TODO could possibly make an informed guess just from the first sequence
      // to save a lengthy calculation
      if (seq.isProtein()) {
        // if even one looks like protein, the alignment is protein
        return false;
      }
    }
    return na;
  }

  /**
   * Answers true if the character is one of aAcCgGtTuU
   * 
   * @param c
   * @return
   */
  public static boolean isNucleotide(char c)
  {
    if ('a' <= c && c <= 'z')
    {
      c -= TO_UPPER_CASE;
    }

    switch (c)
    {
    case 'A':
    case 'C':
    case 'G':
    case 'T':
    case 'U':
      return true;
    }
    return false;
  }

  /**
   * Answers true if every character in the string is one of aAcCgGtTuU, or
   * (optionally) a gap character (dot, dash, space), else false
   * 
   * @param s
   * @param allowGaps
   * @return
   */
  public static boolean isNucleotideSequence(String s, boolean allowGaps)
  {
    if (s == null)
    {
      return false;
    }
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (!isNucleotide(c))
      {
        if (!allowGaps || !isGap(c))
        {
          return false;
        }
      }
    }
    return true;
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
    final SequenceI[] oneDArray = flattened
            .toArray(new SequenceI[flattened.size()]);
    return isNucleotide(oneDArray);
  }

  /**
   * Compares two residues either case sensitively or case insensitively
   * depending on the caseSensitive flag
   * 
   * @param c1
   *          first char
   * @param c2
   *          second char to compare with
   * @param caseSensitive
   *          if true comparison will be case sensitive otherwise its not
   * @return
   */
  public static boolean isSameResidue(char c1, char c2,
          boolean caseSensitive)
  {
    if (caseSensitive)
    {
      return (c1 == c2);
    }
    else
    {
      return Character.toUpperCase(c1) == Character.toUpperCase(c2);
    }
  }
}
