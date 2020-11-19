/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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
package jalview.ext.rbvi.chimera;

import jalview.util.IntRangeComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to model a Chimera atomspec pattern, for example
 * 
 * <pre>
 * #0:15.A,28.A,54.A,63.A,70-72.A,83-84.A,97-98.A|#1:2.A,6.A,11.A,13-14.A,70.A,82.A,96-97.A
 * </pre>
 * 
 * where
 * <ul>
 * <li>#0 is a model number</li>
 * <li>15 or 70-72 is a residue number, or range of residue numbers</li>
 * <li>.A is a chain identifier</li>
 * <li>residue ranges are separated by comma</li>
 * <li>atomspecs for distinct models are separated by | (or)</li>
 * </ul>
 * 
 * <pre>
 * &#64;see http://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/frameatom_spec.html
 * </pre>
 */
public class AtomSpecModel
{
  private Map<Integer, Map<String, List<int[]>>> atomSpec;

  /**
   * Constructor
   */
  public AtomSpecModel()
  {
    atomSpec = new TreeMap<Integer, Map<String, List<int[]>>>();
  }

  /**
   * Adds one contiguous range to this atom spec
   * 
   * @param model
   * @param startPos
   * @param endPos
   * @param chain
   */
  public void addRange(int model, int startPos, int endPos, String chain)
  {
    /*
     * Get/initialize map of data for the colour and model
     */
    Map<String, List<int[]>> modelData = atomSpec.get(model);
    if (modelData == null)
    {
      atomSpec.put(model, modelData = new TreeMap<String, List<int[]>>());
    }

    /*
     * Get/initialize map of data for colour, model and chain
     */
    List<int[]> chainData = modelData.get(chain);
    if (chainData == null)
    {
      chainData = new ArrayList<int[]>();
      modelData.put(chain, chainData);
    }

    /*
     * Add the start/end positions
     */
    chainData.add(new int[] { startPos, endPos });
    // TODO add intelligently, using a RangeList class
  }

  /**
   * Returns the range(s) formatted as a Chimera atomspec
   * 
   * @return
   */
  public String getAtomSpec()
  {
    StringBuilder sb = new StringBuilder(128);
    boolean firstModel = true;
    for (Integer model : atomSpec.keySet())
    {
      if (!firstModel)
      {
        sb.append("|");
      }
      firstModel = false;
      sb.append("#").append(model).append(":");

      boolean firstPositionForModel = true;
      final Map<String, List<int[]>> modelData = atomSpec.get(model);

      for (String chain : modelData.keySet())
      {
        chain = " ".equals(chain) ? chain : chain.trim();

        List<int[]> rangeList = modelData.get(chain);

        /*
         * sort ranges into ascending start position order
         */
        Collections.sort(rangeList, IntRangeComparator.ASCENDING);

        int start = rangeList.isEmpty() ? 0 : rangeList.get(0)[0];
        int end = rangeList.isEmpty() ? 0 : rangeList.get(0)[1];

        Iterator<int[]> iterator = rangeList.iterator();
        while (iterator.hasNext())
        {
          int[] range = iterator.next();
          if (range[0] <= end + 1)
          {
            /*
             * range overlaps or is contiguous with the last one
             * - so just extend the end position, and carry on
             * (unless this is the last in the list)
             */
            end = Math.max(end, range[1]);
          }
          else
          {
            /*
             * we have a break so append the last range
             */
            appendRange(sb, start, end, chain, firstPositionForModel);
            firstPositionForModel = false;
            start = range[0];
            end = range[1];
          }
        }

        /*
         * and append the last range
         */
        if (!rangeList.isEmpty())
        {
          appendRange(sb, start, end, chain, firstPositionForModel);
          firstPositionForModel = false;
        }
      }
    }
    return sb.toString();
  }

  /**
   * @param sb
   * @param start
   * @param end
   * @param chain
   * @param firstPositionForModel
   */
  protected void appendRange(StringBuilder sb, int start, int end,
          String chain, boolean firstPositionForModel)
  {
    if (!firstPositionForModel)
    {
      sb.append(",");
    }
    if (end == start)
    {
      sb.append(start);
    }
    else
    {
      sb.append(start).append("-").append(end);
    }

    sb.append(".");
    if (!" ".equals(chain)) {
      sb.append(chain);
    }
  }
}
