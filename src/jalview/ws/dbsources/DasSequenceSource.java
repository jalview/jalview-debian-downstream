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
package jalview.ws.dbsources;

import java.util.Hashtable;

import org.biojava.dasobert.das.SequenceThread;
import org.biojava.dasobert.das2.Das2Source;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.dasregistry.DasSource;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;

import com.stevesoft.pat.Regex;

import jalview.ws.dbsources.das.DasSequenceSourceListener;
import jalview.ws.seqfetcher.*;
import jalview.datamodel.AlignmentI;

/**
 * an instance of this class is created for each unique DAS Sequence source (ie
 * one capable of handling the 'sequence' for a particular MapMaster)
 * 
 * @author JimP
 * 
 */
public class DasSequenceSource extends DbSourceProxyImpl implements
        DbSourceProxy
{
  protected Das1Source source = null;

  protected String dbname = "DASCS";

  protected String dbrefname = "das:source";

  protected DasCoordinateSystem coordsys = null;

  /**
   * create a new DbSource proxy for a DAS 1 source
   * 
   * @param dbnbame
   *          Human Readable Name to use when fetching from this source
   * @param dbrefname
   *          DbRefName for DbRefs attached to sequences retrieved from this
   *          source
   * @param source
   *          Das1Source
   * @param coordsys
   *          specific coordinate system to use for this source
   * @throws Exception
   *           if source is not capable of the 'sequence' command
   */
  public DasSequenceSource(String dbname, String dbrefname,
          Das1Source source, DasCoordinateSystem coordsys) throws Exception
  {
    if (!source.hasCapability("sequence"))
    {
      throw new Exception("Source " + source.getNickname()
              + " does not support the sequence command.");
    }
    this.source = source;
    this.dbname = dbname;
    this.dbrefname = dbrefname;
    this.coordsys = coordsys;
  }

  public String getAccessionSeparator()
  {
    // cope with single sequences only
    return null;
  }

  public Regex getAccessionValidator()
  {
    /** ? * */
    return Regex.perlCode("\\S+");
  }

  public String getDbName()
  {
    // TODO: map to
    return dbname + " (DAS)";
  }

  public String getDbSource()
  {
    return dbrefname;
  }

  public String getDbVersion()
  {
    return coordsys.getVersion();
  }

  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    SequenceThread seqfetcher = new org.biojava.dasobert.das.SequenceThread(
            queries, source);
    DasSequenceSourceListener ourlistener = new DasSequenceSourceListener(
            this, queries);
    seqfetcher.addSequenceListener(ourlistener);
    seqfetcher.start();
    try
    {
      Thread.sleep(5);
    } catch (Exception e)
    {
    }
    ;
    while (ourlistener.isNotCalled() && seqfetcher.isAlive())
    {
      try
      {
        Thread.sleep(5);
      } catch (Exception e)
      {
      }
      ;
    }
    if (ourlistener.isNotCalled() || ourlistener.hasNoSequences())
    {
      System.err.println("Sequence Query to " + source.getNickname()
              + " with '" + queries + "' returned no sequences.");
      return null;
    }
    else
    {
      return ourlistener.getSequences();
    }
  }

  public String getTestQuery()
  {
    return coordsys.getTestCode();
  }

  public boolean isValidReference(String accession)
  {
    // TODO try to validate an accession against source
    // We don't really know how to do this without querying source

    return true;
  }

  /**
   * @return the source
   */
  public Das1Source getSource()
  {
    return source;
  }

  /**
   * @return the coordsys
   */
  public DasCoordinateSystem getCoordsys()
  {
    return coordsys;
  }
}
