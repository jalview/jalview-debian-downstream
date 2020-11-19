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
package jalview.gui;

import jalview.api.FeatureRenderer;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.rbvi.chimera.ChimeraCommands;
import jalview.ext.rbvi.chimera.JalviewChimeraBinding;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.BrowserLauncher;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.dbsources.Pdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * GUI elements for handling an external chimera display
 * 
 * @author jprocter
 *
 */
public class ChimeraViewFrame extends StructureViewerBase
{
  private JalviewChimeraBinding jmb;

  private IProgressIndicator progressBar = null;

  /*
   * Path to Chimera session file. This is set when an open Jalview/Chimera
   * session is saved, or on restore from a Jalview project (if it holds the
   * filename of any saved Chimera sessions).
   */
  private String chimeraSessionFile = null;

  private Random random = new Random();

  private int myWidth = 500;

  private int myHeight = 150;

  /**
   * Initialise menu options.
   */
  @Override
  protected void initMenus()
  {
    super.initMenus();

    viewerActionMenu.setText(MessageManager.getString("label.chimera"));

    viewerColour
            .setText(MessageManager.getString("label.colour_with_chimera"));
    viewerColour.setToolTipText(MessageManager
            .getString("label.let_chimera_manage_structure_colours"));

    helpItem.setText(MessageManager.getString("label.chimera_help"));
    savemenu.setVisible(false); // not yet implemented
    viewMenu.add(fitToWindow);

    JMenuItem writeFeatures = new JMenuItem(
            MessageManager.getString("label.create_chimera_attributes"));
    writeFeatures.setToolTipText(MessageManager
            .getString("label.create_chimera_attributes_tip"));
    writeFeatures.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sendFeaturesToChimera();
      }
    });
    viewerActionMenu.add(writeFeatures);

    final JMenu fetchAttributes = new JMenu(
            MessageManager.getString("label.fetch_chimera_attributes"));
    fetchAttributes.setToolTipText(
            MessageManager.getString("label.fetch_chimera_attributes_tip"));
    fetchAttributes.addMouseListener(new MouseAdapter()
    {

      @Override
      public void mouseEntered(MouseEvent e)
      {
        buildAttributesMenu(fetchAttributes);
      }
    });
    viewerActionMenu.add(fetchAttributes);
  }

  /**
   * Query Chimera for its residue attribute names and add them as items off the
   * attributes menu
   * 
   * @param attributesMenu
   */
  protected void buildAttributesMenu(JMenu attributesMenu)
  {
    List<String> atts = jmb.sendChimeraCommand("list resattr", true);
    if (atts == null)
    {
      return;
    }
    attributesMenu.removeAll();
    Collections.sort(atts);
    for (String att : atts)
    {
      final String attName = att.split(" ")[1];

      /*
       * ignore 'jv_*' attributes, as these are Jalview features that have
       * been transferred to residue attributes in Chimera!
       */
      if (!attName.startsWith(ChimeraCommands.NAMESPACE_PREFIX))
      {
        JMenuItem menuItem = new JMenuItem(attName);
        menuItem.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            getChimeraAttributes(attName);
          }
        });
        attributesMenu.add(menuItem);
      }
    }
  }

  /**
   * Read residues in Chimera with the given attribute name, and set as features
   * on the corresponding sequence positions (if any)
   * 
   * @param attName
   */
  protected void getChimeraAttributes(String attName)
  {
    jmb.copyStructureAttributesToFeatures(attName, getAlignmentPanel());
  }

  /**
   * Send a command to Chimera to create residue attributes for Jalview features
   * <p>
   * The syntax is: setattr r <attName> <attValue> <atomSpec>
   * <p>
   * For example: setattr r jv:chain "Ferredoxin-1, Chloroplastic" #0:94.A
   */
  protected void sendFeaturesToChimera()
  {
    int count = jmb.sendFeaturesToViewer(getAlignmentPanel());
    statusBar.setText(
            MessageManager.formatMessage("label.attributes_set", count));
  }

  /**
   * open a single PDB structure in a new Chimera view
   * 
   * @param pdbentry
   * @param seq
   * @param chains
   * @param ap
   */
  public ChimeraViewFrame(PDBEntry pdbentry, SequenceI[] seq,
          String[] chains, final AlignmentPanel ap)
  {
    this();

    openNewChimera(ap, new PDBEntry[] { pdbentry },
            new SequenceI[][]
            { seq });
  }

  /**
   * Create a helper to manage progress bar display
   */
  protected void createProgressBar()
  {
    if (progressBar == null)
    {
      progressBar = new ProgressBar(statusPanel, statusBar);
    }
  }

  private void openNewChimera(AlignmentPanel ap, PDBEntry[] pdbentrys,
          SequenceI[][] seqs)
  {
    createProgressBar();
    jmb = new JalviewChimeraBindingModel(this,
            ap.getStructureSelectionManager(), pdbentrys, seqs, null);
    addAlignmentPanel(ap);
    useAlignmentPanelForColourbyseq(ap);

    if (pdbentrys.length > 1)
    {
      useAlignmentPanelForSuperposition(ap);
    }
    jmb.setColourBySequence(true);
    setSize(myWidth, myHeight);
    initMenus();

    addingStructures = false;
    worker = new Thread(this);
    worker.start();

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(
              InternalFrameEvent internalFrameEvent)
      {
        closeViewer(false);
      }
    });

  }

  /**
   * Create a new viewer from saved session state data including Chimera session
   * file
   * 
   * @param chimeraSessionFile
   * @param alignPanel
   * @param pdbArray
   * @param seqsArray
   * @param colourByChimera
   * @param colourBySequence
   * @param newViewId
   */
  public ChimeraViewFrame(String chimeraSessionFile,
          AlignmentPanel alignPanel, PDBEntry[] pdbArray,
          SequenceI[][] seqsArray, boolean colourByChimera,
          boolean colourBySequence, String newViewId)
  {
    this();
    setViewId(newViewId);
    this.chimeraSessionFile = chimeraSessionFile;
    openNewChimera(alignPanel, pdbArray, seqsArray);
    if (colourByChimera)
    {
      jmb.setColourBySequence(false);
      seqColour.setSelected(false);
      viewerColour.setSelected(true);
    }
    else if (colourBySequence)
    {
      jmb.setColourBySequence(true);
      seqColour.setSelected(true);
      viewerColour.setSelected(false);
    }
  }

  /**
   * create a new viewer containing several structures, optionally superimposed
   * using the given alignPanel.
   * 
   * @param pe
   * @param seqs
   * @param ap
   */
  public ChimeraViewFrame(PDBEntry[] pe, boolean alignAdded,
          SequenceI[][] seqs,
          AlignmentPanel ap)
  {
    this();
    setAlignAddedStructures(alignAdded);
    openNewChimera(ap, pe, seqs);
  }

  /**
   * Default constructor
   */
  public ChimeraViewFrame()
  {
    super();

    /*
     * closeViewer will decide whether or not to close this frame
     * depending on whether user chooses to Cancel or not
     */
    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
  }

  /**
   * Launch Chimera. If we have a chimera session file name, send Chimera the
   * command to open its saved session file.
   */
  void initChimera()
  {
    jmb.setFinishedInit(false);
    Desktop.addInternalFrame(this,
            jmb.getViewerTitle(getViewerName(), true), getBounds().width,
            getBounds().height);

    if (!jmb.launchChimera())
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.getString("label.chimera_failed"),
              MessageManager.getString("label.error_loading_file"),
              JvOptionPane.ERROR_MESSAGE);
      this.dispose();
      return;
    }

    if (this.chimeraSessionFile != null)
    {
      boolean opened = jmb.openSession(chimeraSessionFile);
      if (!opened)
      {
        System.err.println("An error occurred opening Chimera session file "
                + chimeraSessionFile);
      }
    }

    jmb.startChimeraListener();
  }

  /**
   * Show only the selected chain(s) in the viewer
   */
  @Override
  void showSelectedChains()
  {
    List<String> toshow = new ArrayList<>();
    for (int i = 0; i < chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) chainMenu.getItem(i);
        if (item.isSelected())
        {
          toshow.add(item.getText());
        }
      }
    }
    jmb.showChains(toshow);
  }

  /**
   * Close down this instance of Jalview's Chimera viewer, giving the user the
   * option to close the associated Chimera window (process). They may wish to
   * keep it open until they have had an opportunity to save any work.
   * 
   * @param closeChimera
   *          if true, close any linked Chimera process; if false, prompt first
   */
  @Override
  public void closeViewer(boolean closeChimera)
  {
    if (jmb != null && jmb.isChimeraRunning())
    {
      if (!closeChimera)
      {
        String prompt = MessageManager
                .formatMessage("label.confirm_close_chimera", new Object[]
                { jmb.getViewerTitle(getViewerName(), false) });
        prompt = JvSwingUtils.wrapTooltip(true, prompt);
        int confirm = JvOptionPane.showConfirmDialog(this, prompt,
                MessageManager.getString("label.close_viewer"),
                JvOptionPane.YES_NO_CANCEL_OPTION);
        /*
         * abort closure if user hits escape or Cancel
         */
        if (confirm == JvOptionPane.CANCEL_OPTION
                || confirm == JvOptionPane.CLOSED_OPTION)
        {
          return;
        }
        closeChimera = confirm == JvOptionPane.YES_OPTION;
      }
      jmb.closeViewer(closeChimera);
    }
    setAlignmentPanel(null);
    _aps.clear();
    _alignwith.clear();
    _colourwith.clear();
    // TODO: check for memory leaks where instance isn't finalised because jmb
    // holds a reference to the window
    jmb = null;
    dispose();
  }

  /**
   * Open any newly added PDB structures in Chimera, having first fetched data
   * from PDB (if not already saved).
   */
  @Override
  public void run()
  {
    _started = true;
    // todo - record which pdbids were successfully imported.
    StringBuilder errormsgs = new StringBuilder(128);
    StringBuilder files = new StringBuilder(128);
    List<PDBEntry> filePDB = new ArrayList<>();
    List<Integer> filePDBpos = new ArrayList<>();
    PDBEntry thePdbEntry = null;
    StructureFile pdb = null;
    try
    {
      String[] curfiles = jmb.getStructureFiles(); // files currently in viewer
      // TODO: replace with reference fetching/transfer code (validate PDBentry
      // as a DBRef?)
      for (int pi = 0; pi < jmb.getPdbCount(); pi++)
      {
        String file = null;
        thePdbEntry = jmb.getPdbEntry(pi);
        if (thePdbEntry.getFile() == null)
        {
          /*
           * Retrieve PDB data, save to file, attach to PDBEntry
           */
          file = fetchPdbFile(thePdbEntry);
          if (file == null)
          {
            errormsgs.append("'" + thePdbEntry.getId() + "' ");
          }
        }
        else
        {
          /*
           * Got file already - ignore if already loaded in Chimera.
           */
          file = new File(thePdbEntry.getFile()).getAbsoluteFile()
                  .getPath();
          if (curfiles != null && curfiles.length > 0)
          {
            addingStructures = true; // already files loaded.
            for (int c = 0; c < curfiles.length; c++)
            {
              if (curfiles[c].equals(file))
              {
                file = null;
                break;
              }
            }
          }
        }
        if (file != null)
        {
          filePDB.add(thePdbEntry);
          filePDBpos.add(Integer.valueOf(pi));
          files.append(" \"" + Platform.escapeBackslashes(file) + "\"");
        }
      }
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB files: " + thePdbEntry.getId(),
              oomerror);
    } catch (Exception ex)
    {
      ex.printStackTrace();
      errormsgs.append(
              "When retrieving pdbfiles for '" + thePdbEntry.getId() + "'");
    }
    if (errormsgs.length() > 0)
    {

      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.pdb_entries_couldnt_be_retrieved", new Object[]
                      { errormsgs.toString() }),
              MessageManager.getString("label.couldnt_load_file"),
              JvOptionPane.ERROR_MESSAGE);
    }

    if (files.length() > 0)
    {
      jmb.setFinishedInit(false);
      if (!addingStructures)
      {
        try
        {
          initChimera();
        } catch (Exception ex)
        {
          Cache.log.error("Couldn't open Chimera viewer!", ex);
        }
      }
      int num = -1;
      for (PDBEntry pe : filePDB)
      {
        num++;
        if (pe.getFile() != null)
        {
          try
          {
            int pos = filePDBpos.get(num).intValue();
            long startTime = startProgressBar(getViewerName() + " "
                    + MessageManager.getString("status.opening_file_for")
                    + " " + pe.getId());
            jmb.openFile(pe);
            jmb.addSequence(pos, jmb.getSequence()[pos]);
            File fl = new File(pe.getFile());
            DataSourceType protocol = DataSourceType.URL;
            try
            {
              if (fl.exists())
              {
                protocol = DataSourceType.FILE;
              }
            } catch (Throwable e)
            {
            } finally
            {
              stopProgressBar("", startTime);
            }
            // Explicitly map to the filename used by Chimera ;

            pdb = jmb.getSsm().setMapping(jmb.getSequence()[pos],
                    jmb.getChains()[pos], pe.getFile(), protocol,
                    progressBar);
            stashFoundChains(pdb, pe.getFile());

          } catch (OutOfMemoryError oomerror)
          {
            new OOMWarning(
                    "When trying to open and map structures from Chimera!",
                    oomerror);
          } catch (Exception ex)
          {
            Cache.log.error(
                    "Couldn't open " + pe.getFile() + " in Chimera viewer!",
                    ex);
          } finally
          {
            Cache.log.debug("File locations are " + files);
          }
        }
      }

      jmb.refreshGUI();
      jmb.setFinishedInit(true);
      jmb.setLoadingFromArchive(false);

      /*
       * ensure that any newly discovered features (e.g. RESNUM)
       * are notified to the FeatureRenderer (and added to any 
       * open feature settings dialog)
       */
      FeatureRenderer fr = getBinding().getFeatureRenderer(null);
      if (fr != null)
      {
        fr.featuresAdded();
      }

      // refresh the sequence colours for the new structure(s)
      for (AlignmentPanel ap : _colourwith)
      {
        jmb.updateColours(ap);
      }
      // do superposition if asked to
      if (alignAddedStructures)
      {
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            alignStructs_withAllAlignPanels();
          }
        }).start();
      }
      addingStructures = false;
    }
    _started = false;
    worker = null;
  }

  /**
   * Fetch PDB data and save to a local file. Returns the full path to the file,
   * or null if fetch fails. TODO: refactor to common with Jmol ? duplication
   * 
   * @param processingEntry
   * @return
   * @throws Exception
   */

  private void stashFoundChains(StructureFile pdb, String file)
  {
    for (int i = 0; i < pdb.getChains().size(); i++)
    {
      String chid = new String(
              pdb.getId() + ":" + pdb.getChains().elementAt(i).id);
      jmb.getChainNames().add(chid);
      jmb.getChainFile().put(chid, file);
    }
  }

  private String fetchPdbFile(PDBEntry processingEntry) throws Exception
  {
    String filePath = null;
    Pdb pdbclient = new Pdb();
    AlignmentI pdbseq = null;
    String pdbid = processingEntry.getId();
    long handle = System.currentTimeMillis()
            + Thread.currentThread().hashCode();

    /*
     * Write 'fetching PDB' progress on AlignFrame as we are not yet visible
     */
    String msg = MessageManager.formatMessage("status.fetching_pdb",
            new Object[]
            { pdbid });
    getAlignmentPanel().alignFrame.setProgressBar(msg, handle);
    // long hdl = startProgressBar(MessageManager.formatMessage(
    // "status.fetching_pdb", new Object[]
    // { pdbid }));
    try
    {
      pdbseq = pdbclient.getSequenceRecords(pdbid);
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB id " + pdbid, oomerror);
    } finally
    {
      msg = pdbid + " " + MessageManager.getString("label.state_completed");
      getAlignmentPanel().alignFrame.setProgressBar(msg, handle);
      // stopProgressBar(msg, hdl);
    }
    /*
     * If PDB data were saved and are not invalid (empty alignment), return the
     * file path.
     */
    if (pdbseq != null && pdbseq.getHeight() > 0)
    {
      // just use the file name from the first sequence's first PDBEntry
      filePath = new File(pdbseq.getSequenceAt(0).getAllPDBEntries()
              .elementAt(0).getFile()).getAbsolutePath();
      processingEntry.setFile(filePath);
    }
    return filePath;
  }

  /**
   * Convenience method to update the progress bar if there is one. Be sure to
   * call stopProgressBar with the returned handle to remove the message.
   * 
   * @param msg
   * @param handle
   */
  public long startProgressBar(String msg)
  {
    // TODO would rather have startProgress/stopProgress as the
    // IProgressIndicator interface
    long tm = random.nextLong();
    if (progressBar != null)
    {
      progressBar.setProgressBar(msg, tm);
    }
    return tm;
  }

  /**
   * End the progress bar with the specified handle, leaving a message (if not
   * null) on the status bar
   * 
   * @param msg
   * @param handle
   */
  public void stopProgressBar(String msg, long handle)
  {
    if (progressBar != null)
    {
      progressBar.setProgressBar(msg, handle);
    }
  }

  @Override
  public void eps_actionPerformed(ActionEvent e)
  {
    throw new Error(MessageManager
            .getString("error.eps_generation_not_implemented"));
  }

  @Override
  public void png_actionPerformed(ActionEvent e)
  {
    throw new Error(MessageManager
            .getString("error.png_generation_not_implemented"));
  }

  @Override
  public void showHelp_actionPerformed(ActionEvent actionEvent)
  {
    try
    {
      BrowserLauncher
              .openURL("https://www.cgl.ucsf.edu/chimera/docs/UsersGuide");
    } catch (IOException ex)
    {
    }
  }

  @Override
  public AAStructureBindingModel getBinding()
  {
    return jmb;
  }

  /**
   * Ask Chimera to save its session to the designated file path, or to a
   * temporary file if the path is null. Returns the file path if successful,
   * else null.
   * 
   * @param filepath
   * @see getStateInfo
   */
  protected String saveSession(String filepath)
  {
    String pathUsed = filepath;
    try
    {
      if (pathUsed == null)
      {
        File tempFile = File.createTempFile("chimera", ".py");
        tempFile.deleteOnExit();
        pathUsed = tempFile.getPath();
      }
      boolean result = jmb.saveSession(pathUsed);
      if (result)
      {
        this.chimeraSessionFile = pathUsed;
        return pathUsed;
      }
    } catch (IOException e)
    {
    }
    return null;
  }

  /**
   * Returns a string representing the state of the Chimera session. This is
   * done by requesting Chimera to save its session to a temporary file, then
   * reading the file contents. Returns an empty string on any error.
   */
  @Override
  public String getStateInfo()
  {
    String sessionFile = saveSession(null);
    if (sessionFile == null)
    {
      return "";
    }
    InputStream is = null;
    try
    {
      File f = new File(sessionFile);
      byte[] bytes = new byte[(int) f.length()];
      is = new FileInputStream(sessionFile);
      is.read(bytes);
      return new String(bytes);
    } catch (IOException e)
    {
      return "";
    } finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
  }

  @Override
  protected void fitToWindow_actionPerformed()
  {
    jmb.focusView();
  }

  @Override
  public ViewerType getViewerType()
  {
    return ViewerType.CHIMERA;
  }

  @Override
  protected String getViewerName()
  {
    return "Chimera";
  }

  /**
   * Sends commands to align structures according to associated alignment(s).
   * 
   * @return
   */
  @Override
  protected String alignStructs_withAllAlignPanels()
  {
    String reply = super.alignStructs_withAllAlignPanels();
    if (reply != null)
    {
      statusBar.setText("Superposition failed: " + reply);
    }
    return reply;
  }

  @Override
  protected IProgressIndicator getIProgressIndicator()
  {
    return progressBar;
  }
}
