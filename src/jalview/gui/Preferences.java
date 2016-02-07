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

import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.bin.Cache;
import jalview.gui.Help.HelpId;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GPreferences;
import jalview.jbgui.GSequenceLink;
import jalview.schemes.ColourSchemeProperty;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.help.HelpSetException;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ext.edu.ucsf.rbvi.strucviz2.StructureManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class Preferences extends GPreferences
{
  public static final String ENABLE_SPLIT_FRAME = "ENABLE_SPLIT_FRAME";

  public static final String SCALE_PROTEIN_TO_CDNA = "SCALE_PROTEIN_TO_CDNA";

  public static final String DEFAULT_COLOUR = "DEFAULT_COLOUR";

  public static final String DEFAULT_COLOUR_PROT = "DEFAULT_COLOUR_PROT";

  public static final String DEFAULT_COLOUR_NUC = "DEFAULT_COLOUR_NUC";

  public static final String ADD_TEMPFACT_ANN = "ADD_TEMPFACT_ANN";

  public static final String ADD_SS_ANN = "ADD_SS_ANN";

  public static final String USE_RNAVIEW = "USE_RNAVIEW";

  public static final String STRUCT_FROM_PDB = "STRUCT_FROM_PDB";

  public static final String STRUCTURE_DISPLAY = "STRUCTURE_DISPLAY";

  public static final String CHIMERA_PATH = "CHIMERA_PATH";

  public static final String SORT_ANNOTATIONS = "SORT_ANNOTATIONS";

  public static final String SHOW_AUTOCALC_ABOVE = "SHOW_AUTOCALC_ABOVE";

  private static final int MIN_FONT_SIZE = 1;

  private static final int MAX_FONT_SIZE = 30;

  /**
   * Holds name and link separated with | character. Sequence ID must be
   * $SEQUENCE_ID$ or $SEQUENCE_ID=/.possible | chars ./=$
   */
  public static Vector sequenceURLLinks;

  /**
   * Holds name and link separated with | character. Sequence IDS and Sequences
   * must be $SEQUENCEIDS$ or $SEQUENCEIDS=/.possible | chars ./=$ and
   * $SEQUENCES$ or $SEQUENCES=/.possible | chars ./=$ and separation character
   * for first and second token specified after a pipe character at end |,|.
   * (TODO: proper escape for using | to separate ids or sequences
   */

  public static Vector groupURLLinks;
  static
  {
    String string = Cache
            .getDefault(
                    "SEQUENCE_LINKS",
                    "EMBL-EBI Search|http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$");
    sequenceURLLinks = new Vector();

    try
    {
      StringTokenizer st = new StringTokenizer(string, "|");
      while (st.hasMoreElements())
      {
        String name = st.nextToken();
        String url = st.nextToken();
        // check for '|' within a regex
        int rxstart = url.indexOf("$SEQUENCE_ID$");
        while (rxstart == -1 && url.indexOf("/=$") == -1)
        {
          url = url + "|" + st.nextToken();
        }
        sequenceURLLinks.addElement(name + "|" + url);
      }
    } catch (Exception ex)
    {
      System.out.println(ex + "\nError parsing sequence links");
    }
    {
      // upgrade old SRS link
      int srsPos = sequenceURLLinks
              .indexOf("SRS|http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-newId+(([uniprot-all:$SEQUENCE_ID$]))+-view+SwissEntry");
      if (srsPos > -1)
      {
        sequenceURLLinks
                .setElementAt(
                        "EMBL-EBI Search|http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$",
                        srsPos);
      }
    }

    /**
     * TODO: reformulate groupURL encoding so two or more can be stored in the
     * .properties file as '|' separated strings
     */

    groupURLLinks = new Vector();
  }

  Vector nameLinks, urlLinks;

  JInternalFrame frame;

  DasSourceBrowser dasSource;

  private WsPreferences wsPrefs;

  /**
   * Creates a new Preferences object.
   */
  public Preferences()
  {
    super();
    frame = new JInternalFrame();
    frame.setContentPane(this);
    dasSource = new DasSourceBrowser();
    dasTab.add(dasSource, BorderLayout.CENTER);
    wsPrefs = new WsPreferences();
    wsTab.add(wsPrefs, BorderLayout.CENTER);
    int width = 500, height = 450;
    if (new jalview.util.Platform().isAMac())
    {
      width = 570;
      height = 480;
    }

    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.preferences"), width, height);
    frame.setMinimumSize(new Dimension(width, height));

    /*
     * Set Visual tab defaults
     */
    seqLimit.setSelected(Cache.getDefault("SHOW_JVSUFFIX", true));
    rightAlign.setSelected(Cache.getDefault("RIGHT_ALIGN_IDS", false));
    fullScreen.setSelected(Cache.getDefault("SHOW_FULLSCREEN", false));
    annotations.setSelected(Cache.getDefault("SHOW_ANNOTATIONS", true));

    conservation.setSelected(Cache.getDefault("SHOW_CONSERVATION", true));
    quality.setSelected(Cache.getDefault("SHOW_QUALITY", true));
    identity.setSelected(Cache.getDefault("SHOW_IDENTITY", true));
    openoverv.setSelected(Cache.getDefault("SHOW_OVERVIEW", false));
    showUnconserved
            .setSelected(Cache.getDefault("SHOW_UNCONSERVED", false));
    showGroupConsensus.setSelected(Cache.getDefault("SHOW_GROUP_CONSENSUS",
            false));
    showGroupConservation.setSelected(Cache.getDefault(
            "SHOW_GROUP_CONSERVATION", false));
    showConsensHistogram.setSelected(Cache.getDefault(
            "SHOW_CONSENSUS_HISTOGRAM", true));
    showConsensLogo.setSelected(Cache.getDefault("SHOW_CONSENSUS_LOGO",
            false));
    showNpTooltip.setSelected(Cache
            .getDefault("SHOW_NPFEATS_TOOLTIP", true));
    showDbRefTooltip.setSelected(Cache.getDefault("SHOW_DBREFS_TOOLTIP",
            true));

    String[] fonts = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (int i = 0; i < fonts.length; i++)
    {
      fontNameCB.addItem(fonts[i]);
    }

    for (int i = MIN_FONT_SIZE; i <= MAX_FONT_SIZE; i++)
    {
      fontSizeCB.addItem(i + "");
    }

    fontStyleCB.addItem("plain");
    fontStyleCB.addItem("bold");
    fontStyleCB.addItem("italic");

    fontNameCB.setSelectedItem(Cache.getDefault("FONT_NAME", "SansSerif"));
    fontSizeCB.setSelectedItem(Cache.getDefault("FONT_SIZE", "10"));
    fontStyleCB.setSelectedItem(Cache.getDefault("FONT_STYLE", Font.PLAIN
            + ""));

    smoothFont.setSelected(Cache.getDefault("ANTI_ALIAS", false));
    scaleProteinToCdna.setSelected(Cache.getDefault(SCALE_PROTEIN_TO_CDNA,
            false));

    idItalics.setSelected(Cache.getDefault("ID_ITALICS", true));

    wrap.setSelected(Cache.getDefault("WRAP_ALIGNMENT", false));

    gapSymbolCB.addItem("-");
    gapSymbolCB.addItem(".");

    gapSymbolCB.setSelectedItem(Cache.getDefault("GAP_SYMBOL", "-"));

    sortby.addItem("No sort");
    sortby.addItem("Id");
    sortby.addItem("Pairwise Identity");
    sortby.setSelectedItem(Cache.getDefault("SORT_ALIGNMENT", "No sort"));

    sortAnnBy.addItem(SequenceAnnotationOrder.NONE.toString());
    sortAnnBy
            .addItem(SequenceAnnotationOrder.SEQUENCE_AND_LABEL.toString());
    sortAnnBy
            .addItem(SequenceAnnotationOrder.LABEL_AND_SEQUENCE.toString());
    SequenceAnnotationOrder savedSort = SequenceAnnotationOrder
            .valueOf(Cache.getDefault(SORT_ANNOTATIONS,
                    SequenceAnnotationOrder.NONE.name()));
    sortAnnBy.setSelectedItem(savedSort.toString());

    sortAutocalc.addItem("Autocalculated first");
    sortAutocalc.addItem("Autocalculated last");
    final boolean showAbove = Cache.getDefault(SHOW_AUTOCALC_ABOVE, true);
    sortAutocalc.setSelectedItem(showAbove ? sortAutocalc.getItemAt(0)
            : sortAutocalc.getItemAt(1));
    startupCheckbox
            .setSelected(Cache.getDefault("SHOW_STARTUP_FILE", true));
    startupFileTextfield.setText(Cache.getDefault("STARTUP_FILE",
            Cache.getDefault("www.jalview.org", "http://www.jalview.org")
                    + "/examples/exampleFile_2_3.jar"));

    /*
     * Set Colours tab defaults
     */
    for (int i = ColourSchemeProperty.FIRST_COLOUR; i <= ColourSchemeProperty.LAST_COLOUR; i++)
    {
      protColour.addItem(ColourSchemeProperty.getColourName(i));
      nucColour.addItem(ColourSchemeProperty.getColourName(i));
    }
    String oldProp = Cache.getDefault(DEFAULT_COLOUR, "None");
    String newProp = Cache.getDefault(DEFAULT_COLOUR_PROT, null);
    protColour.setSelectedItem(newProp != null ? newProp : oldProp);
    newProp = Cache.getDefault(DEFAULT_COLOUR_NUC, null);
    nucColour.setSelectedItem(newProp != null ? newProp : oldProp);
    minColour.setBackground(Cache.getDefaultColour("ANNOTATIONCOLOUR_MIN",
            Color.orange));
    maxColour.setBackground(Cache.getDefaultColour("ANNOTATIONCOLOUR_MAX",
            Color.red));

    /*
     * Set Structure tab defaults.
     */
    final boolean structSelected = Cache.getDefault(STRUCT_FROM_PDB, false);
    structFromPdb.setSelected(structSelected);
    useRnaView.setSelected(Cache.getDefault(USE_RNAVIEW, false));
    useRnaView.setEnabled(structSelected);
    addSecondaryStructure.setSelected(Cache.getDefault(ADD_SS_ANN, false));
    addSecondaryStructure.setEnabled(structSelected);
    addTempFactor.setSelected(Cache.getDefault(ADD_TEMPFACT_ANN, false));
    addTempFactor.setEnabled(structSelected);
    structViewer.setSelectedItem(Cache.getDefault(STRUCTURE_DISPLAY,
            ViewerType.JMOL.name()));
    chimeraPath.setText(Cache.getDefault(CHIMERA_PATH, ""));
    chimeraPath.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        validateChimeraPath();
      }
    });

    /*
     * Set Connections tab defaults
     */
    nameLinks = new Vector();
    urlLinks = new Vector();
    for (int i = 0; i < sequenceURLLinks.size(); i++)
    {
      String link = sequenceURLLinks.elementAt(i).toString();
      nameLinks.addElement(link.substring(0, link.indexOf("|")));
      urlLinks.addElement(link.substring(link.indexOf("|") + 1));
    }

    updateLinkData();

    useProxy.setSelected(Cache.getDefault("USE_PROXY", false));
    proxyServerTB.setEnabled(useProxy.isSelected());
    proxyPortTB.setEnabled(useProxy.isSelected());
    proxyServerTB.setText(Cache.getDefault("PROXY_SERVER", ""));
    proxyPortTB.setText(Cache.getDefault("PROXY_PORT", ""));

    defaultBrowser.setText(Cache.getDefault("DEFAULT_BROWSER", ""));

    usagestats.setSelected(Cache.getDefault("USAGESTATS", false));
    // note antisense here: default is true
    questionnaire
            .setSelected(Cache.getProperty("NOQUESTIONNAIRES") == null);
    versioncheck.setSelected(Cache.getDefault("VERSION_CHECK", true));

    /*
     * Set Output tab defaults
     */
    epsRendering
            .addItem(MessageManager.getString("label.prompt_each_time"));
    epsRendering.addItem(MessageManager.getString("label.lineart"));
    epsRendering.addItem(MessageManager.getString("action.text"));
    epsRendering.setSelectedItem(Cache.getDefault("EPS_RENDERING",
            "Prompt each time"));
    autoIdWidth.setSelected(Cache.getDefault("FIGURE_AUTOIDWIDTH", false));
    userIdWidth.setEnabled(!autoIdWidth.isSelected());
    userIdWidthlabel.setEnabled(!autoIdWidth.isSelected());
    Integer wi = Cache.getIntegerProperty("FIGURE_USERIDWIDTH");
    userIdWidth.setText(wi == null ? "" : wi.toString());
    blcjv.setSelected(Cache.getDefault("BLC_JVSUFFIX", true));
    clustaljv.setSelected(Cache.getDefault("CLUSTAL_JVSUFFIX", true));
    fastajv.setSelected(Cache.getDefault("FASTA_JVSUFFIX", true));
    msfjv.setSelected(Cache.getDefault("MSF_JVSUFFIX", true));
    pfamjv.setSelected(Cache.getDefault("PFAM_JVSUFFIX", true));
    pileupjv.setSelected(Cache.getDefault("PILEUP_JVSUFFIX", true));
    pirjv.setSelected(Cache.getDefault("PIR_JVSUFFIX", true));
    modellerOutput.setSelected(Cache.getDefault("PIR_MODELLER", false));
    embbedBioJSON.setSelected(Cache.getDefault("EXPORT_EMBBED_BIOJSON",
            true));

    /*
     * Set Editing tab defaults
     */
    autoCalculateConsCheck.setSelected(Cache.getDefault(
            "AUTO_CALC_CONSENSUS", true));
    padGaps.setSelected(Cache.getDefault("PAD_GAPS", false));
    sortByTree.setSelected(Cache.getDefault("SORT_BY_TREE", false));

    annotations_actionPerformed(null); // update the display of the annotation
                                       // settings
  }

  /**
   * Save user selections on the Preferences tabs to the Cache and write out to
   * file.
   * 
   * @param e
   */
  public void ok_actionPerformed(ActionEvent e)
  {
    if (!validateSettings())
    {
      return;
    }

    /*
     * Save Visual settings
     */
    Cache.applicationProperties.setProperty("SHOW_JVSUFFIX",
            Boolean.toString(seqLimit.isSelected()));
    Cache.applicationProperties.setProperty("RIGHT_ALIGN_IDS",
            Boolean.toString(rightAlign.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_FULLSCREEN",
            Boolean.toString(fullScreen.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_OVERVIEW",
            Boolean.toString(openoverv.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS",
            Boolean.toString(annotations.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION",
            Boolean.toString(conservation.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_QUALITY",
            Boolean.toString(quality.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.toString(identity.isSelected()));

    Cache.applicationProperties.setProperty("GAP_SYMBOL", gapSymbolCB
            .getSelectedItem().toString());

    Cache.applicationProperties.setProperty("FONT_NAME", fontNameCB
            .getSelectedItem().toString());
    Cache.applicationProperties.setProperty("FONT_STYLE", fontStyleCB
            .getSelectedItem().toString());
    Cache.applicationProperties.setProperty("FONT_SIZE", fontSizeCB
            .getSelectedItem().toString());

    Cache.applicationProperties.setProperty("ID_ITALICS",
            Boolean.toString(idItalics.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_UNCONSERVED",
            Boolean.toString(showUnconserved.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_GROUP_CONSENSUS",
            Boolean.toString(showGroupConsensus.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_GROUP_CONSERVATION",
            Boolean.toString(showGroupConservation.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSENSUS_HISTOGRAM",
            Boolean.toString(showConsensHistogram.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSENSUS_LOGO",
            Boolean.toString(showConsensLogo.isSelected()));
    Cache.applicationProperties.setProperty("ANTI_ALIAS",
            Boolean.toString(smoothFont.isSelected()));
    Cache.applicationProperties.setProperty(SCALE_PROTEIN_TO_CDNA,
            Boolean.toString(scaleProteinToCdna.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_NPFEATS_TOOLTIP",
            Boolean.toString(showNpTooltip.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_DBREFS_TOOLTIP",
            Boolean.toString(showDbRefTooltip.isSelected()));

    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT",
            Boolean.toString(wrap.isSelected()));

    Cache.applicationProperties.setProperty("STARTUP_FILE",
            startupFileTextfield.getText());
    Cache.applicationProperties.setProperty("SHOW_STARTUP_FILE",
            Boolean.toString(startupCheckbox.isSelected()));

    Cache.applicationProperties.setProperty("SORT_ALIGNMENT", sortby
            .getSelectedItem().toString());

    // convert description of sort order to enum name for save
    SequenceAnnotationOrder annSortOrder = SequenceAnnotationOrder
            .forDescription(sortAnnBy.getSelectedItem().toString());
    if (annSortOrder != null)
    {
      Cache.applicationProperties.setProperty(SORT_ANNOTATIONS,
              annSortOrder.name());
    }

    final boolean showAutocalcFirst = sortAutocalc.getSelectedIndex() == 0;
    Cache.applicationProperties.setProperty(SHOW_AUTOCALC_ABOVE, Boolean
            .valueOf(showAutocalcFirst).toString());

    /*
     * Save Colours settings
     */
    Cache.applicationProperties.setProperty(DEFAULT_COLOUR_PROT, protColour
            .getSelectedItem().toString());
    Cache.applicationProperties.setProperty(DEFAULT_COLOUR_NUC, nucColour
            .getSelectedItem().toString());
    Cache.setColourProperty("ANNOTATIONCOLOUR_MIN",
            minColour.getBackground());
    Cache.setColourProperty("ANNOTATIONCOLOUR_MAX",
            maxColour.getBackground());

    /*
     * Save Structure settings
     */
    Cache.applicationProperties.setProperty(ADD_TEMPFACT_ANN,
            Boolean.toString(addTempFactor.isSelected()));
    Cache.applicationProperties.setProperty(ADD_SS_ANN,
            Boolean.toString(addSecondaryStructure.isSelected()));
    Cache.applicationProperties.setProperty(USE_RNAVIEW,
            Boolean.toString(useRnaView.isSelected()));
    Cache.applicationProperties.setProperty(STRUCT_FROM_PDB,
            Boolean.toString(structFromPdb.isSelected()));
    Cache.applicationProperties.setProperty(STRUCTURE_DISPLAY, structViewer
            .getSelectedItem().toString());
    Cache.setOrRemove(CHIMERA_PATH, chimeraPath.getText());

    /*
     * Save Output settings
     */
    if (epsRendering.getSelectedItem().equals("Prompt each time"))
    {
      Cache.applicationProperties.remove("EPS_RENDERING");
    }
    else
    {
      Cache.applicationProperties.setProperty("EPS_RENDERING", epsRendering
              .getSelectedItem().toString());
    }

    /*
     * Save Connections settings
     */
    Cache.setOrRemove("DEFAULT_BROWSER", defaultBrowser.getText());

    jalview.util.BrowserLauncher.resetBrowser();

    if (nameLinks.size() > 0)
    {
      StringBuffer links = new StringBuffer();
      sequenceURLLinks = new Vector();
      for (int i = 0; i < nameLinks.size(); i++)
      {
        sequenceURLLinks.addElement(nameLinks.elementAt(i) + "|"
                + urlLinks.elementAt(i));
        links.append(sequenceURLLinks.elementAt(i).toString());
        links.append("|");
      }
      // remove last "|"
      links.setLength(links.length() - 1);
      Cache.applicationProperties.setProperty("SEQUENCE_LINKS",
              links.toString());
    }
    else
    {
      Cache.applicationProperties.remove("SEQUENCE_LINKS");
    }

    Cache.applicationProperties.setProperty("USE_PROXY",
            Boolean.toString(useProxy.isSelected()));

    Cache.setOrRemove("PROXY_SERVER", proxyServerTB.getText());

    Cache.setOrRemove("PROXY_PORT", proxyPortTB.getText());

    if (useProxy.isSelected())
    {
      System.setProperty("http.proxyHost", proxyServerTB.getText());
      System.setProperty("http.proxyPort", proxyPortTB.getText());
    }
    else
    {
      System.setProperty("http.proxyHost", "");
      System.setProperty("http.proxyPort", "");
    }
    Cache.setProperty("VERSION_CHECK",
            Boolean.toString(versioncheck.isSelected()));
    if (Cache.getProperty("USAGESTATS") != null || usagestats.isSelected())
    {
      // default is false - we only set this if the user has actively agreed
      Cache.setProperty("USAGESTATS",
              Boolean.toString(usagestats.isSelected()));
    }
    if (!questionnaire.isSelected())
    {
      Cache.setProperty("NOQUESTIONNAIRES", "true");
    }
    else
    {
      // special - made easy to edit a property file to disable questionnaires
      // by just adding the given line
      Cache.removeProperty("NOQUESTIONNAIRES");
    }

    /*
     * Save Output settings
     */
    Cache.applicationProperties.setProperty("BLC_JVSUFFIX",
            Boolean.toString(blcjv.isSelected()));
    Cache.applicationProperties.setProperty("CLUSTAL_JVSUFFIX",
            Boolean.toString(clustaljv.isSelected()));
    Cache.applicationProperties.setProperty("FASTA_JVSUFFIX",
            Boolean.toString(fastajv.isSelected()));
    Cache.applicationProperties.setProperty("MSF_JVSUFFIX",
            Boolean.toString(msfjv.isSelected()));
    Cache.applicationProperties.setProperty("PFAM_JVSUFFIX",
            Boolean.toString(pfamjv.isSelected()));
    Cache.applicationProperties.setProperty("PILEUP_JVSUFFIX",
            Boolean.toString(pileupjv.isSelected()));
    Cache.applicationProperties.setProperty("PIR_JVSUFFIX",
            Boolean.toString(pirjv.isSelected()));
    Cache.applicationProperties.setProperty("PIR_MODELLER",
            Boolean.toString(modellerOutput.isSelected()));
    Cache.applicationProperties.setProperty("EXPORT_EMBBED_BIOJSON",
            Boolean.toString(embbedBioJSON.isSelected()));
    jalview.io.PIRFile.useModellerOutput = modellerOutput.isSelected();

    Cache.applicationProperties.setProperty("FIGURE_AUTOIDWIDTH",
            Boolean.toString(autoIdWidth.isSelected()));
    userIdWidth_actionPerformed();
    Cache.applicationProperties.setProperty("FIGURE_USERIDWIDTH",
            userIdWidth.getText());

    /*
     * Save Editing settings
     */
    Cache.applicationProperties.setProperty("AUTO_CALC_CONSENSUS",
            Boolean.toString(autoCalculateConsCheck.isSelected()));
    Cache.applicationProperties.setProperty("SORT_BY_TREE",
            Boolean.toString(sortByTree.isSelected()));
    Cache.applicationProperties.setProperty("PAD_GAPS",
            Boolean.toString(padGaps.isSelected()));

    dasSource.saveProperties(Cache.applicationProperties);
    wsPrefs.updateAndRefreshWsMenuConfig(false);
    Cache.saveProperties();
    Desktop.instance.doConfigureStructurePrefs();
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  /**
   * Do any necessary validation before saving settings. Return focus to the
   * first tab which fails validation.
   * 
   * @return
   */
  private boolean validateSettings()
  {
    if (!validateStructure())
    {
      structureTab.requestFocusInWindow();
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateStructure()
  {
    return validateChimeraPath();

  }

  /**
   * DOCUMENT ME!
   */
  public void startupFileTextfield_mouseClicked()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"), new String[] {
                "fa, fasta, fastq", "aln", "pfam", "msf", "pir", "blc",
                "jar" }, new String[] { "Fasta", "Clustal", "PFAM", "MSF",
                "PIR", "BLC", "Jalview" },
            jalview.bin.Cache.getProperty("DEFAULT_FILE_FORMAT"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.select_startup_file"));

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      jalview.bin.Cache.applicationProperties.setProperty(
              "DEFAULT_FILE_FORMAT", chooser.getSelectedFormat());
      startupFileTextfield.setText(chooser.getSelectedFile()
              .getAbsolutePath());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      wsPrefs.updateWsMenuConfig(true);
      wsPrefs.refreshWs_actionPerformed(e);
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void annotations_actionPerformed(ActionEvent e)
  {
    conservation.setEnabled(annotations.isSelected());
    quality.setEnabled(annotations.isSelected());
    identity.setEnabled(annotations.isSelected());
    showGroupConsensus.setEnabled(annotations.isSelected());
    showGroupConservation.setEnabled(annotations.isSelected());
    showConsensHistogram.setEnabled(annotations.isSelected()
            && (identity.isSelected() || showGroupConsensus.isSelected()));
    showConsensLogo.setEnabled(annotations.isSelected()
            && (identity.isSelected() || showGroupConsensus.isSelected()));
  }

  public void newLink_actionPerformed(ActionEvent e)
  {

    GSequenceLink link = new GSequenceLink();
    boolean valid = false;
    while (!valid)
    {
      if (JOptionPane.showInternalConfirmDialog(Desktop.desktop, link,
              MessageManager.getString("label.new_sequence_url_link"),
              JOptionPane.OK_CANCEL_OPTION, -1, null) == JOptionPane.OK_OPTION)
      {
        if (link.checkValid())
        {
          nameLinks.addElement(link.getName());
          urlLinks.addElement(link.getURL());
          updateLinkData();
          valid = true;
        }
      }
      else
      {
        break;
      }
    }
  }

  public void editLink_actionPerformed(ActionEvent e)
  {
    GSequenceLink link = new GSequenceLink();

    int index = linkNameList.getSelectedIndex();
    if (index == -1)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.getString("label.no_link_selected"),
              MessageManager.getString("label.no_link_selected"),
              JOptionPane.WARNING_MESSAGE);
      return;
    }

    link.setName(nameLinks.elementAt(index).toString());
    link.setURL(urlLinks.elementAt(index).toString());

    boolean valid = false;
    while (!valid)
    {

      if (JOptionPane.showInternalConfirmDialog(Desktop.desktop, link,
              MessageManager.getString("label.new_sequence_url_link"),
              JOptionPane.OK_CANCEL_OPTION, -1, null) == JOptionPane.OK_OPTION)
      {
        if (link.checkValid())
        {
          nameLinks.setElementAt(link.getName(), index);
          urlLinks.setElementAt(link.getURL(), index);
          updateLinkData();
          valid = true;
        }
      }

      else
      {
        break;
      }
    }
  }

  public void deleteLink_actionPerformed(ActionEvent e)
  {
    int index = linkNameList.getSelectedIndex();
    if (index == -1)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.getString("label.no_link_selected"),
              MessageManager.getString("label.no_link_selected"),
              JOptionPane.WARNING_MESSAGE);
      return;
    }
    nameLinks.removeElementAt(index);
    urlLinks.removeElementAt(index);
    updateLinkData();
  }

  void updateLinkData()
  {
    linkNameList.setListData(nameLinks);
    linkURLList.setListData(urlLinks);
  }

  public void defaultBrowser_mouseClicked(MouseEvent e)
  {
    JFileChooser chooser = new JFileChooser(".");
    chooser.setDialogTitle(MessageManager
            .getString("label.select_default_browser"));

    int value = chooser.showOpenDialog(this);

    if (value == JFileChooser.APPROVE_OPTION)
    {
      defaultBrowser.setText(chooser.getSelectedFile().getAbsolutePath());
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GPreferences#showunconserved_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  protected void showunconserved_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub
    super.showunconserved_actionPerformed(e);
  }

  public static Collection getGroupURLLinks()
  {
    return groupURLLinks;
  }

  @Override
  public void minColour_actionPerformed(JPanel panel)
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_colour_minimum_value"),
            minColour.getBackground());
    if (col != null)
    {
      panel.setBackground(col);
    }
    panel.repaint();
  }

  @Override
  public void maxColour_actionPerformed(JPanel panel)
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_colour_maximum_value"),
            maxColour.getBackground());
    if (col != null)
    {
      panel.setBackground(col);
    }
    panel.repaint();
  }

  @Override
  protected void userIdWidth_actionPerformed()
  {
    try
    {
      String val = userIdWidth.getText().trim();
      if (val.length() > 0)
      {
        Integer iw = Integer.parseInt(val);
        if (iw.intValue() < 12)
        {
          throw new NumberFormatException();
        }
        userIdWidth.setText(iw.toString());
      }
    } catch (NumberFormatException x)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop, MessageManager
              .getString("warn.user_defined_width_requirements"),
              MessageManager.getString("label.invalid_id_column_width"),
              JOptionPane.WARNING_MESSAGE);
      userIdWidth.setText("");
    }
  }

  @Override
  protected void autoIdWidth_actionPerformed()
  {
    userIdWidth.setEnabled(!autoIdWidth.isSelected());
    userIdWidthlabel.setEnabled(!autoIdWidth.isSelected());
  }

  /**
   * Returns true if chimera path is to a valid executable, else show an error
   * dialog.
   */
  private boolean validateChimeraPath()
  {
    if (chimeraPath.getText().trim().length() > 0)
    {
      File f = new File(chimeraPath.getText());
      if (!f.canExecute())
      {
        JOptionPane.showInternalMessageDialog(Desktop.desktop,
                MessageManager.getString("label.invalid_chimera_path"),
                MessageManager.getString("label.invalid_name"),
                JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    return true;
  }

  /**
   * If Chimera is selected, check it can be found on default or user-specified
   * path, if not show a warning/help dialog.
   */
  @Override
  protected void structureViewer_actionPerformed(String selectedItem)
  {
    if (!selectedItem.equals(ViewerType.CHIMERA.name()))
    {
      return;
    }
    boolean found = false;

    /*
     * Try user-specified and standard paths for Chimera executable.
     */
    List<String> paths = StructureManager.getChimeraPaths();
    paths.add(0, chimeraPath.getText());
    for (String path : paths)
    {
      if (new File(path.trim()).canExecute())
      {
        found = true;
        break;
      }
    }
    if (!found)
    {
      String[] options = { "OK", "Help" };
      int showHelp = JOptionPane.showInternalOptionDialog(
              Desktop.desktop,
              JvSwingUtils.wrapTooltip(true,
                      MessageManager.getString("label.chimera_missing")),
              "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
              null, options, options[0]);
      if (showHelp == JOptionPane.NO_OPTION)
      {
        try
        {
          Help.showHelpWindow(HelpId.StructureViewer);
        } catch (HelpSetException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

}
