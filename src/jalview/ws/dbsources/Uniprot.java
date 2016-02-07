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
package jalview.ws.dbsources;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.UniprotEntry;
import jalview.datamodel.UniprotFile;
import jalview.ws.ebi.EBIFetchClient;
import jalview.ws.seqfetcher.DbSourceProxy;
import jalview.ws.seqfetcher.DbSourceProxyImpl;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Vector;

import org.exolab.castor.xml.Unmarshaller;

import com.stevesoft.pat.Regex;

/**
 * @author JimP
 * 
 */
public class Uniprot extends DbSourceProxyImpl implements DbSourceProxy
{

  private static final String BAR_DELIMITER = "|";

  private static final String NEWLINE = "\n";

  private static org.exolab.castor.mapping.Mapping map;

  /**
   * Constructor
   */
  public Uniprot()
  {
    super();
    addDbSourceProperty(DBRefSource.SEQDB, DBRefSource.SEQDB);
    addDbSourceProperty(DBRefSource.PROTSEQDB);
    // addDbSourceProperty(DBRefSource.MULTIACC, new Integer(50));
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator()
   */
  public String getAccessionSeparator()
  {
    return null; // ";";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator()
   */
  public Regex getAccessionValidator()
  {
    return new Regex("([A-Z]+[0-9]+[A-Z0-9]+|[A-Z0-9]+_[A-Z0-9]+)");
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  public String getDbSource()
  {
    return DBRefSource.UNIPROT;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
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
        map = new org.exolab.castor.mapping.Mapping(uni.getClass()
                .getClassLoader());
        java.net.URL url = getClass().getResource("/uniprot_mapping.xml");
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
              null);
      Vector<UniprotEntry> entries = getUniprotEntries(new FileReader(file));

      if (entries != null)
      {
        /*
         * If Castor binding included sequence@length, we could guesstimate the
         * size of buffer to hold the alignment
         */
        StringBuffer result = new StringBuffer(128);
        // First, make the new sequences
        for (UniprotEntry entry : entries)
        {
          StringBuilder name = constructSequenceFastaHeader(entry);

          result.append(name).append(NEWLINE)
                  .append(entry.getUniprotSequence().getContent())
                  .append(NEWLINE);
        }

        // Then read in the features and apply them to the dataset
        al = parseResult(result.toString());
        if (al != null)
        {
          // Decorate the alignment with database entries.
          addUniprotXrefs(al, entries);
        }
        else
        {
          results = result;
        }
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
   * Construct a Fasta-format sequence header by concatenating the source,
   * accession id(s) and name(s), delimited by '|', plus any protein names, now
   * with space rather than bar delimiter
   * 
   * @param entry
   * @return
   */
  public static StringBuilder constructSequenceFastaHeader(
          UniprotEntry entry)
  {
    StringBuilder name = new StringBuilder(32);
    name.append(">UniProt/Swiss-Prot");
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

    if (entry.getProtein() != null && entry.getProtein().getName() != null)
    {
      for (String nm : entry.getProtein().getName())
      {
        name.append(" ").append(nm);
      }
    }
    return name;
  }

  /**
   * add an ordered set of UniprotEntry objects to an ordered set of seuqences.
   * 
   * @param al
   *          - a sequence of n sequences
   * @param entries
   *          a list of n uniprot entries to be analysed.
   */
  public void addUniprotXrefs(AlignmentI al, Vector<UniprotEntry> entries)
  {
    final String dbVersion = getDbVersion();

    for (int i = 0; i < entries.size(); i++)
    {
      UniprotEntry entry = entries.elementAt(i);
      Vector<PDBEntry> onlyPdbEntries = new Vector<PDBEntry>();
      Vector<DBRefEntry> dbxrefs = new Vector<DBRefEntry>();

      for (PDBEntry pdb : entry.getDbReference())
      {
        DBRefEntry dbr = new DBRefEntry();
        dbr.setSource(pdb.getType());
        dbr.setAccessionId(pdb.getId());
        dbr.setVersion(DBRefSource.UNIPROT + ":" + dbVersion);
        dbxrefs.addElement(dbr);
        if ("PDB".equals(pdb.getType()))
        {
          onlyPdbEntries.addElement(pdb);
        }
      }

      SequenceI sq = al.getSequenceAt(i);
      while (sq.getDatasetSequence() != null)
      {
        sq = sq.getDatasetSequence();
      }

      for (String accessionId : entry.getAccession())
      {
        /*
         * add as uniprot whether retrieved from uniprot or uniprot_name
         */
        sq.addDBRef(new DBRefEntry(DBRefSource.UNIPROT, dbVersion,
                accessionId));
      }

      for (DBRefEntry dbRef : dbxrefs)
      {
        sq.addDBRef(dbRef);
      }
      sq.setPDBId(onlyPdbEntries);
      if (entry.getFeature() != null)
      {
        for (SequenceFeature sf : entry.getFeature())
        {
          sf.setFeatureGroup("Uniprot");
          sq.addSequenceFeature(sf);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  public boolean isValidReference(String accession)
  {
    // TODO: make the following a standard validator
    return (accession == null || accession.length() < 2) ? false
            : getAccessionValidator().search(accession);
  }

  /**
   * return LDHA_CHICK uniprot entry
   */
  public String getTestQuery()
  {
    return "P00340";
  }

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
