package jalview.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.util.MapList;

public class EmblFlatFileTest
{
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Cache.initLogger();
  }

  /**
   * A fairly tough test, using J03321 (circular DNA), which has 8 CDS features,
   * one of them reverse strand
   * 
   * @throws MalformedURLException
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testParse() throws MalformedURLException, IOException
  {
    File dataFile = new File("test/jalview/io/J03321.embl.txt");
    FileParse fp = new FileParse(dataFile.getAbsolutePath(), DataSourceType.FILE);
    EmblFlatFile parser = new EmblFlatFile(fp, "EmblTest");
    parser.parse();
    List<SequenceI> seqs = parser.getSeqs();

    assertEquals(seqs.size(), 1);
    SequenceI seq = seqs.get(0);
    assertEquals(seq.getName(), "EmblTest|J03321");
    assertEquals(seq.getLength(), 7502);
    assertEquals(seq.getDescription(),
            "Chlamydia trachomatis plasmid pCHL1, complete sequence");

    /*
     * should be 9 CDS features (one is a 'join' of two exons)
     */
    Set<String> featureTypes = seq.getFeatures().getFeatureTypes();
    assertEquals(featureTypes.size(), 1);
    assertTrue(featureTypes.contains("CDS"));

    /*
     * inspect some features (sorted just for convenience of test assertions)
     */
    List<SequenceFeature> features = seq.getFeatures()
            .getAllFeatures("CDS");
    SequenceFeatures.sortFeatures(features, true);
    assertEquals(features.size(), 9);

    SequenceFeature sf = features.get(0);
    assertEquals(sf.getBegin(), 1);
    assertEquals(sf.getEnd(), 437);
    assertEquals(sf.getDescription(),
            "Exon 2 for protein EMBLCDS:AAA91567.1");
    assertEquals(sf.getFeatureGroup(), "EmblTest");
    assertEquals(sf.getEnaLocation(), "join(7022..7502,1..437)");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), 1);
    assertEquals(sf.getValue("note"), "pGP7-D");
    // this is the second exon of circular CDS!
    assertEquals(sf.getValue("exon number"), 2);
    assertEquals(sf.getValue("product"), "hypothetical protein");
    assertEquals(sf.getValue("transl_table"), "11");

    sf = features.get(1);
    assertEquals(sf.getBegin(), 488);
    assertEquals(sf.getEnd(), 1480);
    assertEquals(sf.getDescription(),
            "Exon 1 for protein EMBLCDS:AAA91568.1");
    assertEquals(sf.getFeatureGroup(), "EmblTest");
    assertEquals(sf.getEnaLocation(), "complement(488..1480)");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), -1); // reverse strand!
    assertEquals(sf.getValue("note"), "pGP8-D");
    assertEquals(sf.getValue("exon number"), 1);
    assertEquals(sf.getValue("product"), "hypothetical protein");

    sf = features.get(7);
    assertEquals(sf.getBegin(), 6045);
    assertEquals(sf.getEnd(), 6788);
    assertEquals(sf.getDescription(),
            "Exon 1 for protein EMBLCDS:AAA91574.1");
    assertEquals(sf.getFeatureGroup(), "EmblTest");
    assertEquals(sf.getEnaLocation(), "6045..6788");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), 1);
    assertEquals(sf.getValue("note"), "pGP6-D (gtg start codon)");
    assertEquals(sf.getValue("exon number"), 1);
    assertEquals(sf.getValue("product"), "hypothetical protein");

    /*
     * CDS at 7022-7502 is the first exon of the circular CDS
     */
    sf = features.get(8);
    assertEquals(sf.getBegin(), 7022);
    assertEquals(sf.getEnd(), 7502);
    assertEquals(sf.getDescription(),
            "Exon 1 for protein EMBLCDS:AAA91567.1");
    assertEquals(sf.getFeatureGroup(), "EmblTest");
    assertEquals(sf.getEnaLocation(), "join(7022..7502,1..437)");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), 1);
    assertEquals(sf.getValue("note"), "pGP7-D");
    assertEquals(sf.getValue("exon number"), 1);
    assertEquals(sf.getValue("product"), "hypothetical protein");

    /*
     * Verify DBRefs, whether declared in the file or added by Jalview.
     * There are 4 'direct' (DR) dbrefs, and numerous CDS /db_xref entries 
     * (some e.g. INTERPRO are duplicates). Jalview adds a dbref to 'self'.
     * Sample a few here. Note DBRefEntry constructor capitalises source.
     */
    List<DBRefEntry> dbrefs = Arrays.asList(seq.getDBRefs());

    assertEquals(dbrefs.size(), 32);
    // xref to 'self':
    DBRefEntry selfRef = new DBRefEntry("EMBLTEST", "1", "J03321");
    int[] range = new int[] { 1, seq.getLength() };
    selfRef.setMap(new Mapping(null, range, range, 1, 1));
    assertTrue(dbrefs.contains(selfRef));

    // 1st DR line; note trailing period is removed
    assertTrue(dbrefs.contains(new DBRefEntry("MD5", "0",
            "d4c4942a634e3df4995fd5ac75c26a61")));
    // the 4th DR line:
    assertTrue(
            dbrefs.contains(new DBRefEntry("EUROPEPMC", "0", "PMC87941")));
    // from the first CDS feature
    assertTrue(dbrefs.contains(new DBRefEntry("GOA", "0", "P0CE19")));
    // from the last CDS feature
    assertTrue(
            dbrefs.contains(new DBRefEntry("INTERPRO", "0", "IPR005350")));

    /*
     * verify mappings to, and sequences for, UNIPROT proteins
     */
    int uniprotCount = 0;
    List<int[]> ranges;
    for (DBRefEntry dbref : dbrefs)
    {
      if ("UNIPROT".equals(dbref.getSource()))
      {
        uniprotCount++;
        Mapping mapping = dbref.getMap();
        assertNotNull(mapping);
        MapList map = mapping.getMap();
        String mappedToName = mapping.getTo().getName();
        if ("UNIPROT|P0CE16".equals(mappedToName))
        {
          assertEquals((ranges = map.getFromRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1579);
          assertEquals(ranges.get(0)[1], 2931);
          assertEquals((ranges = map.getToRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1);
          assertEquals(ranges.get(0)[1], 451);
          // CDS /product carries over as protein product description
          assertEquals(mapping.getTo().getDescription(),
                  "hypothetical protein");
        }
        else if ("UNIPROT|P0CE17".equals(mappedToName))
        {
          assertEquals((ranges = map.getFromRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 2928);
          assertEquals(ranges.get(0)[1], 3989);
          assertEquals((ranges = map.getToRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1);
          assertEquals(ranges.get(0)[1], 354);
        }
        else if ("UNIPROT|P0CE18".equals(mappedToName))
        {
          assertEquals((ranges = map.getFromRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 4054);
          assertEquals(ranges.get(0)[1], 4845);
          assertEquals((ranges = map.getToRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1);
          assertEquals(ranges.get(0)[1], 264);
        }
        else if ("UNIPROT|P0CE19".equals(mappedToName))
        {
          // join(7022..7502,1..437)
          assertEquals((ranges = map.getFromRanges()).size(), 2);
          assertEquals(ranges.get(0)[0], 7022);
          assertEquals(ranges.get(0)[1], 7502);
          assertEquals(ranges.get(1)[0], 1);
          assertEquals(ranges.get(1)[1], 434);
          assertEquals((ranges = map.getToRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1);
          assertEquals(ranges.get(0)[1], 305);
        }
        else if ("UNIPROT|P0CE20".equals(mappedToName))
        {
          // complement(488..1480)
          assertEquals((ranges = map.getFromRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1480);
          assertEquals(ranges.get(0)[1], 491);
          assertEquals((ranges = map.getToRanges()).size(), 1);
          assertEquals(ranges.get(0)[0], 1);
          assertEquals(ranges.get(0)[1], 330);
        }
        else if (!"UNIPROT|P0CE23".equals(mappedToName)
                && !"UNIPROT|P10559".equals(mappedToName)
                && !"UNIPROT|P10560".equals(mappedToName))
        {
          fail("Unexpected UNIPROT dbref to " + mappedToName);
        }
      }
    }
    assertEquals(uniprotCount, 8);
  }

  @Test(groups = "Functional")
  public void testParse_codonStartNot1()
  {
    // TODO verify CDS-to-protein mapping for CDS with /codon_start=2
    // example: https://www.ebi.ac.uk/ena/browser/api/embl/EU498516
  }

  /**
   * Test for the case that the EMBL CDS has no UNIPROT xref. In this case
   * Jalview should synthesize an xref to EMBLCDSPROTEIN in the hope this will
   * allow Get Cross-References.
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testParse_noUniprotXref() throws IOException
  {
    // MN908947 cut down to 40BP, one CDS, length 5 peptide for test purposes
    // plus an additional (invented) test case:
    // - multi-line /product qualifier including escaped quotes
    String data = "ID   MN908947; SV 3; linear; genomic RNA; STD; VRL; 20 BP.\n"
            + "DE   Severe acute respiratory syndrome coronavirus 2 isolate Wuhan-Hu-1,\n"
            + "FT   CDS             3..17\n"
            + "FT                   /protein_id=\"QHD43415.1\"\n"
            + "FT                   /product=\"orf1ab polyprotein\n"
            + "FT                   \"\"foobar\"\" \"\n"
            + "FT                   /translation=\"MRKLD\n"
            + "SQ   Sequence 7496 BP; 2450 A; 1290 C; 1434 G; 2322 T; 0 other;\n"
            + "     ggatGcgtaa gttagacgaa attttgtctt tgcgcacaga        40\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    EmblFlatFile parser = new EmblFlatFile(fp, "EmblTest");
    parser.parse();
    List<SequenceI> seqs = parser.getSeqs();
    assertEquals(seqs.size(), 1);
    SequenceI seq = seqs.get(0);
    List<DBRefEntry> dbrefs = Arrays.asList(seq.getDBRefs());

    /*
     * dna should have dbref to itself, and to inferred EMBLCDSPROTEIN:QHD43415.1
     */
    assertEquals(dbrefs.size(), 2);
    
    // dbref to self
    DBRefEntry dbref = dbrefs.get(0);
    assertEquals(dbref.getSource(), "EMBLTEST");
    assertEquals(dbref.getAccessionId(), "MN908947");
    Mapping mapping = dbref.getMap();
    assertNull(mapping.getTo());
    MapList map = mapping.getMap();
    assertEquals(map.getFromLowest(), 1);
    assertEquals(map.getFromHighest(), 40);
    assertEquals(map.getToLowest(), 1);
    assertEquals(map.getToHighest(), 40);
    assertEquals(map.getFromRatio(), 1);
    assertEquals(map.getToRatio(), 1);
    
    // dbref to inferred EMBLCDSPROTEIN:
    dbref = dbrefs.get(1);
    assertEquals(dbref.getSource(), "EMBLCDSPROTEIN");
    assertEquals(dbref.getAccessionId(), "QHD43415.1");
    mapping = dbref.getMap();
    SequenceI mapTo = mapping.getTo();
    assertEquals(mapTo.getName(), "QHD43415.1");
    // the /product qualifier transfers to protein product description
    assertEquals(mapTo.getDescription(), "orf1ab polyprotein \"foobar\"");
    assertEquals(mapTo.getSequenceAsString(), "MRKLD");
    map = mapping.getMap();
    assertEquals(map.getFromLowest(), 3);
    assertEquals(map.getFromHighest(), 17);
    assertEquals(map.getToLowest(), 1);
    assertEquals(map.getToHighest(), 5);
    assertEquals(map.getFromRatio(), 3);
    assertEquals(map.getToRatio(), 1);
  }

  @Test(groups = "Functional")
  public void testAdjustForProteinLength()
  {
    int[] exons = new int[] { 11, 15, 21, 25, 31, 38 }; // 18 bp

    int[] exons_nostop = new int[] { 11, 15, 21, 25, 31, 35 }; // 15 bp
    // exact length match:
    assertSame(exons, EmblFlatFile.adjustForProteinLength(6, exons));

    // trimmed if we assume exons include stop codon not in protein:
    assertEquals(Arrays.toString(exons_nostop), Arrays.toString(EmblFlatFile.adjustForProteinLength(5, exons)));

    // truncate last exon by 6bp
    int[] truncated = EmblFlatFile.adjustForProteinLength(4, exons);
    assertEquals("[11, 15, 21, 25, 31, 32]", Arrays.toString(truncated));

    // remove last exon and truncate preceding by 1bp (so 3bp in total)
    truncated = EmblFlatFile.adjustForProteinLength(3, exons);
    assertEquals("[11, 15, 21, 24]", Arrays.toString(truncated));

    // exact removal of exon case:
    exons = new int[] { 11, 15, 21, 27, 33, 38 }; // 18 bp
    truncated = EmblFlatFile.adjustForProteinLength(4, exons);
    assertEquals("[11, 15, 21, 27]", Arrays.toString(truncated));

    // what if exons are too short for protein?
    truncated = EmblFlatFile.adjustForProteinLength(7, exons);
    assertSame(exons, truncated);
  }

  @Test(groups = "Functional")
  public void testRemoveQuotes()
  {
    assertNull(EmblFlatFile.removeQuotes(null));
    assertEquals(EmblFlatFile.removeQuotes("No quotes here"), "No quotes here");
    assertEquals(EmblFlatFile.removeQuotes("\"Enclosing quotes\""), "Enclosing quotes");
    assertEquals(EmblFlatFile.removeQuotes("\"Escaped \"\"quotes\"\" example\""), "Escaped \"quotes\" example");
  }
}
