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
package jalview.gui;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.jbgui.GStructureChooser.FilterOption;

import java.util.Collection;
import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import junit.extensions.PA;

public class StructureChooserTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Sequence seq;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    seq = new Sequence("PDB|4kqy|4KQY|A", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1,
            26);
    seq.createDatasetSequence();
    for (int x = 1; x < 5; x++)
    {
      DBRefEntry dbRef = new DBRefEntry();
      dbRef.setAccessionId("XYZ_" + x);
      seq.addDBRef(dbRef);
    }

    PDBEntry dbRef = new PDBEntry();
    dbRef.setId("1tim");

    Vector<PDBEntry> pdbIds = new Vector<>();
    pdbIds.add(dbRef);

    seq.setPDBId(pdbIds);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
    seq = null;
  }

  @Test(groups = { "Functional" })
  public void buildQueryTest()
  {
    String query = StructureChooser.buildQuery(seq);
    assertEquals("pdb_id:1tim", query);
    System.out.println("seq >>>> " + seq);
    seq.getAllPDBEntries().clear();
    query = StructureChooser.buildQuery(seq);
    assertEquals(
            "text:XYZ_1 OR text:XYZ_2 OR text:XYZ_3 OR text:XYZ_4 OR text:4kqy",
            query);
    seq.setDBRefs(null);
    query = StructureChooser.buildQuery(seq);
    assertEquals("text:4kqy", query);

    DBRefEntry uniprotDBRef = new DBRefEntry();
    uniprotDBRef.setAccessionId("P12345");
    uniprotDBRef.setSource(DBRefSource.UNIPROT);
    seq.addDBRef(uniprotDBRef);

    DBRefEntry pdbDBRef = new DBRefEntry();
    pdbDBRef.setAccessionId("1XYZ");
    pdbDBRef.setSource(DBRefSource.PDB);
    seq.addDBRef(pdbDBRef);

    for (int x = 1; x < 5; x++)
    {
      DBRefEntry dbRef = new DBRefEntry();
      dbRef.setAccessionId("XYZ_" + x);
      seq.addDBRef(dbRef);
    }
    query = StructureChooser.buildQuery(seq);
    assertEquals(
            "uniprot_accession:P12345 OR uniprot_id:P12345 OR pdb_id:1xyz",
            query);
  }

  @Test(groups = { "Functional" })
  public void populateFilterComboBoxTest() throws InterruptedException
  {
    SequenceI[] selectedSeqs = new SequenceI[] { seq };
    StructureChooser sc = new StructureChooser(selectedSeqs, seq, null);
    sc.populateFilterComboBox(false, false);
    int optionsSize = sc.getCmbFilterOption().getItemCount();
    assertEquals(2, optionsSize); // if structures are not discovered then don't
                                  // populate filter options

    sc.populateFilterComboBox(true, false);
    optionsSize = sc.getCmbFilterOption().getItemCount();
    assertTrue(optionsSize > 3); // if structures are found, filter options
                                 // should be populated

    sc.populateFilterComboBox(true, true);
    assertTrue(sc.getCmbFilterOption().getSelectedItem() != null);
    FilterOption filterOpt = (FilterOption) sc.getCmbFilterOption()
            .getSelectedItem();
    assertEquals("Cached Structures", filterOpt.getName());
  }

  @Test(groups = { "Network" })
  public void fetchStructuresInfoTest()
  {
    SequenceI[] selectedSeqs = new SequenceI[] { seq };
    StructureChooser sc = new StructureChooser(selectedSeqs, seq, null);
    sc.fetchStructuresMetaData();
    Collection<FTSData> ss = (Collection<FTSData>) PA.getValue(sc,
            "discoveredStructuresSet");
    assertNotNull(ss);
    assertTrue(ss.size() > 0);

  }

  @Test(groups = { "Functional" })
  public void sanitizeSeqNameTest()
  {
    String name = "ab_cdEF|fwxyz012349";
    assertEquals(name, StructureChooser.sanitizeSeqName(name));

    // remove a [nn] substring
    name = "abcde12[345]fg";
    assertEquals("abcde12fg", StructureChooser.sanitizeSeqName(name));

    // remove characters other than a-zA-Z0-9 | or _
    name = "ab[cd],.\t£$*!- \\\"@:e";
    assertEquals("abcde", StructureChooser.sanitizeSeqName(name));

    name = "abcde12[345a]fg";
    assertEquals("abcde12345afg", StructureChooser.sanitizeSeqName(name));
  }
}
