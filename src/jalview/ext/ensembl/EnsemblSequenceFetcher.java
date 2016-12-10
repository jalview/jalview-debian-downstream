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

import jalview.datamodel.DBRefSource;
import jalview.ws.seqfetcher.DbSourceProxyImpl;

import com.stevesoft.pat.Regex;

/**
 * A base class for Ensembl sequence fetchers
 * 
 * @author gmcarstairs
 */
abstract class EnsemblSequenceFetcher extends DbSourceProxyImpl
{
  /*
   * accepts ENSG/T/E/P with 11 digits
   * or ENSMUSP or similar for other species
   * or CCDSnnnnn.nn with at least 3 digits
   */
  private static final Regex ACCESSION_REGEX = new Regex(
          "(ENS([A-Z]{3}|)[GTEP]{1}[0-9]{11}$)" + "|" + "(CCDS[0-9.]{3,}$)");

  protected static final String ENSEMBL_GENOMES_REST = "http://rest.ensemblgenomes.org";

  protected static final String ENSEMBL_REST = "http://rest.ensembl.org";

  /*
   * possible values for the 'feature' parameter of the /overlap REST service
   * @see http://rest.ensembl.org/documentation/info/overlap_id
   */
  protected enum EnsemblFeatureType
  {
    gene, transcript, cds, exon, repeat, simple, misc, variation,
    somatic_variation, structural_variation, somatic_structural_variation,
    constrained, regulatory
  }

  private String domain = ENSEMBL_REST;

  @Override
  public String getDbSource()
  {
    // NB ensure Uniprot xrefs are canonicalised from "Ensembl" to "ENSEMBL"
    if (ENSEMBL_GENOMES_REST.equals(getDomain()))
    {
      return DBRefSource.ENSEMBLGENOMES;
    }
    return DBRefSource.ENSEMBL;
  }

  @Override
  public String getAccessionSeparator()
  {
    return " ";
  }

  /**
   * Ensembl accession are ENST + 11 digits for human transcript, ENSG for human
   * gene. Other species insert 3 letters e.g. ENSMUST..., ENSMUSG...
   * 
   * @see http://www.ensembl.org/Help/View?id=151
   */
  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  @Override
  public boolean isValidReference(String accession)
  {
    return getAccessionValidator().search(accession);
  }

  @Override
  public int getTier()
  {
    return 0;
  }

  /**
   * Default test query is a transcript
   */
  @Override
  public String getTestQuery()
  {
    // has CDS on reverse strand:
    return "ENST00000288602";
    // ENST00000461457 // forward strand
  }

  @Override
  public boolean isDnaCoding()
  {
    return true;
  }

  /**
   * Returns the domain name to query e.g. http://rest.ensembl.org or
   * http://rest.ensemblgenomes.org
   * 
   * @return
   */
  protected String getDomain()
  {
    return domain;
  }

  protected void setDomain(String d)
  {
    domain = d;
  }
}
