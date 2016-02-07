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

/**
 * Data model for the sequence extracted from an EMBL query reply
 * 
 * @see embl_mapping.xml
 */
public class EmblSequence
{
  String version;

  String sequence;

  String type;

  /**
   * @return the sequence
   */
  public String getSequence()
  {
    return sequence;
  }

  /**
   * @param sequence
   *          the sequence to set
   */
  public void setSequence(String sequence)
  {
    this.sequence = sequence;
  }

  /**
   * @return the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * @return the version
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * @param version
   *          the version to set
   */
  public void setVersion(String version)
  {
    this.version = version;
  }
}
