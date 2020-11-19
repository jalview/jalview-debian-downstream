package jalview.io.gff;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class SequenceOntologyLiteTest
{
  @Test(groups = "Functional")
  public void testIsA_sequenceVariant()
  {
    SequenceOntologyI so = new SequenceOntologyLite();

    assertFalse(so.isA("CDS", "sequence_variant"));
    assertTrue(so.isA("sequence_variant", "sequence_variant"));

    /*
     * these should all be sub-types of sequence_variant
     */
    assertTrue(so.isA("structural_variant", "sequence_variant"));
    assertTrue(so.isA("feature_variant", "sequence_variant"));
    assertTrue(so.isA("gene_variant", "sequence_variant"));
    assertTrue(so.isA("transcript_variant", "sequence_variant"));
    assertTrue(so.isA("NMD_transcript_variant", "sequence_variant"));
    assertTrue(so.isA("missense_variant", "sequence_variant"));
    assertTrue(so.isA("synonymous_variant", "sequence_variant"));
    assertTrue(so.isA("frameshift_variant", "sequence_variant"));
    assertTrue(so.isA("5_prime_UTR_variant", "sequence_variant"));
    assertTrue(so.isA("3_prime_UTR_variant", "sequence_variant"));
    assertTrue(so.isA("stop_gained", "sequence_variant"));
    assertTrue(so.isA("stop_lost", "sequence_variant"));
    assertTrue(so.isA("inframe_deletion", "sequence_variant"));
    assertTrue(so.isA("inframe_insertion", "sequence_variant"));
    assertTrue(so.isA("splice_region_variant", "sequence_variant"));
  }
}
