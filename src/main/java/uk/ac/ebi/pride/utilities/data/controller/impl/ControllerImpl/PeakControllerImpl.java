package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.PeakCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.PeakTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.io.file.PeakUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;
import uk.ac.ebi.pride.tools.apl_parser.AplFile;
import uk.ac.ebi.pride.tools.dta_parser.DtaFile;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.ms2_parser.Ms2File;
import uk.ac.ebi.pride.tools.pkl_parser.PklFile;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Peak Controller keep the information from different Peak files. It support mgf, ms2, pkl,
 * and DTa files. This files only contain the name of the file, the
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */

public class PeakControllerImpl extends CachedDataAccessController {

    private static final Logger logger = LoggerFactory.getLogger(PeakControllerImpl.class);
    /**
     * Reader for getting information from jmzReader file
     */
    private PeakUnmarshallerAdaptor unmarshaller;

    /**
     * Construct a data access controller using a given mzML file
     *
     * @param file jmzReader file
     * @throws uk.ac.ebi.pride.utilities.data.controller.DataAccessException
     *          data access exception
     */
    public PeakControllerImpl(File file) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize(false);
    }

    public PeakControllerImpl(File file, boolean useTitle){
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize(useTitle);
    }

    /**
     * Initialize the data access controller
     */
    private void initialize(boolean useTitle) {

        File file = (File) this.getSource();
        JMzReader um = null;
        if (isValidFormat(file) != null) {
            try {
                if (isValidFormat(file) == MgfFile.class) {
                    um = new MgfFile(file, true);
                }
                if (isValidFormat(file) == DtaFile.class) {
                    um = new DtaFile(file);
                }
                if (isValidFormat(file) == PklFile.class) {
                    um = new PklFile(file);
                }
                if (isValidFormat(file) == Ms2File.class) {
                    um = new Ms2File(file);
                }if(isValidFormat(file) == AplFile.class){
                    um = new AplFile(file);
                }
            } catch (JMzReaderException e) {
                String msg = "Error while trying to initialize the Peak file";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);

            }
        }
        unmarshaller = new PeakUnmarshallerAdaptor(um, useTitle);

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
        setCachingStrategy(new PeakCachingStrategy());

        // populate cache
        populateCache();
    }

    /**
     * Get the backend data reader
     *
     * @return MzMLUnmarshallerAdaptor mzML reader
     */
    public PeakUnmarshallerAdaptor getUnmarshaller() {
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
     * Get spectrum using a spectrum id, gives the option to choose whether to use cache.
     * This implementation provides a way of by passing the cache.
     *
     * @param id       spectrum id
     * @param useCache true means to use cache
     * @return Spectrum spectrum object
     */
    @Override
    public Spectrum getSpectrumById(Comparable id, boolean useCache) {

        Spectrum spectrum = super.getSpectrumById(id, useCache);
        Map<Comparable, Comparable> ids = (Map<Comparable, Comparable>) getCache().get(CacheEntry.TITLE_MGF_INDEX);
        if (spectrum == null){
            if((ids != null && !ids.isEmpty())){
                id = ids.get(id);
            }
            uk.ac.ebi.pride.tools.jmzreader.model.Spectrum rawSpec;
            try {
                rawSpec = unmarshaller.getSpectrumById(id.toString());
                spectrum = PeakTransformer.transformSpectrum(rawSpec);
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
    public ExperimentMetaData getExperimentMetaData() {

        ExperimentMetaData metaData = super.getExperimentMetaData();

        if (metaData == null) {
            // id , accession and version
            String id = null;

            String accession = null;

            String version = null;
            // SourceFile List
            List<SourceFile> sourceFileList = null;
            // List of Persons
            List<Person> personList = null;
            // List of Organizations
            List<Organization> organizationList = null;
            // Sample list
            List<Sample> samples = null;
            // Software list
            List<Software> softwares = null;
            // ScanSettings list
            ParamGroup fileContent = null;

            metaData = new ExperimentMetaData(fileContent, id, accession, version, null, samples, softwares, personList, sourceFileList, null, organizationList, null, null, null, null);
        }
        return metaData;
    }

    @Override
    public MzGraphMetaData getMzGraphMetaData() {
        return null;
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
    public static Class isValidFormat(File file) {

        String filename = file.getName().toLowerCase();
        if (filename.endsWith(Constants.DTA_EXT)) {
            return DtaFile.class;
        } else if (filename.endsWith(Constants.MGF_EXT)) {
            return MgfFile.class;
        } else if (filename.endsWith(Constants.MS2_EXT)) {
            return Ms2File.class;
        } else if (filename.endsWith(Constants.PKL_EXT)) {
            return PklFile.class;
        } else if (filename.endsWith(Constants.APL_EXT)){
            return AplFile.class;
        }
        return null;
    }

    /**
     * The only file format that not contain any MetaData are the pure peak files.
     *
     * @return Return allways false because the MetaData Information is not supported for Peak Files
     */
    @Override
    public boolean hasMetaDataInformation() {
        return false;
    }

    @Override
    public Collection<Comparable> getSpectrumIds() {
        Map<Comparable, Comparable> ids = (Map<Comparable, Comparable>) getCache().get(CacheEntry.TITLE_MGF_INDEX);

        if(ids != null && ids.size() > 0)
            return ids.keySet();

        return super.getSpectrumIds();
    }
}
