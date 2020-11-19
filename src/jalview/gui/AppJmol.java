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

import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.StructureViewer.ViewerType;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.BrowserLauncher;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.dbsources.Pdb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class AppJmol extends StructureViewerBase
{
  // ms to wait for Jmol to load files
  private static final int JMOL_LOAD_TIMEOUT = 20000;

  private static final String SPACE = " ";

  private static final String QUOTE = "\"";

  AppJmolBinding jmb;

  JPanel scriptWindow;

  JSplitPane splitPane;

  RenderPanel renderPanel;

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
          boolean leaveColouringToJmol, String loadStatus, Rectangle bounds,
          String viewid)
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
            pdbentrys, seqs, null);

    jmb.setLoadingFromArchive(true);
    addAlignmentPanel(ap);
    if (useToAlign)
    {
      useAlignmentPanelForSuperposition(ap);
    }
    initMenus();
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
    setViewId(viewid);
    // jalview.gui.Desktop.addInternalFrame(this, "Loading File",
    // bounds.width,bounds.height);

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(
              InternalFrameEvent internalFrameEvent)
      {
        closeViewer(false);
      }
    });
    initJmol(loadStatus); // pdbentry, seq, JBPCHECK!
  }

  @Override
  protected void initMenus()
  {
    super.initMenus();

    viewerActionMenu.setText(MessageManager.getString("label.jmol"));

    viewerColour
            .setText(MessageManager.getString("label.colour_with_jmol"));
    viewerColour.setToolTipText(MessageManager
            .getString("label.let_jmol_manage_structure_colours"));
  }

  IProgressIndicator progressBar = null;

  @Override
  protected IProgressIndicator getIProgressIndicator()
  {
    return progressBar;
  }
  
  /**
   * display a single PDB structure in a new Jmol view
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

    openNewJmol(ap, alignAddedStructures, new PDBEntry[] { pdbentry },
            new SequenceI[][]
            { seq });
  }

  private void openNewJmol(AlignmentPanel ap, boolean alignAdded,
          PDBEntry[] pdbentrys,
          SequenceI[][] seqs)
  {
    progressBar = ap.alignFrame;
    jmb = new AppJmolBinding(this, ap.getStructureSelectionManager(),
            pdbentrys, seqs, null);
    addAlignmentPanel(ap);
    useAlignmentPanelForColourbyseq(ap);

    alignAddedStructures = alignAdded;
    useAlignmentPanelForSuperposition(ap);

    jmb.setColourBySequence(true);
    setSize(400, 400); // probably should be a configurable/dynamic default here
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
   * create a new Jmol containing several structures optionally superimposed
   * using the given alignPanel.
   * 
   * @param ap
   * @param alignAdded
   *          - true to superimpose
   * @param pe
   * @param seqs
   */
  public AppJmol(AlignmentPanel ap, boolean alignAdded, PDBEntry[] pe,
          SequenceI[][] seqs)
  {
    openNewJmol(ap, alignAdded, pe, seqs);
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
    jmb.evalStateCommand("set hoverDelay=0.1");
    jmb.setFinishedInit(true);
  }

  @Override
  void showSelectedChains()
  {
    Vector<String> toshow = new Vector<>();
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

  @Override
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

  @Override
  public void run()
  {
    _started = true;
    try
    {
      List<String> files = fetchPdbFiles();
      if (files.size() > 0)
      {
        showFilesInViewer(files);
      }
    } finally
    {
      _started = false;
      worker = null;
    }
  }

  /**
   * Either adds the given files to a structure viewer or opens a new viewer to
   * show them
   * 
   * @param files
   *          list of absolute paths to structure files
   */
  void showFilesInViewer(List<String> files)
  {
    long lastnotify = jmb.getLoadNotifiesHandled();
    StringBuilder fileList = new StringBuilder();
    for (String s : files)
    {
      fileList.append(SPACE).append(QUOTE)
              .append(Platform.escapeBackslashes(s)).append(QUOTE);
    }
    String filesString = fileList.toString();

    if (!addingStructures)
    {
      try
      {
        initJmol("load FILES " + filesString);
      } catch (OutOfMemoryError oomerror)
      {
        new OOMWarning("When trying to open the Jmol viewer!", oomerror);
        Cache.log.debug("File locations are " + filesString);
      } catch (Exception ex)
      {
        Cache.log.error("Couldn't open Jmol viewer!", ex);
      }
    }
    else
    {
      StringBuilder cmd = new StringBuilder();
      cmd.append("loadingJalviewdata=true\nload APPEND ");
      cmd.append(filesString);
      cmd.append("\nloadingJalviewdata=null");
      final String command = cmd.toString();
      lastnotify = jmb.getLoadNotifiesHandled();

      try
      {
        jmb.evalStateCommand(command);
      } catch (OutOfMemoryError oomerror)
      {
        new OOMWarning("When trying to add structures to the Jmol viewer!",
                oomerror);
        Cache.log.debug("File locations are " + filesString);
      } catch (Exception ex)
      {
        Cache.log.error("Couldn't add files to Jmol viewer!", ex);
      }
    }

    // need to wait around until script has finished
    int waitMax = JMOL_LOAD_TIMEOUT;
    int waitFor = 35;
    int waitTotal = 0;
    while (addingStructures ? lastnotify >= jmb.getLoadNotifiesHandled()
            : !(jmb.isFinishedInit() && jmb.getStructureFiles() != null
                    && jmb.getStructureFiles().length == files.size()))
    {
      try
      {
        Cache.log.debug("Waiting around for jmb notify.");
        Thread.sleep(waitFor);
        waitTotal += waitFor;
      } catch (Exception e)
      {
      }
      if (waitTotal > waitMax)
      {
        System.err.println("Timed out waiting for Jmol to load files after "
                + waitTotal + "ms");
        // System.err.println("finished: " + jmb.isFinishedInit()
        // + "; loaded: " + Arrays.toString(jmb.getPdbFile())
        // + "; files: " + files.toString());
        jmb.getStructureFiles();
        break;
      }
    }

    // refresh the sequence colours for the new structure(s)
    for (AlignmentPanel ap : _colourwith)
    {
      jmb.updateColours(ap);
    }
    // do superposition if asked to
    if (alignAddedStructures)
    {
      alignAddedStructures();
    }
    addingStructures = false;
  }

  /**
   * Queues a thread to align structures with Jalview alignments
   */
  void alignAddedStructures()
  {
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        if (jmb.viewer.isScriptExecuting())
        {
          SwingUtilities.invokeLater(this);
          try
          {
            Thread.sleep(5);
          } catch (InterruptedException q)
          {
          }
          return;
        }
        else
        {
          alignStructs_withAllAlignPanels();
        }
      }
    });

  }

  /**
   * Retrieves and saves as file any modelled PDB entries for which we do not
   * already have a file saved. Returns a list of absolute paths to structure
   * files which were either retrieved, or already stored but not modelled in
   * the structure viewer (i.e. files to add to the viewer display).
   * 
   * @return
   */
  List<String> fetchPdbFiles()
  {
    // todo - record which pdbids were successfully imported.
    StringBuilder errormsgs = new StringBuilder();

    List<String> files = new ArrayList<>();
    String pdbid = "";
    try
    {
      String[] filesInViewer = jmb.getStructureFiles();
      // TODO: replace with reference fetching/transfer code (validate PDBentry
      // as a DBRef?)
      Pdb pdbclient = new Pdb();
      for (int pi = 0; pi < jmb.getPdbCount(); pi++)
      {
        String file = jmb.getPdbEntry(pi).getFile();
        if (file == null)
        {
          // todo: extract block as method and pull up (also ChimeraViewFrame)
          // retrieve the pdb and store it locally
          AlignmentI pdbseq = null;
          pdbid = jmb.getPdbEntry(pi).getId();
          long hdl = pdbid.hashCode() - System.currentTimeMillis();
          if (progressBar != null)
          {
            progressBar.setProgressBar(MessageManager
                    .formatMessage("status.fetching_pdb", new String[]
                    { pdbid }), hdl);
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
            errormsgs.append("'").append(pdbid).append("'");
          } finally
          {
            if (progressBar != null)
            {
              progressBar.setProgressBar(
                      MessageManager.getString("label.state_completed"),
                      hdl);
            }
          }
          if (pdbseq != null)
          {
            // just transfer the file name from the first sequence's first
            // PDBEntry
            file = new File(pdbseq.getSequenceAt(0).getAllPDBEntries()
                    .elementAt(0).getFile()).getAbsolutePath();
            jmb.getPdbEntry(pi).setFile(file);
            files.add(file);
          }
          else
          {
            errormsgs.append("'").append(pdbid).append("' ");
          }
        }
        else
        {
          if (filesInViewer != null && filesInViewer.length > 0)
          {
            addingStructures = true; // already files loaded.
            for (int c = 0; c < filesInViewer.length; c++)
            {
              if (Platform.pathEquals(filesInViewer[c], file))
              {
                file = null;
                break;
              }
            }
          }
          if (file != null)
          {
            files.add(file);
          }
        }
      }
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB files: " + pdbid, oomerror);
    } catch (Exception ex)
    {
      ex.printStackTrace();
      errormsgs.append("When retrieving pdbfiles : current was: '")
              .append(pdbid).append("'");
    }
    if (errormsgs.length() > 0)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.pdb_entries_couldnt_be_retrieved", new String[]
                      { errormsgs.toString() }),
              MessageManager.getString("label.couldnt_load_file"),
              JvOptionPane.ERROR_MESSAGE);
    }
    return files;
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
              width, height, null, null, null, 0, false);
    }
    else if (type == jalview.util.ImageMaker.TYPE.EPS)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.EPS, "Make EPS file from view",
              width, height, null, this.getTitle(), null, 0, false);
    }
    else
    {

      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.SVG, "Make SVG file from PCA",
              width, height, null, this.getTitle(), null, 0, false);
    }

    if (im.getGraphics() != null)
    {
      jmb.viewer.renderScreenImage(im.getGraphics(), width, height);
      im.writeImage();
    }
  }

  @Override
  public void showHelp_actionPerformed(ActionEvent actionEvent)
  {
    try
    {
      BrowserLauncher
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

      if (jmb != null && jmb.hasFileLoadingError())
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
            g.drawString(sb.toString(), 20, currentSize.height / 2
                    - lines * g.getFontMetrics().getHeight());
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
  protected String getViewerName()
  {
    return "Jmol";
  }
}
