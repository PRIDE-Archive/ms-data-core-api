package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * All identifications made from searching one spectrum. For PMF data, all peptide identifications will be listed underneath as SpectrumIdentificationItems. For MS/MS data, there will be ranked SpectrumIdentificationItems corresponding to possible different peptide IDs.
 *
 * <p>Java class for SpectrumIdentificationResultType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SpectrumIdentificationResultType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SpectrumIdentificationItem" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SpectrumIdentificationItemType" maxOccurs="unbounded"/&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="spectrumID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="spectraData_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumIdentificationResultType", propOrder = {
        "spectrumIdentificationItem"
})
@Getter
@Setter
public class SpectrumIdentificationResult extends Identifiable implements Serializable {

  private final static long serialVersionUID = 100L;

  @XmlElement(name = "SpectrumIdentificationItem", required = true)
  protected List<SpectrumIdentificationItem> spectrumIdentificationItem;
  @XmlAttribute(required = true)
  protected String spectrumID;
  @XmlAttribute(name = "spectraData_ref", required = true)
  protected String spectraDataRef;
  @XmlTransient
  protected String formattedSpectrumID;

  /**
   * Gets the value of the spectrumIdentificationItem property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the spectrumIdentificationItem property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getSpectrumIdentificationItem().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link SpectrumIdentificationItem }
   */
  public List<SpectrumIdentificationItem> getSpectrumIdentificationItem() {
    if (spectrumIdentificationItem == null) {
      spectrumIdentificationItem = new ArrayList<SpectrumIdentificationItem>();
    }
    return this.spectrumIdentificationItem;
  }
}
