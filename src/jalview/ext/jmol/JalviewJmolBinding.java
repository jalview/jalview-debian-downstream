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
package jalview.ext.jmol;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.AppletFormatAdapter;
import jalview.io.StructureFile;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueProperties;
import jalview.structure.AtomSpec;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.api.JmolSelectionListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.c.CBK;
import org.jmol.script.T;
import org.jmol.viewer.Viewer;

public abstract class JalviewJmolBinding extends AAStructureBindingModel
        implements JmolStatusListener, JmolSelectionListener,
        ComponentListener
{
  boolean allChainsSelected = false;

  /*
   * when true, try to search the associated datamodel for sequences that are
   * associated with any unknown structures in the Jmol view.
   */
  private boolean associateNewStructs = false;

  Vector<String> atomsPicked = new Vector<String>();

  private List<String> chainNames;

  Hashtable<String, String> chainFile;

  public String fileLoadingError;

  /*
   * the default or current model displayed if the model cannot be identified
   * from the selection message
   */
  int frameNo = 0;

  // protected JmolGenericPopup jmolpopup; // not used - remove?

  String lastCommand;

  String lastMessage;

  boolean loadedInline;

  StringBuffer resetLastRes = new StringBuffer();

  public Viewer viewer;

  public JalviewJmolBinding(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs,
          String protocol)
  {
    super(ssm, pdbentry, sequenceIs, protocol);
    /*
     * viewer = JmolViewer.allocateViewer(renderPanel, new SmarterJmolAdapter(),
     * "jalviewJmol", ap.av.applet .getDocumentBase(),
     * ap.av.applet.getCodeBase(), "", this);
     * 
     * jmolpopup = JmolPopup.newJmolPopup(viewer, true, "Jmol", true);
     */
  }

  public JalviewJmolBinding(StructureSelectionManager ssm,
          SequenceI[][] seqs, Viewer theViewer)
  {
    super(ssm, seqs);

    viewer = theViewer;
    viewer.setJmolStatusListener(this);
    viewer.addSelectionListener(this);
  }

  /**
   * construct a title string for the viewer window based on the data jalview
   * knows about
   * 
   * @return
   */
  public String getViewerTitle()
  {
    return getViewerTitle("Jmol", true);
  }

  /**
   * prepare the view for a given set of models/chains. chainList contains
   * strings of the form 'pdbfilename:Chaincode'
   * 
   * @param chainList
   *          list of chains to make visible
   */
  public void centerViewer(Vector<String> chainList)
  {
    StringBuilder cmd = new StringBuilder(128);
    int mlength, p;
    for (String lbl : chainList)
    {
      mlength = 0;
      do
      {
        p = mlength;
        mlength = lbl.indexOf(":", p);
      } while (p < mlength && mlength < (lbl.length() - 2));
      // TODO: lookup each pdb id and recover proper model number for it.
      cmd.append(":" + lbl.substring(mlength + 1) + " /"
              + (1 + getModelNum(chainFile.get(lbl))) + " or ");
    }
    if (cmd.length() > 0)
    {
      cmd.setLength(cmd.length() - 4);
    }
    evalStateCommand("select *;restrict " + cmd + ";cartoon;center " + cmd);
  }

  public void closeViewer()
  {
    // remove listeners for all structures in viewer
    getSsm().removeStructureViewerListener(this, this.getPdbFile());
    viewer.dispose();
    lastCommand = null;
    viewer = null;
    releaseUIResources();
  }

  public void colourByChain()
  {
    colourBySequence = false;
    // TODO: colour by chain should colour each chain distinctly across all
    // visible models
    // TODO: http://issues.jalview.org/browse/JAL-628
    evalStateCommand("select *;color chain");
  }

  public void colourByCharge()
  {
    colourBySequence = false;
    evalStateCommand("select *;color white;select ASP,GLU;color red;"
            + "select LYS,ARG;color blue;select CYS;color yellow");
  }

  /**
   * superpose the structures associated with sequences in the alignment
   * according to their corresponding positions.
   */
  public void superposeStructures(AlignmentI alignment)
  {
    superposeStructures(alignment, -1, null);
  }

  /**
   * superpose the structures associated with sequences in the alignment
   * according to their corresponding positions. ded)
   * 
   * @param refStructure
   *          - select which pdb file to use as reference (default is -1 - the
   *          first structure in the alignment)
   */
  public void superposeStructures(AlignmentI alignment, int refStructure)
  {
    superposeStructures(alignment, refStructure, null);
  }

  /**
   * superpose the structures associated with sequences in the alignment
   * according to their corresponding positions. ded)
   * 
   * @param refStructure
   *          - select which pdb file to use as reference (default is -1 - the
   *          first structure in the alignment)
   * @param hiddenCols
   *          TODO
   */
  public void superposeStructures(AlignmentI alignment, int refStructure,
          ColumnSelection hiddenCols)
  {
    superposeStructures(new AlignmentI[] { alignment },
            new int[] { refStructure },
            new ColumnSelection[] { hiddenCols });
  }

  /**
   * Construct and send a command to align structures against a reference
   * structure, based on one or more sequence alignments
   * 
   * @param _alignment
   *          an array of alignments to process
   * @param _refStructure
   *          an array of corresponding reference structures (index into pdb
   *          file array); if a negative value is passed, the first PDB file
   *          mapped to an alignment sequence is used as the reference for
   *          superposition
   * @param _hiddenCols
   *          an array of corresponding hidden columns for each alignment
   */
  public void superposeStructures(AlignmentI[] _alignment,
          int[] _refStructure, ColumnSelection[] _hiddenCols)
  {
    while (viewer.isScriptExecuting())
    {
      try
      {
        Thread.sleep(10);
      } catch (InterruptedException i)
      {
      }
    }

    /*
     * get the distinct structure files modelled
     * (a file with multiple chains may map to multiple sequences)
     */
    String[] files = getPdbFile();
    if (!waitForFileLoad(files))
    {
      return;
    }

    StringBuilder selectioncom = new StringBuilder(256);
    // In principle - nSeconds specifies the speed of animation for each
    // superposition - but is seems to behave weirdly, so we don't specify it.
    String nSeconds = " ";
    if (files.length > 10)
    {
      nSeconds = " 0.005 ";
    }
    else
    {
      nSeconds = " " + (2.0 / files.length) + " ";
      // if (nSeconds).substring(0,5)+" ";
    }
    // see JAL-1345 - should really automatically turn off the animation for
    // large numbers of structures, but Jmol doesn't seem to allow that.
    // nSeconds = " ";
    // union of all aligned positions are collected together.
    for (int a = 0; a < _alignment.length; a++)
    {
      int refStructure = _refStructure[a];
      AlignmentI alignment = _alignment[a];
      ColumnSelection hiddenCols = _hiddenCols[a];
      if (a > 0
              && selectioncom.length() > 0
              && !selectioncom.substring(selectioncom.length() - 1).equals(
                      "|"))
      {
        selectioncom.append("|");
      }
      // process this alignment
      if (refStructure >= files.length)
      {
        System.err.println("Invalid reference structure value "
                + refStructure);
        refStructure = -1;
      }

      /*
       * 'matched' array will hold 'true' for visible alignment columns where
       * all sequences have a residue with a mapping to the PDB structure
       */
      // TODO could use a BitSet for matched
      boolean matched[] = new boolean[alignment.getWidth()];
      for (int m = 0; m < matched.length; m++)
      {
        matched[m] = (hiddenCols != null) ? hiddenCols.isVisible(m) : true;
      }

      SuperposeData[] structures = new SuperposeData[files.length];
      for (int f = 0; f < files.length; f++)
      {
        structures[f] = new SuperposeData(alignment.getWidth());
      }

      /*
       * Calculate the superposable alignment columns ('matched'), and the
       * corresponding structure residue positions (structures.pdbResNo)
       */
      int candidateRefStructure = findSuperposableResidues(alignment,
              matched, structures);
      if (refStructure < 0)
      {
        /*
         * If no reference structure was specified, pick the first one that has
         * a mapping in the alignment
         */
        refStructure = candidateRefStructure;
      }

      String[] selcom = new String[files.length];
      int nmatched = 0;
      for (boolean b : matched)
      {
        if (b)
        {
          nmatched++;
        }
      }
      if (nmatched < 4)
      {
        // TODO: bail out here because superposition illdefined?
      }

      /*
       * generate select statements to select regions to superimpose structures
       */
      {
        // TODO extract method to construct selection statements
        for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
        {
          String chainCd = ":" + structures[pdbfnum].chain;
          int lpos = -1;
          boolean run = false;
          StringBuilder molsel = new StringBuilder();
          molsel.append("{");
          for (int r = 0; r < matched.length; r++)
          {
            if (matched[r])
            {
              int pdbResNo = structures[pdbfnum].pdbResNo[r];
              if (lpos != pdbResNo - 1)
              {
                // discontinuity
                if (lpos != -1)
                {
                  molsel.append(lpos);
                  molsel.append(chainCd);
                  molsel.append("|");
                }
                run = false;
              }
              else
              {
                // continuous run - and lpos >-1
                if (!run)
                {
                  // at the beginning, so add dash
                  molsel.append(lpos);
                  molsel.append("-");
                }
                run = true;
              }
              lpos = pdbResNo;
            }
          }
          /*
           * add final selection phrase
           */
          if (lpos != -1)
          {
            molsel.append(lpos);
            molsel.append(chainCd);
            molsel.append("}");
          }
          if (molsel.length() > 1)
          {
            selcom[pdbfnum] = molsel.toString();
            selectioncom.append("((");
            selectioncom.append(selcom[pdbfnum].substring(1,
                    selcom[pdbfnum].length() - 1));
            selectioncom.append(" )& ");
            selectioncom.append(pdbfnum + 1);
            selectioncom.append(".1)");
            if (pdbfnum < files.length - 1)
            {
              selectioncom.append("|");
            }
          }
          else
          {
            selcom[pdbfnum] = null;
          }
        }
      }
      StringBuilder command = new StringBuilder(256);
      // command.append("set spinFps 10;\n");

      for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
      {
        if (pdbfnum == refStructure || selcom[pdbfnum] == null
                || selcom[refStructure] == null)
        {
          continue;
        }
        command.append("echo ");
        command.append("\"Superposing (");
        command.append(structures[pdbfnum].pdbId);
        command.append(") against reference (");
        command.append(structures[refStructure].pdbId);
        command.append(")\";\ncompare " + nSeconds);
        command.append("{");
        command.append(Integer.toString(1 + pdbfnum));
        command.append(".1} {");
        command.append(Integer.toString(1 + refStructure));
        // conformation=1 excludes alternate locations for CA (JAL-1757)
        command.append(".1} SUBSET {(*.CA | *.P) and conformation=1} ATOMS ");

        // for (int s = 0; s < 2; s++)
        // {
        // command.append(selcom[(s == 0 ? pdbfnum : refStructure)]);
        // }
        command.append(selcom[pdbfnum]);
        command.append(selcom[refStructure]);
        command.append(" ROTATE TRANSLATE;\n");
      }
      if (selectioncom.length() > 0)
      {
        // TODO is performing selectioncom redundant here? is done later on
        // System.out.println("Select regions:\n" + selectioncom.toString());
        evalStateCommand("select *; cartoons off; backbone; select ("
                + selectioncom.toString() + "); cartoons; ");
        // selcom.append("; ribbons; ");
        String cmdString = command.toString();
        // System.out.println("Superimpose command(s):\n" + cmdString);

        evalStateCommand(cmdString);
      }
    }
    if (selectioncom.length() > 0)
    {// finally, mark all regions that were superposed.
      if (selectioncom.substring(selectioncom.length() - 1).equals("|"))
      {
        selectioncom.setLength(selectioncom.length() - 1);
      }
      // System.out.println("Select regions:\n" + selectioncom.toString());
      evalStateCommand("select *; cartoons off; backbone; select ("
              + selectioncom.toString() + "); cartoons; ");
      // evalStateCommand("select *; backbone; select "+selcom.toString()+"; cartoons; center "+selcom.toString());
    }
  }

  public void evalStateCommand(String command)
  {
    jmolHistory(false);
    if (lastCommand == null || !lastCommand.equals(command))
    {
      viewer.evalStringQuiet(command + "\n");
    }
    jmolHistory(true);
    lastCommand = command;
  }

  /**
   * colour any structures associated with sequences in the given alignment
   * using the getFeatureRenderer() and getSequenceRenderer() renderers but only
   * if colourBySequence is enabled.
   */
  public void colourBySequence(AlignmentViewPanel alignmentv)
  {
    boolean showFeatures = alignmentv.getAlignViewport()
            .isShowSequenceFeatures();
    if (!colourBySequence || !isLoadingFinished())
    {
      return;
    }
    if (getSsm() == null)
    {
      return;
    }
    String[] files = getPdbFile();

    SequenceRenderer sr = getSequenceRenderer(alignmentv);

    FeatureRenderer fr = null;
    if (showFeatures)
    {
      fr = getFeatureRenderer(alignmentv);
    }
    AlignmentI alignment = alignmentv.getAlignment();

    for (jalview.structure.StructureMappingcommandSet cpdbbyseq : getColourBySequenceCommands(
            files, sr, fr, alignment))
    {
      for (String cbyseq : cpdbbyseq.commands)
      {
        executeWhenReady(cbyseq);
      }
    }
  }

  /**
   * @param files
   * @param sr
   * @param fr
   * @param alignment
   * @return
   */
  protected StructureMappingcommandSet[] getColourBySequenceCommands(
          String[] files, SequenceRenderer sr, FeatureRenderer fr,
          AlignmentI alignment)
  {
    return JmolCommands.getColourBySequenceCommand(getSsm(), files,
            getSequence(), sr, fr, alignment);
  }

  /**
   * @param command
   */
  protected void executeWhenReady(String command)
  {
    evalStateCommand(command);
  }

  public void createImage(String file, String type, int quality)
  {
    System.out.println("JMOL CREATE IMAGE");
  }

  @Override
  public String createImage(String fileName, String type,
          Object textOrBytes, int quality)
  {
    System.out.println("JMOL CREATE IMAGE");
    return null;
  }

  @Override
  public String eval(String strEval)
  {
    // System.out.println(strEval);
    // "# 'eval' is implemented only for the applet.";
    return null;
  }

  // End StructureListener
  // //////////////////////////

  @Override
  public float[][] functionXY(String functionName, int x, int y)
  {
    return null;
  }

  @Override
  public float[][][] functionXYZ(String functionName, int nx, int ny, int nz)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Color getColour(int atomIndex, int pdbResNum, String chain,
          String pdbfile)
  {
    if (getModelNum(pdbfile) < 0)
    {
      return null;
    }
    // TODO: verify atomIndex is selecting correct model.
    // return new Color(viewer.getAtomArgb(atomIndex)); Jmol 12.2.4
    int colour = viewer.ms.at[atomIndex].atomPropertyInt(T.color);
    return new Color(colour);
  }

  /**
   * returns the current featureRenderer that should be used to colour the
   * structures
   * 
   * @param alignment
   * 
   * @return
   */
  public abstract FeatureRenderer getFeatureRenderer(
          AlignmentViewPanel alignment);

  /**
   * instruct the Jalview binding to update the pdbentries vector if necessary
   * prior to matching the jmol view's contents to the list of structure files
   * Jalview knows about.
   */
  public abstract void refreshPdbEntries();

  private int getModelNum(String modelFileName)
  {
    String[] mfn = getPdbFile();
    if (mfn == null)
    {
      return -1;
    }
    for (int i = 0; i < mfn.length; i++)
    {
      if (mfn[i].equalsIgnoreCase(modelFileName))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * map between index of model filename returned from getPdbFile and the first
   * index of models from this file in the viewer. Note - this is not trimmed -
   * use getPdbFile to get number of unique models.
   */
  private int _modelFileNameMap[];

  // ////////////////////////////////
  // /StructureListener
  @Override
  public synchronized String[] getPdbFile()
  {
    if (viewer == null)
    {
      return new String[0];
    }
    if (modelFileNames == null)
    {
      List<String> mset = new ArrayList<String>();
      _modelFileNameMap = new int[viewer.ms.mc];
      String m = viewer.ms.getModelFileName(0);
      if (m != null)
      {
        String filePath = m;
        try
        {
          filePath = new File(m).getAbsolutePath();
        } catch (AccessControlException x)
        {
          // usually not allowed to do this in applet
          System.err
                  .println("jmolBinding: Using local file string from Jmol: "
                          + m);
        }
        if (filePath.indexOf("/file:") != -1)
        {
          // applet path with docroot - discard as format won't match pdbfile
          filePath = m;
        }
        mset.add(filePath);
        _modelFileNameMap[0] = 0; // filename index for first model is always 0.
      }
      int j = 1;
      for (int i = 1; i < viewer.ms.mc; i++)
      {
        m = viewer.ms.getModelFileName(i);
        String filePath = m;
        if (m != null)
        {
          try
          {
            filePath = new File(m).getAbsolutePath();
          } catch (AccessControlException x)
          {
            // usually not allowed to do this in applet, so keep raw handle
            // System.err.println("jmolBinding: Using local file string from Jmol: "+m);
          }
        }

        /*
         * add this model unless it is read from a structure file we have
         * already seen (example: 2MJW is an NMR structure with 10 models)
         */
        if (!mset.contains(filePath))
        {
          mset.add(filePath);
          _modelFileNameMap[j] = i; // record the model index for the filename
          j++;
        }
      }
      modelFileNames = mset.toArray(new String[mset.size()]);
    }
    return modelFileNames;
  }

  /**
   * map from string to applet
   */
  @Override
  public Map<String, Object> getRegistryInfo()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * returns the current sequenceRenderer that should be used to colour the
   * structures
   * 
   * @param alignment
   * 
   * @return
   */
  public abstract SequenceRenderer getSequenceRenderer(
          AlignmentViewPanel alignment);

  // ///////////////////////////////
  // JmolStatusListener

  public void handlePopupMenu(int x, int y)
  {
    // jmolpopup.show(x, y);
    // jmolpopup.jpiShow(x, y);
  }

  /**
   * Highlight zero, one or more atoms on the structure
   */
  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    if (atoms != null)
    {
      if (resetLastRes.length() > 0)
      {
        viewer.evalStringQuiet(resetLastRes.toString());
        resetLastRes.setLength(0);
      }
      for (AtomSpec atom : atoms)
      {
        highlightAtom(atom.getAtomIndex(), atom.getPdbResNum(),
                atom.getChain(), atom.getPdbFile());
      }
    }
  }

  // jmol/ssm only
  public void highlightAtom(int atomIndex, int pdbResNum, String chain,
          String pdbfile)
  {
    if (modelFileNames == null)
    {
      return;
    }

    // look up file model number for this pdbfile
    int mdlNum = 0;
    // may need to adjust for URLencoding here - we don't worry about that yet.
    while (mdlNum < modelFileNames.length
            && !pdbfile.equals(modelFileNames[mdlNum]))
    {
      mdlNum++;
    }
    if (mdlNum == modelFileNames.length)
    {
      return;
    }

    jmolHistory(false);

    StringBuilder cmd = new StringBuilder(64);
    cmd.append("select " + pdbResNum); // +modelNum

    resetLastRes.append("select " + pdbResNum); // +modelNum

    cmd.append(":");
    resetLastRes.append(":");
    if (!chain.equals(" "))
    {
      cmd.append(chain);
      resetLastRes.append(chain);
    }
    {
      cmd.append(" /" + (mdlNum + 1));
      resetLastRes.append("/" + (mdlNum + 1));
    }
    cmd.append(";wireframe 100;" + cmd.toString() + " and not hetero;");

    resetLastRes.append(";wireframe 0;" + resetLastRes.toString()
            + " and not hetero; spacefill 0;");

    cmd.append("spacefill 200;select none");

    viewer.evalStringQuiet(cmd.toString());
    jmolHistory(true);

  }

  boolean debug = true;

  private void jmolHistory(boolean enable)
  {
    viewer.evalStringQuiet("History " + ((debug || enable) ? "on" : "off"));
  }

  public void loadInline(String string)
  {
    loadedInline = true;
    // TODO: re JAL-623
    // viewer.loadInline(strModel, isAppend);
    // could do this:
    // construct fake fullPathName and fileName so we can identify the file
    // later.
    // Then, construct pass a reader for the string to Jmol.
    // ((org.jmol.Viewer.Viewer) viewer).loadModelFromFile(fullPathName,
    // fileName, null, reader, false, null, null, 0);
    viewer.openStringInline(string);
  }

  public void mouseOverStructure(int atomIndex, String strInfo)
  {
    int pdbResNum;
    int alocsep = strInfo.indexOf("^");
    int mdlSep = strInfo.indexOf("/");
    int chainSeparator = strInfo.indexOf(":"), chainSeparator1 = -1;

    if (chainSeparator == -1)
    {
      chainSeparator = strInfo.indexOf(".");
      if (mdlSep > -1 && mdlSep < chainSeparator)
      {
        chainSeparator1 = chainSeparator;
        chainSeparator = mdlSep;
      }
    }
    // handle insertion codes
    if (alocsep != -1)
    {
      pdbResNum = Integer.parseInt(strInfo.substring(
              strInfo.indexOf("]") + 1, alocsep));

    }
    else
    {
      pdbResNum = Integer.parseInt(strInfo.substring(
              strInfo.indexOf("]") + 1, chainSeparator));
    }
    String chainId;

    if (strInfo.indexOf(":") > -1)
    {
      chainId = strInfo.substring(strInfo.indexOf(":") + 1,
              strInfo.indexOf("."));
    }
    else
    {
      chainId = " ";
    }

    String pdbfilename = modelFileNames[frameNo]; // default is first or current
    // model
    if (mdlSep > -1)
    {
      if (chainSeparator1 == -1)
      {
        chainSeparator1 = strInfo.indexOf(".", mdlSep);
      }
      String mdlId = (chainSeparator1 > -1) ? strInfo.substring(mdlSep + 1,
              chainSeparator1) : strInfo.substring(mdlSep + 1);
      try
      {
        // recover PDB filename for the model hovered over.
        int _mp = _modelFileNameMap.length - 1, mnumber = new Integer(mdlId)
                .intValue() - 1;
        while (mnumber < _modelFileNameMap[_mp])
        {
          _mp--;
        }
        pdbfilename = modelFileNames[_mp];
        if (pdbfilename == null)
        {
          pdbfilename = new File(viewer.ms.getModelFileName(mnumber))
                  .getAbsolutePath();
        }

      } catch (Exception e)
      {
      }
      ;
    }
    if (lastMessage == null || !lastMessage.equals(strInfo))
    {
      getSsm().mouseOverStructure(pdbResNum, chainId, pdbfilename);
    }

    lastMessage = strInfo;
  }

  public void notifyAtomHovered(int atomIndex, String strInfo, String data)
  {
    if (data != null)
    {
      System.err.println("Ignoring additional hover info: " + data
              + " (other info: '" + strInfo + "' pos " + atomIndex + ")");
    }
    mouseOverStructure(atomIndex, strInfo);
  }

  /*
   * { if (history != null && strStatus != null &&
   * !strStatus.equals("Script completed")) { history.append("\n" + strStatus);
   * } }
   */

  public void notifyAtomPicked(int atomIndex, String strInfo, String strData)
  {
    /**
     * this implements the toggle label behaviour copied from the original
     * structure viewer, MCView
     */
    if (strData != null)
    {
      System.err.println("Ignoring additional pick data string " + strData);
    }
    int chainSeparator = strInfo.indexOf(":");
    int p = 0;
    if (chainSeparator == -1)
    {
      chainSeparator = strInfo.indexOf(".");
    }

    String picked = strInfo.substring(strInfo.indexOf("]") + 1,
            chainSeparator);
    String mdlString = "";
    if ((p = strInfo.indexOf(":")) > -1)
    {
      picked += strInfo.substring(p, strInfo.indexOf("."));
    }

    if ((p = strInfo.indexOf("/")) > -1)
    {
      mdlString += strInfo.substring(p, strInfo.indexOf(" #"));
    }
    picked = "((" + picked + ".CA" + mdlString + ")|(" + picked + ".P"
            + mdlString + "))";
    jmolHistory(false);

    if (!atomsPicked.contains(picked))
    {
      viewer.evalStringQuiet("select " + picked + ";label %n %r:%c");
      atomsPicked.addElement(picked);
    }
    else
    {
      viewer.evalString("select " + picked + ";label off");
      atomsPicked.removeElement(picked);
    }
    jmolHistory(true);
    // TODO: in application this happens
    //
    // if (scriptWindow != null)
    // {
    // scriptWindow.sendConsoleMessage(strInfo);
    // scriptWindow.sendConsoleMessage("\n");
    // }

  }

  @Override
  public void notifyCallback(CBK type, Object[] data)
  {
    try
    {
      switch (type)
      {
      case LOADSTRUCT:
        notifyFileLoaded((String) data[1], (String) data[2],
                (String) data[3], (String) data[4],
                ((Integer) data[5]).intValue());

        break;
      case PICK:
        notifyAtomPicked(((Integer) data[2]).intValue(), (String) data[1],
                (String) data[0]);
        // also highlight in alignment
      case HOVER:
        notifyAtomHovered(((Integer) data[2]).intValue(), (String) data[1],
                (String) data[0]);
        break;
      case SCRIPT:
        notifyScriptTermination((String) data[2],
                ((Integer) data[3]).intValue());
        break;
      case ECHO:
        sendConsoleEcho((String) data[1]);
        break;
      case MESSAGE:
        sendConsoleMessage((data == null) ? ((String) null)
                : (String) data[1]);
        break;
      case ERROR:
        // System.err.println("Ignoring error callback.");
        break;
      case SYNC:
      case RESIZE:
        refreshGUI();
        break;
      case MEASURE:

      case CLICK:
      default:
        System.err.println("Unhandled callback " + type + " "
                + data[1].toString());
        break;
      }
    } catch (Exception e)
    {
      System.err.println("Squashed Jmol callback handler error:");
      e.printStackTrace();
    }
  }

  @Override
  public boolean notifyEnabled(CBK callbackPick)
  {
    switch (callbackPick)
    {
    case ECHO:
    case LOADSTRUCT:
    case MEASURE:
    case MESSAGE:
    case PICK:
    case SCRIPT:
    case HOVER:
    case ERROR:
      return true;
    default:
      return false;
    }
  }

  // incremented every time a load notification is successfully handled -
  // lightweight mechanism for other threads to detect when they can start
  // referrring to new structures.
  private long loadNotifiesHandled = 0;

  public long getLoadNotifiesHandled()
  {
    return loadNotifiesHandled;
  }

  public void notifyFileLoaded(String fullPathName, String fileName2,
          String modelName, String errorMsg, int modelParts)
  {
    if (errorMsg != null)
    {
      fileLoadingError = errorMsg;
      refreshGUI();
      return;
    }
    // TODO: deal sensibly with models loaded inLine:
    // modelName will be null, as will fullPathName.

    // the rest of this routine ignores the arguments, and simply interrogates
    // the Jmol view to find out what structures it contains, and adds them to
    // the structure selection manager.
    fileLoadingError = null;
    String[] oldmodels = modelFileNames;
    modelFileNames = null;
    chainNames = new ArrayList<String>();
    chainFile = new Hashtable<String, String>();
    boolean notifyLoaded = false;
    String[] modelfilenames = getPdbFile();
    // first check if we've lost any structures
    if (oldmodels != null && oldmodels.length > 0)
    {
      int oldm = 0;
      for (int i = 0; i < oldmodels.length; i++)
      {
        for (int n = 0; n < modelfilenames.length; n++)
        {
          if (modelfilenames[n] == oldmodels[i])
          {
            oldmodels[i] = null;
            break;
          }
        }
        if (oldmodels[i] != null)
        {
          oldm++;
        }
      }
      if (oldm > 0)
      {
        String[] oldmfn = new String[oldm];
        oldm = 0;
        for (int i = 0; i < oldmodels.length; i++)
        {
          if (oldmodels[i] != null)
          {
            oldmfn[oldm++] = oldmodels[i];
          }
        }
        // deregister the Jmol instance for these structures - we'll add
        // ourselves again at the end for the current structure set.
        getSsm().removeStructureViewerListener(this, oldmfn);
      }
    }
    refreshPdbEntries();
    for (int modelnum = 0; modelnum < modelfilenames.length; modelnum++)
    {
      String fileName = modelfilenames[modelnum];
      boolean foundEntry = false;
      StructureFile pdb = null;
      String pdbfile = null;
      // model was probably loaded inline - so check the pdb file hashcode
      if (loadedInline)
      {
        // calculate essential attributes for the pdb data imported inline.
        // prolly need to resolve modelnumber properly - for now just use our
        // 'best guess'
        pdbfile = viewer.getData("" + (1 + _modelFileNameMap[modelnum])
                + ".0", "PDB");
      }
      // search pdbentries and sequences to find correct pdbentry for this
      // model
      for (int pe = 0; pe < getPdbCount(); pe++)
      {
        boolean matches = false;
        addSequence(pe, getSequence()[pe]);
        if (fileName == null)
        {
          if (false)
          // see JAL-623 - need method of matching pasted data up
          {
            pdb = getSsm().setMapping(getSequence()[pe], getChains()[pe],
                    pdbfile, AppletFormatAdapter.PASTE);
            getPdbEntry(modelnum).setFile("INLINE" + pdb.getId());
            matches = true;
            foundEntry = true;
          }
        }
        else
        {
          File fl = new File(getPdbEntry(pe).getFile());
          matches = fl.equals(new File(fileName));
          if (matches)
          {
            foundEntry = true;
            // TODO: Jmol can in principle retrieve from CLASSLOADER but
            // this
            // needs
            // to be tested. See mantis bug
            // https://mantis.lifesci.dundee.ac.uk/view.php?id=36605
            String protocol = AppletFormatAdapter.URL;
            try
            {
              if (fl.exists())
              {
                protocol = AppletFormatAdapter.FILE;
              }
            } catch (Exception e)
            {
            } catch (Error e)
            {
            }
            // Explicitly map to the filename used by Jmol ;
            pdb = getSsm().setMapping(getSequence()[pe], getChains()[pe],
                    fileName, protocol);
            // pdbentry[pe].getFile(), protocol);

          }
        }
        if (matches)
        {
          // add an entry for every chain in the model
          for (int i = 0; i < pdb.getChains().size(); i++)
          {
            String chid = new String(pdb.getId() + ":"
                    + pdb.getChains().elementAt(i).id);
            chainFile.put(chid, fileName);
            chainNames.add(chid);
          }
          notifyLoaded = true;
        }
      }

      if (!foundEntry && associateNewStructs)
      {
        // this is a foreign pdb file that jalview doesn't know about - add
        // it to the dataset and try to find a home - either on a matching
        // sequence or as a new sequence.
        String pdbcontent = viewer.getData("/" + (modelnum + 1) + ".1",
                "PDB");
        // parse pdb file into a chain, etc.
        // locate best match for pdb in associated views and add mapping to
        // ssm
        // if properly registered then
        notifyLoaded = true;

      }
    }
    // FILE LOADED OK
    // so finally, update the jmol bits and pieces
    // if (jmolpopup != null)
    // {
    // // potential for deadlock here:
    // // jmolpopup.updateComputedMenus();
    // }
    if (!isLoadingFromArchive())
    {
      viewer.evalStringQuiet("model *; select backbone;restrict;cartoon;wireframe off;spacefill off");
    }
    // register ourselves as a listener and notify the gui that it needs to
    // update itself.
    getSsm().addStructureViewerListener(this);
    if (notifyLoaded)
    {
      FeatureRenderer fr = getFeatureRenderer(null);
      if (fr != null)
      {
        fr.featuresAdded();
      }
      refreshGUI();
      loadNotifiesHandled++;
    }
    setLoadingFromArchive(false);
  }

  @Override
  public List<String> getChainNames()
  {
    return chainNames;
  }

  public void notifyNewPickingModeMeasurement(int iatom, String strMeasure)
  {
    notifyAtomPicked(iatom, strMeasure, null);
  }

  public abstract void notifyScriptTermination(String strStatus,
          int msWalltime);

  /**
   * display a message echoed from the jmol viewer
   * 
   * @param strEcho
   */
  public abstract void sendConsoleEcho(String strEcho); /*
                                                         * { showConsole(true);
                                                         * 
                                                         * history.append("\n" +
                                                         * strEcho); }
                                                         */

  // /End JmolStatusListener
  // /////////////////////////////

  /**
   * @param strStatus
   *          status message - usually the response received after a script
   *          executed
   */
  public abstract void sendConsoleMessage(String strStatus);

  @Override
  public void setCallbackFunction(String callbackType,
          String callbackFunction)
  {
    System.err.println("Ignoring set-callback request to associate "
            + callbackType + " with function " + callbackFunction);

  }

  public void setJalviewColourScheme(ColourSchemeI cs)
  {
    colourBySequence = false;

    if (cs == null)
    {
      return;
    }

    jmolHistory(false);
    StringBuilder command = new StringBuilder(128);
    command.append("select *;color white;");
    List<String> residueSet = ResidueProperties.getResidues(isNucleotide(),
            false);
    for (String res : residueSet)
    {
      Color col = cs.findColour(res.charAt(0));
      command.append("select " + res + ";color[" + col.getRed() + ","
              + col.getGreen() + "," + col.getBlue() + "];");
    }

    evalStateCommand(command.toString());
    jmolHistory(true);
  }

  public void showHelp()
  {
    showUrl("http://jmol.sourceforge.net/docs/JmolUserGuide/", "jmolHelp");
  }

  /**
   * open the URL somehow
   * 
   * @param target
   */
  public abstract void showUrl(String url, String target);

  /**
   * called when the binding thinks the UI needs to be refreshed after a Jmol
   * state change. this could be because structures were loaded, or because an
   * error has occured.
   */
  public abstract void refreshGUI();

  /**
   * called to show or hide the associated console window container.
   * 
   * @param show
   */
  public abstract void showConsole(boolean show);

  /**
   * @param renderPanel
   * @param jmolfileio
   *          - when true will initialise jmol's file IO system (should be false
   *          in applet context)
   * @param htmlName
   * @param documentBase
   * @param codeBase
   * @param commandOptions
   */
  public void allocateViewer(Container renderPanel, boolean jmolfileio,
          String htmlName, URL documentBase, URL codeBase,
          String commandOptions)
  {
    allocateViewer(renderPanel, jmolfileio, htmlName, documentBase,
            codeBase, commandOptions, null, null);
  }

  /**
   * 
   * @param renderPanel
   * @param jmolfileio
   *          - when true will initialise jmol's file IO system (should be false
   *          in applet context)
   * @param htmlName
   * @param documentBase
   * @param codeBase
   * @param commandOptions
   * @param consolePanel
   *          - panel to contain Jmol console
   * @param buttonsToShow
   *          - buttons to show on the console, in ordr
   */
  public void allocateViewer(Container renderPanel, boolean jmolfileio,
          String htmlName, URL documentBase, URL codeBase,
          String commandOptions, final Container consolePanel,
          String buttonsToShow)
  {
    if (commandOptions == null)
    {
      commandOptions = "";
    }
    viewer = (Viewer) JmolViewer.allocateViewer(renderPanel,
            (jmolfileio ? new SmarterJmolAdapter() : null), htmlName
                    + ((Object) this).toString(), documentBase, codeBase,
            commandOptions, this);

    viewer.setJmolStatusListener(this); // extends JmolCallbackListener

    console = createJmolConsole(consolePanel, buttonsToShow);
    if (consolePanel != null)
    {
      consolePanel.addComponentListener(this);

    }

  }

  protected abstract JmolAppConsoleInterface createJmolConsole(
          Container consolePanel, String buttonsToShow);

  protected org.jmol.api.JmolAppConsoleInterface console = null;

  public void setBackgroundColour(java.awt.Color col)
  {
    jmolHistory(false);
    viewer.evalStringQuiet("background [" + col.getRed() + ","
            + col.getGreen() + "," + col.getBlue() + "];");
    jmolHistory(true);
  }

  @Override
  public int[] resizeInnerPanel(String data)
  {
    // Jalview doesn't honour resize panel requests
    return null;
  }

  /**
   * 
   */
  protected void closeConsole()
  {
    if (console != null)
    {
      try
      {
        console.setVisible(false);
      } catch (Error e)
      {
      } catch (Exception x)
      {
      }
      ;
      console = null;
    }
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentMoved(ComponentEvent e)
  {
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentResized(ComponentEvent e)
  {
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentShown(ComponentEvent e)
  {
    showConsole(true);
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentHidden(ComponentEvent e)
  {
    showConsole(false);
  }
}
