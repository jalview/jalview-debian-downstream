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
package jalview.renderer;

import jalview.api.AlignViewportI;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculate and display alignment rulers
 * 
 * @author jprocter
 *
 */
public class ScaleRenderer
{
  public final class ScaleMark
  {
    public final boolean major;

    public final int column;

    public final String text;

    ScaleMark(boolean isMajor, int col, String txt)
    {
      major = isMajor;
      column = col;
      text = txt;
    }
  }

  /**
   * calculate positions markers on the alignment ruler
   * 
   * @param av
   * @param startx
   *          left-most column in visible view
   * @param endx
   *          - right-most column in visible view
   * @return List of ScaleMark holding boolean: true/false for major/minor mark,
   *         marker position in alignment column coords, a String to be rendered
   *         at the position (or null)
   */
  public List<ScaleMark> calculateMarks(AlignViewportI av, int startx,
          int endx)
  {
    int scalestartx = (startx / 10) * 10;

    SequenceI refSeq = av.getAlignment().getSeqrep();
    int refSp = 0, refStartI = 0, refEndI = -1;
    if (refSeq != null)
    {
      // find bounds and set origin appopriately
      // locate first visible position for this sequence
      int[] refbounds = av.getColumnSelection()
              .locateVisibleBoundsOfSequence(refSeq);

      refSp = refbounds[0];
      refStartI = refbounds[4];
      refEndI = refbounds[5];
      scalestartx = refSp + ((scalestartx - refSp) / 10) * 10;
    }

    if (refSeq == null && scalestartx % 10 == 0)
    {
      scalestartx += 5;
    }
    List<ScaleMark> marks = new ArrayList<ScaleMark>();
    String string;
    int refN, iadj;
    // todo: add a 'reference origin column' to set column number relative to
    for (int i = scalestartx; i < endx; i += 5)
    {
      if (((i - refSp) % 10) == 0)
      {
        if (refSeq == null)
        {
          iadj = av.getColumnSelection().adjustForHiddenColumns(i - 1) + 1;
          string = String.valueOf(iadj);
        }
        else
        {
          iadj = av.getColumnSelection().adjustForHiddenColumns(i - 1);
          refN = refSeq.findPosition(iadj);
          // TODO show bounds if position is a gap
          // - ie L--R -> "1L|2R" for
          // marker
          if (iadj < refStartI)
          {
            string = String.valueOf(iadj - refStartI);
          }
          else if (iadj > refEndI)
          {
            string = "+" + String.valueOf(iadj - refEndI);
          }
          else
          {
            string = String.valueOf(refN) + refSeq.getCharAt(iadj);
          }
        }
        marks.add(new ScaleMark(true, i - startx - 1, string));
      }
      else
      {
        marks.add(new ScaleMark(false, i - startx - 1, null));
      }
    }
    return marks;
  }

}
