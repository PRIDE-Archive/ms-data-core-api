package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.jmzml.model.mzml.*;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.MzMlCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzMLTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Chromatogram;
import uk.ac.ebi.pride.utilities.data.core.DataProcessing;
import uk.ac.ebi.pride.utilities.data.core.InstrumentConfiguration;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.ReferenceableParamGroup;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;
import uk.ac.ebi.pride.utilities.data.io.file.MzMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MzMlControllerImpl provides methods to access mzML files.
 * <p/>
 * @author Rui Wang
 * @author  Yasset Perez-Riverol
 */
public class MzMLControllerImpl extends CachedDataAccessController {

    private static final Logger logger = LoggerFactory.getLogger(MzMLControllerImpl.class);
    /**
     * Pattern for validating mzML format
     */
    private static final Pattern mzMLHeaderPattern = Pattern.compile("^[^<]*(<\\?xml [^>]*>\\s*(<!--[^>]*-->\\s*)*)?<(mzML)|(indexedmzML) xmlns=.*", Pattern.MULTILINE);

    /**
     * Reader for getting information from mzML file
     */
    private MzMLUnmarshallerAdaptor unmarshaller = null;

    /**
     * Construct a data access controller using a given mzML file
     *
     * @param file mzML file
     */
    public MzMLControllerImpl(File file) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize();
    }

    private void initialize() {
        File file = (File) this.getSource();
        // create unmarshaller
        unmarshaller = new MzMLUnmarshallerAdaptor(file);

        // set data source name
        this.setName(file.getName());
        // set the type
        this.setType(DataAccessController.Type.XML_FILE);
        // set the content categories
        this.setContentCategories(DataAccessController.ContentCategory.SPECTRUM,
                DataAccessController.ContentCategory.CHROMATOGRAM,
                DataAccessController.ContentCategory.SAMPLE,
                DataAccessController.ContentCategory.INSTRUMENT,
                DataAccessController.ContentCategory.SOFTWARE,
                DataAccessController.ContentCategory.DATA_PROCESSING);
        // create cache builder
        setCachingStrategy(new MzMlCachingStrategy());
        // populate cache
        populateCache();
    }

    /**
     * Get the backend data reader
     *
     * @return MzMLUnmarshallerAdaptor mzML reader
     */
    public MzMLUnmarshallerAdaptor getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Get the unique id for this data access controller
     * It generates a MD5 hash using the absolute path of the file
     * This will guarantee the same id if the file path is the same
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
     * Get a list of cvlookups, these are not cached
     *
     * @return List<CvLookup>  a list of cvlookups
     */
    public List<CVLookup> getCvLookups() {
        try {
            CVList rawCvList = unmarshaller.getCVList();
            return MzMLTransformer.transformCVList(rawCvList);
        } catch (Exception e) {
            String msg = "Exception while trying to read a list of cv lookups";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get Referenceable paramgroup, this concept is only available in mzML
     * It is a paramgroup with id and this is not cached
     *
     * @return ReferenceableParamGroup param group
     */
    public ReferenceableParamGroup getReferenceableParamGroup() {

        try {
            ReferenceableParamGroupList rawRefParamGroup = unmarshaller.getReferenceableParamGroupList();
            return MzMLTransformer.transformReferenceableParamGroupList(rawRefParamGroup);
        } catch (Exception e) {
            String msg = "Exception while trying to read referenceable param group";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get a list of samples by checking the cache first
     *
     * @return List<Sample>    a list of samples
     */
    @Override
    public List<Sample> getSamples() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                SampleList rawSample = unmarshaller.getSampleList();
                return MzMLTransformer.transformSampleList(rawSample);
            } catch (Exception e) {
                String msg = "Exception while trying to read samples";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getSamples();
        }
    }

    /**
     * Get a list of person contacts
     *
     * @return List<Person>    list of persons
     */
    public List<Person> getPersonContacts() {
        try {
            FileDescription rawFileDesc = unmarshaller.getFileDescription();
            // List of Persons
            return MzMLTransformer.transformFileDescriptionToPerson(rawFileDesc);
        } catch (Exception e) {
            String msg = "Error while getting a list of person contacts";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get a list of Organization contacts
     *
     * @return List<Organization>    list of persons
     */
    public List<Organization> getOrganizationContacts() {
        try {
            FileDescription rawFileDesc = unmarshaller.getFileDescription();
            // List of Organization
            return MzMLTransformer.transformFileDescriptionOrganization(rawFileDesc);
        } catch (Exception e) {
            String msg = "Error while getting a list of organizational contacts";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get a List of SourceFiles
     *
     * @return List<SourceFile> List of SourceFile
     */
    public List<SourceFile> getSourceFiles() {
        try {
            FileDescription rawFileDesc = unmarshaller.getFileDescription();
            // List of Persons
            return MzMLTransformer.transformFileDescriptionToFileSource(rawFileDesc);
        } catch (Exception e) {
            String msg = "Error while getting a list of source files";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * This summarizes the different types of spectra that can be expected
     * in the file. This is expected to aid processing software in skipping
     * files that do not contain appropriate spectrum types for it. It should
     * also describe the nativeID format used in the file by referring to an
     * appropriate CV term.
     *
     * @return ParamGroup A list of CvTerms Related with the File Content
     */
    public ParamGroup getFileContent() {
        try {
            FileDescription rawFileDesc = unmarshaller.getFileDescription();
            return MzMLTransformer.transformFileDescriptionToFileContent(rawFileDesc);
        } catch (Exception e) {
            String msg = "Error while getting a list of file content";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get a List of Softwares
     *
     * @return List<Software> List of Softwares
     */
    public List<Software> getSoftwares() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                SoftwareList rawSoftware = unmarshaller.getSoftwares();
                return MzMLTransformer.transformSoftwareList(rawSoftware);
            } catch (Exception e) {
                String msg = "Error while getting a list of software";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getSoftwares();
        }
    }

    /**
     * Get a list of scan settings by checking the cache first
     *
     * @return List<ScanSetting>   a list of scan settings
     */
    public List<ScanSetting> getScanSettings() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {
            try {
                ScanSettingsList rawScanSettingList = unmarshaller.getScanSettingsList();
                return MzMLTransformer.transformScanSettingList(rawScanSettingList);
            } catch (Exception e) {
                String msg = "Error while getting a list of scan settings";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getScanSettings();
        }
    }

    /**
     * Get a list of instrument configurations by checking the cache first
     *
     * @return List<Instrumentconfiguration>   a list of instrument configurations
     */
    public List<InstrumentConfiguration> getInstrumentConfigurations() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {
            try {
                InstrumentConfigurationList rawInstrumentList = unmarshaller.getInstrumentConfigurationList();
                return MzMLTransformer.transformInstrumentConfigurationList(rawInstrumentList);
            } catch (Exception e) {
                String msg = "Error while getting a list of instrument configurations";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getInstrumentConfigurations();
        }
    }

    /**
     * Get a list of data processings by checking the cache first
     *
     * @return List<DataProcessing>    a list of data processings
     */
    public List<DataProcessing> getDataProcessings() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {
            try {
                uk.ac.ebi.jmzml.model.mzml.DataProcessingList rawDataProcList = unmarshaller.getDataProcessingList();
                return MzMLTransformer.transformDataProcessingList(rawDataProcList);
            } catch (Exception e) {
                String msg = "Error while getting a list of data processings";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getDataProcessings();
        }

    }

    /**
     * In case of mzMl the additional parameters are related with the getFileContent
     * This are a set of CVTerms related with the Content in the MzML File. Also we add the
     * the Date of the Run as a CvParam, it is very important to know when this information was
     * generated.
     *
     * @return ParamGroup  param group
     */
    @Override
    public ParamGroup getAdditional() {
        ParamGroup paramGroup = null;
        ParamGroup fileContent = getFileContent();
        if (fileContent != null) {
            paramGroup = fileContent;
        }
        Date dateCreation = unmarshaller.getCreationDate();
        if (dateCreation != null && paramGroup != null) {
            paramGroup.addCvParam(MzMLTransformer.transformDateToCvParam(dateCreation));

        }
        return paramGroup;
    }

    /**
     * Get spectrum using a spectrum id, gives the option to choose whether to use cache.
     * This implementation provides a way of by passing the cache.
     *
     * @param id       spectrum id
     * @param useCache true means to use cache
     * @return Spectrum spectrum object
     */
    @Override
    Spectrum getSpectrumById(Comparable id, boolean useCache) {
        Spectrum spectrum = super.getSpectrumById(id, useCache);
        if (spectrum == null) {
            try {
//                System.out.println(id);
                uk.ac.ebi.jmzml.model.mzml.Spectrum
                        rawSpec = unmarshaller.getSpectrumById(id.toString());

                spectrum = MzMLTransformer.transformSpectrum(rawSpec);
                if (useCache) {
                    getCache().store(CacheEntry.SPECTRUM, id, spectrum);
                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_CHARGE,id, DataAccessUtilities.getPrecursorCharge(spectrum.getPrecursors()));
                }
            } catch (MzMLUnmarshallerException ex) {
                logger.error("Get spectrum by id", ex);
                throw new DataAccessException("Exception while trying to read Spectrum using Spectrum ID", ex);
            }
        }
        return spectrum;
    }

    /**
     * Get chromatogram using a chromatogram id, gives the option to choose whether to use cache.
     * This implementation provides a way of by passing the cache.
     *
     * @param id       chromatogram id
     * @param useCache true means to use cache
     * @return Chromatogram chromatogram object
     */
    @Override
    public Chromatogram getChromatogramById(Comparable id, boolean useCache) {
        Chromatogram chroma = super.getChromatogramById(id, useCache);
        if (chroma == null) {
            try {
                uk.ac.ebi.jmzml.model.mzml.Chromatogram
                        rawChroma = unmarshaller.getChromatogramById(id.toString());
                chroma = MzMLTransformer.transformChromatogram(rawChroma);
                if (useCache) {
                    getCache().store(CacheEntry.CHROMATOGRAM, id, chroma);
                }
            } catch (MzMLUnmarshallerException ex) {
                logger.error("Get chromatogram by id", ex);
                throw new DataAccessException("Exception while trying to read Chromatogram using chromatogram ID", ex);
            }
        }
        return chroma;
    }

    /**
     * Close data access controller by resetting the data reader first
     */
    @Override
    public void close() {
        unmarshaller = null;
        super.close();
    }

    @Override
    public ExperimentMetaData getExperimentMetaData() {

        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            // id , accession and version
            String id = unmarshaller.getMzMLId();
            String accession = unmarshaller.getMzMLAccession();
            String version = unmarshaller.getMzMLVersion();

            // SourceFile List
            List<SourceFile> sourceFileList = getSourceFiles();

            // List of Persons
            List<Person> personList = getPersonContacts();

            // List of Organizations
            List<Organization> organizationList = getOrganizationContacts();

            // Sample list
            List<Sample> samples = getSamples();

            // Software list
            List<Software> softwares = getSoftwares();

            // ScanSettings list
            ParamGroup adittional = getAdditional();

            metaData = new ExperimentMetaData(adittional, id, accession, version, null, samples, softwares, personList, sourceFileList, null, organizationList, null, null, null, null);
        }
        return metaData;
    }

    /**
     * Get the Spectrum Metadata, the ScanSettings, The DataProcessing, and Instrument
     * Configurations.
     *
     * @return MzGraphMetaData is the Metadata related with Spectrum Information
     */
    @Override
    public MzGraphMetaData getMzGraphMetaData() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();
        if (metaData == null) {
            List<ScanSetting> scanSettings = getScanSettings();
            List<DataProcessing> dataProcessings = getDataProcessings();
            List<InstrumentConfiguration> instrumentConfigurations = getInstrumentConfigurations();
            metaData = new MzGraphMetaData(null, null, scanSettings, instrumentConfigurations, dataProcessings);
        }
        return metaData;
    }

    /**
     * The mzML do not contains Identification Metadata, just Spectrum Information
     * @return The MzML do not support Identification Metadata
     */
    @Override
    public IdentificationMetaData getIdentificationMetaData() {
        throw new UnsupportedOperationException("This method is not supported");
    }

    /**
     * This method provide the way to know if the controller contain
     * Identification Data and IdentificationMetadata.
     * @return True if the DataAccessController contains Proteins
     */
    @Override
    public boolean hasProtein() {
        return false;
    }

    /**
     * Check a file is mzML file
     *
     * @param file input file
     * @return boolean true means mzML
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

            Matcher matcher = mzMLHeaderPattern.matcher(content);

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


}
