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

import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.Map;

public class Blosum62ColourScheme extends ResidueColourScheme
{
  private static final Color LIGHT_BLUE = new Color(204, 204, 255);
  private static final Color DARK_BLUE = new Color(154, 154, 255);

  public Blosum62ColourScheme()
  {
    super();
  }

  @Override
  public Color findColour(char res, int j, SequenceI seq)
  {
    if ('a' <= res && res <= 'z')
    {
      // TO UPPERCASE !!!
      res -= ('a' - 'A');
    }

    if (consensus == null || consensus.get(j) == null
            || (threshold != 0 && !aboveThreshold(res, j)))
    {
      return Color.white;
    }

    Color currentColour;

    if (!Comparison.isGap(res))
    {
      /*
       * test if this is the consensus (or joint consensus) residue
       */
      String max = consensus.get(j).getModalResidue();

      if (max.indexOf(res) > -1)
      {
        currentColour = DARK_BLUE;
      }
      else
      {
        int c = 0;
        int max_aa = 0;
        int n = max.length();

        do
        {
          c += ResidueProperties.getBLOSUM62(max.charAt(max_aa), res);
        } while (++max_aa < n);

        if (c > 0)
        {
          currentColour = LIGHT_BLUE;
        }
        else
        {
          currentColour = Color.white;
        }
      }

      if (conservationColouring)
      {
        currentColour = applyConservation(currentColour, j);
      }
    }
    else
    {
      return Color.white;
    }

    return currentColour;
  }

  @Override
  public ColourSchemeI applyTo(AnnotatedCollectionI sg,
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    ColourSchemeI newcs = super.applyTo(sg, hiddenRepSequences);
    return newcs;
  }
}
