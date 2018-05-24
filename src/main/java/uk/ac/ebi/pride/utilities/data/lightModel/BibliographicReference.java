package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Represents bibliographic references.
 *
 * <p>Java class for BibliographicReferenceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="BibliographicReferenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType"&gt;
 *       &lt;attribute name="authors" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="publication" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="publisher" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="editor" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="year" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="volume" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="issue" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="pages" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="doi" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BibliographicReferenceType")
@Getter
@Setter
public class BibliographicReference extends Identifiable implements Serializable {

  private final static long serialVersionUID = 100L;
  @XmlAttribute
  protected String authors;
  @XmlAttribute
  protected String publication;
  @XmlAttribute
  protected String publisher;
  @XmlAttribute
  protected String editor;
  @XmlAttribute
  protected Integer year;
  @XmlAttribute
  protected String volume;
  @XmlAttribute
  protected String issue;
  @XmlAttribute
  protected String pages;
  @XmlAttribute
  protected String title;
  @XmlAttribute
  protected String doi;
}
