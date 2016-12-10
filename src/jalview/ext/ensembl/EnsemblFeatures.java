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

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.io.FeaturesFile;
import jalview.io.FileParse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A client for fetching and processing Ensembl feature data in GFF format by
 * calling the overlap REST service
 * 
 * @author gmcarstairs
 * @see http://rest.ensembl.org/documentation/info/overlap_id
 */
class EnsemblFeatures extends EnsemblRestClient
{
  /*
   * The default features to retrieve from Ensembl
   * can override in getSequenceRecords parameter
   */
  private EnsemblFeatureType[] featuresWanted = { EnsemblFeatureType.cds,
      EnsemblFeatureType.exon, EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblFeatures()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblFeatures(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (features)";
  }

  /**
   * Makes a query to the REST overlap endpoint for the given sequence
   * identifier. This returns an 'alignment' consisting of one 'dummy sequence'
   * (the genomic sequence for which overlap features are returned by the
   * service). This sequence will have on it sequence features which are the
   * real information of interest, such as CDS regions or sequence variations.
   */
  @Override
  public AlignmentI getSequenceRecords(String query) throws IOException
  {
    // TODO: use a vararg String... for getSequenceRecords instead?
    List<String> queries = new ArrayList<String>();
    queries.add(query);
    FileParse fp = getSequenceReader(queries);
    FeaturesFile fr = new FeaturesFile(fp);
    return new Alignment(fr.getSeqsAsArray());
  }

  /**
   * Returns a URL for the REST overlap endpoint
   * 
   * @param ids
   * @return
   */
  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    StringBuffer urlstring = new StringBuffer(128);
    urlstring.append(getDomain()).append("/overlap/id/").append(ids.get(0));

    // @see https://github.com/Ensembl/ensembl-rest/wiki/Output-formats
    urlstring.append("?content-type=text/x-gff3");

    /*
     * specify  features to retrieve
     * @see http://rest.ensembl.org/documentation/info/overlap_id
     * could make the list a configurable entry in jalview.properties
     */
    for (EnsemblFeatureType feature : featuresWanted)
    {
      urlstring.append("&feature=").append(feature.name());
    }

    return new URL(urlstring.toString());
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  /**
   * Returns the MIME type for GFF3. For GET requests the Content-type header
   * describes the required encoding of the response.
   */
  @Override
  protected String getRequestMimeType(boolean multipleIds)
  {
    return "text/x-gff3";
  }

  /**
   * Returns the MIME type for GFF3.
   */
  @Override
  protected String getResponseMimeType()
  {
    return "text/x-gff3";
  }

  /**
   * Overloaded method that allows a list of features to retrieve to be
   * specified
   * 
   * @param accId
   * @param features
   * @return
   * @throws IOException
   */
  protected AlignmentI getSequenceRecords(String accId,
          EnsemblFeatureType[] features) throws IOException
  {
    featuresWanted = features;
    return getSequenceRecords(accId);
  }
}
