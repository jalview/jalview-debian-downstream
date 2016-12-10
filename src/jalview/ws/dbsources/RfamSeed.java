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

/**
 * Flyweight class specifying retrieval of Seed family alignments from RFAM
 * 
 * @author Lauren Michelle Lui
 * 
 */
public class RfamSeed extends Rfam
{
  public RfamSeed()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.dbsources.Rfam#getRFAMURL()
   */
  @Override
  protected String getXFAMURL()
  {
    return "http://rfam.xfam.org/family/";
    // Janelia Farms url
    // "http://rfam.janelia.org/cgi-bin/getalignment?type=seed&fmt=stockholm&acc=";
  }

  @Override
  public String getXFAMURLSUFFIX()
  {
    return "/alignment";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.seqfetcher.DbSourceProxy#getDbName()
   */
  @Override
  public String getDbName()
  {
    return "RFAM (Seed)";
  }

  @Override
  public String getDbSource()
  {
    return getDbName(); // so we have unique DbSource string.
  }

  @Override
  public String getTestQuery()
  {
    return "RF00014";
  } // http://rfam.janelia.org/cgi-bin/getdesc?acc=RF00014

  @Override
  public String getDbVersion()
  {
    return null;
  }

  @Override
  public int getTier()
  {
    return 0;
  }
}
