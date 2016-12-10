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
package jalview.datamodel;

import java.util.List;
import java.util.Vector;

import fr.orsay.lri.varna.models.rna.RNA;

/**
 * Methods for manipulating a sequence, its metadata and related annotation in
 * an alignment or dataset.
 * 
 * @author $author$
 * @version $Revision$
 */
public interface SequenceI extends ASequenceI
{
  /**
   * Set the display name for the sequence
   * 
   * @param name
   */
  public void setName(String name);

  /**
   * Get the display name
   */
  public String getName();

  /**
   * Set start position of first non-gapped symbol in sequence
   * 
   * @param start
   *          new start position
   */
  public void setStart(int start);

  /**
   * get start position of first non-gapped residue in sequence
   * 
   * @return
   */
  public int getStart();

  /**
   * get the displayed id of the sequence
   * 
   * @return true means the id will be returned in the form
   *         DisplayName/Start-End
   */
  public String getDisplayId(boolean jvsuffix);

  /**
   * set end position for last residue in sequence
   * 
   * @param end
   */
  public void setEnd(int end);

  /**
   * get end position for last residue in sequence getEnd()>getStart() unless
   * sequence only consists of gap characters
   * 
   * @return
   */
  public int getEnd();

  /**
   * @return length of sequence including gaps
   * 
   */
  public int getLength();

  /**
   * Replace the sequence with the given string
   * 
   * @param sequence
   *          new sequence string
   */
  public void setSequence(String sequence);

  /**
   * @return sequence as string
   */
  public String getSequenceAsString();

  /**
   * get a range on the sequence as a string
   * 
   * @param start
   *          position relative to start of sequence including gaps (from 0)
   * @param end
   *          position relative to start of sequence including gaps (from 0)
   * 
   * @return String containing all gap and symbols in specified range
   */
  public String getSequenceAsString(int start, int end);

  /**
   * Get the sequence as a character array
   * 
   * @return seqeunce and any gaps
   */
  public char[] getSequence();

  /**
   * get stretch of sequence characters in an array
   * 
   * @param start
   *          absolute index into getSequence()
   * @param end
   *          exclusive index of last position in segment to be returned.
   * 
   * @return char[max(0,end-start)];
   */
  public char[] getSequence(int start, int end);

  /**
   * create a new sequence object with a subsequence of this one but sharing the
   * same dataset sequence
   * 
   * @param start
   *          int index for start position (base 0, inclusive)
   * @param end
   *          int index for end position (base 0, exclusive)
   * 
   * @return SequenceI
   * @note implementations may use getSequence to get the sequence data
   */
  public SequenceI getSubSequence(int start, int end);

  /**
   * get the i'th character in this sequence's local reference frame (ie from
   * 0-number of characters lying from start-end)
   * 
   * @param i
   *          index
   * @return character or ' '
   */
  public char getCharAt(int i);

  /**
   * DOCUMENT ME!
   * 
   * @param desc
   *          DOCUMENT ME!
   */
  public void setDescription(String desc);

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getDescription();

  /**
   * Return the alignment column for a sequence position
   * 
   * @param pos
   *          lying from start to end
   * 
   * @return aligned column for residue (0 if residue is upstream from
   *         alignment, -1 if residue is downstream from alignment) note.
   *         Sequence object returns sequence.getEnd() for positions upstream
   *         currently. TODO: change sequence for
   *         assert(findIndex(seq.getEnd()+1)==-1) and fix incremental bugs
   * 
   */
  public int findIndex(int pos);

  /**
   * Returns the sequence position for an alignment position
   * 
   * @param i
   *          column index in alignment (from 0..<length)
   * 
   * @return residue number for residue (left of and) nearest ith column
   */
  public int findPosition(int i);

  /**
   * Returns an int array where indices correspond to each residue in the
   * sequence and the element value gives its position in the alignment
   * 
   * @return int[SequenceI.getEnd()-SequenceI.getStart()+1] or null if no
   *         residues in SequenceI object
   */
  public int[] gapMap();

  /**
   * Returns an int array where indices correspond to each position in sequence
   * char array and the element value gives the result of findPosition for that
   * index in the sequence.
   * 
   * @return int[SequenceI.getLength()]
   */
  public int[] findPositionMap();

  /**
   * Answers true if the sequence is composed of amino acid characters. Note
   * that implementations may use heuristic methods which are not guaranteed to
   * give the biologically 'right' answer.
   * 
   * @return
   */
  public boolean isProtein();

  /**
   * Delete a range of aligned sequence columns, creating a new dataset sequence
   * if necessary and adjusting start and end positions accordingly.
   * 
   * @param i
   *          first column in range to delete (inclusive)
   * @param j
   *          last column in range to delete (exclusive)
   */
  public void deleteChars(int i, int j);

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          alignment column number
   * @param c
   *          character to insert
   */
  public void insertCharAt(int i, char c);

  /**
   * insert given character at alignment column position
   * 
   * @param position
   *          alignment column number
   * @param count
   *          length of insert
   * @param ch
   *          character to insert
   */
  public void insertCharAt(int position, int count, char ch);

  /**
   * Gets array holding sequence features associated with this sequence. The
   * array may be held by the sequence's dataset sequence if that is defined.
   * 
   * @return hard reference to array
   */
  public SequenceFeature[] getSequenceFeatures();

