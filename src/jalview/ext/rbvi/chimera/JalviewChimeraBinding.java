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
package jalview.ext.rbvi.chimera;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.httpserver.AbstractRequestHandler;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueProperties;
import jalview.structure.AtomSpec;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MessageManager;

import java.awt.Color;
import java.net.BindException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ext.edu.ucsf.rbvi.strucviz2.ChimeraManager;
import ext.edu.ucsf.rbvi.strucviz2.ChimeraModel;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager.ModelType;

public abstract class JalviewChimeraBinding extends AAStructureBindingModel
{
  // Chimera clause to exclude alternate locations in atom selection
  private static final String NO_ALTLOCS = "&~@.B-Z&~@.2-9";

  private static final String COLOURING_CHIMERA = MessageManager
          .getString("status.colouring_chimera");

  private static final boolean debug = false;

  private static final String PHOSPHORUS = "P";

  private static final String ALPHACARBON = "CA";

  /*
   * Object through which we talk to Chimera
   */
  private ChimeraManager viewer;

  /*
   * Object which listens to Chimera notifications
   */
  private AbstractRequestHandler chimeraListener;

  /*
   * set if chimera state is being restored from some source - instructs binding
   * not to apply default display style when structure set is updated for first
   * time.
   */
  private boolean loadingFromArchive = false;

  /*
   * flag to indicate if the Chimera viewer should ignore sequence colouring
   * events from the structure manager because the GUI is still setting up
   */
  private boolean loadingFinished = true;

  public String fileLoadingError;

  /*
   * Map of ChimeraModel objects keyed by PDB full local file name
   */
  private Map<String, List<ChimeraModel>> chimeraMaps = new LinkedHashMap<String, List<ChimeraModel>>();

  /*
   * the default or current model displayed if the model cannot be identified
   * from the selection message
   */
  private int frameNo = 0;

  private String lastCommand;

  private boolean loadedInline;

  /**
   * current set of model filenames loaded
   */
  String[] modelFileNames = null;

  String lastMousedOverAtomSpec;

  private List<String> lastReply;

  /*
   * incremented every time a load notification is successfully handled -
   * lightweight mechanism for other threads to detect when they can start
   * referring to new structures.
   */
  private long loadNotifiesHandled = 0;

