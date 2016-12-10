/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
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
package jalview.structure;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;

import java.util.HashMap;

public class StructureMapping
{
  String mappingDetails;

  SequenceI sequence;

  String pdbfile;

  String pdbid;

  String pdbchain;

  public static final int UNASSIGNED_VALUE = -1;

  private static final int PDB_RES_NUM_INDEX = 0;

  private static final int PDB_ATOM_NUM_INDEX = 1;

  // Mapping key is residue index while value is an array containing PDB resNum,
  // and atomNo
  HashMap<Integer, int[]> mapping;

  public StructureMapping(SequenceI seq, String pdbfile, String pdbid,
          String chain, HashMap<Integer, int[]> mapping,
          String mappingDetails)
  {
    sequence = seq;
    this.pdbfile = pdbfile;
    this.pdbid = pdbid;
    this.pdbchain = chain;
    this.mapping = mapping;
    this.mappingDetails = mappingDetails;
  }

  public SequenceI getSequence()
  {
    return sequence;
  }

  public String getChain()
  {
    return pdbchain;
  }

  public String getPdbId()
  {
    return pdbid;
  }

  /**
   * 
   * @param seqpos
   * @return 0 or corresponding atom number for the sequence position
   */
  public int getAtomNum(int seqpos)
  {
    int[] resNumAtomMap = mapping.get(seqpos);
    if (resNumAtomMap != null)
    {
      return resNumAtomMap[PDB_ATOM_NUM_INDEX];
    }
    else
    {
      return UNASSIGNED_VALUE;
    }
  }

  /**
   * 
   * @param seqpos
   * @return 0 or the corresponding residue number for the sequence position
   */
  public int getPDBResNum(int seqpos)
  {
    int[] resNumAtomMap = mapping.get(seqpos);
    if (resNumAtomMap != null)
    {
      return resNumAtomMap[PDB_RES_NUM_INDEX];
    }
    else
    {
      return UNASSIGNED_VALUE;
    }
  }

  /**
   * 
   * @param pdbResNum
   * @return -1 or the corresponding sequence position for a pdb residue number
   */
  public int getSeqPos(int pdbResNum)
  {
    for (Integer seqPos : mapping.keySet())
    {
      if (pdbResNum == getPDBResNum(seqPos))
      {
        return seqPos;
      }
    }
    return UNASSIGNED_VALUE;
  }

  /**
   * transfer a copy of an alignment annotation row in the PDB chain coordinate
   * system onto the mapped sequence
   * 
   * @param ana
   * @return the copy that was remapped to the mapped sequence
   * @note this method will create a copy and add it to the dataset sequence for
   *       the mapped sequence as well as the mapped sequence (if it is not a
   *       dataset sequence).
   */
  public AlignmentAnnotation transfer(AlignmentAnnotation ana)
  {
    AlignmentAnnotation ala_copy = new AlignmentAnnotation(ana);
    SequenceI ds = sequence;
    while (ds.getDatasetSequence() != null)
    {
      ds = ds.getDatasetSequence();
    }
    // need to relocate annotation from pdb coordinates to local sequence
    // -1,-1 doesn't look at pdbresnum but fails to remap sequence positions...

    ala_copy.remap(ds, mapping, -1, -1, 0);
    ds.addAlignmentAnnotation(ala_copy);
    if (ds != sequence)
    {
      // mapping wasn't to an original dataset sequence, so we make a copy on
      // the mapped sequence too
      ala_copy = new AlignmentAnnotation(ala_copy);
      sequence.addAlignmentAnnotation(ala_copy);
    }
    return ala_copy;
  }

  public String getMappingDetailsOutput()
  {
    return mappingDetails;
  }

  public HashMap<Integer, int[]> getMapping()
  {
    return mapping;
  }
}
