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

import jalview.datamodel.SearchResults;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GFinder;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Performs the menu option for searching the alignment, for the next or all
 * matches. If matches are found, they are highlighted, and the user has the
 * option to create a new feature on the alignment for the matched positions.
 * 
 * Searches can be for a simple base sequence, or may use a regular expression.
 * Any gaps are ignored.
 * 
 * @author $author$
 * @version $Revision$
 */
public class Finder extends GFinder
{
  private static final int HEIGHT = 110;

  private static final int WIDTH = 340;

  AlignmentViewport av;

  AlignmentPanel ap;

  JInternalFrame frame;

  int seqIndex = 0;

  int resIndex = -1;

  SearchResults searchResults;

  /**
   * Creates a new Finder object with no associated viewport or panel.
   */
  public Finder()
  {
    this(null, null);
    focusfixed = false;
  }

  /**
   * Constructor given an associated viewport and alignment panel. Constructs
   * and displays an internal frame where the user can enter a search string.
   * 
   * @param viewport
   * @param alignPanel
   */
  public Finder(AlignmentViewport viewport, AlignmentPanel alignPanel)
  {
    av = viewport;
    ap = alignPanel;
    focusfixed = true;
    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    addEscapeHandler();
    Desktop.addInternalFrame(frame, MessageManager.getString("label.find"),
            WIDTH, HEIGHT);

    textfield.requestFocus();
  }

  /**
   * Add a handler for the Escape key when the window has focus
   */
  private void addEscapeHandler()
  {
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
    getRootPane().getActionMap().put("Cancel", new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
        escapeActionPerformed();
      }
    });
  }

  /**
   * Close the panel on Escape key press
   */
  protected void escapeActionPerformed()
  {
    setVisible(false);
    frame.dispose();
  }

  /**
   * Performs the 'Find Next' action.
   * 
   * @param e
   */
  public void findNext_actionPerformed(ActionEvent e)
  {
    if (getFocusedViewport())
    {
      doSearch(false);
    }
  }

  /**
   * Performs the 'Find All' action.
   * 
   * @param e
   */
  public void findAll_actionPerformed(ActionEvent e)
  {
    if (getFocusedViewport())
    {
      resIndex = -1;
      seqIndex = 0;
      doSearch(true);
    }
  }

  /**
   * do we only search a given alignment view ?
   */
  private boolean focusfixed;

  /**
   * if !focusfixed and not in a desktop environment, checks that av and ap are
   * valid. Otherwise, gets the topmost alignment window and sets av and ap
   * accordingly
   * 
   * @return false if no alignment window was found
   */
  boolean getFocusedViewport()
  {
    if (focusfixed || Desktop.desktop == null)
    {
      if (ap != null && av != null)
      {
        return true;
      }
      // we aren't in a desktop environment, so give up now.
      return false;
    }
    // now checks further down the window stack to fix bug
    // https://mantis.lifesci.dundee.ac.uk/view.php?id=36008
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();
    for (int f = 0; f < frames.length; f++)
    {
      JInternalFrame frame = frames[f];
      if (frame != null && frame instanceof AlignFrame)
      {
        av = ((AlignFrame) frame).viewport;
        ap = ((AlignFrame) frame).alignPanel;
        return true;
      }
    }
    return false;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void createNewGroup_actionPerformed(ActionEvent e)
  {
    SequenceI[] seqs = new SequenceI[searchResults.getSize()];
    SequenceFeature[] features = new SequenceFeature[searchResults
            .getSize()];

    for (int i = 0; i < searchResults.getSize(); i++)
    {
      seqs[i] = searchResults.getResultSequence(i).getDatasetSequence();

      features[i] = new SequenceFeature(textfield.getText().trim(),
              "Search Results", null, searchResults.getResultStart(i),
              searchResults.getResultEnd(i), "Search Results");
    }

    if (ap.getSeqPanel().seqCanvas.getFeatureRenderer().amendFeatures(seqs,
            features, true, ap))
    {
      ap.alignFrame.showSeqFeatures.setSelected(true);
      av.setShowSequenceFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  /**
   * Search the alignment for the next or all matches. If 'all matches', a
   * dialog is shown with the number of sequence ids and subsequences matched.
   * 
   * @param findAll
   */
  void doSearch(boolean findAll)
  {
    createNewGroup.setEnabled(false);

    String searchString = textfield.getText().trim();

    if (isInvalidSearchString(searchString))
    {
      return;
    }
    // TODO: extend finder to match descriptions, features and annotation, and
    // other stuff
    // TODO: add switches to control what is searched - sequences, IDS,
    // descriptions, features
    jalview.analysis.Finder finder = new jalview.analysis.Finder(
            av.getAlignment(), av.getSelectionGroup(), seqIndex, resIndex);
    finder.setCaseSensitive(caseSensitive.isSelected());
    finder.setIncludeDescription(searchDescription.isSelected());

    finder.setFindAll(findAll);

    finder.find(searchString); // returns true if anything was actually found

    seqIndex = finder.getSeqIndex();
    resIndex = finder.getResIndex();

    searchResults = finder.getSearchResults(); // find(regex,
    // caseSensitive.isSelected(), )
    Vector idMatch = finder.getIdMatch();
    boolean haveResults = false;
    // set or reset the GUI
    if ((idMatch.size() > 0))
    {
      haveResults = true;
      ap.getIdPanel().highlightSearchResults(idMatch);
    }
    else
    {
      ap.getIdPanel().highlightSearchResults(null);
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
      JOptionPane.showInternalMessageDialog(this,
              MessageManager.getString("label.finished_searching"), null,
              JOptionPane.INFORMATION_MESSAGE);
      resIndex = -1;
      seqIndex = 0;
    }
    else
    {
      if (findAll)
      {
        // then we report the matches that were found
        String message = (idMatch.size() > 0) ? "" + idMatch.size()
                + " IDs" : "";
        if (searchResults != null)
        {
          if (idMatch.size() > 0 && searchResults.getSize() > 0)
          {
            message += " and ";
          }
          message += searchResults.getSize()
                  + " subsequence matches found.";
        }
        JOptionPane.showInternalMessageDialog(this, message, null,
                JOptionPane.INFORMATION_MESSAGE);
        resIndex = -1;
        seqIndex = 0;
      }
    }

  }

  /**
   * Displays an error dialog, and answers false, if the search string is
   * invalid, else answers true.
   * 
   * @param searchString
   * @return
   */
  protected boolean isInvalidSearchString(String searchString)
  {
    String error = getSearchValidationError(searchString);
    if (error == null)
    {
      return false;
    }
    JOptionPane.showInternalMessageDialog(this, error,
            MessageManager.getString("label.invalid_search"), // $NON-NLS-1$
            JOptionPane.ERROR_MESSAGE);
    return true;
  }

  /**
   * Returns an error message string if the search string is invalid, else
   * returns null.
   * 
   * Currently validation is limited to checking the string is not empty, and is
   * a valid regular expression (simple searches for base sub-sequences will
   * pass this test). Additional validations may be added in future if the
   * search syntax is expanded.
   * 
   * @param searchString
   * @return
   */
  protected String getSearchValidationError(String searchString)
  {
    String error = null;
    if (searchString == null || searchString.length() == 0)
    {
      error = MessageManager.getString("label.invalid_search");
    }
    try
    {
      Pattern.compile(searchString);
    } catch (PatternSyntaxException e)
    {
      error = MessageManager.getString("error.invalid_regex") + ": "
              + e.getDescription();
    }
    return error;
  }
}
