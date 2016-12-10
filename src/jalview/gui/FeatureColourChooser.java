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
package jalview.gui;

import jalview.api.FeatureColourI;
import jalview.datamodel.GraphLine;
import jalview.schemes.FeatureColour;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FeatureColourChooser extends JalviewDialog
{
  // FeatureSettings fs;
  FeatureRenderer fr;

  private FeatureColourI cs;

  private FeatureColourI oldcs;

  /**
   * 
   * @return the last colour setting selected by user - either oldcs (which may
   *         be a java.awt.Color) or the new GraduatedColor
   */
  public FeatureColourI getLastColour()
  {
    if (cs == null)
    {
      return oldcs;
    }
    return cs;
  }

  AlignmentPanel ap;

  boolean adjusting = false;

  final private float min;

  final private float max;

  final private float scaleFactor;

  String type = null;

  public FeatureColourChooser(FeatureRenderer frender, String type)
  {
    this(frender, false, type);
  }

  public FeatureColourChooser(FeatureRenderer frender, boolean block,
          String type)
  {
    this.fr = frender;
    this.type = type;
    ap = fr.ap;
    String title = MessageManager.formatMessage(
            "label.graduated_color_for_params", new String[] { type });
    initDialogFrame(this, true, block, title, 480, 185);
    // frame.setLayer(JLayeredPane.PALETTE_LAYER);
    // Desktop.addInternalFrame(frame, "Graduated Feature Colour for "+type,
    // 480, 145);

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        if (!adjusting)
        {
          thresholdValue.setText((slider.getValue() / scaleFactor) + "");
          valueChanged();
        }
      }
    });
    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseReleased(MouseEvent evt)
      {
        if (ap != null)
        {
          ap.paintAlignment(true);
        }
        ;
      }
    });

    float mm[] = fr.getMinMax().get(type)[0];
    min = mm[0];
    max = mm[1];

    /*
     * ensure scale factor allows a scaled range with
     * 10 integer divisions ('ticks'); if we have got here,
     * we should expect that max != min
     */
    scaleFactor = (max == min) ? 1f : 100f / (max - min);

    oldcs = fr.getFeatureColours().get(type);
    if (!oldcs.isSimpleColour())
    {
      if (oldcs.isAutoScaled())
      {
        // update the scale
        cs = new FeatureColour((FeatureColour) oldcs, min, max);
      }
      else
      {
        cs = new FeatureColour((FeatureColour) oldcs);
      }
    }
    else
    {
      // promote original color to a graduated color
      Color bl = oldcs.getColour();
      if (bl == null)
      {
        bl = Color.BLACK;
      }
      // original colour becomes the maximum colour
      cs = new FeatureColour(Color.white, bl, mm[0], mm[1]);
      cs.setColourByLabel(false);
    }
    minColour.setBackground(oldminColour = cs.getMinColour());
    maxColour.setBackground(oldmaxColour = cs.getMaxColour());
    adjusting = true;

    try
    {
      jbInit();
    } catch (Exception ex)
    {
    }
    // update the gui from threshold state
    thresholdIsMin.setSelected(!cs.isAutoScaled());
    colourByLabel.setSelected(cs.isColourByLabel());
    if (cs.hasThreshold())
    {
      // initialise threshold slider and selector
      threshold.setSelectedIndex(cs.isAboveThreshold() ? 1 : 2);
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      threshline = new GraphLine((max - min) / 2f, "Threshold", Color.black);

    }

    adjusting = false;

    changeColour();
    waitForInput();
  }

  private void jbInit() throws Exception
  {

    minColour.setFont(JvSwingUtils.getLabelFont());
    minColour.setBorder(BorderFactory.createLineBorder(Color.black));
    minColour.setPreferredSize(new Dimension(40, 20));
    minColour.setToolTipText(MessageManager.getString("label.min_colour"));
    minColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (minColour.isEnabled())
        {
          minColour_actionPerformed();
        }
      }
    });
    maxColour.setFont(JvSwingUtils.getLabelFont());
    maxColour.setBorder(BorderFactory.createLineBorder(Color.black));
    maxColour.setPreferredSize(new Dimension(40, 20));
    maxColour.setToolTipText(MessageManager.getString("label.max_colour"));
    maxColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (maxColour.isEnabled())
        {
          maxColour_actionPerformed();
        }
      }
    });
    maxColour.setBorder(new LineBorder(Color.black));
    minText.setText(MessageManager.getString("label.min"));
    minText.setFont(JvSwingUtils.getLabelFont());
    maxText.setText(MessageManager.getString("label.max"));
    maxText.setFont(JvSwingUtils.getLabelFont());
    this.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);
    threshold.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        threshold_actionPerformed(e);
      }
    });
    threshold.setToolTipText(MessageManager
            .getString("label.threshold_feature_display_by_score"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold")); // index 0
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold")); // index 1
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold")); // index 2
    jPanel3.setLayout(flowLayout2);
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
    slider.setToolTipText(MessageManager
            .getString("label.adjust_threshold"));
    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);
    jPanel3.setBackground(Color.white);
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setText(MessageManager
            .getString("label.threshold_minmax"));
    thresholdIsMin.setToolTipText(MessageManager
            .getString("label.toggle_absolute_relative_display_threshold"));
    thresholdIsMin.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        thresholdIsMin_actionPerformed(actionEvent);
      }
    });
    colourByLabel.setBackground(Color.white);
    colourByLabel
            .setText(MessageManager.getString("label.colour_by_label"));
    colourByLabel
            .setToolTipText(MessageManager
                    .getString("label.display_features_same_type_different_label_using_different_colour"));
    colourByLabel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        colourByLabel_actionPerformed(actionEvent);
      }
    });
    colourPanel.setBackground(Color.white);
    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel2.add(colourByLabel, java.awt.BorderLayout.WEST);
    jPanel2.add(colourPanel, java.awt.BorderLayout.EAST);
    colourPanel.add(minText);
    colourPanel.add(minColour);
    colourPanel.add(maxText);
    colourPanel.add(maxColour);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);
    jPanel3.add(threshold);
    jPanel3.add(slider);
    jPanel3.add(thresholdValue);
    jPanel3.add(thresholdIsMin);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel2, java.awt.BorderLayout.NORTH);
  }

  JLabel minText = new JLabel();

  JLabel maxText = new JLabel();

  JPanel minColour = new JPanel();

  JPanel maxColour = new JPanel();

  JPanel colourPanel = new JPanel();

  JPanel jPanel1 = new JPanel();

  JPanel jPanel2 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  JComboBox threshold = new JComboBox();

  FlowLayout flowLayout1 = new FlowLayout();

  JPanel jPanel3 = new JPanel();

  FlowLayout flowLayout2 = new FlowLayout();

  JSlider slider = new JSlider();

  JTextField thresholdValue = new JTextField(20);

  // TODO implement GUI for tolower flag
  // JCheckBox toLower = new JCheckBox();

  JCheckBox thresholdIsMin = new JCheckBox();

  JCheckBox colourByLabel = new JCheckBox();

  private GraphLine threshline;

  private Color oldmaxColour;

  private Color oldminColour;

  public void minColour_actionPerformed()
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_colour_minimum_value"),
            minColour.getBackground());
    if (col != null)
    {
      minColour.setBackground(col);
      minColour.setForeground(col);
    }
    minColour.repaint();
    changeColour();
  }

  public void maxColour_actionPerformed()
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_colour_maximum_value"),
            maxColour.getBackground());
    if (col != null)
    {
      maxColour.setBackground(col);
      maxColour.setForeground(col);
    }
    maxColour.repaint();
    changeColour();
  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    boolean aboveThreshold = false;
    boolean belowThreshold = false;
    if (threshold.getSelectedIndex() == 1)
    {
      aboveThreshold = true;
    }
    else if (threshold.getSelectedIndex() == 2)
    {
      belowThreshold = true;
    }
    boolean hasThreshold = aboveThreshold || belowThreshold;

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);

    FeatureColourI acg;
    if (cs.isColourByLabel())
    {
      acg = new FeatureColour(oldminColour, oldmaxColour, min, max);
    }
    else
    {
      acg = new FeatureColour(oldminColour = minColour.getBackground(),
              oldmaxColour = maxColour.getBackground(), min, max);

    }

    if (!hasThreshold)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
      thresholdIsMin.setEnabled(false);
    }
    else if (threshline == null)
    {
      // todo visual indication of feature threshold
      threshline = new GraphLine((max - min) / 2f, "Threshold", Color.black);
    }

    if (hasThreshold)
    {
      adjusting = true;
      acg.setThreshold(threshline.value);

      float range = (max - min) * scaleFactor;

      slider.setMinimum((int) (min * scaleFactor));
      slider.setMaximum((int) (max * scaleFactor));
      slider.setValue((int) (threshline.value * scaleFactor));
      thresholdValue.setText(threshline.value + "");
      slider.setMajorTickSpacing((int) (range / 10f));
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      thresholdIsMin.setEnabled(!colourByLabel.isSelected());
      adjusting = false;
    }

    acg.setAboveThreshold(aboveThreshold);
    acg.setBelowThreshold(belowThreshold);
    if (thresholdIsMin.isSelected() && hasThreshold)
    {
      acg.setAutoScaled(false);
      if (aboveThreshold)
      {
        acg = new FeatureColour((FeatureColour) acg, threshline.value, max);
      }
      else
      {
        acg = new FeatureColour((FeatureColour) acg, min, threshline.value);
      }
    }
    else
    {
      acg.setAutoScaled(true);
    }
    acg.setColourByLabel(colourByLabel.isSelected());
    if (acg.isColourByLabel())
    {
      maxColour.setEnabled(false);
      minColour.setEnabled(false);
      maxColour.setBackground(this.getBackground());
      maxColour.setForeground(this.getBackground());
      minColour.setBackground(this.getBackground());
      minColour.setForeground(this.getBackground());

    }
    else
    {
      maxColour.setEnabled(true);
      minColour.setEnabled(true);
      maxColour.setBackground(oldmaxColour);
      minColour.setBackground(oldminColour);
      maxColour.setForeground(oldmaxColour);
      minColour.setForeground(oldminColour);
    }
    fr.setColour(type, acg);
    cs = acg;
    ap.paintAlignment(false);
  }

  @Override
  protected void raiseClosed()
  {
    if (this.colourEditor != null)
    {
      colourEditor.actionPerformed(new ActionEvent(this, 0, "CLOSED"));
    }
  }

  @Override
  public void okPressed()
  {
    changeColour();
  }

  @Override
  public void cancelPressed()
  {
    reset();
  }

  void reset()
  {
    fr.setColour(type, oldcs);
    ap.paintAlignment(false);
    cs = null;
  }

  public void thresholdCheck_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

  public void annotations_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

  public void threshold_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

  public void thresholdValue_actionPerformed(ActionEvent e)
  {
    try
    {
      float f = Float.parseFloat(thresholdValue.getText());
      slider.setValue((int) (f * scaleFactor));
      threshline.value = f;
    } catch (NumberFormatException ex)
    {
    }
  }

  public void valueChanged()
  {
    threshline.value = slider.getValue() / scaleFactor;
    cs.setThreshold(threshline.value);
    changeColour();
    ap.paintAlignment(false);
  }

  public void thresholdIsMin_actionPerformed(ActionEvent actionEvent)
  {
    changeColour();
  }

  public void colourByLabel_actionPerformed(ActionEvent actionEvent)
  {
    changeColour();
  }

  ActionListener colourEditor = null;

  public void addActionListener(ActionListener graduatedColorEditor)
  {
    if (colourEditor != null)
    {
      System.err
              .println("IMPLEMENTATION ISSUE: overwriting action listener for FeatureColourChooser");
    }
    colourEditor = graduatedColorEditor;
  }

}
