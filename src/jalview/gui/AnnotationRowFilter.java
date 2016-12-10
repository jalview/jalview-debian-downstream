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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.GraphLine;
import jalview.datamodel.SequenceGroup;
import jalview.schemes.AnnotationColourGradient;
import jalview.util.MessageManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public abstract class AnnotationRowFilter extends JPanel
{
  protected AlignViewport av;

  protected AlignmentPanel ap;

  protected int[] annmap;

  protected boolean enableSeqAss = false;

  private AlignmentAnnotation currentAnnotation;

  protected boolean adjusting = false;

  protected JCheckBox currentColours = new JCheckBox();

  protected JPanel minColour = new JPanel();

  protected JPanel maxColour = new JPanel();

  protected JCheckBox seqAssociated = new JCheckBox();

  protected JCheckBox thresholdIsMin = new JCheckBox();

  protected JSlider slider = new JSlider();

  protected JTextField thresholdValue = new JTextField(20);

  protected JInternalFrame frame;

  protected JButton ok = new JButton();

  protected JButton cancel = new JButton();

  /**
   * enabled if the user is dragging the slider - try to keep updates to a
   * minimun
   */
  protected boolean sliderDragging = false;

  protected void addSliderChangeListener()
  {

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        if (!adjusting)
        {
          thresholdValue.setText((slider.getValue() / 1000f) + "");
          valueChanged(!sliderDragging);
        }
      }
    });
  }

  protected void addSliderMouseListeners()
  {

    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        sliderDragging = true;
        super.mousePressed(e);
      }

      @Override
      public void mouseDragged(MouseEvent e)
      {
        sliderDragging = true;
        super.mouseDragged(e);
      }

      @Override
      public void mouseReleased(MouseEvent evt)
      {
        if (sliderDragging)
        {
          sliderDragging = false;
          valueChanged(true);
        }
        ap.paintAlignment(true);
      }
    });
  }

  public AnnotationRowFilter(AlignViewport av, final AlignmentPanel ap)
  {
    this.av = av;
    this.ap = ap;
  }

  public AnnotationRowFilter()
  {

  }

  public Vector<String> getAnnotationItems(boolean isSeqAssociated)
  {
    Vector<String> list = new Vector<String>();
    int index = 1;
    int[] anmap = new int[av.getAlignment().getAlignmentAnnotation().length];
    for (int i = 0; i < av.getAlignment().getAlignmentAnnotation().length; i++)
    {
      if (av.getAlignment().getAlignmentAnnotation()[i].sequenceRef == null)
      {
        if (isSeqAssociated)
        {
          continue;
        }
      }
      else
      {
        enableSeqAss = true;
      }
      String label = av.getAlignment().getAlignmentAnnotation()[i].label;
      // add associated sequence ID if available
      if (!isSeqAssociated
              && av.getAlignment().getAlignmentAnnotation()[i].sequenceRef != null)
      {
        label = label
                + "_"
                + av.getAlignment().getAlignmentAnnotation()[i].sequenceRef
                        .getName();
      }
      // make label unique
      if (!list.contains(label))
      {
        anmap[list.size()] = i;
        list.add(label);
      }
      else
      {
        if (!isSeqAssociated)
        {
          anmap[list.size()] = i;
          list.add(label + "_" + (index++));
        }
      }
    }
    this.annmap = new int[list.size()];
    System.arraycopy(anmap, 0, this.annmap, 0, this.annmap.length);
    return list;
  }

  protected int getSelectedThresholdItem(int indexValue)
  {
    int selectedThresholdItem = -1;
    if (indexValue == 1)
    {
      selectedThresholdItem = AnnotationColourGradient.ABOVE_THRESHOLD;
    }
    else if (indexValue == 2)
    {
      selectedThresholdItem = AnnotationColourGradient.BELOW_THRESHOLD;
    }
    return selectedThresholdItem;
  }

  public void modelChanged()
  {
    seqAssociated.setEnabled(enableSeqAss);
  }

  public void ok_actionPerformed()
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  public void cancel_actionPerformed()
  {
    reset();
    ap.paintAlignment(true);
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  public void thresholdCheck_actionPerformed()
  {
    updateView();
  }

  public void annotations_actionPerformed()
  {
    updateView();
  }

  public void threshold_actionPerformed()
  {
    updateView();
  }

  public void thresholdValue_actionPerformed()
  {
    try
    {
      float f = Float.parseFloat(thresholdValue.getText());
      slider.setValue((int) (f * 1000));
      updateView();
    } catch (NumberFormatException ex)
    {
    }
  }

  public void thresholdIsMin_actionPerformed()
  {
    updateView();
  }

  protected void populateThresholdComboBox(JComboBox<String> threshold)
  {
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold"));
  }

  protected void seqAssociated_actionPerformed(JComboBox<String> annotations)
  {
    adjusting = true;
    String cursel = (String) annotations.getSelectedItem();
    boolean isvalid = false, isseqs = seqAssociated.isSelected();
    annotations.removeAllItems();
    for (String anitem : getAnnotationItems(seqAssociated.isSelected()))
    {
      if (anitem.equals(cursel) || (isseqs && cursel.startsWith(anitem)))
      {
        isvalid = true;
        cursel = anitem;
      }
      annotations.addItem(anitem);
    }
    adjusting = false;
    if (isvalid)
    {
      annotations.setSelectedItem(cursel);
    }
    else
    {
      if (annotations.getItemCount() > 0)
      {
        annotations.setSelectedIndex(0);
      }
    }
  }

  protected void propagateSeqAssociatedThreshold(boolean allAnnotation,
          AlignmentAnnotation annotation)
  {
    if (annotation.sequenceRef == null || annotation.threshold == null)
    {
      return;
    }

    float thr = annotation.threshold.value;
    for (int i = 0; i < av.getAlignment().getAlignmentAnnotation().length; i++)
    {
      AlignmentAnnotation aa = av.getAlignment().getAlignmentAnnotation()[i];
      if (aa.label.equals(annotation.label)
              && (annotation.getCalcId() == null ? aa.getCalcId() == null
                      : annotation.getCalcId().equals(aa.getCalcId())))
      {
        if (aa.threshold == null)
        {
          aa.threshold = new GraphLine(annotation.threshold);
        }
        else
        {
          aa.threshold.value = thr;
        }
      }
    }
  }

  protected boolean colorAlignmContaining(AlignmentAnnotation currentAnn,
          int selectedThresholdOption)
  {

    AnnotationColourGradient acg = null;
    if (currentColours.isSelected())
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

    acg.thresholdIsMinMax = thresholdIsMin.isSelected();

    av.setGlobalColourScheme(acg);

    if (av.getAlignment().getGroups() != null)
    {

      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.cs == null)
        {
          continue;
        }

        if (currentColours.isSelected())
        {
          sg.cs = new AnnotationColourGradient(currentAnn, sg.cs,
                  selectedThresholdOption);
          ((AnnotationColourGradient) sg.cs).setSeqAssociated(seqAssociated
                  .isSelected());

        }
        else
        {
          sg.cs = new AnnotationColourGradient(currentAnn,
                  minColour.getBackground(), maxColour.getBackground(),
                  selectedThresholdOption);
          ((AnnotationColourGradient) sg.cs).setSeqAssociated(seqAssociated
                  .isSelected());
        }

      }
    }
    return false;
  }

  public jalview.datamodel.AlignmentAnnotation getCurrentAnnotation()
  {
    return currentAnnotation;
  }

  public void setCurrentAnnotation(
          jalview.datamodel.AlignmentAnnotation currentAnnotation)
  {
    this.currentAnnotation = currentAnnotation;
  }

  public abstract void valueChanged(boolean updateAllAnnotation);

  public abstract void updateView();

  public abstract void reset();
}
