//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.06.07 at 02:21:15 PM BST 
//


package jalview.xml.binding.jalview;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				The results of a PCA calculation
 * 			
 * 
 * <p>Java class for PcaDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PcaDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pairwiseMatrix" type="{www.jalview.org}DoubleMatrix"/>
 *         &lt;element name="tridiagonalMatrix" type="{www.jalview.org}DoubleMatrix"/>
 *         &lt;element name="eigenMatrix" type="{www.jalview.org}DoubleMatrix"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PcaDataType", namespace = "www.jalview.org", propOrder = {
    "pairwiseMatrix",
    "tridiagonalMatrix",
    "eigenMatrix"
})
public class PcaDataType {

    @XmlElement(required = true)
    protected DoubleMatrix pairwiseMatrix;
    @XmlElement(required = true)
    protected DoubleMatrix tridiagonalMatrix;
    @XmlElement(required = true)
    protected DoubleMatrix eigenMatrix;

    /**
     * Gets the value of the pairwiseMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link DoubleMatrix }
     *     
     */
    public DoubleMatrix getPairwiseMatrix() {
        return pairwiseMatrix;
    }

    /**
     * Sets the value of the pairwiseMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoubleMatrix }
     *     
     */
    public void setPairwiseMatrix(DoubleMatrix value) {
        this.pairwiseMatrix = value;
    }

    /**
     * Gets the value of the tridiagonalMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link DoubleMatrix }
     *     
     */
    public DoubleMatrix getTridiagonalMatrix() {
        return tridiagonalMatrix;
    }

    /**
     * Sets the value of the tridiagonalMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoubleMatrix }
     *     
     */
    public void setTridiagonalMatrix(DoubleMatrix value) {
        this.tridiagonalMatrix = value;
    }

    /**
     * Gets the value of the eigenMatrix property.
     * 
     * @return
     *     possible object is
     *     {@link DoubleMatrix }
     *     
     */
    public DoubleMatrix getEigenMatrix() {
        return eigenMatrix;
    }

    /**
     * Sets the value of the eigenMatrix property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoubleMatrix }
     *     
     */
    public void setEigenMatrix(DoubleMatrix value) {
        this.eigenMatrix = value;
    }

}
