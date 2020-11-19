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

import jalview.api.FeatureSettingsModelI;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.fts.core.GFTSPanel;
import jalview.fts.service.pdb.PDBFTSPanel;
import jalview.fts.service.uniprot.UniprotFTSPanel;
import jalview.io.FileFormatI;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.DBRefUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

public class SequenceFetcher extends JPanel implements Runnable
{
  JLabel dbeg = new JLabel();

  JDatabaseTree database;

  JButton databaseButt;

  JLabel jLabel1 = new JLabel();

  JCheckBox replacePunctuation = new JCheckBox();

  JButton ok = new JButton();

  JButton clear = new JButton();

  JButton example = new JButton();

  JButton close = new JButton();

  JButton back = new JButton();

  JPanel jPanel1 = new JPanel();

  JTextArea textArea = new JTextArea();

  JScrollPane jScrollPane1 = new JScrollPane();

  JPanel jPanel2 = new JPanel();

  JPanel jPanel3 = new JPanel();

  JPanel jPanel4 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  BorderLayout borderLayout3 = new BorderLayout();

  JInternalFrame frame;

  IProgressIndicator guiWindow;

  AlignFrame alignFrame;

  StringBuffer result;

  final String noDbSelected = "-- Select Database --";

  private static jalview.ws.SequenceFetcher sfetch = null;

  private static boolean _initingFetcher = false;

  private static Thread initingThread = null;

