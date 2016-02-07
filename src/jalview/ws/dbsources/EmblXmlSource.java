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

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.datamodel.xdb.embl.EmblEntry;
import jalview.datamodel.xdb.embl.EmblFile;
import jalview.util.MessageManager;
import jalview.ws.ebi.EBIFetchClient;

import java.io.File;

public abstract class EmblXmlSource extends EbiFileRetrievedProxy
{

  /**
   * Last properly parsed embl file.
   */
  public EmblFile efile = null;

  public EmblXmlSource()
  {
    super();
  }

  /**
   * retrieve and parse an emblxml file
   * 
   * @param emprefx
   *          either EMBL or EMBLCDS strings are allowed - anything else will
   *          not retrieve emblxml
   * @param query
   * @return
   * @throws Exception
   */
  public AlignmentI getEmblSequenceRecords(String emprefx, String query)
          throws Exception
  {
    startQuery();
    EBIFetchClient dbFetch = new EBIFetchClient();
    File reply;
    try
    {
      reply = dbFetch.fetchDataAsFile(
              emprefx.toLowerCase() + ":" + query.trim(), "emblxml", null);
    } catch (Exception e)
    {
      stopQuery();
      throw new Exception(MessageManager.formatMessage(
              "exception.ebiembl_retrieval_failed_on", new String[] {
                  emprefx.toLowerCase(), query.trim() }), e);
    }
    return getEmblSequenceRecords(emprefx, query, reply);
  }

  /**
   * parse an emblxml file stored locally
   * 
   * @param emprefx
   *          either EMBL or EMBLCDS strings are allowed - anything else will
   *          not retrieve emblxml
   * @param query
   * @param file
   *          the EMBL XML file containing the results of a query
   * @return
   * @throws Exception
   */
  public AlignmentI getEmblSequenceRecords(String emprefx, String query,
          File reply) throws Exception
  {
    SequenceI seqs[] = null;
    StringBuffer result = new StringBuffer();
    if (reply != null && reply.exists())
    {
      efile = null;
      file = reply.getAbsolutePath();
      if (reply.length() > 25)
      {
        efile = EmblFile.getEmblFile(reply);
      }
      else
      {
        result.append(MessageManager.formatMessage(
                "label.no_embl_record_found",
                new String[] { emprefx.toLowerCase(), query.trim() }));
      }
    }
    if (efile != null)
    {
      for (EmblEntry entry : efile.getEntries())
      {
        SequenceI[] seqparts = entry.getSequences(false, true, emprefx);
        // TODO: use !fetchNa,!fetchPeptide here instead - see todo in EmblEntry
        if (seqparts != null)
        {
          SequenceI[] newseqs = null;
          int si = 0;
          if (seqs == null)
          {
            newseqs = new SequenceI[seqparts.length];
          }
          else
          {
            newseqs = new SequenceI[seqs.length + seqparts.length];

            for (; si < seqs.length; si++)
            {
              newseqs[si] = seqs[si];
              seqs[si] = null;
            }
          }
          for (int j = 0; j < seqparts.length; si++, j++)
          {
            newseqs[si] = seqparts[j].deriveSequence();
            // place DBReferences on dataset and refer
          }
          seqs = newseqs;

        }
      }
    }
    else
    {
      result = null;
    }
    AlignmentI al = null;
    if (seqs != null && seqs.length > 0)
    {
      al = new Alignment(seqs);
      result.append(MessageManager.formatMessage(
              "label.embl_successfully_parsed", new String[] { emprefx }));
      results = result;
    }
    stopQuery();
    return al;
  }

}
