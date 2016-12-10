/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1</a>, using an XML
 * Schema.
 * $Id$
 */

package jalview.schemabinding.version2;

/**
 * Class PdbentryItem.
 * 
 * @version $Revision$ $Date$
 */
public class PdbentryItem implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _propertyList.
   */
  private java.util.Vector _propertyList;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public PdbentryItem()
  {
    super();
    this._propertyList = new java.util.Vector();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * 
   * 
   * @param vProperty
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addProperty(
          final jalview.schemabinding.version2.Property vProperty)
          throws java.lang.IndexOutOfBoundsException
  {
    this._propertyList.addElement(vProperty);
  }

  /**
   * 
   * 
   * @param index
   * @param vProperty
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addProperty(final int index,
          final jalview.schemabinding.version2.Property vProperty)
          throws java.lang.IndexOutOfBoundsException
  {
    this._propertyList.add(index, vProperty);
  }

  /**
   * Method enumerateProperty.
   * 
   * @return an Enumeration over all jalview.schemabinding.version2.Property
   *         elements
   */
  public java.util.Enumeration enumerateProperty()
  {
    return this._propertyList.elements();
  }

  /**
   * Method getProperty.
   * 
   * @param index
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   * @return the value of the jalview.schemabinding.version2.Property at the
   *         given index
   */
  public jalview.schemabinding.version2.Property getProperty(final int index)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._propertyList.size())
    {
      throw new IndexOutOfBoundsException("getProperty: Index value '"
              + index + "' not in range [0.."
              + (this._propertyList.size() - 1) + "]");
    }

    return (jalview.schemabinding.version2.Property) _propertyList
            .get(index);
  }

  /**
   * Method getProperty.Returns the contents of the collection in an Array.
   * <p>
   * Note: Just in case the collection contents are changing in another thread,
   * we pass a 0-length Array of the correct type into the API call. This way we
   * <i>know</i> that the Array returned is of exactly the correct length.
   * 
   * @return this collection as an Array
   */
  public jalview.schemabinding.version2.Property[] getProperty()
  {
    jalview.schemabinding.version2.Property[] array = new jalview.schemabinding.version2.Property[0];
    return (jalview.schemabinding.version2.Property[]) this._propertyList
            .toArray(array);
  }

  /**
   * Method getPropertyCount.
   * 
   * @return the size of this collection
   */
  public int getPropertyCount()
  {
    return this._propertyList.size();
  }

  /**
     */
  public void removeAllProperty()
  {
    this._propertyList.clear();
  }

  /**
   * Method removeProperty.
   * 
   * @param vProperty
   * @return true if the object was removed from the collection.
   */
  public boolean removeProperty(
          final jalview.schemabinding.version2.Property vProperty)
  {
    boolean removed = _propertyList.remove(vProperty);
    return removed;
  }

  /**
   * Method removePropertyAt.
   * 
   * @param index
   * @return the element removed from the collection
   */
  public jalview.schemabinding.version2.Property removePropertyAt(
          final int index)
  {
    java.lang.Object obj = this._propertyList.remove(index);
    return (jalview.schemabinding.version2.Property) obj;
  }

  /**
   * 
   * 
   * @param index
   * @param vProperty
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void setProperty(final int index,
          final jalview.schemabinding.version2.Property vProperty)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._propertyList.size())
    {
      throw new IndexOutOfBoundsException("setProperty: Index value '"
              + index + "' not in range [0.."
              + (this._propertyList.size() - 1) + "]");
    }

    this._propertyList.set(index, vProperty);
  }

  /**
   * 
   * 
   * @param vPropertyArray
   */
  public void setProperty(
          final jalview.schemabinding.version2.Property[] vPropertyArray)
  {
    // -- copy array
    _propertyList.clear();

    for (int i = 0; i < vPropertyArray.length; i++)
    {
      this._propertyList.add(vPropertyArray[i]);
    }
  }

}
