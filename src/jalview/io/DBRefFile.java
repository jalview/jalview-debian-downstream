/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
package jalview.io;

import java.io.IOException;

/**
 * jalview flatfile for io of sequence ID mapping data
 * DATABASE\t<nickname>\t<name>\t<version>... properties?jdbc, url, handler
 * DBREF
 * <seqID>\tDBID\taccno\t|seqstart,seqend,start,end...|\t|dbstart,dbend,...|
 * 
 */
public class DBRefFile extends AlignFile
{

  public void parse() throws IOException
  {
    // TODO Auto-generated method stub

  }

  public String print()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
