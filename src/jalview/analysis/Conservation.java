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
import jalview.datamodel.Annotation;
import jalview.datamodel.ResidueCount;
import jalview.datamodel.ResidueCount.SymbolCounts;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Calculates conservation values for a given set of sequences
 */
public class Conservation
{
  /*
   * need to have a minimum of 3% of sequences with a residue
   * for it to be included in the conservation calculation
   */
  private static final int THRESHOLD_PERCENT = 3;

  private static final int TOUPPERCASE = 'a' - 'A';

  SequenceI[] sequences;

  int start;

  int end;

  Vector<int[]> seqNums; // vector of int vectors where first is sequence
                         // checksum

  int maxLength = 0; // used by quality calcs

  boolean seqNumsChanged = false; // updated after any change via calcSeqNum;

  /*
   * a map per column with {property, conservation} where conservation value is
   * 1 (property is conserved), 0 (absence of property is conserved) or -1
   * (property is not conserved i.e. column has residues with and without it)
   */
  Map<String, Integer>[] total;

  boolean canonicaliseAa = true; // if true then conservation calculation will

  // map all symbols to canonical aa numbering
  // rather than consider conservation of that
  // symbol

  /** Stores calculated quality values */
  private Vector<Double> quality;

  /** Stores maximum and minimum values of quality values */
  private double[] qualityRange = new double[2];

  private Sequence consSequence;

  /*
   * percentage of residues in a column to qualify for counting conservation
   */
  private int threshold;

  private String name = "";

  private int[][] cons2;

  private String[] consSymbs;

  /**
   * Constructor using default threshold of 3%
   * 
   * @param name
   *          Name of conservation
   * @param sequences
   *          sequences to be used in calculation
   * @param start
   *          start residue position
   * @param end
   *          end residue position
   */
  public Conservation(String name, List<SequenceI> sequences, int start,
          int end)
  {
    this(name, THRESHOLD_PERCENT, sequences, start, end);
  }

  /**
   * Constructor
   * 
   * @param name
   *          Name of conservation
   * @param threshold
   *          percentage of sequences at or below which property conservation is
   *          ignored
   * @param sequences
   *          sequences to be used in calculation
   * @param start
   *          start column position
   * @param end
   *          end column position
   */
  public Conservation(String name, int threshold,
          List<SequenceI> sequences, int start, int end)
  {
    this.name = name;
    this.threshold = threshold;
    this.start = start;
    this.end = end;

    maxLength = end - start + 1; // default width includes bounds of
    // calculation

    int s, sSize = sequences.size();
    SequenceI[] sarray = new SequenceI[sSize];
    this.sequences = sarray;
    try
    {
      for (s = 0; s < sSize; s++)
      {
        sarray[s] = sequences.get(s);
        if (sarray[s].getLength() > maxLength)
        {
          maxLength = sarray[s].getLength();
        }
      }
    } catch (ArrayIndexOutOfBoundsException ex)
    {
      // bail - another thread has modified the sequence array, so the current
      // calculation is probably invalid.
      this.sequences = new SequenceI[0];
      maxLength = 0;
    }
  }

  /**
   * Translate sequence i into a numerical representation and store it in the
   * i'th position of the seqNums array.
   * 
   * @param i
   */
  private void calcSeqNum(int i)
  {
    String sq = null; // for dumb jbuilder not-inited exception warning
    int[] sqnum = null;

    int sSize = sequences.length;

    if ((i > -1) && (i < sSize))
    {
      sq = sequences[i].getSequenceAsString();

      if (seqNums.size() <= i)
      {
        seqNums.addElement(new int[sq.length() + 1]);
      }

      if (sq.hashCode() != seqNums.elementAt(i)[0])
      {
        int j;
        int len;
        seqNumsChanged = true;
        len = sq.length();

        if (maxLength < len)
        {
          maxLength = len;
        }

        sqnum = new int[len + 1]; // better to always make a new array -
        // sequence can change its length
        sqnum[0] = sq.hashCode();

        for (j = 1; j <= len; j++)
        {
          sqnum[j] = jalview.schemes.ResidueProperties.aaIndex[sq
                  .charAt(j - 1)];
        }

        seqNums.setElementAt(sqnum, i);
      }
      else
      {
        System.out.println("SEQUENCE HAS BEEN DELETED!!!");
      }
    }
    else
    {
      // JBPNote INFO level debug
      System.err
              .println("ERROR: calcSeqNum called with out of range sequence index for Alignment\n");
    }
  }

