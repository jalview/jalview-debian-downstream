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
package jalview.appletgui;

import java.util.*;

import java.awt.*;

import jalview.analysis.*;
import jalview.bin.*;
import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;

public class AlignViewport implements SelectionSource, VamsasSource
{
  int startRes;

  int endRes;

  int startSeq;

  int endSeq;

  boolean cursorMode = false;

  boolean showJVSuffix = true;

  boolean showText = true;

  boolean showColourText = false;

  boolean showBoxes = true;

  boolean wrapAlignment = false;

  boolean renderGaps = true;

  boolean showSequenceFeatures = false;

  boolean showAnnotation = true;

  boolean showConservation = true;

  boolean showQuality = true;

  boolean showConsensus = true;

  boolean upperCasebold = false;

  boolean colourAppliesToAllGroups = true;

  ColourSchemeI globalColourScheme = null;

  boolean conservationColourSelected = false;

  boolean abovePIDThreshold = false;

  SequenceGroup selectionGroup;

  int charHeight;

  int charWidth;

  int wrappedWidth;

  Font font = new Font("SansSerif", Font.PLAIN, 10);

  boolean validCharWidth = true;

  AlignmentI alignment;

  ColumnSelection colSel = new ColumnSelection();

  int threshold;

  int increment;

  NJTree currentTree = null;

  boolean scaleAboveWrapped = true;

  boolean scaleLeftWrapped = true;

  boolean scaleRightWrapped = true;

  // The following vector holds the features which are
  // currently visible, in the correct order or rendering
  public Hashtable featuresDisplayed;

  boolean hasHiddenColumns = false;

  boolean hasHiddenRows = false;

  boolean showHiddenMarkers = true;

  public Hashtable[] hconsensus;

  AlignmentAnnotation consensus;

  AlignmentAnnotation conservation;

  AlignmentAnnotation quality;

  AlignmentAnnotation[] groupConsensus;

  AlignmentAnnotation[] groupConservation;

  boolean autocalculateConsensus = true;

  public int ConsPercGaps = 25; // JBPNote : This should be a scalable property!

  private java.beans.PropertyChangeSupport changeSupport = new java.beans.PropertyChangeSupport(
          this);

  boolean ignoreGapsInConsensusCalculation = false;

  public jalview.bin.JalviewLite applet;

  Hashtable sequenceColours;

  boolean MAC = false;

  Stack historyList = new Stack();

  Stack redoList = new Stack();

  String sequenceSetID;

  Hashtable hiddenRepSequences;
  
  public void finalize() {
    applet=null;
    quality=null;
    alignment=null;
    colSel=null;
  }

