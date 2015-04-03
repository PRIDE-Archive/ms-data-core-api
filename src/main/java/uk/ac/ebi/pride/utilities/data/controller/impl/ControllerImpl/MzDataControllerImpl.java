package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.MzDataCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzDataTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.io.file.MzDataUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Admin;
import uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.CvLookup;
import uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.InstrumentDescription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This controller is used to retrieve the information from mzData files. It uses the jmzReader
 * to retrieve the information from mzData files. The mzData files support Spectrum and Chromatogram
 * Information and also other important Metadata.
 *
 * @author Yasset Perez-Riverol
 * Date: 3/15/12
 * Time: 8:17 AM
 *
 */
public class MzDataControllerImpl extends CachedDataAccessController {

    private static final Logger logger = LoggerFactory.getLogger(MzDataControllerImpl.class);
    /**
     * Pattern for validating mzData format
     */
    private static final Pattern mzDataHeaderPattern = Pattern.compile("^[^<]*(<\\?xml [^>]*>\\s*(<!--[^>]*-->\\s*)*)?<(mzData) version=.*", Pattern.MULTILINE);

    /**
     * Reader for getting information from mzData file
     */
    private MzDataUnmarshallerAdaptor unmarshaller;

    public MzDataControllerImpl(File file) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize();
    }

    private void initialize() {
        File file = (File) this.getSource();
        // create unmarshaller
        MzDataFile um;
        try {
            um = new MzDataFile(file);
            unmarshaller = new MzDataUnmarshallerAdaptor(um);

            // set data source name
            this.setName(file.getName());
            // set the type
            this.setType(DataAccessController.Type.XML_FILE);
            // set the content categories
            this.setContentCategories(DataAccessController.ContentCategory.SPECTRUM,
                DataAccessController.ContentCategory.SAMPLE,
                DataAccessController.ContentCategory.INSTRUMENT,
                DataAccessController.ContentCategory.SOFTWARE,
                DataAccessController.ContentCategory.DATA_PROCESSING);
            // create cache builder
            setCachingStrategy(new MzDataCachingStrategy());
            // populate cache
            populateCache();
        } catch (JMzReaderException e) {
            String msg = "Exception while create the MzData File";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);

        }


    }

    /**
     * Get the backend data reader
     *
     * @return MzMLUnmarshallerAdaptor mzML reader
     */
    public MzDataUnmarshallerAdaptor getUnmarshaller() {
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
     * @ data access exception
     */
    public List<CVLookup> getCvLookups() {
        try {
            List<CvLookup> rawCvList = unmarshaller.getCvLookups();
            return MzDataTransformer.transformCVList(rawCvList);
        } catch (JMzReaderException e) {
            String msg = "Exception while trying to read a list of cv lookups";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get a list of samples by checking the cache first
     *
     * @return List<Sample>    a list of samples
     * @ data access exception
     */
    @Override
    public List<Sample> getSamples() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                Admin rawSample = unmarshaller.getAdmin();
                return MzDataTransformer.transformSampleList(rawSample);
            } catch (JMzReaderException e) {
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
     * @ data access exception
     */
    public List<Person> getPersonContacts() {
        try {
            List<uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Person> rawFileDesc = unmarshaller.getPersonContacts();
            // List of Persons
            return MzDataTransformer.transformToPerson(rawFileDesc);
        } catch (JMzReaderException e) {
            String msg = "Error while getting a list of person contacts";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get the information of SourceFiles.
     *
     * @return List of Source Files
     * @
     */
    public List<SourceFile> getSourceFiles() {
        try {
            uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.SourceFile rawFileDesc = unmarshaller.getSourceFiles();
            // List of Persons
            return MzDataTransformer.transformToFileSource(rawFileDesc);
        } catch (JMzReaderException e) {
            String msg = "Error while getting a list of source files";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * File Content is extra parameters related with Source Files in case of mzData.
     *
     * @return ParamGroup
     * @
     */
    public ParamGroup getFileContent() {
        ParamGroup paramGroup = null;
        List<SourceFile> sourceFiles = getSourceFiles();
        Set<CvParam> cvParamSet = null;

        if(sourceFiles != null){
            cvParamSet = new HashSet<CvParam>();
            for(SourceFile sourceFile: sourceFiles){
                if(sourceFile.getFileFormat() != null){
                    cvParamSet.add(sourceFile.getFileFormat());
                }
            }
        }
        if (cvParamSet != null){
            List<CvParam> cvParams = new ArrayList<CvParam>();
            cvParams.addAll(cvParamSet);
            paramGroup = new ParamGroup(cvParams,null);
        }
        return paramGroup;
    }

    /**
     * Get a List of Software
     * @return List<Software> List of Software
     * @
     */
    public List<Software> getSoftwares() {
        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            try {
                uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Software rawSoftware = unmarshaller.getSoftware();
                return MzDataTransformer.transformSoftware(rawSoftware);
            } catch (JMzReaderException e) {
                String msg = "Error while getting a list of software";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getSoftwares();
        }
    }

    /**
     * Get a list of instrument configurations by checking the cache first
     *
     * @return List<Instrumentconfiguration>   a list of instrument configurations
     * @ data access exception
     */
    public List<InstrumentConfiguration> getInstrumentConfigurations() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {
            try {
                InstrumentDescription rawInstrumentList = unmarshaller.getInstrument();
                return MzDataTransformer.transformInstrumentConfiguration(rawInstrumentList);
            } catch (JMzReaderException e) {
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
     * @ data access exception
     */
    public List<DataProcessing> getDataProcessings() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {
            try {
                uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.DataProcessing rawDataProcList = unmarshaller.getDataProcessing();
                return MzDataTransformer.transformDataProcessing(rawDataProcList);
            } catch (JMzReaderException e) {
                String msg = "Error while getting a list of data processings";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        } else {
            return metaData.getDataProcessings();
        }

    }

    /**
     * Get spectrum using a spectrum id, gives the option to choose whether to use cache.
     * This implementation provides a way of by passing the cache.
     *
     * @param id       spectrum id
     * @param useCache true means to use cache
     * @return Spectrum spectrum object
     * @ data access exception
     */
    @Override
    Spectrum getSpectrumById(Comparable id, boolean useCache) {
        Spectrum spectrum = super.getSpectrumById(id, useCache);
        if (spectrum == null) {
            try {
                uk.ac.ebi.pride.tools.jmzreader.model.Spectrum rawSpec = unmarshaller.getSpectrumById(id.toString());
                spectrum = MzDataTransformer.transformSpectrum(rawSpec);
                if (useCache) {
                    getCache().store(CacheEntry.SPECTRUM, id, spectrum);
                }
            } catch (JMzReaderException ex) {
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
     * @ data access exception
     */
    @Override
    public Chromatogram getChromatogramById(Comparable id, boolean useCache) {
        throw new UnsupportedOperationException("This method is not supported");
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
    public ExperimentMetaData getExperimentMetaData(){

        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            // id , accession and version
            String id = unmarshaller.getIdMzData();

            String accession = null;
            // File Version
            String version = unmarshaller.getVersionMzData();

            // SourceFile List
            List<SourceFile> sourceFileList = getSourceFiles();

            // List of Persons
            List<Person> personList = getPersonContacts();

            // List of Organizations
            List<Organization> organizationList = null;

            // Sample list
            List<Sample> samples = getSamples();

            // Software list
            List<Software> softwares = getSoftwares();

            ParamGroup fileContent = getFileContent();

            metaData = new ExperimentMetaData(fileContent, id, accession, version, null, samples, softwares, personList, sourceFileList, null, organizationList, null, null, null, null);
        }
        return metaData;
    }

    @Override
    public MzGraphMetaData getMzGraphMetaData() {
        MzGraphMetaData metaData = super.getMzGraphMetaData();
        if (metaData == null) {
            List<ScanSetting> scanSettings = null;
            List<DataProcessing> dataProcessings = getDataProcessings();
            List<InstrumentConfiguration> instrumentConfigurations = getInstrumentConfigurations();
            metaData = new MzGraphMetaData(null, null, scanSettings, instrumentConfigurations, dataProcessings);
        }
        return metaData;
    }

    @Override
    public IdentificationMetaData getIdentificationMetaData() {
        return null;
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

            Matcher matcher = mzDataHeaderPattern.matcher(content);

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
