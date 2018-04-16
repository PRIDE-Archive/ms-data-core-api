package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The collection of protocols which include the parameters and settings of the performed analyses.
 *
 * <p>Java class for AnalysisProtocolCollectionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AnalysisProtocolCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SpectrumIdentificationProtocol" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SpectrumIdentificationProtocolType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="ProteinDetectionProtocol" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ProteinDetectionProtocolType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnalysisProtocolCollectionType",
        propOrder = {
                "spectrumIdentificationProtocol"
        }
)
public class AnalysisProtocolCollection extends MzIdentMLObject implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "SpectrumIdentificationProtocol", required = true)
  protected List<SpectrumIdentificationProtocol> spectrumIdentificationProtocol;

  /**
   * Gets the value of the spectrumIdentificationProtocol property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the spectrumIdentificationProtocol property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getSpectrumIdentificationProtocol().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link SpectrumIdentificationProtocol }
   *
   * @return spectrumIdentificationProtocol
   */
  public List<SpectrumIdentificationProtocol> getSpectrumIdentificationProtocol() {
    if (spectrumIdentificationProtocol == null) {
      spectrumIdentificationProtocol = new ArrayList<SpectrumIdentificationProtocol>();
    }
    return this.spectrumIdentificationProtocol;
  }
}
