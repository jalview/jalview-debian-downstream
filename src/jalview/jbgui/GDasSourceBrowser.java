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
package jalview.jbgui;

import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class GDasSourceBrowser extends JPanel
{
  public GDasSourceBrowser()
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
    refresh.setText(MessageManager
            .getString("label.refresh_available_sources"));
    refresh.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        refresh_actionPerformed(e);
      }
    });
    progressBar.setPreferredSize(new Dimension(450, 20));
    progressBar.setString("");
    scrollPane.setBorder(titledBorder1);
    scrollPane.setBorder(BorderFactory.createEtchedBorder());
    fullDetailsScrollpane.setBorder(BorderFactory.createEtchedBorder());
    fullDetails.addHyperlinkListener(new HyperlinkListener()
    {
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        fullDetails_hyperlinkUpdate(e);
      }
    });
    fullDetails.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    fullDetails.setEditable(false);
    registryLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    registryLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    registryLabel.setText(MessageManager.getString("label.use_registry"));
    addLocal.setText(MessageManager.getString("label.add_local_source"));
    addLocal.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        amendLocal(true);
      }
    });
    jPanel1.setLayout(flowLayout1);
    jPanel1.setMinimumSize(new Dimension(596, 30));
    jPanel1.setPreferredSize(new Dimension(596, 30));
    jScrollPane2.setBorder(titledBorder3);
    jScrollPane3.setBorder(titledBorder4);
    jScrollPane4.setBorder(titledBorder5);
    titledBorder2
            .setTitleFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    titledBorder3
            .setTitleFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    titledBorder4
            .setTitleFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    filter1.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    filter2.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    filter3.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    table.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    reset.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    reset.setMargin(new Insets(2, 2, 2, 2));
    reset.setText(MessageManager.getString("action.reset"));
    reset.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        reset_actionPerformed(e);
      }
    });
    jPanel2.setLayout(borderLayout1);
    borderLayout1.setHgap(5);
    registryURL.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    scrollPane.getViewport().add(table);
    fullDetailsScrollpane.getViewport().add(fullDetails);
    jScrollPane3.getViewport().add(filter2);
    jScrollPane4.getViewport().add(filter3);
    jPanel1.add(refresh, null);
    jPanel1.add(addLocal, null);
    jPanel1.add(progressBar, null);
    jScrollPane2.getViewport().add(filter1);
    this.add(jPanel1, new GridBagConstraints(0, 3, 3, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0), 0, 0));
    this.add(fullDetailsScrollpane, new GridBagConstraints(1, 0, 2, 1, 1.0,
            1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(3, 0, 0, 3), 240, 130));
    this.add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    3, 2, 0, 0), 150, 130));
    jPanel2.add(registryLabel, java.awt.BorderLayout.WEST);
    jPanel2.add(registryURL, java.awt.BorderLayout.CENTER);
    jPanel2.add(reset, java.awt.BorderLayout.EAST);
    this.add(jPanel2, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(5, 10, 0, 10), 339, 0));
    this.add(jScrollPane2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    0, 0, 0, 60), 80, 60));
    this.add(jScrollPane4, new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    0, -80, 0, 0), 80, 60));
    this.add(jScrollPane3, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                    0, -60, 0, 80), 80, 60));
  }

  protected JTable table = new JTable();

  protected JEditorPane fullDetails = new JEditorPane("text/html", "");

  TitledBorder titledBorder1 = new TitledBorder(
          MessageManager.getString("label.available_das_sources"));

  protected JButton refresh = new JButton();

  protected JProgressBar progressBar = new JProgressBar();

  protected JScrollPane scrollPane = new JScrollPane();

  TitledBorder titledBorder2 = new TitledBorder(
          MessageManager.getString("label.full_details"));

  protected JScrollPane fullDetailsScrollpane = new JScrollPane();

  protected JList filter1 = new JList();

  protected JList filter2 = new JList();

  protected JList filter3 = new JList();

  JScrollPane jScrollPane2 = new JScrollPane();

  JScrollPane jScrollPane3 = new JScrollPane();

  JScrollPane jScrollPane4 = new JScrollPane();

  protected JTextField registryURL = new JTextField();

  protected JLabel registryLabel = new JLabel();

  protected JButton addLocal = new JButton();

  JPanel jPanel1 = new JPanel();

  FlowLayout flowLayout1 = new FlowLayout();

  GridBagLayout gridBagLayout1 = new GridBagLayout();

  TitledBorder titledBorder3 = new TitledBorder(
          MessageManager.getString("label.authority") + ":");

  TitledBorder titledBorder4 = new TitledBorder(
          MessageManager.getString("label.type") + ":");

  TitledBorder titledBorder5 = new TitledBorder(
          MessageManager.getString("label.label") + ":");

  JButton reset = new JButton();

  JPanel jPanel2 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  public void refresh_actionPerformed(ActionEvent e)
  {

  }

  public void fullDetails_hyperlinkUpdate(HyperlinkEvent e)
  {
    try
    {

      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
        jalview.util.BrowserLauncher.openURL(e.getURL().toString());
      }
    } catch (Exception ex)
    {
      System.out.println(e.getURL());
      ex.printStackTrace();
    }
  }

  public void amendLocal(boolean newSource)
  {

  }

  public void reset_actionPerformed(ActionEvent e)
  {

  }

}
