package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A person's name and contact details. Any additional information such as the address, contact
 * email etc. should be supplied using CV parameters or user parameters.
 *
 * <p>Java class for PersonType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PersonType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}AbstractContactType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Affiliation" type="{http://psidev.info/psi/pi/mzIdentML/1.1}AffiliationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="lastName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="firstName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="midInitials" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "PersonType",
  propOrder = {"affiliation"}
)
@Getter
@Setter
public class Person extends AbstractContact implements Serializable {

  private static final long serialVersionUID = 100L;

  @XmlElement(name = "Affiliation")
  protected List<Affiliation> affiliation;

  @XmlAttribute protected String lastName;
  @XmlAttribute protected String firstName;
  @XmlAttribute protected String midInitials;

  /**
   * Gets the value of the affiliation property.
   *
   * <p>
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the affiliation property. For example, to add a new
   * item, do as follows:
   *
   * <pre>
   *    getAffiliation().add(newItem);
   * </pre>
   *
   * Objects of the following type(s) are allowed in the list {@link Affiliation }
   */
  public List<Affiliation> getAffiliation() {
    if (affiliation == null) {
      affiliation = new ArrayList<>();
    }
    return this.affiliation;
  }
}
