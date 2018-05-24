package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.ebi.jmzidml.model.mzidml.DBSequence;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.MzIdentMLCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzIdentMLTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Organization;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Person;
import uk.ac.ebi.pride.utilities.data.core.Provider;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentificationProtocol;
import uk.ac.ebi.pride.utilities.data.io.file.MzIdentMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MzIdentMLControllerImpl is the controller that retrieve the information from
 * the mzidentml files. It have support for Experiment Metadata (Global metadata),
 * also it have information about the IdentificationMetadata. The MzGraphMetaData is not
 * supported for this files because they not contains information about spectrum. The controller
 * support the mzidentml schema version 1.1.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 19/09/11
 */
public class MzIdentMLControllerImpl extends ReferencedIdentificationController {

    // Logger property to trace the Errors
    private static final Logger logger = LoggerFactory.getLogger(MzIdentMLControllerImpl.class);

    //The unmarshller class that retrieve the information from the mzidentml files
    private MzIdentMLUnmarshallerAdaptor unmarshaller;

    // The Match pattern for a valid mzidentml file, its support now the version 1.1.
    private static final Pattern mzIdentMLHeaderPattern = Pattern.compile("^[^<]*(<\\?xml [^>]*>\\s*(<!--[^>]*-->\\s*)*)?<(MzIdentML)|(indexedmzIdentML) xmlns=.*", Pattern.MULTILINE);

    public MzIdentMLControllerImpl(File file) {
        this(file, false);
    }

