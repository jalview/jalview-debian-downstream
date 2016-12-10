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

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.stevesoft.pat.Regex;

public class Finder
{
  /**
   * Implements the search algorithms for the Find dialog box.
   */
  SearchResultsI searchResults;

  AlignmentI alignment;

  SequenceGroup selection = null;

  Vector<SequenceI> idMatch = null;

  boolean caseSensitive = false;

  private boolean includeDescription = false;

  boolean findAll = false;

  Regex regex = null;

  /**
   * holds last-searched position between calls to find(false)
   */
  int seqIndex = 0, resIndex = -1;

  public Finder(AlignmentI alignment, SequenceGroup selection)
  {
    this.alignment = alignment;
    this.selection = selection;
  }

  /**
   * restart search at given sequence and residue on alignment and (optionally)
   * contained in selection
   * 
   * @param alignment
   * @param selectionGroup
   * @param seqIndex
   * @param resIndex
   */
  public Finder(AlignmentI alignment, SequenceGroup selectionGroup,
          int seqIndex, int resIndex)
  {
    this(alignment, selectionGroup);
    this.seqIndex = seqIndex;
    this.resIndex = resIndex;
  }

  public boolean find(String searchString)
  {
    boolean hasResults = false;
    if (!caseSensitive)
    {
      searchString = searchString.toUpperCase();
    }
    regex = new Regex(searchString);
    regex.setIgnoreCase(!caseSensitive);
    searchResults = new SearchResults();
    idMatch = new Vector<SequenceI>();
    String item = null;
    boolean found = false;
    int end = alignment.getHeight();

    // /////////////////////////////////////////////

    if (selection != null)
    {
      if ((selection.getSize() < 1)
              || ((selection.getEndRes() - selection.getStartRes()) < 2))
      {
        selection = null;
      }
    }
    SearchResultMatchI lastm = null;

    while (!found && (seqIndex < end))
    {
      SequenceI seq = alignment.getSequenceAt(seqIndex);

      if ((selection != null && selection.getSize() > 0)
              && !selection.getSequences(null).contains(seq))
      {
        seqIndex++;
        resIndex = -1;

        continue;
      }
      if (resIndex < 0)
      {
        resIndex = 0;
        // test for one off matches - sequence position and sequence ID
        // //// is the searchString a residue number?
        try
        {
          int res = Integer.parseInt(searchString);
          // possibly a residue number - check if valid for seq
          if (seq.getEnd() >= res)
          {
            searchResults.addResult(seq, res, res);
            hasResults = true;
            // resIndex=seq.getLength();
            // seqIndex++;
            if (!findAll)
            {
              found = true;
              break;
            }
          }
        } catch (NumberFormatException ex)
        {
        }

        if (regex.search(seq.getName()) && !idMatch.contains(seq))
        {
          idMatch.addElement(seq);
          hasResults = true;
          if (!findAll)
          {
            // stop and return the match
            found = true;
            break;
          }
        }

        if (isIncludeDescription() && seq.getDescription() != null
                && regex.search(seq.getDescription())
                && !idMatch.contains(seq))
        {
          idMatch.addElement(seq);
          hasResults = true;
          if (!findAll)
          {
            // stop and return the match
            found = true;
            break;
          }
        }
      }
      item = seq.getSequenceAsString();

      if ((selection != null)
              && (selection.getEndRes() < alignment.getWidth() - 1))
      {
        item = item.substring(0, selection.getEndRes() + 1);
      }

      // /Shall we ignore gaps???? - JBPNote: Add Flag for forcing this or not
      StringBuilder noGapsSB = new StringBuilder();
      int insertCount = 0;
      List<Integer> spaces = new ArrayList<Integer>();

      for (int j = 0; j < item.length(); j++)
      {
        if (!Comparison.isGap(item.charAt(j)))
        {
          noGapsSB.append(item.charAt(j));
          spaces.add(Integer.valueOf(insertCount));
        }
        else
        {
          insertCount++;
        }
      }

      String noGaps = noGapsSB.toString();
      for (int r = resIndex; r < noGaps.length(); r++)
      {

        if (regex.searchFrom(noGaps, r))
        {
          resIndex = regex.matchedFrom();

          if ((selection != null && selection.getSize() > 0)
                  && (resIndex + spaces.get(resIndex) < selection
                          .getStartRes()))
          {
            continue;
          }
          // if invalid string used, then regex has no matched to/from
          int sres = seq.findPosition(resIndex + spaces.get(resIndex));
          int eres = seq.findPosition(regex.matchedTo() - 1
                  + (spaces.get(regex.matchedTo() - 1)));
          // only add result if not contained in previous result
          if (lastm == null
                  || (lastm.getSequence() != seq || (!(lastm.getStart() <= sres && lastm
                          .getEnd() >= eres))))
          {
            lastm = searchResults.addResult(seq, sres, eres);
          }
          hasResults = true;
          if (!findAll)
          {
            // thats enough, break and display the result
            found = true;
            resIndex++;

            break;
          }

          r = resIndex;
        }
        else
        {
          break;
        }
      }

      if (!found)
      {
        seqIndex++;
        resIndex = -1;
      }
    }

    /**
     * We now search the Id string in the main search loop. for (int id = 0; id
     * < alignment.getHeight(); id++) { if
     * (regex.search(alignment.getSequenceAt(id).getName())) {
     * idMatch.addElement(alignment.getSequenceAt(id)); hasResults = true; } }
     */
    return hasResults;
  }

