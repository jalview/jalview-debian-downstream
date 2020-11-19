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
package jalview.util;

import org.json.simple.JSONArray;

public class JSONUtils
{

  /**
   * Converts a JSONArray of values to a string as a comma-separated list.
   * Answers null if the array is null or empty.
   * 
   * @param jsonArray
   * @return
   */
  public static String arrayToList(JSONArray jsonArray)
  {
    if (jsonArray == null)
    {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < jsonArray.size(); i++)
    {
      if (i > 0)
      {
        sb.append(",");
      }
      sb.append(jsonArray.get(i).toString());
    }
    return sb.length() == 0 ? null : sb.toString();
  }

}
