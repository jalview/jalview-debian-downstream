/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.8.2)
 * Copyright (C) 2014 The Jalview Authors
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

import jalview.gui.Desktop;
import jalview.gui.JvSwingUtils;
import jalview.jbgui.PDBDocFieldPreferences.PreferenceSource;
import jalview.util.MessageManager;
import jalview.ws.dbsources.PDBRestClient.PDBDocField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * GUI layout for PDB Fetch Panel
 * 
 * @author tcnofoegbu
 *
 */
@SuppressWarnings("serial")
public abstract class GPDBSearchPanel extends JPanel
{
  protected String frameTitle = MessageManager
          .getString("label.pdb_sequence_getcher");

  protected JInternalFrame mainFrame = new JInternalFrame(frameTitle);

  protected JComboBox<PDBDocField> cmb_searchTarget = new JComboBox<PDBDocField>();

  protected JButton btn_ok = new JButton();

  protected JButton btn_back = new JButton();

  protected JButton btn_cancel = new JButton();

  protected JTextField txt_search = new JTextField(20);

  protected JTable tbl_summary = new JTable()
  {
    public String getToolTipText(MouseEvent evt)
    {
      String toolTipText = null;
      java.awt.Point pnt = evt.getPoint();
      int rowIndex = rowAtPoint(pnt);
      int colIndex = columnAtPoint(pnt);

      try
      {
        toolTipText = getValueAt(rowIndex, colIndex).toString();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
      toolTipText = (toolTipText == null ? null
              : (toolTipText.length() > 500 ? JvSwingUtils.wrapTooltip(
                      true, toolTipText.subSequence(0, 500) + "...")
                      : JvSwingUtils.wrapTooltip(true, toolTipText)));

      return toolTipText;
    }
  };

  protected StringBuilder errorWarning = new StringBuilder();

  protected JScrollPane scrl_searchResult = new JScrollPane(tbl_summary);

  protected ImageIcon warningImage = new ImageIcon(getClass().getResource(
          "/images/warning.gif"));

  protected ImageIcon loadingImage = new ImageIcon(getClass().getResource(
          "/images/loading.gif"));

  protected JLabel lbl_warning = new JLabel(warningImage);

  protected JLabel lbl_loading = new JLabel(loadingImage);

  private JTabbedPane tabbedPane = new JTabbedPane();

  private PDBDocFieldPreferences pdbDocFieldPrefs = new PDBDocFieldPreferences(
          PreferenceSource.SEARCH_SUMMARY);

  private JPanel pnl_actions = new JPanel();

  private JPanel pnl_results = new JPanel();

  private JPanel pnl_inputs = new JPanel();

  private BorderLayout mainLayout = new BorderLayout();

  protected PDBDocField[] previousWantedFields;

  public GPDBSearchPanel()
  {
    try
    {
      jbInit();
      mainFrame.invalidate();
      mainFrame.pack();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Initializes the GUI default properties
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    lbl_warning.setVisible(false);
    lbl_warning.setFont(new java.awt.Font("Verdana", 0, 12));
    lbl_loading.setVisible(false);
    lbl_loading.setFont(new java.awt.Font("Verdana", 0, 12));

    tbl_summary.setAutoCreateRowSorter(true);
    tbl_summary.getTableHeader().setReorderingAllowed(false);
    tbl_summary.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        validateSelection();
      }

      public void mouseReleased(MouseEvent e)
      {
        validateSelection();
      }
    });

