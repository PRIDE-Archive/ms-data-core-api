package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.FastMzIdentMLCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.SimpleToJmzIdentMLTransformer;
import uk.ac.ebi.pride.utilities.data.core.AssayFileValidationSummary;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;
import uk.ac.ebi.pride.utilities.data.io.file.FastMzIdentMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.lightModel.*;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FastMzIdentMLController is a controller supporting for fast access of mzIdentML file
 * designed for EBI submission validation pipeline.
 *
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLController extends ReferencedIdentificationController {

    // Logger property to trace the Errors
    private static final Logger logger = LoggerFactory.getLogger(FastMzIdentMLController.class);

    private FastMzIdentMLUnmarshallerAdaptor unmarshaller;
    DataAccessController dataAccessController;
    private AssayFileValidationSummary assayFileValidationSummary = new AssayFileValidationSummary();

    public FastMzIdentMLController(File inputFile) {
        super(inputFile, DataAccessMode.CACHE_AND_SOURCE);
        initialize();
    }

    /**
     * This method mainly instantiate the cache according to the FastMzIdentMLCachingStrategy
     */
    protected void initialize() {

        // create pride access utils
        File file = (File) getSource();

        unmarshaller = new FastMzIdentMLUnmarshallerAdaptor(file);

        // init ms data accession controller map
        this.msDataAccessControllers = new HashMap<>();

        // set data source description
        this.setName(file.getName());

        // set the type
        this.setType(Type.MZIDENTML);

        // set the content categories
        this.setContentCategories(ContentCategory.SPECTRUM);

        setCachingStrategy(new FastMzIdentMLCachingStrategy());
        populateCache();
    }

    /**
     * Return the mzIdentML unmarshall adaptor to be used by the CacheBuilder
     * Implementation.
     *
     * @return MzIdentMLUnmarshallerAdaptor
     */
    public FastMzIdentMLUnmarshallerAdaptor getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * The main method which execute all the validation methods.
     *
     * @return AssayFileValidationSummary object which contains all the results of validation
     */
    public AssayFileValidationSummary validateMzIdentML() {

        proteinValidation();
        peptideValidation();
        spectraValidation(100);
        assayFileValidationSummary.printResults();
        return assayFileValidationSummary;
    }

    /**
     * This method scans through all the spectrumIdentificationLists and spectrumIdentificationLists,
     * and cross check if the spectra are available in the peak file. Number of calculations such as
     * Number of missing spectra, identified spectra done. Optionally, if the numberOfRandomChecks > 0,
     *
     * @param numberOfRandomChecks number of checks to perform DeltaMass Threshold checks
     */
    public void spectraValidation(final int numberOfRandomChecks) {

        int spectrumIdentificationListCount = 0;
        int spectrumIdentificationResultCount;
        int numberOfIdentifiedSpectra = 0;
        int errorPSMCount = 0;


        // Spectra details extracted from MzIdentML -> DataCollection -> Inputs
        // eg:  <SpectraData location="file:///Carbamoyl-phosphate synthase small chain-47029-41-G2-4-biotools.mgf" id="SD_1"></SpectraData>
        Map<Comparable, SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();

        // get all the Spectrum Identification Lists from the mzIdentML object
        List<SpectrumIdentificationList> spectrumIdentificationLists = unmarshaller.getSpectrumIdentificationList();

        Map<Integer, Integer> randomlySelectedList = getRandomlySelectedPSMs(spectrumIdentificationLists.size(), numberOfRandomChecks);

        // Run through each SpectrumIdentificationList
        for (SpectrumIdentificationList spectrumIdentificationList : spectrumIdentificationLists) {
            spectrumIdentificationResultCount = 0;
            // Run through each SpectrumIdentificationResult
            // eg: <SpectrumIdentificationResult id="SIR_12" spectrumID="index=35" spectraData_ref="SD_1">...</SpectrumIdentificationResult>
            for (SpectrumIdentificationResult spectrumIdentificationResult : spectrumIdentificationList.getSpectrumIdentificationResult()) {

                numberOfIdentifiedSpectra++;

                // eg: spectraData_ref="SD_1"
                String spectrumDataReference = spectrumIdentificationResult.getSpectraDataRef();

                // eg: spectrumID="index=35"
                String spectrumID = spectrumIdentificationResult.getSpectrumID();

                // get Spectra Data (eg: mgf file location, file format etc) for the current Spectra Identification List
                SpectraData spectraData = spectraDataIds.get(spectrumDataReference);

                // format Spectrum Id according to the peak file type
                String formattedSpectrumID = MzIdentMLUtils.getSpectrumId(SimpleToJmzIdentMLTransformer.convertSpectraDataToJmzidml(spectraData), spectrumID);

                // get the corresponding DataAccessController
                dataAccessController = this.msDataAccessControllers.get(spectrumDataReference);

                isSpectraInPeakFile(dataAccessController, formattedSpectrumID);

                if (numberOfRandomChecks > 0 &&
                        randomlySelectedList.containsKey(spectrumIdentificationResultCount) &&
                        randomlySelectedList.containsValue(spectrumIdentificationListCount)
                        ) {
                    // Spectrum spectrum = dataAccessController.getSpectrumById(formattedSpectrumID);
                    SpectrumIdentificationItem spectrumIdentificationItem = getRandomSpectrumIdentificationItem(spectrumIdentificationResult);
                    boolean passed = checkDeltaMassThreshold(spectrumIdentificationItem, formattedSpectrumID, Constants.DELTATHESHOLD);
                    if(!passed) errorPSMCount++;
                }
                spectrumIdentificationResultCount++;
            }
            spectrumIdentificationListCount++;
        }

        // Set values to AssayFileValidationSummary
        assayFileValidationSummary.setNumberOfSpectra(super.getNumberOfSpectra());
        assayFileValidationSummary.setNumberOfIdentifiedSpectra(numberOfIdentifiedSpectra);
        if(numberOfRandomChecks > 0) {
            assayFileValidationSummary.setDeltaMzErrorRate(new BigDecimal(((double) errorPSMCount / numberOfRandomChecks)).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
    }

    public AssayFileValidationSummary proteinValidation() {
        assayFileValidationSummary.setNumberOfProteins(unmarshaller.getProteinIds().size());
        return assayFileValidationSummary;
    }

    public AssayFileValidationSummary peptideValidation() {
        assayFileValidationSummary.setNumberOfPeptides(unmarshaller.getPeptideIds().size());
        return assayFileValidationSummary;
    }

    /**
     * Given a SpectrumIdentificationResult object, this method randomly select SpectrumIdentificationItem
     * from the SpectrumIdentificationResult object
     *
     * @param spectrumIdentificationResult Given SpectrumIdentificationItem object
     * @return randomly selected SpectrumIdentificationItem
     */
    private SpectrumIdentificationItem getRandomSpectrumIdentificationItem(SpectrumIdentificationResult spectrumIdentificationResult) {
        Random random = new Random();
        int randomSpectrumIdentificationItemIndex = random.ints(0, spectrumIdentificationResult.getSpectrumIdentificationItem().size()).findFirst().getAsInt();
        SpectrumIdentificationItem spectrumIdentificationItem = spectrumIdentificationResult.getSpectrumIdentificationItem().get(randomSpectrumIdentificationItemIndex);
        return spectrumIdentificationItem;
    }

    /**
     * Check if the Delta Mass is within the threshold value passed as a parameter.
     * Peptide modifications are also included for the calculations.
     *
     * @param spectrumIdentificationItem SpectrumIdentificationItem object from mzIdentML
     * @param deltaThreshold Non-negative Double value(eg: 4.0)
     * @return boolean
     */
    private boolean checkDeltaMassThreshold(SpectrumIdentificationItem spectrumIdentificationItem, String formattedSpectrumID, Double deltaThreshold) {
        boolean isDeltaMassThresholdPassed = true;

        Integer charge = spectrumIdentificationItem.getChargeState();
        double mz = spectrumIdentificationItem.getExperimentalMassToCharge();

        // find the peptide
        String peptideRef = spectrumIdentificationItem.getPeptideRef();
        Peptide peptide = unmarshaller.getPeptideById(peptideRef);

        if (peptide == null) {
            logger.error("Random peptide is null! peptideRef:" + peptideRef);
            isDeltaMassThresholdPassed = false;
        } else {
            List<Double> ptmMasses = new ArrayList<>();
            for (Modification modification : peptide.getModification()) {
                double monoMasses = modification.getMonoisotopicMassDelta();
                ptmMasses.add(monoMasses);
            }
            if (charge == null || mz == -1) {
                Spectrum spectrum = dataAccessController.getSpectrumById(formattedSpectrumID);
                if (spectrum != null) {
                    charge = dataAccessController.getSpectrumPrecursorCharge(spectrum.getId());
                    mz = dataAccessController.getSpectrumPrecursorMz(spectrum.getId());
                } else {
                    charge = null;
                }
                if (charge != null && charge == 0) {
                    charge = null;
                }
            }
            if (charge == null) {
                isDeltaMassThresholdPassed = false;
            } else {
                Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptide.getPeptideSequence(), mz, charge, ptmMasses);
                if (deltaMass == null || Math.abs(deltaMass) > deltaThreshold) {
                    isDeltaMassThresholdPassed = false;
                }
            }
        }
        return isDeltaMassThresholdPassed;
    }

    /**
     * This method first randomly select the indexes SpectrumIdentificationList and then for each SpectrumIdentificationList,
     * it will select random indexes of spectrumIdentificationResult. Those selected index numbers are saved in a map where:
     * Keys - Indexes of SpectrumIdentificationResult
     * Values - Indexes of SpectrumIdentificationList
     * Finally, it returns a Map<Indexes of SpectrumIdentificationResult, Indexes of SpectrumIdentificationList>
     *
     * @param numberOfSpectrumIdentificationLists Number of SpectrumIdentificationLists
     * @param numberOfRandomChecks Number of Random checks to be performed
     * @return Map<Integer, Integer>
     */
    private Map<Integer, Integer> getRandomlySelectedPSMs(int numberOfSpectrumIdentificationLists, int numberOfRandomChecks) {

        Random random = new Random();
        Map<Integer, Integer> randomlySelectedList = new HashMap<>(numberOfRandomChecks);

        // select random SpectrumIdentificationList for random validate checkup
        List<Integer> randomSpectrumIdentificationListIndexes = random.ints(0, numberOfSpectrumIdentificationLists).limit(numberOfRandomChecks).boxed().collect(Collectors.toList());
        for (Integer SpectrumIdentificationListIndex : randomSpectrumIdentificationListIndexes) {
            int spectrumIdentificationResultMaxRange = unmarshaller.getSpectrumIdentificationResultByIndex(SpectrumIdentificationListIndex).size();
            int spectrumIdentificationResult = random.ints(0, spectrumIdentificationResultMaxRange).limit(numberOfRandomChecks).findFirst().getAsInt();
            randomlySelectedList.put(spectrumIdentificationResult, SpectrumIdentificationListIndex);
        }
        return randomlySelectedList;
    }

    /**
     * Checks if the spectra available in the peak list. If it is not available,
     * missing spectrumIDs will be collected to the assayFileValidationSummary.
     *
     * @param dataAccessController DataAccessController
     * @param formattedSpectrumID  SpectrumID formatted based on the peak list file type
     * @return boolean value, false - if spectra cannot be found in the peak file
     */
    private boolean isSpectraInPeakFile(DataAccessController dataAccessController, String formattedSpectrumID) {
        if (dataAccessController != null) {
            if (!dataAccessController.getSpectrumIds().contains(formattedSpectrumID)) {
                assayFileValidationSummary.addMissingIdentifiedSpectraId(formattedSpectrumID);
                return false;
            }
        }
        return true;
    }

    @Override
    public List<uk.ac.ebi.pride.utilities.data.core.SpectraData> getSpectraDataFiles() {
        return null;
    }
}
