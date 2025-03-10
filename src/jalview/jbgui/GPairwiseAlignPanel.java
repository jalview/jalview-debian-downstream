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
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GPairwiseAlignPanel extends JPanel
{
  protected JScrollPane scrollPane = new JScrollPane();

  protected JTextArea textarea = new JTextArea();

  protected JButton viewInEditorButton = new JButton();

  JPanel jPanel1 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  /**
   * Creates a new GPairwiseAlignPanel object.
   */
  public GPairwiseAlignPanel()
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
    this.setLayout(borderLayout1);
    textarea.setFont(new java.awt.Font("Monospaced", 0, 12));
    textarea.setText("");
    textarea.setWrapStyleWord(false);
    viewInEditorButton.setFont(new java.awt.Font("Verdana", 0, 12));
    viewInEditorButton.setText(
            MessageManager.getString("label.view_alignment_editor"));
    viewInEditorButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        viewInEditorButton_actionPerformed(e);
      }
    });
    this.add(scrollPane, BorderLayout.CENTER);
    scrollPane.getViewport().add(textarea, null);
    this.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(viewInEditorButton, null);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void viewInEditorButton_actionPerformed(ActionEvent e)
  {
  }
}
