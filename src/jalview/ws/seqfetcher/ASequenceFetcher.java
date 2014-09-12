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
package jalview.ws.seqfetcher;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ASequenceFetcher
{

  /**
   * set of databases we can retrieve entries from
   */
  protected Hashtable FETCHABLEDBS;

  public ASequenceFetcher()
  {
    super();
  }

  /**
   * get list of supported Databases
   * 
   * @return database source string for each database - only the latest version
   *         of a source db is bound to each source.
   */
  public String[] getSupportedDb()
  {
    if (FETCHABLEDBS == null)
      return null;
    String[] sf = new String[FETCHABLEDBS.size()];
    Enumeration e = FETCHABLEDBS.keys();
    int i = 0;
    while (e.hasMoreElements())
    {
      sf[i++] = (String) e.nextElement();
    }
    ;
    return sf;
  }

  public boolean isFetchable(String source)
  {
    Enumeration e = FETCHABLEDBS.keys();
    while (e.hasMoreElements())
    {
      String db = (String) e.nextElement();
      if (source.compareToIgnoreCase(db) == 0)
        return true;
    }
    jalview.bin.Cache.log.warn("isFetchable doesn't know about '" + source
            + "'");
    return false;
  }

  public SequenceI[] getSequences(jalview.datamodel.DBRefEntry[] refs)
  {
    SequenceI[] ret = null;
    Vector rseqs = new Vector();
    Hashtable queries = new Hashtable();
    for (int r = 0; r < refs.length; r++)
    {
      if (!queries.containsKey(refs[r].getSource()))
      {
        queries.put(refs[r].getSource(), new Vector());
      }
      Vector qset = (Vector) queries.get(refs[r].getSource());
      if (!qset.contains(refs[r].getAccessionId()))
      {
        qset.addElement(refs[r].getAccessionId());
      }
    }
    Enumeration e = queries.keys();
    while (e.hasMoreElements())
    {
      Vector query = null;
      String db = null;
      try
      {
        db = (String) e.nextElement();
        query = (Vector) queries.get(db);
        if (!isFetchable(db))
          throw new Exception(
                  "Don't know how to fetch from this database :" + db);
        DbSourceProxy fetcher = getSourceProxy(db);
        boolean doMultiple = fetcher.getAccessionSeparator() != null; // No
        // separator
        // - no
        // Multiple
        // Queries
        Enumeration qs = query.elements();
        while (qs.hasMoreElements())
        {
          StringBuffer qsb = new StringBuffer();
          do
          {
            qsb.append((String) qs.nextElement());
            if (qs.hasMoreElements() && doMultiple) // and not reached limit for
            // multiple queries at one
            // time for this source
            {
              qsb.append(fetcher.getAccessionSeparator());
            }
          } while (doMultiple && qs.hasMoreElements());

          AlignmentI seqset = null;
          try
          {
            // create a fetcher and go to it
            seqset = fetcher.getSequenceRecords(qsb.toString());
          } catch (Exception ex)
          {
            System.err.println("Failed to retrieve the following from "
                    + db);
            System.err.println(qsb);
            ex.printStackTrace(System.err);
          }
          // TODO: Merge alignment together - perhaps
          if (seqset != null)
          {
            SequenceI seqs[] = seqset.getSequencesArray();
            if (seqs != null)
            {
              for (int is = 0; is < seqs.length; is++)
              {
                rseqs.addElement(seqs[is]);
                seqs[is] = null;
              }
            }
            else
            {
              if (fetcher.getRawRecords() != null)
              {
                System.out.println("# Retrieved from " + db + ":"
                        + qs.toString());
                StringBuffer rrb = fetcher.getRawRecords();
                /*
                 * for (int rr = 0; rr<rrb.length; rr++) {
                 */
                String hdr;
                // if (rr<qs.length)
                // {
                hdr = "# " + db + ":" + qsb.toString();
                /*
                 * } else { hdr = "# part "+rr; }
                 */
                System.out.println(hdr);
                if (rrb != null)
                  System.out.println(rrb);
                System.out.println("# end of " + hdr);
              }
            }
          }
        }
      } catch (Exception ex)
      {
        System.err
                .println("Failed to retrieve the following references from "
                        + db);
        Enumeration qv = query.elements();
        int n = 0;
        while (qv.hasMoreElements())
        {
          System.err.print(" " + qv.nextElement() + ";");
          if (n++ > 10)
          {
            System.err.println();
            n = 0;
          }
        }
        System.err.println();
        ex.printStackTrace();
      }
    }
    if (rseqs.size() > 0)
    {
      ret = new SequenceI[rseqs.size()];
      Enumeration sqs = rseqs.elements();
      int si = 0;
      while (sqs.hasMoreElements())
      {
        SequenceI s = (SequenceI) sqs.nextElement();
        ret[si++] = s;
        s.updatePDBIds();
      }
    }
    return ret;
  }

  /**
   * Retrieve an instance of the proxy for the given source
   * 
   * @param db
   *          database source string TODO: add version string/wildcard for
   *          retrieval of specific DB source/version combinations.
   * @return an instance of DbSourceProxy for that db.
   */
  public DbSourceProxy getSourceProxy(String db)
  {
    DbSourceProxy dbs = (DbSourceProxy) FETCHABLEDBS.get(db);
    return dbs;
  }

  /**
   * constructs and instance of the proxy and registers it as a valid
   * dbrefsource
   * 
   * @param dbSourceProxy
   *          reference for class implementing
   *          jalview.ws.seqfetcher.DbSourceProxy
   * @throws java.lang.IllegalArgumentException
   *           if class does not implement jalview.ws.seqfetcher.DbSourceProxy
   */
  protected void addDBRefSourceImpl(Class dbSourceProxy)
          throws java.lang.IllegalArgumentException
  {
    DbSourceProxy proxy = null;
    try
    {
      Object proxyObj = dbSourceProxy.getConstructor(null)
              .newInstance(null);
      if (!DbSourceProxy.class.isInstance(proxyObj))
      {
        throw new IllegalArgumentException(
                dbSourceProxy.toString()
                        + " does not implement the jalview.ws.seqfetcher.DbSourceProxy");
      }
      proxy = (DbSourceProxy) proxyObj;
    } catch (IllegalArgumentException e)
    {
      throw e;
    } catch (Exception e)
    {
      // Serious problems if this happens.
      throw new Error("DBRefSource Implementation Exception", e);
    }
    addDbRefSourceImpl(proxy);
  }

  /**
   * add the properly initialised DbSourceProxy object 'proxy' to the list of
   * sequence fetchers
   * 
   * @param proxy
   */
  protected void addDbRefSourceImpl(DbSourceProxy proxy)
  {
    if (proxy != null)
    {
      if (FETCHABLEDBS == null)
      {
        FETCHABLEDBS = new Hashtable();
      }
      FETCHABLEDBS.put(proxy.getDbSource(), proxy);
    }
  }

  /**
   * test if the database handler for dbName contains the given dbProperty
   * 
   * @param dbName
   * @param dbProperty
   * @return true if proxy has the given property
   */
  public boolean hasDbSourceProperty(String dbName, String dbProperty)
  {
    // TODO: decide if invalidDbName exception is thrown here.
    DbSourceProxy proxy = getSourceProxy(dbName);
    if (proxy != null)
    {
      if (proxy.getDbSourceProperties() != null)
      {
        return proxy.getDbSourceProperties().containsKey(dbProperty);
      }
    }
    return false;
  }

  /**
   * select sources which are implemented by instances of the given class
   * 
   * @param class that implements DbSourceProxy
   * @return null or vector of source names for fetchers
   */
  public String[] getDbInstances(Class class1)
  {
    if (!jalview.ws.seqfetcher.DbSourceProxy.class.isAssignableFrom(class1))
    {
      throw new Error(
              "Implmentation Error - getDbInstances must be given a class that implements jalview.ws.seqfetcher.DbSourceProxy (was given '"
                      + class1 + "')");
    }
    if (FETCHABLEDBS == null)
    {
      return null;
    }
    String[] sources = null;
    Vector src = new Vector();
    Enumeration dbs = FETCHABLEDBS.keys();
    while (dbs.hasMoreElements())
    {
      String dbn = (String) dbs.nextElement();
      DbSourceProxy dbp = (DbSourceProxy) FETCHABLEDBS.get(dbn);
      if (class1.isAssignableFrom(dbp.getClass()))
      {
        src.addElement(dbn);
      }
    }
    if (src.size() > 0)
    {
      src.copyInto(sources = new String[src.size()]);
    }
    return sources;
  }
}
