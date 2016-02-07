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

public interface AnnotatedCollectionI extends SequenceCollectionI
{

  /**
   * TODO: decide if null is a valid response if there is no annotation on the
   * object
   * 
   * @return null
   */
  AlignmentAnnotation[] getAlignmentAnnotation();

  Iterable<AlignmentAnnotation> findAnnotation(String calcId);

  Iterable<AlignmentAnnotation> findAnnotations(SequenceI seq,
          String calcId, String label);

  /**
   * context for this annotated collection
   * 
   * @return null or the collection upon which this collection is defined (e.g.
   *         alignment, parent group).
   */
  AnnotatedCollectionI getContext();
}
