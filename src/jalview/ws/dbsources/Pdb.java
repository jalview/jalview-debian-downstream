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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.FormatAdapter;
import jalview.util.MessageManager;
import jalview.ws.ebi.EBIFetchClient;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.stevesoft.pat.Regex;

/**
 * @author JimP
 * 
 */
public class Pdb extends EbiFileRetrievedProxy implements DbSourceProxy
{
  public Pdb()
  {
    super();
    addDbSourceProperty(DBRefSource.PROTSEQDB);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator()
   */
  public String getAccessionSeparator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator()
   */
  public Regex getAccessionValidator()
  {
    return new Regex("([1-9][0-9A-Za-z]{3}):?([ _A-Za-z0-9]?)");
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  public String getDbSource()
  {
    return DBRefSource.PDB;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  public String getDbVersion()
  {
    return "0";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getSequenceRecords(java.lang.String[])
   */
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    AlignmentI pdbfile = null;
    Vector result = new Vector();
    String chain = null;
    String id = null;
    if (queries.indexOf(":") > -1)
    {
      chain = queries.substring(queries.indexOf(":") + 1);
      id = queries.substring(0, queries.indexOf(":"));
    }
    else
    {
      id = queries;
    }
    if (queries.length() > 4 && chain == null)
    {
      chain = queries.substring(4, 5);
      id = queries.substring(0, 4);
    }
    if (!isValidReference(id))
    {
      System.err.println("Ignoring invalid pdb query: '" + id + "'");
      stopQuery();
      return null;
    }
    EBIFetchClient ebi = new EBIFetchClient();
    file = ebi.fetchDataAsFile("pdb:" + id, "pdb", "raw").getAbsolutePath();
    stopQuery();
    if (file == null)
    {
      return null;
    }
    try
    {

      pdbfile = new FormatAdapter().readFile(file,
              jalview.io.AppletFormatAdapter.FILE, "PDB");
      if (pdbfile != null)
      {
        List<SequenceI> toremove = new ArrayList<SequenceI>();
        for (SequenceI pdbcs : pdbfile.getSequences())
        {
          String chid = null;
          // Mapping map=null;
          for (PDBEntry pid : pdbcs.getAllPDBEntries())
          {
            if (pid.getFile() == file)
            {
              chid = pid.getChainCode();

            }
            ;

          }
          if (chain == null
                  || (chid != null && (chid.equals(chain)
                          || chid.trim().equals(chain.trim()) || (chain
                          .trim().length() == 0 && chid.equals("_")))))
          {
            pdbcs.setName(jalview.datamodel.DBRefSource.PDB + "|" + id
                    + "|" + pdbcs.getName());
            // Might need to add more metadata to the PDBEntry object
            // like below
            /*
             * PDBEntry entry = new PDBEntry(); // Construct the PDBEntry
             * entry.setId(id); if (entry.getProperty() == null)
             * entry.setProperty(new Hashtable());
             * entry.getProperty().put("chains", pdbchain.id + "=" +
             * sq.getStart() + "-" + sq.getEnd());
             * sq.getDatasetSequence().addPDBId(entry);
             */
            // Add PDB DB Refs
            // We make a DBRefEtntry because we have obtained the PDB file from
            // a
            // verifiable source
            // JBPNote - PDB DBRefEntry should also carry the chain and mapping
            // information
            DBRefEntry dbentry = new DBRefEntry(getDbSource(),
                    getDbVersion(), (chid == null ? id : id + chid));
            // dbentry.setMap()
            pdbcs.addDBRef(dbentry);
          }
          else
          {
            // mark this sequence to be removed from the alignment
            // - since it's not from the right chain
            toremove.add(pdbcs);
          }
        }
        // now remove marked sequences
        for (SequenceI pdbcs : toremove)
        {
          pdbfile.deleteSequence(pdbcs);
          if (pdbcs.getAnnotation() != null)
          {
            for (AlignmentAnnotation aa : pdbcs.getAnnotation())
            {
              pdbfile.deleteAnnotation(aa);
            }
          }
        }
      }

      if (pdbfile == null || pdbfile.getHeight() < 1)
      {
        throw new Exception(MessageManager.formatMessage(
                "exception.no_pdb_records_for_chain", new String[] { id,
                    ((chain == null) ? "' '" : chain) }));
      }

    } catch (Exception ex) // Problem parsing PDB file
    {
      stopQuery();
      throw (ex);
    }
    return pdbfile;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  public boolean isValidReference(String accession)
  {
    Regex r = getAccessionValidator();
    return r.search(accession.trim());
  }

  /**
   * obtain human glyoxalase chain A sequence
   */
  public String getTestQuery()
  {
    return "1QIPA";
  }

  public String getDbName()
  {
    return "PDB"; // getDbSource();
  }

  @Override
  public int getTier()
  {
    return 0;
  }
}
