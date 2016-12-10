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
package jalview.controller;

import jalview.analysis.AlignmentSorter;
import jalview.api.AlignViewControllerGuiI;
import jalview.api.AlignViewControllerI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.commands.OrderCommand;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.FeaturesFile;
import jalview.util.MessageManager;

import java.awt.Color;
import java.util.BitSet;
import java.util.List;

public class AlignViewController implements AlignViewControllerI
{
  AlignViewportI viewport = null;

  AlignmentViewPanel alignPanel = null;

  /**
   * the GUI container that is handling interactions with the user
   */
  private AlignViewControllerGuiI avcg;

  @Override
  protected void finalize() throws Throwable
  {
    viewport = null;
    alignPanel = null;
    avcg = null;
  };

  public AlignViewController(AlignViewControllerGuiI alignFrame,
          AlignViewportI viewport, AlignmentViewPanel alignPanel)
  {
    this.avcg = alignFrame;
    this.viewport = viewport;
    this.alignPanel = alignPanel;
  }

  @Override
  public void setViewportAndAlignmentPanel(AlignViewportI viewport,
          AlignmentViewPanel alignPanel)
  {
    this.alignPanel = alignPanel;
    this.viewport = viewport;

  }

  @Override
  public boolean makeGroupsFromSelection()
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    ColumnSelection cs = viewport.getColumnSelection();
    SequenceGroup[] gps = null;
    if (sg != null && (cs == null || cs.isEmpty()))
    {
      gps = jalview.analysis.Grouping.makeGroupsFrom(viewport
              .getSequenceSelection(), viewport.getAlignmentView(true)
              .getSequenceStrings(viewport.getGapCharacter()), viewport
              .getAlignment().getGroups());
    }
    else
    {
      if (cs != null)
      {
        gps = jalview.analysis.Grouping.makeGroupsFromCols(
                (sg == null) ? viewport.getAlignment().getSequencesArray()
                        : sg.getSequences().toArray(new SequenceI[0]), cs,
                viewport.getAlignment().getGroups());
      }
    }
    if (gps != null)
    {
      viewport.getAlignment().deleteAllGroups();
      viewport.clearSequenceColours();
      viewport.setSelectionGroup(null);
      // set view properties for each group
      for (int g = 0; g < gps.length; g++)
      {
        // gps[g].setShowunconserved(viewport.getShowUnconserved());
        gps[g].setshowSequenceLogo(viewport.isShowSequenceLogo());
        viewport.getAlignment().addGroup(gps[g]);
        Color col = new Color((int) (Math.random() * 255),
                (int) (Math.random() * 255), (int) (Math.random() * 255));
        col = col.brighter();
        for (SequenceI sq : gps[g].getSequences(null))
        {
          viewport.setSequenceColour(sq, col);
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean createGroup()
  {

    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null)
    {
      viewport.getAlignment().addGroup(sg);
      return true;
    }
    return false;
  }

  @Override
  public boolean unGroup()
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null)
    {
      viewport.getAlignment().deleteGroup(sg);
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteGroups()
  {
    if (viewport.getAlignment().getGroups() != null
            && viewport.getAlignment().getGroups().size() > 0)
    {
      viewport.getAlignment().deleteAllGroups();
      viewport.clearSequenceColours();
      viewport.setSelectionGroup(null);
      return true;
    }
    return false;
  }

  @Override
  public boolean markColumnsContainingFeatures(boolean invert,
          boolean extendCurrent, boolean toggle, String featureType)
  {
    // JBPNote this routine could also mark rows, not just columns.
    // need a decent query structure to allow all types of feature searches
    BitSet bs = new BitSet();
    SequenceCollectionI sqcol = (viewport.getSelectionGroup() == null || extendCurrent) ? viewport
            .getAlignment() : viewport.getSelectionGroup();

    int nseq = findColumnsWithFeature(featureType, sqcol, bs);

    ColumnSelection cs = viewport.getColumnSelection();
    if (cs == null)
    {
      cs = new ColumnSelection();
    }

    if (bs.cardinality() > 0 || invert)
    {
      boolean changed = cs.markColumns(bs, sqcol.getStartRes(),
              sqcol.getEndRes(), invert, extendCurrent, toggle);
      if (changed)
      {
        viewport.setColumnSelection(cs);
        alignPanel.paintAlignment(true);
        int columnCount = invert ? (sqcol.getEndRes() - sqcol.getStartRes() + 1)
                - bs.cardinality()
                : bs.cardinality();
        avcg.setStatus(MessageManager.formatMessage(
                "label.view_controller_toggled_marked",
                new String[] {
                    toggle ? MessageManager.getString("label.toggled")
                            : MessageManager.getString("label.marked"),
                    String.valueOf(columnCount),
                    invert ? MessageManager
                            .getString("label.not_containing")
                            : MessageManager.getString("label.containing"),
                    featureType, Integer.valueOf(nseq).toString() }));
        return true;
      }
    }
    else
    {
      avcg.setStatus(MessageManager.formatMessage(
              "label.no_feature_of_type_found",
              new String[] { featureType }));
      if (!extendCurrent)
      {
        cs.clear();
        alignPanel.paintAlignment(true);
      }
    }
    return false;
  }

  /**
   * Sets a bit in the BitSet for each column (base 0) in the sequence
   * collection which includes the specified feature type. Returns the number of
   * sequences which have the feature in the selected range.
   * 
   * @param featureType
   * @param sqcol
   * @param bs
   * @return
   */
  static int findColumnsWithFeature(String featureType,
          SequenceCollectionI sqcol, BitSet bs)
  {
    final int startPosition = sqcol.getStartRes() + 1; // converted to base 1
    final int endPosition = sqcol.getEndRes() + 1;
    List<SequenceI> seqs = sqcol.getSequences();
    int nseq = 0;
    for (SequenceI sq : seqs)
    {
      boolean sequenceHasFeature = false;
      if (sq != null)
      {
        SequenceFeature[] sfs = sq.getSequenceFeatures();
        if (sfs != null)
        {
          int ist = sq.findIndex(sq.getStart());
          int iend = sq.findIndex(sq.getEnd());
          if (iend < startPosition || ist > endPosition)
          {
            // sequence not in region
            continue;
          }
          for (SequenceFeature sf : sfs)
          {
            // future functionality - featureType == null means mark columns
            // containing all displayed features
            if (sf != null && (featureType.equals(sf.getType())))
            {
              // optimisation - could consider 'spos,apos' like cursor argument
              // - findIndex wastes time by starting from first character and
              // counting

              int sfStartCol = sq.findIndex(sf.getBegin());
              int sfEndCol = sq.findIndex(sf.getEnd());

              if (sf.isContactFeature())
              {
                /*
                 * 'contact' feature - check for 'start' or 'end'
                 * position within the selected region
                 */
                if (sfStartCol >= startPosition
                        && sfStartCol <= endPosition)
                {
                  bs.set(sfStartCol - 1);
                  sequenceHasFeature = true;
                }
                if (sfEndCol >= startPosition && sfEndCol <= endPosition)
                {
                  bs.set(sfEndCol - 1);
                  sequenceHasFeature = true;
                }
                continue;
              }

              /*
               * contiguous feature - select feature positions (if any) 
               * within the selected region
               */
              if (sfStartCol > endPosition || sfEndCol < startPosition)
              {
                // feature is outside selected region
                continue;
              }
              sequenceHasFeature = true;
              if (sfStartCol < startPosition)
              {
                sfStartCol = startPosition;
              }
              if (sfStartCol < ist)
              {
                sfStartCol = ist;
              }
              if (sfEndCol > endPosition)
              {
                sfEndCol = endPosition;
              }
              for (; sfStartCol <= sfEndCol; sfStartCol++)
              {
                bs.set(sfStartCol - 1); // convert to base 0
              }
            }
          }
        }

        if (sequenceHasFeature)
        {
          nseq++;
        }
      }
    }
    return nseq;
  }

  @Override
  public void sortAlignmentByFeatureDensity(List<String> typ)
  {
    sortBy(typ, "Sort by Density", AlignmentSorter.FEATURE_DENSITY);
  }

  protected void sortBy(List<String> typ, String methodText,
          final String method)
  {
    FeatureRenderer fr = alignPanel.getFeatureRenderer();
    if (typ == null && fr != null)
    {
      typ = fr.getDisplayedFeatureTypes();
    }
    List<String> gps = null;
    if (fr != null)
    {
      gps = fr.getDisplayedFeatureGroups();
    }
    AlignmentI al = viewport.getAlignment();

    int start, stop;
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null)
    {
      start = sg.getStartRes();
      stop = sg.getEndRes();
    }
    else
    {
      start = 0;
      stop = al.getWidth();
    }
    SequenceI[] oldOrder = al.getSequencesArray();
    AlignmentSorter.sortByFeature(typ, gps, start, stop, al, method);
    avcg.addHistoryItem(new OrderCommand(methodText, oldOrder, viewport
            .getAlignment()));
    alignPanel.paintAlignment(true);

  }

  @Override
  public void sortAlignmentByFeatureScore(List<String> typ)
  {
    sortBy(typ, "Sort by Feature Score", AlignmentSorter.FEATURE_SCORE);
  }

  @Override
  public boolean parseFeaturesFile(String file, String protocol,
          boolean relaxedIdMatching)
  {
    boolean featuresFile = false;
    try
    {
      featuresFile = new FeaturesFile(false, file, protocol).parse(viewport
              .getAlignment().getDataset(), alignPanel.getFeatureRenderer()
              .getFeatureColours(), false, relaxedIdMatching);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    if (featuresFile)
    {
      avcg.refreshFeatureUI(true);
      if (alignPanel.getFeatureRenderer() != null)
      {
        // update the min/max ranges where necessary
        alignPanel.getFeatureRenderer().findAllFeatures(true);
      }
      if (avcg.getFeatureSettingsUI() != null)
      {
        avcg.getFeatureSettingsUI().discoverAllFeatureData();
      }
      alignPanel.paintAlignment(true);
    }

    return featuresFile;

  }

  @Override
  public boolean markHighlightedColumns(boolean invert,
          boolean extendCurrent, boolean toggle)
  {
    if (!viewport.hasSearchResults())
    {
      // do nothing if no selection exists
      return false;
    }
    // JBPNote this routine could also mark rows, not just columns.
    BitSet bs = new BitSet();
    SequenceCollectionI sqcol = (viewport.getSelectionGroup() == null || extendCurrent) ? viewport
            .getAlignment() : viewport.getSelectionGroup();

    // this could be a lambda... - the remains of the method is boilerplate,
    // except for the different messages for reporting selection.
    int nseq = viewport.getSearchResults().markColumns(sqcol, bs);

    ColumnSelection cs = viewport.getColumnSelection();
    if (cs == null)
    {
      cs = new ColumnSelection();
    }

    if (bs.cardinality() > 0 || invert)
    {
      boolean changed = cs.markColumns(bs, sqcol.getStartRes(),
              sqcol.getEndRes(), invert, extendCurrent, toggle);
      if (changed)
      {
        viewport.setColumnSelection(cs);
        alignPanel.paintAlignment(true);
        int columnCount = invert ? (sqcol.getEndRes() - sqcol.getStartRes() + 1)
                - bs.cardinality()
                : bs.cardinality();
        avcg.setStatus(MessageManager.formatMessage(
                "label.view_controller_toggled_marked",
                new String[] {
                    toggle ? MessageManager.getString("label.toggled")
                            : MessageManager.getString("label.marked"),
                    String.valueOf(columnCount),
                    invert ? MessageManager
                            .getString("label.not_containing")
                            : MessageManager.getString("label.containing"),
                    "Highlight", Integer.valueOf(nseq).toString() }));
        return true;
      }
    }
    else
    {
      avcg.setStatus(MessageManager
              .formatMessage("No highlighted regions marked"));
      if (!extendCurrent)
      {
        cs.clear();
        alignPanel.paintAlignment(true);
      }
    }
    return false;
  }

}
