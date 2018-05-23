package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * The software used for performing the analyses.
 *
 * <p>Java class for AnalysisSoftwareType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AnalysisSoftwareType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ContactRole" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ContactRoleType" minOccurs="0"/&gt;
 *         &lt;element name="SoftwareName" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamType"/&gt;
 *         &lt;element name="Customizations" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "AnalysisSoftwareType",
  propOrder = {
    "softwareName",
    "customizations"
  }
)
@Getter
@Setter
public class AnalysisSoftware extends Identifiable implements Serializable, ParamCapable {

  private static final long serialVersionUID = 100L;

  @XmlElement(name = "SoftwareName", required = true)
  protected Param softwareName;
  @XmlElement(name = "Customizations")
  protected String customizations;
  @XmlAttribute protected
  String version;
  @XmlAttribute
  @XmlSchemaType(name = "anyURI")
  protected String uri;
}
