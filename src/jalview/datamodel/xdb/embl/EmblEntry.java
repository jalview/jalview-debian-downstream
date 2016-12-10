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
package jalview.datamodel.xdb.embl;

import jalview.analysis.SequenceIdMatcher;
import jalview.bin.Cache;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.FeatureProperties;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.DBRefUtils;
import jalview.util.DnaUtils;
import jalview.util.MapList;
import jalview.util.MappingUtils;
import jalview.util.StringUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Data model for one entry returned from an EMBL query, as marshalled by a
 * Castor binding file
 * 
 * For example: http://www.ebi.ac.uk/ena/data/view/J03321&display=xml
 * 
 * @see embl_mapping.xml
 */
public class EmblEntry
{
  private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

  String accession;

  String entryVersion;

  String sequenceVersion;

  String dataClass;

  String moleculeType;

  String topology;

  String sequenceLength;

  String taxonomicDivision;

  String description;

  String firstPublicDate;

  String firstPublicRelease;

  String lastUpdatedDate;

  String lastUpdatedRelease;

  Vector<String> keywords;

  Vector<DBRefEntry> dbRefs;

  Vector<EmblFeature> features;

  EmblSequence sequence;

  /**
   * @return the accession
   */
  public String getAccession()
  {
    return accession;
  }

  /**
   * @param accession
   *          the accession to set
   */
  public void setAccession(String accession)
  {
    this.accession = accession;
  }

  /**
   * @return the dbRefs
   */
  public Vector<DBRefEntry> getDbRefs()
  {
    return dbRefs;
  }

  /**
   * @param dbRefs
   *          the dbRefs to set
   */
  public void setDbRefs(Vector<DBRefEntry> dbRefs)
  {
    this.dbRefs = dbRefs;
  }

  /**
   * @return the features
   */
  public Vector<EmblFeature> getFeatures()
  {
    return features;
  }

  /**
   * @param features
   *          the features to set
   */
  public void setFeatures(Vector<EmblFeature> features)
  {
    this.features = features;
  }

  /**
   * @return the keywords
   */
  public Vector<String> getKeywords()
  {
    return keywords;
  }

  /**
   * @param keywords
   *          the keywords to set
   */
  public void setKeywords(Vector<String> keywords)
  {
    this.keywords = keywords;
  }

  /**
   * @return the sequence
   */
  public EmblSequence getSequence()
  {
    return sequence;
  }

  /**
   * @param sequence
   *          the sequence to set
   */
  public void setSequence(EmblSequence sequence)
  {
    this.sequence = sequence;
  }

  /**
   * Recover annotated sequences from EMBL file
   * 
   * @param sourceDb
   * @param peptides
   *          a list of protein products found so far (to add to)
   * @return dna dataset sequence with DBRefs and features
   */
  public SequenceI getSequence(String sourceDb, List<SequenceI> peptides)
  {
    SequenceI dna = makeSequence(sourceDb);
    if (dna == null)
    {
      return null;
    }
    dna.setDescription(description);
    DBRefEntry retrievedref = new DBRefEntry(sourceDb,
            getSequenceVersion(), accession);
    dna.addDBRef(retrievedref);
    // add map to indicate the sequence is a valid coordinate frame for the
    // dbref
    retrievedref.setMap(new Mapping(null, new int[] { 1, dna.getLength() },
            new int[] { 1, dna.getLength() }, 1, 1));

    /*
     * transform EMBL Database refs to canonical form
     */
    if (dbRefs != null)
    {
      for (DBRefEntry dbref : dbRefs)
      {
        dbref.setSource(DBRefUtils.getCanonicalName(dbref.getSource()));
        dna.addDBRef(dbref);
      }
    }

    SequenceIdMatcher matcher = new SequenceIdMatcher(peptides);
    try
    {
      for (EmblFeature feature : features)
      {
        if (FeatureProperties.isCodingFeature(sourceDb, feature.getName()))
        {
          parseCodingFeature(feature, sourceDb, dna, peptides, matcher);
        }
      }
    } catch (Exception e)
    {
      System.err.println("EMBL Record Features parsing error!");
      System.err
              .println("Please report the following to help@jalview.org :");
      System.err.println("EMBL Record " + accession);
      System.err.println("Resulted in exception: " + e.getMessage());
      e.printStackTrace(System.err);
    }

    return dna;
  }

