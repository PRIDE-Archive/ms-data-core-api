package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The list of controlled vocabularies used in the file.
 *
 * <p>Java class for CVListType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CVListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cv" type="{http://psidev.info/psi/pi/mzIdentML/1.1}cvType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "CVListType",
  propOrder = {"cv"}
)
public class CvList extends MzIdentMLObject implements Serializable {

  private static final long serialVersionUID = 100L;

  @XmlElement(required = true)
  protected List<Cv> cv;

  /**
   * Gets the value of the cv property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the cv property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getCv().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link Cv }
   */
  public List<Cv> getCv() {
    if (cv == null) {
      cv = new ArrayList<>();
    }
    return this.cv;
  }
}
