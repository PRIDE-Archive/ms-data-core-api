package uk.ac.ebi.pride.utilities.data.lightModel;

import uk.ac.ebi.pride.utilities.data.utils.Constants;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the uk.ac.ebi.pride.model package.
 * <p>An ObjectFactory allows you to programmatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MzIdentML_QNAME = new QName("http://psidev.info/psi/pi/mzIdentML/1.1", "MzIdentML");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: uk.ac.ebi.pride.model
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SpectrumIdentificationProtocol }
     */
    public SpectrumIdentificationProtocol createSpectrumIdentificationProtocol() {
        return new SpectrumIdentificationProtocol();
    }

    /**
     * Create an instance of {@link CvParam }
     */
    public CvParam createCvParam() {
        return new CvParam();
    }

    /**
     * Create an instance of {@link SequenceCollection }
     */
    public SequenceCollection createSequenceCollection() {
        return new SequenceCollection();
    }

    /**
     * Create an instance of {@link Inputs }
     */
    public Inputs createInputs() {
        return new Inputs();
    }

    /**
     * Create an instance of {@link SearchModification }
     */
    public SearchModification createSearchModification() {
        return new SearchModification();
    }

    /**
     * Create an instance of {@link Peptide }
     */
    public Peptide createPeptide() {
        return new Peptide();
    }

    /**
     * Create an instance of {@link Cv }
     */
    public Cv createCv() {
        return new Cv();
    }

    /**
     * Create an instance of {@link SpectraData }
     */
    public SpectraData createSpectraData() {
        return new SpectraData();
    }

    /**
     * Create an instance of {@link AnalysisProtocolCollection }
     */
    public AnalysisProtocolCollection createAnalysisProtocolCollection() {
        return new AnalysisProtocolCollection();
    }


    /**
     * Create an instance of {@link DBSequence }
     */
    public DBSequence createDBSequence() {
        return new DBSequence();
    }

    /**
     * Create an instance of {@link SpectrumIdentificationList }
     */
    public SpectrumIdentificationList createSpectrumIdentificationList() {
        return new SpectrumIdentificationList();
    }

    /**
     * Create an instance of {@link AnalysisData }
     */
    public AnalysisData createAnalysisData() {
        return new AnalysisData();
    }

    /**
     * Create an instance of {@link SpectrumIdentificationItem }
     */
    public SpectrumIdentificationItem createSpectrumIdentificationItem() {
        return new SpectrumIdentificationItem();
    }

    /**
     * Create an instance of {@link SpectrumIDFormat }
     */
    public SpectrumIDFormat createSpectrumIDFormat() {
        return new SpectrumIDFormat();
    }

    /**
     * Create an instance of {@link MzIdentML }
     */
    public MzIdentML createMzIdentML() {
        return new MzIdentML();
    }

    /**
     * Create an instance of {@link SpectrumIdentificationResult }
     */
    public SpectrumIdentificationResult createSpectrumIdentificationResult() {
        return new SpectrumIdentificationResult();
    }

    /**
     * Create an instance of {@link DataCollection }
     */
    public DataCollection createDataCollection() {
        return new DataCollection();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MzIdentML }{@code >}}
     */
    @XmlElementDecl(namespace = Constants.MZIDENTML_NAMESPACE, name = "MzIdentML")
    public JAXBElement<MzIdentML> createMzIdentML(MzIdentML value) {
        return new JAXBElement<MzIdentML>(_MzIdentML_QNAME, MzIdentML.class, null, value);
    }


}