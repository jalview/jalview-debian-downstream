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
import java.util.ArrayList;
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
    if (sg != null
            && (cs == null || cs.getSelected() == null || cs.size() == 0))
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
    int alw, alStart;
    SequenceCollectionI sqcol = (viewport.getSelectionGroup() == null ? viewport
            .getAlignment() : viewport.getSelectionGroup());
    alStart = sqcol.getStartRes();
    alw = sqcol.getEndRes() + 1;
    List<SequenceI> seqs = sqcol.getSequences();
    int nseq = 0;
    for (SequenceI sq : seqs)
    {
      int tfeat = 0;
      if (sq != null)
      {
        SequenceFeature[] sf = sq.getSequenceFeatures();
        if (sf != null)
        {
          int ist = sq.findIndex(sq.getStart());
          int iend = sq.findIndex(sq.getEnd());
          if (iend < alStart || ist > alw)
          {
            // sequence not in region
            continue;
          }
          for (SequenceFeature sfpos : sf)
          {
            // future functionalty - featureType == null means mark columns
            // containing all displayed features
            if (sfpos != null && (featureType.equals(sfpos.getType())))
            {
              tfeat++;
              // optimisation - could consider 'spos,apos' like cursor argument
              // - findIndex wastes time by starting from first character and
              // counting

              int i = sq.findIndex(sfpos.getBegin());
              int j = sq.findIndex(sfpos.getEnd());
              if (j < alStart || i > alw)
              {
                // feature is outside selected region
                continue;
              }
              if (i < alStart)
              {
                i = alStart;
              }
              if (i < ist)
              {
                i = ist;
              }
              if (j > alw)
              {
                j = alw;
              }
              for (; i <= j; i++)
              {
                bs.set(i - 1);
              }
            }
          }
        }

        if (tfeat > 0)
        {
          nseq++;
        }
      }
    }
    ColumnSelection cs = viewport.getColumnSelection();
    if (bs.cardinality() > 0 || invert)
    {
      if (cs == null)
      {
        cs = new ColumnSelection();
      }
      else
      {
        if (!extendCurrent)
        {
          cs.clear();
        }
      }
      if (invert)
      {
        // invert only in the currently selected sequence region
        for (int i = bs.nextClearBit(alStart), ibs = bs.nextSetBit(alStart); i >= alStart
                && i < (alw);)
        {
          if (ibs < 0 || i < ibs)
          {
            if (toggle && cs.contains(i))
            {
              cs.removeElement(i++);
            }
            else
            {
              cs.addElement(i++);
            }
          }
          else
          {
            i = bs.nextClearBit(ibs);
            ibs = bs.nextSetBit(i);
          }
        }
      }
      else
      {
        for (int i = bs.nextSetBit(alStart); i >= alStart; i = bs
                .nextSetBit(i + 1))
        {
          if (toggle && cs.contains(i))
          {
            cs.removeElement(i);
          }
          else
          {
            cs.addElement(i);
          }
        }
      }
      viewport.setColumnSelection(cs);
      alignPanel.paintAlignment(true);
      avcg.setStatus(MessageManager.formatMessage(
              "label.view_controller_toggled_marked",
              new String[] {
                  (toggle ? MessageManager.getString("label.toggled")
                          : MessageManager.getString("label.marked")),
                  (invert ? (Integer.valueOf((alw - alStart)
                          - bs.cardinality()).toString()) : (Integer
                          .valueOf(bs.cardinality()).toString())),
                  featureType, Integer.valueOf(nseq).toString() }));
      return true;
    }
    else
    {
      avcg.setStatus(MessageManager.formatMessage(
              "label.no_feature_of_type_found",
              new String[] { featureType }));
      if (!extendCurrent && cs != null)
      {
        cs.clear();
        alignPanel.paintAlignment(true);
      }
      return false;
    }
  }

  @Override
  public void sortAlignmentByFeatureDensity(String[] typ)
  {
    sortBy(typ, "Sort by Density", AlignmentSorter.FEATURE_DENSITY);
  }

  protected void sortBy(String[] typ, String methodText, final String method)
  {
    FeatureRenderer fr = alignPanel.getFeatureRenderer();
    if (typ == null)
    {
      typ = fr == null ? null : fr.getDisplayedFeatureTypes();
    }
    String gps[] = null;
    gps = fr == null ? null : fr.getDisplayedFeatureGroups();
    if (typ != null)
    {
      ArrayList types = new ArrayList();
      for (int i = 0; i < typ.length; i++)
      {
        if (typ[i] != null)
        {
          types.add(typ[i]);
        }
        typ = new String[types.size()];
        types.toArray(typ);
      }
    }
    if (gps != null)
    {
      ArrayList grps = new ArrayList();

      for (int i = 0; i < gps.length; i++)
      {
        if (gps[i] != null)
        {
          grps.add(gps[i]);
        }
      }
      gps = new String[grps.size()];
      grps.toArray(gps);
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
  public void sortAlignmentByFeatureScore(String[] typ)
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
      featuresFile = new FeaturesFile(file, protocol).parse(viewport
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
}
