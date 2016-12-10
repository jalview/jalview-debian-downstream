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

import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.ViewBasedAnalysisI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceFeature;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class FeatureScoreModel implements ScoreModelI, ViewBasedAnalysisI
{
  jalview.api.FeatureRenderer fr;

  @Override
  public boolean configureFromAlignmentView(
          jalview.api.AlignmentViewPanel view)
  {
    fr = view.cloneFeatureRenderer();
    return true;
  }

  @Override
  public float[][] findDistances(AlignmentView seqData)
  {
    int nofeats = 0;
    List<String> dft = fr.getDisplayedFeatureTypes();
    nofeats = dft.size();
    SeqCigar[] seqs = seqData.getSequences();
    int noseqs = seqs.length;
    int cpwidth = 0;// = seqData.getWidth();
    float[][] distance = new float[noseqs][noseqs];
    if (nofeats == 0)
    {
      for (float[] d : distance)
      {
        for (int i = 0; i < d.length; d[i++] = 0f)
        {
          ;
        }
      }
      return distance;
    }
    // need to get real position for view position
    int[] viscont = seqData.getVisibleContigs();
    for (int vc = 0; vc < viscont.length; vc += 2)
    {

      for (int cpos = viscont[vc]; cpos <= viscont[vc + 1]; cpos++)
      {
        cpwidth++;
        // get visible features at cpos under view's display settings and
        // compare them
        List<Hashtable<String, SequenceFeature>> sfap = new ArrayList<Hashtable<String, SequenceFeature>>();
        for (int i = 0; i < noseqs; i++)
        {
          Hashtable<String, SequenceFeature> types = new Hashtable<String, SequenceFeature>();
          int spos = seqs[i].findPosition(cpos);
          if (spos != -1)
          {
            List<SequenceFeature> sfs = fr.findFeaturesAtRes(
                    seqs[i].getRefSeq(), spos);
            for (SequenceFeature sf : sfs)
            {
              types.put(sf.getType(), sf);
            }
          }
          sfap.add(types);
        }
        for (int i = 0; i < (noseqs - 1); i++)
        {
          if (cpos == 0)
          {
            distance[i][i] = 0f;
          }
          for (int j = i + 1; j < noseqs; j++)
          {
            int sfcommon = 0;
            // compare the two lists of features...
            Hashtable<String, SequenceFeature> fi = sfap.get(i), fk, fj = sfap
                    .get(j);
            if (fi.size() > fj.size())
            {
              fk = fj;
            }
            else
            {
              fk = fi;
              fi = fj;
            }
            for (String k : fi.keySet())
            {
              SequenceFeature sfj = fk.get(k);
              if (sfj != null)
              {
                sfcommon++;
              }
            }
            distance[i][j] += (fi.size() + fk.size() - 2f * sfcommon);
            distance[j][i] += distance[i][j];
          }
        }
      }
    }
    for (int i = 0; i < noseqs; i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        distance[i][j] /= cpwidth;
        distance[j][i] = distance[i][j];
      }
    }
    return distance;
  }

  @Override
  public String getName()
  {
    return "Sequence Feature Similarity";
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

  @Override
  public String toString()
  {
    return "Score between sequences based on hamming distance between binary vectors marking features displayed at each column";
  }
}
