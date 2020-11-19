package jalview.renderer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.renderer.ScaleRenderer.ScaleMark;

import java.util.List;

import org.testng.annotations.Test;

public class ScaleRendererTest
{
  @Test(groups = "Functional")
  public void testCalculateMarks()
  {
    String data = ">Seq/20-45\nABCDEFGHIJKLMNOPQRSTUVWXYS\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(data,
            DataSourceType.PASTE);
    AlignViewport av = af.getViewport();

    /*
     * scale has minor ticks at 5, 15, 25, major at 10 and 20
     * (these are base 1, ScaleMark holds base 0 values)
     */
    List<ScaleMark> marks = new ScaleRenderer().calculateMarks(av, 0, 25);
    assertEquals(marks.size(), 5);

    assertFalse(marks.get(0).major);
    assertEquals(marks.get(0).column, 4);
    assertNull(marks.get(0).text);

    assertTrue(marks.get(1).major);
    assertEquals(marks.get(1).column, 9);
    assertEquals(marks.get(1).text, "10");

    assertFalse(marks.get(2).major);
    assertEquals(marks.get(2).column, 14);
    assertNull(marks.get(2).text);

    assertTrue(marks.get(3).major);
    assertEquals(marks.get(3).column, 19);
    assertEquals(marks.get(3).text, "20");

    assertFalse(marks.get(4).major);
    assertEquals(marks.get(4).column, 24);
    assertNull(marks.get(4).text);

    /*
     * now hide columns 9-11 and 18-20 (base 1)
     * scale marks are now in the same columns as before, but
     * with column numbering adjusted for hidden columns
     */
    av.hideColumns(8, 10);
    av.hideColumns(17, 19);
    marks = new ScaleRenderer().calculateMarks(av, 0, 25);
    assertEquals(marks.size(), 5);
    assertFalse(marks.get(0).major);
    assertEquals(marks.get(0).column, 4);
    assertNull(marks.get(0).text);
    assertTrue(marks.get(1).major);
    assertEquals(marks.get(1).column, 9);
    assertEquals(marks.get(1).text, "13"); // +3 hidden columns
    assertFalse(marks.get(2).major);
    assertEquals(marks.get(2).column, 14);
    assertNull(marks.get(2).text);
    assertTrue(marks.get(3).major);
    assertEquals(marks.get(3).column, 19);
    assertEquals(marks.get(3).text, "26"); // +6 hidden columns
    assertFalse(marks.get(4).major);
    assertEquals(marks.get(4).column, 24);
    assertNull(marks.get(4).text);
  }
}
