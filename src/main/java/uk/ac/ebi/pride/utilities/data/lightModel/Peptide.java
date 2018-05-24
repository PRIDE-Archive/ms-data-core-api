package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * One (poly)peptide (a sequence with modifications). The combination of Peptide sequence and modifications must be unique in the file.
 *
 * <p>Java class for PeptideType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PeptideType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PeptideSequence" type="{http://psidev.info/psi/pi/mzIdentML/1.1}sequence"/&gt;
 *         &lt;element name="Modification" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ModificationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="SubstitutionModification" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SubstitutionModificationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PeptideType", propOrder = {
        "peptideSequence",
        "modification"
})
@Getter
@Setter
public class Peptide extends Identifiable implements Serializable {

  private final static long serialVersionUID = 100L;

  @XmlElement(name = "PeptideSequence", required = true)
  protected String peptideSequence;
  @XmlElement(name = "Modification")
  protected List<Modification> modification;

  /**
   * Gets the value of the modification property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the modification property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getModification().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Modification }
   */
  public List<Modification> getModification() {
    if (modification == null) {
      modification = new ArrayList<Modification>();
    }
    return this.modification;
  }
}