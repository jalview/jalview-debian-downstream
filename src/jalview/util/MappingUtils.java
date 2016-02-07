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

import jalview.analysis.AlignmentSorter;
import jalview.api.AlignViewportI;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.commands.OrderCommand;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResults.Match;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper methods for manipulations involving sequence mappings.
 * 
 * @author gmcarstairs
 *
 */
public final class MappingUtils
{

  /**
   * Helper method to map a CUT or PASTE command.
   * 
   * @param edit
   *          the original command
   * @param undo
   *          if true, the command is to be undone
   * @param targetSeqs
   *          the mapped sequences to apply the mapped command to
   * @param result
   *          the mapped EditCommand to add to
   * @param mappings
   */
  protected static void mapCutOrPaste(Edit edit, boolean undo,
          List<SequenceI> targetSeqs, EditCommand result,
          Set<AlignedCodonFrame> mappings)
  {
    Action action = edit.getAction();
    if (undo)
    {
      action = action.getUndoAction();
    }
    // TODO write this
    System.err.println("MappingUtils.mapCutOrPaste not yet implemented");
  }

  /**
   * Returns a new EditCommand representing the given command as mapped to the
   * given sequences. If there is no mapping, returns null.
   * 
   * @param command
   * @param undo
   * @param mapTo
   * @param gapChar
   * @param mappings
   * @return
   */
  public static EditCommand mapEditCommand(EditCommand command,
          boolean undo, final AlignmentI mapTo, char gapChar,
          Set<AlignedCodonFrame> mappings)
  {
    /*
     * For now, only support mapping from protein edits to cDna
     */
    if (!mapTo.isNucleotide())
    {
      return null;
    }

    /*
     * Cache a copy of the target sequences so we can mimic successive edits on
     * them. This lets us compute mappings for all edits in the set.
     */
    Map<SequenceI, SequenceI> targetCopies = new HashMap<SequenceI, SequenceI>();
    for (SequenceI seq : mapTo.getSequences())
    {
      SequenceI ds = seq.getDatasetSequence();
      if (ds != null)
      {
        final SequenceI copy = new Sequence(seq);
        copy.setDatasetSequence(ds);
        targetCopies.put(ds, copy);
      }
    }

    /*
     * Compute 'source' sequences as they were before applying edits:
     */
    Map<SequenceI, SequenceI> originalSequences = command.priorState(undo);

    EditCommand result = new EditCommand();
    Iterator<Edit> edits = command.getEditIterator(!undo);
    while (edits.hasNext())
    {
      Edit edit = edits.next();
      if (edit.getAction() == Action.CUT
              || edit.getAction() == Action.PASTE)
      {
        mapCutOrPaste(edit, undo, mapTo.getSequences(), result, mappings);
      }
      else if (edit.getAction() == Action.INSERT_GAP
              || edit.getAction() == Action.DELETE_GAP)
      {
        mapInsertOrDelete(edit, undo, originalSequences,
                mapTo.getSequences(), targetCopies, gapChar, result,
                mappings);
      }
    }
    return result.getSize() > 0 ? result : null;
  }

  /**
   * Helper method to map an edit command to insert or delete gaps.
   * 
   * @param edit
   *          the original command
   * @param undo
   *          if true, the action is to undo the command
   * @param originalSequences
   *          the sequences the command acted on
   * @param targetSeqs
   * @param targetCopies
   * @param gapChar
   * @param result
   *          the new EditCommand to add mapped commands to
   * @param mappings
   */
  protected static void mapInsertOrDelete(Edit edit, boolean undo,
          Map<SequenceI, SequenceI> originalSequences,
          final List<SequenceI> targetSeqs,
          Map<SequenceI, SequenceI> targetCopies, char gapChar,
          EditCommand result, Set<AlignedCodonFrame> mappings)
  {
    Action action = edit.getAction();

    /*
     * Invert sense of action if an Undo.
     */
    if (undo)
    {
      action = action.getUndoAction();
    }
    final int count = edit.getNumber();
    final int editPos = edit.getPosition();
    for (SequenceI seq : edit.getSequences())
    {
      /*
       * Get residue position at (or to right of) edit location. Note we use our
       * 'copy' of the sequence before editing for this.
       */
      SequenceI ds = seq.getDatasetSequence();
      if (ds == null)
      {
        continue;
      }
      final SequenceI actedOn = originalSequences.get(ds);
      final int seqpos = actedOn.findPosition(editPos);

      /*
       * Determine all mappings from this position to mapped sequences.
       */
      SearchResults sr = buildSearchResults(seq, seqpos, mappings);

      if (!sr.isEmpty())
      {
        for (SequenceI targetSeq : targetSeqs)
        {
          ds = targetSeq.getDatasetSequence();
          if (ds == null)
          {
            continue;
          }
          SequenceI copyTarget = targetCopies.get(ds);
          final int[] match = sr.getResults(copyTarget, 0,
                  copyTarget.getLength());
          if (match != null)
          {
            final int ratio = 3; // TODO: compute this - how?
            final int mappedCount = count * ratio;

            /*
             * Shift Delete start position left, as it acts on positions to its
             * right.
             */
            int mappedEditPos = action == Action.DELETE_GAP ? match[0]
                    - mappedCount : match[0];
            Edit e = result.new Edit(action, new SequenceI[] { targetSeq },
                    mappedEditPos, mappedCount, gapChar);
            result.addEdit(e);

            /*
             * and 'apply' the edit to our copy of its target sequence
             */
            if (action == Action.INSERT_GAP)
            {
              copyTarget.setSequence(new String(StringUtils.insertCharAt(
                      copyTarget.getSequence(), mappedEditPos, mappedCount,
                      gapChar)));
            }
            else if (action == Action.DELETE_GAP)
            {
              copyTarget.setSequence(new String(StringUtils.deleteChars(
                      copyTarget.getSequence(), mappedEditPos,
                      mappedEditPos + mappedCount)));
            }
          }
        }
      }
      /*
       * and 'apply' the edit to our copy of its source sequence
       */
      if (action == Action.INSERT_GAP)
      {
        actedOn.setSequence(new String(StringUtils.insertCharAt(
                actedOn.getSequence(), editPos, count, gapChar)));
      }
      else if (action == Action.DELETE_GAP)
      {
        actedOn.setSequence(new String(StringUtils.deleteChars(
                actedOn.getSequence(), editPos, editPos + count)));
      }
    }
  }

