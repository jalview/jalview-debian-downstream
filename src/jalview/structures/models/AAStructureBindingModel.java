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
package jalview.structures.models;

import jalview.api.StructureSelectionManagerProvider;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.structure.AtomSpec;
import jalview.structure.StructureListener;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.Comparison;
import jalview.util.MessageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * A base class to hold common function for protein structure model binding.
 * Initial version created by refactoring JMol and Chimera binding models, but
 * other structure viewers could in principle be accommodated in future.
 * 
 * @author gmcarstairs
 *
 */
public abstract class AAStructureBindingModel extends
        SequenceStructureBindingModel implements StructureListener,
        StructureSelectionManagerProvider
{

  private StructureSelectionManager ssm;

  private PDBEntry[] pdbEntry;

  /*
   * sequences mapped to each pdbentry
   */
  private SequenceI[][] sequence;

  /*
   * array of target chains for sequences - tied to pdbentry and sequence[]
   */
  private String[][] chains;

  /*
   * datasource protocol for access to PDBEntrylatest
   */
  String protocol = null;

  protected boolean colourBySequence = true;

  private boolean nucleotide;

  private boolean finishedInit = false;

  /**
   * Data bean class to simplify parameterisation in superposeStructures
   */
  protected class SuperposeData
  {
    /**
     * Constructor with alignment width argument
     * 
     * @param width
     */
    public SuperposeData(int width)
    {
      pdbResNo = new int[width];
    }

    public String filename;

    public String pdbId;

    public String chain = "";

    public boolean isRna;

    /*
     * The pdb residue number (if any) mapped to each column of the alignment
     */
    public int[] pdbResNo;
  }

  /**
   * Constructor
   * 
   * @param ssm
   * @param seqs
   */
  public AAStructureBindingModel(StructureSelectionManager ssm,
          SequenceI[][] seqs)
  {
    this.ssm = ssm;
    this.sequence = seqs;
  }

  /**
   * Constructor
   * 
   * @param ssm
   * @param pdbentry
   * @param sequenceIs
   * @param chains
   * @param protocol
   */
  public AAStructureBindingModel(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs, String[][] chains,
          String protocol)
  {
    this.ssm = ssm;
    this.sequence = sequenceIs;
    this.nucleotide = Comparison.isNucleotide(sequenceIs);
    this.chains = chains;
    this.pdbEntry = pdbentry;
    this.protocol = protocol;
    if (chains == null)
    {
      this.chains = new String[pdbentry.length][];
    }
  }

  public StructureSelectionManager getSsm()
  {
    return ssm;
  }

  /**
   * Returns the i'th PDBEntry (or null)
   * 
   * @param i
   * @return
   */
  public PDBEntry getPdbEntry(int i)
  {
    return (pdbEntry != null && pdbEntry.length > i) ? pdbEntry[i] : null;
  }

  /**
   * Answers true if this binding includes the given PDB id, else false
   * 
   * @param pdbId
   * @return
   */
  public boolean hasPdbId(String pdbId)
  {
    if (pdbEntry != null)
    {
      for (PDBEntry pdb : pdbEntry)
      {
        if (pdb.getId().equals(pdbId))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the number of modelled PDB file entries.
   * 
   * @return
   */
  public int getPdbCount()
  {
    return pdbEntry == null ? 0 : pdbEntry.length;
  }

  public SequenceI[][] getSequence()
  {
    return sequence;
  }

  public String[][] getChains()
  {
    return chains;
  }

  public String getProtocol()
  {
    return protocol;
  }

  // TODO may remove this if calling methods can be pulled up here
  protected void setPdbentry(PDBEntry[] pdbentry)
  {
    this.pdbEntry = pdbentry;
  }

  protected void setSequence(SequenceI[][] sequence)
  {
    this.sequence = sequence;
  }

  protected void setChains(String[][] chains)
  {
    this.chains = chains;
  }

  /**
   * Construct a title string for the viewer window based on the data Jalview
   * knows about
   * 
   * @param viewerName
   *          TODO
   * @param verbose
   * 
   * @return
   */
  public String getViewerTitle(String viewerName, boolean verbose)
  {
    if (getSequence() == null || getSequence().length < 1
            || getPdbCount() < 1 || getSequence()[0].length < 1)
    {
      return ("Jalview " + viewerName + " Window");
    }
    // TODO: give a more informative title when multiple structures are
    // displayed.
    StringBuilder title = new StringBuilder(64);
    final PDBEntry pdbEntry = getPdbEntry(0);
    title.append(viewerName + " view for " + getSequence()[0][0].getName()
            + ":" + pdbEntry.getId());

    if (verbose)
    {
      if (pdbEntry.getProperty() != null)
      {
        if (pdbEntry.getProperty().get("method") != null)
        {
          title.append(" Method: ");
          title.append(pdbEntry.getProperty().get("method"));
        }
        if (pdbEntry.getProperty().get("chains") != null)
        {
          title.append(" Chain:");
          title.append(pdbEntry.getProperty().get("chains"));
        }
      }
    }
    return title.toString();
  }

  /**
   * Called by after closeViewer is called, to release any resources and
   * references so they can be garbage collected. Override if needed.
   */
  protected void releaseUIResources()
  {

  }

  public boolean isColourBySequence()
  {
    return colourBySequence;
  }

  public void setColourBySequence(boolean colourBySequence)
  {
    this.colourBySequence = colourBySequence;
  }

  protected void addSequenceAndChain(int pe, SequenceI[] seq,
          String[] tchain)
  {
    if (pe < 0 || pe >= getPdbCount())
    {
      throw new Error(MessageManager.formatMessage(
              "error.implementation_error_no_pdbentry_from_index",
              new Object[] { Integer.valueOf(pe).toString() }));
    }
    final String nullChain = "TheNullChain";
    List<SequenceI> s = new ArrayList<SequenceI>();
    List<String> c = new ArrayList<String>();
    if (getChains() == null)
    {
      setChains(new String[getPdbCount()][]);
    }
    if (getSequence()[pe] != null)
    {
      for (int i = 0; i < getSequence()[pe].length; i++)
      {
        s.add(getSequence()[pe][i]);
        if (getChains()[pe] != null)
        {
          if (i < getChains()[pe].length)
          {
            c.add(getChains()[pe][i]);
          }
          else
          {
            c.add(nullChain);
          }
        }
        else
        {
          if (tchain != null && tchain.length > 0)
          {
            c.add(nullChain);
          }
        }
      }
    }
    for (int i = 0; i < seq.length; i++)
    {
      if (!s.contains(seq[i]))
      {
        s.add(seq[i]);
        if (tchain != null && i < tchain.length)
        {
          c.add(tchain[i] == null ? nullChain : tchain[i]);
        }
      }
    }
    SequenceI[] tmp = s.toArray(new SequenceI[s.size()]);
    getSequence()[pe] = tmp;
    if (c.size() > 0)
    {
      String[] tch = c.toArray(new String[c.size()]);
      for (int i = 0; i < tch.length; i++)
      {
        if (tch[i] == nullChain)
        {
          tch[i] = null;
        }
      }
      getChains()[pe] = tch;
    }
    else
    {
      getChains()[pe] = null;
    }
  }

  /**
   * add structures and any known sequence associations
   * 
   * @returns the pdb entries added to the current set.
   */
  public synchronized PDBEntry[] addSequenceAndChain(PDBEntry[] pdbe,
          SequenceI[][] seq, String[][] chns)
  {
    List<PDBEntry> v = new ArrayList<PDBEntry>();
    List<int[]> rtn = new ArrayList<int[]>();
    for (int i = 0; i < getPdbCount(); i++)
    {
      v.add(getPdbEntry(i));
    }
    for (int i = 0; i < pdbe.length; i++)
    {
      int r = v.indexOf(pdbe[i]);
      if (r == -1 || r >= getPdbCount())
      {
        rtn.add(new int[] { v.size(), i });
        v.add(pdbe[i]);
      }
      else
      {
        // just make sure the sequence/chain entries are all up to date
        addSequenceAndChain(r, seq[i], chns[i]);
      }
    }
    pdbe = v.toArray(new PDBEntry[v.size()]);
    setPdbentry(pdbe);
    if (rtn.size() > 0)
    {
      // expand the tied sequence[] and string[] arrays
      SequenceI[][] sqs = new SequenceI[getPdbCount()][];
      String[][] sch = new String[getPdbCount()][];
      System.arraycopy(getSequence(), 0, sqs, 0, getSequence().length);
      System.arraycopy(getChains(), 0, sch, 0, this.getChains().length);
      setSequence(sqs);
      setChains(sch);
      pdbe = new PDBEntry[rtn.size()];
      for (int r = 0; r < pdbe.length; r++)
      {
        int[] stri = (rtn.get(r));
        // record the pdb file as a new addition
        pdbe[r] = getPdbEntry(stri[0]);
        // and add the new sequence/chain entries
        addSequenceAndChain(stri[0], seq[stri[1]], chns[stri[1]]);
      }
    }
    else
    {
      pdbe = null;
    }
    return pdbe;
  }

  /**
   * Add sequences to the pe'th pdbentry's sequence set.
   * 
   * @param pe
   * @param seq
   */
  public void addSequence(int pe, SequenceI[] seq)
  {
    addSequenceAndChain(pe, seq, null);
  }

  /**
   * add the given sequences to the mapping scope for the given pdb file handle
   * 
   * @param pdbFile
   *          - pdbFile identifier
   * @param seq
   *          - set of sequences it can be mapped to
   */
  public void addSequenceForStructFile(String pdbFile, SequenceI[] seq)
  {
    for (int pe = 0; pe < getPdbCount(); pe++)
    {
      if (getPdbEntry(pe).getFile().equals(pdbFile))
      {
        addSequence(pe, seq);
      }
    }
  }

  @Override
  public abstract void highlightAtoms(List<AtomSpec> atoms);

  protected boolean isNucleotide()
  {
    return this.nucleotide;
  }

  /**
   * Returns a readable description of all mappings for the wrapped pdbfile to
   * any mapped sequences
   * 
   * @param pdbfile
   * @param seqs
   * @return
   */
  public String printMappings()
  {
    if (pdbEntry == null)
    {
      return "";
    }
    StringBuilder sb = new StringBuilder(128);
    for (int pdbe = 0; pdbe < getPdbCount(); pdbe++)
    {
      String pdbfile = getPdbEntry(pdbe).getFile();
      List<SequenceI> seqs = Arrays.asList(getSequence()[pdbe]);
      sb.append(getSsm().printMappings(pdbfile, seqs));
    }
    return sb.toString();
  }

  /**
   * Returns the mapped structure position for a given aligned column of a given
   * sequence, or -1 if the column is gapped, beyond the end of the sequence, or
   * not mapped to structure.
   * 
   * @param seq
   * @param alignedPos
   * @param mapping
   * @return
   */
  protected int getMappedPosition(SequenceI seq, int alignedPos,
          StructureMapping mapping)
  {
    if (alignedPos >= seq.getLength())
    {
      return -1;
    }

    if (Comparison.isGap(seq.getCharAt(alignedPos)))
    {
      return -1;
    }
    int seqPos = seq.findPosition(alignedPos);
    int pos = mapping.getPDBResNum(seqPos);
    return pos;
  }

  /**
   * Helper method to identify residues that can participate in a structure
   * superposition command. For each structure, identify a sequence in the
   * alignment which is mapped to the structure. Identify non-gapped columns in
   * the sequence which have a mapping to a residue in the structure. Returns
   * the index of the first structure that has a mapping to the alignment.
   * 
   * @param alignment
   *          the sequence alignment which is the basis of structure
   *          superposition
   * @param matched
   *          an array of booleans, indexed by alignment column, where true
   *          indicates that every structure has a mapped residue present in the
   *          column (so the column can participate in structure alignment)
   * @param structures
   *          an array of data beans corresponding to pdb file index
   * @return
   */
  protected int findSuperposableResidues(AlignmentI alignment,
          boolean[] matched, SuperposeData[] structures)
  {
    int refStructure = -1;
    String[] files = getPdbFile();
    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      StructureMapping[] mappings = getSsm().getMapping(files[pdbfnum]);
      int lastPos = -1;

      /*
       * Find the first mapped sequence (if any) for this PDB entry which is in
       * the alignment
       */
      final int seqCountForPdbFile = getSequence()[pdbfnum].length;
      for (int s = 0; s < seqCountForPdbFile; s++)
      {
        for (StructureMapping mapping : mappings)
        {
          final SequenceI theSequence = getSequence()[pdbfnum][s];
          if (mapping.getSequence() == theSequence
                  && alignment.findIndex(theSequence) > -1)
          {
            if (refStructure < 0)
            {
              refStructure = pdbfnum;
            }
            for (int r = 0; r < matched.length; r++)
            {
              if (!matched[r])
              {
                continue;
              }
              int pos = getMappedPosition(theSequence, r, mapping);
              if (pos < 1 || pos == lastPos)
              {
                matched[r] = false;
                continue;
              }
              lastPos = pos;
              structures[pdbfnum].pdbResNo[r] = pos;
            }
            String chain = mapping.getChain();
            if (chain != null && chain.trim().length() > 0)
            {
              structures[pdbfnum].chain = chain;
            }
            structures[pdbfnum].pdbId = mapping.getPdbId();
            structures[pdbfnum].isRna = theSequence.getRNA() != null;
            // move on to next pdb file
            s = seqCountForPdbFile;
            break;
          }
        }
      }
    }
    return refStructure;
  }

  /**
   * Returns true if the structure viewer has loaded all of the files of
   * interest (identified by the file mapping having been set up), or false if
   * any are still not loaded after a timeout interval.
   * 
   * @param files
   */
  protected boolean waitForFileLoad(String[] files)
  {
    /*
     * give up after 10 secs plus 1 sec per file
     */
    long starttime = System.currentTimeMillis();
    long endTime = 10000 + 1000 * files.length + starttime;
    String notLoaded = null;

    boolean waiting = true;
    while (waiting && System.currentTimeMillis() < endTime)
    {
      waiting = false;
      for (String file : files)
      {
        notLoaded = file;
        try
        {
          StructureMapping[] sm = getSsm().getMapping(file);
          if (sm == null || sm.length == 0)
          {
            waiting = true;
          }
        } catch (Throwable x)
        {
          waiting = true;
        }
      }
    }

    if (waiting)
    {
      System.err
              .println("Timed out waiting for structure viewer to load file "
                      + notLoaded);
      return false;
    }
    return true;
  }

  @Override
  public boolean isListeningFor(SequenceI seq)
  {
    if (sequence != null)
    {
      for (SequenceI[] seqs : sequence)
      {
        if (seqs != null)
        {
          for (SequenceI s : seqs)
          {
            if (s == seq)
            {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public boolean isFinishedInit()
  {
    return finishedInit;
  }

  public void setFinishedInit(boolean fi)
  {
    this.finishedInit = fi;
  }
}
