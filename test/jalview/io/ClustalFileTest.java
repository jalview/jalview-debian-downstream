package jalview.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.SequenceI;

import java.io.IOException;

import org.testng.annotations.Test;

public class ClustalFileTest
{
  @Test(groups="Functional")
  public void testParse_withNumbering() throws IOException
  {
    //@formatter:off
    String data = "CLUSTAL\n\n"
            + "FER_CAPAA/1-8      -----------------------------------------------------------A\t1\n"
            + "FER_CAPAN/1-55     MA------SVSATMISTSFMPRKPAVTSL-KPIPNVGE--ALFGLKS-A--NGGKVTCMA 48\n"
            + "FER1_SOLLC/1-55    MA------SISGTMISTSFLPRKPAVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA 48\n"
            + "Q93XJ9_SOLTU/1-55  MA------SISGTMISTSFLPRKPVVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA 48\n"
            + "FER1_PEA/1-60      MATT---PALYGTAVSTSFLRTQPMPMSV-TTTKAFSN--GFLGLKT-SLKRGDLAVAMA 53\n\n"
            + "FER_CAPAA/1-8      SYKVKLI 8\n"
            + "FER_CAPAN/1-55     SYKVKLI 55\n"
            + "FER1_SOLLC/1-55    SYKVKLI 55\n"
            + "Q93XJ9_SOLTU/1-55  SYKVKLI 55\n"
            + "FER1_PEA/1-60      SYKVKLV 60\n"
            + "                   .*     .:....*******..** ..........**  ********...*:::*  ...\n"
            + "\t\t.:.::.  *\n";
    //@formatter:on
    ClustalFile cf = new ClustalFile(data, DataSourceType.PASTE);
    cf.parse();
    SequenceI[] seqs = cf.getSeqsAsArray();
    assertEquals(seqs.length, 5);
    assertEquals(seqs[0].getName(), "FER_CAPAA");
    assertEquals(seqs[0].getStart(), 1);
    assertEquals(seqs[0].getEnd(), 8);
    assertTrue(seqs[0].getSequenceAsString().endsWith("ASYKVKLI"));
  }

  @Test(groups="Functional")
  public void testParse_noNumbering() throws IOException
  {
    //@formatter:off
    String data = "CLUSTAL\n\n"
            + "FER_CAPAA/1-8      -----------------------------------------------------------A\n"
            + "FER_CAPAN/1-55     MA------SVSATMISTSFMPRKPAVTSL-KPIPNVGE--ALFGLKS-A--NGGKVTCMA\n"
            + "FER1_SOLLC/1-55    MA------SISGTMISTSFLPRKPAVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA\n"
            + "Q93XJ9_SOLTU/1-55  MA------SISGTMISTSFLPRKPVVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA\n"
            + "FER1_PEA/1-60      MATT---PALYGTAVSTSFLRTQPMPMSV-TTTKAFSN--GFLGLKT-SLKRGDLAVAMA\n\n"
            + "FER_CAPAA/1-8      SYKVKLI\n"
            + "FER_CAPAN/1-55     SYKVKLI\n"
            + "FER1_SOLLC/1-55    SYKVKLI\n"
            + "Q93XJ9_SOLTU/1-55  SYKVKLI\n"
            + "FER1_PEA/1-60      SYKVKLV\n";
    //@formatter:on
    ClustalFile cf = new ClustalFile(data, DataSourceType.PASTE);
    cf.parse();
    SequenceI[] seqs = cf.getSeqsAsArray();
    assertEquals(seqs.length, 5);
    assertEquals(seqs[0].getName(), "FER_CAPAA");
    assertEquals(seqs[0].getStart(), 1);
    assertEquals(seqs[0].getEnd(), 8);
    assertTrue(seqs[0].getSequenceAsString().endsWith("ASYKVKLI"));
  }
}
