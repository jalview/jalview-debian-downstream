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
package jalview.appletgui;

import jalview.analysis.AAFrequency;
import jalview.analysis.AlignmentAnnotationUtils;
import jalview.analysis.AlignmentUtils;
import jalview.analysis.Conservation;
import jalview.commands.ChangeCaseCommand;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.AppletFormatAdapter;
import jalview.io.SequenceAnnotationReport;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.ResidueProperties;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.util.MessageManager;
import jalview.util.UrlLink;

import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class APopupMenu extends java.awt.PopupMenu implements
        ActionListener, ItemListener
{
  private static final String ALL_ANNOTATIONS = "All";

  Menu groupMenu = new Menu();

  MenuItem editGroupName = new MenuItem();

  protected MenuItem clustalColour = new MenuItem();

  protected MenuItem zappoColour = new MenuItem();

  protected MenuItem taylorColour = new MenuItem();

  protected MenuItem hydrophobicityColour = new MenuItem();

  protected MenuItem helixColour = new MenuItem();

  protected MenuItem strandColour = new MenuItem();

  protected MenuItem turnColour = new MenuItem();

  protected MenuItem buriedColour = new MenuItem();

  protected CheckboxMenuItem abovePIDColour = new CheckboxMenuItem();

  protected MenuItem userDefinedColour = new MenuItem();

  protected MenuItem PIDColour = new MenuItem();

  protected MenuItem BLOSUM62Colour = new MenuItem();

  MenuItem noColourmenuItem = new MenuItem();

  protected CheckboxMenuItem conservationMenuItem = new CheckboxMenuItem();

  final AlignmentPanel ap;

  MenuItem unGroupMenuItem = new MenuItem();

  MenuItem createGroupMenuItem = new MenuItem();

  MenuItem nucleotideMenuItem = new MenuItem();

  Menu colourMenu = new Menu();

  CheckboxMenuItem showBoxes = new CheckboxMenuItem();

  CheckboxMenuItem showText = new CheckboxMenuItem();

  CheckboxMenuItem showColourText = new CheckboxMenuItem();

  CheckboxMenuItem displayNonconserved = new CheckboxMenuItem();

  Menu seqShowAnnotationsMenu = new Menu(
          MessageManager.getString("label.show_annotations"));

  Menu seqHideAnnotationsMenu = new Menu(
          MessageManager.getString("label.hide_annotations"));

  MenuItem seqAddReferenceAnnotations = new MenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  Menu groupShowAnnotationsMenu = new Menu(
          MessageManager.getString("label.show_annotations"));

  Menu groupHideAnnotationsMenu = new Menu(
          MessageManager.getString("label.hide_annotations"));

  MenuItem groupAddReferenceAnnotations = new MenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  Menu editMenu = new Menu(MessageManager.getString("action.edit"));

  MenuItem copy = new MenuItem(MessageManager.getString("action.copy"));

  MenuItem cut = new MenuItem(MessageManager.getString("action.cut"));

  MenuItem toUpper = new MenuItem(
          MessageManager.getString("label.to_upper_case"));

  MenuItem toLower = new MenuItem(
          MessageManager.getString("label.to_lower_case"));

  MenuItem toggleCase = new MenuItem(
          MessageManager.getString("label.toggle_case"));

  Menu outputmenu = new Menu();

  Menu seqMenu = new Menu();

  MenuItem pdb = new MenuItem();

  MenuItem hideSeqs = new MenuItem();

  MenuItem repGroup = new MenuItem();

  MenuItem sequenceName = new MenuItem(
          MessageManager.getString("label.edit_name_description"));

  MenuItem sequenceFeature = new MenuItem(
          MessageManager.getString("label.create_sequence_feature"));

  MenuItem editSequence = new MenuItem(
          MessageManager.getString("label.edit_sequence"));

  MenuItem sequenceDetails = new MenuItem(
          MessageManager.getString("label.sequence_details"));

  MenuItem selSeqDetails = new MenuItem(
          MessageManager.getString("label.sequence_details"));

  MenuItem makeReferenceSeq = new MenuItem();

  SequenceI seq;

  MenuItem revealAll = new MenuItem();

  MenuItem revealSeq = new MenuItem();

  /**
   * index of sequence to be revealed
   */
  int revealSeq_index = -1;

  Menu menu1 = new Menu();

  public APopupMenu(AlignmentPanel apanel, final SequenceI seq,
          Vector<String> links)
  {
    // /////////////////////////////////////////////////////////
    // If this is activated from the sequence panel, the user may want to
    // edit or annotate a particular residue. Therefore display the residue menu
    //
    // If from the IDPanel, we must display the sequence menu
    // ////////////////////////////////////////////////////////

    this.ap = apanel;
    this.seq = seq;

    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int i = 0; i < jalview.io.AppletFormatAdapter.WRITEABLE_FORMATS.length; i++)
    {
      MenuItem item = new MenuItem(
              jalview.io.AppletFormatAdapter.WRITEABLE_FORMATS[i]);

      item.addActionListener(this);
      outputmenu.add(item);
    }

    buildAnnotationSubmenus();

    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg != null && sg.getSize() > 0)
    {
      editGroupName.setLabel(MessageManager.formatMessage(
              "label.name_param", new Object[] { sg.getName() }));
      showText.setState(sg.getDisplayText());
      showColourText.setState(sg.getColourText());
      showBoxes.setState(sg.getDisplayBoxes());
      displayNonconserved.setState(sg.getShowNonconserved());
      if (!ap.av.getAlignment().getGroups().contains(sg))
      {
        menu1.setLabel(MessageManager.getString("action.edit_new_group"));
        groupMenu.remove(unGroupMenuItem);
      }
      else
      {
        menu1.setLabel(MessageManager.getString("action.edit_group"));
        groupMenu.remove(createGroupMenuItem);
      }

    }
    else
    {
      remove(hideSeqs);
      remove(groupMenu);
    }

    if (links != null && links.size() > 0)
    {
      Menu linkMenu = new Menu(MessageManager.getString("action.link"));
      for (int i = 0; i < links.size(); i++)
      {
        String link = links.elementAt(i);
        UrlLink urlLink = new UrlLink(link);
        if (!urlLink.isValid())
        {
          System.err.println(urlLink.getInvalidMessage());
          continue;
        }
        final String target = urlLink.getTarget(); // link.substring(0,
        // link.indexOf("|"));
        final String label = urlLink.getLabel();
        if (seq != null && urlLink.isDynamic())
        {

          // collect matching db-refs
          DBRefEntry[] dbr = jalview.util.DBRefUtils.selectRefs(
                  seq.getDBRef(), new String[] { target });
          // collect id string too
          String id = seq.getName();
          String descr = seq.getDescription();
          if (descr != null && descr.length() < 1)
          {
            descr = null;
          }
          if (dbr != null)
          {
            for (int r = 0; r < dbr.length; r++)
            {
              if (id != null && dbr[r].getAccessionId().equals(id))
              {
                // suppress duplicate link creation for the bare sequence ID
                // string with this link
                id = null;
              }
              // create Bare ID link for this RUL
              String[] urls = urlLink.makeUrls(dbr[r].getAccessionId(),
                      true);
              if (urls != null)
              {
                for (int u = 0; u < urls.length; u += 2)
                {
                  addshowLink(linkMenu, label + "|" + urls[u], urls[u + 1]);
                }
              }
            }
          }
          if (id != null)
          {
            // create Bare ID link for this RUL
            String[] urls = urlLink.makeUrls(id, true);
            if (urls != null)
            {
              for (int u = 0; u < urls.length; u += 2)
              {
                addshowLink(linkMenu, label, urls[u + 1]);
              }
            }
            // addshowLink(linkMenu, target, url_pref + id + url_suff);
          }
          // Now construct URLs from description but only try to do it for regex
          // URL links
          if (descr != null && urlLink.getRegexReplace() != null)
          {
            // create link for this URL from description only if regex matches
            String[] urls = urlLink.makeUrls(descr, true);
            if (urls != null)
            {
              for (int u = 0; u < urls.length; u += 2)
              {
                addshowLink(linkMenu, label, urls[u + 1]);
              }
            }
          }
        }
        else
        {
          addshowLink(linkMenu, target, urlLink.getUrl_prefix()); // link.substring(link.lastIndexOf("|")+1));
        }
        /*
         * final String url;
         * 
         * if (link.indexOf("$SEQUENCE_ID$") > -1) { // Substitute SEQUENCE_ID
         * string and any matching database reference accessions String url_pref
         * = link.substring(link.indexOf("|") + 1,
         * link.indexOf("$SEQUENCE_ID$"));
         * 
         * String url_suff = link.substring(link.indexOf("$SEQUENCE_ID$") + 13);
         * // collect matching db-refs DBRefEntry[] dbr =
         * jalview.util.DBRefUtils.selectRefs(seq.getDBRef(), new
         * String[]{target}); // collect id string too String id =
         * seq.getName(); if (id.indexOf("|") > -1) { id =
         * id.substring(id.lastIndexOf("|") + 1); } if (dbr!=null) { for (int
         * r=0;r<dbr.length; r++) { if (dbr[r].getAccessionId().equals(id)) { //
         * suppress duplicate link creation for the bare sequence ID string with
         * this link id = null; } addshowLink(linkMenu,
         * dbr[r].getSource()+"|"+dbr[r].getAccessionId(), target,
         * url_pref+dbr[r].getAccessionId()+url_suff); } } if (id!=null) { //
         * create Bare ID link for this RUL addshowLink(linkMenu, target,
         * url_pref + id + url_suff); } } else { addshowLink(linkMenu, target,
         * link.substring(link.lastIndexOf("|")+1)); }
         */
      }
      if (linkMenu.getItemCount() > 0)
      {
        if (seq != null)
        {
          seqMenu.add(linkMenu);
        }
        else
        {
          add(linkMenu);
        }
      }
    }
    // TODO: add group link menu entry here
    if (seq != null)
    {
      seqMenu.setLabel(seq.getName());
      if (seq == ap.av.getAlignment().getSeqrep())
      {
        makeReferenceSeq.setLabel(MessageManager
                .getString("action.unmark_as_reference"));// Unmark
                                                          // representative");
      }
      else
      {
        makeReferenceSeq.setLabel(MessageManager
                .getString("action.set_as_reference")); // );
      }
      repGroup.setLabel(MessageManager.formatMessage(
              "label.represent_group_with", new Object[] { seq.getName() }));
    }
    else
    {
      remove(seqMenu);
    }

    if (!ap.av.hasHiddenRows())
    {
      remove(revealAll);
      remove(revealSeq);
    }
    else
    {
      final int index = ap.av.getAlignment().findIndex(seq);

      if (ap.av.adjustForHiddenSeqs(index)
              - ap.av.adjustForHiddenSeqs(index - 1) > 1)
      {
        revealSeq_index = index;
      }
      else
      {
        remove(revealSeq);
      }
    }
  }

  /**
   * Build menus for annotation types that may be shown or hidden, and for
   * 'reference annotations' that may be added to the alignment.
   */
  private void buildAnnotationSubmenus()
  {
    /*
     * First for the currently selected sequence (if there is one):
     */
    final List<SequenceI> selectedSequence = (seq == null ? Collections
            .<SequenceI> emptyList() : Arrays.asList(seq));
    buildAnnotationTypesMenus(seqShowAnnotationsMenu,
            seqHideAnnotationsMenu, selectedSequence);
    configureReferenceAnnotationsMenu(seqAddReferenceAnnotations,
            selectedSequence);

    /*
     * and repeat for the current selection group (if there is one):
     */
    final List<SequenceI> selectedGroup = (ap.av.getSelectionGroup() == null ? Collections
            .<SequenceI> emptyList() : ap.av.getSelectionGroup()
            .getSequences());
    buildAnnotationTypesMenus(groupShowAnnotationsMenu,
            groupHideAnnotationsMenu, selectedGroup);
    configureReferenceAnnotationsMenu(groupAddReferenceAnnotations,
            selectedGroup);
  }

  /**
   * Determine whether or not to enable 'add reference annotations' menu item.
   * It is enable if there are any annotations, on any of the selected
   * sequences, which are not yet on the alignment (visible or not).
   * 
   * @param menu
   * @param forSequences
   */
  private void configureReferenceAnnotationsMenu(MenuItem menuItem,
          List<SequenceI> forSequences)
  {
    menuItem.setEnabled(false);

    /*
     * Temporary store to hold distinct calcId / type pairs for the tooltip.
     * Using TreeMap means calcIds are shown in alphabetical order.
     */
    Map<String, String> tipEntries = new TreeMap<String, String>();
    final Map<SequenceI, List<AlignmentAnnotation>> candidates = new LinkedHashMap<SequenceI, List<AlignmentAnnotation>>();
    AlignmentI al = this.ap.av.getAlignment();
    AlignmentUtils.findAddableReferenceAnnotations(forSequences,
            tipEntries, candidates, al);
    if (!candidates.isEmpty())
    {
      StringBuilder tooltip = new StringBuilder(64);
      tooltip.append(MessageManager.getString("label.add_annotations_for"));

      /*
       * Found annotations that could be added. Enable the menu item, and
       * configure its action.
       */
      menuItem.setEnabled(true);

      menuItem.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          addReferenceAnnotations_actionPerformed(candidates);
        }
      });
    }
  }

  /**
   * Add annotations to the sequences and to the alignment.
   * 
   * @param candidates
   *          a map whose keys are sequences on the alignment, and values a list
   *          of annotations to add to each sequence
   */
  protected void addReferenceAnnotations_actionPerformed(
          Map<SequenceI, List<AlignmentAnnotation>> candidates)
  {
    final SequenceGroup selectionGroup = this.ap.av.getSelectionGroup();
    final AlignmentI alignment = this.ap.getAlignment();
    AlignmentUtils.addReferenceAnnotations(candidates, alignment,
            selectionGroup);
    refresh();
  }

  /**
   * add a show URL menu item to the given linkMenu
   * 
   * @param linkMenu
   * @param target
   *          - menu label string
   * @param url
   *          - url to open
   */
  private void addshowLink(Menu linkMenu, final String target,
          final String url)
  {
    addshowLink(linkMenu, target, target, url);
  }

  /**
   * add a show URL menu item to the given linkMenu
   * 
   * @param linkMenu
   * @param target
   *          - URL target window
   * @param label
   *          - menu label string
   * @param url
   *          - url to open
   */
  private void addshowLink(Menu linkMenu, final String target,
          final String label, final String url)
  {
    MenuItem item = new MenuItem(label);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ap.alignFrame.showURL(url, target);
      }
    });
    linkMenu.add(item);
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == abovePIDColour)
    {
      abovePIDColour_itemStateChanged();
    }
    else if (evt.getSource() == showColourText)
    {
      showColourText_itemStateChanged();
    }
    else if (evt.getSource() == showText)
    {
      showText_itemStateChanged();
    }
    else if (evt.getSource() == showBoxes)
    {
      showBoxes_itemStateChanged();
    }
    else if (evt.getSource() == displayNonconserved)
    {
      this.showNonconserved_itemStateChanged();
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    Object source = evt.getSource();
    if (source == clustalColour)
    {
      clustalColour_actionPerformed();
    }
    else if (source == zappoColour)
    {
      zappoColour_actionPerformed();
    }
    else if (source == taylorColour)
    {
      taylorColour_actionPerformed();
    }
    else if (source == hydrophobicityColour)
    {
      hydrophobicityColour_actionPerformed();
    }
    else if (source == helixColour)
    {
      helixColour_actionPerformed();
    }
    else if (source == strandColour)
    {
      strandColour_actionPerformed();
    }
    else if (source == turnColour)
    {
      turnColour_actionPerformed();
    }
    else if (source == buriedColour)
    {
      buriedColour_actionPerformed();
    }
    else if (source == nucleotideMenuItem)
    {
      nucleotideMenuItem_actionPerformed();
    }

    else if (source == userDefinedColour)
    {
      userDefinedColour_actionPerformed();
    }
    else if (source == PIDColour)
    {
      PIDColour_actionPerformed();
    }
    else if (source == BLOSUM62Colour)
    {
      BLOSUM62Colour_actionPerformed();
    }
    else if (source == noColourmenuItem)
    {
      noColourmenuItem_actionPerformed();
    }
    else if (source == conservationMenuItem)
    {
      conservationMenuItem_itemStateChanged();
    }
    else if (source == unGroupMenuItem)
    {
      unGroupMenuItem_actionPerformed();
    }

    else if (source == createGroupMenuItem)
    {
      createGroupMenuItem_actionPerformed();
    }

    else if (source == sequenceName)
    {
      editName();
    }
    else if (source == makeReferenceSeq)
    {
      makeReferenceSeq_actionPerformed();
    }
    else if (source == sequenceDetails)
    {
      showSequenceDetails();
    }
    else if (source == selSeqDetails)
    {
      showSequenceSelectionDetails();
    }
    else if (source == pdb)
    {
      addPDB();
    }
    else if (source == hideSeqs)
    {
      hideSequences(false);
    }
    else if (source == repGroup)
    {
      hideSequences(true);
    }
    else if (source == revealSeq)
    {
      ap.av.showSequence(revealSeq_index);
    }
    else if (source == revealAll)
    {
      ap.av.showAllHiddenSeqs();
    }

    else if (source == editGroupName)
    {
      EditNameDialog dialog = new EditNameDialog(getGroup().getName(),
              getGroup().getDescription(), "       Group Name",
              "Group Description", ap.alignFrame,
              "Edit Group Name / Description", 500, 100, true);

      if (dialog.accept)
      {
        getGroup().setName(dialog.getName().replace(' ', '_'));
        getGroup().setDescription(dialog.getDescription());
      }

    }
    else if (source == copy)
    {
      ap.alignFrame.copy_actionPerformed();
    }
    else if (source == cut)
    {
      ap.alignFrame.cut_actionPerformed();
    }
    else if (source == editSequence)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();

      if (sg != null)
      {
        if (seq == null)
        {
          seq = sg.getSequenceAt(0);
        }

        EditNameDialog dialog = new EditNameDialog(seq.getSequenceAsString(
                sg.getStartRes(), sg.getEndRes() + 1), null,
                "Edit Sequence ", null,

                ap.alignFrame, "Edit Sequence", 500, 100, true);

        if (dialog.accept)
        {
          EditCommand editCommand = new EditCommand(
                  MessageManager.getString("label.edit_sequences"),
                  Action.REPLACE, dialog.getName().replace(' ',
                          ap.av.getGapCharacter()),
                  sg.getSequencesAsArray(ap.av.getHiddenRepSequences()),
                  sg.getStartRes(), sg.getEndRes() + 1,
                  ap.av.getAlignment());

          ap.alignFrame.addHistoryItem(editCommand);

          ap.av.firePropertyChange("alignment", null, ap.av.getAlignment()
                  .getSequences());
        }
      }
    }
    else if (source == toUpper || source == toLower || source == toggleCase)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();
      if (sg != null)
      {
        List<int[]> startEnd = ap.av.getVisibleRegionBoundaries(
                sg.getStartRes(), sg.getEndRes() + 1);

        String description;
        int caseChange;

        if (source == toggleCase)
        {
          description = "Toggle Case";
          caseChange = ChangeCaseCommand.TOGGLE_CASE;
        }
        else if (source == toUpper)
        {
          description = "To Upper Case";
          caseChange = ChangeCaseCommand.TO_UPPER;
        }
        else
        {
          description = "To Lower Case";
          caseChange = ChangeCaseCommand.TO_LOWER;
        }

        ChangeCaseCommand caseCommand = new ChangeCaseCommand(description,
                sg.getSequencesAsArray(ap.av.getHiddenRepSequences()),
                startEnd, caseChange);

        ap.alignFrame.addHistoryItem(caseCommand);

        ap.av.firePropertyChange("alignment", null, ap.av.getAlignment()
                .getSequences());

      }
    }
    else if (source == sequenceFeature)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();
      if (sg == null)
      {
        return;
      }

      int rsize = 0, gSize = sg.getSize();
      SequenceI[] rseqs, seqs = new SequenceI[gSize];
      SequenceFeature[] tfeatures, features = new SequenceFeature[gSize];

      for (int i = 0; i < gSize; i++)
      {
        int start = sg.getSequenceAt(i).findPosition(sg.getStartRes());
        int end = sg.findEndRes(sg.getSequenceAt(i));
        if (start <= end)
        {
          seqs[rsize] = sg.getSequenceAt(i);
          features[rsize] = new SequenceFeature(null, null, null, start,
                  end, "Jalview");
          rsize++;
        }
      }
      rseqs = new SequenceI[rsize];
      tfeatures = new SequenceFeature[rsize];
      System.arraycopy(seqs, 0, rseqs, 0, rsize);
      System.arraycopy(features, 0, tfeatures, 0, rsize);
      features = tfeatures;
      seqs = rseqs;

      if (ap.seqPanel.seqCanvas.getFeatureRenderer().amendFeatures(seqs,
              features, true, ap))
      {
        ap.alignFrame.sequenceFeatures.setState(true);
        ap.av.setShowSequenceFeatures(true);
        ;
        ap.highlightSearchResults(null);
      }
    }
    else
    {
      outputText(evt);
    }

  }

  void outputText(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, ap.alignFrame);

    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, MessageManager.formatMessage(
            "label.selection_output_command",
            new Object[] { e.getActionCommand() }), 600, 500);
    // JBPNote: getSelectionAsNewSequence behaviour has changed - this method
    // now returns a full copy of sequence data
    // TODO consider using getSequenceSelection instead here

    cap.setText(new jalview.io.AppletFormatAdapter().formatSequences(
            e.getActionCommand(), ap.av.getShowJVSuffix(), ap, true));

  }

  protected void showSequenceSelectionDetails()
  {
    createSequenceDetailsReport(ap.av.getSequenceSelection());
  }

  protected void showSequenceDetails()
  {
    createSequenceDetailsReport(new SequenceI[] { seq });
  }

  public void createSequenceDetailsReport(SequenceI[] sequences)
  {

    CutAndPasteTransfer cap = new CutAndPasteTransfer(false, ap.alignFrame);

    StringBuffer contents = new StringBuffer();
    for (SequenceI seq : sequences)
    {
      contents.append(MessageManager.formatMessage(
              "label.annotation_for_displayid",
              new Object[] { seq.getDisplayId(true) }));
      new SequenceAnnotationReport(null).createSequenceAnnotationReport(
              contents,
              seq,
              true,
              true,
              false,
              (ap.seqPanel.seqCanvas.fr != null) ? ap.seqPanel.seqCanvas.fr
                      .getMinMax() : null);
      contents.append("</p>");
    }
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, "Sequence Details for "
            + (sequences.length == 1 ? sequences[0].getDisplayId(true)
                    : "Selection"), 600, 500);
    cap.setText(MessageManager.formatMessage("label.html_content",
            new Object[] { contents.toString() }));
  }

  void editName()
  {
    EditNameDialog dialog = new EditNameDialog(seq.getName(),
            seq.getDescription(), "       Sequence Name",
            "Sequence Description", ap.alignFrame,
            "Edit Sequence Name / Description", 500, 100, true);

    if (dialog.accept)
    {
      seq.setName(dialog.getName());
      seq.setDescription(dialog.getDescription());
      ap.paintAlignment(false);
    }
  }

  void addPDB()
  {
    if (seq.getAllPDBEntries() != null)
    {
      PDBEntry entry = seq.getAllPDBEntries().firstElement();

      if (ap.av.applet.jmolAvailable)
      {
        new jalview.appletgui.AppletJmol(entry, new SequenceI[] { seq },
                null, ap, AppletFormatAdapter.URL);
      }
      else
      {
        new MCview.AppletPDBViewer(entry, new SequenceI[] { seq }, null,
                ap, AppletFormatAdapter.URL);
      }

    }
    else
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(true, ap.alignFrame);
      cap.setText(MessageManager.getString("label.paste_pdb_file"));
      cap.setPDBImport(seq);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame, MessageManager.formatMessage(
              "label.paste_pdb_file_for_sequence",
              new Object[] { seq.getName() }), 400, 300);
    }
  }

  private void jbInit() throws Exception
  {
    groupMenu.setLabel(MessageManager.getString("label.selection"));
    sequenceFeature.addActionListener(this);

    editGroupName.addActionListener(this);
    unGroupMenuItem.setLabel(MessageManager
            .getString("action.remove_group"));
    unGroupMenuItem.addActionListener(this);

    createGroupMenuItem.setLabel(MessageManager
            .getString("action.create_group"));
    createGroupMenuItem.addActionListener(this);

    nucleotideMenuItem.setLabel(MessageManager
            .getString("label.nucleotide"));
    nucleotideMenuItem.addActionListener(this);
    conservationMenuItem.addItemListener(this);
    abovePIDColour.addItemListener(this);
    colourMenu.setLabel(MessageManager.getString("label.group_colour"));
    showBoxes.setLabel(MessageManager.getString("action.boxes"));
    showBoxes.setState(true);
    showBoxes.addItemListener(this);
    sequenceName.addActionListener(this);
    sequenceDetails.addActionListener(this);
    selSeqDetails.addActionListener(this);
    displayNonconserved.setLabel(MessageManager
            .getString("label.show_non_conversed"));
    displayNonconserved.setState(false);
    displayNonconserved.addItemListener(this);
    showText.setLabel(MessageManager.getString("action.text"));
    showText.addItemListener(this);
    showColourText.setLabel(MessageManager.getString("label.colour_text"));
    showColourText.addItemListener(this);
    outputmenu.setLabel(MessageManager.getString("label.out_to_textbox"));
    seqMenu.setLabel(MessageManager.getString("label.sequence"));
    pdb.setLabel(MessageManager.getString("label.view_pdb_structure"));
    hideSeqs.setLabel(MessageManager.getString("action.hide_sequences"));
    repGroup.setLabel(MessageManager.formatMessage(
            "label.represent_group_with", new Object[] { "" }));
    revealAll.setLabel(MessageManager.getString("action.reveal_all"));
    revealSeq.setLabel(MessageManager.getString("action.reveal_sequences"));
    menu1.setLabel(MessageManager.getString("label.group") + ":");
    add(groupMenu);
    this.add(seqMenu);
    this.add(hideSeqs);
    this.add(revealSeq);
    this.add(revealAll);
    // groupMenu.add(selSeqDetails);
    groupMenu.add(groupShowAnnotationsMenu);
    groupMenu.add(groupHideAnnotationsMenu);
    groupMenu.add(groupAddReferenceAnnotations);
    groupMenu.add(editMenu);
    groupMenu.add(outputmenu);
    groupMenu.add(sequenceFeature);
    groupMenu.add(createGroupMenuItem);
    groupMenu.add(unGroupMenuItem);
    groupMenu.add(menu1);

    colourMenu.add(noColourmenuItem);
    colourMenu.add(clustalColour);
    colourMenu.add(BLOSUM62Colour);
    colourMenu.add(PIDColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydrophobicityColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(nucleotideMenuItem);
    colourMenu.add(userDefinedColour);
    colourMenu.addSeparator();
    colourMenu.add(abovePIDColour);
    colourMenu.add(conservationMenuItem);

    noColourmenuItem.setLabel(MessageManager.getString("label.none"));
    noColourmenuItem.addActionListener(this);

    clustalColour.setLabel(MessageManager
            .getString("label.clustalx_colours"));
    clustalColour.addActionListener(this);
    zappoColour.setLabel(MessageManager.getString("label.zappo"));
    zappoColour.addActionListener(this);
    taylorColour.setLabel(MessageManager.getString("label.taylor"));
    taylorColour.addActionListener(this);
    hydrophobicityColour.setLabel(MessageManager
            .getString("label.hydrophobicity"));
    hydrophobicityColour.addActionListener(this);
    helixColour
            .setLabel(MessageManager.getString("label.helix_propensity"));
    helixColour.addActionListener(this);
    strandColour.setLabel(MessageManager
            .getString("label.strand_propensity"));
    strandColour.addActionListener(this);
    turnColour.setLabel(MessageManager.getString("label.turn_propensity"));
    turnColour.addActionListener(this);
    buriedColour.setLabel(MessageManager.getString("label.buried_index"));
    buriedColour.addActionListener(this);
    abovePIDColour.setLabel(MessageManager
            .getString("label.above_identity_percentage"));

    userDefinedColour.setLabel(MessageManager
            .getString("action.user_defined"));
    userDefinedColour.addActionListener(this);
    PIDColour.setLabel(MessageManager
            .getString("label.percentage_identity"));
    PIDColour.addActionListener(this);
    BLOSUM62Colour.setLabel("BLOSUM62");
    BLOSUM62Colour.addActionListener(this);
    conservationMenuItem.setLabel(MessageManager
            .getString("label.conservation"));

    editMenu.add(copy);
    copy.addActionListener(this);
    editMenu.add(cut);
    cut.addActionListener(this);

    editMenu.add(editSequence);
    editSequence.addActionListener(this);

    editMenu.add(toUpper);
    toUpper.addActionListener(this);
    editMenu.add(toLower);
    toLower.addActionListener(this);
    editMenu.add(toggleCase);
    seqMenu.add(seqShowAnnotationsMenu);
    seqMenu.add(seqHideAnnotationsMenu);
    seqMenu.add(seqAddReferenceAnnotations);
    seqMenu.add(sequenceName);
    seqMenu.add(makeReferenceSeq);
    // seqMenu.add(sequenceDetails);

    if (!ap.av.applet.useXtrnalSviewer)
    {
      seqMenu.add(pdb);
    }
    seqMenu.add(repGroup);
    menu1.add(editGroupName);
    menu1.add(colourMenu);
    menu1.add(showBoxes);
    menu1.add(showText);
    menu1.add(showColourText);
    menu1.add(displayNonconserved);
    toggleCase.addActionListener(this);
    pdb.addActionListener(this);
    hideSeqs.addActionListener(this);
    repGroup.addActionListener(this);
    revealAll.addActionListener(this);
    revealSeq.addActionListener(this);
    makeReferenceSeq.addActionListener(this);
  }

  void refresh()
  {
    ap.paintAlignment(true);
  }

  protected void clustalColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new ClustalxColourScheme(sg, ap.av.getHiddenRepSequences());
    refresh();
  }

  protected void zappoColour_actionPerformed()
  {
    getGroup().cs = new ZappoColourScheme();
    refresh();
  }

  protected void taylorColour_actionPerformed()
  {
    getGroup().cs = new TaylorColourScheme();
    refresh();
  }

  protected void hydrophobicityColour_actionPerformed()
  {
    getGroup().cs = new HydrophobicColourScheme();
    refresh();
  }

  protected void helixColour_actionPerformed()
  {
    getGroup().cs = new HelixColourScheme();
    refresh();
  }

  protected void strandColour_actionPerformed()
  {
    getGroup().cs = new StrandColourScheme();
    refresh();
  }

  protected void turnColour_actionPerformed()
  {
    getGroup().cs = new TurnColourScheme();
    refresh();
  }

  protected void buriedColour_actionPerformed()
  {
    getGroup().cs = new BuriedColourScheme();
    refresh();
  }

  public void nucleotideMenuItem_actionPerformed()
  {
    getGroup().cs = new NucleotideColourScheme();
    refresh();
  }

  protected void abovePIDColour_itemStateChanged()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (abovePIDColour.getState())
    {
      sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av
              .getHiddenRepSequences()), 0, ap.av.getAlignment().getWidth()));
      int threshold = SliderPanel.setPIDSliderSource(ap, sg.cs, getGroup()
              .getName());

      sg.cs.setThreshold(threshold, ap.av.isIgnoreGapsConsensus());

      SliderPanel.showPIDSlider();

    }
    else
    // remove PIDColouring
    {
      sg.cs.setThreshold(0, ap.av.isIgnoreGapsConsensus());
    }

    refresh();

  }

  protected void userDefinedColour_actionPerformed()
  {
    new UserDefinedColours(ap, getGroup());
  }

  protected void PIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new PIDColourScheme();
    sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av
            .getHiddenRepSequences()), 0, ap.av.getAlignment().getWidth()));
    refresh();
  }

  protected void BLOSUM62Colour_actionPerformed()
  {
    SequenceGroup sg = getGroup();

    sg.cs = new Blosum62ColourScheme();

    sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av
            .getHiddenRepSequences()), 0, ap.av.getAlignment().getWidth()));

    refresh();
  }

  protected void noColourmenuItem_actionPerformed()
  {
    getGroup().cs = null;
    refresh();
  }

  protected void conservationMenuItem_itemStateChanged()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (conservationMenuItem.getState())
    {

      sg.cs.setConservation(Conservation.calculateConservation("Group",
              ResidueProperties.propHash, 3, sg.getSequences(ap.av
                      .getHiddenRepSequences()), 0, ap.av.getAlignment()
                      .getWidth(), false, ap.av.getConsPercGaps(), false));
      SliderPanel.setConservationSlider(ap, sg.cs, sg.getName());
      SliderPanel.showConservationSlider();
    }
    else
    // remove ConservationColouring
    {
      sg.cs.setConservation(null);
    }

    refresh();
  }

  SequenceGroup getGroup()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();

    // this method won't add a new group if it already exists
    if (sg != null)
    {
      ap.av.getAlignment().addGroup(sg);
    }

    return sg;
  }

  void unGroupMenuItem_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    ap.av.getAlignment().deleteGroup(sg);
    ap.av.setSelectionGroup(null);
    ap.paintAlignment(true);
  }

  void createGroupMenuItem_actionPerformed()
  {
    getGroup(); // implicitly create group
    refresh();
  }

  public void showColourText_itemStateChanged()
  {
    getGroup().setColourText(showColourText.getState());
    refresh();
  }

  public void showText_itemStateChanged()
  {
    getGroup().setDisplayText(showText.getState());
    refresh();
  }

  public void makeReferenceSeq_actionPerformed()
  {
    if (!ap.av.getAlignment().hasSeqrep())
    {
      // initialise the display flags so the user sees something happen
      ap.av.setDisplayReferenceSeq(true);
      ap.av.setColourByReferenceSeq(true);
      ap.av.getAlignment().setSeqrep(seq);
    }
    else
    {
      if (ap.av.getAlignment().getSeqrep() == seq)
      {
        ap.av.getAlignment().setSeqrep(null);
      }
      else
      {
        ap.av.getAlignment().setSeqrep(seq);
      }
    }
    refresh();
  }

  public void showNonconserved_itemStateChanged()
  {
    getGroup().setShowNonconserved(this.displayNonconserved.getState());
    refresh();
  }

  public void showBoxes_itemStateChanged()
  {
    getGroup().setDisplayBoxes(showBoxes.getState());
    refresh();
  }

  void hideSequences(boolean representGroup)
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null || sg.getSize() < 1)
    {
      ap.av.hideSequence(new SequenceI[] { seq });
      return;
    }

    ap.av.setSelectionGroup(null);

    if (representGroup)
    {
      ap.av.hideRepSequences(seq, sg);

      return;
    }

    int gsize = sg.getSize();
    SequenceI[] hseqs;

    hseqs = new SequenceI[gsize];

    int index = 0;
    for (int i = 0; i < gsize; i++)
    {
      hseqs[index++] = sg.getSequenceAt(i);
    }

    ap.av.hideSequence(hseqs);
    ap.av.sendSelection();
  }

  /**
   * Add annotation types to 'Show annotations' and/or 'Hide annotations' menus.
   * "All" is added first, followed by a separator. Then add any annotation
   * types associated with the current selection. Separate menus are built for
   * the selected sequence group (if any), and the selected sequence.
   * <p>
   * Some annotation rows are always rendered together - these can be identified
   * by a common graphGroup property > -1. Only one of each group will be marked
   * as visible (to avoid duplication of the display). For such groups we add a
   * composite type name, e.g.
   * <p>
   * IUPredWS (Long), IUPredWS (Short)
   * 
   * @param seq
   */
  protected void buildAnnotationTypesMenus(Menu showMenu, Menu hideMenu,
          List<SequenceI> forSequences)
  {
    showMenu.removeAll();
    hideMenu.removeAll();

    final List<String> all = Arrays.asList(ALL_ANNOTATIONS);
    addAnnotationTypeToShowHide(showMenu, forSequences, "", all, true, true);
    addAnnotationTypeToShowHide(hideMenu, forSequences, "", all, true,
            false);
    showMenu.addSeparator();
    hideMenu.addSeparator();

    final AlignmentAnnotation[] annotations = ap.getAlignment()
            .getAlignmentAnnotation();

    /*
     * Find shown/hidden annotations types, distinguished by source (calcId),
     * and grouped by graphGroup. Using LinkedHashMap means we will retrieve in
     * the insertion order, which is the order of the annotations on the
     * alignment.
     */
    Map<String, List<List<String>>> shownTypes = new LinkedHashMap<String, List<List<String>>>();
    Map<String, List<List<String>>> hiddenTypes = new LinkedHashMap<String, List<List<String>>>();
    AlignmentAnnotationUtils.getShownHiddenTypes(shownTypes, hiddenTypes,
            AlignmentAnnotationUtils.asList(annotations), forSequences);

    for (String calcId : hiddenTypes.keySet())
    {
      for (List<String> type : hiddenTypes.get(calcId))
      {
        addAnnotationTypeToShowHide(showMenu, forSequences, calcId, type,
                false, true);
      }
    }
    // grey out 'show annotations' if none are hidden
    showMenu.setEnabled(!hiddenTypes.isEmpty());

    for (String calcId : shownTypes.keySet())
    {
      for (List<String> type : shownTypes.get(calcId))
      {
        addAnnotationTypeToShowHide(hideMenu, forSequences, calcId, type,
                false, false);
      }
    }
    // grey out 'hide annotations' if none are shown
    hideMenu.setEnabled(!shownTypes.isEmpty());
  }

  /**
   * Add one annotation type to the 'Show Annotations' or 'Hide Annotations'
   * menus.
   * 
   * @param showOrHideMenu
   *          the menu to add to
   * @param forSequences
   *          the sequences whose annotations may be shown or hidden
   * @param calcId
   * @param types
   *          the label to add
   * @param allTypes
   *          if true this is a special label meaning 'All'
   * @param actionIsShow
   *          if true, the select menu item action is to show the annotation
   *          type, else hide
   */
  protected void addAnnotationTypeToShowHide(Menu showOrHideMenu,
          final List<SequenceI> forSequences, String calcId,
          final List<String> types, final boolean allTypes,
          final boolean actionIsShow)
  {
    String label = types.toString(); // [a, b, c]
    label = label.substring(1, label.length() - 1);
    final MenuItem item = new MenuItem(label);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        AlignmentUtils.showOrHideSequenceAnnotations(ap.getAlignment(),
                types, forSequences, allTypes, actionIsShow);
        refresh();
      }
    });
    showOrHideMenu.add(item);
  }

}
