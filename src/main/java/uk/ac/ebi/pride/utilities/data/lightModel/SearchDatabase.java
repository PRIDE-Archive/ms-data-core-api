package uk.ac.ebi.pride.utilities.data.lightModel;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.jmzidml.xml.jaxb.adapters.CalendarAdapter;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A database for searching mass spectra. Examples include a set of amino acid sequence entries, or annotated spectra libraries.
 *
 * <p>Java class for SearchDatabaseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SearchDatabaseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}ExternalDataType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DatabaseName" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamType"/&gt;
 *         &lt;element name="cvParam" type="{http://psidev.info/psi/pi/mzIdentML/1.1}CVParamType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="numDatabaseSequences" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="numResidues" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchDatabaseType", propOrder = {
        "databaseName",
        "cvParam"
})
@Getter
@Setter
public class SearchDatabase extends ExternalData implements Serializable, CvParamListCapable {

  private final static long serialVersionUID = 100L;
  @XmlElement(name = "DatabaseName", required = true)
  protected Param databaseName;
  protected List<CvParam> cvParam;
  @XmlAttribute
  protected String version;
  @XmlAttribute
  @XmlJavaTypeAdapter(CalendarAdapter.class)
  @XmlSchemaType(name = "dateTime")
  protected Calendar releaseDate;
  @XmlAttribute
  protected Long numDatabaseSequences;
  @XmlAttribute
  protected Long numResidues;

  /**
   * Gets the value of the cvParam property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the cvParam property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getCvParam().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link CvParam }
   */
  public List<CvParam> getCvParam() {
    if (cvParam == null) {
      cvParam = new ArrayList<CvParam>();
    }
    return this.cvParam;
  }
}