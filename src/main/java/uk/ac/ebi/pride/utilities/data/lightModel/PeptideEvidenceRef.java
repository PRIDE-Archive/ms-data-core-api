package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Reference to the PeptideEvidence element identified. If a specific sequence can be assigned to multiple proteins and or positions in a protein all possible PeptideEvidence elements should be referenced here.
 * <p>
 * <p>Java class for PeptideEvidenceRefType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="PeptideEvidenceRefType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="peptideEvidence_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PeptideEvidenceRefType")
public class PeptideEvidenceRef
        extends MzIdentMLObject
        implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlAttribute(name = "peptideEvidence_ref", required = true)
    protected String peptideEvidenceRef;

    /**
     * Gets the value of the peptideEvidenceRef property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPeptideEvidenceRef() {
        return peptideEvidenceRef;
    }

    @XmlTransient
    protected PeptideEvidence peptideEvidence;

    public PeptideEvidence getPeptideEvidence() {
        return peptideEvidence;
    }

    public void setPeptideEvidence(PeptideEvidence peptideEvidence) {
        if (peptideEvidence == null) {
            this.peptideEvidenceRef = null;
        } else {
            String refId = peptideEvidence.getId();
            if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
            this.peptideEvidenceRef = refId;
        }
        this.peptideEvidence = peptideEvidence;
    }
}
