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
package jalview.gui;

import jalview.api.StructureSelectionManagerProvider;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;

import javax.swing.JOptionPane;

/**
 * GUI related routines for associating PDB files with sequences
 * 
 * @author JimP
 * 
 */
public class AssociatePdbFileWithSeq
{

  /**
   * assocate the given PDB file with
   * 
   * @param choice
   * @param sequence
   */
  public PDBEntry associatePdbWithSeq(String choice, DataSourceType file,
          SequenceI sequence, boolean prompt,
          StructureSelectionManagerProvider ssmp)
  {
    PDBEntry entry = new PDBEntry();
    StructureFile pdbfile = null;
    pdbfile = StructureSelectionManager.getStructureSelectionManager(ssmp)
            .setMapping(false, new SequenceI[]
            { sequence }, null, choice, file);
    if (pdbfile == null)
    {
      // stacktrace already thrown so just return
      return null;
    }
    if (pdbfile.getId() == null)
    {
      String reply = null;

      if (prompt)
      {
        reply = JvOptionPane.showInternalInputDialog(Desktop.desktop,
                MessageManager
                        .getString("label.couldnt_find_pdb_id_in_file"),
                MessageManager.getString("label.no_pdb_id_in_file"),
                JvOptionPane.QUESTION_MESSAGE);
      }
      if (reply == null)
      {
        return null;
      }

      entry.setId(reply);
    }
    else
    {
      entry.setId(pdbfile.getId());
    }
    entry.setType(PDBEntry.Type.FILE);

    if (pdbfile != null)
    {
      entry.setFile(choice);
      sequence.getDatasetSequence().addPDBId(entry);
      StructureSelectionManager.getStructureSelectionManager(ssmp)
              .registerPDBEntry(entry);
    }
    return entry;
  }
}
