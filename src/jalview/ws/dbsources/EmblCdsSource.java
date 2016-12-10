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
package jalview.ws.dbsources;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;

import com.stevesoft.pat.Regex;

public class EmblCdsSource extends EmblXmlSource
{

  public EmblCdsSource()
  {
    super();
  }

  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  @Override
  public Regex getAccessionValidator()
  {
    return new Regex("^[A-Z]+[0-9]+");
  }

  @Override
  public String getDbSource()
  {
    return DBRefSource.EMBLCDS;
  }

  @Override
  public String getDbVersion()
  {
    return "0"; // TODO : this is dynamically set for a returned record - not
    // tied to proxy
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    if (queries.indexOf(".") > -1)
    {
      queries = queries.substring(0, queries.indexOf("."));
    }
    return getEmblSequenceRecords(DBRefSource.EMBLCDS, queries);
  }

  @Override
  public boolean isValidReference(String accession)
  {
    // most embl CDS refs look like ..
    // TODO: improve EMBLCDS regex
    return (accession == null || accession.length() < 2) ? false
            : getAccessionValidator().search(accession);
  }

  /**
   * cDNA for LDHA_CHICK swissprot sequence
   */
  @Override
  public String getTestQuery()
  {
    return "CAA37824";
  }

  @Override
  public String getDbName()
  {
    return "EMBL (CDS)";
  }

  @Override
  public int getTier()
  {
    return 0;
  }

}
