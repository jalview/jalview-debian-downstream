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
  int modelNo;

  private String pdbFile;

  private String chain;

  private int pdbResNum;

  private int atomIndex;

  /**
   * Parses a Chimera atomspec e.g. #1:12.A to construct an AtomSpec model (with
   * null pdb file name)
   * 
   * @param spec
   * @return
   * @throw IllegalArgumentException if the spec cannot be parsed, or represents
   *        more than one residue
   */
  public static AtomSpec fromChimeraAtomspec(String spec)
  {
    int colonPos = spec.indexOf(":");
    if (colonPos == -1)
    {
      throw new IllegalArgumentException(spec);
    }

    int hashPos = spec.indexOf("#");
    if (hashPos == -1 && colonPos != 0)
    {
      // # is missing but something precedes : - reject
      throw new IllegalArgumentException(spec);
    }

    String modelSubmodel = spec.substring(hashPos + 1, colonPos);
    int dotPos = modelSubmodel.indexOf(".");
    int modelId = 0;
    try
    {
      modelId = Integer.valueOf(dotPos == -1 ? modelSubmodel
              : modelSubmodel.substring(0, dotPos));
    } catch (NumberFormatException e)
    {
      // ignore, default to model 0
    }

    String residueChain = spec.substring(colonPos + 1);
    dotPos = residueChain.indexOf(".");
    int resNum = 0;
    try
    {
      resNum = Integer.parseInt(dotPos == -1 ? residueChain
              : residueChain.substring(0, dotPos));
    } catch (NumberFormatException e)
    {
      // could be a range e.g. #1:4-7.B
      throw new IllegalArgumentException(spec);
    }

    String chainId = dotPos == -1 ? "" : residueChain.substring(dotPos + 1);

    return new AtomSpec(modelId, chainId, resNum, 0);
  }

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

  /**
   * Constructor
   * 
   * @param modelId
   * @param chainId
   * @param resNo
   * @param atomNo
   */
  public AtomSpec(int modelId, String chainId, int resNo, int atomNo)
  {
    this.modelNo = modelId;
    this.chain = chainId;
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

  public int getModelNumber()
  {
    return modelNo;
  }

  public void setPdbFile(String file)
  {
    pdbFile = file;
  }

  @Override
  public String toString()
  {
    return "pdbFile: " + pdbFile + ", chain: " + chain + ", res: "
            + pdbResNum + ", atom: " + atomIndex;
  }
}
