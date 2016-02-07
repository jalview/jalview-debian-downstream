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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.ColumnSelection;
import jalview.schemes.AnnotationColourGradient;
import jalview.util.MessageManager;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class AnnotationColumnChooser extends AnnotationRowFilter implements
        ItemListener
{

  private JComboBox<String> annotations;

  private JPanel actionPanel = new JPanel();

  private JPanel thresholdPanel = new JPanel();

  private JPanel switchableViewsPanel = new JPanel(new CardLayout());

  private CardLayout switchableViewsLayout = (CardLayout) (switchableViewsPanel
          .getLayout());

  private JPanel noGraphFilterView = new JPanel();

  private JPanel graphFilterView = new JPanel();

  private JPanel annotationComboBoxPanel = new JPanel();

  private BorderLayout borderLayout1 = new BorderLayout();

  private JComboBox<String> threshold = new JComboBox<String>();

  private StructureFilterPanel gStructureFilterPanel;

  private StructureFilterPanel ngStructureFilterPanel;

  private StructureFilterPanel currentStructureFilterPanel;

  private SearchPanel currentSearchPanel;

  private SearchPanel gSearchPanel;

  private SearchPanel ngSearchPanel;

  private FurtherActionPanel currentFurtherActionPanel;

  private FurtherActionPanel gFurtherActionPanel;

  private FurtherActionPanel ngFurtherActionPanel;

  public static final int ACTION_OPTION_SELECT = 1;

  public static int ACTION_OPTION_HIDE = 2;

  public static String NO_GRAPH_VIEW = "0";

  public static String GRAPH_VIEW = "1";

  private int actionOption = ACTION_OPTION_SELECT;

  private ColumnSelection oldColumnSelection;

  public AnnotationColumnChooser()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public AnnotationColumnChooser(AlignViewport av, final AlignmentPanel ap)
  {
    super(av, ap);
    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.select_by_annotation"), 520,
            215);

    addSliderChangeListener();
    addSliderMouseListeners();

    if (av.getAlignment().getAlignmentAnnotation() == null)
    {
      return;
    }
    setOldColumnSelection(av.getColumnSelection());
    adjusting = true;

    setAnnotations(new JComboBox<String>(getAnnotationItems(false)));
    populateThresholdComboBox(threshold);

    // restore Object state from the previous session if one exists
    if (av.getAnnotationColumnSelectionState() != null)
    {
      currentSearchPanel = av.getAnnotationColumnSelectionState()
              .getCurrentSearchPanel();
      currentStructureFilterPanel = av.getAnnotationColumnSelectionState()
              .getCurrentStructureFilterPanel();
      annotations.setSelectedIndex(av.getAnnotationColumnSelectionState()
              .getAnnotations().getSelectedIndex());
      threshold.setSelectedIndex(av.getAnnotationColumnSelectionState()
              .getThreshold().getSelectedIndex());
      actionOption = av.getAnnotationColumnSelectionState()
              .getActionOption();
    }

    try
    {
      jbInit();
    } catch (Exception ex)
    {
    }
    adjusting = false;

    updateView();
    frame.invalidate();
    frame.pack();
  }

  private void jbInit() throws Exception
  {
    ok.setOpaque(false);
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });

    cancel.setOpaque(false);
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });

    annotations.addItemListener(this);
    annotations.setToolTipText(MessageManager
            .getString("info.select_annotation_row"));
    threshold.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        threshold_actionPerformed(e);
      }
    });

    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);
    thresholdValue.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        thresholdValue_actionPerformed(e);
      }
    });

    slider.setPaintLabels(false);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setOpaque(false);
    slider.setPreferredSize(new Dimension(100, 32));

    thresholdPanel.setBorder(new TitledBorder(MessageManager
            .getString("label.threshold_filter")));
    thresholdPanel.setBackground(Color.white);
    thresholdPanel.setFont(JvSwingUtils.getLabelFont());
    thresholdPanel.setLayout(new MigLayout("", "[left][right]", "[][]"));

    actionPanel.setBackground(Color.white);
    actionPanel.setFont(JvSwingUtils.getLabelFont());

    graphFilterView.setLayout(new MigLayout("", "[left][right]", "[][]"));
    graphFilterView.setBackground(Color.white);

    noGraphFilterView.setLayout(new MigLayout("", "[left][right]", "[][]"));
    noGraphFilterView.setBackground(Color.white);

    annotationComboBoxPanel.setBackground(Color.white);
    annotationComboBoxPanel.setFont(JvSwingUtils.getLabelFont());

    gSearchPanel = new SearchPanel(this);
    ngSearchPanel = new SearchPanel(this);
    gFurtherActionPanel = new FurtherActionPanel(this);
    ngFurtherActionPanel = new FurtherActionPanel(this);
    gStructureFilterPanel = new StructureFilterPanel(this);
    ngStructureFilterPanel = new StructureFilterPanel(this);

    thresholdPanel.add(getThreshold());
    thresholdPanel.add(thresholdValue, "wrap");
    thresholdPanel.add(slider, "grow, span, wrap");

    actionPanel.add(ok);
    actionPanel.add(cancel);

    graphFilterView.add(gSearchPanel, "grow, span, wrap");
    graphFilterView.add(gStructureFilterPanel, "grow, span, wrap");
    graphFilterView.add(thresholdPanel, "grow, span, wrap");
    graphFilterView.add(gFurtherActionPanel);

    noGraphFilterView.add(ngSearchPanel, "grow, span, wrap");
    noGraphFilterView.add(ngStructureFilterPanel, "grow, span, wrap");
    noGraphFilterView.add(ngFurtherActionPanel);

    annotationComboBoxPanel.add(getAnnotations());
    switchableViewsPanel.add(noGraphFilterView,
            AnnotationColumnChooser.NO_GRAPH_VIEW);
    switchableViewsPanel.add(graphFilterView,
            AnnotationColumnChooser.GRAPH_VIEW);

    this.setLayout(borderLayout1);
    this.add(annotationComboBoxPanel, java.awt.BorderLayout.PAGE_START);
    this.add(switchableViewsPanel, java.awt.BorderLayout.CENTER);
    this.add(actionPanel, java.awt.BorderLayout.SOUTH);

    selectedAnnotationChanged();
    updateThresholdPanelToolTip();
    this.validate();
  }

  public void updateThresholdPanelToolTip()
  {
    thresholdValue.setToolTipText("");
    slider.setToolTipText("");

    String defaultTtip = MessageManager
            .getString("info.change_threshold_mode_to_enable");

    String threshold = getThreshold().getSelectedItem().toString();
    if (threshold.equalsIgnoreCase("No Threshold"))
    {
      thresholdValue.setToolTipText(defaultTtip);
      slider.setToolTipText(defaultTtip);
    }
  }

  public void reset()
  {
    if (this.getOldColumnSelection() != null)
    {
      av.getColumnSelection().clear();

      if (av.getAnnotationColumnSelectionState() != null)
      {
        ColumnSelection oldSelection = av
                .getAnnotationColumnSelectionState()
                .getOldColumnSelection();
        if (oldSelection != null && oldSelection.getHiddenColumns() != null
                && !oldSelection.getHiddenColumns().isEmpty())
        {
          for (Iterator<int[]> itr = oldSelection.getHiddenColumns()
                  .iterator(); itr.hasNext();)
          {
            int positions[] = itr.next();
            av.hideColumns(positions[0], positions[1]);
          }
        }
        av.setColumnSelection(oldSelection);
      }
      ap.paintAlignment(true);
    }

  }

  public void valueChanged(boolean updateAllAnnotation)
  {
    if (slider.isEnabled())
    {
      getCurrentAnnotation().threshold.value = slider.getValue() / 1000f;
      updateView();
      propagateSeqAssociatedThreshold(updateAllAnnotation,
              getCurrentAnnotation());
      ap.paintAlignment(false);
    }
  }

  public JComboBox<String> getThreshold()
  {
    return threshold;
  }

  public void setThreshold(JComboBox<String> threshold)
  {
    this.threshold = threshold;
  }

  public JComboBox<String> getAnnotations()
  {
    return annotations;
  }

  public void setAnnotations(JComboBox<String> annotations)
  {
    this.annotations = annotations;
  }

  @Override
  public void updateView()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    AnnotationFilterParameter filterParams = new AnnotationFilterParameter();

    setCurrentAnnotation(av.getAlignment().getAlignmentAnnotation()[annmap[getAnnotations()
            .getSelectedIndex()]]);

    int selectedThresholdItem = getSelectedThresholdItem(getThreshold()
            .getSelectedIndex());

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);

    if (selectedThresholdItem == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
      // build filter params
    }
    else if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD)
    {
      if (getCurrentAnnotation().threshold == null)
      {
        getCurrentAnnotation()
                .setThreshold(
                        new jalview.datamodel.GraphLine(
                                (getCurrentAnnotation().graphMax - getCurrentAnnotation().graphMin) / 2f,
                                "Threshold", Color.black));
      }

      adjusting = true;
      float range = getCurrentAnnotation().graphMax * 1000
              - getCurrentAnnotation().graphMin * 1000;

      slider.setMinimum((int) (getCurrentAnnotation().graphMin * 1000));
      slider.setMaximum((int) (getCurrentAnnotation().graphMax * 1000));
      slider.setValue((int) (getCurrentAnnotation().threshold.value * 1000));
      thresholdValue.setText(getCurrentAnnotation().threshold.value + "");
      slider.setMajorTickSpacing((int) (range / 10f));
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;

      // build filter params
      filterParams
              .setThresholdType(AnnotationFilterParameter.ThresholdType.NO_THRESHOLD);
      if (getCurrentAnnotation().graph != AlignmentAnnotation.NO_GRAPH)
      {
        filterParams
                .setThresholdValue(getCurrentAnnotation().threshold.value);

        if (selectedThresholdItem == AnnotationColourGradient.ABOVE_THRESHOLD)
        {
          filterParams
                  .setThresholdType(AnnotationFilterParameter.ThresholdType.ABOVE_THRESHOLD);
        }
        else if (selectedThresholdItem == AnnotationColourGradient.BELOW_THRESHOLD)
        {
          filterParams
                  .setThresholdType(AnnotationFilterParameter.ThresholdType.BELOW_THRESHOLD);
        }
      }
    }

    updateThresholdPanelToolTip();
    if (currentStructureFilterPanel != null)
    {
      if (currentStructureFilterPanel.alphaHelix.isSelected())
      {
        filterParams.setFilterAlphaHelix(true);
      }
      if (currentStructureFilterPanel.betaStrand.isSelected())
      {
        filterParams.setFilterBetaSheet(true);
      }
      if (currentStructureFilterPanel.turn.isSelected())
      {
        filterParams.setFilterTurn(true);
      }
    }

    if (currentSearchPanel != null)
    {

      if (!currentSearchPanel.searchBox.getText().isEmpty())
      {
        currentSearchPanel.description.setEnabled(true);
        currentSearchPanel.displayName.setEnabled(true);
        filterParams.setRegexString(currentSearchPanel.searchBox.getText());
        if (currentSearchPanel.displayName.isSelected())
        {
          filterParams
                  .addRegexSearchField(AnnotationFilterParameter.SearchableAnnotationField.DISPLAY_STRING);
        }
        if (currentSearchPanel.description.isSelected())
        {
          filterParams
                  .addRegexSearchField(AnnotationFilterParameter.SearchableAnnotationField.DESCRIPTION);
        }
      }
      else
      {
        currentSearchPanel.description.setEnabled(false);
        currentSearchPanel.displayName.setEnabled(false);
      }
    }

    av.getColumnSelection().filterAnnotations(
            getCurrentAnnotation().annotations, filterParams);

    av.showAllHiddenColumns();
    if (getActionOption() == ACTION_OPTION_HIDE)
    {
      av.hideSelectedColumns();
    }

    filterParams = null;
    av.setAnnotationColumnSelectionState(this);
    ap.paintAlignment(true);
  }

  public ColumnSelection getOldColumnSelection()
  {
    return oldColumnSelection;
  }

  public void setOldColumnSelection(ColumnSelection currentColumnSelection)
  {
    if (currentColumnSelection != null)
    {
      this.oldColumnSelection = new ColumnSelection(currentColumnSelection);
    }
  }

  public FurtherActionPanel getCurrentFutherActionPanel()
  {
    return currentFurtherActionPanel;
  }

  public void setCurrentFutherActionPanel(
          FurtherActionPanel currentFutherActionPanel)
  {
    this.currentFurtherActionPanel = currentFutherActionPanel;
  }

  public SearchPanel getCurrentSearchPanel()
  {
    return currentSearchPanel;
  }

  public void setCurrentSearchPanel(SearchPanel currentSearchPanel)
  {
    this.currentSearchPanel = currentSearchPanel;
  }

  public int getActionOption()
  {
    return actionOption;
  }

  public void setActionOption(int actionOption)
  {
    this.actionOption = actionOption;
  }

  public StructureFilterPanel getCurrentStructureFilterPanel()
  {
    return currentStructureFilterPanel;
  }

  public void setCurrentStructureFilterPanel(
          StructureFilterPanel currentStructureFilterPanel)
  {
    this.currentStructureFilterPanel = currentStructureFilterPanel;
  }

  public void select_action(ActionEvent actionEvent)
  {
    JRadioButton radioButton = (JRadioButton) actionEvent.getSource();
    if (radioButton.isSelected())
    {
      setActionOption(ACTION_OPTION_SELECT);
      updateView();
    }
  }

  public void hide_action(ActionEvent actionEvent)
  {
    JRadioButton radioButton = (JRadioButton) actionEvent.getSource();
    if (radioButton.isSelected())
    {
      setActionOption(ACTION_OPTION_HIDE);
      updateView();
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e)
  {
    selectedAnnotationChanged();
  }

  public void selectedAnnotationChanged()
  {
    String currentView = AnnotationColumnChooser.NO_GRAPH_VIEW;
    if (av.getAlignment().getAlignmentAnnotation()[annmap[getAnnotations()
            .getSelectedIndex()]].graph != AlignmentAnnotation.NO_GRAPH)
    {
      currentView = AnnotationColumnChooser.GRAPH_VIEW;
    }

    gSearchPanel.syncState();
    gFurtherActionPanel.syncState();
    gStructureFilterPanel.syncState();

    ngSearchPanel.syncState();
    ngFurtherActionPanel.syncState();
    ngStructureFilterPanel.syncState();

    switchableViewsLayout.show(switchableViewsPanel, currentView);
    updateView();
  }

  public class FurtherActionPanel extends JPanel
  {
    private AnnotationColumnChooser aColChooser;

    private JRadioButton hideOption = new JRadioButton();

    private JRadioButton selectOption = new JRadioButton();

    private ButtonGroup optionsGroup = new ButtonGroup();

    public FurtherActionPanel(AnnotationColumnChooser aColChooser)
    {
      this.aColChooser = aColChooser;
      JvSwingUtils.jvInitComponent(selectOption, "action.select");
      selectOption.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          selectRadioAction(actionEvent);
        }
      });

      JvSwingUtils.jvInitComponent(hideOption, "action.hide");
      hideOption.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          hideRadioAction(actionEvent);
        }
      });

      optionsGroup.add(selectOption);
      optionsGroup.add(hideOption);
      optionsGroup.setSelected(selectOption.getModel(), true);

      JvSwingUtils.jvInitComponent(this);
      syncState();

      this.add(selectOption);
      this.add(hideOption);
    }

    public void selectRadioAction(ActionEvent actionEvent)
    {
      aColChooser.setCurrentFutherActionPanel(this);
      aColChooser.select_action(actionEvent);
    }

    public void hideRadioAction(ActionEvent actionEvent)
    {
      aColChooser.setCurrentFutherActionPanel(this);
      aColChooser.hide_action(actionEvent);
    }

    public void syncState()
    {
      if (aColChooser.getActionOption() == AnnotationColumnChooser.ACTION_OPTION_HIDE)
      {
        this.optionsGroup.setSelected(this.hideOption.getModel(), true);
      }
      else
      {
        this.optionsGroup.setSelected(this.selectOption.getModel(), true);
      }
    }
  }

  public class StructureFilterPanel extends JPanel
  {
    private AnnotationColumnChooser aColChooser;

    private JCheckBox alphaHelix = new JCheckBox();

    private JCheckBox betaStrand = new JCheckBox();

    private JCheckBox turn = new JCheckBox();

    private JCheckBox all = new JCheckBox();

    public StructureFilterPanel(AnnotationColumnChooser aColChooser)
    {
      this.aColChooser = aColChooser;

      JvSwingUtils.jvInitComponent(alphaHelix, "label.alpha_helix");
      alphaHelix.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          alphaHelix_actionPerformed();
        }
      });

      JvSwingUtils.jvInitComponent(betaStrand, "label.beta_strand");
      betaStrand.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          betaStrand_actionPerformed();
        }
      });

      JvSwingUtils.jvInitComponent(turn, "label.turn");
      turn.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          turn_actionPerformed();
        }
      });

      JvSwingUtils.jvInitComponent(all, "label.select_all");
      all.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          all_actionPerformed();
        }
      });

      this.setBorder(new TitledBorder(MessageManager
              .getString("label.structures_filter")));
      JvSwingUtils.jvInitComponent(this);

      this.add(all);
      this.add(alphaHelix);
      this.add(betaStrand);
      this.add(turn);
    }

    public void alphaHelix_actionPerformed()
    {
      updateSelectAllState();
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void betaStrand_actionPerformed()
    {
      updateSelectAllState();
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void turn_actionPerformed()
    {
      updateSelectAllState();
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void all_actionPerformed()
    {
      if (all.isSelected())
      {
        alphaHelix.setSelected(true);
        betaStrand.setSelected(true);
        turn.setSelected(true);
      }
      else
      {
        alphaHelix.setSelected(false);
        betaStrand.setSelected(false);
        turn.setSelected(false);
      }
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void updateSelectAllState()
    {
      if (alphaHelix.isSelected() && betaStrand.isSelected()
              && turn.isSelected())
      {
        all.setSelected(true);
      }
      else
      {
        all.setSelected(false);
      }
    }

    public void syncState()
    {
      StructureFilterPanel sfp = aColChooser
              .getCurrentStructureFilterPanel();
      if (sfp != null)
      {
        alphaHelix.setSelected(sfp.alphaHelix.isSelected());
        betaStrand.setSelected(sfp.betaStrand.isSelected());
        turn.setSelected(sfp.turn.isSelected());
        if (sfp.all.isSelected())
        {
          all.setSelected(true);
          alphaHelix.setSelected(true);
          betaStrand.setSelected(true);
          turn.setSelected(true);
        }
      }

    }
  }

  public class SearchPanel extends JPanel
  {
    private AnnotationColumnChooser aColChooser;

    private JCheckBox displayName = new JCheckBox();

    private JCheckBox description = new JCheckBox();

    private JTextField searchBox = new JTextField(10);

    public SearchPanel(AnnotationColumnChooser aColChooser)
    {

      this.aColChooser = aColChooser;
      JvSwingUtils.jvInitComponent(this);
      this.setBorder(new TitledBorder(MessageManager
              .getString("label.search_filter")));

      JvSwingUtils.jvInitComponent(searchBox);
      searchBox.setToolTipText(MessageManager
              .getString("info.enter_search_text_here"));
      searchBox.getDocument().addDocumentListener(new DocumentListener()
      {
        @Override
        public void insertUpdate(DocumentEvent e)
        {
          searchStringAction();
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
          searchStringAction();
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
          searchStringAction();
        }
      });

      JvSwingUtils.jvInitComponent(displayName, "label.label");
      displayName.setEnabled(false);
      displayName.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          displayNameCheckboxAction();
        }
      });

      JvSwingUtils.jvInitComponent(description, "label.description");
      description.setEnabled(false);
      description.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          discriptionCheckboxAction();
        }
      });

      syncState();
      this.add(searchBox);
      this.add(displayName);
      this.add(description);
    }

    public void displayNameCheckboxAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void discriptionCheckboxAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void searchStringAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
      updateSearchPanelToolTips();
    }

    public void syncState()
    {
      SearchPanel sp = aColChooser.getCurrentSearchPanel();
      if (sp != null)
      {
        description.setEnabled(sp.description.isEnabled());
        description.setSelected(sp.description.isSelected());

        displayName.setEnabled(sp.displayName.isEnabled());
        displayName.setSelected(sp.displayName.isSelected());

        searchBox.setText(sp.searchBox.getText());
      }
      updateSearchPanelToolTips();
    }

    public void updateSearchPanelToolTips()
    {
      String defaultTtip = MessageManager
              .getString("info.enter_search_text_to_enable");
      String labelTtip = MessageManager.formatMessage(
              "info.search_in_annotation_label", annotations
                      .getSelectedItem().toString());
      String descTtip = MessageManager.formatMessage(
              "info.search_in_annotation_description", annotations
                      .getSelectedItem().toString());
      displayName.setToolTipText(displayName.isEnabled() ? labelTtip
              : defaultTtip);
      description.setToolTipText(description.isEnabled() ? descTtip
              : defaultTtip);
    }
  }

}
