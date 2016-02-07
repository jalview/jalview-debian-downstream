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

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;
import jalview.util.DBRefUtils;
import jalview.util.MessageManager;
import jalview.ws.dbsources.das.api.DasSourceRegistryI;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import com.stevesoft.pat.Regex;

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

  private static long lastDasSourceRegistry = -3;

  private static DasSourceRegistryI dasRegistry = null;

  private static boolean _initingFetcher = false;

  private static Thread initingThread = null;

  int debounceTrap = 0;

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
        guiWindow
                .setProgressBar(
                        MessageManager
                                .getString("status.waiting_sequence_database_fetchers_init"),
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
        guiWindow
                .setProgressBar(
                        MessageManager
                                .getString("status.waiting_sequence_database_fetchers_init"),
                        Thread.currentThread().hashCode());
      }
    }
    if (sfetch == null
            || dasRegistry != jalview.bin.Cache.getDasSourceRegistry()
            || lastDasSourceRegistry != (jalview.bin.Cache
                    .getDasSourceRegistry().getDasRegistryURL() + jalview.bin.Cache
                    .getDasSourceRegistry().getLocalSourceString())
                    .hashCode())
    {
      _initingFetcher = true;
      initingThread = Thread.currentThread();
      /**
       * give a visual indication that sequence fetcher construction is occuring
       */
      if (guiWindow != null)
      {
        guiWindow.setProgressBar(MessageManager
                .getString("status.init_sequence_database_fetchers"),
                Thread.currentThread().hashCode());
      }
      dasRegistry = jalview.bin.Cache.getDasSourceRegistry();
      dasRegistry.refreshSources();

      jalview.ws.SequenceFetcher sf = new jalview.ws.SequenceFetcher();
      if (guiWindow != null)
      {
        guiWindow.setProgressBar(null, Thread.currentThread().hashCode());
      }
      lastDasSourceRegistry = (dasRegistry.getDasRegistryURL() + dasRegistry
              .getLocalSourceString()).hashCode();
      sfetch = sf;
      _initingFetcher = false;
      initingThread = null;
    }
    return sfetch;
  }

  private IProgressIndicator progressIndicator;

  public SequenceFetcher(IProgressIndicator guiIndic)
  {
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
          us.initGui(progressIndicator);
        }
        else
        {
          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              JOptionPane
                      .showInternalMessageDialog(
                              Desktop.desktop,
                              MessageManager
                                      .getString("warn.couldnt_create_sequence_fetcher_client"),
                              MessageManager
                                      .getString("label.couldnt_create_sequence_fetcher"),
                              JOptionPane.ERROR_MESSAGE);
            }
          });

          // raise warning dialog
        }
      }
    });
    sf.start();
  }

  private class DatabaseAuthority extends DefaultMutableTreeNode
  {

  };

  private class DatabaseSource extends DefaultMutableTreeNode
  {

  };

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
    database = new JDatabaseTree(sfetch);
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
    return ((alignFrame == null) ? MessageManager
            .getString("label.new_sequence_fetcher") : MessageManager
            .getString("label.additional_sequence_fetcher"));
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
    replacePunctuation.setText(MessageManager
            .getString("label.replace_commas_semicolons"));
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
    close.setText(MessageManager.getString("action.close"));
    close.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
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
    jPanel1.add(ok);
    jPanel1.add(example);
    jPanel1.add(clear);
    jPanel1.add(close);
    jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);
    jPanel2.setLayout(borderLayout3);
    databaseButt = database.getDatabaseSelectorButton();
    databaseButt.setFont(JvSwingUtils.getLabelFont());
    database.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        debounceTrap++;
        String currentSelection = database.getSelectedItem();
        if (!currentSelection.equalsIgnoreCase("pdb"))
        {
          otherSourceAction();
        }
        if (currentSelection.equalsIgnoreCase("pdb")
                && (database.action == KeyEvent.VK_ENTER || ((debounceTrap % 2) == 0)))
        {
          pdbSourceAction();
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
    new PDBSearchPanel(this);
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
      dbeg.setText(MessageManager.formatMessage(
              "label.example_query_param", new String[] { eq }));
      boolean enablePunct = !(eq != null && eq.indexOf(",") > -1);
      for (DbSourceProxy dbs : database.getSelectedSources())
      {
        if (dbs instanceof jalview.ws.dbsources.das.datamodel.DasSequenceSource)
        {
          enablePunct = false;
          break;
        }
      }
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
    // indicate if successive sources should be merged into one alignment.
    boolean addToLast = false;
    ArrayList<String> aresultq = new ArrayList<String>(), presultTitle = new ArrayList<String>();
    ArrayList<AlignmentI> presult = new ArrayList<AlignmentI>(), aresult = new ArrayList<AlignmentI>();
    Iterator<DbSourceProxy> proxies = database.getSelectedSources()
            .iterator();
    String[] qries;
    List<String> nextfetch = Arrays.asList(qries = textArea.getText()
            .split(";"));
    Iterator<String> en = Arrays.asList(new String[0]).iterator();
    int nqueries = qries.length;
    while (proxies.hasNext() && (en.hasNext() || nextfetch.size() > 0))
    {
      if (!en.hasNext() && nextfetch.size() > 0)
      {
        en = nextfetch.iterator();
        nqueries = nextfetch.size();
        // save the remaining queries in the original array
        qries = nextfetch.toArray(new String[nqueries]);
        nextfetch = new ArrayList<String>();
      }

      DbSourceProxy proxy = proxies.next();
      boolean isAliSource = false;
      try
      {
        // update status
        guiWindow
                .setProgressBar(MessageManager.formatMessage(
                        "status.fetching_sequence_queries_from",
                        new String[] {
                            Integer.valueOf(nqueries).toString(),
                            proxy.getDbName() }), Thread.currentThread()
                        .hashCode());
        isAliSource = proxy.isA(DBRefSource.ALIGNMENTDB);
        if (proxy.getAccessionSeparator() == null)
        {
          while (en.hasNext())
          {
            String item = en.next();
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
                        + proxy.getDbName(), oome, this);
              }
              if (indres != null)
              {
                aresultq.add(item);
                aresult.add(indres);
              }
              else
              {
                nextfetch.add(item);
              }
            } catch (Exception e)
            {
              jalview.bin.Cache.log.info("Error retrieving " + item
                      + " from " + proxy.getDbName(), e);
              nextfetch.add(item);
            }
          }
        }
        else
        {
          StringBuffer multiacc = new StringBuffer();
          ArrayList<String> tosend = new ArrayList<String>();
          while (en.hasNext())
          {
            String nel = en.next();
            tosend.add(nel);
            multiacc.append(nel);
            if (en.hasNext())
            {
              multiacc.append(proxy.getAccessionSeparator());
            }
          }
          try
          {
            AlignmentI rslt;
            SequenceI[] rs;
            List<String> nores = new ArrayList<String>();
            rslt = proxy.getSequenceRecords(multiacc.toString());
            if (rslt == null || rslt.getHeight() == 0)
            {
              // no results - pass on all queries to next source
              nextfetch.addAll(tosend);
            }
            else
            {
              aresultq.add(multiacc.toString());
              aresult.add(rslt);

              rs = rslt.getSequencesArray();
              // search for each query in the dbrefs associated with each
              // sequence
              // returned.
              // ones we do not find will be used to query next source (if any)
              for (String q : tosend)
              {
                DBRefEntry dbr = new DBRefEntry(), found[] = null;
                dbr.setSource(proxy.getDbSource());
                dbr.setVersion(null);
                if (proxy.getAccessionValidator() != null)
                {
                  Regex vgr = proxy.getAccessionValidator();
                  vgr.search(q);
                  if (vgr.numSubs() > 0)
                  {
                    dbr.setAccessionId(vgr.stringMatched(1));
                  }
                  else
                  {
                    dbr.setAccessionId(vgr.stringMatched());
                  }
                }
                else
                {
                  dbr.setAccessionId(q);
                }
                boolean rfound = false;
                for (int r = 0; r < rs.length; r++)
                {
                  if (rs[r] != null
                          && (found = DBRefUtils.searchRefs(
                                  rs[r].getDBRef(), dbr)) != null
                          && found.length > 0)
                  {
                    rfound = true;
                    rs[r] = null;
                    continue;
                  }
                }
                if (!rfound)
                {
                  nextfetch.add(q);
                }
              }
            }
          } catch (OutOfMemoryError oome)
          {
            new OOMWarning("fetching " + multiacc + " from "
                    + database.getSelectedItem(), oome, this);
          }
        }

      } catch (Exception e)
      {
        showErrorMessage("Error retrieving " + textArea.getText()
                + " from " + database.getSelectedItem());
        // error
        // +="Couldn't retrieve sequences from "+database.getSelectedItem();
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
      // Stack results ready for opening in alignment windows
      if (aresult != null && aresult.size() > 0)
      {
        AlignmentI ar = null;
        if (isAliSource)
        {
          addToLast = false;
          // new window for each result
          while (aresult.size() > 0)
          {
            presult.add(aresult.remove(0));
            presultTitle.add(aresultq.remove(0) + " "
                    + getDefaultRetrievalTitle());
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
            ;
          }
          addToLast = true;
          presult.add(ar);
          presultTitle.add(titl);
        }
      }
      guiWindow.setProgressBar(MessageManager
              .getString("status.finshed_querying"), Thread.currentThread()
              .hashCode());
    }
    guiWindow.setProgressBar(
            (presult.size() > 0) ? MessageManager
                    .getString("status.parsing_results") : MessageManager
                    .getString("status.processing"), Thread.currentThread()
                    .hashCode());
    // process results
    while (presult.size() > 0)
    {
      parseResult(presult.remove(0), presultTitle.remove(0), null);
    }
    // only remove visual delay after we finished parsing.
    guiWindow.setProgressBar(null, Thread.currentThread().hashCode());
    if (nextfetch.size() > 0)
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Didn't retrieve the following "
              + (nextfetch.size() == 1 ? "query" : nextfetch.size()
                      + " queries") + ": \n");
      int l = sb.length(), lr = 0;
      for (String s : nextfetch)
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

  AlignmentI parseResult(String result, String title)
  {
    String format = new IdentifyFile().Identify(result, "Paste");
    AlignmentI sequences = null;
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
   * @return a standard title for any results retrieved using the currently
   *         selected source and settings
   */
  public String getDefaultRetrievalTitle()
  {
    return "Retrieved from " + database.getSelectedItem();
  }

  AlignmentI parseResult(AlignmentI al, String title,
          String currentFileFormat)
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
            if ((sfs = sq.getSequenceFeatures()) != null)
            {
              if (sfs.length > 0)
              {
                af.setShowSeqFeatures(true);
                break;
              }
            }

          }
        }
        Desktop.addInternalFrame(af, title, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);

        af.statusBar.setText(MessageManager
                .getString("label.successfully_pasted_alignment_file"));

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
        JOptionPane.showInternalMessageDialog(Desktop.desktop, error,
                MessageManager.getString("label.error_retrieving_data"),
                JOptionPane.WARNING_MESSAGE);
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
}
