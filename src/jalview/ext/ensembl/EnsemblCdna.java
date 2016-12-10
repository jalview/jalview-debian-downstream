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
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;

import com.stevesoft.pat.Regex;

/**
 * A client to fetch CDNA sequence from Ensembl (i.e. that part of the genomic
 * sequence that is transcribed to RNA, but not necessarily translated to
 * protein)
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblCdna extends EnsemblSeqProxy
{
  /*
   * accepts ENST or ENSTG with 11 digits
   * or ENSMUST or similar for other species
   * or CCDSnnnnn.nn with at least 3 digits
   */
  private static final Regex ACCESSION_REGEX = new Regex(
          "(ENS([A-Z]{3}|)[TG][0-9]{11}$)" + "|" + "(CCDS[0-9.]{3,}$)");

  /*
   * fetch exon features on genomic sequence (to identify the cdna regions)
   * and cds and variation features (to retain)
   */
  private static final EnsemblFeatureType[] FEATURES_TO_FETCH = {
      EnsemblFeatureType.exon, EnsemblFeatureType.cds,
      EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblCdna()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblCdna(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (CDNA)";
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.CDNA;
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return FEATURES_TO_FETCH;
  }

  /**
   * Answers true unless the feature type is 'transcript' (or a sub-type in the
   * Sequence Ontology).
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
   * Answers true if the sequence feature type is 'exon' (or a subtype of exon
   * in the Sequence Ontology), and the Parent of the feature is the transcript
   * we are retrieving
   */
  @Override
  protected boolean identifiesSequence(SequenceFeature sf, String accId)
  {
    if (SequenceOntologyFactory.getInstance().isA(sf.getType(),
            SequenceOntologyI.EXON))
    {
      String parentFeature = (String) sf.getValue(PARENT);
      if (("transcript:" + accId).equals(parentFeature))
      {
        return true;
      }
    }
    return false;
  }

}
