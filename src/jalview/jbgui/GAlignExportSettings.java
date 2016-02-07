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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class GAlignExportSettings extends JPanel
{
  protected JPanel hiddenRegionConfPanel = new JPanel();

  protected JPanel complexExportPanel = new JPanel();

  protected JPanel optionsPanel = new JPanel();

  protected JPanel actionPanel = new JPanel();

  protected BorderLayout hiddenRegionLayout = new BorderLayout();

  protected BorderLayout complexExportLayout = new BorderLayout();

  protected BorderLayout mainLayout = new BorderLayout();

  protected JCheckBox chkAll = new JCheckBox();

  protected JCheckBox chkHiddenSeqs = new JCheckBox();

  protected JCheckBox chkHiddenCols = new JCheckBox();

  protected JCheckBox chkExportAnnots = new JCheckBox();

  protected JCheckBox chkExportFeats = new JCheckBox();

  protected JCheckBox chkExportGrps = new JCheckBox();

  JButton btnOk = new JButton();

  JButton btnCancel = new JButton();

  private boolean hasHiddenSeq, hasHiddenCols, isComplexAlignFile,
          showDialog;

  public GAlignExportSettings(boolean hasHiddenSeq, boolean hasHiddenCols,
          String alignFileFormat)
  {
    this.hasHiddenSeq = hasHiddenSeq;
    this.hasHiddenCols = hasHiddenCols;
    String[] complexFormats = { "JSON", "HTML" };

    for (String format : complexFormats)
    {
      if (format.equalsIgnoreCase(alignFileFormat))
      {
        this.isComplexAlignFile = true;
        break;
      }
    }
    if (this.hasHiddenCols || this.hasHiddenSeq || this.isComplexAlignFile)
    {
      this.showDialog = true;
    }
    init();
  }

  public void init()
  {
    chkHiddenSeqs.setText(MessageManager
            .getString("action.export_hidden_sequences"));
    chkHiddenCols.setText(MessageManager
            .getString("action.export_hidden_columns"));
    chkExportAnnots.setText(MessageManager
            .getString("action.export_annotations"));
    chkExportFeats.setText(MessageManager
            .getString("action.export_features"));
    chkExportGrps.setText(MessageManager.getString("action.export_groups"));
    btnOk.setText(MessageManager.getString("action.ok"));
    btnCancel.setText(MessageManager.getString("action.cancel"));
    chkAll.setText(MessageManager.getString("action.select_all"));

    hiddenRegionConfPanel.setLayout(hiddenRegionLayout);
    complexExportPanel.setLayout(complexExportLayout);
    setLayout(mainLayout);

    chkAll.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        checkAllAction();
      }
    });

    btnOk.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });

    btnCancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });

    // hiddenRegionConfPanel.add(chkAll, BorderLayout.NORTH);
    hiddenRegionConfPanel.add(chkHiddenSeqs, BorderLayout.CENTER);
    hiddenRegionConfPanel.add(chkHiddenCols, BorderLayout.SOUTH);
    chkHiddenSeqs.setEnabled(hasHiddenSeq);
    chkHiddenCols.setEnabled(hasHiddenCols);

    complexExportPanel.add(chkExportAnnots, BorderLayout.NORTH);
    complexExportPanel.add(chkExportFeats, BorderLayout.CENTER);
    complexExportPanel.add(chkExportGrps, BorderLayout.SOUTH);

    actionPanel.add(chkAll);

    if (this.isComplexAlignFile)
    {
      optionsPanel.add(complexExportPanel);
    }

    if (hasHiddenSeq || hasHiddenCols)
    {
      optionsPanel.add(hiddenRegionConfPanel);
    }

    actionPanel.add(btnCancel);
    actionPanel.add(btnOk);

    add(optionsPanel, BorderLayout.NORTH);
    add(actionPanel, BorderLayout.SOUTH);

  }

  private void checkAllAction()
  {
    boolean isSelected = chkAll.isSelected();
    chkHiddenSeqs.setSelected(chkHiddenSeqs.isEnabled() && isSelected);
    chkHiddenCols.setSelected(chkHiddenCols.isEnabled() && isSelected);
    chkExportAnnots.setSelected(isComplexAlignFile
            && chkExportAnnots.isEnabled() && isSelected);
    chkExportFeats.setSelected(isComplexAlignFile
            && chkExportFeats.isEnabled() && isSelected);
    chkExportGrps.setSelected(isComplexAlignFile
            && chkExportGrps.isEnabled() && isSelected);
  }

  public boolean isShowDialog()
  {
    return showDialog;
  }

  public void setShowDialog(boolean showDialog)
  {
    this.showDialog = showDialog;
  }

  public abstract void ok_actionPerformed(ActionEvent e);

  public abstract void cancel_actionPerformed(ActionEvent e);
}
