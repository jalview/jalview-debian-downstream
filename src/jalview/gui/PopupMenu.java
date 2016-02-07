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

import jalview.analysis.AAFrequency;
import jalview.analysis.AlignmentAnnotationUtils;
import jalview.analysis.AlignmentUtils;
import jalview.analysis.Conservation;
import jalview.commands.ChangeCaseCommand;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.FormatAdapter;
import jalview.io.SequenceAnnotationReport;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.ResidueProperties;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.UserColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.util.GroupUrlLink;
import jalview.util.GroupUrlLink.UrlStringTooLongException;
import jalview.util.MessageManager;
import jalview.util.UrlLink;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.118 $
 */
public class PopupMenu extends JPopupMenu
{
  private static final String ALL_ANNOTATIONS = "All";

  private static final String COMMA = ",";

  JMenu groupMenu = new JMenu();

  JMenuItem groupName = new JMenuItem();

  protected JRadioButtonMenuItem clustalColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem zappoColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem taylorColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem hydrophobicityColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem helixColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem strandColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem turnColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem buriedColour = new JRadioButtonMenuItem();

  protected JCheckBoxMenuItem abovePIDColour = new JCheckBoxMenuItem();

  protected JRadioButtonMenuItem userDefinedColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem PIDColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem BLOSUM62Colour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem purinePyrimidineColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem RNAInteractionColour = new JRadioButtonMenuItem();

  JRadioButtonMenuItem noColourmenuItem = new JRadioButtonMenuItem();

  protected JCheckBoxMenuItem conservationMenuItem = new JCheckBoxMenuItem();

  AlignmentPanel ap;

  JMenu sequenceMenu = new JMenu();

  JMenuItem sequenceName = new JMenuItem();

  JMenuItem sequenceDetails = new JMenuItem();

  JMenuItem sequenceSelDetails = new JMenuItem();

  JMenuItem makeReferenceSeq = new JMenuItem();

  JMenuItem chooseAnnotations = new JMenuItem();

  SequenceI sequence;

  JMenuItem createGroupMenuItem = new JMenuItem();

  JMenuItem unGroupMenuItem = new JMenuItem();

  JMenuItem outline = new JMenuItem();

  JRadioButtonMenuItem nucleotideMenuItem = new JRadioButtonMenuItem();

  JMenu colourMenu = new JMenu();

  JCheckBoxMenuItem showBoxes = new JCheckBoxMenuItem();

  JCheckBoxMenuItem showText = new JCheckBoxMenuItem();

  JCheckBoxMenuItem showColourText = new JCheckBoxMenuItem();

  JCheckBoxMenuItem displayNonconserved = new JCheckBoxMenuItem();

  JMenu editMenu = new JMenu();

  JMenuItem cut = new JMenuItem();

  JMenuItem copy = new JMenuItem();

  JMenuItem upperCase = new JMenuItem();

  JMenuItem lowerCase = new JMenuItem();

  JMenuItem toggle = new JMenuItem();

  JMenu pdbMenu = new JMenu();

  JMenuItem pdbFromFile = new JMenuItem();

  JMenuItem enterPDB = new JMenuItem();

  JMenuItem discoverPDB = new JMenuItem();

  JMenu outputMenu = new JMenu();

  JMenu seqShowAnnotationsMenu = new JMenu();

  JMenu seqHideAnnotationsMenu = new JMenu();

  JMenuItem seqAddReferenceAnnotations = new JMenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  JMenu groupShowAnnotationsMenu = new JMenu();

  JMenu groupHideAnnotationsMenu = new JMenu();

  JMenuItem groupAddReferenceAnnotations = new JMenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  JMenuItem sequenceFeature = new JMenuItem();

  JMenuItem textColour = new JMenuItem();

  JMenu jMenu1 = new JMenu();

  JMenuItem pdbStructureDialog = new JMenuItem();

  JMenu rnaStructureMenu = new JMenu();

  JMenuItem editSequence = new JMenuItem();

  JMenu groupLinksMenu;

  JMenuItem hideInsertions = new JMenuItem();

  /**
   * Creates a new PopupMenu object.
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param seq
   *          DOCUMENT ME!
   */
  public PopupMenu(final AlignmentPanel ap, Sequence seq, Vector links)
  {
    this(ap, seq, links, null);
  }

  /**
   * 
   * @param ap
   * @param seq
   * @param links
   * @param groupLinks
   */
  public PopupMenu(final AlignmentPanel ap, final SequenceI seq,
          Vector links, Vector groupLinks)
  {
    // /////////////////////////////////////////////////////////
    // If this is activated from the sequence panel, the user may want to
    // edit or annotate a particular residue. Therefore display the residue menu
    //
    // If from the IDPanel, we must display the sequence menu
    // ////////////////////////////////////////////////////////
    this.ap = ap;
    sequence = seq;

    ButtonGroup colours = new ButtonGroup();
    colours.add(noColourmenuItem);
    colours.add(clustalColour);
    colours.add(zappoColour);
    colours.add(taylorColour);
    colours.add(hydrophobicityColour);
    colours.add(helixColour);
    colours.add(strandColour);
    colours.add(turnColour);
    colours.add(buriedColour);
    colours.add(abovePIDColour);
    colours.add(userDefinedColour);
    colours.add(PIDColour);
    colours.add(BLOSUM62Colour);
    colours.add(purinePyrimidineColour);
    colours.add(RNAInteractionColour);

    for (int i = 0; i < jalview.io.FormatAdapter.WRITEABLE_FORMATS.length; i++)
    {
      JMenuItem item = new JMenuItem(
              jalview.io.FormatAdapter.WRITEABLE_FORMATS[i]);

      item.addActionListener(new java.awt.event.ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          outputText_actionPerformed(e);
        }
      });

