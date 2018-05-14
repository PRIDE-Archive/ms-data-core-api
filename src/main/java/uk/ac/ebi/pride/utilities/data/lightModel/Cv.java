package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * A source controlled vocabulary from which cvParams will be obtained.
 *
 * <p>Java class for cvType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="cvType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="fullName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="uri" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /\&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cvType")
public class Cv extends MzIdentMLObject implements Serializable {

    private final static long serialVersionUID = 100L;

    @XmlAttribute(required = true)
    protected String fullName;
    @XmlAttribute
    protected String version;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String uri;
    @XmlAttribute(required = true)
    protected String id;
}
