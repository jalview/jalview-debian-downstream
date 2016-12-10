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
package jalview.api;

import jalview.datamodel.SequenceFeature;

import java.awt.Color;

public interface FeatureColourI
{

  /**
   * Answers true when the feature colour varies across the score range
   * 
   * @return
   */
  boolean isGraduatedColour();

  /**
   * Returns the feature colour (when isGraduatedColour answers false)
   * 
   * @return
   */
  Color getColour();

  /**
   * Returns the minimum colour (when isGraduatedColour answers true)
   * 
   * @return
   */
  Color getMinColour();

  /**
   * Returns the maximum colour (when isGraduatedColour answers true)
   * 
   * @return
   */
  Color getMaxColour();

  /**
   * Answers true if the feature has a single colour, i.e. if isColourByLabel()
   * and isGraduatedColour() both answer false
   * 
   * @return
   */
  boolean isSimpleColour();

  /**
   * Answers true if the feature is coloured by label (description)
   * 
   * @return
   */
  boolean isColourByLabel();

  void setColourByLabel(boolean b);

  /**
   * Answers true if the feature is coloured below a threshold value; only
   * applicable when isGraduatedColour answers true
   * 
   * @return
   */
  boolean isBelowThreshold();

  void setBelowThreshold(boolean b);

  /**
   * Answers true if the feature is coloured above a threshold value; only
   * applicable when isGraduatedColour answers true
   * 
   * @return
   */
  boolean isAboveThreshold();

  void setAboveThreshold(boolean b);

  /**
   * Answers true if the threshold is the minimum value (when
   * isAboveThreshold()) or maximum value (when isBelowThreshold()) of the
   * colour range; only applicable when isGraduatedColour and either
   * isAboveThreshold() or isBelowThreshold() answers true
   * 
   * @return
   */
  boolean isThresholdMinMax();

  void setThresholdMinMax(boolean b);

  /**
   * Returns the threshold value (if any), else zero
   * 
   * @return
   */
  float getThreshold();

  void setThreshold(float f);

  /**
   * Answers true if the colour varies between the actual minimum and maximum
   * score values of the feature, or false if between absolute minimum and
   * maximum values (or if not a graduated colour).
   * 
   * @return
   */
  boolean isAutoScaled();

  void setAutoScaled(boolean b);

  /**
   * Returns the maximum score of the graduated colour range
   * 
   * @return
   */
  float getMax();

  /**
   * Returns the minimum score of the graduated colour range
   * 
   * @return
   */
  float getMin();

  /**
   * Answers true if either isAboveThreshold or isBelowThreshold answers true
   * 
   * @return
   */
  boolean hasThreshold();

  /**
   * Returns the computed colour for the given sequence feature
   * 
   * @param feature
   * @return
   */
  Color getColor(SequenceFeature feature);

  /**
   * Answers true if the feature has a simple colour, or is coloured by label,
   * or has a graduated colour and the score of this feature instance is within
   * the range to render (if any), i.e. does not lie below or above any
   * threshold set.
   * 
   * @param feature
   * @return
   */
  boolean isColored(SequenceFeature feature);

  /**
   * Update the min-max range for a graduated colour scheme
   * 
   * @param min
   * @param max
   */
  void updateBounds(float min, float max);

  /**
   * Returns the colour in Jalview features file format
   * 
   * @return
   */
  String toJalviewFormat(String featureType);
}
