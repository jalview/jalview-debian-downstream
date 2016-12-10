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

import jalview.util.Comparison;
import jalview.util.ShiftList;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter.SearchableAnnotationField;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Data class holding the selected columns and hidden column ranges for a view.
 * Ranges are base 1.
 */
public class ColumnSelection
{
  /**
   * A class to hold an efficient representation of selected columns
   */
  private class IntList
  {
    /*
     * list of selected columns (ordered by selection order, not column order)
     */
    private List<Integer> order;

    /*
     * an unmodifiable view of the selected columns list
     */
    private List<Integer> _uorder;

    /**
     * bitfield for column selection - allows quick lookup
     */
    private BitSet selected;

    /**
     * Constructor
     */
    IntList()
    {
      order = new ArrayList<Integer>();
      _uorder = Collections.unmodifiableList(order);
      selected = new BitSet();
    }

    /**
     * Copy constructor
     * 
     * @param other
     */
    IntList(IntList other)
    {
      this();
      if (other != null)
      {
        int j = other.size();
        for (int i = 0; i < j; i++)
        {
          add(other.elementAt(i));
        }
      }
    }

    /**
     * adds a new column i to the selection - only if i is not already selected
     * 
     * @param i
     */
    void add(int i)
    {
      if (!selected.get(i))
      {
        order.add(Integer.valueOf(i));
        selected.set(i);
      }
    }

    void clear()
    {
      order.clear();
      selected.clear();
    }

    void remove(int col)
    {

      Integer colInt = new Integer(col);

      if (selected.get(col))
      {
        // if this ever changes to List.remove(), ensure Integer not int
        // argument
        // as List.remove(int i) removes the i'th item which is wrong
        order.remove(colInt);
        selected.clear(col);
      }
    }

    boolean contains(Integer colInt)
    {
      return selected.get(colInt);
    }

    boolean isEmpty()
    {
      return order.isEmpty();
    }

    /**
     * Returns a read-only view of the selected columns list
     * 
     * @return
     */
    List<Integer> getList()
    {
      return _uorder;
    }

    int size()
    {
      return order.size();
    }

    /**
     * gets the column that was selected first, second or i'th
     * 
     * @param i
     * @return
     */
    int elementAt(int i)
    {
      return order.get(i);
    }

    protected boolean pruneColumnList(final List<int[]> shifts)
    {
      int s = 0, t = shifts.size();
      int[] sr = shifts.get(s++);
      boolean pruned = false;
      int i = 0, j = order.size();
      while (i < j && s <= t)
      {
        int c = order.get(i++).intValue();
        if (sr[0] <= c)
        {
          if (sr[1] + sr[0] >= c)
          { // sr[1] -ve means inseriton.
            order.remove(--i);
            selected.clear(c);
            j--;
          }
          else
          {
            if (s < t)
            {
              sr = shifts.get(s);
            }
            s++;
          }
        }
      }
      return pruned;
    }

    /**
     * shift every selected column at or above start by change
     * 
     * @param start
     *          - leftmost column to be shifted
     * @param change
     *          - delta for shift
     */
    void compensateForEdits(int start, int change)
    {
      BitSet mask = new BitSet();
      for (int i = 0; i < order.size(); i++)
      {
        int temp = order.get(i);

        if (temp >= start)
        {
          // clear shifted bits and update List of selected columns
          selected.clear(temp);
          mask.set(temp - change);
          order.set(i, new Integer(temp - change));
        }
      }
      // lastly update the bitfield all at once
      selected.or(mask);
    }

    boolean isSelected(int column)
    {
      return selected.get(column);
    }

    int getMaxColumn()
    {
      return selected.length() - 1;
    }

    int getMinColumn()
    {
      return selected.get(0) ? 0 : selected.nextSetBit(0);
    }

    /**
     * @return a series of selection intervals along the range
     */
    List<int[]> getRanges()
    {
      List<int[]> rlist = new ArrayList<int[]>();
      if (selected.isEmpty())
      {
        return rlist;
      }
      int next = selected.nextSetBit(0), clear = -1;
      while (next != -1)
      {
        clear = selected.nextClearBit(next);
        rlist.add(new int[] { next, clear - 1 });
        next = selected.nextSetBit(clear);
      }
      return rlist;
    }

