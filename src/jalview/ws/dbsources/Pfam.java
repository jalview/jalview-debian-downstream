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
package jalview.ws.dbsources;

import jalview.bin.Cache;
import jalview.datamodel.DBRefSource;

import com.stevesoft.pat.Regex;

/**
 * TODO: later PFAM is a complex datasource - it could return a tree in addition
 * to an alignment TODO: create interface to pass alignment properties and tree
 * back to sequence fetcher
 * 
 * @author JimP
 * 
 */
abstract public class Pfam extends Xfam
{
  /*
   * append to URLs to retrieve as a gzipped file
   */
  protected static final String GZIPPED = "/gzipped";

  static final String PFAM_BASEURL_KEY = "PFAM_BASEURL";

  private static final String DEFAULT_PFAM_BASEURL = "https://pfam.xfam.org";

  public Pfam()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator()
   */
  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator()
   */
  @Override
  public Regex getAccessionValidator()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource() public String getDbSource() { *
   * this doesn't work - DbSource is key for the hash of DbSourceProxy instances
   * - 1:many mapping for DbSource to proxy will be lost. * suggest : PFAM is an
   * 'alignment' source - means proxy is higher level than a sequence source.
   * return jalview.datamodel.DBRefSource.PFAM; }
   */

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSourceProperties() public Hashtable
   * getDbSourceProperties() {
   * 
   * return null; }
   */

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    return null;
  }

  @Override
  protected String getURLPrefix()
  {
    return Cache.getDefault(PFAM_BASEURL_KEY, DEFAULT_PFAM_BASEURL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    return accession.indexOf("PF") == 0;
  }

  /*
   * public String getDbName() { return "PFAM"; // getDbSource(); }
   */

  @Override
  public String getXfamSource()
  {
    return DBRefSource.PFAM;
  }

}
