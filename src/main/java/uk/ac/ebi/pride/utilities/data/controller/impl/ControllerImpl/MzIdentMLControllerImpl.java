package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.jmzidml.model.mzidml.ProteinAmbiguityGroup;
import uk.ac.ebi.jmzidml.model.mzidml.ProteinDetectionHypothesis;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.MzIdentMLCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzIdentMLTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.io.file.MzIdentMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;

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
 * @author ypriverol
 * Date: 19/09/11
 */
public class MzIdentMLControllerImpl extends CachedDataAccessController {

    // Logger property to trace the Errors
    private static final Logger logger = LoggerFactory.getLogger(MzIdentMLControllerImpl.class);

    //The unmarshller class that retrieve the information from the mzidentml files
    private MzIdentMLUnmarshallerAdaptor unmarshaller;

    // The Match pattern for a valid mzidentml file, its support now the version 1.1.
    private static final Pattern mzIdentMLHeaderPattern = Pattern.compile("^[^<]*(<\\?xml [^>]*>\\s*(<!--[^>]*-->\\s*)*)?<(MzIdentML)|(indexedmzIdentML) xmlns=.*", Pattern.MULTILINE);

    /*
      * This is a set of controllers related with the MS information in the mzidentml file
      * one or more controllers can be related with the same file formats. The Comparable
      * name of the file is an id of the file and the controller is the DataAccessController
       * related with the file.
     */
    private Map<Comparable, DataAccessController> msDataAccessControllers;

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
     * The mzidentml do not support Quatitation Data
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
        if (metaData == null) {
            return MzIdentMLTransformer.transformToSpectraData(unmarshaller.getSpectraData());
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
     * If the spectrum information associated with the identification files is provided
     * then the mzidentml contains spectra.
     *
     * @return if the spectrum files is provided then is true else false.
     */
    @Override
    public boolean hasSpectrum() {
        if (msDataAccessControllers != null) {
            for (Comparable id : msDataAccessControllers.keySet()) {
                if (msDataAccessControllers.get(id) != null) {
                    return true;
                }
            }
        }
        return false;
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
                if (!hasProteinAmbiguityGroup()) {
                    // when protein groups are not present
                    uk.ac.ebi.jmzidml.model.mzidml.DBSequence dbSequence = unmarshaller.getDBSequenceById(proteinId);
                    List<SpectrumIdentificationItem> spectrumIdentificationItems = getScannedSpectrumIdentificationItems(proteinId);
                    ident = MzIdentMLTransformer.transformSpectrumIdentificationItemToIdentification(dbSequence, spectrumIdentificationItems);
                } else {
                    uk.ac.ebi.jmzidml.model.mzidml.ProteinDetectionHypothesis proteinHypothesis = unmarshaller.getIdentificationById(proteinId);
                    // when protein groups are present
                    uk.ac.ebi.jmzidml.model.mzidml.DBSequence dbSequence = unmarshaller.getDBSequenceById(proteinHypothesis.getDBSequenceRef());
                    proteinHypothesis.setDBSequence(dbSequence);

                    ident = MzIdentMLTransformer.transformProteinHypothesisToIdentification(proteinHypothesis);
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

    private void cacheProtein(Protein ident) {
        // store identification into cache
        getCache().store(CacheEntry.PROTEIN, ident.getId(), ident);
        // store precursor charge and m/z
        for (Peptide peptide : ident.getPeptides()) {
            getCache().store(CacheEntry.PEPTIDE, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getSpectrumIdentification().getId()), peptide);
            Comparable spectrumId = getSpectrumIdBySpectrumIdentificationItemId(peptide.getSpectrumIdentification().getId());
            if (hasSpectrum()) {
                Spectrum spectrum = getSpectrumById(spectrumId);
                if(spectrum != null) {
                    spectrum.setPeptide(peptide);
                    peptide.setSpectrum(spectrum);

                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_CHARGE, spectrum.getId(), DataAccessUtilities.getPrecursorChargeParamGroup(spectrum));
                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_MZ, spectrum.getId(), DataAccessUtilities.getPrecursorMz(spectrum));
                }

            }
        }
    }

