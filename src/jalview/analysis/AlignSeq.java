/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.schemes.ScoreMatrix;
import jalview.util.Comparison;
import jalview.util.Format;
import jalview.util.MapList;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * 
 * @author $author$
 * @version $Revision$
 */
public class AlignSeq
{
  public static final String PEP = "pep";

  public static final String DNA = "dna";

  private static final String NEWLINE = System.lineSeparator();

  static String[] dna = { "A", "C", "G", "T", "-" };

  // "C", "T", "A", "G", "-"};
  static String[] pep = { "A", "R", "N", "D", "C", "Q", "E", "G", "H", "I",
      "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V", "B", "Z", "X", "-" };

  int[][] score;

  int[][] E;

  int[][] F;

  int[][] traceback;

  int[] seq1;

  int[] seq2;

  SequenceI s1;

  SequenceI s2;

  public String s1str;

  public String s2str;

  int maxi;

  int maxj;

  int[] aseq1;

  int[] aseq2;

  public String astr1 = "";

  public String astr2 = "";

  /** DOCUMENT ME!! */
  public int seq1start;

  /** DOCUMENT ME!! */
  public int seq1end;

  /** DOCUMENT ME!! */
  public int seq2start;

  /** DOCUMENT ME!! */
  public int seq2end;

  int count;

  /** DOCUMENT ME!! */
  public int maxscore;

  float pid;

  int prev = 0;

  int gapOpen = 120;

  int gapExtend = 20;

  int[][] lookup = ResidueProperties.getBLOSUM62();

  String[] intToStr = pep;

  int defInt = 23;

  StringBuffer output = new StringBuffer();

  String type;

  private int[] charToInt;

  /**
   * Creates a new AlignSeq object.
   * 
   * @param s1
   *          DOCUMENT ME!
   * @param s2
   *          DOCUMENT ME!
   * @param type
   *          DOCUMENT ME!
   */
  public AlignSeq(SequenceI s1, SequenceI s2, String type)
  {
    SeqInit(s1, s1.getSequenceAsString(), s2, s2.getSequenceAsString(),
            type);
  }

