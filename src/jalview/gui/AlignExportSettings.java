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
package jalview.gui;

import jalview.api.AlignExportSettingI;
import jalview.bin.Jalview;
import jalview.io.FileFormatI;
import jalview.jbgui.GAlignExportSettings;
import jalview.util.MessageManager;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class AlignExportSettings extends GAlignExportSettings
        implements AlignExportSettingI
{
  boolean cancelled = false;

  JDialog dialog;

  public AlignExportSettings(boolean hasHiddenSeq, boolean hasHiddenCols,
          FileFormatI format)
  {
    super(hasHiddenSeq, hasHiddenCols, format);
    if (!Jalview.isHeadlessMode() && isShowDialog())
    {

      JOptionPane pane = new JOptionPane(null, JOptionPane.DEFAULT_OPTION,
              JvOptionPane.DEFAULT_OPTION, null, new Object[]
              { this });
      dialog = pane.createDialog(Desktop.desktop,
              MessageManager.getString("label.export_settings"));
      dialog.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          cancelled = true;
        }
      });
      dialog.pack();
      dialog.setVisible(true);
      dialog.setContentPane(this);
      dialog.validate();

    }
  }

  @Override
  public void ok_actionPerformed(ActionEvent e)
  {
    cancelled = false;
    dialog.setVisible(false);
    dialog.dispose();
  }

  @Override
  public void cancel_actionPerformed(ActionEvent e)
  {
    cancelled = true;
    dialog.setVisible(false);
    dialog.dispose();
  }

  @Override
  public boolean isExportHiddenSequences()
  {
    return chkHiddenSeqs.isSelected();
  }

  @Override
  public boolean isExportHiddenColumns()
  {
    return chkHiddenCols.isSelected();
  }

  @Override
  public boolean isExportAnnotations()
  {
    return chkExportAnnots.isSelected();
  }

  @Override
  public boolean isExportFeatures()
  {
    return chkExportFeats.isSelected();
  }

  @Override
  public boolean isExportGroups()
  {
    return chkExportGrps.isSelected();
  }

  @Override
  public boolean isCancelled()
  {
    return cancelled;
  }

}
