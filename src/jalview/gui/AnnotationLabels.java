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

import jalview.analysis.AlignmentUtils;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.FormatAdapter;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class AnnotationLabels extends JPanel implements MouseListener,
        MouseMotionListener, ActionListener
{
  private static final Pattern LEFT_ANGLE_BRACKET_PATTERN = Pattern
          .compile("<");

  String TOGGLE_LABELSCALE = MessageManager
          .getString("label.scale_label_to_column");

  String ADDNEW = MessageManager.getString("label.add_new_row");

  String EDITNAME = MessageManager
          .getString("label.edit_label_description");

  String HIDE = MessageManager.getString("label.hide_row");

  String DELETE = MessageManager.getString("label.delete_row");

  String SHOWALL = MessageManager.getString("label.show_all_hidden_rows");

  String OUTPUT_TEXT = MessageManager.getString("label.export_annotation");

  String COPYCONS_SEQ = MessageManager
          .getString("label.copy_consensus_sequence");

  boolean resizePanel = false;

  Image image;

  AlignmentPanel ap;

  AlignViewport av;

  boolean resizing = false;

  MouseEvent dragEvent;

  int oldY;

  int selectedRow;

  private int scrollOffset = 0;

  Font font = new Font("Arial", Font.PLAIN, 11);

  private boolean hasHiddenRows;

  /**
   * Creates a new AnnotationLabels object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public AnnotationLabels(AlignmentPanel ap)
  {
    this.ap = ap;
    av = ap.av;
    ToolTipManager.sharedInstance().registerComponent(this);

    java.net.URL url = getClass().getResource("/images/idwidth.gif");
    Image temp = null;

    if (url != null)
    {
      temp = java.awt.Toolkit.getDefaultToolkit().createImage(url);
    }

    try
    {
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(temp, 0);
      mt.waitForID(0);
    } catch (Exception ex)
    {
    }

    BufferedImage bi = new BufferedImage(temp.getHeight(this),
            temp.getWidth(this), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) bi.getGraphics();
    g.rotate(Math.toRadians(90));
    g.drawImage(temp, 0, -bi.getWidth(this), this);
    image = bi;

    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(ap.getAnnotationPanel());
  }

  public AnnotationLabels(AlignViewport av)
  {
    this.av = av;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param y
   *          DOCUMENT ME!
   */
  public void setScrollOffset(int y)
  {
    scrollOffset = y;
    repaint();
  }

  /**
   * sets selectedRow to -2 if no annotation preset, -1 if no visible row is at
   * y
   * 
   * @param y
   *          coordinate position to search for a row
   */
  void getSelectedRow(int y)
  {
    int height = 0;
    AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();
    selectedRow = -2;
    if (aa != null)
    {
      for (int i = 0; i < aa.length; i++)
      {
        selectedRow = -1;
        if (!aa[i].visible)
        {
          continue;
        }

        height += aa[i].height;

        if (y < height)
        {
          selectedRow = i;

          break;
        }
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
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();

    boolean fullRepaint = false;
    if (evt.getActionCommand().equals(ADDNEW))
    {
      AlignmentAnnotation newAnnotation = new AlignmentAnnotation(null,
              null, new Annotation[ap.av.getAlignment().getWidth()]);

      if (!editLabelDescription(newAnnotation))
      {
        return;
      }

      ap.av.getAlignment().addAnnotation(newAnnotation);
      ap.av.getAlignment().setAnnotationIndex(newAnnotation, 0);
      fullRepaint = true;
    }
    else if (evt.getActionCommand().equals(EDITNAME))
    {
      String name = aa[selectedRow].label;
      editLabelDescription(aa[selectedRow]);
      if (!name.equalsIgnoreCase(aa[selectedRow].label))
      {
        fullRepaint = true;
      }
    }
    else if (evt.getActionCommand().equals(HIDE))
    {
      aa[selectedRow].visible = false;
    }
    else if (evt.getActionCommand().equals(DELETE))
    {
      ap.av.getAlignment().deleteAnnotation(aa[selectedRow]);
      ap.av.getCalcManager().removeWorkerForAnnotation(aa[selectedRow]);
      fullRepaint = true;
    }
    else if (evt.getActionCommand().equals(SHOWALL))
    {
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible && aa[i].annotations != null)
        {
          aa[i].visible = true;
        }
      }
      fullRepaint = true;
    }
    else if (evt.getActionCommand().equals(OUTPUT_TEXT))
    {
      new AnnotationExporter().exportAnnotations(ap,
              new AlignmentAnnotation[] { aa[selectedRow] });
    }
    else if (evt.getActionCommand().equals(COPYCONS_SEQ))
    {
      SequenceI cons = null;
      if (aa[selectedRow].groupRef != null)
      {
        cons = aa[selectedRow].groupRef.getConsensusSeq();
      }
      else
      {
        cons = av.getConsensusSeq();
      }
      if (cons != null)
      {
        copy_annotseqtoclipboard(cons);
      }

    }
    else if (evt.getActionCommand().equals(TOGGLE_LABELSCALE))
    {
      aa[selectedRow].scaleColLabel = !aa[selectedRow].scaleColLabel;
    }

    refresh(fullRepaint);

  }

  /**
   * Redraw sensibly.
   * 
   * @adjustHeight if true, try to recalculate panel height for visible
   *               annotations
   */
  protected void refresh(boolean adjustHeight)
  {
    ap.validateAnnotationDimensions(adjustHeight);
    ap.addNotify();
    if (adjustHeight)
    {
      // sort, repaint, update overview
      ap.paintAlignment(true);
    }
    else
    {
      // lightweight repaint
      ap.repaint();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  boolean editLabelDescription(AlignmentAnnotation annotation)
  {
    // TODO i18n
    EditNameDialog dialog = new EditNameDialog(annotation.label,
            annotation.description, "       Annotation Name ",
            "Annotation Description ", "Edit Annotation Name/Description",
            ap.alignFrame);

    if (!dialog.accept)
    {
      return false;
    }

    annotation.label = dialog.getName();

    String text = dialog.getDescription();
    if (text != null && text.length() == 0)
    {
      text = null;
    }
    annotation.description = text;

    return true;
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    getSelectedRow(evt.getY() - getScrollOffset());
    oldY = evt.getY();
    if (evt.isPopupTrigger())
    {
      showPopupMenu(evt);
    }
  }

  /**
   * Build and show the Pop-up menu at the right-click mouse position
   * 
   * @param evt
   */
  void showPopupMenu(MouseEvent evt)
  {
    evt.consume();
    final AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();

    JPopupMenu pop = new JPopupMenu(
            MessageManager.getString("label.annotations"));
    JMenuItem item = new JMenuItem(ADDNEW);
    item.addActionListener(this);
    pop.add(item);
    if (selectedRow < 0)
    {
      if (hasHiddenRows)
      { // let the user make everything visible again
        item = new JMenuItem(SHOWALL);
        item.addActionListener(this);
        pop.add(item);
      }
      pop.show(this, evt.getX(), evt.getY());
      return;
    }
    item = new JMenuItem(EDITNAME);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(HIDE);
    item.addActionListener(this);
    pop.add(item);
    // JAL-1264 hide all sequence-specific annotations of this type
    if (selectedRow < aa.length)
    {
      if (aa[selectedRow].sequenceRef != null)
      {
        final String label = aa[selectedRow].label;
        JMenuItem hideType = new JMenuItem();
        String text = MessageManager.getString("label.hide_all") + " "
                + label;
        hideType.setText(text);
        hideType.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            AlignmentUtils.showOrHideSequenceAnnotations(
                    ap.av.getAlignment(), Collections.singleton(label),
                    null, false, false);
            // for (AlignmentAnnotation ann : ap.av.getAlignment()
            // .getAlignmentAnnotation())
            // {
            // if (ann.sequenceRef != null && ann.label != null
            // && ann.label.equals(label))
            // {
            // ann.visible = false;
            // }
            // }
            refresh(true);
          }
        });
        pop.add(hideType);
      }
    }
    item = new JMenuItem(DELETE);
    item.addActionListener(this);
    pop.add(item);
    if (hasHiddenRows)
    {
      item = new JMenuItem(SHOWALL);
      item.addActionListener(this);
      pop.add(item);
    }
    item = new JMenuItem(OUTPUT_TEXT);
    item.addActionListener(this);
    pop.add(item);
    // TODO: annotation object should be typed for autocalculated/derived
    // property methods
    if (selectedRow < aa.length)
    {
      final String label = aa[selectedRow].label;
      if (!aa[selectedRow].autoCalculated)
      {
        if (aa[selectedRow].graph == AlignmentAnnotation.NO_GRAPH)
        {
          // display formatting settings for this row.
          pop.addSeparator();
          // av and sequencegroup need to implement same interface for
          item = new JCheckBoxMenuItem(TOGGLE_LABELSCALE,
                  aa[selectedRow].scaleColLabel);
          item.addActionListener(this);
          pop.add(item);
        }
      }
      else if (label.indexOf("Consensus") > -1)
      {
        pop.addSeparator();
        // av and sequencegroup need to implement same interface for
        final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(
                MessageManager.getString("label.ignore_gaps_consensus"),
                (aa[selectedRow].groupRef != null) ? aa[selectedRow].groupRef
                        .getIgnoreGapsConsensus() : ap.av
                        .isIgnoreGapsConsensus());
        final AlignmentAnnotation aaa = aa[selectedRow];
        cbmi.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            if (aaa.groupRef != null)
            {
              // TODO: pass on reference to ap so the view can be updated.
              aaa.groupRef.setIgnoreGapsConsensus(cbmi.getState());
              ap.getAnnotationPanel().paint(
                      ap.getAnnotationPanel().getGraphics());
            }
            else
            {
              ap.av.setIgnoreGapsConsensus(cbmi.getState(), ap);
            }
            ap.alignmentChanged();
          }
        });
        pop.add(cbmi);
        // av and sequencegroup need to implement same interface for
        if (aaa.groupRef != null)
        {
          final JCheckBoxMenuItem chist = new JCheckBoxMenuItem(
                  MessageManager.getString("label.show_group_histogram"),
                  aa[selectedRow].groupRef.isShowConsensusHistogram());
          chist.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
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
          pop.add(chist);
          final JCheckBoxMenuItem cprofl = new JCheckBoxMenuItem(
                  MessageManager.getString("label.show_group_logo"),
                  aa[selectedRow].groupRef.isShowSequenceLogo());
          cprofl.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
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
          pop.add(cprofl);
          final JCheckBoxMenuItem cproflnorm = new JCheckBoxMenuItem(
                  MessageManager.getString("label.normalise_group_logo"),
                  aa[selectedRow].groupRef.isNormaliseSequenceLogo());
          cproflnorm.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {

              // TODO: pass on reference
              // to ap
              // so the
              // view
              // can be
              // updated.
              aaa.groupRef.setNormaliseSequenceLogo(cproflnorm.getState());
              // automatically enable logo display if we're clicked
              aaa.groupRef.setshowSequenceLogo(true);
              ap.repaint();
              // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
            }
          });
          pop.add(cproflnorm);
        }
        else
        {
          final JCheckBoxMenuItem chist = new JCheckBoxMenuItem(
                  MessageManager.getString("label.show_histogram"),
                  av.isShowConsensusHistogram());
          chist.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              // TODO: pass on reference
              // to ap
              // so the
              // view
              // can be
              // updated.
              av.setShowConsensusHistogram(chist.getState());
              ap.alignFrame.setMenusForViewport();
              ap.repaint();
              // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
            }
          });
          pop.add(chist);
          final JCheckBoxMenuItem cprof = new JCheckBoxMenuItem(
                  MessageManager.getString("label.show_logo"),
                  av.isShowSequenceLogo());
          cprof.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              // TODO: pass on reference
              // to ap
              // so the
              // view
              // can be
              // updated.
              av.setShowSequenceLogo(cprof.getState());
              ap.alignFrame.setMenusForViewport();
              ap.repaint();
              // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
            }
          });
          pop.add(cprof);
          final JCheckBoxMenuItem cprofnorm = new JCheckBoxMenuItem(
                  MessageManager.getString("label.normalise_logo"),
                  av.isNormaliseSequenceLogo());
          cprofnorm.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              // TODO: pass on reference
              // to ap
              // so the
              // view
              // can be
              // updated.
              av.setShowSequenceLogo(true);
              av.setNormaliseSequenceLogo(cprofnorm.getState());
              ap.alignFrame.setMenusForViewport();
              ap.repaint();
              // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
            }
          });
          pop.add(cprofnorm);
        }
        final JMenuItem consclipbrd = new JMenuItem(COPYCONS_SEQ);
        consclipbrd.addActionListener(this);
        pop.add(consclipbrd);
      }
    }
    pop.show(this, evt.getX(), evt.getY());
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
    if (evt.isPopupTrigger())
    {
      showPopupMenu(evt);
      return;
    }

    int start = selectedRow;
    getSelectedRow(evt.getY() - getScrollOffset());
    int end = selectedRow;

    if (start != end)
    {
      // Swap these annotations
      AlignmentAnnotation startAA = ap.av.getAlignment()
              .getAlignmentAnnotation()[start];
      if (end == -1)
      {
        end = ap.av.getAlignment().getAlignmentAnnotation().length - 1;
      }
      AlignmentAnnotation endAA = ap.av.getAlignment()
              .getAlignmentAnnotation()[end];

      ap.av.getAlignment().getAlignmentAnnotation()[end] = startAA;
      ap.av.getAlignment().getAlignmentAnnotation()[start] = endAA;
    }

    resizePanel = false;
    dragEvent = null;
    repaint();
    ap.getAnnotationPanel().repaint();
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
    if (evt.getY() < 10)
    {
      resizePanel = true;
      repaint();
    }
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
    if (dragEvent == null)
    {
      resizePanel = false;
      repaint();
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
    dragEvent = evt;

    if (resizePanel)
    {
      Dimension d = ap.annotationScroller.getPreferredSize();
      int dif = evt.getY() - oldY;

      dif /= ap.av.getCharHeight();
      dif *= ap.av.getCharHeight();

      if ((d.height - dif) > 20)
      {
        ap.annotationScroller.setPreferredSize(new Dimension(d.width,
                d.height - dif));
        d = ap.annotationSpaceFillerHolder.getPreferredSize();
        ap.annotationSpaceFillerHolder.setPreferredSize(new Dimension(
                d.width, d.height - dif));
        ap.paintAlignment(true);
      }

      ap.addNotify();
    }
    else
    {
      repaint();
    }
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
    resizePanel = evt.getY() < 10;

    getSelectedRow(evt.getY() - getScrollOffset());

    if (selectedRow > -1
            && ap.av.getAlignment().getAlignmentAnnotation().length > selectedRow)
    {
      AlignmentAnnotation aa = ap.av.getAlignment()
              .getAlignmentAnnotation()[selectedRow];

      StringBuffer desc = new StringBuffer();
      if (aa.description != null
              && !aa.description.equals("New description"))
      {
        // TODO: we could refactor and merge this code with the code in
        // jalview.gui.SeqPanel.mouseMoved(..) that formats sequence feature
        // tooltips
        desc.append(aa.getDescription(true).trim());
        // check to see if the description is an html fragment.
        if (desc.length() < 6
                || (desc.substring(0, 6).toLowerCase().indexOf("<html>") < 0))
        {
          // clean the description ready for embedding in html
          desc = new StringBuffer(LEFT_ANGLE_BRACKET_PATTERN.matcher(desc)
                  .replaceAll("&lt;"));
          desc.insert(0, "<html>");
        }
        else
        {
          // remove terminating html if any
          int i = desc.substring(desc.length() - 7).toLowerCase()
                  .lastIndexOf("</html>");
          if (i > -1)
          {
            desc.setLength(desc.length() - 7 + i);
          }
        }
        if (aa.hasScore())
        {
          desc.append("<br/>");
        }
        // if (aa.hasProperties())
        // {
        // desc.append("<table>");
        // for (String prop : aa.getProperties())
        // {
        // desc.append("<tr><td>" + prop + "</td><td>"
        // + aa.getProperty(prop) + "</td><tr>");
        // }
        // desc.append("</table>");
        // }
      }
      else
      {
        // begin the tooltip's html fragment
        desc.append("<html>");
        if (aa.hasScore())
        {
          // TODO: limit precision of score to avoid noise from imprecise
          // doubles
          // (64.7 becomes 64.7+/some tiny value).
          desc.append(" Score: " + aa.score);
        }
      }
      if (desc.length() > 6)
      {
        desc.append("</html>");
        this.setToolTipText(desc.toString());
      }
      else
      {
        this.setToolTipText(null);
      }
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    final AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();
    if (!evt.isPopupTrigger() && SwingUtilities.isLeftMouseButton(evt))
    {
      if (selectedRow > -1 && selectedRow < aa.length)
      {
        if (aa[selectedRow].groupRef != null)
        {
          if (evt.getClickCount() >= 2)
          {
            // todo: make the ap scroll to the selection - not necessary, first
            // click highlights/scrolls, second selects
            ap.getSeqPanel().ap.getIdPanel().highlightSearchResults(null);
            // process modifiers
            SequenceGroup sg = ap.av.getSelectionGroup();
            if (sg == null
                    || sg == aa[selectedRow].groupRef
                    || !(jalview.util.Platform.isControlDown(evt) || evt
                            .isShiftDown()))
            {
              if (jalview.util.Platform.isControlDown(evt)
                      || evt.isShiftDown())
              {
                // clone a new selection group from the associated group
                ap.av.setSelectionGroup(new SequenceGroup(
                        aa[selectedRow].groupRef));
              }
              else
              {
                // set selection to the associated group so it can be edited
                ap.av.setSelectionGroup(aa[selectedRow].groupRef);
              }
            }
            else
            {
              // modify current selection with associated group
              int remainToAdd = aa[selectedRow].groupRef.getSize();
              for (SequenceI sgs : aa[selectedRow].groupRef.getSequences())
              {
                if (jalview.util.Platform.isControlDown(evt))
                {
                  sg.addOrRemove(sgs, --remainToAdd == 0);
                }
                else
                {
                  // notionally, we should also add intermediate sequences from
                  // last added sequence ?
                  sg.addSequence(sgs, --remainToAdd == 0);
                }
              }
            }

            ap.paintAlignment(false);
            PaintRefresher.Refresh(ap, ap.av.getSequenceSetId());
            ap.av.sendSelection();
          }
          else
          {
            ap.getSeqPanel().ap.getIdPanel().highlightSearchResults(
                    aa[selectedRow].groupRef.getSequences(null));
          }
          return;
        }
        else if (aa[selectedRow].sequenceRef != null)
        {
          if (evt.getClickCount() == 1)
          {
            ap.getSeqPanel().ap
                    .getIdPanel()
                    .highlightSearchResults(
                            Arrays.asList(new SequenceI[] { aa[selectedRow].sequenceRef }));
          }
          else if (evt.getClickCount() >= 2)
          {
            ap.getSeqPanel().ap.getIdPanel().highlightSearchResults(null);
            SequenceGroup sg = ap.av.getSelectionGroup();
            if (sg != null)
            {
              // we make a copy rather than edit the current selection if no
              // modifiers pressed
              // see Enhancement JAL-1557
              if (!(jalview.util.Platform.isControlDown(evt) || evt
                      .isShiftDown()))
              {
                sg = new SequenceGroup(sg);
                sg.clear();
                sg.addSequence(aa[selectedRow].sequenceRef, false);
              }
              else
              {
                if (jalview.util.Platform.isControlDown(evt))
                {
                  sg.addOrRemove(aa[selectedRow].sequenceRef, true);
                }
                else
                {
                  // notionally, we should also add intermediate sequences from
                  // last added sequence ?
                  sg.addSequence(aa[selectedRow].sequenceRef, true);
                }
              }
            }
            else
            {
              sg = new SequenceGroup();
              sg.setStartRes(0);
              sg.setEndRes(ap.av.getAlignment().getWidth() - 1);
              sg.addSequence(aa[selectedRow].sequenceRef, false);
            }
            ap.av.setSelectionGroup(sg);
            ap.paintAlignment(false);
            PaintRefresher.Refresh(ap, ap.av.getSequenceSetId());
            ap.av.sendSelection();
          }

        }
      }
      return;
    }
  }

  /**
   * do a single sequence copy to jalview and the system clipboard
   * 
   * @param sq
   *          sequence to be copied to clipboard
   */
  protected void copy_annotseqtoclipboard(SequenceI sq)
  {
    SequenceI[] seqs = new SequenceI[] { sq };
    String[] omitHidden = null;
    SequenceI[] dseqs = new SequenceI[] { sq.getDatasetSequence() };
    if (dseqs[0] == null)
    {
      dseqs[0] = new Sequence(sq);
      dseqs[0].setSequence(jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, sq.getSequenceAsString()));

      sq.setDatasetSequence(dseqs[0]);
    }
    Alignment ds = new Alignment(dseqs);
    if (av.hasHiddenColumns())
    {
      omitHidden = av.getColumnSelection().getVisibleSequenceStrings(0,
              sq.getLength(), seqs);
    }

    int[] alignmentStartEnd = new int[] { 0, ds.getWidth() - 1 };
    List<int[]> hiddenCols = av.getColumnSelection().getHiddenColumns();
    if (hiddenCols != null)
    {
      alignmentStartEnd = av.getAlignment().getVisibleStartAndEndIndex(
              hiddenCols);
    }
    String output = new FormatAdapter().formatSequences("Fasta", seqs,
            omitHidden, alignmentStartEnd);

    Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(output), Desktop.instance);

    ArrayList<int[]> hiddenColumns = null;
    if (av.hasHiddenColumns())
    {
      hiddenColumns = new ArrayList<int[]>();
      for (int[] region : av.getColumnSelection().getHiddenColumns())
      {
        hiddenColumns.add(new int[] { region[0], region[1] });
      }
    }

    Desktop.jalviewClipboard = new Object[] { seqs, ds, // what is the dataset
                                                        // of a consensus
                                                        // sequence ? need to
                                                        // flag
        // sequence as special.
        hiddenColumns };
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g1
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {

    int width = getWidth();
    if (width == 0)
    {
      width = ap.calculateIdWidth().width + 4;
    }

    Graphics2D g2 = (Graphics2D) g;
    if (av.antiAlias)
    {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    drawComponent(g2, true, width);

  }

  /**
   * Draw the full set of annotation Labels for the alignment at the given
   * cursor
   * 
   * @param g
   *          Graphics2D instance (needed for font scaling)
   * @param width
   *          Width for scaling labels
   * 
   */
  public void drawComponent(Graphics g, int width)
  {
    drawComponent(g, false, width);
  }

  private final boolean debugRedraw = false;

  /**
   * Draw the full set of annotation Labels for the alignment at the given
   * cursor
   * 
   * @param g
   *          Graphics2D instance (needed for font scaling)
   * @param clip
   *          - true indicates that only current visible area needs to be
   *          rendered
   * @param width
   *          Width for scaling labels
   */
  public void drawComponent(Graphics g, boolean clip, int width)
  {
    if (av.getFont().getSize() < 10)
    {
      g.setFont(font);
    }
    else
    {
      g.setFont(av.getFont());
    }

    FontMetrics fm = g.getFontMetrics(g.getFont());
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    g.translate(0, getScrollOffset());
    g.setColor(Color.black);

    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    int fontHeight = g.getFont().getSize();
    int y = 0;
    int x = 0;
    int graphExtras = 0;
    int offset = 0;
    Font baseFont = g.getFont();
    FontMetrics baseMetrics = fm;
    int ofontH = fontHeight;
    int sOffset = 0;
    int visHeight = 0;
    int[] visr = (ap != null && ap.getAnnotationPanel() != null) ? ap
            .getAnnotationPanel().getVisibleVRange() : null;
    if (clip && visr != null)
    {
      sOffset = visr[0];
      visHeight = visr[1];
    }
    boolean visible = true, before = false, after = false;
    if (aa != null)
    {
      hasHiddenRows = false;
      int olY = 0;
      for (int i = 0; i < aa.length; i++)
      {
        visible = true;
        if (!aa[i].visible)
        {
          hasHiddenRows = true;
          continue;
        }
        olY = y;
        y += aa[i].height;
        if (clip)
        {
          if (y < sOffset)
          {
            if (!before)
            {
              if (debugRedraw)
              {
                System.out.println("before vis: " + i);
              }
              before = true;
            }
            // don't draw what isn't visible
            continue;
          }
          if (olY > visHeight)
          {

            if (!after)
            {
              if (debugRedraw)
              {
                System.out.println("Scroll offset: " + sOffset
                        + " after vis: " + i);
              }
              after = true;
            }
            // don't draw what isn't visible
            continue;
          }
        }
        g.setColor(Color.black);

        offset = -aa[i].height / 2;

        if (aa[i].hasText)
        {
          offset += fm.getHeight() / 2;
          offset -= fm.getDescent();
        }
        else
        {
          offset += fm.getDescent();
        }

        x = width - fm.stringWidth(aa[i].label) - 3;

        if (aa[i].graphGroup > -1)
        {
          int groupSize = 0;
          // TODO: JAL-1291 revise rendering model so the graphGroup map is
          // computed efficiently for all visible labels
          for (int gg = 0; gg < aa.length; gg++)
          {
            if (aa[gg].graphGroup == aa[i].graphGroup)
            {
              groupSize++;
            }
          }
          if (groupSize * (fontHeight + 8) < aa[i].height)
          {
            graphExtras = (aa[i].height - (groupSize * (fontHeight + 8))) / 2;
          }
          else
          {
            // scale font to fit
            float h = aa[i].height / (float) groupSize, s;
            if (h < 9)
            {
              visible = false;
            }
            else
            {
              fontHeight = -8 + (int) h;
              s = ((float) fontHeight) / (float) ofontH;
              Font f = baseFont.deriveFont(AffineTransform
                      .getScaleInstance(s, s));
              g.setFont(f);
              fm = g.getFontMetrics();
              graphExtras = (aa[i].height - (groupSize * (fontHeight + 8))) / 2;
            }
          }
          if (visible)
          {
            for (int gg = 0; gg < aa.length; gg++)
            {
              if (aa[gg].graphGroup == aa[i].graphGroup)
              {
                x = width - fm.stringWidth(aa[gg].label) - 3;
                g.drawString(aa[gg].label, x, y - graphExtras);

                if (aa[gg]._linecolour != null)
                {

                  g.setColor(aa[gg]._linecolour);
                  g.drawLine(x, y - graphExtras + 3,
                          x + fm.stringWidth(aa[gg].label), y - graphExtras
                                  + 3);
                }

                g.setColor(Color.black);
                graphExtras += fontHeight + 8;
              }
            }
          }
          g.setFont(baseFont);
          fm = baseMetrics;
          fontHeight = ofontH;
        }
        else
        {
          g.drawString(aa[i].label, x, y + offset);
        }
      }
    }

    if (resizePanel)
    {
      g.drawImage(image, 2, 0 - getScrollOffset(), this);
    }
    else if (dragEvent != null && aa != null)
    {
      g.setColor(Color.lightGray);
      g.drawString(aa[selectedRow].label, dragEvent.getX(),
              dragEvent.getY() - getScrollOffset());
    }

    if (!av.getWrapAlignment() && ((aa == null) || (aa.length < 1)))
    {
      g.drawString(MessageManager.getString("label.right_click"), 2, 8);
      g.drawString(MessageManager.getString("label.to_add_annotation"), 2,
              18);
    }
  }

  public int getScrollOffset()
  {
    return scrollOffset;
  }
}
