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
package jalview.schemes;

import java.awt.*;

public interface ColourSchemeI
{
  public Color findColour(char c);

  public Color findColour(char c, int j);

  public void setConsensus(java.util.Hashtable[] h);

  public void setConservation(jalview.analysis.Conservation c);

  public boolean conservationApplied();

  public void setConservationInc(int i);

  public int getConservationInc();

  public int getThreshold();

  public void setThreshold(int ct, boolean ignoreGaps);

}
