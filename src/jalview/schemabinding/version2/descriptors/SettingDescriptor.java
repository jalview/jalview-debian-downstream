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

import jalview.schemabinding.version2.Setting;

/**
 * Class SettingDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class SettingDescriptor extends
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

  public SettingDescriptor()
  {
    super();
    _nsURI = "www.jalview.org";
    _xmlName = "setting";
    _elementDefinition = true;
    org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
    org.exolab.castor.mapping.FieldHandler handler = null;
    org.exolab.castor.xml.FieldValidator fieldValidator = null;
    // -- initialize attribute descriptors

    // -- _type
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.String.class, "_type", "type",
            org.exolab.castor.xml.NodeType.Attribute);
    desc.setImmutable(true);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        return target.getType();
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          target.setType((java.lang.String) value);
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
    desc.setRequired(true);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _type
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    fieldValidator.setMinOccurs(1);
    { // -- local scope
      org.exolab.castor.xml.validators.StringValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.StringValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setWhiteSpace("preserve");
    }
    desc.setValidator(fieldValidator);
    // -- _colour
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_colour", "colour",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasColour())
        {
          return null;
        }
        return new java.lang.Integer(target.getColour());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // ignore null values for non optional primitives
          if (value == null)
          {
            return;
          }

          target.setColour(((java.lang.Integer) value).intValue());
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
    desc.setRequired(true);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _colour
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    fieldValidator.setMinOccurs(1);
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _display
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Boolean.TYPE, "_display", "display",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasDisplay())
        {
          return null;
        }
        return (target.getDisplay() ? java.lang.Boolean.TRUE
                : java.lang.Boolean.FALSE);
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // ignore null values for non optional primitives
          if (value == null)
          {
            return;
          }

          target.setDisplay(((java.lang.Boolean) value).booleanValue());
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
    desc.setRequired(true);
    desc.setMultivalued(false);
    addFieldDescriptor(desc);

    // -- validation code for: _display
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    fieldValidator.setMinOccurs(1);
    { // -- local scope
      org.exolab.castor.xml.validators.BooleanValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.BooleanValidator();
      fieldValidator.setValidator(typeValidator);
    }
    desc.setValidator(fieldValidator);
    // -- _order
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Float.TYPE, "_order", "order",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasOrder())
        {
          return null;
        }
        return new java.lang.Float(target.getOrder());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteOrder();
            return;
          }
          target.setOrder(((java.lang.Float) value).floatValue());
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

    // -- validation code for: _order
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.FloatValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.FloatValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive((float) -3.4028235E38);
      typeValidator.setMaxInclusive((float) 3.4028235E38);
    }
    desc.setValidator(fieldValidator);
    // -- _mincolour
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_mincolour", "mincolour",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasMincolour())
        {
          return null;
        }
        return new java.lang.Integer(target.getMincolour());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteMincolour();
            return;
          }
          target.setMincolour(((java.lang.Integer) value).intValue());
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

    // -- validation code for: _mincolour
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _threshold
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Float.TYPE, "_threshold", "threshold",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasThreshold())
        {
          return null;
        }
        return new java.lang.Float(target.getThreshold());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteThreshold();
            return;
          }
          target.setThreshold(((java.lang.Float) value).floatValue());
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

    // -- validation code for: _threshold
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.FloatValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.FloatValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive((float) -3.4028235E38);
      typeValidator.setMaxInclusive((float) 3.4028235E38);
    }
    desc.setValidator(fieldValidator);
    // -- _threshstate
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Integer.TYPE, "_threshstate", "threshstate",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasThreshstate())
        {
          return null;
        }
        return new java.lang.Integer(target.getThreshstate());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteThreshstate();
            return;
          }
          target.setThreshstate(((java.lang.Integer) value).intValue());
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

    // -- validation code for: _threshstate
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.IntValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.IntValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive(-2147483648);
      typeValidator.setMaxInclusive(2147483647);
    }
    desc.setValidator(fieldValidator);
    // -- _max
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Float.TYPE, "_max", "max",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasMax())
        {
          return null;
        }
        return new java.lang.Float(target.getMax());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteMax();
            return;
          }
          target.setMax(((java.lang.Float) value).floatValue());
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

    // -- validation code for: _max
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.FloatValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.FloatValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive((float) -3.4028235E38);
      typeValidator.setMaxInclusive((float) 3.4028235E38);
    }
    desc.setValidator(fieldValidator);
    // -- _min
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Float.TYPE, "_min", "min",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasMin())
        {
          return null;
        }
        return new java.lang.Float(target.getMin());
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteMin();
            return;
          }
          target.setMin(((java.lang.Float) value).floatValue());
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

    // -- validation code for: _min
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.FloatValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.FloatValidator();
      fieldValidator.setValidator(typeValidator);
      typeValidator.setMinInclusive((float) -3.4028235E38);
      typeValidator.setMaxInclusive((float) 3.4028235E38);
    }
    desc.setValidator(fieldValidator);
    // -- _colourByLabel
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Boolean.TYPE, "_colourByLabel", "colourByLabel",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasColourByLabel())
        {
          return null;
        }
        return (target.getColourByLabel() ? java.lang.Boolean.TRUE
                : java.lang.Boolean.FALSE);
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteColourByLabel();
            return;
          }
          target.setColourByLabel(((java.lang.Boolean) value)
                  .booleanValue());
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

    // -- validation code for: _colourByLabel
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.BooleanValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.BooleanValidator();
      fieldValidator.setValidator(typeValidator);
    }
    desc.setValidator(fieldValidator);
    // -- _autoScale
    desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.lang.Boolean.TYPE, "_autoScale", "autoScale",
            org.exolab.castor.xml.NodeType.Attribute);
    handler = new org.exolab.castor.xml.XMLFieldHandler()
    {
      public java.lang.Object getValue(java.lang.Object object)
              throws IllegalStateException
      {
        Setting target = (Setting) object;
        if (!target.hasAutoScale())
        {
          return null;
        }
        return (target.getAutoScale() ? java.lang.Boolean.TRUE
                : java.lang.Boolean.FALSE);
      }

      public void setValue(java.lang.Object object, java.lang.Object value)
              throws IllegalStateException, IllegalArgumentException
      {
        try
        {
          Setting target = (Setting) object;
          // if null, use delete method for optional primitives
          if (value == null)
          {
            target.deleteAutoScale();
            return;
          }
          target.setAutoScale(((java.lang.Boolean) value).booleanValue());
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

    // -- validation code for: _autoScale
    fieldValidator = new org.exolab.castor.xml.FieldValidator();
    { // -- local scope
      org.exolab.castor.xml.validators.BooleanValidator typeValidator;
      typeValidator = new org.exolab.castor.xml.validators.BooleanValidator();
      fieldValidator.setValidator(typeValidator);
    }
    desc.setValidator(fieldValidator);
    // -- initialize element descriptors

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
    return jalview.schemabinding.version2.Setting.class;
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
