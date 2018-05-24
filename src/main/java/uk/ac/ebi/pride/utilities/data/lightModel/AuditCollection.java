package uk.ac.ebi.pride.utilities.data.lightModel;

import uk.ac.ebi.pride.utilities.data.utils.FacadeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * The complete set of Contacts (people and organisations) for this file.
 *
 * <p>Java class for AuditCollectionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AuditCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="Person" type="{http://psidev.info/psi/pi/mzIdentML/1.1}PersonType"/&gt;
 *         &lt;element name="Organization" type="{http://psidev.info/psi/pi/mzIdentML/1.1}OrganizationType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditCollectionType", propOrder = {
        "personOrOrganization"
})
public class AuditCollection extends MzIdentMLObject implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElements({
          @XmlElement(name = "Person", type = Person.class),
          @XmlElement(name = "Organization", type = Organization.class)
  })
  protected List<AbstractContact> personOrOrganization;

  /**
   * Gets the value of the personOrOrganization property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the personOrOrganization property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getPersonOrOrganization().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Person }
   * {@link Organization }
   */
  public List<AbstractContact> getPersonOrOrganization() {
    if (personOrOrganization == null) {
      personOrOrganization = new ArrayList<>();
    }
    return this.personOrOrganization;
  }

  /**
   * Returns a list of the Person objects contained in list returned from getPersonOrOrganization.
   *
   * @return List&lt;Person&gt; The list of Person Contacts.
   */
  public List<Person> getPerson() {
    return new FacadeList<>(this.getPersonOrOrganization(), Person.class);
  }

  /**
   * Returns a list of the Organization objects contained in list returned from getPersonOrOrganization.
   *
   * @return list of organizations
   */
  public List<Organization> getOrganization() {
    return new FacadeList<>(this.getPersonOrOrganization(), Organization.class);
  }
}
