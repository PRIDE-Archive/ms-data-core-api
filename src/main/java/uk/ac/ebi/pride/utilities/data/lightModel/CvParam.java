package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * A single entry from an ontology or a controlled
 * vocabulary.
 * <p>Java class for CVParamType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CVParamType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}AbstractParamType"&gt;
 *       &lt;attribute name="cvRef" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="accession" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CVParamType")
@Getter
@Setter
@ToString
public class CvParam extends AbstractParam implements Serializable {

    private final static long serialVersionUID = 100L;
    @XmlAttribute(required = true)
    protected String accession;
    @XmlAttribute(required = true)
    protected String cvRef;
    @XmlTransient
    protected Cv cv;

    public void setCv(Cv cv) {
        if (cv == null) {
            this.cvRef = null;
        } else {
            String refId = cv.getId();
            if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
            this.cvRef = refId;
        }
        this.cv = cv;
    }
}
