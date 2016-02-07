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
package jalview.schemes;

import java.awt.Color;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class StrandColourScheme extends ScoreColourScheme
{
  /**
   * Creates a new StrandColourScheme object.
   */
  public StrandColourScheme()
  {
    super(ResidueProperties.aaIndex, ResidueProperties.strand,
            ResidueProperties.strandmin, ResidueProperties.strandmax);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param c
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Color makeColour(float c)
  {
    return new Color(c, c, (float) 1.0 - c);
  }
}
