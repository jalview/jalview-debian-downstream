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
package jalview.structure;

import jalview.analysis.AlignSeq;
import jalview.api.StructureSelectionManagerProvider;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.OrderCommand;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SequenceI;
import jalview.io.AppletFormatAdapter;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import MCview.Atom;
import MCview.PDBChain;
import MCview.PDBfile;

public class StructureSelectionManager
{
  public final static String NEWLINE = System.lineSeparator();

  static IdentityHashMap<StructureSelectionManagerProvider, StructureSelectionManager> instances;

  private List<StructureMapping> mappings = new ArrayList<StructureMapping>();

  private boolean processSecondaryStructure = false;

  private boolean secStructServices = false;

  private boolean addTempFacAnnot = false;

  /*
   * Set of any registered mappings between (dataset) sequences.
   */
  public Set<AlignedCodonFrame> seqmappings = new LinkedHashSet<AlignedCodonFrame>();

  private List<CommandListener> commandListeners = new ArrayList<CommandListener>();

  private List<SelectionListener> sel_listeners = new ArrayList<SelectionListener>();

  /**
   * @return true if will try to use external services for processing secondary
   *         structure
   */
  public boolean isSecStructServices()
  {
    return secStructServices;
  }

  /**
   * control use of external services for processing secondary structure
   * 
   * @param secStructServices
   */
  public void setSecStructServices(boolean secStructServices)
  {
    this.secStructServices = secStructServices;
  }

  /**
   * flag controlling addition of any kind of structural annotation
   * 
   * @return true if temperature factor annotation will be added
   */
  public boolean isAddTempFacAnnot()
  {
    return addTempFacAnnot;
  }

  /**
   * set flag controlling addition of structural annotation
   * 
   * @param addTempFacAnnot
   */
  public void setAddTempFacAnnot(boolean addTempFacAnnot)
  {
    this.addTempFacAnnot = addTempFacAnnot;
  }

  /**
   * 
   * @return if true, the structure manager will attempt to add secondary
   *         structure lines for unannotated sequences
   */

  public boolean isProcessSecondaryStructure()
  {
    return processSecondaryStructure;
  }

  /**
   * Control whether structure manager will try to annotate mapped sequences
   * with secondary structure from PDB data.
   * 
   * @param enable
   */
  public void setProcessSecondaryStructure(boolean enable)
  {
    processSecondaryStructure = enable;
  }

  /**
   * debug function - write all mappings to stdout
   */
  public void reportMapping()
  {
    if (mappings.isEmpty())
    {
      System.err.println("reportMapping: No PDB/Sequence mappings.");
    }
    else
    {
      System.err.println("reportMapping: There are " + mappings.size()
              + " mappings.");
      int i = 0;
      for (StructureMapping sm : mappings)
      {
        System.err.println("mapping " + i++ + " : " + sm.pdbfile);
      }
    }
  }

  /**
   * map between the PDB IDs (or structure identifiers) used by Jalview and the
   * absolute filenames for PDB data that corresponds to it
   */
  Map<String, String> pdbIdFileName = new HashMap<String, String>();

  Map<String, String> pdbFileNameId = new HashMap<String, String>();

  public void registerPDBFile(String idForFile, String absoluteFile)
  {
    pdbIdFileName.put(idForFile, absoluteFile);
    pdbFileNameId.put(absoluteFile, idForFile);
  }

  public String findIdForPDBFile(String idOrFile)
  {
    String id = pdbFileNameId.get(idOrFile);
    return id;
  }

  public String findFileForPDBId(String idOrFile)
  {
    String id = pdbIdFileName.get(idOrFile);
    return id;
  }

  public boolean isPDBFileRegistered(String idOrFile)
  {
    return pdbFileNameId.containsKey(idOrFile)
            || pdbIdFileName.containsKey(idOrFile);
  }

  private static StructureSelectionManager nullProvider = null;

