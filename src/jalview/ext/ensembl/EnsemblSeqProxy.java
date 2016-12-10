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

import jalview.analysis.AlignmentUtils;
import jalview.analysis.Dna;
import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.exceptions.JalviewException;
import jalview.io.FastaFile;
import jalview.io.FileParse;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.Comparison;
import jalview.util.DBRefUtils;
import jalview.util.MapList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for Ensembl sequence fetchers
 * 
 * @see http://rest.ensembl.org/documentation/info/sequence_id
 * @author gmcarstairs
 */
public abstract class EnsemblSeqProxy extends EnsemblRestClient
{
  private static final String ALLELES = "alleles";

  protected static final String PARENT = "Parent";

  protected static final String ID = "ID";

  protected static final String NAME = "Name";

  protected static final String DESCRIPTION = "description";

  /*
   * enum for 'type' parameter to the /sequence REST service
   */
  public enum EnsemblSeqType
  {
    /**
     * type=genomic to fetch full dna including introns
     */
    GENOMIC("genomic"),

    /**
     * type=cdna to fetch coding dna including UTRs
     */
    CDNA("cdna"),

    /**
     * type=cds to fetch coding dna excluding UTRs
     */
    CDS("cds"),

    /**
     * type=protein to fetch peptide product sequence
     */
    PROTEIN("protein");

    /*
     * the value of the 'type' parameter to fetch this version of 
     * an Ensembl sequence
     */
    private String type;

    EnsemblSeqType(String t)
    {
      type = t;
    }

    public String getType()
    {
      return type;
    }

  }

  /**
   * A comparator to sort ranges into ascending start position order
   */
  private class RangeSorter implements Comparator<int[]>
  {
    boolean forwards;

    RangeSorter(boolean forward)
    {
      forwards = forward;
    }

    @Override
    public int compare(int[] o1, int[] o2)
    {
      return (forwards ? 1 : -1) * Integer.compare(o1[0], o2[0]);
    }

  }

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblSeqProxy()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   */
  public EnsemblSeqProxy(String d)
  {
    super(d);
  }

  /**
   * Makes the sequence queries to Ensembl's REST service and returns an
   * alignment consisting of the returned sequences.
   */
  @Override
  public AlignmentI getSequenceRecords(String query) throws Exception
  {
    // TODO use a String... query vararg instead?

    // danger: accession separator used as a regex here, a string elsewhere
    // in this case it is ok (it is just a space), but (e.g.) '\' would not be
    List<String> allIds = Arrays.asList(query
            .split(getAccessionSeparator()));
    AlignmentI alignment = null;
    inProgress = true;

    /*
     * execute queries, if necessary in batches of the
     * maximum allowed number of ids
     */
    int maxQueryCount = getMaximumQueryCount();
    for (int v = 0, vSize = allIds.size(); v < vSize; v += maxQueryCount)
    {
      int p = Math.min(vSize, v + maxQueryCount);
      List<String> ids = allIds.subList(v, p);
      try
      {
        alignment = fetchSequences(ids, alignment);
      } catch (Throwable r)
      {
        inProgress = false;
        String msg = "Aborting ID retrieval after " + v
                + " chunks. Unexpected problem (" + r.getLocalizedMessage()
                + ")";
        System.err.println(msg);
        r.printStackTrace();
        break;
      }
    }

    if (alignment == null)
    {
      return null;
    }

    /*
     * fetch and transfer genomic sequence features,
     * fetch protein product and add as cross-reference
     */
    for (String accId : allIds)
    {
      addFeaturesAndProduct(accId, alignment);
    }

    for (SequenceI seq : alignment.getSequences())
    {
      getCrossReferences(seq);
    }

    return alignment;
  }

