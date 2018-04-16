package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * An identification of a single (poly)peptide, resulting from querying an input spectra, along with the set of confidence values for that identification.
 * PeptideEvidence elements should be given for all mappings of the corresponding Peptide sequence within protein sequences.
 *
 * NOTE: There is no setter method for the peptideRef/massTableRef/sampleRef. This simplifies keeping the peptide/massTable/sample object reference and
 * peptideRef/massTableRef/sampleRef synchronized.
 * <p>
 * <p>Java class for SpectrumIdentificationItemType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="SpectrumIdentificationItemType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PeptideEvidenceRef" type="{http://psidev.info/psi/pi/mzIdentML/1.1}PeptideEvidenceRefType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Fragmentation" type="{http://psidev.info/psi/pi/mzIdentML/1.1}FragmentationType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="chargeState" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="experimentalMassToCharge" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="calculatedMassToCharge" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="calculatedPI" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="peptide_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="rank" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="passThreshold" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="massTable_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sample_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumIdentificationItemType")
@Getter
@Setter
public class SpectrumIdentificationItem extends Identifiable implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlAttribute(required = true)
    protected int chargeState;
    @XmlAttribute(required = true)
    protected double experimentalMassToCharge;
    @XmlAttribute
    protected Double calculatedMassToCharge;
    @XmlAttribute
    protected Float calculatedPI;
    @XmlAttribute(name = "peptide_ref")
    protected String peptideRef;
    @XmlAttribute(required = true)
    protected int rank;
    @XmlAttribute(required = true)
    protected boolean passThreshold;
    @XmlTransient
    protected String formattedSpectrumID;
}

