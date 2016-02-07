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

import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class SVGOptions extends JPanel
{
  JDialog dialog;

  public boolean cancelled = false;

  String value;

  public SVGOptions()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    ButtonGroup bg = new ButtonGroup();
    bg.add(lineart);
    bg.add(text);

    JOptionPane pane = new JOptionPane(null, JOptionPane.DEFAULT_OPTION,
            JOptionPane.DEFAULT_OPTION, null, new Object[] { this });

    dialog = pane.createDialog(Desktop.desktop, "SVG Rendering options");
    dialog.setVisible(true);

  }

  private void jbInit() throws Exception
  {
    lineart.setFont(JvSwingUtils.getLabelFont());
    lineart.setText(MessageManager.getString("label.lineart"));
    text.setFont(JvSwingUtils.getLabelFont());
    text.setText(MessageManager.getString("action.text"));
    text.setSelected(true);
    askAgain.setFont(JvSwingUtils.getLabelFont());
    askAgain.setText(MessageManager.getString("label.dont_ask_me_again"));
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    jLabel1.setFont(JvSwingUtils.getLabelFont());
    jLabel1.setText("Select SVG character rendering style");
    this.setLayout(borderLayout1);
    jPanel3.setBorder(BorderFactory.createEtchedBorder());
    jPanel2.add(text);
    jPanel2.add(lineart);
    jPanel2.add(askAgain);
    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel3.add(jLabel1);
    jPanel3.add(jPanel2);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
  }

  JRadioButton lineart = new JRadioButton();

  JRadioButton text = new JRadioButton();

  JCheckBox askAgain = new JCheckBox();

  JButton ok = new JButton();

  JButton cancel = new JButton();

  JPanel jPanel1 = new JPanel();

  JLabel jLabel1 = new JLabel();

  JPanel jPanel2 = new JPanel();

  JPanel jPanel3 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  public void ok_actionPerformed(ActionEvent e)
  {
    if (lineart.isSelected())
    {
      value = "Lineart";
    }
    else
    {
      value = "Text";
    }

    if (!askAgain.isSelected())
    {
      jalview.bin.Cache.applicationProperties.remove("SVG_RENDERING");
    }
    else
    {
      jalview.bin.Cache.setProperty("SVG_RENDERING", value);
    }

    dialog.setVisible(false);
  }

  public void cancel_actionPerformed(ActionEvent e)
  {
    cancelled = true;
    dialog.setVisible(false);
  }

  public String getValue()
  {
    return value;
  }
}
