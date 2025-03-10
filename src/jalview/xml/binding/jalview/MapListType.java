//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.06.07 at 02:21:15 PM BST 
//


package jalview.xml.binding.jalview;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				This effectively represents a java.util.MapList object
 * 			
 * 
 * <p>Java class for mapListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mapListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mapListFrom" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="mapListTo" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="mapFromUnit" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="mapToUnit" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mapListType", propOrder = {
    "mapListFrom",
    "mapListTo"
})
@XmlSeeAlso({
    Mapping.class
})
public class MapListType {

    protected List<MapListType.MapListFrom> mapListFrom;
    protected List<MapListType.MapListTo> mapListTo;
    @XmlAttribute(name = "mapFromUnit", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger mapFromUnit;
    @XmlAttribute(name = "mapToUnit", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger mapToUnit;

    /**
     * Gets the value of the mapListFrom property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapListFrom property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapListFrom().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MapListType.MapListFrom }
     * 
     * 
     */
    public List<MapListType.MapListFrom> getMapListFrom() {
        if (mapListFrom == null) {
            mapListFrom = new ArrayList<MapListType.MapListFrom>();
        }
        return this.mapListFrom;
    }

    /**
     * Gets the value of the mapListTo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapListTo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapListTo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MapListType.MapListTo }
     * 
     * 
     */
    public List<MapListType.MapListTo> getMapListTo() {
        if (mapListTo == null) {
            mapListTo = new ArrayList<MapListType.MapListTo>();
        }
        return this.mapListTo;
    }

    /**
     * Gets the value of the mapFromUnit property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMapFromUnit() {
        return mapFromUnit;
    }

    /**
     * Sets the value of the mapFromUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMapFromUnit(BigInteger value) {
        this.mapFromUnit = value;
    }

    /**
     * Gets the value of the mapToUnit property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMapToUnit() {
        return mapToUnit;
    }

    /**
     * Sets the value of the mapToUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMapToUnit(BigInteger value) {
        this.mapToUnit = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class MapListFrom {

        @XmlAttribute(name = "start", required = true)
        protected int start;
        @XmlAttribute(name = "end", required = true)
        protected int end;

        /**
         * Gets the value of the start property.
         * 
         */
        public int getStart() {
            return start;
        }

        /**
         * Sets the value of the start property.
         * 
         */
        public void setStart(int value) {
            this.start = value;
        }

        /**
         * Gets the value of the end property.
         * 
         */
        public int getEnd() {
            return end;
        }

        /**
         * Sets the value of the end property.
         * 
         */
        public void setEnd(int value) {
            this.end = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class MapListTo {

        @XmlAttribute(name = "start", required = true)
        protected int start;
        @XmlAttribute(name = "end", required = true)
        protected int end;

        /**
         * Gets the value of the start property.
         * 
         */
        public int getStart() {
            return start;
        }

        /**
         * Sets the value of the start property.
         * 
         */
        public void setStart(int value) {
            this.start = value;
        }

        /**
         * Gets the value of the end property.
         * 
         */
        public int getEnd() {
            return end;
        }

        /**
         * Sets the value of the end property.
         * 
         */
        public void setEnd(int value) {
            this.end = value;
        }

    }

}
