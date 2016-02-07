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

import jalview.datamodel.SequenceGroup;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TextColourChooser
{
  AlignmentPanel ap;

  SequenceGroup sg;

  public void chooseColour(AlignmentPanel ap, SequenceGroup sg)
  {
    this.ap = ap;
    this.sg = sg;

    int original1, original2, originalThreshold;
    if (sg == null)
    {
      original1 = ap.av.getTextColour().getRGB();
      original2 = ap.av.getTextColour2().getRGB();
      originalThreshold = ap.av.getThresholdTextColour();
    }
    else
    {
      original1 = sg.textColour.getRGB();
      original2 = sg.textColour2.getRGB();
      originalThreshold = sg.thresholdTextColour;
    }

    final JSlider slider = new JSlider(0, 750, originalThreshold);
    final JPanel col1 = new JPanel();
    col1.setPreferredSize(new Dimension(40, 20));
    col1.setBorder(BorderFactory.createEtchedBorder());
    col1.setToolTipText(MessageManager.getString("label.dark_colour"));
    col1.setBackground(new Color(original1));
    final JPanel col2 = new JPanel();
    col2.setPreferredSize(new Dimension(40, 20));
    col2.setBorder(BorderFactory.createEtchedBorder());
    col2.setToolTipText(MessageManager.getString("label.ligth_colour"));
    col2.setBackground(new Color(original2));
    final JPanel bigpanel = new JPanel(new BorderLayout());
    JPanel panel = new JPanel();
    bigpanel.add(panel, BorderLayout.CENTER);
    bigpanel.add(
            new JLabel(
                    "<html>"
                            + MessageManager
                                    .getString("label.select_dark_light_set_thereshold")
                            + "</html>"), BorderLayout.NORTH);
    panel.add(col1);
    panel.add(slider);
    panel.add(col2);

    col1.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        Color col = JColorChooser.showDialog(bigpanel,
                MessageManager.getString("label.select_colour_for_text"),
                col1.getBackground());
        if (col != null)
        {
          colour1Changed(col);
          col1.setBackground(col);
        }
      }
    });

    col2.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        Color col = JColorChooser.showDialog(bigpanel,
                MessageManager.getString("label.select_colour_for_text"),
                col2.getBackground());
        if (col != null)
        {
          colour2Changed(col);
          col2.setBackground(col);
        }
      }
    });

    slider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        thresholdChanged(slider.getValue());
      }
    });

    int reply = JOptionPane
            .showInternalOptionDialog(
                    ap,
                    bigpanel,
                    MessageManager
                            .getString("label.adjunst_foreground_text_colour_thereshold"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);

    if (reply == JOptionPane.CANCEL_OPTION)
    {
      if (sg == null)
      {
        ap.av.setTextColour(new Color(original1));
        ap.av.setTextColour2(new Color(original2));
        ap.av.setThresholdTextColour(originalThreshold);
      }
      else
      {
        sg.textColour = new Color(original1);
        sg.textColour2 = new Color(original2);
        sg.thresholdTextColour = originalThreshold;
      }
    }
  }

  void colour1Changed(Color col)
  {
    if (sg == null)
    {
      ap.av.setTextColour(col);
      if (ap.av.getColourAppliesToAllGroups())
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.textColour = col;
    }

    ap.paintAlignment(true);
  }

  void colour2Changed(Color col)
  {
    if (sg == null)
    {
      ap.av.setTextColour2(col);
      if (ap.av.getColourAppliesToAllGroups())
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.textColour2 = col;
    }

    ap.paintAlignment(true);
  }

  void thresholdChanged(int value)
  {
    if (sg == null)
    {
      ap.av.setThresholdTextColour(value);
      if (ap.av.getColourAppliesToAllGroups())
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.thresholdTextColour = value;
    }

    ap.paintAlignment(true);
  }

  void setGroupTextColour()
  {
    if (ap.av.getAlignment().getGroups() == null)
    {
      return;
    }

    for (SequenceGroup sg : ap.av.getAlignment().getGroups())
    {
      sg.textColour = ap.av.getTextColour();
      sg.textColour2 = ap.av.getTextColour2();
      sg.thresholdTextColour = ap.av.getThresholdTextColour();
    }
  }

}
