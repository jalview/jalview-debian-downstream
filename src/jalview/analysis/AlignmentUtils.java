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

import jalview.datamodel.AlignedCodon;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.FeatureProperties;
import jalview.datamodel.Mapping;
import jalview.datamodel.SearchResults;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.util.DBRefUtils;
import jalview.util.MapList;
import jalview.util.MappingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * grab bag of useful alignment manipulation operations Expect these to be
 * refactored elsewhere at some point.
 * 
 * @author jimp
 * 
 */
public class AlignmentUtils
{

  /**
   * given an existing alignment, create a new alignment including all, or up to
   * flankSize additional symbols from each sequence's dataset sequence
   * 
   * @param core
   * @param flankSize
   * @return AlignmentI
   */
  public static AlignmentI expandContext(AlignmentI core, int flankSize)
  {
    List<SequenceI> sq = new ArrayList<SequenceI>();
    int maxoffset = 0;
    for (SequenceI s : core.getSequences())
    {
      SequenceI newSeq = s.deriveSequence();
      final int newSeqStart = newSeq.getStart() - 1;
      if (newSeqStart > maxoffset
              && newSeq.getDatasetSequence().getStart() < s.getStart())
      {
        maxoffset = newSeqStart;
      }
      sq.add(newSeq);
    }
    if (flankSize > -1)
    {
      maxoffset = Math.min(maxoffset, flankSize);
    }

    /*
     * now add offset left and right to create an expanded alignment
     */
    for (SequenceI s : sq)
    {
      SequenceI ds = s;
      while (ds.getDatasetSequence() != null)
      {
        ds = ds.getDatasetSequence();
      }
      int s_end = s.findPosition(s.getStart() + s.getLength());
      // find available flanking residues for sequence
      int ustream_ds = s.getStart() - ds.getStart();
      int dstream_ds = ds.getEnd() - s_end;

      // build new flanked sequence

      // compute gap padding to start of flanking sequence
      int offset = maxoffset - ustream_ds;

      // padding is gapChar x ( maxoffset - min(ustream_ds, flank)
      if (flankSize >= 0)
      {
        if (flankSize < ustream_ds)
        {
          // take up to flankSize residues
          offset = maxoffset - flankSize;
          ustream_ds = flankSize;
        }
        if (flankSize <= dstream_ds)
        {
          dstream_ds = flankSize - 1;
        }
      }
      // TODO use Character.toLowerCase to avoid creating String objects?
      char[] upstream = new String(ds.getSequence(s.getStart() - 1
              - ustream_ds, s.getStart() - 1)).toLowerCase().toCharArray();
      char[] downstream = new String(ds.getSequence(s_end - 1, s_end
              + dstream_ds)).toLowerCase().toCharArray();
      char[] coreseq = s.getSequence();
      char[] nseq = new char[offset + upstream.length + downstream.length
              + coreseq.length];
      char c = core.getGapCharacter();

      int p = 0;
      for (; p < offset; p++)
      {
        nseq[p] = c;
      }

      System.arraycopy(upstream, 0, nseq, p, upstream.length);
      System.arraycopy(coreseq, 0, nseq, p + upstream.length,
              coreseq.length);
      System.arraycopy(downstream, 0, nseq, p + coreseq.length
              + upstream.length, downstream.length);
      s.setSequence(new String(nseq));
      s.setStart(s.getStart() - ustream_ds);
      s.setEnd(s_end + downstream.length);
    }
    AlignmentI newAl = new jalview.datamodel.Alignment(
            sq.toArray(new SequenceI[0]));
    for (SequenceI s : sq)
    {
      if (s.getAnnotation() != null)
      {
        for (AlignmentAnnotation aa : s.getAnnotation())
        {
          aa.adjustForAlignment(); // JAL-1712 fix
          newAl.addAnnotation(aa);
        }
      }
    }
    newAl.setDataset(core.getDataset());
    return newAl;
  }

  /**
   * Returns the index (zero-based position) of a sequence in an alignment, or
   * -1 if not found.
   * 
   * @param al
   * @param seq
   * @return
   */
  public static int getSequenceIndex(AlignmentI al, SequenceI seq)
  {
    int result = -1;
    int pos = 0;
    for (SequenceI alSeq : al.getSequences())
    {
      if (alSeq == seq)
      {
        result = pos;
        break;
      }
      pos++;
    }
    return result;
  }

  /**
   * Returns a map of lists of sequences in the alignment, keyed by sequence
   * name. For use in mapping between different alignment views of the same
   * sequences.
   * 
   * @see jalview.datamodel.AlignmentI#getSequencesByName()
   */
  public static Map<String, List<SequenceI>> getSequencesByName(
          AlignmentI al)
  {
    Map<String, List<SequenceI>> theMap = new LinkedHashMap<String, List<SequenceI>>();
    for (SequenceI seq : al.getSequences())
    {
      String name = seq.getName();
      if (name != null)
      {
        List<SequenceI> seqs = theMap.get(name);
        if (seqs == null)
        {
          seqs = new ArrayList<SequenceI>();
          theMap.put(name, seqs);
        }
        seqs.add(seq);
      }
    }
    return theMap;
  }

