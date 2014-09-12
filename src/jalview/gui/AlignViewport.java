/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.gui;

import java.util.*;

import java.awt.*;

import jalview.analysis.*;
import jalview.api.StructureSelectionManagerProvider;

import jalview.bin.*;

import jalview.datamodel.*;

import jalview.schemes.*;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.141 $
 */
public class AlignViewport implements SelectionSource, VamsasSource
{
  private static final int RIGHT_JUSTIFY = 1;

  int startRes;

  int endRes;

  int startSeq;

  int endSeq;

  boolean showJVSuffix = true;

  boolean showText = true;

  boolean showColourText = false;

  boolean showBoxes = true;

  boolean wrapAlignment = false;

  boolean renderGaps = true;

  boolean showSequenceFeatures = false;

  boolean showAnnotation = true;

  boolean colourAppliesToAllGroups = true;

  ColourSchemeI globalColourScheme = null;

  boolean conservationColourSelected = false;

  boolean abovePIDThreshold = false;

  SequenceGroup selectionGroup;

  int charHeight;

  int charWidth;

  boolean validCharWidth;

  int wrappedWidth;

  Font font;

  boolean seqNameItalics;

  AlignmentI alignment;

  ColumnSelection colSel = new ColumnSelection();

  int threshold;

  int increment;

  NJTree currentTree = null;

  boolean scaleAboveWrapped = false;

  boolean scaleLeftWrapped = true;

  boolean scaleRightWrapped = true;

  boolean hasHiddenColumns = false;

  boolean hasHiddenRows = false;

  boolean showHiddenMarkers = true;

  boolean cursorMode = false;

  /**
   * Keys are the feature types which are currently visible. Note: Values are
   * not used!
   */
  Hashtable featuresDisplayed = null;

  /** DOCUMENT ME!! */
  public Hashtable[] hconsensus;

  AlignmentAnnotation consensus;

  AlignmentAnnotation conservation;

  AlignmentAnnotation quality;

  AlignmentAnnotation[] groupConsensus;

  AlignmentAnnotation[] groupConservation;

  boolean autoCalculateConsensus = true;

  /** DOCUMENT ME!! */
  public int ConsPercGaps = 25; // JBPNote : This should be a scalable property!

  // JBPNote Prolly only need this in the applet version.
  private java.beans.PropertyChangeSupport changeSupport = new java.beans.PropertyChangeSupport(
          this);

  boolean ignoreGapsInConsensusCalculation = false;

  boolean isDataset = false;

  boolean antiAlias = false;

  boolean padGaps = false;

  Rectangle explodedPosition;

  String viewName;

  String sequenceSetID;

  boolean gatherViewsHere = false;

  Stack historyList = new Stack();

  Stack redoList = new Stack();

  Hashtable sequenceColours;

  int thresholdTextColour = 0;

  Color textColour = Color.black;

  Color textColour2 = Color.white;

  boolean rightAlignIds = false;

  Hashtable hiddenRepSequences;

  boolean sortByTree;

  /**
   * Creates a new AlignViewport object.
   * 
   * @param al
   *          alignment to view
   */
  public AlignViewport(AlignmentI al)
  {
    setAlignment(al);
    init();
  }

  /**
   * Create a new AlignViewport object with a specific sequence set ID
   * 
   * @param al
   * @param seqsetid
   *          (may be null - but potential for ambiguous constructor exception)
   */
  public AlignViewport(AlignmentI al, String seqsetid)
  {
    this(al, seqsetid, null);
  }

  public AlignViewport(AlignmentI al, String seqsetid, String viewid)
  {
    sequenceSetID = seqsetid;
    viewId = viewid;
    // TODO remove these once 2.4.VAMSAS release finished
    if (Cache.log != null && Cache.log.isDebugEnabled() && seqsetid != null)
    {
      Cache.log.debug("Setting viewport's sequence set id : "
              + sequenceSetID);
    }
    if (Cache.log != null && Cache.log.isDebugEnabled() && viewId != null)
    {
      Cache.log.debug("Setting viewport's view id : " + viewId);
    }
    setAlignment(al);
    init();
  }

  /**
   * Create a new AlignViewport with hidden regions
   * 
   * @param al
   *          AlignmentI
   * @param hiddenColumns
   *          ColumnSelection
   */
  public AlignViewport(AlignmentI al, ColumnSelection hiddenColumns)
  {
    setAlignment(al);
    if (hiddenColumns != null)
    {
      this.colSel = hiddenColumns;
      if (hiddenColumns.getHiddenColumns() != null
              && hiddenColumns.getHiddenColumns().size() > 0)
      {
        hasHiddenColumns = true;
      }
      else
      {
        hasHiddenColumns = false;
      }
    }
    init();
  }

  /**
   * New viewport with hidden columns and an existing sequence set id
   * 
   * @param al
   * @param hiddenColumns
   * @param seqsetid
   *          (may be null)
   */
  public AlignViewport(AlignmentI al, ColumnSelection hiddenColumns,
          String seqsetid)
  {
    this(al, hiddenColumns, seqsetid, null);
  }

  /**
   * New viewport with hidden columns and an existing sequence set id and viewid
   * 
   * @param al
   * @param hiddenColumns
   * @param seqsetid
   *          (may be null)
   * @param viewid
   *          (may be null)
   */
  public AlignViewport(AlignmentI al, ColumnSelection hiddenColumns,
          String seqsetid, String viewid)
  {
    sequenceSetID = seqsetid;
    viewId = viewid;
    // TODO remove these once 2.4.VAMSAS release finished
    if (Cache.log != null && Cache.log.isDebugEnabled() && seqsetid != null)
    {
      Cache.log.debug("Setting viewport's sequence set id : "
              + sequenceSetID);
    }
    if (Cache.log != null && Cache.log.isDebugEnabled() && viewId != null)
    {
      Cache.log.debug("Setting viewport's view id : " + viewId);
    }
    setAlignment(al);
    if (hiddenColumns != null)
    {
      this.colSel = hiddenColumns;
      if (hiddenColumns.getHiddenColumns() != null
              && hiddenColumns.getHiddenColumns().size() > 0)
      {
        hasHiddenColumns = true;
      }
      else
      {
        hasHiddenColumns = false;
      }
    }
    init();
  }