  /**
   * Calculates the conservation values for given set of sequences
   */
  public void calculate()
  {
    int height = sequences.length;

    total = new Map[maxLength];

    for (int column = start; column <= end; column++)
    {
      ResidueCount values = countResidues(column);

      /*
       * percentage count at or below which we ignore residues
       */
      int thresh = (threshold * height) / 100;

      /*
       * check observed residues in column and record whether each 
       * physico-chemical property is conserved (+1), absence conserved (0),
       * or not conserved (-1)
       * Using TreeMap means properties are displayed in alphabetical order
       */
      Map<String, Integer> resultHash = new TreeMap<String, Integer>();
      SymbolCounts symbolCounts = values.getSymbolCounts();
      char[] symbols = symbolCounts.symbols;
      int[] counts = symbolCounts.values;
      for (int j = 0; j < symbols.length; j++)
      {
        char c = symbols[j];
        if (counts[j] > thresh)
        {
          recordConservation(resultHash, String.valueOf(c));
        }
      }
      if (values.getGapCount() > thresh)
      {
        recordConservation(resultHash, "-");
      }

      if (total.length > 0)
      {
        total[column - start] = resultHash;
      }
    }
  }

  /**
   * Updates the conservation results for an observed residue
   * 
   * @param resultMap
   *          a map of {property, conservation} where conservation value is +1
   *          (all residues have the property), 0 (no residue has the property)
   *          or -1 (some do, some don't)
   * @param res
   */
  protected static void recordConservation(Map<String, Integer> resultMap,
          String res)
  {
    res = res.toUpperCase();
    for (Entry<String, Map<String, Integer>> property : ResidueProperties.propHash
            .entrySet())
    {
      String propertyName = property.getKey();
      Integer residuePropertyValue = property.getValue().get(res);

      if (!resultMap.containsKey(propertyName))
      {
        /*
         * first time we've seen this residue - note whether it has this property
         */
        if (residuePropertyValue != null)
        {
          resultMap.put(propertyName, residuePropertyValue);
        }
        else
        {
          /*
           * unrecognised residue - use default value for property
           */
          resultMap.put(propertyName, property.getValue().get("-"));
        }
      }
      else
      {
        Integer currentResult = resultMap.get(propertyName);
        if (currentResult.intValue() != -1
                && !currentResult.equals(residuePropertyValue))
        {
          /*
           * property is unconserved - residues seen both with and without it
           */
          resultMap.put(propertyName, Integer.valueOf(-1));
        }
      }
    }
  }

  /**
   * Counts residues (upper-cased) and gaps in the given column
   * 
   * @param column
   * @return
   */
  protected ResidueCount countResidues(int column)
  {
    ResidueCount values = new ResidueCount(false);

    for (int row = 0; row < sequences.length; row++)
    {
      if (sequences[row].getLength() > column)
      {
        char c = sequences[row].getCharAt(column);
        if (canonicaliseAa)
        {
          int index = ResidueProperties.aaIndex[c];
          c = index > 20 ? '-' : ResidueProperties.aa[index].charAt(0);
        }
        else
        {
          c = toUpperCase(c);
        }
        if (Comparison.isGap(c))
        {
          values.addGap();
        }
        else
        {
          values.add(c);
        }
      }
      else
      {
        values.addGap();
      }
    }
    return values;
  }

