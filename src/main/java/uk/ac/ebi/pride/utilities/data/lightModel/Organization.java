package uk.ac.ebi.pride.utilities.data.lightModel;


import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Organizations are entities like companies, universities, government agencies. Any additional information such as the address, email etc. should be supplied either as CV parameters or as user parameters.
 *
 * <p>Java class for OrganizationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="OrganizationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}AbstractContactType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Parent" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParentOrganizationType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrganizationType", propOrder = {
        "parent"
})
@Getter
@Setter
public class Organization extends AbstractContact implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "Parent")
  protected ParentOrganization parent;
}
