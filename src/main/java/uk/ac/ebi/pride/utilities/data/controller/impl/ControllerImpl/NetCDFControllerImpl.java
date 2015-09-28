package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.NetCDFCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.NetCDFTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.io.file.NetCDFUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;
import uk.ac.ebi.pride.utilities.netCDF.NetCDFFile;
import uk.ac.ebi.pride.utilities.netCDF.core.Metadata;
import uk.ac.ebi.pride.utilities.netCDF.core.MsScan;
import uk.ac.ebi.pride.utilities.netCDF.utils.netCDFParsingException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import java.util.Collection;

import java.util.List;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 28/09/15
 */
public class NetCDFControllerImpl extends CachedDataAccessController{

    private static final Logger logger = LoggerFactory.getLogger(NetCDFControllerImpl.class);
    /**
     * Reader for getting information from jmzReader file
     */
    private NetCDFUnmarshallerAdaptor unmarshaller;

    /**
     * Construct a data access controller using a given mzML file
     *
     * @param file jmzReader file
     * @throws uk.ac.ebi.pride.utilities.data.controller.DataAccessException
     *          data access exception
     */
    public NetCDFControllerImpl(File file) {
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize(false);
    }

    public NetCDFControllerImpl(File file, boolean useTitle){
        super(file, DataAccessMode.CACHE_AND_SOURCE);
        initialize(useTitle);
    }

    /**
     * Initialize the data access controller
     */
    private void initialize(boolean useTitle) {

        File file = (File) this.getSource();

        try {

            NetCDFFile netCDF = new NetCDFFile(file);
            unmarshaller = new NetCDFUnmarshallerAdaptor(netCDF, useTitle);

            // set the type
            this.setType(Type.PEAK_FILE);

            // set data source name
            this.setName(file.getName());


            // set the content categories
            this.setContentCategories(DataAccessController.ContentCategory.SPECTRUM,
                    DataAccessController.ContentCategory.CHROMATOGRAM,
                    DataAccessController.ContentCategory.SAMPLE,
                    DataAccessController.ContentCategory.INSTRUMENT,
                    DataAccessController.ContentCategory.SOFTWARE,
                    DataAccessController.ContentCategory.DATA_PROCESSING);
            // create cache builder
            setCachingStrategy(new NetCDFCachingStrategy());

            // populate cache
            populateCache();

        } catch (netCDFParsingException e) {
            String msg = "Error while trying to initialize the NetCDF file";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        } catch (IOException e) {
            String msg = "Error while trying to initialize the Peak file";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Get the backend data reader
     *
     * @return MzMLUnmarshallerAdaptor mzML reader
     */
    public NetCDFUnmarshallerAdaptor getUnmarshaller() {
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
        if (spectrum == null){
            MsScan rawSpec;
            try {
                rawSpec = unmarshaller.getSpectrumById(Integer.parseInt(id.toString()));
                spectrum = NetCDFTransformer.transformSpectrum(rawSpec);
                if (useCache) {
                    getCache().store(CacheEntry.SPECTRUM, id, spectrum);
                }
            } catch (netCDFParsingException|InvalidRangeException|IOException e) {
                String msg = "Error while trying to parse the NetCDF file";
                logger.error(msg, e);
                throw new DataAccessException(msg, e);
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

            Metadata rawMetadata = unmarshaller.getMetadata();

            metaData = NetCDFTransformer.transfromExperimentMetaData(rawMetadata);
        }
        return metaData;
    }

    @Override
    public MzGraphMetaData getMzGraphMetaData() {

        MzGraphMetaData metaData = super.getMzGraphMetaData();

        if (metaData == null) {

            Metadata rawMetadata = unmarshaller.getMetadata();

            metaData = NetCDFTransformer.transfromMzGraphMetadata(rawMetadata);
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

        String filename = file.getName().toLowerCase();
        if (filename.endsWith(Constants.NETCDF_EXT))
            return true;

        return false;

    }

    @Override
    public Collection<Comparable> getSpectrumIds () {
        Collection<Comparable> ids = super.getSpectrumIds();
        if(ids == null && ids.size() == 0){
            ids = unmarshaller.getSpectrumIds();
            getCache().storeInBatch(CacheEntry.SPECTRUM_ID, ids);
        }
        return ids;
    }
}