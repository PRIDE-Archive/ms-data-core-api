package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The samples analysed can optionally be recorded using CV terms for descriptions. If a composite sample has been analysed, the subsample association can be used to build a hierarchical description.
 *
 * <p>Java class for AnalysisSampleCollectionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AnalysisSampleCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Sample" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SampleType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnalysisSampleCollectionType", propOrder = {
        "sample"
})
public class AnalysisSampleCollection extends MzIdentMLObject implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "Sample", required = true)
  protected List<Sample> sample;

  /**
   * Gets the value of the sample property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the sample property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getSample().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Sample }
   *
   * @return sample
   */
  public List<Sample> getSample() {
    if (sample == null) {
      sample = new ArrayList<>();
    }
    return this.sample;
  }
}
