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
package jalview.analysis;

import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceI;
import jalview.util.Format;
import jalview.util.MappingUtils;
import jalview.util.QuickSort;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Takes in a vector or array of sequences and column start and column end and
 * returns a new Hashtable[] of size maxSeqLength, if Hashtable not supplied.
 * This class is used extensively in calculating alignment colourschemes that
 * depend on the amount of conservation in each alignment column.
 * 
 * @author $author$
 * @version $Revision$
 */
public class AAFrequency
{
  private static final int TO_UPPER_CASE = 'A' - 'a'; // -32

  public static final String MAXCOUNT = "C";

  public static final String MAXRESIDUE = "R";

  public static final String PID_GAPS = "G";

  public static final String PID_NOGAPS = "N";

  public static final String PROFILE = "P";

  public static final String ENCODED_CHARS = "E";

  /*
   * Quick look-up of String value of char 'A' to 'Z'
   */
  private static final String[] CHARS = new String['Z' - 'A' + 1];

  static
  {
    for (char c = 'A'; c <= 'Z'; c++)
    {
      CHARS[c - 'A'] = String.valueOf(c);
    }
  }

  public static final Hashtable[] calculate(List<SequenceI> list,
          int start, int end)
  {
    return calculate(list, start, end, false);
  }

  public static final Hashtable[] calculate(List<SequenceI> sequences,
          int start, int end, boolean profile)
  {
    SequenceI[] seqs = new SequenceI[sequences.size()];
    int width = 0;
    synchronized (sequences)
    {
      for (int i = 0; i < sequences.size(); i++)
      {
        seqs[i] = sequences.get(i);
        if (seqs[i].getLength() > width)
        {
          width = seqs[i].getLength();
        }
      }

      Hashtable[] reply = new Hashtable[width];

      if (end >= width)
      {
        end = width;
      }

      calculate(seqs, start, end, reply, profile);
      return reply;
    }
  }

  public static final void calculate(SequenceI[] sequences, int start,
          int end, Hashtable[] result, boolean profile)
  {
    Hashtable residueHash;
    int maxCount, nongap, i, j, v;
    int jSize = sequences.length;
    String maxResidue;
    char c = '-';
    float percentage;

    int[] values = new int[255];

    char[] seq;

    for (i = start; i < end; i++)
    {
      residueHash = new Hashtable();
      maxCount = 0;
      maxResidue = "";
      nongap = 0;
      values = new int[255];

      for (j = 0; j < jSize; j++)
      {
        if (sequences[j] == null)
        {
          System.err
                  .println("WARNING: Consensus skipping null sequence - possible race condition.");
          continue;
        }
        seq = sequences[j].getSequence();
        if (seq.length > i)
        {
          c = seq[i];

          if (c == '.' || c == ' ')
          {
            c = '-';
          }

          if (c == '-')
          {
            values['-']++;
            continue;
          }
          else if ('a' <= c && c <= 'z')
          {
            c += TO_UPPER_CASE;
          }

          nongap++;
          values[c]++;

        }
        else
        {
          values['-']++;
        }
      }
      if (jSize == 1)
      {
        maxResidue = String.valueOf(c);
        maxCount = 1;
      }
      else
      {
        for (v = 'A'; v <= 'Z'; v++)
        {
          // TODO why ignore values[v] == 1?
          if (values[v] < 1 /* 2 */|| values[v] < maxCount)
          {
            continue;
          }

          if (values[v] > maxCount)
          {
            maxResidue = CHARS[v - 'A'];
          }
          else if (values[v] == maxCount)
          {
            maxResidue += CHARS[v - 'A'];
          }
          maxCount = values[v];
        }
      }
      if (maxResidue.length() == 0)
      {
        maxResidue = "-";
      }
      if (profile)
      {
        // TODO use a 1-dimensional array with jSize, nongap in [0] and [1]
        residueHash.put(PROFILE, new int[][] { values,
            new int[] { jSize, nongap } });
      }
      residueHash.put(MAXCOUNT, new Integer(maxCount));
      residueHash.put(MAXRESIDUE, maxResidue);

      percentage = ((float) maxCount * 100) / jSize;
      residueHash.put(PID_GAPS, new Float(percentage));

      if (nongap > 0)
      {
        // calculate for non-gapped too
        percentage = ((float) maxCount * 100) / nongap;
      }
      residueHash.put(PID_NOGAPS, new Float(percentage));

      result[i] = residueHash;
    }
  }