  /**
   * Open a PDB structure file in Chimera and set up mappings from Jalview.
   * 
   * We check if the PDB model id is already loaded in Chimera, if so don't
   * reopen it. This is the case if Chimera has opened a saved session file.
   * 
   * @param pe
   * @return
   */
  public boolean openFile(PDBEntry pe)
  {
    String file = pe.getFile();
    try
    {
      List<ChimeraModel> modelsToMap = new ArrayList<ChimeraModel>();
      List<ChimeraModel> oldList = viewer.getModelList();
      boolean alreadyOpen = false;

      /*
       * If Chimera already has this model, don't reopen it, but do remap it.
       */
      for (ChimeraModel open : oldList)
      {
        if (open.getModelName().equals(pe.getId()))
        {
          alreadyOpen = true;
          modelsToMap.add(open);
        }
      }

      /*
       * If Chimera doesn't yet have this model, ask it to open it, and retrieve
       * the model name(s) added by Chimera.
       */
      if (!alreadyOpen)
      {
        viewer.openModel(file, pe.getId(), ModelType.PDB_MODEL);
        List<ChimeraModel> newList = viewer.getModelList();
        // JAL-1728 newList.removeAll(oldList) does not work
        for (ChimeraModel cm : newList)
        {
          if (cm.getModelName().equals(pe.getId()))
          {
            modelsToMap.add(cm);
          }
        }
      }

      chimeraMaps.put(file, modelsToMap);

      if (getSsm() != null)
      {
        getSsm().addStructureViewerListener(this);
        // ssm.addSelectionListener(this);
        FeatureRenderer fr = getFeatureRenderer(null);
        if (fr != null)
        {
          fr.featuresAdded();
        }
        refreshGUI();
      }
      return true;
    } catch (Exception q)
    {
      log("Exception when trying to open model " + file + "\n"
              + q.toString());
      q.printStackTrace();
    }
    return false;
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
  public JalviewChimeraBinding(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs, String[][] chains,
          String protocol)
  {
    super(ssm, pdbentry, sequenceIs, chains, protocol);
    viewer = new ChimeraManager(
            new ext.edu.ucsf.rbvi.strucviz2.StructureManager(true));
  }

  /**
   * Start a dedicated HttpServer to listen for Chimera notifications, and tell
   * it to start listening
   */
  public void startChimeraListener()
  {
    try
    {
      chimeraListener = new ChimeraListener(this);
      viewer.startListening(chimeraListener.getUri());
    } catch (BindException e)
    {
      System.err.println("Failed to start Chimera listener: "
              + e.getMessage());
    }
  }

  /**
   * Construct a title string for the viewer window based on the data Jalview
   * knows about
   * 
   * @param verbose
   * @return
   */
  public String getViewerTitle(boolean verbose)
  {
    return getViewerTitle("Chimera", verbose);
  }

  /**
   * Tells Chimera to display only the specified chains
   * 
   * @param toshow
   */
  public void showChains(List<String> toshow)
  {
    /*
     * Construct a chimera command like
     * 
     * ~display #*;~ribbon #*;ribbon :.A,:.B
     */
    StringBuilder cmd = new StringBuilder(64);
    boolean first = true;
    for (String chain : toshow)
    {
      if (!first)
      {
        cmd.append(",");
      }
      cmd.append(":.").append(chain);
      first = false;
    }

    /*
     * could append ";focus" to this command to resize the display to fill the
     * window, but it looks more helpful not to (easier to relate chains to the
     * whole)
     */
    final String command = "~display #*; ~ribbon #*; ribbon "
            + cmd.toString();
    sendChimeraCommand(command, false);
  }

  /**
   * Close down the Jalview viewer and listener, and (optionally) the associated
   * Chimera window.
   */
  public void closeViewer(boolean closeChimera)
  {
    getSsm().removeStructureViewerListener(this, this.getPdbFile());
    if (closeChimera)
    {
      viewer.exitChimera();
    }
    if (this.chimeraListener != null)
    {
      chimeraListener.shutdown();
      chimeraListener = null;
    }
    lastCommand = null;
    viewer = null;

    releaseUIResources();
  }

  public void colourByChain()
  {
    colourBySequence = false;
    sendAsynchronousCommand("rainbow chain", COLOURING_CHIMERA);
  }

  /**
   * Constructs and sends a Chimera command to colour by charge
   * <ul>
   * <li>Aspartic acid and Glutamic acid (negative charge) red</li>
   * <li>Lysine and Arginine (positive charge) blue</li>
   * <li>Cysteine - yellow</li>
   * <li>all others - white</li>
   * </ul>
   */
  public void colourByCharge()
  {
    colourBySequence = false;
    String command = "color white;color red ::ASP;color red ::GLU;color blue ::LYS;color blue ::ARG;color yellow ::CYS";
    sendAsynchronousCommand(command, COLOURING_CHIMERA);
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
    StringBuilder allComs = new StringBuilder(128);
    String[] files = getPdbFile();

    if (!waitForFileLoad(files))
    {
      return;
    }

    refreshPdbEntries();
    StringBuilder selectioncom = new StringBuilder(256);
    for (int a = 0; a < _alignment.length; a++)
    {
      int refStructure = _refStructure[a];
      AlignmentI alignment = _alignment[a];
      ColumnSelection hiddenCols = _hiddenCols[a];

      if (refStructure >= files.length)
      {
        System.err.println("Ignoring invalid reference structure value "
                + refStructure);
        refStructure = -1;
      }

      /*
       * 'matched' array will hold 'true' for visible alignment columns where
       * all sequences have a residue with a mapping to the PDB structure
       */
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
       * Generate select statements to select regions to superimpose structures
       */
      String[] selcom = new String[files.length];
      for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
      {
        String chainCd = "." + structures[pdbfnum].chain;
        int lpos = -1;
        boolean run = false;
        StringBuilder molsel = new StringBuilder();
        for (int r = 0; r < matched.length; r++)
        {
          if (matched[r])
          {
            int pdbResNum = structures[pdbfnum].pdbResNo[r];
            if (lpos != pdbResNum - 1)
            {
              /*
               * discontiguous - append last residue now
               */
              if (lpos != -1)
              {
                molsel.append(String.valueOf(lpos));
                molsel.append(chainCd);
                molsel.append(",");
              }
              run = false;
            }
            else
            {
              /*
               * extending a contiguous run
               */
              if (!run)
              {
                /*
                 * start the range selection
                 */
                molsel.append(String.valueOf(lpos));
                molsel.append("-");
              }
              run = true;
            }
            lpos = pdbResNum;
          }
        }

        /*
         * and terminate final selection
         */
        if (lpos != -1)
        {
          molsel.append(String.valueOf(lpos));
          molsel.append(chainCd);
        }
        if (molsel.length() > 1)
        {
          selcom[pdbfnum] = molsel.toString();
          selectioncom.append("#").append(String.valueOf(pdbfnum))
                  .append(":");
          selectioncom.append(selcom[pdbfnum]);
          selectioncom.append(" ");
          if (pdbfnum < files.length - 1)
          {
            selectioncom.append("| ");
          }
        }
        else
        {
          selcom[pdbfnum] = null;
        }
      }

      StringBuilder command = new StringBuilder(256);
      for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
      {
        if (pdbfnum == refStructure || selcom[pdbfnum] == null
                || selcom[refStructure] == null)
        {
          continue;
        }
        if (command.length() > 0)
        {
          command.append(";");
        }

        /*
         * Form Chimera match command, from the 'new' structure to the
         * 'reference' structure e.g. (50 residues, chain B/A, alphacarbons):
         * 
         * match #1:1-30.B,81-100.B@CA #0:21-40.A,61-90.A@CA
         * 
         * @see
         * https://www.cgl.ucsf.edu/chimera/docs/UsersGuide/midas/match.html
         */
        command.append("match ").append(getModelSpec(pdbfnum)).append(":");
        command.append(selcom[pdbfnum]);
        command.append("@").append(
                structures[pdbfnum].isRna ? PHOSPHORUS : ALPHACARBON);
        // JAL-1757 exclude alternate CA locations
        command.append(NO_ALTLOCS);
        command.append(" ").append(getModelSpec(refStructure)).append(":");
        command.append(selcom[refStructure]);
        command.append("@").append(
                structures[refStructure].isRna ? PHOSPHORUS : ALPHACARBON);
        command.append(NO_ALTLOCS);
      }
      if (selectioncom.length() > 0)
      {
        if (debug)
        {
          System.out.println("Select regions:\n" + selectioncom.toString());
          System.out.println("Superimpose command(s):\n"
                  + command.toString());
        }
        allComs.append("~display all; chain @CA|P; ribbon ")
                .append(selectioncom.toString())
                .append(";" + command.toString());
      }
    }
    if (selectioncom.length() > 0)
    {
      // TODO: visually distinguish regions that were superposed
      if (selectioncom.substring(selectioncom.length() - 1).equals("|"))
      {
        selectioncom.setLength(selectioncom.length() - 1);
      }
      if (debug)
      {
        System.out.println("Select regions:\n" + selectioncom.toString());
      }
      allComs.append("; ~display all; chain @CA|P; ribbon ")
              .append(selectioncom.toString()).append("; focus");
      sendChimeraCommand(allComs.toString(), false);
    }

  }

  /**
   * Helper method to construct model spec in Chimera format:
   * <ul>
   * <li>#0 (#1 etc) for a PDB file with no sub-models</li>
   * <li>#0.1 (#1.1 etc) for a PDB file with sub-models</li>
   * <ul>
   * Note for now we only ever choose the first of multiple models. This
   * corresponds to the hard-coded Jmol equivalent (compare {1.1}). Refactor in
   * future if there is a need to select specific sub-models.
   * 
   * @param pdbfnum
   * @return
   */
  protected String getModelSpec(int pdbfnum)
  {
    if (pdbfnum < 0 || pdbfnum >= getPdbCount())
    {
      return "";
    }

    /*
     * For now, the test for having sub-models is whether multiple Chimera
     * models are mapped for the PDB file; the models are returned as a response
     * to the Chimera command 'list models type molecule', see
     * ChimeraManager.getModelList().
     */
    List<ChimeraModel> maps = chimeraMaps.get(getPdbFile()[pdbfnum]);
    boolean hasSubModels = maps != null && maps.size() > 1;
    return "#" + String.valueOf(pdbfnum) + (hasSubModels ? ".1" : "");
  }

  /**
   * Launch Chimera, unless an instance linked to this object is already
   * running. Returns true if chimera is successfully launched, or already
   * running, else false.
   * 
   * @return
   */
  public boolean launchChimera()
  {
    if (!viewer.isChimeraLaunched())
    {
      return viewer.launchChimera(StructureManager.getChimeraPaths());
    }
    if (viewer.isChimeraLaunched())
    {
      return true;
    }
    log("Failed to launch Chimera!");
    return false;
  }

  /**
   * Answers true if the Chimera process is still running, false if ended or not
   * started.
   * 
   * @return
   */
  public boolean isChimeraRunning()
  {
    return viewer.isChimeraLaunched();
  }

  /**
   * Send a command to Chimera, and optionally log any responses.
   * 
   * @param command
   * @param logResponse
   */
  public void sendChimeraCommand(final String command, boolean logResponse)
  {
    if (viewer == null)
    {
      // ? thread running after viewer shut down
      return;
    }
    viewerCommandHistory(false);
    if (lastCommand == null || !lastCommand.equals(command))
    {
      // trim command or it may never find a match in the replyLog!!
      lastReply = viewer.sendChimeraCommand(command.trim(), logResponse);
      if (logResponse && debug)
      {
        log("Response from command ('" + command + "') was:\n" + lastReply);
      }
    }
    viewerCommandHistory(true);
    lastCommand = command;
  }

  /**
   * Send a Chimera command asynchronously in a new thread. If the progress
   * message is not null, display this message while the command is executing.
   * 
   * @param command
   * @param progressMsg
   */
  protected abstract void sendAsynchronousCommand(String command,
          String progressMsg);

  /**
   * colour any structures associated with sequences in the given alignment
   * using the getFeatureRenderer() and getSequenceRenderer() renderers but only
   * if colourBySequence is enabled.
   */
  public void colourBySequence(boolean showFeatures,
          jalview.api.AlignmentViewPanel alignmentv)
  {
    if (!colourBySequence || !loadingFinished)
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
      for (String command : cpdbbyseq.commands)
      {
        sendAsynchronousCommand(command, COLOURING_CHIMERA);
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
    return ChimeraCommands.getColourBySequenceCommand(getSsm(), files,
            getSequence(), sr, fr, alignment);
  }

  /**
   * @param command
   */
  protected void executeWhenReady(String command)
  {
    waitForChimera();
    sendChimeraCommand(command, false);
    waitForChimera();
  }

  private void waitForChimera()
  {
    while (viewer != null && viewer.isBusy())
    {
      try
      {
        Thread.sleep(15);
      } catch (InterruptedException q)
      {
      }
    }
  }

  // End StructureListener
  // //////////////////////////

  public Color getColour(int atomIndex, int pdbResNum, String chain,
          String pdbfile)
  {
    if (getModelNum(pdbfile) < 0)
    {
      return null;
    }
    log("get model / residue colour attribute unimplemented");
    return null;
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
   * prior to matching the viewer's contents to the list of structure files
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
    // if (modelFileNames == null)
    // {
    // Collection<ChimeraModel> chimodels = viewer.getChimeraModels();
    // _modelFileNameMap = new int[chimodels.size()];
    // int j = 0;
    // for (ChimeraModel chimodel : chimodels)
    // {
    // String mdlName = chimodel.getModelName();
    // }
    // modelFileNames = new String[j];
    // // System.arraycopy(mset, 0, modelFileNames, 0, j);
    // }

    return chimeraMaps.keySet().toArray(
            modelFileNames = new String[chimeraMaps.size()]);
  }

  /**
   * map from string to applet
   */
  public Map getRegistryInfo()
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

  /**
   * Construct and send a command to highlight zero, one or more atoms.
   * 
   * <pre>
   * Done by generating a command like (to 'highlight' position 44)
   *   show #0:44.C
   * </pre>
   */
  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    if (atoms == null)
    {
      return;
    }
    StringBuilder atomSpecs = new StringBuilder();
    boolean first = true;
    for (AtomSpec atom : atoms)
    {
      int pdbResNum = atom.getPdbResNum();
      String chain = atom.getChain();
      String pdbfile = atom.getPdbFile();
      List<ChimeraModel> cms = chimeraMaps.get(pdbfile);
      if (cms != null && !cms.isEmpty())
      {
        /*
         * Formatting as #0:34.A,#1:33.A doesn't work as desired, so instead we
         * concatenate multiple 'show' commands
         */
        atomSpecs.append(first ? "" : ";show ");
        first = false;
        atomSpecs.append("#" + cms.get(0).getModelNumber());
        atomSpecs.append(":" + pdbResNum);
        if (!chain.equals(" "))
        {
          atomSpecs.append("." + chain);
        }
      }
    }
    String atomSpec = atomSpecs.toString();

    /*
     * Avoid repeated commands for the same residue
     */
    if (atomSpec.equals(lastMousedOverAtomSpec))
    {
      return;
    }

    StringBuilder command = new StringBuilder(32);
    viewerCommandHistory(false);
    if (atomSpec.length() > 0)
    {
      command.append("show ").append(atomSpec);
      viewer.sendChimeraCommand(command.toString(), false);
    }
    viewerCommandHistory(true);
    this.lastMousedOverAtomSpec = atomSpec;
  }

  /**
   * Query Chimera for its current selection, and highlight it on the alignment
   */
  public void highlightChimeraSelection()
  {
    /*
     * Ask Chimera for its current selection
     */
    List<String> selection = viewer.getSelectedResidueSpecs();

    /*
     * Parse model number, residue and chain for each selected position,
     * formatted as #0:123.A or #1.2:87.B (#model.submodel:residue.chain)
     */
    List<AtomSpec> atomSpecs = new ArrayList<AtomSpec>();
    for (String atomSpec : selection)
    {
      int colonPos = atomSpec.indexOf(":");
      if (colonPos == -1)
      {
        continue; // malformed
      }

      int hashPos = atomSpec.indexOf("#");
      String modelSubmodel = atomSpec.substring(hashPos + 1, colonPos);
      int dotPos = modelSubmodel.indexOf(".");
      int modelId = 0;
      try
      {
        modelId = Integer.valueOf(dotPos == -1 ? modelSubmodel
                : modelSubmodel.substring(0, dotPos));
      } catch (NumberFormatException e)
      {
        // ignore, default to model 0
      }

      String residueChain = atomSpec.substring(colonPos + 1);
      dotPos = residueChain.indexOf(".");
      int pdbResNum = Integer.parseInt(dotPos == -1 ? residueChain
              : residueChain.substring(0, dotPos));

      String chainId = dotPos == -1 ? "" : residueChain
              .substring(dotPos + 1);

      /*
       * Work out the pdbfilename from the model number
       */
      String pdbfilename = modelFileNames[frameNo];
      findfileloop: for (String pdbfile : this.chimeraMaps.keySet())
      {
        for (ChimeraModel cm : chimeraMaps.get(pdbfile))
        {
          if (cm.getModelNumber() == modelId)
          {
            pdbfilename = pdbfile;
            break findfileloop;
          }
        }
      }
      atomSpecs.add(new AtomSpec(pdbfilename, chainId, pdbResNum, 0));
    }

    /*
     * Broadcast the selection (which may be empty, if the user just cleared all
     * selections)
     */
    getSsm().mouseOverStructure(atomSpecs);
  }

  private void log(String message)
  {
    System.err.println("## Chimera log: " + message);
  }

  private void viewerCommandHistory(boolean enable)
  {
    // log("(Not yet implemented) History "
    // + ((debug || enable) ? "on" : "off"));
  }

  public long getLoadNotifiesHandled()
  {
    return loadNotifiesHandled;
  }

  public void setJalviewColourScheme(ColourSchemeI cs)
  {
    colourBySequence = false;

    if (cs == null)
    {
      return;
    }

    // Chimera expects RBG values in the range 0-1
    final double normalise = 255D;
    viewerCommandHistory(false);
    StringBuilder command = new StringBuilder(128);

    List<String> residueSet = ResidueProperties.getResidues(isNucleotide(),
            false);
    for (String res : residueSet)
    {
      Color col = cs.findColour(res.charAt(0));
      command.append("color " + col.getRed() / normalise + ","
              + col.getGreen() / normalise + "," + col.getBlue()
              / normalise + " ::" + res + ";");
    }

    sendAsynchronousCommand(command.toString(), COLOURING_CHIMERA);
    viewerCommandHistory(true);
  }

  /**
   * called when the binding thinks the UI needs to be refreshed after a Chimera
   * state change. this could be because structures were loaded, or because an
   * error has occurred.
   */
  public abstract void refreshGUI();

  public void setLoadingFromArchive(boolean loadingFromArchive)
  {
    this.loadingFromArchive = loadingFromArchive;
  }

  /**
   * 
   * @return true if Chimeral is still restoring state or loading is still going
   *         on (see setFinsihedLoadingFromArchive)
   */
  public boolean isLoadingFromArchive()
  {
    return loadingFromArchive && !loadingFinished;
  }

  /**
   * modify flag which controls if sequence colouring events are honoured by the
   * binding. Should be true for normal operation
   * 
   * @param finishedLoading
   */
  public void setFinishedLoadingFromArchive(boolean finishedLoading)
  {
    loadingFinished = finishedLoading;
  }

  /**
   * Send the Chimera 'background solid <color>" command.
   * 
   * @see https 
   *      ://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/background
   *      .html
   * @param col
   */
  public void setBackgroundColour(Color col)
  {
    viewerCommandHistory(false);
    double normalise = 255D;
    final String command = "background solid " + col.getRed() / normalise
            + "," + col.getGreen() / normalise + "," + col.getBlue()
            / normalise + ";";
    viewer.sendChimeraCommand(command, false);
    viewerCommandHistory(true);
  }

  /**
   * Ask Chimera to save its session to the given file. Returns true if
   * successful, else false.
   * 
   * @param filepath
   * @return
   */
  public boolean saveSession(String filepath)
  {
    if (isChimeraRunning())
    {
      List<String> reply = viewer.sendChimeraCommand("save " + filepath,
              true);
      if (reply.contains("Session written"))
      {
        return true;
      }
      else
      {
        Cache.log
                .error("Error saving Chimera session: " + reply.toString());
      }
    }
    return false;
  }

  /**
   * Ask Chimera to open a session file. Returns true if successful, else false.
   * The filename must have a .py extension for this command to work.
   * 
   * @param filepath
   * @return
   */
  public boolean openSession(String filepath)
  {
    sendChimeraCommand("open " + filepath, true);
    // todo: test for failure - how?
    return true;
  }

  /**
   * Returns a list of chains mapped in this viewer. Note this list is not
   * currently scoped per structure.
   * 
   * @return
   */
  public List<String> getChainNames()
  {
    List<String> names = new ArrayList<String>();
    String[][] allNames = getChains();
    if (allNames != null)
    {
      for (String[] chainsForPdb : allNames)
      {
        if (chainsForPdb != null)
        {
          for (String chain : chainsForPdb)
          {
            if (chain != null && !names.contains(chain))
            {
              names.add(chain);
            }
          }
        }
      }
    }
    return names;
  }

  /**
   * Send a 'focus' command to Chimera to recentre the visible display
   */
  public void focusView()
  {
    sendChimeraCommand("focus", false);
  }
}