  /**
   * Returns a SearchResults object describing the mapped region corresponding
   * to the specified sequence position.
   * 
   * @param seq
   * @param index
   * @param seqmappings
   * @return
   */
  public static SearchResults buildSearchResults(SequenceI seq, int index,
          Set<AlignedCodonFrame> seqmappings)
  {
    SearchResults results = new SearchResults();
    addSearchResults(results, seq, index, seqmappings);
    return results;
  }

  /**
   * Adds entries to a SearchResults object describing the mapped region
   * corresponding to the specified sequence position.
   * 
   * @param results
   * @param seq
   * @param index
   * @param seqmappings
   */
  public static void addSearchResults(SearchResults results, SequenceI seq,
          int index, Set<AlignedCodonFrame> seqmappings)
  {
    if (index >= seq.getStart() && index <= seq.getEnd())
    {
      for (AlignedCodonFrame acf : seqmappings)
      {
        acf.markMappedRegion(seq, index, results);
      }
    }
  }

  /**
   * Returns a (possibly empty) SequenceGroup containing any sequences in the
   * mapped viewport corresponding to the given group in the source viewport.
   * 
   * @param sg
   * @param mapFrom
   * @param mapTo
   * @return
   */
  public static SequenceGroup mapSequenceGroup(final SequenceGroup sg,
          final AlignViewportI mapFrom, final AlignViewportI mapTo)
  {
    /*
     * Note the SequenceGroup holds aligned sequences, the mappings hold dataset
     * sequences.
     */
    boolean targetIsNucleotide = mapTo.isNucleotide();
    AlignViewportI protein = targetIsNucleotide ? mapFrom : mapTo;
    Set<AlignedCodonFrame> codonFrames = protein.getAlignment()
            .getCodonFrames();
    /*
     * Copy group name, colours etc, but not sequences or sequence colour scheme
     */
    SequenceGroup mappedGroup = new SequenceGroup(sg);
    mappedGroup.cs = mapTo.getGlobalColourScheme();
    mappedGroup.clear();

    int minStartCol = -1;
    int maxEndCol = -1;
    final int selectionStartRes = sg.getStartRes();
    final int selectionEndRes = sg.getEndRes();
    for (SequenceI selected : sg.getSequences())
    {
      /*
       * Find the widest range of non-gapped positions in the selection range
       */
      int firstUngappedPos = selectionStartRes;
      while (firstUngappedPos <= selectionEndRes
              && Comparison.isGap(selected.getCharAt(firstUngappedPos)))
      {
        firstUngappedPos++;
      }

      /*
       * If this sequence is only gaps in the selected range, skip it
       */
      if (firstUngappedPos > selectionEndRes)
      {
        continue;
      }

      int lastUngappedPos = selectionEndRes;
      while (lastUngappedPos >= selectionStartRes
              && Comparison.isGap(selected.getCharAt(lastUngappedPos)))
      {
        lastUngappedPos--;
      }

      /*
       * Find the selected start/end residue positions in sequence
       */
      int startResiduePos = selected.findPosition(firstUngappedPos);
      int endResiduePos = selected.findPosition(lastUngappedPos);

      for (AlignedCodonFrame acf : codonFrames)
      {
        SequenceI mappedSequence = targetIsNucleotide ? acf
                .getDnaForAaSeq(selected) : acf.getAaForDnaSeq(selected);
        if (mappedSequence != null)
        {
          for (SequenceI seq : mapTo.getAlignment().getSequences())
          {
            int mappedStartResidue = 0;
            int mappedEndResidue = 0;
            if (seq.getDatasetSequence() == mappedSequence)
            {
              /*
               * Found a sequence mapping. Locate the start/end mapped residues.
               */
              SearchResults sr = buildSearchResults(selected,
                      startResiduePos, Collections.singleton(acf));
              for (Match m : sr.getResults())
              {
                mappedStartResidue = m.getStart();
                mappedEndResidue = m.getEnd();
              }
              sr = buildSearchResults(selected, endResiduePos,
                      Collections.singleton(acf));
              for (Match m : sr.getResults())
              {
                mappedStartResidue = Math.min(mappedStartResidue,
                        m.getStart());
                mappedEndResidue = Math.max(mappedEndResidue, m.getEnd());
              }

              /*
               * Find the mapped aligned columns, save the range. Note findIndex
               * returns a base 1 position, SequenceGroup uses base 0
               */
              int mappedStartCol = seq.findIndex(mappedStartResidue) - 1;
              minStartCol = minStartCol == -1 ? mappedStartCol : Math.min(
                      minStartCol, mappedStartCol);
              int mappedEndCol = seq.findIndex(mappedEndResidue) - 1;
              maxEndCol = maxEndCol == -1 ? mappedEndCol : Math.max(
                      maxEndCol, mappedEndCol);
              mappedGroup.addSequence(seq, false);
              break;
            }
          }
        }
      }
    }
    mappedGroup.setStartRes(minStartCol < 0 ? 0 : minStartCol);
    mappedGroup.setEndRes(maxEndCol < 0 ? 0 : maxEndCol);
    return mappedGroup;
  }

