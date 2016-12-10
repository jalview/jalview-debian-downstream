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
package jalview.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple way of bijectively mapping a non-contiguous linear range to another
 * non-contiguous linear range.
 * 
 * Use at your own risk!
 * 
 * TODO: efficient implementation of private posMap method
 * 
 * TODO: test/ensure that sense of from and to ratio start position is conserved
 * (codon start position recovery)
 */
public class MapList
{

  /*
   * Subregions (base 1) described as { [start1, end1], [start2, end2], ...}
   */
  private List<int[]> fromShifts;

  /*
   * Same format as fromShifts, for the 'mapped to' sequence
   */
  private List<int[]> toShifts;

  /*
   * number of steps in fromShifts to one toRatio unit
   */
  private int fromRatio;

  /*
   * number of steps in toShifts to one fromRatio
   */
  private int toRatio;

  /*
   * lowest and highest value in the from Map
   */
  private int fromLowest;

  private int fromHighest;

  /*
   * lowest and highest value in the to Map
   */
  private int toLowest;

  private int toHighest;

  /**
   * Constructor
   */
  public MapList()
  {
    fromShifts = new ArrayList<int[]>();
    toShifts = new ArrayList<int[]>();
  }

  /**
   * Two MapList objects are equal if they are the same object, or they both
   * have populated shift ranges and all values are the same.
   */
  @Override
  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof MapList))
    {
      return false;
    }

    MapList obj = (MapList) o;
    if (obj == this)
    {
      return true;
    }
    if (obj.fromRatio != fromRatio || obj.toRatio != toRatio
            || obj.fromShifts == null || obj.toShifts == null)
    {
      return false;
    }
    return Arrays
            .deepEquals(fromShifts.toArray(), obj.fromShifts.toArray())
            && Arrays
                    .deepEquals(toShifts.toArray(), obj.toShifts.toArray());
  }

  /**
   * Returns a hashcode made from the fromRatio, toRatio, and from/to ranges
   */
  @Override
  public int hashCode()
  {
    int hashCode = 31 * fromRatio;
    hashCode = 31 * hashCode + toRatio;
    hashCode = 31 * hashCode + fromShifts.toArray().hashCode();
    hashCode = 31 * hashCode + toShifts.toArray().hashCode();
    return hashCode;
  }

  /**
   * Returns the 'from' ranges as {[start1, end1], [start2, end2], ...}
   * 
   * @return
   */
  public List<int[]> getFromRanges()
  {
    return fromShifts;
  }

  /**
   * Returns the 'to' ranges as {[start1, end1], [start2, end2], ...}
   * 
   * @return
   */
  public List<int[]> getToRanges()
  {
    return toShifts;
  }

  /**
   * Flattens a list of [start, end] into a single [start1, end1, start2,
   * end2,...] array.
   * 
   * @param shifts
   * @return
   */
  protected static int[] getRanges(List<int[]> shifts)
  {
    int[] rnges = new int[2 * shifts.size()];
    int i = 0;
    for (int[] r : shifts)
    {
      rnges[i++] = r[0];
      rnges[i++] = r[1];
    }
    return rnges;
  }

  /**
   * 
   * @return length of mapped phrase in from
   */
  public int getFromRatio()
  {
    return fromRatio;
  }

  /**
   * 
   * @return length of mapped phrase in to
   */
  public int getToRatio()
  {
    return toRatio;
  }

  public int getFromLowest()
  {
    return fromLowest;
  }

  public int getFromHighest()
  {
    return fromHighest;
  }

  public int getToLowest()
  {
    return toLowest;
  }

  public int getToHighest()
  {
    return toHighest;
  }

  /**
   * Constructor given from and to ranges as [start1, end1, start2, end2,...].
   * If any end is equal to the next start, the ranges will be merged. There is
   * no validation check that the ranges do not overlap each other.
   * 
   * @param from
   *          contiguous regions as [start1, end1, start2, end2, ...]
   * @param to
   *          same format as 'from'
   * @param fromRatio
   *          phrase length in 'from' (e.g. 3 for dna)
   * @param toRatio
   *          phrase length in 'to' (e.g. 1 for protein)
   */
  public MapList(int from[], int to[], int fromRatio, int toRatio)
  {
    this();
    this.fromRatio = fromRatio;
    this.toRatio = toRatio;
    fromLowest = Integer.MAX_VALUE;
    fromHighest = Integer.MIN_VALUE;
    int added = 0;

    for (int i = 0; i < from.length; i += 2)
    {
      /*
       * note lowest and highest values - bearing in mind the
       * direction may be reversed
       */
      fromLowest = Math.min(fromLowest, Math.min(from[i], from[i + 1]));
      fromHighest = Math.max(fromHighest, Math.max(from[i], from[i + 1]));
      if (added > 0 && from[i] == fromShifts.get(added - 1)[1])
      {
        /*
         * this range starts where the last ended - just extend it
         */
        fromShifts.get(added - 1)[1] = from[i + 1];
      }
      else
      {
        fromShifts.add(new int[] { from[i], from[i + 1] });
        added++;
      }
    }

    toLowest = Integer.MAX_VALUE;
    toHighest = Integer.MIN_VALUE;
    added = 0;
    for (int i = 0; i < to.length; i += 2)
    {
      toLowest = Math.min(toLowest, Math.min(to[i], to[i + 1]));
      toHighest = Math.max(toHighest, Math.max(to[i], to[i + 1]));
      if (added > 0 && to[i] == toShifts.get(added - 1)[1])
      {
        toShifts.get(added - 1)[1] = to[i + 1];
      }
      else
      {
        toShifts.add(new int[] { to[i], to[i + 1] });
        added++;
      }
    }
  }

  /**
   * Copy constructor. Creates an identical mapping.
   * 
   * @param map
   */
  public MapList(MapList map)
  {
    this();
    // TODO not used - remove?
    this.fromLowest = map.fromLowest;
    this.fromHighest = map.fromHighest;
    this.toLowest = map.toLowest;
    this.toHighest = map.toHighest;

    this.fromRatio = map.fromRatio;
    this.toRatio = map.toRatio;
    if (map.fromShifts != null)
    {
      for (int[] r : map.fromShifts)
      {
        fromShifts.add(new int[] { r[0], r[1] });
      }
    }
    if (map.toShifts != null)
    {
      for (int[] r : map.toShifts)
      {
        toShifts.add(new int[] { r[0], r[1] });
      }
    }
  }

  /**
   * Constructor given ranges as lists of [start, end] positions. There is no
   * validation check that the ranges do not overlap each other.
   * 
   * @param fromRange
   * @param toRange
   * @param fromRatio
   * @param toRatio
   */
  public MapList(List<int[]> fromRange, List<int[]> toRange, int fromRatio,
          int toRatio)
  {
    this();
    fromRange = coalesceRanges(fromRange);
    toRange = coalesceRanges(toRange);
    this.fromShifts = fromRange;
    this.toShifts = toRange;
    this.fromRatio = fromRatio;
    this.toRatio = toRatio;

    fromLowest = Integer.MAX_VALUE;
    fromHighest = Integer.MIN_VALUE;
    for (int[] range : fromRange)
    {
      fromLowest = Math.min(fromLowest, Math.min(range[0], range[1]));
      fromHighest = Math.max(fromHighest, Math.max(range[0], range[1]));
    }

    toLowest = Integer.MAX_VALUE;
    toHighest = Integer.MIN_VALUE;
    for (int[] range : toRange)
    {
      toLowest = Math.min(toLowest, Math.min(range[0], range[1]));
      toHighest = Math.max(toHighest, Math.max(range[0], range[1]));
    }
  }

  /**
   * Consolidates a list of ranges so that any contiguous ranges are merged.
   * This assumes the ranges are already in start order (does not sort them).
   * 
   * @param ranges
   * @return the same list (if unchanged), else a new merged list, leaving the
   *         input list unchanged
   */
  public static List<int[]> coalesceRanges(final List<int[]> ranges)
  {
    if (ranges == null || ranges.size() < 2)
    {
      return ranges;
    }

    boolean changed = false;
    List<int[]> merged = new ArrayList<int[]>();
    int[] lastRange = ranges.get(0);
    int lastDirection = lastRange[1] >= lastRange[0] ? 1 : -1;
    lastRange = new int[] { lastRange[0], lastRange[1] };
    merged.add(lastRange);
    boolean first = true;

    for (final int[] range : ranges)
    {
      if (first)
      {
        first = false;
        continue;
      }
      if (range[0] == lastRange[0] && range[1] == lastRange[1])
      {
        // drop duplicate range
        changed = true;
        continue;
      }

      /*
       * drop this range if it lies within the last range
       */
      if ((lastDirection == 1 && range[0] >= lastRange[0]
              && range[0] <= lastRange[1] && range[1] >= lastRange[0] && range[1] <= lastRange[1])
              || (lastDirection == -1 && range[0] <= lastRange[0]
                      && range[0] >= lastRange[1]
                      && range[1] <= lastRange[0] && range[1] >= lastRange[1]))
      {
        changed = true;
        continue;
      }

      int direction = range[1] >= range[0] ? 1 : -1;

      /*
       * if next range is in the same direction as last and contiguous,
       * just update the end position of the last range
       */
      boolean sameDirection = range[1] == range[0]
              || direction == lastDirection;
      boolean extending = range[0] == lastRange[1] + lastDirection;
      boolean overlapping = (lastDirection == 1 && range[0] >= lastRange[0] && range[0] <= lastRange[1])
              || (lastDirection == -1 && range[0] <= lastRange[0] && range[0] >= lastRange[1]);
      if (sameDirection && (overlapping || extending))
      {
        lastRange[1] = range[1];
        changed = true;
      }
      else
      {
        lastRange = new int[] { range[0], range[1] };
        merged.add(lastRange);
        // careful: merging [5, 5] after [7, 6] should keep negative direction
        lastDirection = (range[1] == range[0]) ? lastDirection : direction;
      }
    }

    return changed ? merged : ranges;
  }

  /**
   * get all mapped positions from 'from' to 'to'
   * 
   * @return int[][] { int[] { fromStart, fromFinish, toStart, toFinish }, int
   *         [fromFinish-fromStart+2] { toStart..toFinish mappings}}
   */
  protected int[][] makeFromMap()
  {
    // TODO not used - remove??
    return posMap(fromShifts, fromRatio, toShifts, toRatio);
  }

  /**
   * get all mapped positions from 'to' to 'from'
   * 
   * @return int[to position]=position mapped in from
   */
  protected int[][] makeToMap()
  {
    // TODO not used - remove??
    return posMap(toShifts, toRatio, fromShifts, fromRatio);
  }

  /**
   * construct an int map for intervals in intVals
   * 
   * @param shiftTo
   * @return int[] { from, to pos in range }, int[range.to-range.from+1]
   *         returning mapped position
   */
  private int[][] posMap(List<int[]> shiftTo, int ratio,
          List<int[]> shiftFrom, int toRatio)
  {
    // TODO not used - remove??
    int iv = 0, ivSize = shiftTo.size();
    if (iv >= ivSize)
    {
      return null;
    }
    int[] intv = shiftTo.get(iv++);
    int from = intv[0], to = intv[1];
    if (from > to)
    {
      from = intv[1];
      to = intv[0];
    }
    while (iv < ivSize)
    {
      intv = shiftTo.get(iv++);
      if (intv[0] < from)
      {
        from = intv[0];
      }
      if (intv[1] < from)
      {
        from = intv[1];
      }
      if (intv[0] > to)
      {
        to = intv[0];
      }
      if (intv[1] > to)
      {
        to = intv[1];
      }
    }
    int tF = 0, tT = 0;
    int mp[][] = new int[to - from + 2][];
    for (int i = 0; i < mp.length; i++)
    {
      int[] m = shift(i + from, shiftTo, ratio, shiftFrom, toRatio);
      if (m != null)
      {
        if (i == 0)
        {
          tF = tT = m[0];
        }
        else
        {
          if (m[0] < tF)
          {
            tF = m[0];
          }
          if (m[0] > tT)
          {
            tT = m[0];
          }
        }
      }
      mp[i] = m;
    }
    int[][] map = new int[][] { new int[] { from, to, tF, tT },
        new int[to - from + 2] };

    map[0][2] = tF;
    map[0][3] = tT;

    for (int i = 0; i < mp.length; i++)
    {
      if (mp[i] != null)
      {
        map[1][i] = mp[i][0] - tF;
      }
      else
      {
        map[1][i] = -1; // indicates an out of range mapping
      }
    }
    return map;
  }

  /**
   * addShift
   * 
   * @param pos
   *          start position for shift (in original reference frame)
   * @param shift
   *          length of shift
   * 
   *          public void addShift(int pos, int shift) { int sidx = 0; int[]
   *          rshift=null; while (sidx<shifts.size() && (rshift=(int[])
   *          shifts.elementAt(sidx))[0]<pos) sidx++; if (sidx==shifts.size())
   *          shifts.insertElementAt(new int[] { pos, shift}, sidx); else
   *          rshift[1]+=shift; }
   */

  /**
   * shift from pos to To(pos)
   * 
   * @param pos
   *          int
   * @return int shifted position in To, frameshift in From, direction of mapped
   *         symbol in To
   */
  public int[] shiftFrom(int pos)
  {
    return shift(pos, fromShifts, fromRatio, toShifts, toRatio);
  }

  /**
   * inverse of shiftFrom - maps pos in To to a position in From
   * 
   * @param pos
   *          (in To)
   * @return shifted position in From, frameshift in To, direction of mapped
   *         symbol in From
   */
  public int[] shiftTo(int pos)
  {
    return shift(pos, toShifts, toRatio, fromShifts, fromRatio);
  }

  /**
   * 
   * @param shiftTo
   * @param fromRatio
   * @param shiftFrom
   * @param toRatio
   * @return
   */
  protected static int[] shift(int pos, List<int[]> shiftTo, int fromRatio,
          List<int[]> shiftFrom, int toRatio)
  {
    // TODO: javadoc; tests
    int[] fromCount = countPos(shiftTo, pos);
    if (fromCount == null)
    {
      return null;
    }
    int fromRemainder = (fromCount[0] - 1) % fromRatio;
    int toCount = 1 + (((fromCount[0] - 1) / fromRatio) * toRatio);
    int[] toPos = countToPos(shiftFrom, toCount);
    if (toPos == null)
    {
      return null; // throw new Error("Bad Mapping!");
    }
    // System.out.println(fromCount[0]+" "+fromCount[1]+" "+toCount);
    return new int[] { toPos[0], fromRemainder, toPos[1] };
  }

  /**
   * count how many positions pos is along the series of intervals.
   * 
   * @param shiftTo
   * @param pos
   * @return number of positions or null if pos is not within intervals
   */
  protected static int[] countPos(List<int[]> shiftTo, int pos)
  {
    int count = 0, intv[], iv = 0, ivSize = shiftTo.size();
    while (iv < ivSize)
    {
      intv = shiftTo.get(iv++);
      if (intv[0] <= intv[1])
      {
        if (pos >= intv[0] && pos <= intv[1])
        {
          return new int[] { count + pos - intv[0] + 1, +1 };
        }
        else
        {
          count += intv[1] - intv[0] + 1;
        }
      }
      else
      {
        if (pos >= intv[1] && pos <= intv[0])
        {
          return new int[] { count + intv[0] - pos + 1, -1 };
        }
        else
        {
          count += intv[0] - intv[1] + 1;
        }
      }
    }
    return null;
  }

  /**
   * count out pos positions into a series of intervals and return the position
   * 
   * @param shiftFrom
   * @param pos
   * @return position pos in interval set
   */
  protected static int[] countToPos(List<int[]> shiftFrom, int pos)
  {
    int count = 0, diff = 0, iv = 0, ivSize = shiftFrom.size();
    int[] intv = { 0, 0 };
    while (iv < ivSize)
    {
      intv = shiftFrom.get(iv++);
      diff = intv[1] - intv[0];
      if (diff >= 0)
      {
        if (pos <= count + 1 + diff)
        {
          return new int[] { pos - count - 1 + intv[0], +1 };
        }
        else
        {
          count += 1 + diff;
        }
      }
      else
      {
        if (pos <= count + 1 - diff)
        {
          return new int[] { intv[0] - (pos - count - 1), -1 };
        }
        else
        {
          count += 1 - diff;
        }
      }
    }
    return null;// (diff<0) ? (intv[1]-1) : (intv[0]+1);
  }

  /**
   * find series of intervals mapping from start-end in the From map.
   * 
   * @param start
   *          position mapped 'to'
   * @param end
   *          position mapped 'to'
   * @return series of [start, end] ranges in sequence mapped 'from'
   */
  public int[] locateInFrom(int start, int end)
  {
    // inefficient implementation
    int fromStart[] = shiftTo(start);
    // needs to be inclusive of end of symbol position
    int fromEnd[] = shiftTo(end);

    return getIntervals(fromShifts, fromStart, fromEnd, fromRatio);
  }

  /**
   * find series of intervals mapping from start-end in the to map.
   * 
   * @param start
   *          position mapped 'from'
   * @param end
   *          position mapped 'from'
   * @return series of [start, end] ranges in sequence mapped 'to'
   */
  public int[] locateInTo(int start, int end)
  {
    int toStart[] = shiftFrom(start);
    int toEnd[] = shiftFrom(end);
    return getIntervals(toShifts, toStart, toEnd, toRatio);
  }

  /**
   * like shift - except returns the intervals in the given vector of shifts
   * which were spanned in traversing fromStart to fromEnd
   * 
   * @param shiftFrom
   * @param fromStart
   * @param fromEnd
   * @param fromRatio2
   * @return series of from,to intervals from from first position of starting
   *         region to final position of ending region inclusive
   */
  protected static int[] getIntervals(List<int[]> shiftFrom,
          int[] fromStart, int[] fromEnd, int fromRatio2)
  {
    if (fromStart == null || fromEnd == null)
    {
      return null;
    }
    int startpos, endpos;
    startpos = fromStart[0]; // first position in fromStart
    endpos = fromEnd[0]; // last position in fromEnd
    int endindx = (fromRatio2 - 1); // additional positions to get to last
    // position from endpos
    int intv = 0, intvSize = shiftFrom.size();
    int iv[], i = 0, fs = -1, fe_s = -1, fe = -1; // containing intervals
    // search intervals to locate ones containing startpos and count endindx
    // positions on from endpos
    while (intv < intvSize && (fs == -1 || fe == -1))
    {
      iv = shiftFrom.get(intv++);
      if (fe_s > -1)
      {
        endpos = iv[0]; // start counting from beginning of interval
        endindx--; // inclusive of endpos
      }
      if (iv[0] <= iv[1])
      {
        if (fs == -1 && startpos >= iv[0] && startpos <= iv[1])
        {
          fs = i;
        }
        if (endpos >= iv[0] && endpos <= iv[1])
        {
          if (fe_s == -1)
          {
            fe_s = i;
          }
          if (fe_s != -1)
          {
            if (endpos + endindx <= iv[1])
            {
              fe = i;
              endpos = endpos + endindx; // end of end token is within this
              // interval
            }
            else
            {
              endindx -= iv[1] - endpos; // skip all this interval too
            }
          }
        }
      }
      else
      {
        if (fs == -1 && startpos <= iv[0] && startpos >= iv[1])
        {
          fs = i;
        }
        if (endpos <= iv[0] && endpos >= iv[1])
        {
          if (fe_s == -1)
          {
            fe_s = i;
          }
          if (fe_s != -1)
          {
            if (endpos - endindx >= iv[1])
            {
              fe = i;
              endpos = endpos - endindx; // end of end token is within this
              // interval
            }
            else
            {
              endindx -= endpos - iv[1]; // skip all this interval too
            }
          }
        }
      }
      i++;
    }
    if (fs == fe && fe == -1)
    {
      return null;
    }
    List<int[]> ranges = new ArrayList<int[]>();
    if (fs <= fe)
    {
      intv = fs;
      i = fs;
      // truncate initial interval
      iv = shiftFrom.get(intv++);
      iv = new int[] { iv[0], iv[1] };// clone
      if (i == fs)
      {
        iv[0] = startpos;
      }
      while (i != fe)
      {
        ranges.add(iv); // add initial range
        iv = shiftFrom.get(intv++); // get next interval
        iv = new int[] { iv[0], iv[1] };// clone
        i++;
      }
      if (i == fe)
      {
        iv[1] = endpos;
      }
      ranges.add(iv); // add only - or final range
    }
    else
    {
      // walk from end of interval.
      i = shiftFrom.size() - 1;
      while (i > fs)
      {
        i--;
      }
      iv = shiftFrom.get(i);
      iv = new int[] { iv[1], iv[0] };// reverse and clone
      // truncate initial interval
      if (i == fs)
      {
        iv[0] = startpos;
      }
      while (--i != fe)
      { // fix apparent logic bug when fe==-1
        ranges.add(iv); // add (truncated) reversed interval
        iv = shiftFrom.get(i);
        iv = new int[] { iv[1], iv[0] }; // reverse and clone
      }
      if (i == fe)
      {
        // interval is already reversed
        iv[1] = endpos;
      }
      ranges.add(iv); // add only - or final range
    }
    // create array of start end intervals.
    int[] range = null;
    if (ranges != null && ranges.size() > 0)
    {
      range = new int[ranges.size() * 2];
      intv = 0;
      intvSize = ranges.size();
      i = 0;
      while (intv < intvSize)
      {
        iv = ranges.get(intv);
        range[i++] = iv[0];
        range[i++] = iv[1];
        ranges.set(intv++, null); // remove
      }
    }
    return range;
  }

  /**
   * get the 'initial' position of mpos in To
   * 
   * @param mpos
   *          position in from
   * @return position of first word in to reference frame
   */
  public int getToPosition(int mpos)
  {
    // TODO not used - remove??
    int[] mp = shiftTo(mpos);
    if (mp != null)
    {
      return mp[0];
    }
    return mpos;
  }

  /**
   * get range of positions in To frame for the mpos word in From
   * 
   * @param mpos
   *          position in From
   * @return null or int[] first position in To for mpos, last position in to
   *         for Mpos
   */
  public int[] getToWord(int mpos)
  {
    int[] mp = shiftTo(mpos);
    if (mp != null)
    {
      return new int[] { mp[0], mp[0] + mp[2] * (getFromRatio() - 1) };
    }
    return null;
  }

  /**
   * get From position in the associated reference frame for position pos in the
   * associated sequence.
   * 
   * @param pos
   * @return
   */
  public int getMappedPosition(int pos)
  {
    // TODO not used - remove??
    int[] mp = shiftFrom(pos);
    if (mp != null)
    {
      return mp[0];
    }
    return pos;
  }

  public int[] getMappedWord(int pos)
  {
    // TODO not used - remove??
    int[] mp = shiftFrom(pos);
    if (mp != null)
    {
      return new int[] { mp[0], mp[0] + mp[2] * (getToRatio() - 1) };
    }
    return null;
  }

  /**
   * 
   * @return a MapList whose From range is this maplist's To Range, and vice
   *         versa
   */
  public MapList getInverse()
  {
    return new MapList(getToRanges(), getFromRanges(), getToRatio(),
            getFromRatio());
  }

  /**
   * test for containment rather than equivalence to another mapping
   * 
   * @param map
   *          to be tested for containment
   * @return true if local or mapped range map contains or is contained by this
   *         mapping
   */
  public boolean containsEither(boolean local, MapList map)
  {
    // TODO not used - remove?
    if (local)
    {
      return ((getFromLowest() >= map.getFromLowest() && getFromHighest() <= map
              .getFromHighest()) || (getFromLowest() <= map.getFromLowest() && getFromHighest() >= map
              .getFromHighest()));
    }
    else
    {
      return ((getToLowest() >= map.getToLowest() && getToHighest() <= map
              .getToHighest()) || (getToLowest() <= map.getToLowest() && getToHighest() >= map
              .getToHighest()));
    }
  }

  /**
   * String representation - for debugging, not guaranteed not to change
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder(64);
    sb.append("[");
    for (int[] shift : fromShifts)
    {
      sb.append(" ").append(Arrays.toString(shift));
    }
    sb.append(" ] ");
    sb.append(fromRatio).append(":").append(toRatio);
    sb.append(" to [");
    for (int[] shift : toShifts)
    {
      sb.append(" ").append(Arrays.toString(shift));
    }
    sb.append(" ]");
    return sb.toString();
  }

  /**
   * Extend this map list by adding the given map's ranges. There is no
   * validation check that the ranges do not overlap existing ranges (or each
   * other), but contiguous ranges are merged.
   * 
   * @param map
   */
  public void addMapList(MapList map)
  {
    if (this.equals(map))
    {
      return;
    }
    this.fromLowest = Math.min(fromLowest, map.fromLowest);
    this.toLowest = Math.min(toLowest, map.toLowest);
    this.fromHighest = Math.max(fromHighest, map.fromHighest);
    this.toHighest = Math.max(toHighest, map.toHighest);

    for (int[] range : map.getFromRanges())
    {
      addRange(range, fromShifts);
    }
    for (int[] range : map.getToRanges())
    {
      addRange(range, toShifts);
    }
  }

  /**
   * Adds the given range to a list of ranges. If the new range just extends
   * existing ranges, the current endpoint is updated instead.
   * 
   * @param range
   * @param addTo
   */
  static void addRange(int[] range, List<int[]> addTo)
  {
    /*
     * list is empty - add to it!
     */
    if (addTo.size() == 0)
    {
      addTo.add(range);
      return;
    }

    int[] last = addTo.get(addTo.size() - 1);
    boolean lastForward = last[1] >= last[0];
    boolean newForward = range[1] >= range[0];

    /*
     * contiguous range in the same direction - just update endpoint
     */
    if (lastForward == newForward && last[1] == range[0])
    {
      last[1] = range[1];
      return;
    }

    /*
     * next range starts at +1 in forward sense - update endpoint
     */
    if (lastForward && newForward && range[0] == last[1] + 1)
    {
      last[1] = range[1];
      return;
    }

    /*
     * next range starts at -1 in reverse sense - update endpoint
     */
    if (!lastForward && !newForward && range[0] == last[1] - 1)
    {
      last[1] = range[1];
      return;
    }

    /*
     * just add the new range
     */
    addTo.add(range);
  }

  /**
   * Returns true if mapping is from forward strand, false if from reverse
   * strand. Result is just based on the first 'from' range that is not a single
   * position. Default is true unless proven to be false. Behaviour is not well
   * defined if the mapping has a mixture of forward and reverse ranges.
   * 
   * @return
   */
  public boolean isFromForwardStrand()
  {
    boolean forwardStrand = true;
    for (int[] range : getFromRanges())
    {
      if (range[1] > range[0])
      {
        break; // forward strand confirmed
      }
      else if (range[1] < range[0])
      {
        forwardStrand = false;
        break; // reverse strand confirmed
      }
    }
    return forwardStrand;
  }

  /**
   * 
   * @return true if from, or to is a three to 1 mapping
   */
  public boolean isTripletMap()
  {
    return (toRatio == 3 && fromRatio == 1)
            || (fromRatio == 3 && toRatio == 1);
  }

}
