package uk.ac.ebi.pride.utilities.data.lightModel;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import lombok.Setter;

/**
 * Helper type to allow either a cvParam or a userParam to be provided for an element.
 *
 * <p>Java class for ParamType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ParamType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "ParamType",
  propOrder = {"paramGroup"}
)
@Setter
public class Param extends MzIdentMLObject implements Serializable {

  private static final long serialVersionUID = 100L;

  @XmlElements({
    @XmlElement(name = "userParam", type = UserParam.class),
    @XmlElement(name = "cvParam", type = CvParam.class)
  })
  protected AbstractParam paramGroup;

  public CvParam getCvParam() {
    if (paramGroup instanceof CvParam) {
      return (CvParam) paramGroup;
    } else {
      return null;
    }
  }

  public UserParam getUserParam() {
    if (paramGroup instanceof UserParam) {
      return (UserParam) paramGroup;
    } else {
      return null;
    }
  }
}
