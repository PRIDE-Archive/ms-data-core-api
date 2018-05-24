package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Suresh Hewapathirana
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "MzIdentMLType",
        propOrder = {
                "cvList",
                "analysisSoftwareList",
                "provider",
                "auditCollection",
                "analysisSampleCollection",
                "sequenceCollection",
                "analysisProtocolCollection",
                "dataCollection",
                "bibliographicReference"
        }
)
public class MzIdentML extends Identifiable implements Serializable {

    private static final long serialVersionUID = 100L;
    @XmlAttribute(required = true)
    protected String version;
    @XmlAttribute
    protected String creationDate;
    @XmlElement(required = true)
    protected CvList cvList;
    @XmlElement(name = "AnalysisSoftwareList")
    protected AnalysisSoftwareList analysisSoftwareList;
    @XmlElement(name = "Provider")
    protected Provider provider;
    @XmlElement(name = "AuditCollection")
    protected AuditCollection auditCollection;
    @XmlElement(name = "AnalysisSampleCollection")
    protected AnalysisSampleCollection analysisSampleCollection;
    @XmlElement(name = "SequenceCollection")
    protected SequenceCollection sequenceCollection;
    @XmlElement(name = "AnalysisProtocolCollection", required = true)
    protected AnalysisProtocolCollection analysisProtocolCollection;
    @XmlElement(name = "DataCollection", required = true)
    protected DataCollection dataCollection;
    @XmlElement(name = "BibliographicReference")
    protected List<BibliographicReference> bibliographicReference;
}
