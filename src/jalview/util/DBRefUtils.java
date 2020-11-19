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
package jalview.util;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.stevesoft.pat.Regex;

/**
 * Utilities for handling DBRef objects and their collections.
 */
public class DBRefUtils
{
  /*
   * lookup from lower-case form of a name to its canonical (standardised) form
   */
  private static Map<String, String> canonicalSourceNameLookup = new HashMap<>();


  static
  {
    // TODO load these from a resource file?
    canonicalSourceNameLookup.put("uniprotkb/swiss-prot",
            DBRefSource.UNIPROT);
    canonicalSourceNameLookup.put("uniprotkb/trembl", DBRefSource.UNIPROT);

    // Ensembl values for dbname in xref REST service:
    canonicalSourceNameLookup.put("uniprot/sptrembl", DBRefSource.UNIPROT);
    canonicalSourceNameLookup.put("uniprot/swissprot", DBRefSource.UNIPROT);

    canonicalSourceNameLookup.put("pdb", DBRefSource.PDB);
    canonicalSourceNameLookup.put("ensembl", DBRefSource.ENSEMBL);
    // Ensembl Gn and Tr are for Ensembl genomic and transcript IDs as served
    // from ENA.
    canonicalSourceNameLookup.put("ensembl-tr", DBRefSource.ENSEMBL);
    canonicalSourceNameLookup.put("ensembl-gn", DBRefSource.ENSEMBL);

    // Make sure we have lowercase entries for all canonical string lookups
    Set<String> keys = canonicalSourceNameLookup.keySet();
    for (String k : keys)
    {
      canonicalSourceNameLookup.put(k.toLowerCase(),
              canonicalSourceNameLookup.get(k));
    }

  }

