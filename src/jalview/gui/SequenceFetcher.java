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
package jalview.gui;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import MCview.*;
import jalview.datamodel.*;
import jalview.datamodel.xdb.embl.*;
import java.io.File;
import jalview.io.*;
import jalview.ws.DBRefFetcher;
import jalview.ws.ebi.EBIFetchClient;
import jalview.ws.seqfetcher.ASequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class SequenceFetcher extends JPanel implements Runnable
{
  // ASequenceFetcher sfetch;
  JInternalFrame frame;

  IProgressIndicator guiWindow;

  AlignFrame alignFrame;

  StringBuffer result;

  final String noDbSelected = "-- Select Database --";

  Hashtable sources = new Hashtable();

  private static jalview.ws.SequenceFetcher sfetch = null;

  private static String dasRegistry = null;

  private static boolean _initingFetcher = false;

  private static Thread initingThread = null;

  /**
   * Blocking method that initialises and returns the shared instance of the
   * SequenceFetcher client
   * 
   * @param guiWindow
   *          - where the initialisation delay message should be shown
   * @return the singleton instance of the sequence fetcher client
   */
  public static jalview.ws.SequenceFetcher getSequenceFetcherSingleton(
          final IProgressIndicator guiWindow)
  {
    if (_initingFetcher && initingThread != null && initingThread.isAlive())
    {
      if (guiWindow != null)
      {
        guiWindow.setProgressBar(
                "Waiting for Sequence Database Fetchers to initialise",
                Thread.currentThread().hashCode());
      }
      // initting happening on another thread - so wait around to see if it
      // finishes.
      while (_initingFetcher && initingThread != null
              && initingThread.isAlive())
      {
        try
        {
          Thread.sleep(10);
        } catch (Exception e)
        {
        }
        ;
      }
      if (guiWindow != null)
      {
        guiWindow.setProgressBar(
                "Waiting for Sequence Database Fetchers to initialise",
                Thread.currentThread().hashCode());
      }
    }
    if (sfetch == null
            || dasRegistry != DasSourceBrowser.getDasRegistryURL())
    {
      _initingFetcher = true;
      initingThread = Thread.currentThread();
      /**
       * give a visual indication that sequence fetcher construction is occuring
       */
      if (guiWindow != null)
      {
        guiWindow.setProgressBar("Initialising Sequence Database Fetchers",
                Thread.currentThread().hashCode());
      }
      dasRegistry = DasSourceBrowser.getDasRegistryURL();
      jalview.ws.SequenceFetcher sf = new jalview.ws.SequenceFetcher();
      if (guiWindow != null)
      {
        guiWindow.setProgressBar("Initialising Sequence Database Fetchers",
                Thread.currentThread().hashCode());
      }
      sfetch = sf;
      _initingFetcher = false;
      initingThread = null;
    }
    return sfetch;
  }

  public SequenceFetcher(IProgressIndicator guiIndic)
  {
    final IProgressIndicator guiWindow = guiIndic;
    final SequenceFetcher us = this;
    // launch initialiser thread
    Thread sf = new Thread(new Runnable()
    {

      public void run()
      {
        if (getSequenceFetcherSingleton(guiWindow) != null)
        {
          us.initGui(guiWindow);
        }
        else
        {
          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              JOptionPane
                      .showInternalMessageDialog(
                              Desktop.desktop,
                              "Could not create the sequence fetcher client. Check error logs for details.",
                              "Couldn't create SequenceFetcher",
                              JOptionPane.ERROR_MESSAGE);
            }
          });

          // raise warning dialog
        }
      }
    });
    sf.start();
  }

  /**
   * called by thread spawned by constructor
   * 
   * @param guiWindow
   */
  private void initGui(IProgressIndicator guiWindow)
  {
    this.guiWindow = guiWindow;
    if (guiWindow instanceof AlignFrame)
    {
      alignFrame = (AlignFrame) guiWindow;
    }

    database.addItem(noDbSelected);
    /*
     * Dynamically generated database list will need a translation function from
     * internal source to externally distinct names. UNIPROT and UP_NAME are
     * identical DB sources, and should be collapsed.
     */

    String dbs[] = sfetch.getOrderedSupportedSources();
    for (int i = 0; i < dbs.length; i++)
    {
      if (!sources.containsValue(dbs[i]))
      {
        String name = sfetch.getSourceProxy(dbs[i]).getDbName();
        // duplicate source names are thrown away, here.
        if (!sources.containsKey(name))
        {
          database.addItem(name);
        }
        // overwrite with latest version of the retriever for this source
        sources.put(name, dbs[i]);
      }
    }
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    frame = new JInternalFrame();
    frame.setContentPane(this);
    if (new jalview.util.Platform().isAMac())
    {
      Desktop.addInternalFrame(frame, getFrameTitle(), 400, 240);
    }
    else
    {
      Desktop.addInternalFrame(frame, getFrameTitle(), 400, 180);
    }
  }

  private String getFrameTitle()
  {
    return ((alignFrame == null) ? "New " : "Additional ")
            + "Sequence Fetcher";
  }

  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout2);

    database.setFont(JvSwingUtils.getLabelFont());
    dbeg.setFont(new java.awt.Font("Verdana", Font.BOLD, 11));
    jLabel1.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText("Separate multiple accession ids with semi colon \";\"");

    replacePunctuation.setHorizontalAlignment(SwingConstants.CENTER);
    replacePunctuation
            .setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    replacePunctuation.setText("Replace commas with semi-colons");
    ok.setText("OK");
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });
    clear.setText("Clear");
    clear.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        clear_actionPerformed();
      }
    });

    example.setText("Example");
    example.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        example_actionPerformed();
      }
    });
    close.setText("Close");
    close.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
    textArea.setFont(JvSwingUtils.getLabelFont());
    textArea.setLineWrap(true);
    textArea.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          ok_actionPerformed();
      }
    });
    jPanel3.setLayout(borderLayout1);
    borderLayout1.setVgap(5);
    jPanel1.add(ok);
    jPanel1.add(example);
    jPanel1.add(clear);
    jPanel1.add(close);
    jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);
    jPanel2.setLayout(borderLayout3);

    database.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        DbSourceProxy db = null;
        try
        {
          db = sfetch.getSourceProxy((String) sources.get(database
                  .getSelectedItem()));
          String eq = db.getTestQuery();
          dbeg.setText("Example query: " + eq);
          replacePunctuation.setEnabled(!(eq != null && eq.indexOf(",") > -1));
        } catch (Exception ex)
        {
          dbeg.setText("");
          replacePunctuation.setEnabled(true);
        }
        jPanel2.repaint();
      }
    });
    dbeg.setText("");
    jPanel2.add(database, java.awt.BorderLayout.NORTH);
    jPanel2.add(dbeg, java.awt.BorderLayout.CENTER);
    JPanel jPanel2a = new JPanel(new BorderLayout());
    jPanel2a.add(jLabel1, java.awt.BorderLayout.NORTH);
    jPanel2a.add(replacePunctuation, java.awt.BorderLayout.SOUTH);
    jPanel2.add(jPanel2a, java.awt.BorderLayout.SOUTH);
    // jPanel2.setPreferredSize(new Dimension())
    jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);
    this.add(jPanel2, java.awt.BorderLayout.NORTH);
    jScrollPane1.getViewport().add(textArea);

  }

  protected void example_actionPerformed()
  {
    DbSourceProxy db = null;
    try
    {
      db = sfetch.getSourceProxy((String) sources.get(database
              .getSelectedItem()));
      textArea.setText(db.getTestQuery());
    } catch (Exception ex)
    {
    }
    jPanel3.repaint();
  }

  protected void clear_actionPerformed()
  {
    textArea.setText("");
    jPanel3.repaint();
  }

  JLabel dbeg = new JLabel();

  JComboBox database = new JComboBox();

  JLabel jLabel1 = new JLabel();

  JCheckBox replacePunctuation = new JCheckBox();

  JButton ok = new JButton();

  JButton clear = new JButton();

  JButton example = new JButton();

  JButton close = new JButton();

  JPanel jPanel1 = new JPanel();

  JTextArea textArea = new JTextArea();

  JScrollPane jScrollPane1 = new JScrollPane();

  JPanel jPanel2 = new JPanel();

  JPanel jPanel3 = new JPanel();

  JPanel jPanel4 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  BorderLayout borderLayout3 = new BorderLayout();

  public void close_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  public void ok_actionPerformed()
  {
    database.setEnabled(false);
    textArea.setEnabled(false);
    ok.setEnabled(false);
    close.setEnabled(false);

    Thread worker = new Thread(this);
    worker.start();
  }

  private void resetDialog()
  {
    database.setEnabled(true);
    textArea.setEnabled(true);
    ok.setEnabled(true);
    close.setEnabled(true);
  }

  public void run()
  {
    String error = "";
    if (database.getSelectedItem().equals(noDbSelected))
    {
      error += "Please select the source database\n";
    }
    // TODO: make this transformation more configurable
    com.stevesoft.pat.Regex empty;
    if (replacePunctuation.isEnabled() && replacePunctuation.isSelected())
    {
      empty = new com.stevesoft.pat.Regex(
      // replace commas and spaces with a semicolon
              "(\\s|[,; ])+", ";");
    }
    else
    {
      // just turn spaces and semicolons into single semicolons
      empty = new com.stevesoft.pat.Regex("(\\s|[; ])+", ";");
    }
    textArea.setText(empty.replaceAll(textArea.getText()));
    // see if there's anthing to search with
    if (!new com.stevesoft.pat.Regex("[A-Za-z0-9_.]").search(textArea
            .getText()))
    {
      error += "Please enter a (semi-colon separated list of) database id(s)";
    }
    if (error.length() > 0)
    {
      showErrorMessage(error);
      resetDialog();
      return;
    }
    ArrayList<String> aresultq=new ArrayList<String>();
    ArrayList<AlignmentI> aresult = new ArrayList<AlignmentI>();
    Object source = database.getSelectedItem();
    Enumeration en = new StringTokenizer(textArea.getText(), ";");
    boolean isAliSource=false;
    try
    {
      guiWindow.setProgressBar(
              "Fetching Sequences from " + database.getSelectedItem(),
              Thread.currentThread().hashCode());
      DbSourceProxy proxy = sfetch.getSourceProxy((String) sources
              .get(source));
      isAliSource = proxy.isA(DBRefSource.ALIGNMENTDB);
      if (proxy.getAccessionSeparator() == null)
      {
        while (en.hasMoreElements())
        {
          String item = (String) en.nextElement();
          try
          {
            if (aresult != null)
            {
              try
              {
                // give the server a chance to breathe
                Thread.sleep(5);
              } catch (Exception e)
              {
                //
              }

            }

            AlignmentI indres = null;
            try
            {
              indres = proxy.getSequenceRecords(item);
            } catch (OutOfMemoryError oome)
            {
              new OOMWarning("fetching " + item + " from "
                      + database.getSelectedItem(), oome, this);
            }
            if (indres != null)
            {
              aresultq.add(item);
              aresult.add(indres);
            }
          } catch (Exception e)
          {
            jalview.bin.Cache.log.info("Error retrieving " + item
                    + " from " + source, e);
          }
        }
      }
      else
      {
        StringBuffer multiacc = new StringBuffer();
        while (en.hasMoreElements())
        {
          multiacc.append(en.nextElement());
          if (en.hasMoreElements())
          {
            multiacc.append(proxy.getAccessionSeparator());
          }
        }
        try
        {
          aresultq.add(multiacc.toString());
          aresult.add(proxy.getSequenceRecords(multiacc.toString()));
        } catch (OutOfMemoryError oome)
        {
          new OOMWarning("fetching " + multiacc + " from "
                  + database.getSelectedItem(), oome, this);
        }

      }

    } catch (Exception e)
    {
      showErrorMessage("Error retrieving " + textArea.getText() + " from "
              + database.getSelectedItem());
      // error +="Couldn't retrieve sequences from "+database.getSelectedItem();
      System.err.println("Retrieval failed for source ='"
              + database.getSelectedItem() + "' and query\n'"
              + textArea.getText() + "'\n");
      e.printStackTrace();
    } catch (OutOfMemoryError e)
    {
      // resets dialog box - so we don't use OOMwarning here.
      showErrorMessage("Out of Memory when retrieving "
              + textArea.getText()
              + " from "
              + database.getSelectedItem()
              + "\nPlease see the Jalview FAQ for instructions for increasing the memory available to Jalview.\n");
      e.printStackTrace();
    } catch (Error e)
    {
      showErrorMessage("Serious Error retrieving " + textArea.getText()
              + " from " + database.getSelectedItem());
      e.printStackTrace();
    }
    if (aresult != null && aresult.size()>0)
    {
      AlignmentI ar=null;
      if (isAliSource) {
        // new window for each result
        while (aresult.size()>0)
        {
          parseResult(aresult.remove(0), aresultq.remove(0)+" "+getDefaultRetrievalTitle(), null);
        }
      } else {
        // concatenate all results in one window
        while (aresult.size()>0)
        {
          if (ar==null) { ar = aresult.remove(0);}
          else { ar.append(aresult.remove(0)); };
        }
        parseResult(ar, null, null);
      } 
    }
    // only remove visual delay after we finished parsing.
    guiWindow.setProgressBar(null, Thread.currentThread().hashCode());
    resetDialog();
  }

  /*
   * result = new StringBuffer(); if
   * (database.getSelectedItem().equals("Uniprot")) {
   * getUniprotFile(textArea.getText()); } else if
   * (database.getSelectedItem().equals("EMBL") ||
   * database.getSelectedItem().equals("EMBLCDS")) { String DBRefSource =
   * database.getSelectedItem().equals("EMBLCDS") ?
   * jalview.datamodel.DBRefSource.EMBLCDS : jalview.datamodel.DBRefSource.EMBL;
   * 
   * StringTokenizer st = new StringTokenizer(textArea.getText(), ";");
   * SequenceI[] seqs = null; while(st.hasMoreTokens()) { EBIFetchClient dbFetch
   * = new EBIFetchClient(); String qry =
   * database.getSelectedItem().toString().toLowerCase( ) + ":" +
   * st.nextToken(); File reply = dbFetch.fetchDataAsFile( qry, "emblxml",null);
   * 
   * jalview.datamodel.xdb.embl.EmblFile efile=null; if (reply != null &&
   * reply.exists()) { efile =
   * jalview.datamodel.xdb.embl.EmblFile.getEmblFile(reply); } if (efile!=null)
   * { for (Iterator i=efile.getEntries().iterator(); i.hasNext(); ) { EmblEntry
   * entry = (EmblEntry) i.next(); SequenceI[] seqparts =
   * entry.getSequences(false,true, DBRefSource); if (seqparts!=null) {
   * SequenceI[] newseqs = null; int si=0; if (seqs==null) { newseqs = new
   * SequenceI[seqparts.length]; } else { newseqs = new
   * SequenceI[seqs.length+seqparts.length];
   * 
   * for (;si<seqs.length; si++) { newseqs[si] = seqs[si]; seqs[si] = null; } }
   * for (int j=0;j<seqparts.length; si++, j++) { newseqs[si] =
   * seqparts[j].deriveSequence(); // place DBReferences on dataset and refer }
   * seqs=newseqs; } } } else { result.append("# no response for "+qry); } } if
   * (seqs!=null && seqs.length>0) { if (parseResult(new Alignment(seqs), null,
   * null)!=null) { result.append("# Successfully parsed the
   * "+database.getSelectedItem()+" Queries into an Alignment"); } } } else if
   * (database.getSelectedItem().equals("PDB")) { StringTokenizer qset = new
   * StringTokenizer(textArea.getText(), ";"); String query; SequenceI[] seqs =
   * null; while (qset.hasMoreTokens() && ((query = qset.nextToken())!=null)) {
   * SequenceI[] seqparts = getPDBFile(query.toUpperCase()); if (seqparts !=
   * null) { if (seqs == null) { seqs = seqparts; } else { SequenceI[] newseqs =
   * new SequenceI[seqs.length+seqparts.length]; int i=0; for (; i <
   * seqs.length; i++) { newseqs[i] = seqs[i]; seqs[i] = null; } for (int
   * j=0;j<seqparts.length; i++, j++) { newseqs[i] = seqparts[j]; }
   * seqs=newseqs; } result.append("# Success for "+query.toUpperCase()+"\n"); }
   * } if (seqs != null && seqs.length > 0) { if (parseResult(new
   * Alignment(seqs), null, null)!=null) { result.append( "# Successfully parsed
   * the PDB File Queries into an
   * Alignment"); } } } else if( database.getSelectedItem().equals("PFAM")) {
   * try { result.append(new FastaFile(
   * "http://www.sanger.ac.uk/cgi-bin/Pfam/getalignment.pl?format=fal&acc=" +
   * textArea.getText().toUpperCase(), "URL").print() );
   * 
   * if(result.length()>0) { parseResult( result.toString(),
   * textArea.getText().toUpperCase() ); } } catch (java.io.IOException ex) {
   * result = null; } }
   * 
   * if (result == null || result.length() == 0) { showErrorMessage("Error
   * retrieving " + textArea.getText() + " from " + database.getSelectedItem());
   * }
   * 
   * resetDialog(); return; }
   * 
   * void getUniprotFile(String id) { EBIFetchClient ebi = new EBIFetchClient();
   * File file = ebi.fetchDataAsFile("uniprot:" + id, "xml", null);
   * 
   * DBRefFetcher dbref = new DBRefFetcher(); Vector entries =
   * dbref.getUniprotEntries(file);
   * 
   * if (entries != null) { //First, make the new sequences Enumeration en =
   * entries.elements(); while (en.hasMoreElements()) { UniprotEntry entry =
   * (UniprotEntry) en.nextElement();
   * 
   * StringBuffer name = new StringBuffer(">UniProt/Swiss-Prot"); Enumeration
   * en2 = entry.getAccession().elements(); while (en2.hasMoreElements()) {
   * name.append("|"); name.append(en2.nextElement()); } en2 =
   * entry.getName().elements(); while (en2.hasMoreElements()) {
   * name.append("|"); name.append(en2.nextElement()); }
   * 
   * if (entry.getProtein() != null) { name.append(" " +
   * entry.getProtein().getName().elementAt(0)); }
   * 
   * result.append(name + "\n" + entry.getUniprotSequence().getContent() +
   * "\n"); }
   * 
   * //Then read in the features and apply them to the dataset Alignment al =
   * parseResult(result.toString(), null); for (int i = 0; i < entries.size();
   * i++) { UniprotEntry entry = (UniprotEntry) entries.elementAt(i);
   * Enumeration e = entry.getDbReference().elements(); Vector onlyPdbEntries =
   * new Vector(); while (e.hasMoreElements()) { PDBEntry pdb = (PDBEntry)
   * e.nextElement(); if (!pdb.getType().equals("PDB")) { continue; }
   * 
   * onlyPdbEntries.addElement(pdb); }
   * 
   * Enumeration en2 = entry.getAccession().elements(); while
   * (en2.hasMoreElements()) {
   * al.getSequenceAt(i).getDatasetSequence().addDBRef(new DBRefEntry(
   * DBRefSource.UNIPROT, "0", en2.nextElement().toString())); }
   * 
   * 
   * 
   * 
   * al.getSequenceAt(i).getDatasetSequence().setPDBId(onlyPdbEntries); if
   * (entry.getFeature() != null) { e = entry.getFeature().elements(); while
   * (e.hasMoreElements()) { SequenceFeature sf = (SequenceFeature)
   * e.nextElement(); sf.setFeatureGroup("Uniprot");
   * al.getSequenceAt(i).getDatasetSequence().addSequenceFeature( sf ); } } } }
   * }
   * 
   * SequenceI[] getPDBFile(String id) { Vector result = new Vector(); String
   * chain = null; if (id.indexOf(":") > -1) { chain =
   * id.substring(id.indexOf(":") + 1); id = id.substring(0, id.indexOf(":")); }
   * 
   * EBIFetchClient ebi = new EBIFetchClient(); String file =
   * ebi.fetchDataAsFile("pdb:" + id, "pdb", "raw"). getAbsolutePath(); if (file
   * == null) { return null; } try { PDBfile pdbfile = new PDBfile(file,
   * jalview.io.AppletFormatAdapter.FILE); for (int i = 0; i <
   * pdbfile.chains.size(); i++) { if (chain == null || ( (PDBChain)
   * pdbfile.chains.elementAt(i)).id. toUpperCase().equals(chain)) { PDBChain
   * pdbchain = (PDBChain) pdbfile.chains.elementAt(i); // Get the Chain's
   * Sequence - who's dataset includes any special features added from the PDB
   * file SequenceI sq = pdbchain.sequence; // Specially formatted name for the
   * PDB chain sequences retrieved from the PDB
   * sq.setName("PDB|"+id+"|"+sq.getName()); // Might need to add more metadata
   * to the PDBEntry object // like below /* PDBEntry entry = new PDBEntry(); //
   * Construct the PDBEntry entry.setId(id); if (entry.getProperty() == null)
   * entry.setProperty(new Hashtable()); entry.getProperty().put("chains",
   * pdbchain.id + "=" + sq.getStart() + "-" + sq.getEnd());
   * sq.getDatasetSequence().addPDBId(entry); // Add PDB DB Refs // We make a
   * DBRefEtntry because we have obtained the PDB file from a verifiable source
   * // JBPNote - PDB DBRefEntry should also carry the chain and mapping
   * information DBRefEntry dbentry = new
   * DBRefEntry(jalview.datamodel.DBRefSource.PDB, "0", id + pdbchain.id);
   * sq.addDBRef(dbentry); // and add seuqence to the retrieved set
   * result.addElement(sq.deriveSequence()); } }
   * 
   * if (result.size() < 1) { throw new Exception("WsDBFetch for PDB id resulted
   * in zero result size"); } } catch (Exception ex) // Problem parsing PDB file
   * { jalview.bin.Cache.log.warn("Exception when retrieving " +
   * textArea.getText() + " from " + database.getSelectedItem(), ex); return
   * null; }
   * 
   * 
   * SequenceI[] results = new SequenceI[result.size()]; for (int i = 0, j =
   * result.size(); i < j; i++) { results[i] = (SequenceI) result.elementAt(i);
   * result.setElementAt(null,i); } return results; }
   */
  AlignmentI parseResult(String result, String title)
  {
    String format = new IdentifyFile().Identify(result, "Paste");
    Alignment sequences = null;
    if (FormatAdapter.isValidFormat(format))
    {
      sequences = null;
      try
      {
        sequences = new FormatAdapter().readFile(result.toString(),
                "Paste", format);
      } catch (Exception ex)
      {
      }

      if (sequences != null)
      {
        return parseResult(sequences, title, format);
      }
    }
    else
    {
      showErrorMessage("Error retrieving " + textArea.getText() + " from "
              + database.getSelectedItem());
    }

    return null;
  }

  /**
   * 
   * @return a standard title for any results retrieved using the currently selected source and settings
   */
  public String getDefaultRetrievalTitle() {
    return "Retrieved from " + database.getSelectedItem();
  }
  AlignmentI parseResult(AlignmentI al, String title,
          String currentFileFormat)
  {

    if (al != null && al.getHeight() > 0)
    {
      if (alignFrame == null)
      {
        AlignFrame af = new AlignFrame(al, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
        if (currentFileFormat != null)
        {
          af.currentFileFormat = currentFileFormat; // WHAT IS THE DEFAULT
          // FORMAT FOR
          // NON-FormatAdapter Sourced
          // Alignments?
        }

        if (title == null)
        {
          title = getDefaultRetrievalTitle();
        }
        SequenceFeature[] sfs = null;
        for (Enumeration sq = al.getSequences().elements(); sq
                .hasMoreElements();)
        {
          if ((sfs = ((SequenceI) sq.nextElement()).getDatasetSequence()
                  .getSequenceFeatures()) != null)
          {
            if (sfs.length > 0)
            {
              af.setShowSeqFeatures(true);
              break;
            }
          }

        }
        Desktop.addInternalFrame(af, title, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);

        af.statusBar.setText("Successfully pasted alignment file");

        try
        {
          af.setMaximum(jalview.bin.Cache.getDefault("SHOW_FULLSCREEN",
                  false));
        } catch (Exception ex)
        {
        }
      }
      else
      {
        for (int i = 0; i < al.getHeight(); i++)
        {
          alignFrame.viewport.alignment.addSequence(al.getSequenceAt(i)); // this
          // also
          // creates
          // dataset
          // sequence
          // entries
        }
        alignFrame.viewport.setEndSeq(alignFrame.viewport.alignment
                .getHeight());
        alignFrame.viewport.alignment.getWidth();
        alignFrame.viewport.firePropertyChange("alignment", null,
                alignFrame.viewport.getAlignment().getSequences());
      }
    }
    return al;
  }

  void showErrorMessage(final String error)
  {
    resetDialog();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        JOptionPane.showInternalMessageDialog(Desktop.desktop, error,
                "Error Retrieving Data", JOptionPane.WARNING_MESSAGE);
      }
    });
  }
}
