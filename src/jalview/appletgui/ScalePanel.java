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

import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceGroup;
import jalview.renderer.ScaleRenderer;
import jalview.renderer.ScaleRenderer.ScaleMark;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class ScalePanel extends Panel implements MouseMotionListener,
        MouseListener
{

  protected int offy = 4;

  public int width;

  protected AlignViewport av;

  AlignmentPanel ap;

  boolean stretchingGroup = false;

  int min; // used by mouseDragged to see if user

  int max; // used by mouseDragged to see if user

  boolean mouseDragging = false;

  int[] reveal;

  public ScalePanel(AlignViewport av, AlignmentPanel ap)
  {
    setLayout(null);
    this.av = av;
    this.ap = ap;

    addMouseListener(this);
    addMouseMotionListener(this);

  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    int x = (evt.getX() / av.getCharWidth()) + av.getStartRes();
    final int res;

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(x);
    }
    else
    {
      res = x;
    }

    min = res;
    max = res;
    if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
    {
      rightMouseButtonPressed(evt, res);
    }
    else
    {
      leftMouseButtonPressed(evt, res);
    }
  }

  /**
   * Handles left mouse button pressed (selection / clear selections)
   * 
   * @param evt
   * @param res
   */
  protected void leftMouseButtonPressed(MouseEvent evt, final int res)
  {
    if (!evt.isControlDown() && !evt.isShiftDown())
    {
      av.getColumnSelection().clear();
    }

    av.getColumnSelection().addElement(res);
    SequenceGroup sg = new SequenceGroup();
    for (int i = 0; i < av.getAlignment().getSequences().size(); i++)
    {
      sg.addSequence(av.getAlignment().getSequenceAt(i), false);
    }

    sg.setStartRes(res);
    sg.setEndRes(res);
    av.setSelectionGroup(sg);

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
    ap.paintAlignment(true);
    av.sendSelection();
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
    PopupMenu pop = new PopupMenu();
    if (reveal != null)
    {
      MenuItem item = new MenuItem(MessageManager.getString("label.reveal"));
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

      if (av.getColumnSelection().hasManyHiddenColumns())
      {
        item = new MenuItem(MessageManager.getString("action.reveal_all"));
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
      this.add(pop);
      pop.show(this, evt.getX(), evt.getY());
    }
    else if (av.getColumnSelection().contains(res))
    {
      MenuItem item = new MenuItem(
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
      this.add(pop);
      pop.show(this, evt.getX(), evt.getY());
    }
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    mouseDragging = false;

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    if (res > av.getAlignment().getWidth())
    {
      res = av.getAlignment().getWidth() - 1;
    }

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (!stretchingGroup)
    {
      ap.paintAlignment(false);

      return;
    }

    SequenceGroup sg = av.getSelectionGroup();

    if (res > sg.getStartRes())
    {
      sg.setEndRes(res);
    }
    else if (res < sg.getStartRes())
    {
      sg.setStartRes(res);
    }

    stretchingGroup = false;
    ap.paintAlignment(false);
    av.sendSelection();
  }

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

    if (res > av.getAlignment().getWidth())
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
        col = av.getColumnSelection().adjustForHiddenColumns(i);

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
      ap.seqPanel.scrollCanvas(null);
    }
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.seqPanel.scrollCanvas(evt);
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {

  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    if (!av.hasHiddenColumns())
    {
      return;
    }

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    res = av.getColumnSelection().adjustForHiddenColumns(res);

    reveal = null;
    for (int[] region : av.getColumnSelection().getHiddenColumns())
    {
      if (res + 1 == region[0] || res - 1 == region[1])
      {
        reveal = region;
        break;
      }
    }

    repaint();
  }

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    drawScale(g, av.getStartRes(), av.getEndRes(), getSize().width,
            getSize().height);
  }

  // scalewidth will normally be screenwidth,
  public void drawScale(Graphics gg, int startx, int endx, int width,
          int height)
  {
    gg.setFont(av.getFont());
    // Fill in the background
    gg.setColor(Color.white);
    gg.fillRect(0, 0, width, height);
    gg.setColor(Color.black);

    // Fill the selected columns
    ColumnSelection cs = av.getColumnSelection();
    int avCharWidth = av.getCharWidth();
    int avcharHeight = av.getCharHeight();
    if (cs != null)
    {
      gg.setColor(new Color(220, 0, 0));
      boolean hasHiddenColumns = cs.hasHiddenColumns();
      for (int sel : cs.getSelected())
      {
        // TODO: JAL-2001 - provide a fast method to list visible selected in a
        // given range
        if (hasHiddenColumns)
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
                  getSize().height);
        }
      }
    }

    // Draw the scale numbers
    gg.setColor(Color.black);

    int maxX = 0;
    List<ScaleMark> marks = new ScaleRenderer().calculateMarks(av, startx,
            endx);

    FontMetrics fm = gg.getFontMetrics(av.getFont());
    int y = avcharHeight;
    int yOf = fm.getDescent();
    y -= yOf;
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

    if (av.hasHiddenColumns())
    {
      gg.setColor(Color.blue);
      int res;
      if (av.getShowHiddenMarkers())
      {
        int widthx = 1 + endx - startx;
        for (int i = 0; i < cs.getHiddenColumns().size(); i++)
        {

          res = cs.findHiddenRegionPosition(i) - startx;

          if (res < 0 || res > widthx)
          {
            continue;
          }

          gg.fillPolygon(new int[] {
              -1 + res * avCharWidth - avcharHeight / 4,
              -1 + res * avCharWidth + avcharHeight / 4,
              -1 + res * avCharWidth }, new int[] { y, y, y + 2 * yOf }, 3);
        }
      }
    }
  }

}
