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

/**
 * Represents the PDB request to be consumed by the PDBRestClient
 * 
 * @author tcnofoegbu
 *
 */
public class PDBRestRequest
{
  private String fieldToSearchBy;

  private String searchTerm;

  private String fieldToSortBy;

  private SequenceI associatedSequence;

  private boolean allowEmptySequence;

  private int responseSize;

  private boolean isSortAscending;

  private Collection<PDBDocField> wantedFields;// = new
                                               // Collection<PDBDocField>();

  public String getFieldToSearchBy()
  {
    return fieldToSearchBy;
  }

  public void setFieldToSearchBy(String fieldToSearchBy)
  {
    this.fieldToSearchBy = fieldToSearchBy;
  }

  public String getSearchTerm()
  {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm)
  {
    this.searchTerm = searchTerm;
  }

  public boolean isAllowEmptySeq()
  {
    return allowEmptySequence;
  }

  public void setAllowEmptySeq(boolean allowEmptySeq)
  {
    this.allowEmptySequence = allowEmptySeq;
  }

  public int getResponseSize()
  {
    return responseSize;
  }

  public void setResponseSize(int responseSize)
  {
    this.responseSize = responseSize;
  }

  public Collection<PDBDocField> getWantedFields()
  {
    return wantedFields;
  }

  public void setWantedFields(Collection<PDBDocField> wantedFields)
  {
    this.wantedFields = wantedFields;
  }

  public String getFieldToSortBy()
  {
    return fieldToSortBy;
  }

  public void setFieldToSortBy(String fieldToSortBy, boolean isSortAscending)
  {
    this.fieldToSortBy = fieldToSortBy;
    this.isSortAscending = isSortAscending;
  }

  public boolean isAscending()
  {
    return isSortAscending;
  }

  public SequenceI getAssociatedSequence()
  {
    return associatedSequence;
  }

  public void setAssociatedSequence(SequenceI associatedSequence)
  {
    this.associatedSequence = associatedSequence;
  }

  public String getQuery()
  {
    return fieldToSearchBy + searchTerm
            + (isAllowEmptySeq() ? "" : " AND molecule_sequence:['' TO *]");
  }

  public String toString()
  {
    return "Query : " + getQuery() + " sort field: " + fieldToSortBy
            + " isAsc: " + isAscending() + " Associated Seq : "
            + associatedSequence;
  }
}
