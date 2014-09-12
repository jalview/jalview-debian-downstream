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
package jalview.ws;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.biojava.dasobert.das2.Das2Source;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.dasregistry.DasSource;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.ws.dbsources.DasSequenceSource;
import jalview.ws.seqfetcher.ASequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

/**
 * This is the the concrete implementation of the sequence retrieval interface
 * and abstract class in jalview.ws.seqfetcher. This implements the run-time
 * discovery of sequence database clients, and provides a hardwired main for
 * testing all registered handlers.
 * 
 */
public class SequenceFetcher extends ASequenceFetcher
{
  /**
   * Thread safe construction of database proxies TODO: extend to a configurable
   * database plugin mechanism where classes are instantiated by reflection and
   * queried for their DbRefSource and version association.
   * 
   */
  public SequenceFetcher()
  {
    addDBRefSourceImpl(jalview.ws.dbsources.EmblSource.class);
    addDBRefSourceImpl(jalview.ws.dbsources.EmblCdsSouce.class);
    addDBRefSourceImpl(jalview.ws.dbsources.Uniprot.class);
    addDBRefSourceImpl(jalview.ws.dbsources.UnprotName.class);
    addDBRefSourceImpl(jalview.ws.dbsources.Pdb.class);
    addDBRefSourceImpl(jalview.ws.dbsources.PfamFull.class);
    addDBRefSourceImpl(jalview.ws.dbsources.PfamSeed.class); // ensures Seed
    // alignment is
    // 'default' for
    // PFAM
    registerDasSequenceSources();
  }

  /**
   * return an ordered list of database sources suitable for using in a GUI
   * element
   */
  public String[] getOrderedSupportedSources()
  {
    String[] srcs = this.getSupportedDb();
    ArrayList dassrc = new ArrayList(), nondas = new ArrayList();
    for (int i = 0; i < srcs.length; i++)
    {
      String nm = getSourceProxy(srcs[i]).getDbName();
      if (getSourceProxy(srcs[i]) instanceof jalview.ws.dbsources.DasSequenceSource)
      {
        if (nm.startsWith("das:"))
        {
          nm = nm.substring(4);
        }
        dassrc.add(new String[]
        { srcs[i], nm.toUpperCase() });
      }
      else
      {
        nondas.add(new String[]
        { srcs[i], nm.toUpperCase() });
      }
    }
    Object[] sorted = nondas.toArray();
    String[] tosort = new String[sorted.length];
    nondas.clear();
    for (int j = 0; j < sorted.length; j++)
    {
      tosort[j] = ((String[]) sorted[j])[1];
    }
    jalview.util.QuickSort.sort(tosort, sorted);
    int i = 0;
    for (int j = sorted.length - 1; j >= 0; j--, i++)
    {
      srcs[i] = ((String[]) sorted[j])[0];
      sorted[j] = null;
    }

    sorted = dassrc.toArray();
    tosort = new String[sorted.length];
    dassrc.clear();
    for (int j = 0; j < sorted.length; j++)
    {
      tosort[j] = ((String[]) sorted[j])[1];
    }
    jalview.util.QuickSort.sort(tosort, sorted);
    for (int j = sorted.length - 1; j >= 0; j--, i++)
    {
      srcs[i] = ((String[]) sorted[j])[0];
      sorted[j] = null;
    }
    return srcs;
  }

