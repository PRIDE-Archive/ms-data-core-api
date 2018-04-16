package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * The collection of input and output data sets of the analyses.
 *
 * <p>Java class for DataCollectionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DataCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Inputs" type="{http://psidev.info/psi/pi/mzIdentML/1.1}InputsType"/&gt;
 *         &lt;element name="AnalysisData" type="{http://psidev.info/psi/pi/mzIdentML/1.1}AnalysisDataType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataCollectionType", propOrder = {
        "inputs",
        "analysisData"
})
@Getter
@Setter
public class DataCollection extends MzIdentMLObject implements Serializable {

    private final static long serialVersionUID = 100L;
    @XmlElement(name = "Inputs", required = true)
    protected Inputs inputs;
    @XmlElement(name = "AnalysisData", required = true)
    protected AnalysisData analysisData;
}
