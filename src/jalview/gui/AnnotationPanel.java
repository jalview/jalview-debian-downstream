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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceI;
import jalview.renderer.AnnotationRenderer;
import jalview.renderer.AwtRenderPanelI;
import jalview.schemes.ResidueProperties;
import jalview.util.Comparison;
import jalview.util.MessageManager;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;
import javax.swing.ToolTipManager;

/**
 * AnnotationPanel displays visible portion of annotation rows below unwrapped
 * alignment
 * 
 * @author $author$
 * @version $Revision$
 */
public class AnnotationPanel extends JPanel implements AwtRenderPanelI,
        MouseListener, MouseWheelListener, MouseMotionListener,
        ActionListener, AdjustmentListener, Scrollable
{
  String HELIX = MessageManager.getString("label.helix");

  String SHEET = MessageManager.getString("label.sheet");

  /**
   * For RNA secondary structure "stems" aka helices
   */
  String STEM = MessageManager.getString("label.rna_helix");

  String LABEL = MessageManager.getString("label.label");

  String REMOVE = MessageManager.getString("label.remove_annotation");

  String COLOUR = MessageManager.getString("action.colour");

  public final Color HELIX_COLOUR = Color.red.darker();

  public final Color SHEET_COLOUR = Color.green.darker().darker();

  public final Color STEM_COLOUR = Color.blue.darker();

  /** DOCUMENT ME!! */
  public AlignViewport av;

  AlignmentPanel ap;

  public int activeRow = -1;

  public BufferedImage image;

  public volatile BufferedImage fadedImage;

  Graphics2D gg;

  public FontMetrics fm;

  public int imgWidth = 0;

  boolean fastPaint = false;

  // Used For mouse Dragging and resizing graphs
  int graphStretch = -1;

  int graphStretchY = -1;

  int min; // used by mouseDragged to see if user

  int max; // used by mouseDragged to see if user

  boolean mouseDragging = false;

  // for editing cursor
  int cursorX = 0;

  int cursorY = 0;

  public final AnnotationRenderer renderer;

  private MouseWheelListener[] _mwl;

  /**
   * Creates a new AnnotationPanel object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public AnnotationPanel(AlignmentPanel ap)
  {
    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);
    this.ap = ap;
    av = ap.av;
    this.setLayout(null);
    addMouseListener(this);
    addMouseMotionListener(this);
    ap.annotationScroller.getVerticalScrollBar()
            .addAdjustmentListener(this);
    // save any wheel listeners on the scroller, so we can propagate scroll
    // events to them.
    _mwl = ap.annotationScroller.getMouseWheelListeners();
    // and then set our own listener to consume all mousewheel events
    ap.annotationScroller.addMouseWheelListener(this);
    renderer = new AnnotationRenderer();
  }

  public AnnotationPanel(AlignViewport av)
  {
    this.av = av;
    renderer = new AnnotationRenderer();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    if (e.isShiftDown())
    {
      e.consume();
      if (e.getWheelRotation() > 0)
      {
        ap.scrollRight(true);
      }
      else
      {
        ap.scrollRight(false);
      }
    }
    else
    {
      // TODO: find the correct way to let the event bubble up to
      // ap.annotationScroller
      for (MouseWheelListener mwl : _mwl)
      {
        if (mwl != null)
        {
          mwl.mouseWheelMoved(e);
        }
        if (e.isConsumed())
        {
          break;
        }
      }
    }
  }

  @Override
  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect,
          int orientation, int direction)
  {
    return 30;
  }

  @Override
  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  @Override
  public boolean getScrollableTracksViewportWidth()
  {
    return true;
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect,
          int orientation, int direction)
  {
    return 30;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event
   * .AdjustmentEvent)
   */
  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    // update annotation label display
    ap.getAlabels().setScrollOffset(-evt.getValue());
  }

  /**
   * Calculates the height of the annotation displayed in the annotation panel.
   * Callers should normally call the ap.adjustAnnotationHeight method to ensure
   * all annotation associated components are updated correctly.
   * 
   */
  public int adjustPanelHeight()
  {
    int height = av.calcPanelHeight();
    this.setPreferredSize(new Dimension(1, height));
    if (ap != null)
    {
      // revalidate only when the alignment panel is fully constructed
      ap.validate();
    }

    return height;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }
    Annotation[] anot = aa[activeRow].annotations;

    if (anot.length < av.getColumnSelection().getMax())
    {
      Annotation[] temp = new Annotation[av.getColumnSelection().getMax() + 2];
      System.arraycopy(anot, 0, temp, 0, anot.length);
      anot = temp;
      aa[activeRow].annotations = anot;
    }

    String action = evt.getActionCommand();
    if (action.equals(REMOVE))
    {
      for (int index : av.getColumnSelection().getSelected())
      {
        if (av.getColumnSelection().isVisible(index))
        {
          anot[index] = null;
        }
      }
    }
    else if (action.equals(LABEL))
    {
      String exMesg = collectAnnotVals(anot, LABEL);
      String label = JOptionPane.showInputDialog(this,
              MessageManager.getString("label.enter_label"), exMesg);

      if (label == null)
      {
        return;
      }

      if ((label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
      }

      for (int index : av.getColumnSelection().getSelected())
      {
        if (!av.getColumnSelection().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", ' ', 0);
        }
        else
        {
          anot[index].displayCharacter = label;
        }
      }
    }
    else if (action.equals(COLOUR))
    {
      Color col = JColorChooser.showDialog(this,
              MessageManager.getString("label.select_foreground_colour"),
              Color.black);

      for (int index : av.getColumnSelection().getSelected())
      {
        if (!av.getColumnSelection().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation("", "", ' ', 0);
        }

        anot[index].colour = col;
      }
    }
    else
    // HELIX, SHEET or STEM
    {
      char type = 0;
      String symbol = "\u03B1"; // alpha

      if (action.equals(HELIX))
      {
        type = 'H';
      }
      else if (action.equals(SHEET))
      {
        type = 'E';
        symbol = "\u03B2"; // beta
      }

      // Added by LML to color stems
      else if (action.equals(STEM))
      {
        type = 'S';
        int column = av.getColumnSelection().getSelectedRanges().get(0)[0];
        symbol = aa[activeRow].getDefaultRnaHelixSymbol(column);
      }

      if (!aa[activeRow].hasIcons)
      {
        aa[activeRow].hasIcons = true;
      }

      String label = JOptionPane.showInputDialog(MessageManager
              .getString("label.enter_label_for_the_structure"), symbol);

      if (label == null)
      {
        return;
      }

      if ((label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
        if (action.equals(STEM))
        {
          aa[activeRow].showAllColLabels = true;
        }
      }
      for (int index : av.getColumnSelection().getSelected())
      {
        if (!av.getColumnSelection().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", type, 0);
        }

        anot[index].secondaryStructure = type != 'S' ? type : label
                .length() == 0 ? ' ' : label.charAt(0);
        anot[index].displayCharacter = label;

      }
    }

    av.getAlignment().validateAnnotation(aa[activeRow]);
    ap.alignmentChanged();
    ap.alignFrame.setMenusForViewport();
    adjustPanelHeight();
    repaint();

    return;
  }

  /**
   * Returns any existing annotation concatenated as a string. For each
   * annotation, takes the description, if any, else the secondary structure
   * character (if type is HELIX, SHEET or STEM), else the display character (if
   * type is LABEL).
   * 
   * @param anots
   * @param type
   * @return
   */
  private String collectAnnotVals(Annotation[] anots, String type)
  {
    // TODO is this method wanted? why? 'last' is not used

    StringBuilder collatedInput = new StringBuilder(64);
    String last = "";
    ColumnSelection viscols = av.getColumnSelection();

    /*
     * the selection list (read-only view) is in selection order, not
     * column order; make a copy so we can sort it
     */
    List<Integer> selected = new ArrayList<Integer>(viscols.getSelected());
    Collections.sort(selected);
    for (int index : selected)
    {
      // always check for current display state - just in case
      if (!viscols.isVisible(index))
      {
        continue;
      }
      String tlabel = null;
      if (anots[index] != null)
      { // LML added stem code
        if (type.equals(HELIX) || type.equals(SHEET) || type.equals(STEM)
                || type.equals(LABEL))
        {
          tlabel = anots[index].description;
          if (tlabel == null || tlabel.length() < 1)
          {
            if (type.equals(HELIX) || type.equals(SHEET)
                    || type.equals(STEM))
            {
              tlabel = "" + anots[index].secondaryStructure;
            }
            else
            {
              tlabel = "" + anots[index].displayCharacter;
            }
          }
        }
        if (tlabel != null && !tlabel.equals(last))
        {
          if (last.length() > 0)
          {
            collatedInput.append(" ");
          }
          collatedInput.append(tlabel);
        }
      }
    }
    return collatedInput.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mousePressed(MouseEvent evt)
  {

    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }

    int height = 0;
    activeRow = -1;

    final int y = evt.getY();
    for (int i = 0; i < aa.length; i++)
    {
      if (aa[i].visible)
      {
        height += aa[i].height;
      }

      if (y < height)
      {
        if (aa[i].editable)
        {
          activeRow = i;
        }
        else if (aa[i].graph > 0)
        {
          // Stretch Graph
          graphStretch = i;
          graphStretchY = y;
        }

        break;
      }
    }

    /*
     * isPopupTrigger fires in mousePressed on Mac,
     * not until mouseRelease on Windows
     */
    if (evt.isPopupTrigger() && activeRow != -1)
    {
      showPopupMenu(y, evt.getX());
      return;
    }

    ap.getScalePanel().mousePressed(evt);
  }

  /**
   * Construct and display a context menu at the right-click position
   * 
   * @param y
   * @param x
   */
  void showPopupMenu(final int y, int x)
  {
    if (av.getColumnSelection() == null
            || av.getColumnSelection().isEmpty())
    {
      return;
    }

    JPopupMenu pop = new JPopupMenu(
            MessageManager.getString("label.structure_type"));
    JMenuItem item;
    /*
     * Just display the needed structure options
     */
    if (av.getAlignment().isNucleotide())
    {
      item = new JMenuItem(STEM);
      item.addActionListener(this);
      pop.add(item);
    }
    else
    {
      item = new JMenuItem(HELIX);
      item.addActionListener(this);
      pop.add(item);
      item = new JMenuItem(SHEET);
      item.addActionListener(this);
      pop.add(item);
    }
    item = new JMenuItem(LABEL);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(COLOUR);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(REMOVE);
    item.addActionListener(this);
    pop.add(item);
    pop.show(this, x, y);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseReleased(MouseEvent evt)
  {
    graphStretch = -1;
    graphStretchY = -1;
    mouseDragging = false;
    ap.getScalePanel().mouseReleased(evt);

    /*
     * isPopupTrigger is set in mouseReleased on Windows
     * (in mousePressed on Mac)
     */
    if (evt.isPopupTrigger() && activeRow != -1)
    {
      showPopupMenu(evt.getY(), evt.getX());
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseEntered(MouseEvent evt)
  {
    ap.getScalePanel().mouseEntered(evt);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseExited(MouseEvent evt)
  {
    ap.getScalePanel().mouseExited(evt);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseDragged(MouseEvent evt)
  {
    if (graphStretch > -1)
    {
      av.getAlignment().getAlignmentAnnotation()[graphStretch].graphHeight += graphStretchY
              - evt.getY();
      if (av.getAlignment().getAlignmentAnnotation()[graphStretch].graphHeight < 0)
      {
        av.getAlignment().getAlignmentAnnotation()[graphStretch].graphHeight = 0;
      }
      graphStretchY = evt.getY();
      adjustPanelHeight();
      ap.paintAlignment(true);
    }
    else
    {
      ap.getScalePanel().mouseDragged(evt);
    }
  }

  /**
   * Constructs the tooltip, and constructs and displays a status message, for
   * the current mouse position
   * 
   * @param evt
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();

    if (aa == null)
    {
      this.setToolTipText(null);
      return;
    }

    int row = -1;
    int height = 0;

    for (int i = 0; i < aa.length; i++)
    {
      if (aa[i].visible)
      {
        height += aa[i].height;
      }

      if (evt.getY() < height)
      {
        row = i;
        break;
      }
    }

    if (row == -1)
    {
      this.setToolTipText(null);
      return;
    }

    int column = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    if (av.hasHiddenColumns())
    {
      column = av.getColumnSelection().adjustForHiddenColumns(column);
    }

    AlignmentAnnotation ann = aa[row];
    if (row > -1 && ann.annotations != null
            && column < ann.annotations.length)
    {
      buildToolTip(ann, column, aa);
      setStatusMessage(column, ann);
    }
    else
    {
      this.setToolTipText(null);
      ap.alignFrame.statusBar.setText(" ");
    }
  }

  /**
   * Builds a tooltip for the annotation at the current mouse position.
   * 
   * @param ann
   * @param column
   * @param anns
   */
  void buildToolTip(AlignmentAnnotation ann, int column,
          AlignmentAnnotation[] anns)
  {
    if (ann.graphGroup > -1)
    {
      StringBuilder tip = new StringBuilder(32);
      tip.append("<html>");
      for (int i = 0; i < anns.length; i++)
      {
        if (anns[i].graphGroup == ann.graphGroup
                && anns[i].annotations[column] != null)
        {
          tip.append(anns[i].label);
          String description = anns[i].annotations[column].description;
          if (description != null && description.length() > 0)
          {
            tip.append(" ").append(description);
          }
          tip.append("<br>");
        }
      }
      if (tip.length() != 6)
      {
        tip.setLength(tip.length() - 4);
        this.setToolTipText(tip.toString() + "</html>");
      }
    }
    else if (ann.annotations[column] != null)
    {
      String description = ann.annotations[column].description;
      if (description != null && description.length() > 0)
      {
        this.setToolTipText(JvSwingUtils.wrapTooltip(true, description));
      }
    }
    else
    {
      // clear the tooltip.
      this.setToolTipText(null);
    }
  }

  /**
   * Constructs and displays the status bar message
   * 
   * @param column
   * @param ann
   */
  void setStatusMessage(int column, AlignmentAnnotation ann)
  {
    /*
     * show alignment column and annotation description if any
     */
    StringBuilder text = new StringBuilder(32);
    text.append(MessageManager.getString("label.column")).append(" ")
            .append(column + 1);

    if (ann.annotations[column] != null)
    {
      String description = ann.annotations[column].description;
      if (description != null && description.trim().length() > 0)
      {
        text.append("  ").append(description);
      }
    }

    /*
     * if the annotation is sequence-specific, show the sequence number
     * in the alignment, and (if not a gap) the residue and position
     */
    SequenceI seqref = ann.sequenceRef;
    if (seqref != null)
    {
      int seqIndex = av.getAlignment().findIndex(seqref);
      if (seqIndex != -1)
      {
        text.append(", ")
                .append(MessageManager.getString("label.sequence"))
                .append(" ").append(seqIndex + 1);
        char residue = seqref.getCharAt(column);
        if (!Comparison.isGap(residue))
        {
          text.append(" ");
          String name;
          if (av.getAlignment().isNucleotide())
          {
            name = ResidueProperties.nucleotideName.get(String
                    .valueOf(residue));
            text.append(" Nucleotide: ").append(
                    name != null ? name : residue);
          }
          else
          {
            name = 'X' == residue ? "X" : ('*' == residue ? "STOP"
                    : ResidueProperties.aa2Triplet.get(String
                            .valueOf(residue)));
            text.append(" Residue: ").append(name != null ? name : residue);
          }
          int residuePos = seqref.findPosition(column);
          text.append(" (").append(residuePos).append(")");
        }
      }
    }

    ap.alignFrame.statusBar.setText(text.toString());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseClicked(MouseEvent evt)
  {
    // if (activeRow != -1)
    // {
    // AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    // AlignmentAnnotation anot = aa[activeRow];
    // }
  }

  // TODO mouseClicked-content and drawCursor are quite experimental!
  public void drawCursor(Graphics graphics, SequenceI seq, int res, int x1,
          int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, av.getCharWidth(), av.getCharHeight());

    if (av.validCharWidth)
    {
      graphics.setColor(Color.white);

      char s = seq.getCharAt(res);

      charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s), charOffset + x1,
              (y1 + av.getCharHeight()) - pady);
    }

  }

  private volatile boolean imageFresh = false;

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (image != null)
    {
      if (fastPaint || (getVisibleRect().width != g.getClipBounds().width)
              || (getVisibleRect().height != g.getClipBounds().height))
      {
        g.drawImage(image, 0, 0, this);
        fastPaint = false;
        return;
      }
    }
    imgWidth = (av.endRes - av.startRes + 1) * av.getCharWidth();
    if (imgWidth < 1)
    {
      return;
    }
    if (image == null || imgWidth != image.getWidth(this)
            || image.getHeight(this) != getHeight())
    {
      try
      {
        image = new BufferedImage(imgWidth, ap.getAnnotationPanel()
                .getHeight(), BufferedImage.TYPE_INT_RGB);
      } catch (OutOfMemoryError oom)
      {
        try
        {
          System.gc();
        } catch (Exception x)
        {
        }
        ;
        new OOMWarning(
                "Couldn't allocate memory to redraw screen. Please restart Jalview",
                oom);
        return;
      }
      gg = (Graphics2D) image.getGraphics();

      if (av.antiAlias)
      {
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
      }

      gg.setFont(av.getFont());
      fm = gg.getFontMetrics();
      gg.setColor(Color.white);
      gg.fillRect(0, 0, imgWidth, image.getHeight());
      imageFresh = true;
    }

    drawComponent(gg, av.startRes, av.endRes + 1);
    imageFresh = false;
    g.drawImage(image, 0, 0, this);
  }

  /**
   * set true to enable redraw timing debug output on stderr
   */
  private final boolean debugRedraw = false;

  /**
   * non-Thread safe repaint
   * 
   * @param horizontal
   *          repaint with horizontal shift in alignment
   */
  public void fastPaint(int horizontal)
  {
    if ((horizontal == 0) || gg == null
            || av.getAlignment().getAlignmentAnnotation() == null
            || av.getAlignment().getAlignmentAnnotation().length < 1
            || av.isCalcInProgress())
    {
      repaint();
      return;
    }
    long stime = System.currentTimeMillis();
    gg.copyArea(0, 0, imgWidth, getHeight(),
            -horizontal * av.getCharWidth(), 0);
    long mtime = System.currentTimeMillis();
    int sr = av.startRes;
    int er = av.endRes + 1;
    int transX = 0;

    if (horizontal > 0) // scrollbar pulled right, image to the left
    {
      transX = (er - sr - horizontal) * av.getCharWidth();
      sr = er - horizontal;
    }
    else if (horizontal < 0)
    {
      er = sr - horizontal;
    }

    gg.translate(transX, 0);

    drawComponent(gg, sr, er);

    gg.translate(-transX, 0);
    long dtime = System.currentTimeMillis();
    fastPaint = true;
    repaint();
    long rtime = System.currentTimeMillis();
    if (debugRedraw)
    {
      System.err.println("Scroll:\t" + horizontal + "\tCopyArea:\t"
              + (mtime - stime) + "\tDraw component:\t" + (dtime - mtime)
              + "\tRepaint call:\t" + (rtime - dtime));
    }

  }

  private volatile boolean lastImageGood = false;

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param startRes
   *          DOCUMENT ME!
   * @param endRes
   *          DOCUMENT ME!
   */
  public void drawComponent(Graphics g, int startRes, int endRes)
  {
    BufferedImage oldFaded = fadedImage;
    if (av.isCalcInProgress())
    {
      if (image == null)
      {
        lastImageGood = false;
        return;
      }
      // We'll keep a record of the old image,
      // and draw a faded image until the calculation
      // has completed
      if (lastImageGood
              && (fadedImage == null || fadedImage.getWidth() != imgWidth || fadedImage
                      .getHeight() != image.getHeight()))
      {
        // System.err.println("redraw faded image ("+(fadedImage==null ?
        // "null image" : "") + " lastGood="+lastImageGood+")");
        fadedImage = new BufferedImage(imgWidth, image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D fadedG = (Graphics2D) fadedImage.getGraphics();

        fadedG.setColor(Color.white);
        fadedG.fillRect(0, 0, imgWidth, image.getHeight());

        fadedG.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, .3f));
        fadedG.drawImage(image, 0, 0, this);

      }
      // make sure we don't overwrite the last good faded image until all
      // calculations have finished
      lastImageGood = false;

    }
    else
    {
      if (fadedImage != null)
      {
        oldFaded = fadedImage;
      }
      fadedImage = null;
    }

    g.setColor(Color.white);
    g.fillRect(0, 0, (endRes - startRes) * av.getCharWidth(), getHeight());

    g.setFont(av.getFont());
    if (fm == null)
    {
      fm = g.getFontMetrics();
    }

    if ((av.getAlignment().getAlignmentAnnotation() == null)
            || (av.getAlignment().getAlignmentAnnotation().length < 1))
    {
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.black);
      if (av.validCharWidth)
      {
        g.drawString(MessageManager
                .getString("label.alignment_has_no_annotations"), 20, 15);
      }

      return;
    }
    lastImageGood = renderer.drawComponent(this, av, g, activeRow,
            startRes, endRes);
    if (!lastImageGood && fadedImage == null)
    {
      fadedImage = oldFaded;
    }
  }

  @Override
  public FontMetrics getFontMetrics()
  {
    return fm;
  }

  @Override
  public Image getFadedImage()
  {
    return fadedImage;
  }

  @Override
  public int getFadedImageWidth()
  {
    return imgWidth;
  }

  private int[] bounds = new int[2];

  @Override
  public int[] getVisibleVRange()
  {
    if (ap != null && ap.getAlabels() != null)
    {
      int sOffset = -ap.getAlabels().getScrollOffset();
      int visHeight = sOffset + ap.annotationSpaceFillerHolder.getHeight();
      bounds[0] = sOffset;
      bounds[1] = visHeight;
      return bounds;
    }
    else
    {
      return null;
    }
  }

  /**
   * Try to ensure any references held are nulled
   */
  public void dispose()
  {
    av = null;
    ap = null;
    image = null;
    fadedImage = null;
    gg = null;
    _mwl = null;

    /*
     * I created the renderer so I will dispose of it
     */
    if (renderer != null)
    {
      renderer.dispose();
    }
  }
}
