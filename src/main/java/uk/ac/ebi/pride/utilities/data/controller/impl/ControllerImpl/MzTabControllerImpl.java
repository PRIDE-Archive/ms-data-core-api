package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.MzTabCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzTabTransformer;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.io.file.MzTabUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MD5Utils;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * MzTab DataAccessController for MzTab Files. The mzTabController is supporting only those files with Protein identification and
 * PSMs and not Peptide Information is store because the link between both objects is missed. MzTab will be supported only based in
 * the PSMs and Core proteins the ambiguity proteins will not be supported.
 *
 * @author ypriverol
 * @author rwang
 */

public class MzTabControllerImpl extends ReferencedIdentificationController{

    private static final Logger logger = LoggerFactory.getLogger(MzTabControllerImpl.class);

    private static Pattern mzTabVersion = Pattern.compile(".*(mzTab-version).*(1.0)");

    private static Pattern mzTabProteinSection    = Pattern.compile(".*(protein_search_engine_score).*");

    private static Pattern mzTabPSMSection    = Pattern.compile(".*(psm_search_engine_score).*");


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
        this.setType(Type.MZTAB);

        // init ms data accession controller map
        this.msDataAccessControllers = new HashMap<Comparable, DataAccessController>();

        // set the content categories
        this.setContentCategories(ContentCategory.PROTEIN,
                ContentCategory.PEPTIDE,
                ContentCategory.SAMPLE,
                ContentCategory.SOFTWARE,
                ContentCategory.PROTEIN_GROUPS,
                ContentCategory.SPECTRUM,
                ContentCategory.QUANTIFICATION);
        //Todo: first cases only support identification
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
     * Get the List of File Spectra that the Mzidentml use to identified peptides
     *
     * @return List of SpectraData Files associated with mzidentml.
     */
    public List<SpectraData> getSpectraDataFiles() {
        ExperimentMetaData metaData = super.getExperimentMetaData();
        if (metaData == null) {
            return new ArrayList<SpectraData>(MzTabTransformer.transformMsRunMap(reader.getSourceFiles()).values());
        }
        return metaData.getSpectraDatas();
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
            List<SpectrumIdentificationProtocol> spectrumIdentificationProtocolList = null;
            Protocol proteinDetectionProtocol = null;
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
              // when protein groups are not present
              Tuple<Integer, uk.ac.ebi.pride.jmztab.model.Protein> rawProtein = reader.getProteinById(proteinId);
              Map<Integer, uk.ac.ebi.pride.jmztab.model.PSM> spectrumIdentificationItems = getScannedSpectrumIdentificationItems(proteinId);
              uk.ac.ebi.pride.jmztab.model.Metadata metadata = reader.getMetadata();
              ident = MzTabTransformer.transformIdentification(rawProtein.getValue(), rawProtein.getKey(), spectrumIdentificationItems, metadata);
                if (ident != null) {
                    cacheProtein(ident);
                }
            } catch (Exception ex) {
                throw new DataAccessException("Failed to retrieve protein identification: " + proteinId, ex);
            }
        }

        return ident;
    }

    private Map<Integer, uk.ac.ebi.pride.jmztab.model.PSM> getScannedSpectrumIdentificationItems(Comparable proteinId) {
        List<Comparable> spectrumIdentIds = null;

        if (getCache().hasCacheEntry(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES)) {
            spectrumIdentIds = ((Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES)).get(proteinId);
        }

        return reader.getSpectrumIdentificationsByIds(spectrumIdentIds);
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

    /**
     * Check a file is mzTab File is supported, it should contain the protein and psm sections, it must be mztab version 1.0 and finally it should be
     * mzTab extension file.
     *
     * @param file input file
     * @return boolean true means the file is an mztab
     */
    public static boolean isValidFormat(File file) {
        boolean valid = false;
        BufferedReader reader = null;
        int count = 0;

        /**
         * To validate  the mzTab if is supported or not we will read the header line by line until the type appear
         * It should contains Proteins and PSMs to be a supported file.
         */
        try {
            reader = new BufferedReader(new FileReader(file));
            // read the first 70 lines
            for (int i = 0; i < 200; i++) {
                String line = reader.readLine();
                if(mzTabVersion.matcher(line).find() || mzTabProteinSection.matcher(line).find() || mzTabPSMSection.matcher(line).find())
                    count++;
            }
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
        String filename = file.getName().toLowerCase();
        if (filename.endsWith(Constants.MZTAB_EXT) && count == 3)
            return true;

        return valid;
    }

    /**
     * The mzTab in the present version will not contains protein Groups because ambiguity members are not Protein Groups
     * In the future we are thinking to support ambiguity groups in a different way
     *
     * @param proteinGroupId
     * @return
     */
    @Override
    public ProteinGroup getProteinAmbiguityGroupById(Comparable proteinGroupId) {

        ProteinGroup proteinGroup = super.getProteinAmbiguityGroupById(proteinGroupId);

        if (proteinGroup == null) {

            try {
 //               proteinGroup = MzTabTransformer.transformProteinAmbiguityGroupToProteinGroup(proteinGroupId);

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

    /**
     * Check if the file contains Protein Sequence by looking inside the CVTerms in the MzTab Unmarshaller
     * @return TRUE if the file contains the SEQUENCE for each PROTEIN and FALSE if proteins are annotated without the sequence
     */
    @Override
    public boolean hasProteinSequence() {
        return reader.hasProteinSequence();
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
            num = reader.getNumIdentifiedPeptides();
        } catch (Exception ex) {
            throw new DataAccessException("Failed to retrieve number of peptides", ex);
        }
        return num;
    }
}
