package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the set of all search results from SpectrumIdentification.
 *
 * <p>Java class for SpectrumIdentificationListType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SpectrumIdentificationListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FragmentationTable" type="{http://psidev.info/psi/pi/mzIdentML/1.1}FragmentationTableType" minOccurs="0"/&gt;
 *         &lt;element name="SpectrumIdentificationResult" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SpectrumIdentificationResultType" maxOccurs="unbounded"/&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="numSequencesSearched" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumIdentificationListType", propOrder = {
        "spectrumIdentificationResult"
})
public class SpectrumIdentificationList extends Identifiable implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "SpectrumIdentificationResult", required = true)
  protected List<SpectrumIdentificationResult> spectrumIdentificationResult;

  /**
   * Gets the value of the spectrumIdentificationResult property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the spectrumIdentificationResult property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getSpectrumIdentificationResult().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link SpectrumIdentificationResult }
   */
  public List<SpectrumIdentificationResult> getSpectrumIdentificationResult() {
    if (spectrumIdentificationResult == null) {
      spectrumIdentificationResult = new ArrayList<SpectrumIdentificationResult>();
    }
    return this.spectrumIdentificationResult;
  }
}
