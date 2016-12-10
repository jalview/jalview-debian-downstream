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
package jalview.ws.dbsources;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.UniprotEntry;
import jalview.datamodel.UniprotFile;
import jalview.ws.ebi.EBIFetchClient;
import jalview.ws.seqfetcher.DbSourceProxyImpl;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

import com.stevesoft.pat.Regex;

/**
 * @author JimP
 * 
 */
public class Uniprot extends DbSourceProxyImpl
{
  private static final String BAR_DELIMITER = "|";

  /*
   * Castor mapping loaded from uniprot_mapping.xml
   */
  private static Mapping map;

  /**
   * Constructor
   */
  public Uniprot()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator()
   */
  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator()
   */
  @Override
  public Regex getAccessionValidator()
  {
    return new Regex("([A-Z]+[0-9]+[A-Z0-9]+|[A-Z0-9]+_[A-Z0-9]+)");
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  @Override
  public String getDbSource()
  {
    return DBRefSource.UNIPROT;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    return "0"; // we really don't know what version we're on.
  }

  /**
   * Reads a file containing the reply to the EBI Fetch Uniprot data query,
   * unmarshals it to a UniprotFile object, and returns the list of UniprotEntry
   * data models (mapped from &lt;entry&gt; elements)
   * 
   * @param fileReader
   * @return
   */
  public Vector<UniprotEntry> getUniprotEntries(Reader fileReader)
  {
    UniprotFile uni = new UniprotFile();
    try
    {
      if (map == null)
      {
        // 1. Load the mapping information from the file
        map = new Mapping(uni.getClass().getClassLoader());
        URL url = getClass().getResource("/uniprot_mapping.xml");
        map.loadMapping(url);
      }

      // 2. Unmarshal the data
      Unmarshaller unmar = new Unmarshaller(uni);
      unmar.setIgnoreExtraElements(true);
      unmar.setMapping(map);
      if (fileReader != null)
      {
        uni = (UniprotFile) unmar.unmarshal(fileReader);
      }
    } catch (Exception e)
    {
      System.out.println("Error getUniprotEntries() " + e);
    }

    return uni.getUniprotEntries();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getSequenceRecords(java.lang.String[])
   */
  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    startQuery();
    try
    {
      queries = queries.toUpperCase().replaceAll(
              "(UNIPROT\\|?|UNIPROT_|UNIREF\\d+_|UNIREF\\d+\\|?)", "");
      AlignmentI al = null;
      EBIFetchClient ebi = new EBIFetchClient();
      // uniprotxml parameter required since december 2007
      // uniprotkb dbname changed introduced december 2008
      File file = ebi.fetchDataAsFile("uniprotkb:" + queries, "uniprotxml",
              ".xml");
      Vector<UniprotEntry> entries = getUniprotEntries(new FileReader(file));

      if (entries != null)
      {
        ArrayList<SequenceI> seqs = new ArrayList<SequenceI>();
        for (UniprotEntry entry : entries)
        {
          seqs.add(uniprotEntryToSequenceI(entry));
        }
        al = new Alignment(seqs.toArray(new SequenceI[0]));

      }
      stopQuery();
      return al;
    } catch (Exception e)
    {
      stopQuery();
      throw (e);
    }
  }

  /**
   * 
   * @param entry
   *          UniprotEntry
   * @return SequenceI instance created from the UniprotEntry instance
   */
  public SequenceI uniprotEntryToSequenceI(UniprotEntry entry)
  {
    String id = getUniprotEntryId(entry);
    SequenceI sequence = new Sequence(id, entry.getUniprotSequence()
            .getContent());
    sequence.setDescription(getUniprotEntryDescription(entry));

    final String dbVersion = getDbVersion();
    ArrayList<DBRefEntry> dbRefs = new ArrayList<DBRefEntry>();
    for (String accessionId : entry.getAccession())
    {
      DBRefEntry dbRef = new DBRefEntry(DBRefSource.UNIPROT, dbVersion,
              accessionId);

      // mark dbRef as a primary reference for this sequence
      dbRefs.add(dbRef);
    }

    Vector<PDBEntry> onlyPdbEntries = new Vector<PDBEntry>();
    for (PDBEntry pdb : entry.getDbReference())
    {
      DBRefEntry dbr = new DBRefEntry();
      dbr.setSource(pdb.getType());
      dbr.setAccessionId(pdb.getId());
      dbr.setVersion(DBRefSource.UNIPROT + ":" + dbVersion);
      dbRefs.add(dbr);
      if ("PDB".equals(pdb.getType()))
      {
        onlyPdbEntries.addElement(pdb);
      }
      if ("EMBL".equals(pdb.getType()))
      {
        // look for a CDS reference and add it, too.
        String cdsId = (String) pdb.getProperty("protein sequence ID");
        if (cdsId != null && cdsId.trim().length() > 0)
        {
          // remove version
          String[] vrs = cdsId.split("\\.");
          dbr = new DBRefEntry(DBRefSource.EMBLCDS, vrs.length > 1 ? vrs[1]
                  : DBRefSource.UNIPROT + ":" + dbVersion, vrs[0]);
          dbRefs.add(dbr);
        }
      }
      if ("Ensembl".equals(pdb.getType()))
      {
        /*UniprotXML
         * <dbReference type="Ensembl" id="ENST00000321556">
        * <molecule id="Q9BXM7-1"/>
        * <property type="protein sequence ID" value="ENSP00000364204"/>
        * <property type="gene ID" value="ENSG00000158828"/>
        * </dbReference> 
         */
        String cdsId = (String) pdb.getProperty("protein sequence ID");
        if (cdsId != null && cdsId.trim().length() > 0)
        {
          dbr = new DBRefEntry(DBRefSource.ENSEMBL, DBRefSource.UNIPROT
                  + ":" + dbVersion, cdsId.trim());
          dbRefs.add(dbr);

        }
      }

    }

    sequence.setPDBId(onlyPdbEntries);
    if (entry.getFeature() != null)
    {
      for (SequenceFeature sf : entry.getFeature())
      {
        sf.setFeatureGroup("Uniprot");
        sequence.addSequenceFeature(sf);
      }
    }
    for (DBRefEntry dbr : dbRefs)
    {
      sequence.addDBRef(dbr);
    }
    return sequence;
  }

  /**
   * 
   * @param entry
   *          UniportEntry
   * @return protein name(s) delimited by a white space character
   */
  public static String getUniprotEntryDescription(UniprotEntry entry)
  {
    StringBuilder desc = new StringBuilder(32);
    if (entry.getProtein() != null && entry.getProtein().getName() != null)
    {
      boolean first = true;
      for (String nm : entry.getProtein().getName())
      {
        if (!first)
        {
          desc.append(" ");
        }
        first = false;
        desc.append(nm);
      }
    }
    return desc.toString();
  }

  /**
   *
   * @param entry
   *          UniportEntry
   * @return The accession id(s) and name(s) delimited by '|'.
   */
  public static String getUniprotEntryId(UniprotEntry entry)
  {
    StringBuilder name = new StringBuilder(32);
    // name.append("UniProt/Swiss-Prot");
    // use 'canonicalised' name for optimal id matching
    name.append(DBRefSource.UNIPROT);
    for (String accessionId : entry.getAccession())
    {
      name.append(BAR_DELIMITER);
      name.append(accessionId);
    }
    for (String n : entry.getName())
    {
      name.append(BAR_DELIMITER);
      name.append(n);
    }
    return name.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    // TODO: make the following a standard validator
    return (accession == null || accession.length() < 2) ? false
            : getAccessionValidator().search(accession);
  }

  /**
   * return LDHA_CHICK uniprot entry
   */
  @Override
  public String getTestQuery()
  {
    return "P00340";
  }

  @Override
  public String getDbName()
  {
    return "Uniprot"; // getDbSource();
  }

  @Override
  public int getTier()
  {
    return 0;
  }
}