  /**
   * Compute all or part of the annotation row from the given consensus
   * hashtable
   * 
   * @param consensus
   *          - pre-allocated annotation row
   * @param hconsensus
   * @param iStart
   * @param width
   * @param ignoreGapsInConsensusCalculation
   * @param includeAllConsSymbols
   * @param nseq
   */
  public static void completeConsensus(AlignmentAnnotation consensus,
          Hashtable[] hconsensus, int iStart, int width,
          boolean ignoreGapsInConsensusCalculation,
          boolean includeAllConsSymbols, long nseq)
  {
    completeConsensus(consensus, hconsensus, iStart, width,
            ignoreGapsInConsensusCalculation, includeAllConsSymbols, null,
            nseq);
  }

  /**
   * Derive the consensus annotations to be added to the alignment for display.
   * This does not recompute the raw data, but may be called on a change in
   * display options, such as 'show logo', which may in turn result in a change
   * in the derived values.
   * 
   * @param consensus
   *          the annotation row to add annotations to
   * @param hconsensus
   *          the source consensus data
   * @param iStart
   *          start column
   * @param width
   *          end column
   * @param ignoreGapsInConsensusCalculation
   *          if true, use the consensus calculated ignoring gaps
   * @param includeAllConsSymbols
   *          if true include all consensus symbols, else just show modal
   *          residue
   * @param alphabet
   * @param nseq
   *          number of sequences
   */
  public static void completeConsensus(AlignmentAnnotation consensus,
          Hashtable[] hconsensus, int iStart, int width,
          boolean ignoreGapsInConsensusCalculation,
          boolean includeAllConsSymbols, char[] alphabet, long nseq)
  {
    if (consensus == null || consensus.annotations == null
            || consensus.annotations.length < width)
    {
      // called with a bad alignment annotation row - wait for it to be
      // initialised properly
      return;
    }

    final Format fmt = getPercentageFormat(nseq);

    for (int i = iStart; i < width; i++)
    {
      Hashtable hci;
      if (i >= hconsensus.length || ((hci = hconsensus[i]) == null))
      {
        // happens if sequences calculated over were shorter than alignment
        // width
        consensus.annotations[i] = null;
        continue;
      }
      Float fv = (Float) hci
              .get(ignoreGapsInConsensusCalculation ? PID_NOGAPS : PID_GAPS);
      if (fv == null)
      {
        consensus.annotations[i] = null;
        // data has changed below us .. give up and
        continue;
      }
      float value = fv.floatValue();
      String maxRes = hci.get(AAFrequency.MAXRESIDUE).toString();
      StringBuilder mouseOver = new StringBuilder(64);
      if (maxRes.length() > 1)
      {
        mouseOver.append("[").append(maxRes).append("] ");
        maxRes = "+";
      }
      else
      {
        mouseOver.append(hci.get(AAFrequency.MAXRESIDUE) + " ");
      }
      int[][] profile = (int[][]) hci.get(AAFrequency.PROFILE);
      if (profile != null && includeAllConsSymbols)
      {
        int sequenceCount = profile[1][0];
        int nonGappedCount = profile[1][1];
        int normalisedBy = ignoreGapsInConsensusCalculation ? nonGappedCount
                : sequenceCount;
        mouseOver.setLength(0);
        if (alphabet != null)
        {
          for (int c = 0; c < alphabet.length; c++)
          {
            float tval = profile[0][alphabet[c]] * 100f / normalisedBy;
            mouseOver
                    .append(((c == 0) ? "" : "; "))
                    .append(alphabet[c])
                    .append(" ")
                    .append(((fmt != null) ? fmt.form(tval) : ((int) tval)))
                    .append("%");
          }
        }
        else
        {
          // TODO do this sort once only in calculate()?
          // char[][] ca = new char[profile[0].length][];
          char[] ca = new char[profile[0].length];
          float[] vl = new float[profile[0].length];
          for (int c = 0; c < ca.length; c++)
          {
            ca[c] = (char) c;
            // ca[c] = new char[]
            // { (char) c };
            vl[c] = profile[0][c];
          }
          QuickSort.sort(vl, ca);
          for (int p = 0, c = ca.length - 1; profile[0][ca[c]] > 0; c--)
          {
            final char residue = ca[c];
            if (residue != '-')
            {
              float tval = profile[0][residue] * 100f / normalisedBy;
              mouseOver
                      .append((((p == 0) ? "" : "; ")))
                      .append(residue)
                      .append(" ")
                      .append(((fmt != null) ? fmt.form(tval)
                              : ((int) tval))).append("%");
              p++;
            }
          }
        }
      }
      else
      {
        mouseOver.append(
                (((fmt != null) ? fmt.form(value) : ((int) value))))
                .append("%");
      }
      consensus.annotations[i] = new Annotation(maxRes,
              mouseOver.toString(), ' ', value);
    }
  }

