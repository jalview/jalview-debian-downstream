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
package jalview.schemes;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.Annotation;
import jalview.datamodel.GraphLine;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.IdentityHashMap;
import java.util.Map;

public class AnnotationColourGradient extends FollowerColourScheme
{
  public static final int NO_THRESHOLD = -1;

  public static final int BELOW_THRESHOLD = 0;

  public static final int ABOVE_THRESHOLD = 1;

  public AlignmentAnnotation annotation;

  int aboveAnnotationThreshold = -1;

  public boolean thresholdIsMinMax = false;

  GraphLine annotationThreshold;

  float r1, g1, b1, rr, gg, bb;

  private boolean predefinedColours = false;

  private boolean seqAssociated = false;

  /**
   * false if the scheme was constructed without a minColour and maxColour used
   * to decide if existing colours should be taken from annotation elements when
   * they exist
   */
  private boolean noGradient = false;

  IdentityHashMap<SequenceI, AlignmentAnnotation> seqannot = null;

  @Override
  public ColourSchemeI applyTo(AnnotatedCollectionI sg,
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    AnnotationColourGradient acg = new AnnotationColourGradient(annotation,
            colourScheme, aboveAnnotationThreshold);
    acg.thresholdIsMinMax = thresholdIsMinMax;
    acg.annotationThreshold = (annotationThreshold == null) ? null
            : new GraphLine(annotationThreshold);
    acg.r1 = r1;
    acg.g1 = g1;
    acg.b1 = b1;
    acg.rr = rr;
    acg.gg = gg;
    acg.bb = bb;
    acg.predefinedColours = predefinedColours;
    acg.seqAssociated = seqAssociated;
    acg.noGradient = noGradient;
    return acg;
  }

  /**
   * Creates a new AnnotationColourGradient object.
   */
  public AnnotationColourGradient(AlignmentAnnotation annotation,
          ColourSchemeI originalColour, int aboveThreshold)
  {
    if (originalColour instanceof AnnotationColourGradient)
    {
      colourScheme = ((AnnotationColourGradient) originalColour).colourScheme;
    }
    else
    {
      colourScheme = originalColour;
    }

    this.annotation = annotation;

    aboveAnnotationThreshold = aboveThreshold;

    if (aboveThreshold != NO_THRESHOLD && annotation.threshold != null)
    {
      annotationThreshold = annotation.threshold;
    }
    // clear values so we don't get weird black bands...
    r1 = 254;
    g1 = 254;
    b1 = 254;
    rr = 0;
    gg = 0;
    bb = 0;

    noGradient = true;
    checkLimits();
  }

  /**
   * Creates a new AnnotationColourGradient object.
   */
  public AnnotationColourGradient(AlignmentAnnotation annotation,
          Color minColour, Color maxColour, int aboveThreshold)
  {
    this.annotation = annotation;

    aboveAnnotationThreshold = aboveThreshold;

    if (aboveThreshold != NO_THRESHOLD && annotation.threshold != null)
    {
      annotationThreshold = annotation.threshold;
    }

    r1 = minColour.getRed();
    g1 = minColour.getGreen();
    b1 = minColour.getBlue();

    rr = maxColour.getRed() - r1;
    gg = maxColour.getGreen() - g1;
    bb = maxColour.getBlue() - b1;

    noGradient = false;
    checkLimits();
  }

  private void checkLimits()
  {
    aamax = annotation.graphMax;
    aamin = annotation.graphMin;
    if (annotation.isRNA())
    {
      // reset colour palette
      ColourSchemeProperty.resetRnaHelicesShading();
      ColourSchemeProperty.initRnaHelicesShading(1 + (int) aamax);
    }
  }

  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    super.alignmentChanged(alignment, hiddenReps);

