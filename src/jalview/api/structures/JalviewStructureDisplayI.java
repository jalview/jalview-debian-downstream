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
package jalview.api.structures;

import jalview.schemes.ColourSchemeI;
import jalview.structures.models.AAStructureBindingModel;

public interface JalviewStructureDisplayI
{

  AAStructureBindingModel getBinding();

  /**
   * @return true if there is an active GUI handling a structure display
   */
  boolean isVisible();

  /**
   * enable or disable the structure display - note this might just hide or show
   * a GUI element, but not actually reset the display
   * 
   * @param b
   */
  void setVisible(boolean b);

  /**
   * free up any external resources that were used by this display and collect
   * garbage
   */
  void dispose();

  /**
   * Shutdown any Jalview structure viewing processes started by this display
   * 
   * @param closeExternalViewer
   *          if true, force close any linked external viewer process
   */
  void closeViewer(boolean closeExternalViewer);

  /**
   * apply a colourscheme to the structures in the viewer
   * 
   * @param colourScheme
   */
  void setJalviewColourScheme(ColourSchemeI colourScheme);

}