  /**
   * @return the alignment
   */
  public AlignmentI getAlignment()
  {
    return alignment;
  }

  /**
   * @param alignment
   *          the alignment to set
   */
  public void setAlignment(AlignmentI alignment)
  {
    this.alignment = alignment;
  }

  /**
   * @return the caseSensitive
   */
  public boolean isCaseSensitive()
  {
    return caseSensitive;
  }

  /**
   * @param caseSensitive
   *          the caseSensitive to set
   */
  public void setCaseSensitive(boolean caseSensitive)
  {
    this.caseSensitive = caseSensitive;
  }

  /**
   * @return the findAll
   */
  public boolean isFindAll()
  {
    return findAll;
  }

  /**
   * @param findAll
   *          the findAll to set
   */
  public void setFindAll(boolean findAll)
  {
    this.findAll = findAll;
  }

  /**
   * @return the selection
   */
  public jalview.datamodel.SequenceGroup getSelection()
  {
    return selection;
  }

  /**
   * @param selection
   *          the selection to set
   */
  public void setSelection(jalview.datamodel.SequenceGroup selection)
  {
    this.selection = selection;
  }

  /**
   * Returns the (possibly empty) list of matching sequences (when search
   * includes searching sequence names)
   * 
   * @return
   */
  public Vector<SequenceI> getIdMatch()
  {
    return idMatch;
  }

  /**
   * @return the regex
   */
  public com.stevesoft.pat.Regex getRegex()
  {
    return regex;
  }

  /**
   * @return the searchResults
   */
  public SearchResultsI getSearchResults()
  {
    return searchResults;
  }

  /**
   * @return the resIndex
   */
  public int getResIndex()
  {
    return resIndex;
  }

  /**
   * @param resIndex
   *          the resIndex to set
   */
  public void setResIndex(int resIndex)
  {
    this.resIndex = resIndex;
  }

  /**
   * @return the seqIndex
   */
  public int getSeqIndex()
  {
    return seqIndex;
  }

  /**
   * @param seqIndex
   *          the seqIndex to set
   */
  public void setSeqIndex(int seqIndex)
  {
    this.seqIndex = seqIndex;
  }

  public boolean isIncludeDescription()
  {
    return includeDescription;
  }

  public void setIncludeDescription(boolean includeDescription)
  {
    this.includeDescription = includeDescription;
  }
}
