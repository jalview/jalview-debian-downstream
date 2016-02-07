/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1</a>, using an XML
 * Schema.
 * $Id$
 */

package jalview.schemabinding.version2;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class AnnotationColourScheme.
 * 
 * @version $Revision$ $Date$
 */
public class AnnotationColourScheme implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _aboveThreshold.
   */
  private int _aboveThreshold;

  /**
   * keeps track of state for field: _aboveThreshold
   */
  private boolean _has_aboveThreshold;

  /**
   * Field _annotation.
   */
  private java.lang.String _annotation;

  /**
   * Field _minColour.
   */
  private int _minColour;

  /**
   * keeps track of state for field: _minColour
   */
  private boolean _has_minColour;

  /**
   * Field _maxColour.
   */
  private int _maxColour;

  /**
   * keeps track of state for field: _maxColour
   */
  private boolean _has_maxColour;

  /**
   * Field _colourScheme.
   */
  private java.lang.String _colourScheme;

  /**
   * Field _threshold.
   */
  private float _threshold;

  /**
   * keeps track of state for field: _threshold
   */
  private boolean _has_threshold;

  /**
   * Field _perSequence.
   */
  private boolean _perSequence;

  /**
   * keeps track of state for field: _perSequence
   */
  private boolean _has_perSequence;

  /**
   * Field _predefinedColours.
   */
  private boolean _predefinedColours;

  /**
   * keeps track of state for field: _predefinedColours
   */
  private boolean _has_predefinedColours;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public AnnotationColourScheme()
  {
    super();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
     */
  public void deleteAboveThreshold()
  {
    this._has_aboveThreshold = false;
  }

  /**
     */
  public void deleteMaxColour()
  {
    this._has_maxColour = false;
  }

  /**
     */
  public void deleteMinColour()
  {
    this._has_minColour = false;
  }

  /**
     */
  public void deletePerSequence()
  {
    this._has_perSequence = false;
  }

  /**
     */
  public void deletePredefinedColours()
  {
    this._has_predefinedColours = false;
  }

  /**
     */
  public void deleteThreshold()
  {
    this._has_threshold = false;
  }

  /**
   * Returns the value of field 'aboveThreshold'.
   * 
   * @return the value of field 'AboveThreshold'.
   */
  public int getAboveThreshold()
  {
    return this._aboveThreshold;
  }

  /**
   * Returns the value of field 'annotation'.
   * 
   * @return the value of field 'Annotation'.
   */
  public java.lang.String getAnnotation()
  {
    return this._annotation;
  }

  /**
   * Returns the value of field 'colourScheme'.
   * 
   * @return the value of field 'ColourScheme'.
   */
  public java.lang.String getColourScheme()
  {
    return this._colourScheme;
  }

  /**
   * Returns the value of field 'maxColour'.
   * 
   * @return the value of field 'MaxColour'.
   */
  public int getMaxColour()
  {
    return this._maxColour;
  }

  /**
   * Returns the value of field 'minColour'.
   * 
   * @return the value of field 'MinColour'.
   */
  public int getMinColour()
  {
    return this._minColour;
  }

  /**
   * Returns the value of field 'perSequence'.
   * 
   * @return the value of field 'PerSequence'.
   */
  public boolean getPerSequence()
  {
    return this._perSequence;
  }

  /**
   * Returns the value of field 'predefinedColours'.
   * 
   * @return the value of field 'PredefinedColours'.
   */
  public boolean getPredefinedColours()
  {
    return this._predefinedColours;
  }

  /**
   * Returns the value of field 'threshold'.
   * 
   * @return the value of field 'Threshold'.
   */
  public float getThreshold()
  {
    return this._threshold;
  }

  /**
   * Method hasAboveThreshold.
   * 
   * @return true if at least one AboveThreshold has been added
   */
  public boolean hasAboveThreshold()
  {
    return this._has_aboveThreshold;
  }

  /**
   * Method hasMaxColour.
   * 
   * @return true if at least one MaxColour has been added
   */
  public boolean hasMaxColour()
  {
    return this._has_maxColour;
  }

  /**
   * Method hasMinColour.
   * 
   * @return true if at least one MinColour has been added
   */
  public boolean hasMinColour()
  {
    return this._has_minColour;
  }

  /**
   * Method hasPerSequence.
   * 
   * @return true if at least one PerSequence has been added
   */
  public boolean hasPerSequence()
  {
    return this._has_perSequence;
  }

  /**
   * Method hasPredefinedColours.
   * 
   * @return true if at least one PredefinedColours has been added
   */
  public boolean hasPredefinedColours()
  {
    return this._has_predefinedColours;
  }

  /**
   * Method hasThreshold.
   * 
   * @return true if at least one Threshold has been added
   */
  public boolean hasThreshold()
  {
    return this._has_threshold;
  }

  /**
   * Returns the value of field 'perSequence'.
   * 
   * @return the value of field 'PerSequence'.
   */
  public boolean isPerSequence()
  {
    return this._perSequence;
  }

  /**
   * Returns the value of field 'predefinedColours'.
   * 
   * @return the value of field 'PredefinedColours'.
   */
  public boolean isPredefinedColours()
  {
    return this._predefinedColours;
  }

  /**
   * Method isValid.
   * 
   * @return true if this object is valid according to the schema
   */
  public boolean isValid()
  {
    try
    {
      validate();
    } catch (org.exolab.castor.xml.ValidationException vex)
    {
      return false;
    }
    return true;
  }

  /**
   * 
   * 
   * @param out
   * @throws org.exolab.castor.xml.MarshalException
   *           if object is null or if any SAXException is thrown during
   *           marshaling
   * @throws org.exolab.castor.xml.ValidationException
   *           if this object is an invalid instance according to the schema
   */
  public void marshal(final java.io.Writer out)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    Marshaller.marshal(this, out);
  }

  /**
   * 
   * 
   * @param handler
   * @throws java.io.IOException
   *           if an IOException occurs during marshaling
   * @throws org.exolab.castor.xml.ValidationException
   *           if this object is an invalid instance according to the schema
   * @throws org.exolab.castor.xml.MarshalException
   *           if object is null or if any SAXException is thrown during
   *           marshaling
   */
  public void marshal(final org.xml.sax.ContentHandler handler)
          throws java.io.IOException,
          org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    Marshaller.marshal(this, handler);
  }

  /**
   * Sets the value of field 'aboveThreshold'.
   * 
   * @param aboveThreshold
   *          the value of field 'aboveThreshold'.
   */
  public void setAboveThreshold(final int aboveThreshold)
  {
    this._aboveThreshold = aboveThreshold;
    this._has_aboveThreshold = true;
  }

  /**
   * Sets the value of field 'annotation'.
   * 
   * @param annotation
   *          the value of field 'annotation'.
   */
  public void setAnnotation(final java.lang.String annotation)
  {
    this._annotation = annotation;
  }

  /**
   * Sets the value of field 'colourScheme'.
   * 
   * @param colourScheme
   *          the value of field 'colourScheme'.
   */
  public void setColourScheme(final java.lang.String colourScheme)
  {
    this._colourScheme = colourScheme;
  }

  /**
   * Sets the value of field 'maxColour'.
   * 
   * @param maxColour
   *          the value of field 'maxColour'.
   */
  public void setMaxColour(final int maxColour)
  {
    this._maxColour = maxColour;
    this._has_maxColour = true;
  }

  /**
   * Sets the value of field 'minColour'.
   * 
   * @param minColour
   *          the value of field 'minColour'.
   */
  public void setMinColour(final int minColour)
  {
    this._minColour = minColour;
    this._has_minColour = true;
  }

  /**
   * Sets the value of field 'perSequence'.
   * 
   * @param perSequence
   *          the value of field 'perSequence'.
   */
  public void setPerSequence(final boolean perSequence)
  {
    this._perSequence = perSequence;
    this._has_perSequence = true;
  }

  /**
   * Sets the value of field 'predefinedColours'.
   * 
   * @param predefinedColours
   *          the value of field 'predefinedColours'.
   */
  public void setPredefinedColours(final boolean predefinedColours)
  {
    this._predefinedColours = predefinedColours;
    this._has_predefinedColours = true;
  }

  /**
   * Sets the value of field 'threshold'.
   * 
   * @param threshold
   *          the value of field 'threshold'.
   */
  public void setThreshold(final float threshold)
  {
    this._threshold = threshold;
    this._has_threshold = true;
  }

  /**
   * Method unmarshal.
   * 
   * @param reader
   * @throws org.exolab.castor.xml.MarshalException
   *           if object is null or if any SAXException is thrown during
   *           marshaling
   * @throws org.exolab.castor.xml.ValidationException
   *           if this object is an invalid instance according to the schema
   * @return the unmarshaled
   *         jalview.schemabinding.version2.AnnotationColourScheme
   */
  public static jalview.schemabinding.version2.AnnotationColourScheme unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.schemabinding.version2.AnnotationColourScheme) Unmarshaller
            .unmarshal(
                    jalview.schemabinding.version2.AnnotationColourScheme.class,
                    reader);
  }

  /**
   * 
   * 
   * @throws org.exolab.castor.xml.ValidationException
   *           if this object is an invalid instance according to the schema
   */
  public void validate() throws org.exolab.castor.xml.ValidationException
  {
    org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
    validator.validate(this);
  }

}
