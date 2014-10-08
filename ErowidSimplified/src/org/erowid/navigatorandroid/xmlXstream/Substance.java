
package org.erowid.navigatorandroid.xmlXstream;

import android.util.Log;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * <p>Java class for substance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="substance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nice-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="slang-name-set" type="{http://www.erowid.org/bigchart}slang-name-set"/>
 *         &lt;element name="alternate-name-set" type="{http://www.erowid.org/bigchart}alternate-name-set"/>
 *         &lt;element name="vault" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basics" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="images" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="law" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="experience" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="faqs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="effects" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="chemistry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="testing" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="health" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="history" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="spiritual" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cultivation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="journal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="writings" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="media" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="summary-card-image" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="molecule-2d-image" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="molecule-3d-image" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="summary-effects" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="summary-chemical-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="summary-description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="summary-caution" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="experience-set" type="{http://www.erowid.org/bigchart}experience-set" minOccurs="0"/>
 *         &lt;element name="image-set" type="{http://www.erowid.org/bigchart}image-set" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

public class Substance {

    //@XmlElement(required = true)
    protected String name;
    @XStreamAlias("nice-name")
    protected String niceName;
    @XStreamImplicit
    @XStreamAlias("alternate-name-set")
    public List<String> alternateNameSet;
    protected String vault;
    protected String basics;
    protected String images;
    protected String law;
    protected String dose;
    protected String experience;
    protected String faqs;
    protected String effects;
    protected String chemistry;
    protected String testing;
    protected String health;
    protected String history;
    protected String spiritual;
    protected String cultivation;
    protected String journal;
    protected String writings;
    protected String media;
    @XStreamAlias("summary-card-image")
    protected String summaryCardImage;
    @XStreamAlias("molecule-2d-image")
    protected String molecule2DImage;
    @XStreamAlias("molecule-3d-image")
    protected String molecule3DImage;
    @XStreamAlias("summary-effects")
    protected String summaryEffects;
    @XStreamAlias("summary-chemical-name")
    protected String summaryChemicalName;
    @XStreamAlias("summary-description")
    protected String summaryDescription;
    @XStreamAlias("summary-caution")
    protected String summaryCaution;
    @XStreamAlias("image-set")
    protected ImageSet imageSet;


    public Substance(String cName, String cNiceName)
    {
        name = cName;
        niceName = cNiceName;
    }

    public Substance()
    {
        name = " ";
        niceName = " ";
        Log.d("No Args Substance","created");
    }

//    public Substance(String cName, String cNiceName, String )
//    {
//        name = cName;
//        niceName = cNiceName;
//
//    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the niceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNiceName() {
        return niceName;
    }

    /**
     * Sets the value of the niceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNiceName(String value) {
        this.niceName = value;
    }

    /**
     * Gets the value of the vault property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVault() {
        return vault;
    }

    /**
     * Sets the value of the vault property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVault(String value) {
        this.vault = value;
    }

    /**
     * Gets the value of the basics property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBasics() {
        return basics;
    }

    /**
     * Sets the value of the basics property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBasics(String value) {
        this.basics = value;
    }

    /**
     * Gets the value of the images property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImages() {
        return images;
    }

    /**
     * Sets the value of the images property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImages(String value) {
        this.images = value;
    }

    /**
     * Gets the value of the law property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLaw() {
        return law;
    }

    /**
     * Sets the value of the law property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaw(String value) {
        this.law = value;
    }

    /**
     * Gets the value of the dose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDose() {
        return dose;
    }

    /**
     * Sets the value of the dose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDose(String value) {
        this.dose = value;
    }

    /**
     * Gets the value of the experience property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExperience() {
        return experience;
    }

    /**
     * Sets the value of the experience property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExperience(String value) {
        this.experience = value;
    }

    /**
     * Gets the value of the faqs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaqs() {
        return faqs;
    }

    /**
     * Sets the value of the faqs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaqs(String value) {
        this.faqs = value;
    }

    /**
     * Gets the value of the effects property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEffects() {
        return effects;
    }

    /**
     * Sets the value of the effects property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEffects(String value) {
        this.effects = value;
    }

    /**
     * Gets the value of the chemistry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChemistry() {
        return chemistry;
    }

    /**
     * Sets the value of the chemistry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChemistry(String value) {
        this.chemistry = value;
    }

    /**
     * Gets the value of the testing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTesting() {
        return testing;
    }

    /**
     * Sets the value of the testing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTesting(String value) {
        this.testing = value;
    }

    /**
     * Gets the value of the health property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHealth() {
        return health;
    }

    /**
     * Sets the value of the health property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHealth(String value) {
        this.health = value;
    }

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHistory() {
        return history;
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHistory(String value) {
        this.history = value;
    }

    /**
     * Gets the value of the spiritual property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpiritual() {
        return spiritual;
    }

    /**
     * Sets the value of the spiritual property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpiritual(String value) {
        this.spiritual = value;
    }

    /**
     * Gets the value of the cultivation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCultivation() {
        return cultivation;
    }

    /**
     * Sets the value of the cultivation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCultivation(String value) {
        this.cultivation = value;
    }

    /**
     * Gets the value of the journal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJournal() {
        return journal;
    }

    /**
     * Sets the value of the journal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJournal(String value) {
        this.journal = value;
    }

    /**
     * Gets the value of the writings property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWritings() {
        return writings;
    }

    /**
     * Sets the value of the writings property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWritings(String value) {
        this.writings = value;
    }

    /**
     * Gets the value of the media property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMedia() {
        return media;
    }

    /**
     * Sets the value of the media property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMedia(String value) {
        this.media = value;
    }

    /**
     * Gets the value of the summaryCardImage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSummaryCardImage() {
        return summaryCardImage;
    }

    /**
     * Sets the value of the summaryCardImage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSummaryCardImage(String value) {
        this.summaryCardImage = value;
    }

    /**
     * Gets the value of the molecule2DImage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMolecule2DImage() {
        return molecule2DImage;
    }

    /**
     * Sets the value of the molecule2DImage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMolecule2DImage(String value) {
        this.molecule2DImage = value;
    }

    /**
     * Gets the value of the molecule3DImage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMolecule3DImage() {
        return molecule3DImage;
    }

    /**
     * Sets the value of the molecule3DImage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMolecule3DImage(String value) {
        this.molecule3DImage = value;
    }

    /**
     * Gets the value of the summaryEffects property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSummaryEffects() {
        return summaryEffects;
    }

    /**
     * Sets the value of the summaryEffects property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSummaryEffects(String value) {
        this.summaryEffects = value;
    }

    /**
     * Gets the value of the summaryChemicalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSummaryChemicalName() {
        return summaryChemicalName;
    }

    /**
     * Sets the value of the summaryChemicalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSummaryChemicalName(String value) {
        this.summaryChemicalName = value;
    }

    /**
     * Gets the value of the summaryDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSummaryDescription() {
        return summaryDescription;
    }

    /**
     * Sets the value of the summaryDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSummaryDescription(String value) {
        this.summaryDescription = value;
    }

    /**
     * Gets the value of the summaryCaution property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSummaryCaution() {
        return summaryCaution;
    }

    /**
     * Sets the value of the summaryCaution property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSummaryCaution(String value) {
        this.summaryCaution = value;
    }


}
