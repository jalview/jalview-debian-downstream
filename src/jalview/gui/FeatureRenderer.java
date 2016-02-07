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
import jalview.schemes.GraduatedColor;
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

import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FeatureRenderer extends
        jalview.renderer.seqfeatures.FeatureRenderer implements
        jalview.api.FeatureRenderer
{
  Color resBoxColour;

  AlignmentPanel ap;

  /**
   * Creates a new FeatureRenderer object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public FeatureRenderer(AlignmentPanel ap)
  {
    super();
    this.ap = ap;
    this.av = ap.av;
    if (ap != null && ap.getSeqPanel() != null
            && ap.getSeqPanel().seqCanvas != null
            && ap.getSeqPanel().seqCanvas.fr != null)
    {
      transferSettings(ap.getSeqPanel().seqCanvas.fr);
    }
  }

  // // /////////////
  // // Feature Editing Dialog
  // // Will be refactored in next release.

  static String lastFeatureAdded;

  static String lastFeatureGroupAdded;

  static String lastDescriptionAdded;

  Object oldcol, fcol;

  int featureIndex = 0;

  boolean amendFeatures(final SequenceI[] sequences,
          final SequenceFeature[] features, boolean newFeatures,
          final AlignmentPanel ap)
  {

    featureIndex = 0;

    final JPanel bigPanel = new JPanel(new BorderLayout());
    final JComboBox overlaps;
    final JTextField name = new JTextField(25);
    final JTextField source = new JTextField(25);
    final JTextArea description = new JTextArea(3, 25);
    final JSpinner start = new JSpinner();
    final JSpinner end = new JSpinner();
    start.setPreferredSize(new Dimension(80, 20));
    end.setPreferredSize(new Dimension(80, 20));
    final FeatureRenderer me = this;
    final JLabel colour = new JLabel();
    colour.setOpaque(true);
    // colour.setBorder(BorderFactory.createEtchedBorder());
    colour.setMaximumSize(new Dimension(30, 16));
    colour.addMouseListener(new MouseAdapter()
    {
      FeatureColourChooser fcc = null;

      public void mousePressed(MouseEvent evt)
      {
        if (fcol instanceof Color)
        {
          Color col = JColorChooser.showDialog(Desktop.desktop,
                  MessageManager.getString("label.select_feature_colour"),
                  ((Color) fcol));
          if (col != null)
          {
            fcol = col;
            updateColourButton(bigPanel, colour, col);
          }
        }
        else
        {

          if (fcc == null)
          {
            final String type = features[featureIndex].getType();
            fcc = new FeatureColourChooser(me, type);
            fcc.setRequestFocusEnabled(true);
            fcc.requestFocus();

            fcc.addActionListener(new ActionListener()
            {

              public void actionPerformed(ActionEvent e)
              {
                fcol = fcc.getLastColour();
                fcc = null;
                setColour(type, fcol);
                updateColourButton(bigPanel, colour, fcol);
              }
            });

          }
        }
      }
    });
    JPanel tmp = new JPanel();
    JPanel panel = new JPanel(new GridLayout(3, 1));

    // /////////////////////////////////////
    // /MULTIPLE FEATURES AT SELECTED RESIDUE
    if (!newFeatures && features.length > 1)
    {
      panel = new JPanel(new GridLayout(4, 1));
      tmp = new JPanel();
      tmp.add(new JLabel(MessageManager.getString("label.select_feature")));
      overlaps = new JComboBox();
      for (int i = 0; i < features.length; i++)
      {
        overlaps.addItem(features[i].getType() + "/"
                + features[i].getBegin() + "-" + features[i].getEnd()
                + " (" + features[i].getFeatureGroup() + ")");
      }

      tmp.add(overlaps);

      overlaps.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          int index = overlaps.getSelectedIndex();
          if (index != -1)
          {
            featureIndex = index;
            name.setText(features[index].getType());
            description.setText(features[index].getDescription());
            source.setText(features[index].getFeatureGroup());
            start.setValue(new Integer(features[index].getBegin()));
            end.setValue(new Integer(features[index].getEnd()));

            SearchResults highlight = new SearchResults();
            highlight.addResult(sequences[0], features[index].getBegin(),
                    features[index].getEnd());

            ap.getSeqPanel().seqCanvas.highlightSearchResults(highlight);

          }
          Object col = getFeatureStyle(name.getText());
          if (col == null)
          {
            col = new jalview.schemes.UserColourScheme()
                    .createColourFromName(name.getText());
          }
          oldcol = fcol = col;
          updateColourButton(bigPanel, colour, col);
        }
      });

      panel.add(tmp);
    }
    // ////////
    // ////////////////////////////////////

    tmp = new JPanel();
    panel.add(tmp);
    tmp.add(new JLabel(MessageManager.getString("label.name"), JLabel.RIGHT));
    tmp.add(name);

    tmp = new JPanel();
    panel.add(tmp);
    tmp.add(new JLabel(MessageManager.getString("label.group") + ":",
            JLabel.RIGHT));
    tmp.add(source);

    tmp = new JPanel();
    panel.add(tmp);
    tmp.add(new JLabel(MessageManager.getString("label.colour"),
            JLabel.RIGHT));
    tmp.add(colour);
    colour.setPreferredSize(new Dimension(150, 15));
    colour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 9));
    colour.setForeground(Color.black);
    colour.setHorizontalAlignment(SwingConstants.CENTER);
    colour.setVerticalAlignment(SwingConstants.CENTER);
    colour.setHorizontalTextPosition(SwingConstants.CENTER);
    colour.setVerticalTextPosition(SwingConstants.CENTER);
    bigPanel.add(panel, BorderLayout.NORTH);

    panel = new JPanel();
    panel.add(new JLabel(MessageManager.getString("label.description"),
            JLabel.RIGHT));
    description.setFont(JvSwingUtils.getTextAreaFont());
    description.setLineWrap(true);
    panel.add(new JScrollPane(description));

    if (!newFeatures)
    {
      bigPanel.add(panel, BorderLayout.SOUTH);

      panel = new JPanel();
      panel.add(new JLabel(MessageManager.getString("label.start"),
              JLabel.RIGHT));
      panel.add(start);
      panel.add(new JLabel(MessageManager.getString("label.end"),
              JLabel.RIGHT));
      panel.add(end);
      bigPanel.add(panel, BorderLayout.CENTER);
    }
    else
    {
      bigPanel.add(panel, BorderLayout.CENTER);
    }

    if (lastFeatureAdded == null)
    {
      if (features[0].type != null)
      {
        lastFeatureAdded = features[0].type;
      }
      else
      {
        lastFeatureAdded = "feature_1";
      }
    }

    if (lastFeatureGroupAdded == null)
    {
      if (features[0].featureGroup != null)
      {
        lastFeatureGroupAdded = features[0].featureGroup;
      }
      else
      {
        lastFeatureGroupAdded = "Jalview";
      }
    }

    if (newFeatures)
    {
      name.setText(lastFeatureAdded);
      source.setText(lastFeatureGroupAdded);
    }
    else
    {
      name.setText(features[0].getType());
      source.setText(features[0].getFeatureGroup());
    }

    start.setValue(new Integer(features[0].getBegin()));
    end.setValue(new Integer(features[0].getEnd()));
    description.setText(features[0].getDescription());
    updateColourButton(bigPanel, colour,
            (oldcol = fcol = getFeatureStyle(name.getText())));
    Object[] options;
    if (!newFeatures)
    {
      options = new Object[] { "Amend", "Delete", "Cancel" };
    }
    else
    {
      options = new Object[] { "OK", "Cancel" };
    }

    String title = newFeatures ? MessageManager
            .getString("label.create_new_sequence_features")
            : MessageManager.formatMessage("label.amend_delete_features",
                    new String[] { sequences[0].getName() });

    int reply = JOptionPane.showInternalOptionDialog(Desktop.desktop,
            bigPanel, title, JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options,
            MessageManager.getString("action.ok"));

    jalview.io.FeaturesFile ffile = new jalview.io.FeaturesFile();

    if (reply == JOptionPane.OK_OPTION && name.getText().length() > 0)
    {
      // This ensures that the last sequence
      // is refreshed and new features are rendered
      lastSeq = null;
      lastFeatureAdded = name.getText().trim();
      lastFeatureGroupAdded = source.getText().trim();
      lastDescriptionAdded = description.getText().replaceAll("\n", " ");
      // TODO: determine if the null feature group is valid
      if (lastFeatureGroupAdded.length() < 1)
      {
        lastFeatureGroupAdded = null;
      }
    }

    if (!newFeatures)
    {
      SequenceFeature sf = features[featureIndex];

      if (reply == JOptionPane.NO_OPTION)
      {
        sequences[0].getDatasetSequence().deleteFeature(sf);
      }
      else if (reply == JOptionPane.YES_OPTION)
      {
        sf.type = lastFeatureAdded;
        sf.featureGroup = lastFeatureGroupAdded;
        sf.description = lastDescriptionAdded;

        setColour(sf.type, fcol);
        getFeaturesDisplayed().setVisible(sf.type);

        try
        {
          sf.begin = ((Integer) start.getValue()).intValue();
          sf.end = ((Integer) end.getValue()).intValue();
        } catch (NumberFormatException ex)
        {
        }

        ffile.parseDescriptionHTML(sf, false);
      }
    }
    else
    // NEW FEATURES ADDED
    {
      if (reply == JOptionPane.OK_OPTION && lastFeatureAdded.length() > 0)
      {
        for (int i = 0; i < sequences.length; i++)
        {
          features[i].type = lastFeatureAdded;
          // fix for JAL-1538 - always set feature group here
          features[i].featureGroup = lastFeatureGroupAdded;
          features[i].description = lastDescriptionAdded;
          sequences[i].addSequenceFeature(features[i]);
          ffile.parseDescriptionHTML(features[i], false);
        }

        if (lastFeatureGroupAdded != null)
        {
          setGroupVisibility(lastFeatureGroupAdded, true);
        }
        setColour(lastFeatureAdded, fcol);
        setVisible(lastFeatureAdded);

        findAllFeatures(false);

        ap.paintAlignment(true);

        return true;
      }
      else
      {
        return false;
      }
    }

    ap.paintAlignment(true);

    return true;
  }

  /**
   * update the amend feature button dependent on the given style
   * 
   * @param bigPanel
   * @param col
   * @param col2
   */
  protected void updateColourButton(JPanel bigPanel, JLabel colour,
          Object col2)
  {
    colour.removeAll();
    colour.setIcon(null);
    colour.setToolTipText(null);
    colour.setText("");

    if (col2 instanceof Color)
    {
      colour.setBackground((Color) col2);
    }
    else
    {
      colour.setBackground(bigPanel.getBackground());
      colour.setForeground(Color.black);
      FeatureSettings.renderGraduatedColor(colour, (GraduatedColor) col2);
      // colour.setForeground(colour.getBackground());
    }
  }
}
