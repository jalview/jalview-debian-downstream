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
package jalview.jbgui;

import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GSliderPanel extends JPanel
{
  // this is used for conservation colours, PID colours and redundancy threshold
  protected JSlider slider = new JSlider();

  protected JTextField valueField = new JTextField();

  protected JLabel label = new JLabel();

  protected JPanel southPanel = new JPanel();

  GridLayout gridLayout1 = new GridLayout();

  JPanel jPanel2 = new JPanel();

  protected JButton applyButton = new JButton();

  protected JButton undoButton = new JButton();

  FlowLayout flowLayout1 = new FlowLayout();

  protected JCheckBox allGroupsCheck = new JCheckBox();

  BorderLayout borderLayout1 = new BorderLayout();

  JPanel jPanel1 = new JPanel();

  BorderLayout borderLayout2 = new BorderLayout();

  /**
   * Creates a new GSliderPanel object.
   */
  public GSliderPanel()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws Exception
   *           DOCUMENT ME!
   */
  private void jbInit() throws Exception
  {
    this.setLayout(gridLayout1);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setFont(new java.awt.Font("Verdana", 0, 11));
    slider.setDoubleBuffered(true);
    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseReleased(MouseEvent e)
      {
        slider_mouseReleased(e);
      }
    });
    valueField.setFont(new java.awt.Font("Verdana", 0, 11));
    valueField.setMinimumSize(new Dimension(6, 14));
    valueField.setPreferredSize(new Dimension(50, 12));
    valueField.setText("");
    valueField.setHorizontalAlignment(SwingConstants.CENTER);
    valueField.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        valueField_actionPerformed();
      }
    });
    valueField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        valueField_actionPerformed();
      }
    });
    label.setFont(new java.awt.Font("Verdana", 0, 11));
    label.setOpaque(false);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setText(MessageManager.getString("label.set_this_label_text"));
    southPanel.setLayout(borderLayout1);
    gridLayout1.setRows(2);
    jPanel2.setLayout(flowLayout1);
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setOpaque(false);
    applyButton.setText(MessageManager.getString("action.apply"));
    applyButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        applyButton_actionPerformed(e);
      }
    });
    undoButton.setEnabled(false);
    undoButton.setFont(new java.awt.Font("Verdana", 0, 11));
    undoButton.setOpaque(false);
    undoButton.setText(MessageManager.getString("action.undo"));
    undoButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        undoButton_actionPerformed(e);
      }
    });
    allGroupsCheck.setEnabled(false);
    allGroupsCheck.setFont(new java.awt.Font("Verdana", 0, 11));
    allGroupsCheck.setOpaque(false);
    allGroupsCheck
            .setText(MessageManager.getString("action.apply_all_groups"));
    allGroupsCheck.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        allGroupsCheck_actionPerformed(e);
      }
    });
    this.setBackground(Color.white);
    this.setPreferredSize(new Dimension(415, 84));
    jPanel2.setOpaque(false);
    southPanel.setOpaque(false);
    jPanel1.setLayout(borderLayout2);
    jPanel1.setOpaque(false);
    this.add(jPanel2, null);
    jPanel2.add(label, null);
    jPanel2.add(applyButton, null);
    jPanel2.add(undoButton, null);
    this.add(southPanel, null);
    southPanel.add(jPanel1, java.awt.BorderLayout.EAST);
    southPanel.add(slider, java.awt.BorderLayout.CENTER);
    jPanel1.add(valueField, java.awt.BorderLayout.CENTER);
    jPanel1.add(allGroupsCheck, java.awt.BorderLayout.EAST);
  }

  /**
   * Action on changing the slider text field value
   */
  protected void valueField_actionPerformed()
  {
    try
    {
      int i = Integer.valueOf(valueField.getText());
      slider.setValue(i);
    } catch (NumberFormatException ex)
    {
      valueField.setText(String.valueOf(slider.getValue()));
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void applyButton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void undoButton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void allGroupsCheck_actionPerformed(ActionEvent e)
  {
  }

  public void slider_mouseReleased(MouseEvent e)
  {

  }
}
