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

import java.awt.*;
import java.awt.event.*;

public class IdwidthAdjuster extends Panel implements MouseListener,
        MouseMotionListener
{
  boolean active = false;

  int oldX = 0;

  Image image;

  AlignmentPanel ap;

  public IdwidthAdjuster(AlignmentPanel ap)
  {
    setLayout(null);
    this.ap = ap;
    java.net.URL url = getClass().getResource("/images/idwidth.gif");
    if (url != null)
    {
      image = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public void mousePressed(MouseEvent evt)
  {
    oldX = evt.getX();
  }

  public void mouseReleased(MouseEvent evt)
  {
    active = false;
    repaint();
  }

  public void mouseEntered(MouseEvent evt)
  {
    active = true;
    repaint();
  }

  public void mouseExited(MouseEvent evt)
  {
    active = false;
    repaint();
  }

  public void mouseDragged(MouseEvent evt)
  {
    active = true;
    Dimension d = ap.idPanel.idCanvas.getSize();
    int dif = evt.getX() - oldX;

    if (d.width + dif > 20 || dif > 0)
    {
      ap.setIdWidth(d.width + dif, d.height);
      this.setSize(d.width + dif, getSize().height);
      oldX = evt.getX();
    }

  }

  public void mouseMoved(MouseEvent evt)
  {
  }

  public void mouseClicked(MouseEvent evt)
  {
  }

  public void paint(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getSize().width, getSize().height);
    if (active)
    {
      if (image != null)
      {
        g.drawImage(image, getSize().width - 20, 2, this);
      }
    }
  }

}
