/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package vamsas.objects.simple;

public class SeqSearchResult_Helper
{
  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
          SeqSearchResult.class, true);

  static
  {
    typeDesc.setXmlType(new javax.xml.namespace.QName(
            "simple.objects.vamsas", "SeqSearchResult"));
    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("alignment");
    elemField.setXmlName(new javax.xml.namespace.QName("", "alignment"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "simple.objects.vamsas", "Alignment"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("annotation");
    elemField.setXmlName(new javax.xml.namespace.QName("", "annotation"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("features");
    elemField.setXmlName(new javax.xml.namespace.QName("", "features"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("newickTree");
    elemField.setXmlName(new javax.xml.namespace.QName("", "newickTree"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
  }

  /**
   * Return type metadata object
   */
  public static org.apache.axis.description.TypeDesc getTypeDesc()
  {
    return typeDesc;
  }

  /**
   * Get Custom Serializer
   */
  public static org.apache.axis.encoding.Serializer getSerializer(
          java.lang.String mechType, java.lang.Class _javaType,
          javax.xml.namespace.QName _xmlType)
  {
    return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
            _xmlType, typeDesc);
  }

  /**
   * Get Custom Deserializer
   */
  public static org.apache.axis.encoding.Deserializer getDeserializer(
          java.lang.String mechType, java.lang.Class _javaType,
          javax.xml.namespace.QName _xmlType)
  {
    return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
            _xmlType, typeDesc);
  }

}