  /**
   * Counts conservation and gaps for a column of the alignment
   * 
   * @return { 1 if fully conserved, else 0, gap count }
   */
  public int[] countConservationAndGaps(int column)
  {
    int gapCount = 0;
    boolean fullyConserved = true;
    int iSize = sequences.length;

    if (iSize == 0)
    {
      return new int[] { 0, 0 };
    }

    char lastRes = '0';
    for (int i = 0; i < iSize; i++)
    {
      if (column >= sequences[i].getLength())
      {
        gapCount++;
        continue;
      }

      char c = sequences[i].getCharAt(column); // gaps do not have upper/lower case

      if (Comparison.isGap((c)))
      {
        gapCount++;
      }
      else
      {
        c = toUpperCase(c);
        if (lastRes == '0')
        {
          lastRes = c;
        }
        if (c != lastRes)
        {
          fullyConserved = false;
        }
      }
    }

    int[] r = new int[] { fullyConserved ? 1 : 0, gapCount };
    return r;
  }

  /**
   * Returns the upper-cased character if between 'a' and 'z', else the
   * unchanged value
   * 
   * @param c
   * @return
   */
  char toUpperCase(char c)
  {
    if ('a' <= c && c <= 'z')
    {
      c -= TOUPPERCASE;
    }
    return c;
  }

  /**
   * Calculates the conservation sequence
   * 
   * @param positiveOnly
   *          if true, calculate positive conservation; else calculate both
   *          positive and negative conservation
   * @param maxPercentageGaps
   *          the percentage of gaps in a column, at or above which no
   *          conservation is asserted
   */
  public void verdict(boolean positiveOnly, float maxPercentageGaps)
  {
    // TODO call this at the end of calculate(), should not be a public method

    StringBuilder consString = new StringBuilder(end);

    // NOTE THIS SHOULD CHECK IF THE CONSEQUENCE ALREADY
    // EXISTS AND NOT OVERWRITE WITH '-', BUT THIS CASE
    // DOES NOT EXIST IN JALVIEW 2.1.2
    for (int i = 0; i < start; i++)
    {
      consString.append('-');
    }
    consSymbs = new String[end - start + 1];
    for (int i = start; i <= end; i++)
    {
      int[] gapcons = countConservationAndGaps(i);
      boolean fullyConserved = gapcons[0] == 1;
      int totGaps = gapcons[1];
      float pgaps = (totGaps * 100f) / sequences.length;

      if (maxPercentageGaps > pgaps)
      {
        Map<String, Integer> resultHash = total[i - start];
        int count = 0;
        StringBuilder positives = new StringBuilder(64);
        StringBuilder negatives = new StringBuilder(32);
        for (String type : resultHash.keySet())
        {
          int result = resultHash.get(type).intValue();
          if (result == -1)
          {
            /*
             * not conserved (present or absent)
             */
            continue;
          }
          count++;
          if (result == 1)
          {
            /*
             * positively conserved property (all residues have it)
             */
            positives.append(positives.length() == 0 ? "" : " ");
            positives.append(type);
          }
          if (result == 0 && !positiveOnly)
          {
            /*
             * absense of property is conserved (all residues lack it)
             */
            negatives.append(negatives.length() == 0 ? "" : " ");
            negatives.append("!").append(type);
          }
        }
        if (negatives.length() > 0)
        {
          positives.append(" ").append(negatives);
        }
        consSymbs[i - start] = positives.toString();

        if (count < 10)
        {
          consString.append(count); // Conserved props!=Identity
        }
        else
        {
          consString.append(fullyConserved ? "*" : "+");
        }
      }
      else
      {
        consString.append('-');
      }
    }

    consSequence = new Sequence(name, consString.toString(), start, end);
  }

  /**
   * 
   * 
   * @return Conservation sequence
   */
  public Sequence getConsSequence()
  {
    return consSequence;
  }

  // From Alignment.java in jalview118
  public void findQuality()
  {
    findQuality(0, maxLength - 1);
  }

