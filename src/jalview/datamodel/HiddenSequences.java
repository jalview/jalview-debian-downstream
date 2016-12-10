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
package jalview.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HiddenSequences
{
  /**
   * holds a list of hidden sequences associated with an alignment.
   */
  public SequenceI[] hiddenSequences;

  AlignmentI alignment;

  /**
   * Constructor given a reference to an alignment (with no hidden sequences)
   * 
   * @param al
   */
  public HiddenSequences(AlignmentI al)
  {
    alignment = al;
  }

  /**
   * Answers the number of hidden sequences
   * 
   * @return
   */
  public int getSize()
  {
    if (hiddenSequences == null)
    {
      return 0;
    }
    int count = 0;
    for (SequenceI seq : hiddenSequences)
    {
      if (seq != null)
      {
        count++;
      }
    }

    return count;
  }

  /**
   * Answers the length of the longest hidden sequence
   * 
   * @return
   */
  public int getWidth()
  {
    if (hiddenSequences == null)
    {
      return 0;
    }
    int width = 0;
    for (SequenceI seq : hiddenSequences)
    {
      if (seq != null && seq.getLength() > width)
      {
        width = seq.getLength();
      }
    }

    return width;
  }

  /**
   * Call this method after a sequence is removed from the main alignment
   */
  public void adjustHeightSequenceDeleted(int seqIndex)
  {
    if (hiddenSequences == null)
    {
      return;
    }

    int alHeight = alignment.getHeight();

    SequenceI[] tmp = new SequenceI[alHeight + getSize()];
    int deletionIndex = adjustForHiddenSeqs(seqIndex);

    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] == null)
      {
        continue;
      }

      if (i > deletionIndex)
      {
        tmp[i - 1] = hiddenSequences[i];
      }
      else
      {
        tmp[i] = hiddenSequences[i];
      }
    }

    hiddenSequences = tmp;

  }

  /**
   * Call this method after a sequence is added to the main alignment
   */
  public void adjustHeightSequenceAdded()
  {
    if (hiddenSequences == null)
    {
      return;
    }

    int alHeight = alignment.getHeight();

    SequenceI[] tmp = new SequenceI[alHeight + getSize()];
    System.arraycopy(hiddenSequences, 0, tmp, 0, hiddenSequences.length);
    hiddenSequences = tmp;
  }

  /**
   * Mark the specified sequence as hidden
   * 
   * @param sequence
   */
  public void hideSequence(SequenceI sequence)
  {
    if (hiddenSequences == null)
    {
      hiddenSequences = new SequenceI[alignment.getHeight()];
    }

    int alignmentIndex = alignment.findIndex(sequence);
    alignmentIndex = adjustForHiddenSeqs(alignmentIndex);

    if (hiddenSequences[alignmentIndex] != null)
    {
      System.out.println("ERROR!!!!!!!!!!!");
    }

    hiddenSequences[alignmentIndex] = sequence;

    alignment.deleteSequence(sequence);
  }

  public List<SequenceI> showAll(
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    List<SequenceI> revealedSeqs = new ArrayList<SequenceI>();
    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] != null)
      {
        List<SequenceI> tmp = showSequence(i, hiddenRepSequences);
        for (SequenceI seq : tmp)
        {
          revealedSeqs.add(seq);
        }
      }
    }
    return revealedSeqs;
  }

  /**
   * Reveals (unhides) consecutive hidden sequences just above the given
   * alignment index. The revealed sequences are selected (including their
   * visible representative sequence if there was one and 'reveal' is being
   * performed on it).
   * 
   * @param alignmentIndex
   * @param hiddenRepSequences
   *          a map of representative sequences to the sequences they represent
   * @return
   */
  public List<SequenceI> showSequence(int alignmentIndex,
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    List<SequenceI> revealedSeqs = new ArrayList<SequenceI>();
    SequenceI repSequence = alignment.getSequenceAt(alignmentIndex);
    if (repSequence != null && hiddenRepSequences != null
            && hiddenRepSequences.containsKey(repSequence))
    {
      hiddenRepSequences.remove(repSequence);
      revealedSeqs.add(repSequence);
    }

    int start = adjustForHiddenSeqs(alignmentIndex - 1);
    int end = adjustForHiddenSeqs(alignmentIndex);
    if (end >= hiddenSequences.length)
    {
      end = hiddenSequences.length - 1;
    }

    List<SequenceI> asequences;
    synchronized (asequences = alignment.getSequences())
    {
      for (int index = end; index > start; index--)
      {
        SequenceI seq = hiddenSequences[index];
        hiddenSequences[index] = null;

        if (seq != null)
        {
          if (seq.getLength() > 0)
          {
            revealedSeqs.add(seq);
            asequences.add(alignmentIndex, seq);
          }
          else
          {
            System.out.println(seq.getName()
                    + " has been deleted whilst hidden");
          }
        }
      }
    }
    return revealedSeqs;
  }

  public SequenceI getHiddenSequence(int alignmentIndex)
  {
    return hiddenSequences == null ? null : hiddenSequences[alignmentIndex];
  }

  public int findIndexWithoutHiddenSeqs(int alignmentIndex)
  {
    if (hiddenSequences == null)
    {
      return alignmentIndex;
    }
    int index = 0;
    int hiddenSeqs = 0;
    if (hiddenSequences.length <= alignmentIndex)
    {
      alignmentIndex = hiddenSequences.length - 1;
    }

    while (index <= alignmentIndex)
    {
      if (hiddenSequences[index] != null)
      {
        hiddenSeqs++;
      }
      index++;
    }

    return (alignmentIndex - hiddenSeqs);
  }

  public int adjustForHiddenSeqs(int alignmentIndex)
  {
    if (hiddenSequences == null)
    {
      return alignmentIndex;
    }
    int index = 0;
    int hSize = hiddenSequences.length;
    while (index <= alignmentIndex && index < hSize)
    {
      if (hiddenSequences[index] != null)
      {
        alignmentIndex++;
      }
      index++;
    }
    ;

    return alignmentIndex;
  }

  /**
   * makes a copy of the alignment with hidden sequences included. Using the
   * copy for anything other than simple output is not recommended. Note - this
   * method DOES NOT USE THE AlignmentI COPY CONSTRUCTOR!
   * 
   * @return
   */
  public AlignmentI getFullAlignment()
  {
    SequenceI[] seq;
    if (hiddenSequences == null)
    {
      seq = alignment.getSequencesArray();
    }
    else
    {
      int isize = hiddenSequences.length;
      seq = new Sequence[isize];

      int index = 0;
      for (int i = 0; i < hiddenSequences.length; i++)
      {
        if (hiddenSequences[i] != null)
        {
          seq[i] = hiddenSequences[i];
        }
        else
        {
          seq[i] = alignment.getSequenceAt(index);
          index++;
        }
      }
    }
    Alignment fAlignmt = new Alignment(seq);
    fAlignmt.annotations = alignment.getAlignmentAnnotation();
    fAlignmt.alignmentProperties = alignment.getProperties();
    fAlignmt.groups = alignment.getGroups();
    fAlignmt.hasRNAStructure = alignment.hasRNAStructure();
    fAlignmt.setSeqrep(alignment.getSeqrep());

    return fAlignmt;
  }

  public boolean isHidden(SequenceI seq)
  {
    if (hiddenSequences != null)
    {
      for (int i = 0; i < hiddenSequences.length; i++)
      {
        if (hiddenSequences[i] != null && hiddenSequences[i] == seq)
        {
          return true;
        }
      }
    }

    return false;
  }
}
