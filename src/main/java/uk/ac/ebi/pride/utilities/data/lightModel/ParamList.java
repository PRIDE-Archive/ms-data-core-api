package uk.ac.ebi.pride.utilities.data.lightModel;


import uk.ac.ebi.pride.utilities.data.utils.FacadeList;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper type to allow multiple cvParams or userParams to be given for an element.
 *
 * <p>Java class for ParamListType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ParamListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamListType", propOrder = {
        "paramGroup"
})
public class ParamList
        extends MzIdentMLObject
        implements Serializable
{

  private final static long serialVersionUID = 100L;
  @XmlElements({
          @XmlElement(name = "cvParam", type = CvParam.class),
          @XmlElement(name = "userParam", type = UserParam.class)
  })
  protected List<AbstractParam> paramGroup;

  /**
   * Gets the value of the paramGroup property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the paramGroup property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getParamGroup().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link CvParam }
   * {@link UserParam }
   *
   *
   */
  public List<AbstractParam> getParamGroup() {
    if (paramGroup == null) {
      paramGroup = new ArrayList<>();
    }
    return this.paramGroup;
  }

  /**
   * Gets the enzyme name cvparams
   *
   * @return possible object is
   *         {@link uk.ac.ebi.pride.utilities.data.utils.FacadeList }
   */
  public List<CvParam> getCvParam() {
    return new FacadeList<>(this.getParamGroup(), CvParam.class);
  }

  /**
   * Gets the enzymename userparams
   *
   * @return possible object is
   *         {@link FacadeList }
   */
  public List<UserParam> getUserParam() {
    return new FacadeList<>(this.getParamGroup(), UserParam.class);
  }

}
