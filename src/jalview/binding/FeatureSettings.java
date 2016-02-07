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
 * Class FeatureSettings.
 * 
 * @version $Revision$ $Date$
 */
public class FeatureSettings implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _settingList.
   */
  private java.util.Vector _settingList;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public FeatureSettings()
  {
    super();
    this._settingList = new java.util.Vector();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * 
   * 
   * @param vSetting
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addSetting(final jalview.binding.Setting vSetting)
          throws java.lang.IndexOutOfBoundsException
  {
    this._settingList.addElement(vSetting);
  }

  /**
   * 
   * 
   * @param index
   * @param vSetting
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addSetting(final int index,
          final jalview.binding.Setting vSetting)
          throws java.lang.IndexOutOfBoundsException
  {
    this._settingList.add(index, vSetting);
  }

  /**
   * Method enumerateSetting.
   * 
   * @return an Enumeration over all jalview.binding.Setting elements
   */
  public java.util.Enumeration enumerateSetting()
  {
    return this._settingList.elements();
  }

  /**
   * Method getSetting.
   * 
   * @param index
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   * @return the value of the jalview.binding.Setting at the given index
   */
  public jalview.binding.Setting getSetting(final int index)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._settingList.size())
    {
      throw new IndexOutOfBoundsException("getSetting: Index value '"
              + index + "' not in range [0.."
              + (this._settingList.size() - 1) + "]");
    }

    return (jalview.binding.Setting) _settingList.get(index);
  }

  /**
   * Method getSetting.Returns the contents of the collection in an Array.
   * <p>
   * Note: Just in case the collection contents are changing in another thread,
   * we pass a 0-length Array of the correct type into the API call. This way we
   * <i>know</i> that the Array returned is of exactly the correct length.
   * 
   * @return this collection as an Array
   */
  public jalview.binding.Setting[] getSetting()
  {
    jalview.binding.Setting[] array = new jalview.binding.Setting[0];
    return (jalview.binding.Setting[]) this._settingList.toArray(array);
  }

  /**
   * Method getSettingCount.
   * 
   * @return the size of this collection
   */
  public int getSettingCount()
  {
    return this._settingList.size();
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
     */
  public void removeAllSetting()
  {
    this._settingList.clear();
  }

  /**
   * Method removeSetting.
   * 
   * @param vSetting
   * @return true if the object was removed from the collection.
   */
  public boolean removeSetting(final jalview.binding.Setting vSetting)
  {
    boolean removed = _settingList.remove(vSetting);
    return removed;
  }

  /**
   * Method removeSettingAt.
   * 
   * @param index
   * @return the element removed from the collection
   */
  public jalview.binding.Setting removeSettingAt(final int index)
  {
    java.lang.Object obj = this._settingList.remove(index);
    return (jalview.binding.Setting) obj;
  }

  /**
   * 
   * 
   * @param index
   * @param vSetting
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void setSetting(final int index,
          final jalview.binding.Setting vSetting)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._settingList.size())
    {
      throw new IndexOutOfBoundsException("setSetting: Index value '"
              + index + "' not in range [0.."
              + (this._settingList.size() - 1) + "]");
    }

    this._settingList.set(index, vSetting);
  }

  /**
   * 
   * 
   * @param vSettingArray
   */
  public void setSetting(final jalview.binding.Setting[] vSettingArray)
  {
    // -- copy array
    _settingList.clear();

    for (int i = 0; i < vSettingArray.length; i++)
    {
      this._settingList.add(vSettingArray[i]);
    }
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
   * @return the unmarshaled jalview.binding.FeatureSettings
   */
  public static jalview.binding.FeatureSettings unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.binding.FeatureSettings) Unmarshaller.unmarshal(
            jalview.binding.FeatureSettings.class, reader);
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
