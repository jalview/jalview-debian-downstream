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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds a list of search result matches, where each match is a contiguous
 * stretch of a single sequence.
 * 
 * @author gmcarstairs
 *
 */
public class SearchResults
{

  private List<Match> matches = new ArrayList<Match>();

  /**
   * One match consists of a sequence reference, start and end positions.
   * Discontiguous ranges in a sequence require two or more Match objects.
   */
  public class Match
  {
    SequenceI sequence;

    /**
     * Start position of match in sequence (base 1)
     */
    int start;

    /**
     * End position (inclusive) (base 1)
     */
    int end;

    /**
     * Constructor
     * 
     * @param seq
     *          a sequence
     * @param start
     *          start position of matched range (base 1)
     * @param end
     *          end of matched range (inclusive, base 1)
     */
    public Match(SequenceI seq, int start, int end)
    {
      sequence = seq;
      this.start = start;
      this.end = end;
    }

    public SequenceI getSequence()
    {
      return sequence;
    }

    public int getStart()
    {
      return start;
    }

    public int getEnd()
    {
      return end;
    }

    /**
     * Returns the string of characters in the matched region, prefixed by the
     * start position, e.g. "12CGT" or "208K"
     */
    @Override
    public String toString()
    {
      final int from = Math.max(start - 1, 0);
      String startPosition = String.valueOf(from);
      return startPosition + getCharacters();
    }

    /**
     * Returns the string of characters in the matched region.
     */
    public String getCharacters()
    {
      char[] chars = sequence.getSequence();
      // convert start/end to base 0 (with bounds check)
      final int from = Math.max(start - 1, 0);
      final int to = Math.min(end, chars.length + 1);
      return String.valueOf(Arrays.copyOfRange(chars, from, to));
    }

    public void setSequence(SequenceI seq)
    {
      this.sequence = seq;
    }

    /**
     * Hashcode is the hashcode of the matched sequence plus a hash of start and
     * end positions. Match objects that pass the test for equals are guaranteed
     * to have the same hashcode.
     */
    @Override
    public int hashCode()
    {
      int hash = sequence == null ? 0 : sequence.hashCode();
      hash += 31 * start;
      hash += 67 * end;
      return hash;
    }

    /**
     * Two Match objects are equal if they are for the same sequence, start and
     * end positions
     */
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof Match))
      {
        return false;
      }
      Match m = (Match) obj;
      return (this.sequence == m.sequence && this.start == m.start && this.end == m.end);
    }
  }

  /**
   * This method replaces the old search results which merely held an alignment
   * index of search matches. This broke when sequences were moved around the
   * alignment
   * 
   * @param seq
   *          Sequence
   * @param start
   *          int
   * @param end
   *          int
   */
  public void addResult(SequenceI seq, int start, int end)
  {
    matches.add(new Match(seq, start, end));
  }

  /**
   * Quickly check if the given sequence is referred to in the search results
   * 
   * @param sequence
   *          (specific alignment sequence or a dataset sequence)
   * @return true if the results involve sequence
   */
  public boolean involvesSequence(SequenceI sequence)
  {
    SequenceI ds = sequence.getDatasetSequence();
    for (Match m : matches)
    {
      if (m.sequence != null
              && (m.sequence == sequence || m.sequence == ds))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * This Method returns the search matches which lie between the start and end
   * points of the sequence in question. It is optimised for returning objects
   * for drawing on SequenceCanvas
   */
  public int[] getResults(SequenceI sequence, int start, int end)
  {
    if (matches.isEmpty())
    {
      return null;
    }

    int[] result = null;
    int[] tmp = null;
    int resultLength, matchStart = 0, matchEnd = 0;
    boolean mfound;
    for (Match m : matches)
    {
      mfound = false;
      if (m.sequence == sequence)
      {
        mfound = true;
        // locate aligned position
        matchStart = sequence.findIndex(m.start) - 1;
        matchEnd = sequence.findIndex(m.end) - 1;
      }
      else if (m.sequence == sequence.getDatasetSequence())
      {
        mfound = true;
        // locate region in local context
        matchStart = sequence.findIndex(m.start) - 1;
        matchEnd = sequence.findIndex(m.end) - 1;
      }
      if (mfound)
      {
        if (matchStart <= end && matchEnd >= start)
        {
          if (matchStart < start)
          {
            matchStart = start;
          }

          if (matchEnd > end)
          {
            matchEnd = end;
          }

          if (result == null)
          {
            result = new int[] { matchStart, matchEnd };
          }
          else
          {
            resultLength = result.length;
            tmp = new int[resultLength + 2];
            System.arraycopy(result, 0, tmp, 0, resultLength);
            result = tmp;
            result[resultLength] = matchStart;
            result[resultLength + 1] = matchEnd;
          }
        }
        else
        {
          // debug
          // System.err.println("Outwith bounds!" + matchStart+">"+end +"  or "
          // + matchEnd+"<"+start);
        }
      }
    }
    return result;
  }

  public int getSize()
  {
    return matches.size();
  }

  public SequenceI getResultSequence(int index)
  {
    return matches.get(index).sequence;
  }

  /**
   * Returns the start position of the i'th match in the search results.
   * 
   * @param i
   * @return
   */
  public int getResultStart(int i)
  {
    return matches.get(i).start;
  }

  /**
   * Returns the end position of the i'th match in the search results.
   * 
   * @param i
   * @return
   */
  public int getResultEnd(int i)
  {
    return matches.get(i).end;
  }

  /**
   * Returns true if no search result matches are held.
   * 
   * @return
   */
  public boolean isEmpty()
  {
    return matches.isEmpty();
  }

  /**
   * Returns the list of matches.
   * 
   * @return
   */
  public List<Match> getResults()
  {
    return matches;
  }

  /**
   * Return the results as a string of characters (bases) prefixed by start
   * position(s). Meant for use when the context ensures that all matches are to
   * regions of the same sequence (otherwise the result is meaningless).
   * 
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(256);
    for (Match m : matches)
    {
      result.append(m.toString());
    }
    return result.toString();
  }

  /**
   * Return the results as a string of characters (bases). Meant for use when
   * the context ensures that all matches are to regions of the same sequence
   * (otherwise the result is meaningless).
   * 
   * @return
   */
  public String getCharacters()
  {
    StringBuilder result = new StringBuilder(256);
    for (Match m : matches)
    {
      result.append(m.getCharacters());
    }
    return result.toString();
  }

  /**
   * Hashcode is has derived from the list of matches. This ensures that when
   * two SearchResults objects satisfy the test for equals(), then they have the
   * same hashcode.
   */
  @Override
  public int hashCode()
  {
    return matches.hashCode();
  }

  /**
   * Two SearchResults are considered equal if they contain the same matches in
   * the same order.
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof SearchResults))
    {
      return false;
    }
    SearchResults sr = (SearchResults) obj;
    return ((ArrayList<Match>) this.matches).equals(sr.matches);
  }
}