  /**
   * Returns a Format designed to show all significant figures for profile
   * percentages. For less than 100 sequences, returns null (the integer
   * percentage value will be displayed). For 100-999 sequences, returns "%3.1f"
   * 
   * @param nseq
   * @return
   */
  protected static Format getPercentageFormat(long nseq)
  {
    int scale = 0;
    while (nseq >= 10)
    {
      scale++;
      nseq /= 10;
    }
    return scale <= 1 ? null : new Format("%3." + (scale - 1) + "f");
  }

  /**
   * Returns the sorted profile for the given consensus data. The returned array
   * contains
   * 
   * <pre>
   *    [profileType, numberOfValues, nonGapCount, charValue1, percentage1, charValue2, percentage2, ...]
   * in descending order of percentage value
   * </pre>
   * 
   * @param hconsensus
   *          the data table from which to extract and sort values
   * @param ignoreGaps
   *          if true, only non-gapped values are included in percentage
   *          calculations
   * @return
   */
  public static int[] extractProfile(Hashtable hconsensus,
          boolean ignoreGaps)
  {
    int[] rtnval = new int[64];
    int[][] profile = (int[][]) hconsensus.get(AAFrequency.PROFILE);
    if (profile == null)
    {
      return null;
    }
    char[] ca = new char[profile[0].length];
    float[] vl = new float[profile[0].length];
    for (int c = 0; c < ca.length; c++)
    {
      ca[c] = (char) c;
      vl[c] = profile[0][c];
    }
    QuickSort.sort(vl, ca);
    int nextArrayPos = 2;
    int totalPercentage = 0;
    int distinctValuesCount = 0;
    final int divisor = profile[1][ignoreGaps ? 1 : 0];
    for (int c = ca.length - 1; profile[0][ca[c]] > 0; c--)
    {
      if (ca[c] != '-')
      {
        rtnval[nextArrayPos++] = ca[c];
        final int percentage = (int) (profile[0][ca[c]] * 100f / divisor);
        rtnval[nextArrayPos++] = percentage;
        totalPercentage += percentage;
        distinctValuesCount++;
      }
    }
    rtnval[0] = distinctValuesCount;
    rtnval[1] = totalPercentage;
    int[] result = new int[rtnval.length + 1];
    result[0] = AlignmentAnnotation.SEQUENCE_PROFILE;
    System.arraycopy(rtnval, 0, result, 1, rtnval.length);

    return result;
  }