  /**
   * Replaces the array of sequence features associated with this sequence with
   * a new array reference. If this sequence has a dataset sequence, then this
   * method will update the dataset sequence's feature array
   * 
   * @param features
   *          New array of sequence features
   */
  public void setSequenceFeatures(SequenceFeature[] features);

  /**
   * DOCUMENT ME!
   * 
   * @param id
   *          DOCUMENT ME!
   */
  public void setPDBId(Vector<PDBEntry> ids);

  /**
   * Returns a list
   * 
   * @return DOCUMENT ME!
   */
  public Vector<PDBEntry> getAllPDBEntries();

  /**
   * Adds the entry to the *normalised* list of PDBIds.
   * 
   * If a PDBEntry is passed with the same entry.getID() string as one already
   * in the list, or one is added that appears to be the same but has a chain ID
   * appended, then the existing PDBEntry will be updated with the new
   * attributes instead, unless the entries have distinct chain codes or
   * associated structure files.
   * 
   * @param entry
   * @return true if the entry was added, false if updated
   */
  public boolean addPDBId(PDBEntry entry);

  /**
   * update the list of PDBEntrys to include any DBRefEntrys citing structural
   * databases
   * 
   * @return true if PDBEntry list was modified
   */
  public boolean updatePDBIds();

  public String getVamsasId();

  public void setVamsasId(String id);

  /**
   * set the array of Database references for the sequence.
   * 
   * @param dbs
   * @deprecated - use is discouraged since side-effects may occur if DBRefEntry
   *             set are not normalised.
   */
  @Deprecated
  public void setDBRefs(DBRefEntry[] dbs);

  public DBRefEntry[] getDBRefs();

  /**
   * add the given entry to the list of DBRefs for this sequence, or replace a
   * similar one if entry contains a map object and the existing one doesnt.
   * 
   * @param entry
   */
  public void addDBRef(DBRefEntry entry);

  public void addSequenceFeature(SequenceFeature sf);

  public void deleteFeature(SequenceFeature sf);

  public void setDatasetSequence(SequenceI seq);

  public SequenceI getDatasetSequence();

  /**
   * Returns a new array containing this sequence's annotations, or null.
   */
  public AlignmentAnnotation[] getAnnotation();

  /**
   * Returns true if this sequence has the given annotation (by object
   * identity).
   */
  public boolean hasAnnotation(AlignmentAnnotation ann);

  /**
   * Add the given annotation, if not already added, and set its sequence ref to
   * be this sequence. Does nothing if this sequence's annotations already
   * include this annotation (by identical object reference).
   */
  public void addAlignmentAnnotation(AlignmentAnnotation annotation);

  public void removeAlignmentAnnotation(AlignmentAnnotation annotation);

  /**
   * Derive a sequence (using this one's dataset or as the dataset)
   * 
   * @return duplicate sequence with valid dataset sequence
   */
  public SequenceI deriveSequence();

  /**
   * set the array of associated AlignmentAnnotation for this sequenceI
   * 
   * @param revealed
   */
  public void setAlignmentAnnotation(AlignmentAnnotation[] annotation);

  /**
   * Get one or more alignment annotations with a particular label.
   * 
   * @param label
   *          string which each returned annotation must have as a label.
   * @return null or array of annotations.
   */
  public AlignmentAnnotation[] getAnnotation(String label);

  /**
   * Returns a (possibly empty) list of any annotations that match on given
   * calcId (source) and label (type). Null values do not match.
   * 
   * @param calcId
   * @param label
   */
  public List<AlignmentAnnotation> getAlignmentAnnotations(String calcId,
          String label);

  /**
   * create a new dataset sequence (if necessary) for this sequence and sets
   * this sequence to refer to it. This call will move any features or
   * references on the sequence onto the dataset. It will also make a duplicate
   * of existing annotation rows for the dataset sequence, rather than relocate
   * them in order to preserve external references (since 2.8.2).
   * 
   * @return dataset sequence for this sequence
   */
  public SequenceI createDatasetSequence();

  /**
   * Transfer any database references or annotation from entry under a sequence
   * mapping. <br/>
   * <strong>Note: DOES NOT transfer sequence associated alignment annotation
   * </strong><br/>
   * 
   * @param entry
   * @param mp
   *          null or mapping from entry's numbering to local start/end
   */
  public void transferAnnotation(SequenceI entry, Mapping mp);

  /**
   * @param index
   *          The sequence index in the MSA
   */
  public void setIndex(int index);

  /**
   * @return The index of the sequence in the alignment
   */
  public int getIndex();

  /**
   * @return The RNA of the sequence in the alignment
   */

  public RNA getRNA();

  /**
   * @param rna
   *          The RNA.
   */
  public void setRNA(RNA rna);

  /**
   * 
   * @return list of insertions (gap characters) in sequence
   */
  public List<int[]> getInsertions();

  /**
   * Given a pdbId String, return the equivalent PDBEntry if available in the
   * given sequence
   * 
   * @param pdbId
   * @return
   */
  public PDBEntry getPDBEntry(String pdbId);

  /**
   * Get all primary database/accessions for this sequence's data. These
   * DBRefEntry are expected to resolve to a valid record in the associated
   * external database, either directly or via a provided 1:1 Mapping.
   * 
   * @return just the primary references (if any) for this sequence, or an empty
   *         list
   */
  public List<DBRefEntry> getPrimaryDBRefs();
}
