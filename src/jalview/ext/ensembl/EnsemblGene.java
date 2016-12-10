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

import jalview.api.FeatureColourI;
import jalview.api.FeatureSettingsModelI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;
import jalview.schemes.FeatureColour;
import jalview.schemes.FeatureSettingsAdapter;
import jalview.util.MapList;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stevesoft.pat.Regex;

/**
 * A class that fetches genomic sequence and all transcripts for an Ensembl gene
 * 
 * @author gmcarstairs
 */
public class EnsemblGene extends EnsemblSeqProxy
{
  private static final String GENE_PREFIX = "gene:";

  /*
   * accepts anything as we will attempt lookup of gene or 
   * transcript id or gene name
   */
  private static final Regex ACCESSION_REGEX = new Regex(".*");

  private static final EnsemblFeatureType[] FEATURES_TO_FETCH = {
      EnsemblFeatureType.gene, EnsemblFeatureType.transcript,
      EnsemblFeatureType.exon, EnsemblFeatureType.cds,
      EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblGene()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblGene(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL";
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return FEATURES_TO_FETCH;
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.GENOMIC;
  }

  /**
   * Returns an alignment containing the gene(s) for the given gene or
   * transcript identifier, or external identifier (e.g. Uniprot id). If given a
   * gene name or external identifier, returns any related gene sequences found
   * for model organisms. If only a single gene is queried for, then its
   * transcripts are also retrieved and added to the alignment. <br>
   * Method:
   * <ul>
   * <li>resolves a transcript identifier by looking up its parent gene id</li>
   * <li>resolves an external identifier by looking up xref-ed gene ids</li>
   * <li>fetches the gene sequence</li>
   * <li>fetches features on the sequence</li>
   * <li>identifies "transcript" features whose Parent is the requested gene</li>
   * <li>fetches the transcript sequence for each transcript</li>
   * <li>makes a mapping from the gene to each transcript</li>
   * <li>copies features from gene to transcript sequences</li>
   * <li>fetches the protein sequence for each transcript, maps and saves it as
   * a cross-reference</li>
   * <li>aligns each transcript against the gene sequence based on the position
   * mappings</li>
   * </ul>
   * 
   * @param query
   *          a single gene or transcript identifier or gene name
   * @return an alignment containing a gene, and possibly transcripts, or null
   */
  @Override
  public AlignmentI getSequenceRecords(String query) throws Exception
  {
    /*
     * convert to a non-duplicated list of gene identifiers
     */
    List<String> geneIds = getGeneIds(query);

    AlignmentI al = null;
    for (String geneId : geneIds)
    {
      /*
       * fetch the gene sequence(s) with features and xrefs
       */
      AlignmentI geneAlignment = super.getSequenceRecords(geneId);
      if (geneAlignment == null)
      {
        continue;
      }
      if (geneAlignment.getHeight() == 1)
      {
        getTranscripts(geneAlignment, geneId);
      }
      if (al == null)
      {
        al = geneAlignment;
      }
      else
      {
        al.append(geneAlignment);
      }
    }
    return al;
  }

  /**
   * Converts a query, which may contain one or more gene or transcript
   * identifiers, into a non-redundant list of gene identifiers.
   * 
   * @param accessions
   * @return
   */
  List<String> getGeneIds(String accessions)
  {
    List<String> geneIds = new ArrayList<String>();

    for (String acc : accessions.split(getAccessionSeparator()))
    {
      if (isGeneIdentifier(acc))
      {
        if (!geneIds.contains(acc))
        {
          geneIds.add(acc);
        }
      }

      /*
       * if given a transcript id, look up its gene parent
       */
      else if (isTranscriptIdentifier(acc))
      {
        String geneId = new EnsemblLookup(getDomain()).getParent(acc);
        if (geneId != null && !geneIds.contains(geneId))
        {
          geneIds.add(geneId);
        }
      }

      /*
       * if given a gene or other external name, lookup and fetch 
       * the corresponding gene for all model organisms 
       */
      else
      {
        List<String> ids = new EnsemblSymbol(getDomain(), getDbSource(),
                getDbVersion()).getIds(acc);
        for (String geneId : ids)
        {
          if (!geneIds.contains(geneId))
          {
            geneIds.add(geneId);
          }
        }
      }
    }
    return geneIds;
  }

  /**
   * Attempts to get Ensembl stable identifiers for model organisms for a gene
   * name by calling the xrefs symbol REST service to resolve the gene name.
   * 
   * @param query
   * @return
   */
  protected String getGeneIdentifiersForName(String query)
  {
    List<String> ids = new EnsemblSymbol(getDomain(), getDbSource(),
            getDbVersion()).getIds(query);
    if (ids != null)
    {
      for (String id : ids)
      {
        if (isGeneIdentifier(id))
        {
          return id;
        }
      }
    }
    return null;
  }

  /**
   * Constructs all transcripts for the gene, as identified by "transcript"
   * features whose Parent is the requested gene. The coding transcript
   * sequences (i.e. with introns omitted) are added to the alignment.
   * 
   * @param al
   * @param accId
   * @throws Exception
   */
  protected void getTranscripts(AlignmentI al, String accId)
          throws Exception
  {
    SequenceI gene = al.getSequenceAt(0);
    List<SequenceFeature> transcriptFeatures = getTranscriptFeatures(accId,
            gene);

    for (SequenceFeature transcriptFeature : transcriptFeatures)
    {
      makeTranscript(transcriptFeature, al, gene);
    }

    clearGeneFeatures(gene);
  }

  /**
   * Remove unwanted features (transcript, exon, CDS) from the gene sequence
   * after we have used them to derive transcripts and transfer features
   * 
   * @param gene
   */
  protected void clearGeneFeatures(SequenceI gene)
  {
    SequenceFeature[] sfs = gene.getSequenceFeatures();
    if (sfs != null)
    {
      SequenceOntologyI so = SequenceOntologyFactory.getInstance();
      List<SequenceFeature> filtered = new ArrayList<SequenceFeature>();
      for (SequenceFeature sf : sfs)
      {
        String type = sf.getType();
        if (!isTranscript(type) && !so.isA(type, SequenceOntologyI.EXON)
                && !so.isA(type, SequenceOntologyI.CDS))
        {
          filtered.add(sf);
        }
      }
      gene.setSequenceFeatures(filtered
              .toArray(new SequenceFeature[filtered.size()]));
    }
  }

  /**
   * Constructs a spliced transcript sequence by finding 'exon' features for the
   * given id (or failing that 'CDS'). Copies features on to the new sequence.
   * 'Aligns' the new sequence against the gene sequence by padding with gaps,
   * and adds it to the alignment.
   * 
   * @param transcriptFeature
   * @param al
   *          the alignment to which to add the new sequence
   * @param gene
   *          the parent gene sequence, with features
   * @return
   */
  SequenceI makeTranscript(SequenceFeature transcriptFeature,
          AlignmentI al, SequenceI gene)
  {
    String accId = getTranscriptId(transcriptFeature);
    if (accId == null)
    {
      return null;
    }

    /*
     * NB we are mapping from gene sequence (not genome), so do not
     * need to check for reverse strand (gene and transcript sequences 
     * are in forward sense)
     */

    /*
     * make a gene-length sequence filled with gaps
     * we will fill in the bases for transcript regions
     */
    char[] seqChars = new char[gene.getLength()];
    Arrays.fill(seqChars, al.getGapCharacter());

    /*
     * look for exon features of the transcript, failing that for CDS
     * (for example ENSG00000124610 has 1 CDS but no exon features)
     */
    String parentId = "transcript:" + accId;
    List<SequenceFeature> splices = findFeatures(gene,
            SequenceOntologyI.EXON, parentId);
    if (splices.isEmpty())
    {
      splices = findFeatures(gene, SequenceOntologyI.CDS, parentId);
    }

    int transcriptLength = 0;
    final char[] geneChars = gene.getSequence();
    int offset = gene.getStart(); // to convert to 0-based positions
    List<int[]> mappedFrom = new ArrayList<int[]>();

    for (SequenceFeature sf : splices)
    {
      int start = sf.getBegin() - offset;
      int end = sf.getEnd() - offset;
      int spliceLength = end - start + 1;
      System.arraycopy(geneChars, start, seqChars, start, spliceLength);
      transcriptLength += spliceLength;
      mappedFrom.add(new int[] { sf.getBegin(), sf.getEnd() });
    }

    Sequence transcript = new Sequence(accId, seqChars, 1, transcriptLength);

    /*
     * Ensembl has gene name as transcript Name
     * EnsemblGenomes doesn't, but has a url-encoded description field
     */
    String description = (String) transcriptFeature.getValue(NAME);
    if (description == null)
    {
      description = (String) transcriptFeature.getValue(DESCRIPTION);
    }
    if (description != null)
    {
      try
      {
        transcript.setDescription(URLDecoder.decode(description, "UTF-8"));
      } catch (UnsupportedEncodingException e)
      {
        e.printStackTrace(); // as if
      }
    }
    transcript.createDatasetSequence();

    al.addSequence(transcript);

    /*
     * transfer features to the new sequence; we use EnsemblCdna to do this,
     * to filter out unwanted features types (see method retainFeature)
     */
    List<int[]> mapTo = new ArrayList<int[]>();
    mapTo.add(new int[] { 1, transcriptLength });
    MapList mapping = new MapList(mappedFrom, mapTo, 1, 1);
    EnsemblCdna cdna = new EnsemblCdna(getDomain());
    cdna.transferFeatures(gene.getSequenceFeatures(),
            transcript.getDatasetSequence(), mapping, parentId);

    /*
     * fetch and save cross-references
     */
    cdna.getCrossReferences(transcript);

    /*
     * and finally fetch the protein product and save as a cross-reference
     */
    cdna.addProteinProduct(transcript);

    return transcript;
  }

  /**
   * Returns the 'transcript_id' property of the sequence feature (or null)
   * 
   * @param feature
   * @return
   */
  protected String getTranscriptId(SequenceFeature feature)
  {
    return (String) feature.getValue("transcript_id");
  }

  /**
   * Returns a list of the transcript features on the sequence whose Parent is
   * the gene for the accession id.
   * 
   * @param accId
   * @param geneSequence
   * @return
   */
  protected List<SequenceFeature> getTranscriptFeatures(String accId,
          SequenceI geneSequence)
  {
    List<SequenceFeature> transcriptFeatures = new ArrayList<SequenceFeature>();

    String parentIdentifier = GENE_PREFIX + accId;
    SequenceFeature[] sfs = geneSequence.getSequenceFeatures();

    if (sfs != null)
    {
      for (SequenceFeature sf : sfs)
      {
        if (isTranscript(sf.getType()))
        {
          String parent = (String) sf.getValue(PARENT);
          if (parentIdentifier.equals(parent))
          {
            transcriptFeatures.add(sf);
          }
        }
      }
    }

    return transcriptFeatures;
  }

  @Override
  public String getDescription()
  {
    return "Fetches all transcripts and variant features for a gene or transcript";
  }

  /**
   * Default test query is a gene id (can also enter a transcript id)
   */
  @Override
  public String getTestQuery()
  {
    return "ENSG00000157764"; // BRAF, 5 transcripts, reverse strand
    // ENSG00000090266 // NDUFB2, 15 transcripts, forward strand
    // ENSG00000101812 // H2BFM histone, 3 transcripts, forward strand
    // ENSG00000123569 // H2BFWT histone, 2 transcripts, reverse strand
  }

  /**
   * Answers true for a feature of type 'gene' (or a sub-type of gene in the
   * Sequence Ontology), whose ID is the accession we are retrieving
   */
  @Override
  protected boolean identifiesSequence(SequenceFeature sf, String accId)
  {
    if (SequenceOntologyFactory.getInstance().isA(sf.getType(),
            SequenceOntologyI.GENE))
    {
      String id = (String) sf.getValue(ID);
      if ((GENE_PREFIX + accId).equals(id))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Answers true unless feature type is 'gene', or 'transcript' with a parent
   * which is a different gene. We need the gene features to identify the range,
   * but it is redundant information on the gene sequence. Checking the parent
   * allows us to drop transcript features which belong to different
   * (overlapping) genes.
   */
  @Override
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    SequenceOntologyI so = SequenceOntologyFactory.getInstance();
    String type = sf.getType();
    if (so.isA(type, SequenceOntologyI.GENE))
    {
      return false;
    }
    if (isTranscript(type))
    {
      String parent = (String) sf.getValue(PARENT);
      if (!(GENE_PREFIX + accessionId).equals(parent))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Answers false. This allows an optimisation - a single 'gene' feature is all
   * that is needed to identify the positions of the gene on the genomic
   * sequence.
   */
  @Override
  protected boolean isSpliceable()
  {
    return false;
  }

  /**
   * Override to do nothing as Ensembl doesn't return a protein sequence for a
   * gene identifier
   */
  @Override
  protected void addProteinProduct(SequenceI querySeq)
  {
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  /**
   * Returns a descriptor for suitable feature display settings with
   * <ul>
   * <li>only exon or sequence_variant features (or their subtypes in the
   * Sequence Ontology) visible</li>
   * <li>variant features coloured red</li>
   * <li>exon features coloured by label (exon name)</li>
   * <li>variants displayed above (on top of) exons</li>
   * </ul>
   */
  @Override
  public FeatureSettingsModelI getFeatureColourScheme()
  {
    return new FeatureSettingsAdapter()
    {
      SequenceOntologyI so = SequenceOntologyFactory.getInstance();

      @Override
      public boolean isFeatureDisplayed(String type)
      {
        return (so.isA(type, SequenceOntologyI.EXON) || so.isA(type,
                SequenceOntologyI.SEQUENCE_VARIANT));
      }

      @Override
      public FeatureColourI getFeatureColour(String type)
      {
        if (so.isA(type, SequenceOntologyI.EXON))
        {
          return new FeatureColour()
          {
            @Override
            public boolean isColourByLabel()
            {
              return true;
            }
          };
        }
        if (so.isA(type, SequenceOntologyI.SEQUENCE_VARIANT))
        {
          return new FeatureColour()
          {

            @Override
            public Color getColour()
            {
              return Color.RED;
            }
          };
        }
        return null;
      }

      /**
       * order to render sequence_variant after exon after the rest
       */
      @Override
      public int compare(String feature1, String feature2)
      {
        if (so.isA(feature1, SequenceOntologyI.SEQUENCE_VARIANT))
        {
          return +1;
        }
        if (so.isA(feature2, SequenceOntologyI.SEQUENCE_VARIANT))
        {
          return -1;
        }
        if (so.isA(feature1, SequenceOntologyI.EXON))
        {
          return +1;
        }
        if (so.isA(feature2, SequenceOntologyI.EXON))
        {
          return -1;
        }
        return 0;
      }
    };
  }

}
