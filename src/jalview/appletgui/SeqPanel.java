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
package jalview.appletgui;

import jalview.api.AlignViewportI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.structure.SelectionListener;
import jalview.structure.SelectionSource;
import jalview.structure.SequenceListener;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

public class SeqPanel extends Panel implements MouseMotionListener,
        MouseListener, SequenceListener, SelectionListener
{

  public SeqCanvas seqCanvas;

  public AlignmentPanel ap;

  protected int lastres;

  protected int startseq;

  protected AlignViewport av;

  // if character is inserted or deleted, we will need to recalculate the
  // conservation
  boolean seqEditOccurred = false;

  ScrollThread scrollThread = null;

  boolean mouseDragging = false;

  boolean editingSeqs = false;

  boolean groupEditing = false;

  int oldSeq = -1;

  boolean changeEndSeq = false;

  boolean changeStartSeq = false;

  boolean changeEndRes = false;

  boolean changeStartRes = false;

  SequenceGroup stretchGroup = null;

  StringBuffer keyboardNo1;

  StringBuffer keyboardNo2;

  boolean mouseWheelPressed = false;

  Point lastMousePress;

  EditCommand editCommand;

  StructureSelectionManager ssm;

  public SeqPanel(AlignViewport avp, AlignmentPanel p)
  {
    this.av = avp;

    seqCanvas = new SeqCanvas(avp);
    setLayout(new BorderLayout());
    add(seqCanvas);

    ap = p;

    seqCanvas.addMouseMotionListener(this);
    seqCanvas.addMouseListener(this);
    ssm = StructureSelectionManager.getStructureSelectionManager(av.applet);
    ssm.addStructureViewerListener(this);
    ssm.addSelectionListener(this);

    seqCanvas.repaint();
  }

  void endEditing()
  {
    if (editCommand != null && editCommand.getSize() > 0)
    {
      ap.alignFrame.addHistoryItem(editCommand);
      av.firePropertyChange("alignment", null, av.getAlignment()
              .getSequences());
    }

    startseq = -1;
    lastres = -1;
    editingSeqs = false;
    groupEditing = false;
    keyboardNo1 = null;
    keyboardNo2 = null;
    editCommand = null;
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
        sg.clear();
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
    editSequence(true, seqCanvas.cursorX + getKeyboardNo1());
    endEditing();
  }

  void deleteGapAtCursor(boolean group)
  {
    groupEditing = group;
    startseq = seqCanvas.cursorY;
    lastres = seqCanvas.cursorX + getKeyboardNo1();
    editSequence(false, seqCanvas.cursorX);
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
  void setStatusMessage(SequenceI sequence, int res, int seq)
  {
    // TODO remove duplication of identical gui method
    StringBuilder text = new StringBuilder(32);
    String seqno = seq == -1 ? "" : " " + (seq + 1);
    text.append("Sequence" + seqno + " ID: " + sequence.getName());

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
  }

  /**
   * Set the status bar message to highlight the first matched position in
   * search results.
   * 
   * @param results
   * @return true if results were matched, false if not
   */
  private boolean setStatusMessage(SearchResultsI results)
  {
    AlignmentI al = this.av.getAlignment();
    int sequenceIndex = al.findIndex(results);
    if (sequenceIndex == -1)
    {
      return false;
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
        return true;
      }
    }
    return false;
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    lastMousePress = evt.getPoint();

    // For now, ignore the mouseWheel font resizing on Macs
    // As the Button2_mask always seems to be true
    if ((evt.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK
            && !av.MAC)
    {
      mouseWheelPressed = true;
      return;
    }

    if (evt.isShiftDown() || evt.isControlDown() || evt.isAltDown())
    {
      if (evt.isControlDown() || evt.isAltDown())
      {
        groupEditing = true;
      }
      editingSeqs = true;
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

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    SequenceI sequence = av.getAlignment().getSequenceAt(findSeq(evt));
    if (evt.getClickCount() > 1)
    {
      if (av.getSelectionGroup() != null
              && av.getSelectionGroup().getSize() == 1
              && av.getSelectionGroup().getEndRes()
                      - av.getSelectionGroup().getStartRes() < 2)
      {
        av.setSelectionGroup(null);
      }

      SequenceFeature[] features = findFeaturesAtRes(sequence,
              sequence.findPosition(findRes(evt)));

      if (features != null && features.length > 0)
      {
        SearchResultsI highlight = new SearchResults();
        highlight.addResult(sequence, features[0].getBegin(),
                features[0].getEnd());
        seqCanvas.highlightSearchResults(highlight);
      }
      if (features != null && features.length > 0)
      {
        seqCanvas.getFeatureRenderer().amendFeatures(
                new SequenceI[] { sequence }, features, false, ap);

        seqCanvas.highlightSearchResults(null);
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    mouseDragging = false;
    mouseWheelPressed = false;
    ap.paintAlignment(true);

    if (!editingSeqs)
    {
      doMouseReleasedDefineMode(evt);
      return;
    }

    endEditing();

  }

  int startWrapBlock = -1;

  int wrappedBlock = -1;

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

      int cwidth = seqCanvas.getWrappedCanvasWidth(getSize().width);
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
      res = (x / av.getCharWidth()) + av.getStartRes();
    }

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    return res;

  }

  int findSeq(MouseEvent evt)
  {
    final int sqnum = findAlRow(evt);
    return (sqnum < 0) ? 0 : sqnum;
  }

  /**
   * 
   * @param evt
   * @return row in alignment that was selected (or -1 for column selection)
   */
  private int findAlRow(MouseEvent evt)
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
      if (seq < 0)
      {
        seq = -1;
      }
    }
    else
    {
      seq = Math.min((y / av.getCharHeight()) + av.getStartSeq(), av
              .getAlignment().getHeight() - 1);
      if (seq < 0)
      {
        seq = -1;
      }
    }

    return seq;
  }

  public void doMousePressed(MouseEvent evt)
  {

    int seq = findSeq(evt);
    int res = findRes(evt);

    if (seq < av.getAlignment().getHeight()
            && res < av.getAlignment().getSequenceAt(seq).getLength())
    {
      // char resstr = align.getSequenceAt(seq).getSequence().charAt(res);
      // Find the residue's position in the sequence (res is the position
      // in the alignment

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
    String tmp = sequence.hashCode() + index + "";
    if (lastMessage == null || !lastMessage.equals(tmp))
    {
      ssm.mouseOverSequence(sequence, index, pos, av);
    }

    lastMessage = tmp;
  }

  @Override
  public void highlightSequence(SearchResultsI results)
  {
    if (av.isFollowHighlight())
    {
      if (ap.scrollToPosition(results, true))
      {
        ap.alignFrame.repaint();
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

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    int res = findRes(evt);
    int seq = findSeq(evt);

    if (seq >= av.getAlignment().getHeight() || seq < 0 || res < 0)
    {
      if (tooltip != null)
      {
        tooltip.setTip("");
      }
      return;
    }

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);
    if (res > sequence.getLength())
    {
      if (tooltip != null)
      {
        tooltip.setTip("");
      }
      return;
    }

    int respos = sequence.findPosition(res);
    if (ssm != null)
    {
      mouseOverSequence(sequence, res, respos);
    }

    StringBuilder text = new StringBuilder();
    text.append("Sequence ").append(Integer.toString(seq + 1))
            .append(" ID: ").append(sequence.getName());

    String obj = null;
    final String ch = String.valueOf(sequence.getCharAt(res));
    if (av.getAlignment().isNucleotide())
    {
      obj = ResidueProperties.nucleotideName.get(ch);
      if (obj != null)
      {
        text.append(" Nucleotide: ").append(obj);
      }
    }
    else
    {
      obj = "X".equalsIgnoreCase(ch) ? "X" : ResidueProperties.aa2Triplet
              .get(ch);
      if (obj != null)
      {
        text.append(" Residue: ").append(obj);
      }
    }

    if (obj != null)
    {
      text.append(" (").append(Integer.toString(respos)).append(")");
    }

    ap.alignFrame.statusBar.setText(text.toString());

    StringBuilder tooltipText = new StringBuilder();
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
            tooltipText.append(groups[g].getName()).append(" ");
          }
          if (groups[g].getDescription() != null)
          {
            tooltipText.append(groups[g].getDescription());
          }
          tooltipText.append("\n");
        }
      }
    }

    // use aa to see if the mouse pointer is on a
    SequenceFeature[] allFeatures = findFeaturesAtRes(sequence,
            sequence.findPosition(res));

    int index = 0;
    while (index < allFeatures.length)
    {
      SequenceFeature sf = allFeatures[index];

      tooltipText.append(sf.getType() + " " + sf.begin + ":" + sf.end);

      if (sf.getDescription() != null)
      {
        tooltipText.append(" " + sf.getDescription());
      }

      if (sf.getValue("status") != null)
      {
        String status = sf.getValue("status").toString();
        if (status.length() > 0)
        {
          tooltipText.append(" (" + sf.getValue("status") + ")");
        }
      }
      tooltipText.append("\n");

      index++;
    }

    if (tooltip == null)
    {
      tooltip = new Tooltip(tooltipText.toString(), seqCanvas);
    }
    else
    {
      tooltip.setTip(tooltipText.toString());
    }
  }

  SequenceFeature[] findFeaturesAtRes(SequenceI sequence, int res)
  {
    Vector tmp = new Vector();
    SequenceFeature[] features = sequence.getSequenceFeatures();
    if (features != null)
    {
      for (int i = 0; i < features.length; i++)
      {
        if (av.getFeaturesDisplayed() == null
                || !av.getFeaturesDisplayed().isVisible(
                        features[i].getType()))
        {
          continue;
        }

        if (features[i].featureGroup != null
                && !seqCanvas.fr.checkGroupVisibility(
                        features[i].featureGroup, false))
        {
          continue;
        }

        if ((features[i].getBegin() <= res)
                && (features[i].getEnd() >= res))
        {
          tmp.addElement(features[i]);
        }
      }
    }

    features = new SequenceFeature[tmp.size()];
    tmp.copyInto(features);

    return features;
  }

  Tooltip tooltip;

  /**
   * set when the current UI interaction has resulted in a change that requires
   * overview shading to be recalculated. this could be changed to something
   * more expressive that indicates what actually has changed, so selective
   * redraws can be applied
   */
  private boolean needOverviewUpdate; // TODO: refactor to avcontroller

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    if (mouseWheelPressed)
    {
      int oldWidth = av.getCharWidth();

      // Which is bigger, left-right or up-down?
      if (Math.abs(evt.getY() - lastMousePress.y) > Math.abs(evt.getX()
              - lastMousePress.x))
      {
        int fontSize = av.font.getSize();

        if (evt.getY() < lastMousePress.y && av.getCharHeight() > 1)
        {
          fontSize--;
        }
        else if (evt.getY() > lastMousePress.y)
        {
          fontSize++;
        }

        if (fontSize < 1)
        {
          fontSize = 1;
        }

        av.setFont(new Font(av.font.getName(), av.font.getStyle(), fontSize));
        av.setCharWidth(oldWidth);
      }
      else
      {
        if (evt.getX() < lastMousePress.x && av.getCharWidth() > 1)
        {
          av.setCharWidth(av.getCharWidth() - 1);
        }
        else if (evt.getX() > lastMousePress.x)
        {
          av.setCharWidth(av.getCharWidth() + 1);
        }

        if (av.getCharWidth() < 1)
        {
          av.setCharWidth(1);
        }
      }

      ap.fontChanged();

      FontMetrics fm = getFontMetrics(av.getFont());
      av.validCharWidth = fm.charWidth('M') <= av.getCharWidth();

      lastMousePress = evt.getPoint();

      ap.paintAlignment(false);
      ap.annotationPanel.image = null;
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
      editSequence(false, res);
    }
    else
    {
      editSequence(true, res);
    }

    mouseDragging = true;
    if (scrollThread != null)
    {
      scrollThread.setEvent(evt);
    }

  }

  synchronized void editSequence(boolean insertGap, int startres)
  {
    int fixedLeft = -1;
    int fixedRight = -1;
    boolean fixedColumns = false;
    SequenceGroup sg = av.getSelectionGroup();

    SequenceI seq = av.getAlignment().getSequenceAt(startseq);

    if (!groupEditing && av.hasHiddenRows())
    {
      if (av.isHiddenRepSequence(seq))
      {
        sg = av.getRepresentedSequences(seq);
        groupEditing = true;
      }
    }

    StringBuffer message = new StringBuffer();
    if (groupEditing)
    {
      message.append(MessageManager.getString("action.edit_group")).append(
              ":");
      if (editCommand == null)
      {
        editCommand = new EditCommand(
                MessageManager.getString("action.edit_group"));
      }
    }
    else
    {
      message.append(MessageManager.getString("label.edit_sequence"))
              .append(" " + seq.getName());
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
      SequenceI[] groupSeqs = sg.getSequences(av.getHiddenRepSequences())
              .toArray(new SequenceI[0]);

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

          for (SequenceI gs : groupSeqs)
          {
            for (int j = 0; j < startres - lastres; j++)
            {
              if (!jalview.util.Comparison.isGap(gs.getCharAt(fixedRight
                      - j)))
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

        for (SequenceI gs : groupSeqs)
        {
          for (int j = startres; j < lastres; j++)
          {
            if (gs.getLength() <= j)
            {
              continue;
            }

            if (!jalview.util.Comparison.isGap(gs.getCharAt(j)))
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
          editCommand.appendEdit(Action.INSERT_GAP, groupSeqs, startres,
                  startres - lastres, av.getAlignment(), true);
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
          editCommand.appendEdit(Action.DELETE_GAP, groupSeqs, startres,
                  lastres - startres, av.getAlignment(), true);
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
          editCommand.appendEdit(Action.INSERT_GAP,
                  new SequenceI[] { seq }, lastres, startres - lastres,
                  av.getAlignment(), true);
        }
      }
      else
      {
        // dragging to the left
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j > startres; j--)
          {
            if (!jalview.util.Comparison.isGap(seq.getCharAt(startres)))
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
            if (!jalview.util.Comparison.isGap(seq.getCharAt(m)))
            {
              break;
            }
            max++;
          }

          if (max > 0)
          {
            editCommand.appendEdit(Action.DELETE_GAP,
                    new SequenceI[] { seq }, startres, max,
                    av.getAlignment(), true);
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
        if (jalview.util.Comparison.isGap(seq[s].getCharAt(blankColumn)))
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

    editCommand.appendEdit(Action.DELETE_GAP, seq, blankColumn, 1,
            av.getAlignment(), true);

    editCommand.appendEdit(Action.INSERT_GAP, seq, j, 1, av.getAlignment(),
            true);

  }

  void deleteChar(int j, SequenceI[] seq, int fixedColumn)
  {

    editCommand.appendEdit(Action.DELETE_GAP, seq, j, 1, av.getAlignment(),
            true);

    editCommand.appendEdit(Action.INSERT_GAP, seq, fixedColumn, 1,
            av.getAlignment(), true);
  }

  // ////////////////////////////////////////
  // ///Everything below this is for defining the boundary of the rubberband
  // ////////////////////////////////////////
  public void doMousePressedDefineMode(MouseEvent evt)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
      scrollThread = null;
    }

    int res = findRes(evt);
    int seq = findSeq(evt);
    oldSeq = seq;
    startWrapBlock = wrappedBlock;

    if (seq == -1)
    {
      return;
    }

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    if (sequence == null || res > sequence.getLength())
    {
      return;
    }

    stretchGroup = av.getSelectionGroup();

    if (stretchGroup == null)
    {
      stretchGroup = av.getAlignment().findGroup(sequence);
      if (stretchGroup != null && res > stretchGroup.getStartRes()
              && res < stretchGroup.getEndRes())
      {
        av.setSelectionGroup(stretchGroup);
      }
      else
      {
        stretchGroup = null;
      }
    }

    else if (!stretchGroup.getSequences(null).contains(sequence)
            || stretchGroup.getStartRes() > res
            || stretchGroup.getEndRes() < res)
    {
      stretchGroup = null;

      SequenceGroup[] allGroups = av.getAlignment().findAllGroups(sequence);

      if (allGroups != null)
      {
        for (int i = 0; i < allGroups.length; i++)
        {
          if (allGroups[i].getStartRes() <= res
                  && allGroups[i].getEndRes() >= res)
          {
            stretchGroup = allGroups[i];
            break;
          }
        }
      }
      av.setSelectionGroup(stretchGroup);
    }

    // DETECT RIGHT MOUSE BUTTON IN AWT
    if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
    {
      SequenceFeature[] allFeatures = findFeaturesAtRes(sequence,
              sequence.findPosition(res));

      Vector<String> links = null;
      if (allFeatures != null)
      {
        for (int i = 0; i < allFeatures.length; i++)
        {
          if (allFeatures[i].links != null)
          {
            if (links == null)
            {
              links = new Vector<String>();
            }
            for (int j = 0; j < allFeatures[i].links.size(); j++)
            {
              links.addElement(allFeatures[i].links.elementAt(j));
            }
          }
        }
      }
      APopupMenu popup = new APopupMenu(ap, null, links);
      this.add(popup);
      popup.show(this, evt.getX(), evt.getY());
      return;
    }

    if (av.cursorMode)
    {
      seqCanvas.cursorX = findRes(evt);
      seqCanvas.cursorY = findSeq(evt);
      seqCanvas.repaint();
      return;
    }

    // Only if left mouse button do we want to change group sizes

    if (stretchGroup == null)
    {
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

    }
  }

  public void doMouseReleasedDefineMode(MouseEvent evt)
  {
    if (stretchGroup == null)
    {
      return;
    }
    // always do this - annotation has own state
    // but defer colourscheme update until hidden sequences are passed in
    boolean vischange = stretchGroup.recalcConservation(true);
    // here we rely on stretchGroup == av.getSelection()
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
    PaintRefresher.Refresh(ap, av.getSequenceSetId());
    ap.paintAlignment(needOverviewUpdate);
    needOverviewUpdate = false;
    changeEndRes = false;
    changeStartRes = false;
    stretchGroup = null;
    av.sendSelection();
  }

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

    mouseDragging = true;

    if (y > av.getAlignment().getHeight())
    {
      y = av.getAlignment().getHeight() - 1;
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

    if (res < 0)
    {
      res = 0;
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

    if (res > av.endRes || res < av.startRes || y < av.startSeq
            || y > av.endSeq)
    {
      mouseExited(evt);
    }

    if (scrollThread != null)
    {
      scrollThread.setEvent(evt);
    }

    seqCanvas.repaint();
  }

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

  @Override
  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging && scrollThread == null)
    {
      scrollThread = new ScrollThread();
    }
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

          if (mouseDragging && evt.getY() < 0 && av.getStartSeq() > 0)
          {
            running = ap.scrollUp(true);
          }

          if (mouseDragging && evt.getY() >= getSize().height
                  && av.getAlignment().getHeight() > av.getEndSeq())
          {
            running = ap.scrollUp(false);
          }

          if (mouseDragging && evt.getX() < 0)
          {
            running = ap.scrollRight(false);
          }

          else if (mouseDragging && evt.getX() >= getSize().width)
          {
            running = ap.scrollRight(true);
          }
        }

        try
        {
          Thread.sleep(75);
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
    if (av != null
            && (av == source || !av.followSelection || (source instanceof AlignViewport && ((AlignmentViewport) source)
                    .getSequenceSetId().equals(av.getSequenceSetId()))))
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
    if (av.getSelectionGroup() == null || !av.isSelectionGroupChanged(true))
    {
      SequenceGroup sgroup = null;
      if (seqsel != null && seqsel.getSize() > 0)
      {
        if (av.getAlignment() == null)
        {
          System.out
                  .println("Selection message: alignviewport av SeqSetId="
                          + av.getSequenceSetId() + " ViewId="
                          + av.getViewId()
                          + " 's alignment is NULL! returning immediatly.");
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
      repaint = av.isSelectionGroupChanged(true);
    }
    if (copycolsel
            && (av.getColumnSelection() == null || !av
                    .isColSelChanged(true)))
    {
      // the current selection is unset or from a previous message
      // so import the new colsel.
      if (colsel == null || colsel.isEmpty())
      {
        if (av.getColumnSelection() != null)
        {
          av.getColumnSelection().clear();
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
      repaint |= av.isColSelChanged(true);
    }
    if (copycolsel
            && av.hasHiddenColumns()
            && (av.getColumnSelection() == null || av.getColumnSelection()
                    .getHiddenColumns() == null))
    {
      System.err.println("Bad things");
    }
    if (repaint)
    {
      ap.scalePanelHolder.repaint();
      ap.repaint();
    }
  }

  /**
   * scroll to the given row/column - or nearest visible location
   * 
   * @param row
   * @param column
   */
  public void scrollTo(int row, int column)
  {

    row = row < 0 ? ap.av.startSeq : row;
    column = column < 0 ? ap.av.startRes : column;
    ap.scrollTo(column, column, row, true, true);
  }

  /**
   * scroll to the given row - or nearest visible location
   * 
   * @param row
   */
  public void scrollToRow(int row)
  {

    row = row < 0 ? ap.av.startSeq : row;
    ap.scrollTo(ap.av.startRes, ap.av.startRes, row, true, true);
  }

  /**
   * scroll to the given column - or nearest visible location
   * 
   * @param column
   */
  public void scrollToColumn(int column)
  {

    column = column < 0 ? ap.av.startRes : column;
    ap.scrollTo(column, column, ap.av.startSeq, true, true);
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

    ap.scalePanelHolder.repaint();
    ap.repaint();

    return true;
  }

}
