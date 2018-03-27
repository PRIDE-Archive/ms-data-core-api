package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


/**
 * @author Suresh Hewapathirana
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MzIdentMLType", propOrder = {
        "sequenceCollection",
        "dataCollection",
        "analysisProtocolCollection"
})
public class MzIdentML
        extends Identifiable
        implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlElement(name = "SequenceCollection")
    protected SequenceCollection sequenceCollection;
    @XmlElement(name = "DataCollection", required = true)
    protected DataCollection dataCollection;
    @XmlElement(name = "AnalysisProtocolCollection", required = true)
    protected AnalysisProtocolCollection analysisProtocolCollection;
    @XmlAttribute(required = true)
    protected String version;

    /**
     * Gets the value of the sequenceCollection property.
     *
     * @return possible object is
     * {@link SequenceCollection }
     */
    public SequenceCollection getSequenceCollection() {
        return sequenceCollection;
    }

    /**
     * Sets the value of the sequenceCollection property.
     *
     * @param value allowed object is
     *              {@link SequenceCollection }
     */
    public void setSequenceCollection(SequenceCollection value) {
        this.sequenceCollection = value;
    }

    /**
     * Gets the value of the dataCollection property.
     *
     * @return possible object is
     * {@link DataCollection }
     */
    public DataCollection getDataCollection() {
        return dataCollection;
    }

    /**
     * Sets the value of the dataCollection property.
     *
     * @param value allowed object is
     *              {@link DataCollection }
     */
    public void setDataCollection(DataCollection value) {
        this.dataCollection = value;
    }

    /**
     * Gets the value of the analysisProtocolCollection property.
     *
     * @return
     *     possible object is
     *     {@link AnalysisProtocolCollection }
     *
     */
    public AnalysisProtocolCollection getAnalysisProtocolCollection() {
        return analysisProtocolCollection;
    }

    /**
     * Sets the value of the analysisProtocolCollection property.
     *
     * @param value
     *     allowed object is
     *     {@link AnalysisProtocolCollection }
     *
     */
    public void setAnalysisProtocolCollection(AnalysisProtocolCollection value) {
        this.analysisProtocolCollection = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(String value) {
        this.version = value;
    }

}