    if (seqAssociated && annotation.getCalcId() != null)
    {
      if (seqannot != null)
      {
        seqannot.clear();
      }
      else
      {
        seqannot = new IdentityHashMap<SequenceI, AlignmentAnnotation>();
      }
      // resolve the context containing all the annotation for the sequence
      AnnotatedCollectionI alcontext = alignment instanceof AlignmentI ? alignment
              : alignment.getContext();
      boolean f = true, rna = false;
      for (AlignmentAnnotation alan : alcontext.findAnnotation(annotation
              .getCalcId()))
      {
        if (alan.sequenceRef != null
                && (alan.label != null && annotation != null && alan.label
                        .equals(annotation.label)))
        {
          if (!rna && alan.isRNA())
          {
            rna = true;
          }
          seqannot.put(alan.sequenceRef, alan);
          if (f || alan.graphMax > aamax)
          {
            aamax = alan.graphMax;
          }
          if (f || alan.graphMin < aamin)
          {
            aamin = alan.graphMin;
          }
          f = false;
        }
      }
      if (rna)
      {
        ColourSchemeProperty.initRnaHelicesShading(1 + (int) aamax);
      }
    }
  }

  float aamin = 0f, aamax = 0f;

  public String getAnnotation()
  {
    return annotation.label;
  }

  public int getAboveThreshold()
  {
    return aboveAnnotationThreshold;
  }

  public float getAnnotationThreshold()
  {
    if (annotationThreshold == null)
    {
      return 0;
    }
    else
    {
      return annotationThreshold.value;
    }
  }

  public Color getMinColour()
  {
    return new Color((int) r1, (int) g1, (int) b1);
  }

  public Color getMaxColour()
  {
    return new Color((int) (r1 + rr), (int) (g1 + gg), (int) (b1 + bb));
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Color findColour(char c)
  {
    return Color.red;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    Color currentColour = Color.white;
    AlignmentAnnotation annotation = (seqAssociated && seqannot != null ? seqannot
            .get(seq) : this.annotation);
    if (annotation == null)
    {
      return currentColour;
    }
    if ((threshold == 0) || aboveThreshold(c, j))
    {
      if (annotation.annotations != null
              && j < annotation.annotations.length
              && annotation.annotations[j] != null
              && !jalview.util.Comparison.isGap(c))
      {
        Annotation aj = annotation.annotations[j];
        // 'use original colours' => colourScheme != null
        // -> look up colour to be used
        // predefined colours => preconfigured shading
        // -> only use original colours reference if thresholding enabled &
        // minmax exists
        // annotation.hasIcons => null or black colours replaced with glyph
        // colours
        // -> reuse original colours if present
        // -> if thresholding enabled then return colour on non-whitespace glyph

        if (aboveAnnotationThreshold == NO_THRESHOLD
                || (annotationThreshold != null && (aboveAnnotationThreshold == ABOVE_THRESHOLD ? aj.value >= annotationThreshold.value
                        : aj.value <= annotationThreshold.value)))
        {
          if (predefinedColours && aj.colour != null
                  && !aj.colour.equals(Color.black))
          {
            currentColour = aj.colour;
          }
          else if (annotation.hasIcons
                  && annotation.graph == AlignmentAnnotation.NO_GRAPH)
          {
            if (aj.secondaryStructure > ' ' && aj.secondaryStructure != '.'
                    && aj.secondaryStructure != '-')
            {
              if (colourScheme != null)
              {
                currentColour = colourScheme.findColour(c, j, seq);
              }
              else
              {
                if (annotation.isRNA())
                {
                  currentColour = ColourSchemeProperty.rnaHelices[(int) aj.value];
                }
                else
                {
                  currentColour = annotation.annotations[j].secondaryStructure == 'H' ? jalview.renderer.AnnotationRenderer.HELIX_COLOUR
                          : annotation.annotations[j].secondaryStructure == 'E' ? jalview.renderer.AnnotationRenderer.SHEET_COLOUR
                                  : jalview.renderer.AnnotationRenderer.STEM_COLOUR;
                }
              }
            }
            else
            {
              //
              return Color.white;
            }
          }
          else if (noGradient)
          {
            if (colourScheme != null)
            {
              currentColour = colourScheme.findColour(c, j, seq);
            }
            else
            {
              if (aj.colour != null)
              {
                currentColour = aj.colour;
              }
            }
          }
          else
          {
            currentColour = shadeCalculation(annotation, j);
          }
        }
        if (conservationColouring)
        {
          currentColour = applyConservation(currentColour, j);
        }
      }
    }
    return currentColour;
  }

  private Color shadeCalculation(AlignmentAnnotation annotation, int j)
  {

    // calculate a shade
    float range = 1f;
    if (thresholdIsMinMax
            && annotation.threshold != null
            && aboveAnnotationThreshold == ABOVE_THRESHOLD
            && annotation.annotations[j].value >= annotation.threshold.value)
    {
      range = (annotation.annotations[j].value - annotation.threshold.value)
              / (annotation.graphMax - annotation.threshold.value);
    }
    else if (thresholdIsMinMax && annotation.threshold != null
            && aboveAnnotationThreshold == BELOW_THRESHOLD
            && annotation.annotations[j].value >= annotation.graphMin)
    {
      range = (annotation.annotations[j].value - annotation.graphMin)
              / (annotation.threshold.value - annotation.graphMin);
    }
    else
    {
      if (annotation.graphMax != annotation.graphMin)
      {
        range = (annotation.annotations[j].value - annotation.graphMin)
                / (annotation.graphMax - annotation.graphMin);
      }
      else
      {
        range = 0f;
      }
    }

    int dr = (int) (rr * range + r1), dg = (int) (gg * range + g1), db = (int) (bb
            * range + b1);

    return new Color(dr, dg, db);

  }

  public boolean isPredefinedColours()
  {
    return predefinedColours;
  }

  public void setPredefinedColours(boolean predefinedColours)
  {
    this.predefinedColours = predefinedColours;
  }

  public boolean isSeqAssociated()
  {
    return seqAssociated;
  }

  public void setSeqAssociated(boolean sassoc)
  {
    seqAssociated = sassoc;
  }
}
