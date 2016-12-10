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
package jalview.analysis.scoremodels;

import jalview.analysis.AlignSeq;
import jalview.api.analysis.ScoreModelI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

public class SWScoreModel implements ScoreModelI
{

  @Override
  public float[][] findDistances(AlignmentView seqData)
  {
    SequenceI[] sequenceString = seqData.getVisibleAlignment(
            Comparison.GapChars.charAt(0)).getSequencesArray();
    int noseqs = sequenceString.length;
    float[][] distance = new float[noseqs][noseqs];

    float max = -1;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i; j < noseqs; j++)
      {
        AlignSeq as = new AlignSeq(sequenceString[i], sequenceString[j],
                seqData.isNa() ? "dna" : "pep");
        as.calcScoreMatrix();
        as.traceAlignment();
        as.printAlignment(System.out);
        distance[i][j] = (float) as.maxscore;

        if (max < distance[i][j])
        {
          max = distance[i][j];
        }
      }
    }

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i; j < noseqs; j++)
      {
        distance[i][j] = max - distance[i][j];
        distance[j][i] = distance[i][j];
      }
    }

    return distance;
  }

  @Override
  public String getName()
  {
    return "Smith Waterman Score";
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

  public String toString()
  {
    return "Score between two sequences aligned with Smith Waterman with default Peptide/Nucleotide matrix";
  }
}
