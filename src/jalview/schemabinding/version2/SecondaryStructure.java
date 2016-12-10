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
 * Class SecondaryStructure.
 * 
 * @version $Revision$ $Date$
 */
public class SecondaryStructure implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _title.
   */
  private java.lang.String _title;

  /**
   * id attribute of Annotation in vamsasModel for the secondary structure
   * annotation shown in the viewer
   * 
   */
  private java.lang.String _annotationId;

  /**
   * if true the RNA structure is shown with gaps, if false without
   * 
   */
  private boolean _gapped;

  /**
   * keeps track of state for field: _gapped
   */
  private boolean _has_gapped;

  /**
   * name of the project jar entry that holds the VARNA viewer state for the
   * structure
   * 
   */
  private java.lang.String _viewerState;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public SecondaryStructure()
  {
    super();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
     */
  public void deleteGapped()
  {
    this._has_gapped = false;
  }

  /**
   * Returns the value of field 'annotationId'. The field 'annotationId' has the
   * following description: id attribute of Annotation in vamsasModel for the
   * secondary structure annotation shown in the viewer
   * 
   * 
   * @return the value of field 'AnnotationId'.
   */
  public java.lang.String getAnnotationId()
  {
    return this._annotationId;
  }

  /**
   * Returns the value of field 'gapped'. The field 'gapped' has the following
   * description: if true the RNA structure is shown with gaps, if false without
   * 
   * 
   * @return the value of field 'Gapped'.
   */
  public boolean getGapped()
  {
    return this._gapped;
  }

  /**
   * Returns the value of field 'title'.
   * 
   * @return the value of field 'Title'.
   */
  public java.lang.String getTitle()
  {
    return this._title;
  }

  /**
   * Returns the value of field 'viewerState'. The field 'viewerState' has the
   * following description: name of the project jar entry that holds the VARNA
   * viewer state for the structure
   * 
   * 
   * @return the value of field 'ViewerState'.
   */
  public java.lang.String getViewerState()
  {
    return this._viewerState;
  }

  /**
   * Method hasGapped.
   * 
   * @return true if at least one Gapped has been added
   */
  public boolean hasGapped()
  {
    return this._has_gapped;
  }

  /**
   * Returns the value of field 'gapped'. The field 'gapped' has the following
   * description: if true the RNA structure is shown with gaps, if false without
   * 
   * 
   * @return the value of field 'Gapped'.
   */
  public boolean isGapped()
  {
    return this._gapped;
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
   * Sets the value of field 'annotationId'. The field 'annotationId' has the
   * following description: id attribute of Annotation in vamsasModel for the
   * secondary structure annotation shown in the viewer
   * 
   * 
   * @param annotationId
   *          the value of field 'annotationId'.
   */
  public void setAnnotationId(final java.lang.String annotationId)
  {
    this._annotationId = annotationId;
  }

  /**
   * Sets the value of field 'gapped'. The field 'gapped' has the following
   * description: if true the RNA structure is shown with gaps, if false without
   * 
   * 
   * @param gapped
   *          the value of field 'gapped'.
   */
  public void setGapped(final boolean gapped)
  {
    this._gapped = gapped;
    this._has_gapped = true;
  }

  /**
   * Sets the value of field 'title'.
   * 
   * @param title
   *          the value of field 'title'.
   */
  public void setTitle(final java.lang.String title)
  {
    this._title = title;
  }

  /**
   * Sets the value of field 'viewerState'. The field 'viewerState' has the
   * following description: name of the project jar entry that holds the VARNA
   * viewer state for the structure
   * 
   * 
   * @param viewerState
   *          the value of field 'viewerState'.
   */
  public void setViewerState(final java.lang.String viewerState)
  {
    this._viewerState = viewerState;
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
   * @return the unmarshaled jalview.schemabinding.version2.SecondaryStructure
   */
  public static jalview.schemabinding.version2.SecondaryStructure unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.schemabinding.version2.SecondaryStructure) Unmarshaller
            .unmarshal(
                    jalview.schemabinding.version2.SecondaryStructure.class,
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
