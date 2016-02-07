/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1</a>, using an XML
 * Schema.
 * $Id$
 */

package jalview.schemabinding.version2.descriptors;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import jalview.schemabinding.version2.RnaViewer;

/**
 * Class RnaViewerDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class RnaViewerDescriptor extends
        org.exolab.castor.xml.util.XMLClassDescriptorImpl
{

  // --------------------------/
  // - Class/Member Variables -/
  // --------------------------/

  /**
   * Field _elementDefinition.
   */
  private boolean _elementDefinition;

  /**
   * Field _nsPrefix.
   */
  private java.lang.String _nsPrefix;

  /**
   * Field _nsURI.
   */
  private java.lang.String _nsURI;

  /**
   * Field _xmlName.
   */
  private java.lang.String _xmlName;

  // ----------------/
  // - Constructors -/
  // ----------------/

  public RnaViewerDescriptor()
  {
    super();
    _nsURI = "www.jalview.org";
    _xmlName = "rnaViewer";
    _elementDefinition = true;

    // -- set grouping compositor
    setCompositorAsSequence();
    org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
    org.exolab.castor.mapping.FieldHandler handler = null;
    org.exolab.castor.xml.FieldValidator fieldValidator = null;
    // -- initialize attribute descriptors

    // -- _title
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.String.class, "_title", "title",
            org.exolab.castor.xml.NodeType.Attribute);
    desc.setImmutable(true);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        return target.getTitle();
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          target.setTitle((java.lang.String) value);
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _title
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.StringValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.StringValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setWhiteSpace("preserve");
    }
    desc.setValidator(fieldValidator);
    // -- _viewId
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.String.class, "_viewId", "viewId",
            org.exolab.castor.xml.NodeType.Attribute);
    desc.setImmutable(true);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        return target.getViewId();
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          target.setViewId((java.lang.String) value);
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _viewId
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.StringValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.StringValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setWhiteSpace("preserve");
    }
    desc.setValidator(fieldValidator);
    // -- _dividerLocation
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_dividerLocation", "dividerLocation",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        if (!target.hasDividerLocation())
        {
          return null;
        }
        return new java.lang.Integer(target.getDividerLocation());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteDividerLocation();
            return;
          }
          target.setDividerLocation(((java.lang.Integer) value).intValue());
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _dividerLocation
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _selectedRna
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_selectedRna", "selectedRna",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        if (!target.hasSelectedRna())
        {
          return null;
        }
        return new java.lang.Integer(target.getSelectedRna());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteSelectedRna();
            return;
          }
          target.setSelectedRna(((java.lang.Integer) value).intValue());
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _selectedRna
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _width
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_width", "width",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        if (!target.hasWidth())
        {
          return null;
        }
        return new java.lang.Integer(target.getWidth());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteWidth();
            return;
          }
          target.setWidth(((java.lang.Integer) value).intValue());
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _width
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _height
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_height", "height",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        if (!target.hasHeight())
        {
          return null;
        }
        return new java.lang.Integer(target.getHeight());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteHeight();
            return;
          }
          target.setHeight(((java.lang.Integer) value).intValue());
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _height
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _xpos
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_xpos", "xpos",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        if (!target.hasXpos())
        {
          return null;
        }
        return new java.lang.Integer(target.getXpos());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteXpos();
            return;
          }
          target.setXpos(((java.lang.Integer) value).intValue());
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _xpos
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _ypos
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_ypos", "ypos",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        if (!target.hasYpos())
        {
          return null;
        }
        return new java.lang.Integer(target.getYpos());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteYpos();
            return;
          }
          target.setYpos(((java.lang.Integer) value).intValue());
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return null;
      }
    };
    desc.setHandler(handler);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _ypos
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- initialize element descriptors

    // -- _secondaryStructureList
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            jalview.schemabinding.version2.SecondaryStructure.class,
            "_secondaryStructureList", "secondaryStructure",
            org.exolab.castor.xml.NodeType.Element);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        RnaViewer target = (RnaViewer) object;
        return target.getSecondaryStructure();
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          target.addSecondaryStructure((jalview.schemabinding.version2.SecondaryStructure) value);
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public void resetValue(Object object) throws IllegalStateException,
              IllegalArgumentException
      {
        try
        {
          RnaViewer target = (RnaViewer) object;
          target.removeAllSecondaryStructure();
        } catch (java.lang.Exception ex)
        {
          throw new IllegalStateException(ex.toString());
        }
      }

      public java.lang.Object newInstance(java.lang.Object parent)
      {
        return new jalview.schemabinding.version2.SecondaryStructure();
      }
    };
    desc.setHandler(handler);
    desc.setNameSpaceURI("www.jalview.org");
    desc.setRequired(true);
    desc.setMultivalued(true);
    addFieldDescriptor(desc);

    // -- validation code for: _secondaryStructureList
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    fieldValidator.setMinOccurs(1);
    { // -- local scope
    }
    desc.setValidator(fieldValidator);
  }

  // -----------/
  // - Methods -/
  // -----------/

  /**
   * Method getAccessMode.
   * 
   * @return the access mode specified for this class.
   */
  public org.exolab.castor.mapping.AccessMode getAccessMode()
  {
    return null;
  }

  /**
   * Method getIdentity.
   * 
   * @return the identity field, null if this class has no identity.
   */
  public org.exolab.castor.mapping.FieldDescriptor getIdentity()
  {
    return super.getIdentity();
  }

  /**
   * Method getJavaClass.
   * 
   * @return the Java class represented by this descriptor.
   */
  public java.lang.Class getJavaClass()
  {
    return jalview.schemabinding.version2.RnaViewer.class;
  }

  /**
   * Method getNameSpacePrefix.
   * 
   * @return the namespace prefix to use when marshaling as XML.
   */
  public java.lang.String getNameSpacePrefix()
  {
    return _nsPrefix;
  }

  /**
   * Method getNameSpaceURI.
   * 
   * @return the namespace URI used when marshaling and unmarshaling as XML.
   */
  public java.lang.String getNameSpaceURI()
  {
    return _nsURI;
  }

  /**
   * Method getValidator.
   * 
   * @return a specific validator for the class described by this
   *         ClassDescriptor.
   */
  public org.exolab.castor.xml.TypeValidator getValidator()
  {
    return this;
  }

  /**
   * Method getXMLName.
   * 
   * @return the XML Name for the Class being described.
   */
  public java.lang.String getXMLName()
  {
    return _xmlName;
  }

  /**
   * Method isElementDefinition.
   * 
   * @return true if XML schema definition of this Class is that of a global
   *         element or element with anonymous type definition.
   */
  public boolean isElementDefinition()
  {
    return _elementDefinition;
  }

}
