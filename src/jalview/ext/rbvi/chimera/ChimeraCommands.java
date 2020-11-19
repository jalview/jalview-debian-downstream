/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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
package jalview.ext.rbvi.chimera;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.Desktop;
import jalview.renderer.seqfeatures.FeatureColourFinder;
import jalview.structure.StructureMapping;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;
import jalview.util.ColorUtils;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Routines for generating Chimera commands for Jalview/Chimera binding
 * 
 * @author JimP
 * 
 */
public class ChimeraCommands
{

  public static final String NAMESPACE_PREFIX = "jv_";

  /**
   * Constructs Chimera commands to colour residues as per the Jalview alignment
   * 
   * @param ssm
   * @param files
   * @param sequence
   * @param sr
   * @param fr
   * @param viewPanel
   * @return
   */
  public static StructureMappingcommandSet[] getColourBySequenceCommand(
          StructureSelectionManager ssm, String[] files,
          SequenceI[][] sequence, SequenceRenderer sr,
          AlignmentViewPanel viewPanel)
  {
    Map<Object, AtomSpecModel> colourMap = buildColoursMap(ssm, files,
            sequence, sr, viewPanel);

    List<String> colourCommands = buildColourCommands(colourMap);

    StructureMappingcommandSet cs = new StructureMappingcommandSet(
            ChimeraCommands.class, null,
            colourCommands.toArray(new String[colourCommands.size()]));

    return new StructureMappingcommandSet[] { cs };
  }

  /**
   * Traverse the map of colours/models/chains/positions to construct a list of
   * 'color' commands (one per distinct colour used). The format of each command
   * is
   * 
   * <pre>
   * <blockquote> 
   * color colorname #modelnumber:range.chain 
   * e.g. color #00ff00 #0:2.B,4.B,9-12.B|#1:1.A,2-6.A,...
   * </blockquote>
   * </pre>
   * 
   * @param colourMap
   * @return
   */
  protected static List<String> buildColourCommands(
          Map<Object, AtomSpecModel> colourMap)
  {
    /*
     * This version concatenates all commands into a single String (semi-colon
     * delimited). If length limit issues arise, refactor to return one color
     * command per colour.
     */
    List<String> commands = new ArrayList<>();
    StringBuilder sb = new StringBuilder(256);
    boolean firstColour = true;
    for (Object key : colourMap.keySet())
    {
      Color colour = (Color) key;
      String colourCode = ColorUtils.toTkCode(colour);
      if (!firstColour)
      {
        sb.append("; ");
      }
      sb.append("color ").append(colourCode).append(" ");
      firstColour = false;
      final AtomSpecModel colourData = colourMap.get(colour);
      sb.append(colourData.getAtomSpec());
    }
    commands.add(sb.toString());
    return commands;
  }

  /**
   * Traverses a map of { modelNumber, {chain, {list of from-to ranges} } } and
   * builds a Chimera format atom spec
   * 
   * @param modelAndChainRanges
   */
  protected static String getAtomSpec(
          Map<Integer, Map<String, List<int[]>>> modelAndChainRanges)
  {
    StringBuilder sb = new StringBuilder(128);
    boolean firstModelForColour = true;
    for (Integer model : modelAndChainRanges.keySet())
    {
      boolean firstPositionForModel = true;
      if (!firstModelForColour)
      {
        sb.append("|");
      }
      firstModelForColour = false;
      sb.append("#").append(model).append(":");

      final Map<String, List<int[]>> modelData = modelAndChainRanges
              .get(model);
      for (String chain : modelData.keySet())
      {
        boolean hasChain = !"".equals(chain.trim());
        for (int[] range : modelData.get(chain))
        {
          if (!firstPositionForModel)
          {
            sb.append(",");
          }
          if (range[0] == range[1])
          {
            sb.append(range[0]);
          }
          else
          {
            sb.append(range[0]).append("-").append(range[1]);
          }
          if (hasChain)
          {
            sb.append(".").append(chain);
          }
          firstPositionForModel = false;
        }
      }
    }
    return sb.toString();
  }

