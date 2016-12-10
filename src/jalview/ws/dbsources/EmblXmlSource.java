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
import jalview.datamodel.SequenceI;
import jalview.datamodel.xdb.embl.EmblEntry;
import jalview.datamodel.xdb.embl.EmblFile;
import jalview.util.MessageManager;
import jalview.ws.ebi.EBIFetchClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class EmblXmlSource extends EbiFileRetrievedProxy
{
  /*
   * JAL-1856 Embl returns this text for query not found
   */
  private static final String EMBL_NOT_FOUND_REPLY = "ERROR 12 No entries found.";

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
              emprefx.toLowerCase() + ":" + query.trim(), "display=xml",
              ".xml");
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
    EmblFile efile = null;
    List<SequenceI> seqs = new ArrayList<SequenceI>();

    if (reply != null && reply.exists())
    {
      file = reply.getAbsolutePath();
      if (reply.length() > EMBL_NOT_FOUND_REPLY.length())
      {
        efile = EmblFile.getEmblFile(reply);
      }
    }

    /*
     * invalid accession gets a reply with no <entry> elements, text content of
     * EmbFile reads something like (e.g.) this ungrammatical phrase
     * Entry: <acc> display type is either not supported or entry is not found.
     */
    List<SequenceI> peptides = new ArrayList<SequenceI>();
    if (efile != null && efile.getEntries() != null)
    {
      for (EmblEntry entry : efile.getEntries())
      {
        SequenceI seq = entry.getSequence(emprefx, peptides);
        if (seq != null)
        {
          seqs.add(seq.deriveSequence());
          // place DBReferences on dataset and refer
        }
      }
    }

    AlignmentI al = null;
    if (!seqs.isEmpty())
    {
      al = new Alignment(seqs.toArray(new SequenceI[seqs.size()]));
    }
    stopQuery();
    return al;
  }

  @Override
  public boolean isDnaCoding()
  {
    return true;
  }

}
