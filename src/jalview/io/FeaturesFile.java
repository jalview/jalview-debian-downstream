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
package jalview.io;

import jalview.analysis.AlignmentUtils;
import jalview.analysis.SequenceIdMatcher;
import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.api.FeaturesSourceI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.gff.GffHelperBase;
import jalview.io.gff.GffHelperFactory;
import jalview.io.gff.GffHelperI;
import jalview.schemes.FeatureColour;
import jalview.schemes.UserColourScheme;
import jalview.util.MapList;
import jalview.util.ParseHtmlBodyAndLinks;
import jalview.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parses and writes features files, which may be in Jalview, GFF2 or GFF3
 * format. These are tab-delimited formats but with differences in the use of
 * columns.
 * 
 * A Jalview feature file may define feature colours and then declare that the
 * remainder of the file is in GFF format with the line 'GFF'.
 * 
 * GFF3 files may include alignment mappings for features, which Jalview will
 * attempt to model, and may include sequence data following a ##FASTA line.
 * 
 * 
 * @author AMW
 * @author jbprocter
 * @author gmcarstairs
 */
public class FeaturesFile extends AlignFile implements FeaturesSourceI
{
  private static final String ID_NOT_SPECIFIED = "ID_NOT_SPECIFIED";

  private static final String NOTE = "Note";

  protected static final String TAB = "\t";

  protected static final String GFF_VERSION = "##gff-version";

  private AlignmentI lastmatchedAl = null;

  private SequenceIdMatcher matcher = null;

  protected AlignmentI dataset;

  protected int gffVersion;

  /**
   * Creates a new FeaturesFile object.
   */
  public FeaturesFile()
  {
  }

  /**
   * Constructor which does not parse the file immediately
   * 
   * @param inFile
   * @param type
   * @throws IOException
   */
  public FeaturesFile(String inFile, String type) throws IOException
  {
    super(false, inFile, type);
  }

  /**
   * @param source
   * @throws IOException
   */
  public FeaturesFile(FileParse source) throws IOException
  {
    super(source);
  }

  /**
   * Constructor that optionally parses the file immediately
   * 
   * @param parseImmediately
   * @param inFile
   * @param type
   * @throws IOException
   */
  public FeaturesFile(boolean parseImmediately, String inFile, String type)
          throws IOException
  {
    super(parseImmediately, inFile, type);
  }

  /**
   * Parse GFF or sequence features file using case-independent matching,
   * discarding URLs
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - hashtable to store feature colour definitions
   * @param removeHTML
   *          - process html strings into plain text
   * @return true if features were added
   */
  public boolean parse(AlignmentI align,
          Map<String, FeatureColourI> colours, boolean removeHTML)
  {
    return parse(align, colours, removeHTML, false);
  }

  /**
   * Extends the default addProperties by also adding peptide-to-cDNA mappings
   * (if any) derived while parsing a GFF file
   */
  @Override
  public void addProperties(AlignmentI al)
  {
    super.addProperties(al);
    if (dataset != null && dataset.getCodonFrames() != null)
    {
      AlignmentI ds = (al.getDataset() == null) ? al : al.getDataset();
      for (AlignedCodonFrame codons : dataset.getCodonFrames())
      {
        ds.addCodonFrame(codons);
      }
    }
  }

