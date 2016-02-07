/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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
package jalview.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A class to perform efficient sorting of arrays of objects based on arrays of
 * scores or other attributes. For example, residues by percentage frequency.
 * 
 * @author gmcarstairs
 *
 */
public class QuickSort
{
  static class FloatComparator implements Comparator<Integer>
  {

    private final float[] values;

    FloatComparator(float[] v)
    {
      values = v;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
      return Float.compare(values[o1], values[o2]);
    }

  }

  static class IntComparator implements Comparator<Integer>
  {

    private final int[] values;

    IntComparator(int[] v)
    {
      values = v;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
      return Integer.compare(values[o1], values[o2]);
    }

  }

  /**
   * Sorts both arrays with respect to ascending order of the items in the first
   * array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(int[] arr, Object[] s)
  {
    sort(arr, 0, arr.length - 1, s);
  }

  /**
   * Sorts both arrays with respect to ascending order of the items in the first
   * array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(float[] arr, Object[] s)
  {
    sort(arr, 0, arr.length - 1, s);
  }

  /**
   * Sorts both arrays with respect to ascending order of the items in the first
   * array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(double[] arr, Object[] s)
  {
    sort(arr, 0, arr.length - 1, s);
  }

  /**
   * Sorts both arrays with respect to descending order of the items in the
   * first array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(String[] arr, Object[] s)
  {
    stringSort(arr, 0, arr.length - 1, s);
  }

  static void stringSort(String[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = stringPartition(arr, p, r, s);
      stringSort(arr, p, q, s);
      stringSort(arr, q + 1, r, s);
    }
  }

  static void sort(float[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  static void sort(double[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  static void sort(int[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  static int partition(float[] arr, int p, int r, Object[] s)
  {
    float x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        float tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int partition(float[] arr, int p, int r, char[] s)
  {
    float x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        float tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        char tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int partition(int[] arr, int p, int r, Object[] s)
  {
    int x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int partition(double[] arr, int p, int r, Object[] s)
  {
    double x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        double tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int stringPartition(String[] arr, int p, int r, Object[] s)
  {
    String x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j].compareTo(x) < 0);

      do
      {
        i = i + 1;
      } while (arr[i].compareTo(x) > 0);

      if (i < j)
      {
        String tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  /**
   * Sorts both arrays to give ascending order in the first array, by first
   * partitioning into zero and non-zero values before sorting the latter.
   * 
   * @param arr
   * @param s
   */
  public static void sort(float[] arr, char[] s)
  {
    /*
     * Sort all zero values to the front
     */
    float[] f1 = new float[arr.length];
    char[] s1 = new char[s.length];
    int nextZeroValue = 0;
    int nextNonZeroValue = arr.length - 1;
    for (int i = 0; i < arr.length; i++)
    {
      float val = arr[i];
      if (val > 0f)
      {
        f1[nextNonZeroValue] = val;
        s1[nextNonZeroValue] = s[i];
        nextNonZeroValue--;
      }
      else
      {
        f1[nextZeroValue] = val;
        s1[nextZeroValue] = s[i];
        nextZeroValue++;
      }
    }

    /*
     * Copy zero values back to original arrays
     */
    System.arraycopy(f1, 0, arr, 0, nextZeroValue);
    System.arraycopy(s1, 0, s, 0, nextZeroValue);

    if (nextZeroValue == arr.length)
    {
      return; // all zero
    }
    /*
     * Sort the non-zero values
     */
    float[] nonZeroFloats = Arrays
            .copyOfRange(f1, nextZeroValue, f1.length);
    char[] nonZeroChars = Arrays.copyOfRange(s1, nextZeroValue, s1.length);
    externalSort(nonZeroFloats, nonZeroChars);
    // sort(nonZeroFloats, 0, nonZeroFloats.length - 1, nonZeroChars);

    /*
     * Assemble sorted non-zero results
     */
    System.arraycopy(nonZeroFloats, 0, arr, nextZeroValue,
            nonZeroFloats.length);
    System.arraycopy(nonZeroChars, 0, s, nextZeroValue, nonZeroChars.length);
  }

  /**
   * Sort by making an array of indices, and sorting it using a comparator that
   * refers to the float values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   */
  protected static void externalSort(float[] arr, char[] s)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new FloatComparator(arr));

    /*
     * Copy the array values as per the sorted indices
     */
    float[] sortedFloats = new float[length];
    char[] sortedChars = new char[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedFloats[i] = arr[indices[i]];
      sortedChars[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedFloats, 0, arr, 0, length);
    System.arraycopy(sortedChars, 0, s, 0, s.length);
  }

  /**
   * Make an array whose values are 0...length.
   * 
   * @param length
   * @return
   */
  protected static Integer[] makeIndexArray(final int length)
  {
    Integer[] indices = new Integer[length];
    for (int i = 0; i < length; i++)
    {
      indices[i] = i;
    }
    return indices;
  }

  static void sort(float[] arr, int p, int r, char[] s)
  {
    int q;
    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  /**
   * Sorts both arrays to give ascending order in the first array, by first
   * partitioning into zero and non-zero values before sorting the latter.
   * 
   * @param arr
   * @param s
   */
  public static void sort(int[] arr, char[] s)
  {
    /*
     * Sort all zero values to the front
     */
    int[] f1 = new int[arr.length];
    char[] s1 = new char[s.length];
    int nextZeroValue = 0;
    int nextNonZeroValue = arr.length - 1;
    for (int i = 0; i < arr.length; i++)
    {
      int val = arr[i];
      if (val > 0f)
      {
        f1[nextNonZeroValue] = val;
        s1[nextNonZeroValue] = s[i];
        nextNonZeroValue--;
      }
      else
      {
        f1[nextZeroValue] = val;
        s1[nextZeroValue] = s[i];
        nextZeroValue++;
      }
    }

    /*
     * Copy zero values back to original arrays
     */
    System.arraycopy(f1, 0, arr, 0, nextZeroValue);
    System.arraycopy(s1, 0, s, 0, nextZeroValue);

    if (nextZeroValue == arr.length)
    {
      return; // all zero
    }
    /*
     * Sort the non-zero values
     */
    int[] nonZeroInts = Arrays.copyOfRange(f1, nextZeroValue, f1.length);
    char[] nonZeroChars = Arrays.copyOfRange(s1, nextZeroValue, s1.length);
    externalSort(nonZeroInts, nonZeroChars);
    // sort(nonZeroFloats, 0, nonZeroFloats.length - 1, nonZeroChars);

    /*
     * Assemble sorted non-zero results
     */
    System.arraycopy(nonZeroInts, 0, arr, nextZeroValue, nonZeroInts.length);
    System.arraycopy(nonZeroChars, 0, s, nextZeroValue, nonZeroChars.length);
  }

  /**
   * Sort by making an array of indices, and sorting it using a comparator that
   * refers to the float values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   */
  protected static void externalSort(int[] arr, char[] s)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new IntComparator(arr));

    /*
     * Copy the array values as per the sorted indices
     */
    int[] sortedInts = new int[length];
    char[] sortedChars = new char[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedInts[i] = arr[indices[i]];
      sortedChars[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedInts, 0, arr, 0, length);
    System.arraycopy(sortedChars, 0, s, 0, s.length);
  }
}
