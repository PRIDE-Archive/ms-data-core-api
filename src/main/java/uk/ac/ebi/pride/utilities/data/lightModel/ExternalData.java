package uk.ac.ebi.pride.utilities.data.lightModel;

/**
 * @author Suresh Hewapathirana
 */


import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Data external to the XML instance document. The location of the data file is given in the location attribute.
 *
 * <p>Java class for ExternalDataType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExternalDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ExternalFormatDocumentation" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="FileFormat" type="{http://psidev.info/psi/pi/mzIdentML/1.1}FileFormatType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="location" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExternalDataType"

//        ,propOrder = {
//        "externalFormatDocumentation",
//        "fileFormat"
//}
)
//@XmlSeeAlso({
//        SpectraData.class,
//        SourceFile.class,
//        SearchDatabase.class
//})
public class ExternalData
        extends Identifiable
        implements Serializable
{

    private final static long serialVersionUID = 100L;

    @XmlElement(name = "ExternalFormatDocumentation")
    @XmlSchemaType(name = "anyURI")
    protected String externalFormatDocumentation;

//    @XmlElement(name = "FileFormat")
//    protected FileFormat fileFormat;

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String location;

//    /**
//     * Gets the value of the externalFormatDocumentation property.
//     *
//     * @return
//     *     possible object is
//     *     {@link String }
//     *
//     */
//    public String getExternalFormatDocumentation() {
//        return externalFormatDocumentation;
//    }
//
//    /**
//     * Sets the value of the externalFormatDocumentation property.
//     *
//     * @param value
//     *     allowed object is
//     *     {@link String }
//     *
//     */
//    public void setExternalFormatDocumentation(String value) {
//        this.externalFormatDocumentation = value;
//    }

//    /**
//     * Gets the value of the fileFormat property.
//     *
//     * @return
//     *     possible object is
//     *     {@link FileFormat }
//     *
//     */
//    public FileFormat getFileFormat() {
//        return fileFormat;
//    }
//
//    /**
//     * Sets the value of the fileFormat property.
//     *
//     * @param value
//     *     allowed object is
//     *     {@link FileFormat }
//     *
//     */
//    public void setFileFormat(FileFormat value) {
//        this.fileFormat = value;
//    }

    /**
     * Gets the value of the location property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLocation(String value) {
        this.location = value;
    }

}
