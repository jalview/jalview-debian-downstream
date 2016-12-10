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
package jalview.ext.rbvi.chimera;

import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.structure.StructureMapping;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;
import jalview.util.ColorUtils;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Routines for generating Chimera commands for Jalview/Chimera binding
 * 
 * @author JimP
 * 
 */
public class ChimeraCommands
{

  /**
   * utility to construct the commands to colour chains by the given alignment
   * for passing to Chimera
   * 
   * @returns Object[] { Object[] { <model being coloured>,
   * 
   */
  public static StructureMappingcommandSet[] getColourBySequenceCommand(
          StructureSelectionManager ssm, String[] files,
          SequenceI[][] sequence, SequenceRenderer sr, FeatureRenderer fr,
          AlignmentI alignment)
  {
    Map<Color, Map<Integer, Map<String, List<int[]>>>> colourMap = buildColoursMap(
            ssm, files, sequence, sr, fr, alignment);

    List<String> colourCommands = buildColourCommands(colourMap);

    StructureMappingcommandSet cs = new StructureMappingcommandSet(
            ChimeraCommands.class, null,
            colourCommands.toArray(new String[0]));

    return new StructureMappingcommandSet[] { cs };
  }

  /**
   * Traverse the map of colours/models/chains/positions to construct a list of
   * 'color' commands (one per distinct colour used). The format of each command
   * is
   * 
   * <blockquote> color colorname #modelnumber:range.chain e.g. color #00ff00
   * #0:2.B,4.B,9-12.B|#1:1.A,2-6.A,...
   * 
   * @see http 
   *      ://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/frameatom_spec
   *      .html </pre>
   * 
   * @param colourMap
   * @return
   */
  protected static List<String> buildColourCommands(
          Map<Color, Map<Integer, Map<String, List<int[]>>>> colourMap)
  {
    /*
     * This version concatenates all commands into a single String (semi-colon
     * delimited). If length limit issues arise, refactor to return one color
     * command per colour.
     */
    List<String> commands = new ArrayList<String>();
    StringBuilder sb = new StringBuilder(256);
    boolean firstColour = true;
    for (Color colour : colourMap.keySet())
    {
      String colourCode = ColorUtils.toTkCode(colour);
      if (!firstColour)
      {
        sb.append("; ");
      }
      sb.append("color ").append(colourCode).append(" ");
      firstColour = false;
      boolean firstModelForColour = true;
      final Map<Integer, Map<String, List<int[]>>> colourData = colourMap
              .get(colour);
      for (Integer model : colourData.keySet())
      {
        boolean firstPositionForModel = true;
        if (!firstModelForColour)
        {
          sb.append("|");
        }
        firstModelForColour = false;
        sb.append("#").append(model).append(":");

        final Map<String, List<int[]>> modelData = colourData.get(model);
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
    }
    commands.add(sb.toString());
    return commands;
  }

  /**
   * <pre>
   * Build a data structure which maps contiguous subsequences for each colour. 
   * This generates a data structure from which we can easily generate the 
   * Chimera command for colour by sequence.
   * Color
   *     Model number
   *         Chain
   *             list of start/end ranges
   * Ordering is by order of addition (for colours and positions), natural ordering (for models and chains)
   * </pre>
   */
  protected static Map<Color, Map<Integer, Map<String, List<int[]>>>> buildColoursMap(
          StructureSelectionManager ssm, String[] files,
          SequenceI[][] sequence, SequenceRenderer sr, FeatureRenderer fr,
          AlignmentI alignment)
  {
    Map<Color, Map<Integer, Map<String, List<int[]>>>> colourMap = new LinkedHashMap<Color, Map<Integer, Map<String, List<int[]>>>>();
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
                  && (sp = alignment.findIndex(seq)) > -1)
          {
            SequenceI asp = alignment.getSequenceAt(sp);
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

              Color colour = sr.getResidueColour(seq, r, fr);
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
                  addColourRange(colourMap, lastColour, pdbfnum, startPos,
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
              addColourRange(colourMap, lastColour, pdbfnum, startPos,
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
   * Helper method to add one contiguous colour range to the colour map.
   * 
   * @param colourMap
   * @param colour
   * @param model
   * @param startPos
   * @param endPos
   * @param chain
   */
  protected static void addColourRange(
          Map<Color, Map<Integer, Map<String, List<int[]>>>> colourMap,
          Color colour, int model, int startPos, int endPos, String chain)
  {
    /*
     * Get/initialize map of data for the colour
     */
    Map<Integer, Map<String, List<int[]>>> colourData = colourMap
            .get(colour);
    if (colourData == null)
    {
      colourMap
              .put(colour,
                      colourData = new TreeMap<Integer, Map<String, List<int[]>>>());
    }

    /*
     * Get/initialize map of data for the colour and model
     */
    Map<String, List<int[]>> modelData = colourData.get(model);
    if (modelData == null)
    {
      colourData.put(model, modelData = new TreeMap<String, List<int[]>>());
    }

    /*
     * Get/initialize map of data for colour, model and chain
     */
    List<int[]> chainData = modelData.get(chain);
    if (chainData == null)
    {
      modelData.put(chain, chainData = new ArrayList<int[]>());
    }

    /*
     * Add the start/end positions
     */
    chainData.add(new int[] { startPos, endPos });
  }

}
