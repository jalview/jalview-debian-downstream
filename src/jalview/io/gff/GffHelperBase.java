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
package jalview.io.gff;

import jalview.analysis.SequenceIdMatcher;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.MappingType;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.MapList;
import jalview.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class with common functionality for flavours of GFF handler (GFF2 or
 * GFF3)
 */
public abstract class GffHelperBase implements GffHelperI
{
  private static final String NOTE = "Note";

  /*
   * GFF columns 1-9 (zero-indexed):
   */
  protected static final int SEQID_COL = 0;

  protected static final int SOURCE_COL = 1;

  protected static final int TYPE_COL = 2;

  protected static final int START_COL = 3;

  protected static final int END_COL = 4;

  protected static final int SCORE_COL = 5;

  protected static final int STRAND_COL = 6;

  protected static final int PHASE_COL = 7;

  protected static final int ATTRIBUTES_COL = 8;

  private AlignmentI lastmatchedAl = null;

  private SequenceIdMatcher matcher = null;

  /**
   * Constructs and returns a mapping, or null if data appear invalid
   * 
   * @param fromStart
   * @param fromEnd
   * @param toStart
   * @param toEnd
   * @param mappingType
   *          type of mapping (e.g. protein to nucleotide)
   * @return
   */
  protected MapList constructMappingFromAlign(int fromStart, int fromEnd,
          int toStart, int toEnd, MappingType mappingType)
  {
    int[] from = new int[] { fromStart, fromEnd };
    int[] to = new int[] { toStart, toEnd };

    /*
     * Jalview always models from dna to protein, so switch values if the
     * GFF mapping is from protein to dna
     */
    if (mappingType == MappingType.PeptideToNucleotide)
    {
      int[] temp = from;
      from = to;
      to = temp;
      mappingType = mappingType.getInverse();
    }

    int fromRatio = mappingType.getFromRatio();
    int toRatio = mappingType.getToRatio();

    /*
     * sanity check that mapped residue counts match
     * TODO understand why PASA generates such cases...
     */
    if (!trimMapping(from, to, fromRatio, toRatio))
    {
      System.err.println("Ignoring mapping from " + Arrays.toString(from)
              + " to " + Arrays.toString(to) + " as counts don't match!");
      return null;
    }

    /*
     * If a codon has an intron gap, there will be contiguous 'toRanges';
     * this is handled for us by the MapList constructor. 
     * (It is not clear that exonerate ever generates this case)  
     */

    return new MapList(from, to, fromRatio, toRatio);
  }

  /**
   * Checks that the 'from' and 'to' ranges have equivalent lengths. If not,
   * tries to trim the end of the longer so they do. Returns true if the
   * mappings could be made equivalent, else false. Note the range array values
   * may be modified by this method.
   * 
   * @param from
   * @param to
   * @param fromRatio
   * @param toRatio
   * @return
   */
  protected static boolean trimMapping(int[] from, int[] to, int fromRatio,
          int toRatio)
  {
    int fromLength = Math.abs(from[1] - from[0]) + 1;
    int toLength = Math.abs(to[1] - to[0]) + 1;
    int fromOverlap = fromLength * toRatio - toLength * fromRatio;
    if (fromOverlap == 0)
    {
      return true;
    }
    if (fromOverlap > 0 && fromOverlap % toRatio == 0)
    {
      /*
       * restrict from range to make them match up
       * it's kind of arbitrary which end we truncate - here it is the end
       */
      System.err.print("Truncating mapping from " + Arrays.toString(from)
              + " to ");
      if (from[1] > from[0])
      {
        from[1] -= fromOverlap / toRatio;
      }
      else
      {
        from[1] += fromOverlap / toRatio;
      }
      System.err.println(Arrays.toString(from));
      return true;
    }
    else if (fromOverlap < 0 && fromOverlap % fromRatio == 0)
    {
      fromOverlap = -fromOverlap; // > 0
      /*
       * restrict to range to make them match up
       */
      System.err.print("Truncating mapping to " + Arrays.toString(to)
              + " to ");
      if (to[1] > to[0])
      {
        to[1] -= fromOverlap / fromRatio;
      }
      else
      {
        to[1] += fromOverlap / fromRatio;
      }
      System.err.println(Arrays.toString(to));
      return true;
    }

    /*
     * Couldn't truncate to an exact match..
     */
    return false;
  }

  /**
   * Returns a sequence matching the given id, as follows
   * <ul>
   * <li>strict matching is on exact sequence name</li>
   * <li>relaxed matching allows matching on a token within the sequence name,
   * or a dbxref</li>
   * <li>first tries to find a match in the alignment sequences</li>
   * <li>else tries to find a match in the new sequences already generated while
   * parsing the features file</li>
   * <li>else creates a new placeholder sequence, adds it to the new sequences
   * list, and returns it</li>
   * </ul>
   * 
   * @param seqId
   * @param align
   * @param newseqs
   * @param relaxedIdMatching
   * 
   * @return
   */
  protected SequenceI findSequence(String seqId, AlignmentI align,
          List<SequenceI> newseqs, boolean relaxedIdMatching)
  {
    if (seqId == null)
    {
      return null;
    }
    SequenceI match = null;
    if (relaxedIdMatching)
    {
      if (lastmatchedAl != align)
      {
        lastmatchedAl = align;
        matcher = new SequenceIdMatcher(align.getSequencesArray());
        if (newseqs != null)
        {
          matcher.addAll(newseqs);
        }
      }
      match = matcher.findIdMatch(seqId);
    }
    else
    {
      match = align.findName(seqId, true);
      if (match == null && newseqs != null)
      {
        for (SequenceI m : newseqs)
        {
          if (seqId.equals(m.getName()))
          {
            return m;
          }
        }
      }

    }
    if (match == null && newseqs != null)
    {
      match = new SequenceDummy(seqId);
      if (relaxedIdMatching)
      {
        matcher.addAll(Arrays.asList(new SequenceI[] { match }));
      }
      // add dummy sequence to the newseqs list
      newseqs.add(match);
    }
    return match;
  }

