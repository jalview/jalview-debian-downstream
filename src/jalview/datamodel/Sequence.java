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
package jalview.datamodel;

import jalview.analysis.AlignSeq;
import jalview.util.StringUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import fr.orsay.lri.varna.models.rna.RNA;

/**
 * 
 * Implements the SequenceI interface for a char[] based sequence object.
 * 
 * @author $author$
 * @version $Revision$
 */
public class Sequence extends ASequence implements SequenceI
{
  SequenceI datasetSequence;

  String name;

  private char[] sequence;

  String description;

  int start;

  int end;

  Vector<PDBEntry> pdbIds;

  String vamsasId;

  DBRefEntry[] dbrefs;

  RNA rna;

  /**
   * This annotation is displayed below the alignment but the positions are tied
   * to the residues of this sequence
   *
   * TODO: change to List<>
   */
  Vector<AlignmentAnnotation> annotation;

  /**
   * The index of the sequence in a MSA
   */
  int index = -1;

  /** array of sequence features - may not be null for a valid sequence object */
  public SequenceFeature[] sequenceFeatures;

  /**
   * Creates a new Sequence object.
   * 
   * @param name
   *          display name string
   * @param sequence
   *          string to form a possibly gapped sequence out of
   * @param start
   *          first position of non-gap residue in the sequence
   * @param end
   *          last position of ungapped residues (nearly always only used for
   *          display purposes)
   */
  public Sequence(String name, String sequence, int start, int end)
  {
    initSeqAndName(name, sequence.toCharArray(), start, end);
  }

  public Sequence(String name, char[] sequence, int start, int end)
  {
    initSeqAndName(name, sequence, start, end);
  }

  /**
   * Stage 1 constructor - assign name, sequence, and set start and end fields.
   * start and end are updated values from name2 if it ends with /start-end
   * 
   * @param name2
   * @param sequence2
   * @param start2
   * @param end2
   */
  protected void initSeqAndName(String name2, char[] sequence2, int start2,
          int end2)
  {
    this.name = name2;
    this.sequence = sequence2;
    this.start = start2;
    this.end = end2;
    parseId();
    checkValidRange();
  }

  com.stevesoft.pat.Regex limitrx = new com.stevesoft.pat.Regex(
          "[/][0-9]{1,}[-][0-9]{1,}$");

  com.stevesoft.pat.Regex endrx = new com.stevesoft.pat.Regex("[0-9]{1,}$");

  void parseId()
  {
    if (name == null)
    {
      System.err
              .println("POSSIBLE IMPLEMENTATION ERROR: null sequence name passed to constructor.");
      name = "";
    }
    // Does sequence have the /start-end signature?
    if (limitrx.search(name))
    {
      name = limitrx.left();
      endrx.search(limitrx.stringMatched());
      setStart(Integer.parseInt(limitrx.stringMatched().substring(1,
              endrx.matchedFrom() - 1)));
      setEnd(Integer.parseInt(endrx.stringMatched()));
    }
  }

  void checkValidRange()
  {
    // Note: JAL-774 :
    // http://issues.jalview.org/browse/JAL-774?focusedCommentId=11239&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-11239
    {
      int endRes = 0;
      for (int j = 0; j < sequence.length; j++)
      {
        if (!jalview.util.Comparison.isGap(sequence[j]))
        {
          endRes++;
        }
      }
      if (endRes > 0)
      {
        endRes += start - 1;
      }

      if (end < endRes)
      {
        end = endRes;
      }
    }

  }

  /**
   * Creates a new Sequence object.
   * 
   * @param name
   *          DOCUMENT ME!
   * @param sequence
   *          DOCUMENT ME!
   */
  public Sequence(String name, String sequence)
  {
    this(name, sequence, 1, -1);
  }

  /**
   * Creates a new Sequence object with new features, DBRefEntries,
   * AlignmentAnnotations, and PDBIds but inherits any existing dataset sequence
   * reference.
   * 
   * @param seq
   *          DOCUMENT ME!
   */
  public Sequence(SequenceI seq)
  {
    this(seq, seq.getAnnotation());
  }

