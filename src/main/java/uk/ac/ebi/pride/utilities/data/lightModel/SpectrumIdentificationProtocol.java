package uk.ac.ebi.pride.utilities.data.lightModel;


import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The parameters and settings of a SpectrumIdentification analysis.
 *
 * <p>Java class for SpectrumIdentificationProtocolType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SpectrumIdentificationProtocolType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SearchType" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamType"/&gt;
 *         &lt;element name="AdditionalSearchParams" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamListType" minOccurs="0"/&gt;
 *         &lt;element name="ModificationParams" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ModificationParamsType" minOccurs="0"/&gt;
 *         &lt;element name="Enzymes" type="{http://psidev.info/psi/pi/mzIdentML/1.1}EnzymesType" minOccurs="0"/&gt;
 *         &lt;element name="MassTable" type="{http://psidev.info/psi/pi/mzIdentML/1.1}MassTableType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="FragmentTolerance" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ToleranceType" minOccurs="0"/&gt;
 *         &lt;element name="ParentTolerance" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ToleranceType" minOccurs="0"/&gt;
 *         &lt;element name="Threshold" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamListType"/&gt;
 *         &lt;element name="DatabaseFilters" type="{http://psidev.info/psi/pi/mzIdentML/1.1}DatabaseFiltersType" minOccurs="0"/&gt;
 *         &lt;element name="DatabaseTranslation" type="{http://psidev.info/psi/pi/mzIdentML/1.1}DatabaseTranslationType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="analysisSoftware_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumIdentificationProtocolType", propOrder = {
        "modificationParams"
})
public class SpectrumIdentificationProtocol
        extends Identifiable
        implements Serializable
{

    private final static long serialVersionUID = 100L;
    @XmlElement(name = "ModificationParams")
    protected ModificationParams modificationParams;

    /**
     * Gets the value of the modificationParams property.
     *
     * @return
     *     possible object is
     *     {@link ModificationParams }
     *
     */
    public ModificationParams getModificationParams() {
        return modificationParams;
    }

    /**
     * Sets the value of the modificationParams property.
     *
     * @param value
     *     allowed object is
     *     {@link ModificationParams }
     *
     */
    public void setModificationParams(ModificationParams value) {
        this.modificationParams = value;
    }
}
