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
package jalview.datamodel;

import jalview.util.MapList;
import jalview.util.MappingUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores mapping between the columns of a protein alignment and a DNA alignment
 * and a list of individual codon to amino acid mappings between sequences.
 */
public class AlignedCodonFrame
{

  /**
   * tied array of na Sequence objects.
   */
  private SequenceI[] dnaSeqs = null;

  /**
   * tied array of Mappings to protein sequence Objects and SequenceI[]
   * aaSeqs=null; MapLists where each maps from the corresponding dnaSeqs
   * element to corresponding aaSeqs element
   */
  private Mapping[] dnaToProt = null;

  /**
   * Constructor
   */
  public AlignedCodonFrame()
  {
  }

  /**
   * Adds a mapping between the dataset sequences for the associated dna and
   * protein sequence objects
   * 
   * @param dnaseq
   * @param aaseq
   * @param map
   */
  public void addMap(SequenceI dnaseq, SequenceI aaseq, MapList map)
  {
    int nlen = 1;
    if (dnaSeqs != null)
    {
      nlen = dnaSeqs.length + 1;
    }
    SequenceI[] ndna = new SequenceI[nlen];
    Mapping[] ndtp = new Mapping[nlen];
    if (dnaSeqs != null)
    {
      System.arraycopy(dnaSeqs, 0, ndna, 0, dnaSeqs.length);
      System.arraycopy(dnaToProt, 0, ndtp, 0, dnaSeqs.length);
    }
    dnaSeqs = ndna;
    dnaToProt = ndtp;
    nlen--;
    dnaSeqs[nlen] = (dnaseq.getDatasetSequence() == null) ? dnaseq : dnaseq
            .getDatasetSequence();
    Mapping mp = new Mapping(map);
    // JBPNote DEBUG! THIS !
    // dnaseq.transferAnnotation(aaseq, mp);
    // aaseq.transferAnnotation(dnaseq, new Mapping(map.getInverse()));
    mp.to = (aaseq.getDatasetSequence() == null) ? aaseq : aaseq
            .getDatasetSequence();
    dnaToProt[nlen] = mp;
  }

  public SequenceI[] getdnaSeqs()
  {
    return dnaSeqs;
  }

  public SequenceI[] getAaSeqs()
  {
    if (dnaToProt == null)
    {
      return null;
    }
    SequenceI[] sqs = new SequenceI[dnaToProt.length];
    for (int sz = 0; sz < dnaToProt.length; sz++)
    {
      sqs[sz] = dnaToProt[sz].to;
    }
    return sqs;
  }

  public MapList[] getdnaToProt()
  {
    if (dnaToProt == null)
    {
      return null;
    }
    MapList[] sqs = new MapList[dnaToProt.length];
    for (int sz = 0; sz < dnaToProt.length; sz++)
    {
      sqs[sz] = dnaToProt[sz].map;
    }
    return sqs;
  }

  public Mapping[] getProtMappings()
  {
    return dnaToProt;
  }

  /**
   * Returns the first mapping found which is to or from the given sequence, or
   * null.
   * 
   * @param seq
   * @return
   */
  public Mapping getMappingForSequence(SequenceI seq)
  {
    if (dnaSeqs == null)
    {
      return null;
    }
    SequenceI seqDs = seq.getDatasetSequence();
    seqDs = seqDs != null ? seqDs : seq;

    for (int ds = 0; ds < dnaSeqs.length; ds++)
    {
      if (dnaSeqs[ds] == seqDs || dnaToProt[ds].to == seqDs)
      {
        return dnaToProt[ds];
      }
    }
    return null;
  }

  /**
   * Return the corresponding aligned or dataset aa sequence for given dna
   * sequence, null if not found.
   * 
   * @param sequenceRef
   * @return
   */
  public SequenceI getAaForDnaSeq(SequenceI dnaSeqRef)
  {
    if (dnaSeqs == null)
    {
      return null;
    }
    SequenceI dnads = dnaSeqRef.getDatasetSequence();
    for (int ds = 0; ds < dnaSeqs.length; ds++)
    {
      if (dnaSeqs[ds] == dnaSeqRef || dnaSeqs[ds] == dnads)
      {
        return dnaToProt[ds].to;
      }
    }
    return null;
  }

