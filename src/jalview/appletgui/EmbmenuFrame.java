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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class implements a pattern form embedding toolbars as a panel with
 * popups for situations where the system menu bar is either invisible or
 * inappropriate. It was derived from the code for embedding the jalview applet
 * alignFrame as a component on the web-page, which requires the local
 * alignFrame menu to be attached to that panel rather than placed on the parent
 * (which isn't allowed anyhow). TODO: try to modify the embeddedMenu display so
 * it looks like a real toolbar menu TODO: modify click/mouse handler for
 * embeddedMenu so it behaves more like a real pulldown menu toolbar
 * 
 * @author Jim Procter and Andrew Waterhouse
 * 
 */
public class EmbmenuFrame extends Frame implements MouseListener
{
  /**
   * map from labels to popup menus for the embedded menubar
   */
  protected Hashtable embeddedPopup;

  /**
   * the embedded menu is built on this and should be added to the frame at the
   * appropriate position.
   * 
   */
  protected Panel embeddedMenu;

  public EmbmenuFrame() throws HeadlessException
  {
    super();
  }

  public EmbmenuFrame(String title) throws HeadlessException
  {
    super(title);
  }

  /**
   * Check if the applet is running on a platform that requires the Frame
   * menuBar to be embedded, and if so, embeds it.
   * 
   * @param tobeAdjusted
   *          the panel that is to be reduced to make space for the embedded
   *          menu bar
   * @return true if menuBar was embedded and tobeAdjusted's height modified
   */
  protected boolean embedMenuIfNeeded(Panel tobeAdjusted)
  {
    MenuBar topMenuBar = getMenuBar();
    if (topMenuBar == null)
    {
      return false;
    }
    // DEBUG Hint: can test embedded menus by inserting true here.
    if (new jalview.util.Platform().isAMac())
    {
      // Build the embedded menu panel
      embeddedMenu = makeEmbeddedPopupMenu(topMenuBar, "Arial", Font.PLAIN,
              10, true); // try to pickup system font.
      setMenuBar(null);
      // add the components to the TreePanel area.
      add(embeddedMenu, BorderLayout.NORTH);
      tobeAdjusted.setSize(getSize().width, getSize().height
              - embeddedMenu.HEIGHT);
      return true;
    }
    return false;
  }

  /**
   * move all menus on menuBar onto embeddedMenu. embeddedPopup is used to store
   * the popups for each menu removed from the menuBar and added to the panel.
   * NOTE: it is up to the caller to remove menuBar from the Frame if it is
   * already attached.
   * 
   * @param menuBar
   * @param fn
   * @param fstyle
   * @param fsz
   * @param overrideFonts
   *          true if we take the menuBar fonts in preference to the supplied
   *          defaults
   * @return the embedded menu instance to be added to the frame.
   */
  protected Panel makeEmbeddedPopupMenu(MenuBar menuBar, String fn,
          int fstyle, int fsz, boolean overrideFonts)
  {
    return makeEmbeddedPopupMenu(menuBar, fn, fstyle, fsz, overrideFonts,
            false);
  }

  /**
   * Create or add elements to the embedded menu from menuBar. This removes all
   * menu from menuBar and it is up to the caller to remove the now useless
   * menuBar from the Frame if it is already attached.
   * 
   * @param menuBar
   * @param fn
   * @param fstyle
   * @param fsz
   * @param overrideFonts
   * @param append
   *          true means existing menu will be emptied before adding new
   *          elements
   * @return
   */
  protected Panel makeEmbeddedPopupMenu(MenuBar menuBar, String fn,
          int fstyle, int fsz, boolean overrideFonts, boolean append)
  {
    if (!append)
    {
      if (embeddedPopup != null)
      {
        embeddedPopup.clear(); // TODO: check if j1.1
      }
      if (embeddedMenu != null)
      {
        embeddedMenu.removeAll();
      }
    }
    if (embeddedPopup == null)
    {
      embeddedPopup = new Hashtable();
    }

    embeddedMenu = makeEmbeddedPopupMenu(menuBar, fn, fstyle, fsz,
            overrideFonts, embeddedPopup, new Panel(), this);
    return embeddedMenu;
  }

  /**
   * Generic method to move elements from menubar onto embeddedMenu using the
   * existing or the supplied font, and adds binding from panel to attached
   * menus in embeddedPopup This removes all menu from menuBar and it is up to
   * the caller to remove the now useless menuBar from the Frame if it is
   * already attached.
   * 
   * @param menuBar
   *          must be non-null
   * @param fn
   * @param fstyle
   * @param fsz
   * @param overrideFonts
   * @param embeddedPopup
   *          must be non-null
   * @param embeddedMenu
   *          if null, a new panel will be created and returned
   * @param clickHandler
   *          - usually the instance of EmbmenuFrame that holds references to
   *          embeddedPopup and embeddedMenu
   * @return the panel instance for convenience.
   */
  protected Panel makeEmbeddedPopupMenu(MenuBar menuBar, String fn,
          int fstyle, int fsz, boolean overrideFonts,
          Hashtable embeddedPopup, Panel embeddedMenu,
          MouseListener clickHandler)
  {
    if (embeddedPopup == null)
    {
      throw new Error(
              "Implementation error - embeddedPopup must be non-null");
    }
    if (overrideFonts)
    {
      Font mbf = menuBar.getFont();
      if (mbf != null)
      {
        fn = mbf.getName();
        fstyle = mbf.getStyle();
        fsz = mbf.getSize();
      }
    }
    if (embeddedMenu == null)
      embeddedMenu = new Panel();
    FlowLayout flowLayout1 = new FlowLayout();
    embeddedMenu.setBackground(Color.lightGray);
    embeddedMenu.setLayout(flowLayout1);
    // loop thru
    for (int mbi = 0, nMbi = menuBar.getMenuCount(); mbi < nMbi; mbi++)
    {
      Menu mi = menuBar.getMenu(mbi);
      Label elab = new Label(mi.getLabel());
      elab.setFont(new java.awt.Font(fn, fstyle, fsz));
      // add the menu entries
      PopupMenu popup = new PopupMenu();
      int m, mSize = mi.getItemCount();
      for (m = 0; m < mSize; m++)
      {
        popup.add(mi.getItem(m));
        mSize--;
        m--;
      }
      embeddedPopup.put(elab, popup);
      embeddedMenu.add(elab);
      elab.addMouseListener(clickHandler);
    }
    flowLayout1.setAlignment(FlowLayout.LEFT);
    flowLayout1.setHgap(2);
    flowLayout1.setVgap(0);
    return embeddedMenu;
  }

  public void mousePressed(MouseEvent evt)
  {
    PopupMenu popup = null;
    Label source = (Label) evt.getSource();
    popup = getPopupMenu(source);
    if (popup != null)
    {
      embeddedMenu.add(popup);
      popup.show(embeddedMenu, source.getBounds().x, source.getBounds().y
              + source.getBounds().getSize().height);
    }
  }

  /**
   * get the menu for source from the hash.
   * 
   * @param source
   *          what was clicked on.
   */
  PopupMenu getPopupMenu(Label source)
  {
    return (PopupMenu) embeddedPopup.get(source);
  }

  public void mouseClicked(MouseEvent evt)
  {
  }

  public void mouseReleased(MouseEvent evt)
  {
  }

  public void mouseEntered(MouseEvent evt)
  {
  }

  public void mouseExited(MouseEvent evt)
  {
  }

  /**
   * called to clear the GUI resources taken up for embedding and remove any
   * self references so we can be garbage collected.
   */
  public void destroyMenus()
  {
    if (embeddedPopup != null)
    {
      Enumeration e = embeddedPopup.keys();
      while (e.hasMoreElements())
      {
        Label lb = (Label) e.nextElement();
        lb.removeMouseListener(this);
      }
      embeddedPopup.clear();
    }
    if (embeddedMenu != null)
    {
      embeddedMenu.removeAll();
    }
  }

  /**
   * calls destroyMenus()
   */
  public void finalize() throws Throwable
  {
    destroyMenus();
    embeddedPopup = null;
    embeddedMenu = null;
    super.finalize();
  }
}
