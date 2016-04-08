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

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Vector;

import jalview.api.AlignmentViewPanel;
import jalview.datamodel.*;
import jalview.structure.StructureSelectionManager;

public class AlignmentPanel extends Panel implements AdjustmentListener, AlignmentViewPanel
{

  public AlignViewport av;

  OverviewPanel overviewPanel;

  SeqPanel seqPanel;

  IdPanel idPanel;

  IdwidthAdjuster idwidthAdjuster;

  public AlignFrame alignFrame;

  ScalePanel scalePanel;

  AnnotationPanel annotationPanel;

  AnnotationLabels alabels;

  // this value is set false when selection area being dragged
  boolean fastPaint = true;
  
  public void finalize() {
    alignFrame=null;
    av=null;
    seqPanel=null;
    seqPanelHolder=null;
    sequenceHolderPanel=null;
    scalePanel=null;
    scalePanelHolder=null;
    annotationPanel=null;
    annotationPanelHolder=null;
    annotationSpaceFillerHolder=null;
  }
  public AlignmentPanel(AlignFrame af, final AlignViewport av)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    alignFrame = af;
    this.av = av;
    seqPanel = new SeqPanel(av, this);
    idPanel = new IdPanel(av, this);
    scalePanel = new ScalePanel(av, this);
    idwidthAdjuster = new IdwidthAdjuster(this);
    annotationPanel = new AnnotationPanel(this);
    annotationPanelHolder.add(annotationPanel, BorderLayout.CENTER);

    sequenceHolderPanel.add(annotationPanelHolder, BorderLayout.SOUTH);

    alabels = new AnnotationLabels(this);

    setAnnotationVisible(av.showAnnotation);

    idPanelHolder.add(idPanel, BorderLayout.CENTER);
    idSpaceFillerPanel1.add(idwidthAdjuster, BorderLayout.CENTER);
    annotationSpaceFillerHolder.add(alabels, BorderLayout.CENTER);
    scalePanelHolder.add(scalePanel, BorderLayout.CENTER);
    seqPanelHolder.add(seqPanel, BorderLayout.CENTER);

    fontChanged();
    setScrollValues(0, 0);

    apvscroll.addAdjustmentListener(this);
    hscroll.addAdjustmentListener(this);
    vscroll.addAdjustmentListener(this);

    addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent evt)
      {
        setScrollValues(av.getStartRes(), av.getStartSeq());
        if (getSize().height>0 && annotationPanelHolder.getSize().height>0) {
          validateAnnotationDimensions(false);
        }
        repaint();
      }
      
    });

    Dimension d = calculateIdWidth();
    idPanel.idCanvas.setSize(d);

    hscrollFillerPanel.setSize(d.width, annotationPanel.getSize().height);

    idPanel.idCanvas.setSize(d.width, seqPanel.seqCanvas.getSize().height);
    annotationSpaceFillerHolder.setSize(d.width,
            annotationPanel.getSize().height);
    alabels.setSize(d.width, annotationPanel.getSize().height);
    final AlignmentPanel ap = this;
    av.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      public void propertyChange(java.beans.PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          PaintRefresher.Refresh(ap, av.getSequenceSetId(), true, true);
          alignmentChanged();
        }
      }
    });

  }

  public SequenceRenderer getSequenceRenderer()
  {
    return seqPanel.seqCanvas.sr;
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return seqPanel.seqCanvas.fr;
  }

  public void alignmentChanged()
  {
    av.alignmentChanged(this);

    if (overviewPanel != null)
    {
      overviewPanel.updateOverviewImage();
    }

    alignFrame.updateEditMenuBar();

    repaint();
  }

  public void fontChanged()
  {
    // set idCanvas bufferedImage to null
    // to prevent drawing old image
    idPanel.idCanvas.image = null;
    FontMetrics fm = getFontMetrics(av.getFont());

    scalePanel.setSize(new Dimension(10, av.charHeight + fm.getDescent()));
    idwidthAdjuster.setSize(new Dimension(10, av.charHeight
            + fm.getDescent()));
    av.updateSequenceIdColours();
    annotationPanel.image = null;
    int ap = annotationPanel.adjustPanelHeight(false);
    Dimension d = calculateIdWidth();
    d.setSize(d.width + 4, seqPanel.seqCanvas.getSize().height);
    alabels.setSize(d.width + 4, ap);
    
    idPanel.idCanvas.setSize(d);
    hscrollFillerPanel.setSize(d);
    
    validateAnnotationDimensions(false);
    annotationPanel.repaint();
    validate();
    repaint();

    if (overviewPanel != null)
    {
      overviewPanel.updateOverviewImage();
    }
  }

  public void setIdWidth(int w, int h)
  {
    idPanel.idCanvas.setSize(w, h);
    idPanelHolder.setSize(w, idPanelHolder.getSize().height);
    annotationSpaceFillerHolder.setSize(w,annotationSpaceFillerHolder.getSize().height);
    alabels.setSize(w, alabels.getSize().height);
    validate();
  }

  Dimension calculateIdWidth()
  {
    if (av.nullFrame == null)
    {
      av.nullFrame = new Frame();
      av.nullFrame.addNotify();
    }

    Graphics g = av.nullFrame.getGraphics();

    FontMetrics fm = g.getFontMetrics(av.font);
    AlignmentI al = av.getAlignment();

    int i = 0;
    int idWidth = 0;
    String id;
    while (i < al.getHeight() && al.getSequenceAt(i) != null)
    {
      SequenceI s = al.getSequenceAt(i);
      id = s.getDisplayId(av.getShowJVSuffix());

      if (fm.stringWidth(id) > idWidth)
      {
        idWidth = fm.stringWidth(id);
      }
      i++;
    }

    // Also check annotation label widths
    i = 0;
    if (al.getAlignmentAnnotation() != null)
    {
      fm = g.getFontMetrics(av.nullFrame.getFont());
      while (i < al.getAlignmentAnnotation().length)
      {
        String label = al.getAlignmentAnnotation()[i].label;
        if (fm.stringWidth(label) > idWidth)
        {
          idWidth = fm.stringWidth(label);
        }
        i++;
      }
    }

    return new Dimension(idWidth, idPanel.idCanvas.getSize().height);
  }

  /**
   * Highlight the given results on the alignment.
   * 
   */
  public void highlightSearchResults(SearchResults results)
  {
    scrollToPosition(results);
    seqPanel.seqCanvas.highlightSearchResults(results);
  }

  /**
   * scroll the view to show the position of the highlighted region in results
   * (if any) and redraw the overview
   * 
   * @param results
   * @return false if results were not found
   */
  public boolean scrollToPosition(SearchResults results)
  {
    return scrollToPosition(results, true);
  }

  /**
   * scroll the view to show the position of the highlighted region in results
   * (if any)
   * 
   * @param results
   * @param redrawOverview
   *          - when set, the overview will be recalculated (takes longer)
   * @return false if results were not found
   */
  public boolean scrollToPosition(SearchResults results,
          boolean redrawOverview)
  {
	  
    // do we need to scroll the panel?
    if (results != null && results.getSize() > 0)
    {
      int seqIndex = av.alignment.findIndex(results);
      if (seqIndex == -1)
      {
        return false;
      }
      SequenceI seq = av.alignment.getSequenceAt(seqIndex);
      int[] r = results.getResults(seq, 0,av.alignment.getWidth());
      if (r == null)
      {
        if (av.applet.debug) {// DEBUG
          System.out.println("DEBUG: scroll didn't happen - results not within alignment : "+seq.getStart()+","+seq.getEnd());
        }
        return false;
      }
      if (av.applet.debug) {
        // DEBUG
        /*System.out.println("DEBUG: scroll: start=" + r[0]
                + " av.getStartRes()=" + av.getStartRes() + " end=" + r[1]
                + " seq.end=" + seq.getEnd() + " av.getEndRes()="
                + av.getEndRes() + " hextent=" + hextent);
         */
      }
      int start = r[0];
      int end = r[1];
      if (start < 0)
      {
        return false;
      }
      if (end == seq.getEnd())
      {
        return false;
      }
      return scrollTo(start, end, seqIndex, false, redrawOverview);
    }
    return true;
  }
  public boolean scrollTo(int ostart, int end, int seqIndex, boolean scrollToNearest, boolean redrawOverview)
  {
	    int startv, endv, starts, ends, width;

	  int start=-1;
    if (av.hasHiddenColumns)
    {
      start = av.getColumnSelection().findColumnPosition(ostart);
      end = av.getColumnSelection().findColumnPosition(end);
      if (start == end)
      {
        if (!scrollToNearest && !av.colSel.isVisible(ostart))
        {
          // don't scroll - position isn't visible
          return false;
        }
      }
    }
    else
    {
      start = ostart;
    }
    if (!av.wrapAlignment)
    {
    	  /*
    	  int spos=av.getStartRes(),sqpos=av.getStartSeq();
          if ((startv = av.getStartRes()) >= start)
          {
        	  spos=start-1;
//        	  seqIn
//            setScrollValues(start - 1, seqIndex);
          }
          else if ((endv = av.getEndRes()) <= end)
          {
//            setScrollValues(spos=startv + 1 + end - endv, seqIndex);
        	  spos=startv + 1 + end - endv;
          }
          else if ((starts = av.getStartSeq()) > seqIndex)
          {
            setScrollValues(av.getStartRes(), seqIndex);
          }
          else if ((ends = av.getEndSeq()) <= seqIndex)
          {
            setScrollValues(av.getStartRes(), starts + seqIndex - ends + 1);
          }

    	  /* */
        if ((av.getStartRes() > end)
                || (av.getEndRes() < start)
                || ((av.getStartSeq() > seqIndex) || (av.getEndSeq() < seqIndex)))
        {
          if (start > av.alignment.getWidth() - hextent)
          {
            start = av.alignment.getWidth() - hextent;
            if (start < 0)
            {
              start = 0;
            }

          }
          if (seqIndex > av.alignment.getHeight() - vextent)
          {
            seqIndex = av.alignment.getHeight() - vextent;
            if (seqIndex < 0)
            {
              seqIndex = 0;
            }
          }
          // System.out.println("trying to scroll to: "+start+" "+seqIndex);
          setScrollValues(start, seqIndex);
        }/**/
      }
      else
      {
        scrollToWrappedVisible(start);
      }
    if (redrawOverview && overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }
    paintAlignment(redrawOverview);
    return true;
  }

  void scrollToWrappedVisible(int res)
  {
    int cwidth = seqPanel.seqCanvas
            .getWrappedCanvasWidth(seqPanel.seqCanvas.getSize().width);
    if (res <= av.getStartRes() || res >= (av.getStartRes() + cwidth))
    {
      vscroll.setValue(res / cwidth);
      av.startRes = vscroll.getValue() * cwidth;
    }
  }

  public OverviewPanel getOverviewPanel()
  {
    return overviewPanel;
  }

  public void setOverviewPanel(OverviewPanel op)
  {
    overviewPanel = op;
  }

  public void setAnnotationVisible(boolean b)
  {
    if (!av.wrapAlignment)
    {
      annotationSpaceFillerHolder.setVisible(b);
      annotationPanelHolder.setVisible(b);
    }
    validate();
    repaint();
  }
  
  /**
   * automatically adjust annotation panel height for new annotation whilst
   * ensuring the alignment is still visible.
   */
  public void adjustAnnotationHeight()
  {
    // TODO: display vertical annotation scrollbar if necessary
    // this is called after loading new annotation onto alignment
    if (alignFrame.getSize().height == 0)
    {
      System.out.println("NEEDS FIXING");
    }
    fontChanged();
    validateAnnotationDimensions(true);
    apvscroll.addNotify();
    hscroll.addNotify();
    validate();
    paintAlignment(true);
  }
  /**
   * calculate the annotation dimensions and refresh slider values accordingly.
   * need to do repaints/notifys afterwards. 
   */
  protected void validateAnnotationDimensions(boolean adjustPanelHeight) {
    boolean modified=false;
    int height = annotationPanel.calcPanelHeight();
    int minsize=0;
    if (hscroll.isVisible())
    {
      height += (minsize=hscroll.getPreferredSize().height);
    }
    if (apvscroll.isVisible()) {
      minsize+=apvscroll.getPreferredSize().height;
    }
    int mheight = height;
    Dimension d=sequenceHolderPanel.getSize(),e=idPanel.getSize(); 
    int seqandannot=d.height-scalePanelHolder.getSize().height;
    // sets initial preferred height
    if ((height+40) > seqandannot / 2)
    {
      height = seqandannot / 2;
    }
    if (!adjustPanelHeight)
    {
      // maintain same window layout whilst updating sliders
      height=annotationPanelHolder.getSize().height;
    }
            
    if (seqandannot-height<5)
    {
      height = seqandannot;
    }
    annotationPanel.setSize(new Dimension(d.width,height));
    alabels.setSize(new Dimension(e.width,height));
    annotationSpaceFillerHolder.setSize(new Dimension(e.width, height));
    annotationPanelHolder.setSize(new Dimension(d.width, height));
    seqPanelHolder.setSize(d.width,seqandannot-height);
    seqPanel.seqCanvas.setSize(d.width, seqPanel.seqCanvas.getSize().height);
    int s=apvscroll.getValue();
    if (s>mheight-height)
    {
      s = 0;
    }
    apvscroll.setValues(s, height, 0, mheight);
    annotationPanel.setScrollOffset(apvscroll.getValue());
    alabels.setScrollOffset(apvscroll.getValue());
  }
  
  public void setWrapAlignment(boolean wrap)
  {
    av.startSeq = 0;
    av.startRes = 0;
    scalePanelHolder.setVisible(!wrap);

    hscroll.setVisible(!wrap);
    idwidthAdjuster.setVisible(!wrap);

    if (wrap)
    {
      annotationPanelHolder.setVisible(false);
      annotationSpaceFillerHolder.setVisible(false);
    }
    else if (av.showAnnotation)
    {
      annotationPanelHolder.setVisible(true);
      annotationSpaceFillerHolder.setVisible(true);
    }

    idSpaceFillerPanel1.setVisible(!wrap);

    fontChanged(); // This is so that the scalePanel is resized correctly

    validate();
    repaint();

  }

  int hextent = 0;

  int vextent = 0;

  // return value is true if the scroll is valid
  public boolean scrollUp(boolean up)
  {
    if (up)
    {
      if (vscroll.getValue() < 1)
      {
        return false;
      }
      setScrollValues(hscroll.getValue(), vscroll.getValue() - 1);
    }
    else
    {
      if (vextent + vscroll.getValue() >= av.getAlignment().getHeight())
      {
        return false;
      }
      setScrollValues(hscroll.getValue(), vscroll.getValue() + 1);
    }

    repaint();
    return true;
  }

  public boolean scrollRight(boolean right)
  {
    if (!right)
    {
      if (hscroll.getValue() < 1)
      {
        return false;
      }
      setScrollValues(hscroll.getValue() - 1, vscroll.getValue());
    }
    else
    {
      if (hextent + hscroll.getValue() >= av.getAlignment().getWidth())
      {
        return false;
      }
      setScrollValues(hscroll.getValue() + 1, vscroll.getValue());
    }

    repaint();
    return true;
  }

  public void setScrollValues(int x, int y)
  {
    int width = av.alignment.getWidth();
    int height = av.alignment.getHeight();

    if (av.hasHiddenColumns)
    {
      width = av.getColumnSelection().findColumnPosition(width);
    }
    if (x<0) { x = 0; };

    hextent = seqPanel.seqCanvas.getSize().width / av.charWidth;
    vextent = seqPanel.seqCanvas.getSize().height / av.charHeight;
    
    if (hextent > width)
    {
      hextent = width;
    }

    if (vextent > height)
    {
      vextent = height;
    }

    if ((hextent + x) > width)
    {
      System.err.println("hextent was "+hextent+" and x was "+x);

      x = width - hextent;
    }

    if ((vextent + y) > height)
    {
      y = height - vextent;
    }

    if (y < 0)
    {
      y = 0;
    }

    if (x < 0)
    {
      System.err.println("x was "+x);
      x = 0;
    }

    av.setStartSeq(y);

    int endSeq = y + vextent;
    if (endSeq > av.alignment.getHeight())
    {
      endSeq = av.alignment.getHeight();
    }

    av.setEndSeq(endSeq);
    av.setStartRes(x);
    av.setEndRes((x + (seqPanel.seqCanvas.getSize().width / av.charWidth)) - 1);

    hscroll.setValues(x, hextent, 0, width);
    vscroll.setValues(y, vextent, 0, height);

    if (overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }
    sendViewPosition();


  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    int oldX = av.getStartRes();
    int oldY = av.getStartSeq();

    if (evt == null || evt.getSource() == apvscroll)
    {
      annotationPanel.setScrollOffset(apvscroll.getValue());
      alabels.setScrollOffset(apvscroll.getValue());
      // annotationPanel.image=null;
      // alabels.image=null;
      // alabels.repaint();
      // annotationPanel.repaint();
    }
    if (evt == null || evt.getSource() == hscroll)
    {
      int x = hscroll.getValue();
      av.setStartRes(x);
      av.setEndRes(x + seqPanel.seqCanvas.getSize().width
              / av.getCharWidth() - 1);
    }

    if (evt == null || evt.getSource() == vscroll)
    {
      int offy = vscroll.getValue();
      if (av.getWrapAlignment())
      {
        int rowSize = seqPanel.seqCanvas
                .getWrappedCanvasWidth(seqPanel.seqCanvas.getSize().width);
        av.setStartRes(vscroll.getValue() * rowSize);
        av.setEndRes((vscroll.getValue() + 1) * rowSize);
      }
      else
      {
        av.setStartSeq(offy);
        av.setEndSeq(offy + seqPanel.seqCanvas.getSize().height
                / av.getCharHeight());
      }
    }

    if (overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }

    int scrollX = av.startRes - oldX;
    int scrollY = av.startSeq - oldY;

    if (av.getWrapAlignment() || !fastPaint || av.MAC)
    {
      repaint();
    }
    else
    {
      // Make sure we're not trying to draw a panel
      // larger than the visible window
      if (scrollX > av.endRes - av.startRes)
      {
        scrollX = av.endRes - av.startRes;
      }
      else if (scrollX < av.startRes - av.endRes)
      {
        scrollX = av.startRes - av.endRes;
      }

      idPanel.idCanvas.fastPaint(scrollY);
      seqPanel.seqCanvas.fastPaint(scrollX, scrollY);

      scalePanel.repaint();
      if (av.getShowAnnotation())
      {
        annotationPanel.fastPaint(av.getStartRes() - oldX);
      }
    }
    sendViewPosition();

  }
  private void sendViewPosition()
  {
    StructureSelectionManager.getStructureSelectionManager(av.applet).sendViewPosition(this, av.startRes, av.endRes, av.startSeq, av.endSeq);
  }

  public void paintAlignment(boolean updateOverview)
  {
    repaint();

    if (updateOverview)
    {
      jalview.structure.StructureSelectionManager
              .getStructureSelectionManager(av.applet).sequenceColoursChanged(this);

      if (overviewPanel != null)
      {
        overviewPanel.updateOverviewImage();
      }
    }
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    invalidate();
    Dimension d = idPanel.idCanvas.getSize();
    idPanel.idCanvas.setSize(d.width, seqPanel.seqCanvas.getSize().height);
    
    if (av.getWrapAlignment())
    {
      int maxwidth = av.alignment.getWidth();

      if (av.hasHiddenColumns)
      {
        maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
      }

      int canvasWidth = seqPanel.seqCanvas
              .getWrappedCanvasWidth(seqPanel.seqCanvas.getSize().width);

      if (canvasWidth > 0)
      {
        int max = maxwidth / canvasWidth;
        vscroll.setMaximum(1 + max);
        vscroll.setUnitIncrement(1);
        vscroll.setVisibleAmount(1);
      }
    }
    else
    {
      setScrollValues(av.getStartRes(), av.getStartSeq());
    }

    alabels.repaint();

    seqPanel.seqCanvas.repaint();
    scalePanel.repaint();
    annotationPanel.repaint();
    idPanel.idCanvas.repaint();
  }

  protected Panel sequenceHolderPanel = new Panel();

  protected Scrollbar vscroll = new Scrollbar();

  protected Scrollbar hscroll = new Scrollbar();

  protected Panel seqPanelHolder = new Panel();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout3 = new BorderLayout();

  protected Panel scalePanelHolder = new Panel();

  protected Panel idPanelHolder = new Panel();

  BorderLayout borderLayout5 = new BorderLayout();

  protected Panel idSpaceFillerPanel1 = new Panel();

  public Panel annotationSpaceFillerHolder = new Panel();

  BorderLayout borderLayout6 = new BorderLayout();

  BorderLayout borderLayout7 = new BorderLayout();

  Panel hscrollHolder = new Panel();

  BorderLayout borderLayout10 = new BorderLayout();

  protected Panel hscrollFillerPanel = new Panel();

  BorderLayout borderLayout11 = new BorderLayout();

  BorderLayout borderLayout4 = new BorderLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  Panel annotationPanelHolder = new Panel();

  protected Scrollbar apvscroll = new Scrollbar();

  BorderLayout borderLayout12 = new BorderLayout();

  private void jbInit() throws Exception
  {
    // idPanelHolder.setPreferredSize(new Dimension(70, 10));
    this.setLayout(borderLayout7);

    //sequenceHolderPanel.setPreferredSize(new Dimension(150, 150));
    sequenceHolderPanel.setLayout(borderLayout3);
    seqPanelHolder.setLayout(borderLayout1);
    scalePanelHolder.setBackground(Color.white);

    // scalePanelHolder.setPreferredSize(new Dimension(10, 30));
    scalePanelHolder.setLayout(borderLayout6);
    idPanelHolder.setLayout(borderLayout5);
    idSpaceFillerPanel1.setBackground(Color.white);

    // idSpaceFillerPanel1.setPreferredSize(new Dimension(10, 30));
    idSpaceFillerPanel1.setLayout(borderLayout11);
    annotationSpaceFillerHolder.setBackground(Color.white);

    // annotationSpaceFillerHolder.setPreferredSize(new Dimension(10, 80));
    annotationSpaceFillerHolder.setLayout(borderLayout4);
    hscroll.setOrientation(Scrollbar.HORIZONTAL);
    hscrollHolder.setLayout(borderLayout10);
    hscrollFillerPanel.setBackground(Color.white);
    apvscroll.setOrientation(Scrollbar.VERTICAL);
    apvscroll.setVisible(true);
    apvscroll.addAdjustmentListener(this);

    annotationPanelHolder.setBackground(Color.white);
    annotationPanelHolder.setLayout(borderLayout12);
    annotationPanelHolder.add(apvscroll, BorderLayout.EAST);
    // hscrollFillerPanel.setPreferredSize(new Dimension(70, 10));
    hscrollHolder.setBackground(Color.white);

    // annotationScroller.setPreferredSize(new Dimension(10, 80));
    // this.setPreferredSize(new Dimension(220, 166));
    seqPanelHolder.setBackground(Color.white);
    idPanelHolder.setBackground(Color.white);
    sequenceHolderPanel.add(scalePanelHolder, BorderLayout.NORTH);
    sequenceHolderPanel.add(seqPanelHolder, BorderLayout.CENTER);
    seqPanelHolder.add(vscroll, BorderLayout.EAST);

    // Panel3.add(secondaryPanelHolder, BorderLayout.SOUTH);
    this.add(idPanelHolder, BorderLayout.WEST);
    idPanelHolder.add(idSpaceFillerPanel1, BorderLayout.NORTH);
    idPanelHolder.add(annotationSpaceFillerHolder, BorderLayout.SOUTH);
    this.add(hscrollHolder, BorderLayout.SOUTH);
    hscrollHolder.add(hscroll, BorderLayout.CENTER);
    hscrollHolder.add(hscrollFillerPanel, BorderLayout.WEST);
    this.add(sequenceHolderPanel, BorderLayout.CENTER);
  }

  /**
   * hides or shows dynamic annotation rows based on groups and av state flags
   */
  public void updateAnnotation()
  {
    updateAnnotation(false);
  }

  public void updateAnnotation(boolean applyGlobalSettings)
  {
    // TODO: this should be merged with other annotation update stuff - that
    // sits on AlignViewport
    boolean updateCalcs = false;
    boolean conv = av.isShowGroupConservation();
    boolean cons = av.isShowGroupConsensus();
    boolean showprf = av.isShowSequenceLogo();
    boolean showConsHist = av.isShowConsensusHistogram();

    boolean sortg = true;

    // remove old automatic annotation
    // add any new annotation

    Vector gr = av.alignment.getGroups(); // OrderedBy(av.alignment.getSequencesArray());
    // intersect alignment annotation with alignment groups

    AlignmentAnnotation[] aan = av.alignment.getAlignmentAnnotation();
    Hashtable oldrfs = new Hashtable();
    if (aan != null)
    {
      for (int an = 0; an < aan.length; an++)
      {
        if (aan[an].autoCalculated && aan[an].groupRef != null)
        {
          oldrfs.put(aan[an].groupRef, aan[an].groupRef);
          av.alignment.deleteAnnotation(aan[an]);
          aan[an] = null;
        }
      }
    }
    SequenceGroup sg;
    if (gr != null)
    {
      for (int g = 0; g < gr.size(); g++)
      {
        updateCalcs = false;
        sg = (SequenceGroup) gr.elementAt(g);
        if (applyGlobalSettings || !oldrfs.containsKey(sg))
        {
          // set defaults for this group's conservation/consensus
          sg.setshowSequenceLogo(showprf);
          sg.setShowConsensusHistogram(showConsHist);
        }
        if (conv)
        {
          updateCalcs = true;
          av.alignment.addAnnotation(sg.getConservationRow(), 0);
        }
        if (cons)
        {
          updateCalcs = true;
          av.alignment.addAnnotation(sg.getConsensus(), 0);
        }
        // refresh the annotation rows
        if (updateCalcs)
        {
          sg.recalcConservation();
        }
      }
    }
    oldrfs.clear();
    adjustAnnotationHeight();
  }

  @Override
  public AlignmentI getAlignment()
  {
    return av.alignment;
  }
  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return StructureSelectionManager.getStructureSelectionManager(av.applet);
  }

}
