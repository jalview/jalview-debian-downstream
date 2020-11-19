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

import jalview.util.MessageManager;
import jalview.util.Platform;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.desktop.QuitStrategy;

import javax.swing.JOptionPane;

public class APQHandlers
{
  private static boolean setAPQHandlers = false;

  public APQHandlers() {
  }

  protected static boolean setAPQHandlers(jalview.gui.Desktop jalviewDesktop)
  {
    // flagging this test to avoid unnecessary reflection
    if (!setAPQHandlers)
    {
      // see if the Quit, About and Preferences handlers are available
      Class desktopClass = Desktop.class;
      Desktop hdesktop = Desktop.getDesktop();

      try
      {
        Float specversion = Float.parseFloat(
                System.getProperty("java.specification.version"));

        if (specversion >= 9)
        {
          if (Platform.isAMac())
          {
            if (desktopClass.getDeclaredMethod("setAboutHandler",
                    new Class[]
                    { AboutHandler.class }) != null)
            {

              hdesktop.setAboutHandler(new AboutHandler()
              {
                @Override
                public void handleAbout(AboutEvent e)
                {
                  jalviewDesktop.aboutMenuItem_actionPerformed(null);
                }
              });

            }

            if (desktopClass.getDeclaredMethod("setPreferencesHandler",
                    new Class[]
                    { PreferencesHandler.class }) != null)
            {

              hdesktop.setPreferencesHandler(
                      new PreferencesHandler()
              {
                        @Override
                        public void handlePreferences(
                                PreferencesEvent e)
                        {
                          jalviewDesktop.preferences_actionPerformed(null);
                        }
                      });

            }

            if (desktopClass.getDeclaredMethod("setQuitHandler",
                    new Class[]
                    { QuitHandler.class }) != null)
            {

              hdesktop.setQuitHandler(new QuitHandler()
              {
                @Override
                public void handleQuitRequestWith(
                        QuitEvent e, QuitResponse r)
                {
                  boolean confirmQuit = jalview.bin.Cache
                          .getDefault(
                                  jalview.gui.Desktop.CONFIRM_KEYBOARD_QUIT,
                                  true);
                  int n;
                  if (confirmQuit)
                  {
                    n = JOptionPane.showConfirmDialog(null,
                            MessageManager.getString("label.quit_jalview"),
                            MessageManager.getString("action.quit"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE, null);
                  }
                  else
                  {
                    n = JOptionPane.OK_OPTION;
                  }
                  if (n == JOptionPane.OK_OPTION)
                  {
                    System.out.println("Shortcut Quit confirmed by user");
                    jalviewDesktop.quit();
                    r.performQuit(); // probably won't reach this line, but just
                                     // in
                                     // case
                  }
                  else
                  {
                    r.cancelQuit();
                    System.out.println("Shortcut Quit cancelled by user");
                  }
                }
              });
              hdesktop.setQuitStrategy(
                      QuitStrategy.CLOSE_ALL_WINDOWS);

            }
          }
          setAPQHandlers = true;
        }
        else
        {
          System.out.println(
                  "Not going to try setting APQ Handlers as java.spec.version is "
                          + specversion);
        }

      } catch (Exception e)
      {
        System.out.println(
                "Exception when looking for About, Preferences, Quit Handlers");
        // e.printStackTrace();
      } catch (Throwable t)
      {
        System.out.println(
                "Throwable when looking for About, Preferences, Quit Handlers");
        // t.printStackTrace();
      }

    }
    
    return setAPQHandlers;
  }

}
