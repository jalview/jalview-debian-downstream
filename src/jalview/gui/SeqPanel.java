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

import jalview.api.AlignViewportI;
import jalview.bin.Cache;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.SequenceAnnotationReport;
import jalview.schemes.ResidueProperties;
import jalview.structure.SelectionListener;
import jalview.structure.SelectionSource;
import jalview.structure.SequenceListener;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.Comparison;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.130 $
 */
public class SeqPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener, SequenceListener,
        SelectionListener

{
  /** DOCUMENT ME!! */
  public SeqCanvas seqCanvas;

  /** DOCUMENT ME!! */
  public AlignmentPanel ap;

  protected int lastres;

  protected int startseq;

  protected AlignViewport av;

  ScrollThread scrollThread = null;

  boolean mouseDragging = false;

  boolean editingSeqs = false;

  boolean groupEditing = false;

  // ////////////////////////////////////////
  // ///Everything below this is for defining the boundary of the rubberband
  // ////////////////////////////////////////
  int oldSeq = -1;

  boolean changeEndSeq = false;

  boolean changeStartSeq = false;

  boolean changeEndRes = false;

  boolean changeStartRes = false;

  SequenceGroup stretchGroup = null;

  boolean remove = false;

  Point lastMousePress;

  boolean mouseWheelPressed = false;

  StringBuffer keyboardNo1;

  StringBuffer keyboardNo2;

  java.net.URL linkImageURL;

  private final SequenceAnnotationReport seqARep;

  StringBuilder tooltipText = new StringBuilder();

  String tmpString;

  EditCommand editCommand;

  StructureSelectionManager ssm;

  SearchResultsI lastSearchResults;

  /**
   * Creates a new SeqPanel object.
   * 
   * @param avp
   *          DOCUMENT ME!
   * @param p
   *          DOCUMENT ME!
   */
  public SeqPanel(AlignViewport av, AlignmentPanel ap)
  {
    linkImageURL = getClass().getResource("/images/link.gif");
    seqARep = new SequenceAnnotationReport(linkImageURL.toString());
    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);
    this.av = av;
    setBackground(Color.white);

    seqCanvas = new SeqCanvas(ap);
    setLayout(new BorderLayout());
    add(seqCanvas, BorderLayout.CENTER);

    this.ap = ap;

    if (!av.isDataset())
    {
      addMouseMotionListener(this);
      addMouseListener(this);
      addMouseWheelListener(this);
      ssm = av.getStructureSelectionManager();
      ssm.addStructureViewerListener(this);
      ssm.addSelectionListener(this);
    }
  }

  int startWrapBlock = -1;

  int wrappedBlock = -1;

  /**
   * Returns the aligned sequence position (base 0) at the mouse position, or
   * the closest visible one
   * 
   * @param evt
   * @return
   */
  int findRes(MouseEvent evt)
  {
    int res = 0;
    int x = evt.getX();

    if (av.getWrapAlignment())
    {

      int hgap = av.getCharHeight();
      if (av.getScaleAboveWrapped())
      {
        hgap += av.getCharHeight();
      }

      int cHeight = av.getAlignment().getHeight() * av.getCharHeight()
              + hgap + seqCanvas.getAnnotationHeight();

      int y = evt.getY();
      y -= hgap;
      x -= seqCanvas.LABEL_WEST;

      int cwidth = seqCanvas.getWrappedCanvasWidth(this.getWidth());
      if (cwidth < 1)
      {
        return 0;
      }

      wrappedBlock = y / cHeight;
      wrappedBlock += av.getStartRes() / cwidth;

      res = wrappedBlock * cwidth + x / av.getCharWidth();

    }
    else
    {
      if (x > seqCanvas.getX() + seqCanvas.getWidth())
      {
        // make sure we calculate relative to visible alignment, rather than
        // right-hand gutter
        x = seqCanvas.getX() + seqCanvas.getWidth();
      }
      res = (x / av.getCharWidth()) + av.getStartRes();
      if (res > av.getEndRes())
      {
        // moused off right
        res = av.getEndRes();
      }
    }

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    return res;

  }

  int findSeq(MouseEvent evt)
  {
    int seq = 0;
    int y = evt.getY();

    if (av.getWrapAlignment())
    {
      int hgap = av.getCharHeight();
      if (av.getScaleAboveWrapped())
      {
        hgap += av.getCharHeight();
      }

      int cHeight = av.getAlignment().getHeight() * av.getCharHeight()
              + hgap + seqCanvas.getAnnotationHeight();

      y -= hgap;

      seq = Math.min((y % cHeight) / av.getCharHeight(), av.getAlignment()
              .getHeight() - 1);
    }
    else
    {
      seq = Math.min((y / av.getCharHeight()) + av.getStartSeq(), av
              .getAlignment().getHeight() - 1);
    }

    return seq;
  }

  /**
   * When all of a sequence of edits are complete, put the resulting edit list
   * on the history stack (undo list), and reset flags for editing in progress.
   */
  void endEditing()
  {
    try
    {
      if (editCommand != null && editCommand.getSize() > 0)
      {
        ap.alignFrame.addHistoryItem(editCommand);
        av.firePropertyChange("alignment", null, av.getAlignment()
                .getSequences());
      }
    } finally
    {
      /*
       * Tidy up come what may...
       */
      startseq = -1;
      lastres = -1;
      editingSeqs = false;
      groupEditing = false;
      keyboardNo1 = null;
      keyboardNo2 = null;
      editCommand = null;
    }
  }

  void setCursorRow()
  {
    seqCanvas.cursorY = getKeyboardNo1() - 1;
    scrollToVisible();
  }

  void setCursorColumn()
  {
    seqCanvas.cursorX = getKeyboardNo1() - 1;
    scrollToVisible();
  }

  void setCursorRowAndColumn()
  {
    if (keyboardNo2 == null)
    {
      keyboardNo2 = new StringBuffer();
    }
    else
    {
      seqCanvas.cursorX = getKeyboardNo1() - 1;
      seqCanvas.cursorY = getKeyboardNo2() - 1;
      scrollToVisible();
    }
  }

  void setCursorPosition()
  {
    SequenceI sequence = av.getAlignment().getSequenceAt(seqCanvas.cursorY);

    seqCanvas.cursorX = sequence.findIndex(getKeyboardNo1()) - 1;
    scrollToVisible();
  }

  void moveCursor(int dx, int dy)
  {
    seqCanvas.cursorX += dx;
    seqCanvas.cursorY += dy;
    if (av.hasHiddenColumns()
            && !av.getColumnSelection().isVisible(seqCanvas.cursorX))
    {
      int original = seqCanvas.cursorX - dx;
      int maxWidth = av.getAlignment().getWidth();

      while (!av.getColumnSelection().isVisible(seqCanvas.cursorX)
              && seqCanvas.cursorX < maxWidth && seqCanvas.cursorX > 0)
      {
        seqCanvas.cursorX += dx;
      }

      if (seqCanvas.cursorX >= maxWidth
              || !av.getColumnSelection().isVisible(seqCanvas.cursorX))
      {
        seqCanvas.cursorX = original;
      }
    }

    scrollToVisible();
  }

  void scrollToVisible()
  {
    if (seqCanvas.cursorX < 0)
    {
      seqCanvas.cursorX = 0;
    }
    else if (seqCanvas.cursorX > av.getAlignment().getWidth() - 1)
    {
      seqCanvas.cursorX = av.getAlignment().getWidth() - 1;
    }

    if (seqCanvas.cursorY < 0)
    {
      seqCanvas.cursorY = 0;
    }
    else if (seqCanvas.cursorY > av.getAlignment().getHeight() - 1)
    {
      seqCanvas.cursorY = av.getAlignment().getHeight() - 1;
    }

    endEditing();
    if (av.getWrapAlignment())
    {
      ap.scrollToWrappedVisible(seqCanvas.cursorX);
    }
    else
    {
      while (seqCanvas.cursorY < av.startSeq)
      {
        ap.scrollUp(true);
      }
      while (seqCanvas.cursorY + 1 > av.endSeq)
      {
        ap.scrollUp(false);
      }
      if (!av.getWrapAlignment())
      {
        while (seqCanvas.cursorX < av.getColumnSelection()
                .adjustForHiddenColumns(av.startRes))
        {
          if (!ap.scrollRight(false))
          {
            break;
          }
        }
        while (seqCanvas.cursorX > av.getColumnSelection()
                .adjustForHiddenColumns(av.endRes))
        {
          if (!ap.scrollRight(true))
          {
            break;
          }
        }
      }
    }
    setStatusMessage(av.getAlignment().getSequenceAt(seqCanvas.cursorY),
            seqCanvas.cursorX, seqCanvas.cursorY);

    seqCanvas.repaint();
  }

  void setSelectionAreaAtCursor(boolean topLeft)
  {
    SequenceI sequence = av.getAlignment().getSequenceAt(seqCanvas.cursorY);

    if (av.getSelectionGroup() != null)
    {
      SequenceGroup sg = av.getSelectionGroup();
      // Find the top and bottom of this group
      int min = av.getAlignment().getHeight(), max = 0;
      for (int i = 0; i < sg.getSize(); i++)
      {
        int index = av.getAlignment().findIndex(sg.getSequenceAt(i));
        if (index > max)
        {
          max = index;
        }
        if (index < min)
        {
          min = index;
        }
      }

      max++;

      if (topLeft)
      {
        sg.setStartRes(seqCanvas.cursorX);
        if (sg.getEndRes() < seqCanvas.cursorX)
        {
          sg.setEndRes(seqCanvas.cursorX);
        }

        min = seqCanvas.cursorY;
      }
      else
      {
        sg.setEndRes(seqCanvas.cursorX);
        if (sg.getStartRes() > seqCanvas.cursorX)
        {
          sg.setStartRes(seqCanvas.cursorX);
        }

        max = seqCanvas.cursorY + 1;
      }

      if (min > max)
      {
        // Only the user can do this
        av.setSelectionGroup(null);
      }
      else
      {
        // Now add any sequences between min and max
        sg.getSequences(null).clear();
        for (int i = min; i < max; i++)
        {
          sg.addSequence(av.getAlignment().getSequenceAt(i), false);
        }
      }
    }

    if (av.getSelectionGroup() == null)
    {
      SequenceGroup sg = new SequenceGroup();
      sg.setStartRes(seqCanvas.cursorX);
      sg.setEndRes(seqCanvas.cursorX);
      sg.addSequence(sequence, false);
      av.setSelectionGroup(sg);
    }

    ap.paintAlignment(false);
    av.sendSelection();
  }

  void insertGapAtCursor(boolean group)
  {
    groupEditing = group;
    startseq = seqCanvas.cursorY;
    lastres = seqCanvas.cursorX;
    editSequence(true, false, seqCanvas.cursorX + getKeyboardNo1());
    endEditing();
  }

  void deleteGapAtCursor(boolean group)
  {
    groupEditing = group;
    startseq = seqCanvas.cursorY;
    lastres = seqCanvas.cursorX + getKeyboardNo1();
    editSequence(false, false, seqCanvas.cursorX);
    endEditing();
  }

  void insertNucAtCursor(boolean group, String nuc)
  {
    // TODO not called - delete?
    groupEditing = group;
    startseq = seqCanvas.cursorY;
    lastres = seqCanvas.cursorX;
    editSequence(false, true, seqCanvas.cursorX + getKeyboardNo1());
    endEditing();
  }

  void numberPressed(char value)
  {
    if (keyboardNo1 == null)
    {
      keyboardNo1 = new StringBuffer();
    }

    if (keyboardNo2 != null)
    {
      keyboardNo2.append(value);
    }
    else
    {
      keyboardNo1.append(value);
    }
  }

  int getKeyboardNo1()
  {
    try
    {
      if (keyboardNo1 != null)
      {
        int value = Integer.parseInt(keyboardNo1.toString());
        keyboardNo1 = null;
        return value;
      }
    } catch (Exception x)
    {
    }
    keyboardNo1 = null;
    return 1;
  }

  int getKeyboardNo2()
  {
    try
    {
      if (keyboardNo2 != null)
      {
        int value = Integer.parseInt(keyboardNo2.toString());
        keyboardNo2 = null;
        return value;
      }
    } catch (Exception x)
    {
    }
    keyboardNo2 = null;
    return 1;
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
    mouseDragging = false;
    mouseWheelPressed = false;

    if (evt.isPopupTrigger()) // Windows: mouseReleased
    {
      showPopupMenu(evt);
      evt.consume();
      return;
    }

    if (!editingSeqs)
    {
      doMouseReleasedDefineMode(evt);
      return;
    }

    endEditing();
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
    lastMousePress = evt.getPoint();

    if (SwingUtilities.isMiddleMouseButton(evt))
    {
      mouseWheelPressed = true;
      return;
    }

    boolean isControlDown = Platform.isControlDown(evt);
    if (evt.isShiftDown() || isControlDown)
    {
      editingSeqs = true;
      if (isControlDown)
      {
        groupEditing = true;
      }
    }
    else
    {
      doMousePressedDefineMode(evt);
      return;
    }

    int seq = findSeq(evt);
    int res = findRes(evt);

    if (seq < 0 || res < 0)
    {
      return;
    }

    if ((seq < av.getAlignment().getHeight())
            && (res < av.getAlignment().getSequenceAt(seq).getLength()))
    {
      startseq = seq;
      lastres = res;
    }
    else
    {
      startseq = -1;
      lastres = -1;
    }

    return;
  }

  String lastMessage;

  @Override
  public void mouseOverSequence(SequenceI sequence, int index, int pos)
  {
    String tmp = sequence.hashCode() + " " + index + " " + pos;

    if (lastMessage == null || !lastMessage.equals(tmp))
    {
      // System.err.println("mouseOver Sequence: "+tmp);
      ssm.mouseOverSequence(sequence, index, pos, av);
    }
    lastMessage = tmp;
  }

  /**
   * Highlight the mapped region described by the search results object (unless
   * unchanged). This supports highlight of protein while mousing over linked
   * cDNA and vice versa. The status bar is also updated to show the location of
   * the start of the highlighted region.
   */
  @Override
  public void highlightSequence(SearchResultsI results)
  {
    if (results == null || results.equals(lastSearchResults))
    {
      return;
    }
    lastSearchResults = results;

    if (av.isFollowHighlight())
    {
      /*
       * if scrollToPosition requires a scroll adjustment, this flag prevents
       * another scroll event being propagated back to the originator
       * 
       * @see AlignmentPanel#adjustmentValueChanged
       */
      ap.setDontScrollComplement(true);
      if (ap.scrollToPosition(results, false))
      {
        seqCanvas.revalidate();
      }
    }
    setStatusMessage(results);
    seqCanvas.highlightSearchResults(results);
  }

  @Override
  public VamsasSource getVamsasSource()
  {
    return this.ap == null ? null : this.ap.av;
  }

  @Override
  public void updateColours(SequenceI seq, int index)
  {
    System.out.println("update the seqPanel colours");
    // repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    if (editingSeqs)
    {
      // This is because MacOSX creates a mouseMoved
      // If control is down, other platforms will not.
      mouseDragged(evt);
    }

    int res = findRes(evt);
    int seq = findSeq(evt);
    int pos;
    if (res < 0 || seq < 0 || seq >= av.getAlignment().getHeight())
    {
      return;
    }

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    if (res >= sequence.getLength())
    {
      return;
    }

    pos = setStatusMessage(sequence, res, seq);
    if (ssm != null && pos > -1)
    {
      mouseOverSequence(sequence, res, pos);
    }

    tooltipText.setLength(6); // Cuts the buffer back to <html>

    SequenceGroup[] groups = av.getAlignment().findAllGroups(sequence);
    if (groups != null)
    {
      for (int g = 0; g < groups.length; g++)
      {
        if (groups[g].getStartRes() <= res && groups[g].getEndRes() >= res)
        {
          if (!groups[g].getName().startsWith("JTreeGroup")
                  && !groups[g].getName().startsWith("JGroup"))
          {
            tooltipText.append(groups[g].getName());
          }

          if (groups[g].getDescription() != null)
          {
            tooltipText.append(": " + groups[g].getDescription());
          }
        }
      }
    }

    // use aa to see if the mouse pointer is on a
    if (av.isShowSequenceFeatures())
    {
      int rpos;
      List<SequenceFeature> features = ap.getFeatureRenderer()
              .findFeaturesAtRes(sequence.getDatasetSequence(),
                      rpos = sequence.findPosition(res));
      seqARep.appendFeatures(tooltipText, rpos, features,
              this.ap.getSeqPanel().seqCanvas.fr.getMinMax());
    }
    if (tooltipText.length() == 6) // <html>
    {
      setToolTipText(null);
      lastTooltip = null;
    }
    else
    {
      if (lastTooltip == null
              || !lastTooltip.equals(tooltipText.toString()))
      {
        String formatedTooltipText = JvSwingUtils.wrapTooltip(true,
                tooltipText.toString());
        // String formatedTooltipText = tooltipText.toString();
        setToolTipText(formatedTooltipText);
        lastTooltip = tooltipText.toString();
      }

    }

  }

  private Point lastp = null;

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#getToolTipLocation(java.awt.event.MouseEvent)
   */
  @Override
  public Point getToolTipLocation(MouseEvent event)
  {
    int x = event.getX(), w = getWidth();
    int wdth = (w - x < 200) ? -(w / 2) : 5; // switch sides when tooltip is too
    // close to edge
    Point p = lastp;
    if (!event.isShiftDown() || p == null)
    {
      p = (tooltipText != null && tooltipText.length() > 6) ? new Point(
              event.getX() + wdth, event.getY() - 20) : null;
    }
    /*
     * TODO: try to modify position region is not obcured by tooltip
     */
    return lastp = p;
  }

  String lastTooltip;

  /**
   * set when the current UI interaction has resulted in a change that requires
   * overview shading to be recalculated. this could be changed to something
   * more expressive that indicates what actually has changed, so selective
   * redraws can be applied
   */
  private boolean needOverviewUpdate = false; // TODO: refactor to avcontroller

  /**
   * set if av.getSelectionGroup() refers to a group that is defined on the
   * alignment view, rather than a transient selection
   */
  // private boolean editingDefinedGroup = false; // TODO: refactor to
  // avcontroller or viewModel

  /**
   * Set status message in alignment panel
   * 
   * @param sequence
   *          aligned sequence object
   * @param res
   *          alignment column
   * @param seq
   *          index of sequence in alignment
   * @return position of res in sequence
   */
  int setStatusMessage(SequenceI sequence, int res, int seq)
  {
    StringBuilder text = new StringBuilder(32);

    /*
     * Sequence number (if known), and sequence name.
     */
    String seqno = seq == -1 ? "" : " " + (seq + 1);
    text.append("Sequence").append(seqno).append(" ID: ")
            .append(sequence.getName());

    String residue = null;
    /*
     * Try to translate the display character to residue name (null for gap).
     */
    final String displayChar = String.valueOf(sequence.getCharAt(res));
    if (av.getAlignment().isNucleotide())
    {
      residue = ResidueProperties.nucleotideName.get(displayChar);
      if (residue != null)
      {
        text.append(" Nucleotide: ").append(residue);
      }
    }
    else
    {
      residue = "X".equalsIgnoreCase(displayChar) ? "X" : ("*"
              .equals(displayChar) ? "STOP" : ResidueProperties.aa2Triplet
              .get(displayChar));
      if (residue != null)
      {
        text.append(" Residue: ").append(residue);
      }
    }

    int pos = -1;
    if (residue != null)
    {
      pos = sequence.findPosition(res);
      text.append(" (").append(Integer.toString(pos)).append(")");
    }
    ap.alignFrame.statusBar.setText(text.toString());
    return pos;
  }

  /**
   * Set the status bar message to highlight the first matched position in
   * search results.
   * 
   * @param results
   */
  private void setStatusMessage(SearchResultsI results)
  {
    AlignmentI al = this.av.getAlignment();
    int sequenceIndex = al.findIndex(results);
    if (sequenceIndex == -1)
    {
      return;
    }
    SequenceI ds = al.getSequenceAt(sequenceIndex).getDatasetSequence();
    for (SearchResultMatchI m : results.getResults())
    {
      SequenceI seq = m.getSequence();
      if (seq.getDatasetSequence() != null)
      {
        seq = seq.getDatasetSequence();
      }

      if (seq == ds)
      {
        /*
         * Convert position in sequence (base 1) to sequence character array
         * index (base 0)
         */
        int start = m.getStart() - m.getSequence().getStart();
        setStatusMessage(seq, start, sequenceIndex);
        return;
      }
    }
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
    if (mouseWheelPressed)
    {
      int oldWidth = av.getCharWidth();

      // Which is bigger, left-right or up-down?
      if (Math.abs(evt.getY() - lastMousePress.getY()) > Math.abs(evt
              .getX() - lastMousePress.getX()))
      {
        int fontSize = av.font.getSize();

        if (evt.getY() < lastMousePress.getY())
        {
          fontSize--;
        }
        else if (evt.getY() > lastMousePress.getY())
        {
          fontSize++;
        }

        if (fontSize < 1)
        {
          fontSize = 1;
        }

        av.setFont(
                new Font(av.font.getName(), av.font.getStyle(), fontSize),
                true);
        av.setCharWidth(oldWidth);
        ap.fontChanged();
      }
      else
      {
        if (evt.getX() < lastMousePress.getX() && av.getCharWidth() > 1)
        {
          av.setCharWidth(av.getCharWidth() - 1);
        }
        else if (evt.getX() > lastMousePress.getX())
        {
          av.setCharWidth(av.getCharWidth() + 1);
        }

        ap.paintAlignment(false);
      }

      FontMetrics fm = getFontMetrics(av.getFont());
      av.validCharWidth = fm.charWidth('M') <= av.getCharWidth();

      lastMousePress = evt.getPoint();

      return;
    }

    if (!editingSeqs)
    {
      doMouseDraggedDefineMode(evt);
      return;
    }

    int res = findRes(evt);

    if (res < 0)
    {
      res = 0;
    }

    if ((lastres == -1) || (lastres == res))
    {
      return;
    }

    if ((res < av.getAlignment().getWidth()) && (res < lastres))
    {
      // dragLeft, delete gap
      editSequence(false, false, res);
    }
    else
    {
      editSequence(true, false, res);
    }

    mouseDragging = true;
    if (scrollThread != null)
    {
      scrollThread.setEvent(evt);
    }
  }

  // TODO: Make it more clever than many booleans
  synchronized void editSequence(boolean insertGap, boolean editSeq,
          int startres)
  {
    int fixedLeft = -1;
    int fixedRight = -1;
    boolean fixedColumns = false;
    SequenceGroup sg = av.getSelectionGroup();

    SequenceI seq = av.getAlignment().getSequenceAt(startseq);

    // No group, but the sequence may represent a group
    if (!groupEditing && av.hasHiddenRows())
    {
      if (av.isHiddenRepSequence(seq))
      {
        sg = av.getRepresentedSequences(seq);
        groupEditing = true;
      }
    }

    StringBuilder message = new StringBuilder(64);
    if (groupEditing)
    {
      message.append("Edit group:");
      if (editCommand == null)
      {
        editCommand = new EditCommand(
                MessageManager.getString("action.edit_group"));
      }
    }
    else
    {
      message.append("Edit sequence: " + seq.getName());
      String label = seq.getName();
      if (label.length() > 10)
      {
        label = label.substring(0, 10);
      }
      if (editCommand == null)
      {
        editCommand = new EditCommand(MessageManager.formatMessage(
                "label.edit_params", new String[] { label }));
      }
    }

    if (insertGap)
    {
      message.append(" insert ");
    }
    else
    {
      message.append(" delete ");
    }

    message.append(Math.abs(startres - lastres) + " gaps.");
    ap.alignFrame.statusBar.setText(message.toString());

    // Are we editing within a selection group?
    if (groupEditing
            || (sg != null && sg.getSequences(av.getHiddenRepSequences())
                    .contains(seq)))
    {
      fixedColumns = true;

      // sg might be null as the user may only see 1 sequence,
      // but the sequence represents a group
      if (sg == null)
      {
        if (!av.isHiddenRepSequence(seq))
        {
          endEditing();
          return;
        }
        sg = av.getRepresentedSequences(seq);
      }

      fixedLeft = sg.getStartRes();
      fixedRight = sg.getEndRes();

      if ((startres < fixedLeft && lastres >= fixedLeft)
              || (startres >= fixedLeft && lastres < fixedLeft)
              || (startres > fixedRight && lastres <= fixedRight)
              || (startres <= fixedRight && lastres > fixedRight))
      {
        endEditing();
        return;
      }

      if (fixedLeft > startres)
      {
        fixedRight = fixedLeft - 1;
        fixedLeft = 0;
      }
      else if (fixedRight < startres)
      {
        fixedLeft = fixedRight;
        fixedRight = -1;
      }
    }

    if (av.hasHiddenColumns())
    {
      fixedColumns = true;
      int y1 = av.getColumnSelection().getHiddenBoundaryLeft(startres);
      int y2 = av.getColumnSelection().getHiddenBoundaryRight(startres);

      if ((insertGap && startres > y1 && lastres < y1)
              || (!insertGap && startres < y2 && lastres > y2))
      {
        endEditing();
        return;
      }

      // System.out.print(y1+" "+y2+" "+fixedLeft+" "+fixedRight+"~~");
      // Selection spans a hidden region
      if (fixedLeft < y1 && (fixedRight > y2 || fixedRight == -1))
      {
        if (startres >= y2)
        {
          fixedLeft = y2;
        }
        else
        {
          fixedRight = y2 - 1;
        }
      }
    }

    if (groupEditing)
    {
      List<SequenceI> vseqs = sg.getSequences(av.getHiddenRepSequences());
      int g, groupSize = vseqs.size();
      SequenceI[] groupSeqs = new SequenceI[groupSize];
      for (g = 0; g < groupSeqs.length; g++)
      {
        groupSeqs[g] = vseqs.get(g);
      }

      // drag to right
      if (insertGap)
      {
        // If the user has selected the whole sequence, and is dragging to
        // the right, we can still extend the alignment and selectionGroup
        if (sg.getStartRes() == 0 && sg.getEndRes() == fixedRight
                && sg.getEndRes() == av.getAlignment().getWidth() - 1)
        {
          sg.setEndRes(av.getAlignment().getWidth() + startres - lastres);
          fixedRight = sg.getEndRes();
        }

        // Is it valid with fixed columns??
        // Find the next gap before the end
        // of the visible region boundary
        boolean blank = false;
        for (fixedRight = fixedRight; fixedRight > lastres; fixedRight--)
        {
          blank = true;

          for (g = 0; g < groupSize; g++)
          {
            for (int j = 0; j < startres - lastres; j++)
            {
              if (!Comparison.isGap(groupSeqs[g].getCharAt(fixedRight - j)))
              {
                blank = false;
                break;
              }
            }
          }
          if (blank)
          {
            break;
          }
        }

        if (!blank)
        {
          if (sg.getSize() == av.getAlignment().getHeight())
          {
            if ((av.hasHiddenColumns() && startres < av
                    .getColumnSelection().getHiddenBoundaryRight(startres)))
            {
              endEditing();
              return;
            }

            int alWidth = av.getAlignment().getWidth();
            if (av.hasHiddenRows())
            {
              int hwidth = av.getAlignment().getHiddenSequences()
                      .getWidth();
              if (hwidth > alWidth)
              {
                alWidth = hwidth;
              }
            }
            // We can still insert gaps if the selectionGroup
            // contains all the sequences
            sg.setEndRes(sg.getEndRes() + startres - lastres);
            fixedRight = alWidth + startres - lastres;
          }
          else
          {
            endEditing();
            return;
          }
        }
      }

      // drag to left
      else if (!insertGap)
      {
        // / Are we able to delete?
        // ie are all columns blank?

        for (g = 0; g < groupSize; g++)
        {
          for (int j = startres; j < lastres; j++)
          {
            if (groupSeqs[g].getLength() <= j)
            {
              continue;
            }

            if (!Comparison.isGap(groupSeqs[g].getCharAt(j)))
            {
              // Not a gap, block edit not valid
              endEditing();
              return;
            }
          }
        }
      }

      if (insertGap)
      {
        // dragging to the right
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j < startres; j++)
          {
            insertChar(j, groupSeqs, fixedRight);
          }
        }
        else
        {
          appendEdit(Action.INSERT_GAP, groupSeqs, startres, startres
                  - lastres);
        }
      }
      else
      {
        // dragging to the left
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j > startres; j--)
          {
            deleteChar(startres, groupSeqs, fixedRight);
          }
        }
        else
        {
          appendEdit(Action.DELETE_GAP, groupSeqs, startres, lastres
                  - startres);
        }

      }
    }
    else
    // ///Editing a single sequence///////////
    {
      if (insertGap)
      {
        // dragging to the right
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j < startres; j++)
          {
            insertChar(j, new SequenceI[] { seq }, fixedRight);
          }
        }
        else
        {
          appendEdit(Action.INSERT_GAP, new SequenceI[] { seq }, lastres,
                  startres - lastres);
        }
      }
      else
      {
        if (!editSeq)
        {
          // dragging to the left
          if (fixedColumns && fixedRight != -1)
          {
            for (int j = lastres; j > startres; j--)
            {
              if (!Comparison.isGap(seq.getCharAt(startres)))
              {
                endEditing();
                break;
              }
              deleteChar(startres, new SequenceI[] { seq }, fixedRight);
            }
          }
          else
          {
            // could be a keyboard edit trying to delete none gaps
            int max = 0;
            for (int m = startres; m < lastres; m++)
            {
              if (!Comparison.isGap(seq.getCharAt(m)))
              {
                break;
              }
              max++;
            }

            if (max > 0)
            {
              appendEdit(Action.DELETE_GAP, new SequenceI[] { seq },
                      startres, max);
            }
          }
        }
        else
        {// insertGap==false AND editSeq==TRUE;
          if (fixedColumns && fixedRight != -1)
          {
            for (int j = lastres; j < startres; j++)
            {
              insertChar(j, new SequenceI[] { seq }, fixedRight);
            }
          }
          else
          {
            appendEdit(Action.INSERT_NUC, new SequenceI[] { seq }, lastres,
                    startres - lastres);
          }
        }
      }
    }

    lastres = startres;
    seqCanvas.repaint();
  }

  void insertChar(int j, SequenceI[] seq, int fixedColumn)
  {
    int blankColumn = fixedColumn;
    for (int s = 0; s < seq.length; s++)
    {
      // Find the next gap before the end of the visible region boundary
      // If lastCol > j, theres a boundary after the gap insertion

      for (blankColumn = fixedColumn; blankColumn > j; blankColumn--)
      {
        if (Comparison.isGap(seq[s].getCharAt(blankColumn)))
        {
          // Theres a space, so break and insert the gap
          break;
        }
      }

      if (blankColumn <= j)
      {
        blankColumn = fixedColumn;
        endEditing();
        return;
      }
    }

    appendEdit(Action.DELETE_GAP, seq, blankColumn, 1);

    appendEdit(Action.INSERT_GAP, seq, j, 1);

  }

  /**
   * Helper method to add and perform one edit action.
   * 
   * @param action
   * @param seq
   * @param pos
   * @param count
   */
  protected void appendEdit(Action action, SequenceI[] seq, int pos,
          int count)
  {

    final Edit edit = new EditCommand().new Edit(action, seq, pos, count,
            av.getAlignment().getGapCharacter());

    editCommand.appendEdit(edit, av.getAlignment(), true, null);
  }

  void deleteChar(int j, SequenceI[] seq, int fixedColumn)
  {

    appendEdit(Action.DELETE_GAP, seq, j, 1);

    appendEdit(Action.INSERT_GAP, seq, fixedColumn, 1);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (oldSeq < 0)
    {
      oldSeq = 0;
    }

    if (scrollThread != null)
    {
      scrollThread.running = false;
      scrollThread = null;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging)
    {
      scrollThread = new ScrollThread();
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    SequenceGroup sg = null;
    SequenceI sequence = av.getAlignment().getSequenceAt(findSeq(evt));
    if (evt.getClickCount() > 1)
    {
      sg = av.getSelectionGroup();
      if (sg != null && sg.getSize() == 1
              && sg.getEndRes() - sg.getStartRes() < 2)
      {
        av.setSelectionGroup(null);
      }

      List<SequenceFeature> features = seqCanvas.getFeatureRenderer()
              .findFeaturesAtRes(sequence.getDatasetSequence(),
                      sequence.findPosition(findRes(evt)));

      if (features != null && features.size() > 0)
      {
        SearchResultsI highlight = new SearchResults();
        highlight.addResult(sequence, features.get(0).getBegin(), features
                .get(0).getEnd());
        seqCanvas.highlightSearchResults(highlight);
      }
      if (features != null && features.size() > 0)
      {
        seqCanvas.getFeatureRenderer().amendFeatures(
                new SequenceI[] { sequence },
                features.toArray(new SequenceFeature[features.size()]),
                false, ap);

        seqCanvas.highlightSearchResults(null);
      }
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    e.consume();
    if (e.getWheelRotation() > 0)
    {
      if (e.isShiftDown())
      {
        ap.scrollRight(true);

      }
      else
      {
        ap.scrollUp(false);
      }
    }
    else
    {
      if (e.isShiftDown())
      {
        ap.scrollRight(false);
      }
      else
      {
        ap.scrollUp(true);
      }
    }
    // TODO Update tooltip for new position.
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void doMousePressedDefineMode(MouseEvent evt)
  {
    final int res = findRes(evt);
    final int seq = findSeq(evt);
    oldSeq = seq;
    needOverviewUpdate = false;

    startWrapBlock = wrappedBlock;

    if (av.getWrapAlignment() && seq > av.getAlignment().getHeight())
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop, MessageManager
              .getString("label.cannot_edit_annotations_in_wrapped_view"),
              MessageManager.getString("label.wrapped_view_no_edit"),
              JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (seq < 0 || res < 0)
    {
      return;
    }

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    if ((sequence == null) || (res > sequence.getLength()))
    {
      return;
    }

    stretchGroup = av.getSelectionGroup();

    if (stretchGroup == null)
    {
      stretchGroup = av.getAlignment().findGroup(sequence);

      if ((stretchGroup != null) && (res > stretchGroup.getStartRes())
              && (res < stretchGroup.getEndRes()))
      {
        av.setSelectionGroup(stretchGroup);
      }
      else
      {
        stretchGroup = null;
      }
    }
    else if (!stretchGroup.getSequences(null).contains(sequence)
            || (stretchGroup.getStartRes() > res)
            || (stretchGroup.getEndRes() < res))
    {
      stretchGroup = null;

      SequenceGroup[] allGroups = av.getAlignment().findAllGroups(sequence);

      if (allGroups != null)
      {
        for (int i = 0; i < allGroups.length; i++)
        {
          if ((allGroups[i].getStartRes() <= res)
                  && (allGroups[i].getEndRes() >= res))
          {
            stretchGroup = allGroups[i];
            break;
          }
        }
      }

      av.setSelectionGroup(stretchGroup);
    }

    if (evt.isPopupTrigger()) // Mac: mousePressed
    {
      showPopupMenu(evt);
      return;
    }

    /*
     * defer right-mouse click handling to mouseReleased on Windows
     * (where isPopupTrigger() will answer true)
     * NB isRightMouseButton is also true for Cmd-click on Mac
     */
    if (SwingUtilities.isRightMouseButton(evt) && !Platform.isAMac())
    {
      return;
    }

    if (av.cursorMode)
    {
      seqCanvas.cursorX = findRes(evt);
      seqCanvas.cursorY = findSeq(evt);
      seqCanvas.repaint();
      return;
    }

    if (stretchGroup == null)
    {
      // Only if left mouse button do we want to change group sizes

      // define a new group here
      SequenceGroup sg = new SequenceGroup();
      sg.setStartRes(res);
      sg.setEndRes(res);
      sg.addSequence(sequence, false);
      av.setSelectionGroup(sg);
      stretchGroup = sg;

      if (av.getConservationSelected())
      {
        SliderPanel.setConservationSlider(ap, av.getGlobalColourScheme(),
                "Background");
      }

      if (av.getAbovePIDThreshold())
      {
        SliderPanel.setPIDSliderSource(ap, av.getGlobalColourScheme(),
                "Background");
      }
      if ((stretchGroup != null) && (stretchGroup.getEndRes() == res))
      {
        // Edit end res position of selected group
        changeEndRes = true;
      }
      else if ((stretchGroup != null)
              && (stretchGroup.getStartRes() == res))
      {
        // Edit end res position of selected group
        changeStartRes = true;
      }
      stretchGroup.getWidth();
    }

    seqCanvas.repaint();
  }

  /**
   * Build and show a pop-up menu at the right-click mouse position
   * 
   * @param evt
   * @param res
   * @param sequence
   */
  void showPopupMenu(MouseEvent evt)
  {
    final int res = findRes(evt);
    final int seq = findSeq(evt);
    SequenceI sequence = av.getAlignment().getSequenceAt(seq);
    List<SequenceFeature> allFeatures = ap.getFeatureRenderer()
            .findFeaturesAtRes(sequence.getDatasetSequence(),
                    sequence.findPosition(res));
    List<String> links = new ArrayList<String>();
    for (SequenceFeature sf : allFeatures)
    {
      if (sf.links != null)
      {
        for (String link : sf.links)
        {
          links.add(link);
        }
      }
    }

    PopupMenu pop = new PopupMenu(ap, null, links);
    pop.show(this, evt.getX(), evt.getY());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void doMouseReleasedDefineMode(MouseEvent evt)
  {
    if (stretchGroup == null)
    {
      return;
    }
    // always do this - annotation has own state
    // but defer colourscheme update until hidden sequences are passed in
    boolean vischange = stretchGroup.recalcConservation(true);
    needOverviewUpdate |= vischange && av.isSelectionDefinedGroup();
    if (stretchGroup.cs != null)
    {
      stretchGroup.cs.alignmentChanged(stretchGroup,
              av.getHiddenRepSequences());

      if (stretchGroup.cs.conservationApplied())
      {
        SliderPanel.setConservationSlider(ap, stretchGroup.cs,
                stretchGroup.getName());
      }
      else
      {
        SliderPanel.setPIDSliderSource(ap, stretchGroup.cs,
                stretchGroup.getName());
      }
    }
    PaintRefresher.Refresh(this, av.getSequenceSetId());
    ap.paintAlignment(needOverviewUpdate);
    needOverviewUpdate = false;
    changeEndRes = false;
    changeStartRes = false;
    stretchGroup = null;
    av.sendSelection();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void doMouseDraggedDefineMode(MouseEvent evt)
  {
    int res = findRes(evt);
    int y = findSeq(evt);

    if (wrappedBlock != startWrapBlock)
    {
      return;
    }

    if (stretchGroup == null)
    {
      return;
    }

    if (res >= av.getAlignment().getWidth())
    {
      res = av.getAlignment().getWidth() - 1;
    }

    if (stretchGroup.getEndRes() == res)
    {
      // Edit end res position of selected group
      changeEndRes = true;
    }
    else if (stretchGroup.getStartRes() == res)
    {
      // Edit start res position of selected group
      changeStartRes = true;
    }

    if (res < av.getStartRes())
    {
      res = av.getStartRes();
    }

    if (changeEndRes)
    {
      if (res > (stretchGroup.getStartRes() - 1))
      {
        stretchGroup.setEndRes(res);
        needOverviewUpdate |= av.isSelectionDefinedGroup();
      }
    }
    else if (changeStartRes)
    {
      if (res < (stretchGroup.getEndRes() + 1))
      {
        stretchGroup.setStartRes(res);
        needOverviewUpdate |= av.isSelectionDefinedGroup();
      }
    }

    int dragDirection = 0;

    if (y > oldSeq)
    {
      dragDirection = 1;
    }
    else if (y < oldSeq)
    {
      dragDirection = -1;
    }

    while ((y != oldSeq) && (oldSeq > -1)
            && (y < av.getAlignment().getHeight()))
    {
      // This routine ensures we don't skip any sequences, as the
      // selection is quite slow.
      Sequence seq = (Sequence) av.getAlignment().getSequenceAt(oldSeq);

      oldSeq += dragDirection;

      if (oldSeq < 0)
      {
        break;
      }

      Sequence nextSeq = (Sequence) av.getAlignment().getSequenceAt(oldSeq);

      if (stretchGroup.getSequences(null).contains(nextSeq))
      {
        stretchGroup.deleteSequence(seq, false);
        needOverviewUpdate |= av.isSelectionDefinedGroup();
      }
      else
      {
        if (seq != null)
        {
          stretchGroup.addSequence(seq, false);
        }

        stretchGroup.addSequence(nextSeq, false);
        needOverviewUpdate |= av.isSelectionDefinedGroup();
      }
    }

    if (oldSeq < 0)
    {
      oldSeq = -1;
    }

    mouseDragging = true;

    if (scrollThread != null)
    {
      scrollThread.setEvent(evt);
    }

    seqCanvas.repaint();
  }

  void scrollCanvas(MouseEvent evt)
  {
    if (evt == null)
    {
      if (scrollThread != null)
      {
        scrollThread.running = false;
        scrollThread = null;
      }
      mouseDragging = false;
    }
    else
    {
      if (scrollThread == null)
      {
        scrollThread = new ScrollThread();
      }

      mouseDragging = true;
      scrollThread.setEvent(evt);
    }

  }

  // this class allows scrolling off the bottom of the visible alignment
  class ScrollThread extends Thread
  {
    MouseEvent evt;

    boolean running = false;

    public ScrollThread()
    {
      start();
    }

    public void setEvent(MouseEvent e)
    {
      evt = e;
    }

    public void stopScrolling()
    {
      running = false;
    }

    @Override
    public void run()
    {
      running = true;

      while (running)
      {
        if (evt != null)
        {
          if (mouseDragging && (evt.getY() < 0) && (av.getStartSeq() > 0))
          {
            running = ap.scrollUp(true);
          }

          if (mouseDragging && (evt.getY() >= getHeight())
                  && (av.getAlignment().getHeight() > av.getEndSeq()))
          {
            running = ap.scrollUp(false);
          }

          if (mouseDragging && (evt.getX() < 0))
          {
            running = ap.scrollRight(false);
          }
          else if (mouseDragging && (evt.getX() >= getWidth()))
          {
            running = ap.scrollRight(true);
          }
        }

        try
        {
          Thread.sleep(20);
        } catch (Exception ex)
        {
        }
      }
    }
  }

  /**
   * modify current selection according to a received message.
   */
  @Override
  public void selection(SequenceGroup seqsel, ColumnSelection colsel,
          SelectionSource source)
  {
    // TODO: fix this hack - source of messages is align viewport, but SeqPanel
    // handles selection messages...
    // TODO: extend config options to allow user to control if selections may be
    // shared between viewports.
    boolean iSentTheSelection = (av == source || (source instanceof AlignViewport && ((AlignmentViewport) source)
            .getSequenceSetId().equals(av.getSequenceSetId())));
    if (iSentTheSelection || !av.followSelection)
    {
      return;
    }

    /*
     * Ignore the selection if there is one of our own pending.
     */
    if (av.isSelectionGroupChanged(false) || av.isColSelChanged(false))
    {
      return;
    }

    /*
     * Check for selection in a view of which this one is a dna/protein
     * complement.
     */
    if (selectionFromTranslation(seqsel, colsel, source))
    {
      return;
    }

    // do we want to thread this ? (contention with seqsel and colsel locks, I
    // suspect)
    /*
     * only copy colsel if there is a real intersection between
     * sequence selection and this panel's alignment
     */
    boolean repaint = false;
    boolean copycolsel = false;

    SequenceGroup sgroup = null;
    if (seqsel != null && seqsel.getSize() > 0)
    {
      if (av.getAlignment() == null)
      {
        Cache.log.warn("alignviewport av SeqSetId=" + av.getSequenceSetId()
                + " ViewId=" + av.getViewId()
                + " 's alignment is NULL! returning immediately.");
        return;
      }
      sgroup = seqsel.intersect(av.getAlignment(),
              (av.hasHiddenRows()) ? av.getHiddenRepSequences() : null);
      if ((sgroup != null && sgroup.getSize() > 0))
      {
        copycolsel = true;
      }
    }
    if (sgroup != null && sgroup.getSize() > 0)
    {
      av.setSelectionGroup(sgroup);
    }
    else
    {
      av.setSelectionGroup(null);
    }
    av.isSelectionGroupChanged(true);
    repaint = true;

    if (copycolsel)
    {
      // the current selection is unset or from a previous message
      // so import the new colsel.
      if (colsel == null || colsel.isEmpty())
      {
        if (av.getColumnSelection() != null)
        {
          av.getColumnSelection().clear();
          repaint = true;
        }
      }
      else
      {
        // TODO: shift colSel according to the intersecting sequences
        if (av.getColumnSelection() == null)
        {
          av.setColumnSelection(new ColumnSelection(colsel));
        }
        else
        {
          av.getColumnSelection().setElementsFrom(colsel);
        }
      }
      av.isColSelChanged(true);
      repaint = true;
    }

    if (copycolsel
            && av.hasHiddenColumns()
            && (av.getColumnSelection() == null || av.getColumnSelection()
                    .getHiddenColumns() == null))
    {
      System.err.println("Bad things");
    }
    if (repaint) // always true!
    {
      // probably finessing with multiple redraws here
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      // ap.paintAlignment(false);
    }
  }

  /**
   * If this panel is a cdna/protein translation view of the selection source,
   * tries to map the source selection to a local one, and returns true. Else
   * returns false.
   * 
   * @param seqsel
   * @param colsel
   * @param source
   */
  protected boolean selectionFromTranslation(SequenceGroup seqsel,
          ColumnSelection colsel, SelectionSource source)
  {
    if (!(source instanceof AlignViewportI))
    {
      return false;
    }
    final AlignViewportI sourceAv = (AlignViewportI) source;
    if (sourceAv.getCodingComplement() != av
            && av.getCodingComplement() != sourceAv)
    {
      return false;
    }

    /*
     * Map sequence selection
     */
    SequenceGroup sg = MappingUtils.mapSequenceGroup(seqsel, sourceAv, av);
    av.setSelectionGroup(sg);
    av.isSelectionGroupChanged(true);

    /*
     * Map column selection
     */
    ColumnSelection cs = MappingUtils.mapColumnSelection(colsel, sourceAv,
            av);
    av.setColumnSelection(cs);

    PaintRefresher.Refresh(this, av.getSequenceSetId());

    return true;
  }
}
