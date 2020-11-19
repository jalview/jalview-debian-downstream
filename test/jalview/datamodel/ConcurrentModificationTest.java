package jalview.datamodel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Not a test of code specific to Jalview, but some tests to verify Java
 * behaviour under certain scenarios of concurrent modification of iterated
 * lists or arrays
 */
public class ConcurrentModificationTest
{
  static int MAX = 10;

  int[] intArray;

  List<Integer> intList;

  /**
   * Setup: populate array and list with values 0,...,9
   */
  @BeforeMethod()
  public void setUp()
  {
    intArray = new int[MAX];
    intList = new ArrayList<Integer>();
    for (int i = 0; i < MAX; i++)
    {
      intArray[i] = i;
      intList.add(i);
    }
  }

  /**
   * Sanity check of values if no 'interference'
   */
  @Test
  public void test_nullCase()
  {
    /*
     * array iteration
     */
    int j = 0;
    for (int i : intArray)
    {
      assertEquals(i, j);
      j++;
    }

    /*
     * list iteration
     */
    j = 0;
    for (int i : intList)
    {
      assertEquals(i, j);
      j++;
    }
  }

  /**
   * Test for the case where the array is reallocated and enlarged during the
   * iteration. The for loop iteration is not affected.
   */
  @Test
  public void testEnhancedForLoop_arrayExtended()
  {
    int j = 0;
    for (int i : intArray)
    {
      if (j == 5)
      {
        intArray = new int[MAX + 1];
      }
      assertEquals(i, j);
      j++;
    }
    assertEquals(j, MAX);
  }

  /**
   * Test for the case where the array is nulled during the iteration. The for
   * loop iteration is not affected.
   */
  @Test
  public void testEnhancedForLoop_arrayNulled()
  {
    int j = 0;
    for (int i : intArray)
    {
      if (j == 5)
      {
        intArray = null;
      }
      assertEquals(i, j);
      j++;
    }
    assertEquals(j, MAX);
  }

  /**
   * Test for the case where a value is changed before the iteration reaches it.
   * The iteration reads the new value.
   * <p>
   * This is analagous to Jalview's consensus thread modifying entries in the
   * AlignmentAnnotation.annotations array of Annotation[] while it is being
   * read.
   */
  @Test
  public void testEnhancedForLoop_arrayModified()
  {
    int j = 0;
    for (int i : intArray)
    {
      if (j == 5)
      {
        intArray[5] = -1;
        intArray[6] = -2;
      }
      /*
       * the value 'just read' by the for loop is not affected;
       * the next value read is affected
       */
      int expected = j == 6 ? -2 : j;
      assertEquals(i, expected);
      j++;
    }
    assertEquals(j, MAX);
  }

  /**
   * Test for the case where a list entry is added during the iteration.
   */
  @Test
  public void testEnhancedForLoop_listExtended()
  {
    int j = 0;
    try
    {
      for (int i : intList)
      {
        if (j == 5)
        {
          intList.add(MAX + 1);
        }
        assertEquals(i, j);
        j++;
      }
    } catch (ConcurrentModificationException e)
    {
      /*
       * exception occurs on next loop iteration after 'concurrent'
       * modification
       */
      assertEquals(j, 6);
      return;
    }
    fail("Expected exception");
  }

  /**
   * Test for the case where a list entry is modified during the iteration. No
   * exception occurs.
   */
  @Test
  public void testEnhancedForLoop_listModified()
  {
    int j = 0;
    for (int i : intList)
    {
      if (j == 5)
      {
        intList.set(5, -1);
        intList.set(6, -2);
      }

      /*
       * the value 'just read' is not affected, the next value
       * is read as modified, no exception
       */
      int expected = j == 6 ? -2 : j;
      assertEquals(i, expected);
      j++;
    }
    assertEquals(j, MAX);
  }

  /**
   * Test for the case where the list is recreated during the iteration.
   */
  @Test
  public void testEnhancedForLoop_listRenewed()
  {
    Object theList = intList;
    int j = 0;
    for (int i : intList)
    {
      if (j == 5)
      {
        /*
         * recreate a new List object
         */
        setUp();
        assertNotSame(theList, intList);
      }
      assertEquals(i, j);
      j++;
    }

    /*
     * no exception in the for loop; changing the object intList refers to
     * does not affect the loop's iteration over the original object
     */
    assertEquals(j, MAX);
  }
}
