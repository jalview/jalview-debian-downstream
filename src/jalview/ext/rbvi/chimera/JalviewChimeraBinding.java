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
package jalview.ext.rbvi.chimera;

import jalview.api.AlignmentViewPanel;
import jalview.api.SequenceRenderer;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.httpserver.AbstractRequestHandler;
import jalview.io.DataSourceType;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueProperties;
import jalview.structure.AtomSpec;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MessageManager;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ext.edu.ucsf.rbvi.strucviz2.ChimeraManager;
import ext.edu.ucsf.rbvi.strucviz2.ChimeraModel;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager.ModelType;

public abstract class JalviewChimeraBinding extends AAStructureBindingModel
{
  public static final String CHIMERA_FEATURE_GROUP = "Chimera";

  // Chimera clause to exclude alternate locations in atom selection
  private static final String NO_ALTLOCS = "&~@.B-Z&~@.2-9";

  private static final String COLOURING_CHIMERA = MessageManager
          .getString("status.colouring_chimera");

  private static final boolean debug = false;

  private static final String PHOSPHORUS = "P";

  private static final String ALPHACARBON = "CA";

  private List<String> chainNames = new ArrayList<String>();

  private Hashtable<String, String> chainFile = new Hashtable<String, String>();

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

  /*
   * Map of ChimeraModel objects keyed by PDB full local file name
   */
  private Map<String, List<ChimeraModel>> chimeraMaps = new LinkedHashMap<String, List<ChimeraModel>>();

  String lastHighlightCommand;

  /*
   * incremented every time a load notification is successfully handled -
   * lightweight mechanism for other threads to detect when they can start
   * referring to new structures.
   */
  private long loadNotifiesHandled = 0;

  private Thread chimeraMonitor;

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
   * @param protocol
   */
  public JalviewChimeraBinding(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs,
          DataSourceType protocol)
  {
    super(ssm, pdbentry, sequenceIs, protocol);
    viewer = new ChimeraManager(new StructureManager(true));
  }

