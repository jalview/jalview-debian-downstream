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

import jalview.api.FeatureColourI;
import jalview.datamodel.SequenceFeature;
import jalview.util.Format;

import java.awt.Color;
import java.util.StringTokenizer;

/**
 * A class that wraps either a simple colour or a graduated colour
 */
public class FeatureColour implements FeatureColourI
{
  private static final String BAR = "|";

  final private Color colour;

  final private Color minColour;

  final private Color maxColour;

  private boolean graduatedColour;

  private boolean colourByLabel;

  private float threshold;

  private float base;

  private float range;

  private boolean belowThreshold;

  private boolean aboveThreshold;

  private boolean thresholdIsMinOrMax;

  private boolean isHighToLow;

  private boolean autoScaled;

  final private float minRed;

  final private float minGreen;

  final private float minBlue;

  final private float deltaRed;

  final private float deltaGreen;

  final private float deltaBlue;

  /**
   * Parses a Jalview features file format colour descriptor
   * [label|][mincolour|maxcolour
   * |[absolute|]minvalue|maxvalue|thresholdtype|thresholdvalue] Examples:
   * <ul>
   * <li>red</li>
   * <li>a28bbb</li>
   * <li>25,125,213</li>
   * <li>label</li>
   * <li>label|||0.0|0.0|above|12.5</li>
   * <li>label|||0.0|0.0|below|12.5</li>
   * <li>red|green|12.0|26.0|none</li>
   * <li>a28bbb|3eb555|12.0|26.0|above|12.5</li>
   * <li>a28bbb|3eb555|abso|12.0|26.0|below|12.5</li>
   * </ul>
   * 
   * @param descriptor
   * @return
   * @throws IllegalArgumentException
   *           if not parseable
   */
  public static FeatureColour parseJalviewFeatureColour(String descriptor)
  {
    StringTokenizer gcol = new StringTokenizer(descriptor, "|", true);
    float min = Float.MIN_VALUE;
    float max = Float.MAX_VALUE;
    boolean labelColour = false;

    String mincol = gcol.nextToken();
    if (mincol == "|")
    {
      throw new IllegalArgumentException(
              "Expected either 'label' or a colour specification in the line: "
                      + descriptor);
    }
    String maxcol = null;
    if (mincol.toLowerCase().indexOf("label") == 0)
    {
      labelColour = true;
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
      // skip '|'
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
    }

    if (!labelColour && !gcol.hasMoreTokens())
    {
      /*
       * only a simple colour specification - parse it
       */
      Color colour = UserColourScheme.getColourFromString(descriptor);
      if (colour == null)
      {
        throw new IllegalArgumentException("Invalid colour descriptor: "
                + descriptor);
      }
      return new FeatureColour(colour);
    }

    /*
     * autoScaled == true: colours range over actual score range
     * autoScaled == false ('abso'): colours range over min/max range
     */
    boolean autoScaled = true;
    String tok = null, minval, maxval;
    if (mincol != null)
    {
      // at least four more tokens
      if (mincol.equals("|"))
      {
        mincol = "";
      }
      else
      {
        gcol.nextToken(); // skip next '|'
      }
      maxcol = gcol.nextToken();
      if (maxcol.equals("|"))
      {
        maxcol = "";
      }
      else
      {
        gcol.nextToken(); // skip next '|'
      }
      tok = gcol.nextToken();
      gcol.nextToken(); // skip next '|'
      if (tok.toLowerCase().startsWith("abso"))
      {
        minval = gcol.nextToken();
        gcol.nextToken(); // skip next '|'
        autoScaled = false;
      }
      else
      {
        minval = tok;
      }
      maxval = gcol.nextToken();
      if (gcol.hasMoreTokens())
      {
        gcol.nextToken(); // skip next '|'
      }
      try
      {
        if (minval.length() > 0)
        {
          min = new Float(minval).floatValue();
        }
      } catch (Exception e)
      {
        throw new IllegalArgumentException(
                "Couldn't parse the minimum value for graduated colour ("
                        + descriptor + ")");
      }
      try
      {
        if (maxval.length() > 0)
        {
          max = new Float(maxval).floatValue();
        }
      } catch (Exception e)
      {
        throw new IllegalArgumentException(
                "Couldn't parse the maximum value for graduated colour ("
                        + descriptor + ")");
      }
    }
    else
    {
      // add in some dummy min/max colours for the label-only
      // colourscheme.
      mincol = "FFFFFF";
      maxcol = "000000";
    }

    /*
     * construct the FeatureColour
     */
    FeatureColour featureColour;
    try
    {
      featureColour = new FeatureColour(
              new UserColourScheme(mincol).findColour('A'),
              new UserColourScheme(maxcol).findColour('A'), min, max);
      featureColour.setColourByLabel(labelColour);
      featureColour.setAutoScaled(autoScaled);
      // add in any additional parameters
      String ttype = null, tval = null;
      if (gcol.hasMoreTokens())
      {
        // threshold type and possibly a threshold value
        ttype = gcol.nextToken();
        if (ttype.toLowerCase().startsWith("below"))
        {
          featureColour.setBelowThreshold(true);
        }
        else if (ttype.toLowerCase().startsWith("above"))
        {
          featureColour.setAboveThreshold(true);
        }
        else
        {
          if (!ttype.toLowerCase().startsWith("no"))
          {
            System.err.println("Ignoring unrecognised threshold type : "
                    + ttype);
          }
        }
      }
      if (featureColour.hasThreshold())
      {
        try
        {
          gcol.nextToken();
          tval = gcol.nextToken();
          featureColour.setThreshold(new Float(tval).floatValue());
        } catch (Exception e)
        {
          System.err.println("Couldn't parse threshold value as a float: ("
                  + tval + ")");
        }
      }
      if (gcol.hasMoreTokens())
      {
        System.err
                .println("Ignoring additional tokens in parameters in graduated colour specification\n");
        while (gcol.hasMoreTokens())
        {
          System.err.println("|" + gcol.nextToken());
        }
        System.err.println("\n");
      }
      return featureColour;
    } catch (Exception e)
    {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Default constructor
   */
  public FeatureColour()
  {
    this((Color) null);
  }

  /**
   * Constructor given a simple colour
   * 
   * @param c
   */
  public FeatureColour(Color c)
  {
    minColour = Color.WHITE;
    maxColour = Color.BLACK;
    minRed = 0f;
    minGreen = 0f;
    minBlue = 0f;
    deltaRed = 0f;
    deltaGreen = 0f;
    deltaBlue = 0f;
    colour = c;
  }

  /**
   * Constructor given a colour range and a score range
   * 
   * @param low
   * @param high
   * @param min
   * @param max
   */
  public FeatureColour(Color low, Color high, float min, float max)
  {
    graduatedColour = true;
    colour = null;
    minColour = low;
    maxColour = high;
    threshold = Float.NaN;
    isHighToLow = min >= max;
    minRed = low.getRed() / 255f;
    minGreen = low.getGreen() / 255f;
    minBlue = low.getBlue() / 255f;
    deltaRed = (high.getRed() / 255f) - minRed;
    deltaGreen = (high.getGreen() / 255f) - minGreen;
    deltaBlue = (high.getBlue() / 255f) - minBlue;
    if (isHighToLow)
    {
      base = max;
      range = min - max;
    }
    else
    {
      base = min;
      range = max - min;
    }
  }

  /**
   * Copy constructor
   * 
   * @param fc
   */
  public FeatureColour(FeatureColour fc)
  {
    graduatedColour = fc.graduatedColour;
    colour = fc.colour;
    minColour = fc.minColour;
    maxColour = fc.maxColour;
    minRed = fc.minRed;
    minGreen = fc.minGreen;
    minBlue = fc.minBlue;
    deltaRed = fc.deltaRed;
    deltaGreen = fc.deltaGreen;
    deltaBlue = fc.deltaBlue;
    base = fc.base;
    range = fc.range;
    isHighToLow = fc.isHighToLow;
    setAboveThreshold(fc.isAboveThreshold());
    setBelowThreshold(fc.isBelowThreshold());
    setThreshold(fc.getThreshold());
    setAutoScaled(fc.isAutoScaled());
    setColourByLabel(fc.isColourByLabel());
  }

  /**
   * Copy constructor with new min/max ranges
   * 
   * @param fc
   * @param min
   * @param max
   */
  public FeatureColour(FeatureColour fc, float min, float max)
  {
    this(fc);
    graduatedColour = true;
    updateBounds(min, max);
  }

  @Override
  public boolean isGraduatedColour()
  {
    return graduatedColour;
  }

  /**
   * Sets the 'graduated colour' flag. If true, also sets 'colour by label' to
   * false.
   */
  void setGraduatedColour(boolean b)
  {
    graduatedColour = b;
    if (b)
    {
      setColourByLabel(false);
    }
  }

  @Override
  public Color getColour()
  {
    return colour;
  }

  @Override
  public Color getMinColour()
  {
    return minColour;
  }

  @Override
  public Color getMaxColour()
  {
    return maxColour;
  }

  @Override
  public boolean isColourByLabel()
  {
    return colourByLabel;
  }

  /**
   * Sets the 'colour by label' flag. If true, also sets 'graduated colour' to
   * false.
   */
  @Override
  public void setColourByLabel(boolean b)
  {
    colourByLabel = b;
    if (b)
    {
      setGraduatedColour(false);
    }
  }

  @Override
  public boolean isBelowThreshold()
  {
    return belowThreshold;
  }

  @Override
  public void setBelowThreshold(boolean b)
  {
    belowThreshold = b;
    if (b)
    {
      setAboveThreshold(false);
    }
  }

  @Override
  public boolean isAboveThreshold()
  {
    return aboveThreshold;
  }

  @Override
  public void setAboveThreshold(boolean b)
  {
    aboveThreshold = b;
    if (b)
    {
      setBelowThreshold(false);
    }
  }

  @Override
  public boolean isThresholdMinMax()
  {
    return thresholdIsMinOrMax;
  }

  @Override
  public void setThresholdMinMax(boolean b)
  {
    thresholdIsMinOrMax = b;
  }

  @Override
  public float getThreshold()
  {
    return threshold;
  }

  @Override
  public void setThreshold(float f)
  {
    threshold = f;
  }

  @Override
  public boolean isAutoScaled()
  {
    return autoScaled;
  }

  @Override
  public void setAutoScaled(boolean b)
  {
    this.autoScaled = b;
  }

  /**
   * Updates the base and range appropriately for the given minmax range
   * 
   * @param min
   * @param max
   */
  @Override
  public void updateBounds(float min, float max)
  {
    if (max < min)
    {
      base = max;
      range = min - max;
      isHighToLow = true;
    }
    else
    {
      base = min;
      range = max - min;
      isHighToLow = false;
    }
  }

  /**
   * Returns the colour for the given instance of the feature. This may be a
   * simple colour, a colour generated from the feature description (if
   * isColourByLabel()), or a colour derived from the feature score (if
   * isGraduatedColour()).
   * 
   * @param feature
   * @return
   */
  @Override
  public Color getColor(SequenceFeature feature)
  {
    if (isColourByLabel())
    {
      return UserColourScheme
              .createColourFromName(feature.getDescription());
    }

    if (!isGraduatedColour())
    {
      return getColour();
    }

    // todo should we check for above/below threshold here?
    if (range == 0.0)
    {
      return getMaxColour();
    }
    float scr = feature.getScore();
    if (Float.isNaN(scr))
    {
      return getMinColour();
    }
    float scl = (scr - base) / range;
    if (isHighToLow)
    {
      scl = -scl;
    }
    if (scl < 0f)
    {
      scl = 0f;
    }
    if (scl > 1f)
    {
      scl = 1f;
    }
    return new Color(minRed + scl * deltaRed, minGreen + scl * deltaGreen,
            minBlue + scl * deltaBlue);
  }

  /**
   * Returns the maximum score of the graduated colour range
   * 
   * @return
   */
  @Override
  public float getMax()
  {
    // regenerate the original values passed in to the constructor
    return (isHighToLow) ? base : (base + range);
  }

  /**
   * Returns the minimum score of the graduated colour range
   * 
   * @return
   */
  @Override
  public float getMin()
  {
    // regenerate the original value passed in to the constructor
    return (isHighToLow) ? (base + range) : base;
  }

  /**
   * Answers true if the feature has a simple colour, or is coloured by label,
   * or has a graduated colour and the score of this feature instance is within
   * the range to render (if any), i.e. does not lie below or above any
   * threshold set.
   * 
   * @param feature
   * @return
   */
  @Override
  public boolean isColored(SequenceFeature feature)
  {
    if (isColourByLabel() || !isGraduatedColour())
    {
      return true;
    }

    float val = feature.getScore();
    if (Float.isNaN(val))
    {
      return true;
    }
    if (Float.isNaN(this.threshold))
    {
      return true;
    }

    if (isAboveThreshold() && val <= threshold)
    {
      return false;
    }
    if (isBelowThreshold() && val >= threshold)
    {
      return false;
    }
    return true;
  }

  @Override
  public boolean isSimpleColour()
  {
    return (!isColourByLabel() && !isGraduatedColour());
  }

  @Override
  public boolean hasThreshold()
  {
    return isAboveThreshold() || isBelowThreshold();
  }

  @Override
  public String toJalviewFormat(String featureType)
  {
    String colourString = null;
    if (isSimpleColour())
    {
      colourString = Format.getHexString(getColour());
    }
    else
    {
      StringBuilder sb = new StringBuilder(32);
      if (isColourByLabel())
      {
        sb.append("label");
        if (hasThreshold())
        {
          sb.append(BAR).append(BAR).append(BAR);
        }
      }
      if (isGraduatedColour())
      {
        sb.append(Format.getHexString(getMinColour())).append(BAR);
        sb.append(Format.getHexString(getMaxColour())).append(BAR);
        if (!isAutoScaled())
        {
          sb.append("abso").append(BAR);
        }
      }
      if (hasThreshold() || isGraduatedColour())
      {
        sb.append(getMin()).append(BAR);
        sb.append(getMax()).append(BAR);
        if (isBelowThreshold())
        {
          sb.append("below").append(BAR).append(getThreshold());
        }
        else if (isAboveThreshold())
        {
          sb.append("above").append(BAR).append(getThreshold());
        }
        else
        {
          sb.append("none");
        }
      }
      colourString = sb.toString();
    }
    return String.format("%s\t%s", featureType, colourString);
  }

}
