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
 * Reference to a viewer showing RNA structure for this sequence. Schema
 * supports one viewer showing multiple annotations for multiple sequences,
 * though currently only one annotation for one sequence (gapped or trimmed) is
 * used
 * 
 * 
 * @version $Revision$ $Date$
 */
public class RnaViewer implements java.io.Serializable
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _title.
   */
  private java.lang.String _title;

  /**
   * An id unique to the RNA viewer panel
   * 
   */
  private java.lang.String _viewId;

  /**
   * horizontal position of split pane divider
   * 
   */
  private int _dividerLocation;

  /**
   * keeps track of state for field: _dividerLocation
   */
  private boolean _has_dividerLocation;

  /**
   * Index of the selected structure in the viewer panel
   * 
   */
  private int _selectedRna;

  /**
   * keeps track of state for field: _selectedRna
   */
  private boolean _has_selectedRna;

  /**
   * Field _width.
   */
  private int _width;

  /**
   * keeps track of state for field: _width
   */
  private boolean _has_width;

  /**
   * Field _height.
   */
  private int _height;

  /**
   * keeps track of state for field: _height
   */
  private boolean _has_height;

  /**
   * Field _xpos.
   */
  private int _xpos;

  /**
   * keeps track of state for field: _xpos
   */
  private boolean _has_xpos;

  /**
   * Field _ypos.
   */
  private int _ypos;

  /**
   * keeps track of state for field: _ypos
   */
  private boolean _has_ypos;

  /**
   * Field _secondaryStructureList.
   */
  private java.util.Vector _secondaryStructureList;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public RnaViewer()
  {
    super();
    this._secondaryStructureList = new java.util.Vector();
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * 
   * 
   * @param vSecondaryStructure
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addSecondaryStructure(
          final jalview.schemabinding.version2.SecondaryStructure vSecondaryStructure)
          throws java.lang.IndexOutOfBoundsException
  {
    this._secondaryStructureList.addElement(vSecondaryStructure);
  }

  /**
   * 
   * 
   * @param index
   * @param vSecondaryStructure
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void addSecondaryStructure(
          final int index,
          final jalview.schemabinding.version2.SecondaryStructure vSecondaryStructure)
          throws java.lang.IndexOutOfBoundsException
  {
    this._secondaryStructureList.add(index, vSecondaryStructure);
  }

  /**
     */
  public void deleteDividerLocation()
  {
    this._has_dividerLocation = false;
  }

  /**
     */
  public void deleteHeight()
  {
    this._has_height = false;
  }

  /**
     */
  public void deleteSelectedRna()
  {
    this._has_selectedRna = false;
  }

  /**
     */
  public void deleteWidth()
  {
    this._has_width = false;
  }

  /**
     */
  public void deleteXpos()
  {
    this._has_xpos = false;
  }

  /**
     */
  public void deleteYpos()
  {
    this._has_ypos = false;
  }

  /**
   * Method enumerateSecondaryStructure.
   * 
   * @return an Enumeration over all
   *         jalview.schemabinding.version2.SecondaryStructure elements
   */
  public java.util.Enumeration enumerateSecondaryStructure()
  {
    return this._secondaryStructureList.elements();
  }

  /**
   * Returns the value of field 'dividerLocation'. The field 'dividerLocation'
   * has the following description: horizontal position of split pane divider
   * 
   * 
   * @return the value of field 'DividerLocation'.
   */
  public int getDividerLocation()
  {
    return this._dividerLocation;
  }

  /**
   * Returns the value of field 'height'.
   * 
   * @return the value of field 'Height'.
   */
  public int getHeight()
  {
    return this._height;
  }

  /**
   * Method getSecondaryStructure.
   * 
   * @param index
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   * @return the value of the jalview.schemabinding.version2.SecondaryStructure
   *         at the given index
   */
  public jalview.schemabinding.version2.SecondaryStructure getSecondaryStructure(
          final int index) throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._secondaryStructureList.size())
    {
      throw new IndexOutOfBoundsException(
              "getSecondaryStructure: Index value '" + index
                      + "' not in range [0.."
                      + (this._secondaryStructureList.size() - 1) + "]");
    }

    return (jalview.schemabinding.version2.SecondaryStructure) _secondaryStructureList
            .get(index);
  }

  /**
   * Method getSecondaryStructure.Returns the contents of the collection in an
   * Array.
   * <p>
   * Note: Just in case the collection contents are changing in another thread,
   * we pass a 0-length Array of the correct type into the API call. This way we
   * <i>know</i> that the Array returned is of exactly the correct length.
   * 
   * @return this collection as an Array
   */
  public jalview.schemabinding.version2.SecondaryStructure[] getSecondaryStructure()
  {
    jalview.schemabinding.version2.SecondaryStructure[] array = new jalview.schemabinding.version2.SecondaryStructure[0];
    return (jalview.schemabinding.version2.SecondaryStructure[]) this._secondaryStructureList
            .toArray(array);
  }

  /**
   * Method getSecondaryStructureCount.
   * 
   * @return the size of this collection
   */
  public int getSecondaryStructureCount()
  {
    return this._secondaryStructureList.size();
  }

  /**
   * Returns the value of field 'selectedRna'. The field 'selectedRna' has the
   * following description: Index of the selected structure in the viewer panel
   * 
   * 
   * @return the value of field 'SelectedRna'.
   */
  public int getSelectedRna()
  {
    return this._selectedRna;
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
   * Returns the value of field 'viewId'. The field 'viewId' has the following
   * description: An id unique to the RNA viewer panel
   * 
   * 
   * @return the value of field 'ViewId'.
   */
  public java.lang.String getViewId()
  {
    return this._viewId;
  }

  /**
   * Returns the value of field 'width'.
   * 
   * @return the value of field 'Width'.
   */
  public int getWidth()
  {
    return this._width;
  }

  /**
   * Returns the value of field 'xpos'.
   * 
   * @return the value of field 'Xpos'.
   */
  public int getXpos()
  {
    return this._xpos;
  }

  /**
   * Returns the value of field 'ypos'.
   * 
   * @return the value of field 'Ypos'.
   */
  public int getYpos()
  {
    return this._ypos;
  }

  /**
   * Method hasDividerLocation.
   * 
   * @return true if at least one DividerLocation has been added
   */
  public boolean hasDividerLocation()
  {
    return this._has_dividerLocation;
  }

  /**
   * Method hasHeight.
   * 
   * @return true if at least one Height has been added
   */
  public boolean hasHeight()
  {
    return this._has_height;
  }

  /**
   * Method hasSelectedRna.
   * 
   * @return true if at least one SelectedRna has been added
   */
  public boolean hasSelectedRna()
  {
    return this._has_selectedRna;
  }

  /**
   * Method hasWidth.
   * 
   * @return true if at least one Width has been added
   */
  public boolean hasWidth()
  {
    return this._has_width;
  }

  /**
   * Method hasXpos.
   * 
   * @return true if at least one Xpos has been added
   */
  public boolean hasXpos()
  {
    return this._has_xpos;
  }

  /**
   * Method hasYpos.
   * 
   * @return true if at least one Ypos has been added
   */
  public boolean hasYpos()
  {
    return this._has_ypos;
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
  public void removeAllSecondaryStructure()
  {
    this._secondaryStructureList.clear();
  }

  /**
   * Method removeSecondaryStructure.
   * 
   * @param vSecondaryStructure
   * @return true if the object was removed from the collection.
   */
  public boolean removeSecondaryStructure(
          final jalview.schemabinding.version2.SecondaryStructure vSecondaryStructure)
  {
    boolean removed = _secondaryStructureList.remove(vSecondaryStructure);
    return removed;
  }

  /**
   * Method removeSecondaryStructureAt.
   * 
   * @param index
   * @return the element removed from the collection
   */
  public jalview.schemabinding.version2.SecondaryStructure removeSecondaryStructureAt(
          final int index)
  {
    java.lang.Object obj = this._secondaryStructureList.remove(index);
    return (jalview.schemabinding.version2.SecondaryStructure) obj;
  }

  /**
   * Sets the value of field 'dividerLocation'. The field 'dividerLocation' has
   * the following description: horizontal position of split pane divider
   * 
   * 
   * @param dividerLocation
   *          the value of field 'dividerLocation'.
   */
  public void setDividerLocation(final int dividerLocation)
  {
    this._dividerLocation = dividerLocation;
    this._has_dividerLocation = true;
  }

  /**
   * Sets the value of field 'height'.
   * 
   * @param height
   *          the value of field 'height'.
   */
  public void setHeight(final int height)
  {
    this._height = height;
    this._has_height = true;
  }

  /**
   * 
   * 
   * @param index
   * @param vSecondaryStructure
   * @throws java.lang.IndexOutOfBoundsException
   *           if the index given is outside the bounds of the collection
   */
  public void setSecondaryStructure(
          final int index,
          final jalview.schemabinding.version2.SecondaryStructure vSecondaryStructure)
          throws java.lang.IndexOutOfBoundsException
  {
    // check bounds for index
    if (index < 0 || index >= this._secondaryStructureList.size())
    {
      throw new IndexOutOfBoundsException(
              "setSecondaryStructure: Index value '" + index
                      + "' not in range [0.."
                      + (this._secondaryStructureList.size() - 1) + "]");
    }

    this._secondaryStructureList.set(index, vSecondaryStructure);
  }

  /**
   * 
   * 
   * @param vSecondaryStructureArray
   */
  public void setSecondaryStructure(
          final jalview.schemabinding.version2.SecondaryStructure[] vSecondaryStructureArray)
  {
    // -- copy array
    _secondaryStructureList.clear();

    for (int i = 0; i < vSecondaryStructureArray.length; i++)
    {
      this._secondaryStructureList.add(vSecondaryStructureArray[i]);
    }
  }

  /**
   * Sets the value of field 'selectedRna'. The field 'selectedRna' has the
   * following description: Index of the selected structure in the viewer panel
   * 
   * 
   * @param selectedRna
   *          the value of field 'selectedRna'.
   */
  public void setSelectedRna(final int selectedRna)
  {
    this._selectedRna = selectedRna;
    this._has_selectedRna = true;
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
   * Sets the value of field 'viewId'. The field 'viewId' has the following
   * description: An id unique to the RNA viewer panel
   * 
   * 
   * @param viewId
   *          the value of field 'viewId'.
   */
  public void setViewId(final java.lang.String viewId)
  {
    this._viewId = viewId;
  }

  /**
   * Sets the value of field 'width'.
   * 
   * @param width
   *          the value of field 'width'.
   */
  public void setWidth(final int width)
  {
    this._width = width;
    this._has_width = true;
  }

  /**
   * Sets the value of field 'xpos'.
   * 
   * @param xpos
   *          the value of field 'xpos'.
   */
  public void setXpos(final int xpos)
  {
    this._xpos = xpos;
    this._has_xpos = true;
  }

  /**
   * Sets the value of field 'ypos'.
   * 
   * @param ypos
   *          the value of field 'ypos'.
   */
  public void setYpos(final int ypos)
  {
    this._ypos = ypos;
    this._has_ypos = true;
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
   * @return the unmarshaled jalview.schemabinding.version2.RnaViewer
   */
  public static jalview.schemabinding.version2.RnaViewer unmarshal(
          final java.io.Reader reader)
          throws org.exolab.castor.xml.MarshalException,
          org.exolab.castor.xml.ValidationException
  {
    return (jalview.schemabinding.version2.RnaViewer) Unmarshaller
            .unmarshal(jalview.schemabinding.version2.RnaViewer.class,
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