    @Override
    public int hashCode()
    {
      // TODO Auto-generated method stub
      return selected.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof IntList)
      {
        return ((IntList) obj).selected.equals(selected);
      }
      return false;
    }
  }

  IntList selection = new IntList();

  /*
   * list of hidden column [start, end] ranges; the list is maintained in
   * ascending start column order
   */
  Vector<int[]> hiddenColumns;

  /**
   * Add a column to the selection
   * 
   * @param col
   *          index of column
   */
  public void addElement(int col)
  {
    selection.add(col);
  }

  /**
   * clears column selection
   */
  public void clear()
  {
    selection.clear();
  }

  /**
   * Removes value 'col' from the selection (not the col'th item)
   * 
   * @param col
   *          index of column to be removed
   */
  public void removeElement(int col)
  {
    selection.remove(col);
  }

  /**
   * removes a range of columns from the selection
   * 
   * @param start
   *          int - first column in range to be removed
   * @param end
   *          int - last col
   */
  public void removeElements(int start, int end)
  {
    Integer colInt;
    for (int i = start; i < end; i++)
    {
      colInt = new Integer(i);
      if (selection.contains(colInt))
      {
        selection.remove(colInt);
      }
    }
  }

  /**
   * Returns a read-only view of the (possibly empty) list of selected columns
   * <p>
   * The list contains no duplicates but is not necessarily ordered. It also may
   * include columns hidden from the current view. To modify (for example sort)
   * the list, you should first make a copy.
   * <p>
   * The list is not thread-safe: iterating over it could result in
   * ConcurrentModificationException if it is modified by another thread.
   */
  public List<Integer> getSelected()
  {
    return selection.getList();
  }

  /**
   * @return list of int arrays containing start and end column position for
   *         runs of selected columns ordered from right to left.
   */
  public List<int[]> getSelectedRanges()
  {
    return selection.getRanges();
  }

  /**
   * 
   * @param col
   *          index to search for in column selection
   * 
   * @return true if col is selected
   */
  public boolean contains(int col)
  {
    return (col > -1) ? selection.isSelected(col) : false;
  }

  /**
   * Answers true if no columns are selected, else false
   */
  public boolean isEmpty()
  {
    return selection == null || selection.isEmpty();
  }

  /**
   * rightmost selected column
   * 
   * @return rightmost column in alignment that is selected
   */
  public int getMax()
  {
    if (selection.isEmpty())
    {
      return -1;
    }
    return selection.getMaxColumn();
  }

  /**
   * Leftmost column in selection
   * 
   * @return column index of leftmost column in selection
   */
  public int getMin()
  {
    if (selection.isEmpty())
    {
      return 1000000000;
    }
    return selection.getMinColumn();
  }

  /**
   * propagate shift in alignment columns to column selection
   * 
   * @param start
   *          beginning of edit
   * @param left
   *          shift in edit (+ve for removal, or -ve for inserts)
   */
  public List<int[]> compensateForEdit(int start, int change)
  {
    List<int[]> deletedHiddenColumns = null;
    selection.compensateForEdits(start, change);

    if (hiddenColumns != null)
    {
      deletedHiddenColumns = new ArrayList<int[]>();
      int hSize = hiddenColumns.size();
      for (int i = 0; i < hSize; i++)
      {
        int[] region = hiddenColumns.elementAt(i);
        if (region[0] > start && start + change > region[1])
        {
          deletedHiddenColumns.add(region);

          hiddenColumns.removeElementAt(i);
          i--;
          hSize--;
          continue;
        }

        if (region[0] > start)
        {
          region[0] -= change;
          region[1] -= change;
        }

        if (region[0] < 0)
        {
          region[0] = 0;
        }

      }

      this.revealHiddenColumns(0);
    }

    return deletedHiddenColumns;
  }

  /**
   * propagate shift in alignment columns to column selection special version of
   * compensateForEdit - allowing for edits within hidden regions
   * 
   * @param start
   *          beginning of edit
   * @param left
   *          shift in edit (+ve for removal, or -ve for inserts)
   */
  private void compensateForDelEdits(int start, int change)
  {

    selection.compensateForEdits(start, change);

    if (hiddenColumns != null)
    {
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = hiddenColumns.elementAt(i);
        if (region[0] >= start)
        {
          region[0] -= change;
        }
        if (region[1] >= start)
        {
          region[1] -= change;
        }
        if (region[1] < region[0])
        {
          hiddenColumns.removeElementAt(i--);
        }

        if (region[0] < 0)
        {
          region[0] = 0;
        }
        if (region[1] < 0)
        {
          region[1] = 0;
        }
      }
    }
  }

  /**
   * Adjust hidden column boundaries based on a series of column additions or
   * deletions in visible regions.
   * 
   * @param shiftrecord
   * @return
   */
  public ShiftList compensateForEdits(ShiftList shiftrecord)
  {
    if (shiftrecord != null)
    {
      final List<int[]> shifts = shiftrecord.getShifts();
      if (shifts != null && shifts.size() > 0)
      {
        int shifted = 0;
        for (int i = 0, j = shifts.size(); i < j; i++)
        {
          int[] sh = shifts.get(i);
          // compensateForEdit(shifted+sh[0], sh[1]);
          compensateForDelEdits(shifted + sh[0], sh[1]);
          shifted -= sh[1];
        }
      }
      return shiftrecord.getInverse();
    }
    return null;
  }

  /**
   * removes intersection of position,length ranges in deletions from the
   * start,end regions marked in intervals.
   * 
   * @param shifts
   * @param intervals
   * @return
   */
  private boolean pruneIntervalVector(final List<int[]> shifts,
          Vector<int[]> intervals)
  {
    boolean pruned = false;
    int i = 0, j = intervals.size() - 1, s = 0, t = shifts.size() - 1;
    int hr[] = intervals.elementAt(i);
    int sr[] = shifts.get(s);
    while (i <= j && s <= t)
    {
      boolean trailinghn = hr[1] >= sr[0];
      if (!trailinghn)
      {
        if (i < j)
        {
          hr = intervals.elementAt(++i);
        }
        else
        {
          i++;
        }
        continue;
      }
      int endshift = sr[0] + sr[1]; // deletion ranges - -ve means an insert
      if (endshift < hr[0] || endshift < sr[0])
      { // leadinghc disjoint or not a deletion
        if (s < t)
        {
          sr = shifts.get(++s);
        }
        else
        {
          s++;
        }
        continue;
      }
      boolean leadinghn = hr[0] >= sr[0];
      boolean leadinghc = hr[0] < endshift;
      boolean trailinghc = hr[1] < endshift;
      if (leadinghn)
      {
        if (trailinghc)
        { // deleted hidden region.
          intervals.removeElementAt(i);
          pruned = true;
          j--;
          if (i <= j)
          {
            hr = intervals.elementAt(i);
          }
          continue;
        }
        if (leadinghc)
        {
          hr[0] = endshift; // clip c terminal region
          leadinghn = !leadinghn;
          pruned = true;
        }
      }
      if (!leadinghn)
      {
        if (trailinghc)
        {
          if (trailinghn)
          {
            hr[1] = sr[0] - 1;
            pruned = true;
          }
        }
        else
        {
          // sr contained in hr
          if (s < t)
          {
            sr = shifts.get(++s);
          }
          else
          {
            s++;
          }
          continue;
        }
      }
    }
    return pruned; // true if any interval was removed or modified by
    // operations.
  }

  /**
   * remove any hiddenColumns or selected columns and shift remaining based on a
   * series of position, range deletions.
   * 
   * @param deletions
   */
  public void pruneDeletions(ShiftList deletions)
  {
    if (deletions != null)
    {
      final List<int[]> shifts = deletions.getShifts();
      if (shifts != null && shifts.size() > 0)
      {
        // delete any intervals intersecting.
        if (hiddenColumns != null)
        {
          pruneIntervalVector(shifts, hiddenColumns);
          if (hiddenColumns != null && hiddenColumns.size() == 0)
          {
            hiddenColumns = null;
          }
        }
        if (selection != null && selection.size() > 0)
        {
          selection.pruneColumnList(shifts);
          if (selection != null && selection.size() == 0)
          {
            selection = null;
          }
        }
        // and shift the rest.
        this.compensateForEdits(deletions);
      }
    }
  }

  /**
   * This Method is used to return all the HiddenColumn regions
   * 
   * @return empty list or List of hidden column intervals
   */
  public List<int[]> getHiddenColumns()
  {
    return hiddenColumns == null ? Collections.<int[]> emptyList()
            : hiddenColumns;
  }

  /**
   * Return absolute column index for a visible column index
   * 
   * @param column
   *          int column index in alignment view (count from zero)
   * @return alignment column index for column
   */
  public int adjustForHiddenColumns(int column)
  {
    int result = column;
    if (hiddenColumns != null)
    {
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = hiddenColumns.elementAt(i);
        if (result >= region[0])
        {
          result += region[1] - region[0] + 1;
        }
      }
    }
    return result;
  }

  /**
   * Use this method to find out where a column will appear in the visible
   * alignment when hidden columns exist. If the column is not visible, then the
   * left-most visible column will always be returned.
   * 
   * @param hiddenColumn
   *          int
   * @return int
   */
  public int findColumnPosition(int hiddenColumn)
  {
    int result = hiddenColumn;
    if (hiddenColumns != null)
    {
      int index = 0;
      int[] region;
      do
      {
        region = hiddenColumns.elementAt(index++);
        if (hiddenColumn > region[1])
        {
          result -= region[1] + 1 - region[0];
        }
      } while ((hiddenColumn > region[1]) && (index < hiddenColumns.size()));
      if (hiddenColumn > region[0] && hiddenColumn < region[1])
      {
        return region[0] + hiddenColumn - result;
      }
    }
    return result; // return the shifted position after removing hidden columns.
  }

  /**
   * Use this method to determine where the next hiddenRegion starts
   * 
   * @param hiddenRegion
   *          index of hidden region (counts from 0)
   * @return column number in visible view
   */
  public int findHiddenRegionPosition(int hiddenRegion)
  {
    int result = 0;
    if (hiddenColumns != null)
    {
      int index = 0;
      int gaps = 0;
      do
      {
        int[] region = hiddenColumns.elementAt(index);
        if (hiddenRegion == 0)
        {
          return region[0];
        }

        gaps += region[1] + 1 - region[0];
        result = region[1] + 1;
        index++;
      } while (index <= hiddenRegion);

      result -= gaps;
    }

    return result;
  }

  /**
   * THis method returns the rightmost limit of a region of an alignment with
   * hidden columns. In otherwords, the next hidden column.
   * 
   * @param index
   *          int
   */
  public int getHiddenBoundaryRight(int alPos)
  {
    if (hiddenColumns != null)
    {
      int index = 0;
      do
      {
        int[] region = hiddenColumns.elementAt(index);
        if (alPos < region[0])
        {
          return region[0];
        }

        index++;
      } while (index < hiddenColumns.size());
    }

    return alPos;

  }

  /**
   * This method returns the leftmost limit of a region of an alignment with
   * hidden columns. In otherwords, the previous hidden column.
   * 
   * @param index
   *          int
   */
  public int getHiddenBoundaryLeft(int alPos)
  {
    if (hiddenColumns != null)
    {
      int index = hiddenColumns.size() - 1;
      do
      {
        int[] region = hiddenColumns.elementAt(index);
        if (alPos > region[1])
        {
          return region[1];
        }

        index--;
      } while (index > -1);
    }

    return alPos;

  }

  public void hideSelectedColumns()
  {
    synchronized (selection)
    {
      for (int[] selregions : selection.getRanges())
      {
        hideColumns(selregions[0], selregions[1]);
      }
      selection.clear();
    }

  }

  /**
   * Adds the specified column range to the hidden columns
   * 
   * @param start
   * @param end
   */
  public void hideColumns(int start, int end)
  {
    if (hiddenColumns == null)
    {
      hiddenColumns = new Vector<int[]>();
    }

    /*
     * traverse existing hidden ranges and insert / amend / append as
     * appropriate
     */
    for (int i = 0; i < hiddenColumns.size(); i++)
    {
      int[] region = hiddenColumns.elementAt(i);

      if (end < region[0] - 1)
      {
        /*
         * insert discontiguous preceding range
         */
        hiddenColumns.insertElementAt(new int[] { start, end }, i);
        return;
      }

      if (end <= region[1])
      {
        /*
         * new range overlaps existing, or is contiguous preceding it - adjust
         * start column
         */
        region[0] = Math.min(region[0], start);
        return;
      }

      if (start <= region[1] + 1)
      {
        /*
         * new range overlaps existing, or is contiguous following it - adjust
         * start and end columns
         */
        region[0] = Math.min(region[0], start);
        region[1] = Math.max(region[1], end);
        return;
      }
    }

    /*
     * remaining case is that the new range follows everything else
     */
    hiddenColumns.addElement(new int[] { start, end });
  }

  /**
   * Hides the specified column and any adjacent selected columns
   * 
   * @param res
   *          int
   */
  public void hideColumns(int col)
  {
    /*
     * deselect column (whether selected or not!)
     */
    removeElement(col);

    /*
     * find adjacent selected columns
     */
    int min = col - 1, max = col + 1;
    while (contains(min))
    {
      removeElement(min);
      min--;
    }

    while (contains(max))
    {
      removeElement(max);
      max++;
    }

    /*
     * min, max are now the closest unselected columns
     */
    min++;
    max--;
    if (min > max)
    {
      min = max;
    }

    hideColumns(min, max);
  }

  /**
   * Unhides, and adds to the selection list, all hidden columns
   */
  public void revealAllHiddenColumns()
  {
    if (hiddenColumns != null)
    {
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = hiddenColumns.elementAt(i);
        for (int j = region[0]; j < region[1] + 1; j++)
        {
          addElement(j);
        }
      }
    }

    hiddenColumns = null;
  }

  /**
   * Reveals, and marks as selected, the hidden column range with the given
   * start column
   * 
   * @param start
   */
  public void revealHiddenColumns(int start)
  {
    for (int i = 0; i < hiddenColumns.size(); i++)
    {
      int[] region = hiddenColumns.elementAt(i);
      if (start == region[0])
      {
        for (int j = region[0]; j < region[1] + 1; j++)
        {
          addElement(j);
        }

        hiddenColumns.removeElement(region);
        break;
      }
    }
    if (hiddenColumns.size() == 0)
    {
      hiddenColumns = null;
    }
  }

  public boolean isVisible(int column)
  {
    if (hiddenColumns != null)
    {
      for (int[] region : hiddenColumns)
      {
        if (column >= region[0] && column <= region[1])
        {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Copy constructor
   * 
   * @param copy
   */
  public ColumnSelection(ColumnSelection copy)
  {
    if (copy != null)
    {
      selection = new IntList(copy.selection);
      if (copy.hiddenColumns != null)
      {
        hiddenColumns = new Vector<int[]>(copy.hiddenColumns.size());
        for (int i = 0, j = copy.hiddenColumns.size(); i < j; i++)
        {
          int[] rh, cp;
          rh = copy.hiddenColumns.elementAt(i);
          if (rh != null)
          {
            cp = new int[rh.length];
            System.arraycopy(rh, 0, cp, 0, rh.length);
            hiddenColumns.addElement(cp);
          }
        }
      }
    }
  }

  /**
   * ColumnSelection
   */
  public ColumnSelection()
  {
  }

  public String[] getVisibleSequenceStrings(int start, int end,
          SequenceI[] seqs)
  {
    int i, iSize = seqs.length;
    String selections[] = new String[iSize];
    if (hiddenColumns != null && hiddenColumns.size() > 0)
    {
      for (i = 0; i < iSize; i++)
      {
        StringBuffer visibleSeq = new StringBuffer();
        List<int[]> regions = getHiddenColumns();

        int blockStart = start, blockEnd = end;
        int[] region;
        int hideStart, hideEnd;

        for (int j = 0; j < regions.size(); j++)
        {
          region = regions.get(j);
          hideStart = region[0];
          hideEnd = region[1];

          if (hideStart < start)
          {
            continue;
          }

          blockStart = Math.min(blockStart, hideEnd + 1);
          blockEnd = Math.min(blockEnd, hideStart);

          if (blockStart > blockEnd)
          {
            break;
          }

          visibleSeq.append(seqs[i].getSequence(blockStart, blockEnd));

          blockStart = hideEnd + 1;
          blockEnd = end;
        }

        if (end > blockStart)
        {
          visibleSeq.append(seqs[i].getSequence(blockStart, end));
        }

        selections[i] = visibleSeq.toString();
      }
    }
    else
    {
      for (i = 0; i < iSize; i++)
      {
        selections[i] = seqs[i].getSequenceAsString(start, end);
      }
    }

    return selections;
  }

  /**
   * return all visible segments between the given start and end boundaries
   * 
   * @param start
   *          (first column inclusive from 0)
   * @param end
   *          (last column - not inclusive)
   * @return int[] {i_start, i_end, ..} where intervals lie in
   *         start<=i_start<=i_end<end
   */
  public int[] getVisibleContigs(int start, int end)
  {
    if (hiddenColumns != null && hiddenColumns.size() > 0)
    {
      List<int[]> visiblecontigs = new ArrayList<int[]>();
      List<int[]> regions = getHiddenColumns();

      int vstart = start;
      int[] region;
      int hideStart, hideEnd;

      for (int j = 0; vstart < end && j < regions.size(); j++)
      {
        region = regions.get(j);
        hideStart = region[0];
        hideEnd = region[1];

        if (hideEnd < vstart)
        {
          continue;
        }
        if (hideStart > vstart)
        {
          visiblecontigs.add(new int[] { vstart, hideStart - 1 });
        }
        vstart = hideEnd + 1;
      }

      if (vstart < end)
      {
        visiblecontigs.add(new int[] { vstart, end - 1 });
      }
      int[] vcontigs = new int[visiblecontigs.size() * 2];
      for (int i = 0, j = visiblecontigs.size(); i < j; i++)
      {
        int[] vc = visiblecontigs.get(i);
        visiblecontigs.set(i, null);
        vcontigs[i * 2] = vc[0];
        vcontigs[i * 2 + 1] = vc[1];
      }
      visiblecontigs.clear();
      return vcontigs;
    }
    else
    {
      return new int[] { start, end - 1 };
    }
  }

  /**
   * Locate the first and last position visible for this sequence. if seq isn't
   * visible then return the position of the left and right of the hidden
   * boundary region, and the corresponding alignment column indices for the
   * extent of the sequence
   * 
   * @param seq
   * @return int[] { visible start, visible end, first seqpos, last seqpos,
   *         alignment index for seq start, alignment index for seq end }
   */
  public int[] locateVisibleBoundsOfSequence(SequenceI seq)
  {
    int fpos = seq.getStart(), lpos = seq.getEnd();
    int start = 0;

    if (hiddenColumns == null || hiddenColumns.size() == 0)
    {
      int ifpos = seq.findIndex(fpos) - 1, ilpos = seq.findIndex(lpos) - 1;
      return new int[] { ifpos, ilpos, fpos, lpos, ifpos, ilpos };
    }

    // Simply walk along the sequence whilst watching for hidden column
    // boundaries
    List<int[]> regions = getHiddenColumns();
    int spos = fpos, lastvispos = -1, rcount = 0, hideStart = seq
            .getLength(), hideEnd = -1;
    int visPrev = 0, visNext = 0, firstP = -1, lastP = -1;
    boolean foundStart = false;
    for (int p = 0, pLen = seq.getLength(); spos <= seq.getEnd()
            && p < pLen; p++)
    {
      if (!Comparison.isGap(seq.getCharAt(p)))
      {
        // keep track of first/last column
        // containing sequence data regardless of visibility
        if (firstP == -1)
        {
          firstP = p;
        }
        lastP = p;
        // update hidden region start/end
        while (hideEnd < p && rcount < regions.size())
        {
          int[] region = regions.get(rcount++);
          visPrev = visNext;
          visNext += region[0] - visPrev;
          hideStart = region[0];
          hideEnd = region[1];
        }
        if (hideEnd < p)
        {
          hideStart = seq.getLength();
        }
        // update visible boundary for sequence
        if (p < hideStart)
        {
          if (!foundStart)
          {
            fpos = spos;
            start = p;
            foundStart = true;
          }
          lastvispos = p;
          lpos = spos;
        }
        // look for next sequence position
        spos++;
      }
    }
    if (foundStart)
    {
      return new int[] { findColumnPosition(start),
          findColumnPosition(lastvispos), fpos, lpos, firstP, lastP };
    }
    // otherwise, sequence was completely hidden
    return new int[] { visPrev, visNext, 0, 0, firstP, lastP };
  }

  /**
   * delete any columns in alignmentAnnotation that are hidden (including
   * sequence associated annotation).
   * 
   * @param alignmentAnnotation
   */
  public void makeVisibleAnnotation(AlignmentAnnotation alignmentAnnotation)
  {
    makeVisibleAnnotation(-1, -1, alignmentAnnotation);
  }

  /**
   * delete any columns in alignmentAnnotation that are hidden (including
   * sequence associated annotation).
   * 
   * @param start
   *          remove any annotation to the right of this column
   * @param end
   *          remove any annotation to the left of this column
   * @param alignmentAnnotation
   *          the annotation to operate on
   */
  public void makeVisibleAnnotation(int start, int end,
          AlignmentAnnotation alignmentAnnotation)
  {
    if (alignmentAnnotation.annotations == null)
    {
      return;
    }
    if (start == end && end == -1)
    {
      start = 0;
      end = alignmentAnnotation.annotations.length;
    }
    if (hiddenColumns != null && hiddenColumns.size() > 0)
    {
      // then mangle the alignmentAnnotation annotation array
      Vector<Annotation[]> annels = new Vector<Annotation[]>();
      Annotation[] els = null;
      List<int[]> regions = getHiddenColumns();
      int blockStart = start, blockEnd = end;
      int[] region;
      int hideStart, hideEnd, w = 0;

      for (int j = 0; j < regions.size(); j++)
      {
        region = regions.get(j);
        hideStart = region[0];
        hideEnd = region[1];

        if (hideStart < start)
        {
          continue;
        }

        blockStart = Math.min(blockStart, hideEnd + 1);
        blockEnd = Math.min(blockEnd, hideStart);

        if (blockStart > blockEnd)
        {
          break;
        }

        annels.addElement(els = new Annotation[blockEnd - blockStart]);
        System.arraycopy(alignmentAnnotation.annotations, blockStart, els,
                0, els.length);
        w += els.length;
        blockStart = hideEnd + 1;
        blockEnd = end;
      }

      if (end > blockStart)
      {
        annels.addElement(els = new Annotation[end - blockStart + 1]);
        if ((els.length + blockStart) <= alignmentAnnotation.annotations.length)
        {
          // copy just the visible segment of the annotation row
          System.arraycopy(alignmentAnnotation.annotations, blockStart,
                  els, 0, els.length);
        }
        else
        {
          // copy to the end of the annotation row
          System.arraycopy(alignmentAnnotation.annotations, blockStart,
                  els, 0,
                  (alignmentAnnotation.annotations.length - blockStart));
        }
        w += els.length;
      }
      if (w == 0)
      {
        return;
      }

      alignmentAnnotation.annotations = new Annotation[w];
      w = 0;

      for (Annotation[] chnk : annels)
      {
        System.arraycopy(chnk, 0, alignmentAnnotation.annotations, w,
                chnk.length);
        w += chnk.length;
      }
    }
    else
    {
      alignmentAnnotation.restrict(start, end);
    }
  }

  /**
   * Invert the column selection from first to end-1. leaves hiddenColumns
   * untouched (and unselected)
   * 
   * @param first
   * @param end
   */
  public void invertColumnSelection(int first, int width)
  {
    boolean hasHidden = hiddenColumns != null && hiddenColumns.size() > 0;
    for (int i = first; i < width; i++)
    {
      if (contains(i))
      {
        removeElement(i);
      }
      else
      {
        if (!hasHidden || isVisible(i))
        {
          addElement(i);
        }
      }
    }
  }

  /**
   * add in any unselected columns from the given column selection, excluding
   * any that are hidden.
   * 
   * @param colsel
   */
  public void addElementsFrom(ColumnSelection colsel)
  {
    if (colsel != null && !colsel.isEmpty())
    {
      for (Integer col : colsel.getSelected())
      {
        if (hiddenColumns != null && isVisible(col.intValue()))
        {
          selection.add(col);
        }
      }
    }
  }

  /**
   * set the selected columns the given column selection, excluding any columns
   * that are hidden.
   * 
   * @param colsel
   */
  public void setElementsFrom(ColumnSelection colsel)
  {
    selection = new IntList();
    if (colsel.selection != null && colsel.selection.size() > 0)
    {
      if (hiddenColumns != null && hiddenColumns.size() > 0)
      {
        // only select visible columns in this columns selection
        addElementsFrom(colsel);
      }
      else
      {
        // add everything regardless
        for (Integer col : colsel.getSelected())
        {
          addElement(col);
        }
      }
    }
  }

  /**
   * Add gaps into the sequences aligned to profileseq under the given
   * AlignmentView
   * 
   * @param profileseq
   * @param al
   *          - alignment to have gaps inserted into it
   * @param input
   *          - alignment view where sequence corresponding to profileseq is
   *          first entry
   * @return new Column selection for new alignment view, with insertions into
   *         profileseq marked as hidden.
   */
  public static ColumnSelection propagateInsertions(SequenceI profileseq,
          AlignmentI al, AlignmentView input)
  {
    int profsqpos = 0;

    // return propagateInsertions(profileseq, al, )
    char gc = al.getGapCharacter();
    Object[] alandcolsel = input.getAlignmentAndColumnSelection(gc);
    ColumnSelection nview = (ColumnSelection) alandcolsel[1];
    SequenceI origseq = ((SequenceI[]) alandcolsel[0])[profsqpos];
    nview.propagateInsertions(profileseq, al, origseq);
    return nview;
  }

  /**
   * 
   * @param profileseq
   *          - sequence in al which corresponds to origseq
   * @param al
   *          - alignment which is to have gaps inserted into it
   * @param origseq
   *          - sequence corresponding to profileseq which defines gap map for
   *          modifying al
   */
  public void propagateInsertions(SequenceI profileseq, AlignmentI al,
          SequenceI origseq)
  {
    char gc = al.getGapCharacter();
    // recover mapping between sequence's non-gap positions and positions
    // mapping to view.
    pruneDeletions(ShiftList.parseMap(origseq.gapMap()));
    int[] viscontigs = getVisibleContigs(0, profileseq.getLength());
    int spos = 0;
    int offset = 0;
    // input.pruneDeletions(ShiftList.parseMap(((SequenceI[])
    // alandcolsel[0])[0].gapMap()))
    // add profile to visible contigs
    for (int v = 0; v < viscontigs.length; v += 2)
    {
      if (viscontigs[v] > spos)
      {
        StringBuffer sb = new StringBuffer();
        for (int s = 0, ns = viscontigs[v] - spos; s < ns; s++)
        {
          sb.append(gc);
        }
        for (int s = 0, ns = al.getHeight(); s < ns; s++)
        {
          SequenceI sqobj = al.getSequenceAt(s);
          if (sqobj != profileseq)
          {
            String sq = al.getSequenceAt(s).getSequenceAsString();
            if (sq.length() <= spos + offset)
            {
              // pad sequence
              int diff = spos + offset - sq.length() - 1;
              if (diff > 0)
              {
                // pad gaps
                sq = sq + sb;
                while ((diff = spos + offset - sq.length() - 1) > 0)
                {
                  // sq = sq
                  // + ((diff >= sb.length()) ? sb.toString() : sb
                  // .substring(0, diff));
                  if (diff >= sb.length())
                  {
                    sq += sb.toString();
                  }
                  else
                  {
                    char[] buf = new char[diff];
                    sb.getChars(0, diff, buf, 0);
                    sq += buf.toString();
                  }
                }
              }
              sq += sb.toString();
            }
            else
            {
              al.getSequenceAt(s).setSequence(
                      sq.substring(0, spos + offset) + sb.toString()
                              + sq.substring(spos + offset));
            }
          }
        }
        // offset+=sb.length();
      }
      spos = viscontigs[v + 1] + 1;
    }
    if ((offset + spos) < profileseq.getLength())
    {
      // pad the final region with gaps.
      StringBuffer sb = new StringBuffer();
      for (int s = 0, ns = profileseq.getLength() - spos - offset; s < ns; s++)
      {
        sb.append(gc);
      }
      for (int s = 0, ns = al.getHeight(); s < ns; s++)
      {
        SequenceI sqobj = al.getSequenceAt(s);
        if (sqobj == profileseq)
        {
          continue;
        }
        String sq = sqobj.getSequenceAsString();
        // pad sequence
        int diff = origseq.getLength() - sq.length();
        while (diff > 0)
        {
          // sq = sq
          // + ((diff >= sb.length()) ? sb.toString() : sb
          // .substring(0, diff));
          if (diff >= sb.length())
          {
            sq += sb.toString();
          }
          else
          {
            char[] buf = new char[diff];
            sb.getChars(0, diff, buf, 0);
            sq += buf.toString();
          }
          diff = origseq.getLength() - sq.length();
        }
      }
    }
  }

  /**
   * 
   * @return true if there are columns marked
   */
  public boolean hasSelectedColumns()
  {
    return (selection != null && selection.size() > 0);
  }

  /**
   * 
   * @return true if there are columns hidden
   */
  public boolean hasHiddenColumns()
  {
    return hiddenColumns != null && hiddenColumns.size() > 0;
  }

  /**
   * 
   * @return true if there are more than one set of columns hidden
   */
  public boolean hasManyHiddenColumns()
  {
    return hiddenColumns != null && hiddenColumns.size() > 1;
  }

  /**
   * mark the columns corresponding to gap characters as hidden in the column
   * selection
   * 
   * @param sr
   */
  public void hideInsertionsFor(SequenceI sr)
  {
    List<int[]> inserts = sr.getInsertions();
    for (int[] r : inserts)
    {
      hideColumns(r[0], r[1]);
    }
  }

  public boolean filterAnnotations(Annotation[] annotations,
          AnnotationFilterParameter filterParams)
  {
    // JBPNote - this method needs to be refactored to become independent of
    // viewmodel package
    this.revealAllHiddenColumns();
    this.clear();
    int count = 0;
    do
    {
      if (annotations[count] != null)
      {

        boolean itemMatched = false;

        if (filterParams.getThresholdType() == AnnotationFilterParameter.ThresholdType.ABOVE_THRESHOLD
                && annotations[count].value >= filterParams
                        .getThresholdValue())
        {
          itemMatched = true;
        }
        if (filterParams.getThresholdType() == AnnotationFilterParameter.ThresholdType.BELOW_THRESHOLD
                && annotations[count].value <= filterParams
                        .getThresholdValue())
        {
          itemMatched = true;
        }

        if (filterParams.isFilterAlphaHelix()
                && annotations[count].secondaryStructure == 'H')
        {
          itemMatched = true;
        }

        if (filterParams.isFilterBetaSheet()
                && annotations[count].secondaryStructure == 'E')
        {
          itemMatched = true;
        }

        if (filterParams.isFilterTurn()
                && annotations[count].secondaryStructure == 'S')
        {
          itemMatched = true;
        }

        String regexSearchString = filterParams.getRegexString();
        if (regexSearchString != null
                && !filterParams.getRegexSearchFields().isEmpty())
        {
          List<SearchableAnnotationField> fields = filterParams
                  .getRegexSearchFields();
          try
          {
            if (fields.contains(SearchableAnnotationField.DISPLAY_STRING)
                    && annotations[count].displayCharacter
                            .matches(regexSearchString))
            {
              itemMatched = true;
            }
          } catch (java.util.regex.PatternSyntaxException pse)
          {
            if (annotations[count].displayCharacter
                    .equals(regexSearchString))
            {
              itemMatched = true;
            }
          }
          if (fields.contains(SearchableAnnotationField.DESCRIPTION)
                  && annotations[count].description != null
                  && annotations[count].description
                          .matches(regexSearchString))
          {
            itemMatched = true;
          }
        }

        if (itemMatched)
        {
          this.addElement(count);
        }
      }
      count++;
    } while (count < annotations.length);
    return false;
  }

  /**
   * Returns a hashCode built from selected columns and hidden column ranges
   */
  @Override
  public int hashCode()
  {
    int hashCode = selection.hashCode();
    if (hiddenColumns != null)
    {
      for (int[] hidden : hiddenColumns)
      {
        hashCode = 31 * hashCode + hidden[0];
        hashCode = 31 * hashCode + hidden[1];
      }
    }
    return hashCode;
  }

  /**
   * Answers true if comparing to a ColumnSelection with the same selected
   * columns and hidden columns, else false
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ColumnSelection))
    {
      return false;
    }
    ColumnSelection that = (ColumnSelection) obj;

    /*
     * check columns selected are either both null, or match
     */
    if (this.selection == null)
    {
      if (that.selection != null)
      {
        return false;
      }
    }
    if (!this.selection.equals(that.selection))
    {
      return false;
    }

    /*
     * check hidden columns are either both null, or match
     */
    if (this.hiddenColumns == null)
    {
      return (that.hiddenColumns == null);
    }
    if (that.hiddenColumns == null
            || that.hiddenColumns.size() != this.hiddenColumns.size())
    {
      return false;
    }
    int i = 0;
    for (int[] thisRange : hiddenColumns)
    {
      int[] thatRange = that.hiddenColumns.get(i++);
      if (thisRange[0] != thatRange[0] || thisRange[1] != thatRange[1])
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Updates the column selection depending on the parameters, and returns true
   * if any change was made to the selection
   * 
   * @param markedColumns
   *          a set identifying marked columns (base 0)
   * @param startCol
   *          the first column of the range to operate over (base 0)
   * @param endCol
   *          the last column of the range to operate over (base 0)
   * @param invert
   *          if true, deselect marked columns and select unmarked
   * @param extendCurrent
   *          if true, extend rather than replacing the current column selection
   * @param toggle
   *          if true, toggle the selection state of marked columns
   * 
   * @return
   */
  public boolean markColumns(BitSet markedColumns, int startCol,
          int endCol, boolean invert, boolean extendCurrent, boolean toggle)
  {
    boolean changed = false;
    if (!extendCurrent && !toggle)
    {
      changed = !this.isEmpty();
      clear();
    }
    if (invert)
    {
      // invert only in the currently selected sequence region
      int i = markedColumns.nextClearBit(startCol);
      int ibs = markedColumns.nextSetBit(startCol);
      while (i >= startCol && i <= endCol)
      {
        if (ibs < 0 || i < ibs)
        {
          changed = true;
          if (toggle && contains(i))
          {
            removeElement(i++);
          }
          else
          {
            addElement(i++);
          }
        }
        else
        {
          i = markedColumns.nextClearBit(ibs);
          ibs = markedColumns.nextSetBit(i);
        }
      }
    }
    else
    {
      int i = markedColumns.nextSetBit(startCol);
      while (i >= startCol && i <= endCol)
      {
        changed = true;
        if (toggle && contains(i))
        {
          removeElement(i);
        }
        else
        {
          addElement(i);
        }
        i = markedColumns.nextSetBit(i + 1);
      }
    }
    return changed;
  }

}
