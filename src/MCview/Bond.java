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
package MCview;

import java.awt.Color;

public class Bond
{
  float[] start;

  float[] end;

  Color startCol = Color.lightGray;

  Color endCol = Color.lightGray;

  public Atom at1;

  public Atom at2;

  public Bond(Atom at1, Atom at2)
  {
    this.start = new float[] { at1.x, at1.y, at1.z };
    this.end = new float[] { at2.x, at2.y, at2.z };
    this.startCol = at1.color;
    this.endCol = at2.color;
    this.at1 = at1;
    this.at2 = at2;
  }

  /*
   * public Bond(Bond bond) { this.start = new float[3];
   * 
   * this.start[0] = bond.start[0]; this.start[1] = bond.start[1]; this.start[2]
   * = bond.start[2];
   * 
   * this.end = new float[3];
   * 
   * this.end[0] = bond.end[0]; this.end[1] = bond.end[1]; this.end[2] =
   * bond.end[2];
   * 
   * this.startCol = bond.startCol; this.endCol = bond.endCol; }
   * 
   * public float length() { float len = ((end[0] - start[0]) * (end[0] -
   * start[0])) + ((end[1] - start[1]) * (end[1] - start[1])) + ((end[2] -
   * start[2]) * (end[2] - start[2]));
   * 
   * len = (float) (Math.sqrt(len));
   * 
   * return len; }
   */

  public void translate(float x, float y, float z)
  {
    start[0] = start[0] + x;
    end[0] = end[0] + x;

    start[1] = start[1] + y;
    end[1] = end[1] + y;

    start[2] = start[2] + z;
    end[2] = end[2] + z;
  }
}
