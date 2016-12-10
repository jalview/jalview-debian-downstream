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
package jalview.workers;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.util.ColorUtils;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to compute an alignment annotation with column counts of any
 * properties of interest of positions in an alignment. <br>
 * This is designed to be extensible, by supplying to the constructor an object
 * that computes a count for each residue position, based on the residue value
 * and any sequence features at that position.
 * 
 */
class ColumnCounterWorker extends AlignCalcWorker
{
  FeatureCounterI counter;

  /**
   * Constructor registers the annotation for the given alignment frame
   * 
   * @param af
   * @param counter
   */
  public ColumnCounterWorker(AlignViewportI viewport,
          AlignmentViewPanel panel, FeatureCounterI counter)
  {
    super(viewport, panel);
    ourAnnots = new ArrayList<AlignmentAnnotation>();
    this.counter = counter;
    calcMan.registerWorker(this);
  }

  /**
   * method called under control of AlignCalcManager to recompute the annotation
   * when the alignment changes
   */
  @Override
  public void run()
  {
    try
    {
      calcMan.notifyStart(this);

      while (!calcMan.notifyWorking(this))
      {
        try
        {
          Thread.sleep(200);
        } catch (InterruptedException ex)
        {
          ex.printStackTrace();
        }
      }
      if (alignViewport.isClosed())
      {
        abortAndDestroy();
        return;
      }

      if (alignViewport.getAlignment() != null)
      {
        try
        {
          computeAnnotations();
        } catch (IndexOutOfBoundsException x)
        {
          // probable race condition. just finish and return without any fuss.
          return;
        }
      }
    } catch (OutOfMemoryError error)
    {
      ap.raiseOOMWarning("calculating feature counts", error);
      calcMan.disableWorker(this);
    } finally
    {
      calcMan.workerComplete(this);
    }

    if (ap != null)
    {
      ap.adjustAnnotationHeight();
      ap.paintAlignment(true);
    }

  }

  /**
   * Scan each column of the alignment to calculate a count by feature type. Set
   * the count as the value of the alignment annotation for that feature type.
   */
  void computeAnnotations()
  {
    FeatureRenderer fr = new FeatureRenderer(alignViewport);
    // TODO use the commented out code once JAL-2075 is fixed
    // to get adequate performance on genomic length sequence
    AlignmentI alignment = alignViewport.getAlignment();
    // AlignmentView alignmentView = alignViewport.getAlignmentView(false);
    // AlignmentI alignment = alignmentView.getVisibleAlignment(' ');

    // int width = alignmentView.getWidth();
    int width = alignment.getWidth();
    int height = alignment.getHeight();
    int[] counts = new int[width];
    int max = 0;

    for (int col = 0; col < width; col++)
    {
      int count = 0;
      for (int row = 0; row < height; row++)
      {
        count += countFeaturesAt(alignment, col, row, fr);
      }
      counts[col] = count;
      max = Math.max(count, max);
    }

    Annotation[] anns = new Annotation[width];
    /*
     * add non-zero counts as annotations
     */
    for (int i = 0; i < counts.length; i++)
    {
      int count = counts[i];
      if (count > 0)
      {
        Color color = ColorUtils.getGraduatedColour(count, 0, Color.cyan,
                max, Color.blue);
        String str = String.valueOf(count);
        anns[i] = new Annotation(str, str, '0', count, color);
      }
    }

    /*
     * construct or update the annotation
     */
    AlignmentAnnotation ann = alignViewport.getAlignment()
            .findOrCreateAnnotation(counter.getName(),
                    counter.getDescription(), false, null, null);
    ann.description = counter.getDescription();
    ann.showAllColLabels = true;
    ann.scaleColLabel = true;
    ann.graph = AlignmentAnnotation.BAR_GRAPH;
    ann.annotations = anns;
    setGraphMinMax(ann, anns);
    ann.validateRangeAndDisplay();
    if (!ourAnnots.contains(ann))
    {
      ourAnnots.add(ann);
    }
  }

  /**
   * Returns a count of any feature types present at the specified position of
   * the alignment
   * 
   * @param alignment
   * @param col
   * @param row
   * @param fr
   */
  int countFeaturesAt(AlignmentI alignment, int col, int row,
          FeatureRenderer fr)
  {
    SequenceI seq = alignment.getSequenceAt(row);
    if (seq == null)
    {
      return 0;
    }
    if (col >= seq.getLength())
    {
      return 0;// sequence doesn't extend this far
    }
    char res = seq.getCharAt(col);
    if (Comparison.isGap(res))
    {
      return 0;
    }
    int pos = seq.findPosition(col);

    /*
     * compute a count for any displayed features at residue
     */
    // NB have to adjust pos if using AlignmentView.getVisibleAlignment
    // see JAL-2075
    List<SequenceFeature> features = fr.findFeaturesAtRes(seq, pos);
    int count = this.counter.count(String.valueOf(res), features);
    return count;
  }

  /**
   * Method called when the user changes display options that may affect how the
   * annotation is rendered, but do not change its values. Currently no such
   * options affect user-defined annotation, so this method does nothing.
   */
  @Override
  public void updateAnnotation()
  {
    // do nothing
  }

  /**
   * Answers true to indicate that if this worker's annotation is deleted from
   * the display, the worker should also be removed. This prevents it running
   * and recreating the annotation when the alignment changes.
   */
  @Override
  public boolean isDeletable()
  {
    return true;
  }
}
