/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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
package jalview.analysis;

import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class SeqsetUtils
{

  /**
   * Store essential properties of a sequence in a hashtable for later recovery
   * Keys are Name, Start, End, SeqFeatures, PdbId
   * 
   * @param seq
   *          SequenceI
   * @return Hashtable
   */
  public static Hashtable SeqCharacterHash(SequenceI seq)
  {
    Hashtable sqinfo = new Hashtable();
    sqinfo.put("Name", seq.getName());
    sqinfo.put("Start", Integer.valueOf(seq.getStart()));
    sqinfo.put("End", Integer.valueOf(seq.getEnd()));
    if (seq.getDescription() != null)
    {
      sqinfo.put("Description", seq.getDescription());
    }

    Vector<SequenceFeature> sfeat = new Vector<SequenceFeature>();
    List<SequenceFeature> sfs = seq.getFeatures().getAllFeatures();
    sfeat.addAll(sfs);

    if (seq.getDatasetSequence() == null)
    {
      sqinfo.put("SeqFeatures", sfeat);
      sqinfo.put("PdbId",
              (seq.getAllPDBEntries() != null) ? seq.getAllPDBEntries()
                      : new Vector<PDBEntry>());
    }
    else
    {
      sqinfo.put("datasetSequence",
              (seq.getDatasetSequence() != null) ? seq.getDatasetSequence()
                      : new Sequence("THISISAPLACEHOLDER", ""));
    }
    return sqinfo;
  }

  /**
   * Recover essential properties of a sequence from a hashtable TODO: replace
   * these methods with something more elegant.
   * 
   * @param sq
   *          SequenceI
   * @param sqinfo
   *          Hashtable
   * @return boolean true if name was not updated from sqinfo Name entry
   */
  public static boolean SeqCharacterUnhash(SequenceI sq, Hashtable sqinfo)
  {
    return SeqCharacterUnhash(sq, sqinfo, false, false);
  }

  /**
   * restore some characteristics for a sequence from its hash
   * @param sq
   * @param sqinfo
   * @param excludeLimits - when true, start/end is left unmodified
   * @param excludeFeatures - when true, features are not restored from stashed vector
   * @return true if sequence's name was modified
   */
          
  public static boolean SeqCharacterUnhash(SequenceI sq, Hashtable sqinfo, boolean excludeLimits,boolean excludeFeatures)
  {
    boolean namePresent = true;
    if (sqinfo == null)
    {
      return false;
    }
    String oldname = (String) sqinfo.get("Name");
    Integer start = (Integer) sqinfo.get("Start");
    Integer end = (Integer) sqinfo.get("End");
    Vector<SequenceFeature> sfeatures = (Vector<SequenceFeature>) sqinfo
            .get("SeqFeatures");
    Vector<PDBEntry> pdbid = (Vector<PDBEntry>) sqinfo.get("PdbId");
    String description = (String) sqinfo.get("Description");
    Sequence seqds = (Sequence) sqinfo.get("datasetSequence");
    if (oldname == null)
    {
      namePresent = false;
    }
    else
    {
      sq.setName(oldname);
    }
    if (pdbid != null && pdbid.size() > 0)
    {
      sq.setPDBId(pdbid);
    }

    if (!excludeLimits && (start != null) && (end != null))
    {
      sq.setStart(start.intValue());
      sq.setEnd(end.intValue());
    }
    // TODO: drop this completely since we should not manipulate sequenceFeatures as a vector any more
    if (!excludeFeatures && sfeatures != null && !sfeatures.isEmpty())
    {
      sq.setSequenceFeatures(sfeatures);
    }
    if (description != null)
    {
      sq.setDescription(description);
    }
    if ((seqds != null) && !(seqds.getName().equals("THISISAPLACEHOLDER")
            && seqds.getLength() == 0))
    {
      if (sfeatures != null)
      {
        System.err.println(
                "Implementation error: setting dataset sequence for a sequence which has sequence features.\n\tDataset sequence features will not be visible.");
      }
      sq.setDatasetSequence(seqds);
    }

    return namePresent;
  }

  /**
   * Form of the unique name used in uniquify for the i'th sequence in an
   * ordered vector of sequences.
   * 
   * @param i
   *          int
   * @return String
   */
  public static String unique_name(int i)
  {
    return new String("Sequence" + i);
  }

  /**
   * Generates a hash of SeqCharacterHash properties for each sequence in a
   * sequence set, and optionally renames the sequences to an unambiguous 'safe'
   * name.
   * 
   * @param sequences
   *          SequenceI[]
   * @param write_names
   *          boolean set this to rename each of the sequences to its
   *          unique_name(index) name
   * @return Hashtable to be passed to
   * @see deuniquify to recover original names (and properties) for renamed
   *      sequences
   */
  public static Hashtable uniquify(SequenceI[] sequences,
          boolean write_names)
  {
    // Generate a safely named sequence set and a hash to recover the sequence
    // names
    Hashtable map = new Hashtable();
    // String[] un_names = new String[sequences.length];

    for (int i = 0; i < sequences.length; i++)
    {
      String safename = unique_name(i);
      map.put(safename, SeqCharacterHash(sequences[i]));

      if (write_names)
      {
        sequences[i].setName(safename);
      }
    }

    return map;
  }

  /**
   * recover unsafe sequence names and original properties for a sequence set
   * using a map generated by
   * 
   * @see uniquify(sequences,true)
   * @param map
   *          Hashtable
   * @param sequences
   *          SequenceI[]
   * @return boolean
   */
  public static boolean deuniquify(Hashtable map, SequenceI[] sequences)
  {
    return deuniquify(map, sequences, true);
  }

  /**
   * recover unsafe sequence names and original properties for a sequence set
   * using a map generated by
   * 
   * @see uniquify(sequences,true)
   * @param map
   *          Hashtable
   * @param sequences
   *          SequenceI[]
   * @param quiet
   *          when false, don't complain about sequences without any data in the
   *          map.
   * @return boolean
   */
  public static boolean deuniquify(Hashtable map, SequenceI[] sequences,
          boolean quiet)
  {
    jalview.analysis.SequenceIdMatcher matcher = new SequenceIdMatcher(
            sequences);
    SequenceI msq = null;
    Enumeration keys = map.keys();
    Vector unmatched = new Vector();
    for (int i = 0, j = sequences.length; i < j; i++)
    {
      unmatched.addElement(sequences[i]);
    }
    while (keys.hasMoreElements())
    {
      Object key = keys.nextElement();
      if (key instanceof String)
      {
        if ((msq = matcher.findIdMatch((String) key)) != null)
        {
          Hashtable sqinfo = (Hashtable) map.get(key);
          unmatched.removeElement(msq);
          SeqCharacterUnhash(msq, sqinfo);
        }
        else
        {
          if (!quiet)
          {
            System.err.println("Can't find '" + ((String) key)
                    + "' in uniquified alignment");
          }
        }
      }
    }
    if (unmatched.size() > 0 && !quiet)
    {
      System.err.println("Did not find matches for :");
      for (Enumeration i = unmatched.elements(); i
              .hasMoreElements(); System.out
                      .println(((SequenceI) i.nextElement()).getName()))
      {
        ;
      }
      return false;
    }

    return true;
  }

  /**
   * returns a subset of the sequenceI seuqences, including only those that
   * contain at least one residue.
   * 
   * @param sequences
   *          SequenceI[]
   * @return SequenceI[]
   */
  public static SequenceI[] getNonEmptySequenceSet(SequenceI[] sequences)
  {
    // Identify first row of alignment with residues for prediction
    boolean ungapped[] = new boolean[sequences.length];
    int msflen = 0;
    for (int i = 0, j = sequences.length; i < j; i++)
    {
      String tempseq = jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars,
              sequences[i].getSequenceAsString());

      if (tempseq.length() == 0)
      {
        ungapped[i] = false;
      }
      else
      {
        ungapped[i] = true;
        msflen++;
      }
    }
    if (msflen == 0)
    {
      return null; // no minimal set
    }
    // compose minimal set
    SequenceI[] mset = new SequenceI[msflen];
    for (int i = 0, j = sequences.length, k = 0; i < j; i++)
    {
      if (ungapped[i])
      {
        mset[k++] = sequences[i];
      }
    }
    ungapped = null;
    return mset;
  }
}
