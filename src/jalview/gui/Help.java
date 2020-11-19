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
package jalview.gui;

import java.awt.Point;
import java.net.URL;

import javax.help.BadIDException;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

/**
 * Utility class to show the help documentation window
 * 
 * @author gmcarstairs
 */
public class Help
{
  public enum HelpId
  {
    Home("home"), SequenceFeatureSettings("seqfeatures.settings"),
    StructureViewer("viewingpdbs"), PdbFts("pdbfts"),
    UniprotFts("uniprotfts");

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

  private static HelpBroker hb;

  /**
   * Not instantiable
   */
  private Help()
  {

  }

  /**
   * Shows the help window, at the entry specified by the given helpId
   * 
   * @param id
   * 
   * @throws HelpSetException
   */
  public static void showHelpWindow(HelpId id) throws HelpSetException
  {
    ClassLoader cl = Desktop.class.getClassLoader();
    URL url = HelpSet.findHelpSet(cl, "help/help"); // $NON-NLS-$
    HelpSet hs = new HelpSet(cl, url);

    if (hb == null)
    {
      /*
       * create help broker first time (only)
       */
      hb = hs.createHelpBroker();
    }

    try
    {
      hb.setCurrentID(id.toString());
    } catch (BadIDException bad)
    {
      System.out.println("Bad help link: " + id.toString()
              + ": must match a target in help.jhm");
      throw bad;
    }

    /*
     * set Help visible - at its current location if it is already shown,
     * else at a location as determined by the window manager
     */
    Point p = hb.getLocation();
    hb.setLocation(p);
    hb.setDisplayed(true);
  }

  /**
   * Show the Help window at the root entry
   * 
   * @throws HelpSetException
   */
  public static void showHelpWindow() throws HelpSetException
  {
    showHelpWindow(HelpId.Home);
  }
}