    private List<SpectrumIdentificationItem> getScannedSpectrumIdentificationItems(Comparable proteinId) throws JAXBException {
        List<Comparable> spectrumIdentIds = null;

        if (getCache().hasCacheEntry(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES)) {
            spectrumIdentIds = ((Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES)).get(proteinId);
        }

        return unmarshaller.getSpectrumIdentificationsByIds(spectrumIdentIds);
    }

    @Override
    public Comparable getPeptideSpectrumId(Comparable proteinId, Comparable peptideId) {
        Peptide peptide = super.getPeptideByIndex(proteinId, peptideId, true);

        if (peptide == null) {
            logger.debug("Get new peptide from file: {}", peptideId);
            Protein ident = getProteinById(proteinId);
            peptide = ident.getPeptides().get(Integer.parseInt(peptideId.toString()));

            getCache().store(CacheEntry.PROTEIN, proteinId, ident);
            getCache().store(CacheEntry.PEPTIDE, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getSpectrumIdentification().getId()), peptide);
        }

        Comparable spectrumIdentificationId = peptide.getSpectrumIdentification().getId();
        return getSpectrumIdBySpectrumIdentificationItemId(spectrumIdentificationId);
    }

    /**
     * Get peptide using a given identification id and a given peptide index
     *
     * @param index    peptide index
     * @param useCache whether to use cache
     * @return Peptide  peptide
     */
    @Override
    public Peptide getPeptideByIndex(Comparable proteinId, Comparable index, boolean useCache) {
        Peptide peptide = super.getPeptideByIndex(proteinId, index, useCache);
        if (peptide == null || (peptide.getSpectrum() == null && hasSpectrum())) {
            logger.debug("Get new peptide from file: {}", index);
            Protein ident = getProteinById(proteinId);

            peptide = ident.getPeptides().get(Integer.parseInt(index.toString()));
            if (useCache && peptide != null) {
                getCache().store(CacheEntry.PEPTIDE, new Tuple<Comparable, Comparable>(proteinId, index), peptide);
                Spectrum spectrum = peptide.getSpectrum();
                if (hasSpectrum()) {
                    spectrum = getSpectrumById(peptide.getSpectrumIdentification().getId());
                    spectrum.setPeptide(peptide);
                    peptide.setSpectrum(spectrum);
                }
                if (spectrum != null) {
                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_CHARGE, spectrum.getId(), DataAccessUtilities.getPrecursorChargeParamGroup(spectrum));
                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_MZ, spectrum.getId(), DataAccessUtilities.getPrecursorMz(spectrum));
                }
            }
        }
        return peptide;
    }

    @Override
    public int getPeptideRank(Comparable proteinId, Comparable peptideId) {
        Peptide peptide = super.getPeptideByIndex(proteinId, peptideId, true);

        if (peptide == null) {
            logger.debug("Get new peptide from file: {}", peptideId);
            Protein ident = getProteinById(proteinId);
            peptide = ident.getPeptides().get(Integer.parseInt(peptideId.toString()));

            getCache().store(CacheEntry.PROTEIN, proteinId, ident);
            getCache().store(CacheEntry.PEPTIDE, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getSpectrumIdentification().getId()), peptide);
        }

        return peptide.getSpectrumIdentification().getRank();
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


    @Override
    public boolean hasProteinAmbiguityGroup() {
        return super.getProteinAmbiguityGroupIds().size() > 0;
    }

    @Override
    public ProteinGroup getProteinAmbiguityGroupById(Comparable proteinGroupId) {
        ProteinGroup proteinGroup = super.getProteinAmbiguityGroupById(proteinGroupId);

        if (proteinGroup == null) {

            try {
                ProteinAmbiguityGroup proteinAmbiguityGroup = unmarshaller.getProteinAmbiguityGroup(proteinGroupId);

                for (ProteinDetectionHypothesis proteinDetectionHypothesis : proteinAmbiguityGroup.getProteinDetectionHypothesis()) {
                    uk.ac.ebi.jmzidml.model.mzidml.DBSequence dbSequence = unmarshaller.getDBSequenceById(proteinDetectionHypothesis.getDBSequenceRef());
                    proteinDetectionHypothesis.setDBSequence(dbSequence);
                }

                proteinGroup = MzIdentMLTransformer.transformProteinAmbiguityGroupToProteinGroup(proteinAmbiguityGroup);

                if (proteinGroup != null) {
                    // store identification into cache
                    getCache().store(CacheEntry.PROTEIN_GROUP, proteinGroupId, proteinGroup);

                    for (Protein protein : proteinGroup.getProteinDetectionHypothesis()) {
                        cacheProtein(protein);
                    }
                }

            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve protein group: " + proteinGroupId, ex);
            }
        }

        return proteinGroup;
    }

    public Comparable getSpectrumIdBySpectrumIdentificationItemId(Comparable id) {
        String[] spectrumIdArray = ((Map<Comparable, String[]>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM)).get(id);

        /** To store in cache the Spectrum files, an Id was constructed using the spectrum ID and the
         *  id of the File.
         **/
        if (spectrumIdArray == null || spectrumIdArray.length <= 0) {
            return null;
        } else {
            return spectrumIdArray[0] + "!" + spectrumIdArray[1];
        }
    }

    /**
     * Get spectrum using a spectrumIdentification id, gives the option to choose whether to
     * use cache. This implementation provides a way of by passing the cache.
     *
     * @param id       spectrum Identification ID
     * @param useCache true means to use cache
     * @return Spectrum spectrum object
     */
    @Override
    public Spectrum getSpectrumById(Comparable id, boolean useCache) {

        String[] spectrumIdArray = ((String) id).split("!");
        if (spectrumIdArray.length != 2) {
            if(((Map<Comparable, String[]>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM)).containsKey(id)){
                spectrumIdArray = ((Map<Comparable, String[]>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM)).get(id);
            }else{
                spectrumIdArray = null;
            }
        }

        Spectrum spectrum = super.getSpectrumById(id, useCache);
        if (spectrum == null && spectrumIdArray != null) {
            logger.debug("Get new spectrum from file: {}", id);
            try {
                DataAccessController spectrumController = msDataAccessControllers.get(spectrumIdArray[1]);
                if (spectrumController != null && spectrumController.getSpectrumIds().contains(spectrumIdArray[0])) {
                    spectrum = spectrumController.getSpectrumById(spectrumIdArray[0]);
                    if (useCache && spectrum != null) {
                        getCache().store(CacheEntry.SPECTRUM, id, spectrum);
                    }
                }
            } catch (Exception ex) {
                String msg = "Error while getting spectrum: " + id;
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        }

        if (spectrum != null) {
            spectrum.setId(id);
        }
        return spectrum;
    }

    @Override
    public Collection<Comparable> getSpectrumIds() {
        Collection<Comparable> spectrumIds = super.getSpectrumIds();
        if (spectrumIds.size() == 0 && hasSpectrum()) {
            spectrumIds = new ArrayList<Comparable>();
            for (Comparable id : msDataAccessControllers.keySet()) {
                if (msDataAccessControllers.get(id) != null)
                    for (Comparable idSpectrum : msDataAccessControllers.get(id).getSpectrumIds()) {
                        spectrumIds.add(idSpectrum + "!" + id);
                    }
            }
        }
        return spectrumIds;
    }

    /**
     * Is identified Spectrum return true if the spectrum was identified
     *
     * @param specId The Spectrum Identification Item, it Can be an spectrum Identification Item or a Peptide ID
     * @return True if the spectrum is identified or false if is not identified
     */
    @Override
    public boolean isIdentifiedSpectrum(Comparable specId) {
        String[] spectrumIdArray = ((Map<Comparable, String[]>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM)).get(specId);

        if (spectrumIdArray != null && spectrumIdArray.length > 0) {
            return true;
        } else {
            Collection<String[]> ids = ((Map<Comparable, String[]>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM)).values();
            Set<String> idsSet = new TreeSet<String>();
            for (String[] values : ids) {
                idsSet.add(values[0] + "!" + values[1]);
            }
            if (idsSet.contains(specId)) return true;
        }
        return false;
    }

    /**
     * Add a List of MS Files to the mzidentml.
     * @param dataAccessControllerFiles A List of DataAccessControllers related with the MzIdentML
     */
    public void addMSController(List<File> dataAccessControllerFiles) {

        Map<SpectraData, File> spectraDataFileMap = checkMScontrollers(dataAccessControllerFiles);
        addMSController(spectraDataFileMap);
    }

    /**
     * Check if the ms File is supported and match with some of the par of the name in the Spectra Files
     * This method should be used in high-throughput, when you add different files.
     *
     * @param msIdentMLFiles List of  the MS files related with the MZIdentML
     * @return The relation between the SpectraData and the corresponding File.
     */
    public Map<SpectraData, File> checkMScontrollers(List<File> msIdentMLFiles) {

        Map<Comparable, SpectraData> spectraDataMap = getSpectraDataMap();

        Map<SpectraData, File> spectraFileMap = new HashMap<SpectraData, File>();

        for (File file : msIdentMLFiles) {
            Set<Map.Entry<Comparable, SpectraData>> entries = spectraDataMap.entrySet();
            Iterator iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                SpectraData spectraData = (SpectraData) mapEntry.getValue();
                if (spectraData.getLocation() != null && spectraData.getLocation().contains(file.getName())) {
                    spectraFileMap.put(spectraData, file);
                }else if(file.getName().contains(spectraData.getId().toString())
                        || (spectraData.getName() != null && file.getName().contains(spectraData.getName()))){
                    spectraFileMap.put(spectraData, file);
                }
            }
        }
        return spectraFileMap;
    }

    /**
     * Check if the File format is supported and the spectrum Id and add a Set of DataAccessControllers.
     * @param spectraDataFileMap A Map of SpectraData Files.
     */
    public void addMSController(Map<SpectraData, File> spectraDataFileMap) {

        Map<SpectraData, File> spectraDataControllerMap = getSpectraDataMSFiles();

        for (SpectraData spectraData : spectraDataControllerMap.keySet()) {
            for (SpectraData spectraDataFile : spectraDataFileMap.keySet()) {
                if (spectraDataControllerMap.get(spectraData) == null && spectraData.getId().equals(spectraDataFile.getId())) {
                    if (MzIdentMLUtils.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MZXML)
                        msDataAccessControllers.put(spectraData.getId(), new MzXmlControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                    if (MzIdentMLUtils.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MGF)
                        msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                    if (MzIdentMLUtils.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MZML)
                        msDataAccessControllers.put(spectraData.getId(), new MzMLControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                    if (MzIdentMLUtils.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.DTA)
                        msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                    //Todo: Need to check if changes
                }
            }

        }
    }


    public void clearMSControllers() {
        msDataAccessControllers.clear();
    }


    public boolean addNewMSController(Map<SpectraData, File> spectraDataFileMap, Map<Comparable, File> newFiles, Map<Comparable, String> fileTypes) {

        Map<SpectraData, File> spectraDataControllerMap = getSpectraDataMSFiles();

        boolean changeStatus = false;

        for (SpectraData spectraData : spectraDataControllerMap.keySet()) {
            File newFile = newFiles.get(spectraData.getId());
            String fileType = fileTypes.get(spectraData.getId());
            File oldSpectraDataFile = spectraDataFileMap.get(spectraData);
            if (oldSpectraDataFile != null && newFile == null) {
                DataAccessController peakList = msDataAccessControllers.remove(spectraData.getId());
                peakList.close();
                changeStatus = true;
            } else if (oldSpectraDataFile == null && newFile != null ||
                    (Constants.getSpecFileFormat(fileType) != Constants.SpecFileFormat.NONE && oldSpectraDataFile != null && newFile != null && !newFile.getAbsolutePath().equalsIgnoreCase(oldSpectraDataFile.getAbsolutePath()))) {
                DataAccessController peakList = createMSDataAccessController(newFile, fileType);
                msDataAccessControllers.put(spectraData.getId(), peakList);
                changeStatus = true;
            }
            if (changeStatus) {
                getCache().clear(CacheEntry.SPECTRUM);
            }
        }
        return changeStatus;
    }

    DataAccessController createMSDataAccessController(File file, String fileType) {
        Constants.SpecFileFormat fileFormatType = Constants.SpecFileFormat.valueOf(fileType);
        if (fileFormatType != null && file != null) {
            if (fileFormatType == Constants.SpecFileFormat.MZXML)
                return new MzXmlControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.MGF)
                return new PeakControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.MZML)
                return new MzMLControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.DTA)
                return new PeakControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.PKL)
                return new PeakControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.MZDATA)
                return new MzDataControllerImpl(file);
            //Todo: Need to check if changes
        }
        return null;
    }

    private Map<Comparable, SpectraData> getSpectraDataMap() {
        Map<Comparable, SpectraData> spectraDataMapResult = (Map<Comparable, SpectraData>) getCache().get(CacheEntry.SPECTRA_DATA);
        if (spectraDataMapResult == null) {
            return new HashMap<Comparable, SpectraData>();
        } else {
            return spectraDataMapResult;
        }
    }

    public Map<SpectraData, DataAccessController> getSpectraDataMSControllers() {
        Map<Comparable, SpectraData> spectraDataMap = getSpectraDataMap();

        Map<SpectraData, DataAccessController> mapResult = new HashMap<SpectraData, DataAccessController>();

        for (Comparable id : spectraDataMap.keySet()) {
            if (msDataAccessControllers.containsKey(id)) {
                mapResult.put(spectraDataMap.get(id), msDataAccessControllers.get(id));
            } else {
                mapResult.put(spectraDataMap.get(id), null);
            }
        }
        return mapResult;
    }

    /**
     * Get the Spectra Data Map with the corresponding File.
     * @return A Map of the SpectraData Objects with the corresponding MS File.
     */
    public Map<SpectraData, File> getSpectraDataMSFiles() {

        Map<SpectraData, DataAccessController> spectraDataControllerMAp = getSpectraDataMSControllers();

        Map<SpectraData, File> spectraDataFileMap = new HashMap<SpectraData, File>();

        for (SpectraData spectraData : spectraDataControllerMAp.keySet()) {
            DataAccessController controller = spectraDataControllerMAp.get(spectraData);
            spectraDataFileMap.put(spectraData, (controller == null) ? null : (File) controller.getSource());
        }
        return spectraDataFileMap;
    }

    public List<Comparable> getSupportedSpectraData() {
        Map<Comparable, SpectraData> spectraDataControllerMAp = getSpectraDataMap();
        List<Comparable> supported = new ArrayList<Comparable>();
        for (Comparable id : spectraDataControllerMAp.keySet()) {
            if (isSpectraDataSupported(spectraDataControllerMAp.get(id))) {
                supported.add(id);
            }
        }
        return supported;
    }


    private boolean isSpectraDataSupported(SpectraData spectraData) {
        //return (!(MzIdentMLUtils.getSpectraDataIdFormat(spectraData) == Constants.SpecIdFormat.NONE ||
        //        MzIdentMLUtils.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.NONE));
        return (!(MzIdentMLUtils.getSpectraDataIdFormat(spectraData) == Constants.SpecIdFormat.NONE));

    }


    /**
     * Get the number of Spectra by File associated with the mzidentml
     *
     * @param spectraData The SpectraData file with the Spectra
     * @return Number of Spectra Identified in the File.
     */
    public Integer getNumberOfSpectrabySpectraData(SpectraData spectraData) {
        Map<Comparable, List<Comparable>> spectraDataIdMap = (Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        if(spectraDataIdMap != null && spectraDataIdMap.containsKey(spectraData.getId())){
            return spectraDataIdMap.get(spectraData.getId()).size();
        }
        return 0;
    }


    /**
     * Return the number of Spectra in the DataAccessController
     * @return The number of Spectra for DataAccessController
     */
    @Override
    public int getNumberOfSpectra() {
        int numberOfSpectra = 0;
        if (!msDataAccessControllers.isEmpty()) {
            Iterator iterator = msDataAccessControllers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                if (mapEntry.getValue() != null) {
                    numberOfSpectra += ((DataAccessController) mapEntry.getValue()).getNumberOfSpectra();
                }
            }
        }
        return numberOfSpectra;
    }

    /**
     * Get the number of Spectra Identified in the DataAccessController
     * @return int the number of spectra identified in the DataAccessController
     */
    @Override
    public int getNumberOfIdentifiedSpectra() {
        Map<Comparable, List<Comparable>> spectraDataIdMap = (Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        int countSpectra = 0;
        if(spectraDataIdMap != null){
            Iterator iterator = spectraDataIdMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                if(mapEntry != null && mapEntry.getValue() != null)
                    countSpectra += ((List<Comparable>) mapEntry.getValue()).size();
            }                            }
        return countSpectra;
    }

    public List<DataAccessController> getSpectrumDataAccessControllers() {
        return new ArrayList<DataAccessController>(msDataAccessControllers.values());
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
}
