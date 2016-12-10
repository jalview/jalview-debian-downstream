/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1</a>, using an XML
 * Schema.
 * $Id$
 */

package jalview.binding;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Alignment.
 * 
 * @version $Revision$ $Date$
 */
public class Alignment implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _annotation.
   */
  private jalview.binding.Annotation _annotation;

  /**
   * Field _sequenceSet.
   */
  private jalview.binding.SequenceSet _sequenceSet;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public Alignment()
  {
    super();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * Returns the value of field 'annotation'.
   * 
   * @return the value of field 'Annotation'.
   */
  public jalview.binding.Annotation getAnnotation()
  {
    return this._annotation;
  }

  /**
   * Returns the value of field 'sequenceSet'.
   * 
   * @return the value of field 'SequenceSet'.
   */
  public jalview.binding.SequenceSet getSequenceSet()
  {
    return this._sequenceSet;
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
   * Sets the value of field 'annotation'.
   * 
   * @param annotation
   *          the value of field 'annotation'.
   */
  public void setAnnotation(final jalview.binding.Annotation annotation)
  {
    this._annotation = annotation;
  }

  /**
   * Sets the value of field 'sequenceSet'.
   * 
   * @param sequenceSet
   *          the value of field 'sequenceSet'.
   */
  public void setSequenceSet(final jalview.binding.SequenceSet sequenceSet)
  {
    this._sequenceSet = sequenceSet;
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
   * @return the unmarshaled jalview.binding.Alignment
   */
  public static jalview.binding.Alignment unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.binding.Alignment) Unmarshaller.unmarshal(
            jalview.binding.Alignment.class, reader);
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
