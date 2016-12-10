/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
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

public class ArrayUtils
{
  /**
   * Reverse the given array 'in situ'
   * 
   * @param arr
   */
  public static void reverseIntArray(int[] arr)
  {
    if (arr != null)
    {
      /*
       * swap [k] with [end-k] up to the half way point in the array
       * if length is odd, the middle entry is left untouched by the excitement
       */
      int last = arr.length - 1;
      for (int k = 0; k < arr.length / 2; k++)
      {
        int temp = arr[k];
        arr[k] = arr[last - k];
        arr[last - k] = temp;
      }
    }
  }
}