  /**
   * Parse GFF or Jalview format sequence features file
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - hashtable to store feature colour definitions
   * @param removeHTML
   *          - process html strings into plain text
   * @param relaxedIdmatching
   *          - when true, ID matches to compound sequence IDs are allowed
   * @return true if features were added
   */
  public boolean parse(AlignmentI align,
          Map<String, FeatureColourI> colours, boolean removeHTML,
          boolean relaxedIdmatching)
  {
    Map<String, String> gffProps = new HashMap<String, String>();
    /*
     * keep track of any sequences we try to create from the data
     */
    List<SequenceI> newseqs = new ArrayList<SequenceI>();

    String line = null;
    try
    {
      String[] gffColumns;
      String featureGroup = null;

      while ((line = nextLine()) != null)
      {
        // skip comments/process pragmas
        if (line.length() == 0 || line.startsWith("#"))
        {
          if (line.toLowerCase().startsWith("##"))
          {
            processGffPragma(line, gffProps, align, newseqs);
          }
          continue;
        }

        gffColumns = line.split("\\t"); // tab as regex
        if (gffColumns.length == 1)
        {
          if (line.trim().equalsIgnoreCase("GFF"))
          {
            /*
             * Jalview features file with appended GFF
             * assume GFF2 (though it may declare ##gff-version 3)
             */
            gffVersion = 2;
            continue;
          }
        }

        if (gffColumns.length > 1 && gffColumns.length < 4)
        {
          /*
           * if 2 or 3 tokens, we anticipate either 'startgroup', 'endgroup' or
           * a feature type colour specification
           */
          String ft = gffColumns[0];
          if (ft.equalsIgnoreCase("startgroup"))
          {
            featureGroup = gffColumns[1];
          }
          else if (ft.equalsIgnoreCase("endgroup"))
          {
            // We should check whether this is the current group,
            // but at present there's no way of showing more than 1 group
            featureGroup = null;
          }
          else
          {
            String colscheme = gffColumns[1];
            FeatureColourI colour = FeatureColour
                    .parseJalviewFeatureColour(colscheme);
            if (colour != null)
            {
              colours.put(ft, colour);
            }
          }
          continue;
        }

        /*
         * if not a comment, GFF pragma, startgroup, endgroup or feature
         * colour specification, that just leaves a feature details line
         * in either Jalview or GFF format
         */
        if (gffVersion == 0)
        {
          parseJalviewFeature(line, gffColumns, align, colours, removeHTML,
                  relaxedIdmatching, featureGroup);
        }
        else
        {
          parseGff(gffColumns, align, relaxedIdmatching, newseqs);
        }
      }
      resetMatcher();
    } catch (Exception ex)
    {
      // should report somewhere useful for UI if necessary
      warningMessage = ((warningMessage == null) ? "" : warningMessage)
              + "Parsing error at\n" + line;
      System.out.println("Error parsing feature file: " + ex + "\n" + line);
      ex.printStackTrace(System.err);
      resetMatcher();
      return false;
    }

    /*
     * experimental - add any dummy sequences with features to the alignment
     * - we need them for Ensembl feature extraction - though maybe not otherwise
     */
    for (SequenceI newseq : newseqs)
    {
      if (newseq.getSequenceFeatures() != null)
      {
        align.addSequence(newseq);
      }
    }
    return true;
  }

  /**
   * Try to parse a Jalview format feature specification and add it as a
   * sequence feature to any matching sequences in the alignment. Returns true
   * if successful (a feature was added), or false if not.
   * 
   * @param line
   * @param gffColumns
   * @param alignment
   * @param featureColours
   * @param removeHTML
   * @param relaxedIdmatching
   * @param featureGroup
   */
  protected boolean parseJalviewFeature(String line, String[] gffColumns,
          AlignmentI alignment, Map<String, FeatureColourI> featureColours,
          boolean removeHTML, boolean relaxedIdMatching, String featureGroup)
  {
    /*
     * tokens: description seqid seqIndex start end type [score]
     */
    if (gffColumns.length < 6)
    {
      System.err.println("Ignoring feature line '" + line
              + "' with too few columns (" + gffColumns.length + ")");
      return false;
    }
    String desc = gffColumns[0];
    String seqId = gffColumns[1];
    SequenceI seq = findSequence(seqId, alignment, null, relaxedIdMatching);

    if (!ID_NOT_SPECIFIED.equals(seqId))
    {
      seq = findSequence(seqId, alignment, null, relaxedIdMatching);
    }
    else
    {
      seqId = null;
      seq = null;
      String seqIndex = gffColumns[2];
      try
      {
        int idx = Integer.parseInt(seqIndex);
        seq = alignment.getSequenceAt(idx);
      } catch (NumberFormatException ex)
      {
        System.err.println("Invalid sequence index: " + seqIndex);
      }
    }

    if (seq == null)
    {
      System.out.println("Sequence not found: " + line);
      return false;
    }

    int startPos = Integer.parseInt(gffColumns[3]);
    int endPos = Integer.parseInt(gffColumns[4]);

    String ft = gffColumns[5];

    if (!featureColours.containsKey(ft))
    {
      /* 
       * Perhaps an old style groups file with no colours -
       * synthesize a colour from the feature type
       */
      UserColourScheme ucs = new UserColourScheme(ft);
      featureColours.put(ft, new FeatureColour(ucs.findColour('A')));
    }
    SequenceFeature sf = new SequenceFeature(ft, desc, "", startPos,
            endPos, featureGroup);
    if (gffColumns.length > 6)
    {
      float score = Float.NaN;
      try
      {
        score = new Float(gffColumns[6]).floatValue();
        // update colourgradient bounds if allowed to
      } catch (NumberFormatException ex)
      {
        // leave as NaN
      }
      sf.setScore(score);
    }

    parseDescriptionHTML(sf, removeHTML);

    seq.addSequenceFeature(sf);

    while (seqId != null
            && (seq = alignment.findName(seq, seqId, false)) != null)
    {
      seq.addSequenceFeature(new SequenceFeature(sf));
    }
    return true;
  }

