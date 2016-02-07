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
package MCview;

import jalview.analysis.AlignSeq;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.FileParse;
import jalview.util.MessageManager;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class PDBfile extends jalview.io.AlignFile
{
  private static String CALC_ID_PREFIX = "JalviewPDB";

  public Vector<PDBChain> chains;

  public String id;

  /**
   * set to true to add derived sequence annotations (temp factor read from
   * file, or computed secondary structure) to the alignment
   */
  private boolean visibleChainAnnotation = false;

  /*
   * Set true to predict secondary structure (using JMol for protein, Annotate3D
   * for RNA)
   */
  private boolean predictSecondaryStructure = true;

  /*
   * Set true (with predictSecondaryStructure=true) to predict secondary
   * structure using an external service (currently Annotate3D for RNA only)
   */
  private boolean externalSecondaryStructure = false;

  public PDBfile(boolean addAlignmentAnnotations,
          boolean predictSecondaryStructure, boolean externalSecStr)
  {
    super();
    this.visibleChainAnnotation = addAlignmentAnnotations;
    this.predictSecondaryStructure = predictSecondaryStructure;
    this.externalSecondaryStructure = externalSecStr;
  }

  public PDBfile(boolean addAlignmentAnnotations,
          boolean predictSecondaryStructure, boolean externalSecStr,
          String file, String protocol) throws IOException
  {
    super(false, file, protocol);
    this.visibleChainAnnotation = addAlignmentAnnotations;
    this.predictSecondaryStructure = predictSecondaryStructure;
    this.externalSecondaryStructure = externalSecStr;
    doParse();
  }

  public PDBfile(boolean addAlignmentAnnotations,
          boolean predictSecondaryStructure, boolean externalSecStr,
          FileParse source) throws IOException
  {
    super(false, source);
    this.visibleChainAnnotation = addAlignmentAnnotations;
    this.predictSecondaryStructure = predictSecondaryStructure;
    this.externalSecondaryStructure = externalSecStr;
    doParse();
  }

  public String print()
  {
    return null;
  }

  public void parse() throws IOException
  {
    // TODO set the filename sensibly - try using data source name.
    id = safeName(getDataName());

    chains = new Vector<PDBChain>();
    List<SequenceI> rna = new ArrayList<SequenceI>();
    List<SequenceI> prot = new ArrayList<SequenceI>();
    PDBChain tmpchain;
    String line = null;
    boolean modelFlag = false;
    boolean terFlag = false;
    String lastID = "";

    int indexx = 0;
    String atomnam = null;
    try
    {
      while ((line = nextLine()) != null)
      {
        if (line.indexOf("HEADER") == 0)
        {
          if (line.length() > 62)
          {
            String tid;
            if (line.length() > 67)
            {
              tid = line.substring(62, 67).trim();
            }
            else
            {
              tid = line.substring(62).trim();
            }
            if (tid.length() > 0)
            {
              id = tid;
            }
            continue;
          }
        }
        // Were we to do anything with SEQRES - we start it here
        if (line.indexOf("SEQRES") == 0)
        {
        }

        if (line.indexOf("MODEL") == 0)
        {
          modelFlag = true;
        }

        if (line.indexOf("TER") == 0)
        {
          terFlag = true;
        }

        if (modelFlag && line.indexOf("ENDMDL") == 0)
        {
          break;
        }
        if (line.indexOf("ATOM") == 0
                || (line.indexOf("HETATM") == 0 && !terFlag))
        {
          terFlag = false;

          // Jalview is only interested in CA bonds????
          atomnam = line.substring(12, 15).trim();
          if (!atomnam.equals("CA") && !atomnam.equals("P"))
          {
            continue;
          }

          Atom tmpatom = new Atom(line);
          tmpchain = findChain(tmpatom.chain);
          if (tmpchain != null)
          {
            if (tmpatom.resNumIns.trim().equals(lastID))
            {
              // phosphorylated protein - seen both CA and P..
              continue;
            }
            tmpchain.atoms.addElement(tmpatom);
          }
          else
          {
            tmpchain = new PDBChain(id, tmpatom.chain);
            chains.addElement(tmpchain);
            tmpchain.atoms.addElement(tmpatom);
          }
          lastID = tmpatom.resNumIns.trim();
        }
        index++;
      }

      makeResidueList();
      makeCaBondList();

      if (id == null)
      {
        id = inFile.getName();
      }
      for (PDBChain chain : chains)
      {
        SequenceI chainseq = postProcessChain(chain);
        if (isRNA(chainseq))
        {
          rna.add(chainseq);
        }
        else
        {
          prot.add(chainseq);
        }
      }
      if (predictSecondaryStructure)
      {
        predictSecondaryStructure(rna, prot);
      }
    } catch (OutOfMemoryError er)
    {
      System.out.println("OUT OF MEMORY LOADING PDB FILE");
      throw new IOException(
              MessageManager
                      .getString("exception.outofmemory_loading_pdb_file"));
    } catch (NumberFormatException ex)
    {
      if (line != null)
      {
        System.err.println("Couldn't read number from line:");
        System.err.println(line);
      }
    }
    markCalcIds();
  }

  /**
   * Predict secondary structure for RNA and/or protein sequences and add as
   * annotations
   * 
   * @param rnaSequences
   * @param proteinSequences
   */
  protected void predictSecondaryStructure(List<SequenceI> rnaSequences,
          List<SequenceI> proteinSequences)
  {
    /*
     * Currently using Annotate3D for RNA, but only if the 'use external
     * prediction' flag is set
     */
    if (externalSecondaryStructure && rnaSequences.size() > 0)
    {
      try
      {
        processPdbFileWithAnnotate3d(rnaSequences);
      } catch (Exception x)
      {
        System.err.println("Exceptions when dealing with RNA in pdb file");
        x.printStackTrace();

      }
    }

    /*
     * Currently using JMol PDB parser for peptide
     */
    if (proteinSequences.size() > 0)
    {
      try
      {
        processPdbFileWithJmol(proteinSequences);
      } catch (Exception x)
      {
        System.err
                .println("Exceptions from Jmol when processing data in pdb file");
        x.printStackTrace();
      }
    }
  }

  /**
   * Process a parsed chain to construct and return a Sequence, and add it to
   * the list of sequences parsed.
   * 
   * @param chain
   * @return
   */
  protected SequenceI postProcessChain(PDBChain chain)
  {
    SequenceI dataset = chain.sequence;
    dataset.setName(id + "|" + dataset.getName());
    PDBEntry entry = new PDBEntry();
    entry.setId(id);
    entry.setType(PDBEntry.Type.PDB);
    entry.setProperty(new Hashtable());
    if (chain.id != null)
    {
      // entry.getProperty().put("CHAIN", chains.elementAt(i).id);
      entry.setChainCode(String.valueOf(chain.id));
    }
    if (inFile != null)
    {
      entry.setFile(inFile.getAbsolutePath());
    }
    else
    {
      // TODO: decide if we should dump the datasource to disk
      entry.setFile(getDataName());
    }
    dataset.addPDBId(entry);
    // PDBChain objects maintain reference to dataset
    SequenceI chainseq = dataset.deriveSequence();
    seqs.addElement(chainseq);

    AlignmentAnnotation[] chainannot = chainseq.getAnnotation();

    if (chainannot != null && visibleChainAnnotation)
    {
      for (int ai = 0; ai < chainannot.length; ai++)
      {
        chainannot[ai].visible = visibleChainAnnotation;
        annotations.addElement(chainannot[ai]);
      }
    }
    return chainseq;
  }

  public static boolean isCalcIdHandled(String calcId)
  {
    return calcId != null && (CALC_ID_PREFIX.equals(calcId));
  }

  public static boolean isCalcIdForFile(AlignmentAnnotation alan,
          String pdbFile)
  {
    return alan.getCalcId() != null
            && CALC_ID_PREFIX.equals(alan.getCalcId())
            && pdbFile.equals(alan.getProperty("PDBID"));
  }

  public static String relocateCalcId(String calcId,
          Hashtable<String, String> alreadyLoadedPDB) throws Exception
  {
    int s = CALC_ID_PREFIX.length(), end = calcId
            .indexOf(CALC_ID_PREFIX, s);
    String between = calcId.substring(s, end - 1);
    return CALC_ID_PREFIX + alreadyLoadedPDB.get(between) + ":"
            + calcId.substring(end);
  }

  private void markCalcIds()
  {
    for (SequenceI sq : seqs)
    {
      if (sq.getAnnotation() != null)
      {
        for (AlignmentAnnotation aa : sq.getAnnotation())
        {
          String oldId = aa.getCalcId();
          if (oldId == null)
          {
            oldId = "";
          }
          aa.setCalcId(CALC_ID_PREFIX);
          aa.setProperty("PDBID", id);
          aa.setProperty("oldCalcId", oldId);
        }
      }
    }
  }

  private void processPdbFileWithJmol(List<SequenceI> prot)
          throws Exception
  {
    try
    {
      Class cl = Class.forName("jalview.ext.jmol.PDBFileWithJmol");
      if (cl != null)
      {
        final Constructor constructor = cl
                .getConstructor(new Class[] { FileParse.class });
        final Object[] args = new Object[] { new FileParse(getDataName(),
                type) };
        Object jmf = constructor.newInstance(args);
        AlignmentI al = new Alignment((SequenceI[]) cl.getMethod(
                "getSeqsAsArray", new Class[] {}).invoke(jmf));
        cl.getMethod("addAnnotations", new Class[] { AlignmentI.class })
                .invoke(jmf, al);
        for (SequenceI sq : al.getSequences())
        {
          if (sq.getDatasetSequence() != null)
          {
            sq.getDatasetSequence().getAllPDBEntries().clear();
          }
          else
          {
            sq.getAllPDBEntries().clear();
          }
        }
        replaceAndUpdateChains(prot, al, AlignSeq.PEP, false);
      }
    } catch (ClassNotFoundException q)
    {
    }
  }

  private void replaceAndUpdateChains(List<SequenceI> prot, AlignmentI al,
          String pep, boolean b)
  {
    List<List<? extends Object>> replaced = AlignSeq
            .replaceMatchingSeqsWith(seqs, annotations, prot, al, pep,
                    false);
    for (PDBChain ch : chains)
    {
      int p = 0;
      for (SequenceI sq : (List<SequenceI>) replaced.get(0))
      {
        p++;
        if (sq == ch.sequence || sq.getDatasetSequence() == ch.sequence)
        {
          p = -p;
          break;
        }
      }
      if (p < 0)
      {
        p = -p - 1;
        // set shadow entry for chains
        ch.shadow = (SequenceI) replaced.get(1).get(p);
        ch.shadowMap = ((AlignSeq) replaced.get(2).get(p))
                .getMappingFromS1(false);
      }
    }
  }

  private void processPdbFileWithAnnotate3d(List<SequenceI> rna)
          throws Exception
  {
    // System.out.println("this is a PDB format and RNA sequence");
    // note: we use reflection here so that the applet can compile and run
    // without the HTTPClient bits and pieces needed for accessing Annotate3D
    // web service
    try
    {
      Class cl = Class.forName("jalview.ws.jws1.Annotate3D");
      if (cl != null)
      {
        // TODO: use the PDB ID of the structure if one is available, to save
        // bandwidth and avoid uploading the whole structure to the service
        Object annotate3d = cl.getConstructor(new Class[] {}).newInstance(
                new Object[] {});
        AlignmentI al = ((AlignmentI) cl.getMethod("getRNAMLFor",
                new Class[] { FileParse.class }).invoke(annotate3d,
                new Object[] { new FileParse(getDataName(), type) }));
        for (SequenceI sq : al.getSequences())
        {
          if (sq.getDatasetSequence() != null)
          {
            if (sq.getDatasetSequence().getAllPDBEntries() != null)
            {
              sq.getDatasetSequence().getAllPDBEntries().clear();
            }
          }
          else
          {
            if (sq.getAllPDBEntries() != null)
            {
              sq.getAllPDBEntries().clear();
            }
          }
        }
        replaceAndUpdateChains(rna, al, AlignSeq.DNA, false);
      }
    } catch (ClassNotFoundException x)
    {
      // ignore classnotfounds - occurs in applet
    }
    ;
  }

  /**
   * make a friendly ID string.
   * 
   * @param dataName
   * @return truncated dataName to after last '/'
   */
  private String safeName(String dataName)
  {
    int p = 0;
    while ((p = dataName.indexOf("/")) > -1 && p < dataName.length())
    {
      dataName = dataName.substring(p + 1);
    }
    return dataName;
  }

  public void makeResidueList()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      chains.elementAt(i).makeResidueList(visibleChainAnnotation);
    }
  }

  public void makeCaBondList()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      chains.elementAt(i).makeCaBondList();
    }
  }

  public PDBChain findChain(String id)
  {
    for (int i = 0; i < chains.size(); i++)
    {
      if (chains.elementAt(i).id.equals(id))
      {
        return chains.elementAt(i);
      }
    }

    return null;
  }

  public void setChargeColours()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      chains.elementAt(i).setChargeColours();
    }
  }

  public void setColours(jalview.schemes.ColourSchemeI cs)
  {
    for (int i = 0; i < chains.size(); i++)
    {
      chains.elementAt(i).setChainColours(cs);
    }
  }

  public void setChainColours()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      // divide by zero --> infinity --> 255 ;-)
      chains.elementAt(i).setChainColours(
              Color.getHSBColor(1.0f / i, .4f, 1.0f));
    }
  }

  public static boolean isRNA(SequenceI seq)
  {
    for (char c : seq.getSequence())
    {
      if ((c != 'A') && (c != 'C') && (c != 'G') && (c != 'U'))
      {
        return false;
      }
    }

    return true;

  }
}