  /**
   * Creates a new AlignSeq object.
   * 
   * @param s1
   *          DOCUMENT ME!
   * @param s2
   *          DOCUMENT ME!
   * @param type
   *          DOCUMENT ME!
   */
  public AlignSeq(SequenceI s1, String string1, SequenceI s2,
          String string2, String type)
  {
    SeqInit(s1, string1.toUpperCase(), s2, string2.toUpperCase(), type);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getMaxScore()
  {
    return maxscore;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq2Start()
  {
    return seq2start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq2End()
  {
    return seq2end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq1Start()
  {
    return seq1start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq1End()
  {
    return seq1end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getOutput()
  {
    return output.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getAStr1()
  {
    return astr1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getAStr2()
  {
    return astr2;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int[] getASeq1()
  {
    return aseq1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int[] getASeq2()
  {
    return aseq2;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public SequenceI getS1()
  {
    return s1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public SequenceI getS2()
  {
    return s2;
  }

  /**
   * 
   * @return aligned instance of Seq 1
   */
  public SequenceI getAlignedSeq1()
  {
    SequenceI alSeq1 = new Sequence(s1.getName(), getAStr1());
    alSeq1.setStart(s1.getStart() + getSeq1Start() - 1);
    alSeq1.setEnd(s1.getStart() + getSeq1End() - 1);
    alSeq1.setDatasetSequence(s1.getDatasetSequence() == null ? s1 : s1
            .getDatasetSequence());
    return alSeq1;
  }

  /**
   * 
   * @return aligned instance of Seq 2
   */
  public SequenceI getAlignedSeq2()
  {
    SequenceI alSeq2 = new Sequence(s2.getName(), getAStr2());
    alSeq2.setStart(s2.getStart() + getSeq2Start() - 1);
    alSeq2.setEnd(s2.getStart() + getSeq2End() - 1);
    alSeq2.setDatasetSequence(s2.getDatasetSequence() == null ? s2 : s2
            .getDatasetSequence());
    return alSeq2;
  }

  /**
   * Construct score matrix for sequences with standard DNA or PEPTIDE matrix
   * 
   * @param s1
   *          - sequence 1
   * @param string1
   *          - string to use for s1
   * @param s2
   *          - sequence 2
   * @param string2
   *          - string to use for s2
   * @param type
   *          DNA or PEPTIDE
   */
  public void SeqInit(SequenceI s1, String string1, SequenceI s2,
          String string2, String type)
  {
    this.s1 = s1;
    this.s2 = s2;
    setDefaultParams(type);
    SeqInit(string1, string2);
  }

  /**
   * Construct score matrix for sequences with custom substitution matrix
   * 
   * @param s1
   *          - sequence 1
   * @param string1
   *          - string to use for s1
   * @param s2
   *          - sequence 2
   * @param string2
   *          - string to use for s2
   * @param scoreMatrix
   *          - substitution matrix to use for alignment
   */
  public void SeqInit(SequenceI s1, String string1, SequenceI s2,
          String string2, ScoreMatrix scoreMatrix)
  {
    this.s1 = s1;
    this.s2 = s2;
    setType(scoreMatrix.isDNA() ? AlignSeq.DNA : AlignSeq.PEP);
    lookup = scoreMatrix.getMatrix();
  }

  /**
   * construct score matrix for string1 and string2 (after removing any existing
   * gaps
   * 
   * @param string1
   * @param string2
   */
  private void SeqInit(String string1, String string2)
  {
    s1str = extractGaps(jalview.util.Comparison.GapChars, string1);
    s2str = extractGaps(jalview.util.Comparison.GapChars, string2);

    if (s1str.length() == 0 || s2str.length() == 0)
    {
      output.append("ALL GAPS: "
              + (s1str.length() == 0 ? s1.getName() : " ")
              + (s2str.length() == 0 ? s2.getName() : ""));
      return;
    }

    // System.out.println("lookuip " + rt.freeMemory() + " "+ rt.totalMemory());
    seq1 = new int[s1str.length()];

    // System.out.println("seq1 " + rt.freeMemory() +" " + rt.totalMemory());
    seq2 = new int[s2str.length()];

    // System.out.println("seq2 " + rt.freeMemory() + " " + rt.totalMemory());
    score = new int[s1str.length()][s2str.length()];

    // System.out.println("score " + rt.freeMemory() + " " + rt.totalMemory());
    E = new int[s1str.length()][s2str.length()];

    // System.out.println("E " + rt.freeMemory() + " " + rt.totalMemory());
    F = new int[s1str.length()][s2str.length()];
    traceback = new int[s1str.length()][s2str.length()];

    // System.out.println("F " + rt.freeMemory() + " " + rt.totalMemory());
    seq1 = stringToInt(s1str, type);

    // System.out.println("seq1 " + rt.freeMemory() + " " + rt.totalMemory());
    seq2 = stringToInt(s2str, type);

    // System.out.println("Seq2 " + rt.freeMemory() + " " + rt.totalMemory());
    // long tstart = System.currentTimeMillis();
    // calcScoreMatrix();
    // long tend = System.currentTimeMillis();
    // System.out.println("Time take to calculate score matrix = " +
    // (tend-tstart) + " ms");
    // printScoreMatrix(score);
    // System.out.println();
    // printScoreMatrix(traceback);
    // System.out.println();
    // printScoreMatrix(E);
    // System.out.println();
    // /printScoreMatrix(F);
    // System.out.println();
    // tstart = System.currentTimeMillis();
    // traceAlignment();
    // tend = System.currentTimeMillis();
    // System.out.println("Time take to traceback alignment = " + (tend-tstart)
    // + " ms");
  }

  private void setDefaultParams(String type)
  {
    setType(type);

    if (type.equals(AlignSeq.PEP))
    {
      lookup = ResidueProperties.getDefaultPeptideMatrix();
    }
    else if (type.equals(AlignSeq.DNA))
    {
      lookup = ResidueProperties.getDefaultDnaMatrix();
    }
  }

  private void setType(String type2)
  {
    this.type = type2;
    if (type.equals(AlignSeq.PEP))
    {
      intToStr = pep;
      charToInt = ResidueProperties.aaIndex;
      defInt = ResidueProperties.maxProteinIndex;
    }
    else if (type.equals(AlignSeq.DNA))
    {
      intToStr = dna;
      charToInt = ResidueProperties.nucleotideIndex;
      defInt = ResidueProperties.maxNucleotideIndex;
    }
    else
    {
      output.append("Wrong type = dna or pep only");
      throw new Error(MessageManager.formatMessage(
              "error.unknown_type_dna_or_pep", new String[] { type2 }));
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void traceAlignment()
  {
    // Find the maximum score along the rhs or bottom row
    int max = -9999;

    for (int i = 0; i < seq1.length; i++)
    {
      if (score[i][seq2.length - 1] > max)
      {
        max = score[i][seq2.length - 1];
        maxi = i;
        maxj = seq2.length - 1;
      }
    }

    for (int j = 0; j < seq2.length; j++)
    {
      if (score[seq1.length - 1][j] > max)
      {
        max = score[seq1.length - 1][j];
        maxi = seq1.length - 1;
        maxj = j;
      }
    }

    // System.out.println(maxi + " " + maxj + " " + score[maxi][maxj]);
    int i = maxi;
    int j = maxj;
    int trace;
    maxscore = score[i][j] / 10;

    seq1end = maxi + 1;
    seq2end = maxj + 1;

    aseq1 = new int[seq1.length + seq2.length];
    aseq2 = new int[seq1.length + seq2.length];

    count = (seq1.length + seq2.length) - 1;

    while ((i > 0) && (j > 0))
    {
      if ((aseq1[count] != defInt) && (i >= 0))
      {
        aseq1[count] = seq1[i];
        astr1 = s1str.charAt(i) + astr1;
      }

      if ((aseq2[count] != defInt) && (j > 0))
      {
        aseq2[count] = seq2[j];
        astr2 = s2str.charAt(j) + astr2;
      }

      trace = findTrace(i, j);

      if (trace == 0)
      {
        i--;
        j--;
      }
      else if (trace == 1)
      {
        j--;
        aseq1[count] = defInt;
        astr1 = "-" + astr1.substring(1);
      }
      else if (trace == -1)
      {
        i--;
        aseq2[count] = defInt;
        astr2 = "-" + astr2.substring(1);
      }

      count--;
    }

    seq1start = i + 1;
    seq2start = j + 1;

    if (aseq1[count] != defInt)
    {
      aseq1[count] = seq1[i];
      astr1 = s1str.charAt(i) + astr1;
    }

    if (aseq2[count] != defInt)
    {
      aseq2[count] = seq2[j];
      astr2 = s2str.charAt(j) + astr2;
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void printAlignment(java.io.PrintStream os)
  {
    // TODO: Use original sequence characters rather than re-translated
    // characters in output
    // Find the biggest id length for formatting purposes
    String s1id = s1.getName(), s2id = s2.getName();
    int maxid = s1.getName().length();
    if (s2.getName().length() > maxid)
    {
      maxid = s2.getName().length();
    }
    if (maxid > 30)
    {
      maxid = 30;
      // JAL-527 - truncate the sequence ids
      if (s1.getName().length() > maxid)
      {
        s1id = s1.getName().substring(0, 30);
      }
      if (s2.getName().length() > maxid)
      {
        s2id = s2.getName().substring(0, 30);
      }
    }
    int len = 72 - maxid - 1;
    int nochunks = ((aseq1.length - count) / len)
            + ((aseq1.length - count) % len > 0 ? 1 : 0);
    pid = 0;

    output.append("Score = ").append(score[maxi][maxj]).append(NEWLINE);
    output.append("Length of alignment = ")
            .append(String.valueOf(aseq1.length - count)).append(NEWLINE);
    output.append("Sequence ");
    output.append(new Format("%" + maxid + "s").form(s1.getName()));
    output.append(" :  ").append(String.valueOf(s1.getStart()))
            .append(" - ").append(String.valueOf(s1.getEnd()));
    output.append(" (Sequence length = ")
            .append(String.valueOf(s1str.length())).append(")")
            .append(NEWLINE);
    output.append("Sequence ");
    output.append(new Format("%" + maxid + "s").form(s2.getName()));
    output.append(" :  ").append(String.valueOf(s2.getStart()))
            .append(" - ").append(String.valueOf(s2.getEnd()));
    output.append(" (Sequence length = ")
            .append(String.valueOf(s2str.length())).append(")")
            .append(NEWLINE).append(NEWLINE);

    for (int j = 0; j < nochunks; j++)
    {
      // Print the first aligned sequence
      output.append(new Format("%" + (maxid) + "s").form(s1id)).append(" ");

      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < astr1.length())
        {
          output.append(astr1.charAt(i + (j * len)));
        }
      }

      output.append(NEWLINE);
      output.append(new Format("%" + (maxid) + "s").form(" ")).append(" ");

      // Print out the matching chars
      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < astr1.length())
        {
          boolean sameChar = Comparison.isSameResidue(
                  astr1.charAt(i + (j * len)), astr2.charAt(i + (j * len)),
                  false);
          if (sameChar
                  && !jalview.util.Comparison.isGap(astr1.charAt(i
                          + (j * len))))
          {
            pid++;
            output.append("|");
          }
          else if (type.equals("pep"))
          {
            if (ResidueProperties.getPAM250(astr1.charAt(i + (j * len)),
                    astr2.charAt(i + (j * len))) > 0)
            {
              output.append(".");
            }
            else
            {
              output.append(" ");
            }
          }
          else
          {
            output.append(" ");
          }
        }
      }

      // Now print the second aligned sequence
      output = output.append(NEWLINE);
      output = output.append(new Format("%" + (maxid) + "s").form(s2id))
              .append(" ");

      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < astr2.length())
        {
          output.append(astr2.charAt(i + (j * len)));
        }
      }

      output.append(NEWLINE).append(NEWLINE);
    }

    pid = pid / (aseq1.length - count) * 100;
    output = output.append(new Format("Percentage ID = %2.2f\n").form(pid));
    try
    {
      os.print(output.toString());
    } catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param mat
   *          DOCUMENT ME!
   */
  public void printScoreMatrix(int[][] mat)
  {
    int n = seq1.length;
    int m = seq2.length;

    for (int i = 0; i < n; i++)
    {
      // Print the top sequence
      if (i == 0)
      {
        Format.print(System.out, "%8s", s2str.substring(0, 1));

        for (int jj = 1; jj < m; jj++)
        {
          Format.print(System.out, "%5s", s2str.substring(jj, jj + 1));
        }

        System.out.println();
      }

      for (int j = 0; j < m; j++)
      {
        if (j == 0)
        {
          Format.print(System.out, "%3s", s1str.substring(i, i + 1));
        }

        Format.print(System.out, "%3d ", mat[i][j] / 10);
      }

      System.out.println();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int findTrace(int i, int j)
  {
    int t = 0;
    int max = score[i - 1][j - 1] + (lookup[seq1[i]][seq2[j]] * 10);

    if (F[i][j] > max)
    {
      max = F[i][j];
      t = -1;
    }
    else if (F[i][j] == max)
    {
      if (prev == -1)
      {
        max = F[i][j];
        t = -1;
      }
    }

    if (E[i][j] >= max)
    {
      max = E[i][j];
      t = 1;
    }
    else if (E[i][j] == max)
    {
      if (prev == 1)
      {
        max = E[i][j];
        t = 1;
      }
    }

    prev = t;

    return t;
  }

  /**
   * DOCUMENT ME!
   */
  public void calcScoreMatrix()
  {
    int n = seq1.length;
    int m = seq2.length;

    // top left hand element
    score[0][0] = lookup[seq1[0]][seq2[0]] * 10;
    E[0][0] = -gapExtend;
    F[0][0] = 0;

    // Calculate the top row first
    for (int j = 1; j < m; j++)
    {
      // What should these values be? 0 maybe
      E[0][j] = max(score[0][j - 1] - gapOpen, E[0][j - 1] - gapExtend);
      F[0][j] = -gapExtend;

      score[0][j] = max(lookup[seq1[0]][seq2[j]] * 10, -gapOpen, -gapExtend);

      traceback[0][j] = 1;
    }

    // Now do the left hand column
    for (int i = 1; i < n; i++)
    {
      E[i][0] = -gapOpen;
      F[i][0] = max(score[i - 1][0] - gapOpen, F[i - 1][0] - gapExtend);

      score[i][0] = max(lookup[seq1[i]][seq2[0]] * 10, E[i][0], F[i][0]);
      traceback[i][0] = -1;
    }

    // Now do all the other rows
    for (int i = 1; i < n; i++)
    {
      for (int j = 1; j < m; j++)
      {
        E[i][j] = max(score[i][j - 1] - gapOpen, E[i][j - 1] - gapExtend);
        F[i][j] = max(score[i - 1][j] - gapOpen, F[i - 1][j] - gapExtend);

        score[i][j] = max(score[i - 1][j - 1]
                + (lookup[seq1[i]][seq2[j]] * 10), E[i][j], F[i][j]);
        traceback[i][j] = findTrace(i, j);
      }
    }
  }

  /**
   * Returns the given sequence with all of the given gap characters removed.
   * 
   * @param gapChars
   *          a string of characters to be treated as gaps
   * @param seq
   *          the input sequence
   * 
   * @return
   */
  public static String extractGaps(String gapChars, String seq)
  {
    if (gapChars == null || seq == null)
    {
      return null;
    }
    StringTokenizer str = new StringTokenizer(seq, gapChars);
    StringBuilder newString = new StringBuilder(seq.length());

    while (str.hasMoreTokens())
    {
      newString.append(str.nextToken());
    }

    return newString.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i1
   *          DOCUMENT ME!
   * @param i2
   *          DOCUMENT ME!
   * @param i3
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int max(int i1, int i2, int i3)
  {
    int max = i1;

    if (i2 > i1)
    {
      max = i2;
    }

    if (i3 > max)
    {
      max = i3;
    }

    return max;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i1
   *          DOCUMENT ME!
   * @param i2
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int max(int i1, int i2)
  {
    int max = i1;

    if (i2 > i1)
    {
      max = i2;
    }

    return max;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param s
   *          DOCUMENT ME!
   * @param type
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int[] stringToInt(String s, String type)
  {
    int[] seq1 = new int[s.length()];

    for (int i = 0; i < s.length(); i++)
    {
      // String ss = s.substring(i, i + 1).toUpperCase();
      char c = s.charAt(i);
      if ('a' <= c && c <= 'z')
      {
        // TO UPPERCASE !!!
        c -= ('a' - 'A');
      }

      try
      {
        seq1[i] = charToInt[c]; // set accordingly from setType
        if (seq1[i] < 0 || seq1[i] > defInt) // set from setType: 23 for
                                             // peptides, or 4 for NA.
        {
          seq1[i] = defInt;
        }

      } catch (Exception e)
      {
        seq1[i] = defInt;
      }
    }

    return seq1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param mat
   *          DOCUMENT ME!
   * @param n
   *          DOCUMENT ME!
   * @param m
   *          DOCUMENT ME!
   * @param psize
   *          DOCUMENT ME!
   */
  public static void displayMatrix(Graphics g, int[][] mat, int n, int m,
          int psize)
  {
    // TODO method dosen't seem to be referenced anywhere delete??
    int max = -1000;
    int min = 1000;

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < m; j++)
      {
        if (mat[i][j] >= max)
        {
          max = mat[i][j];
        }

        if (mat[i][j] <= min)
        {
          min = mat[i][j];
        }
      }
    }

    System.out.println(max + " " + min);

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < m; j++)
      {
        int x = psize * i;
        int y = psize * j;

        // System.out.println(mat[i][j]);
        float score = (float) (mat[i][j] - min) / (float) (max - min);
        g.setColor(new Color(score, 0, 0));
        g.fillRect(x, y, psize, psize);

        // System.out.println(x + " " + y + " " + score);
      }
    }
  }

  /**
   * Compute a globally optimal needleman and wunsch alignment between two
   * sequences
   * 
   * @param s1
   * @param s2
   * @param type
   *          AlignSeq.DNA or AlignSeq.PEP
   */
  public static AlignSeq doGlobalNWAlignment(SequenceI s1, SequenceI s2,
          String type)
  {
    AlignSeq as = new AlignSeq(s1, s2, type);

    as.calcScoreMatrix();
    as.traceAlignment();
    return as;
  }

  /**
   * 
   * @return mapping from positions in S1 to corresponding positions in S2
   */
  public jalview.datamodel.Mapping getMappingFromS1(boolean allowmismatch)
  {
    ArrayList<Integer> as1 = new ArrayList<Integer>(), as2 = new ArrayList<Integer>();
    int pdbpos = s2.getStart() + getSeq2Start() - 2;
    int alignpos = s1.getStart() + getSeq1Start() - 2;
    int lp2 = pdbpos - 3, lp1 = alignpos - 3;
    boolean lastmatch = false;
    // and now trace the alignment onto the atom set.
    for (int i = 0; i < astr1.length(); i++)
    {
      char c1 = astr1.charAt(i), c2 = astr2.charAt(i);
      if (c1 != '-')
      {
        alignpos++;
      }

      if (c2 != '-')
      {
        pdbpos++;
      }

      if (allowmismatch || c1 == c2)
      {
        // extend mapping interval
        if (lp1 + 1 != alignpos || lp2 + 1 != pdbpos)
        {
          as1.add(Integer.valueOf(alignpos));
          as2.add(Integer.valueOf(pdbpos));
        }
        lastmatch = true;
        lp1 = alignpos;
        lp2 = pdbpos;
      }
      else
      {
        // extend mapping interval
        if (lastmatch)
        {
          as1.add(Integer.valueOf(lp1));
          as2.add(Integer.valueOf(lp2));
        }
        lastmatch = false;
      }
    }
    // construct range pairs

    int[] mapseq1 = new int[as1.size() + (lastmatch ? 1 : 0)], mapseq2 = new int[as2
            .size() + (lastmatch ? 1 : 0)];
    int i = 0;
    for (Integer ip : as1)
    {
      mapseq1[i++] = ip;
    }
    ;
    i = 0;
    for (Integer ip : as2)
    {
      mapseq2[i++] = ip;
    }
    ;
    if (lastmatch)
    {
      mapseq1[mapseq1.length - 1] = alignpos;
      mapseq2[mapseq2.length - 1] = pdbpos;
    }
    MapList map = new MapList(mapseq1, mapseq2, 1, 1);

    jalview.datamodel.Mapping mapping = new Mapping(map);
    mapping.setTo(s2);
    return mapping;
  }

  /**
   * matches ochains against al and populates seqs with the best match between
   * each ochain and the set in al
   * 
   * @param ochains
   * @param al
   * @param dnaOrProtein
   * @param removeOldAnnots
   *          when true, old annotation is cleared before new annotation
   *          transferred
   * @return List<List<SequenceI> originals, List<SequenceI> replacement,
   *         List<AlignSeq> alignment between each>
   */
  public static List<List<? extends Object>> replaceMatchingSeqsWith(
          List<SequenceI> seqs, List<AlignmentAnnotation> annotations,
          List<SequenceI> ochains, AlignmentI al, String dnaOrProtein,
          boolean removeOldAnnots)
  {
    List<SequenceI> orig = new ArrayList<SequenceI>(), repl = new ArrayList<SequenceI>();
    List<AlignSeq> aligs = new ArrayList<AlignSeq>();
    if (al != null && al.getHeight() > 0)
    {
      ArrayList<SequenceI> matches = new ArrayList<SequenceI>();
      ArrayList<AlignSeq> aligns = new ArrayList<AlignSeq>();

      for (SequenceI sq : ochains)
      {
        SequenceI bestm = null;
        AlignSeq bestaseq = null;
        int bestscore = 0;
        for (SequenceI msq : al.getSequences())
        {
          AlignSeq aseq = doGlobalNWAlignment(msq, sq, dnaOrProtein);
          if (bestm == null || aseq.getMaxScore() > bestscore)
          {
            bestscore = aseq.getMaxScore();
            bestaseq = aseq;
            bestm = msq;
          }
        }
        System.out.println("Best Score for " + (matches.size() + 1) + " :"
                + bestscore);
        matches.add(bestm);
        aligns.add(bestaseq);
        al.deleteSequence(bestm);
      }
      for (int p = 0, pSize = seqs.size(); p < pSize; p++)
      {
        SequenceI sq, sp = seqs.get(p);
        int q;
        if ((q = ochains.indexOf(sp)) > -1)
        {
          seqs.set(p, sq = matches.get(q));
          orig.add(sp);
          repl.add(sq);
          sq.setName(sp.getName());
          sq.setDescription(sp.getDescription());
          Mapping sp2sq;
          sq.transferAnnotation(sp,
                  sp2sq = aligns.get(q).getMappingFromS1(false));
          aligs.add(aligns.get(q));
          int inspos = -1;
          for (int ap = 0; ap < annotations.size();)
          {
            if (annotations.get(ap).sequenceRef == sp)
            {
              if (inspos == -1)
              {
                inspos = ap;
              }
              if (removeOldAnnots)
              {
                annotations.remove(ap);
              }
              else
              {
                AlignmentAnnotation alan = annotations.remove(ap);
                alan.liftOver(sq, sp2sq);
                alan.setSequenceRef(sq);
                sq.addAlignmentAnnotation(alan);
              }
            }
            else
            {
              ap++;
            }
          }
          if (sq.getAnnotation() != null && sq.getAnnotation().length > 0)
          {
            annotations.addAll(inspos == -1 ? annotations.size() : inspos,
                    Arrays.asList(sq.getAnnotation()));
          }
        }
      }
    }
    return Arrays.asList(orig, repl, aligs);
  }

  /**
   * compute the PID vector used by the redundancy filter.
   * 
   * @param originalSequences
   *          - sequences in alignment that are to filtered
   * @param omitHidden
   *          - null or strings to be analysed (typically, visible portion of
   *          each sequence in alignment)
   * @param start
   *          - first column in window for calculation
   * @param end
   *          - last column in window for calculation
   * @param ungapped
   *          - if true then use ungapped sequence to compute PID
   * @return vector containing maximum PID for i-th sequence and any sequences
   *         longer than that seuqence
   */
  public static float[] computeRedundancyMatrix(
          SequenceI[] originalSequences, String[] omitHidden, int start,
          int end, boolean ungapped)
  {
    int height = originalSequences.length;
    float[] redundancy = new float[height];
    int[] lngth = new int[height];
    for (int i = 0; i < height; i++)
    {
      redundancy[i] = 0f;
      lngth[i] = -1;
    }

    // long start = System.currentTimeMillis();

    float pid;
    String seqi, seqj;
    for (int i = 0; i < height; i++)
    {

      for (int j = 0; j < i; j++)
      {
        if (i == j)
        {
          continue;
        }

        if (omitHidden == null)
        {
          seqi = originalSequences[i].getSequenceAsString(start, end);
          seqj = originalSequences[j].getSequenceAsString(start, end);
        }
        else
        {
          seqi = omitHidden[i];
          seqj = omitHidden[j];
        }
        if (lngth[i] == -1)
        {
          String ug = AlignSeq.extractGaps(Comparison.GapChars, seqi);
          lngth[i] = ug.length();
          if (ungapped)
          {
            seqi = ug;
          }
        }
        if (lngth[j] == -1)
        {
          String ug = AlignSeq.extractGaps(Comparison.GapChars, seqj);
          lngth[j] = ug.length();
          if (ungapped)
          {
            seqj = ug;
          }
        }
        pid = Comparison.PID(seqi, seqj);

        // use real sequence length rather than string length
        if (lngth[j] < lngth[i])
        {
          redundancy[j] = Math.max(pid, redundancy[j]);
        }
        else
        {
          redundancy[i] = Math.max(pid, redundancy[i]);
        }

      }
    }
    return redundancy;
  }
}
