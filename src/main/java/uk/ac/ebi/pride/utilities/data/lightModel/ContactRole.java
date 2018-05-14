package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * The role that a Contact plays in an organization or with respect to the associating class. A Contact may have several Roles within scope, and as such,
 * associations to ContactRole allow the use of a Contact in a certain manner. Examples
 * might include a provider, or a data analyst.
 * <p>
 * TODO marshalling/ persistor add validation to check for case where someone gets contact and changes its id without updating ref id in
 * ContactRole and other such clases.
 * <p>
 * NOTE: There is no setter method for the contactRef. This simplifies keeping the contact object reference and
 * contactRef synchronized.
 *
 * <p>Java class for ContactRoleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ContactRoleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Role" type="{http://psidev.info/psi/pi/mzIdentML/1.1}RoleType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="contact_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactRoleType", propOrder = {
        "role"
})
@Getter
@Setter
public class ContactRole
        extends MzIdentMLObject
        implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "Role", required = true)
  protected Role role;
  @XmlAttribute(name = "contact_ref", required = true)
  protected String contactRef;
  @XmlTransient
  protected AbstractContact contact;

  public Person getPerson() {
    if (contact != null && contact instanceof Person) return (Person) contact;
    else return null;
  }

  public Organization getOrganization() {
    if (contact != null && contact instanceof Organization) return (Organization) contact;
    else return null;
  }

  /**
   * Set contact. contactRef is also updated.
   *
   * @param contact
   */
  public void setContact(AbstractContact contact) {
    if (contact == null) {
      this.contactRef = null;
    } else {
      String refId = contact.getId();
      if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
      this.contactRef = refId;
    }
    this.contact = contact;
  }

}
