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

import jalview.bin.Cache;
import jalview.datamodel.SequenceGroup;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class AnnotationColourChooser extends AnnotationRowFilter
{

  ColourSchemeI oldcs;

  Hashtable<SequenceGroup, ColourSchemeI> oldgroupColours;

  /**
   * enabled if the user is dragging the slider - try to keep updates to a
   * minimun
   */

  JComboBox<String> annotations;

  JButton defColours = new JButton();

  JPanel jPanel1 = new JPanel();

  JPanel jPanel2 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  private JComboBox<String> threshold = new JComboBox<String>();

  public AnnotationColourChooser(AlignViewport av, final AlignmentPanel ap)
  {
    super(av, ap);
    oldcs = av.getGlobalColourScheme();
    if (av.getAlignment().getGroups() != null)
    {
      oldgroupColours = new Hashtable<SequenceGroup, ColourSchemeI>();
      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.cs != null)
        {
          oldgroupColours.put(sg, sg.cs);
        }
      }
    }
    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.colour_by_annotation"), 520,
            215);

    addSliderChangeListener();
    addSliderMouseListeners();

    if (av.getAlignment().getAlignmentAnnotation() == null)
    {
      return;
    }

    // Always get default shading from preferences.
    setDefaultMinMax();

    adjusting = true;
    if (oldcs instanceof AnnotationColourGradient)
    {
      AnnotationColourGradient acg = (AnnotationColourGradient) oldcs;
      currentColours.setSelected(acg.isPredefinedColours()
              || acg.getBaseColour() != null);
      if (!acg.isPredefinedColours() && acg.getBaseColour() == null)
      {
        minColour.setBackground(acg.getMinColour());
        maxColour.setBackground(acg.getMaxColour());
      }
      seqAssociated.setSelected(acg.isSeqAssociated());

    }
    annotations = new JComboBox<String>(
            getAnnotationItems(seqAssociated.isSelected()));

    populateThresholdComboBox(threshold);

    if (oldcs instanceof AnnotationColourGradient)
    {
      AnnotationColourGradient acg = (AnnotationColourGradient) oldcs;
      annotations.setSelectedItem(acg.getAnnotation());
      switch (acg.getAboveThreshold())
      {
      case AnnotationColourGradient.NO_THRESHOLD:
        getThreshold().setSelectedIndex(0);
        break;
      case AnnotationColourGradient.ABOVE_THRESHOLD:
        getThreshold().setSelectedIndex(1);
        break;
      case AnnotationColourGradient.BELOW_THRESHOLD:
        getThreshold().setSelectedIndex(2);
        break;
      default:
        throw new Error(
                MessageManager
                        .getString("error.implementation_error_dont_know_about_thereshold_setting"));
      }
      thresholdIsMin.setSelected(acg.thresholdIsMinMax);
      thresholdValue.setText("" + acg.getAnnotationThreshold());
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

  public AnnotationColourChooser()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    minColour.setFont(JvSwingUtils.getLabelFont());
    minColour.setBorder(BorderFactory.createEtchedBorder());
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
    maxColour.setBorder(BorderFactory.createEtchedBorder());
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
    defColours.setOpaque(false);
    defColours.setText(MessageManager.getString("action.set_defaults"));
    defColours.setToolTipText(MessageManager
            .getString("label.reset_min_max_colours_to_defaults"));
    defColours.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        resetColours_actionPerformed(arg0);
      }
    });

    annotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    getThreshold().addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        threshold_actionPerformed(e);
      }
    });
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
    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);
    currentColours.setFont(JvSwingUtils.getLabelFont());
    currentColours.setOpaque(false);
    currentColours.setText(MessageManager
            .getString("label.use_original_colours"));
    currentColours.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        currentColours_actionPerformed(e);
      }
    });
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setFont(JvSwingUtils.getLabelFont());
    thresholdIsMin.setText(MessageManager
            .getString("label.threshold_minmax"));
    thresholdIsMin.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        thresholdIsMin_actionPerformed(actionEvent);
      }
    });
    seqAssociated.setBackground(Color.white);
    seqAssociated.setFont(JvSwingUtils.getLabelFont());
    seqAssociated.setText(MessageManager
            .getString("label.per_sequence_only"));
    seqAssociated.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        seqAssociated_actionPerformed(arg0, annotations, seqAssociated);
      }
    });

    this.setLayout(borderLayout1);
    jPanel2.setLayout(new MigLayout("", "[left][center][right]", "[][][]"));
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);

    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel2.add(annotations, "grow, wrap");
    jPanel2.add(seqAssociated);
    jPanel2.add(currentColours);
    JPanel colpanel = new JPanel(new FlowLayout());
    colpanel.setBackground(Color.white);
    colpanel.add(minColour);
    colpanel.add(maxColour);
    jPanel2.add(colpanel, "wrap");
    jPanel2.add(getThreshold());
    jPanel2.add(defColours, "skip 1, wrap");
    jPanel2.add(thresholdIsMin);
    jPanel2.add(slider, "grow");
    jPanel2.add(thresholdValue, "grow");
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel2, java.awt.BorderLayout.CENTER);
    this.validate();
  }

  protected void resetColours_actionPerformed(ActionEvent arg0)
  {
    setDefaultMinMax();
    updateView();
  }

  private void setDefaultMinMax()
  {
    minColour.setBackground(Cache.getDefaultColour("ANNOTATIONCOLOUR_MIN",
            Color.orange));
    maxColour.setBackground(Cache.getDefaultColour("ANNOTATIONCOLOUR_MAX",
            Color.red));
  }

  public void minColour_actionPerformed()
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_colour_minimum_value"),
            minColour.getBackground());
    if (col != null)
    {
      minColour.setBackground(col);
    }
    minColour.repaint();
    updateView();
  }

  public void maxColour_actionPerformed()
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_colour_maximum_value"),
            maxColour.getBackground());
    if (col != null)
    {
      maxColour.setBackground(col);
    }
    maxColour.repaint();
    updateView();
  }

  public void reset()
  {
    av.setGlobalColourScheme(oldcs);
    if (av.getAlignment().getGroups() != null)
    {

      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        sg.cs = oldgroupColours.get(sg);
      }
    }
  }

  public void valueChanged(boolean updateAllAnnotation)
  {
    if (slider.isEnabled())
    {
      if (currentColours.isSelected()
              && !(av.getGlobalColourScheme() instanceof AnnotationColourGradient))
      {
        updateView();
      }
      getCurrentAnnotation().threshold.value = slider.getValue() / 1000f;
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

  public void currentColours_actionPerformed(ActionEvent e)
  {
    if (currentColours.isSelected())
    {
      reset();
    }
    maxColour.setEnabled(!currentColours.isSelected());
    minColour.setEnabled(!currentColours.isSelected());
    updateView();
  }

  @Override
  public void updateView()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    setCurrentAnnotation(av.getAlignment().getAlignmentAnnotation()[annmap[annotations
            .getSelectedIndex()]]);

    int selectedThresholdItem = getSelectedThresholdItem(getThreshold()
            .getSelectedIndex());

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    thresholdIsMin.setEnabled(true);

    if (selectedThresholdItem == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
      thresholdIsMin.setEnabled(false);
    }
    else if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD
            && getCurrentAnnotation().threshold == null)
    {
      getCurrentAnnotation()
              .setThreshold(
                      new jalview.datamodel.GraphLine(
                              (getCurrentAnnotation().graphMax - getCurrentAnnotation().graphMin) / 2f,
                              "Threshold", Color.black));
    }

    if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD)
    {
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
    }
    colorAlignmContaining(getCurrentAnnotation(), selectedThresholdItem);

    ap.alignmentChanged();
    // ensure all associated views (overviews, structures, etc) are notified of
    // updated colours.
    ap.paintAlignment(true);
  }

}
