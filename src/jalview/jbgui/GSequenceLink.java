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
package jalview.jbgui;

import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;
import jalview.util.UrlLink;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class GSequenceLink extends Panel
{
  public GSequenceLink()
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
    this.setLayout(gridBagLayout1);
    nameTB.setFont(JvSwingUtils.getLabelFont());
    nameTB.setBounds(new Rectangle(77, 10, 310, 23));
    nameTB.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent e)
      {
        nameTB_keyTyped(e);
      }
    });
    urlTB.setFont(JvSwingUtils.getLabelFont());
    urlTB.setText("http://");
    urlTB.setBounds(new Rectangle(78, 40, 309, 23));
    urlTB.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent e)
      {
        urlTB_keyTyped(e);
      }
    });
    jLabel1.setFont(JvSwingUtils.getLabelFont());
    jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel1.setText(MessageManager.getString("label.link_name"));
    jLabel1.setBounds(new Rectangle(4, 10, 71, 24));
    jLabel2.setFont(JvSwingUtils.getLabelFont());
    jLabel2.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel2.setText(MessageManager.getString("label.url"));
    jLabel2.setBounds(new Rectangle(17, 37, 54, 27));
    jLabel3.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel3.setText(MessageManager.getString("label.use_sequence_id_1"));
    jLabel3.setBounds(new Rectangle(21, 72, 351, 15));
    jLabel4.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel4.setText(MessageManager.getString("label.use_sequence_id_2"));
    jLabel4.setBounds(new Rectangle(21, 88, 351, 15));
    jLabel5.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel5.setText(MessageManager.getString("label.use_sequence_id_3"));
    jLabel5.setBounds(new Rectangle(21, 106, 351, 15));

    String lastLabel = MessageManager.getString("label.use_sequence_id_4");
    if (lastLabel.length() > 0)
    {
      // e.g. Spanish version has longer text
      jLabel6.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
      jLabel6.setText(lastLabel);
      jLabel6.setBounds(new Rectangle(21, 122, 351, 15));
    }

    jPanel1.setBorder(BorderFactory.createEtchedBorder());
    jPanel1.setLayout(null);
    jPanel1.add(jLabel1);
    jPanel1.add(nameTB);
    jPanel1.add(urlTB);
    jPanel1.add(jLabel2);
    jPanel1.add(jLabel3);
    jPanel1.add(jLabel4);
    jPanel1.add(jLabel5);

    int height = 130;
    if (lastLabel.length() > 0)
    {
      jPanel1.add(jLabel6);
      height = 146;
    }

    this.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    5, 4, 6, 5), 390, height));
  }

  @Override
  public void setName(String name)
  {
    nameTB.setText(name);
  }

  public void setURL(String url)
  {
    urlTB.setText(url);
  }

  @Override
  public String getName()
  {
    return nameTB.getText();
  }

  public String getURL()
  {
    return urlTB.getText();
  }

  public boolean checkValid()
  {
    UrlLink ul = new UrlLink("foo|" + urlTB.getText().trim());
    if (ul.isValid() && ul.isDynamic())
    {
      return true;
    }

    JOptionPane.showInternalMessageDialog(jalview.gui.Desktop.desktop,
            MessageManager.getString("warn.url_must_contain"),
            MessageManager.getString("label.invalid_url"),
            JOptionPane.WARNING_MESSAGE);
    return false;
  }

  JTextField nameTB = new JTextField();

  JTextField urlTB = new JTextField();

  JLabel jLabel1 = new JLabel();

  JLabel jLabel2 = new JLabel();

  JLabel jLabel3 = new JLabel();

  JLabel jLabel4 = new JLabel();

  JLabel jLabel5 = new JLabel();

  JLabel jLabel6 = new JLabel();

  JPanel jPanel1 = new JPanel();

  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public void nameTB_keyTyped(KeyEvent e)
  {
    if (e.getKeyChar() == '|')
    {
      e.consume();
    }
  }

  public void urlTB_keyTyped(KeyEvent e)
  {
    // URLLink object validation takes care of incorrect regexes.
    // if (e.getKeyChar() == '|' || e.getKeyChar() == ' ')
    // {
    // e.consume();
    // }

  }
}
