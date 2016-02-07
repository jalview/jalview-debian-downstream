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

import java.net.URL;

import javax.help.BadIDException;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

/**
 * Utility class to show the help documentation window.
 * 
 * @author gmcarstairs
 *
 */
public class Help
{
  public enum HelpId
  {
    Home("home"), SequenceFeatureSettings("seqfeatures.settings"), StructureViewer(
            "viewingpdbs");

    private String id;

    private HelpId(String loc)
    {
      this.id = loc;
    }

    @Override
    public String toString()
    {
      return this.id;
    }
  }

  private static final long HALF_A_MO = 500; // half a second

  private static long lastOpenedTime = 0L;

  /**
   * Not instantiable
   */
  private Help()
  {

  }

  /**
   * Show help text in a new window. But do nothing if within half a second of
   * the last invocation.
   * 
   * This is a workaround for issue JAL-914 - both Desktop and AlignFrame
   * responding to F1 key, resulting in duplicate help windows opened.
   * 
   * @param id
   *          TODO
   * 
   * @throws HelpSetException
   */
  public static void showHelpWindow(HelpId id) throws HelpSetException
  {
    long timeNow = System.currentTimeMillis();

    if (timeNow - lastOpenedTime > HALF_A_MO)
    {
      lastOpenedTime = timeNow;
      ClassLoader cl = Desktop.class.getClassLoader();
      URL url = HelpSet.findHelpSet(cl, "help/help"); // $NON-NLS-$
      HelpSet hs = new HelpSet(cl, url);

      HelpBroker hb = hs.createHelpBroker();
      try
      {
        hb.setCurrentID(id.toString());
      } catch (BadIDException bad)
      {
        System.out.println("Bad help link: " + id.toString()
                + ": must match a target in help.jhm");
        throw bad;
      }
      hb.setDisplayed(true);
    }
  }

  public static void showHelpWindow() throws HelpSetException
  {
    showHelpWindow(HelpId.Home);
  }
}