  /**
   * Parses the input line to a map of name / value(s) pairs. For example the
   * line <br>
   * Notes=Fe-S;Method=manual curation, prediction; source = Pfam; Notes = Metal <br>
   * if parsed with delimiter=";" and separators {' ', '='} <br>
   * would return a map with { Notes={Fe=S, Metal}, Method={manual curation,
   * prediction}, source={Pfam}} <br>
   * 
   * This method supports parsing of either GFF2 format (which uses space ' ' as
   * the name/value delimiter, and allows multiple occurrences of the same
   * name), or GFF3 format (which uses '=' as the name/value delimiter, and
   * strictly does not allow repeat occurrences of the same name - but does
   * allow a comma-separated list of values).
   * 
   * @param text
   * @param namesDelimiter
   *          the major delimiter between name-value pairs
   * @param nameValueSeparator
   *          one or more separators used between name and value
   * @param valuesDelimiter
   *          delimits a list of more than one value
   * @return the name-values map (which may be empty but never null)
   */
  public static Map<String, List<String>> parseNameValuePairs(String text,
          String namesDelimiter, char nameValueSeparator,
          String valuesDelimiter)
  {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    if (text == null || text.trim().length() == 0)
    {
      return map;
    }

    for (String pair : text.trim().split(namesDelimiter))
    {
      pair = pair.trim();
      if (pair.length() == 0)
      {
        continue;
      }

      int sepPos = pair.indexOf(nameValueSeparator);
      if (sepPos == -1)
      {
        // no name=value present
        continue;
      }

      String key = pair.substring(0, sepPos).trim();
      String values = pair.substring(sepPos + 1).trim();
      if (values.length() > 0)
      {
        List<String> vals = map.get(key);
        if (vals == null)
        {
          vals = new ArrayList<String>();
          map.put(key, vals);
        }
        for (String val : values.split(valuesDelimiter))
        {
          vals.add(val);
        }
      }
    }
    return map;
  }

  /**
   * Constructs a SequenceFeature from the GFF column data. Subclasses may wish
   * to call this method then adjust the SequenceFeature depending on the
   * particular usage of different tools that generate GFF.
   * 
   * @param gff
   * @param attributes
   * @return
   */
  protected SequenceFeature buildSequenceFeature(String[] gff,
          Map<String, List<String>> attributes)
  {
    try
    {
      int start = Integer.parseInt(gff[START_COL]);
      int end = Integer.parseInt(gff[END_COL]);

      /*
       * default 'score' is 0 rather than Float.NaN as the latter currently
       * disables the 'graduated colour => colour by label' option
       */
      float score = 0f;
      try
      {
        score = Float.parseFloat(gff[SCORE_COL]);
      } catch (NumberFormatException nfe)
      {
        // e.g. '.' - leave as zero
      }

      SequenceFeature sf = new SequenceFeature(gff[TYPE_COL],
              gff[SOURCE_COL], start, end, score, gff[SOURCE_COL]);

      sf.setStrand(gff[STRAND_COL]);

      sf.setPhase(gff[PHASE_COL]);

      if (attributes != null)
      {
        /*
         * save 'raw' column 9 to allow roundtrip output as input
         */
        sf.setAttributes(gff[ATTRIBUTES_COL]);

        /*
         * Add attributes in column 9 to the sequence feature's 
         * 'otherData' table; use Note as a best proxy for description
         */
        for (Entry<String, List<String>> attr : attributes.entrySet())
        {
          String values = StringUtils.listToDelimitedString(
                  attr.getValue(), ",");
          sf.setValue(attr.getKey(), values);
          if (NOTE.equals(attr.getKey()))
          {
            sf.setDescription(values);
          }
        }
      }

      return sf;
    } catch (NumberFormatException nfe)
    {
      System.err.println("Invalid number in gff: " + nfe.getMessage());
      return null;
    }
  }

  /**
   * Returns the character used to separate attributes names from values in GFF
   * column 9. This is space for GFF2, '=' for GFF3.
   * 
   * @return
   */
  protected abstract char getNameValueSeparator();

  /**
   * Returns any existing mapping held on the alignment between the given
   * dataset sequences, or a new one if none found. This is a convenience method
   * to facilitate processing multiple GFF lines that make up a single 'spliced'
   * mapping, by extending the first mapping as the others are read.
   * 
   * @param align
   * @param fromSeq
   * @param toSeq
   * @return
   */
  protected AlignedCodonFrame getMapping(AlignmentI align,
          SequenceI fromSeq, SequenceI toSeq)
  {
    AlignedCodonFrame acf = align.getMapping(fromSeq, toSeq);
    if (acf == null)
    {
      acf = new AlignedCodonFrame();
    }
    return acf;
  }

}