  void init()
  {
    this.startRes = 0;
    this.endRes = alignment.getWidth() - 1;
    this.startSeq = 0;
    this.endSeq = alignment.getHeight() - 1;

    antiAlias = Cache.getDefault("ANTI_ALIAS", false);

    showJVSuffix = Cache.getDefault("SHOW_JVSUFFIX", true);
    showAnnotation = Cache.getDefault("SHOW_ANNOTATIONS", true);

    rightAlignIds = Cache.getDefault("RIGHT_ALIGN_IDS", false);
    centreColumnLabels = Cache.getDefault("CENTRE_COLUMN_LABELS", false);
    autoCalculateConsensus = Cache.getDefault("AUTO_CALC_CONSENSUS", true);

    padGaps = Cache.getDefault("PAD_GAPS", true);
    shownpfeats = Cache.getDefault("SHOW_NPFEATS_TOOLTIP", true);
    showdbrefs = Cache.getDefault("SHOW_DBREFS_TOOLTIP", true);

    String fontName = Cache.getDefault("FONT_NAME", "SansSerif");
    String fontStyle = Cache.getDefault("FONT_STYLE", Font.PLAIN + "");
    String fontSize = Cache.getDefault("FONT_SIZE", "10");

    seqNameItalics = Cache.getDefault("ID_ITALICS", true);

    int style = 0;

    if (fontStyle.equals("bold"))
    {
      style = 1;
    }
    else if (fontStyle.equals("italic"))
    {
      style = 2;
    }

    setFont(new Font(fontName, style, Integer.parseInt(fontSize)));

    alignment
            .setGapCharacter(Cache.getDefault("GAP_SYMBOL", "-").charAt(0));

    // We must set conservation and consensus before setting colour,
    // as Blosum and Clustal require this to be done
    if (hconsensus == null && !isDataset)
    {
      if (!alignment.isNucleotide())
      {
        conservation = new AlignmentAnnotation("Conservation",
                "Conservation of total alignment less than " + ConsPercGaps
                        + "% gaps", new Annotation[1], 0f, 11f,
                AlignmentAnnotation.BAR_GRAPH);
        conservation.hasText = true;
        conservation.autoCalculated = true;

        if (Cache.getDefault("SHOW_CONSERVATION", true))
        {
          alignment.addAnnotation(conservation);
        }

        if (Cache.getDefault("SHOW_QUALITY", true))
        {
          quality = new AlignmentAnnotation("Quality",
                  "Alignment Quality based on Blosum62 scores",
                  new Annotation[1], 0f, 11f, AlignmentAnnotation.BAR_GRAPH);
          quality.hasText = true;
          quality.autoCalculated = true;

          alignment.addAnnotation(quality);
        }
        showGroupConservation = Cache.getDefault("SHOW_GROUP_CONSERVATION",
                false);

        {

        }
      }
      showConsensusHistogram = Cache.getDefault("SHOW_CONSENSUS_HISTOGRAM",
              true);
      showSequenceLogo = Cache.getDefault("SHOW_CONSENSUS_LOGO", false);
      showGroupConsensus = Cache.getDefault("SHOW_GROUP_CONSENSUS", false);
      // TODO: add menu option action that nulls or creates consensus object
      // depending on if the user wants to see the annotation or not in a
      // specific alignment
      consensus = new AlignmentAnnotation("Consensus", "PID",
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      consensus.hasText = true;
      consensus.autoCalculated = true;

      if (Cache.getDefault("SHOW_IDENTITY", true))
      {
        alignment.addAnnotation(consensus);
      }
    }

    if (jalview.bin.Cache.getProperty("DEFAULT_COLOUR") != null)
    {
      globalColourScheme = ColourSchemeProperty.getColour(alignment,
              jalview.bin.Cache.getProperty("DEFAULT_COLOUR"));

      if (globalColourScheme instanceof UserColourScheme)
      {
        globalColourScheme = UserDefinedColours.loadDefaultColours();
        ((UserColourScheme) globalColourScheme).setThreshold(0,
                getIgnoreGapsConsensus());
      }

      if (globalColourScheme != null)
      {
        globalColourScheme.setConsensus(hconsensus);
      }
    }

    wrapAlignment = jalview.bin.Cache.getDefault("WRAP_ALIGNMENT", false);
    showUnconserved = jalview.bin.Cache.getDefault("SHOW_UNCONSERVED",
            false);
    sortByTree = jalview.bin.Cache.getDefault("SORT_BY_TREE", false);
    followSelection = jalview.bin.Cache.getDefault("FOLLOW_SELECTIONS",
            true);
  }

  /**
   * set the flag
   * 
   * @param b
   *          features are displayed if true
   */
  public void setShowSequenceFeatures(boolean b)
  {
    showSequenceFeatures = b;
  }

  public boolean getShowSequenceFeatures()
  {
    return showSequenceFeatures;
  }

  ConservationThread conservationThread;

  ConsensusThread consensusThread;

  boolean consUpdateNeeded = false;

  static boolean UPDATING_CONSENSUS = false;

  static boolean UPDATING_CONSERVATION = false;

  boolean updatingConsensus = false;

  boolean updatingConservation = false;

  /**
   * centre columnar annotation labels in displayed alignment annotation TODO:
   * add to jalviewXML and annotation display settings
   */
  boolean centreColumnLabels = false;

  private boolean showdbrefs;

  private boolean shownpfeats;

  /**
   * trigger update of conservation annotation
   */
  public void updateConservation(final AlignmentPanel ap)
  {
    // see note in mantis : issue number 8585
    if (alignment.isNucleotide() || conservation == null
            || !autoCalculateConsensus)
    {
      return;
    }

    conservationThread = new ConservationThread(this, ap);
    conservationThread.start();
  }

  /**
   * trigger update of consensus annotation
   */
  public void updateConsensus(final AlignmentPanel ap)
  {
    // see note in mantis : issue number 8585
    if (consensus == null || !autoCalculateConsensus)
    {
      return;
    }
    consensusThread = new ConsensusThread(ap);
    consensusThread.start();
  }

  class ConsensusThread extends Thread
  {
    AlignmentPanel ap;

    public ConsensusThread(AlignmentPanel ap)
    {
      this.ap = ap;
    }

    public void run()
    {
      updatingConsensus = true;
      while (UPDATING_CONSENSUS)
      {
        try
        {
          if (ap != null)
          {
            ap.paintAlignment(false);
          }

          Thread.sleep(200);
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }

      UPDATING_CONSENSUS = true;

      try
      {
        int aWidth = (alignment != null) ? alignment.getWidth() : -1; // null
        // pointer
        // possibility
        // here.
        if (aWidth <= 0)
        {
          updatingConsensus = false;
          UPDATING_CONSENSUS = false;
          return;
        }

        consensus.annotations = null;
        consensus.annotations = new Annotation[aWidth];

        hconsensus = new Hashtable[aWidth];
        AAFrequency.calculate(alignment.getSequencesArray(), 0,
                alignment.getWidth(), hconsensus, true);
        updateAnnotation(true);

        if (globalColourScheme != null)
        {
          globalColourScheme.setConsensus(hconsensus);
        }

      } catch (OutOfMemoryError error)
      {
        alignment.deleteAnnotation(consensus);

        consensus = null;
        hconsensus = null;
        new OOMWarning("calculating consensus", error);
      }
      UPDATING_CONSENSUS = false;
      updatingConsensus = false;

      if (ap != null)
      {
        ap.paintAlignment(true);
      }
    }

    /**
     * update the consensus annotation from the sequence profile data using
     * current visualization settings.
     */
    public void updateAnnotation()
    {
      updateAnnotation(false);
    }

    protected void updateAnnotation(boolean immediate)
    {
      // TODO: make calls thread-safe, so if another thread calls this method,
      // it will either return or wait until one calculation is finished.
      if (immediate
              || (!updatingConsensus && consensus != null && hconsensus != null))
      {
        AAFrequency.completeConsensus(consensus, hconsensus, 0,
                hconsensus.length, ignoreGapsInConsensusCalculation,
                showSequenceLogo);
      }
    }
  }

  /**
   * get the consensus sequence as displayed under the PID consensus annotation
   * row.
   * 
   * @return consensus sequence as a new sequence object
   */
  public SequenceI getConsensusSeq()
  {
    if (consensus == null)
    {
      updateConsensus(null);
    }
    if (consensus == null)
    {
      return null;
    }
    StringBuffer seqs = new StringBuffer();
    for (int i = 0; i < consensus.annotations.length; i++)
    {
      if (consensus.annotations[i] != null)
      {
        if (consensus.annotations[i].description.charAt(0) == '[')
        {
          seqs.append(consensus.annotations[i].description.charAt(1));
        }
        else
        {
          seqs.append(consensus.annotations[i].displayCharacter);
        }
      }
    }

    SequenceI sq = new Sequence("Consensus", seqs.toString());
    sq.setDescription("Percentage Identity Consensus "
            + ((ignoreGapsInConsensusCalculation) ? " without gaps" : ""));
    return sq;
  }

  /**
   * 
   * 
   * @return null or the currently selected sequence region
   */
  public SequenceGroup getSelectionGroup()
  {
    return selectionGroup;
  }

  /**
   * Set the selection group for this window.
   * 
   * @param sg - group holding references to sequences in this alignment view
   *          
   */
  public void setSelectionGroup(SequenceGroup sg)
  {
    selectionGroup = sg;
  }

  /**
   * GUI state
   * @return true if conservation based shading is enabled
   */
  public boolean getConservationSelected()
  {
    return conservationColourSelected;
  }

  /**
   * GUI state
   * @param b
   *          enable conservation based shading
   */
  public void setConservationSelected(boolean b)
  {
    conservationColourSelected = b;
  }

  /**
   * GUI state
   * @return true if percent identity threshold is applied to shading
   */
  public boolean getAbovePIDThreshold()
  {
    return abovePIDThreshold;
  }

  /**
   * GUI state
   * 
   * 
   * @param b indicate if percent identity threshold is applied to shading
   */
  public void setAbovePIDThreshold(boolean b)
  {
    abovePIDThreshold = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getStartRes()
  {
    return startRes;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getEndRes()
  {
    return endRes;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getStartSeq()
  {
    return startSeq;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param cs
   *          DOCUMENT ME!
   */
  public void setGlobalColourScheme(ColourSchemeI cs)
  {
    globalColourScheme = cs;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public ColourSchemeI getGlobalColourScheme()
  {
    return globalColourScheme;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param res
   *          DOCUMENT ME!
   */
  public void setStartRes(int res)
  {
    this.startRes = res;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   */
  public void setStartSeq(int seq)
  {
    this.startSeq = seq;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param res
   *          DOCUMENT ME!
   */
  public void setEndRes(int res)
  {
    if (res > (alignment.getWidth() - 1))
    {
      // log.System.out.println(" Corrected res from " + res + " to maximum " +
      // (alignment.getWidth()-1));
      res = alignment.getWidth() - 1;
    }

    if (res < 0)
    {
      res = 0;
    }

    this.endRes = res;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   */
  public void setEndSeq(int seq)
  {
    if (seq > alignment.getHeight())
    {
      seq = alignment.getHeight();
    }

    if (seq < 0)
    {
      seq = 0;
    }

    this.endSeq = seq;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getEndSeq()
  {
    return endSeq;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param f
   *          DOCUMENT ME!
   */
  public void setFont(Font f)
  {
    font = f;

    Container c = new Container();

    java.awt.FontMetrics fm = c.getFontMetrics(font);
    setCharHeight(fm.getHeight());
    setCharWidth(fm.charWidth('M'));
    validCharWidth = true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Font getFont()
  {
    return font;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param w
   *          DOCUMENT ME!
   */
  public void setCharWidth(int w)
  {
    this.charWidth = w;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getCharWidth()
  {
    return charWidth;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param h
   *          DOCUMENT ME!
   */
  public void setCharHeight(int h)
  {
    this.charHeight = h;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getCharHeight()
  {
    return charHeight;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param w
   *          DOCUMENT ME!
   */
  public void setWrappedWidth(int w)
  {
    this.wrappedWidth = w;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getWrappedWidth()
  {
    return wrappedWidth;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public AlignmentI getAlignment()
  {
    return alignment;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param align
   *          DOCUMENT ME!
   */
  public void setAlignment(AlignmentI align)
  {
    if (alignment != null && alignment.getCodonFrames() != null)
    {
      StructureSelectionManager.getStructureSelectionManager(Desktop.instance)
              .removeMappings(alignment.getCodonFrames());
    }
    this.alignment = align;
    if (alignment.getCodonFrames() != null)
    {
      StructureSelectionManager.getStructureSelectionManager(Desktop.instance).addMappings(
              alignment.getCodonFrames());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setWrapAlignment(boolean state)
  {
    wrapAlignment = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setShowText(boolean state)
  {
    showText = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setRenderGaps(boolean state)
  {
    renderGaps = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getColourText()
  {
    return showColourText;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setColourText(boolean state)
  {
    showColourText = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setShowBoxes(boolean state)
  {
    showBoxes = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getWrapAlignment()
  {
    return wrapAlignment;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getShowText()
  {
    return showText;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getShowBoxes()
  {
    return showBoxes;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public char getGapCharacter()
  {
    return getAlignment().getGapCharacter();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param gap
   *          DOCUMENT ME!
   */
  public void setGapCharacter(char gap)
  {
    if (getAlignment() != null)
    {
      getAlignment().setGapCharacter(gap);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param thresh
   *          DOCUMENT ME!
   */
  public void setThreshold(int thresh)
  {
    threshold = thresh;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getThreshold()
  {
    return threshold;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param inc
   *          DOCUMENT ME!
   */
  public void setIncrement(int inc)
  {
    increment = inc;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getIncrement()
  {
    return increment;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public ColumnSelection getColumnSelection()
  {
    return colSel;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param tree
   *          DOCUMENT ME!
   */
  public void setCurrentTree(NJTree tree)
  {
    currentTree = tree;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public NJTree getCurrentTree()
  {
    return currentTree;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setColourAppliesToAllGroups(boolean b)
  {
    colourAppliesToAllGroups = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getColourAppliesToAllGroups()
  {
    return colourAppliesToAllGroups;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getShowJVSuffix()
  {
    return showJVSuffix;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setShowJVSuffix(boolean b)
  {
    showJVSuffix = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getShowAnnotation()
  {
    return showAnnotation;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setShowAnnotation(boolean b)
  {
    showAnnotation = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getScaleAboveWrapped()
  {
    return scaleAboveWrapped;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getScaleLeftWrapped()
  {
    return scaleLeftWrapped;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getScaleRightWrapped()
  {
    return scaleRightWrapped;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setScaleAboveWrapped(boolean b)
  {
    scaleAboveWrapped = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setScaleLeftWrapped(boolean b)
  {
    scaleLeftWrapped = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setScaleRightWrapped(boolean b)
  {
    scaleRightWrapped = b;
  }

  /**
   * Property change listener for changes in alignment
   * 
   * @param listener
   *          DOCUMENT ME!
   */
  public void addPropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param listener
   *          DOCUMENT ME!
   */
  public void removePropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Property change listener for changes in alignment
   * 
   * @param prop
   *          DOCUMENT ME!
   * @param oldvalue
   *          DOCUMENT ME!
   * @param newvalue
   *          DOCUMENT ME!
   */
  public void firePropertyChange(String prop, Object oldvalue,
          Object newvalue)
  {
    changeSupport.firePropertyChange(prop, oldvalue, newvalue);
  }

  public void setIgnoreGapsConsensus(boolean b, AlignmentPanel ap)
  {
    ignoreGapsInConsensusCalculation = b;
    updateConsensus(ap);
    if (globalColourScheme != null)
    {
      globalColourScheme.setThreshold(globalColourScheme.getThreshold(),
              ignoreGapsInConsensusCalculation);
    }
  }

  public boolean getIgnoreGapsConsensus()
  {
    return ignoreGapsInConsensusCalculation;
  }

  public void setDataset(boolean b)
  {
    isDataset = b;
  }

  public boolean isDataset()
  {
    return isDataset;
  }

  public void hideSelectedColumns()
  {
    if (colSel.size() < 1)
    {
      return;
    }

    colSel.hideSelectedColumns();
    setSelectionGroup(null);

    hasHiddenColumns = true;
  }

  public void hideColumns(int start, int end)
  {
    if (start == end)
    {
      colSel.hideColumns(start);
    }
    else
    {
      colSel.hideColumns(start, end);
    }

    hasHiddenColumns = true;
  }

  public void hideRepSequences(SequenceI repSequence, SequenceGroup sg)
  {
    int sSize = sg.getSize();
    if (sSize < 2)
    {
      return;
    }

    if (hiddenRepSequences == null)
    {
      hiddenRepSequences = new Hashtable();
    }

    hiddenRepSequences.put(repSequence, sg);

    // Hide all sequences except the repSequence
    SequenceI[] seqs = new SequenceI[sSize - 1];
    int index = 0;
    for (int i = 0; i < sSize; i++)
    {
      if (sg.getSequenceAt(i) != repSequence)
      {
        if (index == sSize - 1)
        {
          return;
        }

        seqs[index++] = sg.getSequenceAt(i);
      }
    }
    sg.setSeqrep(repSequence);
    sg.setHidereps(true);
    hideSequence(seqs);

  }

  public void hideAllSelectedSeqs()
  {
    if (selectionGroup == null || selectionGroup.getSize() < 1)
    {
      return;
    }

    SequenceI[] seqs = selectionGroup.getSequencesInOrder(alignment);

    hideSequence(seqs);

    setSelectionGroup(null);
  }

  public void hideSequence(SequenceI[] seq)
  {
    if (seq != null)
    {
      for (int i = 0; i < seq.length; i++)
      {
        alignment.getHiddenSequences().hideSequence(seq[i]);
      }
      hasHiddenRows = true;
      firePropertyChange("alignment", null, alignment.getSequences());
    }
  }

  public void showSequence(int index)
  {
    Vector tmp = alignment.getHiddenSequences().showSequence(index,
            hiddenRepSequences);
    if (tmp.size() > 0)
    {
      if (selectionGroup == null)
      {
        selectionGroup = new SequenceGroup();
        selectionGroup.setEndRes(alignment.getWidth() - 1);
      }

      for (int t = 0; t < tmp.size(); t++)
      {
        selectionGroup.addSequence((SequenceI) tmp.elementAt(t), false);
      }
      firePropertyChange("alignment", null, alignment.getSequences());
      sendSelection();
    }

    if (alignment.getHiddenSequences().getSize() < 1)
    {
      hasHiddenRows = false;
    }
  }

  public void showColumn(int col)
  {
    colSel.revealHiddenColumns(col);
    if (colSel.getHiddenColumns() == null)
    {
      hasHiddenColumns = false;
    }
  }

  public void showAllHiddenColumns()
  {
    colSel.revealAllHiddenColumns();
    hasHiddenColumns = false;
  }

  public void showAllHiddenSeqs()
  {
    if (alignment.getHiddenSequences().getSize() > 0)
    {
      if (selectionGroup == null)
      {
        selectionGroup = new SequenceGroup();
        selectionGroup.setEndRes(alignment.getWidth() - 1);
      }
      Vector tmp = alignment.getHiddenSequences().showAll(
              hiddenRepSequences);
      for (int t = 0; t < tmp.size(); t++)
      {
        selectionGroup.addSequence((SequenceI) tmp.elementAt(t), false);
      }
      firePropertyChange("alignment", null, alignment.getSequences());
      sendSelection();
      hasHiddenRows = false;
      hiddenRepSequences = null;
    }
  }

  public void invertColumnSelection()
  {
    colSel.invertColumnSelection(0, alignment.getWidth());
  }

  public int adjustForHiddenSeqs(int alignmentIndex)
  {
    return alignment.getHiddenSequences().adjustForHiddenSeqs(
            alignmentIndex);
  }

  /**
   * This method returns an array of new SequenceI objects derived from the
   * whole alignment or just the current selection with start and end points
   * adjusted
   * 
   * @note if you need references to the actual SequenceI objects in the
   *       alignment or currently selected then use getSequenceSelection()
   * @return selection as new sequenceI objects
   */
  public SequenceI[] getSelectionAsNewSequence()
  {
    SequenceI[] sequences;

    if (selectionGroup == null)
    {
      sequences = alignment.getSequencesArray();
      AlignmentAnnotation[] annots = alignment.getAlignmentAnnotation();
      for (int i = 0; i < sequences.length; i++)
      {
        sequences[i] = new Sequence(sequences[i], annots); // construct new
        // sequence with
        // subset of visible
        // annotation
      }
    }
    else
    {
      sequences = selectionGroup.getSelectionAsNewSequences(alignment);
    }

    return sequences;
  }

  /**
   * get the currently selected sequence objects or all the sequences in the
   * alignment.
   * 
   * @return array of references to sequence objects
   */
  public SequenceI[] getSequenceSelection()
  {
    SequenceI[] sequences = null;
    if (selectionGroup != null)
    {
      sequences = selectionGroup.getSequencesInOrder(alignment);
    }
    if (sequences == null)
    {
      sequences = alignment.getSequencesArray();
    }
    return sequences;
  }

  /**
   * This method returns the visible alignment as text, as seen on the GUI, ie
   * if columns are hidden they will not be returned in the result. Use this for
   * calculating trees, PCA, redundancy etc on views which contain hidden
   * columns.
   * 
   * @return String[]
   */
  public jalview.datamodel.CigarArray getViewAsCigars(
          boolean selectedRegionOnly)
  {
    return new jalview.datamodel.CigarArray(alignment, (hasHiddenColumns ? colSel : null), (selectedRegionOnly ? selectionGroup : null));
  }

  /**
   * return a compact representation of the current alignment selection to pass
   * to an analysis function
   * 
   * @param selectedOnly
   *          boolean true to just return the selected view
   * @return AlignmentView
   */
  public jalview.datamodel.AlignmentView getAlignmentView(boolean selectedOnly)
  {
    return getAlignmentView(selectedOnly, false);
  }
  
  /**
   * return a compact representation of the current alignment selection to pass
   * to an analysis function
   * 
   * @param selectedOnly
   *          boolean true to just return the selected view
   * @param markGroups
   *          boolean true to annotate the alignment view with groups on the alignment (and intersecting with selected region if selectedOnly is true) 
   * @return AlignmentView
   */
  public jalview.datamodel.AlignmentView getAlignmentView(boolean selectedOnly, boolean markGroups)
  {
    return new AlignmentView(alignment, colSel, selectionGroup, hasHiddenColumns, selectedOnly, markGroups);
  }

  /**
   * This method returns the visible alignment as text, as seen on the GUI, ie
   * if columns are hidden they will not be returned in the result. Use this for
   * calculating trees, PCA, redundancy etc on views which contain hidden
   * columns.
   * 
   * @return String[]
   */
  public String[] getViewAsString(boolean selectedRegionOnly)
  {
    String[] selection = null;
    SequenceI[] seqs = null;
    int i, iSize;
    int start = 0, end = 0;
    if (selectedRegionOnly && selectionGroup != null)
    {
      iSize = selectionGroup.getSize();
      seqs = selectionGroup.getSequencesInOrder(alignment);
      start = selectionGroup.getStartRes();
      end = selectionGroup.getEndRes() + 1;
    }
    else
    {
      iSize = alignment.getHeight();
      seqs = alignment.getSequencesArray();
      end = alignment.getWidth();
    }

    selection = new String[iSize];
    if (hasHiddenColumns)
    {
      selection = colSel.getVisibleSequenceStrings(start, end, seqs);
    }
    else
    {
      for (i = 0; i < iSize; i++)
      {
        selection[i] = seqs[i].getSequenceAsString(start, end);
      }

    }
    return selection;
  }

  public int[][] getVisibleRegionBoundaries(int min, int max)
  {
    Vector regions = new Vector();
    int start = min;
    int end = max;

    do
    {
      if (hasHiddenColumns)
      {
        if (start == 0)
        {
          start = colSel.adjustForHiddenColumns(start);
        }

        end = colSel.getHiddenBoundaryRight(start);
        if (start == end)
        {
          end = max;
        }
        if (end > max)
        {
          end = max;
        }
      }

      regions.addElement(new int[]
      { start, end });

      if (hasHiddenColumns)
      {
        start = colSel.adjustForHiddenColumns(end);
        start = colSel.getHiddenBoundaryLeft(start) + 1;
      }
    } while (end < max);

    int[][] startEnd = new int[regions.size()][2];

    regions.copyInto(startEnd);

    return startEnd;

  }

  public boolean getShowHiddenMarkers()
  {
    return showHiddenMarkers;
  }

  public void setShowHiddenMarkers(boolean show)
  {
    showHiddenMarkers = show;
  }

  public String getSequenceSetId()
  {
    if (sequenceSetID == null)
    {
      sequenceSetID = alignment.hashCode() + "";
    }

    return sequenceSetID;
  }

  /**
   * unique viewId for synchronizing state with stored Jalview Project
   * 
   */
  private String viewId = null;

  public String getViewId()
  {
    if (viewId == null)
    {
      viewId = this.getSequenceSetId() + "." + this.hashCode() + "";
    }
    return viewId;
  }

  public void alignmentChanged(AlignmentPanel ap)
  {
    if (padGaps)
    {
      alignment.padGaps();
    }
    if (hconsensus != null && autoCalculateConsensus)
    {
      updateConservation(ap);
    }
    if (autoCalculateConsensus)
    {
      updateConsensus(ap);
    }

    // Reset endRes of groups if beyond alignment width
    int alWidth = alignment.getWidth();
    Vector groups = alignment.getGroups();
    if (groups != null)
    {
      for (int i = 0; i < groups.size(); i++)
      {
        SequenceGroup sg = (SequenceGroup) groups.elementAt(i);
        if (sg.getEndRes() > alWidth)
        {
          sg.setEndRes(alWidth - 1);
        }
      }
    }

    if (selectionGroup != null && selectionGroup.getEndRes() > alWidth)
    {
      selectionGroup.setEndRes(alWidth - 1);
    }

    resetAllColourSchemes();

    // alignment.adjustSequenceAnnotations();
  }

  void resetAllColourSchemes()
  {
    ColourSchemeI cs = globalColourScheme;
    if (cs != null)
    {
      if (cs instanceof ClustalxColourScheme)
      {
        ((ClustalxColourScheme) cs).resetClustalX(alignment.getSequences(),
                alignment.getWidth());
      }

      cs.setConsensus(hconsensus);
      if (cs.conservationApplied())
      {
        Alignment al = (Alignment) alignment;
        Conservation c = new Conservation("All",
                ResidueProperties.propHash, 3, al.getSequences(), 0,
                al.getWidth() - 1);
        c.calculate();
        c.verdict(false, ConsPercGaps);

        cs.setConservation(c);
      }
    }

    int s, sSize = alignment.getGroups().size();
    for (s = 0; s < sSize; s++)
    {
      SequenceGroup sg = (SequenceGroup) alignment.getGroups().elementAt(s);
      if (sg.cs != null && sg.cs instanceof ClustalxColourScheme)
      {
        ((ClustalxColourScheme) sg.cs).resetClustalX(
                sg.getSequences(hiddenRepSequences), sg.getWidth());
      }
      sg.recalcConservation();
    }
  }

  public Color getSequenceColour(SequenceI seq)
  {
    if (sequenceColours == null || !sequenceColours.containsKey(seq))
    {
      return Color.white;
    }
    else
    {
      return (Color) sequenceColours.get(seq);
    }
  }

  public void setSequenceColour(SequenceI seq, Color col)
  {
    if (sequenceColours == null)
    {
      sequenceColours = new Hashtable();
    }

    if (col == null)
    {
      sequenceColours.remove(seq);
    }
    else
    {
      sequenceColours.put(seq, col);
    }
  }

  /**
   * returns the visible column regions of the alignment
   * 
   * @param selectedRegionOnly
   *          true to just return the contigs intersecting with the selected
   *          area
   * @return
   */
  public int[] getViewAsVisibleContigs(boolean selectedRegionOnly)
  {
    int[] viscontigs = null;
    int start = 0, end = 0;
    if (selectedRegionOnly && selectionGroup != null)
    {
      start = selectionGroup.getStartRes();
      end = selectionGroup.getEndRes() + 1;
    }
    else
    {
      end = alignment.getWidth();
    }
    viscontigs = colSel.getVisibleContigs(start, end);
    return viscontigs;
  }

  /**
   * get hash of undo and redo list for the alignment
   * 
   * @return long[] { historyList.hashCode, redoList.hashCode };
   */
  public long[] getUndoRedoHash()
  {
    if (historyList == null || redoList == null)
      return new long[]
      { -1, -1 };
    return new long[]
    { historyList.hashCode(), this.redoList.hashCode() };
  }

  /**
   * test if a particular set of hashcodes are different to the hashcodes for
   * the undo and redo list.
   * 
   * @param undoredo
   *          the stored set of hashcodes as returned by getUndoRedoHash
   * @return true if the hashcodes differ (ie the alignment has been edited) or
   *         the stored hashcode array differs in size
   */
  public boolean isUndoRedoHashModified(long[] undoredo)
  {
    if (undoredo == null)
    {
      return true;
    }
    long[] cstate = getUndoRedoHash();
    if (cstate.length != undoredo.length)
    {
      return true;
    }

    for (int i = 0; i < cstate.length; i++)
    {
      if (cstate[i] != undoredo[i])
      {
        return true;
      }
    }
    return false;
  }

  public boolean getCentreColumnLabels()
  {
    return centreColumnLabels;
  }

  public void setCentreColumnLabels(boolean centrecolumnlabels)
  {
    centreColumnLabels = centrecolumnlabels;
  }

  public void updateSequenceIdColours()
  {
    Vector groups = alignment.getGroups();
    if (sequenceColours == null)
    {
      sequenceColours = new Hashtable();
    }
    for (int ig = 0, igSize = groups.size(); ig < igSize; ig++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(ig);
      if (sg.idColour != null)
      {
        Vector sqs = sg.getSequences(hiddenRepSequences);
        for (int s = 0, sSize = sqs.size(); s < sSize; s++)
        {
          sequenceColours.put(sqs.elementAt(s), sg.idColour);
        }
      }
    }
  }

  /**
   * enable or disable the display of Database Cross References in the sequence
   * ID tooltip
   */
  public void setShowDbRefs(boolean show)
  {
    showdbrefs = show;
  }

  /**
   * 
   * @return true if Database References are to be displayed on tooltips.
   */
  public boolean isShowDbRefs()
  {
    return showdbrefs;
  }

  /**
   * 
   * @return true if Non-positional features are to be displayed on tooltips.
   */
  public boolean isShowNpFeats()
  {
    return shownpfeats;
  }

  /**
   * enable or disable the display of Non-Positional sequence features in the
   * sequence ID tooltip
   * 
   * @param show
   */
  public void setShowNpFeats(boolean show)
  {
    shownpfeats = show;
  }

  /**
   * 
   * @return true if view has hidden rows
   */
  public boolean hasHiddenRows()
  {
    return hasHiddenRows;
  }

  /**
   * 
   * @return true if view has hidden columns
   */
  public boolean hasHiddenColumns()
  {
    return hasHiddenColumns;
  }

  /**
   * when set, view will scroll to show the highlighted position
   */
  public boolean followHighlight = true;

  /**
   * @return true if view should scroll to show the highlighted region of a
   *         sequence
   * @return
   */
  public boolean getFollowHighlight()
  {
    return followHighlight;
  }

  public boolean followSelection = true;

  /**
   * @return true if view selection should always follow the selections
   *         broadcast by other selection sources
   */
  public boolean getFollowSelection()
  {
    return followSelection;
  }

  private long sgrouphash = -1, colselhash = -1;

  boolean showSeqFeaturesHeight;

  /**
   * checks current SelectionGroup against record of last hash value, and
   * updates record.
   * @param b update the record of last hash value
   * 
   * @return true if SelectionGroup changed since last call (when b is true)
   */
  boolean isSelectionGroupChanged(boolean b)
  {
    int hc = (selectionGroup == null || selectionGroup.getSize()==0) ? -1 : selectionGroup.hashCode();
    if (hc!=-1 && hc != sgrouphash)
    {
      if (b) {sgrouphash = hc;}
      return true;
    }
    return false;
  }

  /**
   * checks current colsel against record of last hash value, and optionally updates
   * record.

   * @param b update the record of last hash value
   * @return true if colsel changed since last call (when b is true)
   */
  boolean isColSelChanged(boolean b)
  {
    int hc = (colSel == null || colSel.size()==0) ? -1 : colSel.hashCode();
    if (hc!=-1 && hc != colselhash)
    {
      if (b) {colselhash = hc;}
      return true;
    }
    return false;
  }

  public void sendSelection()
  {
    jalview.structure.StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance).sendSelection(
                    new SequenceGroup(getSelectionGroup()),
                    new ColumnSelection(getColumnSelection()), this);
  }

  public void setShowSequenceFeaturesHeight(boolean selected)
  {
    showSeqFeaturesHeight = selected;
  }

  public boolean getShowSequenceFeaturesHeight()
  {
    return showSeqFeaturesHeight;
  }

  boolean showUnconserved = false;

  public boolean getShowUnconserved()
  {
    return showUnconserved;
  }

  public void setShowUnconserved(boolean showunconserved)
  {
    showUnconserved = showunconserved;
  }

  /**
   * return the alignPanel containing the given viewport. Use this to get the
   * components currently handling the given viewport.
   * 
   * @param av
   * @return null or an alignPanel guaranteed to have non-null alignFrame
   *         reference
   */
  public AlignmentPanel getAlignPanel()
  {
    AlignmentPanel[] aps = PaintRefresher.getAssociatedPanels(this
            .getSequenceSetId());
    AlignmentPanel ap = null;
    for (int p = 0; aps != null && p < aps.length; p++)
    {
      if (aps[p].av == this)
      {
        return aps[p];
      }
    }
    return null;
  }

  public boolean getSortByTree()
  {
    return sortByTree;
  }

  public void setSortByTree(boolean sort)
  {
    sortByTree = sort;
  }

  /**
   * should conservation rows be shown for groups
   */
  boolean showGroupConservation = false;

  /**
   * should consensus rows be shown for groups
   */
  boolean showGroupConsensus = false;

  /**
   * should consensus profile be rendered by default
   */
  public boolean showSequenceLogo = false;

  /**
   * should consensus histograms be rendered by default
   */
  public boolean showConsensusHistogram = true;

  /**
   * @return the showConsensusProfile
   */
  public boolean isShowSequenceLogo()
  {
    return showSequenceLogo;
  }

  /**
   * @param showSequenceLogo
   *          the new value
   */
  public void setShowSequenceLogo(boolean showSequenceLogo)
  {
    if (showSequenceLogo != this.showSequenceLogo)
    {
      // TODO: decouple settings setting from calculation when refactoring
      // annotation update method from alignframe to viewport
      this.showSequenceLogo = showSequenceLogo;
      if (consensusThread != null)
      {
        consensusThread.updateAnnotation();
      }
    }
    this.showSequenceLogo = showSequenceLogo;
  }

  /**
   * @param showConsensusHistogram
   *          the showConsensusHistogram to set
   */
  public void setShowConsensusHistogram(boolean showConsensusHistogram)
  {
    this.showConsensusHistogram = showConsensusHistogram;
  }

  /**
   * @return the showGroupConservation
   */
  public boolean isShowGroupConservation()
  {
    return showGroupConservation;
  }

  /**
   * @param showGroupConservation
   *          the showGroupConservation to set
   */
  public void setShowGroupConservation(boolean showGroupConservation)
  {
    this.showGroupConservation = showGroupConservation;
  }

  /**
   * @return the showGroupConsensus
   */
  public boolean isShowGroupConsensus()
  {
    return showGroupConsensus;
  }

  /**
   * @param showGroupConsensus
   *          the showGroupConsensus to set
   */
  public void setShowGroupConsensus(boolean showGroupConsensus)
  {
    this.showGroupConsensus = showGroupConsensus;
  }

  /**
   * 
   * @return flag to indicate if the consensus histogram should be rendered by
   *         default
   */
  public boolean isShowConsensusHistogram()
  {
    return this.showConsensusHistogram;
  }

  /**
   * synthesize a column selection if none exists so it covers the given
   * selection group. if wholewidth is false, no column selection is made if the
   * selection group covers the whole alignment width.
   * 
   * @param sg
   * @param wholewidth
   */
  public void expandColSelection(SequenceGroup sg, boolean wholewidth)
  {
    int sgs, sge;
    if (sg != null
            && (sgs = sg.getStartRes()) >= 0
            && sg.getStartRes() <= (sge = sg.getEndRes())
            && (colSel == null || colSel.getSelected() == null || colSel
                    .getSelected().size() == 0))
    {
      if (!wholewidth && alignment.getWidth() == (1 + sge - sgs))
      {
        // do nothing
        return;
      }
      if (colSel == null)
      {
        colSel = new ColumnSelection();
      }
      for (int cspos = sg.getStartRes(); cspos <= sg.getEndRes(); cspos++)
      {
        colSel.addElement(cspos);
      }
    }
  }

  public StructureSelectionManager getStructureSelectionManager()
  {
    return StructureSelectionManager.getStructureSelectionManager(Desktop.instance);
  }

  /**
   * 
   * @param pdbEntries
   * @return a series of SequenceI arrays, one for each PDBEntry, listing which sequence in the alignment holds a reference to it
   */
  public SequenceI[][] collateForPDB(PDBEntry[] pdbEntries)
  {
    ArrayList<SequenceI[]> seqvectors = new ArrayList<SequenceI[]>();
    for (PDBEntry pdb: pdbEntries) {
    ArrayList<SequenceI> seqs = new ArrayList<SequenceI>();
    for (int i = 0; i < alignment.getHeight(); i++)
    {
      Vector pdbs = alignment.getSequenceAt(i)
              .getDatasetSequence().getPDBId();
      if (pdbs == null)
        continue;
      SequenceI sq;
      for (int p = 0; p < pdbs.size(); p++)
      {
        PDBEntry p1 = (PDBEntry) pdbs.elementAt(p);
        if (p1.getId().equals(pdb.getId()))
        {
          if (!seqs.contains(sq=alignment.getSequenceAt(i)))
            seqs.add(sq);

          continue;
        }
      }
    }
    seqvectors.add(seqs.toArray(new SequenceI[seqs.size()]));
    }
    return seqvectors.toArray(new SequenceI[seqvectors.size()][]);
  }
}