  /**
   * Extract a sorted extract of cDNA codon profile data. The returned array
   * contains
   * 
   * <pre>
   *    [profileType, numberOfValues, totalCount, charValue1, percentage1, charValue2, percentage2, ...]
   * in descending order of percentage value, where the character values encode codon triplets
   * </pre>
   * 
   * @param hashtable
   * @return
   */
  public static int[] extractCdnaProfile(Hashtable hashtable,
          boolean ignoreGaps)
  {
    // this holds #seqs, #ungapped, and then codon count, indexed by encoded
    // codon triplet
    int[] codonCounts = (int[]) hashtable.get(PROFILE);
    int[] sortedCounts = new int[codonCounts.length - 2];
    System.arraycopy(codonCounts, 2, sortedCounts, 0,
            codonCounts.length - 2);

    int[] result = new int[3 + 2 * sortedCounts.length];
    // first value is just the type of profile data
    result[0] = AlignmentAnnotation.CDNA_PROFILE;

    char[] codons = new char[sortedCounts.length];
    for (int i = 0; i < codons.length; i++)
    {
      codons[i] = (char) i;
    }
    QuickSort.sort(sortedCounts, codons);
    int totalPercentage = 0;
    int distinctValuesCount = 0;
    int j = 3;
    int divisor = ignoreGaps ? codonCounts[1] : codonCounts[0];
    for (int i = codons.length - 1; i >= 0; i--)
    {
      final int codonCount = sortedCounts[i];
      if (codonCount == 0)
      {
        break; // nothing else of interest here
      }
      distinctValuesCount++;
      result[j++] = codons[i];
      final int percentage = codonCount * 100 / divisor;
      result[j++] = percentage;
      totalPercentage += percentage;
    }
    result[2] = totalPercentage;

    /*
     * Just return the non-zero values
     */
    // todo next value is redundant if we limit the array to non-zero counts
    result[1] = distinctValuesCount;
    return Arrays.copyOfRange(result, 0, j);
  }

  /**
   * Compute a consensus for the cDNA coding for a protein alignment.
   * 
   * @param alignment
   *          the protein alignment (which should hold mappings to cDNA
   *          sequences)
   * @param hconsensus
   *          the consensus data stores to be populated (one per column)
   */
  public static void calculateCdna(AlignmentI alignment,
          Hashtable[] hconsensus)
  {
    final char gapCharacter = alignment.getGapCharacter();
    Set<AlignedCodonFrame> mappings = alignment.getCodonFrames();
    if (mappings == null || mappings.isEmpty())
    {
      return;
    }

    int cols = alignment.getWidth();
    for (int col = 0; col < cols; col++)
    {
      // todo would prefer a Java bean for consensus data
      Hashtable<String, int[]> columnHash = new Hashtable<String, int[]>();
      // #seqs, #ungapped seqs, counts indexed by (codon encoded + 1)
      int[] codonCounts = new int[66];
      codonCounts[0] = alignment.getSequences().size();
      int ungappedCount = 0;
      for (SequenceI seq : alignment.getSequences())
      {
        if (seq.getCharAt(col) == gapCharacter)
        {
          continue;
        }
        char[] codon = MappingUtils.findCodonFor(seq, col, mappings);
        int codonEncoded = CodingUtils.encodeCodon(codon);
        if (codonEncoded >= 0)
        {
          codonCounts[codonEncoded + 2]++;
          ungappedCount++;
        }
      }
      codonCounts[1] = ungappedCount;
      // todo: sort values here, save counts and codons?
      columnHash.put(PROFILE, codonCounts);
      hconsensus[col] = columnHash;
    }
  }

