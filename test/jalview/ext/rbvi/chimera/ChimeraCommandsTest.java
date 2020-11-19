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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.gui.SequenceRenderer;
import jalview.schemes.JalviewColourScheme;
import jalview.structure.StructureMapping;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ChimeraCommandsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testBuildColourCommands()
  {

    Map<Object, AtomSpecModel> map = new LinkedHashMap<Object, AtomSpecModel>();
    ChimeraCommands.addAtomSpecRange(map, Color.blue, 0, 2, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, 0, 7, 7, "B");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, 0, 9, 23, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, 1, 1, 1, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, 1, 4, 7, "B");
    ChimeraCommands.addAtomSpecRange(map, Color.yellow, 1, 8, 8, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.yellow, 1, 3, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.red, 0, 3, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.red, 0, 6, 9, "A");

    // Colours should appear in the Chimera command in the order in which
    // they were added; within colour, by model, by chain, ranges in start order
    String command = ChimeraCommands.buildColourCommands(map).get(0);
    assertEquals(
            command,
            "color #0000ff #0:2-5.A,9-23.A,7.B|#1:1.A,4-7.B; color #ffff00 #1:3-5.A,8.A; color #ff0000 #0:3-9.A");
  }

  @Test(groups = { "Functional" })
  public void testBuildSetAttributeCommands()
  {
    /*
     * make a map of { featureType, {featureValue, {residue range specification } } }
     */
    Map<String, Map<Object, AtomSpecModel>> featuresMap = new LinkedHashMap<String, Map<Object, AtomSpecModel>>();
    Map<Object, AtomSpecModel> featureValues = new HashMap<Object, AtomSpecModel>();
    
    /*
     * start with just one feature/value...
     */
    featuresMap.put("chain", featureValues);
    ChimeraCommands.addAtomSpecRange(featureValues, "X", 0, 8, 20, "A");
  
    List<String> commands = ChimeraCommands
            .buildSetAttributeCommands(featuresMap);
    assertEquals(1, commands.size());

    /*
     * feature name gets a jv_ namespace prefix
     * feature value is quoted in case it contains spaces
     */
    assertEquals(commands.get(0), "setattr r jv_chain 'X' #0:8-20.A");

    // add same feature value, overlapping range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", 0, 3, 9, "A");
    // same feature value, contiguous range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", 0, 21, 25, "A");
    commands = ChimeraCommands.buildSetAttributeCommands(featuresMap);
    assertEquals(1, commands.size());
    assertEquals(commands.get(0), "setattr r jv_chain 'X' #0:3-25.A");

    // same feature value and model, different chain
    ChimeraCommands.addAtomSpecRange(featureValues, "X", 0, 21, 25, "B");
    // same feature value and chain, different model
    ChimeraCommands.addAtomSpecRange(featureValues, "X", 1, 26, 30, "A");
    commands = ChimeraCommands.buildSetAttributeCommands(featuresMap);
    assertEquals(1, commands.size());
    assertEquals(commands.get(0),
            "setattr r jv_chain 'X' #0:3-25.A,21-25.B|#1:26-30.A");

    // same feature, different value
    ChimeraCommands.addAtomSpecRange(featureValues, "Y", 0, 40, 50, "A");
    commands = ChimeraCommands.buildSetAttributeCommands(featuresMap);
    assertEquals(2, commands.size());
    // commands are ordered by feature type but not by value
    // so use contains to test for the expected command:
    assertTrue(commands
            .contains("setattr r jv_chain 'X' #0:3-25.A,21-25.B|#1:26-30.A"));
    assertTrue(commands.contains("setattr r jv_chain 'Y' #0:40-50.A"));

    featuresMap.clear();
    featureValues.clear();
    featuresMap.put("side-chain binding!", featureValues);
    ChimeraCommands.addAtomSpecRange(featureValues,
            "<html>metal <a href=\"http:a.b.c/x\"> 'ion!", 0, 7, 15,
            "A");
    // feature names are sanitised to change non-alphanumeric to underscore
    // feature values are sanitised to encode single quote characters
    commands = ChimeraCommands.buildSetAttributeCommands(featuresMap);
    assertTrue(commands
            .contains("setattr r jv_side_chain_binding_ '<html>metal <a href=\"http:a.b.c/x\"> &#39;ion!' #0:7-15.A"));
  }

  /**
   * Tests for the method that prefixes and sanitises a feature name so it can
   * be used as a valid, namespaced attribute name in Chimera
   */
  @Test(groups = { "Functional" })
  public void testMakeAttributeName()
  {
    assertEquals(ChimeraCommands.makeAttributeName(null), "jv_");
    assertEquals(ChimeraCommands.makeAttributeName(""), "jv_");
    assertEquals(ChimeraCommands.makeAttributeName("helix"), "jv_helix");
    assertEquals(ChimeraCommands.makeAttributeName("Hello World 24"),
            "jv_Hello_World_24");
    assertEquals(
            ChimeraCommands.makeAttributeName("!this is-a_very*{odd(name"),
            "jv__this_is_a_very__odd_name");
    // name ending in color gets underscore appended
    assertEquals(ChimeraCommands.makeAttributeName("helixColor"),
            "jv_helixColor_");
  }

  @Test(groups = { "Functional" })
  public void testGetColourBySequenceCommands_hiddenColumns()
  {
    /*
     * load these sequences, coloured by Strand propensity,
     * with columns 2-4 hidden
     */
    SequenceI seq1 = new Sequence("seq1", "MHRSQSSSGG");
    SequenceI seq2 = new Sequence("seq2", "MVRSNGGSSS");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignFrame af = new AlignFrame(al, 800, 500);
    af.changeColour_actionPerformed(JalviewColourScheme.Strand.toString());
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(2);
    cs.addElement(3);
    cs.addElement(4);
    af.getViewport().setColumnSelection(cs);
    af.hideSelColumns_actionPerformed(null);
    SequenceRenderer sr = new SequenceRenderer(af.getViewport());
    SequenceI[][] seqs = new SequenceI[][] { { seq1 }, { seq2 } };
    String[] files = new String[] { "seq1.pdb", "seq2.pdb" };
    StructureSelectionManager ssm = new StructureSelectionManager();

    /*
     * map residues 1-10 to residues 21-30 (atoms 105-150) in structures
     */
    HashMap<Integer, int[]> map = new HashMap<Integer, int[]>();
    for (int pos = 1; pos <= seq1.getLength(); pos++)
    {
      map.put(pos, new int[] { 20 + pos, 5 * (20 + pos) });
    }
    StructureMapping sm1 = new StructureMapping(seq1, "seq1.pdb", "pdb1",
            "A", map, null);
    ssm.addStructureMapping(sm1);
    StructureMapping sm2 = new StructureMapping(seq2, "seq2.pdb", "pdb2",
            "B", map, null);
    ssm.addStructureMapping(sm2);

    StructureMappingcommandSet[] commands = ChimeraCommands
            .getColourBySequenceCommand(ssm, files, seqs, sr, af.alignPanel);
    assertEquals(1, commands.length);
    assertEquals(1, commands[0].commands.length);
    String theCommand = commands[0].commands[0];
    // M colour is #82827d (see strand.html help page)
    assertTrue(theCommand.contains("color #82827d #0:21.A|#1:21.B"));
    // H colour is #60609f
    assertTrue(theCommand.contains("color #60609f #0:22.A"));
    // V colour is #ffff00
    assertTrue(theCommand.contains("color #ffff00 #1:22.B"));
    // hidden columns are Gray (128, 128, 128)
    assertTrue(theCommand.contains("color #808080 #0:23-25.A|#1:23-25.B"));
    // S and G are both coloured #4949b6
    assertTrue(theCommand.contains("color #4949b6 #0:26-30.A|#1:26-30.B"));
  }
}
