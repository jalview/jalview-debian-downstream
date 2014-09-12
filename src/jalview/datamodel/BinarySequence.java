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
package jalview.datamodel;

import jalview.schemes.*;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class BinarySequence extends Sequence
{
  int[] binary;

  double[] dbinary;

  /**
   * Creates a new BinarySequence object.
   * 
   * @param s
   *          DOCUMENT ME!
   */
  public BinarySequence(String s)
  {
    super("", s, 0, s.length());
  }

  /**
   * DOCUMENT ME!
   */
  public void encode()
  {
    // Set all matrix to 0
    dbinary = new double[getSequence().length * 21];

    int nores = 21;

    for (int i = 0; i < dbinary.length; i++)
    {
      dbinary[i] = 0.0;
    }

    for (int i = 0; i < getSequence().length; i++)
    {
      int aanum = 20;

      try
      {
        aanum = ResidueProperties.aaIndex[getCharAt(i)];
      } catch (NullPointerException e)
      {
        aanum = 20;
      }

      if (aanum > 20)
      {
        aanum = 20;
      }

      dbinary[(i * nores) + aanum] = 1.0;
    }
  }

  /**
   * ancode using substitution matrix given in matrix
   * 
   * @param matrix
   */
  public void matrixEncode(ScoreMatrix matrix)
  {
    matrixEncode(matrix.isDNA() ? ResidueProperties.nucleotideIndex
            : ResidueProperties.aaIndex, matrix.getMatrix());
  }

  /**
   * DOCUMENT ME!
   */
  public void blosumEncode()
  {
    matrixEncode(ResidueProperties.aaIndex, ResidueProperties.getBLOSUM62());
  }

  private void matrixEncode(int[] aaIndex, int[][] matrix)
  {
    // Set all matrix to 0
    dbinary = new double[getSequence().length * 21];

    int nores = 21;

    // for (int i = 0; i < dbinary.length; i++) {
    // dbinary[i] = 0.0;
    // }
    for (int i = 0; i < getSequence().length; i++)
    {
      int aanum = 20;

      try
      {
        aanum = aaIndex[getCharAt(i)];
      } catch (NullPointerException e)
      {
        aanum = 20;
      }

      if (aanum > 20)
      {
        aanum = 20;
      }

      // Do the blosum thing

      for (int j = 0; j < 20; j++)
      {
        dbinary[(i * nores) + j] = matrix[aanum][j];
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String toBinaryString()
  {
    String out = "";

    for (int i = 0; i < binary.length; i++)
    {
      out += (new Integer(binary[i])).toString();

      if (i < (binary.length - 1))
      {
        out += " ";
      }
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public double[] getDBinary()
  {
    return dbinary;
  }

}
