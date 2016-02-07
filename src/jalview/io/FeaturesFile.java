/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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

import jalview.analysis.SequenceIdMatcher;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.GraduatedColor;
import jalview.schemes.UserColourScheme;
import jalview.util.Format;
import jalview.util.MapList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Parse and create Jalview Features files Detects GFF format features files and
 * parses. Does not implement standard print() - call specific printFeatures or
 * printGFF. Uses AlignmentI.findSequence(String id) to find the sequence object
 * for the features annotation - this normally works on an exact match.
 * 
 * @author AMW
 * @version $Revision$
 */
public class FeaturesFile extends AlignFile
{
  /**
   * work around for GFF interpretation bug where source string becomes
   * description rather than a group
   */
  private boolean doGffSource = true;

  private int gffversion;

  /**
   * Creates a new FeaturesFile object.
   */
  public FeaturesFile()
  {
  }

  /**
   * @param inFile
   * @param type
   * @throws IOException
   */
  public FeaturesFile(String inFile, String type) throws IOException
  {
    super(inFile, type);
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
   * @param parseImmediately
   * @param source
   * @throws IOException
   */
  public FeaturesFile(boolean parseImmediately, FileParse source)
          throws IOException
  {
    super(parseImmediately, source);
  }

  /**
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
  public boolean parse(AlignmentI align, Hashtable colours,
          boolean removeHTML)
  {
    return parse(align, colours, null, removeHTML, false);
  }

  /**
   * Parse GFF or sequence features file optionally using case-independent
   * matching, discarding URLs
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
  public boolean parse(AlignmentI align, Map colours, boolean removeHTML,
          boolean relaxedIdMatching)
  {
    return parse(align, colours, null, removeHTML, relaxedIdMatching);
  }

  /**
   * Parse GFF or sequence features file optionally using case-independent
   * matching
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - hashtable to store feature colour definitions
   * @param featureLink
   *          - hashtable to store associated URLs
   * @param removeHTML
   *          - process html strings into plain text
   * @return true if features were added
   */
  public boolean parse(AlignmentI align, Map colours, Map featureLink,
          boolean removeHTML)
  {
    return parse(align, colours, featureLink, removeHTML, false);
  }

  @Override
  public void addAnnotations(AlignmentI al)
  {
    // TODO Auto-generated method stub
    super.addAnnotations(al);
  }

  @Override
  public void addProperties(AlignmentI al)
  {
    // TODO Auto-generated method stub
    super.addProperties(al);
  }

  @Override
  public void addSeqGroups(AlignmentI al)
  {
    // TODO Auto-generated method stub
    super.addSeqGroups(al);
  }

  /**
   * Parse GFF or sequence features file
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - hashtable to store feature colour definitions
   * @param featureLink
   *          - hashtable to store associated URLs
   * @param removeHTML
   *          - process html strings into plain text
   * @param relaxedIdmatching
   *          - when true, ID matches to compound sequence IDs are allowed
   * @return true if features were added
   */
  public boolean parse(AlignmentI align, Map colours, Map featureLink,
          boolean removeHTML, boolean relaxedIdmatching)
  {

    String line = null;
    try
    {
      SequenceI seq = null;
      /**
       * keep track of any sequences we try to create from the data if it is a
       * GFF3 file
       */
      ArrayList<SequenceI> newseqs = new ArrayList<SequenceI>();
      String type, desc, token = null;

      int index, start, end;
      float score;
      StringTokenizer st;
      SequenceFeature sf;
      String featureGroup = null, groupLink = null;
      Map typeLink = new Hashtable();
      /**
       * when true, assume GFF style features rather than Jalview style.
       */
      boolean GFFFile = true;
      Map<String, String> gffProps = new HashMap<String, String>();
      while ((line = nextLine()) != null)
      {
        // skip comments/process pragmas
        if (line.startsWith("#"))
        {
          if (line.startsWith("##"))
          {
            // possibly GFF2/3 version and metadata header
            processGffPragma(line, gffProps, align, newseqs);
            line = "";
          }
          continue;
        }

        st = new StringTokenizer(line, "\t");
        if (st.countTokens() == 1)
        {
          if (line.trim().equalsIgnoreCase("GFF"))
          {
            // Start parsing file as if it might be GFF again.
            GFFFile = true;
            continue;
          }
        }
        if (st.countTokens() > 1 && st.countTokens() < 4)
        {
          GFFFile = false;
          type = st.nextToken();
          if (type.equalsIgnoreCase("startgroup"))
          {
            featureGroup = st.nextToken();
            if (st.hasMoreElements())
            {
              groupLink = st.nextToken();
              featureLink.put(featureGroup, groupLink);
            }
          }
          else if (type.equalsIgnoreCase("endgroup"))
          {
            // We should check whether this is the current group,
            // but at present theres no way of showing more than 1 group
            st.nextToken();
            featureGroup = null;
            groupLink = null;
          }
          else
          {
            Object colour = null;
            String colscheme = st.nextToken();
            if (colscheme.indexOf("|") > -1
                    || colscheme.trim().equalsIgnoreCase("label"))
            {
              // Parse '|' separated graduated colourscheme fields:
              // [label|][mincolour|maxcolour|[absolute|]minvalue|maxvalue|thresholdtype|thresholdvalue]
              // can either provide 'label' only, first is optional, next two
              // colors are required (but may be
              // left blank), next is optional, nxt two min/max are required.
              // first is either 'label'
              // first/second and third are both hexadecimal or word equivalent
              // colour.
              // next two are values parsed as floats.
              // fifth is either 'above','below', or 'none'.
              // sixth is a float value and only required when fifth is either
              // 'above' or 'below'.
              StringTokenizer gcol = new StringTokenizer(colscheme, "|",
                      true);
              // set defaults
              int threshtype = AnnotationColourGradient.NO_THRESHOLD;
              float min = Float.MIN_VALUE, max = Float.MAX_VALUE, threshval = Float.NaN;
              boolean labelCol = false;
              // Parse spec line
              String mincol = gcol.nextToken();
              if (mincol == "|")
              {
                System.err
                        .println("Expected either 'label' or a colour specification in the line: "
                                + line);
                continue;
              }
              String maxcol = null;
              if (mincol.toLowerCase().indexOf("label") == 0)
              {
                labelCol = true;
                mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null); // skip
                                                                           // '|'
                mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
              }
              String abso = null, minval, maxval;
              if (mincol != null)
              {
                // at least four more tokens
                if (mincol.equals("|"))
                {
                  mincol = "";
                }
                else
                {
                  gcol.nextToken(); // skip next '|'
                }
                // continue parsing rest of line
                maxcol = gcol.nextToken();
                if (maxcol.equals("|"))
                {
                  maxcol = "";
                }
                else
                {
                  gcol.nextToken(); // skip next '|'
                }
                abso = gcol.nextToken();
                gcol.nextToken(); // skip next '|'
                if (abso.toLowerCase().indexOf("abso") != 0)
                {
                  minval = abso;
                  abso = null;
                }
                else
                {
                  minval = gcol.nextToken();
                  gcol.nextToken(); // skip next '|'
                }
                maxval = gcol.nextToken();
                if (gcol.hasMoreTokens())
                {
                  gcol.nextToken(); // skip next '|'
                }
                try
                {
                  if (minval.length() > 0)
                  {
                    min = new Float(minval).floatValue();
                  }
                } catch (Exception e)
                {
                  System.err
                          .println("Couldn't parse the minimum value for graduated colour for type ("
                                  + colscheme
                                  + ") - did you misspell 'auto' for the optional automatic colour switch ?");
                  e.printStackTrace();
                }
                try
                {
                  if (maxval.length() > 0)
                  {
                    max = new Float(maxval).floatValue();
                  }
                } catch (Exception e)
                {
                  System.err
                          .println("Couldn't parse the maximum value for graduated colour for type ("
                                  + colscheme + ")");
                  e.printStackTrace();
                }
              }
              else
              {
                // add in some dummy min/max colours for the label-only
                // colourscheme.
                mincol = "FFFFFF";
                maxcol = "000000";
              }
              try
              {
                colour = new jalview.schemes.GraduatedColor(
                        new UserColourScheme(mincol).findColour('A'),
                        new UserColourScheme(maxcol).findColour('A'), min,
                        max);
              } catch (Exception e)
              {
                System.err
                        .println("Couldn't parse the graduated colour scheme ("
                                + colscheme + ")");
                e.printStackTrace();
              }
              if (colour != null)
              {
                ((jalview.schemes.GraduatedColor) colour)
                        .setColourByLabel(labelCol);
                ((jalview.schemes.GraduatedColor) colour)
                        .setAutoScaled(abso == null);
                // add in any additional parameters
                String ttype = null, tval = null;
                if (gcol.hasMoreTokens())
                {
                  // threshold type and possibly a threshold value
                  ttype = gcol.nextToken();
                  if (ttype.toLowerCase().startsWith("below"))
                  {
                    ((jalview.schemes.GraduatedColor) colour)
                            .setThreshType(AnnotationColourGradient.BELOW_THRESHOLD);
                  }
                  else if (ttype.toLowerCase().startsWith("above"))
                  {
                    ((jalview.schemes.GraduatedColor) colour)
                            .setThreshType(AnnotationColourGradient.ABOVE_THRESHOLD);
                  }
                  else
                  {
                    ((jalview.schemes.GraduatedColor) colour)
                            .setThreshType(AnnotationColourGradient.NO_THRESHOLD);
                    if (!ttype.toLowerCase().startsWith("no"))
                    {
                      System.err
                              .println("Ignoring unrecognised threshold type : "
                                      + ttype);
                    }
                  }
                }
                if (((GraduatedColor) colour).getThreshType() != AnnotationColourGradient.NO_THRESHOLD)
                {
                  try
                  {
                    gcol.nextToken();
                    tval = gcol.nextToken();
                    ((jalview.schemes.GraduatedColor) colour)
                            .setThresh(new Float(tval).floatValue());
                  } catch (Exception e)
                  {
                    System.err
                            .println("Couldn't parse threshold value as a float: ("
                                    + tval + ")");
                    e.printStackTrace();
                  }
                }
                // parse the thresh-is-min token ?
                if (gcol.hasMoreTokens())
                {
                  System.err
                          .println("Ignoring additional tokens in parameters in graduated colour specification\n");
                  while (gcol.hasMoreTokens())
                  {
                    System.err.println("|" + gcol.nextToken());
                  }
                  System.err.println("\n");
                }
              }
            }
            else
            {
              UserColourScheme ucs = new UserColourScheme(colscheme);
              colour = ucs.findColour('A');
            }
            if (colour != null)
            {
              colours.put(type, colour);
            }
            if (st.hasMoreElements())
            {
              String link = st.nextToken();
              typeLink.put(type, link);
              if (featureLink == null)
              {
                featureLink = new Hashtable();
              }
              featureLink.put(type, link);
            }
          }
          continue;
        }
        String seqId = "";
        while (st.hasMoreElements())
        {

          if (GFFFile)
          {
            // Still possible this is an old Jalview file,
            // which does not have type colours at the beginning
            seqId = token = st.nextToken();
            seq = findName(align, seqId, relaxedIdmatching, newseqs);
            if (seq != null)
            {
              desc = st.nextToken();
              String group = null;
              if (doGffSource && desc.indexOf(' ') == -1)
              {
                // could also be a source term rather than description line
                group = new String(desc);
              }
              type = st.nextToken();
              try
              {
                String stt = st.nextToken();
                if (stt.length() == 0 || stt.equals("-"))
                {
                  start = 0;
                }
                else
                {
                  start = Integer.parseInt(stt);
                }
              } catch (NumberFormatException ex)
              {
                start = 0;
              }
              try
              {
                String stt = st.nextToken();
                if (stt.length() == 0 || stt.equals("-"))
                {
                  end = 0;
                }
                else
                {
                  end = Integer.parseInt(stt);
                }
              } catch (NumberFormatException ex)
              {
                end = 0;
              }
              // TODO: decide if non positional feature assertion for input data
              // where end==0 is generally valid
              if (end == 0)
              {
                // treat as non-positional feature, regardless.
                start = 0;
              }
              try
              {
                score = new Float(st.nextToken()).floatValue();
              } catch (NumberFormatException ex)
              {
                score = 0;
              }

              sf = new SequenceFeature(type, desc, start, end, score, group);

              try
              {
                sf.setValue("STRAND", st.nextToken());
                sf.setValue("FRAME", st.nextToken());
              } catch (Exception ex)
              {
              }

              if (st.hasMoreTokens())
              {
                StringBuffer attributes = new StringBuffer();
                boolean sep = false;
                while (st.hasMoreTokens())
                {
                  attributes.append((sep ? "\t" : "") + st.nextElement());
                  sep = true;
                }
                // TODO validate and split GFF2 attributes field ? parse out
                // ([A-Za-z][A-Za-z0-9_]*) <value> ; and add as
                // sf.setValue(attrib, val);
                sf.setValue("ATTRIBUTES", attributes.toString());
              }

              if (processOrAddSeqFeature(align, newseqs, seq, sf, GFFFile,
                      relaxedIdmatching))
              {
                // check whether we should add the sequence feature to any other
                // sequences in the alignment with the same or similar
                while ((seq = align.findName(seq, seqId, true)) != null)
                {
                  seq.addSequenceFeature(new SequenceFeature(sf));
                }
              }
              break;
            }
          }

          if (GFFFile && seq == null)
          {
            desc = token;
          }
          else
          {
            desc = st.nextToken();
          }
          if (!st.hasMoreTokens())
          {
            System.err
                    .println("DEBUG: Run out of tokens when trying to identify the destination for the feature.. giving up.");
            // in all probability, this isn't a file we understand, so bail
            // quietly.
            return false;
          }

          token = st.nextToken();

          if (!token.equals("ID_NOT_SPECIFIED"))
          {
            seq = findName(align, seqId = token, relaxedIdmatching, null);
            st.nextToken();
          }
          else
          {
            seqId = null;
            try
            {
              index = Integer.parseInt(st.nextToken());
              seq = align.getSequenceAt(index);
            } catch (NumberFormatException ex)
            {
              seq = null;
            }
          }

          if (seq == null)
          {
            System.out.println("Sequence not found: " + line);
            break;
          }

          start = Integer.parseInt(st.nextToken());
          end = Integer.parseInt(st.nextToken());

          type = st.nextToken();

          if (!colours.containsKey(type))
          {
            // Probably the old style groups file
            UserColourScheme ucs = new UserColourScheme(type);
            colours.put(type, ucs.findColour('A'));
          }
          sf = new SequenceFeature(type, desc, "", start, end, featureGroup);
          if (st.hasMoreTokens())
          {
            try
            {
              score = new Float(st.nextToken()).floatValue();
              // update colourgradient bounds if allowed to
            } catch (NumberFormatException ex)
            {
              score = 0;
            }
            sf.setScore(score);
          }
          if (groupLink != null && removeHTML)
          {
            sf.addLink(groupLink);
            sf.description += "%LINK%";
          }
          if (typeLink.containsKey(type) && removeHTML)
          {
            sf.addLink(typeLink.get(type).toString());
            sf.description += "%LINK%";
          }

          parseDescriptionHTML(sf, removeHTML);

          seq.addSequenceFeature(sf);

          while (seqId != null
                  && (seq = align.findName(seq, seqId, false)) != null)
          {
            seq.addSequenceFeature(new SequenceFeature(sf));
          }
          // If we got here, its not a GFFFile
          GFFFile = false;
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

    return true;
  }

  private enum GffPragmas
  {
    gff_version, sequence_region, feature_ontology, attribute_ontology, source_ontology, species_build, fasta, hash
  };

  private static Map<String, GffPragmas> GFFPRAGMA;
  static
  {
    GFFPRAGMA = new HashMap<String, GffPragmas>();
    GFFPRAGMA.put("sequence-region", GffPragmas.sequence_region);
    GFFPRAGMA.put("feature-ontology", GffPragmas.feature_ontology);
    GFFPRAGMA.put("#", GffPragmas.hash);
    GFFPRAGMA.put("fasta", GffPragmas.fasta);
    GFFPRAGMA.put("species-build", GffPragmas.species_build);
    GFFPRAGMA.put("source-ontology", GffPragmas.source_ontology);
    GFFPRAGMA.put("attribute-ontology", GffPragmas.attribute_ontology);
  }

  private void processGffPragma(String line, Map<String, String> gffProps,
          AlignmentI align, ArrayList<SequenceI> newseqs)
          throws IOException
  {
    // line starts with ##
    int spacepos = line.indexOf(' ');
    String pragma = spacepos == -1 ? line.substring(2).trim() : line
            .substring(2, spacepos);
    GffPragmas gffpragma = GFFPRAGMA.get(pragma.toLowerCase());
    if (gffpragma == null)
    {
      return;
    }
    switch (gffpragma)
    {
    case gff_version:
      try
      {
        gffversion = Integer.parseInt(line.substring(spacepos + 1));
      } finally
      {

      }
      break;
    case feature_ontology:
      // resolve against specific feature ontology
      break;
    case attribute_ontology:
      // resolve against specific attribute ontology
      break;
    case source_ontology:
      // resolve against specific source ontology
      break;
    case species_build:
      // resolve against specific NCBI taxon version
      break;
    case hash:
      // close off any open feature hierarchies
      break;
    case fasta:
      // process the rest of the file as a fasta file and replace any dummy
      // sequence IDs
      process_as_fasta(align, newseqs);
      break;
    default:
      // we do nothing ?
      System.err.println("Ignoring unknown pragma:\n" + line);
    }
  }

  private void process_as_fasta(AlignmentI align, List<SequenceI> newseqs)
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
    // iterate over includedseqs, and replacing matching ones with newseqs
    // sequences. Generic iterator not used here because we modify includedseqs
    // as we go
    for (int p = 0, pSize = includedseqs.size(); p < pSize; p++)
    {
      // search for any dummy seqs that this sequence can be used to update
      SequenceI dummyseq = smatcher.findIdMatch(includedseqs.get(p));
      if (dummyseq != null)
      {
        // dummyseq was created so it could be annotated and referred to in
        // alignments/codon mappings

        SequenceI mseq = includedseqs.get(p);
        // mseq is the 'template' imported from the FASTA file which we'll use
        // to coomplete dummyseq
        if (dummyseq instanceof SequenceDummy)
        {
          // probably have the pattern wrong
          // idea is that a flyweight proxy for a sequence ID can be created for
          // 1. stable reference creation
          // 2. addition of annotation
          // 3. future replacement by a real sequence
          // current pattern is to create SequenceDummy objects - a convenience
          // constructor for a Sequence.
          // problem is that when promoted to a real sequence, all references
          // need
          // to be updated somehow.
          ((SequenceDummy) dummyseq).become(mseq);
          includedseqs.set(p, dummyseq); // template is no longer needed
        }
      }
    }
    // finally add sequences to the dataset
    for (SequenceI seq : includedseqs)
    {
      align.addSequence(seq);
    }
  }

  /**
   * take a sequence feature and examine its attributes to decide how it should
   * be added to a sequence
   * 
   * @param seq
   *          - the destination sequence constructed or discovered in the
   *          current context
   * @param sf
   *          - the base feature with ATTRIBUTES property containing any
   *          additional attributes
   * @param gFFFile
   *          - true if we are processing a GFF annotation file
   * @return true if sf was actually added to the sequence, false if it was
   *         processed in another way
   */
  public boolean processOrAddSeqFeature(AlignmentI align,
          List<SequenceI> newseqs, SequenceI seq, SequenceFeature sf,
          boolean gFFFile, boolean relaxedIdMatching)
  {
    String attr = (String) sf.getValue("ATTRIBUTES");
    boolean add = true;
    if (gFFFile && attr != null)
    {
      int nattr = 8;

      for (String attset : attr.split("\t"))
      {
        if (attset == null || attset.trim().length() == 0)
        {
          continue;
        }
        nattr++;
        Map<String, List<String>> set = new HashMap<String, List<String>>();
        // normally, only expect one column - 9 - in this field
        // the attributes (Gff3) or groups (gff2) field
        for (String pair : attset.trim().split(";"))
        {
          pair = pair.trim();
          if (pair.length() == 0)
          {
            continue;
          }

          // expect either space seperated (gff2) or '=' separated (gff3)
          // key/value pairs here

          int eqpos = pair.indexOf('='), sppos = pair.indexOf(' ');
          String key = null, value = null;

          if (sppos > -1 && (eqpos == -1 || sppos < eqpos))
          {
            key = pair.substring(0, sppos);
            value = pair.substring(sppos + 1);
          }
          else
          {
            if (eqpos > -1 && (sppos == -1 || eqpos < sppos))
            {
              key = pair.substring(0, eqpos);
              value = pair.substring(eqpos + 1);
            }
            else
            {
              key = pair;
            }
          }
          if (key != null)
          {
            List<String> vals = set.get(key);
            if (vals == null)
            {
              vals = new ArrayList<String>();
              set.put(key, vals);
            }
            if (value != null)
            {
              vals.add(value.trim());
            }
          }
        }
        try
        {
          add &= processGffKey(set, nattr, seq, sf, align, newseqs,
                  relaxedIdMatching); // process decides if
                                      // feature is actually
                                      // added
        } catch (InvalidGFF3FieldException ivfe)
        {
          System.err.println(ivfe);
        }
      }
    }
    if (add)
    {
      seq.addSequenceFeature(sf);
    }
    return add;
  }

  public class InvalidGFF3FieldException extends Exception
  {
    String field, value;

    public InvalidGFF3FieldException(String field,
            Map<String, List<String>> set, String message)
    {
      super(message + " (Field was " + field + " and value was "
              + set.get(field).toString());
      this.field = field;
      this.value = set.get(field).toString();
    }

  }

  /**
   * take a set of keys for a feature and interpret them
   * 
   * @param set
   * @param nattr
   * @param seq
   * @param sf
   * @return
   */
  public boolean processGffKey(Map<String, List<String>> set, int nattr,
          SequenceI seq, SequenceFeature sf, AlignmentI align,
          List<SequenceI> newseqs, boolean relaxedIdMatching)
          throws InvalidGFF3FieldException
  {
    String attr;
    // decide how to interpret according to type
    if (sf.getType().equals("similarity"))
    {
      int strand = sf.getStrand();
      // exonerate cdna/protein map
      // look for fields
      List<SequenceI> querySeq = findNames(align, newseqs,
              relaxedIdMatching, set.get(attr = "Query"));
      if (querySeq == null || querySeq.size() != 1)
      {
        throw new InvalidGFF3FieldException(attr, set,
                "Expecting exactly one sequence in Query field (got "
                        + set.get(attr) + ")");
      }
      if (set.containsKey(attr = "Align"))
      {
        // process the align maps and create cdna/protein maps
        // ideally, the query sequences are in the alignment, but maybe not...

        AlignedCodonFrame alco = new AlignedCodonFrame();
        MapList codonmapping = constructCodonMappingFromAlign(set, attr,
                strand);

        // add codon mapping, and hope!
        alco.addMap(seq, querySeq.get(0), codonmapping);
        align.addCodonFrame(alco);
        // everything that's needed to be done is done
        // no features to create here !
        return false;
      }

    }
    return true;
  }

  private MapList constructCodonMappingFromAlign(
          Map<String, List<String>> set, String attr, int strand)
          throws InvalidGFF3FieldException
  {
    if (strand == 0)
    {
      throw new InvalidGFF3FieldException(attr, set,
              "Invalid strand for a codon mapping (cannot be 0)");
    }
    List<Integer> fromrange = new ArrayList<Integer>(), torange = new ArrayList<Integer>();
    int lastppos = 0, lastpframe = 0;
    for (String range : set.get(attr))
    {
      List<Integer> ints = new ArrayList<Integer>();
      StringTokenizer st = new StringTokenizer(range, " ");
      while (st.hasMoreTokens())
      {
        String num = st.nextToken();
        try
        {
          ints.add(new Integer(num));
        } catch (NumberFormatException nfe)
        {
          throw new InvalidGFF3FieldException(attr, set,
                  "Invalid number in field " + num);
        }
      }
      // Align positionInRef positionInQuery LengthInRef
      // contig_1146 exonerate:protein2genome:local similarity 8534 11269
      // 3652 - . alignment_id 0 ;
      // Query DDB_G0269124
      // Align 11270 143 120
      // corresponds to : 120 bases align at pos 143 in protein to 11270 on
      // dna in strand direction
      // Align 11150 187 282
      // corresponds to : 282 bases align at pos 187 in protein to 11150 on
      // dna in strand direction
      //
      // Align 10865 281 888
      // Align 9977 578 1068
      // Align 8909 935 375
      //
      if (ints.size() != 3)
      {
        throw new InvalidGFF3FieldException(attr, set,
                "Invalid number of fields for this attribute ("
                        + ints.size() + ")");
      }
      fromrange.add(new Integer(ints.get(0).intValue()));
      fromrange.add(new Integer(ints.get(0).intValue() + strand
              * ints.get(2).intValue()));
      // how are intron/exon boundaries that do not align in codons
      // represented
      if (ints.get(1).equals(lastppos) && lastpframe > 0)
      {
        // extend existing to map
        lastppos += ints.get(2) / 3;
        lastpframe = ints.get(2) % 3;
        torange.set(torange.size() - 1, new Integer(lastppos));
      }
      else
      {
        // new to map range
        torange.add(ints.get(1));
        lastppos = ints.get(1) + ints.get(2) / 3;
        lastpframe = ints.get(2) % 3;
        torange.add(new Integer(lastppos));
      }
    }
    // from and to ranges must end up being a series of start/end intervals
    if (fromrange.size() % 2 == 1)
    {
      throw new InvalidGFF3FieldException(attr, set,
              "Couldn't parse the DNA alignment range correctly");
    }
    if (torange.size() % 2 == 1)
    {
      throw new InvalidGFF3FieldException(attr, set,
              "Couldn't parse the protein alignment range correctly");
    }
    // finally, build the map
    int[] frommap = new int[fromrange.size()], tomap = new int[torange
            .size()];
    int p = 0;
    for (Integer ip : fromrange)
    {
      frommap[p++] = ip.intValue();
    }
    p = 0;
    for (Integer ip : torange)
    {
      tomap[p++] = ip.intValue();
    }

    return new MapList(frommap, tomap, 3, 1);
  }

  private List<SequenceI> findNames(AlignmentI align,
          List<SequenceI> newseqs, boolean relaxedIdMatching,
          List<String> list)
  {
    List<SequenceI> found = new ArrayList<SequenceI>();
    for (String seqId : list)
    {
      SequenceI seq = findName(align, seqId, relaxedIdMatching, newseqs);
      if (seq != null)
      {
        found.add(seq);
      }
    }
    return found;
  }

  private AlignmentI lastmatchedAl = null;

  private SequenceIdMatcher matcher = null;

  /**
   * clear any temporary handles used to speed up ID matching
   */
  private void resetMatcher()
  {
    lastmatchedAl = null;
    matcher = null;
  }

  private SequenceI findName(AlignmentI align, String seqId,
          boolean relaxedIdMatching, List<SequenceI> newseqs)
  {
    SequenceI match = null;
    if (relaxedIdMatching)
    {
      if (lastmatchedAl != align)
      {
        matcher = new SequenceIdMatcher(
                (lastmatchedAl = align).getSequencesArray());
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
    jalview.util.ParseHtmlBodyAndLinks parsed = new jalview.util.ParseHtmlBodyAndLinks(
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
   * @param seqs
   *          source of sequence features
   * @param visible
   *          hash of feature types and colours
   * @return features file contents
   */
  public String printJalviewFormat(SequenceI[] seqs,
          Map<String, Object> visible)
  {
    return printJalviewFormat(seqs, visible, true, true);
  }

  /**
   * generate a features file for seqs with colours from visible (if any)
   * 
   * @param seqs
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
  public String printJalviewFormat(SequenceI[] seqs, Map visible,
          boolean visOnly, boolean nonpos)
  {
    StringBuffer out = new StringBuffer();
    SequenceFeature[] next;
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
      Iterator en = visible.keySet().iterator();
      String type, color;
      while (en.hasNext())
      {
        type = en.next().toString();

        if (visible.get(type) instanceof GraduatedColor)
        {
          GraduatedColor gc = (GraduatedColor) visible.get(type);
          color = (gc.isColourByLabel() ? "label|" : "")
                  + Format.getHexString(gc.getMinColor()) + "|"
                  + Format.getHexString(gc.getMaxColor())
                  + (gc.isAutoScale() ? "|" : "|abso|") + gc.getMin() + "|"
                  + gc.getMax() + "|";
          if (gc.getThreshType() != AnnotationColourGradient.NO_THRESHOLD)
          {
            if (gc.getThreshType() == AnnotationColourGradient.BELOW_THRESHOLD)
            {
              color += "below";
            }
            else
            {
              if (gc.getThreshType() != AnnotationColourGradient.ABOVE_THRESHOLD)
              {
                System.err.println("WARNING: Unsupported threshold type ("
                        + gc.getThreshType() + ") : Assuming 'above'");
              }
              color += "above";
            }
            // add the value
            color += "|" + gc.getThresh();
          }
          else
          {
            color += "none";
          }
        }
        else if (visible.get(type) instanceof java.awt.Color)
        {
          color = Format.getHexString((java.awt.Color) visible.get(type));
        }
        else
        {
          // legacy support for integer objects containing colour triplet values
          color = Format.getHexString(new java.awt.Color(Integer
                  .parseInt(visible.get(type).toString())));
        }
        out.append(type);
        out.append("\t");
        out.append(color);
        out.append(newline);
      }
    }
    // Work out which groups are both present and visible
    Vector groups = new Vector();
    int groupIndex = 0;
    boolean isnonpos = false;

    for (int i = 0; i < seqs.length; i++)
    {
      next = seqs[i].getSequenceFeatures();
      if (next != null)
      {
        for (int j = 0; j < next.length; j++)
        {
          isnonpos = next[j].begin == 0 && next[j].end == 0;
          if ((!nonpos && isnonpos)
                  || (!isnonpos && visOnly && !visible
                          .containsKey(next[j].type)))
          {
            continue;
          }

          if (next[j].featureGroup != null
                  && !groups.contains(next[j].featureGroup))
          {
            groups.addElement(next[j].featureGroup);
          }
        }
      }
    }

    String group = null;
    do
    {

      if (groups.size() > 0 && groupIndex < groups.size())
      {
        group = groups.elementAt(groupIndex).toString();
        out.append(newline);
        out.append("STARTGROUP\t");
        out.append(group);
        out.append(newline);
      }
      else
      {
        group = null;
      }

      for (int i = 0; i < seqs.length; i++)
      {
        next = seqs[i].getSequenceFeatures();
        if (next != null)
        {
          for (int j = 0; j < next.length; j++)
          {
            isnonpos = next[j].begin == 0 && next[j].end == 0;
            if ((!nonpos && isnonpos)
                    || (!isnonpos && visOnly && !visible
                            .containsKey(next[j].type)))
            {
              // skip if feature is nonpos and we ignore them or if we only
              // output visible and it isn't non-pos and it's not visible
              continue;
            }

            if (group != null
                    && (next[j].featureGroup == null || !next[j].featureGroup
                            .equals(group)))
            {
              continue;
            }

            if (group == null && next[j].featureGroup != null)
            {
              continue;
            }
            // we have features to output
            featuresGen = true;
            if (next[j].description == null
                    || next[j].description.equals(""))
            {
              out.append(next[j].type + "\t");
            }
            else
            {
              if (next[j].links != null
                      && next[j].getDescription().indexOf("<html>") == -1)
              {
                out.append("<html>");
              }

              out.append(next[j].description + " ");
              if (next[j].links != null)
              {
                for (int l = 0; l < next[j].links.size(); l++)
                {
                  String label = next[j].links.elementAt(l).toString();
                  String href = label.substring(label.indexOf("|") + 1);
                  label = label.substring(0, label.indexOf("|"));

                  if (next[j].description.indexOf(href) == -1)
                  {
                    out.append("<a href=\"" + href + "\">" + label + "</a>");
                  }
                }

                if (next[j].getDescription().indexOf("</html>") == -1)
                {
                  out.append("</html>");
                }
              }

              out.append("\t");
            }
            out.append(seqs[i].getName());
            out.append("\t-1\t");
            out.append(next[j].begin);
            out.append("\t");
            out.append(next[j].end);
            out.append("\t");
            out.append(next[j].type);
            if (!Float.isNaN(next[j].score))
            {
              out.append("\t");
              out.append(next[j].score);
            }
            out.append(newline);
          }
        }
      }

      if (group != null)
      {
        out.append("ENDGROUP\t");
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
   * generate a gff file for sequence features includes non-pos features by
   * default.
   * 
   * @param seqs
   * @param visible
   * @return
   */
  public String printGFFFormat(SequenceI[] seqs, Map<String, Object> visible)
  {
    return printGFFFormat(seqs, visible, true, true);
  }

  public String printGFFFormat(SequenceI[] seqs,
          Map<String, Object> visible, boolean visOnly, boolean nonpos)
  {
    StringBuffer out = new StringBuffer();
    SequenceFeature[] next;
    String source;
    boolean isnonpos;
    for (int i = 0; i < seqs.length; i++)
    {
      if (seqs[i].getSequenceFeatures() != null)
      {
        next = seqs[i].getSequenceFeatures();
        for (int j = 0; j < next.length; j++)
        {
          isnonpos = next[j].begin == 0 && next[j].end == 0;
          if ((!nonpos && isnonpos)
                  || (!isnonpos && visOnly && !visible
                          .containsKey(next[j].type)))
          {
            continue;
          }

          source = next[j].featureGroup;
          if (source == null)
          {
            source = next[j].getDescription();
          }

          out.append(seqs[i].getName());
          out.append("\t");
          out.append(source);
          out.append("\t");
          out.append(next[j].type);
          out.append("\t");
          out.append(next[j].begin);
          out.append("\t");
          out.append(next[j].end);
          out.append("\t");
          out.append(next[j].score);
          out.append("\t");

          if (next[j].getValue("STRAND") != null)
          {
            out.append(next[j].getValue("STRAND"));
            out.append("\t");
          }
          else
          {
            out.append(".\t");
          }

          if (next[j].getValue("FRAME") != null)
          {
            out.append(next[j].getValue("FRAME"));
          }
          else
          {
            out.append(".");
          }
          // TODO: verify/check GFF - should there be a /t here before attribute
          // output ?

          if (next[j].getValue("ATTRIBUTES") != null)
          {
            out.append(next[j].getValue("ATTRIBUTES"));
          }

          out.append(newline);

        }
      }
    }

    return out.toString();
  }

  /**
   * this is only for the benefit of object polymorphism - method does nothing.
   */
  public void parse()
  {
    // IGNORED
  }

  /**
   * this is only for the benefit of object polymorphism - method does nothing.
   * 
   * @return error message
   */
  public String print()
  {
    return "USE printGFFFormat() or printJalviewFormat()";
  }

}