  /**
   * simple run method to test dbsources.
   * 
   * @param argv
   */
  public static void main(String[] argv)
  {
    AlignmentI ds = null;
    Vector noProds = new Vector();
    String usage = "SequenceFetcher.main [<DBNAME> <ACCNO>]\n"
            + "With no arguments, all DbSources will be queried with their test Accession number.\n"
            + "If given two arguments, SequenceFetcher will try to find the DbFetcher corresponding to <DBNAME> and retrieve <ACCNO> from it.";
    if (argv != null && argv.length > 0)
    {
      DbSourceProxy sp = new SequenceFetcher().getSourceProxy(argv[0]);
      if (sp != null)
      {
        AlignmentI al = null;
        try
        {
          al = sp.getSequenceRecords(argv[1]);
        } catch (Exception e)
        {
          e.printStackTrace();
          System.err.println("Error when retrieving " + argv[1] + " from "
                  + argv[0] + "\nUsage: " + usage);
        }
        SequenceI[] prod = al.getSequencesArray();
        if (al != null)
        {
          for (int p = 0; p < prod.length; p++)
          {
            System.out.println("Prod " + p + ": "
                    + prod[p].getDisplayId(true) + " : "
                    + prod[p].getDescription());
          }
        }
        return;
      }
      else
      {
        System.err.println("Can't resolve " + argv[0]
                + " as a database name. Allowed values are :\n"
                + new SequenceFetcher().getSupportedDb());
      }
      System.out.println(usage);
    }
    ASequenceFetcher sfetcher = new SequenceFetcher();
    String[] dbSources = sfetcher.getSupportedDb();
    for (int dbsource = 0; dbsource < dbSources.length; dbsource++)
    {
      String db = dbSources[dbsource];
      // skip me
      if (db.equals(DBRefSource.PDB))
        continue;
      DbSourceProxy sp = sfetcher.getSourceProxy(db);
      System.out.println("Source: " + sp.getDbName() + " (" + db
              + "): retrieving test:" + sp.getTestQuery());
      AlignmentI al = null;
      try
      {
        al = sp.getSequenceRecords(sp.getTestQuery());
        if (al != null && al.getHeight() > 0
                && sp.getDbSourceProperties() != null)
        {
          boolean dna = sp.getDbSourceProperties().containsKey(
                  DBRefSource.DNACODINGSEQDB)
                  || sp.getDbSourceProperties().containsKey(
                          DBRefSource.DNASEQDB)
                  || sp.getDbSourceProperties().containsKey(
                          DBRefSource.CODINGSEQDB);
          // try and find products
          String types[] = jalview.analysis.CrossRef.findSequenceXrefTypes(
                  dna, al.getSequencesArray());
          if (types != null)
          {
            System.out.println("Xref Types for: " + (dna ? "dna" : "prot"));
            for (int t = 0; t < types.length; t++)
            {
              System.out.println("Type: " + types[t]);
              SequenceI[] prod = jalview.analysis.CrossRef
                      .findXrefSequences(al.getSequencesArray(), dna,
                              types[t]).getSequencesArray();
              System.out.println("Found "
                      + ((prod == null) ? "no" : "" + prod.length)
                      + " products");
              if (prod != null)
              {
                for (int p = 0; p < prod.length; p++)
                {
                  System.out.println("Prod " + p + ": "
                          + prod[p].getDisplayId(true));
                }
              }
            }
          }
          else
          {
            noProds.addElement((dna ? new Object[]
            { al, al } : new Object[]
            { al }));
          }

        }
      } catch (Exception ex)
      {
        System.out.println("ERROR:Failed to retrieve test query.");
        ex.printStackTrace(System.out);
      }
      if (al == null)
      {
        System.out.println("ERROR:No alignment retrieved.");
        StringBuffer raw = sp.getRawRecords();
        if (raw != null)
          System.out.println(raw.toString());
        else
          System.out.println("ERROR:No Raw results.");
      }
      else
      {
        System.out.println("Retrieved " + al.getHeight() + " sequences.");
        for (int s = 0; s < al.getHeight(); s++)
        {
          SequenceI sq = al.getSequenceAt(s);
          while (sq.getDatasetSequence() != null)
          {
            sq = sq.getDatasetSequence();

          }
          if (ds == null)
          {
            ds = new Alignment(new SequenceI[]
            { sq });

          }
          else
          {
            ds.addSequence(sq);
          }
        }
      }
      System.out.flush();
      System.err.flush();

    }
    if (noProds.size() > 0)
    {
      Enumeration ts = noProds.elements();
      while (ts.hasMoreElements())

      {
        Object[] typeSq = (Object[]) ts.nextElement();
        boolean dna = (typeSq.length > 1);
        AlignmentI al = (AlignmentI) typeSq[0];
        System.out.println("Trying getProducts for "
                + al.getSequenceAt(0).getDisplayId(true));
        System.out.println("Search DS Xref for: " + (dna ? "dna" : "prot"));
        // have a bash at finding the products amongst all the retrieved
        // sequences.
        SequenceI[] seqs = al.getSequencesArray();
        Alignment prodal = jalview.analysis.CrossRef.findXrefSequences(
                seqs, dna, null, ds);
        System.out.println("Found "
                + ((prodal == null) ? "no" : "" + prodal.getHeight())
                + " products");
        if (prodal != null)
        {
          SequenceI[] prod = prodal.getSequencesArray(); // note
          // should
          // test
          // rather
          // than
          // throw
          // away
          // codon
          // mapping
          // (if
          // present)
          for (int p = 0; p < prod.length; p++)
          {
            System.out.println("Prod " + p + ": "
                    + prod[p].getDisplayId(true));
          }
        }
      }

    }
  }

