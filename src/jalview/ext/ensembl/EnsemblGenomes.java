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
package jalview.ext.ensembl;

/**
 * A class to behave much like EnsemblGene but referencing the ensemblgenomes
 * domain and data
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblGenomes extends EnsemblGene
{
  /**
   * Constructor sets domain to rest.ensemblgenomes.org instead of the 'usual'
   * rest.ensembl.org
   */
  public EnsemblGenomes()
  {
    super(ENSEMBL_GENOMES_REST);
  }

  @Override
  public boolean isGeneIdentifier(String query)
  {
    return true;
  }

  @Override
  public String getDbName()
  {
    return "EnsemblGenomes";
  }

  @Override
  public String getTestQuery()
  {
    return "DDB_G0283883";
  }

  @Override
  public String getDbSource()
  {
    return "EnsemblGenomes";
  }

}
