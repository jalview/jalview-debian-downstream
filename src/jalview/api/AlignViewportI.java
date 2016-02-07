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

import jalview.analysis.Conservation;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.CigarArray;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;

import java.awt.Color;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author jimp
 * 
 */
public interface AlignViewportI extends ViewStyleI
{

  int getEndRes();

  /**
   * calculate the height for visible annotation, revalidating bounds where
   * necessary ABSTRACT GUI METHOD
   * 
   * @return total height of annotation
   */
  public int calcPanelHeight();

  boolean hasHiddenColumns();

  boolean isValidCharWidth();

  boolean isShowConsensusHistogram();

  boolean isShowSequenceLogo();

  boolean isNormaliseSequenceLogo();

  ColourSchemeI getGlobalColourScheme();

  AlignmentI getAlignment();

  ColumnSelection getColumnSelection();

  Hashtable[] getSequenceConsensusHash();

  /**
   * Get consensus data table for the cDNA complement of this alignment (if any)
   * 
   * @return
   */
  Hashtable[] getComplementConsensusHash();

  Hashtable[] getRnaStructureConsensusHash();

  boolean isIgnoreGapsConsensus();

  boolean isCalculationInProgress(AlignmentAnnotation alignmentAnnotation);

  AlignmentAnnotation getAlignmentQualityAnnot();

  AlignmentAnnotation getAlignmentConservationAnnotation();

  /**
   * get the container for alignment consensus annotation
   * 
   * @return
   */
  AlignmentAnnotation getAlignmentConsensusAnnotation();

  /**
   * get the container for cDNA complement consensus annotation
   * 
   * @return
   */
  AlignmentAnnotation getComplementConsensusAnnotation();

  /**
   * Test to see if viewport is still open and active
   * 
   * @return true indicates that all references to viewport should be dropped
   */
  boolean isClosed();

  /**
   * get the associated calculation thread manager for the view
   * 
   * @return
   */
  AlignCalcManagerI getCalcManager();

  /**
   * get the percentage gaps allowed in a conservation calculation
   * 
   */
  public int getConsPercGaps();

  /**
   * set the consensus result object for the viewport
   * 
   * @param hconsensus
   */
  void setSequenceConsensusHash(Hashtable[] hconsensus);

  /**
   * Set the cDNA complement consensus for the viewport
   * 
   * @param hconsensus
   */
  void setComplementConsensusHash(Hashtable[] hconsensus);

  /**
   * 
   * @return the alignment annotatino row for the structure consensus
   *         calculation
   */
  AlignmentAnnotation getAlignmentStrucConsensusAnnotation();

  /**
   * set the Rna structure consensus result object for the viewport
   * 
   * @param hStrucConsensus
   */
  void setRnaStructureConsensusHash(Hashtable[] hStrucConsensus);

  /**
   * set global colourscheme
   * 
   * @param rhc
   */
  void setGlobalColourScheme(ColourSchemeI rhc);

  Map<SequenceI, SequenceCollectionI> getHiddenRepSequences();

  void setHiddenRepSequences(
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences);

  /**
   * hides or shows dynamic annotation rows based on groups and group and
   * alignment associated auto-annotation state flags apply the current
   * group/autoannotation settings to the alignment view. Usually you should
   * call the AlignmentViewPanel.adjustAnnotationHeight() method afterwards to
   * ensure the annotation panel bounds are set correctly.
   * 
   * @param applyGlobalSettings
   *          - apply to all autoannotation rows or just the ones associated
   *          with the current visible region
   * @param preserveNewGroupSettings
   *          - don't apply global settings to groups which don't already have
   *          group associated annotation
   */
  void updateGroupAnnotationSettings(boolean applyGlobalSettings,
          boolean preserveNewGroupSettings);

  void setSequenceColour(SequenceI seq, Color col);

  Color getSequenceColour(SequenceI seq);

  void updateSequenceIdColours();

  SequenceGroup getSelectionGroup();

  /**
   * get the currently selected sequence objects or all the sequences in the
   * alignment. TODO: change to List<>
   * 
   * @return array of references to sequence objects
   */
  SequenceI[] getSequenceSelection();

  void clearSequenceColours();

  /**
   * This method returns the visible alignment as text, as seen on the GUI, ie
   * if columns are hidden they will not be returned in the result. Use this for
   * calculating trees, PCA, redundancy etc on views which contain hidden
   * columns.
   * 
   * @return String[]
   */
  CigarArray getViewAsCigars(boolean selectedRegionOnly);

