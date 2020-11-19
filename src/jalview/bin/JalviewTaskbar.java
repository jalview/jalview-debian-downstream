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
package jalview.bin;

import java.awt.Image;
import java.awt.Taskbar;

public class JalviewTaskbar
{
  public JalviewTaskbar()
  {
  }

  protected static void setTaskbar(Jalview jalview)
  {
    
    if (Taskbar.isTaskbarSupported())
    {
      Taskbar tb = Taskbar.getTaskbar();
      if (tb.isSupported(Taskbar.Feature.ICON_IMAGE))
      {
        try
        {
          java.net.URL url = jalview.getClass()
                  .getResource("/images/JalviewLogo_Huge.png");
          if (url != null)
          {
            Image image = java.awt.Toolkit.getDefaultToolkit()
                    .createImage(url);
            tb.setIconImage(image);
          }
        } catch (Exception e)
        {
          System.out.println("Unable to setIconImage()");
        }
      }
    }

  }

}
