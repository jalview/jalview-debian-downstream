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
package jalview.datamodel.xdb.embl;

import jalview.datamodel.DBRefEntry;
import jalview.ws.dbsources.Uniprot;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Vector;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Data model for entries returned from an EMBL query, as marshalled by a Castor
 * binding file
 * 
 * For example: http://www.ebi.ac.uk/Tools/dbfetch/dbfetch/embl/x53828/emblxml
 * 
 * @see embl_mapping.xml
 */
public class EmblFile
{
  Vector<EmblEntry> entries;

  Vector<EmblError> errors;

  String text;

  /**
   * @return the entries
   */
  public Vector<EmblEntry> getEntries()
  {
    return entries;
  }

  /**
   * @param entries
   *          the entries to set
   */
  public void setEntries(Vector<EmblEntry> entries)
  {
    this.entries = entries;
  }

  /**
   * @return the errors
   */
  public Vector<EmblError> getErrors()
  {
    return errors;
  }

  /**
   * @param errors
   *          the errors to set
   */
  public void setErrors(Vector<EmblError> errors)
  {
    this.errors = errors;
  }

  /**
   * Parse an EmblXML file into an EmblFile object
   * 
   * @param file
   * @return parsed EmblXML or null if exceptions were raised
   */
  public static EmblFile getEmblFile(File file)
  {
    if (file == null)
    {
      return null;
    }
    try
    {
      return EmblFile.getEmblFile(new FileReader(file));
    } catch (Exception e)
    {
      System.err.println("Exception whilst reading EMBLfile from " + file);
      e.printStackTrace(System.err);
    }
    return null;
  }

  public static EmblFile getEmblFile(Reader file)
  {
    EmblFile record = new EmblFile();
    try
    {
      // 1. Load the mapping information from the file
      Mapping map = new Mapping(record.getClass().getClassLoader());

      java.net.URL url = record.getClass().getResource("/embl_mapping.xml");
      map.loadMapping(url);

      // 2. Unmarshal the data
      Unmarshaller unmar = new Unmarshaller(record);
      try
      {
        // uncomment to DEBUG EMBLFile reading
        if (jalview.bin.Cache.getDefault(jalview.bin.Cache.CASTORLOGLEVEL,
                "debug").equalsIgnoreCase("DEBUG"))
        {
          unmar.setDebug(jalview.bin.Cache.log.isDebugEnabled());
        }
      } catch (Exception e)
      {
      }
      unmar.setIgnoreExtraElements(true);
      unmar.setIgnoreExtraAttributes(true);
      unmar.setMapping(map);
      unmar.setLogWriter(new PrintWriter(System.out));
      record = (EmblFile) unmar.unmarshal(file);

      canonicaliseDbRefs(record);
    } catch (Exception e)
    {
      e.printStackTrace(System.err);
      record = null;
    }

    return record;
  }

  /**
   * Change blank version to "0" in any DBRefEntry, to ensure consistent
   * comparison with other DBRefEntry in Jalview
   * 
   * @param record
   * @see Uniprot#getDbVersion
   */
  static void canonicaliseDbRefs(EmblFile record)
  {
    if (record.getEntries() == null)
    {
      return;
    }
    for (EmblEntry entry : record.getEntries())
    {
      if (entry.getDbRefs() != null)
      {
        for (DBRefEntry dbref : entry.getDbRefs())
        {
          if ("".equals(dbref.getVersion()))
          {
            dbref.setVersion("0");
          }
        }
      }

      if (entry.getFeatures() != null)
      {
        for (EmblFeature feature : entry.getFeatures())
        {
          if (feature.getDbRefs() != null)
          {
            for (DBRefEntry dbref : feature.getDbRefs())
            {
              if ("".equals(dbref.getVersion()))
              {
                dbref.setVersion("0");
              }
            }
          }
        }
      }
    }
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }
}
