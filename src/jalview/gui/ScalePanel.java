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

import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.ScaleRenderer;
import jalview.renderer.ScaleRenderer.ScaleMark;
import jalview.util.MessageManager;
import jalview.util.Platform;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * The panel containing the sequence ruler (when not in wrapped mode), and
 * supports a range of mouse operations to select, hide or reveal columns.
 */
public class ScalePanel extends JPanel implements MouseMotionListener,
        MouseListener
{
  protected int offy = 4;

  public int width;

  protected AlignViewport av;

  AlignmentPanel ap;

  boolean stretchingGroup = false;

  /*
   * min, max hold the extent of a mouse drag action
   */
  int min;

  int max;

  boolean mouseDragging = false;

  /*
   * holds a hidden column range when the mouse is over an adjacent column
   */
  int[] reveal;

  /**
   * Constructor
   * 
   * @param av
   * @param ap
   */
  public ScalePanel(AlignViewport av, AlignmentPanel ap)
  {
    this.av = av;
    this.ap = ap;

    addMouseListener(this);
    addMouseMotionListener(this);
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
    int x = (evt.getX() / av.getCharWidth()) + av.getStartRes();
    final int res;

    if (av.hasHiddenColumns())
    {
      x = av.getColumnSelection().adjustForHiddenColumns(x);
    }

    if (x >= av.getAlignment().getWidth())
    {
      res = av.getAlignment().getWidth() - 1;
    }
    else
    {
      res = x;
    }

    min = res;
    max = res;

    if (evt.isPopupTrigger()) // Mac: mousePressed
    {
      rightMouseButtonPressed(evt, res);
    }
    else if (SwingUtilities.isRightMouseButton(evt) && !Platform.isAMac())
    {
      /*
       * defer right-mouse click handling to mouse up on Windows
       * (where isPopupTrigger() will answer true)
       * but accept Cmd-click on Mac which passes isRightMouseButton
       */
      return;
    }
    else
    {
      leftMouseButtonPressed(evt, res);
    }
  }

  /**
   * Handles right mouse button press. If pressed in a selected column, opens
   * context menu for 'Hide Columns'. If pressed on a hidden columns marker,
   * opens context menu for 'Reveal / Reveal All'. Else does nothing.
   * 
   * @param evt
   * @param res
   */
  protected void rightMouseButtonPressed(MouseEvent evt, final int res)
  {
    JPopupMenu pop = new JPopupMenu();
    if (reveal != null)
    {
      JMenuItem item = new JMenuItem(
              MessageManager.getString("label.reveal"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          av.showColumn(reveal[0]);
          reveal = null;
          ap.paintAlignment(true);
          if (ap.overviewPanel != null)
          {
            ap.overviewPanel.updateOverviewImage();
          }
          av.sendSelection();
        }
      });
      pop.add(item);

      if (av.getColumnSelection().hasHiddenColumns())
      {
        item = new JMenuItem(MessageManager.getString("action.reveal_all"));
        item.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            av.showAllHiddenColumns();
            reveal = null;
            ap.paintAlignment(true);
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
            av.sendSelection();
          }
        });
        pop.add(item);
      }
      pop.show(this, evt.getX(), evt.getY());
    }
    else if (av.getColumnSelection().contains(res))
    {
      JMenuItem item = new JMenuItem(
              MessageManager.getString("label.hide_columns"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          av.hideColumns(res, res);
          if (av.getSelectionGroup() != null
                  && av.getSelectionGroup().getSize() == av.getAlignment()
                          .getHeight())
          {
            av.setSelectionGroup(null);
          }

          ap.paintAlignment(true);
          if (ap.overviewPanel != null)
          {
            ap.overviewPanel.updateOverviewImage();
          }
          av.sendSelection();
        }
      });
      pop.add(item);
      pop.show(this, evt.getX(), evt.getY());
    }
  }

  /**
   * Handles left mouse button press
   * 
   * @param evt
   * @param res
   */
  protected void leftMouseButtonPressed(MouseEvent evt, final int res)
  {
    /*
     * Ctrl-click/Cmd-click adds to the selection
     * Shift-click extends the selection
     */
    // TODO Problem: right-click on Windows not reported until mouseReleased?!?
    if (!Platform.isControlDown(evt) && !evt.isShiftDown())
    {
      av.getColumnSelection().clear();
    }

    av.getColumnSelection().addElement(res);
    SequenceGroup sg = new SequenceGroup();
    // try to be as quick as possible
    SequenceI[] iVec = av.getAlignment().getSequencesArray();
    for (int i = 0; i < iVec.length; i++)
    {
      sg.addSequence(iVec[i], false);
      iVec[i] = null;
    }
    iVec = null;
    sg.setStartRes(res);
    sg.setEndRes(res);

    if (evt.isShiftDown())
    {
      int min = Math.min(av.getColumnSelection().getMin(), res);
      int max = Math.max(av.getColumnSelection().getMax(), res);
      for (int i = min; i < max; i++)
      {
        av.getColumnSelection().addElement(i);
      }
      sg.setStartRes(min);
      sg.setEndRes(max);
    }
    av.setSelectionGroup(sg);
    ap.paintAlignment(false);
    av.sendSelection();
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

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (res >= av.getAlignment().getWidth())
    {
      res = av.getAlignment().getWidth() - 1;
    }

    if (!stretchingGroup)
    {
      if (evt.isPopupTrigger()) // Windows: mouseReleased
      {
        rightMouseButtonPressed(evt, res);
      }
      else
      {
        ap.paintAlignment(false);
      }
      return;
    }

    SequenceGroup sg = av.getSelectionGroup();

    if (sg != null)
    {
      if (res > sg.getStartRes())
      {
        sg.setEndRes(res);
      }
      else if (res < sg.getStartRes())
      {
        sg.setStartRes(res);
      }
    }
    stretchingGroup = false;
    ap.paintAlignment(false);
    av.sendSelection();
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
    mouseDragging = true;

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();
    if (res < 0)
    {
      res = 0;
    }

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (res >= av.getAlignment().getWidth())
    {
      res = av.getAlignment().getWidth() - 1;
    }

    if (res < min)
    {
      min = res;
    }

    if (res > max)
    {
      max = res;
    }

    SequenceGroup sg = av.getSelectionGroup();

    if (sg != null)
    {
      stretchingGroup = true;

      if (!av.getColumnSelection().contains(res))
      {
        av.getColumnSelection().addElement(res);
      }

      if (res > sg.getStartRes())
      {
        sg.setEndRes(res);
      }
      if (res < sg.getStartRes())
      {
        sg.setStartRes(res);
      }

      int col;
      for (int i = min; i <= max; i++)
      {
        col = i; // av.getColumnSelection().adjustForHiddenColumns(i);

        if ((col < sg.getStartRes()) || (col > sg.getEndRes()))
        {
          av.getColumnSelection().removeElement(col);
        }
        else
        {
          av.getColumnSelection().addElement(col);
        }
      }

      ap.paintAlignment(false);
    }
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.getSeqPanel().scrollCanvas(null);
    }
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.getSeqPanel().scrollCanvas(evt);
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    this.setToolTipText(null);
    reveal = null;
    if (!av.hasHiddenColumns())
    {
      return;
    }

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    res = av.getColumnSelection().adjustForHiddenColumns(res);

    if (av.getColumnSelection().getHiddenColumns() != null)
    {
      for (int[] region : av.getColumnSelection().getHiddenColumns())
      {
        if (res + 1 == region[0] || res - 1 == region[1])
        {
          reveal = region;
          ToolTipManager.sharedInstance().registerComponent(this);
          this.setToolTipText(MessageManager
                  .getString("label.reveal_hidden_columns"));
          break;
        }
      }
    }
    repaint();
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
    drawScale(g, av.getStartRes(), av.getEndRes(), getWidth(), getHeight());
  }

  // scalewidth will normally be screenwidth,
  public void drawScale(Graphics g, int startx, int endx, int width,
          int height)
  {
    Graphics2D gg = (Graphics2D) g;
    gg.setFont(av.getFont());

    if (av.antiAlias)
    {
      gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    // Fill in the background
    gg.setColor(Color.white);
    gg.fillRect(0, 0, width, height);
    gg.setColor(Color.black);

    // Fill the selected columns
    ColumnSelection cs = av.getColumnSelection();
    int avCharWidth = av.getCharWidth(), avCharHeight = av.getCharHeight();

    if (cs != null)
    {
      gg.setColor(new Color(220, 0, 0));

      for (int sel : cs.getSelected())
      {
        // TODO: JAL-2001 - provide a fast method to list visible selected in a
        // given range

        if (av.hasHiddenColumns())
        {
          if (cs.isVisible(sel))
          {
            sel = cs.findColumnPosition(sel);
          }
          else
          {
            continue;
          }
        }

        if ((sel >= startx) && (sel <= endx))
        {
          gg.fillRect((sel - startx) * avCharWidth, 0, avCharWidth,
                  getHeight());
        }
      }
    }

    int widthx = 1 + endx - startx;

    FontMetrics fm = gg.getFontMetrics(av.getFont());
    int y = avCharHeight;
    int yOf = fm.getDescent();
    y -= yOf;
    if (av.hasHiddenColumns())
    {
      // draw any hidden column markers
      gg.setColor(Color.blue);
      int res;
      if (av.getShowHiddenMarkers()
              && av.getColumnSelection().getHiddenColumns() != null)
      {
        for (int i = 0; i < av.getColumnSelection().getHiddenColumns()
                .size(); i++)
        {
          res = av.getColumnSelection().findHiddenRegionPosition(i)
                  - startx;

          if (res < 0 || res > widthx)
          {
            continue;
          }

          gg.fillPolygon(new int[] {
              -1 + res * avCharWidth - avCharHeight / 4,
              -1 + res * avCharWidth + avCharHeight / 4,
              -1 + res * avCharWidth }, new int[] { y, y, y + 2 * yOf }, 3);
        }
      }
    }
    // Draw the scale numbers
    gg.setColor(Color.black);

    int maxX = 0;
    List<ScaleMark> marks = new ScaleRenderer().calculateMarks(av, startx,
            endx);

    for (ScaleMark mark : marks)
    {
      boolean major = mark.major;
      int mpos = mark.column; // (i - startx - 1)
      String mstring = mark.text;
      if (mstring != null)
      {
        if (mpos * avCharWidth > maxX)
        {
          gg.drawString(mstring, mpos * avCharWidth, y);
          maxX = (mpos + 2) * avCharWidth + fm.stringWidth(mstring);
        }
      }
      if (major)
      {
        gg.drawLine((mpos * avCharWidth) + (avCharWidth / 2), y + 2,
                (mpos * avCharWidth) + (avCharWidth / 2), y + (yOf * 2));
      }
      else
      {
        gg.drawLine((mpos * avCharWidth) + (avCharWidth / 2), y + yOf,
                (mpos * avCharWidth) + (avCharWidth / 2), y + (yOf * 2));
      }
    }
  }

}
