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
package jalview.util;

import java.awt.Toolkit;
import java.awt.event.MouseEvent;

/**
 * System platform information used by Applet and Application
 * 
 * @author Jim Procter
 */
public class Platform
{
  private static Boolean isAMac = null;

  private static Boolean isHeadless = null;

  /**
   * sorry folks - Macs really are different
   * 
   * @return true if we do things in a special way.
   */
  public static boolean isAMac()
  {
    if (isAMac == null)
    {
      isAMac = System.getProperty("os.name").indexOf("Mac") > -1;
    }
    return isAMac.booleanValue();

  }

  public static boolean isHeadless()
  {
    if (isHeadless == null)
    {
      isHeadless = "true".equals(System.getProperty("java.awt.headless"));
    }
    return isHeadless;
  }

  /**
   * 
   * @return nominal maximum command line length for this platform
   */
  public static int getMaxCommandLineLength()
  {
    // TODO: determine nominal limits for most platforms.
    return 2046; // this is the max length for a windows NT system.
  }

  /**
   * escape a string according to the local platform's escape character
   * 
   * @param file
   * @return escaped file
   */
  public static String escapeString(String file)
  {
    StringBuffer f = new StringBuffer();
    int p = 0, lastp = 0;
    while ((p = file.indexOf('\\', lastp)) > -1)
    {
      f.append(file.subSequence(lastp, p));
      f.append("\\\\");
      lastp = p + 1;
    }
    f.append(file.substring(lastp));
    return f.toString();
  }

  /**
   * Answers true if the mouse event has Meta-down (Command key on Mac) or
   * Ctrl-down (on other o/s). Note this answers _false_ if the Ctrl key is
   * pressed instead of the Meta/Cmd key on Mac. To test for Ctrl-click on Mac,
   * you can use e.isPopupTrigger().
   * 
   * @param e
   * @return
   */
  public static boolean isControlDown(MouseEvent e)
  {
    boolean aMac = isAMac();
    return isControlDown(e, aMac);
  }

  /**
   * Overloaded version of method (to allow unit testing)
   * 
   * @param e
   * @param aMac
   * @return
   */
  protected static boolean isControlDown(MouseEvent e, boolean aMac)
  {
    if (aMac)
    {
      /*
       * answer false for right mouse button
       */
      if (e.isPopupTrigger())
      {
        return false;
      }
      return (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() & e
              .getModifiers()) != 0;
      // could we use e.isMetaDown() here?
    }
    return e.isControlDown();
  }
}
