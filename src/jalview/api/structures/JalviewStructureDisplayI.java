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
package jalview.api.structures;

import jalview.api.AlignmentViewPanel;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
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

  /**
   * 
   * @return true if all background sequence/structure binding threads have
   *         completed for this viewer instance
   */
  boolean hasMapping();

  /**
   * Checks if the PDB file is already loaded in this viewer, if so just adds
   * mappings as necessary and answers true, else answers false. This supports
   * the use case of adding additional chains of the same structure to a viewer.
   * 
   * @param seq
   * @param chains
   * @param apanel
   * @param pdbId
   * @return
   */
  boolean addAlreadyLoadedFile(SequenceI[] seq, String[] chains,
          AlignmentViewPanel apanel, String pdbId);

  /**
   * Adds one or more chains (sequences) of a PDB structure to this structure
   * viewer
   * 
   * @param pdbentry
   * @param seq
   * @param chains
   * @param apanel
   * @param pdbId
   * @return
   */
  void addToExistingViewer(PDBEntry pdbentry, SequenceI[] seq,
          String[] chains, AlignmentViewPanel apanel, String pdbId);

  /**
   * refresh GUI after reconfiguring structure(s) and alignment panels
   */
  void updateTitleAndMenus();

  /**
   * Answers true if the viewer should attempt to align any added structures,
   * else false
   * 
   * @return
   */
  boolean isAlignAddedStructures();

  /**
   * Sets the flag for whether added structures should be aligned
   * 
   * @param alignAdded
   */
  void setAlignAddedStructures(boolean alignAdded);

  /**
   * Raise the panel to the top of the stack...
   */
  void raiseViewer();

}
