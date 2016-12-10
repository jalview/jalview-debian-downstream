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
package jalview.jbgui;

import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.gui.JvSwingUtils;
import jalview.gui.StructureViewer.ViewerType;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Base class for the Preferences panel.
 * 
 * @author $author$
 * @version $Revision$
 */
public class GPreferences extends JPanel
{
  private static final Font LABEL_FONT = JvSwingUtils.getLabelFont();

  private static final Font LABEL_FONT_ITALIC = JvSwingUtils.getLabelFont(
          false, true);

  /*
   * Visual tab components
   */
  protected JCheckBox fullScreen = new JCheckBox();

  protected JCheckBox openoverv = new JCheckBox();

  protected JCheckBox seqLimit = new JCheckBox();

  protected JCheckBox rightAlign = new JCheckBox();

  protected JComboBox<String> fontSizeCB = new JComboBox<String>();

  protected JComboBox<String> fontStyleCB = new JComboBox<String>();

  protected JComboBox<String> fontNameCB = new JComboBox<String>();

  protected JCheckBox showUnconserved = new JCheckBox();

  protected JCheckBox idItalics = new JCheckBox();

  protected JCheckBox smoothFont = new JCheckBox();

  protected JCheckBox scaleProteinToCdna = new JCheckBox();

  protected JComboBox<String> gapSymbolCB = new JComboBox<String>();

  protected JCheckBox wrap = new JCheckBox();

  protected JComboBox<String> sortby = new JComboBox<String>();

  protected JComboBox<String> sortAnnBy = new JComboBox<String>();

  protected JComboBox<String> sortAutocalc = new JComboBox<String>();

  protected JCheckBox startupCheckbox = new JCheckBox();

  protected JTextField startupFileTextfield = new JTextField();

  // below are in the 'second column'
  protected JCheckBox annotations = new JCheckBox();

  protected JCheckBox quality = new JCheckBox();

  protected JCheckBox conservation = new JCheckBox();

  protected JCheckBox identity = new JCheckBox();

  protected JCheckBox showGroupConsensus = new JCheckBox();

  protected JCheckBox showGroupConservation = new JCheckBox();

  protected JCheckBox showConsensHistogram = new JCheckBox();

  protected JCheckBox showConsensLogo = new JCheckBox();

  protected JCheckBox showDbRefTooltip = new JCheckBox();

  protected JCheckBox showNpTooltip = new JCheckBox();

  /*
   * Structure tab and components
   */
  protected JPanel structureTab;

  protected JCheckBox structFromPdb = new JCheckBox();

  protected JCheckBox useRnaView = new JCheckBox();

  protected JCheckBox addSecondaryStructure = new JCheckBox();

  protected JCheckBox addTempFactor = new JCheckBox();

  protected JComboBox<String> structViewer = new JComboBox<String>();

  protected JTextField chimeraPath = new JTextField();

  protected ButtonGroup mappingMethod = new ButtonGroup();

  protected JRadioButton siftsMapping = new JRadioButton();

  protected JRadioButton nwMapping = new JRadioButton();

  /*
   * Colours tab components
   */
  protected JPanel minColour = new JPanel();

  protected JPanel maxColour = new JPanel();

  protected JComboBox<String> protColour = new JComboBox<String>();

  protected JComboBox<String> nucColour = new JComboBox<String>();

  /*
   * Connections tab components
   */
  protected JList linkURLList = new JList();

  protected JTextField proxyServerTB = new JTextField();

  protected JTextField proxyPortTB = new JTextField();

  protected JTextField defaultBrowser = new JTextField();

  protected JList linkNameList = new JList();

  protected JCheckBox useProxy = new JCheckBox();

  protected JCheckBox usagestats = new JCheckBox();

  protected JCheckBox questionnaire = new JCheckBox();

  protected JCheckBox versioncheck = new JCheckBox();

  /*
   * Output tab components
   */
  protected JComboBox<Object> epsRendering = new JComboBox<Object>();

  protected JLabel userIdWidthlabel = new JLabel();

  protected JCheckBox autoIdWidth = new JCheckBox();

  protected JTextField userIdWidth = new JTextField();

  protected JCheckBox blcjv = new JCheckBox();

  protected JCheckBox pileupjv = new JCheckBox();

  protected JCheckBox clustaljv = new JCheckBox();

  protected JCheckBox msfjv = new JCheckBox();

  protected JCheckBox fastajv = new JCheckBox();

  protected JCheckBox pfamjv = new JCheckBox();

  protected JCheckBox pirjv = new JCheckBox();

  protected JCheckBox modellerOutput = new JCheckBox();

  protected JCheckBox embbedBioJSON = new JCheckBox();

  /*
   * Editing tab components
   */
  protected JCheckBox autoCalculateConsCheck = new JCheckBox();

  protected JCheckBox padGaps = new JCheckBox();

  protected JCheckBox sortByTree = new JCheckBox();

  /*
   * DAS Settings tab
   */
  protected JPanel dasTab = new JPanel();

  /*
   * Web Services tab
   */
  protected JPanel wsTab = new JPanel();

  /**
   * Creates a new GPreferences object.
   */
  public GPreferences()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Construct the panel and its tabbed sub-panels.
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    final JTabbedPane tabbedPane = new JTabbedPane();
    this.setLayout(new BorderLayout());
    JPanel okCancelPanel = initOkCancelPanel();
    this.add(tabbedPane, BorderLayout.CENTER);
    this.add(okCancelPanel, BorderLayout.SOUTH);