  /**
   * return a compact representation of the current alignment selection to pass
   * to an analysis function
   * 
   * @param selectedOnly
   *          boolean true to just return the selected view
   * @return AlignmentView
   */
  AlignmentView getAlignmentView(boolean selectedOnly);

  /**
   * return a compact representation of the current alignment selection to pass
   * to an analysis function
   * 
   * @param selectedOnly
   *          boolean true to just return the selected view
   * @param markGroups
   *          boolean true to annotate the alignment view with groups on the
   *          alignment (and intersecting with selected region if selectedOnly
   *          is true)
   * @return AlignmentView
   */
  AlignmentView getAlignmentView(boolean selectedOnly, boolean markGroups);

  /**
   * This method returns the visible alignment as text, as seen on the GUI, ie
   * if columns are hidden they will not be returned in the result. Use this for
   * calculating trees, PCA, redundancy etc on views which contain hidden
   * columns.
   * 
   * @return String[]
   */
  String[] getViewAsString(boolean selectedRegionOnly);

  void setSelectionGroup(SequenceGroup sg);

  char getGapCharacter();

  void setColumnSelection(ColumnSelection cs);

  void setConservation(Conservation cons);

  /**
   * get a copy of the currently visible alignment annotation
   * 
   * @param selectedOnly
   *          if true - trim to selected regions on the alignment
   * @return an empty list or new alignment annotation objects shown only
   *         visible columns trimmed to selected region only
   */
  List<AlignmentAnnotation> getVisibleAlignmentAnnotation(
          boolean selectedOnly);

  FeaturesDisplayedI getFeaturesDisplayed();

  String getSequenceSetId();

  boolean areFeaturesDisplayed();

  void setFeaturesDisplayed(FeaturesDisplayedI featuresDisplayedI);

  void alignmentChanged(AlignmentViewPanel ap);

  /**
   * @return the padGaps
   */
  boolean isPadGaps();

  /**
   * @param padGaps
   *          the padGaps to set
   */
  void setPadGaps(boolean padGaps);

  /**
   * return visible region boundaries within given column range
   * 
   * @param min
   *          first column (inclusive, from 0)
   * @param max
   *          last column (exclusive)
   * @return int[][] range of {start,end} visible positions
   */
  List<int[]> getVisibleRegionBoundaries(int min, int max);

  /**
   * This method returns an array of new SequenceI objects derived from the
   * whole alignment or just the current selection with start and end points
   * adjusted
   * 
   * @note if you need references to the actual SequenceI objects in the
   *       alignment or currently selected then use getSequenceSelection()
   * @return selection as new sequenceI objects
   */
  SequenceI[] getSelectionAsNewSequence();

  void invertColumnSelection();

  /**
   * broadcast selection to any interested parties
   */
  void sendSelection();

  /**
   * calculate the row position for alignmentIndex if all hidden sequences were
   * shown
   * 
   * @param alignmentIndex
   * @return adjusted row position
   */
  int adjustForHiddenSeqs(int alignmentIndex);

  boolean hasHiddenRows();

  /**
   * 
   * @return a copy of this view's current display settings
   */
  public ViewStyleI getViewStyle();

  /**
   * update the view's display settings with the given style set
   * 
   * @param settingsForView
   */
  public void setViewStyle(ViewStyleI settingsForView);

  /**
   * Returns a viewport which holds the cDna for this (protein), or vice versa,
   * or null if none is set.
   * 
   * @return
   */
  AlignViewportI getCodingComplement();

  /**
   * Sets the viewport which holds the cDna for this (protein), or vice versa.
   * Implementation should guarantee that the reciprocal relationship is always
   * set, i.e. each viewport is the complement of the other.
   */
  void setCodingComplement(AlignViewportI sl);

  /**
   * Answers true if viewport hosts DNA/RNA, else false.
   * 
   * @return
   */
  boolean isNucleotide();

  /**
   * Returns an id guaranteed to be unique for this viewport.
   * 
   * @return
   */
  String getViewId();

  /**
   * Return true if view should scroll to show the highlighted region of a
   * sequence
   * 
   * @return
   */
  boolean isFollowHighlight();

  /**
   * Set whether view should scroll to show the highlighted region of a sequence
   */
  void setFollowHighlight(boolean b);
}
