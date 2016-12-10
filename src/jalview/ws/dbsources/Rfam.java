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

import jalview.datamodel.DBRefSource;

import com.stevesoft.pat.Regex;

/**
 * Contains methods for fetching sequences from Rfam database
 * 
 * @author Lauren Michelle Lui
 */
abstract public class Rfam extends Xfam
{

  public Rfam()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator() Left here for
   * consistency with Pfam class
   */
  @Override
  public String getAccessionSeparator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator() * Left here for
   */
  @Override
  public Regex getAccessionValidator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * Left here for consistency with Pfam class
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource() public String getDbSource() { *
   * this doesn't work - DbSource is key for the hash of DbSourceProxy instances
   * - 1:many mapping for DbSource to proxy will be lost. * suggest : RFAM is an
   * 'alignment' source - means proxy is higher level than a sequence source.
   * return jalview.datamodel.DBRefSource.RFAM; }
   */

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Returns base URL for selected Rfam alignment type
   * 
   * @return RFAM URL stub for this DbSource
   */
  @Override
  protected abstract String getXFAMURL();

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    return accession.indexOf("RF") == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.dbsources.Xfam#getXfamSource()
   */
  @Override
  public String getXfamSource()
  {
    return DBRefSource.RFAM;
  }

}
