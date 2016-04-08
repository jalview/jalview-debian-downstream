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

import java.util.*;

import jalview.analysis.*;

/**
 * Data structure to hold and manipulate a multiple sequence alignment
 */
/**
 * @author JimP
 * 
 */
public class Alignment implements AlignmentI
{
  protected Alignment dataset;

  protected Vector sequences;

  protected Vector groups = new Vector();

  protected char gapCharacter = '-';

  protected int type = NUCLEOTIDE;

  public static final int PROTEIN = 0;

  public static final int NUCLEOTIDE = 1;

  /** DOCUMENT ME!! */
  public AlignmentAnnotation[] annotations;

  HiddenSequences hiddenSequences = new HiddenSequences(this);

  public Hashtable alignmentProperties;

  private void initAlignment(SequenceI[] seqs)
  {
    int i = 0;

    if (jalview.util.Comparison.isNucleotide(seqs))
    {
      type = NUCLEOTIDE;
    }
    else
    {
      type = PROTEIN;
    }

    sequences = new Vector();

    for (i = 0; i < seqs.length; i++)
    {
      sequences.addElement(seqs[i]);
    }

  }

  /**
   * Make an alignment from an array of Sequences.
   * 
   * @param sequences
   */
  public Alignment(SequenceI[] seqs)
  {
    initAlignment(seqs);
  }

  /**
   * Make a new alignment from an array of SeqCigars
   * 
   * @param seqs
   *          SeqCigar[]
   */
  public Alignment(SeqCigar[] alseqs)
  {
    SequenceI[] seqs = SeqCigar.createAlignmentSequences(alseqs,
            gapCharacter, new ColumnSelection(), null);
    initAlignment(seqs);
  }