  /**
   * <pre>
   * Build a data structure which records contiguous subsequences for each colour. 
   * From this we can easily generate the Chimera command for colour by sequence.
   * Color
   *     Model number
   *         Chain
   *             list of start/end ranges
   * Ordering is by order of addition (for colours and positions), natural ordering (for models and chains)
   * </pre>
   */
  protected static Map<Object, AtomSpecModel> buildColoursMap(
          StructureSelectionManager ssm, String[] files,
          SequenceI[][] sequence, SequenceRenderer sr,
          AlignmentViewPanel viewPanel)
  {
    FeatureRenderer fr = viewPanel.getFeatureRenderer();
    FeatureColourFinder finder = new FeatureColourFinder(fr);
    AlignViewportI viewport = viewPanel.getAlignViewport();
    HiddenColumns cs = viewport.getAlignment().getHiddenColumns();
    AlignmentI al = viewport.getAlignment();
    Map<Object, AtomSpecModel> colourMap = new LinkedHashMap<>();
    Color lastColour = null;

    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      StructureMapping[] mapping = ssm.getMapping(files[pdbfnum]);

      if (mapping == null || mapping.length < 1)
      {
        continue;
      }

      int startPos = -1, lastPos = -1;
      String lastChain = "";
      for (int s = 0; s < sequence[pdbfnum].length; s++)
      {
        for (int sp, m = 0; m < mapping.length; m++)
        {
          final SequenceI seq = sequence[pdbfnum][s];
          if (mapping[m].getSequence() == seq
                  && (sp = al.findIndex(seq)) > -1)
          {
            SequenceI asp = al.getSequenceAt(sp);
            for (int r = 0; r < asp.getLength(); r++)
            {
              // no mapping to gaps in sequence
              if (Comparison.isGap(asp.getCharAt(r)))
              {
                continue;
              }
              int pos = mapping[m].getPDBResNum(asp.findPosition(r));

              if (pos < 1 || pos == lastPos)
              {
                continue;
              }

              Color colour = sr.getResidueColour(seq, r, finder);

              /*
               * darker colour for hidden regions
               */
              if (!cs.isVisible(r))
              {
                colour = Color.GRAY;
              }

              final String chain = mapping[m].getChain();

              /*
               * Just keep incrementing the end position for this colour range
               * _unless_ colour, PDB model or chain has changed, or there is a
               * gap in the mapped residue sequence
               */
              final boolean newColour = !colour.equals(lastColour);
              final boolean nonContig = lastPos + 1 != pos;
              final boolean newChain = !chain.equals(lastChain);
              if (newColour || nonContig || newChain)
              {
                if (startPos != -1)
                {
                  addAtomSpecRange(colourMap, lastColour, pdbfnum, startPos,
                          lastPos, lastChain);
                }
                startPos = pos;
              }
              lastColour = colour;
              lastPos = pos;
              lastChain = chain;
            }
            // final colour range
            if (lastColour != null)
            {
              addAtomSpecRange(colourMap, lastColour, pdbfnum, startPos,
                      lastPos, lastChain);
            }
            // break;
          }
        }
      }
    }
    return colourMap;
  }

  /**
   * Helper method to add one contiguous range to the AtomSpec model for the given
   * value (creating the model if necessary). As used by Jalview, {@code value} is
   * <ul>
   * <li>a colour, when building a 'colour structure by sequence' command</li>
   * <li>a feature value, when building a 'set Chimera attributes from features'
   * command</li>
   * </ul>
   * 
   * @param map
   * @param value
   * @param model
   * @param startPos
   * @param endPos
   * @param chain
   */
  protected static void addAtomSpecRange(Map<Object, AtomSpecModel> map,
          Object value, int model, int startPos, int endPos, String chain)
  {
    /*
     * Get/initialize map of data for the colour
     */
    AtomSpecModel atomSpec = map.get(value);
    if (atomSpec == null)
    {
      atomSpec = new AtomSpecModel();
      map.put(value, atomSpec);
    }

    atomSpec.addRange(model, startPos, endPos, chain);
  }

  /**
   * Constructs and returns Chimera commands to set attributes on residues
   * corresponding to features in Jalview. Attribute names are the Jalview
   * feature type, with a "jv_" prefix.
   * 
   * @param ssm
   * @param files
   * @param seqs
   * @param viewPanel
   * @return
   */
  public static StructureMappingcommandSet getSetAttributeCommandsForFeatures(
          StructureSelectionManager ssm, String[] files, SequenceI[][] seqs,
          AlignmentViewPanel viewPanel)
  {
    Map<String, Map<Object, AtomSpecModel>> featureMap = buildFeaturesMap(
            ssm, files, seqs, viewPanel);

    List<String> commands = buildSetAttributeCommands(featureMap);

    StructureMappingcommandSet cs = new StructureMappingcommandSet(
            ChimeraCommands.class, null,
            commands.toArray(new String[commands.size()]));

    return cs;
  }

  /**
   * <pre>
   * Helper method to build a map of 
   *   { featureType, { feature value, AtomSpecModel } }
   * </pre>
   * 
   * @param ssm
   * @param files
   * @param seqs
   * @param viewPanel
   * @return
   */
  protected static Map<String, Map<Object, AtomSpecModel>> buildFeaturesMap(
          StructureSelectionManager ssm, String[] files, SequenceI[][] seqs,
          AlignmentViewPanel viewPanel)
  {
    Map<String, Map<Object, AtomSpecModel>> theMap = new LinkedHashMap<>();

    FeatureRenderer fr = viewPanel.getFeatureRenderer();
    if (fr == null)
    {
      return theMap;
    }

    AlignViewportI viewport = viewPanel.getAlignViewport();
    List<String> visibleFeatures = fr.getDisplayedFeatureTypes();

    /*
     * if alignment is showing features from complement, we also transfer
     * these features to the corresponding mapped structure residues
     */
    boolean showLinkedFeatures = viewport.isShowComplementFeatures();
    List<String> complementFeatures = new ArrayList<>();
    FeatureRenderer complementRenderer = null;
    if (showLinkedFeatures)
    {
      AlignViewportI comp = fr.getViewport().getCodingComplement();
      if (comp != null)
      {
        complementRenderer = Desktop.getAlignFrameFor(comp)
                .getFeatureRenderer();
        complementFeatures = complementRenderer.getDisplayedFeatureTypes();
      }
    }
    if (visibleFeatures.isEmpty() && complementFeatures.isEmpty())
    {
      return theMap;
    }

    AlignmentI alignment = viewPanel.getAlignment();
    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      StructureMapping[] mapping = ssm.getMapping(files[pdbfnum]);

      if (mapping == null || mapping.length < 1)
      {
        continue;
      }

      for (int seqNo = 0; seqNo < seqs[pdbfnum].length; seqNo++)
      {
        for (int m = 0; m < mapping.length; m++)
        {
          final SequenceI seq = seqs[pdbfnum][seqNo];
          int sp = alignment.findIndex(seq);
          StructureMapping structureMapping = mapping[m];
          if (structureMapping.getSequence() == seq && sp > -1)
          {
            /*
             * found a sequence with a mapping to a structure;
             * now scan its features
             */
            if (!visibleFeatures.isEmpty())
            {
              scanSequenceFeatures(visibleFeatures, structureMapping, seq,
                      theMap, pdbfnum);
            }
            if (showLinkedFeatures)
            {
              scanComplementFeatures(complementRenderer, structureMapping,
                      seq, theMap, pdbfnum);
            }
          }
        }
      }
    }
    return theMap;
  }

  /**
   * Scans visible features in mapped positions of the CDS/peptide complement, and
   * adds any found to the map of attribute values/structure positions
   * 
   * @param complementRenderer
   * @param structureMapping
   * @param seq
   * @param theMap
   * @param modelNumber
   */
  protected static void scanComplementFeatures(
          FeatureRenderer complementRenderer,
          StructureMapping structureMapping, SequenceI seq,
          Map<String, Map<Object, AtomSpecModel>> theMap, int modelNumber)
  {
    /*
     * for each sequence residue mapped to a structure position...
     */
    for (int seqPos : structureMapping.getMapping().keySet())
    {
      /*
       * find visible complementary features at mapped position(s)
       */
      MappedFeatures mf = complementRenderer
              .findComplementFeaturesAtResidue(seq, seqPos);
      if (mf != null)
      {
        for (SequenceFeature sf : mf.features)
        {
          String type = sf.getType();

          /*
           * Don't copy features which originated from Chimera
           */
          if (JalviewChimeraBinding.CHIMERA_FEATURE_GROUP
                  .equals(sf.getFeatureGroup()))
          {
            continue;
          }

          /*
           * record feature 'value' (score/description/type) as at the
           * corresponding structure position
           */
          List<int[]> mappedRanges = structureMapping
                  .getPDBResNumRanges(seqPos, seqPos);

          if (!mappedRanges.isEmpty())
          {
            String value = sf.getDescription();
            if (value == null || value.length() == 0)
            {
              value = type;
            }
            float score = sf.getScore();
            if (score != 0f && !Float.isNaN(score))
            {
              value = Float.toString(score);
            }
            Map<Object, AtomSpecModel> featureValues = theMap.get(type);
            if (featureValues == null)
            {
              featureValues = new HashMap<>();
              theMap.put(type, featureValues);
            }
            for (int[] range : mappedRanges)
            {
              addAtomSpecRange(featureValues, value, modelNumber, range[0],
                      range[1], structureMapping.getChain());
            }
          }
        }
      }
    }
  }

  /**
   * Inspect features on the sequence; for each feature that is visible, determine
   * its mapped ranges in the structure (if any) according to the given mapping,
   * and add them to the map.
   * 
   * @param visibleFeatures
   * @param mapping
   * @param seq
   * @param theMap
   * @param modelNumber
   */
  protected static void scanSequenceFeatures(List<String> visibleFeatures,
          StructureMapping mapping, SequenceI seq,
          Map<String, Map<Object, AtomSpecModel>> theMap, int modelNumber)
  {
    List<SequenceFeature> sfs = seq.getFeatures().getPositionalFeatures(
            visibleFeatures.toArray(new String[visibleFeatures.size()]));
    for (SequenceFeature sf : sfs)
    {
      String type = sf.getType();

      /*
       * Don't copy features which originated from Chimera
       */
      if (JalviewChimeraBinding.CHIMERA_FEATURE_GROUP
              .equals(sf.getFeatureGroup()))
      {
        continue;
      }

      List<int[]> mappedRanges = mapping.getPDBResNumRanges(sf.getBegin(),
              sf.getEnd());

      if (!mappedRanges.isEmpty())
      {
        String value = sf.getDescription();
        if (value == null || value.length() == 0)
        {
          value = type;
        }
        float score = sf.getScore();
        if (score != 0f && !Float.isNaN(score))
        {
          value = Float.toString(score);
        }
        Map<Object, AtomSpecModel> featureValues = theMap.get(type);
        if (featureValues == null)
        {
          featureValues = new HashMap<>();
          theMap.put(type, featureValues);
        }
        for (int[] range : mappedRanges)
        {
          addAtomSpecRange(featureValues, value, modelNumber, range[0],
                  range[1], mapping.getChain());
        }
      }
    }
  }

  /**
   * Traverse the map of features/values/models/chains/positions to construct a
   * list of 'setattr' commands (one per distinct feature type and value).
   * <p>
   * The format of each command is
   * 
   * <pre>
   * <blockquote> setattr r <featureName> " " #modelnumber:range.chain 
   * e.g. setattr r jv:chain <value> #0:2.B,4.B,9-12.B|#1:1.A,2-6.A,...
   * </blockquote>
   * </pre>
   * 
   * @param featureMap
   * @return
   */
  protected static List<String> buildSetAttributeCommands(
          Map<String, Map<Object, AtomSpecModel>> featureMap)
  {
    List<String> commands = new ArrayList<>();
    for (String featureType : featureMap.keySet())
    {
      String attributeName = makeAttributeName(featureType);

      /*
       * clear down existing attributes for this feature
       */
      // 'problem' - sets attribute to None on all residues - overkill?
      // commands.add("~setattr r " + attributeName + " :*");

      Map<Object, AtomSpecModel> values = featureMap.get(featureType);
      for (Object value : values.keySet())
      {
        /*
         * for each distinct value recorded for this feature type,
         * add a command to set the attribute on the mapped residues
         * Put values in single quotes, encoding any embedded single quotes
         */
        StringBuilder sb = new StringBuilder(128);
        String featureValue = value.toString();
        featureValue = featureValue.replaceAll("\\'", "&#39;");
        sb.append("setattr r ").append(attributeName).append(" '")
                .append(featureValue).append("' ");
        sb.append(values.get(value).getAtomSpec());
        commands.add(sb.toString());
      }
    }

    return commands;
  }

  /**
   * Makes a prefixed and valid Chimera attribute name. A jv_ prefix is applied
   * for a 'Jalview' namespace, and any non-alphanumeric character is converted
   * to an underscore.
   * 
   * @param featureType
   * @return
   * 
   *         <pre>
   * &#64;see https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/setattr.html
   *         </pre>
   */
  protected static String makeAttributeName(String featureType)
  {
    StringBuilder sb = new StringBuilder();
    if (featureType != null)
    {
      for (char c : featureType.toCharArray())
      {
        sb.append(Character.isLetterOrDigit(c) ? c : '_');
      }
    }
    String attName = NAMESPACE_PREFIX + sb.toString();

    /*
     * Chimera treats an attribute name ending in 'color' as colour-valued;
     * Jalview doesn't, so prevent this by appending an underscore
     */
    if (attName.toUpperCase().endsWith("COLOR"))
    {
      attName += "_";
    }

    return attName;
  }

}
