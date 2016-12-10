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

import jalview.analysis.Conservation;
import jalview.datamodel.ProfilesI;

/**
 * Colourscheme that takes its colours from some other colourscheme
 * 
 * @author jimp
 * 
 */
public class FollowerColourScheme extends ResidueColourScheme
{

  protected ColourSchemeI colourScheme;

  public ColourSchemeI getBaseColour()
  {
    return colourScheme;
  }

  @Override
  public void setConsensus(ProfilesI consensus)
  {
    if (colourScheme != null)
    {
      colourScheme.setConsensus(consensus);
    }
  }

  @Override
  public void setConservation(Conservation cons)
  {
    if (colourScheme != null)
    {
      colourScheme.setConservation(cons);
    }
  }

  @Override
  public void setConservationInc(int i)
  {
    if (colourScheme != null)
    {
      colourScheme.setConservationInc(i);
    }
  }

}
