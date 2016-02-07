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
 * Class Sequence.
 * 
 * @version $Revision$ $Date$
 */
public class Sequence extends jalview.schemabinding.version2.SequenceType
        implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * dataset sequence id for this sequence. Will be created as union of
   * sequences.
   * 
   */
  private java.lang.String _dsseqid;

  /**
   * Field _DBRefList.
   */
  private java.util.Vector _DBRefList;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public Sequence()
  {
    super();
    this._DBRefList = new java.util.Vector();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * 
   * 
   * @param vDBRef
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addDBRef(final jalview.schemabinding.version2.DBRef vDBRef)
          throws java.lang.IndexOutOfBoundsException
  {
    this._DBRefList.addElement(vDBRef);
  }

  /**
   * 
   * 
   * @param index
   * @param vDBRef
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addDBRef(final int index,
          final jalview.schemabinding.version2.DBRef vDBRef)
          throws java.lang.IndexOutOfBoundsException
  {
    this._DBRefList.add(index, vDBRef);
  }

  /**
   * Method enumerateDBRef.
   * 
   * @return an Enumeration over all jalview.schemabinding.version2.DBRef
   *         elements
   */
  public java.util.Enumeration enumerateDBRef()
  {
    return this._DBRefList.elements();
  }

  /**
   * Method getDBRef.
   * 
   * @param index
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   * @return the value of the jalview.schemabinding.version2.DBRef at the given
   *         index
   */
  public jalview.schemabinding.version2.DBRef getDBRef(final int index)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._DBRefList.size())
    {
      throw new IndexOutOfBoundsException("getDBRef: Index value '" + index
              + "' not in range [0.." + (this._DBRefList.size() - 1) + "]");
    }

    return (jalview.schemabinding.version2.DBRef) _DBRefList.get(index);
  }

  /**
   * Method getDBRef.Returns the contents of the collection in an Array.
   * <p>
   * Note: Just in case the collection contents are changing in another thread,
   * we pass a 0-length Array of the correct type into the API call. This way we
   * <i>know</i> that the Array returned is of exactly the correct length.
   * 
   * @return this collection as an Array
   */
  public jalview.schemabinding.version2.DBRef[] getDBRef()
  {
    jalview.schemabinding.version2.DBRef[] array = new jalview.schemabinding.version2.DBRef[0];
    return (jalview.schemabinding.version2.DBRef[]) this._DBRefList
            .toArray(array);
  }

  /**
   * Method getDBRefCount.
   * 
   * @return the size of this collection
   */
  public int getDBRefCount()
  {
    return this._DBRefList.size();
  }

  /**
   * Returns the value of field 'dsseqid'. The field 'dsseqid' has the following
   * description: dataset sequence id for this sequence. Will be created as
   * union of sequences.
   * 
   * 
   * @return the value of field 'Dsseqid'.
   */
  public java.lang.String getDsseqid()
  {
    return this._dsseqid;
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
  public void removeAllDBRef()
  {
    this._DBRefList.clear();
  }

  /**
   * Method removeDBRef.
   * 
   * @param vDBRef
   * @return true if the object was removed from the collection.
   */
  public boolean removeDBRef(
          final jalview.schemabinding.version2.DBRef vDBRef)
  {
    boolean removed = _DBRefList.remove(vDBRef);
    return removed;
  }

  /**
   * Method removeDBRefAt.
   * 
   * @param index
   * @return the element removed from the collection
   */
  public jalview.schemabinding.version2.DBRef removeDBRefAt(final int index)
  {
    java.lang.Object obj = this._DBRefList.remove(index);
    return (jalview.schemabinding.version2.DBRef) obj;
  }

  /**
   * 
   * 
   * @param index
   * @param vDBRef
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void setDBRef(final int index,
          final jalview.schemabinding.version2.DBRef vDBRef)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._DBRefList.size())
    {
      throw new IndexOutOfBoundsException("setDBRef: Index value '" + index
              + "' not in range [0.." + (this._DBRefList.size() - 1) + "]");
    }

    this._DBRefList.set(index, vDBRef);
  }

  /**
   * 
   * 
   * @param vDBRefArray
   */
  public void setDBRef(
          final jalview.schemabinding.version2.DBRef[] vDBRefArray)
  {
    // -- copy array
    _DBRefList.clear();

    for (int i = 0; i < vDBRefArray.length; i++)
    {
      this._DBRefList.add(vDBRefArray[i]);
    }
  }

  /**
   * Sets the value of field 'dsseqid'. The field 'dsseqid' has the following
   * description: dataset sequence id for this sequence. Will be created as
   * union of sequences.
   * 
   * 
   * @param dsseqid
   *          the value of field 'dsseqid'.
   */
  public void setDsseqid(final java.lang.String dsseqid)
  {
    this._dsseqid = dsseqid;
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
   * @return the unmarshaled jalview.schemabinding.version2.SequenceType
   */
  public static jalview.schemabinding.version2.SequenceType unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.schemabinding.version2.SequenceType) Unmarshaller
            .unmarshal(jalview.schemabinding.version2.Sequence.class,
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
