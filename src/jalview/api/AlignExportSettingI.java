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

package jalview.api;

/**
 * Abstract interface implemented by Alignment Export dialog to retrieve user
 * configurations
 * 
 * @author tcnofoegbu
 *
 */
public interface AlignExportSettingI
{
  /**
   * Checks if hidden sequences should be exported
   * 
   * @return
   */
  public boolean isExportHiddenSequences();

  /**
   * Checks if hidden columns shoulb be exported
   * 
   * @return
   */
  public boolean isExportHiddenColumns();

  /**
   * Checks if Annotations should be exported, note this is available for
   * complex flat file exports like JSON, HTML, GFF
   * 
   * @return
   */
  public boolean isExportAnnotations();

  /**
   * Checks if SequenceFeatures should be exported, note this is available for
   * complex flat file exports like JSON, HTML, GFF
   * 
   * @return
   */
  public boolean isExportFeatures();

  /**
   * Checks if SequenceGroups should be exported, note this is available for
   * complex flat file exports like JSON, HTML, GFF
   * 
   * @return
   */
  public boolean isExportGroups();

  public boolean isCancelled();

}