    btn_back.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_back.setText(MessageManager.getString("action.back"));
    btn_back.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        btn_back_ActionPerformed();
      }
    });

    btn_ok.setEnabled(false);
    btn_ok.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_ok.setText(MessageManager.getString("action.ok"));
    btn_ok.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        btn_ok_ActionPerformed();
      }
    });
    btn_cancel.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_cancel.setText(MessageManager.getString("action.cancel"));
    btn_cancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        btn_cancel_ActionPerformed();
      }
    });

    scrl_searchResult.setPreferredSize(new Dimension(500, 300));
    scrl_searchResult
            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    cmb_searchTarget.setFont(new java.awt.Font("Verdana", 0, 12));
    cmb_searchTarget.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String tooltipText;
        if ("all".equalsIgnoreCase(getCmbSearchTarget().getSelectedItem()
                .toString()))
        {
          tooltipText = MessageManager.getString("label.search_all");
        }
        else if ("pdb id".equalsIgnoreCase(getCmbSearchTarget()
                .getSelectedItem().toString()))
        {
          tooltipText = MessageManager
                  .getString("label.separate_multiple_accession_ids");
        }
        else
        {
          tooltipText = MessageManager.formatMessage(
                  "label.separate_multiple_query_values",
                  new Object[] { getCmbSearchTarget().getSelectedItem()
                          .toString() });
        }
        txt_search.setToolTipText(JvSwingUtils.wrapTooltip(true,
                tooltipText));
        txt_search_ActionPerformed();
      }
    });

    populateCmbSearchTargetOptions();

    txt_search.setFont(new java.awt.Font("Verdana", 0, 12));

    txt_search.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          if (txt_search.getText() == null
                  || txt_search.getText().isEmpty())
          {
            return;
          }
          if ("pdb id".equalsIgnoreCase(getCmbSearchTarget()
                  .getSelectedItem().toString()))
          {
            transferToSequenceFetcher(txt_search.getText());
          }
        }
      }
    });

    txt_search.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformed();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformed();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformed();
      }
    });

    final String searchTabTitle = MessageManager
            .getString("label.search_result");
    final String configureCols = MessageManager
            .getString("label.configure_displayed_columns");
    ChangeListener changeListener = new ChangeListener()
    {
      public void stateChanged(ChangeEvent changeEvent)
      {
        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
                .getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        if (sourceTabbedPane.getTitleAt(index).equals(configureCols))
        {
          btn_back.setEnabled(false);
          btn_cancel.setEnabled(false);
          btn_ok.setEnabled(false);
          previousWantedFields = PDBDocFieldPreferences
                  .getSearchSummaryFields().toArray(new PDBDocField[0]);
        }
        if (sourceTabbedPane.getTitleAt(index).equals(searchTabTitle))
        {
          btn_back.setEnabled(true);
          btn_cancel.setEnabled(true);
          if (wantedFieldsUpdated())
          {
            txt_search_ActionPerformed();
          }
          else
          {
            validateSelection();
          }
        }
      }
    };
    tabbedPane.addChangeListener(changeListener);
    tabbedPane.setPreferredSize(new Dimension(500, 300));
    tabbedPane.add(searchTabTitle, scrl_searchResult);
    tabbedPane.add(configureCols, pdbDocFieldPrefs);

    pnl_actions.add(btn_back);
    pnl_actions.add(btn_ok);
    pnl_actions.add(btn_cancel);

    pnl_results.add(tabbedPane);
    pnl_inputs.add(cmb_searchTarget);
    pnl_inputs.add(txt_search);
    pnl_inputs.add(lbl_loading);
    pnl_inputs.add(lbl_warning);

    this.setLayout(mainLayout);
    this.add(pnl_inputs, java.awt.BorderLayout.NORTH);
    this.add(pnl_results, java.awt.BorderLayout.CENTER);
    this.add(pnl_actions, java.awt.BorderLayout.SOUTH);
    mainFrame.setVisible(true);
    mainFrame.setContentPane(this);
    mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Desktop.addInternalFrame(mainFrame, frameTitle, 800, 400);
  }

  public boolean wantedFieldsUpdated()
  {
    if (previousWantedFields == null)
    {
      return true;
    }

    return Arrays.equals(PDBDocFieldPreferences.getSearchSummaryFields()
            .toArray(new PDBDocField[0]), previousWantedFields) ? false
            : true;

  }

  public void validateSelection()
  {
    if (tbl_summary.getSelectedRows().length > 0)
    {
      btn_ok.setEnabled(true);
    }
    else
    {
      btn_ok.setEnabled(false);
    }
  }

  public JComboBox<PDBDocField> getCmbSearchTarget()
  {
    return cmb_searchTarget;
  }

  public JTextField getTxtSearch()
  {
    return txt_search;
  }

  public JInternalFrame getMainFrame()
  {
    return mainFrame;
  }

  public abstract void transferToSequenceFetcher(String ids);

  public abstract void txt_search_ActionPerformed();

  public abstract void btn_ok_ActionPerformed();

  public abstract void btn_back_ActionPerformed();

  public abstract void btn_cancel_ActionPerformed();

  public abstract void populateCmbSearchTargetOptions();

}
