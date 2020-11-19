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
package jalview.renderer;

import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

import java.awt.Color;

public class OverviewResColourFinder extends ResidueColourFinder
{
  final Color GAP_COLOUR; // default colour to use at gaps

  final Color RESIDUE_COLOUR; // default colour to use at residues

  final Color HIDDEN_COLOUR; // colour for hidden regions

  boolean useLegacy = false;

  public static final Color OVERVIEW_DEFAULT_GAP = Color.lightGray;

  public static final Color OVERVIEW_DEFAULT_LEGACY_GAP = Color.white;

  public static final Color OVERVIEW_DEFAULT_HIDDEN = Color.darkGray
          .darker();

  /**
   * Constructor without colour settings (used by applet)
   */
  public OverviewResColourFinder()
  {
    this(false, OVERVIEW_DEFAULT_GAP, OVERVIEW_DEFAULT_HIDDEN);
  }

  /**
   * Constructor with colour settings
   * 
   * @param useLegacyColouring
   *          whether to use legacy gap colouring (white gaps, grey residues)
   * @param gapCol
   *          gap colour if not legacy
   * @param hiddenCol
   *          hidden region colour (transparency applied by rendering code)
   */
  public OverviewResColourFinder(boolean useLegacyColouring, Color gapCol,
          Color hiddenCol)
  {
    if (useLegacyColouring)
    {
      GAP_COLOUR = Color.white;
      RESIDUE_COLOUR = Color.lightGray;
      HIDDEN_COLOUR = hiddenCol;
    }
    else
    {
      GAP_COLOUR = gapCol;
      RESIDUE_COLOUR = Color.white;
      HIDDEN_COLOUR = hiddenCol;
    }
  }

  @Override
  public Color getBoxColour(ResidueShaderI shader, SequenceI seq, int i)
  {
    Color resBoxColour = RESIDUE_COLOUR;
    char currentChar = seq.getCharAt(i);

    // In the overview window, gaps are coloured grey, unless the colour scheme
    // specifies a gap colour, in which case gaps honour the colour scheme
    // settings
    if (shader.getColourScheme() != null)
    {
      if (Comparison.isGap(currentChar)
              && (!shader.getColourScheme().hasGapColour()))
      {
        resBoxColour = GAP_COLOUR;
      }
      else
      {
        resBoxColour = shader.findColour(currentChar, i, seq);
      }
    }
    else if (Comparison.isGap(currentChar))
    {
      resBoxColour = GAP_COLOUR;
    }

    return resBoxColour;
  }

  /**
   * {@inheritDoc} In the overview, the showBoxes setting is ignored, as the
   * overview displays the colours regardless.
   */
  @Override
  protected Color getResidueBoxColour(boolean showBoxes,
          ResidueShaderI shader,
          SequenceGroup[] allGroups, SequenceI seq, int i)
  {
    ResidueShaderI currentShader;
    SequenceGroup currentSequenceGroup = getCurrentSequenceGroup(allGroups,
            i);
    if (currentSequenceGroup != null)
    {
      currentShader = currentSequenceGroup.getGroupColourScheme();
    }
    else
    {
      currentShader = shader;
    }

    return getBoxColour(currentShader, seq, i);
  }

  /**
   * Supply hidden colour
   * 
   * @return colour of hidden regions
   */
  protected Color getHiddenColour()
  {
    return HIDDEN_COLOUR;
  }
}
