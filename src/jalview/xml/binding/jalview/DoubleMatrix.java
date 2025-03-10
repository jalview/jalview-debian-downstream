//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.06.07 at 02:21:15 PM BST 
//


package jalview.xml.binding.jalview;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DoubleMatrix complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DoubleMatrix">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="row" type="{www.jalview.org}DoubleVector" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="D" type="{www.jalview.org}DoubleVector" minOccurs="0"/>
 *         &lt;element name="E" type="{www.jalview.org}DoubleVector" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rows" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="columns" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DoubleMatrix", namespace = "www.jalview.org", propOrder = {
    "row",
    "d",
    "e"
})
public class DoubleMatrix {

    protected List<DoubleVector> row;
    @XmlElement(name = "D")
    protected DoubleVector d;
    @XmlElement(name = "E")
    protected DoubleVector e;
    @XmlAttribute(name = "rows")
    protected Integer rows;
    @XmlAttribute(name = "columns")
    protected Integer columns;

    /**
     * Gets the value of the row property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the row property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRow().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DoubleVector }
     * 
     * 
     */
    public List<DoubleVector> getRow() {
        if (row == null) {
            row = new ArrayList<DoubleVector>();
        }
        return this.row;
    }

    /**
     * Gets the value of the d property.
     * 
     * @return
     *     possible object is
     *     {@link DoubleVector }
     *     
     */
    public DoubleVector getD() {
        return d;
    }

    /**
     * Sets the value of the d property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoubleVector }
     *     
     */
    public void setD(DoubleVector value) {
        this.d = value;
    }

    /**
     * Gets the value of the e property.
     * 
     * @return
     *     possible object is
     *     {@link DoubleVector }
     *     
     */
    public DoubleVector getE() {
        return e;
    }

    /**
     * Sets the value of the e property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoubleVector }
     *     
     */
    public void setE(DoubleVector value) {
        this.e = value;
    }

    /**
     * Gets the value of the rows property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRows() {
        return rows;
    }

    /**
     * Sets the value of the rows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRows(Integer value) {
        this.rows = value;
    }

    /**
     * Gets the value of the columns property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getColumns() {
        return columns;
    }

    /**
     * Sets the value of the columns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setColumns(Integer value) {
        this.columns = value;
    }

}
