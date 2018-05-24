package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * PeptideEvidence links a specific Peptide element to a specific position in a DBSequence. There must only be one PeptideEvidence item per Peptide-to-DBSequence-position.
 * <p>
 * <p>Java class for PeptideEvidenceType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="PeptideEvidenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="dBSequence_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="peptide_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="end" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="pre"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="[ABCDEFGHIJKLMNOPQRSTUVWXYZ?\-]{1}"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="post"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="[ABCDEFGHIJKLMNOPQRSTUVWXYZ?\-]{1}"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="translationTable_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="frame" type="{http://psidev.info/psi/pi/mzIdentML/1.1}allowed_frames" /&gt;
 *       &lt;attribute name="isDecoy" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PeptideEvidenceType")
@Getter
@Setter
public class PeptideEvidence
        extends Identifiable
        implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlAttribute(name = "dBSequence_ref", required = true)
    protected String dbSequenceRef;
    @XmlAttribute(name = "peptide_ref", required = true)
    protected String peptideRef;
    @XmlTransient
    protected DBSequence dbSequence;
    @XmlAttribute
    protected Boolean isDecoy;
    @XmlTransient
    protected Peptide peptide;

    public Peptide getPeptide() {
        return peptide;
    }

    public void setPeptide(Peptide peptide) {
        if (peptide == null) {
            this.peptideRef = null;
        } else {
            String refId = peptide.getId();
            if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
            this.peptideRef = refId;
        }
        this.peptide = peptide;
    }

    public DBSequence getDBSequence() {
        return dbSequence;
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
}
