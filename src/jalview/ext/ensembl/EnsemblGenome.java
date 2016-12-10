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

import jalview.datamodel.SequenceFeature;

/**
 * A client to fetch genomic sequence from Ensembl
 * 
 * TODO: not currently used - delete?
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblGenome extends EnsemblSeqProxy
{
  /*
   * fetch transcript features on genomic sequence (to identify the transcript 
   * regions) and cds, exon and variation features (to retain)
   */
  private static final EnsemblFeatureType[] FEATURES_TO_FETCH = {
      EnsemblFeatureType.transcript, EnsemblFeatureType.exon,
      EnsemblFeatureType.cds, EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblGenome()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblGenome(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (Genomic)";
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.GENOMIC;
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return FEATURES_TO_FETCH;
  }

  /**
   * Answers true unless the feature type is 'transcript' (or a sub-type of
   * transcript in the Sequence Ontology), or has a parent other than the given
   * accession id. Transcript features are only retrieved in order to identify
   * the transcript sequence range, and are redundant information on the
   * transcript sequence itself.
   */
  @Override
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    if (isTranscript(sf.getType()))
    {
      return false;
    }
    return featureMayBelong(sf, accessionId);
  }

  /**
   * Answers true if the sequence feature type is 'transcript' (or a subtype of
   * transcript in the Sequence Ontology), and the ID of the feature is the
   * transcript we are retrieving
   */
  @Override
  protected boolean identifiesSequence(SequenceFeature sf, String accId)
  {
    if (isTranscript(sf.getType()))
    {
      String id = (String) sf.getValue(ID);
      if (("transcript:" + accId).equals(id))
      {
        return true;
      }
    }
    return false;
  }

}