  /**
   * Starts a thread that waits for the Chimera process to finish, so that we
   * can then close the associated resources. This avoids leaving orphaned
   * Chimera viewer panels in Jalview if the user closes Chimera.
   */
  protected void startChimeraProcessMonitor()
  {
    final Process p = viewer.getChimeraProcess();
    chimeraMonitor = new Thread(new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          p.waitFor();
          JalviewStructureDisplayI display = getViewer();
          if (display != null)
          {
            display.closeViewer(false);
          }
        } catch (InterruptedException e)
        {
          // exit thread if Chimera Viewer is closed in Jalview
        }
      }
    });
    chimeraMonitor.start();
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
      System.err.println(
              "Failed to start Chimera listener: " + e.getMessage());
    }
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
      int modelNumber = getModelNoForChain(chain);
      String showChainCmd = modelNumber == -1 ? ""
              : modelNumber + ":." + chain.split(":")[1];
      if (!first)
      {
        cmd.append(",");
      }
      cmd.append(showChainCmd);
      first = false;
    }

    /*
     * could append ";focus" to this command to resize the display to fill the
     * window, but it looks more helpful not to (easier to relate chains to the
     * whole)
     */
    final String command = "~display #*; ~ribbon #*; ribbon :"
            + cmd.toString();
    sendChimeraCommand(command, false);
  }

  /**
   * Close down the Jalview viewer and listener, and (optionally) the associated
   * Chimera window.
   */
  public void closeViewer(boolean closeChimera)
  {
    getSsm().removeStructureViewerListener(this, this.getStructureFiles());
    if (closeChimera)
    {
      viewer.exitChimera();
    }
    if (this.chimeraListener != null)
    {
      chimeraListener.shutdown();
      chimeraListener = null;
    }
    viewer = null;

    if (chimeraMonitor != null)
    {
      chimeraMonitor.interrupt();
    }
    releaseUIResources();
  }

  @Override
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
  @Override
  public void colourByCharge()
  {
    colourBySequence = false;
    String command = "color white;color red ::ASP;color red ::GLU;color blue ::LYS;color blue ::ARG;color yellow ::CYS";
    sendAsynchronousCommand(command, COLOURING_CHIMERA);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String superposeStructures(AlignmentI[] _alignment,
          int[] _refStructure, HiddenColumns[] _hiddenCols)
  {
    StringBuilder allComs = new StringBuilder(128);
    String[] files = getStructureFiles();

    if (!waitForFileLoad(files))
    {
      return null;
    }

    refreshPdbEntries();
    StringBuilder selectioncom = new StringBuilder(256);
    for (int a = 0; a < _alignment.length; a++)
    {
      int refStructure = _refStructure[a];
      AlignmentI alignment = _alignment[a];
      HiddenColumns hiddenCols = _hiddenCols[a];

      if (refStructure >= files.length)
      {
        System.err.println("Ignoring invalid reference structure value "
                + refStructure);
        refStructure = -1;
      }

      /*
       * 'matched' bit i will be set for visible alignment columns i where
       * all sequences have a residue with a mapping to the PDB structure
       */
      BitSet matched = new BitSet();
      for (int m = 0; m < alignment.getWidth(); m++)
      {
        if (hiddenCols == null || hiddenCols.isVisible(m))
        {
          matched.set(m);
        }
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

      int nmatched = matched.cardinality();
      if (nmatched < 4)
      {
        return MessageManager.formatMessage("label.insufficient_residues",
                nmatched);
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

        int nextColumnMatch = matched.nextSetBit(0);
        while (nextColumnMatch != -1)
        {
          int pdbResNum = structures[pdbfnum].pdbResNo[nextColumnMatch];
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
          nextColumnMatch = matched.nextSetBit(nextColumnMatch + 1);
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
          System.out.println(
                  "Superimpose command(s):\n" + command.toString());
        }
        allComs.append("~display all; chain @CA|P; ribbon ")
                .append(selectioncom.toString())
                .append(";" + command.toString());
      }
    }

    String error = null;
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
      List<String> chimeraReplies = sendChimeraCommand(allComs.toString(),
              true);
      for (String reply : chimeraReplies)
      {
        if (reply.toLowerCase().contains("unequal numbers of atoms"))
        {
          error = reply;
        }
      }
    }
    return error;
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
    List<ChimeraModel> maps = chimeraMaps.get(getStructureFiles()[pdbfnum]);
    boolean hasSubModels = maps != null && maps.size() > 1;
    return "#" + String.valueOf(pdbfnum) + (hasSubModels ? ".1" : "");
  }

  /**
   * Launch Chimera, unless an instance linked to this object is already
   * running. Returns true if Chimera is successfully launched, or already
   * running, else false.
   * 
   * @return
   */
  public boolean launchChimera()
  {
    if (viewer.isChimeraLaunched())
    {
      return true;
    }

    boolean launched = viewer
            .launchChimera(StructureManager.getChimeraPaths());
    if (launched)
    {
      startChimeraProcessMonitor();
    }
    else
    {
      log("Failed to launch Chimera!");
    }
    return launched;
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
   * Send a command to Chimera, and optionally log and return any responses.
   * <p>
   * Does nothing, and returns null, if the command is the same as the last one
   * sent [why?].
   * 
   * @param command
   * @param getResponse
   */
  public List<String> sendChimeraCommand(final String command,
          boolean getResponse)
  {
    if (viewer == null)
    {
      // ? thread running after viewer shut down
      return null;
    }
    List<String> reply = null;
    viewerCommandHistory(false);
    if (true /*lastCommand == null || !lastCommand.equals(command)*/)
    {
      // trim command or it may never find a match in the replyLog!!
      List<String> lastReply = viewer.sendChimeraCommand(command.trim(),
              getResponse);
      if (getResponse)
      {
        reply = lastReply;
        if (debug)
        {
          log("Response from command ('" + command + "') was:\n"
                  + lastReply);
        }
      }
    }
    viewerCommandHistory(true);

    return reply;
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
   * Sends a set of colour commands to the structure viewer
   * 
   * @param colourBySequenceCommands
   */
  @Override
  protected void colourBySequence(
          StructureMappingcommandSet[] colourBySequenceCommands)
  {
    for (StructureMappingcommandSet cpdbbyseq : colourBySequenceCommands)
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
   * @param viewPanel
   * @return
   */
  @Override
  protected StructureMappingcommandSet[] getColourBySequenceCommands(
          String[] files, SequenceRenderer sr, AlignmentViewPanel viewPanel)
  {
    return ChimeraCommands.getColourBySequenceCommand(getSsm(), files,
            getSequence(), sr, viewPanel);
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

  /**
   * instruct the Jalview binding to update the pdbentries vector if necessary
   * prior to matching the viewer's contents to the list of structure files
   * Jalview knows about.
   */
  public abstract void refreshPdbEntries();

  /**
   * map between index of model filename returned from getPdbFile and the first
   * index of models from this file in the viewer. Note - this is not trimmed -
   * use getPdbFile to get number of unique models.
   */
  private int _modelFileNameMap[];

  // ////////////////////////////////
  // /StructureListener
  @Override
  public synchronized String[] getStructureFiles()
  {
    if (viewer == null)
    {
      return new String[0];
    }

    return chimeraMaps.keySet()
            .toArray(modelFileNames = new String[chimeraMaps.size()]);
  }

  /**
   * Construct and send a command to highlight zero, one or more atoms. We do
   * this by sending an "rlabel" command to show the residue label at that
   * position.
   */
  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    if (atoms == null || atoms.size() == 0)
    {
      return;
    }

    StringBuilder cmd = new StringBuilder(128);
    boolean first = true;
    boolean found = false;

    for (AtomSpec atom : atoms)
    {
      int pdbResNum = atom.getPdbResNum();
      String chain = atom.getChain();
      String pdbfile = atom.getPdbFile();
      List<ChimeraModel> cms = chimeraMaps.get(pdbfile);
      if (cms != null && !cms.isEmpty())
      {
        if (first)
        {
          cmd.append("rlabel #").append(cms.get(0).getModelNumber())
                  .append(":");
        }
        else
        {
          cmd.append(",");
        }
        first = false;
        cmd.append(pdbResNum);
        if (!chain.equals(" "))
        {
          cmd.append(".").append(chain);
        }
        found = true;
      }
    }
    String command = cmd.toString();

    /*
     * avoid repeated commands for the same residue
     */
    if (command.equals(lastHighlightCommand))
    {
      return;
    }

    /*
     * unshow the label for the previous residue
     */
    if (lastHighlightCommand != null)
    {
      viewer.sendChimeraCommand("~" + lastHighlightCommand, false);
    }
    if (found)
    {
      viewer.sendChimeraCommand(command, false);
    }
    this.lastHighlightCommand = command;
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
    List<AtomSpec> atomSpecs = convertStructureResiduesToAlignment(
            selection);

    /*
     * Broadcast the selection (which may be empty, if the user just cleared all
     * selections)
     */
    getSsm().mouseOverStructure(atomSpecs);
  }

  /**
   * Converts a list of Chimera atomspecs to a list of AtomSpec representing the
   * corresponding residues (if any) in Jalview
   * 
   * @param structureSelection
   * @return
   */
  protected List<AtomSpec> convertStructureResiduesToAlignment(
          List<String> structureSelection)
  {
    List<AtomSpec> atomSpecs = new ArrayList<AtomSpec>();
    for (String atomSpec : structureSelection)
    {
      try
      {
        AtomSpec spec = AtomSpec.fromChimeraAtomspec(atomSpec);
        String pdbfilename = getPdbFileForModel(spec.getModelNumber());
        spec.setPdbFile(pdbfilename);
        atomSpecs.add(spec);
      } catch (IllegalArgumentException e)
      {
        System.err.println("Failed to parse atomspec: " + atomSpec);
      }
    }
    return atomSpecs;
  }

  /**
   * @param modelId
   * @return
   */
  protected String getPdbFileForModel(int modelId)
  {
    /*
     * Work out the pdbfilename from the model number
     */
    String pdbfilename = modelFileNames[0];
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
    return pdbfilename;
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

  @Override
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
    for (String resName : residueSet)
    {
      char res = resName.length() == 3
              ? ResidueProperties.getSingleCharacterCode(resName)
              : resName.charAt(0);
      Color col = cs.findColour(res, 0, null, null, 0f);
      command.append("color " + col.getRed() / normalise + ","
              + col.getGreen() / normalise + "," + col.getBlue() / normalise
              + " ::" + resName + ";");
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

  @Override
  public void setLoadingFromArchive(boolean loadingFromArchive)
  {
    this.loadingFromArchive = loadingFromArchive;
  }

  /**
   * 
   * @return true if Chimeral is still restoring state or loading is still going
   *         on (see setFinsihedLoadingFromArchive)
   */
  @Override
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
  @Override
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
  @Override
  public void setBackgroundColour(Color col)
  {
    viewerCommandHistory(false);
    double normalise = 255D;
    final String command = "background solid " + col.getRed() / normalise
            + "," + col.getGreen() / normalise + ","
            + col.getBlue() / normalise + ";";
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
  @Override
  public List<String> getChainNames()
  {
    return chainNames;
  }

  /**
   * Send a 'focus' command to Chimera to recentre the visible display
   */
  public void focusView()
  {
    sendChimeraCommand("focus", false);
  }

  /**
   * Send a 'show' command for all atoms in the currently selected columns
   * 
   * TODO: pull up to abstract structure viewer interface
   * 
   * @param vp
   */
  public void highlightSelection(AlignmentViewPanel vp)
  {
    List<Integer> cols = vp.getAlignViewport().getColumnSelection()
            .getSelected();
    AlignmentI alignment = vp.getAlignment();
    StructureSelectionManager sm = getSsm();
    for (SequenceI seq : alignment.getSequences())
    {
      /*
       * convert selected columns into sequence positions
       */
      int[] positions = new int[cols.size()];
      int i = 0;
      for (Integer col : cols)
      {
        positions[i++] = seq.findPosition(col);
      }
      sm.highlightStructure(this, seq, positions);
    }
  }

  /**
   * Constructs and send commands to Chimera to set attributes on residues for
   * features visible in Jalview
   * 
   * @param avp
   * @return
   */
  public int sendFeaturesToViewer(AlignmentViewPanel avp)
  {
    // TODO refactor as required to pull up to an interface
    AlignmentI alignment = avp.getAlignment();

    String[] files = getStructureFiles();
    if (files == null)
    {
      return 0;
    }

    StructureMappingcommandSet commandSet = ChimeraCommands
            .getSetAttributeCommandsForFeatures(getSsm(), files,
                    getSequence(), avp);
    String[] commands = commandSet.commands;
    if (commands.length > 10)
    {
      sendCommandsByFile(commands);
    }
    else
    {
      for (String command : commands)
      {
        sendAsynchronousCommand(command, null);
      }
    }
    return commands.length;
  }

  /**
   * Write commands to a temporary file, and send a command to Chimera to open
   * the file as a commands script. For use when sending a large number of
   * separate commands would overload the REST interface mechanism.
   * 
   * @param commands
   */
  protected void sendCommandsByFile(String[] commands)
  {
    try
    {
      File tmp = File.createTempFile("chim", ".com");
      tmp.deleteOnExit();
      PrintWriter out = new PrintWriter(new FileOutputStream(tmp));
      for (String command : commands)
      {
        out.println(command);
      }
      out.flush();
      out.close();
      String path = tmp.getAbsolutePath();
      sendAsynchronousCommand("open cmd:" + path, null);
    } catch (IOException e)
    {
      System.err.println("Sending commands to Chimera via file failed with "
              + e.getMessage());
    }
  }

  /**
   * Get Chimera residues which have the named attribute, find the mapped
   * positions in the Jalview sequence(s), and set as sequence features
   * 
   * @param attName
   * @param alignmentPanel
   */
  public void copyStructureAttributesToFeatures(String attName,
          AlignmentViewPanel alignmentPanel)
  {
    // todo pull up to AAStructureBindingModel (and interface?)

    /*
     * ask Chimera to list residues with the attribute, reporting its value
     */
    // this alternative command
    // list residues spec ':*/attName' attr attName
    // doesn't report 'None' values (which is good), but
    // fails for 'average.bfactor' (which is bad):

    String cmd = "list residues attr '" + attName + "'";
    List<String> residues = sendChimeraCommand(cmd, true);

    boolean featureAdded = createFeaturesForAttributes(attName, residues);
    if (featureAdded)
    {
      alignmentPanel.getFeatureRenderer().featuresAdded();
    }
  }

  /**
   * Create features in Jalview for the given attribute name and structure
   * residues.
   * 
   * <pre>
   * The residue list should be 0, 1 or more reply lines of the format: 
   *     residue id #0:5.A isHelix -155.000836316 index 5 
   * or 
   *     residue id #0:6.A isHelix None
   * </pre>
   * 
   * @param attName
   * @param residues
   * @return
   */
  protected boolean createFeaturesForAttributes(String attName,
          List<String> residues)
  {
    boolean featureAdded = false;
    String featureGroup = getViewerFeatureGroup();

    for (String residue : residues)
    {
      AtomSpec spec = null;
      String[] tokens = residue.split(" ");
      if (tokens.length < 5)
      {
        continue;
      }
      String atomSpec = tokens[2];
      String attValue = tokens[4];

      /*
       * ignore 'None' (e.g. for phi) or 'False' (e.g. for isHelix)
       */
      if ("None".equalsIgnoreCase(attValue)
              || "False".equalsIgnoreCase(attValue))
      {
        continue;
      }

      try
      {
        spec = AtomSpec.fromChimeraAtomspec(atomSpec);
      } catch (IllegalArgumentException e)
      {
        System.err.println("Problem parsing atomspec " + atomSpec);
        continue;
      }

      String chainId = spec.getChain();
      String description = attValue;
      float score = Float.NaN;
      try
      {
        score = Float.valueOf(attValue);
        description = chainId;
      } catch (NumberFormatException e)
      {
        // was not a float value
      }

      String pdbFile = getPdbFileForModel(spec.getModelNumber());
      spec.setPdbFile(pdbFile);

      List<AtomSpec> atoms = Collections.singletonList(spec);

      /*
       * locate the mapped position in the alignment (if any)
       */
      SearchResultsI sr = getSsm()
              .findAlignmentPositionsForStructurePositions(atoms);

      /*
       * expect one matched alignment position, or none 
       * (if the structure position is not mapped)
       */
      for (SearchResultMatchI m : sr.getResults())
      {
        SequenceI seq = m.getSequence();
        int start = m.getStart();
        int end = m.getEnd();
        SequenceFeature sf = new SequenceFeature(attName, description,
                start, end, score, featureGroup);
        // todo: should SequenceFeature have an explicit property for chain?
        // note: repeating the action shouldn't duplicate features
        featureAdded |= seq.addSequenceFeature(sf);
      }
    }
    return featureAdded;
  }

  /**
   * Answers the feature group name to apply to features created in Jalview from
   * Chimera attributes
   * 
   * @return
   */
  protected String getViewerFeatureGroup()
  {
    // todo pull up to interface
    return CHIMERA_FEATURE_GROUP;
  }

  public Hashtable<String, String> getChainFile()
  {
    return chainFile;
  }

  public List<ChimeraModel> getChimeraModelByChain(String chain)
  {
    return chimeraMaps.get(chainFile.get(chain));
  }

  public int getModelNoForChain(String chain)
  {
    List<ChimeraModel> foundModels = getChimeraModelByChain(chain);
    if (foundModels != null && !foundModels.isEmpty())
    {
      return foundModels.get(0).getModelNumber();
    }
    return -1;
  }
}
