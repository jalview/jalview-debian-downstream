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
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.ProfileI;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.util.ColorUtils;
import jalview.util.Comparison;
import jalview.util.MessageManager;

import java.awt.Color;
import java.util.Map;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class ResidueColourScheme implements ColourSchemeI
{
  final int[] symbolIndex;

  boolean conservationColouring = false;

  Color[] colors = null;

  int threshold = 0;

  /* Set when threshold colouring to either pid_gaps or pid_nogaps */
  protected boolean ignoreGaps = false;

  /*
   * Consensus data indexed by column
   */
  ProfilesI consensus;

  /*
   * Conservation string as a char array 
   */
  char[] conservation;

  /*
   * The conservation slider percentage setting 
   */
  int inc = 30;

  /**
   * Creates a new ResidueColourScheme object.
   * 
   * @param final int[] index table into colors (ResidueProperties.naIndex or
   *        ResidueProperties.aaIndex)
   * @param colors
   *          colours for symbols in sequences
   * @param threshold
   *          threshold for conservation shading
   */
  public ResidueColourScheme(int[] aaOrnaIndex, Color[] colours,
          int threshold)
  {
    symbolIndex = aaOrnaIndex;
    this.colors = colours;
    this.threshold = threshold;
  }

  /**
   * Creates a new ResidueColourScheme object with a lookup table for indexing
   * the colour map
   */
  public ResidueColourScheme(int[] aaOrNaIndex)
  {
    symbolIndex = aaOrNaIndex;
  }

  /**
   * Creates a new ResidueColourScheme object - default constructor for
   * non-sequence dependent colourschemes
   */
  public ResidueColourScheme()
  {
    symbolIndex = null;
  }

  /**
   * Find a colour without an index in a sequence
   */
  @Override
  public Color findColour(char c)
  {
    return colors == null ? Color.white : colors[symbolIndex[c]];
  }

  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    Color currentColour;

    if (colors != null && symbolIndex != null && (threshold == 0)
            || aboveThreshold(c, j))
    {
      currentColour = colors[symbolIndex[c]];
    }
    else
    {
      currentColour = Color.white;
    }

    if (conservationColouring)
    {
      currentColour = applyConservation(currentColour, j);
    }

    return currentColour;
  }

  /**
   * Get the percentage threshold for this colour scheme
   * 
   * @return Returns the percentage threshold
   */
  @Override
  public int getThreshold()
  {
    return threshold;
  }

  /**
   * Sets the percentage consensus threshold value, and whether gaps are ignored
   * in percentage identity calculation
   * 
   * @param consensusThreshold
   * @param ignoreGaps
   */
  @Override
  public void setThreshold(int consensusThreshold, boolean ignoreGaps)
  {
    threshold = consensusThreshold;
    this.ignoreGaps = ignoreGaps;
  }

  /**
   * Answers true if there is a consensus profile for the specified column, and
   * the given residue matches the consensus (or joint consensus) residue for
   * the column, and the percentage identity for the profile is equal to or
   * greater than the current threshold; else answers false. The percentage
   * calculation depends on whether or not we are ignoring gapped sequences.
   * 
   * @param residue
   * @param column
   *          (index into consensus profiles)
   * 
   * @return
   * @see #setThreshold(int, boolean)
   */
  public boolean aboveThreshold(char residue, int column)
  {
    if ('a' <= residue && residue <= 'z')
    {
      // TO UPPERCASE !!!
      // Faster than toUpperCase
      residue -= ('a' - 'A');
    }

    if (consensus == null)
    {
      return false;
    }

    ProfileI profile = consensus.get(column);

    /*
     * test whether this is the consensus (or joint consensus) residue
     */
    if (profile != null
            && profile.getModalResidue().contains(String.valueOf(residue)))
    {
      if (profile.getPercentageIdentity(ignoreGaps) >= threshold)
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean conservationApplied()
  {
    return conservationColouring;
  }

  @Override
  public void setConservationApplied(boolean conservationApplied)
  {
    conservationColouring = conservationApplied;
  }

  @Override
  public void setConservationInc(int i)
  {
    inc = i;
  }

  @Override
  public int getConservationInc()
  {
    return inc;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param consensus
   *          DOCUMENT ME!
   */
  @Override
  public void setConsensus(ProfilesI consensus)
  {
    if (consensus == null)
    {
      return;
    }

    this.consensus = consensus;
  }

  @Override
  public void setConservation(Conservation cons)
  {
    if (cons == null)
    {
      conservationColouring = false;
      conservation = null;
    }
    else
    {
      conservationColouring = true;
      int iSize = cons.getConsSequence().getLength();
      conservation = new char[iSize];
      for (int i = 0; i < iSize; i++)
      {
        conservation[i] = cons.getConsSequence().getCharAt(i);
      }
    }

  }

  /**
   * Applies a combination of column conservation score, and conservation
   * percentage slider, to 'bleach' out the residue colours towards white.
   * <p>
   * If a column is fully conserved (identical residues, conservation score 11,
   * shown as *), or all 10 physico-chemical properties are conserved
   * (conservation score 10, shown as +), then the colour is left unchanged.
   * <p>
   * Otherwise a 'bleaching' factor is computed and applied to the colour. This
   * is designed to fade colours for scores of 0-9 completely to white at slider
   * positions ranging from 18% - 100% respectively.
   * 
   * @param currentColour
   * @param column
   * 
   * @return bleached (or unmodified) colour
   */
  Color applyConservation(Color currentColour, int column)
  {
    if (conservation == null || conservation.length <= column)
    {
      return currentColour;
    }
    char conservationScore = conservation[column];

    /*
     * if residues are fully conserved (* or 11), or all properties
     * are conserved (+ or 10), leave colour unchanged
     */
    if (conservationScore == '*' || conservationScore == '+'
            || conservationScore == (char) 10
            || conservationScore == (char) 11)
    {
      return currentColour;
    }

    if (Comparison.isGap(conservationScore))
    {
      return Color.white;
    }

    /*
     * convert score 0-9 to a bleaching factor 1.1 - 0.2
     */
    float bleachFactor = (11 - (conservationScore - '0')) / 10f;

    /*
     * scale this up by 0-5 (percentage slider / 20)
     * as a result, scores of:         0  1  2  3  4  5  6  7  8  9
     * fade to white at slider value: 18 20 22 25 29 33 40 50 67 100%
     */
    bleachFactor *= (inc / 20f);

    return ColorUtils.bleachColour(currentColour, bleachFactor);
  }

  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
  }

  @Override
  public ColourSchemeI applyTo(AnnotatedCollectionI sg,
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    try
    {
      return getClass().newInstance();
    } catch (Exception q)
    {
      throw new Error(MessageManager.formatMessage(
              "error.implementation_error_cannot_duplicate_colour_scheme",
              new String[] { getClass().getName() }), q);
    }
  }
}
