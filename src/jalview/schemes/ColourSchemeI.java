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

import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.Map;

public interface ColourSchemeI
{
  /**
   * 
   * @param c
   * @return the colour for the given character
   */
  public Color findColour(char c);

  /**
   * 
   * @param c
   *          - sequence symbol or gap
   * @param j
   *          - position in seq
   * @param seq
   *          - sequence being coloured
   * @return context dependent colour for the given symbol at the position in
   *         the given sequence
   */
  public Color findColour(char c, int j, SequenceI seq);

  /**
   * assign the given consensus profile for the colourscheme
   */
  public void setConsensus(java.util.Hashtable[] h);

  /**
   * assign the given conservation to the colourscheme
   * 
   * @param c
   */
  public void setConservation(jalview.analysis.Conservation c);

  /**
   * enable or disable conservation shading for this colourscheme
   * 
   * @param conservationApplied
   */
  public void setConservationApplied(boolean conservationApplied);

  /**
   * 
   * @return true if conservation shading is enabled for this colourscheme
   */
  public boolean conservationApplied();

  /**
   * set scale factor for bleaching of colour in unconserved regions
   * 
   * @param i
   */
  public void setConservationInc(int i);

  /**
   * 
   * @return scale factor for bleaching colour in unconserved regions
   */
  public int getConservationInc();

  /**
   * 
   * @return percentage identity threshold for applying colourscheme
   */
  public int getThreshold();

  /**
   * set percentage identity threshold and type of %age identity calculation for
   * shading
   * 
   * @param ct
   *          0..100 percentage identity for applying this colourscheme
   * @param ignoreGaps
   *          when true, calculate PID without including gapped positions
   */
  public void setThreshold(int ct, boolean ignoreGaps);

  /**
   * recalculate dependent data using the given sequence collection, taking
   * account of hidden rows
   * 
   * @param alignment
   * @param hiddenReps
   */
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps);

  /**
   * create a new instance of the colourscheme configured to colour the given
   * connection
   * 
   * @param sg
   * @param hiddenRepSequences
   * @return copy of current scheme with any inherited settings transfered
   */
  public ColourSchemeI applyTo(AnnotatedCollectionI sg,
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences);

}
