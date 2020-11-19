package jalview.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileFormatsTest
{
  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    FileFormats.getInstance().reset();
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    FileFormats.getInstance().reset();
  }

  @Test(groups = "Functional")
  public void testIsIdentifiable()
  {
    FileFormats formats = FileFormats.getInstance();
    assertTrue(formats.isIdentifiable(formats.forName(FileFormat.Fasta
            .getName())));
    assertTrue(formats.isIdentifiable(formats.forName(FileFormat.MMCif
            .getName())));
    assertTrue(formats.isIdentifiable(formats.forName(FileFormat.Jnet
            .getName())));
    assertFalse(formats.isIdentifiable(formats.forName(FileFormat.Jalview
            .getName())));
    assertFalse(formats.isIdentifiable(null));

    /*
     * remove and re-add a format: it is still 'identifiable'
     */
    formats.deregisterFileFormat(FileFormat.Fasta.getName());
    assertNull(formats.forName(FileFormat.Fasta.getName()));
    formats.registerFileFormat(FileFormat.Fasta);
    assertSame(FileFormat.Fasta,
            formats.forName(FileFormat.Fasta.getName()));
    assertTrue(formats.isIdentifiable(FileFormat.Fasta));
  }

  @Test(groups = "Functional")
  public void testGetReadableFormats()
  {
    String expected = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GFF or Jalview features, PDB, mmCIF, Jalview]";
    FileFormats formats = FileFormats.getInstance();
    assertEquals(formats.getReadableFormats().toString(), expected);
  }

  @Test(groups = "Functional")
  public void testGetWritableFormats()
  {
    String expected = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP]";
    FileFormats formats = FileFormats.getInstance();
    assertEquals(formats.getWritableFormats(true).toString(), expected);
    expected = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP, Jalview]";
    assertEquals(formats.getWritableFormats(false).toString(), expected);
  }

  @Test(groups = "Functional")
  public void testDeregisterFileFormat()
  {
    String writable = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP]";
    String readable = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GFF or Jalview features, PDB, mmCIF, Jalview]";
    FileFormats formats = FileFormats.getInstance();
    assertEquals(formats.getWritableFormats(true).toString(), writable);
    assertEquals(formats.getReadableFormats().toString(), readable);

    formats.deregisterFileFormat(FileFormat.Fasta.getName());
    writable = "[PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP]";
    readable = "[PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GFF or Jalview features, PDB, mmCIF, Jalview]";
    assertEquals(formats.getWritableFormats(true).toString(), writable);
    assertEquals(formats.getReadableFormats().toString(), readable);

    /*
     * re-register the format: it gets added to the end of the list
     */
    formats.registerFileFormat(FileFormat.Fasta);
    writable = "[PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP, Fasta]";
    readable = "[PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GFF or Jalview features, PDB, mmCIF, Jalview, Fasta]";
    assertEquals(formats.getWritableFormats(true).toString(), writable);
    assertEquals(formats.getReadableFormats().toString(), readable);
  }

  @Test(groups = "Functional")
  public void testForName()
  {
    FileFormats formats = FileFormats.getInstance();
    for (FileFormatI ff : FileFormat.values())
    {
      assertSame(ff, formats.forName(ff.getName()));
      assertSame(ff, formats.forName(ff.getName().toUpperCase()));
      assertSame(ff, formats.forName(ff.getName().toLowerCase()));
    }
    assertNull(formats.forName(null));
    assertNull(formats.forName("rubbish"));
  }

  @Test(groups = "Functional")
  public void testRegisterFileFormat()
  {
    FileFormats formats = FileFormats.getInstance();
    assertSame(FileFormat.MMCif,
            formats.forName(FileFormat.MMCif.getName()));
    assertTrue(formats.isIdentifiable(FileFormat.MMCif));

    /*
     * deregister mmCIF format
     */
    formats.deregisterFileFormat(FileFormat.MMCif.getName());
    assertNull(formats.forName(FileFormat.MMCif.getName()));

    /*
     * re-register mmCIF format
     * it is reinstated (still 'identifiable')
     */
    formats.registerFileFormat(FileFormat.MMCif);
    assertSame(FileFormat.MMCif,
            formats.forName(FileFormat.MMCif.getName()));
    assertTrue(formats.isIdentifiable(FileFormat.MMCif));
    // repeating does nothing
    formats.registerFileFormat(FileFormat.MMCif);
    assertSame(FileFormat.MMCif,
            formats.forName(FileFormat.MMCif.getName()));
  }

  @Test(groups = "Functional")
  public void testGetFormats()
  {
    /*
     * verify the list of file formats registered matches the enum values
     */
    FileFormats instance = FileFormats.getInstance();
    Iterator<FileFormatI> formats = instance.getFormats()
            .iterator();
    FileFormatI[] builtIn = FileFormat.values();

    for (FileFormatI ff : builtIn)
    {
      assertSame(ff, formats.next());
    }
    assertFalse(formats.hasNext());

    /*
     * remove the first format, check it is no longer in 
     * the list of formats
     */
    String firstFormatName = instance.getFormats().iterator().next()
            .getName();
    instance.deregisterFileFormat(firstFormatName);
    assertNotEquals(instance.getFormats().iterator().next().getName(),
            firstFormatName);
  }
}