  /**
   * query the currently defined DAS source registry for sequence sources and
   * add a DasSequenceSource instance for each source to the SequenceFetcher
   * source list.
   */
  public void registerDasSequenceSources()
  {
    DasSource[] sources = jalview.ws.DasSequenceFeatureFetcher
            .getDASSources();
    if (sources != null)
    {
      for (int s = 0; sources != null && s < sources.length; s++)
      {
        addDasSequenceSource(sources[s]);
      }
    }

    Vector localsources = jalview.bin.Cache.getLocalDasSources();
    if (localsources != null)
    {
      for (Enumeration ls = localsources.elements(); ls.hasMoreElements();)
      {
        addDasSequenceSource((DasSource) ls.nextElement());
      }
    }
  }

  /**
   * Try to create and add a DasSequenceSource to the list of sources.
   * 
   * @param source
   * @return null if no source was added, or the new DasSequenceSource created
   */
  DasSequenceSource addDasSequenceSource(DasSource source)
  {
    DasSequenceSource ds = null;
    Das1Source d1s = null;
    if (source.hasCapability("sequence"))
    {
      if (source instanceof Das2Source)
      {
        if (((Das2Source) source).hasDas1Capabilities())
        {
          try
          {
            d1s = org.biojava.dasobert.das2.DasSourceConverter
                    .toDas1Source((Das2Source) source);
          } catch (Exception e)
          {
            System.err.println("Ignoring DAS2 sequence source "
                    + source.getNickname()
                    + " - couldn't map to Das1Source.\n");
            e.printStackTrace();
          }
        }
      }
      else
      {
        if (source instanceof Das1Source)
        {
          d1s = (Das1Source) source;
        }
      }
    }
    if (d1s != null)
    {
      DasCoordinateSystem[] css = d1s.getCoordinateSystem();
      if (css == null || css.length == 0)
      {
        // TODO: query das source directly to identify coordinate system... or
        // have to make up a coordinate system
        css = new DasCoordinateSystem[]
        { new DasCoordinateSystem() };
        css[0].setName(d1s.getNickname());
        css[0].setUniqueId(d1s.getNickname());
      }
      for (int c = 0; c < css.length; c++)
      {
        try
        {
          addDbRefSourceImpl(ds = new DasSequenceSource("das:"
                  + d1s.getNickname() + " (" + css[c].getName() + ")",
                  css[c].getName(), d1s, css[c]));
        } catch (Exception e)
        {
          System.err.println("Ignoring sequence coord system " + c + " ("
                  + css[c].getName() + ") for source " + d1s.getNickname()
                  + "- threw exception when constructing fetcher.\n");
          e.printStackTrace();
        }
      }
    }
    return ds;
  }
}
