//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.08.21 at 12:24:59 AM EDT 
//


package org.erowid.navigatorandroid.xmlXstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for image-set complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="image-set">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="image-entry" type="{http://www.erowid.org/bigchart}image-entry" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "image-set", propOrder = {
//    "imageEntry"
//})
public class ImageSet {

//    @XmlElement(name = "image-entry")
    @XStreamImplicit
    @XStreamAlias("image-entry")
    protected List<ImageEntry> imageEntry;

    /**
     * Gets the value of the imageEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imageEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImageEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ImageEntry }
     * 
     * 
     */
    public List<ImageEntry> getImageEntry() {
        if (imageEntry == null) {
            imageEntry = new ArrayList<ImageEntry>();
        }
        return this.imageEntry;
    }

}
