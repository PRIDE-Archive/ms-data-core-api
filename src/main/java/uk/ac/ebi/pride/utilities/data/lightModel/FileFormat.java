package uk.ac.ebi.pride.utilities.data.lightModel;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The format of the ExternalData file, for example "tiff" for image files.
 *
 * <p>Java class for FileFormatType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FileFormatType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cvParam" type="{http://psidev.info/psi/pi/mzIdentML/1.1}CVParamType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileFormatType", propOrder = {
        "cvParam"
})
public class FileFormat
        extends MzIdentMLObject
        implements Serializable, CvParamCapable
{

    private final static long serialVersionUID = 100L;
    @XmlElement(required = true)
    protected CvParam cvParam;

    /**
     * Gets the value of the cvParam property.
     *
     * @return
     *     possible object is
     *     {@link CvParam }
     *
     */
    public CvParam getCvParam() {
        return cvParam;
    }

    /**
     * Sets the value of the cvParam property.
     *
     * @param value
     *     allowed object is
     *     {@link CvParam }
     *
     */
    public void setCvParam(CvParam value) {
        this.cvParam = value;
    }
}