  public AlignViewport(AlignmentI al, JalviewLite applet)
  {
    this.applet = applet;
    setAlignment(al);
    this.startRes = 0;
    this.endRes = al.getWidth() - 1;
    this.startSeq = 0;
    this.endSeq = al.getHeight() - 1;
    if (applet != null)
    {
      // get the width and height scaling factors if they were specified
      String param = applet.getParameter("widthScale");
      if (param != null)
      {
        try
        {
          widthScale = new Float(param).floatValue();
        } catch (Exception e)
        {
        }
        if (widthScale <= 1.0)
        {
          System.err
                  .println("Invalid alignment character width scaling factor ("
                          + widthScale + "). Ignoring.");
          widthScale = 1;
        }
        if (applet.debug)
        {
          System.err
                  .println("Alignment character width scaling factor is now "
                          + widthScale);
        }
      }
      param = applet.getParameter("heightScale");
      if (param != null)
      {
        try
        {
          heightScale = new Float(param).floatValue();
        } catch (Exception e)
        {
        }
        if (heightScale <= 1.0)
        {
          System.err
                  .println("Invalid alignment character height scaling factor ("
                          + heightScale + "). Ignoring.");
          heightScale = 1;
        }
        if (applet.debug)
        {
          System.err
                  .println("Alignment character height scaling factor is now "
                          + heightScale);
        }
      }
    }
    setFont(font);

    MAC = new jalview.util.Platform().isAMac();

    if (applet != null)
    {
      showJVSuffix = applet.getDefaultParameter("showFullId", showJVSuffix);

      showAnnotation = applet.getDefaultParameter("showAnnotation", showAnnotation);
      
      showConservation = applet.getDefaultParameter("showConservation", showConservation);
      
      showQuality = applet.getDefaultParameter("showQuality", showQuality);

      showConsensus = applet.getDefaultParameter("showConsensus", showConsensus);

      showUnconserved = applet.getDefaultParameter("showUnconserved", showUnconserved);

      String param = applet.getParameter("upperCase");
      if (param != null)
      {
        if (param.equalsIgnoreCase("bold"))
        {
          upperCasebold = true;
        }
      }
      sortByTree = applet.getDefaultParameter("sortByTree", sortByTree);

      followHighlight = applet.getDefaultParameter("automaticScrolling",followHighlight);
      followSelection = followHighlight;

      showSequenceLogo = applet.getDefaultParameter("showSequenceLogo", showSequenceLogo);
      
      showGroupConsensus = applet.getDefaultParameter("showGroupConsensus", showGroupConsensus);
      
      showGroupConservation = applet.getDefaultParameter("showGroupConservation", showGroupConservation);
        
      showConsensusHistogram = applet.getDefaultParameter("showConsensusHistogram", showConsensusHistogram);
      
    }

    if (applet != null)
    {
      String colour = applet.getParameter("defaultColour");

      if (colour == null)
      {
        colour = applet.getParameter("userDefinedColour");
        if (colour != null)
        {
          colour = "User Defined";
        }
      }

      if (colour != null)
      {
        globalColourScheme = ColourSchemeProperty.getColour(alignment,
                colour);
        if (globalColourScheme != null)
        {
          globalColourScheme.setConsensus(hconsensus);
        }
      }

      if (applet.getParameter("userDefinedColour") != null)
      {
        ((UserColourScheme) globalColourScheme).parseAppletParameter(applet
                .getParameter("userDefinedColour"));
      }
    }
    if (hconsensus == null)
    {
      if (!alignment.isNucleotide())
      {
        conservation = new AlignmentAnnotation("Conservation",
                "Conservation of total alignment less than " + ConsPercGaps
                        + "% gaps", new Annotation[1], 0f, 11f,
                AlignmentAnnotation.BAR_GRAPH);
        conservation.hasText = true;
        conservation.autoCalculated = true;

        if (showConservation)
        {
          alignment.addAnnotation(conservation);
        }

        if (showQuality)
        {
          quality = new AlignmentAnnotation("Quality",
                  "Alignment Quality based on Blosum62 scores",
                  new Annotation[1], 0f, 11f, AlignmentAnnotation.BAR_GRAPH);
          quality.hasText = true;
          quality.autoCalculated = true;

          alignment.addAnnotation(quality);
        }
      }

      consensus = new AlignmentAnnotation("Consensus", "PID",
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      consensus.hasText = true;
      consensus.autoCalculated = true;

      if (showConsensus)
      {
        alignment.addAnnotation(consensus);
      }
    }

  }

  public void showSequenceFeatures(boolean b)
  {
    showSequenceFeatures = b;
  }

  public boolean getShowSequenceFeatures()
  {
    return showSequenceFeatures;
  }

  class ConservationThread extends Thread
  {
    AlignmentPanel ap;

    public ConservationThread(AlignmentPanel ap)
    {
      this.ap = ap;
    }

    public void run()
    {
      try
      {
        updatingConservation = true;

        while (UPDATING_CONSERVATION)
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

        UPDATING_CONSERVATION = true;

        int alWidth = (alignment==null) ? -1 : alignment.getWidth();
        if (alWidth < 0)
        {
          updatingConservation = false;
          UPDATING_CONSERVATION = false;
          return;
        }

        Conservation cons = new jalview.analysis.Conservation("All",
                jalview.schemes.ResidueProperties.propHash, 3,
                alignment.getSequences(), 0, alWidth - 1);

        cons.calculate();
        cons.verdict(false, ConsPercGaps);

        if (quality != null)
        {
          cons.findQuality();
        }

        char[] sequence = cons.getConsSequence().getSequence();
        float minR;
        float minG;
        float minB;
        float maxR;
        float maxG;
        float maxB;
        minR = 0.3f;
        minG = 0.0f;
        minB = 0f;
        maxR = 1.0f - minR;
        maxG = 0.9f - minG;
        maxB = 0f - minB; // scalable range for colouring both Conservation and
        // Quality

        float min = 0f;
        float max = 11f;
        float qmin = 0f;
        float qmax = 0f;

        char c;

        conservation.annotations = new Annotation[alWidth];

        if (quality != null)
        {
          quality.graphMax = cons.qualityRange[1].floatValue();
          quality.annotations = new Annotation[alWidth];
          qmin = cons.qualityRange[0].floatValue();
          qmax = cons.qualityRange[1].floatValue();
        }

        for (int i = 0; i < alWidth; i++)
        {
          float value = 0;

          c = sequence[i];

          if (Character.isDigit(c))
          {
            value = (int) (c - '0');
          }
          else if (c == '*')
          {
            value = 11;
          }
          else if (c == '+')
          {
            value = 10;
          }
          // TODO - refactor to use a graduatedColorScheme to calculate the
          // histogram colors.
          float vprop = value - min;
          vprop /= max;
          conservation.annotations[i] = new Annotation(String.valueOf(c),
                  String.valueOf(value), ' ', value, new Color(minR
                          + (maxR * vprop), minG + (maxG * vprop), minB
                          + (maxB * vprop)));

          // Quality calc
          if (quality != null)
          {
            value = ((Double) cons.quality.elementAt(i)).floatValue();
            vprop = value - qmin;
            vprop /= qmax;
            quality.annotations[i] = new Annotation(" ",
                    String.valueOf(value), ' ', value, new Color(minR
                            + (maxR * vprop), minG + (maxG * vprop), minB
                            + (maxB * vprop)));
          }
        }
      } catch (OutOfMemoryError error)
      {
        System.out.println("Out of memory calculating conservation!!");
        conservation = null;
        quality = null;
        System.gc();
      }

      UPDATING_CONSERVATION = false;
      updatingConservation = false;

      if (ap != null)
      {
        ap.paintAlignment(true);
      }

    }
  }

  ConservationThread conservationThread;

  ConsensusThread consensusThread;

  boolean consUpdateNeeded = false;

  static boolean UPDATING_CONSENSUS = false;

  static boolean UPDATING_CONSERVATION = false;

  boolean updatingConsensus = false;

  boolean updatingConservation = false;

  /**
   * DOCUMENT ME!
   */
  public void updateConservation(final AlignmentPanel ap)
  {
    if (alignment.isNucleotide() || conservation == null)
    {
      return;
    }

    conservationThread = new ConservationThread(ap);
    conservationThread.start();
  }

  /**
   * DOCUMENT ME!
   */
  public void updateConsensus(final AlignmentPanel ap)
  {
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
        int aWidth = alignment==null ? -1 : alignment.getWidth();
        if (aWidth < 0)
        {
          UPDATING_CONSENSUS = false;
          updatingConsensus = false;
          return;
        }

        consensus.annotations = null;
        consensus.annotations = new Annotation[aWidth];

        hconsensus = new Hashtable[aWidth];
        AAFrequency.calculate(alignment.getSequencesArray(), 0,
                alignment.getWidth(), hconsensus, true); // always calculate the
                                                         // full profile
        updateAnnotation(true);
        //AAFrequency.completeConsensus(consensus, hconsensus, 0, aWidth,
        //        ignoreGapsInConsensusCalculation,
        //        true);
        
        if (globalColourScheme != null)
        {
          globalColourScheme.setConsensus(hconsensus);
        }

      } catch (OutOfMemoryError error)
      {
        alignment.deleteAnnotation(consensus);

        consensus = null;
        hconsensus = null;
        System.out.println("Out of memory calculating consensus!!");
        System.gc();
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

  public SequenceGroup getSelectionGroup()
  {
    return selectionGroup;
  }

  public void setSelectionGroup(SequenceGroup sg)
  {
    selectionGroup = sg;
  }

  public boolean getConservationSelected()
  {
    return conservationColourSelected;
  }

  public void setConservationSelected(boolean b)
  {
    conservationColourSelected = b;
  }

  public boolean getAbovePIDThreshold()
  {
    return abovePIDThreshold;
  }

  public void setAbovePIDThreshold(boolean b)
  {
    abovePIDThreshold = b;
  }

  public int getStartRes()
  {
    return startRes;
  }

  public int getEndRes()
  {
    return endRes;
  }

  public int getStartSeq()
  {
    return startSeq;
  }

  public void setGlobalColourScheme(ColourSchemeI cs)
  {
    globalColourScheme = cs;
  }

  public ColourSchemeI getGlobalColourScheme()
  {
    return globalColourScheme;
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

  java.awt.Frame nullFrame;

  protected FeatureSettings featureSettings = null;

  private float heightScale = 1, widthScale = 1;

  public void setFont(Font f)
  {
    font = f;
    if (nullFrame == null)
    {
      nullFrame = new java.awt.Frame();
      nullFrame.addNotify();
    }

    java.awt.FontMetrics fm = nullFrame.getGraphics().getFontMetrics(font);
    setCharHeight((int) (heightScale * fm.getHeight()));
    charWidth = (int) (widthScale * fm.charWidth('M'));

    if (upperCasebold)
    {
      Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
      fm = nullFrame.getGraphics().getFontMetrics(f2);
      charWidth = (int) (widthScale * (fm.stringWidth("MMMMMMMMMMM") / 10));
    }
  }

  public Font getFont()
  {
    return font;
  }

  public int getCharWidth()
  {
    return charWidth;
  }

  public void setCharHeight(int h)
  {
    this.charHeight = h;
  }

  public int getCharHeight()
  {
    return charHeight;
  }

  public void setWrappedWidth(int w)
  {
    this.wrappedWidth = w;
  }

  public int getwrappedWidth()
  {
    return wrappedWidth;
  }

  public AlignmentI getAlignment()
  {
    return alignment;
  }

  public void setAlignment(AlignmentI align)
  {
    this.alignment = align;
  }

  public void setWrapAlignment(boolean state)
  {
    wrapAlignment = state;
  }

  public void setShowText(boolean state)
  {
    showText = state;
  }

  public void setRenderGaps(boolean state)
  {
    renderGaps = state;
  }

  public boolean getColourText()
  {
    return showColourText;
  }

  public void setColourText(boolean state)
  {
    showColourText = state;
  }

  public void setShowBoxes(boolean state)
  {
    showBoxes = state;
  }

  public boolean getWrapAlignment()
  {
    return wrapAlignment;
  }

  public boolean getShowText()
  {
    return showText;
  }

  public boolean getShowBoxes()
  {
    return showBoxes;
  }

  public char getGapCharacter()
  {
    return getAlignment().getGapCharacter();
  }

  public void setGapCharacter(char gap)
  {
    if (getAlignment() != null)
    {
      getAlignment().setGapCharacter(gap);
    }
  }

  public void setThreshold(int thresh)
  {
    threshold = thresh;
  }

  public int getThreshold()
  {
    return threshold;
  }

  public void setIncrement(int inc)
  {
    increment = inc;
  }

  public int getIncrement()
  {
    return increment;
  }

  public void setHiddenColumns(ColumnSelection colsel)
  {
    this.colSel = colsel;
    if (colSel.getHiddenColumns() != null)
    {
      hasHiddenColumns = true;
    }
  }

  public ColumnSelection getColumnSelection()
  {
    return colSel;
  }

  public void resetSeqLimits(int height)
  {
    setEndSeq(height / getCharHeight());
  }

  public void setCurrentTree(NJTree tree)
  {
    currentTree = tree;
  }

  public NJTree getCurrentTree()
  {
    return currentTree;
  }

  public void setColourAppliesToAllGroups(boolean b)
  {
    colourAppliesToAllGroups = b;
  }

  public boolean getColourAppliesToAllGroups()
  {
    return colourAppliesToAllGroups;
  }

  public boolean getShowJVSuffix()
  {
    return showJVSuffix;
  }

  public void setShowJVSuffix(boolean b)
  {
    showJVSuffix = b;
  }

  public boolean getShowAnnotation()
  {
    return showAnnotation;
  }

  public void setShowAnnotation(boolean b)
  {
    showAnnotation = b;
  }

  public boolean getScaleAboveWrapped()
  {
    return scaleAboveWrapped;
  }

  public boolean getScaleLeftWrapped()
  {
    return scaleLeftWrapped;
  }

  public boolean getScaleRightWrapped()
  {
    return scaleRightWrapped;
  }

  public void setScaleAboveWrapped(boolean b)
  {
    scaleAboveWrapped = b;
  }

  public void setScaleLeftWrapped(boolean b)
  {
    scaleLeftWrapped = b;
  }

  public void setScaleRightWrapped(boolean b)
  {
    scaleRightWrapped = b;
  }

  public void setIgnoreGapsConsensus(boolean b)
  {
    ignoreGapsInConsensusCalculation = b;
    updateConsensus(null);
    if (globalColourScheme != null)
    {
      globalColourScheme.setThreshold(globalColourScheme.getThreshold(),
              ignoreGapsInConsensusCalculation);

    }
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

  public boolean getIgnoreGapsConsensus()
  {
    return ignoreGapsInConsensusCalculation;
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

  public void invertColumnSelection()
  {
    for (int i = 0; i < alignment.getWidth(); i++)
    {
      if (colSel.contains(i))
      {
        colSel.removeElement(i);
      }
      else
      {
        if (!hasHiddenColumns || colSel.isVisible(i))
        {
          colSel.addElement(i);
        }
      }
    }
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
      hasHiddenRows = false;
      hiddenRepSequences = null;
      sendSelection();
    }
  }

  public int adjustForHiddenSeqs(int alignmentIndex)
  {
    return alignment.getHiddenSequences().adjustForHiddenSeqs(
            alignmentIndex);
  }

  /**
   * This method returns the a new SequenceI [] with the selection sequence and
   * start and end points adjusted
   * 
   * @return String[]
   */
  public SequenceI[] getSelectionAsNewSequence()
  {
    SequenceI[] sequences;

    if (selectionGroup == null)
    {
      sequences = alignment.getSequencesArray();
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
  jalview.datamodel.AlignmentView getAlignmentView(boolean selectedOnly)
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

    for (i = 0; i < iSize; i++)
    {
      if (hasHiddenColumns)
      {
        StringBuffer visibleSeq = new StringBuffer();
        Vector regions = colSel.getHiddenColumns();

        int blockStart = start, blockEnd = end;
        int[] region;
        int hideStart, hideEnd;

        for (int j = 0; j < regions.size(); j++)
        {
          region = (int[]) regions.elementAt(j);
          hideStart = region[0];
          hideEnd = region[1];

          if (hideStart < start)
          {
            continue;
          }

          blockStart = Math.min(blockStart, hideEnd + 1);
          blockEnd = Math.min(blockEnd, hideStart);

          if (blockStart > blockEnd)
          {
            break;
          }

          visibleSeq.append(seqs[i].getSequence(blockStart, blockEnd));

          blockStart = hideEnd + 1;
          blockEnd = end;
        }

        if (end > blockStart)
        {
          visibleSeq.append(seqs[i].getSequence(blockStart, end));
        }

        selection[i] = visibleSeq.toString();
      }
      else
      {
        selection[i] = seqs[i].getSequenceAsString(start, end);
      }
    }

    return selection;
  }

  public boolean getShowHiddenMarkers()
  {
    return showHiddenMarkers;
  }

  public void setShowHiddenMarkers(boolean show)
  {
    showHiddenMarkers = show;
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
    alignment.padGaps();

    if (hconsensus != null && autocalculateConsensus)
    {
      updateConsensus(ap);
      updateConservation(ap);
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

    // AW alignment.adjustSequenceAnnotations();
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

  boolean centreColumnLabels;

  public boolean getCentreColumnLabels()
  {
    return centreColumnLabels;
  }

  public void updateSequenceIdColours()
  {
    Vector groups = alignment.getGroups();
    for (int ig = 0, igSize = groups.size(); ig < igSize; ig++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(ig);
      if (sg.idColour != null)
      {
        Vector sqs = sg.getSequences(hiddenRepSequences);
        for (int s = 0, sSize = sqs.size(); s < sSize; s++)
        {
          this.setSequenceColour((SequenceI) sqs.elementAt(s), sg.idColour);
        }
      }
    }
  }

  public boolean followHighlight = true;

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

  /**
   * checks current SelectionGroup against record of last hash value, and
   * updates record.
   * 
   * @return true if SelectionGroup changed since last call
   */
  boolean isSelectionGroupChanged()
  {
    int hc = (selectionGroup == null) ? -1 : selectionGroup.hashCode();
    if (hc != sgrouphash)
    {
      sgrouphash = hc;
      return true;
    }
    return false;
  }

  /**
   * checks current colsel against record of last hash value, and updates
   * record.
   * 
   * @return true if colsel changed since last call
   */
  boolean isColSelChanged()
  {
    int hc = (colSel == null) ? -1 : colSel.hashCode();
    if (hc != colselhash)
    {
      colselhash = hc;
      return true;
    }
    return false;
  }
  public void sendSelection()
  {
    jalview.structure.StructureSelectionManager
            .getStructureSelectionManager(applet).sendSelection(
                    new SequenceGroup(getSelectionGroup()),
                    new ColumnSelection(getColumnSelection()), this);
  }




  /**
   * show non-conserved residues only
   */
  public boolean showUnconserved = false;

  /**
   * when set, alignment should be reordered according to a newly opened tree
   */
  public boolean sortByTree = false;

  /**
   * @return the showUnconserved
   */
  public boolean getShowunconserved()
  {
    return showUnconserved;
  }

  /**
   * @param showNonconserved
   *          the showUnconserved to set
   */
  public void setShowunconserved(boolean displayNonconserved)
  {
    this.showUnconserved = displayNonconserved;
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
}
