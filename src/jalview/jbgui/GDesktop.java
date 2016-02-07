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

import jalview.api.AlignmentViewPanel;
import jalview.util.MessageManager;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GDesktop extends JFrame
{
  protected static JMenu windowMenu = new JMenu();

  JMenuBar desktopMenubar = new JMenuBar();

  JMenu FileMenu = new JMenu();

  JMenu HelpMenu = new JMenu();

  protected JMenu VamsasMenu = new JMenu();

  protected JMenu VamsasStMenu = new JMenu();

  JMenuItem inputLocalFileMenuItem = new JMenuItem();

  JMenuItem inputURLMenuItem = new JMenuItem();

  JMenuItem inputTextboxMenuItem = new JMenuItem();

  JMenuItem quit = new JMenuItem();

  JMenuItem aboutMenuItem = new JMenuItem();

  JMenuItem documentationMenuItem = new JMenuItem();

  FlowLayout flowLayout1 = new FlowLayout();

  protected JMenu toolsMenu = new JMenu();

  JMenuItem preferences = new JMenuItem();

  JMenuItem saveState = new JMenuItem();

  JMenuItem loadState = new JMenuItem();

  JMenu inputMenu = new JMenu();

  protected JMenuItem vamsasStart = new JMenuItem();

  protected JMenuItem vamsasImport = new JMenuItem();

  protected JMenuItem vamsasSave = new JMenuItem();

  JMenuItem inputSequence = new JMenuItem();

  protected JMenuItem vamsasStop = new JMenuItem();

  JMenuItem closeAll = new JMenuItem();

  JMenuItem raiseRelated = new JMenuItem();

  JMenuItem minimizeAssociated = new JMenuItem();

  protected JCheckBoxMenuItem showMemusage = new JCheckBoxMenuItem();

  JMenuItem garbageCollect = new JMenuItem();

  protected JCheckBoxMenuItem showConsole = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showNews = new JCheckBoxMenuItem();

  protected JMenuItem snapShotWindow = new JMenuItem();

  /**
   * Creates a new GDesktop object.
   */
  public GDesktop()
  {
    try
    {
      jbInit();
      this.setJMenuBar(desktopMenubar);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    if (!new jalview.util.Platform().isAMac())
    {
      FileMenu.setMnemonic('F');
      inputLocalFileMenuItem.setMnemonic('L');
      VamsasMenu.setMnemonic('V');
      inputURLMenuItem.setMnemonic('U');
      inputTextboxMenuItem.setMnemonic('C');
      quit.setMnemonic('Q');
      saveState.setMnemonic('S');
      loadState.setMnemonic('L');
      inputMenu.setMnemonic('I');
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws Exception
   *           DOCUMENT ME!
   */
  private void jbInit() throws Exception
  {

    FileMenu.setText(MessageManager.getString("action.file"));
    HelpMenu.setText(MessageManager.getString("action.help"));
    VamsasMenu.setText("Vamsas");
    VamsasMenu.setToolTipText(MessageManager
            .getString("label.share_data_vamsas_applications"));
    VamsasStMenu.setText(MessageManager.getString("label.connect_to"));
    VamsasStMenu.setToolTipText(MessageManager
            .getString("label.join_existing_vamsas_session"));
    inputLocalFileMenuItem.setText(MessageManager
            .getString("label.load_tree_from_file"));
    inputLocalFileMenuItem.setAccelerator(javax.swing.KeyStroke
            .getKeyStroke(java.awt.event.KeyEvent.VK_O, Toolkit
                    .getDefaultToolkit().getMenuShortcutKeyMask(), false));
    inputLocalFileMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                inputLocalFileMenuItem_actionPerformed(null);
              }
            });
    inputURLMenuItem.setText(MessageManager.getString("label.from_url"));
    inputURLMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        inputURLMenuItem_actionPerformed(null);
      }
    });
    inputTextboxMenuItem.setText(MessageManager
            .getString("label.from_textbox"));
    inputTextboxMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                inputTextboxMenuItem_actionPerformed(null);
              }
            });
    quit.setText(MessageManager.getString("action.quit"));
    quit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        quit();
      }
    });
    aboutMenuItem.setText(MessageManager.getString("label.about"));
    aboutMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        aboutMenuItem_actionPerformed(e);
      }
    });
    documentationMenuItem.setText(MessageManager
            .getString("label.documentation"));
    documentationMenuItem.setAccelerator(javax.swing.KeyStroke
            .getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0, false));
    documentationMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                documentationMenuItem_actionPerformed(e);
              }
            });
    this.getContentPane().setLayout(flowLayout1);
    windowMenu.setText(MessageManager.getString("label.window"));
    preferences.setText(MessageManager.getString("label.preferences"));
    preferences.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        preferences_actionPerformed(e);
      }
    });
    toolsMenu.setText(MessageManager.getString("label.tools"));
    saveState.setText(MessageManager.getString("action.save_project"));
    saveState.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveState_actionPerformed(e);
      }
    });
    loadState.setText(MessageManager.getString("action.load_project"));
    loadState.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        loadState_actionPerformed(e);
      }
    });
    inputMenu.setText(MessageManager.getString("label.input_alignment"));
    vamsasStart.setText(MessageManager
            .getString("label.new_vamsas_session"));
    vamsasStart.setVisible(false);
    vamsasStart.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        vamsasStart_actionPerformed(e);
      }
    });
    vamsasImport.setText(MessageManager
            .getString("action.load_vamsas_session"));
    vamsasImport.setVisible(false);
    vamsasImport.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        vamsasImport_actionPerformed(e);
      }
    });
    vamsasSave.setText(MessageManager
            .getString("action.save_vamsas_session"));
    vamsasSave.setVisible(false);
    vamsasSave.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        vamsasSave_actionPerformed(e);
      }
    });
    inputSequence.setText(MessageManager
            .getString("action.fetch_sequences"));
    inputSequence.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        inputSequence_actionPerformed(e);
      }
    });
    vamsasStop.setText(MessageManager
            .getString("label.stop_vamsas_session"));
    vamsasStop.setVisible(false);
    vamsasStop.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        vamsasStop_actionPerformed(e);
      }
    });
    closeAll.setText(MessageManager.getString("action.close_all"));
    closeAll.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        closeAll_actionPerformed(e);
      }
    });
    raiseRelated.setText(MessageManager
            .getString("action.raise_associated_windows"));
    raiseRelated.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        raiseRelated_actionPerformed(e);
      }
    });
    minimizeAssociated.setText(MessageManager
            .getString("action.minimize_associated_windows"));
    minimizeAssociated.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        minimizeAssociated_actionPerformed(e);
      }
    });
    garbageCollect.setText(MessageManager
            .getString("label.collect_garbage"));
    garbageCollect.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        garbageCollect_actionPerformed(e);
      }
    });
    showMemusage.setText(MessageManager
            .getString("label.show_memory_usage"));
    showMemusage.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        showMemusage_actionPerformed(e);
      }
    });
    showConsole
            .setText(MessageManager.getString("label.show_java_console"));
    showConsole.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        showConsole_actionPerformed(e);
      }
    });
    showNews.setText(MessageManager.getString("label.show_jalview_news"));
    showNews.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        showNews_actionPerformed(e);
      }
    });
    snapShotWindow.setText(MessageManager.getString("label.take_snapshot"));
    snapShotWindow.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        snapShotWindow_actionPerformed(e);
      }
    });

    desktopMenubar.add(FileMenu);
    desktopMenubar.add(toolsMenu);
    VamsasMenu.setVisible(false);
    desktopMenubar.add(VamsasMenu);
    desktopMenubar.add(HelpMenu);
    desktopMenubar.add(windowMenu);
    FileMenu.add(inputMenu);
    FileMenu.add(inputSequence);
    FileMenu.addSeparator();
    FileMenu.add(saveState);
    FileMenu.add(loadState);
    FileMenu.addSeparator();
    FileMenu.add(quit);
    HelpMenu.add(aboutMenuItem);
    HelpMenu.add(documentationMenuItem);
    VamsasMenu.add(VamsasStMenu);
    VamsasStMenu.setVisible(false);
    VamsasMenu.add(vamsasStart);
    VamsasMenu.add(vamsasImport);
    VamsasMenu.add(vamsasSave);
    VamsasMenu.add(vamsasStop);
    toolsMenu.add(preferences);
    toolsMenu.add(showMemusage);
    toolsMenu.add(showConsole);
    toolsMenu.add(showNews);
    toolsMenu.add(garbageCollect);
    // toolsMenu.add(snapShotWindow);
    inputMenu.add(inputLocalFileMenuItem);
    inputMenu.add(inputURLMenuItem);
    inputMenu.add(inputTextboxMenuItem);
    windowMenu.add(closeAll);
    windowMenu.add(raiseRelated);
    windowMenu.add(minimizeAssociated);
    windowMenu.addSeparator();
    // inputMenu.add(vamsasLoad);
  }

  protected void snapShotWindow_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showConsole_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showNews_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showMemusage_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void garbageCollect_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void vamsasStMenu_actionPerformed()
  {
  }

  public void vamsasSave_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void inputLocalFileMenuItem_actionPerformed(
          jalview.gui.AlignViewport av)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void inputURLMenuItem_actionPerformed(
          jalview.gui.AlignViewport av)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void inputTextboxMenuItem_actionPerformed(AlignmentViewPanel avp)
  {
  }

  /**
   * DOCUMENT ME!
   */
  protected void quit()
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void aboutMenuItem_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void documentationMenuItem_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void SaveState_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void preferences_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void saveState_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void loadState_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void loadJalviewAlign_actionPerformed(ActionEvent e)
  {
  }

  public void vamsasStart_actionPerformed(ActionEvent e)
  {

  }

  public void inputSequence_actionPerformed(ActionEvent e)
  {

  }

  public void vamsasStop_actionPerformed(ActionEvent e)
  {

  }

  public void closeAll_actionPerformed(ActionEvent e)
  {

  }

  public void raiseRelated_actionPerformed(ActionEvent e)
  {

  }

  public void minimizeAssociated_actionPerformed(ActionEvent e)
  {

  }

  public void vamsasImport_actionPerformed(ActionEvent e)
  {
  }
}