  /**
   * clear any temporary handles used to speed up ID matching
   */
  protected void resetMatcher()
  {
    lastmatchedAl = null;
    matcher = null;
  }

  /**
   * Returns a sequence matching the given id, as follows
   * <ul>
   * <li>strict matching is on exact sequence name</li>
   * <li>relaxed matching allows matching on a token within the sequence name,
   * or a dbxref</li>
   * <li>first tries to find a match in the alignment sequences</li>
   * <li>else tries to find a match in the new sequences already generated while
   * parsing the features file</li>
   * <li>else creates a new placeholder sequence, adds it to the new sequences
   * list, and returns it</li>
   * </ul>
   * 
   * @param seqId
   * @param align
   * @param newseqs
   * @param relaxedIdMatching
   * 
   * @return
   */
  protected SequenceI findSequence(String seqId, AlignmentI align,
          List<SequenceI> newseqs, boolean relaxedIdMatching)
  {
    // TODO encapsulate in SequenceIdMatcher, share the matcher
    // with the GffHelper (removing code duplication)
    SequenceI match = null;
    if (relaxedIdMatching)
    {
      if (lastmatchedAl != align)
      {
        lastmatchedAl = align;
        matcher = new SequenceIdMatcher(align.getSequencesArray());
        if (newseqs != null)
        {
          matcher.addAll(newseqs);
        }
      }
      match = matcher.findIdMatch(seqId);
    }
    else
    {
      match = align.findName(seqId, true);
      if (match == null && newseqs != null)
      {
        for (SequenceI m : newseqs)
        {
          if (seqId.equals(m.getName()))
          {
            return m;
          }
        }
      }

    }
    if (match == null && newseqs != null)
    {
      match = new SequenceDummy(seqId);
      if (relaxedIdMatching)
      {
        matcher.addAll(Arrays.asList(new SequenceI[] { match }));
      }
      // add dummy sequence to the newseqs list
      newseqs.add(match);
    }
    return match;
  }

  public void parseDescriptionHTML(SequenceFeature sf, boolean removeHTML)
  {
    if (sf.getDescription() == null)
    {
      return;
    }
    ParseHtmlBodyAndLinks parsed = new ParseHtmlBodyAndLinks(
            sf.getDescription(), removeHTML, newline);

    sf.description = (removeHTML) ? parsed.getNonHtmlContent()
            : sf.description;
    for (String link : parsed.getLinks())
    {
      sf.addLink(link);
    }

  }

  /**
   * generate a features file for seqs includes non-pos features by default.
   * 
   * @param sequences
   *          source of sequence features
   * @param visible
   *          hash of feature types and colours
   * @return features file contents
   */
  public String printJalviewFormat(SequenceI[] sequences,
          Map<String, FeatureColourI> visible)
  {
    return printJalviewFormat(sequences, visible, true, true);
  }

