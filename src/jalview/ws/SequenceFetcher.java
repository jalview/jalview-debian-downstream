/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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
import jalview.ws.dbsources.EmblCdsSource;
import jalview.ws.dbsources.EmblSource;
import jalview.ws.dbsources.Pdb;
import jalview.ws.dbsources.PfamFull;
import jalview.ws.dbsources.PfamSeed;
import jalview.ws.dbsources.RfamSeed;
import jalview.ws.dbsources.Uniprot;
import jalview.ws.seqfetcher.ASequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.ArrayList;

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
    addDBRefSourceImpl(EnsemblGene.class);
    // addDBRefSourceImpl(EnsemblGenomes.class);
    addDBRefSourceImpl(EmblSource.class);
    addDBRefSourceImpl(EmblCdsSource.class);
    addDBRefSourceImpl(Uniprot.class);
    addDBRefSourceImpl(Pdb.class);
    addDBRefSourceImpl(PfamFull.class);
    addDBRefSourceImpl(PfamSeed.class);
    addDBRefSourceImpl(RfamSeed.class);
  }

  /**
   * return an ordered list of database sources excluding alignment only databases
   */
  public String[] getOrderedSupportedSources()
  {
    String[] srcs = this.getSupportedDb();
    ArrayList<String> src = new ArrayList<>();

    for (int i = 0; i < srcs.length; i++)
    {
      boolean skip = false;
      for (DbSourceProxy dbs : getSourceProxy(srcs[i]))
      {
        // Skip the alignment databases for the moment - they're not useful for
        // verifying a single sequence against its reference source
        if (dbs.isAlignmentSource())
        {
          skip = true;
        }
      }
      if (skip)
      {
        continue;
      }
      {
        src.add(srcs[i]);
      }
    }
    String[] tosort = src.toArray(new String[0]),
            sorted = src.toArray(new String[0]);
    for (int j = 0, jSize = sorted.length; j < jSize; j++)
    {
      tosort[j] = tosort[j].toLowerCase();
    }
    jalview.util.QuickSort.sort(tosort, sorted);
    // construct array with all sources listed
    int i = 0;
    for (int j = sorted.length - 1; j >= 0; j--, i++)
    {
      tosort[i] = sorted[j];
    }
    return tosort;
  }
}
