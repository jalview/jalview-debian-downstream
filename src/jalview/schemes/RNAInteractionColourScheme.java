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
package jalview.schemes;

import jalview.datamodel.SequenceI;

import java.awt.Color;

public class RNAInteractionColourScheme extends ResidueColourScheme
{
  public RNAInteractionColourScheme()
  {
    super();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public Color findColour(char c)
  {
    // System.out.println("called"); log.debug
    return colors[ResidueProperties.nucleotideIndex[c]];
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    Color currentColour;
    if ((threshold == 0) || aboveThreshold(c, j))
    {
      try
      {
        currentColour = colors[ResidueProperties.nucleotideIndex[c]];
      } catch (Exception ex)
      {
        return Color.white;
      }
    }
    else
    {
      return Color.white;
    }

    if (conservationColouring)
    {
      currentColour = applyConservation(currentColour, j);
    }

    return currentColour;
  }
}
