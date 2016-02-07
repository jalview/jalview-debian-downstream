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

import java.util.Hashtable;

public class PDBEntry
{
  private String file;

  private String type;

  private String id;

  private String chainCode;

  public enum Type
  {
    PDB, FILE
  }

  Hashtable properties;

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof PDBEntry))
    {
      return false;
    }
    if (obj == this)
    {
      return true;
    }
    PDBEntry o = (PDBEntry) obj;
    return (type == o.type || (type != null && o.type != null && o.type
            .equals(type)))
            && (id == o.id || (id != null && o.id != null && o.id
                    .equalsIgnoreCase(id)))
            && (chainCode == o.chainCode || (chainCode != null
                    && o.chainCode != null && o.chainCode
                      .equalsIgnoreCase(chainCode)))
            && (properties == o.properties || (properties != null
                    && o.properties != null && properties
                      .equals(o.properties)));

  }

  /**
   * Default constructor
   */
  public PDBEntry()
  {
  }

  /**
   * Constructor given file path and PDB id.
   * 
   * @param filePath
   */
  // public PDBEntry(String filePath, String pdbId)
  // {
  // this.file = filePath;
  // this.id = pdbId;
  // }

  public PDBEntry(String pdbId, String chain, PDBEntry.Type type,
          String filePath)
  {
    this.id = pdbId;
    this.chainCode = chain;
    this.type = type == null ? null : type.toString();
    this.file = filePath;
  }

  /**
   * Copy constructor.
   * 
   * @param entry
   */
  public PDBEntry(PDBEntry entry)
  {
    file = entry.file;
    type = entry.type;
    id = entry.id;
    chainCode = entry.chainCode;
    if (entry.properties != null)
    {
      properties = (Hashtable) entry.properties.clone();
    }
  }

  public void setFile(String file)
  {
    this.file = file;
  }

  public String getFile()
  {
    return file;
  }

  public void setType(String t)
  {
    this.type = t;
  }

  public void setType(PDBEntry.Type type)
  {
    this.type = type == null ? null : type.toString();
  }

  public String getType()
  {
    return type;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setProperty(Hashtable property)
  {
    this.properties = property;
  }

  public Hashtable getProperty()
  {
    return properties;
  }

  public String getChainCode()
  {
    return chainCode;
  }

  public void setChainCode(String chainCode)
  {
    this.chainCode = chainCode;
  }

  public String toString()
  {
    return id;
  }
}
