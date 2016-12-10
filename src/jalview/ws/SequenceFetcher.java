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
package jalview.ws;

import jalview.ext.ensembl.EnsemblGene;
import jalview.ext.ensembl.EnsemblGenomes;
import jalview.ws.dbsources.EmblCdsSource;
import jalview.ws.dbsources.EmblSource;
import jalview.ws.dbsources.Pdb;
import jalview.ws.dbsources.PfamFull;
import jalview.ws.dbsources.PfamSeed;
import jalview.ws.dbsources.RfamSeed;
import jalview.ws.dbsources.Uniprot;
import jalview.ws.dbsources.das.api.jalviewSourceI;
import jalview.ws.seqfetcher.ASequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * This implements the run-time discovery of sequence database clients.
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
    this(true);
  }

  public SequenceFetcher(boolean addDas)
  {
    addDBRefSourceImpl(EnsemblGene.class);
    addDBRefSourceImpl(EnsemblGenomes.class);
    addDBRefSourceImpl(EmblSource.class);
    addDBRefSourceImpl(EmblCdsSource.class);
    addDBRefSourceImpl(Uniprot.class);
    addDBRefSourceImpl(Pdb.class);
    addDBRefSourceImpl(PfamFull.class);
    addDBRefSourceImpl(PfamSeed.class);
    addDBRefSourceImpl(RfamSeed.class);

    if (addDas)
    {
      registerDasSequenceSources();
    }
  }

  /**
   * return an ordered list of database sources where non-das database classes
   * appear before das database classes
   */
  public String[] getOrderedSupportedSources()
  {
    String[] srcs = this.getSupportedDb();
    ArrayList<String> dassrc = new ArrayList<String>(), nondas = new ArrayList<String>();
    for (int i = 0; i < srcs.length; i++)
    {
      boolean das = false, skip = false;
      String nm;
      for (DbSourceProxy dbs : getSourceProxy(srcs[i]))
      {
        // Skip the alignment databases for the moment - they're not useful for
        // verifying a single sequence against its reference source
        if (dbs.isAlignmentSource())
        {
          skip = true;
        }
        else
        {
          nm = dbs.getDbName();
          if (getSourceProxy(srcs[i]) instanceof jalview.ws.dbsources.das.datamodel.DasSequenceSource)
          {
            if (nm.startsWith("das:"))
            {
              nm = nm.substring(4);
              das = true;
            }
            break;
          }
        }
      }
      if (skip)
      {
        continue;
      }
      if (das)
      {
        dassrc.add(srcs[i]);
      }
      else
      {
        nondas.add(srcs[i]);
      }
    }
    String[] tosort = nondas.toArray(new String[0]), sorted = nondas
            .toArray(new String[0]);
    for (int j = 0, jSize = sorted.length; j < jSize; j++)
    {
      tosort[j] = tosort[j].toLowerCase();
    }
    jalview.util.QuickSort.sort(tosort, sorted);
    // construct array with all sources listed

    srcs = new String[sorted.length + dassrc.size()];
    int i = 0;
    for (int j = sorted.length - 1; j >= 0; j--, i++)
    {
      srcs[i] = sorted[j];
      sorted[j] = null;
    }

    sorted = dassrc.toArray(new String[0]);
    tosort = dassrc.toArray(new String[0]);
    for (int j = 0, jSize = sorted.length; j < jSize; j++)
    {
      tosort[j] = tosort[j].toLowerCase();
    }
    jalview.util.QuickSort.sort(tosort, sorted);
    for (int j = sorted.length - 1; j >= 0; j--, i++)
    {
      srcs[i] = sorted[j];
    }
    return srcs;
  }

  /**
   * query the currently defined DAS source registry for sequence sources and
   * add a DasSequenceSource instance for each source to the SequenceFetcher
   * source list.
   */
  public void registerDasSequenceSources()
  {
    // TODO: define a context as a registry provider (either desktop,
    // jalview.bin.cache, or something else).
    for (jalviewSourceI source : jalview.bin.Cache.getDasSourceRegistry()
            .getSources())
    {
      if (source.isSequenceSource())
      {
        List<DbSourceProxy> dassources = source.getSequenceSourceProxies();
        for (DbSourceProxy seqsrc : dassources)
        {
          addDbRefSourceImpl(seqsrc);
        }
      }
    }
  }

}
