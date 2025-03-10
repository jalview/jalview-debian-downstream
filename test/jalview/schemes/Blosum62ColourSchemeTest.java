package jalview.schemes;

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class Blosum62ColourSchemeTest
{
  /**
   * Test the method that determines colour as:
   * <ul>
   * <li>white if there is no consensus</li>
   * <li>white if 'residue' is a gap</li>
   * <li>dark blue if residue matches consensus (or joint consensus)</li>
   * <li>else, total the residue's Blosum score with the consensus residue(s)</li>
   * <ul>
   * <li>if positive, light blue, else white</li>
   * </ul>
   * <ul>
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ColourSchemeI blosum = new Blosum62ColourScheme();
    Color lightBlue = new Color(204, 204, 255);
    Color darkBlue = new Color(154, 154, 255);

    /*
     * findColour does not use column, sequence or pid score
     * we assume consensus residue is computed as upper case
     */
    assertEquals(blosum.findColour('A', 0, null, "A", 0f), darkBlue);
    assertEquals(blosum.findColour('a', 0, null, "A", 0f), darkBlue);

    /*
     * L has a Blosum score of 
     * -1 with A
     * -4 with B
     * 0 with F
     * 2 with I
     * -1 with T
     * 1 with V
     * etc
     */
    assertEquals(blosum.findColour('L', 0, null, "A", 0f), Color.white); // -1
    assertEquals(blosum.findColour('L', 0, null, "B", 0f), Color.white); // -4
    assertEquals(blosum.findColour('L', 0, null, "F", 0f), Color.white); // 0
    assertEquals(blosum.findColour('L', 0, null, "I", 0f), lightBlue); // 2
    assertEquals(blosum.findColour('L', 0, null, "TV", 0f), Color.white); // 0
    assertEquals(blosum.findColour('L', 0, null, "IV", 0f), lightBlue); // 3
    assertEquals(blosum.findColour('L', 0, null, "IT", 0f), lightBlue); // 1
    assertEquals(blosum.findColour('L', 0, null, "IAT", 0f), Color.white); // 0
  }
}