  public JTextArea getTextArea()
  {
    return textArea;
  }

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
                MessageManager.getString(
                        "status.waiting_sequence_database_fetchers_init"),
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
                MessageManager.getString(
                        "status.waiting_sequence_database_fetchers_init"),
                Thread.currentThread().hashCode());
      }
    }
    if (sfetch == null)
    {
      _initingFetcher = true;
      initingThread = Thread.currentThread();
      /**
       * give a visual indication that sequence fetcher construction is occuring
       */
      if (guiWindow != null)
      {
        guiWindow.setProgressBar(
                MessageManager.getString(
                        "status.init_sequence_database_fetchers"),
                Thread.currentThread().hashCode());
      }

      jalview.ws.SequenceFetcher sf = new jalview.ws.SequenceFetcher();
      if (guiWindow != null)
      {
        guiWindow.setProgressBar(null, Thread.currentThread().hashCode());
      }
      sfetch = sf;
      _initingFetcher = false;
      initingThread = null;
    }
    return sfetch;
  }

  private IProgressIndicator progressIndicator;

  private volatile boolean _isConstructing = false;

  private List<AlignFrame> newAlframes = null;

  public SequenceFetcher(IProgressIndicator guiIndic)
  {
    this(guiIndic, null, null);
  }

  public SequenceFetcher(IProgressIndicator guiIndic,
          final String selectedDb, final String queryString)
  {
    this._isConstructing = true;
    this.progressIndicator = guiIndic;
    final SequenceFetcher us = this;
    // launch initialiser thread
    Thread sf = new Thread(new Runnable()
    {

      @Override
      public void run()
      {
        if (getSequenceFetcherSingleton(progressIndicator) != null)
        {
          us.initGui(progressIndicator, selectedDb, queryString);
          us._isConstructing = false;
        }
        else
        {
          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                      MessageManager.getString(
                              "warn.couldnt_create_sequence_fetcher_client"),
                      MessageManager.getString(
                              "label.couldnt_create_sequence_fetcher"),
                      JvOptionPane.ERROR_MESSAGE);
            }
          });

          // raise warning dialog
        }
      }
    });
    sf.start();
  }

  /**
   * blocking call which creates a new sequence fetcher panel, configures it and
   * presses the OK button with the given database and query.
   * 
   * @param database
   * @param query
   */
  public static List<AlignFrame> fetchAndShow(String database, String query)
  {
    final SequenceFetcher sf = new SequenceFetcher(Desktop.instance,
            database, query);
    while (sf._isConstructing)
    {
      try
      {
        Thread.sleep(50);
      } catch (Exception q)
      {
        return Collections.emptyList();
      }
    }
    sf.newAlframes = new ArrayList<>();
    sf.run();
    return sf.newAlframes;
  }

  private class DatabaseAuthority extends DefaultMutableTreeNode
  {

  };

  private class DatabaseSource extends DefaultMutableTreeNode
  {

  };

  /**
   * initialise the database and query for this fetcher panel
   * 
   * @param selectedDb
   *          - string that should correspond to a sequence fetcher
   * @param queryString
   *          - string that will be entered in the query dialog
   * @return true if UI was configured with valid database and query string
   */
  protected boolean setInitialQuery(String selectedDb, String queryString)
  {
    if (selectedDb == null || selectedDb.trim().length() == 0)
    {
      return false;
    }
    try
    {
      List<DbSourceProxy> sp = sfetch.getSourceProxy(selectedDb);
      for (DbSourceProxy sourcep : sp)
      {
        if (sourcep.getTier() == 0)
        {
          database.selection = Arrays
                  .asList(new DbSourceProxy[]
                  { sourcep });
          break;
        }
      }
      if (database.selection == null || database.selection.size() == 0)
      {
        System.err.println(
                "Ignoring fetch parameter db='" + selectedDb + "'");
        return false;
      }
      textArea.setText(queryString);
    } catch (Exception q)
    {
      System.err.println("Ignoring fetch parameter db='" + selectedDb
              + "' and query='" + queryString + "'");
      return false;
    }
    return true;
  }

  /**
   * called by thread spawned by constructor
   * 
   * @param guiWindow
   * @param queryString
   * @param selectedDb
   */
  private void initGui(IProgressIndicator guiWindow, String selectedDb,
          String queryString)
  {
    this.guiWindow = guiWindow;
    if (guiWindow instanceof AlignFrame)
    {
      alignFrame = (AlignFrame) guiWindow;
    }
    database = new JDatabaseTree(sfetch);
    try
    {
      jbInit();
      /*
       * configure the UI with any query parameters we were called with
       */
      if (!setInitialQuery(selectedDb, queryString))
      {
        /*
         * none provided, so show the database chooser
         */
        database.waitForInput();
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    frame = new JInternalFrame();
    frame.setContentPane(this);
    if (Platform.isAMac())
    {
      Desktop.addInternalFrame(frame, getFrameTitle(), false, 400, 240);
    }
    else
    {
      Desktop.addInternalFrame(frame, getFrameTitle(), false, 400, 180);
    }
  }

  private String getFrameTitle()
  {
    return ((alignFrame == null)
            ? MessageManager.getString("label.new_sequence_fetcher")
            : MessageManager
                    .getString("label.additional_sequence_fetcher"));
  }

  GFTSPanel parentFTSframe = null;
  /**
   * change the buttons so they fit with the FTS panel.
   */
  public void embedWithFTSPanel(GFTSPanel toClose)
  {
    back.setVisible(true);
    parentFTSframe = toClose;
  }
  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout2);

    database.setFont(JvSwingUtils.getLabelFont());
    dbeg.setFont(new java.awt.Font("Verdana", Font.BOLD, 11));
    jLabel1.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText(MessageManager
            .getString("label.separate_multiple_accession_ids"));

    replacePunctuation.setHorizontalAlignment(SwingConstants.CENTER);
    replacePunctuation
            .setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    replacePunctuation.setText(
            MessageManager.getString("label.replace_commas_semicolons"));
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });
    clear.setText(MessageManager.getString("action.clear"));
    clear.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        clear_actionPerformed();
      }
    });

    example.setText(MessageManager.getString("label.example"));
    example.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        example_actionPerformed();
      }
    });
    close.setText(MessageManager.getString("action.cancel"));
    close.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
    back.setText(MessageManager.getString("action.back"));
    back.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        parentFTSframe.btn_back_ActionPerformed();
      }
    });
    // back not visible unless embedded
    back.setVisible(false);
    textArea.setFont(JvSwingUtils.getLabelFont());
    textArea.setLineWrap(true);
    textArea.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          ok_actionPerformed();
        }
      }
    });
    jPanel3.setLayout(borderLayout1);
    borderLayout1.setVgap(5);
    jPanel1.add(back);
    jPanel1.add(example);
    jPanel1.add(clear);
    jPanel1.add(ok);
    jPanel1.add(close);
    jPanel2.setLayout(borderLayout3);
    databaseButt = /*database.getDatabaseSelectorButton();
                   final JButton viewdbs =*/new JButton(
            MessageManager.getString("action.select_ddbb"));
    databaseButt.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        hidePanel();
        database.showDialog();
      }
    });
    databaseButt.setFont(JvSwingUtils.getLabelFont());
    database.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String currentSelection = database.getSelectedItem();
        if (currentSelection == null)
        {
          close_actionPerformed(null);
        }

        showPanel();

        if ("pdb".equalsIgnoreCase(currentSelection))
        {
          pdbSourceAction();
        }
        else if ("uniprot".equalsIgnoreCase(currentSelection))
        {
          uniprotSourceAction();
        }
        else
        {
          otherSourceAction();
        }
        database.action = -1;
      }
    });

    dbeg.setText("");
    jPanel2.add(databaseButt, java.awt.BorderLayout.NORTH);
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

  private void pdbSourceAction()
  {
    databaseButt.setText(database.getSelectedItem());
    new PDBFTSPanel(this);
    frame.dispose();
  }

  private void uniprotSourceAction()
  {
    databaseButt.setText(database.getSelectedItem());
    new UniprotFTSPanel(this);
    frame.dispose();
  }

  private void otherSourceAction()
  {
    try
    {
      databaseButt.setText(database.getSelectedItem()
              + (database.getSelectedSources().size() > 1 ? " (and "
                      + database.getSelectedSources().size() + " others)"
                      : ""));
      String eq = database.getExampleQueries();
      dbeg.setText(MessageManager.formatMessage("label.example_query_param",
              new String[]
              { eq }));
      // TODO this should be a property of the SequenceFetcher whether commas are and
      // colons are allowed in the IDs...

      boolean enablePunct = !(eq != null && eq.indexOf(",") > -1);
      replacePunctuation.setEnabled(enablePunct);

    } catch (Exception ex)
    {
      dbeg.setText("");
      replacePunctuation.setEnabled(true);
    }
    jPanel2.repaint();
  }

  protected void example_actionPerformed()
  {
    DbSourceProxy db = null;
    try
    {
      textArea.setText(database.getExampleQueries());
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

  public void close_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
      if (parentFTSframe!=null)
      {
        parentFTSframe.btn_cancel_ActionPerformed();
      }
    } catch (Exception ex)
    {
    }
  }

  public void ok_actionPerformed()
  {
    databaseButt.setEnabled(false);
    example.setEnabled(false);
    textArea.setEnabled(false);
    ok.setEnabled(false);
    close.setEnabled(false);
    back.setEnabled(false);
    Thread worker = new Thread(this);
    worker.start();
  }

  private void resetDialog()
  {
    databaseButt.setEnabled(true);
    example.setEnabled(true);
    textArea.setEnabled(true);
    ok.setEnabled(true);
    close.setEnabled(true);
    back.setEnabled(parentFTSframe != null);
  }

  @Override
  public void run()
  {
    String error = "";
    if (!database.hasSelection())
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
    if (!new com.stevesoft.pat.Regex("[A-Za-z0-9_.]")
            .search(textArea.getText()))
    {
      error += "Please enter a (semi-colon separated list of) database id(s)";
    }
    if (error.length() > 0)
    {
      showErrorMessage(error);
      resetDialog();
      return;
    }
    // TODO: Refactor to GUI independent code and write tests.
    // indicate if successive sources should be merged into one alignment.
    boolean addToLast = false;
    List<String> aresultq = new ArrayList<>();
    List<String> presultTitle = new ArrayList<>();
    List<AlignmentI> presult = new ArrayList<>();
    List<AlignmentI> aresult = new ArrayList<>();
    Iterator<DbSourceProxy> proxies = database.getSelectedSources()
            .iterator();
    String[] qries;
    List<String> nextFetch = Arrays
            .asList(qries = textArea.getText().split(";"));
    Iterator<String> en = Arrays.asList(new String[0]).iterator();
    int nqueries = qries.length;

    FeatureSettingsModelI preferredFeatureColours = null;
    while (proxies.hasNext() && (en.hasNext() || nextFetch.size() > 0))
    {
      if (!en.hasNext() && nextFetch.size() > 0)
      {
        en = nextFetch.iterator();
        nqueries = nextFetch.size();
        // save the remaining queries in the original array
        qries = nextFetch.toArray(new String[nqueries]);
        nextFetch = new ArrayList<>();
      }

      DbSourceProxy proxy = proxies.next();
      try
      {
        // update status
        guiWindow.setProgressBar(MessageManager.formatMessage(
                "status.fetching_sequence_queries_from", new String[]
                { Integer.valueOf(nqueries).toString(),
                    proxy.getDbName() }),
                Thread.currentThread().hashCode());
        if (proxy.getMaximumQueryCount() == 1)
        {
          /*
           * proxy only handles one accession id at a time
           */
          while (en.hasNext())
          {
            String acc = en.next();
            if (!fetchSingleAccession(proxy, acc, aresultq, aresult))
            {
              nextFetch.add(acc);
            }
          }
        }
        else
        {
          /*
           * proxy can fetch multiple accessions at one time
           */
          fetchMultipleAccessions(proxy, en, aresultq, aresult, nextFetch);
        }
      } catch (Exception e)
      {
        showErrorMessage("Error retrieving " + textArea.getText() + " from "
                + database.getSelectedItem());
        // error
        // +="Couldn't retrieve sequences from "+database.getSelectedItem();
        System.err.println("Retrieval failed for source ='"
                + database.getSelectedItem() + "' and query\n'"
                + textArea.getText() + "'\n");
        e.printStackTrace();
      } catch (OutOfMemoryError e)
      {
        showErrorMessage("Out of Memory when retrieving "
                + textArea.getText() + " from " + database.getSelectedItem()
                + "\nPlease see the Jalview FAQ for instructions for increasing the memory available to Jalview.\n");
        e.printStackTrace();
      } catch (Error e)
      {
        showErrorMessage("Serious Error retrieving " + textArea.getText()
                + " from " + database.getSelectedItem());
        e.printStackTrace();
      }

      // Stack results ready for opening in alignment windows
      if (aresult != null && aresult.size() > 0)
      {
        FeatureSettingsModelI proxyColourScheme = proxy
                .getFeatureColourScheme();
        if (proxyColourScheme != null)
        {
          preferredFeatureColours = proxyColourScheme;
        }

        AlignmentI ar = null;
        if (proxy.isAlignmentSource())
        {
          addToLast = false;
          // new window for each result
          while (aresult.size() > 0)
          {
            presult.add(aresult.remove(0));
            presultTitle.add(
                    aresultq.remove(0) + " " + getDefaultRetrievalTitle());
          }
        }
        else
        {
          String titl = null;
          if (addToLast && presult.size() > 0)
          {
            ar = presult.remove(presult.size() - 1);
            titl = presultTitle.remove(presultTitle.size() - 1);
          }
          // concatenate all results in one window
          while (aresult.size() > 0)
          {
            if (ar == null)
            {
              ar = aresult.remove(0);
            }
            else
            {
              ar.append(aresult.remove(0));
            }
          }
          addToLast = true;
          presult.add(ar);
          presultTitle.add(titl);
        }
      }
      guiWindow.setProgressBar(
              MessageManager.getString("status.finshed_querying"),
              Thread.currentThread().hashCode());
    }
    guiWindow
            .setProgressBar(
                    (presult.size() > 0)
                            ? MessageManager
                                    .getString("status.parsing_results")
                            : MessageManager.getString("status.processing"),
                    Thread.currentThread().hashCode());
    // process results
    while (presult.size() > 0)
    {
      parseResult(presult.remove(0), presultTitle.remove(0), null,
              preferredFeatureColours);
    }
    // only remove visual delay after we finished parsing.
    guiWindow.setProgressBar(null, Thread.currentThread().hashCode());
    if (nextFetch.size() > 0)
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Didn't retrieve the following "
              + (nextFetch.size() == 1 ? "query"
                      : nextFetch.size() + " queries")
              + ": \n");
      int l = sb.length(), lr = 0;
      for (String s : nextFetch)
      {
        if (l != sb.length())
        {
          sb.append("; ");
        }
        if (lr - sb.length() > 40)
        {
          sb.append("\n");
        }
        sb.append(s);
      }
      showErrorMessage(sb.toString());
    }
    resetDialog();
  }

  /**
   * Tries to fetch one or more accession ids from the database proxy
   * 
   * @param proxy
   * @param accessions
   *          the queries to fetch
   * @param aresultq
   *          a successful queries list to add to
   * @param aresult
   *          a list of retrieved alignments to add to
   * @param nextFetch
   *          failed queries are added to this list
   * @throws Exception
   */
  void fetchMultipleAccessions(DbSourceProxy proxy,
          Iterator<String> accessions, List<String> aresultq,
          List<AlignmentI> aresult, List<String> nextFetch) throws Exception
  {
    StringBuilder multiacc = new StringBuilder();
    List<String> tosend = new ArrayList<>();
    while (accessions.hasNext())
    {
      String nel = accessions.next();
      tosend.add(nel);
      multiacc.append(nel);
      if (accessions.hasNext())
      {
        multiacc.append(proxy.getAccessionSeparator());
      }
    }

    try
    {
      String query = multiacc.toString();
      AlignmentI rslt = proxy.getSequenceRecords(query);
      if (rslt == null || rslt.getHeight() == 0)
      {
        // no results - pass on all queries to next source
        nextFetch.addAll(tosend);
      }
      else
      {
        aresultq.add(query);
        aresult.add(rslt);
        if (tosend.size() > 1)
        {
          checkResultForQueries(rslt, tosend, nextFetch, proxy);
        }
      }
    } catch (OutOfMemoryError oome)
    {
      new OOMWarning("fetching " + multiacc + " from "
              + database.getSelectedItem(), oome, this);
    }
  }

  /**
   * Query for a single accession id via the database proxy
   * 
   * @param proxy
   * @param accession
   * @param aresultq
   *          a list of successful queries to add to
   * @param aresult
   *          a list of retrieved alignments to add to
   * @return true if the fetch was successful, else false
   */
  boolean fetchSingleAccession(DbSourceProxy proxy, String accession,
          List<String> aresultq, List<AlignmentI> aresult)
  {
    boolean success = false;
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
        indres = proxy.getSequenceRecords(accession);
      } catch (OutOfMemoryError oome)
      {
        new OOMWarning(
                "fetching " + accession + " from " + proxy.getDbName(),
                oome, this);
      }
      if (indres != null)
      {
        aresultq.add(accession);
        aresult.add(indres);
        success = true;
      }
    } catch (Exception e)
    {
      Cache.log.info("Error retrieving " + accession + " from "
              + proxy.getDbName(), e);
    }
    return success;
  }

  /**
   * Checks which of the queries were successfully retrieved by searching the
   * DBRefs of the retrieved sequences for a match. Any not found are added to
   * the 'nextFetch' list.
   * 
   * @param rslt
   * @param queries
   * @param nextFetch
   * @param proxy
   */
  void checkResultForQueries(AlignmentI rslt, List<String> queries,
          List<String> nextFetch, DbSourceProxy proxy)
  {
    SequenceI[] rs = rslt.getSequencesArray();

    for (String q : queries)
    {
      DBRefEntry dbr = new DBRefEntry();
      dbr.setSource(proxy.getDbSource());
      dbr.setVersion(null);
      String accId = proxy.getAccessionIdFromQuery(q);
      dbr.setAccessionId(accId);
      boolean rfound = false;
      for (int r = 0; r < rs.length; r++)
      {
        if (rs[r] != null)
        {
          List<DBRefEntry> found = DBRefUtils.searchRefs(rs[r].getDBRefs(),
                  accId);
          if (!found.isEmpty())
          {
            rfound = true;
            break;
          }
        }
      }
      if (!rfound)
      {
        nextFetch.add(q);
      }
    }
  }

  /**
   * 
   * @return a standard title for any results retrieved using the currently
   *         selected source and settings
   */
  public String getDefaultRetrievalTitle()
  {
    return "Retrieved from " + database.getSelectedItem();
  }

  AlignmentI parseResult(AlignmentI al, String title,
          FileFormatI currentFileFormat,
          FeatureSettingsModelI preferredFeatureColours)
  {

    if (al != null && al.getHeight() > 0)
    {
      if (title == null)
      {
        title = getDefaultRetrievalTitle();
      }
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

        SequenceFeature[] sfs = null;
        List<SequenceI> alsqs;
        synchronized (alsqs = al.getSequences())
        {
          for (SequenceI sq : alsqs)
          {
            if (sq.getFeatures().hasFeatures())
            {
              af.setShowSeqFeatures(true);
              break;
            }
          }
        }

        af.getViewport().applyFeaturesStyle(preferredFeatureColours);
        if (Cache.getDefault("HIDE_INTRONS", true))
        {
          af.hideFeatureColumns(SequenceOntologyI.EXON, false);
        }
        if (newAlframes != null)
        {
          newAlframes.add(af);
        }
        Desktop.addInternalFrame(af, title, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);

        af.setStatus(MessageManager
                .getString("label.successfully_pasted_alignment_file"));

        try
        {
          af.setMaximum(Cache.getDefault("SHOW_FULLSCREEN", false));
        } catch (Exception ex)
        {
        }
      }
      else
      {
        alignFrame.viewport.addAlignment(al, title);
      }
    }
    return al;
  }

  void showErrorMessage(final String error)
  {
    resetDialog();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JvOptionPane.showInternalMessageDialog(Desktop.desktop, error,
                MessageManager.getString("label.error_retrieving_data"),
                JvOptionPane.WARNING_MESSAGE);
      }
    });
  }

  public IProgressIndicator getProgressIndicator()
  {
    return progressIndicator;
  }

  public void setProgressIndicator(IProgressIndicator progressIndicator)
  {
    this.progressIndicator = progressIndicator;
  }

  /**
   * Make this panel visible (after a selection has been made in the database
   * chooser)
   */
  void showPanel()
  {
    frame.setVisible(true);
  }

  /**
   * Hide this panel (on clicking the database button to open the database
   * chooser)
   */
  void hidePanel()
  {
    frame.setVisible(false);
  }

  public void setDatabaseChooserVisible(boolean b)
  {
    databaseButt.setVisible(b);
  }
}
