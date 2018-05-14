package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * The parameters and settings of a SpectrumIdentification analysis.
 * <p>
 * <p>Java class for SpectrumIdentificationProtocolType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumIdentificationProtocolType", propOrder = {
        "modificationParams",
        "enzymes"
})
@Getter
@Setter
public class SpectrumIdentificationProtocol extends Identifiable implements Serializable {

    private final static long serialVersionUID = 100L;
    @XmlElement(name = "ModificationParams")
    protected ModificationParams modificationParams;
    @XmlElement(name = "Enzymes")
    protected Enzymes enzymes;

  /**
   * Gets the value of the enzymes property.
   *
   * @return
   *     possible object is
   *     {@link Enzymes }
   *
   */
  public Enzymes getEnzymes() {
    return enzymes;
  }
}
