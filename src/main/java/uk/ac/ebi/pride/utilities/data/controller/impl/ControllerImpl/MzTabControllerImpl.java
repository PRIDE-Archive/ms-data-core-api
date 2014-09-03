package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.MzTabCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzTabTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.io.file.MzTabUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * MzTab DataAccessController for MzTab Files
 * @author ypriverol
 * @author rwang
 */

public class MzTabControllerImpl extends CachedDataAccessController {

    private static final Logger logger = LoggerFactory.getLogger(MzTabControllerImpl.class);

    /**
     * Reader to get information from MzTab file
     */
    private MzTabUnmarshallerAdaptor reader;

    /*
      * This is a set of controllers related with the MS information in the mzTab file
      * one or more controllers can be related with the same file formats. The Comparable
      * name of the file is an id of the file and the controller is the DataAccessController
       * related with the file.
     */
    private Map<Comparable, DataAccessController> msDataAccessControllers;
    private List<SpectrumIdentificationProtocol> spectrumIdentificationProtocol;
    private Protocol proteinDetectionProtocol;
    private List<SearchDataBase> searchDataBases;
    private List<DataProcessing> dataProcessings;
    private List<InstrumentConfiguration> instrumentConfigurations;

    public MzTabControllerImpl(File file) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        try {
            initialize();
        } catch (IOException e) {
            String msg = "Failed to create MzTab unmarshaller for mzTab file: " + file.getAbsolutePath();
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Initialize the mzTab file reader
     * @throws IOException
     */
    protected void initialize() throws IOException {
        // create MzTab access utils
        File file = (File) getSource();
        reader = new MzTabUnmarshallerAdaptor(file, new FileOutputStream(file.getAbsolutePath() + "errors.out"));
        // set data source description
        this.setName(file.getName());
        // set the type
        this.setType(Type.XML_FILE);
        // set the content categories
        this.setContentCategories(ContentCategory.PROTEIN,
                ContentCategory.PEPTIDE,
                ContentCategory.SAMPLE,
                ContentCategory.SOFTWARE,
                ContentCategory.PROTEIN_GROUPS,
                ContentCategory.SPECTRUM,
                ContentCategory.QUANTIFICATION);
        // set cache builder
        setCachingStrategy(new MzTabCachingStrategy());
        // populate cache
        populateCache();
    }

    /**
     * Get the mzTab reader
     *
     * @return MzTabUnmarshallerAdaptor  mzTab file reader
     */
    public MzTabUnmarshallerAdaptor getReader() {
        return reader;
    }

    /**
     * Get md5 hash unique id
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
                String msg = "Failed to generate unique id for mzML file";
                logger.error(msg, e);
            }
        }
        return uid;
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
            sourceFiles = MzTabTransformer.transformSourceFiles(reader.getSourceFiles());
        } catch (Exception ex) {
            String msg = "Error while getting source files";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
        }

        return sourceFiles;
    }

    /**
     * Return the List of Organizations
     * @return Organization List
     */
    public List<Organization> getOrganizationContacts() {
        logger.debug("Get organizational contact");
        List<Organization> organizationList = new ArrayList<Organization>();
        try {
            organizationList.addAll(MzTabTransformer.transformContactToOrganization(reader.getContacts()));
        } catch (Exception ex) {
            String msg = "Error while getting organizational contacts";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
        }
        return organizationList;
    }

    /**
     * Return a List of Persons
     * @return List of Persons
     */
    public List<Person> getPersonContacts() {
        logger.debug("Get person contacts");
        List<Person> personList = new ArrayList<Person>();
        try {
            personList.addAll(MzTabTransformer.transformContactToPersons(reader.getContacts()));
        } catch (Exception ex) {
            String msg = "Error while getting person contacts";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
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
            logger.debug("Get samples");
            List<Sample> samples;
            try {
                samples = MzTabTransformer.transformSamples(reader.getSamples());
                return samples;
            } catch (Exception ex) {
                String msg = "Error while getting samples";
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        } else {
            return metaData.getSamples();
        }
    }

    /**
     * Get a list of software
     *
     * @return List<Software>   a list of software objects.
     */
    public List<Software> getSoftwares() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            logger.debug("Get software");
            List<Software> softwares;
            try {
                softwares = MzTabTransformer.transformSoftwares(reader.getDataSoftwares());
                return softwares;
            } catch (Exception ex) {
                String msg = "Error while getting software list";
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
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
        logger.debug("Get references");
        List<Reference> refs = new ArrayList<Reference>();
        try {
            refs.addAll(MzTabTransformer.transformReferences(reader.getReferences()));
        } catch (Exception ex) {
            String msg = "Error while getting references";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
        }

        return refs;
    }

    /**
     * Get custom parameters
     *
     * @return ParamGroup   a group of cv parameters and user parameters.
     */
    @Override
    public ParamGroup getAdditional() {
        ExperimentMetaData metaData = super.getExperimentMetaData();
        if (metaData == null) {
            logger.debug("Get additional params");
            try {
                return MzTabTransformer.transformAdditional(reader.getAdditionalParams());
            } catch (Exception ex) {
                String msg = "Error while getting additional params";
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        } else {
            return metaData;
        }
    }

    /**
     * Get the protocol object
     *
     * @return Protocol protocol object.
     */
    public ExperimentProtocol getProtocol() {
        logger.debug("Get protocol");
        try {
            return MzTabTransformer.transformProtocol(reader.getProtocol());
        } catch (Exception ex) {
            String msg = "Error while getting protocol";
            logger.error(msg, ex);
            throw new DataAccessException(msg, ex);
        }
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
            logger.debug("Get metadata");
            try {
                // Get Accession for MzTab Object
                String accession = reader.getExpAccession();
                // Get the Version of the MzTab File.
                String version = reader.getVersion();
                //Get Source File List
                List<SourceFile> sources = getSourceFiles();
                // Get Samples objects for MzTab Object
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
                String title = reader.getExpTitle();
                // Get The Experiment Short Label
                String shortLabel = reader.getExpTitle();
                //Get Experiment Protocol
                //Todo: We need to check which information should be converted to Protocol
                ExperimentProtocol protocol = getProtocol();
                // Get References From the Experiment
                List<Reference> references = getReferences();

                metaData = new ExperimentMetaData(additional, accession, title, version, shortLabel, samples, softwares, persons, sources, null, organizations, references, null, null, protocol);
                // store it in the cache
                getCache().store(CacheEntry.EXPERIMENT_METADATA, metaData);
            } catch (Exception ex) {
                String msg = "Error while getting experiment meta data";
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        }

        return metaData;
    }

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

    @Override
    public MzGraphMetaData getMzGraphMetaData() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();
        if (metaData == null) {
            List<ScanSetting> scanSettings = null;
            List<DataProcessing> dataProcessings = null;
            List<InstrumentConfiguration> instrumentConfigurations = getInstrumentConfigurations();
            metaData = new MzGraphMetaData(null, null, scanSettings, instrumentConfigurations, dataProcessings);
        }
        return metaData;
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

    /**
     * Is identified Spectrum return true if the spectrum was identified
     *
     * @param specId Spectrum Id to define if is identified or not
     * @return True if the spectrum was identified in the MzTab File
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
        if (ident == null) {
            logger.debug("Get new identification from file: {}", proteinId);
            try {
                ident = MzTabTransformer.transformIdentification(reader.getIdentById(proteinId.toString()));
                if (useCache && ident != null) {
                    // store identification into cache
                    getCache().store(CacheEntry.PROTEIN, proteinId, ident);
                    // store precursor charge and m/z
                    for (Peptide peptide : ident.getPeptides()) {
                        Spectrum spectrum = peptide.getSpectrum();
                        if (spectrum != null) {
                            getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_CHARGE, spectrum.getId(), DataAccessUtilities.getPrecursorChargeParamGroup(spectrum));
                            getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_MZ, spectrum.getId(), DataAccessUtilities.getPrecursorMz(spectrum));
                        }
                    }
                }
            } catch (Exception ex) {
                String msg = "Error while getting identification: " + proteinId;
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        }
        return ident;
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
            num = reader.getNumberOfPeptides();
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve number of peptides", ex);
        }
        return num;
    }

    /**
     * Get the number of peptides by Rank, in MzTab all peptides are rank 1.
     *
     * @return int  the number of peptides.
     */
    @Override
    public int getNumberOfPeptidesByRank(int rank) {
        int num;
        try {
            // this method is overridden to use the reader directly
            num = reader.getNumberOfPeptides(rank);
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve number of peptides", ex);
        }
        return num;
    }


    @Override
    public void close() {
        reader = null;
        super.close();
    }

    /**
     * Check a file is MzTab file
     *
     * @param file input file
     * @return boolean true means MzTab
     */
    public static boolean isValidFormat(File file) {
        //Todo: To be implemented in mzTab reader
        return true;
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
     * Add a List of MS Files to the MzTab
     * @param dataAccessControllerFiles Add MS Files to a DataAccessController
     */
    public void addMSController(List<File> dataAccessControllerFiles) {
        Map<SpectraData, File> spectraDataFileMap = checkMScontrollers(dataAccessControllerFiles);
        addMSController(spectraDataFileMap);
    }

    /**
     * Check if the ms File is supported and match with some of the par of the name in the Spectra Files
     * This method should be used in high-throughput, when you add different files.
     *
     * @param files Check if the MS files can be added to the DataAccessController
     * @return Map of the SpectraData and the corresponding file.
     */
    public Map<SpectraData, File> checkMScontrollers(List<File> files) {

        Map<Comparable, SpectraData> spectraDataMap = getSpectraDataMap();

        Map<SpectraData, File> spectraFileMap = new HashMap<SpectraData, File>();

        for (File file : files) {
            Set<Map.Entry<Comparable, SpectraData>> entries = spectraDataMap.entrySet();
            Iterator iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                SpectraData spectraData = (SpectraData) mapEntry.getValue();
                if (spectraData.getLocation() != null && spectraData.getLocation().indexOf(file.getName()) >= 0) {
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
     * Add SpectraData Map
     * @param spectraDataFileMap SpectraData Map
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
     * Get the Spectra Data Files. the Map of the SpectraData and the Files associated with them.
     *
     * @return Map of the SpectraData and the Files associated with them
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
     * Get the number of Spectra by File associated with the MzTab
     *
     * @param spectraData SpectraData Object
     * @return Number of Spectra in a given File
     */
    public Integer getNumberOfSpectrabySpectraData(SpectraData spectraData) {
        Map<Comparable, List<Comparable>> spectraDataIdMap = (Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        if(spectraDataIdMap != null && spectraDataIdMap.containsKey(spectraData.getId())){
            return spectraDataIdMap.get(spectraData.getId()).size();
        }
        return 0;
    }

    /**
     * Get the number of spectra
     *
     * @return int the number of spectra
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
            }
        }
        return countSpectra;
    }

    public List<DataAccessController> getSpectrumDataAccessControllers() {
        return new ArrayList<DataAccessController>(msDataAccessControllers.values());
    }

    @Override
    public boolean hasProteinSequence() {
        return reader.hasProteinSequence();
    }

    public List<SpectrumIdentificationProtocol> getSpectrumIdentificationProtocol() {
        return spectrumIdentificationProtocol;
    }

    public Protocol getProteinDetectionProtocol() {
        return proteinDetectionProtocol;
    }

    public List<SearchDataBase> getSearchDataBases() {
        IdentificationMetaData metaData = super.getIdentificationMetaData();

        if (metaData == null) {
            logger.debug("Get instrument configurations");
            try {
                Map<Comparable, SearchDataBase> searchDataBaseMap = (Map<Comparable, SearchDataBase>) getCache().get(CacheEntry.SEARCH_DATABASE);
                List<SearchDataBase> databases;
                if(searchDataBaseMap == null){
                    databases = MzTabTransformer.transformDatabases(reader.getDatabases());
                }else{
                    databases = new ArrayList<SearchDataBase>(searchDataBaseMap.values());
                }
                return databases;
            } catch (Exception ex) {
                String msg = "Error while getting instrument configurations";
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        } else {
            return metaData.getSearchDataBases();
        }
    }

    public List<InstrumentConfiguration> getInstrumentConfigurations() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {
            logger.debug("Get instrument configurations");
            List<InstrumentConfiguration> configs = new ArrayList<InstrumentConfiguration>();
            try {
                configs.addAll(MzTabTransformer.transformInstrument(reader.getInstrument()));
                return configs;
            } catch (Exception ex) {
                String msg = "Error while getting instrument configurations";
                logger.error(msg, ex);
                throw new DataAccessException(msg, ex);
            }
        } else {
            return metaData.getInstrumentConfigurations();
        }
    }
}