  /**
   * generate a features file for seqs with colours from visible (if any)
   * 
   * @param sequences
   *          source of features
   * @param visible
   *          hash of Colours for each feature type
   * @param visOnly
   *          when true only feature types in 'visible' will be output
   * @param nonpos
   *          indicates if non-positional features should be output (regardless
   *          of group or type)
   * @return features file contents
   */
  public String printJalviewFormat(SequenceI[] sequences,
          Map<String, FeatureColourI> visible, boolean visOnly,
          boolean nonpos)
  {
    StringBuilder out = new StringBuilder(256);
    boolean featuresGen = false;
    if (visOnly && !nonpos && (visible == null || visible.size() < 1))
    {
      // no point continuing.
      return "No Features Visible";
    }

    if (visible != null && visOnly)
    {
      // write feature colours only if we're given them and we are generating
      // viewed features
      // TODO: decide if feature links should also be written here ?
      Iterator<String> en = visible.keySet().iterator();
      while (en.hasNext())
      {
        String featureType = en.next().toString();
        FeatureColourI colour = visible.get(featureType);
        out.append(colour.toJalviewFormat(featureType)).append(newline);
      }
    }

    // Work out which groups are both present and visible
    List<String> groups = new ArrayList<String>();
    int groupIndex = 0;
    boolean isnonpos = false;

    SequenceFeature[] features;
    for (int i = 0; i < sequences.length; i++)
    {
      features = sequences[i].getSequenceFeatures();
      if (features != null)
      {
        for (int j = 0; j < features.length; j++)
        {
          isnonpos = features[j].begin == 0 && features[j].end == 0;
          if ((!nonpos && isnonpos)
                  || (!isnonpos && visOnly && !visible
                          .containsKey(features[j].type)))
          {
            continue;
          }

          if (features[j].featureGroup != null
                  && !groups.contains(features[j].featureGroup))
          {
            groups.add(features[j].featureGroup);
          }
        }
      }
    }

    String group = null;
    do
    {
      if (groups.size() > 0 && groupIndex < groups.size())
      {
        group = groups.get(groupIndex);
        out.append(newline);
        out.append("STARTGROUP").append(TAB);
        out.append(group);
        out.append(newline);
      }
      else
      {
        group = null;
      }

      for (int i = 0; i < sequences.length; i++)
      {
        features = sequences[i].getSequenceFeatures();
        if (features != null)
        {
          for (SequenceFeature sequenceFeature : features)
          {
            isnonpos = sequenceFeature.begin == 0
                    && sequenceFeature.end == 0;
            if ((!nonpos && isnonpos)
                    || (!isnonpos && visOnly && !visible
                            .containsKey(sequenceFeature.type)))
            {
              // skip if feature is nonpos and we ignore them or if we only
              // output visible and it isn't non-pos and it's not visible
              continue;
            }

            if (group != null
                    && (sequenceFeature.featureGroup == null || !sequenceFeature.featureGroup
                            .equals(group)))
            {
              continue;
            }

            if (group == null && sequenceFeature.featureGroup != null)
            {
              continue;
            }
            // we have features to output
            featuresGen = true;
            if (sequenceFeature.description == null
                    || sequenceFeature.description.equals(""))
            {
              out.append(sequenceFeature.type).append(TAB);
            }
            else
            {
              if (sequenceFeature.links != null
                      && sequenceFeature.getDescription().indexOf("<html>") == -1)
              {
                out.append("<html>");
              }

              out.append(sequenceFeature.description);
              if (sequenceFeature.links != null)
              {
                for (int l = 0; l < sequenceFeature.links.size(); l++)
                {
                  String label = sequenceFeature.links.elementAt(l);
                  String href = label.substring(label.indexOf("|") + 1);
                  label = label.substring(0, label.indexOf("|"));

                  if (sequenceFeature.description.indexOf(href) == -1)
                  {
                    out.append(" <a href=\"" + href + "\">" + label
                            + "</a>");
                  }
                }

                if (sequenceFeature.getDescription().indexOf("</html>") == -1)
                {
                  out.append("</html>");
                }
              }

              out.append(TAB);
            }
            out.append(sequences[i].getName());
            out.append("\t-1\t");
            out.append(sequenceFeature.begin);
            out.append(TAB);
            out.append(sequenceFeature.end);
            out.append(TAB);
            out.append(sequenceFeature.type);
            if (!Float.isNaN(sequenceFeature.score))
            {
              out.append(TAB);
              out.append(sequenceFeature.score);
            }
            out.append(newline);
          }
        }
      }

      if (group != null)
      {
        out.append("ENDGROUP").append(TAB);
        out.append(group);
        out.append(newline);
        groupIndex++;
      }
      else
      {
        break;
      }

    } while (groupIndex < groups.size() + 1);

    if (!featuresGen)
    {
      return "No Features Visible";
    }

    return out.toString();
  }

  /**
   * Parse method that is called when a GFF file is dragged to the desktop
   */
  @Override
  public void parse()
  {
    AlignViewportI av = getViewport();
    if (av != null)
    {
      if (av.getAlignment() != null)
      {
        dataset = av.getAlignment().getDataset();
      }
      if (dataset == null)
      {
        // working in the applet context ?
        dataset = av.getAlignment();
      }
    }
    else
    {
      dataset = new Alignment(new SequenceI[] {});
    }

    boolean parseResult = parse(dataset, null, false, true);
    if (!parseResult)
    {
      // pass error up somehow
    }
    if (av != null)
    {
      // update viewport with the dataset data ?
    }
    else
    {
      setSeqs(dataset.getSequencesArray());
    }
  }

