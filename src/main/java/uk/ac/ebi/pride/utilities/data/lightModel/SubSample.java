package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * References to the individual component samples within a mixed parent sample.
 *
 * TODO marshalling/ persistor add validation to check for case where someone gets sample and changes its id without updating ref id in
 *      SubSample and other such clases.
 *
 * NOTE: There is no setter method for the sampleRef. This simplifies keeping the sample object reference and
 * sampleRef synchronized.
 * <p>Java class for SubSampleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SubSampleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="sample_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubSampleType")
@Getter
@Setter
public class SubSample extends MzIdentMLObject implements Serializable {

  private final static long serialVersionUID = 100L;

  @XmlAttribute(name = "sample_ref", required = true)
  protected String sampleRef;
  @XmlTransient
  protected Sample sample;

  public void setSample(Sample sample) {
    if (sample == null) {
      this.sampleRef = null;
    } else {
      String refId = sample.getId();
      if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
      this.sampleRef = refId;
    }
    this.sample = sample;
  }
}
