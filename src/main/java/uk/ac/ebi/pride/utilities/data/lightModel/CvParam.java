package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * A single entry from an ontology or a controlled
 * vocabulary.
 * <p>Java class for CVParamType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CVParamType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}AbstractParamType"&gt;
 *       &lt;attribute name="cvRef" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="accession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CVParamType")
public class CvParam
        implements Serializable
{

    private final static long serialVersionUID = 100L;
    @XmlAttribute(required = true)
    protected String cvRef;
    @XmlAttribute(required = true)
    protected String accession;

    /**
     * Gets the value of the cvRef property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCvRef() {
        return cvRef;
    }

    /**
     * Gets the value of the accession property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Sets the value of the accession property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAccession(String value) {
        this.accession = value;
    }

}
