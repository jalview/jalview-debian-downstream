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
package jalview.util;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.stevesoft.pat.Regex;

public class DBRefUtils
{
  private static Map<String, String> canonicalSourceNameLookup = new HashMap<String, String>();

  private static Map<String, String> dasCoordinateSystemsLookup = new HashMap<String, String>();

  static
  {
    // TODO load these from a resource file?
    canonicalSourceNameLookup.put("uniprotkb/swiss-prot",
            DBRefSource.UNIPROT);
    canonicalSourceNameLookup.put("uniprotkb/trembl", DBRefSource.UNIPROT);
    canonicalSourceNameLookup.put("pdb", DBRefSource.PDB);

    dasCoordinateSystemsLookup.put("pdbresnum", DBRefSource.PDB);
    dasCoordinateSystemsLookup.put("uniprot", DBRefSource.UNIPROT);
    dasCoordinateSystemsLookup.put("embl", DBRefSource.EMBL);
    // dasCoordinateSystemsLookup.put("embl", DBRefSource.EMBLCDS);
  }

  /**
   * Utilities for handling DBRef objects and their collections.
   */
  /**
   * 
   * @param dbrefs
   *          Vector of DBRef objects to search
   * @param sources
   *          String[] array of source DBRef IDs to retrieve
   * @return Vector
   */
  public static DBRefEntry[] selectRefs(DBRefEntry[] dbrefs,
          String[] sources)
  {
    if (dbrefs == null)
    {
      return null;
    }
    if (sources == null)
    {
      return dbrefs;
    }
    Map<String, Integer> srcs = new HashMap<String, Integer>();
    ArrayList<DBRefEntry> res = new ArrayList<DBRefEntry>();

    for (int i = 0; i < sources.length; i++)
    {
      srcs.put(new String(sources[i]), new Integer(i));
    }
    for (int i = 0, j = dbrefs.length; i < j; i++)
    {
      if (srcs.containsKey(dbrefs[i].getSource()))
      {
        res.add(dbrefs[i]);
      }
    }

    if (res.size() > 0)
    {
      DBRefEntry[] reply = new DBRefEntry[res.size()];
      return res.toArray(reply);
    }
    res = null;
    // there are probable memory leaks in the hashtable!
    return null;
  }

  /**
   * isDasCoordinateSystem
   * 
   * @param string
   *          String
   * @param dBRefEntry
   *          DBRefEntry
   * @return boolean true if Source DBRefEntry is compatible with DAS
   *         CoordinateSystem name
   */

  public static boolean isDasCoordinateSystem(String string,
          DBRefEntry dBRefEntry)
  {
    if (string == null || dBRefEntry == null)
    {
      return false;
    }
    String coordsys = dasCoordinateSystemsLookup.get(string.toLowerCase());
    return coordsys == null ? false : coordsys.equals(dBRefEntry
            .getSource());
  }

  /**
   * look up source in an internal list of database reference sources and return
   * the canonical jalview name for the source, or the original string if it has
   * no canonical form.
   * 
   * @param source
   * @return canonical jalview source (one of jalview.datamodel.DBRefSource.*)
   *         or original source
   */
  public static String getCanonicalName(String source)
  {
    if (source == null)
    {
      return null;
    }
    String canonical = canonicalSourceNameLookup.get(source.toLowerCase());
    return canonical == null ? source : canonical;
  }

  /**
   * Returns an array of those references that match the given entry, or null if
   * no matches. Currently uses a comparator which matches if
   * <ul>
   * <li>database sources are the same</li>
   * <li>accession ids are the same</li>
   * <li>both have no mapping, or the mappings are the same</li>
   * </ul>
   * 
   * @param ref
   *          Set of references to search
   * @param entry
   *          pattern to match
   * @return
   */
  public static DBRefEntry[] searchRefs(DBRefEntry[] ref, DBRefEntry entry)
  {
    return searchRefs(ref, entry,
            matchDbAndIdAndEitherMapOrEquivalentMapList);
  }