  /**
   * @param sourceDb
   * @return
   */
  SequenceI makeSequence(String sourceDb)
  {
    if (sequence == null)
    {
      System.err.println("No sequence was returned for ENA accession "
              + accession);
      return null;
    }
    SequenceI dna = new Sequence(sourceDb + "|" + accession,
            sequence.getSequence());
    return dna;
  }

  /**
   * Extracts coding region and product from a CDS feature and properly decorate
   * it with annotations.
   * 
   * @param feature
   *          coding feature
   * @param sourceDb
   *          source database for the EMBLXML
   * @param dna
   *          parent dna sequence for this record
   * @param peptides
   *          list of protein product sequences for Embl entry
   * @param matcher
   *          helper to match xrefs in already retrieved sequences
   */
  void parseCodingFeature(EmblFeature feature, String sourceDb,
          SequenceI dna, List<SequenceI> peptides, SequenceIdMatcher matcher)
  {
    boolean isEmblCdna = sourceDb.equals(DBRefSource.EMBLCDS);

    int[] exons = getCdsRanges(feature);

    String translation = null;
    String proteinName = "";
    String proteinId = null;
    Map<String, String> vals = new Hashtable<String, String>();

    /*
     * codon_start 1/2/3 in EMBL corresponds to phase 0/1/2 in CDS
     * (phase is required for CDS features in GFF3 format)
     */
    int codonStart = 1;

    /*
     * parse qualifiers, saving protein translation, protein id,
     * codon start position, product (name), and 'other values'
     */
    if (feature.getQualifiers() != null)
    {
      for (Qualifier q : feature.getQualifiers())
      {
        String qname = q.getName();
        if (qname.equals("translation"))
        {
          // remove all spaces (precompiled String.replaceAll(" ", ""))
          translation = SPACE_PATTERN.matcher(q.getValues()[0]).replaceAll(
                  "");
        }
        else if (qname.equals("protein_id"))
        {
          proteinId = q.getValues()[0].trim();
        }
        else if (qname.equals("codon_start"))
        {
          try
          {
            codonStart = Integer.parseInt(q.getValues()[0].trim());
          } catch (NumberFormatException e)
          {
            System.err.println("Invalid codon_start in XML for "
                    + accession + ": " + e.getMessage());
          }
        }
        else if (qname.equals("product"))
        {
          // sometimes name is returned e.g. for V00488
          proteinName = q.getValues()[0].trim();
        }
        else
        {
          // throw anything else into the additional properties hash
          String[] qvals = q.getValues();
          if (qvals != null)
          {
            String commaSeparated = StringUtils.arrayToSeparatorList(qvals,
                    ",");
            vals.put(qname, commaSeparated);
          }
        }
      }
    }

    DBRefEntry proteinToEmblProteinRef = null;
    exons = MappingUtils.removeStartPositions(codonStart - 1, exons);

    SequenceI product = null;
    Mapping dnaToProteinMapping = null;
    if (translation != null && proteinName != null && proteinId != null)
    {
      int translationLength = translation.length();

      /*
       * look for product in peptides list, if not found, add it
       */
      product = matcher.findIdMatch(proteinId);
      if (product == null)
      {
        product = new Sequence(proteinId, translation, 1, translationLength);
        product.setDescription(((proteinName.length() == 0) ? "Protein Product from "
                + sourceDb
                : proteinName));
        peptides.add(product);
        matcher.add(product);
      }

      // we have everything - create the mapping and perhaps the protein
      // sequence
      if (exons == null || exons.length == 0)
      {
        /*
         * workaround until we handle dna location for CDS sequence
         * e.g. location="X53828.1:60..1058" correctly
         */
        System.err
                .println("Implementation Notice: EMBLCDS records not properly supported yet - Making up the CDNA region of this sequence... may be incorrect ("
                        + sourceDb + ":" + getAccession() + ")");
        if (translationLength * 3 == (1 - codonStart + dna.getSequence().length))
        {
          System.err
                  .println("Not allowing for additional stop codon at end of cDNA fragment... !");
          // this might occur for CDS sequences where no features are marked
          exons = new int[] { dna.getStart() + (codonStart - 1),
              dna.getEnd() };
          dnaToProteinMapping = new Mapping(product, exons, new int[] { 1,
              translationLength }, 3, 1);
        }
        if ((translationLength + 1) * 3 == (1 - codonStart + dna
                .getSequence().length))
        {
          System.err
                  .println("Allowing for additional stop codon at end of cDNA fragment... will probably cause an error in VAMSAs!");
          exons = new int[] { dna.getStart() + (codonStart - 1),
              dna.getEnd() - 3 };
          dnaToProteinMapping = new Mapping(product, exons, new int[] { 1,
              translationLength }, 3, 1);
        }
      }
      else
      {
        // Trim the exon mapping if necessary - the given product may only be a
        // fragment of a larger protein. (EMBL:AY043181 is an example)

        if (isEmblCdna)
        {
          // TODO: Add a DbRef back to the parent EMBL sequence with the exon
          // map
          // if given a dataset reference, search dataset for parent EMBL
          // sequence if it exists and set its map
          // make a new feature annotating the coding contig
        }
        else
        {
          // final product length truncation check
          int[] cdsRanges = adjustForProteinLength(translationLength, exons);
          dnaToProteinMapping = new Mapping(product, cdsRanges, new int[] {
              1, translationLength }, 3, 1);
          if (product != null)
          {
            /*
             * make xref with mapping from protein to EMBL dna
             */
            DBRefEntry proteinToEmblRef = new DBRefEntry(DBRefSource.EMBL,
                    getSequenceVersion(), proteinId, new Mapping(
                            dnaToProteinMapping.getMap().getInverse()));
            product.addDBRef(proteinToEmblRef);

            /*
             * make xref from protein to EMBLCDS; we assume here that the 
             * CDS sequence version is same as dna sequence (?!)
             */
            MapList proteinToCdsMapList = new MapList(new int[] { 1,
                translationLength }, new int[] { 1 + (codonStart - 1),
                (codonStart - 1) + 3 * translationLength }, 1, 3);
            DBRefEntry proteinToEmblCdsRef = new DBRefEntry(
                    DBRefSource.EMBLCDS, getSequenceVersion(), proteinId,
                    new Mapping(proteinToCdsMapList));
            product.addDBRef(proteinToEmblCdsRef);

            /*
             * make 'direct' xref from protein to EMBLCDSPROTEIN
             */
            proteinToEmblProteinRef = new DBRefEntry(proteinToEmblCdsRef);
            proteinToEmblProteinRef.setSource(DBRefSource.EMBLCDSProduct);
            proteinToEmblProteinRef.setMap(null);
            product.addDBRef(proteinToEmblProteinRef);
          }
        }
      }

      /*
       * add cds features to dna sequence
       */
      for (int xint = 0; exons != null && xint < exons.length; xint += 2)
      {
        SequenceFeature sf = makeCdsFeature(exons, xint, proteinName,
                proteinId, vals, codonStart);
        sf.setType(feature.getName()); // "CDS"
        sf.setEnaLocation(feature.getLocation());
        sf.setFeatureGroup(sourceDb);
        dna.addSequenceFeature(sf);
      }
    }

    /*
     * add feature dbRefs to sequence, and mappings for Uniprot xrefs
     */
    boolean hasUniprotDbref = false;
    if (feature.dbRefs != null)
    {
      boolean mappingUsed = false;
      for (DBRefEntry ref : feature.dbRefs)
      {
        /*
         * ensure UniProtKB/Swiss-Prot converted to UNIPROT
         */
        String source = DBRefUtils.getCanonicalName(ref.getSource());
        ref.setSource(source);
        DBRefEntry proteinDbRef = new DBRefEntry(ref.getSource(),
                ref.getVersion(), ref.getAccessionId());
        if (source.equals(DBRefSource.UNIPROT))
        {
          String proteinSeqName = DBRefSource.UNIPROT + "|"
                  + ref.getAccessionId();
          if (dnaToProteinMapping != null
                  && dnaToProteinMapping.getTo() != null)
          {
            if (mappingUsed)
            {
              /*
               * two or more Uniprot xrefs for the same CDS - 
               * each needs a distinct Mapping (as to a different sequence)
               */
              dnaToProteinMapping = new Mapping(dnaToProteinMapping);
            }
            mappingUsed = true;

            /*
             * try to locate the protein mapped to (possibly by a 
             * previous CDS feature); if not found, construct it from
             * the EMBL translation
             */
            SequenceI proteinSeq = matcher.findIdMatch(proteinSeqName);
            if (proteinSeq == null)
            {
              proteinSeq = new Sequence(proteinSeqName,
                      product.getSequenceAsString());
              matcher.add(proteinSeq);
              peptides.add(proteinSeq);
            }
            dnaToProteinMapping.setTo(proteinSeq);
            dnaToProteinMapping.setMappedFromId(proteinId);
            proteinSeq.addDBRef(proteinDbRef);
            ref.setMap(dnaToProteinMapping);
          }
          hasUniprotDbref = true;
        }
        if (product != null)
        {
          /*
           * copy feature dbref to our protein product
           */
          DBRefEntry pref = proteinDbRef;
          pref.setMap(null); // reference is direct
          product.addDBRef(pref);
          // Add converse mapping reference
          if (dnaToProteinMapping != null)
          {
            Mapping pmap = new Mapping(dna, dnaToProteinMapping.getMap()
                    .getInverse());
            pref = new DBRefEntry(sourceDb, getSequenceVersion(),
                    this.getAccession());
            pref.setMap(pmap);
            if (dnaToProteinMapping.getTo() != null)
            {
              dnaToProteinMapping.getTo().addDBRef(pref);
            }
          }
        }
        dna.addDBRef(ref);
      }
    }

    /*
     * if we have a product (translation) but no explicit Uniprot dbref
     * (example: EMBL AAFI02000057 protein_id EAL65544.1)
     * then construct mappings to an assumed EMBLCDSPROTEIN accession
     */
    if (!hasUniprotDbref && product != null)
    {
      if (proteinToEmblProteinRef == null)
      {
        // assuming CDSPROTEIN sequence version = dna version (?!)
        proteinToEmblProteinRef = new DBRefEntry(
                DBRefSource.EMBLCDSProduct, getSequenceVersion(), proteinId);
      }
      product.addDBRef(proteinToEmblProteinRef);

      if (dnaToProteinMapping != null
              && dnaToProteinMapping.getTo() != null)
      {
        DBRefEntry dnaToEmblProteinRef = new DBRefEntry(
                DBRefSource.EMBLCDSProduct, getSequenceVersion(), proteinId);
        dnaToEmblProteinRef.setMap(dnaToProteinMapping);
        dnaToProteinMapping.setMappedFromId(proteinId);
        dna.addDBRef(dnaToEmblProteinRef);
      }
    }
  }

