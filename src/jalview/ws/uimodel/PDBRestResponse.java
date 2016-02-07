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

package jalview.ws.uimodel;

import jalview.datamodel.SequenceI;
import jalview.ws.dbsources.PDBRestClient.PDBDocField;

import java.util.Collection;
import java.util.Objects;

import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONObject;

/**
 * Represents the response model produced by the PDBRestClient upon successful
 * execution of a given request
 * 
 * @author tcnofoegbu
 *
 */
public class PDBRestResponse
{
  private int numberOfItemsFound;

  private String responseTime;

  private Collection<PDBResponseSummary> searchSummary;

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

  public Collection<PDBResponseSummary> getSearchSummary()
  {
    return searchSummary;
  }

  public void setSearchSummary(Collection<PDBResponseSummary> searchSummary)
  {
    this.searchSummary = searchSummary;
  }

  /**
   * Convenience method to obtain a Table model for a given summary List based
   * on the request parameters
   * 
   * @param request
   *          the PDBRestRequest object which holds useful information for
   *          creating a table model
   * @param summariesList
   *          the summary list which contains the data for populating the
   *          table's rows
   * @return the table model which was dynamically generated
   */
  public static DefaultTableModel getTableModel(PDBRestRequest request,
          Collection<PDBResponseSummary> summariesList)
  {
    DefaultTableModel tableModel = new DefaultTableModel()
    {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };
    if (request.getAssociatedSequence() != null)
    {
      tableModel.addColumn("Ref Sequence"); // Create sequence column header if
      // exists in the request
    }
    for (PDBDocField field : request.getWantedFields())
    {
      tableModel.addColumn(field.getName()); // Create sequence column header if
                                             // exists in the request
    }

    for (PDBResponseSummary res : summariesList)
    {
      tableModel.addRow(res.getSummaryData()); // Populate table rows with
                                               // summary list
    }

    return tableModel;
  }

  /**
   * Model for a unique response summary
   * 
   */
  public class PDBResponseSummary
  {
    private String pdbId;

    private Object[] summaryRowData;

    private SequenceI associatedSequence;

    public PDBResponseSummary(JSONObject pdbJsonDoc, PDBRestRequest request)
    {
      Collection<PDBDocField> diplayFields = request.getWantedFields();
      SequenceI associatedSeq = request.getAssociatedSequence();
      int colCounter = 0;
      summaryRowData = new Object[(associatedSeq != null) ? diplayFields
              .size() + 1 : diplayFields.size()];
      if (associatedSeq != null)
      {
        this.associatedSequence = associatedSeq;
        summaryRowData[0] = associatedSequence;
        colCounter = 1;
      }

      for (PDBDocField field : diplayFields)
      {
        String fieldData = (pdbJsonDoc.get(field.getCode()) == null) ? ""
                : pdbJsonDoc.get(field.getCode()).toString();
        if (field.equals(PDBDocField.PDB_ID))
        {
          this.pdbId = fieldData;
          summaryRowData[colCounter++] = this.pdbId;
        }
        else
        {
          summaryRowData[colCounter++] = fieldData;
        }
      }
    }

    public String getPdbId()
    {
      return pdbId;
    }

    public void setPdbId(String pdbId)
    {
      this.pdbId = pdbId;
    }

    public Object[] getSummaryData()
    {
      return summaryRowData;
    }

    public void setSummaryData(String[] summaryData)
    {
      this.summaryRowData = summaryData;
    }

    /**
     * Returns a string representation of this object;
     */
    @Override
    public String toString()
    {
      StringBuilder summaryFieldValues = new StringBuilder();
      for (Object summaryField : summaryRowData)
      {
        summaryFieldValues.append(summaryField.toString()).append("\t");
      }
      return summaryFieldValues.toString();
    }

    /**
     * Returns hash code value for this object
     */
    @Override
    public int hashCode()
    {
      return Objects.hash(this.pdbId, this.toString());
    }

    /**
     * Indicates whether some object is equal to this one
     */
    @Override
    public boolean equals(Object that)
    {
      if (!(that instanceof PDBResponseSummary))
      {
        return false;
      }
      PDBResponseSummary another = (PDBResponseSummary) that;
      return this.toString().equals(another.toString());
    }

  }

}
