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
package jalview.gui;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.rbvi.chimera.JalviewChimeraBinding;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.AppletFormatAdapter;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.io.StructureFile;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.dbsources.Pdb;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * GUI elements for handling an external chimera display
 * 
 * @author jprocter
 *
 */
public class ChimeraViewFrame extends StructureViewerBase
{
  private JalviewChimeraBinding jmb;

  private boolean allChainsSelected = false;

  private IProgressIndicator progressBar = null;

  /*
   * Path to Chimera session file. This is set when an open Jalview/Chimera
   * session is saved, or on restore from a Jalview project (if it holds the
   * filename of any saved Chimera sessions).
   */
  private String chimeraSessionFile = null;

  private Random random = new Random();

  /**
   * Initialise menu options.
   */
  private void initMenus()
  {
    viewerActionMenu.setText(MessageManager.getString("label.chimera"));
    viewerColour.setText(MessageManager
            .getString("label.colour_with_chimera"));
    viewerColour.setToolTipText(MessageManager
            .getString("label.let_chimera_manage_structure_colours"));
    helpItem.setText(MessageManager.getString("label.chimera_help"));
    seqColour.setSelected(jmb.isColourBySequence());
    viewerColour.setSelected(!jmb.isColourBySequence());
    if (_colourwith == null)
    {
      _colourwith = new Vector<AlignmentPanel>();
    }
    if (_alignwith == null)
    {
      _alignwith = new Vector<AlignmentPanel>();
    }

    // save As not yet implemented
    savemenu.setVisible(false);

    ViewSelectionMenu seqColourBy = new ViewSelectionMenu(
            MessageManager.getString("label.colour_by"), this, _colourwith,
            new ItemListener()
            {
              @Override
              public void itemStateChanged(ItemEvent e)
              {
                if (!seqColour.isSelected())
                {
                  seqColour.doClick();
                }
                else
                {
                  // update the Chimera display now.
                  seqColour_actionPerformed(null);
                }
              }
            });
    viewMenu.add(seqColourBy);
    viewMenu.add(fitToWindow);

    final ItemListener handler;
    JMenu alpanels = new ViewSelectionMenu(
            MessageManager.getString("label.superpose_with"), this,
            _alignwith, handler = new ItemListener()
            {
              @Override
              public void itemStateChanged(ItemEvent e)
              {
                alignStructs.setEnabled(_alignwith.size() > 0);
                alignStructs.setToolTipText(MessageManager
                        .formatMessage(
                                "label.align_structures_using_linked_alignment_views",
                                new Object[] { new Integer(_alignwith
                                        .size()).toString() }));
              }
            });
    handler.itemStateChanged(null);
    viewerActionMenu.add(alpanels);
    viewerActionMenu.addMenuListener(new MenuListener()
    {

      @Override
      public void menuSelected(MenuEvent e)
      {
        handler.itemStateChanged(null);
      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
        // TODO Auto-generated method stub
      }

      @Override
      public void menuCanceled(MenuEvent e)
      {
        // TODO Auto-generated method stub
      }
    });
  }

  /**
   * add a single PDB structure to a new or existing Chimera view
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
    String pdbId = pdbentry.getId();

    /*
     * If the PDB file is already loaded, the user may just choose to add to an
     * existing viewer (or cancel)
     */
    if (addAlreadyLoadedFile(seq, chains, ap, pdbId))
    {
      return;
    }

    /*
     * Check if there are other Chimera views involving this alignment and give
     * user the option to add and align this molecule to one of them (or cancel)
     */
    if (addToExistingViewer(pdbentry, seq, chains, ap, pdbId))
    {
      return;
    }

    /*
     * If the options above are declined or do not apply, show the structure in
     * a new viewer
     */
    openNewChimera(ap, new PDBEntry[] { pdbentry },
            new SequenceI[][] { seq });
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

  /**
   * Answers true if this viewer already involves the given PDB ID
   */
  @Override
  protected boolean hasPdbId(String pdbId)
  {
    return jmb.hasPdbId(pdbId);
  }