  /**
   * Derive displayable cDNA consensus annotation from computed consensus data.
   * 
   * @param consensusAnnotation
   *          the annotation row to be populated for display
   * @param consensusData
   *          the computed consensus data
   * @param showProfileLogo
   *          if true show all symbols present at each position, else only the
   *          modal value
   * @param nseqs
   *          the number of sequences in the alignment
   */
  public static void completeCdnaConsensus(
          AlignmentAnnotation consensusAnnotation,
          Hashtable[] consensusData, boolean showProfileLogo, int nseqs)
  {
    if (consensusAnnotation == null
            || consensusAnnotation.annotations == null
            || consensusAnnotation.annotations.length < consensusData.length)
    {
      // called with a bad alignment annotation row - wait for it to be
      // initialised properly
      return;
    }

    // ensure codon triplet scales with font size
    consensusAnnotation.scaleColLabel = true;
    for (int col = 0; col < consensusData.length; col++)
    {
      Hashtable hci = consensusData[col];
      if (hci == null)
      {
        // gapped protein column?
        continue;
      }
      // array holds #seqs, #ungapped, then codon counts indexed by codon
      final int[] codonCounts = (int[]) hci.get(PROFILE);
      int totalCount = 0;

      /*
       * First pass - get total count and find the highest
       */
      final char[] codons = new char[codonCounts.length - 2];
      for (int j = 2; j < codonCounts.length; j++)
      {
        final int codonCount = codonCounts[j];
        codons[j - 2] = (char) (j - 2);
        totalCount += codonCount;
      }

      /*
       * Sort array of encoded codons by count ascending - so the modal value
       * goes to the end; start by copying the count (dropping the first value)
       */
      int[] sortedCodonCounts = new int[codonCounts.length - 2];
      System.arraycopy(codonCounts, 2, sortedCodonCounts, 0,
              codonCounts.length - 2);
      QuickSort.sort(sortedCodonCounts, codons);

      int modalCodonEncoded = codons[codons.length - 1];
      int modalCodonCount = sortedCodonCounts[codons.length - 1];
      String modalCodon = String.valueOf(CodingUtils
              .decodeCodon(modalCodonEncoded));
      if (sortedCodonCounts.length > 1
              && sortedCodonCounts[codons.length - 2] == modalCodonEncoded)
      {
        modalCodon = "+";
      }
      float pid = sortedCodonCounts[sortedCodonCounts.length - 1] * 100
              / (float) totalCount;

      /*
       * todo ? Replace consensus hashtable with sorted arrays of codons and
       * counts (non-zero only). Include total count in count array [0].
       */

      /*
       * Scan sorted array backwards for most frequent values first. Show
       * repeated values compactly.
       */
      StringBuilder mouseOver = new StringBuilder(32);
      StringBuilder samePercent = new StringBuilder();
      String percent = null;
      String lastPercent = null;
      Format fmt = getPercentageFormat(nseqs);

      for (int j = codons.length - 1; j >= 0; j--)
      {
        int codonCount = sortedCodonCounts[j];
        if (codonCount == 0)
        {
          /*
           * remaining codons are 0% - ignore, but finish off the last one if
           * necessary
           */
          if (samePercent.length() > 0)
          {
            mouseOver.append(samePercent).append(": ").append(percent)
                    .append("% ");
          }
          break;
        }
        int codonEncoded = codons[j];
        final int pct = codonCount * 100 / totalCount;
        String codon = String
                .valueOf(CodingUtils.decodeCodon(codonEncoded));
        percent = fmt == null ? Integer.toString(pct) : fmt.form(pct);
        if (showProfileLogo || codonCount == modalCodonCount)
        {
          if (percent.equals(lastPercent) && j > 0)
          {
            samePercent.append(samePercent.length() == 0 ? "" : ", ");
            samePercent.append(codon);
          }
          else
          {
            if (samePercent.length() > 0)
            {
              mouseOver.append(samePercent).append(": ")
                      .append(lastPercent).append("% ");
            }
            samePercent.setLength(0);
            samePercent.append(codon);
          }
          lastPercent = percent;
        }
      }

      consensusAnnotation.annotations[col] = new Annotation(modalCodon,
              mouseOver.toString(), ' ', pid);
    }
  }
}
