package jalview.gui;

import javax.swing.JSlider;

/**
 * A modified {@code javax.swing.JSlider} that
 * <ul>
 * <li>supports float valued numbers (by scaling up integer values)</li>
 * <li>rescales 'true' value range to avoid negative values, as these are not
 * rendered correctly by some look and feel libraries</li>
 * </ul>
 * 
 * @author gmcarstairs
 */
@SuppressWarnings("serial")
public class Slider extends JSlider
{
  /*
   * the number of nominal positions the slider represents
   * (higher number = more fine-grained positioning)
   */
  private static final int SCALE_TICKS = 1000;

  /*
   * 'true' value corresponding to zero on the slider
   */
  private float trueMin;

  /*
   * 'true' value corresponding to slider maximum
   */
  private float trueMax;

  /*
   * scaleFactor applied to true value range to give a
   * slider range of 0 - 100
   */
  private float sliderScaleFactor;

  /**
   * Constructor that rescales min - max to 0 - 100 for the slider
   * 
   * @param min
   * @param max
   * @param value
   */
  public Slider(float min, float max, float value)
  {
    super();
    setSliderModel(min, max, value);
  }

  /**
   * Sets the min-max range and current value of the slider, with rescaling from
   * true values to slider range as required
   * 
   * @param min
   * @param max
   * @param value
   */
  public void setSliderModel(float min, float max, float value)
  {
    trueMin = min;
    trueMax = max;
    setMinimum(0);
    sliderScaleFactor = SCALE_TICKS / (max - min);
    int sliderMax = (int) ((max - min) * sliderScaleFactor);
    setMaximum(sliderMax);
    setSliderValue(value);
  }

  /**
   * Answers the value of the slider position (descaled to 'true' value)
   * 
   * @return
   */
  public float getSliderValue()
  {
    /*
     * convert slider max to 'true max' in case of rounding errors
     */
    int value = getValue();
    return value == getMaximum() ? trueMax
            : value / sliderScaleFactor + trueMin;
  }

  /**
   * Sets the slider value (scaled from the true value to the slider range)
   * 
   * @param value
   */
  public void setSliderValue(float value)
  {
    setValue(Math.round((value - trueMin) * sliderScaleFactor));
  }

  /**
   * Answers the value of the slider position as a percentage between minimum and
   * maximum of its range
   * 
   * @return
   */
  public float getSliderPercentageValue()
  {
    return (getValue() - getMinimum()) * 100f
            / (getMaximum() - getMinimum());
  }

  /**
   * Sets the slider position for a given percentage value of its min-max range
   * 
   * @param pct
   */
  public void setSliderPercentageValue(float pct)
  {
    float pc = pct / 100f * getMaximum();
    setValue((int) pc);
  }
}