  public static StructureSelectionManager getStructureSelectionManager(
          StructureSelectionManagerProvider context)
  {
    if (context == null)
    {
      if (nullProvider == null)
      {
        if (instances != null)
        {
          throw new Error(
                  MessageManager
                          .getString("error.implementation_error_structure_selection_manager_null"),
                  new NullPointerException(MessageManager
                          .getString("exception.ssm_context_is_null")));
        }
        else
        {
          nullProvider = new StructureSelectionManager();
        }
        return nullProvider;
      }
    }
    if (instances == null)
    {
      instances = new java.util.IdentityHashMap<StructureSelectionManagerProvider, StructureSelectionManager>();
    }
    StructureSelectionManager instance = instances.get(context);
    if (instance == null)
    {
      if (nullProvider != null)
      {
        instance = nullProvider;
      }
      else
      {
        instance = new StructureSelectionManager();
      }
      instances.put(context, instance);
    }
    return instance;
  }

  /**
   * flag controlling whether SeqMappings are relayed from received sequence
   * mouse over events to other sequences
   */
  boolean relaySeqMappings = true;

  /**
   * Enable or disable relay of seqMapping events to other sequences. You might
   * want to do this if there are many sequence mappings and the host computer
   * is slow
   * 
   * @param relay
   */
  public void setRelaySeqMappings(boolean relay)
  {
    relaySeqMappings = relay;
  }

  /**
   * get the state of the relay seqMappings flag.
   * 
   * @return true if sequence mouse overs are being relayed to other mapped
   *         sequences
   */
  public boolean isRelaySeqMappingsEnabled()
  {
    return relaySeqMappings;
  }

  Vector listeners = new Vector();

  /**
   * register a listener for alignment sequence mouseover events
   * 
   * @param svl
   */
  public void addStructureViewerListener(Object svl)
  {
    if (!listeners.contains(svl))
    {
      listeners.addElement(svl);
    }
  }

  /**
   * Returns the file name for a mapped PDB id (or null if not mapped).
   * 
   * @param pdbid
   * @return
   */
  public String alreadyMappedToFile(String pdbid)
  {
    for (StructureMapping sm : mappings)
    {
      if (sm.getPdbId().equals(pdbid))
      {
        return sm.pdbfile;
      }
    }
    return null;
  }

  /**
   * Import structure data and register a structure mapping for broadcasting
   * colouring, mouseovers and selection events (convenience wrapper).
   * 
   * @param sequence
   *          - one or more sequences to be mapped to pdbFile
   * @param targetChains
   *          - optional chain specification for mapping each sequence to pdb
   *          (may be nill, individual elements may be nill)
   * @param pdbFile
   *          - structure data resource
   * @param protocol
   *          - how to resolve data from resource
   * @return null or the structure data parsed as a pdb file
   */
  synchronized public PDBfile setMapping(SequenceI[] sequence,
          String[] targetChains, String pdbFile, String protocol)
  {
    return setMapping(true, sequence, targetChains, pdbFile, protocol);
  }

