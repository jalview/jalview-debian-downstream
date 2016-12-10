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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdwidthAdjuster extends JPanel implements MouseListener,
        MouseMotionListener
{
  boolean active = false;

  int oldX = 0;

  Image image;

  AlignmentPanel ap;

  /**
   * Creates a new IdwidthAdjuster object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public IdwidthAdjuster(AlignmentPanel ap)
  {
    this.ap = ap;

    java.net.URL url = getClass().getResource("/images/idwidth.gif");

    if (url != null)
    {
      image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
    }

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
    oldX = evt.getX();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent evt)
  {
    active = false;
    repaint();

    /*
     * If in a SplitFrame with co-scaled alignments, set the other's id width to
     * match
     */
    final AlignViewportI viewport = ap.getAlignViewport();
    if (viewport.getCodingComplement() != null
            && viewport.isScaleProteinAsCdna())
    {
      viewport.getCodingComplement().setIdWidth(viewport.getIdWidth());
      SplitFrame sf = (SplitFrame) ap.alignFrame.getSplitViewContainer();
      sf.repaint();
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent evt)
  {
    active = true;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void mouseExited(MouseEvent evt)
  {
    active = false;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent evt)
  {
    active = true;

    final AlignViewportI viewport = ap.getAlignViewport();
    int curwidth = viewport.getIdWidth();
    int dif = evt.getX() - oldX;

    final int newWidth = curwidth + dif;
    if ((newWidth > 20) || (dif > 0))
    {
      viewport.setIdWidth(newWidth);

      ap.paintAlignment(true);
    }

    oldX = evt.getX();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  public void mouseClicked(MouseEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (active)
    {
      if (image != null)
      {
        g.drawImage(image, getWidth() - 20, 2, this);
      }
    }
  }
}