  /**
   * Make a new alignment from an CigarArray JBPNote - can only do this when
   * compactAlignment does not contain hidden regions. JBPNote - must also check
   * that compactAlignment resolves to a set of SeqCigars - or construct them
   * appropriately.
   * 
   * @param compactAlignment
   *          CigarArray
   */
  public static AlignmentI createAlignment(CigarArray compactAlignment)
  {
    throw new Error("Alignment(CigarArray) not yet implemented");
    // this(compactAlignment.refCigars);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Vector getSequences()
  {
    return sequences;
  }

  public SequenceI[] getSequencesArray()
  {
    if (sequences == null)
      return null;
    SequenceI[] reply = new SequenceI[sequences.size()];
    for (int i = 0; i < sequences.size(); i++)
    {
      reply[i] = (SequenceI) sequences.elementAt(i);
    }
    return reply;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public SequenceI getSequenceAt(int i)
  {
    if (i>-1 && i < sequences.size())
    {
      return (SequenceI) sequences.elementAt(i);
    }

    return null;
  }

  /**
   * Adds a sequence to the alignment. Recalculates maxLength and size.
   * 
   * @param snew
   */
  public void addSequence(SequenceI snew)
  {
    if (dataset != null)
    {
      // maintain dataset integrity
      if (snew.getDatasetSequence() != null)
      {
        getDataset().addSequence(snew.getDatasetSequence());
      }
      else
      {
        // derive new sequence
        SequenceI adding = snew.deriveSequence();
        getDataset().addSequence(adding.getDatasetSequence());
        snew = adding;
      }
    }
    if (sequences == null)
    {
      initAlignment(new SequenceI[]
      { snew });
    }
    else
    {
      sequences.addElement(snew);
    }
    if (hiddenSequences != null)
      hiddenSequences.adjustHeightSequenceAdded();
  }

  /**
   * Adds a sequence to the alignment. Recalculates maxLength and size.
   * 
   * @param snew
   */
  public void setSequenceAt(int i, SequenceI snew)
  {
    SequenceI oldseq = getSequenceAt(i);
    deleteSequence(oldseq);

    sequences.setElementAt(snew, i);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Vector getGroups()
  {
    return groups;
  }

  public void finalize()
  {
    if (getDataset() != null)
      getDataset().removeAlignmentRef();

    dataset = null;
    sequences = null;
    groups = null;
    annotations = null;
    hiddenSequences = null;
  }

  /**
   * decrement the alignmentRefs counter by one and call finalize if it goes to
   * zero.
   */
  private void removeAlignmentRef()
  {
    if (--alignmentRefs == 0)
    {
      finalize();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param s
   *          DOCUMENT ME!
   */
  public void deleteSequence(SequenceI s)
  {
    deleteSequence(findIndex(s));
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   */
  public void deleteSequence(int i)
  {
    if (i > -1 && i < getHeight())
    {
      sequences.removeElementAt(i);
      hiddenSequences.adjustHeightSequenceDeleted(i);
    }
  }

  /**    */
  public SequenceGroup findGroup(SequenceI s)
  {
    for (int i = 0; i < this.groups.size(); i++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(i);

      if (sg.getSequences(null).contains(s))
      {
        return sg;
      }
    }

    return null;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param s
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public SequenceGroup[] findAllGroups(SequenceI s)
  {
    Vector temp = new Vector();

    int gSize = groups.size();
    for (int i = 0; i < gSize; i++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(i);
      if (sg == null || sg.getSequences(null) == null)
      {
        this.deleteGroup(sg);
        gSize--;
        continue;
      }

      if (sg.getSequences(null).contains(s))
      {
        temp.addElement(sg);
      }
    }

    SequenceGroup[] ret = new SequenceGroup[temp.size()];

    for (int i = 0; i < temp.size(); i++)
    {
      ret[i] = (SequenceGroup) temp.elementAt(i);
    }

    return ret;
  }

  /**    */
  public void addGroup(SequenceGroup sg)
  {
    if (!groups.contains(sg))
    {
      if (hiddenSequences.getSize() > 0)
      {
        int i, iSize = sg.getSize();
        for (i = 0; i < iSize; i++)
        {
          if (!sequences.contains(sg.getSequenceAt(i)))
          {
            sg.deleteSequence(sg.getSequenceAt(i), false);
            iSize--;
            i--;
          }
        }

        if (sg.getSize() < 1)
        {
          return;
        }
      }

      groups.addElement(sg);
    }
  }

  /**
   * remove any annotation that references gp
   * 
   * @param gp
   *          (if null, removes all group associated annotation)
   */
  private void removeAnnotationForGroup(SequenceGroup gp)
  {
    if (annotations == null || annotations.length == 0)
    {
      return;
    }
    // remove annotation very quickly
    AlignmentAnnotation[] t, todelete = new AlignmentAnnotation[annotations.length], tokeep = new AlignmentAnnotation[annotations.length];
    int i, p, k;
    if (gp == null)
    {
      for (i = 0, p = 0, k = 0; i < annotations.length; i++)
      {
        if (annotations[i].groupRef != null)
        {
          todelete[p++] = annotations[i];
        }
        else
        {
          tokeep[k++] = annotations[i];
        }
      }
    }
    else
    {
      for (i = 0, p = 0, k = 0; i < annotations.length; i++)
      {
        if (annotations[i].groupRef == gp)
        {
          todelete[p++] = annotations[i];
        }
        else
        {
          tokeep[k++] = annotations[i];
        }
      }
    }
    if (p > 0)
    {
      // clear out the group associated annotation.
      for (i = 0; i < p; i++)
      {
        unhookAnnotation(todelete[i]);
        todelete[i] = null;
      }
      t = new AlignmentAnnotation[k];
      for (i = 0; i < k; i++)
      {
        t[i] = tokeep[i];
      }
      annotations = t;
    }
  }

  public void deleteAllGroups()
  {
    if (annotations != null)
    {
      removeAnnotationForGroup(null);
    }
    groups.removeAllElements();
  }

  /**    */
  public void deleteGroup(SequenceGroup g)
  {
    if (groups.contains(g))
    {
      removeAnnotationForGroup(g);
      groups.removeElement(g);
    }
  }

  /**    */
  public SequenceI findName(String name)
  {
    return findName(name, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findName(java.lang.String, boolean)
   */
  public SequenceI findName(String token, boolean b)
  {
    return findName(null, token, b);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findName(SequenceI, java.lang.String,
   * boolean)
   */
  public SequenceI findName(SequenceI startAfter, String token, boolean b)
  {

    int i = 0;
    SequenceI sq = null;
    String sqname = null;
    if (startAfter != null)
    {
      // try to find the sequence in the alignment
      boolean matched = false;
      while (i < sequences.size())
      {
        if (getSequenceAt(i++) == startAfter)
        {
          matched = true;
          break;
        }
      }
      if (!matched)
      {
        i = 0;
      }
    }
    while (i < sequences.size())
    {
      sq = getSequenceAt(i);
      sqname = sq.getName();
      if (sqname.equals(token) // exact match
              || (b && // allow imperfect matches - case varies
              (sqname.equalsIgnoreCase(token))))
      {
        return getSequenceAt(i);
      }

      i++;
    }

    return null;
  }

  public SequenceI[] findSequenceMatch(String name)
  {
    Vector matches = new Vector();
    int i = 0;

    while (i < sequences.size())
    {
      if (getSequenceAt(i).getName().equals(name))
      {
        matches.addElement(getSequenceAt(i));
      }
      i++;
    }

    SequenceI[] result = new SequenceI[matches.size()];
    for (i = 0; i < result.length; i++)
    {
      result[i] = (SequenceI) matches.elementAt(i);
    }

    return result;

  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findIndex(jalview.datamodel.SequenceI)
   */
  public int findIndex(SequenceI s)
  {
    int i = 0;

    while (i < sequences.size())
    {
      if (s == getSequenceAt(i))
      {
        return i;
      }

      i++;
    }

    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.AlignmentI#findIndex(jalview.datamodel.SearchResults)
   */
  public int findIndex(SearchResults results)
  {
    int i = 0;

    while (i < sequences.size())
    {
      if (results.involvesSequence(getSequenceAt(i)))
      {
        return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getHeight()
  {
    return sequences.size();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getWidth()
  {
    int maxLength = -1;

    for (int i = 0; i < sequences.size(); i++)
    {
      if (getSequenceAt(i).getLength() > maxLength)
      {
        maxLength = getSequenceAt(i).getLength();
      }
    }

    return maxLength;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param gc
   *          DOCUMENT ME!
   */
  public void setGapCharacter(char gc)
  {
    gapCharacter = gc;

    for (int i = 0; i < sequences.size(); i++)
    {
      Sequence seq = (Sequence) sequences.elementAt(i);
      seq.setSequence(seq.getSequenceAsString().replace('.', gc)
              .replace('-', gc).replace(' ', gc));
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public char getGapCharacter()
  {
    return gapCharacter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#isAligned()
   */
  public boolean isAligned()
  {
    return isAligned(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#isAligned(boolean)
   */
  public boolean isAligned(boolean includeHidden)
  {
    int width = getWidth();
    if (hiddenSequences == null || hiddenSequences.getSize() == 0)
    {
      includeHidden = true; // no hidden sequences to check against.
    }
    for (int i = 0; i < sequences.size(); i++)
    {
      if (includeHidden || !hiddenSequences.isHidden(getSequenceAt(i)))
      {
        if (getSequenceAt(i).getLength() != width)
        {
          return false;
        }
      }
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#deleteAnnotation(jalview.datamodel.
   * AlignmentAnnotation)
   */
  public boolean deleteAnnotation(AlignmentAnnotation aa)
  {
    return deleteAnnotation(aa, true);
  }
  
  public boolean deleteAnnotation(AlignmentAnnotation aa, boolean unhook)
  {
    int aSize = 1;

    if (annotations != null)
    {
      aSize = annotations.length;
    }

    if (aSize < 1)
    {
      return false;
    }

    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize - 1];

    boolean swap = false;
    int tIndex = 0;

    for (int i = 0; i < aSize; i++)
    {
      if (annotations[i] == aa)
      {
        swap = true;
        continue;
      }
      if (tIndex < temp.length)
        temp[tIndex++] = annotations[i];
    }

    if (swap)
    {
      annotations = temp;
      if (unhook) {
        unhookAnnotation(aa);
      }
    }
    return swap;
  }

  /**
   * remove any object references associated with this annotation
   * 
   * @param aa
   */
  private void unhookAnnotation(AlignmentAnnotation aa)
  {
    if (aa.sequenceRef != null)
    {
      aa.sequenceRef.removeAlignmentAnnotation(aa);
    }
    if (aa.groupRef != null)
    {
      // probably need to do more here in the future (post 2.5.0)
      aa.groupRef = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#addAnnotation(jalview.datamodel.
   * AlignmentAnnotation)
   */
  public void addAnnotation(AlignmentAnnotation aa)
  {
    addAnnotation(aa, -1);
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#addAnnotation(jalview.datamodel.
   * AlignmentAnnotation, int)
   */
  public void addAnnotation(AlignmentAnnotation aa, int pos)
  {
    int aSize = 1;
    if (annotations != null)
    {
      aSize = annotations.length + 1;
    }

    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize];
    int i = 0;
    if (pos == -1 || pos >= aSize)
    {
      temp[aSize - 1] = aa;
    }
    else
    {
      temp[pos] = aa;
    }
    if (aSize > 1)
    {
      int p = 0;
      for (i = 0; i < (aSize - 1); i++, p++)
      {
        if (p == pos)
        {
          p++;
        }
        if (p < temp.length)
        {
          temp[p] = annotations[i];
        }
      }
    }

    annotations = temp;
  }

  public void setAnnotationIndex(AlignmentAnnotation aa, int index)
  {
    if (aa == null || annotations == null || annotations.length - 1 < index)
    {
      return;
    }

    int aSize = annotations.length;
    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize];

    temp[index] = aa;

    for (int i = 0; i < aSize; i++)
    {
      if (i == index)
      {
        continue;
      }

      if (i < index)
      {
        temp[i] = annotations[i];
      }
      else
      {
        temp[i] = annotations[i - 1];
      }
    }

    annotations = temp;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public AlignmentAnnotation[] getAlignmentAnnotation()
  {
    return annotations;
  }

  public void setNucleotide(boolean b)
  {
    if (b)
    {
      type = NUCLEOTIDE;
    }
    else
    {
      type = PROTEIN;
    }
  }

  public boolean isNucleotide()
  {
    if (type == NUCLEOTIDE)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public void setDataset(Alignment data)
  {
    if (dataset == null && data == null)
    {
      // Create a new dataset for this alignment.
      // Can only be done once, if dataset is not null
      // This will not be performed
      SequenceI[] seqs = new SequenceI[getHeight()];
      SequenceI currentSeq;
      for (int i = 0; i < getHeight(); i++)
      {
        currentSeq = getSequenceAt(i);
        if (currentSeq.getDatasetSequence() != null)
        {
          seqs[i] = (Sequence) currentSeq.getDatasetSequence();
        }
        else
        {
          seqs[i] = currentSeq.createDatasetSequence();
        }
      }

      dataset = new Alignment(seqs);
    }
    else if (dataset == null && data != null)
    {
      dataset = data;
    }
    dataset.addAlignmentRef();
  }

  /**
   * reference count for number of alignments referencing this one.
   */
  int alignmentRefs = 0;

  /**
   * increase reference count to this alignment.
   */
  private void addAlignmentRef()
  {
    alignmentRefs++;
  }

  public Alignment getDataset()
  {
    return dataset;
  }

  public boolean padGaps()
  {
    boolean modified = false;

    // Remove excess gaps from the end of alignment
    int maxLength = -1;

    SequenceI current;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);
      for (int j = current.getLength(); j > maxLength; j--)
      {
        if (j > maxLength
                && !jalview.util.Comparison.isGap(current.getCharAt(j)))
        {
          maxLength = j;
          break;
        }
      }
    }

    maxLength++;

    int cLength;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);
      cLength = current.getLength();

      if (cLength < maxLength)
      {
        current.insertCharAt(cLength, maxLength - cLength, gapCharacter);
        modified = true;
      }
      else if (current.getLength() > maxLength)
      {
        current.deleteChars(maxLength, current.getLength());
      }
    }
    return modified;
  }

  /**
   * Justify the sequences to the left or right by deleting and inserting gaps
   * before the initial residue or after the terminal residue
   * 
   * @param right
   *          true if alignment padded to right, false to justify to left
   * @return true if alignment was changed
   */
  public boolean justify(boolean right)
  {
    boolean modified = false;

    // Remove excess gaps from the end of alignment
    int maxLength = -1;
    int ends[] = new int[sequences.size() * 2];
    SequenceI current;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);
      // This should really be a sequence method
      ends[i * 2] = current.findIndex(current.getStart());
      ends[i * 2 + 1] = current.findIndex(current.getStart()
              + current.getLength());
      boolean hitres = false;
      for (int j = 0, rs = 0, ssiz = current.getLength(); j < ssiz; j++)
      {
        if (!jalview.util.Comparison.isGap(current.getCharAt(j)))
        {
          if (!hitres)
          {
            ends[i * 2] = j;
            hitres = true;
          }
          else
          {
            ends[i * 2 + 1] = j;
            if (j - ends[i * 2] > maxLength)
            {
              maxLength = j - ends[i * 2];
            }
          }
        }
      }
    }

    maxLength++;
    // now edit the flanking gaps to justify to either left or right
    int cLength, extent, diff;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);

      cLength = 1 + ends[i * 2 + 1] - ends[i * 2];
      diff = maxLength - cLength; // number of gaps to indent
      extent = current.getLength();
      if (right)
      {
        // right justify
        if (extent > ends[i * 2 + 1])
        {
          current.deleteChars(ends[i * 2 + 1] + 1, extent);
          modified = true;
        }
        if (ends[i * 2] > diff)
        {
          current.deleteChars(0, ends[i * 2] - diff);
          modified = true;
        }
        else
        {
          if (ends[i * 2] < diff)
          {
            current.insertCharAt(0, diff - ends[i * 2], gapCharacter);
            modified = true;
          }
        }
      }
      else
      {
        // left justify
        if (ends[i * 2] > 0)
        {
          current.deleteChars(0, ends[i * 2]);
          modified = true;
          ends[i * 2 + 1] -= ends[i * 2];
          extent -= ends[i * 2];
        }
        if (extent > maxLength)
        {
          current.deleteChars(maxLength + 1, extent);
          modified = true;
        }
        else
        {
          if (extent < maxLength)
          {
            current.insertCharAt(extent, maxLength - extent, gapCharacter);
            modified = true;
          }
        }
      }
    }
    return modified;
  }

  public HiddenSequences getHiddenSequences()
  {
    return hiddenSequences;
  }

  public CigarArray getCompactAlignment()
  {
    SeqCigar alseqs[] = new SeqCigar[sequences.size()];
    for (int i = 0; i < sequences.size(); i++)
    {
      alseqs[i] = new SeqCigar((SequenceI) sequences.elementAt(i));
    }
    CigarArray cal = new CigarArray(alseqs);
    cal.addOperation(CigarArray.M, getWidth());
    return cal;
  }

  public void setProperty(Object key, Object value)
  {
    if (alignmentProperties == null)
      alignmentProperties = new Hashtable();

    alignmentProperties.put(key, value);
  }

  public Object getProperty(Object key)
  {
    if (alignmentProperties != null)
      return alignmentProperties.get(key);
    else
      return null;
  }

  public Hashtable getProperties()
  {
    return alignmentProperties;
  }

  AlignedCodonFrame[] codonFrameList = null;

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.AlignmentI#addCodonFrame(jalview.datamodel.AlignedCodonFrame
   * )
   */
  public void addCodonFrame(AlignedCodonFrame codons)
  {
    if (codons == null)
      return;
    if (codonFrameList == null)
    {
      codonFrameList = new AlignedCodonFrame[]
      { codons };
      return;
    }
    AlignedCodonFrame[] t = new AlignedCodonFrame[codonFrameList.length + 1];
    System.arraycopy(codonFrameList, 0, t, 0, codonFrameList.length);
    t[codonFrameList.length] = codons;
    codonFrameList = t;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#getCodonFrame(int)
   */
  public AlignedCodonFrame getCodonFrame(int index)
  {
    return codonFrameList[index];
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.AlignmentI#getCodonFrame(jalview.datamodel.SequenceI)
   */
  public AlignedCodonFrame[] getCodonFrame(SequenceI seq)
  {
    if (seq == null || codonFrameList == null)
      return null;
    Vector cframes = new Vector();
    for (int f = 0; f < codonFrameList.length; f++)
    {
      if (codonFrameList[f].involvesSequence(seq))
        cframes.addElement(codonFrameList[f]);
    }
    if (cframes.size() == 0)
      return null;
    AlignedCodonFrame[] cfr = new AlignedCodonFrame[cframes.size()];
    cframes.copyInto(cfr);
    return cfr;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#getCodonFrames()
   */
  public AlignedCodonFrame[] getCodonFrames()
  {
    return codonFrameList;
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#removeCodonFrame(jalview.datamodel.
   * AlignedCodonFrame)
   */
  public boolean removeCodonFrame(AlignedCodonFrame codons)
  {
    if (codons == null || codonFrameList == null)
      return false;
    boolean removed = false;
    int i = 0, iSize = codonFrameList.length;
    while (i < iSize)
    {
      if (codonFrameList[i] == codons)
      {
        removed = true;
        if (i + 1 < iSize)
        {
          System.arraycopy(codonFrameList, i + 1, codonFrameList, i, iSize
                  - i - 1);
        }
        iSize--;
      }
      else
      {
        i++;
      }
    }
    return removed;
  }

  public void append(AlignmentI toappend)
  {
    // TODO test this method for a future 2.5 release
    // currently tested for use in jalview.gui.SequenceFetcher
    boolean samegap = toappend.getGapCharacter() == getGapCharacter();
    char oldc = toappend.getGapCharacter();
    boolean hashidden = toappend.getHiddenSequences() != null
            && toappend.getHiddenSequences().hiddenSequences != null;
    // get all sequences including any hidden ones
    Vector sqs = (hashidden) ? toappend.getHiddenSequences()
            .getFullAlignment().getSequences() : toappend.getSequences();
    if (sqs != null)
    {
      Enumeration sq = sqs.elements();
      while (sq.hasMoreElements())
      {
        SequenceI addedsq = (SequenceI) sq.nextElement();
        if (!samegap)
        {
          char[] oldseq = addedsq.getSequence();
          for (int c = 0; c < oldseq.length; c++)
          {
            if (oldseq[c] == oldc)
            {
              oldseq[c] = gapCharacter;
            }
          }
        }
        addSequence(addedsq);
      }
    }
    AlignmentAnnotation[] alan = toappend.getAlignmentAnnotation();
    for (int a = 0; alan != null && a < alan.length; a++)
    {
      addAnnotation(alan[a]);
    }
    AlignedCodonFrame[] acod = toappend.getCodonFrames();
    for (int a = 0; acod != null && a < acod.length; a++)
    {
      this.addCodonFrame(acod[a]);
    }
    Vector sg = toappend.getGroups();
    if (sg != null)
    {
      Enumeration el = sg.elements();
      while (el.hasMoreElements())
      {
        addGroup((SequenceGroup) el.nextElement());
      }
    }
    if (toappend.getHiddenSequences() != null)
    {
      HiddenSequences hs = toappend.getHiddenSequences();
      if (hiddenSequences == null)
      {
        hiddenSequences = new HiddenSequences(this);
      }
      if (hs.hiddenSequences != null)
      {
        for (int s = 0; s < hs.hiddenSequences.length; s++)
        {
          // hide the newly appended sequence in the alignment
          if (hs.hiddenSequences[s] != null)
          {
            hiddenSequences.hideSequence(hs.hiddenSequences[s]);
          }
        }
      }
    }
    if (toappend.getProperties() != null)
    {
      // we really can't do very much here - just try to concatenate strings
      // where property collisions occur.
      Enumeration key = toappend.getProperties().keys();
      while (key.hasMoreElements())
      {
        Object k = key.nextElement();
        Object ourval = this.getProperty(k);
        Object toapprop = toappend.getProperty(k);
        if (ourval != null)
        {
          if (ourval.getClass().equals(toapprop.getClass())
                  && !ourval.equals(toapprop))
          {
            if (ourval instanceof String)
            {
              // append strings
              this.setProperty(k, ((String) ourval) + "; "
                      + ((String) toapprop));
            }
            else
            {
              if (ourval instanceof Vector)
              {
                // append vectors
                Enumeration theirv = ((Vector) toapprop).elements();
                while (theirv.hasMoreElements())
                {
                  ((Vector) ourval).addElement(theirv);
                }
              }
            }
          }
        }
        else
        {
          // just add new property directly
          setProperty(k, toapprop);
        }

      }
    }
  }

}
