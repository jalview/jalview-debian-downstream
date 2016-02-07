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
package jalview.gui;

import jalview.analysis.AlignSeq;
import jalview.datamodel.Alignment;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GPairwiseAlignPanel;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PairwiseAlignPanel extends GPairwiseAlignPanel
{

  AlignmentViewport av;

  Vector sequences;

  /**
   * Creates a new PairwiseAlignPanel object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public PairwiseAlignPanel(AlignmentViewport av)
  {
    super();
    this.av = av;

    sequences = new Vector();

    SequenceI[] seqs;
    String[] seqStrings = av.getViewAsString(true);

    if (av.getSelectionGroup() == null)
    {
      seqs = av.getAlignment().getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.getAlignment());
    }

    String type = (av.getAlignment().isNucleotide()) ? AlignSeq.DNA
            : AlignSeq.PEP;

    float[][] scores = new float[seqs.length][seqs.length];
    double totscore = 0;
    int count = seqs.length;

    Sequence seq;

    for (int i = 1; i < count; i++)
    {
      for (int j = 0; j < i; j++)
      {

        AlignSeq as = new AlignSeq(seqs[i], seqStrings[i], seqs[j],
                seqStrings[j], type);

        if (as.s1str.length() == 0 || as.s2str.length() == 0)
        {
          continue;
        }

        as.calcScoreMatrix();
        as.traceAlignment();

        as.printAlignment(System.out);
        scores[i][j] = (float) as.getMaxScore()
                / (float) as.getASeq1().length;
        totscore = totscore + scores[i][j];

        textarea.append(as.getOutput());
        sequences.add(as.getAlignedSeq1());
        sequences.add(as.getAlignedSeq2());
      }
    }

    if (count > 2)
    {
      System.out
              .println("Pairwise alignment scaled similarity score matrix\n");

      for (int i = 0; i < count; i++)
      {
        jalview.util.Format.print(System.out, "%s \n", ("" + i) + " "
                + seqs[i].getName());
      }

      System.out.println("\n");

      for (int i = 0; i < count; i++)
      {
        for (int j = 0; j < i; j++)
        {
          jalview.util.Format.print(System.out, "%7.3f", scores[i][j]
                  / totscore);
        }
      }

      System.out.println("\n");
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void viewInEditorButton_actionPerformed(ActionEvent e)
  {
    Sequence[] seq = new Sequence[sequences.size()];

    for (int i = 0; i < sequences.size(); i++)
    {
      seq[i] = (Sequence) sequences.elementAt(i);
    }

    AlignFrame af = new AlignFrame(new Alignment(seq),
            AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);

    Desktop.addInternalFrame(af,
            MessageManager.getString("label.pairwise_aligned_sequences"),
            AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
  }
}
