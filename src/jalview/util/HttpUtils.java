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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HttpUtils
{

  /**
   * Returns true if it is possible to open an input stream at the given URL,
   * else false. The input stream is closed.
   * 
   * @param url
   * @return
   */
  public static boolean isValidUrl(String url)
  {
    InputStream is = null;
    try
    {
      is = new URL(url).openStream();
      if (is != null)
      {
        return true;
      }
    } catch (IOException x)
    {
      // MalformedURLException, FileNotFoundException
      return false;
    } finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
    return false;
  }

  public static boolean startsWithHttpOrHttps(String file)
  {
    return file.startsWith("http://") || file.startsWith("https://");
  }

}
