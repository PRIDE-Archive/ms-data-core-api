package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The collection of sequences (DBSequence or Peptide) identified and their relationship between each other (PeptideEvidence) to be referenced elsewhere in the results.
 * <p>
 * <p>Java class for SequenceCollectionType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="SequenceCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DBSequence" type="{http://psidev.info/psi/pi/mzIdentML/1.1}DBSequenceType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Peptide" type="{http://psidev.info/psi/pi/mzIdentML/1.1}PeptideType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="PeptideEvidence" type="{http://psidev.info/psi/pi/mzIdentML/1.1}PeptideEvidenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SequenceCollectionType", propOrder = {
        "dbSequence",
        "peptide",
        "peptideEvidence"
})
public class SequenceCollection
        extends MzIdentMLObject
        implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlElement(name = "DBSequence", required = true)
    protected List<DBSequence> dbSequence;
    @XmlElement(name = "Peptide")
    protected List<Peptide> peptide;
    @XmlElement(name = "PeptideEvidence")
    protected List<PeptideEvidence> peptideEvidence;

    /**
     * Gets the value of the dbSequence property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dbSequence property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDBSequence().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DBSequence }
     */
    public List<DBSequence> getDBSequence() {
        if (dbSequence == null) {
            dbSequence = new ArrayList<DBSequence>();
        }
        return this.dbSequence;
    }

    /**
     * Gets the value of the peptide property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the peptide property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPeptide().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Peptide }
     */
    public List<Peptide> getPeptide() {
        if (peptide == null) {
            peptide = new ArrayList<Peptide>();
        }
        return this.peptide;
    }

    /**
     * Gets the value of the peptideEvidence property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the peptideEvidence property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPeptideEvidence().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PeptideEvidence }
     */
    public List<PeptideEvidence> getPeptideEvidence() {
        if (peptideEvidence == null) {
            peptideEvidence = new ArrayList<PeptideEvidence>();
        }
        return this.peptideEvidence;
    }
}