  /**
   * Build mapping of protein to cDNA alignment. Mappings are made between
   * sequences where the cDNA translates to the protein sequence. Any new
   * mappings are added to the protein alignment. Returns true if any mappings
   * either already exist or were added, else false.
   * 
   * @param proteinAlignment
   * @param cdnaAlignment
   * @return
   */
  public static boolean mapProteinToCdna(final AlignmentI proteinAlignment,
          final AlignmentI cdnaAlignment)
  {
    if (proteinAlignment == null || cdnaAlignment == null)
    {
      return false;
    }

    Set<SequenceI> mappedDna = new HashSet<SequenceI>();
    Set<SequenceI> mappedProtein = new HashSet<SequenceI>();

    /*
     * First pass - map sequences where cross-references exist. This include
     * 1-to-many mappings to support, for example, variant cDNA.
     */
    boolean mappingPerformed = mapProteinToCdna(proteinAlignment,
            cdnaAlignment, mappedDna, mappedProtein, true);

    /*
     * Second pass - map sequences where no cross-references exist. This only
     * does 1-to-1 mappings and assumes corresponding sequences are in the same
     * order in the alignments.
     */
    mappingPerformed |= mapProteinToCdna(proteinAlignment, cdnaAlignment,
            mappedDna, mappedProtein, false);
    return mappingPerformed;
  }

  /**
   * Make mappings between compatible sequences (where the cDNA translation
   * matches the protein).
   * 
   * @param proteinAlignment
   * @param cdnaAlignment
   * @param mappedDna
   *          a set of mapped DNA sequences (to add to)
   * @param mappedProtein
   *          a set of mapped Protein sequences (to add to)
   * @param xrefsOnly
   *          if true, only map sequences where xrefs exist
   * @return
   */
  protected static boolean mapProteinToCdna(
          final AlignmentI proteinAlignment,
          final AlignmentI cdnaAlignment, Set<SequenceI> mappedDna,
          Set<SequenceI> mappedProtein, boolean xrefsOnly)
  {
    boolean mappingPerformed = false;
    List<SequenceI> thisSeqs = proteinAlignment.getSequences();
    for (SequenceI aaSeq : thisSeqs)
    {
      boolean proteinMapped = false;
      AlignedCodonFrame acf = new AlignedCodonFrame();

      for (SequenceI cdnaSeq : cdnaAlignment.getSequences())
      {
        /*
         * Always try to map if sequences have xref to each other; this supports
         * variant cDNA or alternative splicing for a protein sequence.
         * 
         * If no xrefs, try to map progressively, assuming that alignments have
         * mappable sequences in corresponding order. These are not
         * many-to-many, as that would risk mixing species with similar cDNA
         * sequences.
         */
        if (xrefsOnly && !AlignmentUtils.haveCrossRef(aaSeq, cdnaSeq))
        {
          continue;
        }

        /*
         * Don't map non-xrefd sequences more than once each. This heuristic
         * allows us to pair up similar sequences in ordered alignments.
         */
        if (!xrefsOnly
                && (mappedProtein.contains(aaSeq) || mappedDna
                        .contains(cdnaSeq)))
        {
          continue;
        }
        if (!mappingExists(proteinAlignment.getCodonFrames(),
                aaSeq.getDatasetSequence(), cdnaSeq.getDatasetSequence()))
        {
          MapList map = mapProteinToCdna(aaSeq, cdnaSeq);
          if (map != null)
          {
            acf.addMap(cdnaSeq, aaSeq, map);
            mappingPerformed = true;
            proteinMapped = true;
            mappedDna.add(cdnaSeq);
            mappedProtein.add(aaSeq);
          }
        }
      }
      if (proteinMapped)
      {
        proteinAlignment.addCodonFrame(acf);
      }
    }
    return mappingPerformed;
  }

