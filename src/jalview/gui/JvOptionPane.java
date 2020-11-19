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

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.Icon;
import javax.swing.JOptionPane;

public class JvOptionPane extends JOptionPane
{
  /**
   * 
   */
  private static final long serialVersionUID = -3019167117756785229L;

  private static Object mockResponse = JvOptionPane.CANCEL_OPTION;

  private static boolean interactiveMode = true;

  public static int showConfirmDialog(Component parentComponent,
          Object message) throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message)
            : (int) getMockResponse();
  }

  public static int showConfirmDialog(Component parentComponent,
          Object message, String title, int optionType)
          throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType)
            : (int) getMockResponse();
  }

  public static int showConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType)
          throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType, messageType)
            : (int) getMockResponse();
  }

  public static int showConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType,
          Icon icon) throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType, messageType, icon)
            : (int) getMockResponse();
  }

  public static int showInternalConfirmDialog(Component parentComponent,
          Object message)
  {
    return isInteractiveMode()
            ? JOptionPane.showInternalConfirmDialog(parentComponent,
                    message)
            : (int) getMockResponse();
  }

  public static int showInternalConfirmDialog(Component parentComponent,
          Object message, String title, int optionType)
  {
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType)
            : (int) getMockResponse();
  }

  public static int showInternalConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType)
  {
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType, messageType)
            : (int) getMockResponse();
  }

  public static int showInternalConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType,
          Icon icon)
  {
    return isInteractiveMode()
            ? JOptionPane.showInternalConfirmDialog(parentComponent,
                    message, title, optionType, messageType, icon)
            : (int) getMockResponse();
  }

  public static int showOptionDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType,
          Icon icon, Object[] options, Object initialValue)
          throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showOptionDialog(parentComponent, message, title,
                    optionType, messageType, icon, options, initialValue)
            : (int) getMockResponse();
  }

  public static void showMessageDialog(Component parentComponent,
          Object message) throws HeadlessException
  {
    if (isInteractiveMode())
    {
      JOptionPane.showMessageDialog(parentComponent, message);
    }
    else
    {
      outputMessage(message);
    }
  }

  public static void showMessageDialog(Component parentComponent,
          Object message, String title, int messageType)
          throws HeadlessException
  {
    if (isInteractiveMode())
    {
      JOptionPane.showMessageDialog(parentComponent, message, title,
              messageType);
    }
    else
    {
      outputMessage(message);
    }
  }

  public static void showMessageDialog(Component parentComponent,
          Object message, String title, int messageType, Icon icon)
          throws HeadlessException
  {
    if (isInteractiveMode())
    {
      JOptionPane.showMessageDialog(parentComponent, message, title,
              messageType, icon);
    }
    else
    {
      outputMessage(message);
    }
  }

  public static void showInternalMessageDialog(Component parentComponent,
          Object message)
  {
    if (isInteractiveMode())
    {
      JOptionPane.showMessageDialog(parentComponent, message);
    }
    else
    {
      outputMessage(message);
    }
  }

  public static void showInternalMessageDialog(Component parentComponent,
          Object message, String title, int messageType)
  {
    if (isInteractiveMode())
    {
      JOptionPane.showMessageDialog(parentComponent, message, title,
              messageType);
    }
    else
    {
      outputMessage(message);
    }
  }

  public static void showInternalMessageDialog(Component parentComponent,
          Object message, String title, int messageType, Icon icon)
  {
    if (isInteractiveMode())
    {
      JOptionPane.showMessageDialog(parentComponent, message, title,
              messageType, icon);
    }
    else
    {
      outputMessage(message);
    }
  }

  public static String showInputDialog(Object message)
          throws HeadlessException
  {
    return isInteractiveMode() ? JOptionPane.showInputDialog(message)
            : getMockResponse().toString();
  }

  public static String showInputDialog(Object message,
          Object initialSelectionValue)
  {
    return isInteractiveMode()
            ? JOptionPane.showInputDialog(message, initialSelectionValue)
            : getMockResponse().toString();
  }

  public static String showInputDialog(Component parentComponent,
          Object message) throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message)
            : getMockResponse().toString();
  }

  public static String showInputDialog(Component parentComponent,
          Object message, Object initialSelectionValue)
  {
    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  public static String showInputDialog(Component parentComponent,
          Object message, String title, int messageType)
          throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message, title,
                    messageType)
            : getMockResponse().toString();
  }

  public static Object showInputDialog(Component parentComponent,
          Object message, String title, int messageType, Icon icon,
          Object[] selectionValues, Object initialSelectionValue)
          throws HeadlessException
  {
    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message, title,
                    messageType, icon, selectionValues,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  public static String showInternalInputDialog(Component parentComponent,
          Object message)
  {
    return isInteractiveMode()
            ? JOptionPane.showInternalInputDialog(parentComponent, message)
            : getMockResponse().toString();
  }

  public static String showInternalInputDialog(Component parentComponent,
          Object message, String title, int messageType)
  {
    return isInteractiveMode()
            ? JOptionPane.showInternalInputDialog(parentComponent, message,
                    title, messageType)
            : getMockResponse().toString();
  }

  public static Object showInternalInputDialog(Component parentComponent,
          Object message, String title, int messageType, Icon icon,
          Object[] selectionValues, Object initialSelectionValue)
  {
    return isInteractiveMode()
            ? JOptionPane.showInternalInputDialog(parentComponent, message,
                    title, messageType, icon, selectionValues,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  private static void outputMessage(Object message)
  {
    System.out.println(">>> JOption Message : " + message.toString());
  }

  public static Object getMockResponse()
  {
    return mockResponse;
  }

  public static void setMockResponse(Object mockOption)
  {
    JvOptionPane.mockResponse = mockOption;
  }

  public static void resetMock()
  {
    setMockResponse(JvOptionPane.CANCEL_OPTION);
    setInteractiveMode(true);
  }

  public static boolean isInteractiveMode()
  {
    return interactiveMode;
  }

  public static void setInteractiveMode(boolean interactiveMode)
  {
    JvOptionPane.interactiveMode = interactiveMode;
  }

}
