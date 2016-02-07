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
package jalview.gui;

import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

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
public class ScalePanel extends JPanel implements MouseMotionListener,
        MouseListener
{
  protected int offy = 4;

  /** DOCUMENT ME!! */
  public int width;

  protected AlignViewport av;

  AlignmentPanel ap;

  boolean stretchingGroup = false;

  int min; // used by mouseDragged to see if user

  int max; // used by mouseDragged to see if user

  boolean mouseDragging = false;

  // wants to delete columns
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

    if (SwingUtilities.isRightMouseButton(evt))
    {
      JPopupMenu pop = new JPopupMenu();
      if (reveal != null)
      {
        JMenuItem item = new JMenuItem(
                MessageManager.getString("label.reveal"));
        item.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            av.showColumn(reveal[0]);
            reveal = null;
            ap.paintAlignment(true);
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
          }
        });
        pop.add(item);

        if (av.getColumnSelection().hasHiddenColumns())
        {
          item = new JMenuItem(
                  MessageManager.getString("action.reveal_all"));
          item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              av.showAllHiddenColumns();
              reveal = null;
              ap.paintAlignment(true);
              if (ap.overviewPanel != null)
              {
                ap.overviewPanel.updateOverviewImage();
              }
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
          public void actionPerformed(ActionEvent e)
          {
            av.hideColumns(res, res);
            if (av.getSelectionGroup() != null
                    && av.getSelectionGroup().getSize() == av
                            .getAlignment().getHeight())
            {
              av.setSelectionGroup(null);
            }

            ap.paintAlignment(true);
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
          }
        });
        pop.add(item);
        pop.show(this, evt.getX(), evt.getY());
      }
    }
    else
    // LEFT MOUSE TO SELECT
    {
      if (!evt.isControlDown() && !evt.isShiftDown())
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
    }

    ap.paintAlignment(false);
    av.sendSelection();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
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
      ap.paintAlignment(false);

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

  public void mouseEntered(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.getSeqPanel().scrollCanvas(null);
    }
  }

  public void mouseExited(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.getSeqPanel().scrollCanvas(evt);
    }
  }

  public void mouseClicked(MouseEvent evt)
  {
  }

  public void mouseMoved(MouseEvent evt)
  {
    if (!av.hasHiddenColumns())
    {
      return;
    }

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    res = av.getColumnSelection().adjustForHiddenColumns(res);

    reveal = null;
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
        else
        {
          this.setToolTipText(null);
        }
      }
    }
    repaint();
  }

  int[] reveal;

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
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

    int s;
    if (cs != null)
    {
      gg.setColor(new Color(220, 0, 0));

      for (int i = 0; i < cs.size(); i++)
      {
        int sel = cs.columnAt(i);
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
    // Draw the scale numbers
    gg.setColor(Color.black);

    int scalestartx = (startx / 10) * 10;

    FontMetrics fm = gg.getFontMetrics(av.getFont());
    int y = avCharHeight - fm.getDescent();

    if ((scalestartx % 10) == 0)
    {
      scalestartx += 5;
    }

    String string;
    int maxX = 0;

    for (int i = scalestartx; i < endx; i += 5)
    {
      if ((i % 10) == 0)
      {
        string = String.valueOf(av.getColumnSelection()
                .adjustForHiddenColumns(i));
        if ((i - startx - 1) * avCharWidth > maxX)
        {
          gg.drawString(string, (i - startx - 1) * avCharWidth, y);
          maxX = (i - startx + 1) * avCharWidth + fm.stringWidth(string);
        }

        gg.drawLine(((i - startx - 1) * avCharWidth) + (avCharWidth / 2),
                y + 2,
                ((i - startx - 1) * avCharWidth) + (avCharWidth / 2), y
                        + (fm.getDescent() * 2));
      }
      else
      {
        gg.drawLine(((i - startx - 1) * avCharWidth) + (avCharWidth / 2), y
                + fm.getDescent(), ((i - startx - 1) * avCharWidth)
                + (avCharWidth / 2), y + (fm.getDescent() * 2));
      }
    }

    if (av.hasHiddenColumns())
    {
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

          if (res < 0 || res > endx - scalestartx)
          {
            continue;
          }

          gg.fillPolygon(new int[] { res * avCharWidth - avCharHeight / 4,
              res * avCharWidth + avCharHeight / 4, res * avCharWidth },
                  new int[] { y - avCharHeight / 2, y - avCharHeight / 2,
                      y + 8 }, 3);

        }
      }

      if (reveal != null && reveal[0] > startx && reveal[0] < endx)
      {
        gg.drawString(MessageManager.getString("label.reveal_columns"),
                reveal[0] * avCharWidth, 0);
      }
    }

  }
}
