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
package uk.ac.ebi.picr.model;

public class UPEntry_Helper
{
  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
          UPEntry.class, true);

  static
  {
    typeDesc.setXmlType(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "UPEntry"));
    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("CRC64");
    elemField.setXmlName(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "CRC64"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("UPI");
    elemField.setXmlName(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "UPI"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("identicalCrossReferences");
    elemField.setXmlName(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "identicalCrossReferences"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "CrossReference"));
    elemField.setMinOccurs(0);
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("logicalCrossReferences");
    elemField.setXmlName(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "logicalCrossReferences"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "CrossReference"));
    elemField.setMinOccurs(0);
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("sequence");
    elemField.setXmlName(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "sequence"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("timestamp");
    elemField.setXmlName(new javax.xml.namespace.QName(
            "http://model.picr.ebi.ac.uk", "timestamp"));
    elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "dateTime"));
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
