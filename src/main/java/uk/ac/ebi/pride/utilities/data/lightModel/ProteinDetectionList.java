package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The protein list resulting from a protein detection process.
 * <p>
 * <p>Java class for ProteinDetectionListType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="ProteinDetectionListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ProteinAmbiguityGroup" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ProteinAmbiguityGroupType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProteinDetectionListType", propOrder = {
        "proteinAmbiguityGroup"
})
public class ProteinDetectionList
        extends Identifiable
        implements Serializable {

    private final static long serialVersionUID = 100L;
    @XmlElement(name = "ProteinAmbiguityGroup")
    protected List<ProteinAmbiguityGroup> proteinAmbiguityGroup;

    /**
     * Gets the value of the proteinAmbiguityGroup property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the proteinAmbiguityGroup property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProteinAmbiguityGroup().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProteinAmbiguityGroup }
     */
    public List<ProteinAmbiguityGroup> getProteinAmbiguityGroup() {
        if (proteinAmbiguityGroup == null) {
            proteinAmbiguityGroup = new ArrayList<ProteinAmbiguityGroup>();
        }
        return this.proteinAmbiguityGroup;
    }
}