  /**
   * Returns an OrderCommand equivalent to the given one, but acting on mapped
   * sequences as described by the mappings, or null if no mapping can be made.
   * 
   * @param command
   *          the original order command
   * @param undo
   *          if true, the action is to undo the sort
   * @param mapTo
   *          the alignment we are mapping to
   * @param mappings
   *          the mappings available
   * @return
   */
  public static CommandI mapOrderCommand(OrderCommand command,
          boolean undo, AlignmentI mapTo, Set<AlignedCodonFrame> mappings)
  {
    SequenceI[] sortOrder = command.getSequenceOrder(undo);
    List<SequenceI> mappedOrder = new ArrayList<SequenceI>();
    int j = 0;

    /*
     * Assumption: we are only interested in a cDNA/protein mapping; refactor in
     * future if we want to support sorting (c)dna as (c)dna or protein as
     * protein
     */
    boolean mappingToNucleotide = mapTo.isNucleotide();
    for (SequenceI seq : sortOrder)
    {
      for (AlignedCodonFrame acf : mappings)
      {
        SequenceI mappedSeq = mappingToNucleotide ? acf.getDnaForAaSeq(seq)
                : acf.getAaForDnaSeq(seq);
        if (mappedSeq != null)
        {
          for (SequenceI seq2 : mapTo.getSequences())
          {
            if (seq2.getDatasetSequence() == mappedSeq)
            {
              mappedOrder.add(seq2);
              j++;
              break;
            }
          }
        }
      }
    }

    /*
     * Return null if no mappings made.
     */
    if (j == 0)
    {
      return null;
    }

    /*
     * Add any unmapped sequences on the end of the sort in their original
     * ordering.
     */
    if (j < mapTo.getHeight())
    {
      for (SequenceI seq : mapTo.getSequences())
      {
        if (!mappedOrder.contains(seq))
        {
          mappedOrder.add(seq);
        }
      }
    }

    /*
     * Have to sort the sequences before constructing the OrderCommand - which
     * then resorts them?!?
     */
    final SequenceI[] mappedOrderArray = mappedOrder
            .toArray(new SequenceI[mappedOrder.size()]);
    SequenceI[] oldOrder = mapTo.getSequencesArray();
    AlignmentSorter.sortBy(mapTo, new AlignmentOrder(mappedOrderArray));
    final OrderCommand result = new OrderCommand(command.getDescription(),
            oldOrder, mapTo);
    return result;
  }

