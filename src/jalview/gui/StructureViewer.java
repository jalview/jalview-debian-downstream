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
package jalview.gui;

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.structure.StructureSelectionManager;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * proxy for handling structure viewers.
 * 
 * this allows new views to be created with the currently configured viewer, the
 * preferred viewer to be set/read and existing views created previously with a
 * particular viewer to be recovered
 * 
 * @author jprocter
 */
public class StructureViewer
{
  StructureSelectionManager ssm;

  public enum ViewerType
  {
    JMOL, CHIMERA
  };

  public ViewerType getViewerType()
  {
    String viewType = Cache.getDefault(Preferences.STRUCTURE_DISPLAY,
            ViewerType.JMOL.name());
    return ViewerType.valueOf(viewType);
  }

  public void setViewerType(ViewerType type)
  {
    Cache.setProperty(Preferences.STRUCTURE_DISPLAY, type.name());
  }

  public StructureViewer(StructureSelectionManager structureSelectionManager)
  {
    ssm = structureSelectionManager;
  }

  /**
   * View multiple PDB entries, each with associated sequences
   * 
   * @param pdbs
   * @param seqsForPdbs
   * @param ap
   * @return
   */
  public JalviewStructureDisplayI viewStructures(PDBEntry[] pdbs,
          SequenceI[][] seqsForPdbs, AlignmentPanel ap)
  {
    JalviewStructureDisplayI viewer = onlyOnePdb(pdbs, seqsForPdbs, ap);
    if (viewer != null)
    {
      return viewer;
    }
    return viewStructures(getViewerType(), pdbs, seqsForPdbs, ap);
  }

  /**
   * A strictly temporary method pending JAL-1761 refactoring. Determines if all
   * the passed PDB entries are the same (this is the case if selected sequences
   * to view structure for are chains of the same structure). If so, calls the
   * single-pdb version of viewStructures and returns the viewer, else returns
   * null.
   * 
   * @param pdbs
   * @param seqsForPdbs
   * @param ap
   * @return
   */
  private JalviewStructureDisplayI onlyOnePdb(PDBEntry[] pdbs,
          SequenceI[][] seqsForPdbs, AlignmentPanel ap)
  {
    List<SequenceI> seqs = new ArrayList<SequenceI>();
    if (pdbs == null || pdbs.length == 0)
    {
      return null;
    }
    int i = 0;
    String firstFile = pdbs[0].getFile();
    for (PDBEntry pdb : pdbs)
    {
      String pdbFile = pdb.getFile();
      if (pdbFile == null || !pdbFile.equals(firstFile))
      {
        return null;
      }
      SequenceI[] pdbseqs = seqsForPdbs[i++];
      if (pdbseqs != null)
      {
        for (SequenceI sq : pdbseqs)
        {
          seqs.add(sq);
        }
      }
    }
    return viewStructures(pdbs[0],
            seqs.toArray(new SequenceI[seqs.size()]), ap);
  }

  public JalviewStructureDisplayI viewStructures(PDBEntry pdb,
          SequenceI[] seqsForPdb, AlignmentPanel ap)
  {
    return viewStructures(getViewerType(), pdb, seqsForPdb, ap);
  }

  protected JalviewStructureDisplayI viewStructures(ViewerType viewerType,
          PDBEntry[] pdbs, SequenceI[][] seqsForPdbs, AlignmentPanel ap)
  {
    JalviewStructureDisplayI sview = null;
    if (viewerType.equals(ViewerType.JMOL))
    {
      sview = new AppJmol(ap, pdbs, ap.av.collateForPDB(pdbs));
    }
    else if (viewerType.equals(ViewerType.CHIMERA))
    {
      sview = new ChimeraViewFrame(pdbs, ap.av.collateForPDB(pdbs), ap);
    }
    else
    {
      Cache.log.error("Unknown structure viewer type "
              + getViewerType().toString());
    }
    return sview;
  }

  protected JalviewStructureDisplayI viewStructures(ViewerType viewerType,
          PDBEntry pdb, SequenceI[] seqsForPdb, AlignmentPanel ap)
  {
    JalviewStructureDisplayI sview = null;
    if (viewerType.equals(ViewerType.JMOL))
    {
      sview = new AppJmol(pdb, seqsForPdb, null, ap);
    }
    else if (viewerType.equals(ViewerType.CHIMERA))
    {
      sview = new ChimeraViewFrame(pdb, seqsForPdb, null, ap);
    }
    else
    {
      Cache.log.error("Unknown structure viewer type "
              + getViewerType().toString());
    }
    return sview;
  }

  /**
   * Create a new panel controlling a structure viewer.
   * 
   * @param type
   * @param pdbf
   * @param id
   * @param sq
   * @param alignPanel
   * @param viewerData
   * @param fileloc
   * @param rect
   * @param vid
   * @return
   */
  public JalviewStructureDisplayI createView(ViewerType type,
          String[] pdbf, String[] id, SequenceI[][] sq,
          AlignmentPanel alignPanel, StructureViewerModel viewerData,
          String fileloc, Rectangle rect, String vid)
  {
    final boolean useinViewerSuperpos = viewerData.isAlignWithPanel();
    final boolean usetoColourbyseq = viewerData.isColourWithAlignPanel();
    final boolean viewerColouring = viewerData.isColourByViewer();

    JalviewStructureDisplayI sview = null;
    switch (type)
    {
    case JMOL:
      sview = new AppJmol(pdbf, id, sq, alignPanel, usetoColourbyseq,
              useinViewerSuperpos, viewerColouring, fileloc, rect, vid);
      break;
    case CHIMERA:
      Cache.log.error("Unsupported structure viewer type "
              + type.toString());
      break;
    default:
      Cache.log.error("Unknown structure viewer type " + type.toString());
    }
    return sview;
  }

}