  /**
   * Implementation of unused abstract method
   * 
   * @return error message
   */
  @Override
  public String print()
  {
    return "Use printGffFormat() or printJalviewFormat()";
  }

  /**
   * Returns features output in GFF2 format, including hidden and non-positional
   * features
   * 
   * @param sequences
   *          the sequences whose features are to be output
   * @param visible
   *          a map whose keys are the type names of visible features
   * @return
   */
  public String printGffFormat(SequenceI[] sequences,
          Map<String, FeatureColourI> visible)
  {
    return printGffFormat(sequences, visible, true, true);
  }

  /**
   * Returns features output in GFF2 format
   * 
   * @param sequences
   *          the sequences whose features are to be output
   * @param visible
   *          a map whose keys are the type names of visible features
   * @param outputVisibleOnly
   * @param includeNonPositionalFeatures
   * @return
   */
  public String printGffFormat(SequenceI[] sequences,
          Map<String, FeatureColourI> visible, boolean outputVisibleOnly,
          boolean includeNonPositionalFeatures)
  {
    StringBuilder out = new StringBuilder(256);
    int version = gffVersion == 0 ? 2 : gffVersion;
    out.append(String.format("%s %d\n", GFF_VERSION, version));
    String source;
    boolean isnonpos;
    for (SequenceI seq : sequences)
    {
      SequenceFeature[] features = seq.getSequenceFeatures();
      if (features != null)
      {
        for (SequenceFeature sf : features)
        {
          isnonpos = sf.begin == 0 && sf.end == 0;
          if (!includeNonPositionalFeatures && isnonpos)
          {
            /*
             * ignore non-positional features if not wanted
             */
            continue;
          }
          // TODO why the test !isnonpos here?
          // what about not visible non-positional features?
          if (!isnonpos && outputVisibleOnly
                  && !visible.containsKey(sf.type))
          {
            /*
             * ignore not visible features if not wanted
             */
            continue;
          }

          source = sf.featureGroup;
          if (source == null)
          {
            source = sf.getDescription();
          }

          out.append(seq.getName());
          out.append(TAB);
          out.append(source);
          out.append(TAB);
          out.append(sf.type);
          out.append(TAB);
          out.append(sf.begin);
          out.append(TAB);
          out.append(sf.end);
          out.append(TAB);
          out.append(sf.score);
          out.append(TAB);

          int strand = sf.getStrand();
          out.append(strand == 1 ? "+" : (strand == -1 ? "-" : "."));
          out.append(TAB);

          String phase = sf.getPhase();
          out.append(phase == null ? "." : phase);

          // miscellaneous key-values (GFF column 9)
          String attributes = sf.getAttributes();
          if (attributes != null)
          {
            out.append(TAB).append(attributes);
          }

          out.append(newline);
        }
      }
    }

    return out.toString();
  }