  /**
   * create sequence structure mappings between each sequence and the given
   * pdbFile (retrieved via the given protocol).
   * 
   * @param forStructureView
   *          when true, record the mapping for use in mouseOvers
   * 
   * @param sequence
   *          - one or more sequences to be mapped to pdbFile
   * @param targetChains
   *          - optional chain specification for mapping each sequence to pdb
   *          (may be nill, individual elements may be nill)
   * @param pdbFile
   *          - structure data resource
   * @param protocol
   *          - how to resolve data from resource
   * @return null or the structure data parsed as a pdb file
   */
  synchronized public PDBfile setMapping(boolean forStructureView,
          SequenceI[] sequence, String[] targetChains, String pdbFile,
          String protocol)
  {
    /*
     * There will be better ways of doing this in the future, for now we'll use
     * the tried and tested MCview pdb mapping
     */
    boolean parseSecStr = processSecondaryStructure;
    if (isPDBFileRegistered(pdbFile))
    {
      for (SequenceI sq : sequence)
      {
        SequenceI ds = sq;
        while (ds.getDatasetSequence() != null)
        {
          ds = ds.getDatasetSequence();
        }
        ;
        if (ds.getAnnotation() != null)
        {
          for (AlignmentAnnotation ala : ds.getAnnotation())
          {
            // false if any annotation present from this structure
            // JBPNote this fails for jmol/chimera view because the *file* is
            // passed, not the structure data ID -
            if (PDBfile.isCalcIdForFile(ala, findIdForPDBFile(pdbFile)))
            {
              parseSecStr = false;
            }
          }
        }
      }
    }
    PDBfile pdb = null;
    try
    {
      pdb = new PDBfile(addTempFacAnnot, parseSecStr, secStructServices,
              pdbFile, protocol);
      if (pdb.id != null && pdb.id.trim().length() > 0
              && AppletFormatAdapter.FILE.equals(protocol))
      {
        registerPDBFile(pdb.id.trim(), pdbFile);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }

    String targetChain;
    for (int s = 0; s < sequence.length; s++)
    {
      boolean infChain = true;
      final SequenceI seq = sequence[s];
      if (targetChains != null && targetChains[s] != null)
      {
        infChain = false;
        targetChain = targetChains[s];
      }
      else if (seq.getName().indexOf("|") > -1)
      {
        targetChain = seq.getName().substring(
                seq.getName().lastIndexOf("|") + 1);
        if (targetChain.length() > 1)
        {
          if (targetChain.trim().length() == 0)
          {
            targetChain = " ";
          }
          else
          {
            // not a valid chain identifier
            targetChain = "";
          }
        }
      }
      else
      {
        targetChain = "";
      }

      /*
       * Attempt pairwise alignment of the sequence with each chain in the PDB,
       * and remember the highest scoring chain
       */
      int max = -10;
      AlignSeq maxAlignseq = null;
      String maxChainId = " ";
      PDBChain maxChain = null;
      boolean first = true;
      for (PDBChain chain : pdb.chains)
      {
        if (targetChain.length() > 0 && !targetChain.equals(chain.id)
                && !infChain)
        {
          continue; // don't try to map chains don't match.
        }
        // TODO: correctly determine sequence type for mixed na/peptide
        // structures
        final String type = chain.isNa ? AlignSeq.DNA : AlignSeq.PEP;
        AlignSeq as = AlignSeq.doGlobalNWAlignment(seq, chain.sequence,
                type);
        // equivalent to:
        // AlignSeq as = new AlignSeq(sequence[s], chain.sequence, type);
        // as.calcScoreMatrix();
        // as.traceAlignment();

        if (first || as.maxscore > max
                || (as.maxscore == max && chain.id.equals(targetChain)))
        {
          first = false;
          maxChain = chain;
          max = as.maxscore;
          maxAlignseq = as;
          maxChainId = chain.id;
        }
      }
      if (maxChain == null)
      {
        continue;
      }
      final StringBuilder mappingDetails = new StringBuilder(128);
      mappingDetails.append(NEWLINE).append("PDB Sequence is :")
              .append(NEWLINE).append("Sequence = ")
              .append(maxChain.sequence.getSequenceAsString());
      mappingDetails.append(NEWLINE).append("No of residues = ")
              .append(maxChain.residues.size()).append(NEWLINE)
              .append(NEWLINE);
      PrintStream ps = new PrintStream(System.out)
      {
        @Override
        public void print(String x)
        {
          mappingDetails.append(x);
        }

        @Override
        public void println()
        {
          mappingDetails.append(NEWLINE);
        }
      };

      maxAlignseq.printAlignment(ps);

      mappingDetails.append(NEWLINE).append("PDB start/end ");
      mappingDetails.append(String.valueOf(maxAlignseq.seq2start)).append(
              " ");
      mappingDetails.append(String.valueOf(maxAlignseq.seq2end));

      mappingDetails.append(NEWLINE).append("SEQ start/end ");
      mappingDetails.append(
              String.valueOf(maxAlignseq.seq1start + seq.getStart() - 1))
              .append(" ");
      mappingDetails.append(String.valueOf(maxAlignseq.seq1end
              + seq.getEnd() - 1));

      maxChain.makeExactMapping(maxAlignseq, seq);
      jalview.datamodel.Mapping sqmpping = maxAlignseq
              .getMappingFromS1(false);
      jalview.datamodel.Mapping omap = new jalview.datamodel.Mapping(
              sqmpping.getMap().getInverse());
      maxChain.transferRESNUMFeatures(seq, null);

      // allocate enough slots to store the mapping from positions in
      // sequence[s] to the associated chain
      int[][] mapping = new int[seq.findPosition(seq.getLength()) + 2][2];
      int resNum = -10000;
      int index = 0;

      do
      {
        Atom tmp = maxChain.atoms.elementAt(index);
        if (resNum != tmp.resNumber && tmp.alignmentMapping != -1)
        {
          resNum = tmp.resNumber;
          if (tmp.alignmentMapping >= -1)
          {
            // TODO (JAL-1836) address root cause: negative residue no in PDB
            // file
            mapping[tmp.alignmentMapping + 1][0] = tmp.resNumber;
            mapping[tmp.alignmentMapping + 1][1] = tmp.atomIndex;
          }
        }

        index++;
      } while (index < maxChain.atoms.size());

      if (protocol.equals(jalview.io.AppletFormatAdapter.PASTE))
      {
        pdbFile = "INLINE" + pdb.id;
      }
      StructureMapping newMapping = new StructureMapping(seq, pdbFile,
              pdb.id, maxChainId, mapping, mappingDetails.toString());
      if (forStructureView)
      {
        mappings.add(newMapping);
      }
      maxChain.transferResidueAnnotation(newMapping, sqmpping);
    }
    // ///////

    return pdb;
  }

  public void removeStructureViewerListener(Object svl, String[] pdbfiles)
  {
    listeners.removeElement(svl);
    if (svl instanceof SequenceListener)
    {
      for (int i = 0; i < listeners.size(); i++)
      {
        if (listeners.elementAt(i) instanceof StructureListener)
        {
          ((StructureListener) listeners.elementAt(i))
                  .releaseReferences(svl);
        }
      }
    }

    if (pdbfiles == null)
    {
      return;
    }

    /*
     * Remove mappings to the closed listener's PDB files, but first check if
     * another listener is still interested
     */
    List<String> pdbs = new ArrayList<String>(Arrays.asList(pdbfiles));

    StructureListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);
        for (String pdbfile : sl.getPdbFile())
        {
          pdbs.remove(pdbfile);
        }
      }
    }

    /*
     * Rebuild the mappings set, retaining only those which are for 'other' PDB
     * files
     */
    if (pdbs.size() > 0)
    {
      List<StructureMapping> tmp = new ArrayList<StructureMapping>();
      for (StructureMapping sm : mappings)
      {
        if (!pdbs.contains(sm.pdbfile))
        {
          tmp.add(sm);
        }
      }

      mappings = tmp;
    }
  }

  /**
   * Propagate mouseover of a single position in a structure
   * 
   * @param pdbResNum
   * @param chain
   * @param pdbfile
   */
  public void mouseOverStructure(int pdbResNum, String chain, String pdbfile)
  {
    AtomSpec atomSpec = new AtomSpec(pdbfile, chain, pdbResNum, 0);
    List<AtomSpec> atoms = Collections.singletonList(atomSpec);
    mouseOverStructure(atoms);
  }

  /**
   * Propagate mouseover or selection of multiple positions in a structure
   * 
   * @param atoms
   */
  public void mouseOverStructure(List<AtomSpec> atoms)
  {
    if (listeners == null)
    {
      // old or prematurely sent event
      return;
    }
    boolean hasSequenceListener = false;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof SequenceListener)
      {
        hasSequenceListener = true;
      }
    }
    if (!hasSequenceListener)
    {
      return;
    }

    SearchResults results = new SearchResults();
    for (AtomSpec atom : atoms)
    {
      SequenceI lastseq = null;
      int lastipos = -1;
      for (StructureMapping sm : mappings)
      {
        if (sm.pdbfile.equals(atom.getPdbFile())
                && sm.pdbchain.equals(atom.getChain()))
        {
          int indexpos = sm.getSeqPos(atom.getPdbResNum());
          if (lastipos != indexpos && lastseq != sm.sequence)
          {
            results.addResult(sm.sequence, indexpos, indexpos);
            lastipos = indexpos;
            lastseq = sm.sequence;
            // construct highlighted sequence list
            for (AlignedCodonFrame acf : seqmappings)
            {
              acf.markMappedRegion(sm.sequence, indexpos, results);
            }
          }
        }
      }
    }
    for (Object li : listeners)
    {
      if (li instanceof SequenceListener)
      {
        ((SequenceListener) li).highlightSequence(results);
      }
    }
  }

  /**
   * highlight regions associated with a position (indexpos) in seq
   * 
   * @param seq
   *          the sequence that the mouse over occurred on
   * @param indexpos
   *          the absolute position being mouseovered in seq (0 to seq.length())
   * @param index
   *          the sequence position (if -1, seq.findPosition is called to
   *          resolve the residue number)
   */
  public void mouseOverSequence(SequenceI seq, int indexpos, int index,
          VamsasSource source)
  {
    boolean hasSequenceListeners = handlingVamsasMo
            || !seqmappings.isEmpty();
    SearchResults results = null;
    if (index == -1)
    {
      index = seq.findPosition(indexpos);
    }
    for (int i = 0; i < listeners.size(); i++)
    {
      Object listener = listeners.elementAt(i);
      if (listener == source)
      {
        // TODO listener (e.g. SeqPanel) is never == source (AlignViewport)
        // Temporary fudge with SequenceListener.getVamsasSource()
        continue;
      }
      if (listener instanceof StructureListener)
      {
        highlightStructure((StructureListener) listener, seq, index);
      }
      else
      {
        if (listener instanceof SequenceListener)
        {
          final SequenceListener seqListener = (SequenceListener) listener;
          if (hasSequenceListeners
                  && seqListener.getVamsasSource() != source)
          {
            if (relaySeqMappings)
            {
              if (results == null)
              {
                results = MappingUtils.buildSearchResults(seq, index,
                        seqmappings);
              }
              if (handlingVamsasMo)
              {
                results.addResult(seq, index, index);

              }
              if (!results.isEmpty())
              {
                seqListener.highlightSequence(results);
              }
            }
          }
        }
        else if (listener instanceof VamsasListener && !handlingVamsasMo)
        {
          ((VamsasListener) listener).mouseOverSequence(seq, indexpos,
                  source);
        }
        else if (listener instanceof SecondaryStructureListener)
        {
          ((SecondaryStructureListener) listener).mouseOverSequence(seq,
                  indexpos, index);
        }
      }
    }
  }

  /**
   * Send suitable messages to a StructureListener to highlight atoms
   * corresponding to the given sequence position.
   * 
   * @param sl
   * @param seq
   * @param index
   */
  protected void highlightStructure(StructureListener sl, SequenceI seq,
          int index)
  {
    if (!sl.isListeningFor(seq))
    {
      return;
    }
    int atomNo;
    List<AtomSpec> atoms = new ArrayList<AtomSpec>();
    for (StructureMapping sm : mappings)
    {
      if (sm.sequence == seq || sm.sequence == seq.getDatasetSequence())
      {
        atomNo = sm.getAtomNum(index);

        if (atomNo > 0)
        {
          atoms.add(new AtomSpec(sm.pdbfile, sm.pdbchain, sm
                  .getPDBResNum(index), atomNo));
        }
      }
    }
    sl.highlightAtoms(atoms);
  }

  /**
   * true if a mouse over event from an external (ie Vamsas) source is being
   * handled
   */
  boolean handlingVamsasMo = false;

  long lastmsg = 0;

  /**
   * as mouseOverSequence but only route event to SequenceListeners
   * 
   * @param sequenceI
   * @param position
   *          in an alignment sequence
   */
  public void mouseOverVamsasSequence(SequenceI sequenceI, int position,
          VamsasSource source)
  {
    handlingVamsasMo = true;
    long msg = sequenceI.hashCode() * (1 + position);
    if (lastmsg != msg)
    {
      lastmsg = msg;
      mouseOverSequence(sequenceI, position, -1, source);
    }
    handlingVamsasMo = false;
  }

  public Annotation[] colourSequenceFromStructure(SequenceI seq,
          String pdbid)
  {
    return null;
    // THIS WILL NOT BE AVAILABLE IN JALVIEW 2.3,
    // UNTIL THE COLOUR BY ANNOTATION IS REWORKED
    /*
     * Annotation [] annotations = new Annotation[seq.getLength()];
     * 
     * StructureListener sl; int atomNo = 0; for (int i = 0; i <
     * listeners.size(); i++) { if (listeners.elementAt(i) instanceof
     * StructureListener) { sl = (StructureListener) listeners.elementAt(i);
     * 
     * for (int j = 0; j < mappings.length; j++) {
     * 
     * if (mappings[j].sequence == seq && mappings[j].getPdbId().equals(pdbid)
     * && mappings[j].pdbfile.equals(sl.getPdbFile())) {
     * System.out.println(pdbid+" "+mappings[j].getPdbId() +"
     * "+mappings[j].pdbfile);
     * 
     * java.awt.Color col; for(int index=0; index<seq.getLength(); index++) {
     * if(jalview.util.Comparison.isGap(seq.getCharAt(index))) continue;
     * 
     * atomNo = mappings[j].getAtomNum(seq.findPosition(index)); col =
     * java.awt.Color.white; if (atomNo > 0) { col = sl.getColour(atomNo,
     * mappings[j].getPDBResNum(index), mappings[j].pdbchain,
     * mappings[j].pdbfile); }
     * 
     * annotations[index] = new Annotation("X",null,' ',0,col); } return
     * annotations; } } } }
     * 
     * return annotations;
     */
  }

  public void structureSelectionChanged()
  {
  }

  public void sequenceSelectionChanged()
  {
  }

  public void sequenceColoursChanged(Object source)
  {
    StructureListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);
        sl.updateColours(source);
      }
    }
  }

  public StructureMapping[] getMapping(String pdbfile)
  {
    List<StructureMapping> tmp = new ArrayList<StructureMapping>();
    for (StructureMapping sm : mappings)
    {
      if (sm.pdbfile.equals(pdbfile))
      {
        tmp.add(sm);
      }
    }
    return tmp.toArray(new StructureMapping[tmp.size()]);
  }

  /**
   * Returns a readable description of all mappings for the given pdbfile to any
   * of the given sequences
   * 
   * @param pdbfile
   * @param seqs
   * @return
   */
  public String printMappings(String pdbfile, List<SequenceI> seqs)
  {
    if (pdbfile == null || seqs == null || seqs.isEmpty())
    {
      return "";
    }

    StringBuilder sb = new StringBuilder(64);
    for (StructureMapping sm : mappings)
    {
      if (sm.pdbfile.equals(pdbfile) && seqs.contains(sm.sequence))
      {
        sb.append(sm.mappingDetails);
        sb.append(NEWLINE);
        // separator makes it easier to read multiple mappings
        sb.append("=====================");
        sb.append(NEWLINE);
      }
    }
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Remove the given mapping
   * 
   * @param acf
   */
  public void deregisterMapping(AlignedCodonFrame acf)
  {
    if (acf != null)
    {
      boolean removed = seqmappings.remove(acf);
      if (removed && seqmappings.isEmpty())
      { // debug
        System.out.println("All mappings removed");
      }
    }
  }

  /**
   * Add each of the given codonFrames to the stored set, if not aready present.
   * 
   * @param set
   */
  public void registerMappings(Set<AlignedCodonFrame> set)
  {
    if (set != null)
    {
      for (AlignedCodonFrame acf : set)
      {
        registerMapping(acf);
      }
    }
  }

  /**
   * Add the given mapping to the stored set, unless already stored.
   */
  public void registerMapping(AlignedCodonFrame acf)
  {
    if (acf != null)
    {
      if (!seqmappings.contains(acf))
      {
        seqmappings.add(acf);
      }
    }
  }

  /**
   * Resets this object to its initial state by removing all registered
   * listeners, codon mappings, PDB file mappings
   */
  public void resetAll()
  {
    if (mappings != null)
    {
      mappings.clear();
    }
    if (seqmappings != null)
    {
      seqmappings.clear();
    }
    if (sel_listeners != null)
    {
      sel_listeners.clear();
    }
    if (listeners != null)
    {
      listeners.clear();
    }
    if (commandListeners != null)
    {
      commandListeners.clear();
    }
    if (view_listeners != null)
    {
      view_listeners.clear();
    }
    if (pdbFileNameId != null)
    {
      pdbFileNameId.clear();
    }
    if (pdbIdFileName != null)
    {
      pdbIdFileName.clear();
    }
  }

  public void addSelectionListener(SelectionListener selecter)
  {
    if (!sel_listeners.contains(selecter))
    {
      sel_listeners.add(selecter);
    }
  }

  public void removeSelectionListener(SelectionListener toremove)
  {
    if (sel_listeners.contains(toremove))
    {
      sel_listeners.remove(toremove);
    }
  }

  public synchronized void sendSelection(
          jalview.datamodel.SequenceGroup selection,
          jalview.datamodel.ColumnSelection colsel, SelectionSource source)
  {
    for (SelectionListener slis : sel_listeners)
    {
      if (slis != source)
      {
        slis.selection(selection, colsel, source);
      }
    }
  }

  Vector<AlignmentViewPanelListener> view_listeners = new Vector<AlignmentViewPanelListener>();

  public synchronized void sendViewPosition(
          jalview.api.AlignmentViewPanel source, int startRes, int endRes,
          int startSeq, int endSeq)
  {

    if (view_listeners != null && view_listeners.size() > 0)
    {
      Enumeration<AlignmentViewPanelListener> listeners = view_listeners
              .elements();
      while (listeners.hasMoreElements())
      {
        AlignmentViewPanelListener slis = listeners.nextElement();
        if (slis != source)
        {
          slis.viewPosition(startRes, endRes, startSeq, endSeq, source);
        }
        ;
      }
    }
  }

  /**
   * release all references associated with this manager provider
   * 
   * @param jalviewLite
   */
  public static void release(StructureSelectionManagerProvider jalviewLite)
  {
    // synchronized (instances)
    {
      if (instances == null)
      {
        return;
      }
      StructureSelectionManager mnger = (instances.get(jalviewLite));
      if (mnger != null)
      {
        instances.remove(jalviewLite);
        try
        {
          mnger.finalize();
        } catch (Throwable x)
        {
        }
      }
    }
  }

  public void registerPDBEntry(PDBEntry pdbentry)
  {
    if (pdbentry.getFile() != null
            && pdbentry.getFile().trim().length() > 0)
    {
      registerPDBFile(pdbentry.getId(), pdbentry.getFile());
    }
  }

  public void addCommandListener(CommandListener cl)
  {
    if (!commandListeners.contains(cl))
    {
      commandListeners.add(cl);
    }
  }

  public boolean hasCommandListener(CommandListener cl)
  {
    return this.commandListeners.contains(cl);
  }

  public boolean removeCommandListener(CommandListener l)
  {
    return commandListeners.remove(l);
  }

  /**
   * Forward a command to any command listeners (except for the command's
   * source).
   * 
   * @param command
   *          the command to be broadcast (in its form after being performed)
   * @param undo
   *          if true, the command was being 'undone'
   * @param source
   */
  public void commandPerformed(CommandI command, boolean undo,
          VamsasSource source)
  {
    for (CommandListener listener : commandListeners)
    {
      listener.mirrorCommand(command, undo, this, source);
    }
  }

  /**
   * Returns a new CommandI representing the given command as mapped to the
   * given sequences. If no mapping could be made, or the command is not of a
   * mappable kind, returns null.
   * 
   * @param command
   * @param undo
   * @param mapTo
   * @param gapChar
   * @return
   */
  public CommandI mapCommand(CommandI command, boolean undo,
          final AlignmentI mapTo, char gapChar)
  {
    if (command instanceof EditCommand)
    {
      return MappingUtils.mapEditCommand((EditCommand) command, undo,
              mapTo, gapChar, seqmappings);
    }
    else if (command instanceof OrderCommand)
    {
      return MappingUtils.mapOrderCommand((OrderCommand) command, undo,
              mapTo, seqmappings);
    }
    return null;
  }
}
