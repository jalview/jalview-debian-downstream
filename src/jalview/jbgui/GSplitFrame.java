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
package jalview.jbgui;

import jalview.util.Platform;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class GSplitFrame extends JInternalFrame
{
  private static final long serialVersionUID = 1L;

  private GAlignFrame topFrame;

  private GAlignFrame bottomFrame;

  private JSplitPane splitPane;

  /**
   * Constructor
   * 
   * @param top
   * @param bottom
   */
  public GSplitFrame(GAlignFrame top, GAlignFrame bottom)
  {
    this.topFrame = top;
    this.bottomFrame = bottom;

    hideTitleBars();

    addSplitPane();
  }

  /**
   * Create and add the split pane containing the top and bottom components.
   */
  protected void addSplitPane()
  {
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topFrame,
            bottomFrame);
    splitPane.setVisible(true);
    final double ratio = bottomFrame.getHeight() == 0 ? 0.5d : topFrame
            .getHeight()
            / (double) (topFrame.getHeight() + bottomFrame.getHeight());
    splitPane.setDividerLocation(ratio);
    splitPane.setResizeWeight(ratio);
    splitPane.setDividerSize(5);
    add(splitPane);
  }

  /**
   * Try to hide the title bars as a waste of precious space.
   * 
   * @see http
   *      ://stackoverflow.com/questions/7218971/java-method-works-on-windows
   *      -but-not-macintosh -java
   */
  protected void hideTitleBars()
  {
    if (new Platform().isAMac())
    {
      // this saves some space - but doesn't hide the title bar
      topFrame.putClientProperty("JInternalFrame.isPalette", true);
      // topFrame.getRootPane().putClientProperty("Window.style", "small");
      bottomFrame.putClientProperty("JInternalFrame.isPalette", true);
    }
    else
    {
      ((BasicInternalFrameUI) topFrame.getUI()).setNorthPane(null);
      ((BasicInternalFrameUI) bottomFrame.getUI()).setNorthPane(null);
    }
  }

  public GAlignFrame getTopFrame()
  {
    return topFrame;
  }

  public GAlignFrame getBottomFrame()
  {
    return bottomFrame;
  }

  /**
   * Returns the split pane component the mouse is in, or null if neither.
   * 
   * @return
   */
  protected GAlignFrame getFrameAtMouse()
  {
    Point loc = MouseInfo.getPointerInfo().getLocation();

    if (isIn(loc, splitPane.getTopComponent()))
    {
      return getTopFrame();
    }
    else if (isIn(loc, splitPane.getBottomComponent()))
    {
      return getBottomFrame();
    }
    return null;
  }

  private boolean isIn(Point loc, Component comp)
  {
    if (!comp.isVisible())
    {
      return false;
    }
    Point p = comp.getLocationOnScreen();
    Rectangle r = new Rectangle(p.x, p.y, comp.getWidth(), comp.getHeight());
    return r.contains(loc);
  }

  /**
   * Make the complement of the specified split component visible or hidden,
   * adjusting the position of the split divide.
   */
  public void setComplementVisible(Object alignFrame, boolean show)
  {
    if (alignFrame == this.topFrame)
    {
      this.bottomFrame.setVisible(show);
    }
    else if (alignFrame == this.bottomFrame)
    {
      this.topFrame.setVisible(show);
    }
    if (show)
    {
      // SplitPane needs nudging to restore 50-50 split
      // TODO save/restore other ratios
      splitPane.setDividerLocation(0.5d);
    }
    validate();
  }
}
