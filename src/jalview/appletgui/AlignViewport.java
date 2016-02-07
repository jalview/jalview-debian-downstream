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
package jalview.appletgui;

import jalview.analysis.NJTree;
import jalview.api.AlignViewportI;
import jalview.bin.JalviewLite;
import jalview.commands.CommandI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SearchResults;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.UserColourScheme;
import jalview.structure.CommandListener;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.viewmodel.AlignmentViewport;

import java.awt.Font;

public class AlignViewport extends AlignmentViewport implements
        SelectionSource, VamsasSource, CommandListener
{
  boolean cursorMode = false;

  Font font = new Font("SansSerif", Font.PLAIN, 10);

  boolean validCharWidth = true;

  NJTree currentTree = null;

  public jalview.bin.JalviewLite applet;

  boolean MAC = false;

  private AnnotationColumnChooser annotationColumnSelectionState;

  public void finalize()
  {
    applet = null;
    quality = null;
    alignment = null;
    colSel = null;
  }

  public AlignViewport(AlignmentI al, JalviewLite applet)
  {
    super();
    calculator = new jalview.workers.AlignCalcManager();
    this.applet = applet;
    alignment = al;
    // we always pad gaps
    this.setPadGaps(true);
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
        if (JalviewLite.debug)
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
        if (JalviewLite.debug)
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
      setShowJVSuffix(applet.getDefaultParameter("showFullId",
              getShowJVSuffix()));

      setShowAnnotation(applet.getDefaultParameter("showAnnotation",
              isShowAnnotation()));

      showConservation = applet.getDefaultParameter("showConservation",
              showConservation);

      showQuality = applet.getDefaultParameter("showQuality", showQuality);

      showConsensus = applet.getDefaultParameter("showConsensus",
              showConsensus);

      setShowUnconserved(applet.getDefaultParameter("showUnconserved",
              getShowUnconserved()));

      setScaleProteinAsCdna(applet.getDefaultParameter(
              "scaleProteinAsCdna", isScaleProteinAsCdna()));

      String param = applet.getParameter("upperCase");
      if (param != null)
      {
        if (param.equalsIgnoreCase("bold"))
        {
          setUpperCasebold(true);
        }
      }
      sortByTree = applet.getDefaultParameter("sortByTree", sortByTree);

      setFollowHighlight(applet.getDefaultParameter("automaticScrolling",
              isFollowHighlight()));
      followSelection = isFollowHighlight();

      showSequenceLogo = applet.getDefaultParameter("showSequenceLogo",
              showSequenceLogo);

      normaliseSequenceLogo = applet.getDefaultParameter(
              "normaliseSequenceLogo", applet.getDefaultParameter(
                      "normaliseLogo", normaliseSequenceLogo));

      showGroupConsensus = applet.getDefaultParameter("showGroupConsensus",
              showGroupConsensus);

      showGroupConservation = applet.getDefaultParameter(
              "showGroupConservation", showGroupConservation);

      showConsensusHistogram = applet.getDefaultParameter(
              "showConsensusHistogram", showConsensusHistogram);

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
    initAutoAnnotation();

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
    StringBuilder seqs = new StringBuilder(consensus.annotations.length);
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
    setCharWidth((int) (widthScale * fm.charWidth('M')));

    if (isUpperCasebold())
    {
      Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
      fm = nullFrame.getGraphics().getFontMetrics(f2);
      setCharWidth((int) (widthScale * (fm.stringWidth("MMMMMMMMMMM") / 10)));
    }
  }

  public Font getFont()
  {
    return font;
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

  boolean centreColumnLabels;

  public boolean getCentreColumnLabels()
  {
    return centreColumnLabels;
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

  public void sendSelection()
  {
    getStructureSelectionManager().sendSelection(
            new SequenceGroup(getSelectionGroup()),
            new ColumnSelection(getColumnSelection()), this);
  }

  /**
   * Returns an instance of the StructureSelectionManager scoped to this applet
   * instance.
   * 
   * @return
   */
  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return jalview.structure.StructureSelectionManager
            .getStructureSelectionManager(applet);
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

  public boolean isNormaliseSequenceLogo()
  {
    return normaliseSequenceLogo;
  }

  public void setNormaliseSequenceLogo(boolean state)
  {
    normaliseSequenceLogo = state;
  }

  /**
   * 
   * @return true if alignment characters should be displayed
   */
  public boolean isValidCharWidth()
  {
    return validCharWidth;
  }

  public AnnotationColumnChooser getAnnotationColumnSelectionState()
  {
    return annotationColumnSelectionState;
  }

  public void setAnnotationColumnSelectionState(
          AnnotationColumnChooser annotationColumnSelectionState)
  {
    this.annotationColumnSelectionState = annotationColumnSelectionState;
  }

  @Override
  public void mirrorCommand(CommandI command, boolean undo,
          StructureSelectionManager ssm, VamsasSource source)
  {
    // TODO refactor so this can be pulled up to superclass or controller
    /*
     * Do nothing unless we are a 'complement' of the source. May replace this
     * with direct calls not via SSM.
     */
    if (source instanceof AlignViewportI
            && ((AlignViewportI) source).getCodingComplement() == this)
    {
      // ok to continue;
    }
    else
    {
      return;
    }

    CommandI mappedCommand = ssm.mapCommand(command, undo, getAlignment(),
            getGapCharacter());
    if (mappedCommand != null)
    {
      mappedCommand.doCommand(null);
      firePropertyChange("alignment", null, getAlignment().getSequences());

      // ap.scalePanelHolder.repaint();
      // ap.repaint();
    }
  }

  @Override
  public VamsasSource getVamsasSource()
  {
    return this;
  }

  /**
   * If this viewport has a (Protein/cDNA) complement, then scroll the
   * complementary alignment to match this one.
   */
  public void scrollComplementaryAlignment(AlignmentPanel complementPanel)
  {
    if (complementPanel == null)
    {
      return;
    }

    /*
     * Populate a SearchResults object with the mapped location to scroll to. If
     * there is no complement, or it is not following highlights, or no mapping
     * is found, the result will be empty.
     */
    SearchResults sr = new SearchResults();
    int seqOffset = findComplementScrollTarget(sr);
    if (!sr.isEmpty())
    {
      complementPanel.setFollowingComplementScroll(true);
      complementPanel.scrollToCentre(sr, seqOffset);
    }
  }

}
