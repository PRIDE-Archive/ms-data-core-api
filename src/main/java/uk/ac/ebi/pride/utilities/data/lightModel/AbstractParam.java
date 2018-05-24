package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Abstract entity allowing either cvParam or userParam to be referenced in other schemas.
 *
 * NOTE: There is no setter method for the unitCvRef. This simplifies keeping the unitCv object reference and
 * unitCvRef synchronized.
 *
 * <p>Java class for AbstractParamType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AbstractParamType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unitAccession" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unitName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unitCvRef" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractParamType")
@XmlSeeAlso({
        CvParam.class
})
@Getter
@Setter
public abstract class AbstractParam extends MzIdentMLObject implements Serializable {

    private final static long serialVersionUID = 100L;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String value;
    @XmlAttribute
    protected String unitAccession;
    @XmlAttribute
    protected String unitName;
    @XmlAttribute
    protected String unitCvRef;
    @XmlTransient
    protected Cv unitCv;

    public void setUnitCv(Cv unitCv) {
        if (unitCv == null) {
            this.unitCvRef = null;
        } else {
            String refId = unitCv.getId();
            if (refId == null) throw new IllegalArgumentException("Referenced object does not have an identifier.");
            this.unitCvRef = refId;
        }
        this.unitCv = unitCv;
    }
}
