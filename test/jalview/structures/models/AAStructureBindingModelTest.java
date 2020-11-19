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
package jalview.structures.models;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.PDBEntry.Type;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormats;
import jalview.schemes.ColourSchemeI;
import jalview.structure.AtomSpec;
import jalview.structure.StructureMappingcommandSet;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel.SuperposeData;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for non-abstract methods of abstract base class
 * 
 * @author gmcarstairs
 *
 */
public class AAStructureBindingModelTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /*
   * Scenario: Jalview has 4 sequences, corresponding to 1YCS (chains A and B), 3A6S|B, 1OOT|A
   */
  private static final String PDB_1 = "HEADER    COMPLEX (ANTI-ONCOGENE/ANKYRIN REPEATS) 30-SEP-96   1YCS              \n"
          + "ATOM      2  CA  VAL A  97      24.134   4.926  45.821  1.00 47.43           C  \n"
          + "ATOM      9  CA  PRO A  98      25.135   8.584  46.217  1.00 41.60           C  \n"
          + "ATOM     16  CA  SER A  99      28.243   9.596  44.271  1.00 39.63           C  \n"
          + "ATOM     22  CA  GLN A 100      31.488  10.133  46.156  1.00 35.60           C  \n"
          // artificial jump in residue numbering to prove it is correctly
          // mapped:
          + "ATOM     31  CA  LYS A 102      33.323  11.587  43.115  1.00 41.69           C  \n"
          + "ATOM   1857  CA  GLU B 374       9.193 -16.005  95.870  1.00 54.22           C  \n"
          + "ATOM   1866  CA  ILE B 375       7.101 -14.921  92.847  1.00 46.82           C  \n"
          + "ATOM   1874  CA  VAL B 376      10.251 -13.625  91.155  1.00 47.80           C  \n"
          + "ATOM   1881  CA  LYS B 377      11.767 -17.068  91.763  1.00 50.21           C  \n"
          + "ATOM   1890  CA  PHE B 378       8.665 -18.948  90.632  1.00 44.85           C  \n";

  private static final String PDB_2 = "HEADER    HYDROLASE                               09-SEP-09   3A6S              \n"
          + "ATOM      2  CA  MET B   1      15.366 -11.648  24.854  1.00 32.05           C  \n"
          + "ATOM     10  CA  LYS B   2      16.846  -9.215  22.340  1.00 25.68           C  \n"
          + "ATOM     19  CA  LYS B   3      15.412  -6.335  20.343  1.00 19.42           C  \n"
          + "ATOM     28  CA  LEU B   4      15.629  -5.719  16.616  1.00 15.49           C  \n"
          + "ATOM     36  CA  GLN B   5      14.412  -2.295  15.567  1.00 12.19           C  \n";

  private static final String PDB_3 = "HEADER    STRUCTURAL GENOMICS                     04-MAR-03   1OOT              \n"
          + "ATOM      2  CA  SER A   7      29.427   3.330  -6.578  1.00 32.50           C  \n"
          + "ATOM      8  CA  PRO A   8      29.975   3.340  -2.797  1.00 17.62           C  \n"
          + "ATOM     16  CA ALYS A   9      26.958   3.024  -0.410  0.50  8.78           C  \n"
          + "ATOM     33  CA  ALA A  10      26.790   4.320   3.172  1.00 11.98           C  \n"
          + "ATOM     39  CA AVAL A  12      24.424   3.853   6.106  0.50 13.83           C  \n";

  /**
   * Multichain PDB with identical sequences imported - Binding should correctly
   * recover chain mappings for each derived sequence
   */
  private static final String PDB_4_MC = "HEADER    HYDROLASE                               09-SEP-09   3A6S              \n"
          + "ATOM      2  CA  MET A   1      15.366 -11.648  24.854  1.00 32.05           C  \n"
          + "ATOM     10  CA  LYS A   2      16.846  -9.215  22.340  1.00 25.68           C  \n"
          + "ATOM     19  CA  LYS A   3      15.412  -6.335  20.343  1.00 19.42           C  \n"
          + "ATOM     28  CA  LEU A   4      15.629  -5.719  16.616  1.00 15.49           C  \n"
          + "ATOM     36  CA  GLN A   5      14.412  -2.295  15.567  1.00 12.19           C  \n"
          + "ATOM   1030  CA  MET B   1      18.869  -7.572   3.432  1.00 31.52           C  \n"
          + "ATOM   1038  CA  LYS B   2      19.182 -10.025   6.313  1.00 26.41           C  \n"
          + "ATOM   1047  CA  LYS B   3      17.107 -12.963   7.534  1.00 19.71           C  \n"
          + "ATOM   1056  CA  LEU B   4      16.142 -13.579  11.164  1.00 14.81           C  \n"
          + "ATOM   1064  CA  GLN B   5      14.648 -17.005  11.785  1.00 13.38           C  \n";

  // TODO: JAL-2227 - import mmCIF PISA assembly & identify master/copy chains

  @Test(groups= {"Functional"})
  public void testImportPDBPreservesChainMappings() throws IOException
  {
    AlignmentI importedAl = new jalview.io.FormatAdapter().readFile(
            PDB_4_MC, DataSourceType.PASTE, FileFormats.getInstance()
                    .forName(jalview.io.FileFormat.PDB.toString()));
    // ideally, we would match on the actual data for the 'File' handle for
    // pasted files,
    // see JAL-623 - pasting is still not correctly handled...
    PDBEntry importedPDB = new PDBEntry("3A6S", "", Type.PDB,
            "Paste");
    AAStructureBindingModel binder = new AAStructureBindingModel(
            new StructureSelectionManager(), new PDBEntry[]
            { importedPDB },
            new SequenceI[][]
            { importedAl.getSequencesArray() }, null)
    {
      
      @Override
      public void updateColours(Object source)
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void releaseReferences(Object svl)
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public String[] getStructureFiles()
      {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String superposeStructures(AlignmentI[] alignments,
              int[] structureIndices, HiddenColumns[] hiddenCols)
      {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public void setJalviewColourScheme(ColourSchemeI cs)
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void setBackgroundColour(Color col)
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void highlightAtoms(List<AtomSpec> atoms)
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public SequenceRenderer getSequenceRenderer(AlignmentViewPanel alignment)
      {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public FeatureRenderer getFeatureRenderer(AlignmentViewPanel alignment)
      {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      protected StructureMappingcommandSet[] getColourBySequenceCommands(
              String[] files, SequenceRenderer sr, AlignmentViewPanel avp)
      {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public List<String> getChainNames()
      {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      protected void colourBySequence(
              StructureMappingcommandSet[] colourBySequenceCommands)
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void colourByCharge()
      {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void colourByChain()
      {
        // TODO Auto-generated method stub
        
      }
    };
    String[][] chains = binder.getChains();
    assertFalse(chains == null || chains[0] == null,
            "No chains discovered by binding");
    assertEquals(2, chains[0].length);
    assertEquals("A", chains[0][0]);
    assertEquals("B", chains[0][1]);
  }
  AAStructureBindingModel testee;

  AlignmentI al = null;

  /**
   * Set up test conditions with three aligned sequences,
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    SequenceI seq1a = new Sequence("1YCS|A", "-VPSQK");
    SequenceI seq1b = new Sequence("1YCS|B", "EIVKF-");
    SequenceI seq2 = new Sequence("3A6S", "MK-KLQ");
    SequenceI seq3 = new Sequence("1OOT", "SPK-AV");
    al = new Alignment(new SequenceI[] { seq1a, seq1b, seq2, seq3 });
    al.setDataset(null);

    /*
     * give pdb files the name generated by Jalview for PASTE source
     */
    PDBEntry[] pdbFiles = new PDBEntry[3];
    pdbFiles[0] = new PDBEntry("1YCS", "A", Type.PDB, "INLINE1YCS");
    pdbFiles[1] = new PDBEntry("3A6S", "B", Type.PDB, "INLINE3A6S");
    pdbFiles[2] = new PDBEntry("1OOT", "A", Type.PDB, "INLINE1OOT");
    SequenceI[][] seqs = new SequenceI[3][];
    seqs[0] = new SequenceI[] { seq1a, seq1b };
    seqs[1] = new SequenceI[] { seq2 };
    seqs[2] = new SequenceI[] { seq3 };
    StructureSelectionManager ssm = new StructureSelectionManager();

    ssm.setMapping(new SequenceI[] { seq1a, seq1b }, null, PDB_1,
            DataSourceType.PASTE, null);
    ssm.setMapping(new SequenceI[] { seq2 }, null, PDB_2,
            DataSourceType.PASTE, null);
    ssm.setMapping(new SequenceI[] { seq3 }, null, PDB_3,
            DataSourceType.PASTE, null);

    testee = new AAStructureBindingModel(ssm, pdbFiles, seqs, null)
    {
      @Override
      public String[] getStructureFiles()
      {
        return new String[] { "INLINE1YCS", "INLINE3A6S", "INLINE1OOT" };
      }

      @Override
      public void updateColours(Object source)
      {
      }

      @Override
      public void releaseReferences(Object svl)
      {
      }

      @Override
      public void highlightAtoms(List<AtomSpec> atoms)
      {
      }

      @Override
      public List<String> getChainNames()
      {
        return null;
      }

      @Override
      public void setJalviewColourScheme(ColourSchemeI cs)
      {
      }

      @Override
      public String superposeStructures(AlignmentI[] als, int[] alm,
              HiddenColumns[] alc)
      {
        return null;
      }

      @Override
      public void setBackgroundColour(Color col)
      {
      }

      @Override
      protected StructureMappingcommandSet[] getColourBySequenceCommands(
              String[] files, SequenceRenderer sr, AlignmentViewPanel avp)
      {
        return null;
      }

      @Override
      public SequenceRenderer getSequenceRenderer(
              AlignmentViewPanel alignment)
      {
        return null;
      }

      @Override
      protected void colourBySequence(
              StructureMappingcommandSet[] colourBySequenceCommands)
      {
      }

      @Override
      public void colourByChain()
      {
      }

      @Override
      public void colourByCharge()
      {
      }

      @Override
      public FeatureRenderer getFeatureRenderer(
              AlignmentViewPanel alignment)
      {
        return null;
      }
    };
  }

  /**
   * Verify that the method determines that columns 2, 5 and 6 of the alignment
   * are alignable in structure
   */
  @Test(groups = { "Functional" })
  public void testFindSuperposableResidues()
  {
    /*
     * create a data bean to hold data per structure file
     */
    SuperposeData[] structs = new SuperposeData[testee.getStructureFiles().length];
    for (int i = 0; i < structs.length; i++)
    {
      structs[i] = testee.new SuperposeData(al.getWidth());
    }
    /*
     * initialise BitSet of 'superposable columns' to true (would be false for
     * hidden columns)
     */
    BitSet matched = new BitSet();
    for (int i = 0; i < al.getWidth(); i++)
    {
      matched.set(i);
    }

    int refStructure = testee
            .findSuperposableResidues(al, matched, structs);

    assertEquals(0, refStructure);

    /*
     * only ungapped, structure-mapped columns are superposable
     */
    assertFalse(matched.get(0)); // gap in first sequence
    assertTrue(matched.get(1));
    assertFalse(matched.get(2)); // gap in third sequence
    assertFalse(matched.get(3)); // gap in fourth sequence
    assertTrue(matched.get(4));
    assertTrue(matched.get(5)); // gap in second sequence

    assertEquals("1YCS", structs[0].pdbId);
    assertEquals("3A6S", structs[1].pdbId);
    assertEquals("1OOT", structs[2].pdbId);
    assertEquals("A", structs[0].chain); // ? struct has chains A _and_ B
    assertEquals("B", structs[1].chain);
    assertEquals("A", structs[2].chain);
    // the 0's for unsuperposable positions propagate down the columns:
    assertEquals("[0, 97, 98, 99, 100, 102]",
            Arrays.toString(structs[0].pdbResNo));
    assertEquals("[0, 2, 0, 3, 4, 5]", Arrays.toString(structs[1].pdbResNo));
    assertEquals("[0, 8, 0, 0, 10, 12]",
            Arrays.toString(structs[2].pdbResNo));
  }

  @Test(groups = { "Functional" })
  public void testFindSuperposableResidues_hiddenColumn()
  {
    SuperposeData[] structs = new SuperposeData[al.getHeight()];
    for (int i = 0; i < structs.length; i++)
    {
      structs[i] = testee.new SuperposeData(al.getWidth());
    }
    /*
     * initialise BitSet of 'superposable columns' to true (would be false for
     * hidden columns)
     */
    BitSet matched = new BitSet();
    for (int i = 0; i < al.getWidth(); i++)
    {
      matched.set(i);
    }

    // treat column 5 of the alignment as hidden
    matched.clear(4);

    int refStructure = testee
            .findSuperposableResidues(al, matched, structs);

    assertEquals(0, refStructure);

    // only ungapped, structure-mapped columns are not superposable
    assertFalse(matched.get(0));
    assertTrue(matched.get(1));
    assertFalse(matched.get(2));
    assertFalse(matched.get(3));
    assertFalse(matched.get(4)); // superposable, but hidden, column
    assertTrue(matched.get(5));
  }
}