    tabbedPane.add(initVisualTab(),
            MessageManager.getString("label.visual"));

    tabbedPane.add(initColoursTab(),
            MessageManager.getString("label.colours"));

    tabbedPane.add(initStructureTab(),
            MessageManager.getString("label.structure"));

    tabbedPane.add(initConnectionsTab(),
            MessageManager.getString("label.connections"));

    tabbedPane.add(initOutputTab(),
            MessageManager.getString("label.output"));

    tabbedPane.add(initEditingTab(),
            MessageManager.getString("label.editing"));

    /*
     * See DasSourceBrowser for the real work of configuring this tab.
     */
    dasTab.setLayout(new BorderLayout());
    tabbedPane.add(dasTab, MessageManager.getString("label.das_settings"));

    /*
     * See WsPreferences for the real work of configuring this tab.
     */
    wsTab.setLayout(new BorderLayout());
    tabbedPane.add(wsTab, MessageManager.getString("label.web_services"));

    /*
     * Handler to validate a tab before leaving it - currently only for
     * Structure.
     */
    tabbedPane.addChangeListener(new ChangeListener()
    {
      private Component lastTab;

      @Override
      public void stateChanged(ChangeEvent e)
      {
        if (lastTab == structureTab
                && tabbedPane.getSelectedComponent() != structureTab)
        {
          if (!validateStructure())
          {
            tabbedPane.setSelectedComponent(structureTab);
            return;
          }
        }
        lastTab = tabbedPane.getSelectedComponent();
      }

    });
  }

  /**
   * Initialises the Editing tabbed panel.
   * 
   * @return
   */
  private JPanel initEditingTab()
  {
    JPanel editingTab = new JPanel();
    editingTab.setLayout(null);
    autoCalculateConsCheck.setFont(LABEL_FONT);
    autoCalculateConsCheck.setText(MessageManager
            .getString("label.autocalculate_consensus"));
    autoCalculateConsCheck.setBounds(new Rectangle(21, 52, 209, 23));
    padGaps.setFont(LABEL_FONT);
    padGaps.setText(MessageManager.getString("label.pad_gaps_when_editing"));
    padGaps.setBounds(new Rectangle(22, 94, 168, 23));
    sortByTree.setFont(LABEL_FONT);
    sortByTree
            .setText(MessageManager.getString("label.sort_with_new_tree"));
    sortByTree
            .setToolTipText(MessageManager
                    .getString("label.any_trees_calculated_or_loaded_alignment_automatically_sort"));
    sortByTree.setBounds(new Rectangle(22, 136, 168, 23));
    editingTab.add(autoCalculateConsCheck);
    editingTab.add(padGaps);
    editingTab.add(sortByTree);
    return editingTab;
  }

  /**
   * Initialises the Output tabbed panel.
   * 
   * @return
   */
  private JPanel initOutputTab()
  {
    JPanel outputTab = new JPanel();
    outputTab.setLayout(null);
    JLabel epsLabel = new JLabel();
    epsLabel.setFont(LABEL_FONT);
    epsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    epsLabel.setText(MessageManager.getString("label.eps_rendering_style"));
    epsLabel.setBounds(new Rectangle(9, 31, 140, 24));
    epsRendering.setFont(LABEL_FONT);
    epsRendering.setBounds(new Rectangle(154, 34, 187, 21));
    JLabel jLabel1 = new JLabel();
    jLabel1.setFont(LABEL_FONT);
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText(MessageManager.getString("label.append_start_end"));
    jLabel1.setFont(LABEL_FONT);
    fastajv.setFont(LABEL_FONT);
    fastajv.setHorizontalAlignment(SwingConstants.LEFT);
    clustaljv.setText(MessageManager.getString("label.clustal") + "     ");
    blcjv.setText(MessageManager.getString("label.blc") + "     ");
    fastajv.setText(MessageManager.getString("label.fasta") + "     ");
    msfjv.setText(MessageManager.getString("label.msf") + "     ");
    pfamjv.setText(MessageManager.getString("label.pfam") + "     ");
    pileupjv.setText(MessageManager.getString("label.pileup") + "     ");
    msfjv.setFont(LABEL_FONT);
    msfjv.setHorizontalAlignment(SwingConstants.LEFT);
    pirjv.setText(MessageManager.getString("label.pir") + "     ");
    JPanel jPanel11 = new JPanel();
    jPanel11.setFont(LABEL_FONT);
    TitledBorder titledBorder2 = new TitledBorder(
            MessageManager.getString("label.file_output"));
    jPanel11.setBorder(titledBorder2);
    jPanel11.setBounds(new Rectangle(30, 72, 196, 182));
    GridLayout gridLayout3 = new GridLayout();
    jPanel11.setLayout(gridLayout3);
    gridLayout3.setRows(8);
    blcjv.setFont(LABEL_FONT);
    blcjv.setHorizontalAlignment(SwingConstants.LEFT);
    clustaljv.setFont(LABEL_FONT);
    clustaljv.setHorizontalAlignment(SwingConstants.LEFT);
    pfamjv.setFont(LABEL_FONT);
    pfamjv.setHorizontalAlignment(SwingConstants.LEFT);
    pileupjv.setFont(LABEL_FONT);
    pileupjv.setHorizontalAlignment(SwingConstants.LEFT);
    pirjv.setFont(LABEL_FONT);
    pirjv.setHorizontalAlignment(SwingConstants.LEFT);
    autoIdWidth.setFont(LABEL_FONT);
    autoIdWidth.setText(MessageManager
            .getString("label.automatically_set_id_width"));
    autoIdWidth.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager
                    .getString("label.adjusts_width_generated_eps_png")));
    autoIdWidth.setBounds(new Rectangle(228, 96, 188, 23));
    autoIdWidth.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        autoIdWidth_actionPerformed();
      }
    });
    userIdWidthlabel.setFont(LABEL_FONT);
    userIdWidthlabel.setText(MessageManager
            .getString("label.figure_id_column_width"));
    userIdWidth
            .setToolTipText(JvSwingUtils.wrapTooltip(true, MessageManager
                    .getString("label.manually_specify_width_left_column")));
    userIdWidthlabel
            .setToolTipText(JvSwingUtils.wrapTooltip(true, MessageManager
                    .getString("label.manually_specify_width_left_column")));
    userIdWidthlabel.setBounds(new Rectangle(236, 120, 168, 23));
    userIdWidth.setFont(JvSwingUtils.getTextAreaFont());
    userIdWidth.setText("");
    userIdWidth.setBounds(new Rectangle(232, 144, 84, 23));
    userIdWidth.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        userIdWidth_actionPerformed();
      }
    });
    modellerOutput.setFont(LABEL_FONT);
    modellerOutput.setText(MessageManager
            .getString("label.use_modeller_output"));
    modellerOutput.setBounds(new Rectangle(228, 226, 168, 23));
    embbedBioJSON.setFont(LABEL_FONT);
    embbedBioJSON.setText(MessageManager.getString("label.embbed_biojson"));
    embbedBioJSON.setBounds(new Rectangle(228, 200, 250, 23));

    jPanel11.add(jLabel1);
    jPanel11.add(blcjv);
    jPanel11.add(clustaljv);
    jPanel11.add(fastajv);
    jPanel11.add(msfjv);
    jPanel11.add(pfamjv);
    jPanel11.add(pileupjv);
    jPanel11.add(pirjv);
    outputTab.add(autoIdWidth);
    outputTab.add(userIdWidth);
    outputTab.add(userIdWidthlabel);
    outputTab.add(modellerOutput);
    outputTab.add(embbedBioJSON);
    outputTab.add(epsLabel);
    outputTab.add(epsRendering);
    outputTab.add(jPanel11);
    return outputTab;
  }

  /**
   * Initialises the Connections tabbed panel.
   * 
   * @return
   */
  private JPanel initConnectionsTab()
  {
    JPanel connectTab = new JPanel();
    connectTab.setLayout(new GridBagLayout());
    JLabel serverLabel = new JLabel();
    serverLabel.setText(MessageManager.getString("label.address"));
    serverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    serverLabel.setFont(LABEL_FONT);
    proxyServerTB.setFont(LABEL_FONT);
    proxyPortTB.setFont(LABEL_FONT);
    JLabel portLabel = new JLabel();
    portLabel.setFont(LABEL_FONT);
    portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    portLabel.setText(MessageManager.getString("label.port"));
    JLabel browserLabel = new JLabel();
    browserLabel.setFont(new java.awt.Font("SansSerif", 0, 11));
    browserLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    browserLabel.setText(MessageManager
            .getString("label.default_browser_unix"));
    defaultBrowser.setFont(LABEL_FONT);
    defaultBrowser.setText("");
    usagestats.setText(MessageManager
            .getString("label.send_usage_statistics"));
    usagestats.setFont(LABEL_FONT);
    usagestats.setHorizontalAlignment(SwingConstants.RIGHT);
    usagestats.setHorizontalTextPosition(SwingConstants.LEADING);
    questionnaire.setText(MessageManager
            .getString("label.check_for_questionnaires"));
    questionnaire.setFont(LABEL_FONT);
    questionnaire.setHorizontalAlignment(SwingConstants.RIGHT);
    questionnaire.setHorizontalTextPosition(SwingConstants.LEADING);
    versioncheck.setText(MessageManager
            .getString("label.check_for_latest_version"));
    versioncheck.setFont(LABEL_FONT);
    versioncheck.setHorizontalAlignment(SwingConstants.RIGHT);
    versioncheck.setHorizontalTextPosition(SwingConstants.LEADING);
    JButton newLink = new JButton();
    newLink.setText(MessageManager.getString("action.new"));
    newLink.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        newLink_actionPerformed(e);
      }
    });
    JButton editLink = new JButton();
    editLink.setText(MessageManager.getString("action.edit"));
    editLink.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        editLink_actionPerformed(e);
      }
    });
    JButton deleteLink = new JButton();
    deleteLink.setText(MessageManager.getString("action.delete"));
    deleteLink.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        deleteLink_actionPerformed(e);
      }
    });

    linkURLList.addListSelectionListener(new ListSelectionListener()
    {
      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        int index = linkURLList.getSelectedIndex();
        linkNameList.setSelectedIndex(index);
      }
    });

    linkNameList.addListSelectionListener(new ListSelectionListener()
    {
      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        int index = linkNameList.getSelectedIndex();
        linkURLList.setSelectedIndex(index);
      }
    });

    JScrollPane linkScrollPane = new JScrollPane();
    linkScrollPane.setBorder(null);
    JPanel linkPanel = new JPanel();
    linkPanel.setBorder(new TitledBorder(MessageManager
            .getString("label.url_linkfrom_sequence_id")));
    linkPanel.setLayout(new BorderLayout());
    GridLayout gridLayout1 = new GridLayout();
    JPanel editLinkButtons = new JPanel();
    editLinkButtons.setLayout(gridLayout1);
    gridLayout1.setRows(3);
    linkNameList.setFont(LABEL_FONT);
    linkNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel linkPanel2 = new JPanel();
    linkPanel2.setLayout(borderLayout3);
    linkURLList.setFont(LABEL_FONT);
    linkURLList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    defaultBrowser.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          defaultBrowser_mouseClicked(e);
        }
      }
    });
    useProxy.setFont(LABEL_FONT);
    useProxy.setHorizontalAlignment(SwingConstants.RIGHT);
    useProxy.setHorizontalTextPosition(SwingConstants.LEADING);
    useProxy.setText(MessageManager.getString("label.use_proxy_server"));
    useProxy.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        useProxy_actionPerformed();
      }
    });
    linkPanel.add(editLinkButtons, BorderLayout.EAST);
    editLinkButtons.add(newLink, null);
    editLinkButtons.add(editLink, null);
    editLinkButtons.add(deleteLink, null);
    linkPanel.add(linkScrollPane, BorderLayout.CENTER);
    linkScrollPane.getViewport().add(linkPanel2, null);
    linkPanel2.add(linkURLList, BorderLayout.CENTER);
    linkPanel2.add(linkNameList, BorderLayout.WEST);
    JPanel jPanel1 = new JPanel();
    TitledBorder titledBorder1 = new TitledBorder(
            MessageManager.getString("label.proxy_server"));
    jPanel1.setBorder(titledBorder1);
    jPanel1.setLayout(new GridBagLayout());
    jPanel1.add(serverLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                    2, 4, 0), 5, 0));
    jPanel1.add(portLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                    0, 4, 0), 11, 6));
    connectTab.add(linkPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    16, 0, 0, 12), 359, -17));
    connectTab.add(jPanel1, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    21, 0, 35, 12), 4, 6));
    connectTab.add(browserLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
            0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(16, 0, 0, 0), 5, 1));
    jPanel1.add(useProxy, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                    2, 5, 185), 2, -4));
    jPanel1.add(proxyPortTB, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 2, 4, 2), 54, 1));
    jPanel1.add(proxyServerTB, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 2, 4, 0), 263, 1));
    connectTab.add(defaultBrowser, new GridBagConstraints(1, 1, 1, 1, 1.0,
            0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(15, 0, 0, 15), 307, 1));
    connectTab.add(usagestats, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 2, 4, 2), 70, 1));
    connectTab.add(questionnaire, new GridBagConstraints(1, 4, 1, 1, 1.0,
            0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 2, 4, 2), 70, 1));
    connectTab.add(versioncheck, new GridBagConstraints(0, 5, 1, 1, 1.0,
            0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 2, 4, 2), 70, 1));
    return connectTab;
  }

  /**
   * Initialises the parent panel which contains the tabbed sections.
   * 
   * @return
   */
  private JPanel initOkCancelPanel()
  {
    JButton ok = new JButton();
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    JButton cancel = new JButton();
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    JPanel okCancelPanel = new JPanel();
    okCancelPanel.add(ok);
    okCancelPanel.add(cancel);
    return okCancelPanel;
  }

  /**
   * Initialises the Colours tabbed panel.
   * 
   * @return
   */
  private JPanel initColoursTab()
  {
    JPanel coloursTab = new JPanel();
    coloursTab.setBorder(new TitledBorder(MessageManager
            .getString("action.open_new_alignment")));
    coloursTab.setLayout(new FlowLayout());
    JLabel mincolourLabel = new JLabel();
    mincolourLabel.setFont(LABEL_FONT);
    mincolourLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    mincolourLabel.setText(MessageManager.getString("label.min_colour"));
    minColour.setFont(LABEL_FONT);
    minColour.setBorder(BorderFactory.createEtchedBorder());
    minColour.setPreferredSize(new Dimension(40, 20));
    minColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        minColour_actionPerformed(minColour);
      }
    });
    JLabel maxcolourLabel = new JLabel();
    maxcolourLabel.setFont(LABEL_FONT);
    maxcolourLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    maxcolourLabel.setText(MessageManager.getString("label.max_colour"));
    maxColour.setFont(LABEL_FONT);
    maxColour.setBorder(BorderFactory.createEtchedBorder());
    maxColour.setPreferredSize(new Dimension(40, 20));
    maxColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        maxColour_actionPerformed(maxColour);
      }
    });

    protColour.setFont(LABEL_FONT);
    protColour.setBounds(new Rectangle(172, 225, 155, 21));
    JLabel protColourLabel = new JLabel();
    protColourLabel.setFont(LABEL_FONT);
    protColourLabel.setHorizontalAlignment(SwingConstants.LEFT);
    protColourLabel.setText(MessageManager
            .getString("label.prot_alignment_colour") + " ");
    JvSwingUtils.addtoLayout(coloursTab, MessageManager
            .getString("label.default_colour_scheme_for_alignment"),
            protColourLabel, protColour);

    nucColour.setFont(LABEL_FONT);
    nucColour.setBounds(new Rectangle(172, 240, 155, 21));
    JLabel nucColourLabel = new JLabel();
    nucColourLabel.setFont(LABEL_FONT);
    nucColourLabel.setHorizontalAlignment(SwingConstants.LEFT);
    nucColourLabel.setText(MessageManager
            .getString("label.nuc_alignment_colour") + " ");
    JvSwingUtils.addtoLayout(coloursTab, MessageManager
            .getString("label.default_colour_scheme_for_alignment"),
            nucColourLabel, nucColour);

    JPanel annotationShding = new JPanel();
    annotationShding.setBorder(new TitledBorder(MessageManager
            .getString("label.annotation_shading_default")));
    annotationShding.setLayout(new GridLayout(1, 2));
    JvSwingUtils.addtoLayout(annotationShding, MessageManager
            .getString("label.default_minimum_colour_annotation_shading"),
            mincolourLabel, minColour);
    JvSwingUtils.addtoLayout(annotationShding, MessageManager
            .getString("label.default_maximum_colour_annotation_shading"),
            maxcolourLabel, maxColour);
    coloursTab.add(annotationShding); // , FlowLayout.LEFT);
    return coloursTab;
  }

  /**
   * Initialises the Structure tabbed panel.
   * 
   * @return
   */
  private JPanel initStructureTab()
  {
    structureTab = new JPanel();

    structureTab.setBorder(new TitledBorder(MessageManager
            .getString("label.structure_options")));
    structureTab.setLayout(null);
    final int width = 400;
    final int height = 22;
    final int lineSpacing = 25;
    int ypos = 15;

    structFromPdb.setFont(LABEL_FONT);
    structFromPdb
            .setText(MessageManager.getString("label.struct_from_pdb"));
    structFromPdb.setBounds(new Rectangle(5, ypos, width, height));
    structFromPdb.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        boolean selected = structFromPdb.isSelected();
        // enable other options only when the first is checked
        useRnaView.setEnabled(selected);
        addSecondaryStructure.setEnabled(selected);
        addTempFactor.setEnabled(selected);
      }
    });
    structureTab.add(structFromPdb);

    // indent checkboxes that are conditional on the first one
    ypos += lineSpacing;
    useRnaView.setFont(LABEL_FONT);
    useRnaView.setText(MessageManager.getString("label.use_rnaview"));
    useRnaView.setBounds(new Rectangle(25, ypos, width, height));
    structureTab.add(useRnaView);

    ypos += lineSpacing;
    addSecondaryStructure.setFont(LABEL_FONT);
    addSecondaryStructure.setText(MessageManager
            .getString("label.autoadd_secstr"));
    addSecondaryStructure.setBounds(new Rectangle(25, ypos, width, height));
    structureTab.add(addSecondaryStructure);

    ypos += lineSpacing;
    addTempFactor.setFont(LABEL_FONT);
    addTempFactor.setText(MessageManager.getString("label.autoadd_temp"));
    addTempFactor.setBounds(new Rectangle(25, ypos, width, height));
    structureTab.add(addTempFactor);

    ypos += lineSpacing;
    JLabel viewerLabel = new JLabel();
    viewerLabel.setFont(LABEL_FONT);
    viewerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    viewerLabel.setText(MessageManager.getString("label.structure_viewer"));
    viewerLabel.setBounds(new Rectangle(10, ypos, 200, height));
    structureTab.add(viewerLabel);

    structViewer.setFont(LABEL_FONT);
    structViewer.setBounds(new Rectangle(160, ypos, 120, height));
    structViewer.addItem(ViewerType.JMOL.name());
    structViewer.addItem(ViewerType.CHIMERA.name());
    structViewer.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        structureViewer_actionPerformed((String) structViewer
                .getSelectedItem());
      }
    });
    structureTab.add(structViewer);

    ypos += lineSpacing;
    JLabel pathLabel = new JLabel();
    pathLabel.setFont(new java.awt.Font("SansSerif", 0, 11));
    pathLabel.setHorizontalAlignment(SwingConstants.LEFT);
    pathLabel.setText(MessageManager.getString("label.chimera_path"));
    final String tooltip = JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.chimera_path_tip"));
    pathLabel.setToolTipText(tooltip);
    pathLabel.setBounds(new Rectangle(10, ypos, 140, height));
    structureTab.add(pathLabel);

    chimeraPath.setFont(LABEL_FONT);
    chimeraPath.setText("");
    chimeraPath.setBounds(new Rectangle(160, ypos, 300, height));
    chimeraPath.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          String chosen = openFileChooser();
          if (chosen != null)
          {
            chimeraPath.setText(chosen);
          }
        }
      }
    });
    structureTab.add(chimeraPath);

    ypos += lineSpacing;
    nwMapping.setFont(LABEL_FONT);
    nwMapping.setText(MessageManager.getString("label.nw_mapping"));
    siftsMapping.setFont(LABEL_FONT);
    siftsMapping.setText(MessageManager.getString("label.sifts_mapping"));
    mappingMethod.add(nwMapping);
    mappingMethod.add(siftsMapping);
    JPanel mappingPanel = new JPanel();
    mappingPanel.setFont(LABEL_FONT);
    TitledBorder mmTitledBorder = new TitledBorder(
            MessageManager.getString("label.mapping_method"));
    mmTitledBorder.setTitleFont(LABEL_FONT);
    mappingPanel.setBorder(mmTitledBorder);
    mappingPanel.setBounds(new Rectangle(10, ypos, 452, 45));
    // GridLayout mappingLayout = new GridLayout();
    mappingPanel.setLayout(new GridLayout());
    mappingPanel.add(nwMapping);
    mappingPanel.add(siftsMapping);
    structureTab.add(mappingPanel);

    ypos += lineSpacing;
    ypos += lineSpacing;
    FTSDataColumnPreferences docFieldPref = new FTSDataColumnPreferences(
            PreferenceSource.PREFERENCES, PDBFTSRestClient.getInstance());
    docFieldPref.setBounds(new Rectangle(10, ypos, 450, 120));
    structureTab.add(docFieldPref);

    return structureTab;
  }

  /**
   * Action on choosing a structure viewer from combobox options.
   * 
   * @param selectedItem
   */
  protected void structureViewer_actionPerformed(String selectedItem)
  {
  }

  /**
   * Show a dialog for the user to choose a file. Returns the chosen path, or
   * null on Cancel.
   * 
   * @return
   */
  protected String openFileChooser()
  {
    String choice = null;
    JFileChooser chooser = new JFileChooser();

    // chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.open_local_file"));
    chooser.setToolTipText(MessageManager.getString("action.open"));

    int value = chooser.showOpenDialog(this);

    if (value == JFileChooser.APPROVE_OPTION)
    {
      choice = chooser.getSelectedFile().getPath();
    }
    return choice;
  }

  /**
   * Validate the structure tab preferences; if invalid, set focus on this tab.
   * 
   * @param e
   */
  protected boolean validateStructure(FocusEvent e)
  {
    if (!validateStructure())
    {
      e.getComponent().requestFocusInWindow();
      return false;
    }
    return true;
  }

  protected boolean validateStructure()
  {
    return false;
  }

  /**
   * Initialises the Visual tabbed panel.
   * 
   * @return
   */
  private JPanel initVisualTab()
  {
    JPanel visualTab = new JPanel();
    visualTab.setBorder(new TitledBorder(MessageManager
            .getString("action.open_new_alignment")));
    visualTab.setLayout(null);
    fullScreen.setFont(LABEL_FONT);
    fullScreen.setHorizontalAlignment(SwingConstants.RIGHT);
    fullScreen.setHorizontalTextPosition(SwingConstants.LEFT);
    fullScreen.setText(MessageManager.getString("label.maximize_window"));
    quality.setEnabled(false);
    quality.setFont(LABEL_FONT);
    quality.setHorizontalAlignment(SwingConstants.RIGHT);
    quality.setHorizontalTextPosition(SwingConstants.LEFT);
    quality.setSelected(true);
    quality.setText(MessageManager.getString("label.quality"));
    conservation.setEnabled(false);
    conservation.setFont(LABEL_FONT);
    conservation.setHorizontalAlignment(SwingConstants.RIGHT);
    conservation.setHorizontalTextPosition(SwingConstants.LEFT);
    conservation.setSelected(true);
    conservation.setText(MessageManager.getString("label.conservation"));
    identity.setEnabled(false);
    identity.setFont(LABEL_FONT);
    identity.setHorizontalAlignment(SwingConstants.RIGHT);
    identity.setHorizontalTextPosition(SwingConstants.LEFT);
    identity.setSelected(true);
    identity.setText(MessageManager.getString("label.consensus"));
    JLabel showGroupbits = new JLabel();
    showGroupbits.setFont(LABEL_FONT);
    showGroupbits.setHorizontalAlignment(SwingConstants.RIGHT);
    showGroupbits.setHorizontalTextPosition(SwingConstants.LEFT);
    showGroupbits.setText(MessageManager.getString("action.show_group")
            + ":");
    JLabel showConsensbits = new JLabel();
    showConsensbits.setFont(LABEL_FONT);
    showConsensbits.setHorizontalAlignment(SwingConstants.RIGHT);
    showConsensbits.setHorizontalTextPosition(SwingConstants.LEFT);
    showConsensbits.setText(MessageManager.getString("label.consensus")
            + ":");
    showConsensHistogram.setEnabled(false);
    showConsensHistogram.setFont(LABEL_FONT);
    showConsensHistogram.setHorizontalAlignment(SwingConstants.RIGHT);
    showConsensHistogram.setHorizontalTextPosition(SwingConstants.LEFT);
    showConsensHistogram.setSelected(true);
    showConsensHistogram.setText(MessageManager
            .getString("label.histogram"));
    showConsensLogo.setEnabled(false);
    showConsensLogo.setFont(LABEL_FONT);
    showConsensLogo.setHorizontalAlignment(SwingConstants.RIGHT);
    showConsensLogo.setHorizontalTextPosition(SwingConstants.LEFT);
    showConsensLogo.setSelected(true);
    showConsensLogo.setText(MessageManager.getString("label.logo"));
    showGroupConsensus.setEnabled(false);
    showGroupConsensus.setFont(LABEL_FONT);
    showGroupConsensus.setHorizontalAlignment(SwingConstants.RIGHT);
    showGroupConsensus.setHorizontalTextPosition(SwingConstants.LEFT);
    showGroupConsensus.setSelected(true);
    showGroupConsensus.setText(MessageManager.getString("label.consensus"));
    showGroupConservation.setEnabled(false);
    showGroupConservation.setFont(LABEL_FONT);
    showGroupConservation.setHorizontalAlignment(SwingConstants.RIGHT);
    showGroupConservation.setHorizontalTextPosition(SwingConstants.LEFT);
    showGroupConservation.setSelected(true);
    showGroupConservation.setText(MessageManager
            .getString("label.conservation"));
    showNpTooltip.setEnabled(true);
    showNpTooltip.setFont(LABEL_FONT);
    showNpTooltip.setHorizontalAlignment(SwingConstants.RIGHT);
    showNpTooltip.setHorizontalTextPosition(SwingConstants.LEFT);
    showNpTooltip.setSelected(true);
    showNpTooltip.setText(MessageManager
            .getString("label.non_positional_features"));
    showDbRefTooltip.setEnabled(true);
    showDbRefTooltip.setFont(LABEL_FONT);
    showDbRefTooltip.setHorizontalAlignment(SwingConstants.RIGHT);
    showDbRefTooltip.setHorizontalTextPosition(SwingConstants.LEFT);
    showDbRefTooltip.setSelected(true);
    showDbRefTooltip.setText(MessageManager
            .getString("label.database_references"));
    annotations.setFont(LABEL_FONT);
    annotations.setHorizontalAlignment(SwingConstants.RIGHT);
    annotations.setHorizontalTextPosition(SwingConstants.LEADING);
    annotations.setSelected(true);
    annotations.setText(MessageManager.getString("label.show_annotations"));
    annotations.setBounds(new Rectangle(169, 12, 200, 23));
    annotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    identity.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    showGroupConsensus.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    showUnconserved.setFont(LABEL_FONT);
    showUnconserved.setHorizontalAlignment(SwingConstants.RIGHT);
    showUnconserved.setHorizontalTextPosition(SwingConstants.LEFT);
    showUnconserved.setSelected(true);
    showUnconserved.setText(MessageManager
            .getString("action.show_unconserved"));
    showUnconserved.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showunconserved_actionPerformed(e);
      }
    });

    // TODO these are not yet added to / action from Preferences
    // JCheckBox shareSelections = new JCheckBox();
    // shareSelections.setFont(verdana11);
    // shareSelections.setHorizontalAlignment(SwingConstants.RIGHT);
    // shareSelections.setHorizontalTextPosition(SwingConstants.LEFT);
    // shareSelections.setSelected(true);
    // shareSelections.setText(MessageManager
    // .getString("label.share_selection_across_views"));
    // JCheckBox followHighlight = new JCheckBox();
    // followHighlight.setFont(verdana11);
    // followHighlight.setHorizontalAlignment(SwingConstants.RIGHT);
    // followHighlight.setHorizontalTextPosition(SwingConstants.LEFT);
    // // showUnconserved.setBounds(new Rectangle(169, 40, 200, 23));
    // followHighlight.setSelected(true);
    // followHighlight.setText(MessageManager
    // .getString("label.scroll_highlighted_regions"));

    seqLimit.setFont(LABEL_FONT);
    seqLimit.setHorizontalAlignment(SwingConstants.RIGHT);
    seqLimit.setHorizontalTextPosition(SwingConstants.LEFT);
    seqLimit.setText(MessageManager.getString("label.full_sequence_id"));
    smoothFont.setFont(LABEL_FONT);
    smoothFont.setHorizontalAlignment(SwingConstants.RIGHT);
    smoothFont.setHorizontalTextPosition(SwingConstants.LEADING);
    smoothFont.setText(MessageManager.getString("label.smooth_font"));
    scaleProteinToCdna.setFont(LABEL_FONT);
    scaleProteinToCdna.setHorizontalAlignment(SwingConstants.RIGHT);
    scaleProteinToCdna.setHorizontalTextPosition(SwingConstants.LEADING);
    scaleProteinToCdna.setText(MessageManager
            .getString("label.scale_protein_to_cdna"));
    scaleProteinToCdna.setToolTipText(MessageManager
            .getString("label.scale_protein_to_cdna_tip"));
    JLabel gapLabel = new JLabel();
    gapLabel.setFont(LABEL_FONT);
    gapLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    gapLabel.setText(MessageManager.getString("label.gap_symbol") + " ");
    JLabel fontLabel = new JLabel();
    fontLabel.setFont(LABEL_FONT);
    fontLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    fontLabel.setText(MessageManager.getString("label.font"));
    fontSizeCB.setFont(LABEL_FONT);
    fontSizeCB.setBounds(new Rectangle(320, 112, 65, 23));
    fontStyleCB.setFont(LABEL_FONT);
    fontStyleCB.setBounds(new Rectangle(382, 112, 80, 23));
    fontNameCB.setFont(LABEL_FONT);
    fontNameCB.setBounds(new Rectangle(172, 112, 147, 23));
    gapSymbolCB.setFont(LABEL_FONT);
    gapSymbolCB.setBounds(new Rectangle(172, 215, 69, 23));
    DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
    dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
    gapSymbolCB.setRenderer(dlcr);

    startupCheckbox.setText(MessageManager.getString("action.open_file"));
    startupCheckbox.setFont(LABEL_FONT);
    startupCheckbox.setHorizontalAlignment(SwingConstants.RIGHT);
    startupCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
    startupCheckbox.setSelected(true);
    startupFileTextfield.setFont(LABEL_FONT);
    startupFileTextfield.setBounds(new Rectangle(172, 310, 330, 20));
    startupFileTextfield.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          startupFileTextfield_mouseClicked();
        }
      }
    });

    sortby.setFont(LABEL_FONT);
    sortby.setBounds(new Rectangle(172, 260, 155, 21));
    JLabel sortLabel = new JLabel();
    sortLabel.setFont(LABEL_FONT);
    sortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    sortLabel.setText(MessageManager.getString("label.sort_by"));
    sortAnnBy.setFont(LABEL_FONT);
    sortAnnBy.setBounds(new Rectangle(172, 285, 110, 21));
    JLabel sortAnnLabel = new JLabel();
    sortAnnLabel.setFont(LABEL_FONT);
    sortAnnLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    sortAnnLabel.setText(MessageManager.getString("label.sort_ann_by"));
    sortAutocalc.setFont(LABEL_FONT);
    sortAutocalc.setBounds(new Rectangle(290, 285, 165, 21));

    JPanel annsettingsPanel = new JPanel();
    annsettingsPanel.setBounds(new Rectangle(173, 34, 320, 75));
    annsettingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    annsettingsPanel.setBorder(new EtchedBorder());
    visualTab.add(annsettingsPanel);
    Border jb = new EmptyBorder(1, 1, 4, 5);
    quality.setBorder(jb);
    conservation.setBorder(jb);
    identity.setBorder(jb);
    showConsensbits.setBorder(jb);
    showGroupbits.setBorder(jb);
    showGroupConsensus.setBorder(jb);
    showGroupConservation.setBorder(jb);
    showConsensHistogram.setBorder(jb);
    showConsensLogo.setBorder(jb);

    JPanel autoAnnotSettings = new JPanel();
    autoAnnotSettings.setLayout(new GridLayout(3, 3));
    annsettingsPanel.add(autoAnnotSettings);
    autoAnnotSettings.add(quality);
    autoAnnotSettings.add(conservation);
    autoAnnotSettings.add(identity);
    autoAnnotSettings.add(showGroupbits);
    autoAnnotSettings.add(showGroupConservation);
    autoAnnotSettings.add(showGroupConsensus);
    autoAnnotSettings.add(showConsensbits);
    autoAnnotSettings.add(showConsensHistogram);
    autoAnnotSettings.add(showConsensLogo);

    JPanel tooltipSettings = new JPanel();
    tooltipSettings.setBorder(new TitledBorder(MessageManager
            .getString("label.sequence_id_tooltip")));
    tooltipSettings.setBounds(173, 140, 220, 62);
    tooltipSettings.setLayout(new GridLayout(2, 1));
    tooltipSettings.add(showDbRefTooltip);
    tooltipSettings.add(showNpTooltip);
    visualTab.add(tooltipSettings);

    wrap.setFont(LABEL_FONT);
    wrap.setHorizontalAlignment(SwingConstants.TRAILING);
    wrap.setHorizontalTextPosition(SwingConstants.LEADING);
    wrap.setText(MessageManager.getString("label.wrap_alignment"));
    rightAlign.setFont(LABEL_FONT);
    rightAlign.setForeground(Color.black);
    rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
    rightAlign.setHorizontalTextPosition(SwingConstants.LEFT);
    rightAlign.setText(MessageManager.getString("label.right_align_ids"));
    idItalics.setFont(LABEL_FONT_ITALIC);
    idItalics.setHorizontalAlignment(SwingConstants.RIGHT);
    idItalics.setHorizontalTextPosition(SwingConstants.LEADING);
    idItalics.setText(MessageManager
            .getString("label.sequence_name_italics"));
    openoverv.setFont(LABEL_FONT);
    openoverv.setActionCommand(MessageManager
            .getString("label.open_overview"));
    openoverv.setHorizontalAlignment(SwingConstants.RIGHT);
    openoverv.setHorizontalTextPosition(SwingConstants.LEFT);
    openoverv.setText(MessageManager.getString("label.open_overview"));
    JPanel jPanel2 = new JPanel();
    jPanel2.setBounds(new Rectangle(7, 17, 158, 310));
    jPanel2.setLayout(new GridLayout(14, 1));
    jPanel2.add(fullScreen);
    jPanel2.add(openoverv);
    jPanel2.add(seqLimit);
    jPanel2.add(rightAlign);
    jPanel2.add(fontLabel);
    jPanel2.add(showUnconserved);
    jPanel2.add(idItalics);
    jPanel2.add(smoothFont);
    jPanel2.add(scaleProteinToCdna);
    jPanel2.add(gapLabel);
    jPanel2.add(wrap);
    jPanel2.add(sortLabel);
    jPanel2.add(sortAnnLabel);
    jPanel2.add(startupCheckbox);
    visualTab.add(jPanel2);
    visualTab.add(annotations);
    visualTab.add(startupFileTextfield);
    visualTab.add(sortby);
    visualTab.add(sortAnnBy);
    visualTab.add(sortAutocalc);
    visualTab.add(gapSymbolCB);
    visualTab.add(fontNameCB);
    visualTab.add(fontSizeCB);
    visualTab.add(fontStyleCB);
    return visualTab;
  }

  protected void autoIdWidth_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void userIdWidth_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void maxColour_actionPerformed(JPanel panel)
  {
  }

  protected void minColour_actionPerformed(JPanel panel)
  {
  }

  protected void showunconserved_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void annotations_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   */
  public void startupFileTextfield_mouseClicked()
  {
  }

  public void newLink_actionPerformed(ActionEvent e)
  {

  }

  public void editLink_actionPerformed(ActionEvent e)
  {

  }

  public void deleteLink_actionPerformed(ActionEvent e)
  {

  }

  public void defaultBrowser_mouseClicked(MouseEvent e)
  {

  }

  public void linkURLList_keyTyped(KeyEvent e)
  {

  }

  public void useProxy_actionPerformed()
  {
    proxyServerTB.setEnabled(useProxy.isSelected());
    proxyPortTB.setEnabled(useProxy.isSelected());
  }

}
