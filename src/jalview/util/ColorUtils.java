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

  /**
   * Returns a color between minColour and maxColour; the RGB values are in
   * proportion to where 'value' lies between minValue and maxValue
   * 
   * @param value
   * @param minValue
   * @param minColour
   * @param maxValue
   * @param maxColour
   * @return
   */
  public static Color getGraduatedColour(float value, float minValue,
          Color minColour, float maxValue, Color maxColour)
  {
    if (minValue == maxValue)
    {
      return minColour;
    }
    if (value < minValue)
    {
      value = minValue;
    }
    if (value > maxValue)
    {
      value = maxValue;
    }

    /*
     * prop = proportion of the way value is from minValue to maxValue
     */
    float prop = (value - minValue) / (maxValue - minValue);
    float r = minColour.getRed() + prop
            * (maxColour.getRed() - minColour.getRed());
    float g = minColour.getGreen() + prop
            * (maxColour.getGreen() - minColour.getGreen());
    float b = minColour.getBlue() + prop
            * (maxColour.getBlue() - minColour.getBlue());
    return new Color(r / 255, g / 255, b / 255);
  }

  /**
   * 'Fades' the given colour towards white by the specified proportion. A
   * factor of 1 or more results in White, a factor of 0 leaves the colour
   * unchanged, and a factor between 0 and 1 results in a proportionate change
   * of RGB values towards (255, 255, 255).
   * <p>
   * A negative bleachFactor can be specified to darken the colour towards Black
   * (0, 0, 0).
   * 
   * @param colour
   * @param bleachFactor
   * @return
   */
  public static Color bleachColour(Color colour, float bleachFactor)
  {
    if (bleachFactor >= 1f)
    {
      return Color.WHITE;
    }
    if (bleachFactor <= -1f)
    {
      return Color.BLACK;
    }
    if (bleachFactor == 0f)
    {
      return colour;
    }

    int red = colour.getRed();
    int green = colour.getGreen();
    int blue = colour.getBlue();

    if (bleachFactor > 0)
    {
      red += (255 - red) * bleachFactor;
      green += (255 - green) * bleachFactor;
      blue += (255 - blue) * bleachFactor;
      return new Color(red, green, blue);
    }
    else
    {
      float factor = 1 + bleachFactor;
      red *= factor;
      green *= factor;
      blue *= factor;
      return new Color(red, green, blue);
    }
  }
}
