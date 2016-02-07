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

public class PIDScoreModel implements ScoreModelI
{

  @Override
  public float[][] findDistances(AlignmentView seqData)
  {
    String[] sequenceString = seqData
            .getSequenceStrings(Comparison.GapChars.charAt(0));
    int noseqs = sequenceString.length;
    float[][] distance = new float[noseqs][noseqs];
    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i; j < noseqs; j++)
      {
        if (j == i)
        {
          distance[i][i] = 0;
        }
        else
        {
          distance[i][j] = 100 - Comparison.PID(sequenceString[i],
                  sequenceString[j]);

          distance[j][i] = distance[i][j];
        }
      }
    }
    return distance;
  }

  @Override
  public String getName()
  {
    return "PID";
  }

  @Override
  public boolean isDNA()
  {
    return true;
  }

  @Override
  public boolean isProtein()
  {
    return true;
  }

}
