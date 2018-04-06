package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Suresh Hewapathirana
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "MzIdentMLType",
        propOrder = {
                "analysisSoftwareList",
                "sequenceCollection",
                "dataCollection",
                "analysisProtocolCollection"
        }
)
public class MzIdentML extends Identifiable implements Serializable {

    private static final long serialVersionUID = 100L;

    @XmlAttribute(required = true)
    protected String version;
    @XmlElement(name = "AnalysisSoftwareList")
    protected AnalysisSoftwareList analysisSoftwareList;
    @XmlElement(name = "SequenceCollection")
    protected SequenceCollection sequenceCollection;
    @XmlElement(name = "DataCollection", required = true)
    protected DataCollection dataCollection;
    @XmlElement(name = "AnalysisProtocolCollection", required = true)
    protected AnalysisProtocolCollection analysisProtocolCollection;
}
