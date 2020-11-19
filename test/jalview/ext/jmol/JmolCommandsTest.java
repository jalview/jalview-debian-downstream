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
package jalview.ext.jmol;

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

import java.util.HashMap;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JmolCommandsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testGetColourBySequenceCommand_noFeatures()
  {
    SequenceI seq1 = new Sequence("seq1", "MHRSQTRALK");
    SequenceI seq2 = new Sequence("seq2", "MRLEITQSGD");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignFrame af = new AlignFrame(al, 800, 500);
    SequenceRenderer sr = new SequenceRenderer(af.getViewport());
    SequenceI[][] seqs = new SequenceI[][] { { seq1 }, { seq2 } };
    String[] files = new String[] { "seq1.pdb", "seq2.pdb" };
    StructureSelectionManager ssm = new StructureSelectionManager();

    // need some mappings!

    StructureMappingcommandSet[] commands = JmolCommands
            .getColourBySequenceCommand(ssm, files, seqs, sr, af.alignPanel);
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
  
    StructureMappingcommandSet[] commands = JmolCommands
            .getColourBySequenceCommand(ssm, files, seqs, sr, af.alignPanel);
    assertEquals(commands.length, 2);
    assertEquals(commands[0].commands.length, 1);

    String chainACommand = commands[0].commands[0];
    // M colour is #82827d == (130, 130, 125) (see strand.html help page)
    assertTrue(chainACommand
            .contains("select 21:A/1.1;color[130,130,125]")); // first one
    // H colour is #60609f == (96, 96, 159)
    assertTrue(chainACommand.contains(";select 22:A/1.1;color[96,96,159]"));
    // hidden columns are Gray (128, 128, 128)
    assertTrue(chainACommand
            .contains(";select 23-25:A/1.1;color[128,128,128]"));
    // S and G are both coloured #4949b6 == (73, 73, 182)
    assertTrue(chainACommand
            .contains(";select 26-30:A/1.1;color[73,73,182]"));

    String chainBCommand = commands[1].commands[0];
    // M colour is #82827d == (130, 130, 125)
    assertTrue(chainBCommand
            .contains("select 21:B/2.1;color[130,130,125]"));
    // V colour is #ffff00 == (255, 255, 0)
    assertTrue(chainBCommand
.contains(";select 22:B/2.1;color[255,255,0]"));
    // hidden columns are Gray (128, 128, 128)
    assertTrue(chainBCommand
            .contains(";select 23-25:B/2.1;color[128,128,128]"));
    // S and G are both coloured #4949b6 == (73, 73, 182)
    assertTrue(chainBCommand
            .contains(";select 26-30:B/2.1;color[73,73,182]"));
  }
}