  /**
   * Fetches Ensembl features using the /overlap REST endpoint, and adds them to
   * the sequence in the alignment. Also fetches the protein product, maps it
   * from the CDS features of the sequence, and saves it as a cross-reference of
   * the dna sequence.
   * 
   * @param accId
   * @param alignment
   */
  protected void addFeaturesAndProduct(String accId, AlignmentI alignment)
  {
    if (alignment == null)
    {
      return;
    }

    try
    {
      /*
       * get 'dummy' genomic sequence with exon, cds and variation features
       */
      SequenceI genomicSequence = null;
      EnsemblFeatures gffFetcher = new EnsemblFeatures(getDomain());
      EnsemblFeatureType[] features = getFeaturesToFetch();
      AlignmentI geneFeatures = gffFetcher.getSequenceRecords(accId,
              features);
      if (geneFeatures.getHeight() > 0)
      {
        genomicSequence = geneFeatures.getSequenceAt(0);
      }
      if (genomicSequence != null)
      {
        /*
         * transfer features to the query sequence
         */
        SequenceI querySeq = alignment.findName(accId);
        if (transferFeatures(accId, genomicSequence, querySeq))
        {

          /*
           * fetch and map protein product, and add it as a cross-reference
           * of the retrieved sequence
           */
          addProteinProduct(querySeq);
        }
      }
    } catch (IOException e)
    {
      System.err.println("Error transferring Ensembl features: "
              + e.getMessage());
    }
  }

  /**
   * Returns those sequence feature types to fetch from Ensembl. We may want
   * features either because they are of interest to the user, or as means to
   * identify the locations of the sequence on the genomic sequence (CDS
   * features identify CDS, exon features identify cDNA etc).
   * 
   * @return
   */
  protected abstract EnsemblFeatureType[] getFeaturesToFetch();

  /**
   * Fetches and maps the protein product, and adds it as a cross-reference of
   * the retrieved sequence
   */
  protected void addProteinProduct(SequenceI querySeq)
  {
    String accId = querySeq.getName();
    try
    {
      AlignmentI protein = new EnsemblProtein(getDomain())
              .getSequenceRecords(accId);
      if (protein == null || protein.getHeight() == 0)
      {
        System.out.println("No protein product found for " + accId);
        return;
      }
      SequenceI proteinSeq = protein.getSequenceAt(0);

      /*
       * need dataset sequences (to be the subject of mappings)
       */
      proteinSeq.createDatasetSequence();
      querySeq.createDatasetSequence();

      MapList mapList = AlignmentUtils
              .mapCdsToProtein(querySeq, proteinSeq);
      if (mapList != null)
      {
        // clunky: ensure Uniprot xref if we have one is on mapped sequence
        SequenceI ds = proteinSeq.getDatasetSequence();
        // TODO: Verify ensp primary ref is on proteinSeq.getDatasetSequence()
        Mapping map = new Mapping(ds, mapList);
        DBRefEntry dbr = new DBRefEntry(getDbSource(),
                getEnsemblDataVersion(), proteinSeq.getName(), map);
        querySeq.getDatasetSequence().addDBRef(dbr);
        DBRefEntry[] uprots = DBRefUtils.selectRefs(ds.getDBRefs(),
                new String[] { DBRefSource.UNIPROT });
        DBRefEntry[] upxrefs = DBRefUtils.selectRefs(querySeq.getDBRefs(),
                new String[] { DBRefSource.UNIPROT });
        if (uprots != null)
        {
          for (DBRefEntry up : uprots)
          {
            // locate local uniprot ref and map
            List<DBRefEntry> upx = DBRefUtils.searchRefs(upxrefs,
                    up.getAccessionId());
            DBRefEntry upxref;
            if (upx.size() != 0)
            {
              upxref = upx.get(0);

              if (upx.size() > 1)
              {
                Cache.log
                        .warn("Implementation issue - multiple uniprot acc on product sequence.");
              }
            }
            else
            {
              upxref = new DBRefEntry(DBRefSource.UNIPROT,
                      getEnsemblDataVersion(), up.getAccessionId());
            }

            Mapping newMap = new Mapping(ds, mapList);
            upxref.setVersion(getEnsemblDataVersion());
            upxref.setMap(newMap);
            if (upx.size() == 0)
            {
              // add the new uniprot ref
              querySeq.getDatasetSequence().addDBRef(upxref);
            }

          }
        }

        /*
         * copy exon features to protein, compute peptide variants from dna 
         * variants and add as features on the protein sequence ta-da
         */
        AlignmentUtils
                .computeProteinFeatures(querySeq, proteinSeq, mapList);
      }
    } catch (Exception e)
    {
      System.err
              .println(String.format("Error retrieving protein for %s: %s",
                      accId, e.getMessage()));
    }
  }

