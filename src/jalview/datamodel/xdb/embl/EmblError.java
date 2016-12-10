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
package jalview.datamodel.xdb.embl;

/**
 * Data model mapped from any &lt;error&gt; elements returned from an EMBL query
 * 
 * @see embl_mapping.xml
 */
public class EmblError
{
  String accession;

  /**
   * @return the accession
   */
  public String getAccession()
  {
    return accession;
  }

  /**
   * @param accession
   *          the accession to set
   */
  public void setAccession(String accession)
  {
    this.accession = accession;
  }
}
