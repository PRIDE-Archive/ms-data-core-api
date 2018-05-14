package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * The details of an individual cleavage enzyme should be provided by giving a regular expression or
 * a CV term if a "standard" enzyme cleavage has been performed.
 *
 * <p>Java class for EnzymeType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EnzymeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SiteRegexp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="EnzymeName" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamListType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="nTermGain"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="[A-Za-z0-9 ]+"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="cTermGain"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="[A-Za-z0-9 ]+"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="semiSpecific" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="missedCleavages" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="minDistance"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int"&gt;
 *             &lt;minInclusive value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "EnzymeType",
  propOrder = {"enzymeName"}
)
@Getter
@Setter
public class Enzyme extends Identifiable implements Serializable, ParamListCapable {
  private static final long serialVersionUID = 100L;

  @XmlElement(name = "EnzymeName")
  protected ParamList enzymeName;
}