  /**
   * 
   * @param sequenceRef
   * @return null or corresponding aaSeq entry for dnaSeq entry
   */
  public SequenceI getDnaForAaSeq(SequenceI aaSeqRef)
  {
    if (dnaToProt == null)
    {
      return null;
    }
    SequenceI aads = aaSeqRef.getDatasetSequence();
    for (int as = 0; as < dnaToProt.length; as++)
    {
      if (dnaToProt[as].to == aaSeqRef || dnaToProt[as].to == aads)
      {
        return dnaSeqs[as];
      }
    }
    return null;
  }

  /**
   * test to see if codon frame involves seq in any way
   * 
   * @param seq
   *          a nucleotide or protein sequence
   * @return true if a mapping exists to or from this sequence to any translated
   *         sequence
   */
  public boolean involvesSequence(SequenceI seq)
  {
    return getAaForDnaSeq(seq) != null || getDnaForAaSeq(seq) != null;
  }

  /**
   * Add search results for regions in other sequences that translate or are
   * translated from a particular position in seq
   * 
   * @param seq
   * @param index
   *          position in seq
   * @param results
   *          where highlighted regions go
   */
  public void markMappedRegion(SequenceI seq, int index,
          SearchResults results)
  {
    if (dnaToProt == null)
    {
      return;
    }
    int[] codon;
    SequenceI ds = seq.getDatasetSequence();
    for (int mi = 0; mi < dnaToProt.length; mi++)
    {
      if (dnaSeqs[mi] == seq || dnaSeqs[mi] == ds)
      {
        // DEBUG System.err.println("dna pos "+index);
        codon = dnaToProt[mi].map.locateInTo(index, index);
        if (codon != null)
        {
          for (int i = 0; i < codon.length; i += 2)
          {
            results.addResult(dnaToProt[mi].to, codon[i], codon[i + 1]);
          }
        }
      }
      else if (dnaToProt[mi].to == seq || dnaToProt[mi].to == ds)
      {
        // DEBUG System.err.println("aa pos "+index);
        {
          codon = dnaToProt[mi].map.locateInFrom(index, index);
          if (codon != null)
          {
            for (int i = 0; i < codon.length; i += 2)
            {
              results.addResult(dnaSeqs[mi], codon[i], codon[i + 1]);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the DNA codon positions (base 1) for the given position (base 1) in
   * a mapped protein sequence, or null if no mapping is found.
   * 
   * Intended for use in aligning cDNA to match aligned protein. Only the first
   * mapping found is returned, so not suitable for use if multiple protein
   * sequences are mapped to the same cDNA (but aligning cDNA as protein is
   * ill-defined for this case anyway).
   * 
   * @param seq
   *          the DNA dataset sequence
   * @param aaPos
   *          residue position (base 1) in a protein sequence
   * @return
   */
  public int[] getDnaPosition(SequenceI seq, int aaPos)
  {
    /*
     * Adapted from markMappedRegion().
     */
    MapList ml = null;
    for (int i = 0; i < dnaToProt.length; i++)
    {
      if (dnaSeqs[i] == seq)
      {
        ml = getdnaToProt()[i];
        break;
      }
    }
    return ml == null ? null : ml.locateInFrom(aaPos, aaPos);
  }

  /**
   * Convenience method to return the first aligned sequence in the given
   * alignment whose dataset has a mapping with the given dataset sequence.
   * 
   * @param seq
   * 
   * @param al
   * @return
   */
  public SequenceI findAlignedSequence(SequenceI seq, AlignmentI al)
  {
    /*
     * Search mapped protein ('to') sequences first.
     */
    if (this.dnaToProt != null)
    {
      for (int i = 0; i < dnaToProt.length; i++)
      {
        if (this.dnaSeqs[i] == seq)
        {
          for (SequenceI sourceAligned : al.getSequences())
          {
            if (this.dnaToProt[i].to == sourceAligned.getDatasetSequence())
            {
              return sourceAligned;
            }
          }
        }
      }
    }

    /*
     * Then try mapped dna sequences.
     */
    if (this.dnaToProt != null)
    {
      for (int i = 0; i < dnaToProt.length; i++)
      {
        if (this.dnaToProt[i].to == seq)
        {
          for (SequenceI sourceAligned : al.getSequences())
          {
            if (this.dnaSeqs[i] == sourceAligned.getDatasetSequence())
            {
              return sourceAligned;
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Returns the region in the 'mappedFrom' sequence's dataset that is mapped to
   * position 'pos' (base 1) in the 'mappedTo' sequence's dataset. The region is
   * a set of start/end position pairs.
   * 
   * @param mappedFrom
   * @param mappedTo
   * @param pos
   * @return
   */
  public int[] getMappedRegion(SequenceI mappedFrom, SequenceI mappedTo,
          int pos)
  {
    SequenceI targetDs = mappedFrom.getDatasetSequence() == null ? mappedFrom
            : mappedFrom.getDatasetSequence();
    SequenceI sourceDs = mappedTo.getDatasetSequence() == null ? mappedTo
            : mappedTo.getDatasetSequence();
    if (targetDs == null || sourceDs == null || dnaToProt == null)
    {
      return null;
    }
    for (int mi = 0; mi < dnaToProt.length; mi++)
    {
      if (dnaSeqs[mi] == targetDs && dnaToProt[mi].to == sourceDs)
      {
        int[] codon = dnaToProt[mi].map.locateInFrom(pos, pos);
        if (codon != null)
        {
          return codon;
        }
      }
    }
    return null;
  }

  /**
   * Returns the DNA codon for the given position (base 1) in a mapped protein
   * sequence, or null if no mapping is found.
   * 
   * @param protein
   *          the peptide dataset sequence
   * @param aaPos
   *          residue position (base 1) in the peptide sequence
   * @return
   */
  public char[] getMappedCodon(SequenceI protein, int aaPos)
  {
    if (dnaToProt == null)
    {
      return null;
    }
    MapList ml = null;
    char[] dnaSeq = null;
    for (int i = 0; i < dnaToProt.length; i++)
    {
      if (dnaToProt[i].to == protein)
      {
        ml = getdnaToProt()[i];
        dnaSeq = dnaSeqs[i].getSequence();
        break;
      }
    }
    if (ml == null)
    {
      return null;
    }
    int[] codonPos = ml.locateInFrom(aaPos, aaPos);
    if (codonPos == null)
    {
      return null;
    }

    /*
     * Read off the mapped nucleotides (converting to position base 0)
     */
    codonPos = MappingUtils.flattenRanges(codonPos);
    return new char[] { dnaSeq[codonPos[0] - 1], dnaSeq[codonPos[1] - 1],
        dnaSeq[codonPos[2] - 1] };
  }

  /**
   * Returns any mappings found which are to (or from) the given sequence, and
   * to distinct sequences.
   * 
   * @param seq
   * @return
   */
  public List<Mapping> getMappingsForSequence(SequenceI seq)
  {
    List<Mapping> result = new ArrayList<Mapping>();
    if (dnaSeqs == null)
    {
      return result;
    }
    List<SequenceI> related = new ArrayList<SequenceI>();
    SequenceI seqDs = seq.getDatasetSequence();
    seqDs = seqDs != null ? seqDs : seq;

    for (int ds = 0; ds < dnaSeqs.length; ds++)
    {
      final Mapping mapping = dnaToProt[ds];
      if (dnaSeqs[ds] == seqDs || mapping.to == seqDs)
      {
        if (!related.contains(mapping.to))
        {
          result.add(mapping);
          related.add(mapping.to);
        }
      }
    }
    return result;
  }
}