  /**
   * Returns a mapping given list of one or more Align descriptors (exonerate
   * format)
   * 
   * @param alignedRegions
   *          a list of "Align fromStart toStart fromCount"
   * @param mapIsFromCdna
   *          if true, 'from' is dna, else 'from' is protein
   * @param strand
   *          either 1 (forward) or -1 (reverse)
   * @return
   * @throws IOException
   */
  protected MapList constructCodonMappingFromAlign(
          List<String> alignedRegions, boolean mapIsFromCdna, int strand)
          throws IOException
  {
    if (strand == 0)
    {
      throw new IOException(
              "Invalid strand for a codon mapping (cannot be 0)");
    }
    int regions = alignedRegions.size();
    // arrays to hold [start, end] for each aligned region
    int[] fromRanges = new int[regions * 2]; // from dna
    int[] toRanges = new int[regions * 2]; // to protein
    int fromRangesIndex = 0;
    int toRangesIndex = 0;

    for (String range : alignedRegions)
    {
      /* 
       * Align mapFromStart mapToStart mapFromCount
       * e.g. if mapIsFromCdna
       *     Align 11270 143 120
       * means:
       *     120 bases from pos 11270 align to pos 143 in peptide
       * if !mapIsFromCdna this would instead be
       *     Align 143 11270 40 
       */
      String[] tokens = range.split(" ");
      if (tokens.length != 3)
      {
        throw new IOException("Wrong number of fields for Align");
      }
      int fromStart = 0;
      int toStart = 0;
      int fromCount = 0;
      try
      {
        fromStart = Integer.parseInt(tokens[0]);
        toStart = Integer.parseInt(tokens[1]);
        fromCount = Integer.parseInt(tokens[2]);
      } catch (NumberFormatException nfe)
      {
        throw new IOException("Invalid number in Align field: "
                + nfe.getMessage());
      }

      /*
       * Jalview always models from dna to protein, so adjust values if the
       * GFF mapping is from protein to dna
       */
      if (!mapIsFromCdna)
      {
        fromCount *= 3;
        int temp = fromStart;
        fromStart = toStart;
        toStart = temp;
      }
      fromRanges[fromRangesIndex++] = fromStart;
      fromRanges[fromRangesIndex++] = fromStart + strand * (fromCount - 1);

      /*
       * If a codon has an intron gap, there will be contiguous 'toRanges';
       * this is handled for us by the MapList constructor. 
       * (It is not clear that exonerate ever generates this case)  
       */
      toRanges[toRangesIndex++] = toStart;
      toRanges[toRangesIndex++] = toStart + (fromCount - 1) / 3;
    }

    return new MapList(fromRanges, toRanges, 3, 1);
  }

  /**
   * Parse a GFF format feature. This may include creating a 'dummy' sequence to
   * hold the feature, or for its mapped sequence, or both, to be resolved
   * either later in the GFF file (##FASTA section), or when the user loads
   * additional sequences.
   * 
   * @param gffColumns
   * @param alignment
   * @param relaxedIdMatching
   * @param newseqs
   * @return
   */
  protected SequenceI parseGff(String[] gffColumns, AlignmentI alignment,
          boolean relaxedIdMatching, List<SequenceI> newseqs)
  {
    /*
     * GFF: seqid source type start end score strand phase [attributes]
     */
    if (gffColumns.length < 5)
    {
      System.err.println("Ignoring GFF feature line with too few columns ("
              + gffColumns.length + ")");
      return null;
    }

    /*
     * locate referenced sequence in alignment _or_ 
     * as a forward or external reference (SequenceDummy)
     */
    String seqId = gffColumns[0];
    SequenceI seq = findSequence(seqId, alignment, newseqs,
            relaxedIdMatching);

    SequenceFeature sf = null;
    GffHelperI helper = GffHelperFactory.getHelper(gffColumns);
    if (helper != null)
    {
      try
      {
        sf = helper.processGff(seq, gffColumns, alignment, newseqs,
                relaxedIdMatching);
        if (sf != null)
        {
          seq.addSequenceFeature(sf);
          while ((seq = alignment.findName(seq, seqId, true)) != null)
          {
            seq.addSequenceFeature(new SequenceFeature(sf));
          }
        }
      } catch (IOException e)
      {
        System.err.println("GFF parsing failed with: " + e.getMessage());
        return null;
      }
    }

    return seq;
  }

  /**
   * Process the 'column 9' data of the GFF file. This is less formally defined,
   * and its interpretation will vary depending on the tool that has generated
   * it.
   * 
   * @param attributes
   * @param sf
   */
  protected void processGffColumnNine(String attributes, SequenceFeature sf)
  {
    sf.setAttributes(attributes);

    /*
     * Parse attributes in column 9 and add them to the sequence feature's 
     * 'otherData' table; use Note as a best proxy for description
     */
    char nameValueSeparator = gffVersion == 3 ? '=' : ' ';
    // TODO check we don't break GFF2 values which include commas here
    Map<String, List<String>> nameValues = GffHelperBase
            .parseNameValuePairs(attributes, ";", nameValueSeparator, ",");
    for (Entry<String, List<String>> attr : nameValues.entrySet())
    {
      String values = StringUtils.listToDelimitedString(attr.getValue(),
              "; ");
      sf.setValue(attr.getKey(), values);
      if (NOTE.equals(attr.getKey()))
      {
        sf.setDescription(values);
      }
    }
  }

