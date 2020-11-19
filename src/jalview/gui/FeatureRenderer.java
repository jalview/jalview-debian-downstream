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

import jalview.api.FeatureColourI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.FeaturesFile;
import jalview.schemes.FeatureColour;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FeatureRenderer
        extends jalview.renderer.seqfeatures.FeatureRenderer
{
  /*
   * defaults for creating a new feature are the last created
   * feature type and group
   */
  static String lastFeatureAdded = "feature_1";

  static String lastFeatureGroupAdded = "Jalview";

  Color resBoxColour;

  AlignmentPanel ap;

  /**
   * Creates a new FeatureRenderer object
   * 
   * @param alignPanel
   */
  public FeatureRenderer(AlignmentPanel alignPanel)
  {
    super(alignPanel.av);
    this.ap = alignPanel;
    if (alignPanel.getSeqPanel() != null
            && alignPanel.getSeqPanel().seqCanvas != null
            && alignPanel.getSeqPanel().seqCanvas.fr != null)
    {
      transferSettings(alignPanel.getSeqPanel().seqCanvas.fr);
    }
  }

  FeatureColourI oldcol, fcol;

  int featureIndex = 0;

  /**
   * Presents a dialog allowing the user to add new features, or amend or delete
   * existing features. Currently this can be on
   * <ul>
   * <li>double-click on a sequence - Amend/Delete features at position</li>
   * <li>Create sequence feature from pop-up menu on selected region</li>
   * <li>Create features for pattern matches from Find</li>
   * </ul>
   * If the supplied feature type is null, show (and update on confirm) the type
   * and group of the last new feature created (with initial defaults of
   * "feature_1" and "Jalview").
   * 
   * @param sequences
   *          the sequences features are to be created on (if creating
   *          features), or a single sequence (if amending features)
   * @param features
   *          the current features at the position (if amending), or template
   *          new feature(s) with start/end position set (if creating)
   * @param create
   *          true to create features, false to amend or delete
   * @param alignPanel
   * @return
   */
  protected boolean amendFeatures(final List<SequenceI> sequences,
          final List<SequenceFeature> features, boolean create,
          final AlignmentPanel alignPanel)
  {
    featureIndex = 0;

    final JPanel mainPanel = new JPanel(new BorderLayout());

    final JTextField name = new JTextField(25);
    name.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        warnIfTypeHidden(mainPanel, name.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        warnIfTypeHidden(mainPanel, name.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        warnIfTypeHidden(mainPanel, name.getText());
      }
    });

    final JTextField group = new JTextField(25);
    group.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        warnIfGroupHidden(mainPanel, group.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        warnIfGroupHidden(mainPanel, group.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        warnIfGroupHidden(mainPanel, group.getText());
      }
    });

    final JTextArea description = new JTextArea(3, 25);
    final JSpinner start = new JSpinner();
    final JSpinner end = new JSpinner();
    start.setPreferredSize(new Dimension(80, 20));
    end.setPreferredSize(new Dimension(80, 20));
    final JLabel colour = new JLabel();
    colour.setOpaque(true);
    // colour.setBorder(BorderFactory.createEtchedBorder());
    colour.setMaximumSize(new Dimension(30, 16));
    colour.addMouseListener(new MouseAdapter()
    {
      /*
       * open colour chooser on click in colour panel
       */
      @Override
      public void mousePressed(MouseEvent evt)
      {
        if (fcol.isSimpleColour())
        {
          Color col = JColorChooser.showDialog(Desktop.desktop,
                  MessageManager.getString("label.select_feature_colour"),
                  fcol.getColour());
          if (col != null)
          {
            fcol = new FeatureColour(col);
            updateColourButton(mainPanel, colour, fcol);
          }
        }
        else
        {
          /*
           * variable colour dialog - on OK, refetch the updated
           * feature colour and update this display
           */
          final String ft = features.get(featureIndex).getType();
          final String type = ft == null ? lastFeatureAdded : ft;
          FeatureTypeSettings fcc = new FeatureTypeSettings(
                  FeatureRenderer.this, type);
          fcc.setRequestFocusEnabled(true);
          fcc.requestFocus();
          fcc.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              fcol = FeatureRenderer.this.getFeatureStyle(ft);
              setColour(type, fcol);
              updateColourButton(mainPanel, colour, fcol);
            }
          });
        }
      }
    });
    JPanel gridPanel = new JPanel(new GridLayout(3, 1));

    if (!create && features.size() > 1)
    {
      /*
       * more than one feature at selected position - 
       * add a drop-down to choose the feature to amend
       * space pad text if necessary to make entries distinct
       */
      gridPanel = new JPanel(new GridLayout(4, 1));
      JPanel choosePanel = new JPanel();
      choosePanel.add(new JLabel(
              MessageManager.getString("label.select_feature") + ":"));
      final JComboBox<String> overlaps = new JComboBox<>();
      List<String> added = new ArrayList<>();
      for (SequenceFeature sf : features)
      {
        String text = String.format("%s/%d-%d (%s)", sf.getType(),
                sf.getBegin(), sf.getEnd(), sf.getFeatureGroup());
        while (added.contains(text))
        {
          text += " ";
        }
        overlaps.addItem(text);
        added.add(text);
      }
      choosePanel.add(overlaps);

      overlaps.addItemListener(new ItemListener()
      {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
          int index = overlaps.getSelectedIndex();
          if (index != -1)
          {
            featureIndex = index;
            SequenceFeature sf = features.get(index);
            name.setText(sf.getType());
            description.setText(sf.getDescription());
            group.setText(sf.getFeatureGroup());
            start.setValue(Integer.valueOf(sf.getBegin()));
            end.setValue(Integer.valueOf(sf.getEnd()));

            SearchResultsI highlight = new SearchResults();
            highlight.addResult(sequences.get(0), sf.getBegin(),
                    sf.getEnd());

            alignPanel.getSeqPanel().seqCanvas.highlightSearchResults(
                    highlight, false);
          }
          FeatureColourI col = getFeatureStyle(name.getText());
          if (col == null)
          {
            col = new FeatureColour(
                    ColorUtils.createColourFromName(name.getText()));
          }
          oldcol = fcol = col;
          updateColourButton(mainPanel, colour, col);
        }
      });

      gridPanel.add(choosePanel);
    }

    JPanel namePanel = new JPanel();
    gridPanel.add(namePanel);
    namePanel.add(new JLabel(MessageManager.getString("label.name:"),
            JLabel.RIGHT));
    namePanel.add(name);

    JPanel groupPanel = new JPanel();
    gridPanel.add(groupPanel);
    groupPanel.add(new JLabel(MessageManager.getString("label.group:"),
            JLabel.RIGHT));
    groupPanel.add(group);

    JPanel colourPanel = new JPanel();
    gridPanel.add(colourPanel);
    colourPanel.add(new JLabel(MessageManager.getString("label.colour"),
            JLabel.RIGHT));
    colourPanel.add(colour);
    colour.setPreferredSize(new Dimension(150, 15));
    colour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 9));
    colour.setForeground(Color.black);
    colour.setHorizontalAlignment(SwingConstants.CENTER);
    colour.setVerticalAlignment(SwingConstants.CENTER);
    colour.setHorizontalTextPosition(SwingConstants.CENTER);
    colour.setVerticalTextPosition(SwingConstants.CENTER);
    mainPanel.add(gridPanel, BorderLayout.NORTH);

    JPanel descriptionPanel = new JPanel();
    descriptionPanel.add(new JLabel(
            MessageManager.getString("label.description:"), JLabel.RIGHT));
    description.setFont(JvSwingUtils.getTextAreaFont());
    description.setLineWrap(true);
    descriptionPanel.add(new JScrollPane(description));

    if (!create)
    {
      mainPanel.add(descriptionPanel, BorderLayout.SOUTH);

      JPanel startEndPanel = new JPanel();
      startEndPanel.add(new JLabel(MessageManager.getString("label.start"),
              JLabel.RIGHT));
      startEndPanel.add(start);
      startEndPanel.add(new JLabel(MessageManager.getString("label.end"),
              JLabel.RIGHT));
      startEndPanel.add(end);
      mainPanel.add(startEndPanel, BorderLayout.CENTER);
    }
    else
    {
      mainPanel.add(descriptionPanel, BorderLayout.CENTER);
    }

    /*
     * default feature type and group to that of the first feature supplied,
     * or to the last feature created if not supplied (null value) 
     */
    SequenceFeature firstFeature = features.get(0);
    boolean useLastDefaults = firstFeature.getType() == null;
    final String featureType = useLastDefaults ? lastFeatureAdded
            : firstFeature.getType();
    final String featureGroup = useLastDefaults ? lastFeatureGroupAdded
            : firstFeature.getFeatureGroup();
    name.setText(featureType);
    group.setText(featureGroup);

    start.setValue(Integer.valueOf(firstFeature.getBegin()));
    end.setValue(Integer.valueOf(firstFeature.getEnd()));
    description.setText(firstFeature.getDescription());
    updateColourButton(mainPanel, colour,
            (oldcol = fcol = getFeatureStyle(featureType)));
    Object[] options;
    if (!create)
    {
      options = new Object[] { MessageManager.getString("label.amend"),
          MessageManager.getString("action.delete"),
          MessageManager.getString("action.cancel") };
    }
    else
    {
      options = new Object[] { MessageManager.getString("action.ok"),
          MessageManager.getString("action.cancel") };
    }

    String title = create
            ? MessageManager.getString("label.create_new_sequence_features")
            : MessageManager.formatMessage("label.amend_delete_features",
                    new String[]
                    { sequences.get(0).getName() });

    /*
     * show the dialog
     */
    int reply = JvOptionPane.showInternalOptionDialog(Desktop.desktop,
            mainPanel, title, JvOptionPane.YES_NO_CANCEL_OPTION,
            JvOptionPane.QUESTION_MESSAGE, null, options,
            MessageManager.getString("action.ok"));

    FeaturesFile ffile = new FeaturesFile();

    final String enteredType = name.getText().trim();
    final String enteredGroup = group.getText().trim();
    final String enteredDescription = description.getText().replaceAll("\n", " ");

    if (reply == JvOptionPane.OK_OPTION && enteredType.length() > 0)
    {
      /*
       * update default values only if creating using default values
       */
      if (useLastDefaults)
      {
        lastFeatureAdded = enteredType;
        lastFeatureGroupAdded = enteredGroup;
        // TODO: determine if the null feature group is valid
        if (lastFeatureGroupAdded.length() < 1)
        {
          lastFeatureGroupAdded = null;
        }
      }
    }

    if (!create)
    {
      SequenceFeature sf = features.get(featureIndex);

      if (reply == JvOptionPane.NO_OPTION)
      {
        /*
         * NO_OPTION corresponds to the Delete button
         */
        sequences.get(0).getDatasetSequence().deleteFeature(sf);
        // update Feature Settings for removal of feature / group
        featuresAdded();
      }
      else if (reply == JvOptionPane.YES_OPTION)
      {
        /*
         * YES_OPTION corresponds to the Amend button
         * need to refresh Feature Settings if type, group or colour changed;
         * note we don't force the feature to be visible - the user has been
         * warned if a hidden feature type or group was entered
         */
        boolean refreshSettings = (!featureType.equals(enteredType) || !featureGroup
                .equals(enteredGroup));
        refreshSettings |= (fcol != oldcol);
        setColour(enteredType, fcol);
        int newBegin = sf.begin;
        int newEnd = sf.end;
        try
        {
          newBegin = ((Integer) start.getValue()).intValue();
          newEnd = ((Integer) end.getValue()).intValue();
        } catch (NumberFormatException ex)
        {
          // JSpinner doesn't accept invalid format data :-)
        }

        /*
         * replace the feature by deleting it and adding a new one
         * (to ensure integrity of SequenceFeatures data store)
         */
        sequences.get(0).deleteFeature(sf);
        SequenceFeature newSf = new SequenceFeature(sf, enteredType,
                newBegin, newEnd, enteredGroup, sf.getScore());
        newSf.setDescription(enteredDescription);
        ffile.parseDescriptionHTML(newSf, false);
        // amend features dialog only updates one sequence at a time
        sequences.get(0).addSequenceFeature(newSf);

        if (refreshSettings)
        {
          featuresAdded();
        }
      }
    }
    else
    // NEW FEATURES ADDED
    {
      if (reply == JvOptionPane.OK_OPTION && enteredType.length() > 0)
      {
        for (int i = 0; i < sequences.size(); i++)
        {
          SequenceFeature sf = features.get(i);
          SequenceFeature sf2 = new SequenceFeature(enteredType,
                  enteredDescription, sf.getBegin(), sf.getEnd(),
                  enteredGroup);
          ffile.parseDescriptionHTML(sf2, false);
          sequences.get(i).addSequenceFeature(sf2);
        }

        setColour(enteredType, fcol);

        featuresAdded();

        alignPanel.paintAlignment(true, true);

        return true;
      }
      else
      {
        return false;
      }
    }

    alignPanel.paintAlignment(true, true);

    return true;
  }

  /**
   * Show a warning message if the entered type is one that is currently hidden
   * 
   * @param panel
   * @param type
   */
  protected void warnIfTypeHidden(JPanel panel, String type)
  {
    if (getRenderOrder().contains(type))
    {
      if (!showFeatureOfType(type))
      {
        String msg = MessageManager.formatMessage("label.warning_hidden",
                MessageManager.getString("label.feature_type"), type);
        JvOptionPane.showMessageDialog(panel, msg, "",
                JvOptionPane.OK_OPTION);
      }
    }
  }

  /**
   * Show a warning message if the entered group is one that is currently hidden
   * 
   * @param panel
   * @param group
   */
  protected void warnIfGroupHidden(JPanel panel, String group)
  {
    if (featureGroups.containsKey(group) && !featureGroups.get(group))
    {
      String msg = MessageManager.formatMessage("label.warning_hidden",
              MessageManager.getString("label.group"), group);
      JvOptionPane.showMessageDialog(panel, msg, "",
              JvOptionPane.OK_OPTION);
    }
  }

  /**
   * update the amend feature button dependent on the given style
   * 
   * @param bigPanel
   * @param col
   * @param col
   */
  protected void updateColourButton(JPanel bigPanel, JLabel colour,
          FeatureColourI col)
  {
    colour.removeAll();
    colour.setIcon(null);
    colour.setToolTipText(null);
    colour.setText("");

    if (col.isSimpleColour())
    {
      colour.setBackground(col.getColour());
    }
    else
    {
      colour.setBackground(bigPanel.getBackground());
      colour.setForeground(Color.black);
      FeatureSettings.renderGraduatedColor(colour, col);
    }
  }

  /**
   * Orders features in render precedence (last in order is last to render, so
   * displayed on top of other features)
   * 
   * @param order
   */
  public void orderFeatures(Comparator<String> order)
  {
    Arrays.sort(renderOrder, order);
  }
}
