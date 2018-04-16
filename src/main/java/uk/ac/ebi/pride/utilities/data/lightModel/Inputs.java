package uk.ac.ebi.pride.utilities.data.lightModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The inputs to the analyses including the databases searched, the spectral data and the source file converted to mzIdentML.
 *
 * <p>Java class for InputsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="InputsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SourceFile" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SourceFileType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="SearchDatabase" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SearchDatabaseType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="SpectraData" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SpectraDataType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InputsType", propOrder = {
        "sourceFile",
        "searchDatabase",
        "spectraData"
})
public class Inputs
        extends MzIdentMLObject
        implements Serializable
{

    private final static long serialVersionUID = 100L;

    @XmlElement(name = "SourceFile")
    protected List<SourceFile> sourceFile;
    @XmlElement(name = "SearchDatabase")
    protected List<SearchDatabase> searchDatabase;
    @XmlElement(name = "SpectraData", required = true)
    protected List<SpectraData> spectraData;

    /**
     * Gets the value of the sourceFile property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceFile property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceFile().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SourceFile }
     *
     *
     */
    public List<SourceFile> getSourceFile() {
        if (sourceFile == null) {
            sourceFile = new ArrayList<SourceFile>();
        }
        return this.sourceFile;
    }

    /**
     * Gets the value of the searchDatabase property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the searchDatabase property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSearchDatabase().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SearchDatabase }
     *
     *
     */
    public List<SearchDatabase> getSearchDatabase() {
        if (searchDatabase == null) {
            searchDatabase = new ArrayList<SearchDatabase>();
        }
        return this.searchDatabase;
    }

    /**
     * Gets the value of the spectraData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spectraData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpectraDataMap().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpectraData }
     *
     *
     */
    public List<SpectraData> getSpectraData() {
        if (spectraData == null) {
            spectraData = new ArrayList<SpectraData>();
        }
        return this.spectraData;
    }
}
