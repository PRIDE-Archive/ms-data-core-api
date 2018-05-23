package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.pride.utilities.data.utils.FacadeList;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A single result of the ProteinDetection analysis (i.e. a protein).
 * <p>
 * <p>Java class for ProteinDetectionHypothesisType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="ProteinDetectionHypothesisType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PeptideHypothesis" type="{http://psidev.info/psi/pi/mzIdentML/1.1}PeptideHypothesisType" maxOccurs="unbounded"/&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="dBSequence_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="passThreshold" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProteinDetectionHypothesisType", propOrder = {
        "paramGroup"
})
@Getter
@Setter
public class ProteinDetectionHypothesis extends Identifiable implements Serializable, ParamGroupCapable {

    private final static long serialVersionUID = 100L;
    @XmlElements({
            @XmlElement(name = "cvParam", type = CvParam.class)
    })
    protected List<AbstractParam> paramGroup;
    @XmlAttribute(name = "dBSequence_ref")
    protected String dbSequenceRef;
    @XmlAttribute(required = true)
    protected boolean passThreshold;
    @XmlTransient
    protected DBSequence dbSequence;

    /**
     * Scores or parameters associated with this ProteinDetectionHypothesis e.g. p-value Gets the value of the paramGroup property.
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
     * {@link CvParam }
     * {@link UserParam }
     */
    public List<AbstractParam> getParamGroup() {
        if (paramGroup == null) {
            paramGroup = new ArrayList<>();
        }
        return this.paramGroup;
    }

    /**
     * Gets the value of the dbSequenceRef property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDBSequenceRef() {
        return dbSequenceRef;
    }

    public void setDBSequence(DBSequence dbSequence) {
        if (dbSequence == null) {
            this.dbSequenceRef = null;
        } else {
            String refId = dbSequence.getId();
            if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
            this.dbSequenceRef = refId;
        }
        this.dbSequence = dbSequence;
    }

    public List<CvParam> getCvParam() {
        return new FacadeList<>(this.getParamGroup(), CvParam.class);
    }

    public List<UserParam> getUserParam() {
        return new FacadeList<>(this.getParamGroup(), UserParam.class);
    }
}
