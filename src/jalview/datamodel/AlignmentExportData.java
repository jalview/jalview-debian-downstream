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
package jalview.datamodel;

import jalview.api.AlignExportSettingI;

public class AlignmentExportData
{
  private AlignmentI alignment;

  private String[] omitHidden;

  private int[] startEnd;

  private AlignExportSettingI settings;

  public AlignmentExportData(AlignmentI align, String[] ommit,
          int[] startEnd, AlignExportSettingI settings)
  {
    this.alignment = align;
    this.omitHidden = ommit;
    this.startEnd = startEnd;
    this.settings = settings;
  }

  public AlignmentI getAlignment()
  {
    return alignment;
  }

  public void setAlignment(AlignmentI alignment)
  {
    this.alignment = alignment;
  }

  public String[] getOmitHidden()
  {
    return omitHidden;
  }

  public void setOmitHidden(String[] omitHidden)
  {
    this.omitHidden = omitHidden;
  }

  public int[] getStartEndPostions()
  {
    return startEnd;
  }

  public void setStartEndPostions(int[] startEnd)
  {
    this.startEnd = startEnd;
  }

  public AlignExportSettingI getSettings()
  {
    return settings;
  }

  public void setSettings(AlignExportSettingI settings)
  {
    this.settings = settings;
  }
}
