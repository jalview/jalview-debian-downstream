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

import jalview.datamodel.SequenceGroup;
import jalview.jbgui.GSliderPanel;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SliderPanel extends GSliderPanel
{
  static JInternalFrame conservationSlider;

  static JInternalFrame PIDSlider;

  AlignmentPanel ap;

  boolean forConservation = true;

  ColourSchemeI cs;

  /**
   * Creates a new SliderPanel object.
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param value
   *          DOCUMENT ME!
   * @param forConserve
   *          DOCUMENT ME!
   * @param cs
   *          DOCUMENT ME!
   */
  public SliderPanel(final AlignmentPanel ap, int value,
          boolean forConserve, ColourSchemeI cs)
  {
    this.ap = ap;
    this.cs = cs;
    forConservation = forConserve;
    undoButton.setVisible(false);
    applyButton.setVisible(false);

    if (forConservation)
    {
      label.setText(MessageManager
              .getString("label.enter_value_increase_conservation_visibility"));
      slider.setMinimum(0);
      slider.setMaximum(100);
    }
    else
    {
      label.setText(MessageManager
              .getString("label.enter_percentage_identity_above_which_colour_residues"));
      slider.setMinimum(0);
      slider.setMaximum(100);
    }

    slider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        valueField.setText(slider.getValue() + "");
        valueChanged(slider.getValue());
      }
    });

    slider.addMouseListener(new MouseAdapter()
    {
      public void mouseReleased(MouseEvent evt)
      {
        ap.paintAlignment(true);
      }
    });

    slider.setValue(value);
    valueField.setText(value + "");
  }

  /**
   * DOCUMENT ME!
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param cs
   *          DOCUMENT ME!
   * @param source
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static int setConservationSlider(AlignmentPanel ap,
          ColourSchemeI cs, String source)
  {
    SliderPanel sp = null;

    if (conservationSlider == null)
    {
      sp = new SliderPanel(ap, cs.getConservationInc(), true, cs);
      conservationSlider = new JInternalFrame();
      conservationSlider.setContentPane(sp);
      conservationSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
    else
    {
      sp = (SliderPanel) conservationSlider.getContentPane();
      sp.cs = cs;
    }

    conservationSlider
            .setTitle(MessageManager.formatMessage(
                    "label.conservation_colour_increment",
                    new String[] { source }));

    if (ap.av.getAlignment().getGroups() != null)
    {
      sp.setAllGroupsCheckEnabled(true);
    }
    else
    {
      sp.setAllGroupsCheckEnabled(false);
    }

    return sp.getValue();
  }

  /**
   * DOCUMENT ME!
   */
  public static void showConservationSlider()
  {
    try
    {
      PIDSlider.setClosed(true);
      PIDSlider = null;
    } catch (Exception ex)
    {
    }

    if (!conservationSlider.isVisible())
    {
      Desktop.addInternalFrame(conservationSlider,
              conservationSlider.getTitle(), 420, 90, false);
      conservationSlider
              .addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
              {
                public void internalFrameClosed(
                        javax.swing.event.InternalFrameEvent e)
                {
                  conservationSlider = null;
                }
              });
      conservationSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param cs
   *          DOCUMENT ME!
   * @param source
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static int setPIDSliderSource(AlignmentPanel ap, ColourSchemeI cs,
          String source)
  {
    SliderPanel pid = null;

    int threshold = cs.getThreshold();

    if (PIDSlider == null)
    {
      pid = new SliderPanel(ap, threshold, false, cs);
      PIDSlider = new JInternalFrame();
      PIDSlider.setContentPane(pid);
      PIDSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
    else
    {
      pid = (SliderPanel) PIDSlider.getContentPane();
      pid.cs = cs;
    }

    PIDSlider
            .setTitle(MessageManager.formatMessage(
                    "label.percentage_identity_threshold",
                    new String[] { source }));

    if (ap.av.getAlignment().getGroups() != null)
    {
      pid.setAllGroupsCheckEnabled(true);
    }
    else
    {
      pid.setAllGroupsCheckEnabled(false);
    }

    return pid.getValue();
  }

  /**
   * DOCUMENT ME!
   */
  public static void showPIDSlider()
  {
    try
    {
      conservationSlider.setClosed(true);
      conservationSlider = null;
    } catch (Exception ex)
    {
    }

    if (!PIDSlider.isVisible())
    {
      Desktop.addInternalFrame(PIDSlider, PIDSlider.getTitle(), 420, 90,
              false);
      PIDSlider.setLayer(JLayeredPane.PALETTE_LAYER);
      PIDSlider
              .addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
              {
                public void internalFrameClosed(
                        javax.swing.event.InternalFrameEvent e)
                {
                  PIDSlider = null;
                }
              });
      PIDSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   */
  public void valueChanged(int i)
  {
    if (cs == null)
    {
      return;
    }

    ColourSchemeI toChange = cs;
    Iterator<SequenceGroup> allGroups = null;

    if (allGroupsCheck.isSelected())
    {
      allGroups = ap.av.getAlignment().getGroups().listIterator();
    }

    while (toChange != null)
    {
      if (forConservation)
      {
        toChange.setConservationInc(i);
      }
      else
      {
        toChange.setThreshold(i, ap.av.isIgnoreGapsConsensus());
      }
      if (allGroups != null && allGroups.hasNext())
      {
        while ((toChange = allGroups.next().cs) == null
                && allGroups.hasNext())
        {
          ;
        }
      }
      else
      {
        toChange = null;
      }
    }

    ap.getSeqPanel().seqCanvas.repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setAllGroupsCheckEnabled(boolean b)
  {
    allGroupsCheck.setEnabled(b);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void valueField_actionPerformed(ActionEvent e)
  {
    try
    {
      int i = Integer.parseInt(valueField.getText());
      slider.setValue(i);
    } catch (NumberFormatException ex)
    {
      valueField.setText(slider.getValue() + "");
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param value
   *          DOCUMENT ME!
   */
  public void setValue(int value)
  {
    slider.setValue(value);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getValue()
  {
    return Integer.parseInt(valueField.getText());
  }

  public void slider_mouseReleased(MouseEvent e)
  {
    if (ap.overviewPanel != null)
    {
      ap.overviewPanel.updateOverviewImage();
    }
  }

}