  /**
   * Returns those DBRefEntry objects whose source identifier (once converted to
   * Jalview's canonical form) is in the list of sources to search for. Returns
   * null if no matches found.
   * 
   * @param dbrefs
   *          DBRefEntry objects to search
   * @param sources
   *          array of sources to select
   * @return
   */
  public static DBRefEntry[] selectRefs(DBRefEntry[] dbrefs,
          String[] sources)
  {
    if (dbrefs == null || sources == null)
    {
      return dbrefs;
    }
    HashSet<String> srcs = new HashSet<>();
    for (String src : sources)
    {
      srcs.add(src.toUpperCase());
    }

    List<DBRefEntry> res = new ArrayList<>();
    for (DBRefEntry dbr : dbrefs)
    {
      String source = getCanonicalName(dbr.getSource());
      if (srcs.contains(source.toUpperCase()))
      {
        res.add(dbr);
      }
    }

    if (res.size() > 0)
    {
      DBRefEntry[] reply = new DBRefEntry[res.size()];
      return res.toArray(reply);
    }
    return null;
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
   * Returns a (possibly empty) list of those references that match the given
   * entry. Currently uses a comparator which matches if
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
  public static List<DBRefEntry> searchRefs(DBRefEntry[] ref,
          DBRefEntry entry)
  {
    return searchRefs(ref, entry,
            matchDbAndIdAndEitherMapOrEquivalentMapList);
  }

  /**
   * Returns a list of those references that match the given accession id
   * <ul>
   * <li>database sources are the same</li>
   * <li>accession ids are the same</li>
   * <li>both have no mapping, or the mappings are the same</li>
   * </ul>
   * 
   * @param refs
   *          Set of references to search
   * @param accId
   *          accession id to match
   * @return
   */
  public static List<DBRefEntry> searchRefs(DBRefEntry[] refs, String accId)
  {
    return searchRefs(refs, new DBRefEntry("", "", accId), matchId);
  }

  /**
   * Returns a (possibly empty) list of those references that match the given
   * entry, according to the given comparator.
   * 
   * @param refs
   *          an array of database references to search
   * @param entry
   *          an entry to compare against
   * @param comparator
   * @return
   */
  static List<DBRefEntry> searchRefs(DBRefEntry[] refs, DBRefEntry entry,
          DbRefComp comparator)
  {
    List<DBRefEntry> rfs = new ArrayList<>();
    if (refs == null || entry == null)
    {
      return rfs;
    }
    for (int i = 0; i < refs.length; i++)
    {
      if (comparator.matches(entry, refs[i]))
      {
        rfs.add(refs[i]);
      }
    }
    return rfs;
  }

  interface DbRefComp
  {
    public boolean matches(DBRefEntry refa, DBRefEntry refb);
  }

  /**
   * match on all non-null fields in refa
   */
  // TODO unused - remove?
  public static DbRefComp matchNonNullonA = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() == null
              || DBRefUtils.getCanonicalName(refb.getSource()).equals(
                      DBRefUtils.getCanonicalName(refa.getSource())))
      {
        if (refa.getVersion() == null
                || refb.getVersion().equals(refa.getVersion()))
        {
          if (refa.getAccessionId() == null
                  || refb.getAccessionId().equals(refa.getAccessionId()))
          {
            if (refa.getMap() == null || (refb.getMap() != null
                    && refb.getMap().equals(refa.getMap())))
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
  // TODO unused - remove?
  public static DbRefComp matchEitherNonNull = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (nullOrEqualSource(refa.getSource(), refb.getSource())
              && nullOrEqual(refa.getVersion(), refb.getVersion())
              && nullOrEqual(refa.getAccessionId(), refb.getAccessionId())
              && nullOrEqual(refa.getMap(), refb.getMap()))
      {
        return true;
      }
      return false;
    }
  };

  /**
   * accession ID and DB must be identical. Version is ignored. Map is either
   * not defined or is a match (or is compatible?)
   */
  // TODO unused - remove?
  public static DbRefComp matchDbAndIdAndEitherMap = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && DBRefUtils.getCanonicalName(refb.getSource()).equals(
                      DBRefUtils.getCanonicalName(refa.getSource())))
      {
        // We dont care about version
        if (refa.getAccessionId() != null && refb.getAccessionId() != null
                // FIXME should be && not || here?
                || refb.getAccessionId().equals(refa.getAccessionId()))
        {
          if ((refa.getMap() == null || refb.getMap() == null)
                  || (refa.getMap() != null && refb.getMap() != null
                          && refb.getMap().equals(refa.getMap())))
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
  // TODO unused - remove?
  public static DbRefComp matchDbAndIdAndComplementaryMapList = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && DBRefUtils.getCanonicalName(refb.getSource()).equals(
                      DBRefUtils.getCanonicalName(refa.getSource())))
      {
        // We dont care about version
        if (refa.getAccessionId() != null && refb.getAccessionId() != null
                || refb.getAccessionId().equals(refa.getAccessionId()))
        {
          if ((refa.getMap() == null && refb.getMap() == null)
                  || (refa.getMap() != null && refb.getMap() != null))
          {
            if ((refb.getMap().getMap() == null
                    && refa.getMap().getMap() == null)
                    || (refb.getMap().getMap() != null
                            && refa.getMap().getMap() != null
                            && refb.getMap().getMap().getInverse()
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
  // TODO unused - remove?
  public static DbRefComp matchDbAndIdAndEquivalentMapList = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && DBRefUtils.getCanonicalName(refb.getSource()).equals(
                      DBRefUtils.getCanonicalName(refa.getSource())))
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
          if (refa.getMap() != null && refb.getMap() != null
                  && ((refb.getMap().getMap() == null
                          && refa.getMap().getMap() == null)
                          || (refb.getMap().getMap() != null
                                  && refa.getMap().getMap() != null
                                  && refb.getMap().getMap()
                                          .equals(refa.getMap().getMap()))))
          {
            return true;
          }
        }
      }
      return false;
    }
  };

  /**
   * accession ID and DB must be identical, or null on a. Version is ignored. No
   * map on either or map but no maplist on either or maplist of map on a is
   * equivalent to the maplist of map on b.
   */
  public static DbRefComp matchDbAndIdAndEitherMapOrEquivalentMapList = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getSource() != null && refb.getSource() != null
              && DBRefUtils.getCanonicalName(refb.getSource()).equals(
                      DBRefUtils.getCanonicalName(refa.getSource())))
      {
        // We dont care about version

        if (refa.getAccessionId() == null
                || refa.getAccessionId().equals(refb.getAccessionId()))
        {
          if (refa.getMap() == null || refb.getMap() == null)
          {
            return true;
          }
          if ((refa.getMap() != null && refb.getMap() != null)
                  && (refb.getMap().getMap() == null
                          && refa.getMap().getMap() == null)
                  || (refb.getMap().getMap() != null
                          && refa.getMap().getMap() != null
                          && (refb.getMap().getMap()
                                  .equals(refa.getMap().getMap()))))
          {
            return true;
          }
        }
      }
      return false;
    }
  };

  /**
   * accession ID only must be identical.
   */
  public static DbRefComp matchId = new DbRefComp()
  {
    @Override
    public boolean matches(DBRefEntry refa, DBRefEntry refb)
    {
      if (refa.getAccessionId() != null && refb.getAccessionId() != null
              && refb.getAccessionId().equals(refa.getAccessionId()))
      {
        return true;
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
          pdbr.setChainCode(chaincode);
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
        ref = new DBRefEntry(locsrc, version, acn.trim());
      }
    }
    if (ref != null)
    {
      seq.addDBRef(ref);
    }
    return ref;
  }

  /**
   * Returns true if either object is null, or they are equal
   * 
   * @param o1
   * @param o2
   * @return
   */
  public static boolean nullOrEqual(Object o1, Object o2)
  {
    if (o1 == null || o2 == null)
    {
      return true;
    }
    return o1.equals(o2);
  }

  /**
   * canonicalise source string before comparing. null is always wildcard
   * 
   * @param o1
   *          - null or source string to compare
   * @param o2
   *          - null or source string to compare
   * @return true if either o1 or o2 are null, or o1 equals o2 under
   *         DBRefUtils.getCanonicalName
   *         (o1).equals(DBRefUtils.getCanonicalName(o2))
   */
  public static boolean nullOrEqualSource(String o1, String o2)
  {
    if (o1 == null || o2 == null)
    {
      return true;
    }
    return DBRefUtils.getCanonicalName(o1)
            .equals(DBRefUtils.getCanonicalName(o2));
  }

  /**
   * Selects just the DNA or protein references from a set of references
   * 
   * @param selectDna
   *          if true, select references to 'standard' DNA databases, else to
   *          'standard' peptide databases
   * @param refs
   *          a set of references to select from
   * @return
   */
  public static DBRefEntry[] selectDbRefs(boolean selectDna,
          DBRefEntry[] refs)
  {
    return selectRefs(refs,
            selectDna ? DBRefSource.DNACODINGDBS : DBRefSource.PROTEINDBS);
    // could attempt to find other cross
    // refs here - ie PDB xrefs
    // (not dna, not protein seq)
  }

  /**
   * Returns the (possibly empty) list of those supplied dbrefs which have the
   * specified source database, with a case-insensitive match of source name
   * 
   * @param dbRefs
   * @param source
   * @return
   */
  public static List<DBRefEntry> searchRefsForSource(DBRefEntry[] dbRefs,
          String source)
  {
    List<DBRefEntry> matches = new ArrayList<>();
    if (dbRefs != null && source != null)
    {
      for (DBRefEntry dbref : dbRefs)
      {
        if (source.equalsIgnoreCase(dbref.getSource()))
        {
          matches.add(dbref);
        }
      }
    }
    return matches;
  }

  /**
   * promote direct database references to primary for nucleotide or protein
   * sequences if they have an appropriate primary ref
   * <table>
   * <tr>
   * <th>Seq Type</th>
   * <th>Primary DB</th>
   * <th>Direct which will be promoted</th>
   * </tr>
   * <tr align=center>
   * <td>peptides</td>
   * <td>Ensembl</td>
   * <td>Uniprot</td>
   * </tr>
   * <tr align=center>
   * <td>peptides</td>
   * <td>Ensembl</td>
   * <td>Uniprot</td>
   * </tr>
   * <tr align=center>
   * <td>dna</td>
   * <td>Ensembl</td>
   * <td>ENA</td>
   * </tr>
   * </table>
   * 
   * @param sequence
   */
  public static void ensurePrimaries(SequenceI sequence)
  {
    List<DBRefEntry> pr = sequence.getPrimaryDBRefs();
    if (pr.size() == 0)
    {
      // nothing to do
      return;
    }
    List<DBRefEntry> selfs = new ArrayList<>();
    {
      DBRefEntry[] selfArray = selectDbRefs(!sequence.isProtein(),
              sequence.getDBRefs());
      if (selfArray == null || selfArray.length == 0)
      {
        // nothing to do
        return;
      }
      selfs.addAll(Arrays.asList(selfArray));
    }

    // filter non-primary refs
    for (DBRefEntry p : pr)
    {
      while (selfs.contains(p))
      {
        selfs.remove(p);
      }
    }
    List<DBRefEntry> toPromote = new ArrayList<>();

    for (DBRefEntry p : pr)
    {
      List<String> promType = new ArrayList<>();
      if (sequence.isProtein())
      {
        switch (getCanonicalName(p.getSource()))
        {
        case DBRefSource.UNIPROT:
          // case DBRefSource.UNIPROTKB:
          // case DBRefSource.UP_NAME:
          // search for and promote ensembl
          promType.add(DBRefSource.ENSEMBL);
          break;
        case DBRefSource.ENSEMBL:
          // search for and promote Uniprot
          promType.add(DBRefSource.UNIPROT);
          break;
        }
      }
      else
      {
        // TODO: promote transcript refs
      }

      // collate candidates and promote them
      DBRefEntry[] candidates = selectRefs(selfs.toArray(new DBRefEntry[0]),
              promType.toArray(new String[0]));
      if (candidates != null)
      {
        for (DBRefEntry cand : candidates)
        {
          if (cand.hasMap())
          {
            if (cand.getMap().getTo() != null
                    && cand.getMap().getTo() != sequence)
            {
              // can't promote refs with mappings to other sequences
              continue;
            }
            if (cand.getMap().getMap().getFromLowest() != sequence
                    .getStart()
                    && cand.getMap().getMap().getFromHighest() != sequence
                            .getEnd())
            {
              // can't promote refs with mappings from a region of this sequence
              // - eg CDS
              continue;
            }
          }
          // and promote
          cand.setVersion(p.getVersion() + " (promoted)");
          selfs.remove(cand);
          toPromote.add(cand);
          if (!cand.isPrimaryCandidate())
          {
            System.out.println(
                    "Warning: Couldn't promote dbref " + cand.toString()
                            + " for sequence " + sequence.toString());
          }
        }
      }
    }
  }

}
