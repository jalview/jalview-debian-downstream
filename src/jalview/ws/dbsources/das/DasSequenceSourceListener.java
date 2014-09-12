/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
package jalview.ws.dbsources.das;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.Alignment;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.ws.dbsources.DasSequenceSource;

import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;

/**
 * Listen for sequence fetch events from a dasobert SequenceThread started with
 * a query string and collect sequences returned from the DAS sequence source.
 * 
 * @author JimP
 * 
 */
public class DasSequenceSourceListener implements SequenceListener
{

  String ourAccession = null;

  DasSequenceSource oursource = null;

  /**
   * 
   * @param source
   *          the DAS Sequence DbProxy object containing database details for
   *          this source
   * @param query
   *          the query string sent to the das source that we should be
   *          listening for.
   */
  public DasSequenceSourceListener(DasSequenceSource source, String query)
  {
    oursource = source;
    ourAccession = query;
  }

  public void clearSelection()
  {
    // TODO Auto-generated method stub

  }

  java.util.Vector seqs = null;

  public void newSequence(SequenceEvent e)
  {
    if (!e.getAccessionCode().equals(ourAccession))
    {
      System.err
              .println("Warning - received sequence event for strange accession code ("
                      + e.getAccessionCode()
                      + ") - we expected "
                      + ourAccession);

      return;
    }
    if (seqs == null)
    {
      if (e.getSequence().length() == 0)
      {
        System.err.println("Empty sequence returned for accession code ("
                + e.getAccessionCode() + ") from " + oursource.getDbName());
        called = true;
        noSequences = true;
        return;
      }
      seqs = new java.util.Vector();
    }
    Sequence sq = new Sequence(e.getAccessionCode(), e.getSequence());
    sq.addDBRef(new DBRefEntry(oursource.getDbSource(), oursource
            .getDbVersion() + ":" + e.getVersion(), e.getAccessionCode()));
    seqs.addElement(sq);
    called = true;
  }

  public void selectedSeqPosition(int position)
  {
    // TODO Auto-generated method stub

  }

  public void selectedSeqRange(int start, int end)
  {
    // TODO Auto-generated method stub

  }

  public void selectionLocked(boolean flag)
  {
    // TODO Auto-generated method stub

  }

  public void newObjectRequested(String accessionCode)
  {
    // TODO Auto-generated method stub

  }

  boolean noSequences = false;

  public void noObjectFound(String accessionCode)
  {
    if (accessionCode.equals(ourAccession))
    {
      noSequences = true;
      called = true;
    }
  }

  public boolean hasNoSequences()
  {
    return noSequences;
  }

  boolean called = false;

  public boolean isNotCalled()
  {
    return !called;
  }

  public AlignmentI getSequences()
  {
    if (noSequences || seqs != null && seqs.size() == 0)
      return null;
    SequenceI[] sqs = new SequenceI[seqs.size()];
    for (int i = 0, iSize = seqs.size(); i < iSize; i++)
    {
      sqs[i] = (SequenceI) seqs.elementAt(i);
    }
    Alignment al = new Alignment(sqs);
    if (oursource.getSource().hasCapability("features"))
    {
      java.util.Vector src = new java.util.Vector();
      src.addElement(oursource.getSource());
      new jalview.ws.DasSequenceFeatureFetcher(sqs, null, src, false, false);
    }

    return al;
  }

}
