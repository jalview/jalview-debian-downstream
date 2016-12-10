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

import java.util.BitSet;
import java.util.List;

/**
 * An interface describing the result of a search or other operation which
 * highlights matched regions of an alignment
 */
public interface SearchResultsI
{

  /**
   * Adds one region to the results
   * 
   * @param seq
   *          Sequence
   * @param start
   *          int
   * @param end
   *          int
   * @return
   */
  SearchResultMatchI addResult(SequenceI seq, int start, int end);

  /**
   * Answers true if the search results include the given sequence (or its
   * dataset sequence), else false
   * 
   * @param sequence
   * @return
   */
  boolean involvesSequence(SequenceI sequence);

  /**
   * Returns an array of [from, to, from, to..] matched columns (base 0) between
   * the given start and end columns of the given sequence. Returns null if no
   * matches overlap the specified region.
   * <p>
   * Implementations should provide an optimised method to return locations to
   * highlight on a visible portion of an alignment.
   * 
   * @param sequence
   * @param start
   *          first column of range (base 0, inclusive)
   * @param end
   *          last column of range base 0, inclusive)
   * @return int[]
   */
  int[] getResults(SequenceI sequence, int start, int end);

  /**
   * Returns the number of matches found
   * 
   * @return
   */
  int getSize();

  /**
   * Returns true if no search result matches are held.
   * 
   * @return
   */
  boolean isEmpty();

  /**
   * Returns the list of matches.
   * 
   * @return
   */
  List<SearchResultMatchI> getResults();

  /**
   * Set bits in a bitfield for all columns in the given sequence collection
   * that are highlighted
   * 
   * @param sqcol
   *          the set of sequences to search for highlighted regions
   * @param bs
   *          bitset to set
   * @return number of bits set
   */
  int markColumns(SequenceCollectionI sqcol, BitSet bs);
}