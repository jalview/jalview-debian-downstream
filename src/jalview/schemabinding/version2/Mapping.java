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
 * Class Mapping.
 * 
 * @version $Revision$ $Date$
 */
public class Mapping extends jalview.schemabinding.version2.MapListType
        implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Internal choice value storage
   */
  private java.lang.Object _choiceValue;

  /**
   * Field _mappingChoice.
   */
  private jalview.schemabinding.version2.MappingChoice _mappingChoice;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public Mapping()
  {
    super();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * Returns the value of field 'choiceValue'. The field 'choiceValue' has the
   * following description: Internal choice value storage
   * 
   * @return the value of field 'ChoiceValue'.
   */
  public java.lang.Object getChoiceValue()
  {
    return this._choiceValue;
  }

  /**
   * Returns the value of field 'mappingChoice'.
   * 
   * @return the value of field 'MappingChoice'.
   */
  public jalview.schemabinding.version2.MappingChoice getMappingChoice()
  {
    return this._mappingChoice;
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
   * Sets the value of field 'mappingChoice'.
   * 
   * @param mappingChoice
   *          the value of field 'mappingChoice'.
   */
  public void setMappingChoice(
          final jalview.schemabinding.version2.MappingChoice mappingChoice)
  {
    this._mappingChoice = mappingChoice;
    this._choiceValue = mappingChoice;
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
   * @return the unmarshaled jalview.schemabinding.version2.MapListType
   */
  public static jalview.schemabinding.version2.MapListType unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.schemabinding.version2.MapListType) Unmarshaller
            .unmarshal(jalview.schemabinding.version2.Mapping.class, reader);
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
