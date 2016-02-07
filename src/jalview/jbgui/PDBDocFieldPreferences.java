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

import jalview.ws.dbsources.PDBRestClient.PDBDocField;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class PDBDocFieldPreferences extends JScrollPane
{
  protected JTable tbl_pdbDocFieldConfig = new JTable();

  protected JScrollPane scrl_pdbDocFieldConfig = new JScrollPane(
          tbl_pdbDocFieldConfig);

  private HashMap<String, PDBDocField> map = new HashMap<String, PDBDocField>();

  private static Collection<PDBDocField> searchSummaryFields = new LinkedHashSet<PDBDocField>();

  private static Collection<PDBDocField> structureSummaryFields = new LinkedHashSet<PDBDocField>();

  public enum PreferenceSource
  {
    SEARCH_SUMMARY, STRUCTURE_CHOOSER, PREFERENCES;
  }

  private PreferenceSource currentSource;

  static
  {
    searchSummaryFields.add(PDBDocField.PDB_ID);
    searchSummaryFields.add(PDBDocField.TITLE);

    structureSummaryFields.add(PDBDocField.PDB_ID);
    structureSummaryFields.add(PDBDocField.TITLE);
  }

  public PDBDocFieldPreferences(PreferenceSource source)
  {
    tbl_pdbDocFieldConfig.setAutoCreateRowSorter(true);
    this.getViewport().add(tbl_pdbDocFieldConfig);
    this.currentSource = source;

    String[] columnNames = null;
    switch (source)
    {
    case SEARCH_SUMMARY:
      columnNames = new String[] { "PDB Field", "Show in search summary" };
      break;
    case STRUCTURE_CHOOSER:
      columnNames = new String[] { "PDB Field", "Show in structure summary" };
      break;
    case PREFERENCES:
      columnNames = new String[] { "PDB Field", "Show in search summary",
          "Show in structure summary" };
      break;
    default:
      break;
    }

    Object[][] data = new Object[PDBDocField.values().length - 1][3];
    int x = 0;
    for (PDBDocField field : PDBDocField.values())
    {
      if (field.getName().equalsIgnoreCase("all"))
      {
        continue;
      }

      switch (source)
      {
      case SEARCH_SUMMARY:
        data[x++] = new Object[] { field.getName(),
            searchSummaryFields.contains(field) };
        break;
      case STRUCTURE_CHOOSER:
        data[x++] = new Object[] { field.getName(),
            structureSummaryFields.contains(field) };
        break;
      case PREFERENCES:
        data[x++] = new Object[] { field.getName(),
            searchSummaryFields.contains(field),
            structureSummaryFields.contains(field) };
        break;
      default:
        break;
      }
      map.put(field.getName(), field);
    }

    PDBFieldTableModel model = new PDBFieldTableModel(columnNames, data);
    tbl_pdbDocFieldConfig.setModel(model);
  }

  public static Collection<PDBDocField> getSearchSummaryFields()
  {
    return searchSummaryFields;
  }

  public static void setSearchSummaryFields(
          Collection<PDBDocField> searchSummaryFields)
  {
    PDBDocFieldPreferences.searchSummaryFields = searchSummaryFields;
  }

  public static Collection<PDBDocField> getStructureSummaryFields()
  {
    return structureSummaryFields;
  }

  public static void setStructureSummaryFields(
          Collection<PDBDocField> structureSummaryFields)
  {
    PDBDocFieldPreferences.structureSummaryFields = structureSummaryFields;
  }

  class PDBFieldTableModel extends AbstractTableModel
  {

    public PDBFieldTableModel(String[] columnNames, Object[][] data)
    {
      this.data = data;
      this.columnNames = columnNames;
    }

    private Object[][] data;

    private String[] columnNames;

    public int getColumnCount()
    {
      return columnNames.length;
    }

    public int getRowCount()
    {
      return data.length;
    }

    public String getColumnName(int col)
    {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
      return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column would
     * contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col)
    {
      // Note that the data/cell address is constant,
      // no matter where the cell appears onscreen.
      // !isPDBID(row, col) ensures the PDB_Id cell is never editable as it
      // serves as a unique id for each row.
      return (col == 1 || col == 2) && !isPDBID(row, col);

    }

    /**
     * Determines whether the data in a given cell is a PDB ID.
     * 
     * @param row
     * @param col
     * @return
     */

    public boolean isPDBID(int row, int col)
    {
      boolean matched = false;
      String name = getValueAt(row, 0).toString();
      PDBDocField pdbField = map.get(name);
      if (pdbField == PDBDocField.PDB_ID)
      {
        matched = true;
      }
      return matched;
    }

    /*
     * Don't need to implement this method unless your table's data can change.
     */
    public void setValueAt(Object value, int row, int col)
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);

      String name = getValueAt(row, 0).toString();
      boolean selected = ((Boolean) value).booleanValue();

      PDBDocField pdbField = map.get(name);

      if (currentSource == PreferenceSource.SEARCH_SUMMARY)
      {
        updatePrefs(searchSummaryFields, pdbField, selected);
      }
      else if (currentSource == PreferenceSource.STRUCTURE_CHOOSER)
      {
        updatePrefs(structureSummaryFields, pdbField, selected);
      }
      else if (currentSource == PreferenceSource.PREFERENCES)
      {
        if (col == 1)
        {
          updatePrefs(searchSummaryFields, pdbField, selected);
        }
        else if (col == 2)
        {
          updatePrefs(structureSummaryFields, pdbField, selected);
        }
      }
    }

    private void updatePrefs(Collection<PDBDocField> prefConfig,
            PDBDocField pdbField, boolean selected)
    {
      if (prefConfig.contains(pdbField) && !selected)
      {
        prefConfig.remove(pdbField);
      }

      if (!prefConfig.contains(pdbField) && selected)
      {
        prefConfig.add(pdbField);
      }
    }

  }
}
