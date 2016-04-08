/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
package jalview.appletgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import jalview.api.SequenceStructureBinding;
import jalview.datamodel.*;
import jalview.structure.*;
import jalview.io.*;

import org.jmol.api.*;

import org.jmol.popup.*;
import org.jmol.viewer.JmolConstants;

import jalview.schemes.*;

public class AppletJmol extends EmbmenuFrame implements
// StructureListener,
        KeyListener, ActionListener, ItemListener, SequenceStructureBinding

{
  Menu fileMenu = new Menu("File");

  Menu viewMenu = new Menu("View");

  Menu coloursMenu = new Menu("Colours");

  Menu chainMenu = new Menu("Show Chain");

  Menu helpMenu = new Menu("Help");

  MenuItem mappingMenuItem = new MenuItem("View Mapping");

  CheckboxMenuItem seqColour = new CheckboxMenuItem("By Sequence", true);

  CheckboxMenuItem jmolColour = new CheckboxMenuItem("Using Jmol", false);
  
  MenuItem chain = new MenuItem("By Chain");

  MenuItem charge = new MenuItem("Charge & Cysteine");

  MenuItem zappo = new MenuItem("Zappo");

  MenuItem taylor = new MenuItem("Taylor");

  MenuItem hydro = new MenuItem("Hydrophobicity");

  MenuItem helix = new MenuItem("Helix Propensity");

  MenuItem strand = new MenuItem("Strand Propensity");

  MenuItem turn = new MenuItem("Turn Propensity");

  MenuItem buried = new MenuItem("Buried Index");

  MenuItem user = new MenuItem("User Defined Colours");

  MenuItem jmolHelp = new MenuItem("Jmol Help");

  Panel scriptWindow;

  TextField inputLine;

  TextArea history;

  RenderPanel renderPanel;

  AlignmentPanel ap;
  ArrayList _aps = new ArrayList();

  String fileLoadingError;

  boolean loadedInline;

  // boolean colourBySequence = true;

  FeatureRenderer fr = null;

  AppletJmolBinding jmb;

  /**
   * datasource protocol for access to PDBEntry
   */
  String protocol = null;

  /**
   * Load a bunch of pdb entries associated with sequences in the alignment and
   * display them - aligning them if necessary.
   * 
   * @param pdbentries
   *          each pdb file (at least one needed)
   * @param boundseqs
   *          each set of sequences for each pdb file (must match number of pdb
   *          files)
   * @param boundchains
   *          the target pdb chain corresponding with each sequence associated
   *          with each pdb file (may be null at any level)
   * @param align
   *          true/false
   * @param ap
   *          associated alignment
   * @param protocol
   *          how to get pdb data
   */
  public AppletJmol(PDBEntry[] pdbentries, SequenceI[][] boundseqs,
          String[][] boundchains, boolean align, AlignmentPanel ap,
          String protocol)
  {
    throw new Error("Not yet implemented.");
  }

  public AppletJmol(PDBEntry pdbentry, SequenceI[] seq, String[] chains,
          AlignmentPanel ap, String protocol)
  {
    this.ap = ap;
    jmb = new AppletJmolBinding(this, ap.getStructureSelectionManager(), new PDBEntry[]
    { pdbentry }, new SequenceI[][]
    { seq }, new String[][]
    { chains }, protocol);
    jmb.setColourBySequence(true);
    if (pdbentry.getId() == null || pdbentry.getId().length() < 1)
    {
      if (protocol.equals(AppletFormatAdapter.PASTE))
      {
        pdbentry.setId("PASTED PDB"
                + (chains == null ? "_" : chains.toString()));
      }
      else
      {
        pdbentry.setId(pdbentry.getFile());
      }
    }

    if (jalview.bin.JalviewLite.debug)
    {
      System.err
              .println("AppletJmol: PDB ID is '" + pdbentry.getId() + "'");
    }

    String alreadyMapped = StructureSelectionManager
            .getStructureSelectionManager(ap.av.applet).alreadyMappedToFile(
                    pdbentry.getId());
    MCview.PDBfile reader = null;
    if (alreadyMapped != null)
    {
      reader = StructureSelectionManager.getStructureSelectionManager(ap.av.applet)
              .setMapping(seq, chains, pdbentry.getFile(), protocol);
      // PROMPT USER HERE TO ADD TO NEW OR EXISTING VIEW?
      // FOR NOW, LETS JUST OPEN A NEW WINDOW
    }
    MenuBar menuBar = new MenuBar();
    menuBar.add(fileMenu);
    fileMenu.add(mappingMenuItem);
    menuBar.add(viewMenu);
    mappingMenuItem.addActionListener(this);
    viewMenu.add(chainMenu);
    menuBar.add(coloursMenu);
    menuBar.add(helpMenu);

    charge.addActionListener(this);
    hydro.addActionListener(this);
    chain.addActionListener(this);
    seqColour.addItemListener(this);
    jmolColour.addItemListener(this);
    zappo.addActionListener(this);
    taylor.addActionListener(this);
    helix.addActionListener(this);
    strand.addActionListener(this);
    turn.addActionListener(this);
    buried.addActionListener(this);
    user.addActionListener(this);
    
    jmolHelp.addActionListener(this);

    coloursMenu.add(seqColour);
    coloursMenu.add(chain);
    coloursMenu.add(charge);
    coloursMenu.add(zappo);
    coloursMenu.add(taylor);
    coloursMenu.add(hydro);
    coloursMenu.add(helix);
    coloursMenu.add(strand);
    coloursMenu.add(turn);
    coloursMenu.add(buried);
    coloursMenu.add(user);
    coloursMenu.add(jmolColour);
    helpMenu.add(jmolHelp);
    this.setLayout(new BorderLayout());

    setMenuBar(menuBar);

    renderPanel = new RenderPanel();
    embedMenuIfNeeded(renderPanel);
    this.add(renderPanel, BorderLayout.CENTER);
    scriptWindow = new Panel();
    scriptWindow.setVisible(false);
    // this.add(scriptWindow, BorderLayout.SOUTH);

    try
    {
      jmb.allocateViewer(renderPanel, true, ap.av.applet.getName()
              + "_jmol_", ap.av.applet.getDocumentBase(),
              ap.av.applet.getCodeBase(), "-applet", scriptWindow, null);
    } catch (Exception e)
    {
      System.err
              .println("Couldn't create a jmol viewer. Args to allocate viewer were:\nDocumentBase="
                      + ap.av.applet.getDocumentBase()
                      + "\nCodebase="
                      + ap.av.applet.getCodeBase());
      e.printStackTrace();
      dispose();
      return;
    }
    jmb.newJmolPopup(true, "Jmol", true);

    this.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent evt)
      {
        closeViewer();
      }
    });
    if (pdbentry.getProperty() == null)
    {
      pdbentry.setProperty(new Hashtable());
      pdbentry.getProperty().put("protocol", protocol);
    }
    if (pdbentry.getFile() != null)
    {
      // import structure data from pdbentry.getFile based on given protocol
      if (protocol.equals(AppletFormatAdapter.PASTE))
      {
        // TODO: JAL-623 : correctly record file contents for matching up later
        // pdbentry.getProperty().put("pdbfilehash",""+pdbentry.getFile().hashCode());
        loadInline(pdbentry.getFile());
      }
      else if (protocol.equals(AppletFormatAdapter.FILE)
              || protocol.equals(AppletFormatAdapter.URL))
      {
        jmb.viewer.openFile(pdbentry.getFile());
      }
      else
      {
        // probably CLASSLOADER based datasource..
        // Try and get a reader on the datasource, and pass that to Jmol
        try
        {
          java.io.Reader freader = null;
          if (reader != null)
          {
            if (jalview.bin.JalviewLite.debug)
            {
              System.err
                      .println("AppletJmol:Trying to reuse existing PDBfile IO parser.");
            }
            // re-use the one we opened earlier
            freader = reader.getReader();
          }
          if (freader == null)
          {
            if (jalview.bin.JalviewLite.debug)
            {
              System.err
                      .println("AppletJmol:Creating new PDBfile IO parser.");
            }
            FileParse fp = new FileParse(pdbentry.getFile(), protocol);
            fp.mark();
            // reader = new MCview.PDBfile(fp);
            // could set ID, etc.
            // if (!reader.isValid())
            // {
            // throw new Exception("Invalid datasource.
            // "+reader.getWarningMessage());
            // }
            // fp.reset();
            freader = fp.getReader();
          }
          if (freader == null)
          {
            throw new Exception(
                    "Invalid datasource. Could not obtain Reader.");
          }
          jmb.viewer.openReader(pdbentry.getFile(), pdbentry.getId(),
                  freader);
        } catch (Exception e)
        {
          // give up!
          System.err.println("Couldn't access pdbentry id="
                  + pdbentry.getId() + " and file=" + pdbentry.getFile()
                  + " using protocol=" + protocol);
          e.printStackTrace();
        }
      }
    }

    jalview.bin.JalviewLite.addFrame(this, jmb.getViewerTitle(), 400, 400);
  }

  public void loadInline(String string)
  {
    loadedInline = true;
    jmb.loadInline(string);
  }

  void setChainMenuItems(Vector chains)
  {
    chainMenu.removeAll();

    MenuItem menuItem = new MenuItem("All");
    menuItem.addActionListener(this);

    chainMenu.add(menuItem);

    CheckboxMenuItem menuItemCB;
    for (int c = 0; c < chains.size(); c++)
    {
      menuItemCB = new CheckboxMenuItem(chains.elementAt(c).toString(),
              true);
      menuItemCB.addItemListener(this);
      chainMenu.add(menuItemCB);
    }
  }

  boolean allChainsSelected = false;

  void centerViewer()
  {
    Vector toshow = new Vector();
    String lbl;
    int mlength, p, mnum;
    for (int i = 0; i < chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof CheckboxMenuItem)
      {
        CheckboxMenuItem item = (CheckboxMenuItem) chainMenu.getItem(i);
        if (item.getState())
        {
          toshow.addElement(item.getLabel());
        }
      }
    }
    jmb.centerViewer(toshow);
  }

  void closeViewer()
  {
    jmb.closeViewer();
    jmb = null;
    this.setVisible(false);
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == mappingMenuItem)
    {
      jalview.appletgui.CutAndPasteTransfer cap = new jalview.appletgui.CutAndPasteTransfer(
              false, null);
      Frame frame = new Frame();
      frame.add(cap);

      StringBuffer sb = new StringBuffer();
      try
      {
        for (int s = 0; s < jmb.pdbentry.length; s++)
        {
          sb.append(jmb.printMapping(
                          jmb.pdbentry[s].getFile()));
          sb.append("\n");
        }
        cap.setText(sb.toString());
      } catch (OutOfMemoryError ex)
      {
        frame.dispose();
        System.err
                .println("Out of memory when trying to create dialog box with sequence-structure mapping.");
        return;
      }
      jalview.bin.JalviewLite.addFrame(frame, "PDB - Sequence Mapping",
              550, 600);
    }
    else if (evt.getSource() == charge)
    {
      setEnabled(charge);
      jmb.colourByCharge();
    }

    else if (evt.getSource() == chain)
    {
      setEnabled(chain);
      jmb.colourByChain();
    }
    else if (evt.getSource() == zappo)
    {
      setEnabled(zappo);
      jmb.setJalviewColourScheme(new ZappoColourScheme());
    }
    else if (evt.getSource() == taylor)
    {
      setEnabled(taylor);
      jmb.setJalviewColourScheme(new TaylorColourScheme());
    }
    else if (evt.getSource() == hydro)
    {
      setEnabled(hydro);
      jmb.setJalviewColourScheme(new HydrophobicColourScheme());
    }
    else if (evt.getSource() == helix)
    {
      setEnabled(helix);
      jmb.setJalviewColourScheme(new HelixColourScheme());
    }
    else if (evt.getSource() == strand)
    {
      setEnabled(strand);
      jmb.setJalviewColourScheme(new StrandColourScheme());
    }
    else if (evt.getSource() == turn)
    {
      setEnabled(turn);
      jmb.setJalviewColourScheme(new TurnColourScheme());
    }
    else if (evt.getSource() == buried)
    {
      setEnabled(buried);
      jmb.setJalviewColourScheme(new BuriedColourScheme());
    }
    else if (evt.getSource() == user)
    {
      setEnabled(user);
      new UserDefinedColours(this);
    }
    else if (evt.getSource() == jmolHelp)
    {
      try
      {
        ap.av.applet.getAppletContext().showDocument(
                new java.net.URL(
                        "http://jmol.sourceforge.net/docs/JmolUserGuide/"),
                "jmolHelp");
      } catch (java.net.MalformedURLException ex)
      {
      }
    }
    else
    {
      allChainsSelected = true;
      for (int i = 0; i < chainMenu.getItemCount(); i++)
      {
        if (chainMenu.getItem(i) instanceof CheckboxMenuItem)
          ((CheckboxMenuItem) chainMenu.getItem(i)).setState(true);
      }

      centerViewer();
      allChainsSelected = false;
    }
  }

  /**
   * tick or untick the seqColour menu entry or jmoColour entry depending upon if it was selected
   * or not.
   * 
   * @param itm
   */
  private void setEnabled(MenuItem itm)
  {
    jmolColour.setState(itm == jmolColour);
    seqColour.setState(itm == seqColour);
    jmb.setColourBySequence(itm == seqColour);
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == jmolColour)
    {
      setEnabled(jmolColour);
      jmb.setColourBySequence(false);
    } else
    if (evt.getSource() == seqColour)
    {
      setEnabled(seqColour);
      jmb.colourBySequence(ap.av.getShowSequenceFeatures(), ap);
    }
    else if (!allChainsSelected)
      centerViewer();
  }

  public void keyPressed(KeyEvent evt)
  {
    if (evt.getKeyCode() == KeyEvent.VK_ENTER && scriptWindow.isVisible())
    {
      jmb.eval(inputLine.getText());
      history.append("\n$ " + inputLine.getText());
      inputLine.setText("");
    }

  }

  public void keyTyped(KeyEvent evt)
  {
  }

  public void keyReleased(KeyEvent evt)
  {
  }

  public void updateColours(Object source)
  {
    AlignmentPanel ap = (AlignmentPanel) source;
    jmb.colourBySequence(ap.av.getShowSequenceFeatures(), ap);
  }

  public void updateTitleAndMenus()
  {
    if (jmb.fileLoadingError != null && jmb.fileLoadingError.length() > 0)
    {
      repaint();
      return;
    }
    setChainMenuItems(jmb.chainNames);
    jmb.colourBySequence(ap.av.getShowSequenceFeatures(), ap);

    setTitle(jmb.getViewerTitle());
  }

  public void showUrl(String url)
  {
    try
    {
      ap.av.applet.getAppletContext().showDocument(new java.net.URL(url),
              "jmolOutput");
    } catch (java.net.MalformedURLException ex)
    {
    }
  }

  Panel splitPane = null;

  public void showConsole(boolean showConsole)
  {
    if (showConsole)
    {
      remove(renderPanel);
      splitPane = new Panel();

      splitPane.setLayout(new java.awt.GridLayout(2, 1));
      splitPane.add(renderPanel);
      splitPane.add(scriptWindow);
      scriptWindow.setVisible(true);
      this.add(splitPane, BorderLayout.CENTER);
      splitPane.setVisible(true);
      splitPane.validate();
    }
    else
    {
      scriptWindow.setVisible(false);
      remove(splitPane);
      add(renderPanel, BorderLayout.CENTER);
      splitPane = null;
    }
    validate();
  }

  public float[][] functionXY(String functionName, int x, int y)
  {
    return null;
  }

  // /End JmolStatusListener
  // /////////////////////////////

  class RenderPanel extends Panel
  {
    Dimension currentSize = new Dimension();

    Rectangle rectClip = new Rectangle();

    public void update(Graphics g)
    {
      paint(g);
    }

    public void paint(Graphics g)
    {
      currentSize = this.getSize();
      rectClip = g.getClipBounds();

      if (jmb.viewer == null)
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString("Retrieving PDB data....", 20, currentSize.height / 2);
      }
      else
      {
        jmb.viewer.renderScreenImage(g, currentSize, rectClip);
      }
    }
  }

  /*
   * @Override public Color getColour(int atomIndex, int pdbResNum, String
   * chain, String pdbId) { return jmb.getColour(atomIndex, pdbResNum, chain,
   * pdbId); }
   * 
   * @Override public String[] getPdbFile() { return jmb.getPdbFile(); }
   * 
   * @Override public void highlightAtom(int atomIndex, int pdbResNum, String
   * chain, String pdbId) { jmb.highlightAtom(atomIndex, pdbResNum, chain,
   * pdbId);
   * 
   * }
   * 
   * @Override public void mouseOverStructure(int atomIndex, String strInfo) {
   * jmb.mouseOverStructure(atomIndex, strInfo);
   * 
   * }
   */
  public void setJalviewColourScheme(UserColourScheme ucs)
  {
    jmb.setJalviewColourScheme(ucs);
  }

  public AlignmentPanel getAlignmentPanelFor(AlignmentI alignment)
  {
    for (int i=0;i<_aps.size();i++)
    {
      if (((AlignmentPanel)_aps.get(i)).av.getAlignment()==alignment)
      {
        return ((AlignmentPanel)_aps.get(i));
      }
    }
    return ap;
  }
}
