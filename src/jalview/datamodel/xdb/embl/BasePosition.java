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
 * Data model for a feature/location/locationElement/basePosition read from an
 * EMBL query reply
 * 
 * @see embl_mapping.xml
 */
public class BasePosition
{
  String type;

  String pos;

  /**
   * @return the pos
   */
  public String getPos()
  {
    return pos;
  }

  /**
   * @param pos
   *          the pos to set
   */
  public void setPos(String pos)
  {
    this.pos = pos;
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
}
