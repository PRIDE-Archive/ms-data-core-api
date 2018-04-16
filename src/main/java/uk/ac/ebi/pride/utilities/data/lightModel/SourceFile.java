package uk.ac.ebi.pride.utilities.data.lightModel;


import uk.ac.ebi.pride.utilities.data.utils.FacadeList;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A file from which this mzIdentML instance was created.
 *
 * <p>Java class for SourceFileType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SourceFileType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}ExternalDataType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SourceFileType", propOrder = {
        "paramGroup"
})
public class SourceFile extends ExternalData implements Serializable, ParamGroupCapable {

  private final static long serialVersionUID = 100L;
  @XmlElements({
          @XmlElement(name = "cvParam", type = CvParam.class),
          @XmlElement(name = "userParam", type = UserParam.class)
  })
  protected List<AbstractParam> paramGroup;

  /**
   * Any additional parameters description the source
   * file.Gets the value of the paramGroup property.
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
   * Objects of the following type(s) are allowed in the list
   * {@link CvParam }
   * {@link UserParam }
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