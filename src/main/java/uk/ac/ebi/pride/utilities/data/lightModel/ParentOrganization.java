package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * The containing organization (the university or business which a lab belongs to, etc.)
 *
 * <p>Java class for ParentOrganizationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ParentOrganizationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="organization_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParentOrganizationType")
@Getter
public class ParentOrganization extends MzIdentMLObject implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlAttribute(name = "organization_ref", required = true)
  protected String organizationRef;
  @XmlTransient
  protected Organization organization;

  public void setOrganization(Organization organization) {
    if (organization == null) {
      this.organizationRef = null;
    } else {
      String refId = organization.getId();
      if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
      this.organizationRef = refId;
    }
    this.organization = organization;
  }
}
