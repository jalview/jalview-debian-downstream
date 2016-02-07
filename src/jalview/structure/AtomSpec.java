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
package jalview.structure;

/**
 * Java bean representing an atom in a PDB (or similar) structure model or
 * viewer
 * 
 * @author gmcarstairs
 *
 */
public class AtomSpec
{
  // TODO clarify do we want pdbFile here, or pdbId?
  // compare highlightAtom in 2.8.2 for JalviewJmolBinding and
  // javascript.MouseOverStructureListener
  private String pdbFile;

  private String chain;

  private int pdbResNum;

  private int atomIndex;

  /**
   * Constructor
   * 
   * @param pdbFile
   * @param chain
   * @param resNo
   * @param atomNo
   */
  public AtomSpec(String pdbFile, String chain, int resNo, int atomNo)
  {
    this.pdbFile = pdbFile;
    this.chain = chain;
    this.pdbResNum = resNo;
    this.atomIndex = atomNo;
  }

  public String getPdbFile()
  {
    return pdbFile;
  }

  public String getChain()
  {
    return chain;
  }

  public int getPdbResNum()
  {
    return pdbResNum;
  }

  public int getAtomIndex()
  {
    return atomIndex;
  }

  @Override
  public String toString()
  {
    return "pdbFile: " + pdbFile + ", chain: " + chain + ", res: "
            + pdbResNum + ", atom: " + atomIndex;
  }
}