  private void openNewChimera(AlignmentPanel ap, PDBEntry[] pdbentrys,
          SequenceI[][] seqs)
  {
    createProgressBar();
    // FIXME extractChains needs pdbentries to match IDs to PDBEntry(s) on seqs
    jmb = new JalviewChimeraBindingModel(this,
            ap.getStructureSelectionManager(), pdbentrys, seqs, null);
    addAlignmentPanel(ap);
    useAlignmentPanelForColourbyseq(ap);
    if (pdbentrys.length > 1)
    {
      alignAddedStructures = true;
      useAlignmentPanelForSuperposition(ap);
    }
    jmb.setColourBySequence(true);
    setSize(400, 400); // probably should be a configurable/dynamic default here
    initMenus();

    addingStructures = false;
    worker = new Thread(this);
    worker.start();

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(InternalFrameEvent internalFrameEvent)
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
   * create a new viewer containing several structures superimposed using the
   * given alignPanel.
   * 
   * @param pe
   * @param seqs
   * @param ap
   */
  public ChimeraViewFrame(PDBEntry[] pe, SequenceI[][] seqs,
          AlignmentPanel ap)
  {
    this();
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
   * Returns a list of any Chimera viewers in the desktop. The list is
   * restricted to those linked to the given alignment panel if it is not null.
   */
  @Override
  protected List<StructureViewerBase> getViewersFor(AlignmentPanel ap)
  {
    List<StructureViewerBase> result = new ArrayList<StructureViewerBase>();
    JInternalFrame[] frames = Desktop.instance.getAllFrames();

    for (JInternalFrame frame : frames)
    {
      if (frame instanceof ChimeraViewFrame)
      {
        if (ap == null || ((StructureViewerBase) frame).isLinkedWith(ap))
        {
          result.add((StructureViewerBase) frame);
        }
      }
    }
    return result;
  }

  /**
   * Launch Chimera. If we have a chimera session file name, send Chimera the
   * command to open its saved session file.
   */
  void initChimera()
  {
    jmb.setFinishedInit(false);
    jalview.gui.Desktop.addInternalFrame(this,
            jmb.getViewerTitle("Chimera", true), getBounds().width,
            getBounds().height);

    if (!jmb.launchChimera())
    {
      JOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.getString("label.chimera_failed"),
              MessageManager.getString("label.error_loading_file"),
              JOptionPane.ERROR_MESSAGE);
      this.dispose();
      return;
    }

    if (this.chimeraSessionFile != null)
    {
      boolean opened = jmb.openSession(chimeraSessionFile);
      if (!opened)
      {
        System.err
                .println("An error occurred opening Chimera session file "
                        + chimeraSessionFile);
      }
    }
    jmb.setFinishedInit(true);

    jmb.startChimeraListener();
  }


  /**
   * Show only the selected chain(s) in the viewer
   */
  @Override
  void showSelectedChains()
  {
    List<String> toshow = new ArrayList<String>();
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
        String prompt = MessageManager.formatMessage(
                "label.confirm_close_chimera",
                new Object[] { jmb.getViewerTitle("Chimera", false) });
        prompt = JvSwingUtils.wrapTooltip(true, prompt);
        int confirm = JOptionPane.showConfirmDialog(this, prompt,
                MessageManager.getString("label.close_viewer"),
                JOptionPane.YES_NO_CANCEL_OPTION);
        /*
         * abort closure if user hits escape or Cancel
         */
        if (confirm == JOptionPane.CANCEL_OPTION
                || confirm == JOptionPane.CLOSED_OPTION)
        {
          return;
        }
        closeChimera = confirm == JOptionPane.YES_OPTION;
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
    List<PDBEntry> filePDB = new ArrayList<PDBEntry>();
    List<Integer> filePDBpos = new ArrayList<Integer>();
    PDBEntry thePdbEntry = null;
    StructureFile pdb = null;
    try
    {
      String[] curfiles = jmb.getPdbFile(); // files currently in viewer
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
          files.append(" \"" + Platform.escapeString(file) + "\"");
        }
      }
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB files: " + thePdbEntry.getId(),
              oomerror);
    } catch (Exception ex)
    {
      ex.printStackTrace();
      errormsgs.append("When retrieving pdbfiles for '"
              + thePdbEntry.getId() + "'");
    }
    if (errormsgs.length() > 0)
    {

      JOptionPane.showInternalMessageDialog(Desktop.desktop, MessageManager
              .formatMessage("label.pdb_entries_couldnt_be_retrieved",
                      new Object[] { errormsgs.toString() }),
              MessageManager.getString("label.couldnt_load_file"),
              JOptionPane.ERROR_MESSAGE);
    }

    if (files.length() > 0)
    {
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
            long startTime = startProgressBar("Chimera "
                    + MessageManager.getString("status.opening_file_for")
                    + " " + pe.getId());
            jmb.openFile(pe);
            jmb.addSequence(pos, jmb.getSequence()[pos]);
            File fl = new File(pe.getFile());
            String protocol = AppletFormatAdapter.URL;
            try
            {
              if (fl.exists())
              {
                protocol = AppletFormatAdapter.FILE;
              }
            } catch (Throwable e)
            {
            } finally
            {
              stopProgressBar("", startTime);
            }
            // Explicitly map to the filename used by Chimera ;
            pdb = jmb.getSsm().setMapping(jmb.getSequence()[pos],
                    jmb.getChains()[pos], pe.getFile(), protocol);
            stashFoundChains(pdb, pe.getFile());
          } catch (OutOfMemoryError oomerror)
          {
            new OOMWarning(
                    "When trying to open and map structures from Chimera!",
                    oomerror);
          } catch (Exception ex)
          {
            Cache.log.error("Couldn't open " + pe.getFile()
                    + " in Chimera viewer!", ex);
          } finally
          {
            Cache.log.debug("File locations are " + files);
          }
        }
      }
      jmb.refreshGUI();
      jmb.setFinishedInit(true);
      jmb.setLoadingFromArchive(false);

      // refresh the sequence colours for the new structure(s)
      for (AlignmentPanel ap : _colourwith)
      {
        jmb.updateColours(ap);
      }
      // do superposition if asked to
      if (Cache.getDefault("AUTOSUPERIMPOSE", true) && alignAddedStructures)
      {
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            alignStructs_withAllAlignPanels();
          }
        }).start();
        alignAddedStructures = false;
      }
      addingStructures = false;
    }
    _started = false;
    worker = null;
  }

  /**
   * Fetch PDB data and save to a local file. Returns the full path to the file,
   * or null if fetch fails.
   * 
   * @param processingEntry
   * @return
   * @throws Exception
   */

  private void stashFoundChains(StructureFile pdb, String file)
  {
    for (int i = 0; i < pdb.getChains().size(); i++)
    {
      String chid = new String(pdb.getId() + ":"
              + pdb.getChains().elementAt(i).id);
      jmb.getChainNames().add(chid);
      jmb.getChainFile().put(chid, file);
    }
  }
  private String fetchPdbFile(PDBEntry processingEntry) throws Exception
  {
    // FIXME: this is duplicated code with Jmol frame ?
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
            new Object[] { pdbid });
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
  public void pdbFile_actionPerformed(ActionEvent actionEvent)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.save_pdb_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      BufferedReader in = null;
      try
      {
        // TODO: cope with multiple PDB files in view
        in = new BufferedReader(new FileReader(jmb.getPdbFile()[0]));
        File outFile = chooser.getSelectedFile();

        PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        String data;
        while ((data = in.readLine()) != null)
        {
          if (!(data.indexOf("<PRE>") > -1 || data.indexOf("</PRE>") > -1))
          {
            out.println(data);
          }
        }
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      } finally
      {
        if (in != null)
        {
          try
          {
            in.close();
          } catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }

  @Override
  public void viewMapping_actionPerformed(ActionEvent actionEvent)
  {
    jalview.gui.CutAndPasteTransfer cap = new jalview.gui.CutAndPasteTransfer();
    try
    {
      cap.appendText(jmb.printMappings());
    } catch (OutOfMemoryError e)
    {
      new OOMWarning(
              "composing sequence-structure alignments for display in text box.",
              e);
      cap.dispose();
      return;
    }
    jalview.gui.Desktop.addInternalFrame(cap,
            MessageManager.getString("label.pdb_sequence_mapping"), 550,
            600);
  }

  @Override
  public void eps_actionPerformed(ActionEvent e)
  {
    throw new Error(
            MessageManager
                    .getString("error.eps_generation_not_implemented"));
  }

  @Override
  public void png_actionPerformed(ActionEvent e)
  {
    throw new Error(
            MessageManager
                    .getString("error.png_generation_not_implemented"));
  }

  @Override
  public void viewerColour_actionPerformed(ActionEvent actionEvent)
  {
    if (viewerColour.isSelected())
    {
      // disable automatic sequence colouring.
      jmb.setColourBySequence(false);
    }
  }

  @Override
  public void seqColour_actionPerformed(ActionEvent actionEvent)
  {
    jmb.setColourBySequence(seqColour.isSelected());
    if (_colourwith == null)
    {
      _colourwith = new Vector<AlignmentPanel>();
    }
    if (jmb.isColourBySequence())
    {
      if (!jmb.isLoadingFromArchive())
      {
        if (_colourwith.size() == 0 && getAlignmentPanel() != null)
        {
          // Make the currently displayed alignment panel the associated view
          _colourwith.add(getAlignmentPanel().alignFrame.alignPanel);
        }
      }
      // Set the colour using the current view for the associated alignframe
      for (AlignmentPanel ap : _colourwith)
      {
        jmb.colourBySequence(ap.av.isShowSequenceFeatures(), ap);
      }
    }
  }

  @Override
  public void chainColour_actionPerformed(ActionEvent actionEvent)
  {
    chainColour.setSelected(true);
    jmb.colourByChain();
  }

  @Override
  public void chargeColour_actionPerformed(ActionEvent actionEvent)
  {
    chargeColour.setSelected(true);
    jmb.colourByCharge();
  }

  @Override
  public void zappoColour_actionPerformed(ActionEvent actionEvent)
  {
    zappoColour.setSelected(true);
    jmb.setJalviewColourScheme(new ZappoColourScheme());
  }

  @Override
  public void taylorColour_actionPerformed(ActionEvent actionEvent)
  {
    taylorColour.setSelected(true);
    jmb.setJalviewColourScheme(new TaylorColourScheme());
  }

  @Override
  public void hydroColour_actionPerformed(ActionEvent actionEvent)
  {
    hydroColour.setSelected(true);
    jmb.setJalviewColourScheme(new HydrophobicColourScheme());
  }

  @Override
  public void helixColour_actionPerformed(ActionEvent actionEvent)
  {
    helixColour.setSelected(true);
    jmb.setJalviewColourScheme(new HelixColourScheme());
  }

  @Override
  public void strandColour_actionPerformed(ActionEvent actionEvent)
  {
    strandColour.setSelected(true);
    jmb.setJalviewColourScheme(new StrandColourScheme());
  }

  @Override
  public void turnColour_actionPerformed(ActionEvent actionEvent)
  {
    turnColour.setSelected(true);
    jmb.setJalviewColourScheme(new TurnColourScheme());
  }

  @Override
  public void buriedColour_actionPerformed(ActionEvent actionEvent)
  {
    buriedColour.setSelected(true);
    jmb.setJalviewColourScheme(new BuriedColourScheme());
  }

  @Override
  public void purinePyrimidineColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new PurinePyrimidineColourScheme());
  }

  @Override
  public void userColour_actionPerformed(ActionEvent actionEvent)
  {
    userColour.setSelected(true);
    new UserDefinedColours(this, null);
  }

  @Override
  public void backGround_actionPerformed(ActionEvent actionEvent)
  {
    java.awt.Color col = JColorChooser
            .showDialog(this, MessageManager
                    .getString("label.select_backgroud_colour"), null);
    if (col != null)
    {
      jmb.setBackgroundColour(col);
    }
  }

  @Override
  public void showHelp_actionPerformed(ActionEvent actionEvent)
  {
    try
    {
      jalview.util.BrowserLauncher
              .openURL("https://www.cgl.ucsf.edu/chimera/docs/UsersGuide");
    } catch (Exception ex)
    {
    }
  }

  public void updateTitleAndMenus()
  {
    if (jmb.fileLoadingError != null && jmb.fileLoadingError.length() > 0)
    {
      repaint();
      return;
    }
    setChainMenuItems(jmb.getChainNames());

    this.setTitle(jmb.getViewerTitle("Chimera", true));
    if (jmb.getPdbFile().length > 1 && jmb.getSequence().length > 1)
    {
      viewerActionMenu.setVisible(true);
    }
    if (!jmb.isLoadingFromArchive())
    {
      seqColour_actionPerformed(null);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GStructureViewer#alignStructs_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  @Override
  protected void alignStructs_actionPerformed(ActionEvent actionEvent)
  {
    alignStructs_withAllAlignPanels();
  }

  private void alignStructs_withAllAlignPanels()
  {
    if (getAlignmentPanel() == null)
    {
      return;
    }

    if (_alignwith.size() == 0)
    {
      _alignwith.add(getAlignmentPanel());
    }

    try
    {
      AlignmentI[] als = new Alignment[_alignwith.size()];
      ColumnSelection[] alc = new ColumnSelection[_alignwith.size()];
      int[] alm = new int[_alignwith.size()];
      int a = 0;

      for (AlignmentPanel ap : _alignwith)
      {
        als[a] = ap.av.getAlignment();
        alm[a] = -1;
        alc[a++] = ap.av.getColumnSelection();
      }
      jmb.superposeStructures(als, alm, alc);
    } catch (Exception e)
    {
      StringBuffer sp = new StringBuffer();
      for (AlignmentPanel ap : _alignwith)
      {
        sp.append("'" + ap.alignFrame.getTitle() + "' ");
      }
      Cache.log.info("Couldn't align structures with the " + sp.toString()
              + "associated alignment panels.", e);
    }
  }

  @Override
  public void setJalviewColourScheme(ColourSchemeI ucs)
  {
    jmb.setJalviewColourScheme(ucs);

  }

  /**
   * 
   * @param alignment
   * @return first alignment panel displaying given alignment, or the default
   *         alignment panel
   */
  public AlignmentPanel getAlignmentPanelFor(AlignmentI alignment)
  {
    for (AlignmentPanel ap : getAllAlignmentPanels())
    {
      if (ap.av.getAlignment() == alignment)
      {
        return ap;
      }
    }
    return getAlignmentPanel();
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
  protected AAStructureBindingModel getBindingModel()
  {
    return jmb;
  }
}
