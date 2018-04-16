package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * The provider of the document in terms of the Contact and the software the produced the document instance.
 *
 * <p>Java class for ProviderType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ProviderType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ContactRole" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ContactRoleType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="analysisSoftware_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProviderType", propOrder = {
        "contactRole"
})
@Getter
@Setter
public class Provider extends Identifiable implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "ContactRole")
  protected ContactRole contactRole;
  @XmlAttribute(name = "analysisSoftware_ref")
  protected String softwareRef;
  @XmlTransient
  protected AnalysisSoftware software;

  public void setSoftware(AnalysisSoftware software) {
    if (software == null) {
      this.softwareRef = null;
    } else {
      String refId = software.getId();
      if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
      this.softwareRef = refId;
    }
    this.software = software;
  }
}