  /**
   * Create a new sequence object with new features, DBRefEntries, and PDBIds
   * but inherits any existing dataset sequence reference, and duplicate of any
   * annotation that is present in the given annotation array.
   * 
   * @param seq
   *          the sequence to be copied
   * @param alAnnotation
   *          an array of annotation including some associated with seq
   */
  public Sequence(SequenceI seq, AlignmentAnnotation[] alAnnotation)
  {
    initSeqFrom(seq, alAnnotation);

  }

  protected void initSeqFrom(SequenceI seq,
          AlignmentAnnotation[] alAnnotation)
  {
    initSeqAndName(seq.getName(), seq.getSequence(), seq.getStart(),
            seq.getEnd());
    description = seq.getDescription();
    if (seq.getSequenceFeatures() != null)
    {
      SequenceFeature[] sf = seq.getSequenceFeatures();
      for (int i = 0; i < sf.length; i++)
      {
        addSequenceFeature(new SequenceFeature(sf[i]));
      }
    }
    setDatasetSequence(seq.getDatasetSequence());
    if (datasetSequence == null && seq.getDBRef() != null)
    {
      // only copy DBRefs if we really are a dataset sequence
      DBRefEntry[] dbr = seq.getDBRef();
      for (int i = 0; i < dbr.length; i++)
      {
        addDBRef(new DBRefEntry(dbr[i]));
      }
    }
    if (seq.getAnnotation() != null)
    {
      AlignmentAnnotation[] sqann = seq.getAnnotation();
      for (int i = 0; i < sqann.length; i++)
      {
        if (sqann[i] == null)
        {
          continue;
        }
        boolean found = (alAnnotation == null);
        if (!found)
        {
          for (int apos = 0; !found && apos < alAnnotation.length; apos++)
          {
            found = (alAnnotation[apos] == sqann[i]);
          }
        }
        if (found)
        {
          // only copy the given annotation
          AlignmentAnnotation newann = new AlignmentAnnotation(sqann[i]);
          addAlignmentAnnotation(newann);
        }
      }
    }
    if (seq.getAllPDBEntries() != null)
    {
      Vector ids = seq.getAllPDBEntries();
      Enumeration e = ids.elements();
      while (e.hasMoreElements())
      {
        this.addPDBId(new PDBEntry((PDBEntry) e.nextElement()));
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param v
   *          DOCUMENT ME!
   */
  public void setSequenceFeatures(SequenceFeature[] features)
  {
    sequenceFeatures = features;
  }

  public synchronized void addSequenceFeature(SequenceFeature sf)
  {
    if (sequenceFeatures == null)
    {
      sequenceFeatures = new SequenceFeature[0];
    }

    for (int i = 0; i < sequenceFeatures.length; i++)
    {
      if (sequenceFeatures[i].equals(sf))
      {
        return;
      }
    }

    SequenceFeature[] temp = new SequenceFeature[sequenceFeatures.length + 1];
    System.arraycopy(sequenceFeatures, 0, temp, 0, sequenceFeatures.length);
    temp[sequenceFeatures.length] = sf;

    sequenceFeatures = temp;
  }

  public void deleteFeature(SequenceFeature sf)
  {
    if (sequenceFeatures == null)
    {
      return;
    }

    int index = 0;
    for (index = 0; index < sequenceFeatures.length; index++)
    {
      if (sequenceFeatures[index].equals(sf))
      {
        break;
      }
    }

    if (index == sequenceFeatures.length)
    {
      return;
    }

    int sfLength = sequenceFeatures.length;
    if (sfLength < 2)
    {
      sequenceFeatures = null;
    }
    else
    {
      SequenceFeature[] temp = new SequenceFeature[sfLength - 1];
      System.arraycopy(sequenceFeatures, 0, temp, 0, index);

      if (index < sfLength)
      {
        System.arraycopy(sequenceFeatures, index + 1, temp, index,
                sequenceFeatures.length - index - 1);
      }

      sequenceFeatures = temp;
    }
  }

  /**
   * Returns the sequence features (if any), looking first on the sequence, then
   * on its dataset sequence, and so on until a non-null value is found (or
   * none). This supports retrieval of sequence features stored on the sequence
   * (as in the applet) or on the dataset sequence (as in the Desktop version).
   * 
   * @return
   */
  public SequenceFeature[] getSequenceFeatures()
  {
    SequenceFeature[] features = sequenceFeatures;

    SequenceI seq = this;
    int count = 0; // failsafe against loop in sequence.datasetsequence...
    while (features == null && seq.getDatasetSequence() != null
            && count++ < 10)
    {
      seq = seq.getDatasetSequence();
      features = ((Sequence) seq).sequenceFeatures;
    }
    return features;
  }

  public void addPDBId(PDBEntry entry)
  {
    if (pdbIds == null)
    {
      pdbIds = new Vector<PDBEntry>();
    }
    if (pdbIds.contains(entry))
    {
      updatePDBEntry(pdbIds.get(pdbIds.indexOf(entry)), entry);
    }
    else
    {
      pdbIds.addElement(entry);
    }
  }

  private static void updatePDBEntry(PDBEntry oldEntry, PDBEntry newEntry)
  {
    if (newEntry.getFile() != null)
    {
      oldEntry.setFile(newEntry.getFile());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param id
   *          DOCUMENT ME!
   */
  @Override
  public void setPDBId(Vector<PDBEntry> id)
  {
    pdbIds = id;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public Vector<PDBEntry> getAllPDBEntries()
  {
    return pdbIds;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getDisplayId(boolean jvsuffix)
  {
    StringBuffer result = new StringBuffer(name);
    if (jvsuffix)
    {
      result.append("/" + start + "-" + end);
    }

    return result.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param name
   *          DOCUMENT ME!
   */
  public void setName(String name)
  {
    this.name = name;
    this.parseId();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param start
   *          DOCUMENT ME!
   */
  public void setStart(int start)
  {
    this.start = start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getStart()
  {
    return this.start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param end
   *          DOCUMENT ME!
   */
  public void setEnd(int end)
  {
    this.end = end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getEnd()
  {
    return this.end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getLength()
  {
    return this.sequence.length;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   */
  public void setSequence(String seq)
  {
    this.sequence = seq.toCharArray();
    checkValidRange();
  }

  public String getSequenceAsString()
  {
    return new String(sequence);
  }

  public String getSequenceAsString(int start, int end)
  {
    return new String(getSequence(start, end));
  }

  public char[] getSequence()
  {
    return sequence;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.SequenceI#getSequence(int, int)
   */
  public char[] getSequence(int start, int end)
  {
    if (start < 0)
    {
      start = 0;
    }
    // JBPNote - left to user to pad the result here (TODO:Decide on this
    // policy)
    if (start >= sequence.length)
    {
      return new char[0];
    }

    if (end >= sequence.length)
    {
      end = sequence.length;
    }

    char[] reply = new char[end - start];
    System.arraycopy(sequence, start, reply, 0, end - start);

    return reply;
  }

  @Override
  public SequenceI getSubSequence(int start, int end)
  {
    if (start < 0)
    {
      start = 0;
    }
    char[] seq = getSequence(start, end);
    if (seq.length == 0)
    {
      return null;
    }
    int nstart = findPosition(start);
    int nend = findPosition(end) - 1;
    // JBPNote - this is an incomplete copy.
    SequenceI nseq = new Sequence(this.getName(), seq, nstart, nend);
    nseq.setDescription(description);
    if (datasetSequence != null)
    {
      nseq.setDatasetSequence(datasetSequence);
    }
    else
    {
      nseq.setDatasetSequence(this);
    }
    return nseq;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public char getCharAt(int i)
  {
    if (i < sequence.length)
    {
      return sequence[i];
    }
    else
    {
      return ' ';
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param desc
   *          DOCUMENT ME!
   */
  public void setDescription(String desc)
  {
    this.description = desc;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getDescription()
  {
    return this.description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.SequenceI#findIndex(int)
   */
  public int findIndex(int pos)
  {
    // returns the alignment position for a residue
    int j = start;
    int i = 0;
    // Rely on end being at least as long as the length of the sequence.
    while ((i < sequence.length) && (j <= end) && (j <= pos))
    {
      if (!jalview.util.Comparison.isGap(sequence[i]))
      {
        j++;
      }

      i++;
    }

    if ((j == end) && (j < pos))
    {
      return end + 1;
    }
    else
    {
      return i;
    }
  }

  @Override
  public int findPosition(int i)
  {
    int j = 0;
    int pos = start;
    int seqlen = sequence.length;
    while ((j < i) && (j < seqlen))
    {
      if (!jalview.util.Comparison.isGap(sequence[j]))
      {
        pos++;
      }

      j++;
    }

    return pos;
  }

  /**
   * Returns an int array where indices correspond to each residue in the
   * sequence and the element value gives its position in the alignment
   * 
   * @return int[SequenceI.getEnd()-SequenceI.getStart()+1] or null if no
   *         residues in SequenceI object
   */
  public int[] gapMap()
  {
    String seq = jalview.analysis.AlignSeq.extractGaps(
            jalview.util.Comparison.GapChars, new String(sequence));
    int[] map = new int[seq.length()];
    int j = 0;
    int p = 0;

    while (j < sequence.length)
    {
      if (!jalview.util.Comparison.isGap(sequence[j]))
      {
        map[p++] = j;
      }

      j++;
    }

    return map;
  }

  @Override
  public int[] findPositionMap()
  {
    int map[] = new int[sequence.length];
    int j = 0;
    int pos = start;
    int seqlen = sequence.length;
    while ((j < seqlen))
    {
      map[j] = pos;
      if (!jalview.util.Comparison.isGap(sequence[j]))
      {
        pos++;
      }

      j++;
    }
    return map;
  }

  @Override
  public List<int[]> getInsertions()
  {
    ArrayList<int[]> map = new ArrayList<int[]>();
    int lastj = -1, j = 0;
    int pos = start;
    int seqlen = sequence.length;
    while ((j < seqlen))
    {
      if (jalview.util.Comparison.isGap(sequence[j]))
      {
        if (lastj == -1)
        {
          lastj = j;
        }
      }
      else
      {
        if (lastj != -1)
        {
          map.add(new int[] { lastj, j - 1 });
          lastj = -1;
        }
      }
      j++;
    }
    if (lastj != -1)
    {
      map.add(new int[] { lastj, j - 1 });
      lastj = -1;
    }
    return map;
  }

  @Override
  public void deleteChars(int i, int j)
  {
    int newstart = start, newend = end;
    if (i >= sequence.length || i < 0)
    {
      return;
    }

    char[] tmp = StringUtils.deleteChars(sequence, i, j);
    boolean createNewDs = false;
    // TODO: take a (second look) at the dataset creation validation method for
    // the very large sequence case
    int eindex = -1, sindex = -1;
    boolean ecalc = false, scalc = false;
    for (int s = i; s < j; s++)
    {
      if (jalview.schemes.ResidueProperties.aaIndex[sequence[s]] != 23)
      {
        if (createNewDs)
        {
          newend--;
        }
        else
        {
          if (!scalc)
          {
            sindex = findIndex(start) - 1;
            scalc = true;
          }
          if (sindex == s)
          {
            // delete characters including start of sequence
            newstart = findPosition(j);
            break; // don't need to search for any more residue characters.
          }
          else
          {
            // delete characters after start.
            if (!ecalc)
            {
              eindex = findIndex(end) - 1;
              ecalc = true;
            }
            if (eindex < j)
            {
              // delete characters at end of sequence
              newend = findPosition(i - 1);
              break; // don't need to search for any more residue characters.
            }
            else
            {
              createNewDs = true;
              newend--; // decrease end position by one for the deleted residue
              // and search further
            }
          }
        }
      }
    }
    // deletion occured in the middle of the sequence
    if (createNewDs && this.datasetSequence != null)
    {
      // construct a new sequence
      Sequence ds = new Sequence(datasetSequence);
      // TODO: remove any non-inheritable properties ?
      // TODO: create a sequence mapping (since there is a relation here ?)
      ds.deleteChars(i, j);
      datasetSequence = ds;
    }
    start = newstart;
    end = newend;
    sequence = tmp;
  }

  @Override
  public void insertCharAt(int i, int length, char c)
  {
    char[] tmp = new char[sequence.length + length];

    if (i >= sequence.length)
    {
      System.arraycopy(sequence, 0, tmp, 0, sequence.length);
      i = sequence.length;
    }
    else
    {
      System.arraycopy(sequence, 0, tmp, 0, i);
    }

    int index = i;
    while (length > 0)
    {
      tmp[index++] = c;
      length--;
    }

    if (i < sequence.length)
    {
      System.arraycopy(sequence, i, tmp, index, sequence.length - i);
    }

    sequence = tmp;
  }

  @Override
  public void insertCharAt(int i, char c)
  {
    insertCharAt(i, 1, c);
  }

  @Override
  public String getVamsasId()
  {
    return vamsasId;
  }

  @Override
  public void setVamsasId(String id)
  {
    vamsasId = id;
  }

  @Override
  public void setDBRef(DBRefEntry[] dbref)
  {
    dbrefs = dbref;
  }

  @Override
  public DBRefEntry[] getDBRef()
  {
    if (dbrefs == null && datasetSequence != null
            && this != datasetSequence)
    {
      return datasetSequence.getDBRef();
    }
    return dbrefs;
  }

  @Override
  public void addDBRef(DBRefEntry entry)
  {
    if (dbrefs == null)
    {
      dbrefs = new DBRefEntry[0];
    }

    int i, iSize = dbrefs.length;

    for (i = 0; i < iSize; i++)
    {
      if (dbrefs[i].equalRef(entry))
      {
        if (entry.getMap() != null)
        {
          if (dbrefs[i].getMap() == null)
          {
            // overwrite with 'superior' entry that contains a mapping.
            dbrefs[i] = entry;
          }
        }
        return;
      }
    }

    DBRefEntry[] temp = new DBRefEntry[iSize + 1];
    System.arraycopy(dbrefs, 0, temp, 0, iSize);
    temp[temp.length - 1] = entry;

    dbrefs = temp;
  }

  @Override
  public void setDatasetSequence(SequenceI seq)
  {
    datasetSequence = seq;
  }

  @Override
  public SequenceI getDatasetSequence()
  {
    return datasetSequence;
  }

  @Override
  public AlignmentAnnotation[] getAnnotation()
  {
    return annotation == null ? null : annotation
            .toArray(new AlignmentAnnotation[annotation.size()]);
  }

  @Override
  public boolean hasAnnotation(AlignmentAnnotation ann)
  {
    return annotation == null ? false : annotation.contains(ann);
  }

  @Override
  public void addAlignmentAnnotation(AlignmentAnnotation annotation)
  {
    if (this.annotation == null)
    {
      this.annotation = new Vector<AlignmentAnnotation>();
    }
    if (!this.annotation.contains(annotation))
    {
      this.annotation.addElement(annotation);
    }
    annotation.setSequenceRef(this);
  }

  public void removeAlignmentAnnotation(AlignmentAnnotation annotation)
  {
    if (this.annotation != null)
    {
      this.annotation.removeElement(annotation);
      if (this.annotation.size() == 0)
      {
        this.annotation = null;
      }
    }
  }

  /**
   * test if this is a valid candidate for another sequence's dataset sequence.
   * 
   */
  private boolean isValidDatasetSequence()
  {
    if (datasetSequence != null)
    {
      return false;
    }
    for (int i = 0; i < sequence.length; i++)
    {
      if (jalview.util.Comparison.isGap(sequence[i]))
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public SequenceI deriveSequence()
  {
    SequenceI seq = new Sequence(this);
    if (datasetSequence != null)
    {
      // duplicate current sequence with same dataset
      seq.setDatasetSequence(datasetSequence);
    }
    else
    {
      if (isValidDatasetSequence())
      {
        // Use this as dataset sequence
        seq.setDatasetSequence(this);
      }
      else
      {
        // Create a new, valid dataset sequence
        SequenceI ds = seq;
        ds.setSequence(AlignSeq.extractGaps(
                jalview.util.Comparison.GapChars, new String(sequence)));
        setDatasetSequence(ds);
        ds.setSequenceFeatures(getSequenceFeatures());
        seq = this; // and return this sequence as the derived sequence.
      }
    }
    return seq;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.SequenceI#createDatasetSequence()
   */
  public SequenceI createDatasetSequence()
  {
    if (datasetSequence == null)
    {
      datasetSequence = new Sequence(getName(), AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, getSequenceAsString()),
              getStart(), getEnd());
      datasetSequence.setSequenceFeatures(getSequenceFeatures());
      datasetSequence.setDescription(getDescription());
      setSequenceFeatures(null);
      // move database references onto dataset sequence
      datasetSequence.setDBRef(getDBRef());
      setDBRef(null);
      datasetSequence.setPDBId(getAllPDBEntries());
      setPDBId(null);
      datasetSequence.updatePDBIds();
      if (annotation != null)
      {
        for (AlignmentAnnotation aa : annotation)
        {
          AlignmentAnnotation _aa = new AlignmentAnnotation(aa);
          _aa.sequenceRef = datasetSequence;
          _aa.adjustForAlignment(); // uses annotation's own record of
                                    // sequence-column mapping
          datasetSequence.addAlignmentAnnotation(_aa);
        }
      }
    }
    return datasetSequence;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.SequenceI#setAlignmentAnnotation(AlignmmentAnnotation[]
   * annotations)
   */
  public void setAlignmentAnnotation(AlignmentAnnotation[] annotations)
  {
    if (annotation != null)
    {
      annotation.removeAllElements();
    }
    if (annotations != null)
    {
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] != null)
        {
          addAlignmentAnnotation(annotations[i]);
        }
      }
    }
  }

  @Override
  public AlignmentAnnotation[] getAnnotation(String label)
  {
    if (annotation == null || annotation.size() == 0)
    {
      return null;
    }

    Vector subset = new Vector();
    Enumeration e = annotation.elements();
    while (e.hasMoreElements())
    {
      AlignmentAnnotation ann = (AlignmentAnnotation) e.nextElement();
      if (ann.label != null && ann.label.equals(label))
      {
        subset.addElement(ann);
      }
    }
    if (subset.size() == 0)
    {
      return null;
    }
    AlignmentAnnotation[] anns = new AlignmentAnnotation[subset.size()];
    int i = 0;
    e = subset.elements();
    while (e.hasMoreElements())
    {
      anns[i++] = (AlignmentAnnotation) e.nextElement();
    }
    subset.removeAllElements();
    return anns;
  }

  @Override
  public boolean updatePDBIds()
  {
    if (datasetSequence != null)
    {
      // TODO: could merge DBRefs
      return datasetSequence.updatePDBIds();
    }
    if (dbrefs == null || dbrefs.length == 0)
    {
      return false;
    }
    Vector newpdb = new Vector();
    for (int i = 0; i < dbrefs.length; i++)
    {
      if (DBRefSource.PDB.equals(dbrefs[i].getSource()))
      {
        PDBEntry pdbe = new PDBEntry();
        pdbe.setId(dbrefs[i].getAccessionId());
        if (pdbIds == null || pdbIds.size() == 0)
        {
          newpdb.addElement(pdbe);
        }
        else
        {
          Enumeration en = pdbIds.elements();
          boolean matched = false;
          while (!matched && en.hasMoreElements())
          {
            PDBEntry anentry = (PDBEntry) en.nextElement();
            if (anentry.getId().equals(pdbe.getId()))
            {
              matched = true;
            }
          }
          if (!matched)
          {
            newpdb.addElement(pdbe);
          }
        }
      }
    }
    if (newpdb.size() > 0)
    {
      Enumeration en = newpdb.elements();
      while (en.hasMoreElements())
      {
        addPDBId((PDBEntry) en.nextElement());
      }
      return true;
    }
    return false;
  }

  @Override
  public void transferAnnotation(SequenceI entry, Mapping mp)
  {
    if (datasetSequence != null)
    {
      datasetSequence.transferAnnotation(entry, mp);
      return;
    }
    if (entry.getDatasetSequence() != null)
    {
      transferAnnotation(entry.getDatasetSequence(), mp);
      return;
    }
    // transfer any new features from entry onto sequence
    if (entry.getSequenceFeatures() != null)
    {

      SequenceFeature[] sfs = entry.getSequenceFeatures();
      for (int si = 0; si < sfs.length; si++)
      {
        SequenceFeature sf[] = (mp != null) ? mp.locateFeature(sfs[si])
                : new SequenceFeature[] { new SequenceFeature(sfs[si]) };
        if (sf != null && sf.length > 0)
        {
          for (int sfi = 0; sfi < sf.length; sfi++)
          {
            addSequenceFeature(sf[sfi]);
          }
        }
      }
    }

    // transfer PDB entries
    if (entry.getAllPDBEntries() != null)
    {
      Enumeration e = entry.getAllPDBEntries().elements();
      while (e.hasMoreElements())
      {
        PDBEntry pdb = (PDBEntry) e.nextElement();
        addPDBId(pdb);
      }
    }
    // transfer database references
    DBRefEntry[] entryRefs = entry.getDBRef();
    if (entryRefs != null)
    {
      for (int r = 0; r < entryRefs.length; r++)
      {
        DBRefEntry newref = new DBRefEntry(entryRefs[r]);
        if (newref.getMap() != null && mp != null)
        {
          // remap ref using our local mapping
        }
        // we also assume all version string setting is done by dbSourceProxy
        /*
         * if (!newref.getSource().equalsIgnoreCase(dbSource)) {
         * newref.setSource(dbSource); }
         */
        addDBRef(newref);
      }
    }
  }

  /**
   * @return The index (zero-based) on this sequence in the MSA. It returns
   *         {@code -1} if this information is not available.
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Defines the position of this sequence in the MSA. Use the value {@code -1}
   * if this information is undefined.
   * 
   * @param The
   *          position for this sequence. This value is zero-based (zero for
   *          this first sequence)
   */
  public void setIndex(int value)
  {
    index = value;
  }

  public void setRNA(RNA r)
  {
    rna = r;
  }

  public RNA getRNA()
  {
    return rna;
  }

  @Override
  public List<AlignmentAnnotation> getAlignmentAnnotations(String calcId,
          String label)
  {
    List<AlignmentAnnotation> result = new ArrayList<AlignmentAnnotation>();
    if (this.annotation != null)
    {
      for (AlignmentAnnotation ann : annotation)
      {
        if (ann.calcId != null && ann.calcId.equals(calcId)
                && ann.label != null && ann.label.equals(label))
        {
          result.add(ann);
        }
      }
    }
    return result;
  }

  public String toString()
  {
    return getDisplayId(false);
  }

  @Override
  public PDBEntry getPDBEntry(String pdbIdStr)
  {
    if (getDatasetSequence() == null
            || getDatasetSequence().getAllPDBEntries() == null)
    {
      return null;
    }
    List<PDBEntry> entries = getDatasetSequence().getAllPDBEntries();
    for (PDBEntry entry : entries)
    {
      if (entry.getId().equalsIgnoreCase(pdbIdStr))
      {
        return entry;
      }
    }
    return null;
  }

}