      outputMenu.add(item);
    }

    /*
     * Build menus for annotation types that may be shown or hidden, and for
     * 'reference annotations' that may be added to the alignment. First for the
     * currently selected sequence (if there is one):
     */
    final List<SequenceI> selectedSequence = (seq == null ? Collections
            .<SequenceI> emptyList() : Arrays.asList(seq));
    buildAnnotationTypesMenus(seqShowAnnotationsMenu,
            seqHideAnnotationsMenu, selectedSequence);
    configureReferenceAnnotationsMenu(seqAddReferenceAnnotations,
            selectedSequence);

    /*
     * And repeat for the current selection group (if there is one):
     */
    final List<SequenceI> selectedGroup = (ap.av.getSelectionGroup() == null ? Collections
            .<SequenceI> emptyList() : ap.av.getSelectionGroup()
            .getSequences());
    buildAnnotationTypesMenus(groupShowAnnotationsMenu,
            groupHideAnnotationsMenu, selectedGroup);
    configureReferenceAnnotationsMenu(groupAddReferenceAnnotations,
            selectedGroup);

    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    JMenuItem menuItem;
    if (seq != null)
    {
      sequenceMenu.setText(sequence.getName());
      if (seq == ap.av.getAlignment().getSeqrep())
      {
        makeReferenceSeq.setText(MessageManager
                .getString("action.unmark_as_reference"));
      }
      else
      {
        makeReferenceSeq.setText(MessageManager
                .getString("action.set_as_reference"));
      }

      if (!ap.av.getAlignment().isNucleotide())
      {
        remove(rnaStructureMenu);
      }
      else
      {
        int origCount = rnaStructureMenu.getItemCount();
        /*
         * add menu items to 2D-render any alignment or sequence secondary
         * structure annotation
         */
        AlignmentAnnotation[] aas = ap.av.getAlignment()
                .getAlignmentAnnotation();
        if (aas != null)
        {
          for (final AlignmentAnnotation aa : aas)
          {
            if (aa.isValidStruc() && aa.sequenceRef == null)
            {
              /*
               * valid alignment RNA secondary structure annotation
               */
              menuItem = new JMenuItem();
              menuItem.setText(MessageManager.formatMessage(
                      "label.2d_rna_structure_line",
                      new Object[] { aa.label }));
              menuItem.addActionListener(new java.awt.event.ActionListener()
              {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                  new AppVarna(seq, aa, ap);
                }
              });
              rnaStructureMenu.add(menuItem);
            }
          }
        }

        if (seq.getAnnotation() != null)
        {
          AlignmentAnnotation seqAnns[] = seq.getAnnotation();
          for (final AlignmentAnnotation aa : seqAnns)
          {
            if (aa.isValidStruc())
            {
              /*
               * valid sequence RNA secondary structure annotation
               */
              // TODO: make rnastrucF a bit more nice
              menuItem = new JMenuItem();
              menuItem.setText(MessageManager.formatMessage(
                      "label.2d_rna_sequence_name",
                      new Object[] { seq.getName() }));
              menuItem.addActionListener(new java.awt.event.ActionListener()
              {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                  // TODO: VARNA does'nt print gaps in the sequence
                  new AppVarna(seq, aa, ap);
                }
              });
              rnaStructureMenu.add(menuItem);
            }
          }
        }
        if (rnaStructureMenu.getItemCount() == origCount)
        {
          remove(rnaStructureMenu);
        }
      }

      menuItem = new JMenuItem(
              MessageManager.getString("action.hide_sequences"));
      menuItem.addActionListener(new java.awt.event.ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          hideSequences(false);
        }
      });
      add(menuItem);

      if (ap.av.getSelectionGroup() != null
              && ap.av.getSelectionGroup().getSize() > 1)
      {
        menuItem = new JMenuItem(MessageManager.formatMessage(
                "label.represent_group_with",
                new Object[] { seq.getName() }));
        menuItem.addActionListener(new java.awt.event.ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            hideSequences(true);
          }
        });
        sequenceMenu.add(menuItem);
      }

      if (ap.av.hasHiddenRows())
      {
        final int index = ap.av.getAlignment().findIndex(seq);

        if (ap.av.adjustForHiddenSeqs(index)
                - ap.av.adjustForHiddenSeqs(index - 1) > 1)
        {
          menuItem = new JMenuItem(
                  MessageManager.getString("action.reveal_sequences"));
          menuItem.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              ap.av.showSequence(index);
              if (ap.overviewPanel != null)
              {
                ap.overviewPanel.updateOverviewImage();
              }
            }
          });
          add(menuItem);
        }
      }
    }
    // for the case when no sequences are even visible
    if (ap.av.hasHiddenRows())
    {
      {
        menuItem = new JMenuItem(
                MessageManager.getString("action.reveal_all"));
        menuItem.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            ap.av.showAllHiddenSeqs();
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
          }
        });

        add(menuItem);
      }

    }

    SequenceGroup sg = ap.av.getSelectionGroup();
    boolean isDefinedGroup = (sg != null) ? ap.av.getAlignment()
            .getGroups().contains(sg) : false;

    if (sg != null && sg.getSize() > 0)
    {
      groupName.setText(MessageManager.formatMessage("label.name_param",
              new Object[] { sg.getName() }));
      groupName.setText(MessageManager
              .getString("label.edit_name_and_description_current_group"));

      if (sg.cs instanceof ZappoColourScheme)
      {
        zappoColour.setSelected(true);
      }
      else if (sg.cs instanceof TaylorColourScheme)
      {
        taylorColour.setSelected(true);
      }
      else if (sg.cs instanceof PIDColourScheme)
      {
        PIDColour.setSelected(true);
      }
      else if (sg.cs instanceof Blosum62ColourScheme)
      {
        BLOSUM62Colour.setSelected(true);
      }
      else if (sg.cs instanceof UserColourScheme)
      {
        userDefinedColour.setSelected(true);
      }
      else if (sg.cs instanceof HydrophobicColourScheme)
      {
        hydrophobicityColour.setSelected(true);
      }
      else if (sg.cs instanceof HelixColourScheme)
      {
        helixColour.setSelected(true);
      }
      else if (sg.cs instanceof StrandColourScheme)
      {
        strandColour.setSelected(true);
      }
      else if (sg.cs instanceof TurnColourScheme)
      {
        turnColour.setSelected(true);
      }
      else if (sg.cs instanceof BuriedColourScheme)
      {
        buriedColour.setSelected(true);
      }
      else if (sg.cs instanceof ClustalxColourScheme)
      {
        clustalColour.setSelected(true);
      }
      else if (sg.cs instanceof PurinePyrimidineColourScheme)
      {
        purinePyrimidineColour.setSelected(true);
      }

      /*
       * else if (sg.cs instanceof CovariationColourScheme) {
       * covariationColour.setSelected(true); }
       */
      else
      {
        noColourmenuItem.setSelected(true);
      }

      if (sg.cs != null && sg.cs.conservationApplied())
      {
        conservationMenuItem.setSelected(true);
      }
      displayNonconserved.setSelected(sg.getShowNonconserved());
      showText.setSelected(sg.getDisplayText());
      showColourText.setSelected(sg.getColourText());
      showBoxes.setSelected(sg.getDisplayBoxes());
      // add any groupURLs to the groupURL submenu and make it visible
      if (groupLinks != null && groupLinks.size() > 0)
      {
        buildGroupURLMenu(sg, groupLinks);
      }
      // Add a 'show all structures' for the current selection
      Hashtable<String, PDBEntry> pdbe = new Hashtable<String, PDBEntry>(), reppdb = new Hashtable<String, PDBEntry>();
      SequenceI sqass = null;
      for (SequenceI sq : ap.av.getSequenceSelection())
      {
        Vector<PDBEntry> pes = sq.getDatasetSequence().getAllPDBEntries();
        if (pes != null && pes.size() > 0)
        {
          reppdb.put(pes.get(0).getId(), pes.get(0));
          for (PDBEntry pe : pes)
          {
            pdbe.put(pe.getId(), pe);
            if (sqass == null)
            {
              sqass = sq;
            }
          }
        }
      }
      if (pdbe.size() > 0)
      {
        final PDBEntry[] pe = pdbe.values().toArray(
                new PDBEntry[pdbe.size()]), pr = reppdb.values().toArray(
                new PDBEntry[reppdb.size()]);
        final JMenuItem gpdbview, rpdbview;
      }
    }
    else
    {
      groupMenu.setVisible(false);
      editMenu.setVisible(false);
    }

    if (!isDefinedGroup)
    {
      createGroupMenuItem.setVisible(true);
      unGroupMenuItem.setVisible(false);
      jMenu1.setText(MessageManager.getString("action.edit_new_group"));
    }
    else
    {
      createGroupMenuItem.setVisible(false);
      unGroupMenuItem.setVisible(true);
      jMenu1.setText(MessageManager.getString("action.edit_group"));
    }

    if (seq == null)
    {
      sequenceMenu.setVisible(false);
      pdbStructureDialog.setVisible(false);
      rnaStructureMenu.setVisible(false);
    }

    if (links != null && links.size() > 0)
    {

      JMenu linkMenu = new JMenu(MessageManager.getString("action.link"));
      Vector linkset = new Vector();
      for (int i = 0; i < links.size(); i++)
      {
        String link = links.elementAt(i).toString();
        UrlLink urlLink = null;
        try
        {
          urlLink = new UrlLink(link);
        } catch (Exception foo)
        {
          jalview.bin.Cache.log.error("Exception for URLLink '" + link
                  + "'", foo);
          continue;
        }
        ;
        if (!urlLink.isValid())
        {
          jalview.bin.Cache.log.error(urlLink.getInvalidMessage());
          continue;
        }
        final String label = urlLink.getLabel();
        if (seq != null && urlLink.isDynamic())
        {

          // collect matching db-refs
          DBRefEntry[] dbr = jalview.util.DBRefUtils.selectRefs(
                  seq.getDBRef(), new String[] { urlLink.getTarget() });
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
                  if (!linkset.contains(urls[u] + "|" + urls[u + 1]))
                  {
                    linkset.addElement(urls[u] + "|" + urls[u + 1]);
                    addshowLink(linkMenu, label + "|" + urls[u],
                            urls[u + 1]);
                  }
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
                if (!linkset.contains(urls[u] + "|" + urls[u + 1]))
                {
                  linkset.addElement(urls[u] + "|" + urls[u + 1]);
                  addshowLink(linkMenu, label, urls[u + 1]);
                }
              }
            }
          }
          // Create urls from description but only for URL links which are regex
          // links
          if (descr != null && urlLink.getRegexReplace() != null)
          {
            // create link for this URL from description where regex matches
            String[] urls = urlLink.makeUrls(descr, true);
            if (urls != null)
            {
              for (int u = 0; u < urls.length; u += 2)
              {
                if (!linkset.contains(urls[u] + "|" + urls[u + 1]))
                {
                  linkset.addElement(urls[u] + "|" + urls[u + 1]);
                  addshowLink(linkMenu, label, urls[u + 1]);
                }
              }
            }
          }
        }
        else
        {
          if (!linkset.contains(label + "|" + urlLink.getUrl_prefix()))
          {
            linkset.addElement(label + "|" + urlLink.getUrl_prefix());
            // Add a non-dynamic link
            addshowLink(linkMenu, label, urlLink.getUrl_prefix());
          }
        }
      }
      if (sequence != null)
      {
        sequenceMenu.add(linkMenu);
      }
      else
      {
        add(linkMenu);
      }
    }
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
  protected void buildAnnotationTypesMenus(JMenu showMenu, JMenu hideMenu,
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
   * Returns a list of sequences - either the current selection group (if there
   * is one), else the specified single sequence.
   * 
   * @param seq
   * @return
   */
  protected List<SequenceI> getSequenceScope(SequenceI seq)
  {
    List<SequenceI> forSequences = null;
    final SequenceGroup selectionGroup = ap.av.getSelectionGroup();
    if (selectionGroup != null && selectionGroup.getSize() > 0)
    {
      forSequences = selectionGroup.getSequences();
    }
    else
    {
      forSequences = seq == null ? Collections.<SequenceI> emptyList()
              : Arrays.asList(seq);
    }
    return forSequences;
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
  protected void addAnnotationTypeToShowHide(JMenu showOrHideMenu,
          final List<SequenceI> forSequences, String calcId,
          final List<String> types, final boolean allTypes,
          final boolean actionIsShow)
  {
    String label = types.toString(); // [a, b, c]
    label = label.substring(1, label.length() - 1); // a, b, c
    final JMenuItem item = new JMenuItem(label);
    item.setToolTipText(calcId);
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

  private void buildGroupURLMenu(SequenceGroup sg, Vector groupLinks)
  {

    // TODO: usability: thread off the generation of group url content so root
    // menu appears asap
    // sequence only URLs
    // ID/regex match URLs
    groupLinksMenu = new JMenu(
            MessageManager.getString("action.group_link"));
    JMenu[] linkMenus = new JMenu[] { null,
        new JMenu(MessageManager.getString("action.ids")),
        new JMenu(MessageManager.getString("action.sequences")),
        new JMenu(MessageManager.getString("action.ids_sequences")) }; // three
                                                                       // types
                                                                       // of url
                                                                       // that
                                                                       // might
                                                                       // be
    // created.
    SequenceI[] seqs = ap.av.getSelectionAsNewSequence();
    String[][] idandseqs = GroupUrlLink.formStrings(seqs);
    Hashtable commonDbrefs = new Hashtable();
    for (int sq = 0; sq < seqs.length; sq++)
    {

      int start = seqs[sq].findPosition(sg.getStartRes()), end = seqs[sq]
              .findPosition(sg.getEndRes());
      // just collect ids from dataset sequence
      // TODO: check if IDs collected from selecton group intersects with the
      // current selection, too
      SequenceI sqi = seqs[sq];
      while (sqi.getDatasetSequence() != null)
      {
        sqi = sqi.getDatasetSequence();
      }
      DBRefEntry[] dbr = sqi.getDBRef();
      if (dbr != null && dbr.length > 0)
      {
        for (int d = 0; d < dbr.length; d++)
        {
          String src = dbr[d].getSource(); // jalview.util.DBRefUtils.getCanonicalName(dbr[d].getSource()).toUpperCase();
          Object[] sarray = (Object[]) commonDbrefs.get(src);
          if (sarray == null)
          {
            sarray = new Object[2];
            sarray[0] = new int[] { 0 };
            sarray[1] = new String[seqs.length];

            commonDbrefs.put(src, sarray);
          }

          if (((String[]) sarray[1])[sq] == null)
          {
            if (!dbr[d].hasMap()
                    || (dbr[d].getMap().locateMappedRange(start, end) != null))
            {
              ((String[]) sarray[1])[sq] = dbr[d].getAccessionId();
              ((int[]) sarray[0])[0]++;
            }
          }
        }
      }
    }
    // now create group links for all distinct ID/sequence sets.
    boolean addMenu = false; // indicates if there are any group links to give
                             // to user
    for (int i = 0; i < groupLinks.size(); i++)
    {
      String link = groupLinks.elementAt(i).toString();
      GroupUrlLink urlLink = null;
      try
      {
        urlLink = new GroupUrlLink(link);
      } catch (Exception foo)
      {
        jalview.bin.Cache.log.error("Exception for GroupURLLink '" + link
                + "'", foo);
        continue;
      }
      ;
      if (!urlLink.isValid())
      {
        jalview.bin.Cache.log.error(urlLink.getInvalidMessage());
        continue;
      }
      final String label = urlLink.getLabel();
      boolean usingNames = false;
      // Now see which parts of the group apply for this URL
      String ltarget = urlLink.getTarget(); // jalview.util.DBRefUtils.getCanonicalName(urlLink.getTarget());
      Object[] idset = (Object[]) commonDbrefs.get(ltarget.toUpperCase());
      String[] seqstr, ids; // input to makeUrl
      if (idset != null)
      {
        int numinput = ((int[]) idset[0])[0];
        String[] allids = ((String[]) idset[1]);
        seqstr = new String[numinput];
        ids = new String[numinput];
        for (int sq = 0, idcount = 0; sq < seqs.length; sq++)
        {
          if (allids[sq] != null)
          {
            ids[idcount] = allids[sq];
            seqstr[idcount++] = idandseqs[1][sq];
          }
        }
      }
      else
      {
        // just use the id/seq set
        seqstr = idandseqs[1];
        ids = idandseqs[0];
        usingNames = true;
      }
      // and try and make the groupURL!

      Object[] urlset = null;
      try
      {
        urlset = urlLink.makeUrlStubs(ids, seqstr,
                "FromJalview" + System.currentTimeMillis(), false);
      } catch (UrlStringTooLongException e)
      {
      }
      if (urlset != null)
      {
        int type = urlLink.getGroupURLType() & 3;
        // first two bits ofurlLink type bitfield are sequenceids and sequences
        // TODO: FUTURE: ensure the groupURL menu structure can be generalised
        addshowLink(linkMenus[type], label
                + (((type & 1) == 1) ? ("("
                        + (usingNames ? "Names" : ltarget) + ")") : ""),
                urlLink, urlset);
        addMenu = true;
      }
    }
    if (addMenu)
    {
      groupLinksMenu = new JMenu(
              MessageManager.getString("action.group_link"));
      for (int m = 0; m < linkMenus.length; m++)
      {
        if (linkMenus[m] != null
                && linkMenus[m].getMenuComponentCount() > 0)
        {
          groupLinksMenu.add(linkMenus[m]);
        }
      }

      groupMenu.add(groupLinksMenu);
    }
  }

  /**
   * add a show URL menu item to the given linkMenu
   * 
   * @param linkMenu
   * @param label
   *          - menu label string
   * @param url
   *          - url to open
   */
  private void addshowLink(JMenu linkMenu, String label, final String url)
  {
    JMenuItem item = new JMenuItem(label);
    item.setToolTipText(MessageManager.formatMessage(
            "label.open_url_param", new Object[] { url }));
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        new Thread(new Runnable()
        {

          @Override
          public void run()
          {
            showLink(url);
          }

        }).start();
      }
    });

    linkMenu.add(item);
  }

  /**
   * add a late bound groupURL item to the given linkMenu
   * 
   * @param linkMenu
   * @param label
   *          - menu label string
   * @param urlgenerator
   *          GroupURLLink used to generate URL
   * @param urlstub
   *          Object array returned from the makeUrlStubs function.
   */
  private void addshowLink(JMenu linkMenu, String label,
          final GroupUrlLink urlgenerator, final Object[] urlstub)
  {
    JMenuItem item = new JMenuItem(label);
    item.setToolTipText(MessageManager.formatMessage(
            "label.open_url_seqs_param",
            new Object[] { urlgenerator.getUrl_prefix(),
                urlgenerator.getNumberInvolved(urlstub) }));
    // TODO: put in info about what is being sent.
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        new Thread(new Runnable()
        {

          @Override
          public void run()
          {
            try
            {
              showLink(urlgenerator.constructFrom(urlstub));
            } catch (UrlStringTooLongException e)
            {
            }
          }

        }).start();
      }
    });

    linkMenu.add(item);
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws Exception
   *           DOCUMENT ME!
   */
  private void jbInit() throws Exception
  {
    groupMenu.setText(MessageManager.getString("label.group"));
    groupMenu.setText(MessageManager.getString("label.selection"));
    groupName.setText(MessageManager.getString("label.name"));
    groupName.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        groupName_actionPerformed();
      }
    });
    sequenceMenu.setText(MessageManager.getString("label.sequence"));
    sequenceName.setText(MessageManager
            .getString("label.edit_name_description"));
    sequenceName.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sequenceName_actionPerformed();
      }
    });
    chooseAnnotations.setText(MessageManager
            .getString("action.choose_annotations"));
    chooseAnnotations.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        chooseAnnotations_actionPerformed(e);
      }
    });
    sequenceDetails.setText(MessageManager
            .getString("label.sequence_details"));
    sequenceDetails.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sequenceDetails_actionPerformed();
      }
    });
    sequenceSelDetails.setText(MessageManager
            .getString("label.sequence_details"));
    sequenceSelDetails
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                sequenceSelectionDetails_actionPerformed();
              }
            });
    PIDColour.setFocusPainted(false);
    unGroupMenuItem
            .setText(MessageManager.getString("action.remove_group"));
    unGroupMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        unGroupMenuItem_actionPerformed();
      }
    });
    createGroupMenuItem.setText(MessageManager
            .getString("action.create_group"));
    createGroupMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                createGroupMenuItem_actionPerformed();
              }
            });

    outline.setText(MessageManager.getString("action.border_colour"));
    outline.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        outline_actionPerformed();
      }
    });
    nucleotideMenuItem
            .setText(MessageManager.getString("label.nucleotide"));
    nucleotideMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        nucleotideMenuItem_actionPerformed();
      }
    });
    colourMenu.setText(MessageManager.getString("label.group_colour"));
    showBoxes.setText(MessageManager.getString("action.boxes"));
    showBoxes.setState(true);
    showBoxes.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showBoxes_actionPerformed();
      }
    });
    showText.setText(MessageManager.getString("action.text"));
    showText.setState(true);
    showText.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showText_actionPerformed();
      }
    });
    showColourText.setText(MessageManager.getString("label.colour_text"));
    showColourText.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showColourText_actionPerformed();
      }
    });
    displayNonconserved.setText(MessageManager
            .getString("label.show_non_conversed"));
    displayNonconserved.setState(true);
    displayNonconserved.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showNonconserved_actionPerformed();
      }
    });
    editMenu.setText(MessageManager.getString("action.edit"));
    cut.setText(MessageManager.getString("action.cut"));
    cut.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cut_actionPerformed();
      }
    });
    upperCase.setText(MessageManager.getString("label.to_upper_case"));
    upperCase.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    copy.setText(MessageManager.getString("action.copy"));
    copy.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        copy_actionPerformed();
      }
    });
    lowerCase.setText(MessageManager.getString("label.to_lower_case"));
    lowerCase.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    toggle.setText(MessageManager.getString("label.toggle_case"));
    toggle.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    pdbMenu.setText(MessageManager
            .getString("label.associate_structure_with_sequence"));
    pdbFromFile.setText(MessageManager.getString("label.from_file"));
    pdbFromFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pdbFromFile_actionPerformed();
      }
    });

    enterPDB.setText(MessageManager.getString("label.enter_pdb_id"));
    enterPDB.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        enterPDB_actionPerformed();
      }
    });
    discoverPDB.setText(MessageManager.getString("label.discover_pdb_ids"));
    discoverPDB.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        discoverPDB_actionPerformed();
      }
    });
    outputMenu.setText(MessageManager.getString("label.out_to_textbox")
            + "...");
    seqShowAnnotationsMenu.setText(MessageManager
            .getString("label.show_annotations"));
    seqHideAnnotationsMenu.setText(MessageManager
            .getString("label.hide_annotations"));
    groupShowAnnotationsMenu.setText(MessageManager
            .getString("label.show_annotations"));
    groupHideAnnotationsMenu.setText(MessageManager
            .getString("label.hide_annotations"));
    sequenceFeature.setText(MessageManager
            .getString("label.create_sequence_feature"));
    sequenceFeature.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sequenceFeature_actionPerformed();
      }
    });
    textColour.setText(MessageManager.getString("label.text_colour"));
    textColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        textColour_actionPerformed();
      }
    });
    jMenu1.setText(MessageManager.getString("label.group"));
    pdbStructureDialog.setText(MessageManager
            .getString("label.show_pdbstruct_dialog"));
    pdbStructureDialog.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        SequenceI[] selectedSeqs = new SequenceI[] { sequence };
        if (ap.av.getSelectionGroup() != null)
        {
          selectedSeqs = ap.av.getSequenceSelection();
        }
        new StructureChooser(selectedSeqs, sequence, ap);
      }
    });

    rnaStructureMenu.setText(MessageManager
            .getString("label.view_rna_structure"));

    // colStructureMenu.setText("Colour By Structure");
    editSequence.setText(MessageManager.getString("label.edit_sequence")
            + "...");
    editSequence.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        editSequence_actionPerformed(actionEvent);
      }
    });
    makeReferenceSeq.setText(MessageManager
            .getString("label.mark_as_representative"));
    makeReferenceSeq.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        makeReferenceSeq_actionPerformed(actionEvent);

      }
    });
    hideInsertions.setText(MessageManager
            .getString("label.hide_insertions"));
    hideInsertions.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideInsertions_actionPerformed(e);
      }
    });
    /*
     * annotationMenuItem.setText("By Annotation");
     * annotationMenuItem.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent actionEvent) {
     * annotationMenuItem_actionPerformed(actionEvent); } });
     */
    groupMenu.add(sequenceSelDetails);
    add(groupMenu);
    add(sequenceMenu);
    add(rnaStructureMenu);
    add(pdbStructureDialog);
    if (sequence != null)
    {
      add(hideInsertions);
    }
    // annotations configuration panel suppressed for now
    // groupMenu.add(chooseAnnotations);

    /*
     * Add show/hide annotations to the Sequence menu, and to the Selection menu
     * (if a selection group is in force).
     */
    sequenceMenu.add(seqShowAnnotationsMenu);
    sequenceMenu.add(seqHideAnnotationsMenu);
    sequenceMenu.add(seqAddReferenceAnnotations);
    groupMenu.add(groupShowAnnotationsMenu);
    groupMenu.add(groupHideAnnotationsMenu);
    groupMenu.add(groupAddReferenceAnnotations);
    groupMenu.add(editMenu);
    groupMenu.add(outputMenu);
    groupMenu.add(sequenceFeature);
    groupMenu.add(createGroupMenuItem);
    groupMenu.add(unGroupMenuItem);
    groupMenu.add(jMenu1);
    sequenceMenu.add(sequenceName);
    sequenceMenu.add(sequenceDetails);
    sequenceMenu.add(makeReferenceSeq);
    colourMenu.add(textColour);
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
    if (ap.getAlignment().isNucleotide())
    {
      // JBPNote - commented since the colourscheme isn't functional
      colourMenu.add(purinePyrimidineColour);
    }
    colourMenu.add(userDefinedColour);

    if (jalview.gui.UserDefinedColours.getUserColourSchemes() != null)
    {
      java.util.Enumeration userColours = jalview.gui.UserDefinedColours
              .getUserColourSchemes().keys();

      while (userColours.hasMoreElements())
      {
        JMenuItem item = new JMenuItem(userColours.nextElement().toString());
        item.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent evt)
          {
            userDefinedColour_actionPerformed(evt);
          }
        });
        colourMenu.add(item);
      }
    }

    colourMenu.addSeparator();
    colourMenu.add(abovePIDColour);
    colourMenu.add(conservationMenuItem);
    editMenu.add(copy);
    editMenu.add(cut);
    editMenu.add(editSequence);
    editMenu.add(upperCase);
    editMenu.add(lowerCase);
    editMenu.add(toggle);
    pdbMenu.add(pdbFromFile);
    // JBPNote: These shouldn't be added here - should appear in a generic
    // 'apply web service to this sequence menu'
    // pdbMenu.add(RNAFold);
    // pdbMenu.add(ContraFold);
    pdbMenu.add(enterPDB);
    pdbMenu.add(discoverPDB);
    jMenu1.add(groupName);
    jMenu1.add(colourMenu);
    jMenu1.add(showBoxes);
    jMenu1.add(showText);
    jMenu1.add(showColourText);
    jMenu1.add(outline);
    jMenu1.add(displayNonconserved);
    noColourmenuItem.setText(MessageManager.getString("label.none"));
    noColourmenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        noColourmenuItem_actionPerformed();
      }
    });

    clustalColour.setText(MessageManager
            .getString("label.clustalx_colours"));
    clustalColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        clustalColour_actionPerformed();
      }
    });
    zappoColour.setText(MessageManager.getString("label.zappo"));
    zappoColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        zappoColour_actionPerformed();
      }
    });
    taylorColour.setText(MessageManager.getString("label.taylor"));
    taylorColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        taylorColour_actionPerformed();
      }
    });
    hydrophobicityColour.setText(MessageManager
            .getString("label.hydrophobicity"));
    hydrophobicityColour
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                hydrophobicityColour_actionPerformed();
              }
            });
    helixColour.setText(MessageManager.getString("label.helix_propensity"));
    helixColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        helixColour_actionPerformed();
      }
    });
    strandColour.setText(MessageManager
            .getString("label.strand_propensity"));
    strandColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        strandColour_actionPerformed();
      }
    });
    turnColour.setText(MessageManager.getString("label.turn_propensity"));
    turnColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        turnColour_actionPerformed();
      }
    });
    buriedColour.setText(MessageManager.getString("label.buried_index"));
    buriedColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        buriedColour_actionPerformed();
      }
    });
    abovePIDColour.setText(MessageManager
            .getString("label.above_identity_percentage"));
    abovePIDColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        abovePIDColour_actionPerformed();
      }
    });
    userDefinedColour.setText(MessageManager
            .getString("action.user_defined"));
    userDefinedColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        userDefinedColour_actionPerformed(e);
      }
    });
    PIDColour
            .setText(MessageManager.getString("label.percentage_identity"));
    PIDColour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        PIDColour_actionPerformed();
      }
    });
    BLOSUM62Colour.setText(MessageManager.getString("label.blosum62"));
    BLOSUM62Colour.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        BLOSUM62Colour_actionPerformed();
      }
    });
    purinePyrimidineColour.setText(MessageManager
            .getString("label.purine_pyrimidine"));
    purinePyrimidineColour
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                purinePyrimidineColour_actionPerformed();
              }
            });

    /*
     * covariationColour.addActionListener(new java.awt.event.ActionListener() {
     * public void actionPerformed(ActionEvent e) {
     * covariationColour_actionPerformed(); } });
     */

    conservationMenuItem.setText(MessageManager
            .getString("label.conservation"));
    conservationMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                conservationMenuItem_actionPerformed();
              }
            });
  }

  /**
   * Check for any annotations on the underlying dataset sequences (for the
   * current selection group) which are not 'on the alignment'.If any are found,
   * enable the option to add them to the alignment. The criteria for 'on the
   * alignment' is finding an alignment annotation on the alignment, matched on
   * calcId, label and sequenceRef.
   * 
   * A tooltip is also constructed that displays the source (calcId) and type
   * (label) of the annotations that can be added.
   * 
   * @param menuItem
   * @param forSequences
   */
  protected void configureReferenceAnnotationsMenu(JMenuItem menuItem,
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
       * configure its tooltip and action.
       */
      menuItem.setEnabled(true);
      for (String calcId : tipEntries.keySet())
      {
        tooltip.append("<br/>" + calcId + "/" + tipEntries.get(calcId));
      }
      String tooltipText = JvSwingUtils.wrapTooltip(true,
              tooltip.toString());
      menuItem.setToolTipText(tooltipText);

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

  protected void makeReferenceSeq_actionPerformed(ActionEvent actionEvent)
  {
    if (!ap.av.getAlignment().hasSeqrep())
    {
      // initialise the display flags so the user sees something happen
      ap.av.setDisplayReferenceSeq(true);
      ap.av.setColourByReferenceSeq(true);
      ap.av.getAlignment().setSeqrep(sequence);
    }
    else
    {
      if (ap.av.getAlignment().getSeqrep() == sequence)
      {
        ap.av.getAlignment().setSeqrep(null);
      }
      else
      {
        ap.av.getAlignment().setSeqrep(sequence);
      }
    }
    refresh();
  }

  protected void hideInsertions_actionPerformed(ActionEvent actionEvent)
  {
    if (sequence != null)
    {
      ColumnSelection cs = ap.av.getColumnSelection();
      if (cs == null)
      {
        cs = new ColumnSelection();
      }
      cs.hideInsertionsFor(sequence);
      ap.av.setColumnSelection(cs);
    }
    refresh();
  }

  protected void sequenceSelectionDetails_actionPerformed()
  {
    createSequenceDetailsReport(ap.av.getSequenceSelection());
  }

  protected void sequenceDetails_actionPerformed()
  {
    createSequenceDetailsReport(new SequenceI[] { sequence });
  }

  public void createSequenceDetailsReport(SequenceI[] sequences)
  {
    CutAndPasteHtmlTransfer cap = new CutAndPasteHtmlTransfer();
    StringBuffer contents = new StringBuffer();
    for (SequenceI seq : sequences)
    {
      contents.append("<p><h2>"
              + MessageManager
                      .formatMessage(
                              "label.create_sequence_details_report_annotation_for",
                              new Object[] { seq.getDisplayId(true) })
              + "</h2></p><p>");
      new SequenceAnnotationReport(null)
              .createSequenceAnnotationReport(
                      contents,
                      seq,
                      true,
                      true,
                      false,
                      (ap.getSeqPanel().seqCanvas.fr != null) ? ap
                              .getSeqPanel().seqCanvas.fr.getMinMax()
                              : null);
      contents.append("</p>");
    }
    cap.setText("<html>" + contents.toString() + "</html>");

    Desktop.addInternalFrame(cap, MessageManager.formatMessage(
            "label.sequence_details_for",
            (sequences.length == 1 ? new Object[] { sequences[0]
                    .getDisplayId(true) } : new Object[] { MessageManager
                    .getString("label.selection") })), 500, 400);

  }

  protected void showNonconserved_actionPerformed()
  {
    getGroup().setShowNonconserved(displayNonconserved.isSelected());
    refresh();
  }

  /**
   * call to refresh view after settings change
   */
  void refresh()
  {
    ap.updateAnnotation();
    ap.paintAlignment(true);

    PaintRefresher.Refresh(this, ap.av.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void clustalColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new ClustalxColourScheme(sg, ap.av.getHiddenRepSequences());
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void zappoColour_actionPerformed()
  {
    getGroup().cs = new ZappoColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void taylorColour_actionPerformed()
  {
    getGroup().cs = new TaylorColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void hydrophobicityColour_actionPerformed()
  {
    getGroup().cs = new HydrophobicColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void helixColour_actionPerformed()
  {
    getGroup().cs = new HelixColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void strandColour_actionPerformed()
  {
    getGroup().cs = new StrandColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void turnColour_actionPerformed()
  {
    getGroup().cs = new TurnColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void buriedColour_actionPerformed()
  {
    getGroup().cs = new BuriedColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void nucleotideMenuItem_actionPerformed()
  {
    getGroup().cs = new NucleotideColourScheme();
    refresh();
  }

  protected void purinePyrimidineColour_actionPerformed()
  {
    getGroup().cs = new PurinePyrimidineColourScheme();
    refresh();
  }

  /*
   * protected void covariationColour_actionPerformed() { getGroup().cs = new
   * CovariationColourScheme(sequence.getAnnotation()[0]); refresh(); }
   */
  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void abovePIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (abovePIDColour.isSelected())
    {
      sg.cs.setConsensus(AAFrequency.calculate(
              sg.getSequences(ap.av.getHiddenRepSequences()),
              sg.getStartRes(), sg.getEndRes() + 1));

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

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void userDefinedColour_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = getGroup();

    if (e.getSource().equals(userDefinedColour))
    {
      new UserDefinedColours(ap, sg);
    }
    else
    {
      UserColourScheme udc = (UserColourScheme) UserDefinedColours
              .getUserColourSchemes().get(e.getActionCommand());

      sg.cs = udc;
    }
    refresh();
  }

  /**
   * Open a panel where the user can choose which types of sequence annotation
   * to show or hide.
   * 
   * @param e
   */
  protected void chooseAnnotations_actionPerformed(ActionEvent e)
  {
    // todo correct way to guard against opening a duplicate panel?
    new AnnotationChooser(ap);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void PIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new PIDColourScheme();
    sg.cs.setConsensus(AAFrequency.calculate(
            sg.getSequences(ap.av.getHiddenRepSequences()),
            sg.getStartRes(), sg.getEndRes() + 1));
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void BLOSUM62Colour_actionPerformed()
  {
    SequenceGroup sg = getGroup();

    sg.cs = new Blosum62ColourScheme();

    sg.cs.setConsensus(AAFrequency.calculate(
            sg.getSequences(ap.av.getHiddenRepSequences()),
            sg.getStartRes(), sg.getEndRes() + 1));

    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void noColourmenuItem_actionPerformed()
  {
    getGroup().cs = null;
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void conservationMenuItem_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (conservationMenuItem.isSelected())
    {
      // JBPNote: Conservation name shouldn't be i18n translated
      Conservation c = new Conservation("Group",
              ResidueProperties.propHash, 3, sg.getSequences(ap.av
                      .getHiddenRepSequences()), sg.getStartRes(),
              sg.getEndRes() + 1);

      c.calculate();
      c.verdict(false, ap.av.getConsPercGaps());

      sg.cs.setConservation(c);

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

  public void annotationMenuItem_actionPerformed(ActionEvent actionEvent)
  {
    SequenceGroup sg = getGroup();
    if (sg == null)
    {
      return;
    }

    AnnotationColourGradient acg = new AnnotationColourGradient(
            sequence.getAnnotation()[0], null,
            AnnotationColourGradient.NO_THRESHOLD);

    acg.setPredefinedColours(true);
    sg.cs = acg;

    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void groupName_actionPerformed()
  {

    SequenceGroup sg = getGroup();
    EditNameDialog dialog = new EditNameDialog(sg.getName(),
            sg.getDescription(), "       "
                    + MessageManager.getString("label.group_name") + " ",
            MessageManager.getString("label.group_description") + " ",
            MessageManager.getString("label.edit_group_name_description"),
            ap.alignFrame);

    if (!dialog.accept)
    {
      return;
    }

    sg.setName(dialog.getName());
    sg.setDescription(dialog.getDescription());
    refresh();
  }

  /**
   * Get selection group - adding it to the alignment if necessary.
   * 
   * @return sequence group to operate on
   */
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

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  void sequenceName_actionPerformed()
  {
    EditNameDialog dialog = new EditNameDialog(sequence.getName(),
            sequence.getDescription(),
            "       " + MessageManager.getString("label.sequence_name")
                    + " ",
            MessageManager.getString("label.sequence_description") + " ",
            MessageManager
                    .getString("label.edit_sequence_name_description"),
            ap.alignFrame);

    if (!dialog.accept)
    {
      return;
    }

    if (dialog.getName() != null)
    {
      if (dialog.getName().indexOf(" ") > -1)
      {
        JOptionPane
                .showMessageDialog(
                        ap,
                        MessageManager
                                .getString("label.spaces_converted_to_backslashes"),
                        MessageManager
                                .getString("label.no_spaces_allowed_sequence_name"),
                        JOptionPane.WARNING_MESSAGE);
      }

      sequence.setName(dialog.getName().replace(' ', '_'));
      ap.paintAlignment(false);
    }

    sequence.setDescription(dialog.getDescription());

    ap.av.firePropertyChange("alignment", null, ap.av.getAlignment()
            .getSequences());

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  void unGroupMenuItem_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    ap.av.getAlignment().deleteGroup(sg);
    ap.av.setSelectionGroup(null);
    refresh();
  }

  void createGroupMenuItem_actionPerformed()
  {
    getGroup(); // implicitly creates group - note - should apply defaults / use
                // standard alignment window logic for this
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void outline_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_outline_colour"),
            Color.BLUE);

    if (col != null)
    {
      sg.setOutlineColour(col);
    }

    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void showBoxes_actionPerformed()
  {
    getGroup().setDisplayBoxes(showBoxes.isSelected());
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void showText_actionPerformed()
  {
    getGroup().setDisplayText(showText.isSelected());
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void showColourText_actionPerformed()
  {
    getGroup().setColourText(showColourText.isSelected());
    refresh();
  }

  public void showLink(String url)
  {
    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    } catch (Exception ex)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.getString("label.web_browser_not_found_unix"),
              MessageManager.getString("label.web_browser_not_found"),
              JOptionPane.WARNING_MESSAGE);

      ex.printStackTrace();
    }
  }

  void hideSequences(boolean representGroup)
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null || sg.getSize() < 1)
    {
      ap.av.hideSequence(new SequenceI[] { sequence });
      return;
    }

    ap.av.setSelectionGroup(null);

    if (representGroup)
    {
      ap.av.hideRepSequences(sequence, sg);

      return;
    }

    int gsize = sg.getSize();
    SequenceI[] hseqs = sg.getSequences().toArray(new SequenceI[gsize]);

    ap.av.hideSequence(hseqs);
    // refresh(); TODO: ? needed ?
    ap.av.sendSelection();
  }

  public void copy_actionPerformed()
  {
    ap.alignFrame.copy_actionPerformed(null);
  }

  public void cut_actionPerformed()
  {
    ap.alignFrame.cut_actionPerformed(null);
  }

  void changeCase(ActionEvent e)
  {
    Object source = e.getSource();
    SequenceGroup sg = ap.av.getSelectionGroup();

    if (sg != null)
    {
      List<int[]> startEnd = ap.av.getVisibleRegionBoundaries(
              sg.getStartRes(), sg.getEndRes() + 1);

      String description;
      int caseChange;

      if (source == toggle)
      {
        description = MessageManager.getString("label.toggle_case");
        caseChange = ChangeCaseCommand.TOGGLE_CASE;
      }
      else if (source == upperCase)
      {
        description = MessageManager.getString("label.to_upper_case");
        caseChange = ChangeCaseCommand.TO_UPPER;
      }
      else
      {
        description = MessageManager.getString("label.to_lower_case");
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

  public void outputText_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setForInput(null);
    Desktop.addInternalFrame(cap, MessageManager.formatMessage(
            "label.alignment_output_command",
            new Object[] { e.getActionCommand() }), 600, 500);

    String[] omitHidden = null;

    System.out.println("PROMPT USER HERE"); // TODO: decide if a prompt happens
    // or we simply trust the user wants
    // wysiwig behaviour

    cap.setText(new FormatAdapter(ap).formatSequences(e.getActionCommand(),
            ap, true));
  }

  public void pdbFromFile_actionPerformed()
  {
    jalview.io.JalviewFileChooser chooser = new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(MessageManager.formatMessage(
            "label.select_pdb_file_for",
            new Object[] { sequence.getDisplayId(false) }));
    chooser.setToolTipText(MessageManager.formatMessage(
            "label.load_pdb_file_associate_with_sequence",
            new Object[] { sequence.getDisplayId(false) }));

    int value = chooser.showOpenDialog(null);

    if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", choice);
      new AssociatePdbFileWithSeq().associatePdbWithSeq(choice,
              jalview.io.AppletFormatAdapter.FILE, sequence, true,
              Desktop.instance);
    }

  }

  public void enterPDB_actionPerformed()
  {
    String id = JOptionPane.showInternalInputDialog(Desktop.desktop,
            MessageManager.getString("label.enter_pdb_id"),
            MessageManager.getString("label.enter_pdb_id"),
            JOptionPane.QUESTION_MESSAGE);

    if (id != null && id.length() > 0)
    {
      PDBEntry entry = new PDBEntry();
      entry.setId(id.toUpperCase());
      sequence.getDatasetSequence().addPDBId(entry);
    }
  }

  public void discoverPDB_actionPerformed()
  {

    final SequenceI[] sequences = ((ap.av.getSelectionGroup() == null) ? new SequenceI[]
    { sequence }
            : ap.av.getSequenceSelection());
    Thread discpdb = new Thread(new Runnable()
    {
      @Override
      public void run()
      {

        new jalview.ws.DBRefFetcher(sequences, ap.alignFrame)
                .fetchDBRefs(false);
      }

    });
    discpdb.start();
  }

  public void sequenceFeature_actionPerformed()
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
        seqs[rsize] = sg.getSequenceAt(i).getDatasetSequence();
        features[rsize] = new SequenceFeature(null, null, null, start, end,
                "Jalview");
        rsize++;
      }
    }
    rseqs = new SequenceI[rsize];
    tfeatures = new SequenceFeature[rsize];
    System.arraycopy(seqs, 0, rseqs, 0, rsize);
    System.arraycopy(features, 0, tfeatures, 0, rsize);
    features = tfeatures;
    seqs = rseqs;
    if (ap.getSeqPanel().seqCanvas.getFeatureRenderer().amendFeatures(seqs,
            features, true, ap))
    {
      ap.alignFrame.setShowSeqFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  public void textColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg != null)
    {
      new TextColourChooser().chooseColour(ap, sg);
    }
  }

  public void colourByStructure(String pdbid)
  {
    Annotation[] anots = ap.av.getStructureSelectionManager()
            .colourSequenceFromStructure(sequence, pdbid);

    AlignmentAnnotation an = new AlignmentAnnotation("Structure",
            "Coloured by " + pdbid, anots);

    ap.av.getAlignment().addAnnotation(an);
    an.createSequenceMapping(sequence, 0, true);
    // an.adjustForAlignment();
    ap.av.getAlignment().setAnnotationIndex(an, 0);

    ap.adjustAnnotationHeight();

    sequence.addAlignmentAnnotation(an);

  }

  public void editSequence_actionPerformed(ActionEvent actionEvent)
  {
    SequenceGroup sg = ap.av.getSelectionGroup();

    if (sg != null)
    {
      if (sequence == null)
      {
        sequence = sg.getSequenceAt(0);
      }

      EditNameDialog dialog = new EditNameDialog(
              sequence.getSequenceAsString(sg.getStartRes(),
                      sg.getEndRes() + 1), null,
              MessageManager.getString("label.edit_sequence"), null,
              MessageManager.getString("label.edit_sequence"),
              ap.alignFrame);

      if (dialog.accept)
      {
        EditCommand editCommand = new EditCommand(
                MessageManager.getString("label.edit_sequences"),
                Action.REPLACE, dialog.getName().replace(' ',
                        ap.av.getGapCharacter()),
                sg.getSequencesAsArray(ap.av.getHiddenRepSequences()),
                sg.getStartRes(), sg.getEndRes() + 1, ap.av.getAlignment());

        ap.alignFrame.addHistoryItem(editCommand);

        ap.av.firePropertyChange("alignment", null, ap.av.getAlignment()
                .getSequences());
      }
    }
  }

}
