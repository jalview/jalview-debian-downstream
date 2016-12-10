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
package jalview.workers;

import jalview.datamodel.SequenceFeature;

import java.util.List;

/**
 * An interface for a type that returns counts of any value of interest at a
 * sequence position that can be determined from the sequence character and any
 * features present at that position
 * 
 */
public interface FeatureCounterI
{
  /**
   * Returns a count of some property of interest, for example
   * <ul>
   * <li>the number of variant features at the position</li>
   * <li>the number of Cath features of status 'True Positive'</li>
   * <li>1 if the residue is hydrophobic, else 0</li>
   * <li>etc</li>
   * </ul>
   * 
   * @param residue
   *          the residue (or gap) at the position
   * @param a
   *          list of any sequence features which include the position
   */
  int count(String residue, List<SequenceFeature> features);

  /**
   * Returns a name for the annotation that this is counting, for use as the
   * displayed label
   * 
   * @return
   */
  String getName();

  /**
   * Returns a description for the annotation, for display as a tooltip
   * 
   * @return
   */
  String getDescription();

  /**
   * Returns the colour (as [red, green, blue] values in the range 0-255) to use
   * for the minimum value on histogram bars. If this is different to
   * getMaxColour(), then bars will have a graduated colour.
   * 
   * @return
   */
  int[] getMinColour();

  /**
   * Returns the colour (as [red, green, blue] values in the range 0-255) to use
   * for the maximum value on histogram bars. If this is the same as
   * getMinColour(), then bars will have a single colour (not graduated).
   * 
   * @return
   */
  int[] getMaxColour();
}