  /**
   * DOCUMENT ME!
   */
  private void percentIdentity2()
  {
    seqNums = new Vector<int[]>();
    // calcSeqNum(s);
    int i = 0, iSize = sequences.length;
    // Do we need to calculate this again?
    for (i = 0; i < iSize; i++)
    {
      calcSeqNum(i);
    }

    if ((cons2 == null) || seqNumsChanged)
    {
      cons2 = new int[maxLength][24];

      // Initialize the array
      for (int j = 0; j < 24; j++)
      {
        for (i = 0; i < maxLength; i++)
        {
          cons2[i][j] = 0;
        }
      }

      int[] sqnum;
      int j = 0;

      while (j < sequences.length)
      {
        sqnum = seqNums.elementAt(j);

        for (i = 1; i < sqnum.length; i++)
        {
          cons2[i - 1][sqnum[i]]++;
        }

        for (i = sqnum.length - 1; i < maxLength; i++)
        {
          cons2[i][23]++; // gap count
        }

        j++;
      }

      // unnecessary ?

      /*
       * for (int i=start; i <= end; i++) { int max = -1000; int maxi = -1; int
       * maxj = -1;
       * 
       * for (int j=0;j<24;j++) { if (cons2[i][j] > max) { max = cons2[i][j];
       * maxi = i; maxj = j; } } }
       */
    }
  }

  /**
   * Calculates the quality of the set of sequences
   * 
   * @param startRes
   *          Start residue
   * @param endRes
   *          End residue
   */
  public void findQuality(int startRes, int endRes)
  {
    quality = new Vector<Double>();

    double max = -10000;
    int[][] BLOSUM62 = ResidueProperties.getBLOSUM62();

    // Loop over columns // JBPNote Profiling info
    // long ts = System.currentTimeMillis();
    // long te = System.currentTimeMillis();
    percentIdentity2();

    int size = seqNums.size();
    int[] lengths = new int[size];
    double tot, bigtot, sr, tmp;
    double[] x, xx;
    int l, j, i, ii, i2, k, seqNum;

    for (l = 0; l < size; l++)
    {
      lengths[l] = seqNums.elementAt(l).length - 1;
    }

    for (j = startRes; j <= endRes; j++)
    {
      bigtot = 0;

      // First Xr = depends on column only
      x = new double[24];

      for (ii = 0; ii < 24; ii++)
      {
        x[ii] = 0;

        for (i2 = 0; i2 < 24; i2++)
        {
          x[ii] += (((double) cons2[j][i2] * BLOSUM62[ii][i2]) + 4);
        }

        x[ii] /= size;
      }

      // Now calculate D for each position and sum
      for (k = 0; k < size; k++)
      {
        tot = 0;
        xx = new double[24];
        seqNum = (j < lengths[k]) ? seqNums.elementAt(k)[j + 1] : 23; // Sequence,
                                                                      // or gap
                                                                      // at the
                                                                      // end

        // This is a loop over r
        for (i = 0; i < 23; i++)
        {
          sr = 0;

          sr = (double) BLOSUM62[i][seqNum] + 4;

          // Calculate X with another loop over residues
          // System.out.println("Xi " + i + " " + x[i] + " " + sr);
          xx[i] = x[i] - sr;

          tot += (xx[i] * xx[i]);
        }

        bigtot += Math.sqrt(tot);
      }

      // This is the quality for one column
      if (max < bigtot)
      {
        max = bigtot;
      }

      // bigtot = bigtot * (size-cons2[j][23])/size;
      quality.addElement(new Double(bigtot));

      // Need to normalize by gaps
    }

    double newmax = -10000;

    for (j = startRes; j <= endRes; j++)
    {
      tmp = quality.elementAt(j).doubleValue();
      tmp = ((max - tmp) * (size - cons2[j][23])) / size;

      // System.out.println(tmp+ " " + j);
      quality.setElementAt(new Double(tmp), j);

      if (tmp > newmax)
      {
        newmax = tmp;
      }
    }

    // System.out.println("Quality " + s);
    qualityRange[0] = 0D;
    qualityRange[1] = newmax;
  }

