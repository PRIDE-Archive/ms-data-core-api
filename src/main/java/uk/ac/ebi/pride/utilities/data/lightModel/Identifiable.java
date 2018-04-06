package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Other classes in the model can be specified as sub-classes, inheriting from Identifiable.
 * Identifiable gives classes a unique identifier within the scope and a name that need not be unique.
 * <p>
 * <p>Java class for IdentifiableType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="IdentifiableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifiableType")
@XmlSeeAlso({
        MzIdentML.class,
        Peptide.class,
        DBSequence.class,
        SequenceCollection.class
})
public abstract class Identifiable extends MzIdentMLObject implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute
    protected String name;
}
