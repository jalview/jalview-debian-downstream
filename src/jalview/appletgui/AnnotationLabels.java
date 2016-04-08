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
import java.awt.event.*;
import java.awt.image.BufferedImage;

import jalview.datamodel.*;
import jalview.util.ParseHtmlBodyAndLinks;

public class AnnotationLabels extends Panel implements ActionListener,
        MouseListener, MouseMotionListener
{
  Image image;

  boolean active = false;

  AlignmentPanel ap;

  AlignViewport av;

  boolean resizing = false;

  int oldY, mouseX;

  static String ADDNEW = "Add New Row";

  static String EDITNAME = "Edit Label/Description";

  static String HIDE = "Hide This Row";

  static String SHOWALL = "Show All Hidden Rows";

  static String OUTPUT_TEXT = "Show Values In Textbox";

  static String COPYCONS_SEQ = "Copy Consensus Sequence";

  int scrollOffset = 0;

  int selectedRow = -1;

  Tooltip tooltip;

  private boolean hasHiddenRows;

  public AnnotationLabels(AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
    setLayout(null);

    /**
     * this retrieves the adjustable height glyph from resources. we don't use
     * it at the moment. java.net.URL url =
     * getClass().getResource("/images/idwidth.gif"); Image temp = null;
     * 
     * if (url != null) { temp =
     * java.awt.Toolkit.getDefaultToolkit().createImage(url); }
     * 
     * try { MediaTracker mt = new MediaTracker(this); mt.addImage(temp, 0);
     * mt.waitForID(0); } catch (Exception ex) { }
     * 
     * BufferedImage bi = new BufferedImage(temp.getHeight(this),
     * temp.getWidth(this), BufferedImage.TYPE_INT_RGB); Graphics2D g =
     * (Graphics2D) bi.getGraphics(); g.rotate(Math.toRadians(90));
     * g.drawImage(temp, 0, -bi.getWidth(this), this); image = (Image) bi;
     */
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public AnnotationLabels(AlignViewport av)
  {
    this.av = av;
  }

  public void setScrollOffset(int y)
  {
    scrollOffset = y;
    repaint();
  }

  /**
   * 
   * @param y
   * @return -2 if no rows are visible at all, -1 if no visible rows were
   *         selected
   */
  int getSelectedRow(int y)
  {
    int row = -2;
    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    if (aa == null)
    {
      return row;
    }
    int height = 0;
    for (int i = 0; i < aa.length; i++)
    {
      row = -1;
      if (!aa[i].visible)
      {
        continue;
      }
      height += aa[i].height;
      if (y < height)
      {
        row = i;
        break;
      }
    }

    return row;
  }

  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();

    if (evt.getActionCommand().equals(ADDNEW))
    {
      AlignmentAnnotation newAnnotation = new AlignmentAnnotation("", null,
              new Annotation[ap.av.alignment.getWidth()]);

      if (!editLabelDescription(newAnnotation))
      {
        return;
      }

      ap.av.alignment.addAnnotation(newAnnotation);
      ap.av.alignment.setAnnotationIndex(newAnnotation, 0);
    }
    else if (evt.getActionCommand().equals(EDITNAME))
    {
      editLabelDescription(aa[selectedRow]);
    }
    else if (evt.getActionCommand().equals(HIDE))
    {
      aa[selectedRow].visible = false;
    }
    else if (evt.getActionCommand().equals(SHOWALL))
    {
      for (int i = 0; i < aa.length; i++)
      {
        aa[i].visible = (aa[i].annotations == null) ? false : true;
      }
    }
    else if (evt.getActionCommand().equals(OUTPUT_TEXT))
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false,
              ap.alignFrame);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame, ap.alignFrame.getTitle()
              + " - " + aa[selectedRow].label, 500, 100);
      cap.setText(aa[selectedRow].toString());
    }
    else if (evt.getActionCommand().equals(COPYCONS_SEQ))
    {
      SequenceI cons = av.getConsensusSeq();
      if (cons != null)
      {
        copy_annotseqtoclipboard(cons);
      }

    }
    ap.annotationPanel.adjustPanelHeight();
    setSize(getSize().width, ap.annotationPanel.getSize().height);
    ap.validate();
    ap.paintAlignment(true);
  }

  boolean editLabelDescription(AlignmentAnnotation annotation)
  {
    Checkbox padGaps = new Checkbox("Fill Empty Gaps With \""
            + ap.av.getGapCharacter() + "\"", annotation.padGaps);

    EditNameDialog dialog = new EditNameDialog(annotation.label,
            annotation.description, "      Annotation Label",
            "Annotation Description", ap.alignFrame,
            "Edit Annotation Name / Description", 500, 180, false);

    Panel empty = new Panel(new FlowLayout());
    empty.add(padGaps);
    dialog.add(empty);
    dialog.pack();

    dialog.setVisible(true);

    if (dialog.accept)
    {
      annotation.label = dialog.getName();
      annotation.description = dialog.getDescription();
      annotation.setPadGaps(padGaps.getState(), av.getGapCharacter());
      repaint();
      return true;
    }
    else
      return false;

  }

  boolean resizePanel = false;

  public void mouseMoved(MouseEvent evt)
  {
    resizePanel = evt.getY() < 10 && evt.getX() < 14;
    int row = getSelectedRow(evt.getY() + scrollOffset);

    if (row > -1)
    {
      ParseHtmlBodyAndLinks phb = new ParseHtmlBodyAndLinks(av.alignment.getAlignmentAnnotation()[row].getDescription(true), true, "\n");
      if (tooltip == null)
      {
        tooltip = new Tooltip(phb.getNonHtmlContent(), this);
      }
      else
      {
        tooltip.setTip(phb.getNonHtmlContent());
      }
    }
    else if (tooltip != null)
    {
      tooltip.setTip("");
    }
  }

  /**
   * curent drag position
   */
  MouseEvent dragEvent = null;

  /**
   * flag to indicate drag events should be ignored
   */
  private boolean dragCancelled = false;

  /**
   * clear any drag events in progress
   */
  public void cancelDrag()
  {
    dragEvent = null;
    dragCancelled = true;
  }

  public void mouseDragged(MouseEvent evt)
  {
    if (dragCancelled)
    {
      return;
    }
    ;
    dragEvent = evt;

    if (resizePanel)
    {
      Dimension d = ap.annotationPanelHolder.getSize(), e = ap.annotationSpaceFillerHolder
              .getSize(), f = ap.seqPanelHolder.getSize();
      int dif = evt.getY() - oldY;

      dif /= ap.av.charHeight;
      dif *= ap.av.charHeight;

      if ((d.height - dif) > 20 && (f.height + dif) > 20)
      {
        ap.annotationPanel.setSize(d.width, d.height - dif);
        setSize(new Dimension(e.width, d.height - dif));
        ap.annotationSpaceFillerHolder.setSize(new Dimension(e.width,
                d.height - dif));
        ap.annotationPanelHolder.setSize(new Dimension(d.width, d.height
                - dif));
        ap.apvscroll.setValues(ap.apvscroll.getValue(), d.height - dif, 0,
                ap.annotationPanel.calcPanelHeight());
        f.height += dif;
        ap.seqPanelHolder.setPreferredSize(f);
        ap.setScrollValues(av.getStartRes(), av.getStartSeq());
        ap.validate();
        // ap.paintAlignment(true);
        ap.addNotify();
      }

    }
    else
    {
      int diff;
      if ((diff = 6 - evt.getY()) > 0)
      {
        // nudge scroll up
        ap.apvscroll.setValue(ap.apvscroll.getValue() - diff);
        ap.adjustmentValueChanged(null);

      }
      else if ((0 < (diff = 6
              - ap.annotationSpaceFillerHolder.getSize().height
              + evt.getY())))
      {
        // nudge scroll down
        ap.apvscroll.setValue(ap.apvscroll.getValue() + diff);
        ap.adjustmentValueChanged(null);
      }
      repaint();
    }
  }

  public void mouseClicked(MouseEvent evt)
  {
  }

  public void mouseReleased(MouseEvent evt)
  {
    if (!resizePanel && !dragCancelled)
    {
      int start = selectedRow;

      int end = getSelectedRow(evt.getY() + scrollOffset);

      if (start>-1 && start != end)
      {
        // Swap these annotations
        AlignmentAnnotation startAA = ap.av.alignment
                .getAlignmentAnnotation()[start];
        if (end == -1)
        {
          end = ap.av.alignment.getAlignmentAnnotation().length - 1;
        }
        AlignmentAnnotation endAA = ap.av.alignment
                .getAlignmentAnnotation()[end];

        ap.av.alignment.getAlignmentAnnotation()[end] = startAA;
        ap.av.alignment.getAlignmentAnnotation()[start] = endAA;
      }
    }
    resizePanel = false;
    dragEvent = null;
    dragCancelled = false;
    repaint();
    ap.annotationPanel.repaint();
  }

  public void mouseEntered(MouseEvent evt)
  {
    if (evt.getY() < 10 && evt.getX() < 14)
    {
      resizePanel = true;
      repaint();
    }
  }

  public void mouseExited(MouseEvent evt)
  {
    dragCancelled = false;

    if (dragEvent == null)
    {
      resizePanel = false;
    }
    else
    {
      if (!resizePanel)
      {
        dragEvent = null;
      }
    }
    repaint();
  }

  public void mousePressed(MouseEvent evt)
  {
    oldY = evt.getY();
    if (resizePanel)
    {
      return;
    }
    dragCancelled=false;
    // todo: move below to mouseClicked ?
    selectedRow = getSelectedRow(evt.getY() + scrollOffset);

    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    // DETECT RIGHT MOUSE BUTTON IN AWT
    if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
    {

      PopupMenu popup = new PopupMenu("Annotations");

      MenuItem item = new MenuItem(ADDNEW);
      item.addActionListener(this);
      popup.add(item);
      if (selectedRow < 0)
      {
        // this never happens at moment: - see comment on JAL-563
        if (hasHiddenRows)
        {
          item = new MenuItem(SHOWALL);
          item.addActionListener(this);
          popup.add(item);
        }
        this.add(popup);
        popup.show(this, evt.getX(), evt.getY());
        return;
      }
      // add the rest if there are actually rows to show
      item = new MenuItem(EDITNAME);
      item.addActionListener(this);
      popup.add(item);
      item = new MenuItem(HIDE);
      item.addActionListener(this);
      popup.add(item);
      if (hasHiddenRows)
      {
        item = new MenuItem(SHOWALL);
        item.addActionListener(this);
        popup.add(item);
      }
      this.add(popup);
      item = new MenuItem(OUTPUT_TEXT);
      item.addActionListener(this);
      popup.add(item);
      if (selectedRow < aa.length)
      {
        if (aa[selectedRow].autoCalculated)
        {
          if (aa[selectedRow].label.indexOf("Consensus") > -1)
          {
            popup.addSeparator();
            final CheckboxMenuItem cbmi = new CheckboxMenuItem(
                    "Ignore Gaps In Consensus",
                    (aa[selectedRow].groupRef != null) ? aa[selectedRow].groupRef
                            .getIgnoreGapsConsensus() : ap.av
                            .getIgnoreGapsConsensus());
            final AlignmentAnnotation aaa = aa[selectedRow];
            cbmi.addItemListener(new ItemListener()
            {
              public void itemStateChanged(ItemEvent e)
              {
                if (aaa.groupRef != null)
                {
                  // TODO: pass on reference to ap so the view can be updated.
                  aaa.groupRef.setIgnoreGapsConsensus(cbmi.getState());
                }
                else
                {
                  ap.av.setIgnoreGapsConsensus(cbmi.getState());
                }
                ap.paintAlignment(true);
              }
            });
            popup.add(cbmi);
            if (aaa.groupRef != null)
            {
              final CheckboxMenuItem chist = new CheckboxMenuItem(
                      "Show Group Histogram",
                      aa[selectedRow].groupRef.isShowConsensusHistogram());
              chist.addItemListener(new ItemListener()
              {
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  aaa.groupRef.setShowConsensusHistogram(chist.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(chist);
              final CheckboxMenuItem cprofl = new CheckboxMenuItem(
                      "Show Group Logo",
                      aa[selectedRow].groupRef.isShowSequenceLogo());
              cprofl.addItemListener(new ItemListener()
              {
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  aaa.groupRef.setshowSequenceLogo(cprofl.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(cprofl);
            }
            else
            {
              final CheckboxMenuItem chist = new CheckboxMenuItem(
                      "Show Histogram", av.isShowConsensusHistogram());
              chist.addItemListener(new ItemListener()
              {
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  av.setShowConsensusHistogram(chist.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(chist);
              final CheckboxMenuItem cprof = new CheckboxMenuItem(
                      "Show Logo", av.isShowSequenceLogo());
              cprof.addItemListener(new ItemListener()
              {
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  av.setShowSequenceLogo(cprof.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(cprof);
            }

            item = new MenuItem(COPYCONS_SEQ);
            item.addActionListener(this);
            popup.add(item);
          }
        }
      }
      popup.show(this, evt.getX(), evt.getY());
    }
    else
    {
      // selection action.
      if (selectedRow > -1 && selectedRow < aa.length)
      {
        if (aa[selectedRow].groupRef != null)
        {
          if (evt.getClickCount() >= 2)
          {
            // todo: make the ap scroll to the selection - not necessary, first
            // click highlights/scrolls, second selects
            ap.seqPanel.ap.idPanel.highlightSearchResults(null);
            ap.av.setSelectionGroup(// new SequenceGroup(
            aa[selectedRow].groupRef); // );
            ap.av.sendSelection();
            ap.paintAlignment(false);
            PaintRefresher.Refresh(ap, ap.av.getSequenceSetId());
          }
          else
          {
            ap.seqPanel.ap.idPanel
                    .highlightSearchResults(aa[selectedRow].groupRef
                            .getSequences(null));
          }
          return;
        }
        else if (aa[selectedRow].sequenceRef != null)
        {
          Vector sr = new Vector();
          sr.addElement(aa[selectedRow].sequenceRef);
          if (evt.getClickCount() == 1)
          {
            ap.seqPanel.ap.idPanel.highlightSearchResults(sr);
          }
          else if (evt.getClickCount() >= 2)
          {
            ap.seqPanel.ap.idPanel.highlightSearchResults(null);
            SequenceGroup sg = new SequenceGroup();
            sg.addSequence(aa[selectedRow].sequenceRef, false);
            ap.av.setSelectionGroup(sg);
            ap.paintAlignment(false);
            PaintRefresher.Refresh(ap, ap.av.getSequenceSetId());
            ap.av.sendSelection();
          }

        }
      }

    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void copy_annotseqtoclipboard(SequenceI sq)
  {
    if (sq == null || sq.getLength() < 1)
    {
      return;
    }
    jalview.appletgui.AlignFrame.copiedSequences = new StringBuffer();
    jalview.appletgui.AlignFrame.copiedSequences.append(sq.getName() + "\t"
            + sq.getStart() + "\t" + sq.getEnd() + "\t"
            + sq.getSequenceAsString() + "\n");
    if (av.hasHiddenColumns)
    {
      jalview.appletgui.AlignFrame.copiedHiddenColumns = new Vector();
      for (int i = 0; i < av.getColumnSelection().getHiddenColumns().size(); i++)
      {
        int[] region = (int[]) av.getColumnSelection().getHiddenColumns()
                .elementAt(i);

        jalview.appletgui.AlignFrame.copiedHiddenColumns
                .addElement(new int[]
                { region[0], region[1] });
      }
    }
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    int w = getSize().width;
    int h = getSize().height;
    if (image == null || w != image.getWidth(this) || h!=image.getHeight(this) )
    {
      image = createImage(w, ap.annotationPanel.getSize().height);
    }

    drawComponent(image.getGraphics(), w);
    g.drawImage(image, 0, 0, this);
  }

  public void drawComponent(Graphics g, int width)
  {
    g.setFont(av.getFont());
    FontMetrics fm = g.getFontMetrics(av.getFont());
    g.setColor(Color.white);
    g.fillRect(0, 0, getSize().width, getSize().height);

    g.translate(0, -scrollOffset);
    g.setColor(Color.black);

    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
    int y = 0, fy = g.getFont().getSize();
    int x = 0, offset;

    if (aa != null)
    {
      hasHiddenRows = false;
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible)
        {
          hasHiddenRows = true;
          continue;
        }

        x = width - fm.stringWidth(aa[i].label) - 3;

        y += aa[i].height;
        offset = -(aa[i].height - fy) / 2;

        g.drawString(aa[i].label, x, y + offset);
      }
    }
    g.translate(0, +scrollOffset);
    if (resizePanel)
    {
      g.setColor(Color.red);
      g.setPaintMode();
      g.drawLine(2, 8, 5, 2);
      g.drawLine(5, 2, 8, 8);
    }
    else if (!dragCancelled && dragEvent != null && aa != null)
    {
      g.setColor(Color.lightGray);
      g.drawString(aa[selectedRow].label, dragEvent.getX(),
              dragEvent.getY());
    }

    if ((aa == null) || (aa.length < 1))
    {
      g.setColor(Color.black);
      g.drawString("Right click", 2, 8);
      g.drawString("to add annotation", 2, 18);
    }
  }
}
