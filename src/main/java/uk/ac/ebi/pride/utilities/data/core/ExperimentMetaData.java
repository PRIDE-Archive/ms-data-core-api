package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * General descriptions shared or used for the whole file.
 * MetaData Description at the Experimental level contain the information for:
 * - Samples Description.
 * - File Version.
 * - List of the Softwares used in the Experimental Protocol.
 * - File Creation Date.
 * - An a List of References.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 08-Mar-2010
 * Time: 10:48:29
 */
public class ExperimentMetaData extends IdentifiableParamGroup {

    /**
     * The Provider of the mzIdentML record in terms of the contact and software. The provider
     * is an instance with the the last final software that generate the file.
     */
    private Provider provider;

    /**
     * Organizations related in the file, this structure comes from mzidentml files.
     */
    private final List<Organization> organizations;

    /**
     * The complete set of Contacts People for this file.
     * In case of MzMl the Person Object Must be constructed whit a set of Params Group.
     * Extracted From CvParams.
     * List of contact details
     * In mzMl 1.1.0.1, each contact must have the following cv terms:
     * May include one or more child terms of "contact person attribute"
     * (contact name, contact address, contact email and et al)
     */
    private final List<Person> persons;

    /**
     * The Experiment Protocols is an small view of the protocols used in the Experiment
     * the idea is to define an small object like pride experiments that could be used also for MzIdentMl in order
     * to put and small description of the steps in the experiment.
     */
    private ExperimentProtocol protocol;

    private Date creationDate;

    private Date publicDate;

    private final List<Reference> references;

    private final List<Sample> samples;

    private Map<Comparable, StudyVariable> studyVariables;

    /*
     * Short Label used by Pride XML Object
     */
    private String shortLabel;

    /**
     * list and description of software used to acquire and/or process
     * the data in the file.
     */
    private final List<Software> softwares;


    private final List<SourceFile> sourceFiles;

    /**
     * List of SpectraData Objects used by MZIdentML to refereed the original mass spectra files.
     * A data set containing spectra data (consisting of one or more spectra).
     * <p/>
     */
    private final List<SpectraData> spectraDatas;

    /**
     * version of this document used for PRIDE and MzIdentML
     */
    private String version;

    public ExperimentMetaData(ParamGroup params, Comparable id, String name, String version, String shortLabel,
                              List<Sample> samples, List<Software> softwares, List<Person> persons,
                              List<SourceFile> sourceFiles, Provider provider, List<Organization> organizations,
                              List<Reference> references, Date creationDate, Date publicDate,
                              ExperimentProtocol protocol) {
        this(params, id, name, version, shortLabel,
                samples, softwares, persons,
                sourceFiles, provider, organizations,
                references, creationDate, publicDate, protocol, null);
    }

    /**
     * @param params        Params Group of Experiment Meta Data
     * @param id            Generic Id of the Experiment
     * @param name          Generic Name of the Experiment
     * @param version       File version
     * @param shortLabel    Short Label of the Experiment (PRIDE XML and PRIDE Database)
     * @param samples       Sample List
     * @param softwares     Software List used in the Experiment
     * @param persons       Contact List
     * @param sourceFiles   Source Files related with the Experiment
     * @param provider      Last Software and Contact that Provide the File or Experiment Results (mzidentML)
     * @param organizations Organization List involve in the Experiment
     * @param references    References related with the Experiments
     * @param creationDate  Creation Date
     * @param publicDate    Publication Date (PRIDE XML)
     * @param protocol      Experiment General Protocol (PRIDE XML)
     * @param spectraDatas  Spectra Data Files related with the Experiment (mzIdentML)
     */
    public ExperimentMetaData(ParamGroup params, Comparable id, String name, String version, String shortLabel,
                              List<Sample> samples, List<Software> softwares, List<Person> persons,
                              List<SourceFile> sourceFiles, Provider provider, List<Organization> organizations,
                              List<Reference> references, Date creationDate, Date publicDate,
                              ExperimentProtocol protocol, List<SpectraData> spectraDatas) {
        super(params, id, name);

        this.version = version;

        this.samples = CollectionUtils.createListFromList(samples);

        this.softwares = CollectionUtils.createListFromList(softwares);

        this.persons = CollectionUtils.createListFromList(persons);

        this.sourceFiles = CollectionUtils.createListFromList(sourceFiles);

        this.provider = provider;

        this.organizations = CollectionUtils.createListFromList(organizations);

        this.references = CollectionUtils.createListFromList(references);

        this.creationDate = creationDate;

        this.publicDate = publicDate;

        this.protocol = protocol;

        this.shortLabel = shortLabel;

        this.spectraDatas = CollectionUtils.createListFromList(spectraDatas);
    }

