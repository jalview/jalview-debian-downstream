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

import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.GraphLine;
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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class AnnotationColourChooser extends AnnotationRowFilter
{
  private ColourSchemeI oldcs;

  private JButton defColours;

  private Hashtable<SequenceGroup, ColourSchemeI> oldgroupColours;

  private JCheckBox useOriginalColours = new JCheckBox();

  private JPanel minColour = new JPanel();

  private JPanel maxColour = new JPanel();

  private JCheckBox thresholdIsMin = new JCheckBox();

  protected static final int MIN_WIDTH = 500;

  protected static final int MIN_HEIGHT = 240;

  public AnnotationColourChooser(AlignViewport av, final AlignmentPanel ap)
  {
    super(av, ap);
    oldcs = av.getGlobalColourScheme();
    if (av.getAlignment().getGroups() != null)
    {
      oldgroupColours = new Hashtable<>();
      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.getColourScheme() != null)
        {
          oldgroupColours.put(sg, sg.getColourScheme());
        }
      }
    }
    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.colour_by_annotation"), 520,
            215);
    frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
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
      useOriginalColours.setSelected(
              acg.isPredefinedColours() || acg.getBaseColour() != null);
      if (!acg.isPredefinedColours() && acg.getBaseColour() == null)
      {
        minColour.setBackground(acg.getMinColour());
        maxColour.setBackground(acg.getMaxColour());
      }
      seqAssociated.setSelected(acg.isSeqAssociated());

    }
    Vector<String> annotItems = getAnnotationItems(
            seqAssociated.isSelected());
    annotations = new JComboBox<>(annotItems);

    populateThresholdComboBox(threshold);

    if (oldcs instanceof AnnotationColourGradient)
    {
      AnnotationColourGradient acg = (AnnotationColourGradient) oldcs;
      String label = getAnnotationMenuLabel(acg.getAnnotation());
      annotations.setSelectedItem(label);
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
        throw new Error(MessageManager.getString(
                "error.implementation_error_dont_know_about_threshold_setting"));
      }
      thresholdIsMin.setSelected(acg.isThresholdIsMinMax());
      thresholdValue
              .setText(String.valueOf(acg.getAnnotationThreshold()));
    }

    jbInit();
    adjusting = false;

    updateView();
    frame.invalidate();
    frame.pack();
  }

  @Override
  protected void jbInit()
  {
    super.jbInit();

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

    defColours = new JButton();
    defColours.setOpaque(false);
    defColours.setText(MessageManager.getString("action.set_defaults"));
    defColours.setToolTipText(MessageManager
            .getString("label.reset_min_max_colours_to_defaults"));
    defColours.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        resetColours_actionPerformed();
      }
    });

    useOriginalColours.setFont(JvSwingUtils.getLabelFont());
    useOriginalColours.setOpaque(false);
    useOriginalColours.setText(
            MessageManager.getString("label.use_original_colours"));
    useOriginalColours.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        originalColours_actionPerformed();
      }
    });
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setFont(JvSwingUtils.getLabelFont());
    thresholdIsMin
            .setText(MessageManager.getString("label.threshold_minmax"));
    thresholdIsMin.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        thresholdIsMin_actionPerformed();
      }
    });
    seqAssociated.setBackground(Color.white);
    seqAssociated.setFont(JvSwingUtils.getLabelFont());
    seqAssociated
            .setText(MessageManager.getString("label.per_sequence_only"));
    seqAssociated.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        seqAssociated_actionPerformed(annotations);
      }
    });

    this.setLayout(new BorderLayout());
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    jPanel2.setLayout(new MigLayout("", "[left][center][right]", "[][][]"));
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);

    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel2.add(annotations, "grow, wrap");
    jPanel2.add(seqAssociated);
    jPanel2.add(useOriginalColours);
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

  protected void resetColours_actionPerformed()
  {
    setDefaultMinMax();
    updateView();
  }

  private void setDefaultMinMax()
  {
    minColour.setBackground(
            Cache.getDefaultColour("ANNOTATIONCOLOUR_MIN", Color.orange));
    maxColour.setBackground(
            Cache.getDefaultColour("ANNOTATIONCOLOUR_MAX", Color.red));
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

  @Override
  public void reset()
  {
    this.ap.alignFrame.changeColour(oldcs);
    if (av.getAlignment().getGroups() != null)
    {

      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        sg.setColourScheme(oldgroupColours.get(sg));
      }
    }
  }

  @Override
  public void valueChanged(boolean updateAllAnnotation)
  {
    if (slider.isEnabled())
    {
      if (useOriginalColours.isSelected() && !(av
              .getGlobalColourScheme() instanceof AnnotationColourGradient))
      {
        updateView();
      }
      getCurrentAnnotation().threshold.value = getSliderValue();
      propagateSeqAssociatedThreshold(updateAllAnnotation,
              getCurrentAnnotation());
      ap.paintAlignment(false, false);
    }
  }

  public void originalColours_actionPerformed()
  {
    boolean selected = useOriginalColours.isSelected();
    if (selected)
    {
      reset();
    }
    maxColour.setEnabled(!selected);
    minColour.setEnabled(!selected);
    thresholdIsMin.setEnabled(!selected);
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

    setCurrentAnnotation(
            av.getAlignment().getAlignmentAnnotation()[annmap[annotations
                    .getSelectedIndex()]]);

    int selectedThresholdItem = getSelectedThresholdItem(
            getThreshold().getSelectedIndex());

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    thresholdIsMin.setEnabled(!useOriginalColours.isSelected());

    final AlignmentAnnotation currentAnnotation = getCurrentAnnotation();
    if (selectedThresholdItem == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
      thresholdIsMin.setEnabled(false);
    }
    else if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD
            && currentAnnotation.threshold == null)
    {
      currentAnnotation.setThreshold(new GraphLine(
              (currentAnnotation.graphMax - currentAnnotation.graphMin)
                      / 2f,
              "Threshold", Color.black));
    }

    if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD)
    {
      adjusting = true;
      setSliderModel(currentAnnotation.graphMin, currentAnnotation.graphMax,
              currentAnnotation.threshold.value);
      slider.setEnabled(true);

      setThresholdValueText();
      thresholdValue.setEnabled(true);
      adjusting = false;
    }
    colorAlignmentContaining(currentAnnotation, selectedThresholdItem);

    ap.alignmentChanged();
  }

  protected void colorAlignmentContaining(AlignmentAnnotation currentAnn,
          int selectedThresholdOption)
  {

    AnnotationColourGradient acg = null;
    if (useOriginalColours.isSelected())
    {
      acg = new AnnotationColourGradient(currentAnn,
              av.getGlobalColourScheme(), selectedThresholdOption);
    }
    else
    {
      acg = new AnnotationColourGradient(currentAnn,
              minColour.getBackground(), maxColour.getBackground(),
              selectedThresholdOption);
    }
    acg.setSeqAssociated(seqAssociated.isSelected());

    if (currentAnn.graphMin == 0f && currentAnn.graphMax == 0f)
    {
      acg.setPredefinedColours(true);
    }

    acg.setThresholdIsMinMax(thresholdIsMin.isSelected());

    this.ap.alignFrame.changeColour(acg);

    if (av.getAlignment().getGroups() != null)
    {

      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.cs == null)
        {
          continue;
        }
        sg.setColourScheme(
                acg.getInstance(av, sg));
      }
    }
  }

  @Override
  protected void sliderDragReleased()
  {
    super.sliderDragReleased();
    ap.paintAlignment(true, true);
  }
}
