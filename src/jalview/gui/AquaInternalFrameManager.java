/*
 * Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jalview.gui;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.util.Vector;

import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.JInternalFrame;

/**
 * Based on AquaInternalFrameManager
 *
 * DesktopManager implementation for Aqua
 *
 * Mac is more like Windows than it's like Motif/Basic
 *
 * From WindowsDesktopManager:
 *
 * This class implements a DesktopManager which more closely follows the MDI
 * model than the DefaultDesktopManager. Unlike the DefaultDesktopManager
 * policy, MDI requires that the selected and activated child frames are the
 * same, and that that frame always be the top-most window.
 * <p>
 * The maximized state is managed by the DesktopManager with MDI, instead of
 * just being a property of the individual child frame. This means that if the
 * currently selected window is maximized and another window is selected, that
 * new window will be maximized.
 *
 * Downloaded from
 * https://raw.githubusercontent.com/frohoff/jdk8u-jdk/master/src/macosx/classes/com/apple/laf/AquaInternalFrameManager.java
 * 
 * Patch from Jim Procter - when the most recently opened frame is closed,
 * correct behaviour is to go to the next most recent frame, rather than wrap
 * around to the bottom of the window stack (as the original implementation
 * does)
 * 
 * see com.sun.java.swing.plaf.windows.WindowsDesktopManager
 */
public class AquaInternalFrameManager extends DefaultDesktopManager
{
  // Variables

  /* The frame which is currently selected/activated.
   * We store this value to enforce Mac's single-selection model.
   */
  JInternalFrame fCurrentFrame;

  JInternalFrame fInitialFrame;

  /* The list of frames, sorted by order of creation.
   * This list is necessary because by default the order of
   * child frames in the JDesktopPane changes during frame
   * activation (the activated frame is moved to index 0).
   * We preserve the creation order so that "next" and "previous"
   * frame actions make sense.
   */
  Vector<JInternalFrame> fChildFrames = new Vector<>(1);

  /**
   * keep a reference to the original LAF manager so we can iconise/de-iconise
   * correctly
   */
  private DesktopManager ourManager;

  public AquaInternalFrameManager(DesktopManager desktopManager)
  {
    ourManager = desktopManager;
  }

  @Override
  public void closeFrame(final JInternalFrame f)
  {
    if (f == fCurrentFrame)
    {
      boolean mostRecentFrame = fChildFrames
              .indexOf(f) == fChildFrames.size() - 1;
      if (!mostRecentFrame)
      {
        activateNextFrame();
      }
      else
      {
        activatePreviousFrame();
      }
    }
    fChildFrames.removeElement(f);
    super.closeFrame(f);
  }

  @Override
  public void deiconifyFrame(final JInternalFrame f)
  {
    JInternalFrame.JDesktopIcon desktopIcon;

    desktopIcon = f.getDesktopIcon();
    // If the icon moved, move the frame to that spot before expanding it
    // reshape does delta checks for us
    f.reshape(desktopIcon.getX(), desktopIcon.getY(), f.getWidth(),
            f.getHeight());
    ourManager.deiconifyFrame(f);
  }

  void addIcon(final Container c,
          final JInternalFrame.JDesktopIcon desktopIcon)
  {
    c.add(desktopIcon);
  }

  /**
   * Removes the frame from its parent and adds its desktopIcon to the parent.
   */
  @Override
  public void iconifyFrame(final JInternalFrame f)
  {
    ourManager.iconifyFrame(f);
  }

  // WindowsDesktopManager code
  @Override
  public void activateFrame(final JInternalFrame f)
  {
    try
    {
      if (f != null)
      {
        super.activateFrame(f);
      }

      // add or relocate to top of stack
      if (fChildFrames.indexOf(f) != -1)
      {
        fChildFrames.remove(f);
      }
      fChildFrames.addElement(f);

      if (fCurrentFrame != null && f != fCurrentFrame)
      {
        if (fCurrentFrame.isSelected())
        {
          fCurrentFrame.setSelected(false);
        }
      }

      if (f != null && !f.isSelected())
      {
        f.setSelected(true);
      }

      fCurrentFrame = f;
    } catch (final PropertyVetoException e)
    {
    }
  }

  private void switchFrame(final boolean next)
  {
    if (fCurrentFrame == null)
    {
      // initialize first frame we find
      if (fInitialFrame != null)
      {
        activateFrame(fInitialFrame);
      }
      return;
    }

    final int count = fChildFrames.size();
    if (count <= 1)
    {
      // No other child frames.
      return;
    }

    final int currentIndex = fChildFrames.indexOf(fCurrentFrame);
    if (currentIndex == -1)
    {
      // the "current frame" is no longer in the list
      fCurrentFrame = null;
      return;
    }

    int nextIndex;
    if (next)
    {
      nextIndex = currentIndex + 1;
      if (nextIndex == count)
      {
        nextIndex = 0;
      }
    }
    else
    {
      nextIndex = currentIndex - 1;
      if (nextIndex == -1)
      {
        nextIndex = count - 1;
      }
    }
    final JInternalFrame f = fChildFrames.elementAt(nextIndex);
    activateFrame(f);
    fCurrentFrame = f;
  }

  /**
   * Activate the next child JInternalFrame, as determined by the frames'
   * Z-order. If there is only one child frame, it remains activated. If there
   * are no child frames, nothing happens.
   */
  public void activateNextFrame()
  {
    switchFrame(true);
  }

  /**
   * same as above but will activate a frame if none have been selected
   */
  public void activateNextFrame(final JInternalFrame f)
  {
    fInitialFrame = f;
    switchFrame(true);
  }

  /**
   * Activate the previous child JInternalFrame, as determined by the frames'
   * Z-order. If there is only one child frame, it remains activated. If there
   * are no child frames, nothing happens.
   */
  public void activatePreviousFrame()
  {
    switchFrame(false);
  }
}
