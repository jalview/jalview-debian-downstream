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
package jalview.datamodel;

import java.util.Vector;

public class Provenance
{
  Vector entries = new Vector();

  public Provenance()
  {

  }

  public ProvenanceEntry[] getEntries()
  {
    ProvenanceEntry[] ret = new ProvenanceEntry[entries.size()];
    for (int i = 0; i < entries.size(); i++)
    {
      ret[i] = (ProvenanceEntry) entries.elementAt(i);
    }
    return ret;
  }

  public void addEntry(String user, String action, java.util.Date date,
          String id)
  {
    entries.addElement(new ProvenanceEntry(user, action, date, id));
  }

}