  /**
   * Complete the given consensus and quuality annotation rows. Note: currently
   * this method will enlarge the given annotation row if it is too small,
   * otherwise will leave its length unchanged.
   * 
   * @param conservation
   *          conservation annotation row
   * @param quality2
   *          (optional - may be null)
   * @param istart
   *          first column for conservation
   * @param alWidth
   *          extent of conservation
   */
  public void completeAnnotations(AlignmentAnnotation conservation,
          AlignmentAnnotation quality2, int istart, int alWidth)
  {
    char[] sequence = getConsSequence().getSequence();
    float minR;
    float minG;
    float minB;
    float maxR;
    float maxG;
    float maxB;
    minR = 0.3f;
    minG = 0.0f;
    minB = 0f;
    maxR = 1.0f - minR;
    maxG = 0.9f - minG;
    maxB = 0f - minB; // scalable range for colouring both Conservation and
    // Quality

    float min = 0f;
    float max = 11f;
    float qmin = 0f;
    float qmax = 0f;

    char c;

    if (conservation != null && conservation.annotations != null
            && conservation.annotations.length < alWidth)
    {
      conservation.annotations = new Annotation[alWidth];
    }

    if (quality2 != null)
    {
      quality2.graphMax = (float) qualityRange[1];
      if (quality2.annotations != null
              && quality2.annotations.length < alWidth)
      {
        quality2.annotations = new Annotation[alWidth];
      }
      qmin = (float) qualityRange[0];
      qmax = (float) qualityRange[1];
    }

    for (int i = istart; i < alWidth; i++)
    {
      float value = 0;

      c = sequence[i];

      if (Character.isDigit(c))
      {
        value = c - '0';
      }
      else if (c == '*')
      {
        value = 11;
      }
      else if (c == '+')
      {
        value = 10;
      }

      if (conservation != null)
      {
        float vprop = value - min;
        vprop /= max;
        int consp = i - start;
        String conssym = (value > 0 && consp > -1 && consp < consSymbs.length) ? consSymbs[consp]
                : "";
        conservation.annotations[i] = new Annotation(String.valueOf(c),
                conssym, ' ', value, new Color(minR + (maxR * vprop), minG
                        + (maxG * vprop), minB + (maxB * vprop)));
      }

      // Quality calc
      if (quality2 != null)
      {
        value = quality.elementAt(i).floatValue();
        float vprop = value - qmin;
        vprop /= qmax;
        quality2.annotations[i] = new Annotation(" ",
                String.valueOf(value), ' ', value, new Color(minR
                        + (maxR * vprop), minG + (maxG * vprop), minB
                        + (maxB * vprop)));
      }
    }
  }

  /**
   * construct and call the calculation methods on a new Conservation object
   * 
   * @param name
   *          - name of conservation
   * @param seqs
   * @param start
   *          first column in calculation window
   * @param end
   *          last column in calculation window
   * @param positiveOnly
   *          calculate positive (true) or positive and negative (false)
   *          conservation
   * @param maxPercentGaps
   *          percentage of gaps tolerated in column
   * @param calcQuality
   *          flag indicating if alignment quality should be calculated
   * @return Conservation object ready for use in visualization
   */
  public static Conservation calculateConservation(String name,
          List<SequenceI> seqs, int start, int end, boolean positiveOnly,
          int maxPercentGaps, boolean calcQuality)
  {
    Conservation cons = new Conservation(name, seqs, start, end);
    cons.calculate();
    cons.verdict(positiveOnly, maxPercentGaps);

    if (calcQuality)
    {
      cons.findQuality();
    }

    return cons;
  }

  /**
   * Returns the computed tooltip (annotation description) for a given column.
   * The tip is empty if the conservation score is zero, otherwise holds the
   * conserved properties (and, optionally, properties whose absence is
   * conserved).
   * 
   * @param column
   * @return
   */
  String getTooltip(int column)
  {
    char[] sequence = getConsSequence().getSequence();
    char val = column < sequence.length ? sequence[column] : '-';
    boolean hasConservation = val != '-' && val != '0';
    int consp = column - start;
    String tip = (hasConservation && consp > -1 && consp < consSymbs.length) ? consSymbs[consp]
            : "";
    return tip;
  }
}