  /**
   * Answers true if the mappings include one between the given (dataset)
   * sequences.
   */
  public static boolean mappingExists(Set<AlignedCodonFrame> set,
          SequenceI aaSeq, SequenceI cdnaSeq)
  {
    if (set != null)
    {
      for (AlignedCodonFrame acf : set)
      {
        if (cdnaSeq == acf.getDnaForAaSeq(aaSeq))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Build a mapping (if possible) of a protein to a cDNA sequence. The cDNA
   * must be three times the length of the protein, possibly after ignoring
   * start and/or stop codons, and must translate to the protein. Returns null
   * if no mapping is determined.
   * 
   * @param proteinSeqs
   * @param cdnaSeq
   * @return
   */
  public static MapList mapProteinToCdna(SequenceI proteinSeq,
          SequenceI cdnaSeq)
  {
    /*
     * Here we handle either dataset sequence set (desktop) or absent (applet).
     * Use only the char[] form of the sequence to avoid creating possibly large
     * String objects.
     */
    final SequenceI proteinDataset = proteinSeq.getDatasetSequence();
    char[] aaSeqChars = proteinDataset != null ? proteinDataset
            .getSequence() : proteinSeq.getSequence();
    final SequenceI cdnaDataset = cdnaSeq.getDatasetSequence();
    char[] cdnaSeqChars = cdnaDataset != null ? cdnaDataset.getSequence()
            : cdnaSeq.getSequence();
    if (aaSeqChars == null || cdnaSeqChars == null)
    {
      return null;
    }

    /*
     * cdnaStart/End, proteinStartEnd are base 1 (for dataset sequence mapping)
     */
    final int mappedLength = 3 * aaSeqChars.length;
    int cdnaLength = cdnaSeqChars.length;
    int cdnaStart = 1;
    int cdnaEnd = cdnaLength;
    final int proteinStart = 1;
    final int proteinEnd = aaSeqChars.length;

    /*
     * If lengths don't match, try ignoring stop codon.
     */
    if (cdnaLength != mappedLength && cdnaLength > 2)
    {
      String lastCodon = String.valueOf(cdnaSeqChars, cdnaLength - 3, 3)
              .toUpperCase();
      for (String stop : ResidueProperties.STOP)
      {
        if (lastCodon.equals(stop))
        {
          cdnaEnd -= 3;
          cdnaLength -= 3;
          break;
        }
      }
    }

    /*
     * If lengths still don't match, try ignoring start codon.
     */
    if (cdnaLength != mappedLength
            && cdnaLength > 2
            && String.valueOf(cdnaSeqChars, 0, 3).toUpperCase()
                    .equals(ResidueProperties.START))
    {
      cdnaStart += 3;
      cdnaLength -= 3;
    }

    if (cdnaLength != mappedLength)
    {
      return null;
    }
    if (!translatesAs(cdnaSeqChars, cdnaStart - 1, aaSeqChars))
    {
      return null;
    }
    MapList map = new MapList(new int[] { cdnaStart, cdnaEnd }, new int[] {
        proteinStart, proteinEnd }, 3, 1);
    return map;
  }

  /**
   * Test whether the given cdna sequence, starting at the given offset,
   * translates to the given amino acid sequence, using the standard translation
   * table. Designed to fail fast i.e. as soon as a mismatch position is found.
   * 
   * @param cdnaSeqChars
   * @param cdnaStart
   * @param aaSeqChars
   * @return
   */
  protected static boolean translatesAs(char[] cdnaSeqChars, int cdnaStart,
          char[] aaSeqChars)
  {
    if (cdnaSeqChars == null || aaSeqChars == null)
    {
      return false;
    }

    int aaResidue = 0;
    for (int i = cdnaStart; i < cdnaSeqChars.length - 2
            && aaResidue < aaSeqChars.length; i += 3, aaResidue++)
    {
      String codon = String.valueOf(cdnaSeqChars, i, 3);
      final String translated = ResidueProperties.codonTranslate(codon);
      /*
       * allow * in protein to match untranslatable in dna
       */
      final char aaRes = aaSeqChars[aaResidue];
      if ((translated == null || "STOP".equals(translated)) && aaRes == '*')
      {
        continue;
      }
      if (translated == null || !(aaRes == translated.charAt(0)))
      {
        // debug
        // System.out.println(("Mismatch at " + i + "/" + aaResidue + ": "
        // + codon + "(" + translated + ") != " + aaRes));
        return false;
      }
    }
    // fail if we didn't match all of the aa sequence
    return (aaResidue == aaSeqChars.length);
  }

  /**
   * Align sequence 'seq' to match the alignment of a mapped sequence. Note this
   * currently assumes that we are aligning cDNA to match protein.
   * 
   * @param seq
   *          the sequence to be realigned
   * @param al
   *          the alignment whose sequence alignment is to be 'copied'
   * @param gap
   *          character string represent a gap in the realigned sequence
   * @param preserveUnmappedGaps
   * @param preserveMappedGaps
   * @return true if the sequence was realigned, false if it could not be
   */
  public static boolean alignSequenceAs(SequenceI seq, AlignmentI al,
          String gap, boolean preserveMappedGaps,
          boolean preserveUnmappedGaps)
  {
    /*
     * Get any mappings from the source alignment to the target (dataset)
     * sequence.
     */
    // TODO there may be one AlignedCodonFrame per dataset sequence, or one with
    // all mappings. Would it help to constrain this?
    List<AlignedCodonFrame> mappings = al.getCodonFrame(seq);
    if (mappings == null || mappings.isEmpty())
    {
      return false;
    }

    /*
     * Locate the aligned source sequence whose dataset sequence is mapped. We
     * just take the first match here (as we can't align cDNA like more than one
     * protein sequence).
     */
    SequenceI alignFrom = null;
    AlignedCodonFrame mapping = null;
    for (AlignedCodonFrame mp : mappings)
    {
      alignFrom = mp.findAlignedSequence(seq.getDatasetSequence(), al);
      if (alignFrom != null)
      {
        mapping = mp;
        break;
      }
    }

    if (alignFrom == null)
    {
      return false;
    }
    alignSequenceAs(seq, alignFrom, mapping, gap, al.getGapCharacter(),
            preserveMappedGaps, preserveUnmappedGaps);
    return true;
  }

  /**
   * Align sequence 'alignTo' the same way as 'alignFrom', using the mapping to
   * match residues and codons. Flags control whether existing gaps in unmapped
   * (intron) and mapped (exon) regions are preserved or not. Gaps linking intro
   * and exon are only retained if both flags are set.
   * 
   * @param alignTo
   * @param alignFrom
   * @param mapping
   * @param myGap
   * @param sourceGap
   * @param preserveUnmappedGaps
   * @param preserveMappedGaps
   */
  public static void alignSequenceAs(SequenceI alignTo,
          SequenceI alignFrom, AlignedCodonFrame mapping, String myGap,
          char sourceGap, boolean preserveMappedGaps,
          boolean preserveUnmappedGaps)
  {
    // TODO generalise to work for Protein-Protein, dna-dna, dna-protein
    final char[] thisSeq = alignTo.getSequence();
    final char[] thatAligned = alignFrom.getSequence();
    StringBuilder thisAligned = new StringBuilder(2 * thisSeq.length);

    // aligned and dataset sequence positions, all base zero
    int thisSeqPos = 0;
    int sourceDsPos = 0;

    int basesWritten = 0;
    char myGapChar = myGap.charAt(0);
    int ratio = myGap.length();

    /*
     * Traverse the aligned protein sequence.
     */
    int sourceGapMappedLength = 0;
    boolean inExon = false;
    for (char sourceChar : thatAligned)
    {
      if (sourceChar == sourceGap)
      {
        sourceGapMappedLength += ratio;
        continue;
      }

      /*
       * Found a residue. Locate its mapped codon (start) position.
       */
      sourceDsPos++;
      // Note mapping positions are base 1, our sequence positions base 0
      int[] mappedPos = mapping.getMappedRegion(alignTo, alignFrom,
              sourceDsPos);
      if (mappedPos == null)
      {
        /*
         * Abort realignment if unmapped protein. Or could ignore it??
         */
        System.err.println("Can't align: no codon mapping to residue "
                + sourceDsPos + "(" + sourceChar + ")");
        return;
      }

      int mappedCodonStart = mappedPos[0]; // position (1...) of codon start
      int mappedCodonEnd = mappedPos[mappedPos.length - 1]; // codon end pos
      StringBuilder trailingCopiedGap = new StringBuilder();

      /*
       * Copy dna sequence up to and including this codon. Optionally, include
       * gaps before the codon starts (in introns) and/or after the codon starts
       * (in exons).
       * 
       * Note this only works for 'linear' splicing, not reverse or interleaved.
       * But then 'align dna as protein' doesn't make much sense otherwise.
       */
      int intronLength = 0;
      while (basesWritten < mappedCodonEnd && thisSeqPos < thisSeq.length)
      {
        final char c = thisSeq[thisSeqPos++];
        if (c != myGapChar)
        {
          basesWritten++;

          if (basesWritten < mappedCodonStart)
          {
            /*
             * Found an unmapped (intron) base. First add in any preceding gaps
             * (if wanted).
             */
            if (preserveUnmappedGaps && trailingCopiedGap.length() > 0)
            {
              thisAligned.append(trailingCopiedGap.toString());
              intronLength += trailingCopiedGap.length();
              trailingCopiedGap = new StringBuilder();
            }
            intronLength++;
            inExon = false;
          }
          else
          {
            final boolean startOfCodon = basesWritten == mappedCodonStart;
            int gapsToAdd = calculateGapsToInsert(preserveMappedGaps,
                    preserveUnmappedGaps, sourceGapMappedLength, inExon,
                    trailingCopiedGap.length(), intronLength, startOfCodon);
            for (int i = 0; i < gapsToAdd; i++)
            {
              thisAligned.append(myGapChar);
            }
            sourceGapMappedLength = 0;
            inExon = true;
          }
          thisAligned.append(c);
          trailingCopiedGap = new StringBuilder();
        }
        else
        {
          if (inExon && preserveMappedGaps)
          {
            trailingCopiedGap.append(myGapChar);
          }
          else if (!inExon && preserveUnmappedGaps)
          {
            trailingCopiedGap.append(myGapChar);
          }
        }
      }
    }

    /*
     * At end of protein sequence. Copy any remaining dna sequence, optionally
     * including (intron) gaps. We do not copy trailing gaps in protein.
     */
    while (thisSeqPos < thisSeq.length)
    {
      final char c = thisSeq[thisSeqPos++];
      if (c != myGapChar || preserveUnmappedGaps)
      {
        thisAligned.append(c);
      }
    }

    /*
     * All done aligning, set the aligned sequence.
     */
    alignTo.setSequence(new String(thisAligned));
  }

  /**
   * Helper method to work out how many gaps to insert when realigning.
   * 
   * @param preserveMappedGaps
   * @param preserveUnmappedGaps
   * @param sourceGapMappedLength
   * @param inExon
   * @param trailingCopiedGap
   * @param intronLength
   * @param startOfCodon
   * @return
   */
  protected static int calculateGapsToInsert(boolean preserveMappedGaps,
          boolean preserveUnmappedGaps, int sourceGapMappedLength,
          boolean inExon, int trailingGapLength, int intronLength,
          final boolean startOfCodon)
  {
    int gapsToAdd = 0;
    if (startOfCodon)
    {
      /*
       * Reached start of codon. Ignore trailing gaps in intron unless we are
       * preserving gaps in both exon and intron. Ignore them anyway if the
       * protein alignment introduces a gap at least as large as the intronic
       * region.
       */
      if (inExon && !preserveMappedGaps)
      {
        trailingGapLength = 0;
      }
      if (!inExon && !(preserveMappedGaps && preserveUnmappedGaps))
      {
        trailingGapLength = 0;
      }
      if (inExon)
      {
        gapsToAdd = Math.max(sourceGapMappedLength, trailingGapLength);
      }
      else
      {
        if (intronLength + trailingGapLength <= sourceGapMappedLength)
        {
          gapsToAdd = sourceGapMappedLength - intronLength;
        }
        else
        {
          gapsToAdd = Math.min(intronLength + trailingGapLength
                  - sourceGapMappedLength, trailingGapLength);
        }
      }
    }
    else
    {
      /*
       * second or third base of codon; check for any gaps in dna
       */
      if (!preserveMappedGaps)
      {
        trailingGapLength = 0;
      }
      gapsToAdd = Math.max(sourceGapMappedLength, trailingGapLength);
    }
    return gapsToAdd;
  }

  /**
   * Returns a list of sequences mapped from the given sequences and aligned
   * (gapped) in the same way. For example, the cDNA for aligned protein, where
   * a single gap in protein generates three gaps in cDNA.
   * 
   * @param sequences
   * @param gapCharacter
   * @param mappings
   * @return
   */
  public static List<SequenceI> getAlignedTranslation(
          List<SequenceI> sequences, char gapCharacter,
          Set<AlignedCodonFrame> mappings)
  {
    List<SequenceI> alignedSeqs = new ArrayList<SequenceI>();

    for (SequenceI seq : sequences)
    {
      List<SequenceI> mapped = getAlignedTranslation(seq, gapCharacter,
              mappings);
      alignedSeqs.addAll(mapped);
    }
    return alignedSeqs;
  }

  /**
   * Returns sequences aligned 'like' the source sequence, as mapped by the
   * given mappings. Normally we expect zero or one 'mapped' sequences, but this
   * will support 1-to-many as well.
   * 
   * @param seq
   * @param gapCharacter
   * @param mappings
   * @return
   */
  protected static List<SequenceI> getAlignedTranslation(SequenceI seq,
          char gapCharacter, Set<AlignedCodonFrame> mappings)
  {
    List<SequenceI> result = new ArrayList<SequenceI>();
    for (AlignedCodonFrame mapping : mappings)
    {
      if (mapping.involvesSequence(seq))
      {
        SequenceI mapped = getAlignedTranslation(seq, gapCharacter, mapping);
        if (mapped != null)
        {
          result.add(mapped);
        }
      }
    }
    return result;
  }

  /**
   * Returns the translation of 'seq' (as held in the mapping) with
   * corresponding alignment (gaps).
   * 
   * @param seq
   * @param gapCharacter
   * @param mapping
   * @return
   */
  protected static SequenceI getAlignedTranslation(SequenceI seq,
          char gapCharacter, AlignedCodonFrame mapping)
  {
    String gap = String.valueOf(gapCharacter);
    boolean toDna = false;
    int fromRatio = 1;
    SequenceI mapTo = mapping.getDnaForAaSeq(seq);
    if (mapTo != null)
    {
      // mapping is from protein to nucleotide
      toDna = true;
      // should ideally get gap count ratio from mapping
      gap = String.valueOf(new char[] { gapCharacter, gapCharacter,
          gapCharacter });
    }
    else
    {
      // mapping is from nucleotide to protein
      mapTo = mapping.getAaForDnaSeq(seq);
      fromRatio = 3;
    }
    StringBuilder newseq = new StringBuilder(seq.getLength()
            * (toDna ? 3 : 1));

    int residueNo = 0; // in seq, base 1
    int[] phrase = new int[fromRatio];
    int phraseOffset = 0;
    int gapWidth = 0;
    boolean first = true;
    final Sequence alignedSeq = new Sequence("", "");

    for (char c : seq.getSequence())
    {
      if (c == gapCharacter)
      {
        gapWidth++;
        if (gapWidth >= fromRatio)
        {
          newseq.append(gap);
          gapWidth = 0;
        }
      }
      else
      {
        phrase[phraseOffset++] = residueNo + 1;
        if (phraseOffset == fromRatio)
        {
          /*
           * Have read a whole codon (or protein residue), now translate: map
           * source phrase to positions in target sequence add characters at
           * these positions to newseq Note mapping positions are base 1, our
           * sequence positions base 0.
           */
          SearchResults sr = new SearchResults();
          for (int pos : phrase)
          {
            mapping.markMappedRegion(seq, pos, sr);
          }
          newseq.append(sr.getCharacters());
          if (first)
          {
            first = false;
            // Hack: Copy sequence dataset, name and description from
            // SearchResults.match[0].sequence
            // TODO? carry over sequence names from original 'complement'
            // alignment
            SequenceI mappedTo = sr.getResultSequence(0);
            alignedSeq.setName(mappedTo.getName());
            alignedSeq.setDescription(mappedTo.getDescription());
            alignedSeq.setDatasetSequence(mappedTo);
          }
          phraseOffset = 0;
        }
        residueNo++;
      }
    }
    alignedSeq.setSequence(newseq.toString());
    return alignedSeq;
  }

  /**
   * Realigns the given protein to match the alignment of the dna, using codon
   * mappings to translate aligned codon positions to protein residues.
   * 
   * @param protein
   *          the alignment whose sequences are realigned by this method
   * @param dna
   *          the dna alignment whose alignment we are 'copying'
   * @return the number of sequences that were realigned
   */
  public static int alignProteinAsDna(AlignmentI protein, AlignmentI dna)
  {
    List<SequenceI> unmappedProtein = new ArrayList<SequenceI>();
    unmappedProtein.addAll(protein.getSequences());

    Set<AlignedCodonFrame> mappings = protein.getCodonFrames();

    /*
     * Map will hold, for each aligned codon position e.g. [3, 5, 6], a map of
     * {dnaSequence, {proteinSequence, codonProduct}} at that position. The
     * comparator keeps the codon positions ordered.
     */
    Map<AlignedCodon, Map<SequenceI, String>> alignedCodons = new TreeMap<AlignedCodon, Map<SequenceI, String>>(
            new CodonComparator());
    for (SequenceI dnaSeq : dna.getSequences())
    {
      for (AlignedCodonFrame mapping : mappings)
      {
        Mapping seqMap = mapping.getMappingForSequence(dnaSeq);
        SequenceI prot = mapping.findAlignedSequence(
                dnaSeq.getDatasetSequence(), protein);
        if (prot != null)
        {
          addCodonPositions(dnaSeq, prot, protein.getGapCharacter(),
                  seqMap, alignedCodons);
          unmappedProtein.remove(prot);
        }
      }
    }
    return alignProteinAs(protein, alignedCodons, unmappedProtein);
  }

  /**
   * Update the aligned protein sequences to match the codon alignments given in
   * the map.
   * 
   * @param protein
   * @param alignedCodons
   *          an ordered map of codon positions (columns), with sequence/peptide
   *          values present in each column
   * @param unmappedProtein
   * @return
   */
  protected static int alignProteinAs(AlignmentI protein,
          Map<AlignedCodon, Map<SequenceI, String>> alignedCodons,
          List<SequenceI> unmappedProtein)
  {
    /*
     * Prefill aligned sequences with gaps before inserting aligned protein
     * residues.
     */
    int alignedWidth = alignedCodons.size();
    char[] gaps = new char[alignedWidth];
    Arrays.fill(gaps, protein.getGapCharacter());
    String allGaps = String.valueOf(gaps);
    for (SequenceI seq : protein.getSequences())
    {
      if (!unmappedProtein.contains(seq))
      {
        seq.setSequence(allGaps);
      }
    }

    int column = 0;
    for (AlignedCodon codon : alignedCodons.keySet())
    {
      final Map<SequenceI, String> columnResidues = alignedCodons
              .get(codon);
      for (Entry<SequenceI, String> entry : columnResidues.entrySet())
      {
        // place translated codon at its column position in sequence
        entry.getKey().getSequence()[column] = entry.getValue().charAt(0);
      }
      column++;
    }
    return 0;
  }

  /**
   * Populate the map of aligned codons by traversing the given sequence
   * mapping, locating the aligned positions of mapped codons, and adding those
   * positions and their translation products to the map.
   * 
   * @param dna
   *          the aligned sequence we are mapping from
   * @param protein
   *          the sequence to be aligned to the codons
   * @param gapChar
   *          the gap character in the dna sequence
   * @param seqMap
   *          a mapping to a sequence translation
   * @param alignedCodons
   *          the map we are building up
   */
  static void addCodonPositions(SequenceI dna, SequenceI protein,
          char gapChar, Mapping seqMap,
          Map<AlignedCodon, Map<SequenceI, String>> alignedCodons)
  {
    Iterator<AlignedCodon> codons = seqMap.getCodonIterator(dna, gapChar);
    while (codons.hasNext())
    {
      AlignedCodon codon = codons.next();
      Map<SequenceI, String> seqProduct = alignedCodons.get(codon);
      if (seqProduct == null)
      {
        seqProduct = new HashMap<SequenceI, String>();
        alignedCodons.put(codon, seqProduct);
      }
      seqProduct.put(protein, codon.product);
    }
  }

  /**
   * Returns true if a cDNA/Protein mapping either exists, or could be made,
   * between at least one pair of sequences in the two alignments. Currently,
   * the logic is:
   * <ul>
   * <li>One alignment must be nucleotide, and the other protein</li>
   * <li>At least one pair of sequences must be already mapped, or mappable</li>
   * <li>Mappable means the nucleotide translation matches the protein sequence</li>
   * <li>The translation may ignore start and stop codons if present in the
   * nucleotide</li>
   * </ul>
   * 
   * @param al1
   * @param al2
   * @return
   */
  public static boolean isMappable(AlignmentI al1, AlignmentI al2)
  {
    if (al1 == null || al2 == null)
    {
      return false;
    }

    /*
     * Require one nucleotide and one protein
     */
    if (al1.isNucleotide() == al2.isNucleotide())
    {
      return false;
    }
    AlignmentI dna = al1.isNucleotide() ? al1 : al2;
    AlignmentI protein = dna == al1 ? al2 : al1;
    Set<AlignedCodonFrame> mappings = protein.getCodonFrames();
    for (SequenceI dnaSeq : dna.getSequences())
    {
      for (SequenceI proteinSeq : protein.getSequences())
      {
        if (isMappable(dnaSeq, proteinSeq, mappings))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the dna sequence is mapped, or could be mapped, to the
   * protein sequence.
   * 
   * @param dnaSeq
   * @param proteinSeq
   * @param mappings
   * @return
   */
  protected static boolean isMappable(SequenceI dnaSeq,
          SequenceI proteinSeq, Set<AlignedCodonFrame> mappings)
  {
    if (dnaSeq == null || proteinSeq == null)
    {
      return false;
    }

    SequenceI dnaDs = dnaSeq.getDatasetSequence() == null ? dnaSeq : dnaSeq
            .getDatasetSequence();
    SequenceI proteinDs = proteinSeq.getDatasetSequence() == null ? proteinSeq
            : proteinSeq.getDatasetSequence();

    /*
     * Already mapped?
     */
    for (AlignedCodonFrame mapping : mappings)
    {
      if (proteinDs == mapping.getAaForDnaSeq(dnaDs))
      {
        return true;
      }
    }

    /*
     * Just try to make a mapping (it is not yet stored), test whether
     * successful.
     */
    return mapProteinToCdna(proteinDs, dnaDs) != null;
  }

  /**
   * Finds any reference annotations associated with the sequences in
   * sequenceScope, that are not already added to the alignment, and adds them
   * to the 'candidates' map. Also populates a lookup table of annotation
   * labels, keyed by calcId, for use in constructing tooltips or the like.
   * 
   * @param sequenceScope
   *          the sequences to scan for reference annotations
   * @param labelForCalcId
   *          (optional) map to populate with label for calcId
   * @param candidates
   *          map to populate with annotations for sequence
   * @param al
   *          the alignment to check for presence of annotations
   */
  public static void findAddableReferenceAnnotations(
          List<SequenceI> sequenceScope,
          Map<String, String> labelForCalcId,
          final Map<SequenceI, List<AlignmentAnnotation>> candidates,
          AlignmentI al)
  {
    if (sequenceScope == null)
    {
      return;
    }

    /*
     * For each sequence in scope, make a list of any annotations on the
     * underlying dataset sequence which are not already on the alignment.
     * 
     * Add to a map of { alignmentSequence, <List of annotations to add> }
     */
    for (SequenceI seq : sequenceScope)
    {
      SequenceI dataset = seq.getDatasetSequence();
      if (dataset == null)
      {
        continue;
      }
      AlignmentAnnotation[] datasetAnnotations = dataset.getAnnotation();
      if (datasetAnnotations == null)
      {
        continue;
      }
      final List<AlignmentAnnotation> result = new ArrayList<AlignmentAnnotation>();
      for (AlignmentAnnotation dsann : datasetAnnotations)
      {
        /*
         * Find matching annotations on the alignment. If none is found, then
         * add this annotation to the list of 'addable' annotations for this
         * sequence.
         */
        final Iterable<AlignmentAnnotation> matchedAlignmentAnnotations = al
                .findAnnotations(seq, dsann.getCalcId(), dsann.label);
        if (!matchedAlignmentAnnotations.iterator().hasNext())
        {
          result.add(dsann);
          if (labelForCalcId != null)
          {
            labelForCalcId.put(dsann.getCalcId(), dsann.label);
          }
        }
      }
      /*
       * Save any addable annotations for this sequence
       */
      if (!result.isEmpty())
      {
        candidates.put(seq, result);
      }
    }
  }

  /**
   * Adds annotations to the top of the alignment annotations, in the same order
   * as their related sequences.
   * 
   * @param annotations
   *          the annotations to add
   * @param alignment
   *          the alignment to add them to
   * @param selectionGroup
   *          current selection group (or null if none)
   */
  public static void addReferenceAnnotations(
          Map<SequenceI, List<AlignmentAnnotation>> annotations,
          final AlignmentI alignment, final SequenceGroup selectionGroup)
  {
    for (SequenceI seq : annotations.keySet())
    {
      for (AlignmentAnnotation ann : annotations.get(seq))
      {
        AlignmentAnnotation copyAnn = new AlignmentAnnotation(ann);
        int startRes = 0;
        int endRes = ann.annotations.length;
        if (selectionGroup != null)
        {
          startRes = selectionGroup.getStartRes();
          endRes = selectionGroup.getEndRes();
        }
        copyAnn.restrict(startRes, endRes);

        /*
         * Add to the sequence (sets copyAnn.datasetSequence), unless the
         * original annotation is already on the sequence.
         */
        if (!seq.hasAnnotation(ann))
        {
          seq.addAlignmentAnnotation(copyAnn);
        }
        // adjust for gaps
        copyAnn.adjustForAlignment();
        // add to the alignment and set visible
        alignment.addAnnotation(copyAnn);
        copyAnn.visible = true;
      }
    }
  }

  /**
   * Set visibility of alignment annotations of specified types (labels), for
   * specified sequences. This supports controls like
   * "Show all secondary structure", "Hide all Temp factor", etc.
   * 
   * @al the alignment to scan for annotations
   * @param types
   *          the types (labels) of annotations to be updated
   * @param forSequences
   *          if not null, only annotations linked to one of these sequences are
   *          in scope for update; if null, acts on all sequence annotations
   * @param anyType
   *          if this flag is true, 'types' is ignored (label not checked)
   * @param doShow
   *          if true, set visibility on, else set off
   */
  public static void showOrHideSequenceAnnotations(AlignmentI al,
          Collection<String> types, List<SequenceI> forSequences,
          boolean anyType, boolean doShow)
  {
    for (AlignmentAnnotation aa : al.getAlignmentAnnotation())
    {
      if (anyType || types.contains(aa.label))
      {
        if ((aa.sequenceRef != null)
                && (forSequences == null || forSequences
                        .contains(aa.sequenceRef)))
        {
          aa.visible = doShow;
        }
      }
    }
  }

  /**
   * Returns true if either sequence has a cross-reference to the other
   * 
   * @param seq1
   * @param seq2
   * @return
   */
  public static boolean haveCrossRef(SequenceI seq1, SequenceI seq2)
  {
    // Note: moved here from class CrossRef as the latter class has dependencies
    // not availability to the applet's classpath
    return hasCrossRef(seq1, seq2) || hasCrossRef(seq2, seq1);
  }

  /**
   * Returns true if seq1 has a cross-reference to seq2. Currently this assumes
   * that sequence name is structured as Source|AccessionId.
   * 
   * @param seq1
   * @param seq2
   * @return
   */
  public static boolean hasCrossRef(SequenceI seq1, SequenceI seq2)
  {
    if (seq1 == null || seq2 == null)
    {
      return false;
    }
    String name = seq2.getName();
    final DBRefEntry[] xrefs = seq1.getDBRef();
    if (xrefs != null)
    {
      for (DBRefEntry xref : xrefs)
      {
        String xrefName = xref.getSource() + "|" + xref.getAccessionId();
        // case-insensitive test, consistent with DBRefEntry.equalRef()
        if (xrefName.equalsIgnoreCase(name))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Constructs an alignment consisting of the mapped exon regions in the given
   * nucleotide sequences, and updates mappings to match.
   * 
   * @param dna
   *          aligned dna sequences
   * @param mappings
   *          from dna to protein; these are replaced with new mappings
   * @return an alignment whose sequences are the exon-only parts of the dna
   *         sequences (or null if no exons are found)
   */
  public static AlignmentI makeExonAlignment(SequenceI[] dna,
          Set<AlignedCodonFrame> mappings)
  {
    Set<AlignedCodonFrame> newMappings = new LinkedHashSet<AlignedCodonFrame>();
    List<SequenceI> exonSequences = new ArrayList<SequenceI>();

    for (SequenceI dnaSeq : dna)
    {
      final SequenceI ds = dnaSeq.getDatasetSequence();
      List<AlignedCodonFrame> seqMappings = MappingUtils
              .findMappingsForSequence(ds, mappings);
      for (AlignedCodonFrame acf : seqMappings)
      {
        AlignedCodonFrame newMapping = new AlignedCodonFrame();
        final List<SequenceI> mappedExons = makeExonSequences(ds, acf,
                newMapping);
        if (!mappedExons.isEmpty())
        {
          exonSequences.addAll(mappedExons);
          newMappings.add(newMapping);
        }
      }
    }
    AlignmentI al = new Alignment(
            exonSequences.toArray(new SequenceI[exonSequences.size()]));
    al.setDataset(null);

    /*
     * Replace the old mappings with the new ones
     */
    mappings.clear();
    mappings.addAll(newMappings);

    return al;
  }

  /**
   * Helper method to make exon-only sequences and populate their mappings to
   * protein products
   * <p>
   * For example, if ggCCaTTcGAg has mappings [3, 4, 6, 7, 9, 10] to protein
   * then generate a sequence CCTTGA with mapping [1, 6] to the same protein
   * residues
   * <p>
   * Typically eukaryotic dna will include exons encoding for a single peptide
   * sequence i.e. return a single result. Bacterial dna may have overlapping
   * exon mappings coding for multiple peptides so return multiple results
   * (example EMBL KF591215).
   * 
   * @param dnaSeq
   *          a dna dataset sequence
   * @param mapping
   *          containing one or more mappings of the sequence to protein
   * @param newMapping
   *          the new mapping to populate, from the exon-only sequences to their
   *          mapped protein sequences
   * @return
   */
  protected static List<SequenceI> makeExonSequences(SequenceI dnaSeq,
          AlignedCodonFrame mapping, AlignedCodonFrame newMapping)
  {
    List<SequenceI> exonSequences = new ArrayList<SequenceI>();
    List<Mapping> seqMappings = mapping.getMappingsForSequence(dnaSeq);
    final char[] dna = dnaSeq.getSequence();
    for (Mapping seqMapping : seqMappings)
    {
      StringBuilder newSequence = new StringBuilder(dnaSeq.getLength());

      /*
       * Get the codon regions as { [2, 5], [7, 12], [14, 14] etc }
       */
      final List<int[]> dnaExonRanges = seqMapping.getMap().getFromRanges();
      for (int[] range : dnaExonRanges)
      {
        for (int pos = range[0]; pos <= range[1]; pos++)
        {
          newSequence.append(dna[pos - 1]);
        }
      }

      SequenceI exon = new Sequence(dnaSeq.getName(),
              newSequence.toString());

      /*
       * Locate any xrefs to CDS database on the protein product and attach to
       * the CDS sequence. Also add as a sub-token of the sequence name.
       */
      // default to "CDS" if we can't locate an actual gene id
      String cdsAccId = FeatureProperties
              .getCodingFeature(DBRefSource.EMBL);
      DBRefEntry[] cdsRefs = DBRefUtils.selectRefs(seqMapping.getTo()
              .getDBRef(), DBRefSource.CODINGDBS);
      if (cdsRefs != null)
      {
        for (DBRefEntry cdsRef : cdsRefs)
        {
          exon.addDBRef(new DBRefEntry(cdsRef));
          cdsAccId = cdsRef.getAccessionId();
        }
      }
      exon.setName(exon.getName() + "|" + cdsAccId);
      exon.createDatasetSequence();

      /*
       * Build new mappings - from the same protein regions, but now to
       * contiguous exons
       */
      List<int[]> exonRange = new ArrayList<int[]>();
      exonRange.add(new int[] { 1, newSequence.length() });
      MapList map = new MapList(exonRange, seqMapping.getMap()
              .getToRanges(), 3, 1);
      newMapping.addMap(exon.getDatasetSequence(), seqMapping.getTo(), map);
      MapList cdsToDnaMap = new MapList(dnaExonRanges, exonRange, 1, 1);
      newMapping.addMap(dnaSeq, exon.getDatasetSequence(), cdsToDnaMap);

      exonSequences.add(exon);
    }
    return exonSequences;
  }
}
