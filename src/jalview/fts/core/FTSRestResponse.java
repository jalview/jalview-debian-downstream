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

package jalview.fts.core;

import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;

import java.util.Collection;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Represents the response model generated by the FTSRestClient upon successful
 * execution of a given FTS request
 * 
 * @author tcnofoegbu
 *
 */
public class FTSRestResponse
{
  private int numberOfItemsFound;

  private String responseTime;

  private Collection<FTSData> searchSummary;

  public int getNumberOfItemsFound()
  {
    return numberOfItemsFound;
  }

  public void setNumberOfItemsFound(int itemFound)
  {
    this.numberOfItemsFound = itemFound;
  }

  public String getResponseTime()
  {
    return responseTime;
  }

  public void setResponseTime(String responseTime)
  {
    this.responseTime = responseTime;
  }

  public Collection<FTSData> getSearchSummary()
  {
    return searchSummary;
  }

  public void setSearchSummary(Collection<FTSData> searchSummary)
  {
    this.searchSummary = searchSummary;
  }

  /**
   * Convenience method to obtain a Table model for a given summary List based
   * on the request parameters
   * 
   * @param request
   *          the FTSRestRequest object which holds useful information for
   *          creating a table model
   * @param summariesList
   *          the summary list which contains the data for populating the
   *          table's rows
   * @return the table model which was dynamically generated
   */
  public static DefaultTableModel getTableModel(FTSRestRequest request,
          Collection<FTSData> summariesList)
  {
    final FTSDataColumnI[] cols = request.getWantedFields()
            .toArray(new FTSDataColumnI[0]);
    final int colOffset = request.getAssociatedSequence() == null ? 0 : 1;
    DefaultTableModel tableModel = new DefaultTableModel()
    {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex)
      {
        if (colOffset == 1 && columnIndex == 0)
        {
          return SequenceI.class;
        }
        return cols[columnIndex - colOffset].getDataType()
                .getDataTypeClass();
      }

    };
    if (request.getAssociatedSequence() != null)
    {
      tableModel.addColumn("Ref Sequence"); // Create sequence column header if
      // exists in the request
    }
    for (FTSDataColumnI field : request.getWantedFields())
    {
      tableModel.addColumn(field.getName()); // Create sequence column header if
                                             // exists in the request
    }

    for (FTSData res : summariesList)
    {
      tableModel.addRow(res.getSummaryData()); // Populate table rows with
                                               // summary list
    }

    return tableModel;
  }

  public static void configureTableColumn(JTable tbl_summary,
          Collection<FTSDataColumnI> wantedFields,
          Map<String, Integer> columnPrefs)
  {
    for (FTSDataColumnI wantedField : wantedFields)
    {
      try
      {
        tbl_summary.getColumn(wantedField.getName())
                .setMinWidth(wantedField.getMinWidth());
        tbl_summary.getColumn(wantedField.getName())
                .setMaxWidth(wantedField.getMaxWidth());
        int prefedWidth = columnPrefs.get(wantedField.getName()) == null
                ? wantedField.getPreferredWidth()
                : columnPrefs.get(wantedField.getName());
        tbl_summary.getColumn(wantedField.getName())
                .setPreferredWidth(prefedWidth);
      } catch (Exception e)
      {
        e.printStackTrace();
      }
      if (wantedField.getDataType().getDataTypeClass() == Double.class)
      {
        DecimalFormatTableCellRenderer dfr = new DecimalFormatTableCellRenderer(
                wantedField.getDataType().isFormtted(),
                wantedField.getDataType().getSignificantFigures());
        tbl_summary.getColumn(wantedField.getName()).setCellRenderer(dfr);
      }
      else if (wantedField.getDataType()
              .getDataTypeClass() == Integer.class)
      {
        DecimalFormatTableCellRenderer dfr = new DecimalFormatTableCellRenderer(
                wantedField.getDataType().isFormtted(),
                wantedField.getDataType().getSignificantFigures());
        tbl_summary.getColumn(wantedField.getName()).setCellRenderer(dfr);
      }
    }
  }

}