  /**
   * Get database xrefs from Ensembl, and attach them to the sequence
   * 
   * @param seq
   */
  protected void getCrossReferences(SequenceI seq)
  {
    while (seq.getDatasetSequence() != null)
    {
      seq = seq.getDatasetSequence();
    }

    EnsemblXref xrefFetcher = new EnsemblXref(getDomain(), getDbSource(),
            getEnsemblDataVersion());
    List<DBRefEntry> xrefs = xrefFetcher.getCrossReferences(seq.getName());
    for (DBRefEntry xref : xrefs)
    {
      seq.addDBRef(xref);
    }

    /*
     * and add a reference to itself
     */
    DBRefEntry self = new DBRefEntry(getDbSource(),
            getEnsemblDataVersion(), seq.getName());
    seq.addDBRef(self);
  }

  /**
   * Fetches sequences for the list of accession ids and adds them to the
   * alignment. Returns the extended (or created) alignment.
   * 
   * @param ids
   * @param alignment
   * @return
   * @throws JalviewException
   * @throws IOException
   */
  protected AlignmentI fetchSequences(List<String> ids, AlignmentI alignment)
          throws JalviewException, IOException
  {
    if (!isEnsemblAvailable())
    {
      inProgress = false;
      throw new JalviewException("ENSEMBL Rest API not available.");
    }
    FileParse fp = getSequenceReader(ids);
    if (fp == null)
    {
      return alignment;
    }

    FastaFile fr = new FastaFile(fp);
    if (fr.hasWarningMessage())
    {
      System.out.println(String.format(
              "Warning when retrieving %d ids %s\n%s", ids.size(),
              ids.toString(), fr.getWarningMessage()));
    }
    else if (fr.getSeqs().size() != ids.size())
    {
      System.out.println(String.format(
              "Only retrieved %d sequences for %d query strings", fr
                      .getSeqs().size(), ids.size()));
    }

    if (fr.getSeqs().size() == 1 && fr.getSeqs().get(0).getLength() == 0)
    {
      /*
       * POST request has returned an empty FASTA file e.g. for invalid id
       */
      throw new IOException("No data returned for " + ids);
    }

    if (fr.getSeqs().size() > 0)
    {
      AlignmentI seqal = new Alignment(fr.getSeqsAsArray());
      for (SequenceI sq : seqal.getSequences())
      {
        if (sq.getDescription() == null)
        {
          sq.setDescription(getDbName());
        }
        String name = sq.getName();
        if (ids.contains(name)
                || ids.contains(name.replace("ENSP", "ENST")))
        {
          DBRefEntry dbref = DBRefUtils.parseToDbRef(sq, getDbSource(),
                  getEnsemblDataVersion(), name);
          sq.addDBRef(dbref);
        }
      }
      if (alignment == null)
      {
        alignment = seqal;
      }
      else
      {
        alignment.append(seqal);
      }
    }
    return alignment;
  }

  /**
   * Returns the URL for the REST call
   * 
   * @return
   * @throws MalformedURLException
   */
  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    /*
     * a single id is included in the URL path
     * multiple ids go in the POST body instead
     */
    StringBuffer urlstring = new StringBuffer(128);
    urlstring.append(getDomain() + "/sequence/id");
    if (ids.size() == 1)
    {
      urlstring.append("/").append(ids.get(0));
    }
    // @see https://github.com/Ensembl/ensembl-rest/wiki/Output-formats
    urlstring.append("?type=").append(getSourceEnsemblType().getType());
    urlstring.append(("&Accept=text/x-fasta"));

