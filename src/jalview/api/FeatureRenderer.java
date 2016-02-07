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
package jalview.api;

import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * Abstract feature renderer interface
 * 
 * @author JimP
 * 
 */
public interface FeatureRenderer
{

  /**
   * compute the perceived colour for a given column position in sequenceI,
   * taking transparency and feature visibility into account.
   * 
   * @param col
   *          - background colour (due to alignment/group shading schemes, etc).
   * @param sequenceI
   *          - sequence providing features
   * @param r
   *          - column position
   * @return
   */
  Color findFeatureColour(Color col, SequenceI sequenceI, int r);

  /**
   * trigger the feature discovery process for a newly created feature renderer.
   */
  void featuresAdded();

  /**
   * 
   * @param ft
   * @return display style for a feature
   */
  Object getFeatureStyle(String ft);

  /**
   * update the feature style for a particular feature
   * 
   * @param ft
   * @param ggc
   *          - currently allows java.awt.Color and
   *          jalview.schemes.GraduatedColor
   */
  void setColour(String ft, Object ggc);

  AlignViewportI getViewport();

  /**
   * 
   * @return container managing list of feature types and their visibility
   */
  FeaturesDisplayedI getFeaturesDisplayed();

  /**
   * get display style for all features types - visible or invisible
   * 
   * @return
   */
  Map<String, Object> getFeatureColours();

  /**
   * query the alignment view to find all features
   * 
   * @param newMadeVisible
   *          - when true, automatically make newly discovered types visible
   */
  void findAllFeatures(boolean newMadeVisible);

  /**
   * get display style for all features types currently visible
   * 
   * @return
   */
  Map<String, Object> getDisplayedFeatureCols();

  /**
   * get all registered groups
   * 
   * @return
   */
  List<String> getFeatureGroups();

  /**
   * get groups that are visible/invisible
   * 
   * @param visible
   * @return
   */
  List<String> getGroups(boolean visible);

  /**
   * change visibility for a range of groups
   * 
   * @param toset
   * @param visible
   */
  void setGroupVisibility(List<String> toset, boolean visible);

  /**
   * change visibiilty of given group
   * 
   * @param group
   * @param visible
   */
  void setGroupVisibility(String group, boolean visible);

  /**
   * locate features at a particular position on the given sequence
   * 
   * @param sequence
   * @param res
   * @return
   */
  List<SequenceFeature> findFeaturesAtRes(SequenceI sequence, int res);

  /**
   * 
   * @return true if the rendering platform supports transparency
   */
  boolean isTransparencyAvailable();

  /**
   * get current displayed types
   * 
   * @return
   */

  String[] getDisplayedFeatureTypes();

  /**
   * get current displayed groups
   * 
   * @return
   */
  String[] getDisplayedFeatureGroups();

  /**
   * display all features of these types
   * 
   * @param featureTypes
   */
  void setAllVisible(List<String> featureTypes);

  /**
   * display featureType
   * 
   * @param featureType
   */
  void setVisible(String featureType);

}
