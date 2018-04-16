package uk.ac.ebi.pride.utilities.data.lightModel;

import uk.ac.ebi.pride.utilities.data.utils.FacadeList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * A contact is either a person or an organization.
 *
 * <p>Java class for AbstractContactType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AbstractContactType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractContactType", propOrder = {
        "paramGroup"
})
@XmlSeeAlso({
        Person.class,
        Organization.class
})
public abstract class AbstractContact extends Identifiable implements Serializable, ParamGroupCapable {

  private final static long serialVersionUID = 100L;
  @XmlElements({
          @XmlElement(name = "userParam", type = UserParam.class),
          @XmlElement(name = "cvParam", type = CvParam.class)
  })
  protected List<AbstractParam> paramGroup;

  /**
   * Attributes of this contact such as address, email, telephone etc.Gets the value of the paramGroup property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the paramGroup property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getParamGroup().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link UserParam }
   * {@link CvParam }
   */
  public List<AbstractParam> getParamGroup() {
    if (paramGroup == null) {
      paramGroup = new ArrayList<AbstractParam>();
    }
    return this.paramGroup;
  }

  public List<CvParam> getCvParam() {
    return new FacadeList<>(this.getParamGroup(), CvParam.class);
  }

  public List<UserParam> getUserParam() {
    return new FacadeList<UserParam>(this.getParamGroup(), UserParam.class);
  }
}