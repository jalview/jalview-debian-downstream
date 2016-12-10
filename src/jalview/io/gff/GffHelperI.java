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
package jalview.io.gff;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.io.IOException;
import java.util.List;

/**
 * An interface to described common functionality of different flavours of GFF
 * 
 * @author gmcarstairs
 *
 */
public interface GffHelperI
{

  final String RENAME_TOKEN = "$RENAME_TO$";

  /**
   * Process one GFF feature line
   * 
   * @param seq
   *          the sequence with which this feature is associated
   * @param gffColumns
   *          the GFF column data
   * @param align
   *          the alignment we are adding GFF to
   * @param newseqs
   *          any new sequences referenced by the GFF
   * @param relaxedIdMatching
   *          if true, match word tokens in sequence names
   * @return a SequenceFeature if one should be created, else null
   * @throws IOException
   */
  SequenceFeature processGff(SequenceI seq, String[] gffColumns,
          AlignmentI align, List<SequenceI> newseqs,
          boolean relaxedIdMatching) throws IOException;

  // java 8 will allow static methods in interfaces:
  // static boolean recognises(String [] columns);
}
