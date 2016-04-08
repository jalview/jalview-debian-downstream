/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
package jalview.appletgui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import jalview.datamodel.*;
import jalview.schemes.*;
import java.awt.Rectangle;

public class FeatureColourChooser extends Panel implements ActionListener,
        AdjustmentListener, ItemListener, MouseListener
{
  JVDialog frame;

  Frame owner;

  FeatureRenderer fr;

  FeatureSettings fs = null;

  // AlignmentPanel ap;

  GraduatedColor cs;

  Object oldcs;

  Hashtable oldgroupColours;

  boolean adjusting = false;

  private float min, max;

  String type = null;

  private AlignFrame af = null;

  public FeatureColourChooser(AlignFrame af, String type)
  {
    this.af = af;
    init(af.getSeqcanvas().getFeatureRenderer(), type);
  }

  public FeatureColourChooser(FeatureSettings fsettings, String type)
  {
    this.fs = fsettings;
    init(fsettings.fr, type);
    // this.ap = fsettings.ap;
  }

  private void init(FeatureRenderer frenderer, String type)
  {
    this.type = type;
    fr = frenderer;
    float mm[] = ((float[][]) fr.minmax.get(type))[0];
    min = mm[0];
    max = mm[1];
    oldcs = fr.featureColours.get(type);
    if (oldcs instanceof GraduatedColor)
    {
      cs = new GraduatedColor((GraduatedColor) oldcs, min, max);
    }
    else
    {
      // promote original color to a graduated color
      Color bl = Color.black;
      if (oldcs instanceof Color)
      {
        bl = (Color) oldcs;
      }
      // original colour becomes the maximum colour
      cs = new GraduatedColor(Color.white, bl, mm[0], mm[1]);
    }
    minColour.setBackground(cs.getMinColor());
    maxColour.setBackground(cs.getMaxColor());
    minColour.setForeground(cs.getMinColor());
    maxColour.setForeground(cs.getMaxColor());
    colourFromLabel.setState(cs.isColourByLabel());
    adjusting = true;

    try
    {
      jbInit();
    } catch (Exception ex)
    {
    }
    threshold
            .select(cs.getThreshType() == AnnotationColourGradient.NO_THRESHOLD ? 0
                    : cs.getThreshType() == AnnotationColourGradient.ABOVE_THRESHOLD ? 1
                            : 2);

    adjusting = false;
    changeColour();
    colourFromLabel.addItemListener(this);
    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);
    owner = (af != null) ? af : fs.frame;
    frame = new JVDialog(owner, "Graduated Feature Colour for " + type,
            true, 480, 248);
    frame.setMainPanel(this);
    validate();
    frame.setVisible(true);
    if (frame.accept)
    {
      changeColour();
    }
    else
    {
      // cancel
      reset();
      PaintRefresher.Refresh(this, fr.av.getSequenceSetId());
      frame.setVisible(false);
    }
  }

  public FeatureColourChooser()
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
    Label minLabel = new Label("Min:"), maxLabel = new Label("Max:");
    minLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    // minColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    // minColour.setLabel("Min Colour");

    minColour.setBounds(0, 0, 40, 27);
    maxColour.setBounds(0, 0, 40, 27);
    minColour.addMouseListener(this);

    maxColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxColour.addMouseListener(this);

    thresholdIsMin.addItemListener(this);

    this.setLayout(new GridLayout(4, 1));
    jPanel1.setLayout(new FlowLayout());
    jPanel2.setLayout(new FlowLayout());
    jPanel3.setLayout(new GridLayout(1, 1));
    jPanel4.setLayout(new FlowLayout());
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);
    jPanel4.setBackground(Color.white);
    threshold.addItemListener(this);
    threshold.addItem("No Threshold");
    threshold.addItem("Above Threshold");
    threshold.addItem("Below Threshold");
    thresholdValue.addActionListener(this);
    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setSize(new Dimension(93, 21));
    thresholdValue.setEnabled(false);
    thresholdValue.setSize(new Dimension(79, 22)); // setBounds(new
                                                   // Rectangle(248, 2, 79,
                                                   // 22));
    thresholdValue.setColumns(5);
    jPanel3.setBackground(Color.white);

    colourFromLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    colourFromLabel.setLabel("Colour by Label");
    colourFromLabel.setSize(new Dimension(139, 22));
    // threshold.setBounds(new Rectangle(11, 3, 139, 22));
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setLabel("Threshold is min/max");
    thresholdIsMin.setSize(new Dimension(135, 23));
    // thresholdIsMin.setBounds(new Rectangle(328, 3, 135, 23));
    jPanel1.add(minLabel);
    jPanel1.add(minColour);
    jPanel1.add(maxLabel);
    jPanel1.add(maxColour);
    jPanel1.add(colourFromLabel);
    jPanel2.add(threshold);
    jPanel3.add(slider);
    jPanel4.add(thresholdValue);
    jPanel4.add(thresholdIsMin);
    this.add(jPanel1);// , java.awt.BorderLayout.NORTH);
    this.add(jPanel2);// , java.awt.BorderLayout.NORTH);
    this.add(jPanel3);// , java.awt.BorderLayout.CENTER);
    this.add(jPanel4);// , java.awt.BorderLayout.CENTER);
  }

  Panel minColour = new Panel();

  Panel maxColour = new Panel();

  Panel jPanel1 = new Panel();

  Panel jPanel2 = new Panel();

  Choice threshold = new Choice();

  Panel jPanel3 = new Panel();

  Panel jPanel4 = new Panel();

  Scrollbar slider = new Scrollbar(Scrollbar.HORIZONTAL);

  TextField thresholdValue = new TextField(20);

  // BorderLayout borderLayout1 = new BorderLayout();

  Checkbox thresholdIsMin = new Checkbox();

  Checkbox colourFromLabel = new Checkbox();

  private GraphLine threshline;

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == thresholdValue)
    {
      try
      {
        float f = new Float(thresholdValue.getText()).floatValue();
        slider.setValue((int) (f * 1000));
        adjustmentValueChanged(null);
      } catch (NumberFormatException ex)
      {
      }
    }
    else if (evt.getSource() == minColour)
    {
      minColour_actionPerformed(null);
    }
    else if (evt.getSource() == maxColour)
    {
      maxColour_actionPerformed(null);
    }
    else
    {
      changeColour();
    }
  }

  public void itemStateChanged(ItemEvent evt)
  {
    maxColour.setEnabled(!colourFromLabel.getState());
    minColour.setEnabled(!colourFromLabel.getState());
    changeColour();
  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (!adjusting)
    {
      thresholdValue.setText(((float) slider.getValue() / 1000f) + "");
      valueChanged();
    }
  }

  protected void valueChanged()
  {
    threshline.value = (float) slider.getValue() / 1000f;
    cs.setThresh(threshline.value);
    changeColour();
    PaintRefresher.Refresh(this, fr.av.getSequenceSetId());
    // ap.paintAlignment(false);
  }

  public void minColour_actionPerformed(Color newCol)
  {
    if (newCol == null)
    {
      UserDefinedColours udc = new UserDefinedColours(this,
              minColour.getBackground(), owner,
              "Select Colour for Minimum Value"); // frame.owner,
    }
    else
    {
      minColour.setBackground(newCol);
      minColour.setForeground(newCol);
      minColour.repaint();
      changeColour();
    }

  }

  public void maxColour_actionPerformed(Color newCol)
  {
    if (newCol == null)
    {

      // UserDefinedColours udc = new UserDefinedColours(this,
      // "Select Colour for Maximum Value",maxColour.getBackground(),true);
      UserDefinedColours udc = new UserDefinedColours(this,
              maxColour.getBackground(), owner,
              "Select Colour for Maximum Value");
    }
    else
    {
      maxColour.setBackground(newCol);
      maxColour.setForeground(newCol);
      maxColour.repaint();
      changeColour();
    }
  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    int aboveThreshold = AnnotationColourGradient.NO_THRESHOLD;
    if (threshold.getSelectedItem().equals("Above Threshold"))
    {
      aboveThreshold = AnnotationColourGradient.ABOVE_THRESHOLD;
    }
    else if (threshold.getSelectedItem().equals("Below Threshold"))
    {
      aboveThreshold = AnnotationColourGradient.BELOW_THRESHOLD;
    }

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    GraduatedColor acg = new GraduatedColor(minColour.getBackground(),
            maxColour.getBackground(), min, max);

    acg.setColourByLabel(colourFromLabel.getState());
    maxColour.setEnabled(!colourFromLabel.getState());
    minColour.setEnabled(!colourFromLabel.getState());
    if (aboveThreshold == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
    }

    else if (aboveThreshold != AnnotationColourGradient.NO_THRESHOLD
            && threshline == null)
    {
      // todo visual indication of feature threshold
      threshline = new jalview.datamodel.GraphLine((max - min) / 2f,
              "Threshold", Color.black);
    }

    if (aboveThreshold != AnnotationColourGradient.NO_THRESHOLD)
    {
      adjusting = true;
      acg.setThresh(threshline.value);

      float range = max * 1000f - min * 1000f;

      slider.setMinimum((int) (min * 1000));
      slider.setMaximum((int) (max * 1000));
      slider.setValue((int) (threshline.value * 1000));
      thresholdValue.setText(threshline.value + "");
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;
    }

    acg.setThreshType(aboveThreshold);
    if (thresholdIsMin.getState()
            && aboveThreshold != AnnotationColourGradient.NO_THRESHOLD)
    {
      if (aboveThreshold == AnnotationColourGradient.ABOVE_THRESHOLD)
      {
        acg = new GraduatedColor(acg, threshline.value, max);
      }
      else
      {
        acg = new GraduatedColor(acg, min, threshline.value);
      }
    }

    fr.featureColours.put(type, acg);
    cs = acg;
    PaintRefresher.Refresh(this, fr.av.getSequenceSetId());
    // ap.paintAlignment(false);
  }

  void reset()
  {
    fr.featureColours.put(type, oldcs);
    PaintRefresher.Refresh(this, fr.av.getSequenceSetId());
    // ap.paintAlignment(true);

  }

  public void mouseClicked(MouseEvent evt)
  {
  }

  public void mousePressed(MouseEvent evt)
  {
  }

  public void mouseReleased(MouseEvent evt)
  {
    if (evt.getSource() == minColour || evt.getSource() == maxColour)
    {
      // relay the event
      actionPerformed(new ActionEvent(evt.getSource(), 1, "Clicked"));
    }
    else
    {
      PaintRefresher.Refresh(this, fr.av.getSequenceSetId());
    }
    // ap.paintAlignment(true);
  }

  public void mouseEntered(MouseEvent evt)
  {
  }

  public void mouseExited(MouseEvent evt)
  {
  }

}
