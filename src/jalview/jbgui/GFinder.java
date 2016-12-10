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

import jalview.datamodel.AlignmentI;
import jalview.io.FormatAdapter;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class GFinder extends JPanel
{
  JLabel jLabelFind = new JLabel();

  protected JButton findAll = new JButton();

  protected JButton findNext = new JButton();

  JPanel actionsPanel = new JPanel();

  GridLayout gridLayout1 = new GridLayout();

  protected JButton createNewGroup = new JButton();

  JScrollPane jScrollPane1 = new JScrollPane();

  protected JTextArea textfield = new JTextArea();

  BorderLayout mainBorderLayout = new BorderLayout();

  JPanel jPanel2 = new JPanel();

  JPanel jPanel3 = new JPanel();

  JPanel jPanel4 = new JPanel();

  BorderLayout borderLayout2 = new BorderLayout();

  JPanel jPanel6 = new JPanel();

  protected JCheckBox caseSensitive = new JCheckBox();

  protected JCheckBox searchDescription = new JCheckBox();

  GridLayout optionsGridLayout = new GridLayout();

  public GFinder()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    jLabelFind.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabelFind.setText(MessageManager.getString("label.find"));
    this.setLayout(mainBorderLayout);
    findAll.setFont(new java.awt.Font("Verdana", 0, 12));
    findAll.setText(MessageManager.getString("action.find_all"));
    findAll.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        findAll_actionPerformed(e);
      }
    });
    findNext.setFont(new java.awt.Font("Verdana", 0, 12));
    findNext.setText(MessageManager.getString("action.find_next"));
    findNext.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        findNext_actionPerformed(e);
      }
    });
    actionsPanel.setLayout(gridLayout1);
    gridLayout1.setHgap(0);
    gridLayout1.setRows(3);
    gridLayout1.setVgap(2);
    createNewGroup.setEnabled(false);
    createNewGroup.setFont(new java.awt.Font("Verdana", 0, 12));
    createNewGroup.setMargin(new Insets(0, 0, 0, 0));
    createNewGroup.setText(MessageManager.getString("label.new_feature"));
    createNewGroup.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        createNewGroup_actionPerformed(e);
      }
    });
    textfield.setFont(new java.awt.Font("Verdana", Font.PLAIN, 12));
    textfield.setText("");
    textfield.setLineWrap(true);
    textfield.addCaretListener(new CaretListener()
    {
      public void caretUpdate(CaretEvent e)
      {
        textfield_caretUpdate(e);
      }
    });
    textfield.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        textfield_keyPressed(e);
      }
    });

    mainBorderLayout.setHgap(5);
    mainBorderLayout.setVgap(5);
    jPanel4.setLayout(borderLayout2);
    jPanel2.setPreferredSize(new Dimension(10, 1));
    jPanel3.setPreferredSize(new Dimension(10, 1));
    caseSensitive.setHorizontalAlignment(SwingConstants.LEFT);
    caseSensitive.setText(MessageManager.getString("label.match_case"));

    searchDescription.setText(MessageManager
            .getString("label.include_description"));

    actionsPanel.add(findNext, null);
    actionsPanel.add(findAll, null);
    actionsPanel.add(createNewGroup, null);
    this.add(jLabelFind, java.awt.BorderLayout.WEST);
    this.add(actionsPanel, java.awt.BorderLayout.EAST);
    this.add(jPanel2, java.awt.BorderLayout.SOUTH);
    this.add(jPanel3, java.awt.BorderLayout.NORTH);
    this.add(jPanel4, java.awt.BorderLayout.CENTER);
    jPanel4.add(jScrollPane1, java.awt.BorderLayout.NORTH);
    jScrollPane1.getViewport().add(textfield);

    JPanel optionsPanel = new JPanel();

    optionsGridLayout.setHgap(0);
    optionsGridLayout.setRows(2);
    optionsGridLayout.setVgap(2);
    optionsPanel.setLayout(optionsGridLayout);
    optionsPanel.add(caseSensitive, null);
    optionsPanel.add(searchDescription, null);

    jPanel4.add(optionsPanel, java.awt.BorderLayout.WEST);
  }

  protected void findNext_actionPerformed(ActionEvent e)
  {
  }

  protected void findAll_actionPerformed(ActionEvent e)
  {
  }

  protected void textfield_keyPressed(KeyEvent e)
  {
    if (e.getKeyCode() == KeyEvent.VK_ENTER)
    {
      e.consume();
      findNext_actionPerformed(null);
    }
  }

  public void createNewGroup_actionPerformed(ActionEvent e)
  {
  }

  public void textfield_caretUpdate(CaretEvent e)
  {
    if (textfield.getText().indexOf(">") > -1)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          String str = textfield.getText();
          AlignmentI al = null;
          try
          {
            al = new FormatAdapter().readFile(str, "Paste", "FASTA");
          } catch (Exception ex)
          {
          }
          if (al != null && al.getHeight() > 0)
          {
            str = jalview.analysis.AlignSeq.extractGaps(
                    jalview.util.Comparison.GapChars, al.getSequenceAt(0)
                            .getSequenceAsString());

            textfield.setText(str);
          }
        }
      });
    }
  }
}