  /**
   * Helper method to construct a SequenceFeature for one cds range
   * 
   * @param exons
   *          array of cds [start, end, ...] positions
   * @param exonStartIndex
   *          offset into the exons array
   * @param proteinName
   * @param proteinAccessionId
   * @param vals
   *          map of 'miscellaneous values' for feature
   * @param codonStart
   *          codon start position for CDS (1/2/3, normally 1)
   * @return
   */
  protected SequenceFeature makeCdsFeature(int[] exons, int exonStartIndex,
          String proteinName, String proteinAccessionId,
          Map<String, String> vals, int codonStart)
  {
    int exonNumber = exonStartIndex / 2 + 1;
    SequenceFeature sf = new SequenceFeature();
    sf.setBegin(Math.min(exons[exonStartIndex], exons[exonStartIndex + 1]));
    sf.setEnd(Math.max(exons[exonStartIndex], exons[exonStartIndex + 1]));
    sf.setDescription(String.format("Exon %d for protein '%s' EMBLCDS:%s",
            exonNumber, proteinName, proteinAccessionId));
    sf.setPhase(String.valueOf(codonStart - 1));
    sf.setStrand(exons[exonStartIndex] <= exons[exonStartIndex + 1] ? "+"
            : "-");
    sf.setValue(FeatureProperties.EXONPOS, exonNumber);
    sf.setValue(FeatureProperties.EXONPRODUCT, proteinName);
    if (!vals.isEmpty())
    {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Entry<String, String> val : vals.entrySet())
      {
        if (!first)
        {
          sb.append(";");
        }
        sb.append(val.getKey()).append("=").append(val.getValue());
        first = false;
        sf.setValue(val.getKey(), val.getValue());
      }
      sf.setAttributes(sb.toString());
    }
    return sf;
  }

  /**
   * Returns the CDS positions as a single array of [start, end, start, end...]
   * positions. If on the reverse strand, these will be in descending order.
   * 
   * @param feature
   * @return
   */
  protected int[] getCdsRanges(EmblFeature feature)
  {
    if (feature.location == null)
    {
      return new int[] {};
    }

    try
    {
      List<int[]> ranges = DnaUtils.parseLocation(feature.location);
      return listToArray(ranges);
    } catch (ParseException e)
    {
      Cache.log.warn(String.format(
              "Not parsing inexact CDS location %s in ENA %s",
              feature.location, this.accession));
      return new int[] {};
    }
  }

  /**
   * Converts a list of [start, end] ranges to a single array of [start, end,
   * start, end ...]
   * 
   * @param ranges
   * @return
   */
  int[] listToArray(List<int[]> ranges)
  {
    int[] result = new int[ranges.size() * 2];
    int i = 0;
    for (int[] range : ranges)
    {
      result[i++] = range[0];
      result[i++] = range[1];
    }
    return result;
  }

  /**
   * Truncates (if necessary) the exon intervals to match 3 times the length of
   * the protein; also accepts 3 bases longer (for stop codon not included in
   * protein)
   * 
   * @param proteinLength
   * @param exon
   *          an array of [start, end, start, end...] intervals
   * @return the same array (if unchanged) or a truncated copy
   */
  static int[] adjustForProteinLength(int proteinLength, int[] exon)
  {
    if (proteinLength <= 0 || exon == null)
    {
      return exon;
    }
    int expectedCdsLength = proteinLength * 3;
    int exonLength = MappingUtils.getLength(Arrays.asList(exon));

    /*
     * if exon length matches protein, or is shorter, or longer by the 
     * length of a stop codon (3 bases), then leave it unchanged
     */
    if (expectedCdsLength >= exonLength
            || expectedCdsLength == exonLength - 3)
    {
      return exon;
    }

    int origxon[];
    int sxpos = -1;
    int endxon = 0;
    origxon = new int[exon.length];
    System.arraycopy(exon, 0, origxon, 0, exon.length);
    int cdspos = 0;
    for (int x = 0; x < exon.length; x += 2)
    {
      cdspos += Math.abs(exon[x + 1] - exon[x]) + 1;
      if (expectedCdsLength <= cdspos)
      {
        // advanced beyond last codon.
        sxpos = x;
        if (expectedCdsLength != cdspos)
        {
          // System.err
          // .println("Truncating final exon interval on region by "
          // + (cdspos - cdslength));
        }

        /*
         * shrink the final exon - reduce end position if forward
         * strand, increase it if reverse
         */
        if (exon[x + 1] >= exon[x])
        {
          endxon = exon[x + 1] - cdspos + expectedCdsLength;
        }
        else
        {
          endxon = exon[x + 1] + cdspos - expectedCdsLength;
        }
        break;
      }
    }

    if (sxpos != -1)
    {
      // and trim the exon interval set if necessary
      int[] nxon = new int[sxpos + 2];
      System.arraycopy(exon, 0, nxon, 0, sxpos + 2);
      nxon[sxpos + 1] = endxon; // update the end boundary for the new exon
                                // set
      exon = nxon;
    }
    return exon;
  }

  public String getSequenceVersion()
  {
    return sequenceVersion;
  }

  public void setSequenceVersion(String sequenceVersion)
  {
    this.sequenceVersion = sequenceVersion;
  }

  public String getSequenceLength()
  {
    return sequenceLength;
  }

  public void setSequenceLength(String sequenceLength)
  {
    this.sequenceLength = sequenceLength;
  }

  public String getEntryVersion()
  {
    return entryVersion;
  }

  public void setEntryVersion(String entryVersion)
  {
    this.entryVersion = entryVersion;
  }

  public String getMoleculeType()
  {
    return moleculeType;
  }

  public void setMoleculeType(String moleculeType)
  {
    this.moleculeType = moleculeType;
  }

  public String getTopology()
  {
    return topology;
  }

  public void setTopology(String topology)
  {
    this.topology = topology;
  }

  public String getTaxonomicDivision()
  {
    return taxonomicDivision;
  }

  public void setTaxonomicDivision(String taxonomicDivision)
  {
    this.taxonomicDivision = taxonomicDivision;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getFirstPublicDate()
  {
    return firstPublicDate;
  }

  public void setFirstPublicDate(String firstPublicDate)
  {
    this.firstPublicDate = firstPublicDate;
  }

  public String getFirstPublicRelease()
  {
    return firstPublicRelease;
  }

  public void setFirstPublicRelease(String firstPublicRelease)
  {
    this.firstPublicRelease = firstPublicRelease;
  }

  public String getLastUpdatedDate()
  {
    return lastUpdatedDate;
  }

  public void setLastUpdatedDate(String lastUpdatedDate)
  {
    this.lastUpdatedDate = lastUpdatedDate;
  }

  public String getLastUpdatedRelease()
  {
    return lastUpdatedRelease;
  }

  public void setLastUpdatedRelease(String lastUpdatedRelease)
  {
    this.lastUpdatedRelease = lastUpdatedRelease;
  }

  public String getDataClass()
  {
    return dataClass;
  }

  public void setDataClass(String dataClass)
  {
    this.dataClass = dataClass;
  }
}
