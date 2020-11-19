/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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

import java.awt.event.MouseEvent;

/**
 * System platform information used by Applet and Application
 * 
 * @author Jim Procter
 */
public class Platform
{
  private static Boolean isAMac = null, isWindows = null, isLinux = null;

  private static Boolean isHeadless = null;

  /**
   * added to check LaF for Linux
   * 
   * @return
   */
  public static boolean isLinux()
  {
    return (isLinux == null
            ? (isLinux = (System.getProperty("os.name").indexOf("Linux") >= 0))
            : isLinux);
  }

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

  /**
   * Check if we are on a Microsoft plaform...
   * 
   * @return true if we have to cope with another platform variation
   */
  public static boolean isWindows()
  {
    if (isWindows == null)
    {
      isWindows = System.getProperty("os.name").indexOf("Win") > -1;
    }
    return isWindows.booleanValue();
  }

  /**
   * 
   * @return true if we are running in non-interactive no UI mode
   */
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
   * Answers the input with every backslash replaced with a double backslash (an
   * 'escaped' single backslash)
   * 
   * @param s
   * @return
   */
  public static String escapeBackslashes(String s)
  {
    return s == null ? null : s.replace("\\", "\\\\");
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
      return (jalview.util.ShortcutKeyMaskExWrapper
              .getMenuShortcutKeyMaskEx() // .getMenuShortcutKeyMaskEx()
              & jalview.util.ShortcutKeyMaskExWrapper
                      .getModifiersEx(e)) != 0; // getModifiers()) != 0;
    }
    return e.isControlDown();
  }

  /**
   * A (case sensitive) file path comparator that ignores the difference between
   * / and \
   * 
   * @param path1
   * @param path2
   * @return
   */
  public static boolean pathEquals(String path1, String path2)
  {
    if (path1 == null)
    {
      return path2 == null;
    }
    if (path2 == null)
    {
      return false;
    }
    String p1 = path1.replace('\\', '/');
    String p2 = path2.replace('\\', '/');
    return p1.equals(p2);
  }
}