    /**
     * This controller is specially designed for mzTab files that contains Study Variables with description, etc.
     *
     * @param params        Params Group of Experiment Meta Data
     * @param id            Generic Id of the Experiment
     * @param name          Generic Name of the Experiment
     * @param version       File version
     * @param shortLabel    Short Label of the Experiment (PRIDE XML and PRIDE Database)
     * @param samples       Sample List
     * @param softwares     Software List used in the Experiment
     * @param persons       Contact List
     * @param sourceFiles   Source Files related with the Experiment
     * @param provider      Last Software and Contact that Provide the File or Experiment Results (mzidentML)
     * @param organizations Organization List involve in the Experiment
     * @param references    References related with the Experiments
     * @param creationDate  Creation Date
     * @param publicDate    Publication Date (PRIDE XML)
     * @param protocol      Experiment General Protocol (PRIDE XML)
     * @param spectraDatas  Spectra Data Files related with the Experiment (mzIdentML)
     *
     */
    public ExperimentMetaData(ParamGroup params, Comparable id, String name, String version, String shortLabel,
                              List<Sample> samples, List<Software> softwares, List<Person> persons,
                              List<SourceFile> sourceFiles, Provider provider, List<Organization> organizations,
                              List<Reference> references, Date creationDate, Date publicDate,
                              ExperimentProtocol protocol, List<SpectraData> spectraDatas, Map<Comparable, StudyVariable> studyVariables) {
        super(params, id, name);

        this.version = version;

        this.samples = CollectionUtils.createListFromList(samples);

        this.softwares = CollectionUtils.createListFromList(softwares);

        this.persons = CollectionUtils.createListFromList(persons);

        this.sourceFiles = CollectionUtils.createListFromList(sourceFiles);

        this.provider = provider;

        this.organizations = CollectionUtils.createListFromList(organizations);

        this.references = CollectionUtils.createListFromList(references);

        this.creationDate = creationDate;

        this.publicDate = publicDate;

        this.protocol = protocol;

        this.shortLabel = shortLabel;

        this.spectraDatas = CollectionUtils.createListFromList(spectraDatas);

        this.studyVariables = studyVariables;
    }

    public Map<Comparable, StudyVariable> getStudyVariables() {
        return studyVariables;
    }

    public void setStudyVariables(Map<Comparable, StudyVariable> studyVariables) {
        this.studyVariables = studyVariables;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        CollectionUtils.replaceValuesInCollection(organizations, this.organizations);
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        CollectionUtils.replaceValuesInCollection(references, this.references);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getPublicDate() {
        return publicDate;
    }

    public void setPublicDate(Date publicDate) {
        this.publicDate = publicDate;
    }

    public ExperimentProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ExperimentProtocol protocol) {
        this.protocol = protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        CollectionUtils.replaceValuesInCollection(samples, this.samples);
    }

    public List<Software> getSoftwares() {
        return softwares;
    }

    public void setSoftwares(List<Software> softwares) {
        CollectionUtils.replaceValuesInCollection(softwares, this.softwares);
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        CollectionUtils.replaceValuesInCollection(persons, this.persons);
    }

    public List<SourceFile> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<SourceFile> sourceFiles) {
        CollectionUtils.replaceValuesInCollection(sourceFiles, this.sourceFiles);
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public ParamGroup getAdditional() {
        return new ParamGroup(this.getCvParams(), this.getUserParams());
    }

    public ParamGroup getFileContent() {
        return new ParamGroup(this.getCvParams(), this.getUserParams());
    }

    public List<SpectraData> getSpectraDatas() {
        return spectraDatas;
    }

    public void setSpectraDatas(List<SpectraData> spectraDatas) {
        CollectionUtils.replaceValuesInCollection(spectraDatas, this.spectraDatas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperimentMetaData)) return false;
        if (!super.equals(o)) return false;

        ExperimentMetaData that = (ExperimentMetaData) o;

        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (!organizations.equals(that.organizations)) return false;
        if (!persons.equals(that.persons)) return false;
        if (protocol != null ? !protocol.equals(that.protocol) : that.protocol != null) return false;
        if (provider != null ? !provider.equals(that.provider) : that.provider != null) return false;
        if (publicDate != null ? !publicDate.equals(that.publicDate) : that.publicDate != null) return false;
        if (!references.equals(that.references)) return false;
        if (!samples.equals(that.samples)) return false;
        if (shortLabel != null ? !shortLabel.equals(that.shortLabel) : that.shortLabel != null) return false;
        return softwares.equals(that.softwares) && sourceFiles.equals(that.sourceFiles) && spectraDatas.equals(that.spectraDatas) && !(version != null ? !version.equals(that.version) : that.version != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + organizations.hashCode();
        result = 31 * result + persons.hashCode();
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (publicDate != null ? publicDate.hashCode() : 0);
        result = 31 * result + references.hashCode();
        result = 31 * result + samples.hashCode();
        result = 31 * result + (shortLabel != null ? shortLabel.hashCode() : 0);
        result = 31 * result + softwares.hashCode();
        result = 31 * result + sourceFiles.hashCode();
        result = 31 * result + spectraDatas.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}



