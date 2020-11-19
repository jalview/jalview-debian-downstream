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
package jalview.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class BackupFilenameFilter implements FilenameFilter
{

  public String base;

  public String template;

  public int digits;

  public BackupFilenameFilter(String base, String template, int digits)
  {
    this.base = base;
    this.template = template;
    this.digits = digits;
  }

  @Override
  public boolean accept(File dir, String filename)
  {
    try
    {
      File file = new File(
              dir.getCanonicalPath() + File.separatorChar + filename);
      if (file.isDirectory())
      {
        // backup files aren't dirs!
        return false;
      }
    } catch (IOException e)
    {
      System.out.println("IOException when checking file '" + filename
              + "' is a backupfile");
    }

    BackupFilenameParts bffp = new BackupFilenameParts(filename, base,
            template, digits);
    return bffp.isBackupFile();
  }

}