  /**
   * After encountering ##fasta in a GFF3 file, process the remainder of the
   * file as FAST sequence data. Any placeholder sequences created during
   * feature parsing are updated with the actual sequences.
   * 
   * @param align
   * @param newseqs
   * @throws IOException
   */
  protected void processAsFasta(AlignmentI align, List<SequenceI> newseqs)
          throws IOException
  {
    try
    {
      mark();
    } catch (IOException q)
    {
    }
    FastaFile parser = new FastaFile(this);
    List<SequenceI> includedseqs = parser.getSeqs();

    SequenceIdMatcher smatcher = new SequenceIdMatcher(newseqs);

    /*
     * iterate over includedseqs, and replacing matching ones with newseqs
     * sequences. Generic iterator not used here because we modify
     * includedseqs as we go
     */
    for (int p = 0, pSize = includedseqs.size(); p < pSize; p++)
    {
      // search for any dummy seqs that this sequence can be used to update
      SequenceI includedSeq = includedseqs.get(p);
      SequenceI dummyseq = smatcher.findIdMatch(includedSeq);
      if (dummyseq != null && dummyseq instanceof SequenceDummy)
      {
        // probably have the pattern wrong
        // idea is that a flyweight proxy for a sequence ID can be created for
        // 1. stable reference creation
        // 2. addition of annotation
        // 3. future replacement by a real sequence
        // current pattern is to create SequenceDummy objects - a convenience
        // constructor for a Sequence.
        // problem is that when promoted to a real sequence, all references
        // need to be updated somehow. We avoid that by keeping the same object.
        ((SequenceDummy) dummyseq).become(includedSeq);
        dummyseq.createDatasetSequence();

        /*
         * Update mappings so they are now to the dataset sequence
         */
        for (AlignedCodonFrame mapping : align.getCodonFrames())
        {
          mapping.updateToDataset(dummyseq);
        }

        /*
         * replace parsed sequence with the realised forward reference
         */
        includedseqs.set(p, dummyseq);

        /*
         * and remove from the newseqs list
         */
        newseqs.remove(dummyseq);
      }
    }

    /*
     * finally add sequences to the dataset
     */
    for (SequenceI seq : includedseqs)
    {
      // experimental: mapping-based 'alignment' to query sequence
      AlignmentUtils.alignSequenceAs(seq, align,
              String.valueOf(align.getGapCharacter()), false, true);

      // rename sequences if GFF handler requested this
      // TODO a more elegant way e.g. gffHelper.postProcess(newseqs) ?
      SequenceFeature[] sfs = seq.getSequenceFeatures();
      if (sfs != null)
      {
        String newName = (String) sfs[0].getValue(GffHelperI.RENAME_TOKEN);
        if (newName != null)
        {
          seq.setName(newName);
        }
      }
      align.addSequence(seq);
    }
  }

  /**
   * Process a ## directive
   * 
   * @param line
   * @param gffProps
   * @param align
   * @param newseqs
   * @throws IOException
   */
  protected void processGffPragma(String line,
          Map<String, String> gffProps, AlignmentI align,
          List<SequenceI> newseqs) throws IOException
  {
    line = line.trim();
    if ("###".equals(line))
    {
      // close off any open 'forward references'
      return;
    }

    String[] tokens = line.substring(2).split(" ");
    String pragma = tokens[0];
    String value = tokens.length == 1 ? null : tokens[1];

    if ("gff-version".equalsIgnoreCase(pragma))
    {
      if (value != null)
      {
        try
        {
          // value may be e.g. "3.1.2"
          gffVersion = Integer.parseInt(value.split("\\.")[0]);
        } catch (NumberFormatException e)
        {
          // ignore
        }
      }
    }
    else if ("sequence-region".equalsIgnoreCase(pragma))
    {
      // could capture <seqid start end> if wanted here
    }
    else if ("feature-ontology".equalsIgnoreCase(pragma))
    {
      // should resolve against the specified feature ontology URI
    }
    else if ("attribute-ontology".equalsIgnoreCase(pragma))
    {
      // URI of attribute ontology - not currently used in GFF3
    }
    else if ("source-ontology".equalsIgnoreCase(pragma))
    {
      // URI of source ontology - not currently used in GFF3
    }
    else if ("species-build".equalsIgnoreCase(pragma))
    {
      // save URI of specific NCBI taxon version of annotations
      gffProps.put("species-build", value);
    }
    else if ("fasta".equalsIgnoreCase(pragma))
    {
      // process the rest of the file as a fasta file and replace any dummy
      // sequence IDs
      processAsFasta(align, newseqs);
    }
    else
    {
      System.err.println("Ignoring unknown pragma: " + line);
    }
  }
}
