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
package jalview.appletgui;

import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

public class Finder extends Panel implements ActionListener
{
  AlignmentViewport av;

  AlignmentPanel ap;

  Frame frame;

  SearchResultsI searchResults;

  int seqIndex = 0;

  int resIndex = -1;

  public Finder(final AlignmentPanel ap)
  {
    try
    {
      jbInit();

    } catch (Exception e)
    {
      e.printStackTrace();
    }

    this.av = ap.av;
    this.ap = ap;
    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("action.find"), 340, 120);
    frame.repaint();
    frame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        ap.highlightSearchResults(null);
      }
    });
    textfield.requestFocus();
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == textfield)
    {
      doSearch(false);
    }

    else if (evt.getSource() == findNext)
    {
      doSearch(false);
    }

    else if (evt.getSource() == findAll)
    {
      resIndex = -1;
      seqIndex = 0;
      doSearch(true);
    }
    else if (evt.getSource() == createNewGroup)
    {
      createNewGroup_actionPerformed();
    }
  }

  public void createNewGroup_actionPerformed()
  {
    SequenceI[] seqs = new SequenceI[searchResults.getSize()];
    SequenceFeature[] features = new SequenceFeature[searchResults
            .getSize()];

    int i = 0;
    for (SearchResultMatchI match : searchResults.getResults())
    {
      seqs[i] = match.getSequence().getDatasetSequence();

      features[i] = new SequenceFeature(textfield.getText().trim(),
              "Search Results", null, match.getStart(), match.getEnd(),
              "Search Results");
      i++;
    }

    if (ap.seqPanel.seqCanvas.getFeatureRenderer().amendFeatures(seqs,
            features, true, ap))
    {
      ap.alignFrame.sequenceFeatures.setState(true);
      av.setShowSequenceFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  void doSearch(boolean findAll)
  {
    if (ap.av.applet.currentAlignFrame != null)
    {
      ap = ap.av.applet.currentAlignFrame.alignPanel;
      av = ap.av;
    }
    createNewGroup.setEnabled(false);
    jalview.analysis.Finder finder = new jalview.analysis.Finder(
            av.getAlignment(), av.getSelectionGroup(), seqIndex, resIndex);
    finder.setCaseSensitive(caseSensitive.getState());
    finder.setIncludeDescription(searchDescription.getState());
    finder.setFindAll(findAll);

    String searchString = textfield.getText();

    finder.find(searchString);
    seqIndex = finder.getSeqIndex();
    resIndex = finder.getResIndex();
    searchResults = finder.getSearchResults();
    Vector<SequenceI> idMatch = finder.getIdMatch();
    boolean haveResults = false;
    // set or reset the GUI
    if ((idMatch.size() > 0))
    {
      haveResults = true;
      ap.idPanel.highlightSearchResults(idMatch);
    }
    else
    {
      ap.idPanel.highlightSearchResults(null);
    }

    if (searchResults.getSize() > 0)
    {
      haveResults = true;
      createNewGroup.setEnabled(true);

    }
    else
    {
      searchResults = null;
    }

    // if allResults is null, this effectively switches displaySearch flag in
    // seqCanvas
    ap.highlightSearchResults(searchResults);
    // TODO: add enablers for 'SelectSequences' or 'SelectColumns' or
    // 'SelectRegion' selection
    if (!haveResults)
    {
      ap.alignFrame.statusBar.setText(MessageManager
              .getString("label.finished_searching"));
      resIndex = -1;
      seqIndex = 0;
    }
    else
    {
      if (findAll)
      {
        String message = (idMatch.size() > 0) ? "" + idMatch.size()
                + " IDs" : "";
        if (idMatch.size() > 0 && searchResults != null
                && searchResults.getSize() > 0)
        {
          message += " and ";
        }
        if (searchResults != null)
        {
          message += searchResults.getSize() + " subsequence matches.";
        }
        ap.alignFrame.statusBar.setText(MessageManager.formatMessage(
                "label.search_results", new String[] { searchString,
                    message }));

      }
      else
      {
        // TODO: indicate sequence and matching position in status bar
        ap.alignFrame.statusBar.setText(MessageManager.formatMessage(
                "label.found_match_for", new String[] { searchString }));
      }
    }
  }

  Label jLabel1 = new Label();

  protected TextField textfield = new TextField();

  protected Button findAll = new Button();

  protected Button findNext = new Button();

  Panel actionsPanel = new Panel();

  GridLayout gridLayout1 = new GridLayout();

  protected Button createNewGroup = new Button();

  Checkbox caseSensitive = new Checkbox();

  Checkbox searchDescription = new Checkbox();

  private void jbInit() throws Exception
  {
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel1.setText(MessageManager.getString("action.find"));
    jLabel1.setBounds(new Rectangle(3, 30, 34, 15));
    this.setLayout(null);
    textfield.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    textfield.setText("");
    textfield.setBounds(new Rectangle(40, 17, 133, 21));
    textfield.addKeyListener(new java.awt.event.KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent e)
      {
        textfield_keyTyped(e);
      }
    });
    textfield.addActionListener(this);
    findAll.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    findAll.setLabel(MessageManager.getString("action.find_all"));
    findAll.addActionListener(this);
    findNext.setEnabled(false);
    findNext.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    findNext.setLabel(MessageManager.getString("action.find_next"));
    findNext.addActionListener(this);
    actionsPanel.setBounds(new Rectangle(195, 5, 141, 64));
    actionsPanel.setLayout(gridLayout1);
    gridLayout1.setHgap(0);
    gridLayout1.setRows(3);
    gridLayout1.setVgap(2);
    createNewGroup.setEnabled(false);
    createNewGroup.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    createNewGroup.setLabel(MessageManager.getString("label.new_feature"));
    createNewGroup.addActionListener(this);
    caseSensitive.setLabel(MessageManager.getString("label.match_case"));
    caseSensitive.setBounds(new Rectangle(30, 39, 126, 23));

    searchDescription.setLabel(MessageManager
            .getString("label.include_description"));
    searchDescription.setBounds(new Rectangle(30, 59, 170, 23));
    actionsPanel.add(findNext, null);
    actionsPanel.add(findAll, null);
    actionsPanel.add(createNewGroup, null);
    this.add(caseSensitive);
    this.add(textfield, null);
    this.add(jLabel1, null);
    this.add(actionsPanel, null);
    this.add(searchDescription);
  }

  void textfield_keyTyped(KeyEvent e)
  {
    findNext.setEnabled(true);
  }

}
