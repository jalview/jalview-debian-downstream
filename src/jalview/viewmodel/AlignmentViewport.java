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
package jalview.viewmodel;

import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.analysis.Conservation;
import jalview.api.AlignCalcManagerI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeaturesDisplayedI;
import jalview.api.ViewStyleI;
import jalview.commands.CommandI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.Annotation;
import jalview.datamodel.CigarArray;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenSequences;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.PIDColourScheme;
import jalview.structure.CommandListener;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.Comparison;
import jalview.util.MapList;
import jalview.util.MappingUtils;
import jalview.viewmodel.styles.ViewStyle;
import jalview.workers.AlignCalcManager;
import jalview.workers.ComplementConsensusThread;
import jalview.workers.ConsensusThread;
import jalview.workers.StrucConsensusThread;

import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * base class holding visualization and analysis attributes and common logic for
 * an active alignment view displayed in the GUI
 * 
 * @author jimp
 * 
 */
public abstract class AlignmentViewport implements AlignViewportI,
        CommandListener, VamsasSource
{
  protected ViewStyleI viewStyle = new ViewStyle();

  /**
   * A viewport that hosts the cDna view of this (protein), or vice versa (if
   * set).
   */
  AlignViewportI codingComplement = null;

  FeaturesDisplayedI featuresDisplayed = null;

  protected Deque<CommandI> historyList = new ArrayDeque<CommandI>();

  protected Deque<CommandI> redoList = new ArrayDeque<CommandI>();

  /**
   * @param name
   * @see jalview.api.ViewStyleI#setFontName(java.lang.String)
   */
  @Override
  public void setFontName(String name)
  {
    viewStyle.setFontName(name);
  }

  /**
   * @param style
   * @see jalview.api.ViewStyleI#setFontStyle(int)
   */
  @Override
  public void setFontStyle(int style)
  {
    viewStyle.setFontStyle(style);
  }

  /**
   * @param size
   * @see jalview.api.ViewStyleI#setFontSize(int)
   */
  @Override
  public void setFontSize(int size)
  {
    viewStyle.setFontSize(size);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getFontStyle()
   */
  @Override
  public int getFontStyle()
  {
    return viewStyle.getFontStyle();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getFontName()
   */
  @Override
  public String getFontName()
  {
    return viewStyle.getFontName();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getFontSize()
   */
  @Override
  public int getFontSize()
  {
    return viewStyle.getFontSize();
  }

  /**
   * @param upperCasebold
   * @see jalview.api.ViewStyleI#setUpperCasebold(boolean)
   */
  @Override
  public void setUpperCasebold(boolean upperCasebold)
  {
    viewStyle.setUpperCasebold(upperCasebold);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isUpperCasebold()
   */
  @Override
  public boolean isUpperCasebold()
  {
    return viewStyle.isUpperCasebold();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isSeqNameItalics()
   */
  @Override
  public boolean isSeqNameItalics()
  {
    return viewStyle.isSeqNameItalics();
  }

  /**
   * @param colourByReferenceSeq
   * @see jalview.api.ViewStyleI#setColourByReferenceSeq(boolean)
   */
  @Override
  public void setColourByReferenceSeq(boolean colourByReferenceSeq)
  {
    viewStyle.setColourByReferenceSeq(colourByReferenceSeq);
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setColourAppliesToAllGroups(boolean)
   */
  @Override
  public void setColourAppliesToAllGroups(boolean b)
  {
    viewStyle.setColourAppliesToAllGroups(b);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getColourAppliesToAllGroups()
   */
  @Override
  public boolean getColourAppliesToAllGroups()
  {
    return viewStyle.getColourAppliesToAllGroups();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getAbovePIDThreshold()
   */
  @Override
  public boolean getAbovePIDThreshold()
  {
    return viewStyle.getAbovePIDThreshold();
  }

  /**
   * @param inc
   * @see jalview.api.ViewStyleI#setIncrement(int)
   */
  @Override
  public void setIncrement(int inc)
  {
    viewStyle.setIncrement(inc);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getIncrement()
   */
  @Override
  public int getIncrement()
  {
    return viewStyle.getIncrement();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setConservationSelected(boolean)
   */
  @Override
  public void setConservationSelected(boolean b)
  {
    viewStyle.setConservationSelected(b);
  }

  /**
   * @param show
   * @see jalview.api.ViewStyleI#setShowHiddenMarkers(boolean)
   */
  @Override
  public void setShowHiddenMarkers(boolean show)
  {
    viewStyle.setShowHiddenMarkers(show);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowHiddenMarkers()
   */
  @Override
  public boolean getShowHiddenMarkers()
  {
    return viewStyle.getShowHiddenMarkers();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setScaleRightWrapped(boolean)
   */
  @Override
  public void setScaleRightWrapped(boolean b)
  {
    viewStyle.setScaleRightWrapped(b);
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setScaleLeftWrapped(boolean)
   */
  @Override
  public void setScaleLeftWrapped(boolean b)
  {
    viewStyle.setScaleLeftWrapped(b);
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setScaleAboveWrapped(boolean)
   */
  @Override
  public void setScaleAboveWrapped(boolean b)
  {
    viewStyle.setScaleAboveWrapped(b);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getScaleLeftWrapped()
   */
  @Override
  public boolean getScaleLeftWrapped()
  {
    return viewStyle.getScaleLeftWrapped();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getScaleAboveWrapped()
   */
  @Override
  public boolean getScaleAboveWrapped()
  {
    return viewStyle.getScaleAboveWrapped();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getScaleRightWrapped()
   */
  @Override
  public boolean getScaleRightWrapped()
  {
    return viewStyle.getScaleRightWrapped();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setAbovePIDThreshold(boolean)
   */
  @Override
  public void setAbovePIDThreshold(boolean b)
  {
    viewStyle.setAbovePIDThreshold(b);
  }

  /**
   * @param thresh
   * @see jalview.api.ViewStyleI#setThreshold(int)
   */
  @Override
  public void setThreshold(int thresh)
  {
    viewStyle.setThreshold(thresh);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getThreshold()
   */
  @Override
  public int getThreshold()
  {
    return viewStyle.getThreshold();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowJVSuffix()
   */
  @Override
  public boolean getShowJVSuffix()
  {
    return viewStyle.getShowJVSuffix();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setShowJVSuffix(boolean)
   */
  @Override
  public void setShowJVSuffix(boolean b)
  {
    viewStyle.setShowJVSuffix(b);
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setWrapAlignment(boolean)
   */
  @Override
  public void setWrapAlignment(boolean state)
  {
    viewStyle.setWrapAlignment(state);
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setShowText(boolean)
   */
  @Override
  public void setShowText(boolean state)
  {
    viewStyle.setShowText(state);
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setRenderGaps(boolean)
   */
  @Override
  public void setRenderGaps(boolean state)
  {
    viewStyle.setRenderGaps(state);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getColourText()
   */
  @Override
  public boolean getColourText()
  {
    return viewStyle.getColourText();
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setColourText(boolean)
   */
  @Override
  public void setColourText(boolean state)
  {
    viewStyle.setColourText(state);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getWrapAlignment()
   */
  @Override
  public boolean getWrapAlignment()
  {
    return viewStyle.getWrapAlignment();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowText()
   */
  @Override
  public boolean getShowText()
  {
    return viewStyle.getShowText();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getWrappedWidth()
   */
  @Override
  public int getWrappedWidth()
  {
    return viewStyle.getWrappedWidth();
  }

  /**
   * @param w
   * @see jalview.api.ViewStyleI#setWrappedWidth(int)
   */
  @Override
  public void setWrappedWidth(int w)
  {
    viewStyle.setWrappedWidth(w);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getCharHeight()
   */
  @Override
  public int getCharHeight()
  {
    return viewStyle.getCharHeight();
  }

  /**
   * @param h
   * @see jalview.api.ViewStyleI#setCharHeight(int)
   */
  @Override
  public void setCharHeight(int h)
  {
    viewStyle.setCharHeight(h);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getCharWidth()
   */
  @Override
  public int getCharWidth()
  {
    return viewStyle.getCharWidth();
  }

  /**
   * @param w
   * @see jalview.api.ViewStyleI#setCharWidth(int)
   */
  @Override
  public void setCharWidth(int w)
  {
    viewStyle.setCharWidth(w);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowBoxes()
   */
  @Override
  public boolean getShowBoxes()
  {
    return viewStyle.getShowBoxes();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowUnconserved()
   */
  @Override
  public boolean getShowUnconserved()
  {
    return viewStyle.getShowUnconserved();
  }

  /**
   * @param showunconserved
   * @see jalview.api.ViewStyleI#setShowUnconserved(boolean)
   */
  @Override
  public void setShowUnconserved(boolean showunconserved)
  {
    viewStyle.setShowUnconserved(showunconserved);
  }

  /**
   * @param default1
   * @see jalview.api.ViewStyleI#setSeqNameItalics(boolean)
   */
  @Override
  public void setSeqNameItalics(boolean default1)
  {
    viewStyle.setSeqNameItalics(default1);
  }

  /**
   * alignment displayed in the viewport. Please use get/setter
   */
  protected AlignmentI alignment;

  @Override
  public AlignmentI getAlignment()
  {
    return alignment;
  }

  @Override
  public char getGapCharacter()
  {
    return alignment.getGapCharacter();
  }

  protected String sequenceSetID;

  /**
   * probably unused indicator that view is of a dataset rather than an
   * alignment
   */
  protected boolean isDataset = false;

  public void setDataset(boolean b)
  {
    isDataset = b;
  }

  public boolean isDataset()
  {
    return isDataset;
  }

  private Map<SequenceI, SequenceCollectionI> hiddenRepSequences;

  protected ColumnSelection colSel = new ColumnSelection();

  public boolean autoCalculateConsensus = true;

  protected boolean autoCalculateStrucConsensus = true;

  protected boolean ignoreGapsInConsensusCalculation = false;

  protected ColourSchemeI globalColourScheme = null;

  @Override
  public void setGlobalColourScheme(ColourSchemeI cs)
  {
    // TODO: logic refactored from AlignFrame changeColour -
    // TODO: autorecalc stuff should be changed to rely on the worker system
    // check to see if we should implement a changeColour(cs) method rather than
    // put th logic in here
    // - means that caller decides if they want to just modify state and defer
    // calculation till later or to do all calculations in thread.
    // via changecolour
    globalColourScheme = cs;
    boolean recalc = false;
    if (cs != null)
    {
      recalc = getConservationSelected();
      if (getAbovePIDThreshold() || cs instanceof PIDColourScheme
              || cs instanceof Blosum62ColourScheme)
      {
        recalc = true;
        cs.setThreshold(viewStyle.getThreshold(),
                ignoreGapsInConsensusCalculation);
      }
      else
      {
        cs.setThreshold(0, ignoreGapsInConsensusCalculation);
      }
      if (recalc)
      {
        cs.setConsensus(hconsensus);
        cs.setConservation(hconservation);
      }
      cs.setConservationApplied(getConservationSelected());
      cs.alignmentChanged(alignment, hiddenRepSequences);
    }
    if (getColourAppliesToAllGroups())
    {
      for (SequenceGroup sg : getAlignment().getGroups())
      {
        if (cs == null)
        {
          sg.cs = null;
          continue;
        }
        sg.cs = cs.applyTo(sg, getHiddenRepSequences());
        sg.setConsPercGaps(ConsPercGaps);
        if (getAbovePIDThreshold() || cs instanceof PIDColourScheme
                || cs instanceof Blosum62ColourScheme)
        {
          sg.cs.setThreshold(viewStyle.getThreshold(),
                  isIgnoreGapsConsensus());
          recalc = true;
        }
        else
        {
          sg.cs.setThreshold(0, isIgnoreGapsConsensus());
        }

        if (getConservationSelected())
        {
          sg.cs.setConservationApplied(true);
          recalc = true;
        }
        else
        {
          sg.cs.setConservation(null);
          // sg.cs.setThreshold(0, getIgnoreGapsConsensus());
        }
        if (recalc)
        {
          sg.recalcConservation();
        }
        else
        {
          sg.cs.alignmentChanged(sg, hiddenRepSequences);
        }
      }
    }
  }

  @Override
  public ColourSchemeI getGlobalColourScheme()
  {
    return globalColourScheme;
  }

  protected AlignmentAnnotation consensus;

  protected AlignmentAnnotation complementConsensus;

  protected AlignmentAnnotation strucConsensus;

  protected AlignmentAnnotation conservation;

  protected AlignmentAnnotation quality;

  protected AlignmentAnnotation[] groupConsensus;

  protected AlignmentAnnotation[] groupConservation;

  /**
   * results of alignment consensus analysis for visible portion of view
   */
  protected ProfilesI hconsensus = null;

  /**
   * results of cDNA complement consensus visible portion of view
   */
  protected Hashtable[] hcomplementConsensus = null;

  /**
   * results of secondary structure base pair consensus for visible portion of
   * view
   */
  protected Hashtable[] hStrucConsensus = null;

  protected Conservation hconservation = null;

  @Override
  public void setConservation(Conservation cons)
  {
    hconservation = cons;
  }

  /**
   * percentage gaps allowed in a column before all amino acid properties should
   * be considered unconserved
   */
  int ConsPercGaps = 25; // JBPNote : This should be a scalable property!

  @Override
  public int getConsPercGaps()
  {
    return ConsPercGaps;
  }

  @Override
  public void setSequenceConsensusHash(ProfilesI hconsensus)
  {
    this.hconsensus = hconsensus;
  }

  @Override
  public void setComplementConsensusHash(Hashtable[] hconsensus)
  {
    this.hcomplementConsensus = hconsensus;
  }

  @Override
  public ProfilesI getSequenceConsensusHash()
  {
    return hconsensus;
  }

  @Override
  public Hashtable[] getComplementConsensusHash()
  {
    return hcomplementConsensus;
  }

  @Override
  public Hashtable[] getRnaStructureConsensusHash()
  {
    return hStrucConsensus;
  }

  @Override
  public void setRnaStructureConsensusHash(Hashtable[] hStrucConsensus)
  {
    this.hStrucConsensus = hStrucConsensus;

  }

  @Override
  public AlignmentAnnotation getAlignmentQualityAnnot()
  {
    return quality;
  }

  @Override
  public AlignmentAnnotation getAlignmentConservationAnnotation()
  {
    return conservation;
  }

  @Override
  public AlignmentAnnotation getAlignmentConsensusAnnotation()
  {
    return consensus;
  }

  @Override
  public AlignmentAnnotation getComplementConsensusAnnotation()
  {
    return complementConsensus;
  }

  @Override
  public AlignmentAnnotation getAlignmentStrucConsensusAnnotation()
  {
    return strucConsensus;
  }

  protected AlignCalcManagerI calculator = new AlignCalcManager();

  /**
   * trigger update of conservation annotation
   */
  public void updateConservation(final AlignmentViewPanel ap)
  {
    // see note in mantis : issue number 8585
    if (alignment.isNucleotide()
            || (conservation == null && quality == null)
            || !autoCalculateConsensus)
    {
      return;
    }
    if (calculator
            .getRegisteredWorkersOfClass(jalview.workers.ConservationThread.class) == null)
    {
      calculator.registerWorker(new jalview.workers.ConservationThread(
              this, ap));
    }
  }

  /**
   * trigger update of consensus annotation
   */
  public void updateConsensus(final AlignmentViewPanel ap)
  {
    // see note in mantis : issue number 8585
    if (consensus == null || !autoCalculateConsensus)
    {
      return;
    }
    if (calculator.getRegisteredWorkersOfClass(ConsensusThread.class) == null)
    {
      calculator.registerWorker(new ConsensusThread(this, ap));
    }

    /*
     * A separate thread to compute cDNA consensus for a protein alignment
     * which has mapping to cDNA
     */
    final AlignmentI al = this.getAlignment();
    if (!al.isNucleotide() && al.getCodonFrames() != null
            && !al.getCodonFrames().isEmpty())
    {
      /*
       * fudge - check first for protein-to-nucleotide mappings
       * (we don't want to do this for protein-to-protein)
       */
      boolean doConsensus = false;
      for (AlignedCodonFrame mapping : al.getCodonFrames())
      {
        // TODO hold mapping type e.g. dna-to-protein in AlignedCodonFrame?
        MapList[] mapLists = mapping.getdnaToProt();
        // mapLists can be empty if project load has not finished resolving seqs
        if (mapLists.length > 0 && mapLists[0].getFromRatio() == 3)
        {
          doConsensus = true;
          break;
        }
      }
      if (doConsensus)
      {
        if (calculator
                .getRegisteredWorkersOfClass(ComplementConsensusThread.class) == null)
        {
          calculator
                  .registerWorker(new ComplementConsensusThread(this, ap));
        }
      }
    }
  }

  // --------START Structure Conservation
  public void updateStrucConsensus(final AlignmentViewPanel ap)
  {
    if (autoCalculateStrucConsensus && strucConsensus == null
            && alignment.isNucleotide() && alignment.hasRNAStructure())
    {
      // secondary structure has been added - so init the consensus line
      initRNAStructure();
    }

    // see note in mantis : issue number 8585
    if (strucConsensus == null || !autoCalculateStrucConsensus)
    {
      return;
    }
    if (calculator.getRegisteredWorkersOfClass(StrucConsensusThread.class) == null)
    {
      calculator.registerWorker(new StrucConsensusThread(this, ap));
    }
  }

  public boolean isCalcInProgress()
  {
    return calculator.isWorking();
  }

  @Override
  public boolean isCalculationInProgress(
          AlignmentAnnotation alignmentAnnotation)
  {
    if (!alignmentAnnotation.autoCalculated)
    {
      return false;
    }
    if (calculator.workingInvolvedWith(alignmentAnnotation))
    {
      // System.err.println("grey out ("+alignmentAnnotation.label+")");
      return true;
    }
    return false;
  }

  public void setAlignment(AlignmentI align)
  {
    this.alignment = align;
  }

  /**
   * Clean up references when this viewport is closed
   */
  @Override
  public void dispose()
  {
    /*
     * defensively null out references to large objects in case
     * this object is not garbage collected (as if!)
     */
    consensus = null;
    complementConsensus = null;
    strucConsensus = null;
    conservation = null;
    quality = null;
    groupConsensus = null;
    groupConservation = null;
    hconsensus = null;
    hcomplementConsensus = null;
    // colour scheme may hold reference to consensus
    globalColourScheme = null;
    // TODO remove listeners from changeSupport?
    changeSupport = null;
    setAlignment(null);
  }

  @Override
  public boolean isClosed()
  {
    // TODO: check that this isClosed is only true after panel is closed, not
    // before it is fully constructed.
    return alignment == null;
  }

  @Override
  public AlignCalcManagerI getCalcManager()
  {
    return calculator;
  }

  /**
   * should conservation rows be shown for groups
   */
  protected boolean showGroupConservation = false;

  /**
   * should consensus rows be shown for groups
   */
  protected boolean showGroupConsensus = false;

  /**
   * should consensus profile be rendered by default
   */
  protected boolean showSequenceLogo = false;

  /**
   * should consensus profile be rendered normalised to row height
   */
  protected boolean normaliseSequenceLogo = false;

  /**
   * should consensus histograms be rendered by default
   */
  protected boolean showConsensusHistogram = true;

  /**
   * @return the showConsensusProfile
   */
  @Override
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
      calculator.updateAnnotationFor(ConsensusThread.class);
      calculator.updateAnnotationFor(ComplementConsensusThread.class);
      calculator.updateAnnotationFor(StrucConsensusThread.class);
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
  @Override
  public boolean isShowConsensusHistogram()
  {
    return this.showConsensusHistogram;
  }

  /**
   * when set, updateAlignment will always ensure sequences are of equal length
   */
  private boolean padGaps = false;

  /**
   * when set, alignment should be reordered according to a newly opened tree
   */
  public boolean sortByTree = false;

  /**
   * 
   * 
   * @return null or the currently selected sequence region
   */
  @Override
  public SequenceGroup getSelectionGroup()
  {
    return selectionGroup;
  }

  /**
   * Set the selection group for this window.
   * 
   * @param sg
   *          - group holding references to sequences in this alignment view
   * 
   */
  @Override
  public void setSelectionGroup(SequenceGroup sg)
  {
    selectionGroup = sg;
  }

  public void setHiddenColumns(ColumnSelection colsel)
  {
    this.colSel = colsel;
  }

  @Override
  public ColumnSelection getColumnSelection()
  {
    return colSel;
  }

  @Override
  public void setColumnSelection(ColumnSelection colSel)
  {
    this.colSel = colSel;
    if (colSel != null)
    {
      updateHiddenColumns();
    }
    isColSelChanged(true);
  }

  /**
   * 
   * @return
   */
  @Override
  public Map<SequenceI, SequenceCollectionI> getHiddenRepSequences()
  {
    return hiddenRepSequences;
  }

  @Override
  public void setHiddenRepSequences(
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    this.hiddenRepSequences = hiddenRepSequences;
  }

  @Override
  public boolean hasSelectedColumns()
  {
    ColumnSelection columnSelection = getColumnSelection();
    return columnSelection != null && columnSelection.hasSelectedColumns();
  }

  @Override
  public boolean hasHiddenColumns()
  {
    return colSel != null && colSel.hasHiddenColumns();
  }

  public void updateHiddenColumns()
  {
    // this method doesn't really do anything now. But - it could, since a
    // column Selection could be in the process of modification
    // hasHiddenColumns = colSel.hasHiddenColumns();
  }

  @Override
  public boolean hasHiddenRows()
  {
    return alignment.getHiddenSequences().getSize() > 0;
  }

  protected SequenceGroup selectionGroup;

  public void setSequenceSetId(String newid)
  {
    if (sequenceSetID != null)
    {
      System.err
              .println("Warning - overwriting a sequenceSetId for a viewport!");
    }
    sequenceSetID = new String(newid);
  }

  @Override
  public String getSequenceSetId()
  {
    if (sequenceSetID == null)
    {
      sequenceSetID = alignment.hashCode() + "";
    }

    return sequenceSetID;
  }

  /**
   * unique viewId for synchronizing state (e.g. with stored Jalview Project)
   * 
   */
  protected String viewId = null;

  @Override
  public String getViewId()
  {
    if (viewId == null)
    {
      viewId = this.getSequenceSetId() + "." + this.hashCode() + "";
    }
    return viewId;
  }

  public void setIgnoreGapsConsensus(boolean b, AlignmentViewPanel ap)
  {
    ignoreGapsInConsensusCalculation = b;
    if (ap != null)
    {
      updateConsensus(ap);
      if (globalColourScheme != null)
      {
        globalColourScheme.setThreshold(globalColourScheme.getThreshold(),
                ignoreGapsInConsensusCalculation);
      }
    }

  }

  private long sgrouphash = -1, colselhash = -1;

  /**
   * checks current SelectionGroup against record of last hash value, and
   * updates record.
   * 
   * @param b
   *          update the record of last hash value
   * 
   * @return true if SelectionGroup changed since last call (when b is true)
   */
  public boolean isSelectionGroupChanged(boolean b)
  {
    int hc = (selectionGroup == null || selectionGroup.getSize() == 0) ? -1
            : selectionGroup.hashCode();
    if (hc != -1 && hc != sgrouphash)
    {
      if (b)
      {
        sgrouphash = hc;
      }
      return true;
    }
    return false;
  }

  /**
   * checks current colsel against record of last hash value, and optionally
   * updates record.
   * 
   * @param b
   *          update the record of last hash value
   * @return true if colsel changed since last call (when b is true)
   */
  public boolean isColSelChanged(boolean b)
  {
    int hc = (colSel == null || colSel.isEmpty()) ? -1 : colSel.hashCode();
    if (hc != -1 && hc != colselhash)
    {
      if (b)
      {
        colselhash = hc;
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean isIgnoreGapsConsensus()
  {
    return ignoreGapsInConsensusCalculation;
  }

  // property change stuff
  // JBPNote Prolly only need this in the applet version.
  private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  protected boolean showConservation = true;

  protected boolean showQuality = true;

  protected boolean showConsensus = true;

  private Map<SequenceI, Color> sequenceColours = new HashMap<SequenceI, Color>();

  protected SequenceAnnotationOrder sortAnnotationsBy = null;

  protected boolean showAutocalculatedAbove;

  /**
   * when set, view will scroll to show the highlighted position
   */
  private boolean followHighlight = true;

  // TODO private with getters and setters?
  public int startRes;

  public int endRes;

  public int startSeq;

  public int endSeq;

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

  // common hide/show column stuff

  public void hideSelectedColumns()
  {
    if (colSel.isEmpty())
    {
      return;
    }

    colSel.hideSelectedColumns();
    setSelectionGroup(null);
    isColSelChanged(true);
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
    isColSelChanged(true);
  }

  public void showColumn(int col)
  {
    colSel.revealHiddenColumns(col);
    isColSelChanged(true);
  }

  public void showAllHiddenColumns()
  {
    colSel.revealAllHiddenColumns();
    isColSelChanged(true);
  }

  // common hide/show seq stuff
  public void showAllHiddenSeqs()
  {
    if (alignment.getHiddenSequences().getSize() > 0)
    {
      if (selectionGroup == null)
      {
        selectionGroup = new SequenceGroup();
        selectionGroup.setEndRes(alignment.getWidth() - 1);
      }
      List<SequenceI> tmp = alignment.getHiddenSequences().showAll(
              hiddenRepSequences);
      for (SequenceI seq : tmp)
      {
        selectionGroup.addSequence(seq, false);
        setSequenceAnnotationsVisible(seq, true);
      }

      hiddenRepSequences = null;

      firePropertyChange("alignment", null, alignment.getSequences());
      // used to set hasHiddenRows/hiddenRepSequences here, after the property
      // changed event
      sendSelection();
    }
  }

  public void showSequence(int index)
  {
    List<SequenceI> tmp = alignment.getHiddenSequences().showSequence(
            index, hiddenRepSequences);
    if (tmp.size() > 0)
    {
      if (selectionGroup == null)
      {
        selectionGroup = new SequenceGroup();
        selectionGroup.setEndRes(alignment.getWidth() - 1);
      }

      for (SequenceI seq : tmp)
      {
        selectionGroup.addSequence(seq, false);
        setSequenceAnnotationsVisible(seq, true);
      }
      firePropertyChange("alignment", null, alignment.getSequences());
      sendSelection();
    }
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
        setSequenceAnnotationsVisible(seq[i], false);
      }
      firePropertyChange("alignment", null, alignment.getSequences());
    }
  }

  /**
   * Hides the specified sequence, or the sequences it represents
   * 
   * @param sequence
   *          the sequence to hide, or keep as representative
   * @param representGroup
   *          if true, hide the current selection group except for the
   *          representative sequence
   */
  public void hideSequences(SequenceI sequence, boolean representGroup)
  {
    if (selectionGroup == null || selectionGroup.getSize() < 1)
    {
      hideSequence(new SequenceI[] { sequence });
      return;
    }

    if (representGroup)
    {
      hideRepSequences(sequence, selectionGroup);
      setSelectionGroup(null);
      return;
    }

    int gsize = selectionGroup.getSize();
    SequenceI[] hseqs = selectionGroup.getSequences().toArray(
            new SequenceI[gsize]);

    hideSequence(hseqs);
    setSelectionGroup(null);
    sendSelection();
  }

  /**
   * Set visibility for any annotations for the given sequence.
   * 
   * @param sequenceI
   */
  protected void setSequenceAnnotationsVisible(SequenceI sequenceI,
          boolean visible)
  {
    AlignmentAnnotation[] anns = alignment.getAlignmentAnnotation();
    if (anns != null)
    {
      for (AlignmentAnnotation ann : anns)
      {
        if (ann.sequenceRef == sequenceI)
        {
          ann.visible = visible;
        }
      }
    }
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
      hiddenRepSequences = new Hashtable<SequenceI, SequenceCollectionI>();
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
    sg.setSeqrep(repSequence); // note: not done in 2.7applet
    sg.setHidereps(true); // note: not done in 2.7applet
    hideSequence(seqs);

  }

  /**
   * 
   * @return null or the current reference sequence
   */
  public SequenceI getReferenceSeq()
  {
    return alignment.getSeqrep();
  }

  /**
   * @param seq
   * @return true iff seq is the reference for the alignment
   */
  public boolean isReferenceSeq(SequenceI seq)
  {
    return alignment.getSeqrep() == seq;
  }

  /**
   * 
   * @param seq
   * @return true if there are sequences represented by this sequence that are
   *         currently hidden
   */
  public boolean isHiddenRepSequence(SequenceI seq)
  {
    return (hiddenRepSequences != null && hiddenRepSequences
            .containsKey(seq));
  }

  /**
   * 
   * @param seq
   * @return null or a sequence group containing the sequences that seq
   *         represents
   */
  public SequenceGroup getRepresentedSequences(SequenceI seq)
  {
    return (SequenceGroup) (hiddenRepSequences == null ? null
            : hiddenRepSequences.get(seq));
  }

  @Override
  public int adjustForHiddenSeqs(int alignmentIndex)
  {
    return alignment.getHiddenSequences().adjustForHiddenSeqs(
            alignmentIndex);
  }

  @Override
  public void invertColumnSelection()
  {
    colSel.invertColumnSelection(0, alignment.getWidth());
  }

  @Override
  public SequenceI[] getSelectionAsNewSequence()
  {
    SequenceI[] sequences;
    // JBPNote: Need to test jalviewLite.getSelectedSequencesAsAlignmentFrom -
    // this was the only caller in the applet for this method
    // JBPNote: in applet, this method returned references to the alignment
    // sequences, and it did not honour the presence/absence of annotation
    // attached to the alignment (probably!)
    if (selectionGroup == null || selectionGroup.getSize() == 0)
    {
      sequences = alignment.getSequencesArray();
      AlignmentAnnotation[] annots = alignment.getAlignmentAnnotation();
      for (int i = 0; i < sequences.length; i++)
      {
        // construct new sequence with subset of visible annotation
        sequences[i] = new Sequence(sequences[i], annots);
      }
    }
    else
    {
      sequences = selectionGroup.getSelectionAsNewSequences(alignment);
    }

    return sequences;
  }

  @Override
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

  @Override
  public CigarArray getViewAsCigars(boolean selectedRegionOnly)
  {
    return new CigarArray(alignment, colSel,
            (selectedRegionOnly ? selectionGroup : null));
  }

  @Override
  public jalview.datamodel.AlignmentView getAlignmentView(
          boolean selectedOnly)
  {
    return getAlignmentView(selectedOnly, false);
  }

  @Override
  public jalview.datamodel.AlignmentView getAlignmentView(
          boolean selectedOnly, boolean markGroups)
  {
    return new AlignmentView(alignment, colSel, selectionGroup,
            colSel != null && colSel.hasHiddenColumns(), selectedOnly,
            markGroups);
  }

  @Override
  public String[] getViewAsString(boolean selectedRegionOnly)
  {
    return getViewAsString(selectedRegionOnly, true);
  }

  @Override
  public String[] getViewAsString(boolean selectedRegionOnly,
          boolean exportHiddenSeqs)
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
      if (hasHiddenRows() && exportHiddenSeqs)
      {
        AlignmentI fullAlignment = alignment.getHiddenSequences()
                .getFullAlignment();
        iSize = fullAlignment.getHeight();
        seqs = fullAlignment.getSequencesArray();
        end = fullAlignment.getWidth();
      }
      else
      {
        iSize = alignment.getHeight();
        seqs = alignment.getSequencesArray();
        end = alignment.getWidth();
      }
    }

    selection = new String[iSize];
    if (colSel != null && colSel.hasHiddenColumns())
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

  @Override
  public List<int[]> getVisibleRegionBoundaries(int min, int max)
  {
    ArrayList<int[]> regions = new ArrayList<int[]>();
    int start = min;
    int end = max;

    do
    {
      if (colSel != null && colSel.hasHiddenColumns())
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

      regions.add(new int[] { start, end });

      if (colSel != null && colSel.hasHiddenColumns())
      {
        start = colSel.adjustForHiddenColumns(end);
        start = colSel.getHiddenBoundaryLeft(start) + 1;
      }
    } while (end < max);

    int[][] startEnd = new int[regions.size()][2];

    return regions;
  }

  @Override
  public List<AlignmentAnnotation> getVisibleAlignmentAnnotation(
          boolean selectedOnly)
  {
    ArrayList<AlignmentAnnotation> ala = new ArrayList<AlignmentAnnotation>();
    AlignmentAnnotation[] aa;
    if ((aa = alignment.getAlignmentAnnotation()) != null)
    {
      for (AlignmentAnnotation annot : aa)
      {
        AlignmentAnnotation clone = new AlignmentAnnotation(annot);
        if (selectedOnly && selectionGroup != null)
        {
          colSel.makeVisibleAnnotation(selectionGroup.getStartRes(),
                  selectionGroup.getEndRes(), clone);
        }
        else
        {
          colSel.makeVisibleAnnotation(clone);
        }
        ala.add(clone);
      }
    }
    return ala;
  }

  @Override
  public boolean isPadGaps()
  {
    return padGaps;
  }

  @Override
  public void setPadGaps(boolean padGaps)
  {
    this.padGaps = padGaps;
  }

  /**
   * apply any post-edit constraints and trigger any calculations needed after
   * an edit has been performed on the alignment
   * 
   * @param ap
   */
  @Override
  public void alignmentChanged(AlignmentViewPanel ap)
  {
    if (isPadGaps())
    {
      alignment.padGaps();
    }
    if (autoCalculateConsensus)
    {
      updateConsensus(ap);
    }
    if (hconsensus != null && autoCalculateConsensus)
    {
      updateConservation(ap);
    }
    if (autoCalculateStrucConsensus)
    {
      updateStrucConsensus(ap);
    }

    // Reset endRes of groups if beyond alignment width
    int alWidth = alignment.getWidth();
    List<SequenceGroup> groups = alignment.getGroups();
    if (groups != null)
    {
      for (SequenceGroup sg : groups)
      {
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
    calculator.restartWorkers();
    // alignment.adjustSequenceAnnotations();
  }

  /**
   * reset scope and do calculations for all applied colourschemes on alignment
   */
  void resetAllColourSchemes()
  {
    ColourSchemeI cs = globalColourScheme;
    if (cs != null)
    {
      cs.alignmentChanged(alignment, hiddenRepSequences);

      cs.setConsensus(hconsensus);
      if (cs.conservationApplied())
      {
        cs.setConservation(Conservation.calculateConservation("All",
                alignment.getSequences(), 0, alignment.getWidth(), false,
                getConsPercGaps(), false));
      }
    }

    for (SequenceGroup sg : alignment.getGroups())
    {
      if (sg.cs != null)
      {
        sg.cs.alignmentChanged(sg, hiddenRepSequences);
      }
      sg.recalcConservation();
    }
  }

  protected void initAutoAnnotation()
  {
    // TODO: add menu option action that nulls or creates consensus object
    // depending on if the user wants to see the annotation or not in a
    // specific alignment

    if (hconsensus == null && !isDataset)
    {
      if (!alignment.isNucleotide())
      {
        initConservation();
        initQuality();
      }
      else
      {
        initRNAStructure();
      }
      consensus = new AlignmentAnnotation("Consensus", "PID",
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      initConsensus(consensus);

      initComplementConsensus();
    }
  }

  /**
   * If this is a protein alignment and there are mappings to cDNA, add the cDNA
   * consensus annotation.
   */
  public void initComplementConsensus()
  {
    if (!alignment.isNucleotide())
    {
      final List<AlignedCodonFrame> codonMappings = alignment
              .getCodonFrames();
      if (codonMappings != null && !codonMappings.isEmpty())
      {
        boolean doConsensus = false;
        for (AlignedCodonFrame mapping : codonMappings)
        {
          // TODO hold mapping type e.g. dna-to-protein in AlignedCodonFrame?
          MapList[] mapLists = mapping.getdnaToProt();
          // mapLists can be empty if project load has not finished resolving
          // seqs
          if (mapLists.length > 0 && mapLists[0].getFromRatio() == 3)
          {
            doConsensus = true;
            break;
          }
        }
        if (doConsensus)
        {
          complementConsensus = new AlignmentAnnotation("cDNA Consensus",
                  "PID for cDNA", new Annotation[1], 0f, 100f,
                  AlignmentAnnotation.BAR_GRAPH);
          initConsensus(complementConsensus);
        }
      }
    }
  }

  private void initConsensus(AlignmentAnnotation aa)
  {
    aa.hasText = true;
    aa.autoCalculated = true;

    if (showConsensus)
    {
      alignment.addAnnotation(aa);
    }
  }

  private void initConservation()
  {
    if (showConservation)
    {
      if (conservation == null)
      {
        conservation = new AlignmentAnnotation("Conservation",
                "Conservation of total alignment less than "
                        + getConsPercGaps() + "% gaps", new Annotation[1],
                0f, 11f, AlignmentAnnotation.BAR_GRAPH);
        conservation.hasText = true;
        conservation.autoCalculated = true;
        alignment.addAnnotation(conservation);
      }
    }
  }

  private void initQuality()
  {
    if (showQuality)
    {
      if (quality == null)
      {
        quality = new AlignmentAnnotation("Quality",
                "Alignment Quality based on Blosum62 scores",
                new Annotation[1], 0f, 11f, AlignmentAnnotation.BAR_GRAPH);
        quality.hasText = true;
        quality.autoCalculated = true;
        alignment.addAnnotation(quality);
      }
    }
  }

  private void initRNAStructure()
  {
    if (alignment.hasRNAStructure() && strucConsensus == null)
    {
      strucConsensus = new AlignmentAnnotation("StrucConsensus", "PID",
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      strucConsensus.hasText = true;
      strucConsensus.autoCalculated = true;

      if (showConsensus)
      {
        alignment.addAnnotation(strucConsensus);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.api.AlignViewportI#calcPanelHeight()
   */
  @Override
  public int calcPanelHeight()
  {
    // setHeight of panels
    AlignmentAnnotation[] anns = getAlignment().getAlignmentAnnotation();
    int height = 0;
    int charHeight = getCharHeight();
    if (anns != null)
    {
      BitSet graphgrp = new BitSet();
      for (AlignmentAnnotation aa : anns)
      {
        if (aa == null)
        {
          System.err.println("Null annotation row: ignoring.");
          continue;
        }
        if (!aa.visible)
        {
          continue;
        }
        if (aa.graphGroup > -1)
        {
          if (graphgrp.get(aa.graphGroup))
          {
            continue;
          }
          else
          {
            graphgrp.set(aa.graphGroup);
          }
        }
        aa.height = 0;

        if (aa.hasText)
        {
          aa.height += charHeight;
        }

        if (aa.hasIcons)
        {
          aa.height += 16;
        }

        if (aa.graph > 0)
        {
          aa.height += aa.graphHeight;
        }

        if (aa.height == 0)
        {
          aa.height = 20;
        }

        height += aa.height;
      }
    }
    if (height == 0)
    {
      // set minimum
      height = 20;
    }
    return height;
  }

  @Override
  public void updateGroupAnnotationSettings(boolean applyGlobalSettings,
          boolean preserveNewGroupSettings)
  {
    boolean updateCalcs = false;
    boolean conv = isShowGroupConservation();
    boolean cons = isShowGroupConsensus();
    boolean showprf = isShowSequenceLogo();
    boolean showConsHist = isShowConsensusHistogram();
    boolean normLogo = isNormaliseSequenceLogo();

    /**
     * TODO reorder the annotation rows according to group/sequence ordering on
     * alignment
     */
    boolean sortg = true;

    // remove old automatic annotation
    // add any new annotation

    // intersect alignment annotation with alignment groups

    AlignmentAnnotation[] aan = alignment.getAlignmentAnnotation();
    List<SequenceGroup> oldrfs = new ArrayList<SequenceGroup>();
    if (aan != null)
    {
      for (int an = 0; an < aan.length; an++)
      {
        if (aan[an].autoCalculated && aan[an].groupRef != null)
        {
          oldrfs.add(aan[an].groupRef);
          alignment.deleteAnnotation(aan[an], false);
        }
      }
    }
    if (alignment.getGroups() != null)
    {
      for (SequenceGroup sg : alignment.getGroups())
      {
        updateCalcs = false;
        if (applyGlobalSettings
                || (!preserveNewGroupSettings && !oldrfs.contains(sg)))
        {
          // set defaults for this group's conservation/consensus
          sg.setshowSequenceLogo(showprf);
          sg.setShowConsensusHistogram(showConsHist);
          sg.setNormaliseSequenceLogo(normLogo);
        }
        if (conv)
        {
          updateCalcs = true;
          alignment.addAnnotation(sg.getConservationRow(), 0);
        }
        if (cons)
        {
          updateCalcs = true;
          alignment.addAnnotation(sg.getConsensus(), 0);
        }
        // refresh the annotation rows
        if (updateCalcs)
        {
          sg.recalcConservation();
        }
      }
    }
    oldrfs.clear();
  }

  @Override
  public boolean isDisplayReferenceSeq()
  {
    return alignment.hasSeqrep() && viewStyle.isDisplayReferenceSeq();
  }

  @Override
  public void setDisplayReferenceSeq(boolean displayReferenceSeq)
  {
    viewStyle.setDisplayReferenceSeq(displayReferenceSeq);
  }

  @Override
  public boolean isColourByReferenceSeq()
  {
    return alignment.hasSeqrep() && viewStyle.isColourByReferenceSeq();
  }

  @Override
  public Color getSequenceColour(SequenceI seq)
  {
    Color sqc = sequenceColours.get(seq);
    return (sqc == null ? Color.white : sqc);
  }

  @Override
  public void setSequenceColour(SequenceI seq, Color col)
  {
    if (col == null)
    {
      sequenceColours.remove(seq);
    }
    else
    {
      sequenceColours.put(seq, col);
    }
  }

  @Override
  public void updateSequenceIdColours()
  {
    for (SequenceGroup sg : alignment.getGroups())
    {
      if (sg.idColour != null)
      {
        for (SequenceI s : sg.getSequences(getHiddenRepSequences()))
        {
          sequenceColours.put(s, sg.idColour);
        }
      }
    }
  }

  @Override
  public void clearSequenceColours()
  {
    sequenceColours.clear();
  };

  @Override
  public AlignViewportI getCodingComplement()
  {
    return this.codingComplement;
  }

  /**
   * Set this as the (cDna/protein) complement of the given viewport. Also
   * ensures the reverse relationship is set on the given viewport.
   */
  @Override
  public void setCodingComplement(AlignViewportI av)
  {
    if (this == av)
    {
      System.err.println("Ignoring recursive setCodingComplement request");
    }
    else
    {
      this.codingComplement = av;
      // avoid infinite recursion!
      if (av.getCodingComplement() != this)
      {
        av.setCodingComplement(this);
      }
    }
  }

  @Override
  public boolean isNucleotide()
  {
    return getAlignment() == null ? false : getAlignment().isNucleotide();
  }

  @Override
  public FeaturesDisplayedI getFeaturesDisplayed()
  {
    return featuresDisplayed;
  }

  @Override
  public void setFeaturesDisplayed(FeaturesDisplayedI featuresDisplayedI)
  {
    featuresDisplayed = featuresDisplayedI;
  }

  @Override
  public boolean areFeaturesDisplayed()
  {
    return featuresDisplayed != null
            && featuresDisplayed.getRegisteredFeaturesCount() > 0;
  }

  /**
   * set the flag
   * 
   * @param b
   *          features are displayed if true
   */
  @Override
  public void setShowSequenceFeatures(boolean b)
  {
    viewStyle.setShowSequenceFeatures(b);
  }

  @Override
  public boolean isShowSequenceFeatures()
  {
    return viewStyle.isShowSequenceFeatures();
  }

  @Override
  public void setShowSequenceFeaturesHeight(boolean selected)
  {
    viewStyle.setShowSequenceFeaturesHeight(selected);
  }

  @Override
  public boolean isShowSequenceFeaturesHeight()
  {
    return viewStyle.isShowSequenceFeaturesHeight();
  }

  @Override
  public void setShowAnnotation(boolean b)
  {
    viewStyle.setShowAnnotation(b);
  }

  @Override
  public boolean isShowAnnotation()
  {
    return viewStyle.isShowAnnotation();
  }

  @Override
  public boolean isRightAlignIds()
  {
    return viewStyle.isRightAlignIds();
  }

  @Override
  public void setRightAlignIds(boolean rightAlignIds)
  {
    viewStyle.setRightAlignIds(rightAlignIds);
  }

  @Override
  public boolean getConservationSelected()
  {
    return viewStyle.getConservationSelected();
  }

  @Override
  public void setShowBoxes(boolean state)
  {
    viewStyle.setShowBoxes(state);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getTextColour()
   */
  @Override
  public Color getTextColour()
  {
    return viewStyle.getTextColour();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getTextColour2()
   */
  @Override
  public Color getTextColour2()
  {
    return viewStyle.getTextColour2();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getThresholdTextColour()
   */
  @Override
  public int getThresholdTextColour()
  {
    return viewStyle.getThresholdTextColour();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isConservationColourSelected()
   */
  @Override
  public boolean isConservationColourSelected()
  {
    return viewStyle.isConservationColourSelected();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isRenderGaps()
   */
  @Override
  public boolean isRenderGaps()
  {
    return viewStyle.isRenderGaps();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isShowColourText()
   */
  @Override
  public boolean isShowColourText()
  {
    return viewStyle.isShowColourText();
  }

  /**
   * @param conservationColourSelected
   * @see jalview.api.ViewStyleI#setConservationColourSelected(boolean)
   */
  @Override
  public void setConservationColourSelected(
          boolean conservationColourSelected)
  {
    viewStyle.setConservationColourSelected(conservationColourSelected);
  }

  /**
   * @param showColourText
   * @see jalview.api.ViewStyleI#setShowColourText(boolean)
   */
  @Override
  public void setShowColourText(boolean showColourText)
  {
    viewStyle.setShowColourText(showColourText);
  }

  /**
   * @param textColour
   * @see jalview.api.ViewStyleI#setTextColour(java.awt.Color)
   */
  @Override
  public void setTextColour(Color textColour)
  {
    viewStyle.setTextColour(textColour);
  }

  /**
   * @param thresholdTextColour
   * @see jalview.api.ViewStyleI#setThresholdTextColour(int)
   */
  @Override
  public void setThresholdTextColour(int thresholdTextColour)
  {
    viewStyle.setThresholdTextColour(thresholdTextColour);
  }

  /**
   * @param textColour2
   * @see jalview.api.ViewStyleI#setTextColour2(java.awt.Color)
   */
  @Override
  public void setTextColour2(Color textColour2)
  {
    viewStyle.setTextColour2(textColour2);
  }

  @Override
  public ViewStyleI getViewStyle()
  {
    return new ViewStyle(viewStyle);
  }

  @Override
  public void setViewStyle(ViewStyleI settingsForView)
  {
    viewStyle = new ViewStyle(settingsForView);
  }

  @Override
  public boolean sameStyle(ViewStyleI them)
  {
    return viewStyle.sameStyle(them);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getIdWidth()
   */
  @Override
  public int getIdWidth()
  {
    return viewStyle.getIdWidth();
  }

  /**
   * @param i
   * @see jalview.api.ViewStyleI#setIdWidth(int)
   */
  @Override
  public void setIdWidth(int i)
  {
    viewStyle.setIdWidth(i);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isCentreColumnLabels()
   */
  @Override
  public boolean isCentreColumnLabels()
  {
    return viewStyle.isCentreColumnLabels();
  }

  /**
   * @param centreColumnLabels
   * @see jalview.api.ViewStyleI#setCentreColumnLabels(boolean)
   */
  @Override
  public void setCentreColumnLabels(boolean centreColumnLabels)
  {
    viewStyle.setCentreColumnLabels(centreColumnLabels);
  }

  /**
   * @param showdbrefs
   * @see jalview.api.ViewStyleI#setShowDBRefs(boolean)
   */
  @Override
  public void setShowDBRefs(boolean showdbrefs)
  {
    viewStyle.setShowDBRefs(showdbrefs);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isShowDBRefs()
   */
  @Override
  public boolean isShowDBRefs()
  {
    return viewStyle.isShowDBRefs();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isShowNPFeats()
   */
  @Override
  public boolean isShowNPFeats()
  {
    return viewStyle.isShowNPFeats();
  }

  /**
   * @param shownpfeats
   * @see jalview.api.ViewStyleI#setShowNPFeats(boolean)
   */
  @Override
  public void setShowNPFeats(boolean shownpfeats)
  {
    viewStyle.setShowNPFeats(shownpfeats);
  }

  public abstract StructureSelectionManager getStructureSelectionManager();

  /**
   * Add one command to the command history list.
   * 
   * @param command
   */
  public void addToHistoryList(CommandI command)
  {
    if (this.historyList != null)
    {
      this.historyList.push(command);
      broadcastCommand(command, false);
    }
  }

  protected void broadcastCommand(CommandI command, boolean undo)
  {
    getStructureSelectionManager().commandPerformed(command, undo,
            getVamsasSource());
  }

  /**
   * Add one command to the command redo list.
   * 
   * @param command
   */
  public void addToRedoList(CommandI command)
  {
    if (this.redoList != null)
    {
      this.redoList.push(command);
    }
    broadcastCommand(command, true);
  }

  /**
   * Clear the command redo list.
   */
  public void clearRedoList()
  {
    if (this.redoList != null)
    {
      this.redoList.clear();
    }
  }

  public void setHistoryList(Deque<CommandI> list)
  {
    this.historyList = list;
  }

  public Deque<CommandI> getHistoryList()
  {
    return this.historyList;
  }

  public void setRedoList(Deque<CommandI> list)
  {
    this.redoList = list;
  }

  public Deque<CommandI> getRedoList()
  {
    return this.redoList;
  }

  @Override
  public VamsasSource getVamsasSource()
  {
    return this;
  }

  public SequenceAnnotationOrder getSortAnnotationsBy()
  {
    return sortAnnotationsBy;
  }

  public void setSortAnnotationsBy(SequenceAnnotationOrder sortAnnotationsBy)
  {
    this.sortAnnotationsBy = sortAnnotationsBy;
  }

  public boolean isShowAutocalculatedAbove()
  {
    return showAutocalculatedAbove;
  }

  public void setShowAutocalculatedAbove(boolean showAutocalculatedAbove)
  {
    this.showAutocalculatedAbove = showAutocalculatedAbove;
  }

  @Override
  public boolean isScaleProteinAsCdna()
  {
    return viewStyle.isScaleProteinAsCdna();
  }

  @Override
  public void setScaleProteinAsCdna(boolean b)
  {
    viewStyle.setScaleProteinAsCdna(b);
  }

  /**
   * @return true if view should scroll to show the highlighted region of a
   *         sequence
   * @return
   */
  @Override
  public final boolean isFollowHighlight()
  {
    return followHighlight;
  }

  @Override
  public final void setFollowHighlight(boolean b)
  {
    this.followHighlight = b;
  }

  public int getStartRes()
  {
    return startRes;
  }

  @Override
  public int getEndRes()
  {
    return endRes;
  }

  public int getStartSeq()
  {
    return startSeq;
  }

  public void setStartRes(int res)
  {
    this.startRes = res;
  }

  public void setStartSeq(int seq)
  {
    this.startSeq = seq;
  }

  public void setEndRes(int res)
  {
    if (res > alignment.getWidth() - 1)
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

  public int getEndSeq()
  {
    return endSeq;
  }

  /**
   * Helper method to populate the SearchResults with the location in the
   * complementary alignment to scroll to, in order to match this one.
   * 
   * @param sr
   *          the SearchResults to add to
   * @return the offset (below top of visible region) of the matched sequence
   */
  protected int findComplementScrollTarget(SearchResultsI sr)
  {
    final AlignViewportI complement = getCodingComplement();
    if (complement == null || !complement.isFollowHighlight())
    {
      return 0;
    }
    boolean iAmProtein = !getAlignment().isNucleotide();
    AlignmentI proteinAlignment = iAmProtein ? getAlignment() : complement
            .getAlignment();
    if (proteinAlignment == null)
    {
      return 0;
    }
    final List<AlignedCodonFrame> mappings = proteinAlignment
            .getCodonFrames();

    /*
     * Heuristic: find the first mapped sequence (if any) with a non-gapped
     * residue in the middle column of the visible region. Scroll the
     * complementary alignment to line up the corresponding residue.
     */
    int seqOffset = 0;
    SequenceI sequence = null;

    /*
     * locate 'middle' column (true middle if an odd number visible, left of
     * middle if an even number visible)
     */
    int middleColumn = getStartRes() + (getEndRes() - getStartRes()) / 2;
    final HiddenSequences hiddenSequences = getAlignment()
            .getHiddenSequences();

    /*
     * searching to the bottom of the alignment gives smoother scrolling across
     * all gapped visible regions
     */
    int lastSeq = alignment.getHeight() - 1;
    List<AlignedCodonFrame> seqMappings = null;
    for (int seqNo = getStartSeq(); seqNo < lastSeq; seqNo++, seqOffset++)
    {
      sequence = getAlignment().getSequenceAt(seqNo);
      if (hiddenSequences != null && hiddenSequences.isHidden(sequence))
      {
        continue;
      }
      if (Comparison.isGap(sequence.getCharAt(middleColumn)))
      {
        continue;
      }
      seqMappings = MappingUtils
              .findMappingsForSequenceAndOthers(sequence, mappings,
                      getCodingComplement().getAlignment().getSequences());
      if (!seqMappings.isEmpty())
      {
        break;
      }
    }

    if (sequence == null || seqMappings == null || seqMappings.isEmpty())
    {
      /*
       * No ungapped mapped sequence in middle column - do nothing
       */
      return 0;
    }
    MappingUtils.addSearchResults(sr, sequence,
            sequence.findPosition(middleColumn), seqMappings);
    return seqOffset;
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
    if (sg != null && (sgs = sg.getStartRes()) >= 0
            && sg.getStartRes() <= (sge = sg.getEndRes())
            && !this.hasSelectedColumns())
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

  /**
   * hold status of current selection group - defined on alignment or not.
   */
  private boolean selectionIsDefinedGroup = false;


  @Override
  public boolean isSelectionDefinedGroup()
  {
    if (selectionGroup == null)
    {
      return false;
    }
    if (isSelectionGroupChanged(true))
    {
      selectionIsDefinedGroup = false;
      List<SequenceGroup> gps = alignment.getGroups();
      if (gps == null || gps.size() == 0)
      {
        selectionIsDefinedGroup = false;
      }
      else
      {
        selectionIsDefinedGroup = gps.contains(selectionGroup);
      }
    }
    return selectionGroup.getContext() == alignment
            || selectionIsDefinedGroup;
  }

  /**
   * null, or currently highlighted results on this view
   */
  private SearchResultsI searchResults = null;

  @Override
  public boolean hasSearchResults()
  {
    return searchResults != null;
  }

  @Override
  public void setSearchResults(SearchResultsI results)
  {
    searchResults = results;
  }

  @Override
  public SearchResultsI getSearchResults()
  {
    return searchResults;
  }
}
