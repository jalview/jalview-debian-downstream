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
/**
 * author: Lauren Michelle Lui
 */

package jalview.util;

import java.awt.Color;
import java.util.Random;

public class ColorUtils
{

  /**
   * Generates a random color, will mix with input color. Code taken from
   * http://stackoverflow
   * .com/questions/43044/algorithm-to-randomly-generate-an-aesthetically
   * -pleasing-color-palette
   * 
   * @param mix
   * @return Random color in RGB
   */
  public static final Color generateRandomColor(Color mix)
  {
    Random random = new Random();
    int red = random.nextInt(256);
    int green = random.nextInt(256);
    int blue = random.nextInt(256);

    // mix the color
    if (mix != null)
    {
      red = (red + mix.getRed()) / 2;
      green = (green + mix.getGreen()) / 2;
      blue = (blue + mix.getBlue()) / 2;
    }

    Color color = new Color(red, green, blue);
    return color;

  }

  /**
   * Convert to Tk colour code format
   * 
   * @param colour
   * @return
   * @see http
   *      ://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/colortool.html#
   *      tkcode
   */
  public static final String toTkCode(Color colour)
  {
    String colstring = "#" + ((colour.getRed() < 16) ? "0" : "")
            + Integer.toHexString(colour.getRed())
            + ((colour.getGreen() < 16) ? "0" : "")
            + Integer.toHexString(colour.getGreen())
            + ((colour.getBlue() < 16) ? "0" : "")
            + Integer.toHexString(colour.getBlue());
    return colstring;
  }

  /**
   * Returns a colour three shades darker. Note you can't guarantee that
   * brighterThan reverses this, as darkerThan may result in black.
   * 
   * @param col
   * @return
   */
  public static Color darkerThan(Color col)
  {
    return col == null ? null : col.darker().darker().darker();
  }

  /**
   * Returns a colour three shades brighter. Note you can't guarantee that
   * darkerThan reverses this, as brighterThan may result in white.
   * 
   * @param col
   * @return
   */
  public static Color brighterThan(Color col)
  {
    return col == null ? null : col.brighter().brighter().brighter();
  }

}