  /**
   * Returns an array of those references that match the given entry, according
   * to the given comparator. Returns null if no matches.
   * 
   * @param refs
   *          an array of database references to search
   * @param entry
   *          an entry to compare against
   * @param comparator
   * @return
   */
  static DBRefEntry[] searchRefs(DBRefEntry[] refs, DBRefEntry entry,
          DbRefComp comparator)
  {
    if (refs == null || entry == null)
    {
      return null;
    }
    List<DBRefEntry> rfs = new ArrayList<DBRefEntry>();
    for (int i = 0; i < refs.length; i++)
    {
      if (comparator.matches(entry, refs[i]))
      {
        rfs.add(refs[i]);
      }
    }
    return rfs.size() == 0 ? null : rfs.toArray(new DBRefEntry[rfs.size()]);
  }

  interface DbRefComp
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb);
  }

  /**
   * match on all non-null fields in refa
   */
  public static DbRefComp matchNonNullonA = new DbRefComp()
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() == null
              || refb.getSource().equals(refa.getSource()))
      {
        if (refa.getVersion() == null
                || refb.getVersion().equals(refa.getVersion()))
        {
          if (refa.getAccessionId() == null
                  || refb.getAccessionId().equals(refa.getAccessionId()))
          {
            if (refa.getMap() == null
                    || (refb.getMap() != null && refb.getMap().equals(
                            refa.getMap())))
            {
              return true;
            }
          }
        }
      }
      return false;
    }
  };

  /**
   * either field is null or field matches for all of source, version, accession
   * id and map.
   */
  public static DbRefComp matchEitherNonNull = new DbRefComp()
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if ((refa.getSource() == null || refb.getSource() == null)
              || refb.getSource().equals(refa.getSource()))
      {
        if ((refa.getVersion() == null || refb.getVersion() == null)
                || refb.getVersion().equals(refa.getVersion()))
        {
          if ((refa.getAccessionId() == null || refb.getAccessionId() == null)
                  || refb.getAccessionId().equals(refa.getAccessionId()))
          {
            if ((refa.getMap() == null || refb.getMap() == null)
                    || (refb.getMap() != null && refb.getMap().equals(
                            refa.getMap())))
            {
              return true;
            }
          }
        }
      }
      return false;
    }
  };

  /**
   * accession ID and DB must be identical. Version is ignored. Map is either
   * not defined or is a match (or is compatible?)
   */
  public static DbRefComp matchDbAndIdAndEitherMap = new DbRefComp()
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && refb.getSource().equals(refa.getSource()))
      {
        // We dont care about version
        // if ((refa.getVersion()==null || refb.getVersion()==null)
        // || refb.getVersion().equals(refa.getVersion()))
        // {
        if (refa.getAccessionId() != null && refb.getAccessionId() != null
                || refb.getAccessionId().equals(refa.getAccessionId()))
        {
          if ((refa.getMap() == null || refb.getMap() == null)
                  || (refa.getMap() != null && refb.getMap() != null && refb
                          .getMap().equals(refa.getMap())))
          {
            return true;
          }
        }
      }
      return false;
    }
  };

  /**
   * accession ID and DB must be identical. Version is ignored. No map on either
   * or map but no maplist on either or maplist of map on a is the complement of
   * maplist of map on b.
   */
  public static DbRefComp matchDbAndIdAndComplementaryMapList = new DbRefComp()
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && refb.getSource().equals(refa.getSource()))
      {
        // We dont care about version
        // if ((refa.getVersion()==null || refb.getVersion()==null)
        // || refb.getVersion().equals(refa.getVersion()))
        // {
        if (refa.getAccessionId() != null && refb.getAccessionId() != null
                || refb.getAccessionId().equals(refa.getAccessionId()))
        {
          if ((refa.getMap() == null && refb.getMap() == null)
                  || (refa.getMap() != null && refb.getMap() != null))
          {
            if ((refb.getMap().getMap() == null && refa.getMap().getMap() == null)
                    || (refb.getMap().getMap() != null
                            && refa.getMap().getMap() != null && refb
                            .getMap().getMap().getInverse()
                            .equals(refa.getMap().getMap())))
            {
              return true;
            }
          }
        }
      }
      return false;
    }
  };

  /**
   * accession ID and DB must be identical. Version is ignored. No map on both
   * or or map but no maplist on either or maplist of map on a is equivalent to
   * the maplist of map on b.
   */
  public static DbRefComp matchDbAndIdAndEquivalentMapList = new DbRefComp()
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && refb.getSource().equals(refa.getSource()))
      {
        // We dont care about version
        // if ((refa.getVersion()==null || refb.getVersion()==null)
        // || refb.getVersion().equals(refa.getVersion()))
        // {
        if (refa.getAccessionId() != null && refb.getAccessionId() != null
                || refb.getAccessionId().equals(refa.getAccessionId()))
        {
          if (refa.getMap() == null && refb.getMap() == null)
          {
            return true;
          }
          if (refa.getMap() != null
                  && refb.getMap() != null
                  && ((refb.getMap().getMap() == null && refa.getMap()
                          .getMap() == null) || (refb.getMap().getMap() != null
                          && refa.getMap().getMap() != null && refb
                          .getMap().getMap().equals(refa.getMap().getMap()))))
          {
            return true;
          }
        }
      }
      return false;
    }
  };

  /**
   * accession ID and DB must be identical. Version is ignored. No map on either
   * or map but no maplist on either or maplist of map on a is equivalent to the
   * maplist of map on b.
   */
  public static DbRefComp matchDbAndIdAndEitherMapOrEquivalentMapList = new DbRefComp()
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      // System.err.println("Comparing A: "+refa.getSrcAccString()+(refa.hasMap()?" has map.":"."));
      // System.err.println("Comparing B: "+refb.getSrcAccString()+(refb.hasMap()?" has map.":"."));
      if (refa.getSource() != null && refb.getSource() != null
              && refb.getSource().equals(refa.getSource()))
      {
        // We dont care about version
        // if ((refa.getVersion()==null || refb.getVersion()==null)
        // || refb.getVersion().equals(refa.getVersion()))
        // {
        if (refa.getAccessionId() != null && refb.getAccessionId() != null
                && refb.getAccessionId().equals(refa.getAccessionId()))
        {
          if (refa.getMap() == null || refb.getMap() == null)
          {
            return true;
          }
          if ((refa.getMap() != null && refb.getMap() != null)
                  && (refb.getMap().getMap() == null && refa.getMap()
                          .getMap() == null)
                  || (refb.getMap().getMap() != null
                          && refa.getMap().getMap() != null && (refb
                          .getMap().getMap().equals(refa.getMap().getMap()))))
          { // getMap().getMap().containsEither(false,refa.getMap().getMap())
            return true;
          }
        }
      }
      return false;
    }
  };

  /**
   * Parses a DBRefEntry and adds it to the sequence, also a PDBEntry if the
   * database is PDB.
   * <p>
   * Used by file parsers to generate DBRefs from annotation within file (eg
   * Stockholm)
   * 
   * @param dbname
   * @param version
   * @param acn
   * @param seq
   *          where to annotate with reference
   * @return parsed version of entry that was added to seq (if any)
   */
  public static DBRefEntry parseToDbRef(SequenceI seq, String dbname,
          String version, String acn)
  {
    DBRefEntry ref = null;
    if (dbname != null)
    {
      String locsrc = DBRefUtils.getCanonicalName(dbname);
      if (locsrc.equals(DBRefSource.PDB))
      {
        /*
         * Check for PFAM style stockhom PDB accession id citation e.g.
         * "1WRI A; 7-80;"
         */
        Regex r = new com.stevesoft.pat.Regex(
                "([0-9][0-9A-Za-z]{3})\\s*(.?)\\s*;\\s*([0-9]+)-([0-9]+)");
        if (r.search(acn.trim()))
        {
          String pdbid = r.stringMatched(1);
          String chaincode = r.stringMatched(2);
          if (chaincode == null)
          {
            chaincode = " ";
          }
          // String mapstart = r.stringMatched(3);
          // String mapend = r.stringMatched(4);
          if (chaincode.equals(" "))
          {
            chaincode = "_";
          }
          // construct pdb ref.
          ref = new DBRefEntry(locsrc, version, pdbid + chaincode);
          PDBEntry pdbr = new PDBEntry();
          pdbr.setId(pdbid);
          pdbr.setType(PDBEntry.Type.PDB);
          pdbr.setProperty(new Hashtable());
          pdbr.setChainCode(chaincode);
          // pdbr.getProperty().put("CHAIN", chaincode);
          seq.addPDBId(pdbr);
        }
        else
        {
          System.err.println("Malformed PDB DR line:" + acn);
        }
      }
      else
      {
        // default:
        ref = new DBRefEntry(locsrc, version, acn);
      }
    }
    if (ref != null)
    {
      seq.addDBRef(ref);
    }
    return ref;
  }

}
