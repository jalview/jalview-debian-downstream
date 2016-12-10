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
package jalview.gui;

import jalview.analysis.AnnotationSorter;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GAlignmentPanel;
import jalview.math.AlignmentDimension;
import jalview.schemes.ResidueProperties;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.util.Platform;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.161 $
 */
public class AlignmentPanel extends GAlignmentPanel implements
        AdjustmentListener, Printable, AlignmentViewPanel
{
  public AlignViewport av;

  OverviewPanel overviewPanel;

  private SeqPanel seqPanel;

  private IdPanel idPanel;

  private boolean headless;

  IdwidthAdjuster idwidthAdjuster;

  /** DOCUMENT ME!! */
  public AlignFrame alignFrame;

  private ScalePanel scalePanel;

  private AnnotationPanel annotationPanel;

  private AnnotationLabels alabels;

  // this value is set false when selection area being dragged
  boolean fastPaint = true;

  int hextent = 0;

  int vextent = 0;

  /*
   * Flag set while scrolling to follow complementary cDNA/protein scroll. When
   * true, suppresses invoking the same method recursively.
   */
  private boolean dontScrollComplement;

  private PropertyChangeListener propertyChangeListener;

  /**
   * Creates a new AlignmentPanel object.
   * 
   * @param af
   * @param av
   */
  public AlignmentPanel(AlignFrame af, final AlignViewport av)
  {
    alignFrame = af;
    this.av = av;
    setSeqPanel(new SeqPanel(av, this));
    setIdPanel(new IdPanel(av, this));

    setScalePanel(new ScalePanel(av, this));

    idPanelHolder.add(getIdPanel(), BorderLayout.CENTER);
    idwidthAdjuster = new IdwidthAdjuster(this);
    idSpaceFillerPanel1.add(idwidthAdjuster, BorderLayout.CENTER);

    setAnnotationPanel(new AnnotationPanel(this));
    setAlabels(new AnnotationLabels(this));

    annotationScroller.setViewportView(getAnnotationPanel());
    annotationSpaceFillerHolder.add(getAlabels(), BorderLayout.CENTER);

    scalePanelHolder.add(getScalePanel(), BorderLayout.CENTER);
    seqPanelHolder.add(getSeqPanel(), BorderLayout.CENTER);

    setScrollValues(0, 0);

    hscroll.addAdjustmentListener(this);
    vscroll.addAdjustmentListener(this);

    final AlignmentPanel ap = this;
    propertyChangeListener = new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          PaintRefresher.Refresh(ap, av.getSequenceSetId(), true, true);
          alignmentChanged();
        }
      }
    };
    av.addPropertyChangeListener(propertyChangeListener);
    fontChanged();
    adjustAnnotationHeight();
    updateLayout();
  }

  @Override
  public AlignViewportI getAlignViewport()
  {
    return av;
  }

  public void alignmentChanged()
  {
    av.alignmentChanged(this);

    alignFrame.updateEditMenuBar();

    paintAlignment(true);

  }

  /**
   * DOCUMENT ME!
   */
  public void fontChanged()
  {
    // set idCanvas bufferedImage to null
    // to prevent drawing old image
    FontMetrics fm = getFontMetrics(av.getFont());

    scalePanelHolder.setPreferredSize(new Dimension(10, av.getCharHeight()
            + fm.getDescent()));
    idSpaceFillerPanel1.setPreferredSize(new Dimension(10, av
            .getCharHeight() + fm.getDescent()));

    getIdPanel().getIdCanvas().gg = null;
    getSeqPanel().seqCanvas.img = null;
    getAnnotationPanel().adjustPanelHeight();

    Dimension d = calculateIdWidth();

    d.setSize(d.width + 4, d.height);
    getIdPanel().getIdCanvas().setPreferredSize(d);
    hscrollFillerPanel.setPreferredSize(d);

    if (overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }
    if (this.alignFrame.getSplitViewContainer() != null)
    {
      ((SplitFrame) this.alignFrame.getSplitViewContainer()).adjustLayout();
    }

    repaint();
  }

  /**
   * Calculate the width of the alignment labels based on the displayed names
   * and any bounds on label width set in preferences.
   * 
   * @return Dimension giving the maximum width of the alignment label panel
   *         that should be used.
   */
  public Dimension calculateIdWidth()
  {
    // calculate sensible default width when no preference is available
    Dimension r = null;
    if (av.getIdWidth() < 0)
    {
      int afwidth = (alignFrame != null ? alignFrame.getWidth() : 300);
      int maxwidth = Math.max(20, Math.min(afwidth - 200, 2 * afwidth / 3));
      r = calculateIdWidth(maxwidth);
      av.setIdWidth(r.width);
    }
    else
    {
      r = new Dimension();
      r.width = av.getIdWidth();
      r.height = 0;
    }
    return r;
  }

  /**
   * Calculate the width of the alignment labels based on the displayed names
   * and any bounds on label width set in preferences.
   * 
   * @param maxwidth
   *          -1 or maximum width allowed for IdWidth
   * @return Dimension giving the maximum width of the alignment label panel
   *         that should be used.
   */
  public Dimension calculateIdWidth(int maxwidth)
  {
    Container c = new Container();

    FontMetrics fm = c.getFontMetrics(new Font(av.font.getName(),
            Font.ITALIC, av.font.getSize()));

    AlignmentI al = av.getAlignment();
    int i = 0;
    int idWidth = 0;
    String id;

    while ((i < al.getHeight()) && (al.getSequenceAt(i) != null))
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
      fm = c.getFontMetrics(getAlabels().getFont());

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

    return new Dimension(maxwidth < 0 ? idWidth : Math.min(maxwidth,
            idWidth), 12);
  }

  /**
   * Highlight the given results on the alignment.
   * 
   */
  public void highlightSearchResults(SearchResultsI results)
  {
    scrollToPosition(results);
    getSeqPanel().seqCanvas.highlightSearchResults(results);
  }

  /**
   * Scroll the view to show the position of the highlighted region in results
   * (if any) and redraw the overview
   * 
   * @param results
   */
  public boolean scrollToPosition(SearchResultsI results)
  {
    return scrollToPosition(results, 0, true, false);
  }

  /**
   * Scroll the view to show the position of the highlighted region in results
   * (if any)
   * 
   * @param searchResults
   * @param redrawOverview
   * @return
   */
  public boolean scrollToPosition(SearchResultsI searchResults,
          boolean redrawOverview)
  {
    return scrollToPosition(searchResults, 0, redrawOverview, false);
  }

  /**
   * Scroll the view to show the position of the highlighted region in results
   * (if any)
   * 
   * @param results
   * @param verticalOffset
   *          if greater than zero, allows scrolling to a position below the
   *          first displayed sequence
   * @param redrawOverview
   *          - when set, the overview will be recalculated (takes longer)
   * @param centre
   *          if true, try to centre the search results horizontally in the view
   * @return false if results were not found
   */
  public boolean scrollToPosition(SearchResultsI results,
          int verticalOffset, boolean redrawOverview, boolean centre)
  {
    int startv, endv, starts, ends;
    // TODO: properly locate search results in view when large numbers of hidden
    // columns exist before highlighted region
    // do we need to scroll the panel?
    // TODO: tons of nullpointerexceptions raised here.
    if (results != null && results.getSize() > 0 && av != null
            && av.getAlignment() != null)
    {
      int seqIndex = av.getAlignment().findIndex(results);
      if (seqIndex == -1)
      {
        return false;
      }
      SequenceI seq = av.getAlignment().getSequenceAt(seqIndex);

      int[] r = results.getResults(seq, 0, av.getAlignment().getWidth());
      if (r == null)
      {
        return false;
      }
      int start = r[0];
      int end = r[1];
      // DEBUG
      // System.err.println(this.av.viewName + " Seq : " + seqIndex
      // + " Scroll to " + start + "," + end);

      /*
       * To centre results, scroll to positions half the visible width
       * left/right of the start/end positions
       */
      if (centre)
      {
        int offset = (av.getEndRes() - av.getStartRes() + 1) / 2 - 1;
        start = Math.max(start - offset, 0);
        end = end + offset - 1;
      }
      if (start < 0)
      {
        return false;
      }
      if (end == seq.getEnd())
      {
        return false;
      }
      if (av.hasHiddenColumns())
      {
        start = av.getColumnSelection().findColumnPosition(start);
        end = av.getColumnSelection().findColumnPosition(end);
        if (start == end)
        {
          if (!av.getColumnSelection().isVisible(r[0]))
          {
            // don't scroll - position isn't visible
            return false;
          }
        }
      }

      /*
       * allow for offset of target sequence (actually scroll to one above it)
       */
      seqIndex = Math.max(0, seqIndex - verticalOffset);

      // System.out.println("start=" + start + ", end=" + end + ", startv="
      // + av.getStartRes() + ", endv=" + av.getEndRes() + ", starts="
      // + av.getStartSeq() + ", ends=" + av.getEndSeq());
      if (!av.getWrapAlignment())
      {
        if ((startv = av.getStartRes()) >= start)
        {
          /*
           * Scroll left to make start of search results visible
           */
          // setScrollValues(start - 1, seqIndex); // plus one residue
          setScrollValues(start, seqIndex);
        }
        else if ((endv = av.getEndRes()) <= end)
        {
          /*
           * Scroll right to make end of search results visible
           */
          // setScrollValues(startv + 1 + end - endv, seqIndex); // plus one
          setScrollValues(startv + end - endv, seqIndex);
        }
        else if ((starts = av.getStartSeq()) > seqIndex)
        {
          /*
           * Scroll up to make start of search results visible
           */
          setScrollValues(av.getStartRes(), seqIndex);
        }
        else if ((ends = av.getEndSeq()) <= seqIndex)
        {
          /*
           * Scroll down to make end of search results visible
           */
          setScrollValues(av.getStartRes(), starts + seqIndex - ends + 1);
        }
        /*
         * Else results are already visible - no need to scroll
         */
      }
      else
      {
        scrollToWrappedVisible(start);
      }
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
    int cwidth = getSeqPanel().seqCanvas
            .getWrappedCanvasWidth(getSeqPanel().seqCanvas.getWidth());
    if (res < av.getStartRes() || res >= (av.getStartRes() + cwidth))
    {
      vscroll.setValue((res / cwidth));
      av.startRes = vscroll.getValue() * cwidth;
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public OverviewPanel getOverviewPanel()
  {
    return overviewPanel;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param op
   *          DOCUMENT ME!
   */
  public void setOverviewPanel(OverviewPanel op)
  {
    overviewPanel = op;
  }

  /**
   * 
   * @param b
   *          Hide or show annotation panel
   * 
   */
  public void setAnnotationVisible(boolean b)
  {
    if (!av.getWrapAlignment())
    {
      annotationSpaceFillerHolder.setVisible(b);
      annotationScroller.setVisible(b);
    }
    repaint();
  }

  /**
   * automatically adjust annotation panel height for new annotation whilst
   * ensuring the alignment is still visible.
   */
  @Override
  public void adjustAnnotationHeight()
  {
    // TODO: display vertical annotation scrollbar if necessary
    // this is called after loading new annotation onto alignment
    if (alignFrame.getHeight() == 0)
    {
      System.out.println("NEEDS FIXING");
    }
    validateAnnotationDimensions(true);
    addNotify();
    paintAlignment(true);
  }

  /**
   * calculate the annotation dimensions and refresh slider values accordingly.
   * need to do repaints/notifys afterwards.
   */
  protected void validateAnnotationDimensions(boolean adjustPanelHeight)
  {
    int annotationHeight = getAnnotationPanel().adjustPanelHeight();

    if (adjustPanelHeight)
    {
      int rowHeight = av.getCharHeight();
      int alignmentHeight = rowHeight * av.getAlignment().getHeight();

      /*
       * Estimate available height in the AlignFrame for alignment +
       * annotations. Deduct an estimate for title bar, menu bar, scale panel,
       * hscroll, status bar (as these are not laid out we can't inspect their
       * actual heights). Insets gives frame borders.
       */
      int stuff = Platform.isAMac() ? 80 : 100;
      Insets insets = alignFrame.getInsets();
      int availableHeight = alignFrame.getHeight() - stuff - insets.top
              - insets.bottom;

      /*
       * If not enough vertical space, maximize annotation height while keeping
       * at least two rows of alignment visible
       */
      if (annotationHeight + alignmentHeight > availableHeight)
      {
        annotationHeight = Math.min(annotationHeight, availableHeight - 2
                * rowHeight);
      }
    }
    else
    {
      // maintain same window layout whilst updating sliders
      annotationHeight = annotationScroller.getSize().height;
    }
    hscroll.addNotify();

    annotationScroller.setPreferredSize(new Dimension(annotationScroller
            .getWidth(), annotationHeight));

    Dimension e = idPanel.getSize();
    alabels.setSize(new Dimension(e.width, annotationHeight));

    annotationSpaceFillerHolder.setPreferredSize(new Dimension(
            annotationSpaceFillerHolder.getWidth(), annotationHeight));
    annotationScroller.validate();
    annotationScroller.addNotify();
  }

  /**
   * update alignment layout for viewport settings
   * 
   * @param wrap
   *          DOCUMENT ME!
   */
  public void updateLayout()
  {
    fontChanged();
    setAnnotationVisible(av.isShowAnnotation());
    boolean wrap = av.getWrapAlignment();
    av.startSeq = 0;
    scalePanelHolder.setVisible(!wrap);
    hscroll.setVisible(!wrap);
    idwidthAdjuster.setVisible(!wrap);

    if (wrap)
    {
      annotationScroller.setVisible(false);
      annotationSpaceFillerHolder.setVisible(false);
    }
    else if (av.isShowAnnotation())
    {
      annotationScroller.setVisible(true);
      annotationSpaceFillerHolder.setVisible(true);
    }

    idSpaceFillerPanel1.setVisible(!wrap);

    repaint();
  }

  // return value is true if the scroll is valid
  public boolean scrollUp(boolean up)
  {
    if (up)
    {
      if (vscroll.getValue() < 1)
      {
        return false;
      }

      fastPaint = false;
      vscroll.setValue(vscroll.getValue() - 1);
    }
    else
    {
      if ((vextent + vscroll.getValue()) >= av.getAlignment().getHeight())
      {
        return false;
      }

      fastPaint = false;
      vscroll.setValue(vscroll.getValue() + 1);
    }

    fastPaint = true;

    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param right
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean scrollRight(boolean right)
  {
    if (!right)
    {
      if (hscroll.getValue() < 1)
      {
        return false;
      }

      fastPaint = false;
      hscroll.setValue(hscroll.getValue() - 1);
    }
    else
    {
      if ((hextent + hscroll.getValue()) >= av.getAlignment().getWidth())
      {
        return false;
      }

      fastPaint = false;
      hscroll.setValue(hscroll.getValue() + 1);
    }

    fastPaint = true;

    return true;
  }

  /**
   * Adjust row/column scrollers to show a visible position in the alignment.
   * 
   * @param x
   *          visible column to scroll to
   * @param y
   *          visible row to scroll to
   * 
   */
  public void setScrollValues(int x, int y)
  {
    // System.err.println("Scroll " + this.av.viewName + " to " + x + "," + y);
    if (av == null || av.getAlignment() == null)
    {
      return;
    }
    int width = av.getAlignment().getWidth();
    int height = av.getAlignment().getHeight();

    if (av.hasHiddenColumns())
    {
      width = av.getColumnSelection().findColumnPosition(width);
    }

    av.setEndRes((x + (getSeqPanel().seqCanvas.getWidth() / av
            .getCharWidth())) - 1);

    hextent = getSeqPanel().seqCanvas.getWidth() / av.getCharWidth();
    vextent = getSeqPanel().seqCanvas.getHeight() / av.getCharHeight();

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
      x = 0;
    }

    /*
     * each scroll adjustment triggers adjustmentValueChanged, which resets the
     * 'do not scroll complement' flag; ensure it is the same for both
     * operations
     */
    boolean flag = isDontScrollComplement();
    hscroll.setValues(x, hextent, 0, width);
    setDontScrollComplement(flag);
    vscroll.setValues(y, vextent, 0, height);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    int oldX = av.getStartRes();
    int oldY = av.getStartSeq();

    if (evt.getSource() == hscroll)
    {
      int x = hscroll.getValue();
      av.setStartRes(x);
      av.setEndRes((x + (getSeqPanel().seqCanvas.getWidth() / av
              .getCharWidth())) - 1);
    }

    if (evt.getSource() == vscroll)
    {
      int offy = vscroll.getValue();

      if (av.getWrapAlignment())
      {
        if (offy > -1)
        {
          int rowSize = getSeqPanel().seqCanvas
                  .getWrappedCanvasWidth(getSeqPanel().seqCanvas.getWidth());
          av.setStartRes(offy * rowSize);
          av.setEndRes((offy + 1) * rowSize);
        }
        else
        {
          // This is only called if file loaded is a jar file that
          // was wrapped when saved and user has wrap alignment true
          // as preference setting
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              setScrollValues(av.getStartRes(), av.getStartSeq());
            }
          });
        }
      }
      else
      {
        av.setStartSeq(offy);
        av.setEndSeq(offy
                + (getSeqPanel().seqCanvas.getHeight() / av.getCharHeight()));
      }
    }

    if (overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }

    int scrollX = av.startRes - oldX;
    int scrollY = av.startSeq - oldY;

    if (av.getWrapAlignment() || !fastPaint)
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

      if (scrollX != 0 || scrollY != 0)
      {
        getIdPanel().getIdCanvas().fastPaint(scrollY);
        getSeqPanel().seqCanvas.fastPaint(scrollX, scrollY);
        getScalePanel().repaint();

        if (av.isShowAnnotation() && scrollX != 0)
        {
          getAnnotationPanel().fastPaint(scrollX);
        }
      }
    }
    /*
     * If there is one, scroll the (Protein/cDNA) complementary alignment to
     * match, unless we are ourselves doing that.
     */
    if (isDontScrollComplement())
    {
      setDontScrollComplement(false);
    }
    else
    {
      av.scrollComplementaryAlignment();
    }
  }

  /**
   * Repaint the alignment including the annotations and overview panels (if
   * shown).
   */
  @Override
  public void paintAlignment(boolean updateOverview)
  {
    final AnnotationSorter sorter = new AnnotationSorter(getAlignment(),
            av.isShowAutocalculatedAbove());
    sorter.sort(getAlignment().getAlignmentAnnotation(),
            av.getSortAnnotationsBy());
    repaint();

    if (updateOverview)
    {
      // TODO: determine if this paintAlignment changed structure colours
      av.getStructureSelectionManager().sequenceColoursChanged(this);

      if (overviewPanel != null)
      {
        overviewPanel.updateOverviewImage();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    invalidate();

    Dimension d = getIdPanel().getIdCanvas().getPreferredSize();
    idPanelHolder.setPreferredSize(d);
    hscrollFillerPanel.setPreferredSize(new Dimension(d.width, 12));
    validate();

    /*
     * set scroll bar positions; first suppress this being 'followed' in any
     * complementary split pane
     */
    setDontScrollComplement(true);

    if (av.getWrapAlignment())
    {
      int maxwidth = av.getAlignment().getWidth();

      if (av.hasHiddenColumns())
      {
        maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
      }

      int canvasWidth = getSeqPanel().seqCanvas
              .getWrappedCanvasWidth(getSeqPanel().seqCanvas.getWidth());
      if (canvasWidth > 0)
      {
        int max = maxwidth
                / getSeqPanel().seqCanvas
                        .getWrappedCanvasWidth(getSeqPanel().seqCanvas
                                .getWidth()) + 1;
        vscroll.setMaximum(max);
        vscroll.setUnitIncrement(1);
        vscroll.setVisibleAmount(1);
      }
    }
    else
    {
      setScrollValues(av.getStartRes(), av.getStartSeq());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pg
   *          DOCUMENT ME!
   * @param pf
   *          DOCUMENT ME!
   * @param pi
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   * 
   * @throws PrinterException
   *           DOCUMENT ME!
   */
  @Override
  public int print(Graphics pg, PageFormat pf, int pi)
          throws PrinterException
  {
    pg.translate((int) pf.getImageableX(), (int) pf.getImageableY());

    int pwidth = (int) pf.getImageableWidth();
    int pheight = (int) pf.getImageableHeight();

    if (av.getWrapAlignment())
    {
      return printWrappedAlignment(pwidth, pheight, pi, pg);
    }
    else
    {
      return printUnwrapped(pwidth, pheight, pi, pg, pg);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pg
   *          DOCUMENT ME!
   * @param pwidth
   *          DOCUMENT ME!
   * @param pheight
   *          DOCUMENT ME!
   * @param pi
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   * 
   * @throws PrinterException
   *           DOCUMENT ME!
   */
  /**
   * Draws the alignment image, including sequence ids, sequences, and
   * annotation labels and annotations if shown, on either one or two Graphics
   * context.
   * 
   * @param pageWidth
   * @param pageHeight
   * @param pi
   * @param idGraphics
   *          the graphics context for sequence ids and annotation labels
   * @param alignmentGraphics
   *          the graphics context for sequences and annotations (may or may not
   *          be the same context as idGraphics)
   * @return
   * @throws PrinterException
   */
  public int printUnwrapped(int pageWidth, int pageHeight, int pi,
          Graphics idGraphics, Graphics alignmentGraphics)
          throws PrinterException
  {
    final int idWidth = getVisibleIdWidth(false);

    /*
     * Get the horizontal offset to where we draw the sequences.
     * This is idWidth if using a single Graphics context, else zero.
     */
    final int alignmentGraphicsOffset = idGraphics != alignmentGraphics ? 0 : idWidth;

    FontMetrics fm = getFontMetrics(av.getFont());
    int charHeight = av.getCharHeight();
    int scaleHeight = charHeight + fm.getDescent();

    idGraphics.setColor(Color.white);
    idGraphics.fillRect(0, 0, pageWidth, pageHeight);
    idGraphics.setFont(av.getFont());

    /*
     * How many sequences and residues can we fit on a printable page?
     */
    int totalRes = (pageWidth - idWidth) / av.getCharWidth();

    int totalSeq = (pageHeight - scaleHeight) / charHeight - 1;

    int alignmentWidth = av.getAlignment().getWidth();
    int pagesWide = (alignmentWidth / totalRes) + 1;

    final int startRes = (pi % pagesWide) * totalRes;
    int endRes = (startRes + totalRes) - 1;

    if (endRes > (alignmentWidth - 1))
    {
      endRes = alignmentWidth - 1;
    }

    final int startSeq = (pi / pagesWide) * totalSeq;
    int endSeq = startSeq + totalSeq;

    int alignmentHeight = av.getAlignment().getHeight();
    if (endSeq > alignmentHeight)
    {
      endSeq = alignmentHeight;
    }

    int pagesHigh = ((alignmentHeight / totalSeq) + 1)
            * pageHeight;

    if (av.isShowAnnotation())
    {
      pagesHigh += getAnnotationPanel().adjustPanelHeight() + 3;
    }

    pagesHigh /= pageHeight;

    if (pi >= (pagesWide * pagesHigh))
    {
      return Printable.NO_SUCH_PAGE;
    }
    final int alignmentDrawnHeight = (endSeq - startSeq) * charHeight
            + 3;

    /*
     * draw the Scale at horizontal offset, then reset to top left (0, 0)
     */
    alignmentGraphics.translate(alignmentGraphicsOffset, 0);
    getScalePanel().drawScale(alignmentGraphics, startRes, endRes,
            pageWidth - idWidth, scaleHeight);
    alignmentGraphics.translate(-alignmentGraphicsOffset, 0);

    /*
     * Draw the sequence ids, offset for scale height,
     * then reset to top left (0, 0)
     */
    idGraphics.translate(0, scaleHeight);
    idGraphics.setFont(getIdPanel().getIdCanvas().getIdfont());
    Color currentColor = null;
    Color currentTextColor = null;

    SequenceI seq;
    for (int i = startSeq; i < endSeq; i++)
    {
      seq = av.getAlignment().getSequenceAt(i);
      if ((av.getSelectionGroup() != null)
              && av.getSelectionGroup().getSequences(null).contains(seq))
      {
        /*
         * gray out ids of sequences in selection group (if any)
         */
        currentColor = Color.gray;
        currentTextColor = Color.black;
      }
      else
      {
        currentColor = av.getSequenceColour(seq);
        currentTextColor = Color.black;
      }

      idGraphics.setColor(currentColor);
      idGraphics.fillRect(0, (i - startSeq) * charHeight, idWidth,
              charHeight);

      idGraphics.setColor(currentTextColor);

      int xPos = 0;
      String displayId = seq.getDisplayId(av.getShowJVSuffix());
      if (av.isRightAlignIds())
      {
        fm = idGraphics.getFontMetrics();
        xPos = idWidth
                - fm.stringWidth(displayId)
                - 4;
      }

      idGraphics.drawString(displayId, xPos,
              (((i - startSeq) * charHeight) + charHeight)
                      - (charHeight / 5));
    }
    idGraphics.setFont(av.getFont());
    idGraphics.translate(0, -scaleHeight);

    /*
     * draw the sequences, offset for scale height, and id width (if using a
     * single graphics context), then reset to (0, scale height)
     */
    alignmentGraphics.translate(alignmentGraphicsOffset, scaleHeight);
    getSeqPanel().seqCanvas.drawPanel(alignmentGraphics, startRes, endRes,
            startSeq, endSeq, 0);
    alignmentGraphics.translate(-alignmentGraphicsOffset, 0);

    if (av.isShowAnnotation() && (endSeq == alignmentHeight))
    {
      /*
       * draw annotation labels; drawComponent() translates by
       * getScrollOffset(), so compensate for that first;
       * then reset to (0, scale height)
       */
      int offset = getAlabels().getScrollOffset();
      idGraphics.translate(0, -offset);
      idGraphics.translate(0, alignmentDrawnHeight);
      getAlabels().drawComponent(idGraphics, idWidth);
      idGraphics.translate(0, -alignmentDrawnHeight);

      /*
       * draw the annotations starting at 
       * (idOffset, alignmentHeight) from (0, scaleHeight)
       */
      alignmentGraphics.translate(alignmentGraphicsOffset, alignmentDrawnHeight);
      getAnnotationPanel().renderer.drawComponent(getAnnotationPanel(), av,
              alignmentGraphics, -1, startRes, endRes + 1);
    }

    return Printable.PAGE_EXISTS;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pg
   *          DOCUMENT ME!
   * @param pwidth
   *          DOCUMENT ME!
   * @param pheight
   *          DOCUMENT ME!
   * @param pi
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   * 
   * @throws PrinterException
   *           DOCUMENT ME!
   */
  public int printWrappedAlignment(int pwidth, int pheight, int pi,
          Graphics pg) throws PrinterException
  {
    int annotationHeight = 0;
    AnnotationLabels labels = null;
    if (av.isShowAnnotation())
    {
      annotationHeight = getAnnotationPanel().adjustPanelHeight();
      labels = new AnnotationLabels(av);
    }

    int hgap = av.getCharHeight();
    if (av.getScaleAboveWrapped())
    {
      hgap += av.getCharHeight();
    }

    int cHeight = av.getAlignment().getHeight() * av.getCharHeight() + hgap
            + annotationHeight;

    int idWidth = getVisibleIdWidth(false);

    int maxwidth = av.getAlignment().getWidth();
    if (av.hasHiddenColumns())
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    int resWidth = getSeqPanel().seqCanvas.getWrappedCanvasWidth(pwidth
            - idWidth);

    int totalHeight = cHeight * (maxwidth / resWidth + 1);

    pg.setColor(Color.white);
    pg.fillRect(0, 0, pwidth, pheight);
    pg.setFont(av.getFont());

    // //////////////
    // Draw the ids
    pg.setColor(Color.black);

    pg.translate(0, -pi * pheight);

    pg.setClip(0, pi * pheight, pwidth, pheight);

    int ypos = hgap;

    do
    {
      for (int i = 0; i < av.getAlignment().getHeight(); i++)
      {
        pg.setFont(getIdPanel().getIdCanvas().getIdfont());
        SequenceI s = av.getAlignment().getSequenceAt(i);
        String string = s.getDisplayId(av.getShowJVSuffix());
        int xPos = 0;
        if (av.isRightAlignIds())
        {
          FontMetrics fm = pg.getFontMetrics();
          xPos = idWidth - fm.stringWidth(string) - 4;
        }
        pg.drawString(string, xPos,
                ((i * av.getCharHeight()) + ypos + av.getCharHeight())
                        - (av.getCharHeight() / 5));
      }
      if (labels != null)
      {
        pg.translate(-3,
                ypos + (av.getAlignment().getHeight() * av.getCharHeight()));

        pg.setFont(av.getFont());
        labels.drawComponent(pg, idWidth);
        pg.translate(
                +3,
                -ypos
                        - (av.getAlignment().getHeight() * av
                                .getCharHeight()));
      }

      ypos += cHeight;
    } while (ypos < totalHeight);

    pg.translate(idWidth, 0);

    getSeqPanel().seqCanvas.drawWrappedPanel(pg, pwidth - idWidth,
            totalHeight, 0);

    if ((pi * pheight) < totalHeight)
    {
      return Printable.PAGE_EXISTS;

    }
    else
    {
      return Printable.NO_SUCH_PAGE;
    }
  }

  /**
   * get current sequence ID panel width, or nominal value if panel were to be
   * displayed using default settings
   * 
   * @return
   */
  public int getVisibleIdWidth()
  {
    return getVisibleIdWidth(true);
  }

  /**
   * get current sequence ID panel width, or nominal value if panel were to be
   * displayed using default settings
   * 
   * @param onscreen
   *          indicate if the Id width for onscreen or offscreen display should
   *          be returned
   * @return
   */
  public int getVisibleIdWidth(boolean onscreen)
  {
    // see if rendering offscreen - check preferences and calc width accordingly
    if (!onscreen && Cache.getDefault("FIGURE_AUTOIDWIDTH", false))
    {
      return calculateIdWidth(-1).width + 4;
    }
    Integer idwidth = null;
    if (onscreen
            || (idwidth = Cache.getIntegerProperty("FIGURE_FIXEDIDWIDTH")) == null)
    {
      int w = getIdPanel().getWidth();
      return (w > 0 ? w : calculateIdWidth().width + 4);
    }
    return idwidth.intValue() + 4;
  }

  void makeAlignmentImage(jalview.util.ImageMaker.TYPE type, File file)
  {
    int boarderBottomOffset = 5;
    long pSessionId = System.currentTimeMillis();
    headless = (System.getProperty("java.awt.headless") != null && System
            .getProperty("java.awt.headless").equals("true"));
    if (alignFrame != null && !headless)
    {
      if (file != null)
      {
        alignFrame.setProgressBar(MessageManager.formatMessage(
                "status.saving_file", new Object[] { type.getLabel() }),
                pSessionId);
      }
    }
    try
    {
      AlignmentDimension aDimension = getAlignmentDimension();
      try
      {
        jalview.util.ImageMaker im;
        final String imageAction, imageTitle;
        if (type == jalview.util.ImageMaker.TYPE.PNG)
        {
          imageAction = "Create PNG image from alignment";
          imageTitle = null;
        }
        else if (type == jalview.util.ImageMaker.TYPE.EPS)
        {
          imageAction = "Create EPS file from alignment";
          imageTitle = alignFrame.getTitle();
        }
        else
        {
          imageAction = "Create SVG file from alignment";
          imageTitle = alignFrame.getTitle();
        }

        im = new jalview.util.ImageMaker(this, type, imageAction,
                aDimension.getWidth(), aDimension.getHeight()
                        + boarderBottomOffset, file, imageTitle,
                alignFrame, pSessionId, headless);
        Graphics graphics = im.getGraphics();
        if (av.getWrapAlignment())
        {
          if (graphics != null)
          {
            printWrappedAlignment(aDimension.getWidth(),
                    aDimension.getHeight() + boarderBottomOffset, 0,
                    graphics);
            im.writeImage();
          }
        }
        else
        {
          if (graphics != null)
          {
            printUnwrapped(aDimension.getWidth(), aDimension.getHeight(),
                    0, graphics, graphics);
            im.writeImage();
          }
        }

      } catch (OutOfMemoryError err)
      {
        // Be noisy here.
        System.out.println("########################\n" + "OUT OF MEMORY "
                + file + "\n" + "########################");
        new OOMWarning("Creating Image for " + file, err);
        // System.out.println("Create IMAGE: " + err);
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    } finally
    {

    }
  }

  public AlignmentDimension getAlignmentDimension()
  {
    int maxwidth = av.getAlignment().getWidth();
    if (av.hasHiddenColumns())
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth);
    }

    int height = ((av.getAlignment().getHeight() + 1) * av.getCharHeight())
            + getScalePanel().getHeight();
    int width = getVisibleIdWidth(false) + (maxwidth * av.getCharWidth());

    if (av.getWrapAlignment())
    {
      height = getWrappedHeight();
      if (headless)
      {
        // need to obtain default alignment width and then add in any
        // additional allowance for id margin
        // this duplicates the calculation in getWrappedHeight but adjusts for
        // offscreen idWith
        width = alignFrame.getWidth() - vscroll.getPreferredSize().width
                - alignFrame.getInsets().left
                - alignFrame.getInsets().right - getVisibleIdWidth()
                + getVisibleIdWidth(false);
      }
      else
      {
        width = getSeqPanel().getWidth() + getVisibleIdWidth(false);
      }

    }
    else if (av.isShowAnnotation())
    {
      height += getAnnotationPanel().adjustPanelHeight() + 3;
    }
    return new AlignmentDimension(width, height);

  }

  /**
   * DOCUMENT ME!
   */
  public void makeEPS(File epsFile)
  {
    makeAlignmentImage(jalview.util.ImageMaker.TYPE.EPS, epsFile);
  }

  /**
   * DOCUMENT ME!
   */
  public void makePNG(File pngFile)
  {
    makeAlignmentImage(jalview.util.ImageMaker.TYPE.PNG, pngFile);
  }

  public void makeSVG(File svgFile)
  {
    makeAlignmentImage(jalview.util.ImageMaker.TYPE.SVG, svgFile);
  }

  public void makePNGImageMap(File imgMapFile, String imageName)
  {
    // /////ONLY WORKS WITH NON WRAPPED ALIGNMENTS
    // ////////////////////////////////////////////
    int idWidth = getVisibleIdWidth(false);
    FontMetrics fm = getFontMetrics(av.getFont());
    int scaleHeight = av.getCharHeight() + fm.getDescent();

    // Gen image map
    // ////////////////////////////////
    if (imgMapFile != null)
    {
      try
      {
        int s, sSize = av.getAlignment().getHeight(), res, alwidth = av
                .getAlignment().getWidth(), g, gSize, f, fSize, sy;
        PrintWriter out = new PrintWriter(new FileWriter(imgMapFile));
        out.println(jalview.io.HTMLOutput.getImageMapHTML());
        out.println("<img src=\"" + imageName
                + "\" border=\"0\" usemap=\"#Map\" >"
                + "<map name=\"Map\">");

        for (s = 0; s < sSize; s++)
        {
          sy = s * av.getCharHeight() + scaleHeight;

          SequenceI seq = av.getAlignment().getSequenceAt(s);
          SequenceFeature[] features = seq.getSequenceFeatures();
          SequenceGroup[] groups = av.getAlignment().findAllGroups(seq);
          for (res = 0; res < alwidth; res++)
          {
            StringBuilder text = new StringBuilder();
            String triplet = null;
            if (av.getAlignment().isNucleotide())
            {
              triplet = ResidueProperties.nucleotideName.get(seq
                      .getCharAt(res) + "");
            }
            else
            {
              triplet = ResidueProperties.aa2Triplet.get(seq.getCharAt(res)
                      + "");
            }

            if (triplet == null)
            {
              continue;
            }

            int alIndex = seq.findPosition(res);
            gSize = groups.length;
            for (g = 0; g < gSize; g++)
            {
              if (text.length() < 1)
              {
                text.append("<area shape=\"rect\" coords=\"")
                        .append((idWidth + res * av.getCharWidth()))
                        .append(",").append(sy).append(",")
                        .append((idWidth + (res + 1) * av.getCharWidth()))
                        .append(",").append((av.getCharHeight() + sy))
                        .append("\"").append(" onMouseOver=\"toolTip('")
                        .append(alIndex).append(" ").append(triplet);
              }

              if (groups[g].getStartRes() < res
                      && groups[g].getEndRes() > res)
              {
                text.append("<br><em>").append(groups[g].getName())
                        .append("</em>");
              }
            }

            if (features != null)
            {
              if (text.length() < 1)
              {
                text.append("<area shape=\"rect\" coords=\"")
                        .append((idWidth + res * av.getCharWidth()))
                        .append(",").append(sy).append(",")
                        .append((idWidth + (res + 1) * av.getCharWidth()))
                        .append(",").append((av.getCharHeight() + sy))
                        .append("\"").append(" onMouseOver=\"toolTip('")
                        .append(alIndex).append(" ").append(triplet);
              }
              fSize = features.length;
              for (f = 0; f < fSize; f++)
              {

                if ((features[f].getBegin() <= seq.findPosition(res))
                        && (features[f].getEnd() >= seq.findPosition(res)))
                {
                  if (features[f].isContactFeature())
                  {
                    if (features[f].getBegin() == seq.findPosition(res)
                            || features[f].getEnd() == seq
                                    .findPosition(res))
                    {
                      text.append("<br>").append(features[f].getType())
                              .append(" ").append(features[f].getBegin())
                              .append(":").append(features[f].getEnd());
                    }
                  }
                  else
                  {
                    text.append("<br>");
                    text.append(features[f].getType());
                    if (features[f].getDescription() != null
                            && !features[f].getType().equals(
                                    features[f].getDescription()))
                    {
                      text.append(" ").append(features[f].getDescription());
                    }

                    if (features[f].getValue("status") != null)
                    {
                      text.append(" (").append(features[f].getValue("status"))
                              .append(")");
                    }
                  }
                }

              }
            }
            if (text.length() > 1)
            {
              text.append("')\"; onMouseOut=\"toolTip()\";  href=\"#\">");
              out.println(text.toString());
            }
          }
        }
        out.println("</map></body></html>");
        out.close();

      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    } // /////////END OF IMAGE MAP

  }

  int getWrappedHeight()
  {
    int seqPanelWidth = getSeqPanel().seqCanvas.getWidth();

    if (System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true"))
    {
      seqPanelWidth = alignFrame.getWidth() - getVisibleIdWidth()
              - vscroll.getPreferredSize().width
              - alignFrame.getInsets().left - alignFrame.getInsets().right;
    }

    int chunkWidth = getSeqPanel().seqCanvas
            .getWrappedCanvasWidth(seqPanelWidth);

    int hgap = av.getCharHeight();
    if (av.getScaleAboveWrapped())
    {
      hgap += av.getCharHeight();
    }

    int annotationHeight = 0;
    if (av.isShowAnnotation())
    {
      annotationHeight = getAnnotationPanel().adjustPanelHeight();
    }

    int cHeight = av.getAlignment().getHeight() * av.getCharHeight() + hgap
            + annotationHeight;

    int maxwidth = av.getAlignment().getWidth();
    if (av.hasHiddenColumns())
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    int height = ((maxwidth / chunkWidth) + 1) * cHeight;

    return height;
  }

  /**
   * close the panel - deregisters all listeners and nulls any references to
   * alignment data.
   */
  public void closePanel()
  {
    PaintRefresher.RemoveComponent(getSeqPanel().seqCanvas);
    PaintRefresher.RemoveComponent(getIdPanel().getIdCanvas());
    PaintRefresher.RemoveComponent(this);

    /*
     * try to ensure references are nulled
     */
    if (annotationPanel != null)
    {
      annotationPanel.dispose();
    }

    if (av != null)
    {
      av.removePropertyChangeListener(propertyChangeListener);
      jalview.structure.StructureSelectionManager ssm = av
              .getStructureSelectionManager();
      ssm.removeStructureViewerListener(getSeqPanel(), null);
      ssm.removeSelectionListener(getSeqPanel());
      ssm.removeCommandListener(av);
      ssm.removeStructureViewerListener(getSeqPanel(), null);
      ssm.removeSelectionListener(getSeqPanel());
      av.dispose();
      av = null;
    }
    else
    {
      if (Cache.log.isDebugEnabled())
      {
        Cache.log.warn("Closing alignment panel which is already closed.");
      }
    }
  }

  /**
   * hides or shows dynamic annotation rows based on groups and av state flags
   */
  public void updateAnnotation()
  {
    updateAnnotation(false, false);
  }

  public void updateAnnotation(boolean applyGlobalSettings)
  {
    updateAnnotation(applyGlobalSettings, false);
  }

  public void updateAnnotation(boolean applyGlobalSettings,
          boolean preserveNewGroupSettings)
  {
    av.updateGroupAnnotationSettings(applyGlobalSettings,
            preserveNewGroupSettings);
    adjustAnnotationHeight();
  }

  @Override
  public AlignmentI getAlignment()
  {
    return av == null ? null : av.getAlignment();
  }

  @Override
  public String getViewName()
  {
    return av.viewName;
  }

  /**
   * Make/Unmake this alignment panel the current input focus
   * 
   * @param b
   */
  public void setSelected(boolean b)
  {
    try
    {
      if (alignFrame.getSplitViewContainer() != null)
      {
        /*
         * bring enclosing SplitFrame to front first if there is one
         */
        ((SplitFrame) alignFrame.getSplitViewContainer()).setSelected(b);
      }
      alignFrame.setSelected(b);
    } catch (Exception ex)
    {
    }

    if (b)
    {
      alignFrame.setDisplayedView(this);
    }
  }

  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return av.getStructureSelectionManager();
  }

  @Override
  public void raiseOOMWarning(String string, OutOfMemoryError error)
  {
    new OOMWarning(string, error, this);
  }

  @Override
  public jalview.api.FeatureRenderer cloneFeatureRenderer()
  {

    return new FeatureRenderer(this);
  }

  @Override
  public jalview.api.FeatureRenderer getFeatureRenderer()
  {
    return seqPanel.seqCanvas.getFeatureRenderer();
  }

  public void updateFeatureRenderer(
          jalview.renderer.seqfeatures.FeatureRenderer fr)
  {
    fr.transferSettings(getSeqPanel().seqCanvas.getFeatureRenderer());
  }

  public void updateFeatureRendererFrom(jalview.api.FeatureRenderer fr)
  {
    if (getSeqPanel().seqCanvas.getFeatureRenderer() != null)
    {
      getSeqPanel().seqCanvas.getFeatureRenderer().transferSettings(fr);
    }
  }

  public ScalePanel getScalePanel()
  {
    return scalePanel;
  }

  public void setScalePanel(ScalePanel scalePanel)
  {
    this.scalePanel = scalePanel;
  }

  public SeqPanel getSeqPanel()
  {
    return seqPanel;
  }

  public void setSeqPanel(SeqPanel seqPanel)
  {
    this.seqPanel = seqPanel;
  }

  public AnnotationPanel getAnnotationPanel()
  {
    return annotationPanel;
  }

  public void setAnnotationPanel(AnnotationPanel annotationPanel)
  {
    this.annotationPanel = annotationPanel;
  }

  public AnnotationLabels getAlabels()
  {
    return alabels;
  }

  public void setAlabels(AnnotationLabels alabels)
  {
    this.alabels = alabels;
  }

  public IdPanel getIdPanel()
  {
    return idPanel;
  }

  public void setIdPanel(IdPanel idPanel)
  {
    this.idPanel = idPanel;
  }

  /**
   * Follow a scrolling change in the (cDNA/Protein) complementary alignment.
   * The aim is to keep the two alignments 'lined up' on their centre columns.
   * 
   * @param sr
   *          holds mapped region(s) of this alignment that we are scrolling
   *          'to'; may be modified for sequence offset by this method
   * @param verticalOffset
   *          the number of visible sequences to show above the mapped region
   */
  public void scrollToCentre(SearchResultsI sr, int verticalOffset)
  {
    /*
     * To avoid jumpy vertical scrolling (if some sequences are gapped or not
     * mapped), we can make the scroll-to location a sequence above the one
     * actually mapped.
     */
    SequenceI mappedTo = sr.getResults().get(0).getSequence();
    List<SequenceI> seqs = av.getAlignment().getSequences();

    /*
     * This is like AlignmentI.findIndex(seq) but here we are matching the
     * dataset sequence not the aligned sequence
     */
    boolean matched = false;
    for (SequenceI seq : seqs)
    {
      if (mappedTo == seq.getDatasetSequence())
      {
        matched = true;
        break;
      }
    }
    if (!matched)
    {
      return; // failsafe, shouldn't happen
    }

    /*
     * Scroll to position but centring the target residue.
     */
    scrollToPosition(sr, verticalOffset, true, true);
  }

  /**
   * Set a flag to say do not scroll any (cDNA/protein) complement.
   * 
   * @param b
   */
  protected void setDontScrollComplement(boolean b)
  {
    this.dontScrollComplement = b;
  }

  protected boolean isDontScrollComplement()
  {
    return this.dontScrollComplement;
  }
}
