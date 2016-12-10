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
package jalview.datamodel;

import jalview.api.DBRefEntryI;

import java.util.Arrays;
import java.util.List;

public class DBRefEntry implements DBRefEntryI
{
  String source = "", version = "", accessionId = "";

  /**
   * maps from associated sequence to the database sequence's coordinate system
   */
  Mapping map = null;

  public DBRefEntry()
  {

  }

  public DBRefEntry(String source, String version, String accessionId)
  {
    this(source, version, accessionId, null);
  }

  /**
   * 
   * @param source
   *          canonical source (uppercase only)
   * @param version
   *          (source dependent version string)
   * @param accessionId
   *          (source dependent accession number string)
   * @param map
   *          (mapping from local sequence numbering to source accession
   *          numbering)
   */
  public DBRefEntry(String source, String version, String accessionId,
          Mapping map)
  {
    this.source = source.toUpperCase();
    this.version = version;
    this.accessionId = accessionId;
    this.map = map;
  }

  public DBRefEntry(DBRefEntryI entry)
  {
    this((entry.getSource() == null ? "" : new String(entry.getSource())),
            (entry.getVersion() == null ? "" : new String(
                    entry.getVersion())),
            (entry.getAccessionId() == null ? "" : new String(
                    entry.getAccessionId())),
            (entry.getMap() == null ? null : new Mapping(entry.getMap())));
  }

  @Override
  public boolean equals(Object o)
  {
    // TODO should also override hashCode to ensure equal objects have equal
    // hashcodes
    if (o == null || !(o instanceof DBRefEntry))
    {
      return false;
    }
    DBRefEntry entry = (DBRefEntry) o;
    if (entry == this)
    {
      return true;
    }
    if (equalRef(entry)
            && ((map == null && entry.map == null) || (map != null
                    && entry.map != null && map.equals(entry.map))))
    {
      return true;
    }
    return false;
  }

  /**
   * Answers true if this object is either equivalent to, or can be 'improved'
   * by, the given entry. Specifically, answers true if
   * <ul>
   * <li>source and accession are identical (ignoring case)</li>
   * <li>version is identical (ignoring case), or this version is of the format
   * "someSource:0", in which case the version for the other entry replaces it</li>
   * <li>mappings are not compared but if this entry has no mapping, replace
   * with that for the other entry</li>
   * </ul>
   * 
   * @param other
   * @return
   */
  @Override
  public boolean updateFrom(DBRefEntryI other)
  {
    if (other == null)
    {
      return false;
    }
    if (other == this)
    {
      return true;
    }

    /*
     * source must either match or be both null
     */
    String otherSource = other.getSource();
    if ((source == null && otherSource != null)
            || (source != null && otherSource == null)
            || (source != null && !source.equalsIgnoreCase(otherSource)))
    {
      return false;
    }

    /*
     * accession id must either match or be both null
     */
    String otherAccession = other.getAccessionId();
    if ((accessionId == null && otherAccession != null)
            || (accessionId != null && otherAccession == null)
            || (accessionId != null && !accessionId
                    .equalsIgnoreCase(otherAccession)))
    {
      return false;
    }

    /*
     * if my version is null, "0" or "source:0" then replace with other version,
     * otherwise the versions have to match
     */
    String otherVersion = other.getVersion();

    if ((version == null || version.equals("0") || version.endsWith(":0"))
            && otherVersion != null)
    {
      setVersion(otherVersion);
    }
    else
    {
      if (version != null
              && (otherVersion == null || !version
                      .equalsIgnoreCase(otherVersion)))
      {
        return false;
      }
    }

    /*
     * if I have no mapping, take that of the other dbref
     */
    if (map == null)
    {
      setMap(other.getMap());
    }
    return true;
  }

  /**
   * test for similar DBRef attributes, except for the map object.
   * 
   * @param entry
   * @return true if source, accession and version are equal with those of entry
   */
  @Override
  public boolean equalRef(DBRefEntryI entry)
  {
    // TODO is this method and equals() not needed?
    if (entry == null)
    {
      return false;
    }
    if (entry == this)
    {
      return true;
    }
    if (entry != null
            && (source != null && entry.getSource() != null && source
                    .equalsIgnoreCase(entry.getSource()))
            && (accessionId != null && entry.getAccessionId() != null && accessionId
                    .equalsIgnoreCase(entry.getAccessionId()))
            && (version != null && entry.getVersion() != null && version
                    .equalsIgnoreCase(entry.getVersion())))
    {
      return true;
    }
    return false;
  }

  @Override
  public String getSource()
  {
    return source;
  }

  @Override
  public String getVersion()
  {
    return version;
  }

  @Override
  public String getAccessionId()
  {
    return accessionId;
  }

  @Override
  public void setAccessionId(String accessionId)
  {
    this.accessionId = accessionId;
  }

  @Override
  public void setSource(String source)
  {
    this.source = source;
  }

  @Override
  public void setVersion(String version)
  {
    this.version = version;
  }

  @Override
  public Mapping getMap()
  {
    return map;
  }

  /**
   * @param map
   *          the map to set
   */
  public void setMap(Mapping map)
  {
    this.map = map;
  }

  public boolean hasMap()
  {
    return map != null;
  }

  /**
   * 
   * @return source+":"+accessionId
   */
  public String getSrcAccString()
  {
    return ((source != null) ? source : "") + ":"
            + ((accessionId != null) ? accessionId : "");
  }

  @Override
  public String toString()
  {
    return getSrcAccString();
  }

  @Override
  public boolean isPrimaryCandidate()
  {
    /*
     * if a map is present, unless it is 1:1 and has no SequenceI mate, it cannot be a primary reference.  
     */
    if (map != null)
    {
      if (map.getTo() != null)
      {
        return false;
      }
      if (map.getMap().getFromRatio() != map.getMap().getToRatio()
              || map.getMap().getFromRatio() != 1)
      {
        return false;
      }
      // check map is between identical single contiguous ranges
      List<int[]> fromRanges = map.getMap().getFromRanges();
      List<int[]> toRanges = map.getMap().getToRanges();
      if (fromRanges.size() != 1 || toRanges.size() != 1)
      {
        return false;
      }
      if (fromRanges.get(0)[0] != toRanges.get(0)[0]
              || fromRanges.get(0)[1] != toRanges.get(0)[1])
      {
        return false;
      }
    }
    if (version == null)
    {
      // no version string implies the reference has not been verified at all.
      return false;
    }
    // tricky - this test really needs to search the sequence's set of dbrefs to
    // see if there is a primary reference that derived this reference.
    String ucv = version.toUpperCase();
    for (String primsrc : Arrays.asList(DBRefSource.allSources()))
    {
      if (ucv.startsWith(primsrc.toUpperCase()))
      {
        // by convention, many secondary references inherit the primary
        // reference's
        // source string as a prefix for any version information from the
        // secondary reference.
        return false;
      }
    }
    return true;
  }
}
