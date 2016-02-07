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
package jalview.gui;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class AppJmol extends StructureViewerBase
{
  AppJmolBinding jmb;

  JPanel scriptWindow;

  JSplitPane splitPane;

  RenderPanel renderPanel;

  ViewSelectionMenu seqColourBy;

  /**
   * 
   * @param files
   * @param ids
   * @param seqs
   * @param ap
   * @param usetoColour
   *          - add the alignment panel to the list used for colouring these
   *          structures
   * @param useToAlign
   *          - add the alignment panel to the list used for aligning these
   *          structures
   * @param leaveColouringToJmol
   *          - do not update the colours from any other source. Jmol is
   *          handling them
   * @param loadStatus
   * @param bounds
   * @param viewid
   */
  public AppJmol(String[] files, String[] ids, SequenceI[][] seqs,
          AlignmentPanel ap, boolean usetoColour, boolean useToAlign,
          boolean leaveColouringToJmol, String loadStatus,
          Rectangle bounds, String viewid)
  {
    PDBEntry[] pdbentrys = new PDBEntry[files.length];
    for (int i = 0; i < pdbentrys.length; i++)
    {
      // PDBEntry pdbentry = new PDBEntry(files[i], ids[i]);
      PDBEntry pdbentry = new PDBEntry(ids[i], null, PDBEntry.Type.PDB,
              files[i]);
      pdbentrys[i] = pdbentry;
    }
    // / TODO: check if protocol is needed to be set, and if chains are
    // autodiscovered.
    jmb = new AppJmolBinding(this, ap.getStructureSelectionManager(),
            pdbentrys, seqs, null, null);

    jmb.setLoadingFromArchive(true);
    addAlignmentPanel(ap);
    if (useToAlign)
    {
      useAlignmentPanelForSuperposition(ap);
    }
    if (leaveColouringToJmol || !usetoColour)
    {
      jmb.setColourBySequence(false);
      seqColour.setSelected(false);
      viewerColour.setSelected(true);
    }
    else if (usetoColour)
    {
      useAlignmentPanelForColourbyseq(ap);
      jmb.setColourBySequence(true);
      seqColour.setSelected(true);
      viewerColour.setSelected(false);
    }
    this.setBounds(bounds);
    initMenus();
    setViewId(viewid);
    // jalview.gui.Desktop.addInternalFrame(this, "Loading File",
    // bounds.width,bounds.height);

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(InternalFrameEvent internalFrameEvent)
      {
        closeViewer(false);
      }
    });
    initJmol(loadStatus); // pdbentry, seq, JBPCHECK!

  }

  private void initMenus()
  {
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

    seqColourBy = new ViewSelectionMenu(
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
                  // update the jmol display now.
                  seqColour_actionPerformed(null);
                }
              }
            });
    viewMenu.add(seqColourBy);
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
                                new String[] { new Integer(_alignwith
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

  IProgressIndicator progressBar = null;

  /**
   * add a single PDB structure to a new or existing Jmol view
   * 
   * @param pdbentry
   * @param seq
   * @param chains
   * @param ap
   */
  public AppJmol(PDBEntry pdbentry, SequenceI[] seq, String[] chains,
          final AlignmentPanel ap)
  {
    progressBar = ap.alignFrame;
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
     * Check if there are other Jmol views involving this alignment and prompt
     * user about adding this molecule to one of them
     */
    if (addToExistingViewer(pdbentry, seq, chains, ap, pdbId))
    {
      return;
    }

    /*
     * If the options above are declined or do not apply, open a new viewer
     */
    openNewJmol(ap, new PDBEntry[] { pdbentry }, new SequenceI[][] { seq });
  }

  /**
   * Answers true if this viewer already involves the given PDB ID
   */
  @Override
  protected boolean hasPdbId(String pdbId)
  {
    return jmb.hasPdbId(pdbId);
  }

  private void openNewJmol(AlignmentPanel ap, PDBEntry[] pdbentrys,
          SequenceI[][] seqs)
  {
    progressBar = ap.alignFrame;
    jmb = new AppJmolBinding(this, ap.getStructureSelectionManager(),
            pdbentrys, seqs, null, null);
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
    worker = null;
    {
      addingStructures = false;
      worker = new Thread(this);
      worker.start();
    }
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
   * create a new Jmol containing several structures superimposed using the
   * given alignPanel.
   * 
   * @param ap
   * @param pe
   * @param seqs
   */
  public AppJmol(AlignmentPanel ap, PDBEntry[] pe, SequenceI[][] seqs)
  {
    openNewJmol(ap, pe, seqs);
  }

  /**
   * Returns a list of any Jmol viewers. The list is restricted to those linked
   * to the given alignment panel if it is not null.
   */
  @Override
  protected List<StructureViewerBase> getViewersFor(AlignmentPanel apanel)
  {
    List<StructureViewerBase> result = new ArrayList<StructureViewerBase>();
    JInternalFrame[] frames = Desktop.instance.getAllFrames();

    for (JInternalFrame frame : frames)
    {
      if (frame instanceof AppJmol)
      {
        if (apanel == null
                || ((StructureViewerBase) frame).isLinkedWith(apanel))
        {
          result.add((StructureViewerBase) frame);
        }
      }
    }
    return result;
  }

  void initJmol(String command)
  {
    jmb.setFinishedInit(false);
    renderPanel = new RenderPanel();
    // TODO: consider waiting until the structure/view is fully loaded before
    // displaying
    this.getContentPane().add(renderPanel, java.awt.BorderLayout.CENTER);
    jalview.gui.Desktop.addInternalFrame(this, jmb.getViewerTitle(),
            getBounds().width, getBounds().height);
    if (scriptWindow == null)
    {
      BorderLayout bl = new BorderLayout();
      bl.setHgap(0);
      bl.setVgap(0);
      scriptWindow = new JPanel(bl);
      scriptWindow.setVisible(false);
    }

    jmb.allocateViewer(renderPanel, true, "", null, null, "", scriptWindow,
            null);
    // jmb.newJmolPopup("Jmol");
    if (command == null)
    {
      command = "";
    }
    jmb.evalStateCommand(command);
    jmb.setFinishedInit(true);
  }

  void setChainMenuItems(Vector<String> chains)
  {
    chainMenu.removeAll();
    if (chains == null)
    {
      return;
    }
    JMenuItem menuItem = new JMenuItem(
            MessageManager.getString("label.all"));
    menuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        allChainsSelected = true;
        for (int i = 0; i < chainMenu.getItemCount(); i++)
        {
          if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
          {
            ((JCheckBoxMenuItem) chainMenu.getItem(i)).setSelected(true);
          }
        }
        centerViewer();
        allChainsSelected = false;
      }
    });

    chainMenu.add(menuItem);

    for (String chain : chains)
    {
      menuItem = new JCheckBoxMenuItem(chain, true);
      menuItem.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent evt)
        {
          if (!allChainsSelected)
          {
            centerViewer();
          }
        }
      });

      chainMenu.add(menuItem);
    }
  }

  boolean allChainsSelected = false;

  void centerViewer()
  {
    Vector<String> toshow = new Vector<String>();
    for (int i = 0; i < chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) chainMenu.getItem(i);
        if (item.isSelected())
        {
          toshow.addElement(item.getText());
        }
      }
    }
    jmb.centerViewer(toshow);
  }

  public void closeViewer(boolean closeExternalViewer)
  {
    // Jmol does not use an external viewer
    if (jmb != null)
    {
      jmb.closeViewer();
    }
    setAlignmentPanel(null);
    _aps.clear();
    _alignwith.clear();
    _colourwith.clear();
    // TODO: check for memory leaks where instance isn't finalised because jmb
    // holds a reference to the window
    jmb = null;
  }

  public void run()
  {
    _started = true;
    String pdbid = "";
    // todo - record which pdbids were successfuly imported.
    StringBuffer errormsgs = new StringBuffer(), files = new StringBuffer();
    try
    {
      String[] curfiles = jmb.getPdbFile(); // files currently in viewer
      // TODO: replace with reference fetching/transfer code (validate PDBentry
      // as a DBRef?)
      jalview.ws.dbsources.Pdb pdbclient = new jalview.ws.dbsources.Pdb();
      for (int pi = 0; pi < jmb.getPdbCount(); pi++)
      {
        String file = jmb.getPdbEntry(pi).getFile();
        if (file == null)
        {
          // retrieve the pdb and store it locally
          AlignmentI pdbseq = null;
          pdbid = jmb.getPdbEntry(pi).getId();
          long hdl = pdbid.hashCode() - System.currentTimeMillis();
          if (progressBar != null)
          {
            progressBar.setProgressBar(MessageManager.formatMessage(
                    "status.fetching_pdb", new String[] { pdbid }), hdl);
          }
          try
          {
            pdbseq = pdbclient.getSequenceRecords(pdbid);
          } catch (OutOfMemoryError oomerror)
          {
            new OOMWarning("Retrieving PDB id " + pdbid, oomerror);
          } catch (Exception ex)
          {
            ex.printStackTrace();
            errormsgs.append("'" + pdbid + "'");
          }
          if (progressBar != null)
          {
            progressBar.setProgressBar(
                    MessageManager.getString("label.state_completed"), hdl);
          }
          if (pdbseq != null)
          {
            // just transfer the file name from the first sequence's first
            // PDBEntry
            file = new File(pdbseq.getSequenceAt(0).getAllPDBEntries()
                    .elementAt(0).getFile()).getAbsolutePath();
            jmb.getPdbEntry(pi).setFile(file);

            files.append(" \"" + Platform.escapeString(file) + "\"");
          }
          else
          {
            errormsgs.append("'" + pdbid + "' ");
          }
        }
        else
        {
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
          if (file != null)
          {
            files.append(" \"" + Platform.escapeString(file) + "\"");
          }
        }
      }
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB files: " + pdbid, oomerror);
    } catch (Exception ex)
    {
      ex.printStackTrace();
      errormsgs.append("When retrieving pdbfiles : current was: '" + pdbid
              + "'");
    }
    if (errormsgs.length() > 0)
    {

      JOptionPane.showInternalMessageDialog(Desktop.desktop, MessageManager
              .formatMessage("label.pdb_entries_couldnt_be_retrieved",
                      new String[] { errormsgs.toString() }),
              MessageManager.getString("label.couldnt_load_file"),
              JOptionPane.ERROR_MESSAGE);

    }
    long lastnotify = jmb.getLoadNotifiesHandled();
    if (files.length() > 0)
    {
      if (!addingStructures)
      {

        try
        {
          initJmol("load FILES " + files.toString());
        } catch (OutOfMemoryError oomerror)
        {
          new OOMWarning("When trying to open the Jmol viewer!", oomerror);
          Cache.log.debug("File locations are " + files);
        } catch (Exception ex)
        {
          Cache.log.error("Couldn't open Jmol viewer!", ex);
        }
      }
      else
      {
        StringBuffer cmd = new StringBuffer();
        cmd.append("loadingJalviewdata=true\nload APPEND ");
        cmd.append(files.toString());
        cmd.append("\nloadingJalviewdata=null");
        final String command = cmd.toString();
        cmd = null;
        lastnotify = jmb.getLoadNotifiesHandled();

        try
        {
          jmb.evalStateCommand(command);
        } catch (OutOfMemoryError oomerror)
        {
          new OOMWarning(
                  "When trying to add structures to the Jmol viewer!",
                  oomerror);
          Cache.log.debug("File locations are " + files);
        } catch (Exception ex)
        {
          Cache.log.error("Couldn't add files to Jmol viewer!", ex);
        }
      }

      // need to wait around until script has finished
      while (addingStructures ? lastnotify >= jmb.getLoadNotifiesHandled()
              : (!jmb.isFinishedInit() && jmb.getPdbFile() != null && jmb
                      .getPdbFile().length != jmb.getPdbCount()))
      {
        try
        {
          Cache.log.debug("Waiting around for jmb notify.");
          Thread.sleep(35);
        } catch (Exception e)
        {
        }
      }

      // refresh the sequence colours for the new structure(s)
      for (AlignmentPanel ap : _colourwith)
      {
        jmb.updateColours(ap);
      }
      // do superposition if asked to
      if (Cache.getDefault("AUTOSUPERIMPOSE", true) && alignAddedStructures)
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            if (jmb.viewer.isScriptExecuting())
            {
              javax.swing.SwingUtilities.invokeLater(this);
              try
              {
                Thread.sleep(5);
              } catch (InterruptedException q)
              {
              }
              ;
              return;
            }
            else
            {
              alignStructs_withAllAlignPanels();
            }
          }
        });
        alignAddedStructures = false;
      }
      addingStructures = false;

    }
    _started = false;
    worker = null;
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
            // ignore
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
    makePDBImage(jalview.util.ImageMaker.TYPE.EPS);
  }

  @Override
  public void png_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.TYPE.PNG);
  }

  void makePDBImage(jalview.util.ImageMaker.TYPE type)
  {
    int width = getWidth();
    int height = getHeight();

    jalview.util.ImageMaker im;

    if (type == jalview.util.ImageMaker.TYPE.PNG)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.PNG, "Make PNG image from view",
              width, height, null, null);
    }
    else if (type == jalview.util.ImageMaker.TYPE.EPS)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.EPS, "Make EPS file from view",
              width, height, null, this.getTitle());
    }
    else
    {

      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.SVG, "Make SVG file from PCA",
              width, height, null, this.getTitle());
    }

    if (im.getGraphics() != null)
    {
      jmb.viewer.renderScreenImage(im.getGraphics(), width, height);
      im.writeImage();
    }
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
        jmb.colourBySequence(ap);
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
              .openURL("http://jmol.sourceforge.net/docs/JmolUserGuide/");
    } catch (Exception ex)
    {
    }
  }

  public void showConsole(boolean showConsole)
  {

    if (showConsole)
    {
      if (splitPane == null)
      {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(renderPanel);
        splitPane.setBottomComponent(scriptWindow);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(getHeight() - 200);
        scriptWindow.setVisible(true);
        scriptWindow.validate();
        splitPane.validate();
      }

    }
    else
    {
      if (splitPane != null)
      {
        splitPane.setVisible(false);
      }

      splitPane = null;

      this.getContentPane().add(renderPanel, BorderLayout.CENTER);
    }

    validate();
  }

  class RenderPanel extends JPanel
  {
    final Dimension currentSize = new Dimension();

    @Override
    public void paintComponent(Graphics g)
    {
      getSize(currentSize);

      if (jmb != null && jmb.fileLoadingError != null)
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString(MessageManager.getString("label.error_loading_file")
                + "...", 20, currentSize.height / 2);
        StringBuffer sb = new StringBuffer();
        int lines = 0;
        for (int e = 0; e < jmb.getPdbCount(); e++)
        {
          sb.append(jmb.getPdbEntry(e).getId());
          if (e < jmb.getPdbCount() - 1)
          {
            sb.append(",");
          }

          if (e == jmb.getPdbCount() - 1 || sb.length() > 20)
          {
            lines++;
            g.drawString(sb.toString(), 20, currentSize.height / 2 - lines
                    * g.getFontMetrics().getHeight());
          }
        }
      }
      else if (jmb == null || jmb.viewer == null || !jmb.isFinishedInit())
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString(MessageManager.getString("label.retrieving_pdb_data"),
                20, currentSize.height / 2);
      }
      else
      {
        jmb.viewer.renderScreenImage(g, currentSize.width,
                currentSize.height);
      }
    }
  }

  public void updateTitleAndMenus()
  {
    if (jmb.fileLoadingError != null && jmb.fileLoadingError.length() > 0)
    {
      repaint();
      return;
    }
    setChainMenuItems(jmb.chainNames);

    this.setTitle(jmb.getViewerTitle());
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
    ;
    if (_alignwith.size() == 0)
    {
      _alignwith.add(getAlignmentPanel());
    }
    ;
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
    return this.jmb;
  }

  @Override
  public String getStateInfo()
  {
    return jmb == null ? null : jmb.viewer.getStateInfo();
  }

  @Override
  public ViewerType getViewerType()
  {
    return ViewerType.JMOL;
  }

  @Override
  protected AAStructureBindingModel getBindingModel()
  {
    return jmb;
  }

}
