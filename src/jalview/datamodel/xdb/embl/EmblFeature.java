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
package jalview.datamodel.xdb.embl;

import jalview.datamodel.DBRefEntry;

import java.util.Vector;

/**
 * Data model for a &lt;feature&gt; element returned from an EMBL query reply
 * 
 * @see embl_mapping.xml
 */
public class EmblFeature
{
  String name;

  Vector<DBRefEntry> dbRefs;

  Vector<Qualifier> qualifiers;

  Vector<EmblFeatureLocations> locations;

  /**
   * @return the dbRefs
   */
  public Vector<DBRefEntry> getDbRefs()
  {
    return dbRefs;
  }

  /**
   * @param dbRefs
   *          the dbRefs to set
   */
  public void setDbRefs(Vector<DBRefEntry> dbRefs)
  {
    this.dbRefs = dbRefs;
  }

  /**
   * @return the locations
   */
  public Vector<EmblFeatureLocations> getLocations()
  {
    return locations;
  }

  /**
   * @param locations
   *          the locations to set
   */
  public void setLocations(Vector<EmblFeatureLocations> locations)
  {
    this.locations = locations;
  }

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return the qualifiers
   */
  public Vector<Qualifier> getQualifiers()
  {
    return qualifiers;
  }

  /**
   * @param qualifiers
   *          the qualifiers to set
   */
  public void setQualifiers(Vector<Qualifier> qualifiers)
  {
    this.qualifiers = qualifiers;
  }
}
