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
package jalview.datamodel;

import java.util.List;
import java.util.Map;

public interface SequenceCollectionI
{
  List<SequenceI> getSequences();

  List<SequenceI> getSequences(
          Map<SequenceI, SequenceCollectionI> hiddenReps);

  int getWidth();

  /**
   * 
   * @return true if getSeqrep doesn't return null
   */
  boolean hasSeqrep();

  /**
   * get the reference or representative sequence within this collection
   * 
   * @return null or the current reference sequence
   */
  SequenceI getSeqrep();

  /**
   * set the reference or representative sequence for this collection. Reference
   * is assumed to be present within the collection.
   * 
   * @return
   */
  void setSeqrep(SequenceI refseq);

  /**
   * @return the first column included in this collection. Runs from 0<=i<N_cols
   */
  int getStartRes();

  /**
   * 
   * @return the last column in this collection. Runs from 0<=i<N_cols
   */
  int getEndRes();

}