    public MzIdentMLControllerImpl(File file, boolean inMemory) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize(inMemory, false);
    }

    public MzIdentMLControllerImpl(File file, boolean inMemory, boolean avoidProteinInference) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize(inMemory, avoidProteinInference);
    }

    /**
     * This function initialize all the Categories in which the Controller
     * used the Cache System. In this case it wil be use cache for PROTEIN,
     * PEPTIDE, SAMPLE and SOFTWARE.
     */
    protected void initialize(boolean inMemory, boolean avoidProteinInference) {
        // create pride access utils
        File file = (File) getSource();
        try {
            unmarshaller = new MzIdentMLUnmarshallerAdaptor(file, inMemory, avoidProteinInference);
        } catch (ConfigurationException e) {
            String msg = "Failed to create XML unmarshaller for mzIdentML file: " + file.getAbsolutePath();
            throw new DataAccessException(msg, e);
        }

        // init ms data accession controller map
        this.msDataAccessControllers = new HashMap<Comparable, DataAccessController>();

        // set data source description
        this.setName(file.getName());

        // set the type
        this.setType(Type.MZIDENTML);

        // set the content categories
        this.setContentCategories(
                ContentCategory.PROTEIN,
                ContentCategory.PEPTIDE,
                ContentCategory.SAMPLE,
                ContentCategory.SOFTWARE,
                ContentCategory.PROTEIN_GROUPS,
                ContentCategory.SPECTRUM
        );

        setCachingStrategy(new MzIdentMLCachingStrategy());
        populateCache();

        Object cvLookup = getCache().get(CacheEntry.CV_LOOKUP);
        if (cvLookup != null) {
            MzIdentMLTransformer.setCvLookupMap((Map<String, CVLookup>) cvLookup);
        }

        Object fragmentationTable = getCache().get(CacheEntry.FRAGMENTATION_TABLE);
        MzIdentMLTransformer.setFragmentationTable((Map<String, IdentifiableParamGroup>) fragmentationTable);
    }

    /**
     * Return the mzidentml unmarshall adaptor to be used by the CacheBuilder
     * Implementation.
     *
     * @return MzIdentMLUnmarshallerAdaptor
     */
    public MzIdentMLUnmarshallerAdaptor getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Get the unique id of the data access controller
     *
     * @return String  unique id
     */
    @Override
    public String getUid() {
        String uid = super.getUid();
        if (uid == null) {
            // create a new UUID
            File file = (File) this.getSource();
            try {
                uid = MD5Utils.generateHash(file.getAbsolutePath());
            } catch (NoSuchAlgorithmException e) {
                String msg = "Failed to generate unique id for mzIdentML file";
                logger.error(msg, e);
            }
        }
        return uid;
    }

    /**
     * Get a list of cv lookup objects.
     *
     * @return List<CVLookup>   a list of cvlookup objects.
     */
    public List<CVLookup> getCvLookups() {
        return MzIdentMLTransformer.transformCVList(unmarshaller.getCvList());
    }

    /**
     * Get a list of source files.
     *
     * @return List<SourceFile> a list of source file objects.
     * @throws uk.ac.ebi.pride.utilities.data.controller.DataAccessException
     *
     */
    public List<SourceFile> getSourceFiles() {
        List<SourceFile> sourceFiles;

        try {
            sourceFiles = MzIdentMLTransformer.transformToSourceFile(unmarshaller.getSourceFiles());
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve source files", ex);
        }

        return sourceFiles;
    }

    /**
     * Get a list of Organization Contacts
     *
     * @return List<Organization> A List of Organizations
     */
    public List<Organization> getOrganizationContacts() {
        List<Organization> organizationList;

        try {
            organizationList = MzIdentMLTransformer.transformToOrganization(unmarshaller.getOrganizationContacts());
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve organization contacts", ex);
        }

        return organizationList;
    }

    /**
     * Get a list of Person Contacts
     *
     * @return List<Person> A list of Persons
     */
    public List<Person> getPersonContacts() {
        List<Person> personList;
        try {
            personList = MzIdentMLTransformer.transformToPerson(unmarshaller.getPersonContacts());
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve person contacts", ex);
        }
        return personList;
    }

    /**
     * Get a list of samples
     *
     * @return List<Sample> a list of sample objects.
     */
    @Override
    public List<Sample> getSamples() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                return MzIdentMLTransformer.transformToSample(unmarshaller.getSampleList());
            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve samples", ex);
            }
        } else {
            return metaData.getSamples();
        }
    }

    /**
     * Get provider of the experiment
     *
     * @return Provider    data provider
     */
    public Provider getProvider() {
        ExperimentMetaData metaData = super.getExperimentMetaData();
        if (metaData == null) {
            return MzIdentMLTransformer.transformToProvider(unmarshaller.getProvider());
        }
        return metaData.getProvider();
    }

    /**
     * Get a list of softwares
     *
     * @return List<Software>   a list of software objects.
     */
    public List<Software> getSoftwares() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                return MzIdentMLTransformer.transformToSoftware(unmarshaller.getSoftwares());
            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve software", ex);
            }
        } else {
            return metaData.getSoftwares();
        }
    }

    /**
     * Get a list of references
     *
     * @return List<Reference>  a list of reference objects
     */
    public List<Reference> getReferences() {
        List<Reference> refs;
        try {
            refs = MzIdentMLTransformer.transformToReference(unmarshaller.getReferences());
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve references", ex);
        }

        return refs;
    }

    /**
     * Additional is a concept that comes from PRIDE XML Files. In the mzidentml
     * all the concepts of the Additional comes inside different objects.
     * This function construct an Additional Object a relation of
     * creationDate, Original Spectra Data Files and finally the Original software
     * that provide the mzidentml file.
     *
     * @return ParamGroup   a group of cv parameters and user parameters.
     */
    @Override
    public ParamGroup getAdditional() {
        ParamGroup additionals = null;
        // Take information from provider !!!
        Provider provider = getProvider();
        Date date = unmarshaller.getCreationDate();
        List<SpectraData> spectraDataList = getSpectraDataFiles();

        if ((provider != null && provider.getSoftware() != null) || date != null || !spectraDataList.isEmpty()) {
            additionals = new ParamGroup();
            // Get information from last software that provide the file
            if (provider != null && provider.getSoftware() != null)
                additionals.addCvParams(provider.getSoftware().getCvParams());

            // Get the information of the creation file
            if (unmarshaller.getCreationDate() != null) {
                additionals.addCvParam(MzIdentMLTransformer.transformDateToCvParam(unmarshaller.getCreationDate()));
            }
            //Get spectra information as additional
            if (!spectraDataList.isEmpty()) {
                Set<CvParam> cvParamList = new HashSet<CvParam>();
                for (SpectraData spectraData : spectraDataList) {
                    if (spectraData.getSpectrumIdFormat() != null)
                        cvParamList.add(spectraData.getSpectrumIdFormat());
                    if (spectraData.getFileFormat() != null)
                        cvParamList.add(spectraData.getFileFormat());
                }
                List<CvParam> list = new ArrayList<CvParam>(cvParamList);
                additionals.addCvParams(list);
            }
        }
        return additionals;
    }

    /**
     * The mzidentml do not support Quantitation Data
     *
     * @return false.
     */
    @Override
    public boolean hasQuantData() {
        return false;
    }

    /**
     * Get meta data related to this experiment
     *
     * @return MetaData meta data object
     */
    @Override
    public ExperimentMetaData getExperimentMetaData() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                // Get Accession for MzIdentML Object
                String accession = unmarshaller.getMzIdentMLId();
                // Get the Version of the MzIdentML File.
                String version = unmarshaller.getMzIdentMLVersion();
                //Get Source File List
                List<SourceFile> sources = getSourceFiles();
                //Get Sample List
                List<Sample> samples = getSamples();
                // Get all the softwares related with the object
                List<Software> softwares = getSoftwares();
                // Get Contact Persons
                List<Person> persons = getPersonContacts();
                // Get the Contact Organization
                List<Organization> organizations = getOrganizationContacts();
                // Get Additional Information Related with the Project
                ParamGroup additional = getAdditional();
                // Get the Experiment Title
                String title = unmarshaller.getMzIdentMLName();
                // Get The Experiment Short Label, in case of mzidentml this date is not provided.
                String shortLabel = null;
                //Get Experiment Protocol in case of mzidentml Experiment Protocol is empty.
                ExperimentProtocol protocol = null;
                // Get References From the Experiment
                List<Reference> references = getReferences();
                // Get the provider object of the MzIdentMl file
                Provider provider = getProvider();
                //Get Creation Date
                Date creationDate = unmarshaller.getCreationDate();
                //Get SpectraData Files
                List<SpectraData> spectraData = getSpectraDataFiles();
                //Create the ExperimentMetaData Object
                metaData = new ExperimentMetaData(additional, accession, title, version, shortLabel, samples, softwares,
                        persons, sources, provider, organizations, references, creationDate, null, protocol, spectraData);
                // store it in the cache
                getCache().store(CacheEntry.EXPERIMENT_METADATA, metaData);
            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve meta data", ex);
            }
        }
        //System.out.println("Protein Ids: " + getProteinIds().size());
        //System.out.println("Peptide Ids: " + getNumberOfPeptides());
        return metaData;
    }

    /**
     * The spectrum IdentificationProtocol is the Set of parameters Related with
     * the Spectrum Identification Process in terms of Search Engines, Databases,
     * Enzymes, Modifications and Database Filters, etc
     *
     * @return List<SpectrumIdentificationProtocol> A List of Spectrum Identification Protocols
     */
    public List<SpectrumIdentificationProtocol> getSpectrumIdentificationProtocol() {
        IdentificationMetaData identificationMetaData = super.getIdentificationMetaData();

        if (identificationMetaData == null) {
            return MzIdentMLTransformer.transformToSpectrumIdentificationProtocol(unmarshaller.getSpectrumIdentificationProtocol());
        }
        return identificationMetaData.getSpectrumIdentificationProtocols();
    }

    /**
     * The Protein Protocol is a relation of different Software and Processing Steps with
     * the Identified Proteins.
     *
     * @return Protocol Protein Protocol
     */
    public Protocol getProteinDetectionProtocol() {
        IdentificationMetaData identificationMetaData = super.getIdentificationMetaData();
        if (identificationMetaData == null) {
            return MzIdentMLTransformer.transformToProteinDetectionProtocol(unmarshaller.getProteinDetectionProtocol());
        }
        return identificationMetaData.getProteinDetectionProtocol();
    }

    /**
     * Get the List of Databases used in the Experiment
     *
     * @return List<SearchDataBase> List of SearchDatabases
     */
    public List<SearchDataBase> getSearchDataBases() {
        IdentificationMetaData identificationMetaData = super.getIdentificationMetaData();
        if (identificationMetaData == null) {
            return MzIdentMLTransformer.transformToSearchDataBase(unmarshaller.getSearchDatabases());
        }
        return identificationMetaData.getSearchDataBases();
    }

    /**
     * The IdentificationMetadata is a Combination of SpectrumIdentificationProtocol,
     * a Protein Protocol and finally the Databases used in the Experiment.
     *
     * @return IdentificationMetadata the metadata related with the identification process
     */
    @Override
    public IdentificationMetaData getIdentificationMetaData() {
        IdentificationMetaData metaData = super.getIdentificationMetaData();
        if (metaData == null) {
            List<SpectrumIdentificationProtocol> spectrumIdentificationProtocolList = getSpectrumIdentificationProtocol();
            Protocol proteinDetectionProtocol = getProteinDetectionProtocol();
            List<SearchDataBase> searchDataBaseList = getSearchDataBases();
            metaData = new IdentificationMetaData(null, null, spectrumIdentificationProtocolList, proteinDetectionProtocol, searchDataBaseList);
        }
        return metaData;
    }

    /**
     * Get the List of File Spectra that the Mzidentml use to identified peptides
     *
     * @return List of SpectraData Files associated with mzidentml.
     */
    public List<SpectraData> getSpectraDataFiles() {
        ExperimentMetaData metaData = super.getExperimentMetaData();
        List<Comparable> basedOnTitle = new ArrayList<Comparable>();

        if (metaData == null) {
            if(isSpectrumBasedOnTitle())
                basedOnTitle = getSpectraDataBasedOnTitle();
            return MzIdentMLTransformer.transformToSpectraData(unmarshaller.getSpectraData(), basedOnTitle);

        }
        return metaData.getSpectraDatas();
    }

    /**
     * MzidemtML files will support in the future Spectra MetaData if is present
     * PRIDE Objects, also by other file Formats.
     *
     * @return The metadata related with mz information
     */
    @Override
    public MzGraphMetaData getMzGraphMetaData() {
        return null;
    }

    /**
     * Get identification using a identification id, gives the option to choose whether to use cache.
     * This implementation provides a way of by passing the cache.
     *
     * @param proteinId identification id
     * @param useCache  true means to use cache
     * @return Identification identification object
     */
    @Override
    public Protein getProteinById(Comparable proteinId, boolean useCache) {
        Protein ident = super.getProteinById(proteinId, useCache);

        if (ident == null && useCache) {

            logger.debug("Get new identification from file: {}", proteinId);

            try {
                // get protein hypothesis
                if (!hasProteinAmbiguityGroup() || proteinsAreInferred()) {
                    // when protein groups are not present or inferred from all proteins
                    uk.ac.ebi.jmzidml.model.mzidml.DBSequence dbSequence = (DBSequence) super.getObjectByID(CacheEntry.DB_SEQUENCE,proteinId, true);
                    if(dbSequence == null){
                        dbSequence = unmarshaller.getDBSequenceById(proteinId);
                        getCache().store(CacheEntry.DB_SEQUENCE, proteinId, dbSequence);
                    }
                    dbSequence = unmarshaller.getDBSequenceById(proteinId);
                    List<SpectrumIdentificationItem> spectrumIdentificationItems = getScannedSpectrumIdentificationItems(proteinId);
                    Iterator<SpectrumIdentificationItem> itSpec = spectrumIdentificationItems.iterator();
                    List<Peptide> peptides = new ArrayList<Peptide>();
                    while(itSpec.hasNext()){
                        SpectrumIdentificationItem item = itSpec.next();
                        for(PeptideEvidenceRef ref: item.getPeptideEvidenceRef()){
                            uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence evidence = unmarshaller.getPeptideEvidenceById(ref.getPeptideEvidenceRef());
                            peptides.add(MzIdentMLTransformer.transformToPeptideFromSpectrumItemAndPeptideEvidence(item,evidence,peptides.size()));
                        }
                    }

                    ident = MzIdentMLTransformer.transformDBSequenceToIdentification(dbSequence, peptides);

                } else {

                    List<Peptide> peptides = new ArrayList<Peptide>();
                    uk.ac.ebi.jmzidml.model.mzidml.ProteinDetectionHypothesis proteinHypothesis = unmarshaller.getIdentificationById(proteinId);
                    // when protein groups are present
                    for(PeptideHypothesis peptideHypothesis: proteinHypothesis.getPeptideHypothesis()){

                        if(peptideHypothesis != null &&
                                peptideHypothesis.getSpectrumIdentificationItemRef() != null &&
                                !peptideHypothesis.getSpectrumIdentificationItemRef().isEmpty()){
                            uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence peptideEvidence = (uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence) super.getObjectByID(CacheEntry.PEPTIDE_EVIDENCE, peptideHypothesis.getPeptideEvidenceRef(), true);
                            if(peptideEvidence == null){
                                peptideEvidence = unmarshaller.getPeptideEvidenceById(peptideHypothesis.getPeptideEvidenceRef());
                                getCache().store(CacheEntry.PEPTIDE_EVIDENCE, peptideHypothesis.getPeptideEvidenceRef(), peptideEvidence);
                            }
                            for(SpectrumIdentificationItemRef ref: peptideHypothesis.getSpectrumIdentificationItemRef()){
                                uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem spectrumID = (uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem) super.getObjectByID(CacheEntry.SPECTRUM_ID_ITEM, ref.getSpectrumIdentificationItemRef(), true);
                                if(spectrumID == null){
                                    spectrumID = unmarshaller.getSpectrumIdentificationsById(ref.getSpectrumIdentificationItemRef());
                                    getCache().store(CacheEntry.SPECTRUM_ID_ITEM, ref.getSpectrumIdentificationItemRef(), spectrumID);
                                }
                                if(spectrumID != null && peptideEvidence != null)
                                    peptides.add(MzIdentMLTransformer.transformToPeptideFromSpectrumItemAndPeptideEvidence(spectrumID,peptideEvidence, peptides.size()));
                            }
                        }
                    }
                    uk.ac.ebi.jmzidml.model.mzidml.DBSequence dbSequence = (uk.ac.ebi.jmzidml.model.mzidml.DBSequence)super.getObjectByID(CacheEntry.DB_SEQUENCE, proteinHypothesis.getDBSequenceRef(), true);
                    if(dbSequence == null){
                        dbSequence = unmarshaller.getDBSequenceById(proteinHypothesis.getDBSequenceRef());
                        getCache().store(CacheEntry.DB_SEQUENCE, proteinHypothesis.getDBSequenceRef(), dbSequence);
                    }
                    proteinHypothesis.setDBSequence(dbSequence);

                    ident = MzIdentMLTransformer.transformProteinHypothesisToIdentification(proteinHypothesis,peptides);
                }

                if (ident != null) {
                    cacheProtein(ident);
                }
            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve protein identification: " + proteinId, ex);
            }
        }
        return ident;
    }

    private List<SpectrumIdentificationItem> getScannedSpectrumIdentificationItems(Comparable proteinId) throws JAXBException {
        List<Comparable> spectrumIdentIds = null;

        if (getCache().hasCacheEntry(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES)) {
            spectrumIdentIds = ((Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES)).get(proteinId);
        }

        return unmarshaller.getSpectrumIdentificationsByIds(spectrumIdentIds);
    }

    /**
     * Get the number of peptides.
     *
     * @return int  the number of peptides.
     */
    @Override
    public int getNumberOfPeptides() {
        int num;
        try {
            // this method is overridden to use the reader directly
            num = unmarshaller.getNumIdentifiedPeptides();
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve number of peptides", ex);
        }
        return num;
    }

    @Override
    public void close() {
        unmarshaller = null;
        super.close();

    }


    /**
     * Check a file is MZIdentML XML file
     *
     * @param file input file
     * @return boolean true means MZIdentML XML
     */
    public static boolean isValidFormat(File file) {
        boolean valid = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            // read the first ten lines
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                content.append(reader.readLine());
            }
            // check file type
            Matcher matcher = mzIdentMLHeaderPattern.matcher(content);
            valid = matcher.find();
        } catch (Exception e) {
            logger.error("Failed to read file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing here
                }
            }
        }

        return valid;
    }

    /**
     * Returns the ProteinAmbiguityGroup with an specific ID
     * @param proteinGroupId Protein group ID
     * @return the ProteinGroup
     */
    @Override
    public ProteinGroup getProteinAmbiguityGroupById(Comparable proteinGroupId) {
        ProteinGroup proteinGroup = super.getProteinAmbiguityGroupById(proteinGroupId);

        if (proteinGroup == null) {

            try {
                ProteinAmbiguityGroup proteinAmbiguityGroup = unmarshaller.getProteinAmbiguityGroup(proteinGroupId);
                List<Protein> proteins = new ArrayList<>();

                for (ProteinDetectionHypothesis proteinDetectionHypothesis : proteinAmbiguityGroup.getProteinDetectionHypothesis()) {
                    proteins.add(getProteinById(proteinDetectionHypothesis.getId()));
                }

                proteinGroup = MzIdentMLTransformer.transformProteinAmbiguityGroupToProteinGroup(proteinAmbiguityGroup, proteins);

                if (proteinGroup != null) {
                    // store identification into cache
                    getCache().store(CacheEntry.PROTEIN_GROUP, proteinGroupId, proteinGroup);

//                    for (Protein protein : proteins) {
//                        cacheProtein(protein);
//                    }
                }

            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve protein group: " + proteinGroupId, ex);
            }
        }

        return proteinGroup;
    }

    /**
     * Check if the DataAccessController contains the ProteinSequence
     * @return True if the DataAccessController contains Protein Sequences
     */
    @Override
    public boolean hasProteinSequence() {
        try {
            return unmarshaller.hasProteinSequence();
        } catch (ConfigurationException ex) {
            String msg = "Error while reading the mzidentml file";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
        }
    }

    /**
     * Check if the DataAccessController contains the ProteinSequence
     * @return True if the DataAccessController contains Protein Sequences
     */
    @Override
    public boolean hasDecoyInformation() {
        try {
            return unmarshaller.hasDecoyInformation();
        } catch (ConfigurationException ex) {
            String msg = "Error while reading the mzidentml file";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
        }
    }
}