    URL url = new URL(urlstring.toString());
    return url;
  }

  /**
   * A sequence/id POST request currently allows up to 50 queries
   * 
   * @see http://rest.ensembl.org/documentation/info/sequence_id_post
   */
  @Override
  public int getMaximumQueryCount()
  {
    return 50;
  }

  @Override
  protected boolean useGetRequest()
  {
    return false;
  }

  @Override
  protected String getRequestMimeType(boolean multipleIds)
  {
    return multipleIds ? "application/json" : "text/x-fasta";
  }

  @Override
  protected String getResponseMimeType()
  {
    return "text/x-fasta";
  }

  /**
   * 
   * @return the configured sequence return type for this source
   */
  protected abstract EnsemblSeqType getSourceEnsemblType();

  /**
   * Returns a list of [start, end] genomic ranges corresponding to the sequence
   * being retrieved.
   * 
   * The correspondence between the frames of reference is made by locating
   * those features on the genomic sequence which identify the retrieved
   * sequence. Specifically
   * <ul>
   * <li>genomic sequence is identified by "transcript" features with
   * ID=transcript:transcriptId</li>
   * <li>cdna sequence is identified by "exon" features with
   * Parent=transcript:transcriptId</li>
   * <li>cds sequence is identified by "CDS" features with
   * Parent=transcript:transcriptId</li>
   * </ul>
   * 
   * The returned ranges are sorted to run forwards (for positive strand) or
   * backwards (for negative strand). Aborts and returns null if both positive
   * and negative strand are found (this should not normally happen).
   * 
   * @param sourceSequence
   * @param accId
   * @param start
   *          the start position of the sequence we are mapping to
   * @return
   */
  protected MapList getGenomicRangesFromFeatures(SequenceI sourceSequence,
          String accId, int start)
  {
    SequenceFeature[] sfs = sourceSequence.getSequenceFeatures();
    if (sfs == null)
    {
      return null;
    }

    /*
     * generously initial size for number of cds regions
     * (worst case titin Q8WZ42 has c. 313 exons)
     */
    List<int[]> regions = new ArrayList<int[]>(100);
    int mappedLength = 0;
    int direction = 1; // forward
    boolean directionSet = false;

    for (SequenceFeature sf : sfs)
    {
      /*
       * accept the target feature type or a specialisation of it
       * (e.g. coding_exon for exon)
       */
      if (identifiesSequence(sf, accId))
      {
        int strand = sf.getStrand();
        strand = strand == 0 ? 1 : strand; // treat unknown as forward

        if (directionSet && strand != direction)
        {
          // abort - mix of forward and backward
          System.err.println("Error: forward and backward strand for "
                  + accId);
          return null;
        }
        direction = strand;
        directionSet = true;

        /*
         * add to CDS ranges, semi-sorted forwards/backwards
         */
        if (strand < 0)
        {
          regions.add(0, new int[] { sf.getEnd(), sf.getBegin() });
        }
        else
        {
          regions.add(new int[] { sf.getBegin(), sf.getEnd() });
        }
        mappedLength += Math.abs(sf.getEnd() - sf.getBegin() + 1);

        if (!isSpliceable())
        {
          /*
           * 'gene' sequence is contiguous so we can stop as soon as its
           * identifying feature has been found
           */
          break;
        }
      }
    }

    if (regions.isEmpty())
    {
      System.out.println("Failed to identify target sequence for " + accId
              + " from genomic features");
      return null;
    }

    /*
     * a final sort is needed since Ensembl returns CDS sorted within source
     * (havana / ensembl_havana)
     */
    Collections.sort(regions, new RangeSorter(direction == 1));

    List<int[]> to = Arrays.asList(new int[] { start,
        start + mappedLength - 1 });

    return new MapList(regions, to, 1, 1);
  }

  /**
   * Answers true if the sequence being retrieved may occupy discontiguous
   * regions on the genomic sequence.
   */
  protected boolean isSpliceable()
  {
    return true;
  }

  /**
   * Returns true if the sequence feature marks positions of the genomic
   * sequence feature which are within the sequence being retrieved. For
   * example, an 'exon' feature whose parent is the target transcript marks the
   * cdna positions of the transcript.
   * 
   * @param sf
   * @param accId
   * @return
   */
  protected abstract boolean identifiesSequence(SequenceFeature sf,
          String accId);

  /**
   * Transfers the sequence feature to the target sequence, locating its start
   * and end range based on the mapping. Features which do not overlap the
   * target sequence are ignored.
   * 
   * @param sf
   * @param targetSequence
   * @param mapping
   *          mapping from the sequence feature's coordinates to the target
   *          sequence
   * @param forwardStrand
   */
  protected void transferFeature(SequenceFeature sf,
          SequenceI targetSequence, MapList mapping, boolean forwardStrand)
  {
    int start = sf.getBegin();
    int end = sf.getEnd();
    int[] mappedRange = mapping.locateInTo(start, end);

    if (mappedRange != null)
    {
      SequenceFeature copy = new SequenceFeature(sf);
      copy.setBegin(Math.min(mappedRange[0], mappedRange[1]));
      copy.setEnd(Math.max(mappedRange[0], mappedRange[1]));
      if (".".equals(copy.getFeatureGroup()))
      {
        copy.setFeatureGroup(getDbSource());
      }
      targetSequence.addSequenceFeature(copy);

      /*
       * for sequence_variant on reverse strand, have to convert the allele
       * values to their complements
       */
      if (!forwardStrand
              && SequenceOntologyFactory.getInstance().isA(sf.getType(),
                      SequenceOntologyI.SEQUENCE_VARIANT))
      {
        reverseComplementAlleles(copy);
      }
    }
  }

  /**
   * Change the 'alleles' value of a feature by converting to complementary
   * bases, and also update the feature description to match
   * 
   * @param sf
   */
  static void reverseComplementAlleles(SequenceFeature sf)
  {
    final String alleles = (String) sf.getValue(ALLELES);
    if (alleles == null)
    {
      return;
    }
    StringBuilder complement = new StringBuilder(alleles.length());
    for (String allele : alleles.split(","))
    {
      reverseComplementAllele(complement, allele);
    }
    String comp = complement.toString();
    sf.setValue(ALLELES, comp);
    sf.setDescription(comp);

    /*
     * replace value of "alleles=" in sf.ATTRIBUTES as well
     * so 'output as GFF' shows reverse complement alleles
     */
    String atts = sf.getAttributes();
    if (atts != null)
    {
      atts = atts.replace(ALLELES + "=" + alleles, ALLELES + "=" + comp);
      sf.setAttributes(atts);
    }
  }

  /**
   * Makes the 'reverse complement' of the given allele and appends it to the
   * buffer, after a comma separator if not the first
   * 
   * @param complement
   * @param allele
   */
  static void reverseComplementAllele(StringBuilder complement,
          String allele)
  {
    if (complement.length() > 0)
    {
      complement.append(",");
    }

    /*
     * some 'alleles' are actually descriptive terms 
     * e.g. HGMD_MUTATION, PhenCode_variation
     * - we don't want to 'reverse complement' these
     */
    if (!Comparison.isNucleotideSequence(allele, true))
    {
      complement.append(allele);
    }
    else
    {
      for (int i = allele.length() - 1; i >= 0; i--)
      {
        complement.append(Dna.getComplement(allele.charAt(i)));
      }
    }
  }

  /**
   * Transfers features from sourceSequence to targetSequence
   * 
   * @param accessionId
   * @param sourceSequence
   * @param targetSequence
   * @return true if any features were transferred, else false
   */
  protected boolean transferFeatures(String accessionId,
          SequenceI sourceSequence, SequenceI targetSequence)
  {
    if (sourceSequence == null || targetSequence == null)
    {
      return false;
    }

    // long start = System.currentTimeMillis();
    SequenceFeature[] sfs = sourceSequence.getSequenceFeatures();
    MapList mapping = getGenomicRangesFromFeatures(sourceSequence,
            accessionId, targetSequence.getStart());
    if (mapping == null)
    {
      return false;
    }

    boolean result = transferFeatures(sfs, targetSequence, mapping,
            accessionId);
    // System.out.println("transferFeatures (" + (sfs.length) + " --> "
    // + targetSequence.getSequenceFeatures().length + ") to "
    // + targetSequence.getName()
    // + " took " + (System.currentTimeMillis() - start) + "ms");
    return result;
  }

  /**
   * Transfer features to the target sequence. The start/end positions are
   * converted using the mapping. Features which do not overlap are ignored.
   * Features whose parent is not the specified identifier are also ignored.
   * 
   * @param features
   * @param targetSequence
   * @param mapping
   * @param parentId
   * @return
   */
  protected boolean transferFeatures(SequenceFeature[] features,
          SequenceI targetSequence, MapList mapping, String parentId)
  {
    final boolean forwardStrand = mapping.isFromForwardStrand();

    /*
     * sort features by start position (which corresponds to end
     * position descending if reverse strand) so as to add them in
     * 'forwards' order to the target sequence
     */
    sortFeatures(features, forwardStrand);

    boolean transferred = false;
    for (SequenceFeature sf : features)
    {
      if (retainFeature(sf, parentId))
      {
        transferFeature(sf, targetSequence, mapping, forwardStrand);
        transferred = true;
      }
    }
    return transferred;
  }

  /**
   * Sort features by start position ascending (if on forward strand), or end
   * position descending (if on reverse strand)
   * 
   * @param features
   * @param forwardStrand
   */
  protected static void sortFeatures(SequenceFeature[] features,
          final boolean forwardStrand)
  {
    Arrays.sort(features, new Comparator<SequenceFeature>()
    {
      @Override
      public int compare(SequenceFeature o1, SequenceFeature o2)
      {
        if (forwardStrand)
        {
          return Integer.compare(o1.getBegin(), o2.getBegin());
        }
        else
        {
          return Integer.compare(o2.getEnd(), o1.getEnd());
        }
      }
    });
  }

  /**
   * Answers true if the feature type is one we want to keep for the sequence.
   * Some features are only retrieved in order to identify the sequence range,
   * and may then be discarded as redundant information (e.g. "CDS" feature for
   * a CDS sequence).
   */
  @SuppressWarnings("unused")
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    return true; // override as required
  }

  /**
   * Answers true if the feature has a Parent which refers to the given
   * accession id, or if the feature has no parent. Answers false if the
   * feature's Parent is for a different accession id.
   * 
   * @param sf
   * @param identifier
   * @return
   */
  protected boolean featureMayBelong(SequenceFeature sf, String identifier)
  {
    String parent = (String) sf.getValue(PARENT);
    // using contains to allow for prefix "gene:", "transcript:" etc
    if (parent != null && !parent.contains(identifier))
    {
      // this genomic feature belongs to a different transcript
      return false;
    }
    return true;
  }

  @Override
  public String getDescription()
  {
    return "Ensembl " + getSourceEnsemblType().getType()
            + " sequence with variant features";
  }

  /**
   * Returns a (possibly empty) list of features on the sequence which have the
   * specified sequence ontology type (or a sub-type of it), and the given
   * identifier as parent
   * 
   * @param sequence
   * @param type
   * @param parentId
   * @return
   */
  protected List<SequenceFeature> findFeatures(SequenceI sequence,
          String type, String parentId)
  {
    List<SequenceFeature> result = new ArrayList<SequenceFeature>();

    SequenceFeature[] sfs = sequence.getSequenceFeatures();
    if (sfs != null)
    {
      SequenceOntologyI so = SequenceOntologyFactory.getInstance();
      for (SequenceFeature sf : sfs)
      {
        if (so.isA(sf.getType(), type))
        {
          String parent = (String) sf.getValue(PARENT);
          if (parent.equals(parentId))
          {
            result.add(sf);
          }
        }
      }
    }
    return result;
  }

  /**
   * Answers true if the feature type is either 'NMD_transcript_variant' or
   * 'transcript' or one of its sub-types in the Sequence Ontology. This is
   * needed because NMD_transcript_variant behaves like 'transcript' in Ensembl
   * although strictly speaking it is not (it is a sub-type of
   * sequence_variant).
   * 
   * @param featureType
   * @return
   */
  public static boolean isTranscript(String featureType)
  {
    return SequenceOntologyI.NMD_TRANSCRIPT_VARIANT.equals(featureType)
            || SequenceOntologyFactory.getInstance().isA(featureType,
                    SequenceOntologyI.TRANSCRIPT);
  }
}