  /**
   * Returns a ColumnSelection in the 'mapTo' view which corresponds to the
   * given selection in the 'mapFrom' view. We assume one is nucleotide, the
   * other is protein (and holds the mappings from codons to protein residues).
   * 
   * @param colsel
   * @param mapFrom
   * @param mapTo
   * @return
   */
  public static ColumnSelection mapColumnSelection(ColumnSelection colsel,
          AlignViewportI mapFrom, AlignViewportI mapTo)
  {
    boolean targetIsNucleotide = mapTo.isNucleotide();
    AlignViewportI protein = targetIsNucleotide ? mapFrom : mapTo;
    Set<AlignedCodonFrame> codonFrames = protein.getAlignment()
            .getCodonFrames();
    ColumnSelection mappedColumns = new ColumnSelection();

    if (colsel == null)
    {
      return mappedColumns;
    }

    char fromGapChar = mapFrom.getAlignment().getGapCharacter();

    // FIXME allow for hidden columns

    /*
     * For each mapped column, find the range of columns that residues in that
     * column map to.
     */
    for (Object obj : colsel.getSelected())
    {
      int col = ((Integer) obj).intValue();
      int mappedToMin = Integer.MAX_VALUE;
      int mappedToMax = Integer.MIN_VALUE;

      /*
       * For each sequence in the 'from' alignment
       */
      for (SequenceI fromSeq : mapFrom.getAlignment().getSequences())
      {
        /*
         * Ignore gaps (unmapped anyway)
         */
        if (fromSeq.getCharAt(col) == fromGapChar)
        {
          continue;
        }

        /*
         * Get the residue position and find the mapped position.
         */
        int residuePos = fromSeq.findPosition(col);
        SearchResults sr = buildSearchResults(fromSeq, residuePos,
                codonFrames);
        for (Match m : sr.getResults())
        {
          int mappedStartResidue = m.getStart();
          int mappedEndResidue = m.getEnd();
          SequenceI mappedSeq = m.getSequence();

          /*
           * Locate the aligned sequence whose dataset is mappedSeq. TODO a
           * datamodel that can do this efficiently.
           */
          for (SequenceI toSeq : mapTo.getAlignment().getSequences())
          {
            if (toSeq.getDatasetSequence() == mappedSeq)
            {
              int mappedStartCol = toSeq.findIndex(mappedStartResidue);
              int mappedEndCol = toSeq.findIndex(mappedEndResidue);
              mappedToMin = Math.min(mappedToMin, mappedStartCol);
              mappedToMax = Math.max(mappedToMax, mappedEndCol);
              // System.out.println(fromSeq.getName() + " mapped to cols "
              // + mappedStartCol + ":" + mappedEndCol);
              break;
              // note: remove break if we ever want to map one to many sequences
            }
          }
        }
      }
      /*
       * Add the range of mapped columns to the mapped selection (converting
       * base 1 to base 0). Note that this may include intron-only regions which
       * lie between the start and end ranges of the selection.
       */
      for (int i = mappedToMin; i <= mappedToMax; i++)
      {
        mappedColumns.addElement(i - 1);
      }
    }
    return mappedColumns;
  }

  /**
   * Returns the mapped codon for a given aligned sequence column position (base
   * 0).
   * 
   * @param seq
   *          an aligned peptide sequence
   * @param col
   *          an aligned column position (base 0)
   * @param mappings
   *          a set of codon mappings
   * @return the bases of the mapped codon in the cDNA dataset sequence, or null
   *         if not found
   */
  public static char[] findCodonFor(SequenceI seq, int col,
          Set<AlignedCodonFrame> mappings)
  {
    int dsPos = seq.findPosition(col);
    for (AlignedCodonFrame mapping : mappings)
    {
      if (mapping.involvesSequence(seq))
      {
        return mapping.getMappedCodon(seq.getDatasetSequence(), dsPos);
      }
    }
    return null;
  }

  /**
   * Converts a series of [start, end] ranges into an array of individual
   * positions.
   * 
   * @param ranges
   * @return
   */
  public static int[] flattenRanges(int[] ranges)
  {
    /*
     * Count how many positions altogether
     */
    int count = 0;
    for (int i = 0; i < ranges.length - 1; i += 2)
    {
      count += ranges[i + 1] - ranges[i] + 1;
    }

    int[] result = new int[count];
    int k = 0;
    for (int i = 0; i < ranges.length - 1; i += 2)
    {
      for (int j = ranges[i]; j <= ranges[i + 1]; j++)
      {
        result[k++] = j;
      }
    }
    return result;
  }

  /**
   * Returns a list of any mappings that are from or to the given (aligned or
   * dataset) sequence.
   * 
   * @param sequence
   * @param mappings
   * @return
   */
  public static List<AlignedCodonFrame> findMappingsForSequence(
          SequenceI sequence, Set<AlignedCodonFrame> mappings)
  {
    List<AlignedCodonFrame> result = new ArrayList<AlignedCodonFrame>();
    if (sequence == null || mappings == null)
    {
      return result;
    }
    for (AlignedCodonFrame mapping : mappings)
    {
      if (mapping.involvesSequence(sequence))
      {
        result.add(mapping);
      }
    }
    return result;
  }
}
