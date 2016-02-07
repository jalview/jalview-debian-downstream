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
package jalview.analysis.scoremodels;

import jalview.api.analysis.ScoreModelI;
import jalview.datamodel.AlignmentView;
import jalview.util.Comparison;

public abstract class PairwiseSeqScoreModel implements ScoreModelI
{
  abstract public int getPairwiseScore(char c, char d);

  public float[][] findDistances(AlignmentView seqData)
  {
    String[] sequenceString = seqData
            .getSequenceStrings(Comparison.GapChars.charAt(0));
    int noseqs = sequenceString.length;
    float[][] distance = new float[noseqs][noseqs];

    int maxscore = 0;
    int end = sequenceString[0].length();
    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i; j < noseqs; j++)
      {
        int score = 0;

        for (int k = 0; k < end; k++)
        {
          try
          {
            score += getPairwiseScore(sequenceString[i].charAt(k),
                    sequenceString[j].charAt(k));
          } catch (Exception ex)
          {
            System.err.println("err creating " + getName() + " tree");
            ex.printStackTrace();
          }
        }

        distance[i][j] = (float) score;

        if (score > maxscore)
        {
          maxscore = score;
        }
      }
    }

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i; j < noseqs; j++)
      {
        distance[i][j] = (float) maxscore - distance[i][j];
        distance[j][i] = distance[i][j];
      }
    }
    return distance;
  }

  abstract public int[][] getMatrix();
}
