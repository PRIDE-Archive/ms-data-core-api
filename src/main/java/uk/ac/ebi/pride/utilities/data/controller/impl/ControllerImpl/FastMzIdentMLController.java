package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.cache.strategy.FastMzIdentMLCachingStrategy;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.SimpleToJmzIdentMLTransformer;
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

/**
 * FastMzIdentMLController is a controller supporting for fast access of mzIdentML file designed for
 * EBI submission validation pipeline.
 *
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLController extends ReferencedIdentificationController {

    private static final Logger logger = LoggerFactory.getLogger(FastMzIdentMLController.class);

    private int numberOfIdentifiedSpectra = 0;
    private double deltaMzErrorRate = 0.0;
    private DataAccessController dataAccessController;
    private FastMzIdentMLUnmarshallerAdaptor unmarshaller;
    private Set<Comparable> missingIdentifiedSpectraIds;
    private Map<String, List<SpectrumIdentificationResult>> SpectrumIdentResultsGroupedBySpectraIDs;

    /**
     * This constructor forces to cache objects that are required for later use
     *
     * @param inputFile MzIdentML file
     */
    public FastMzIdentMLController(File inputFile) {
        super(inputFile, DataAccessMode.CACHE_AND_SOURCE);
        initialize();
    }

    /**
     * This method mainly instantiate the cache according to the logic specified in
     * FastMzIdentMLCachingStrategy
     */
    protected void initialize() {
        File file = (File) getSource();
        unmarshaller = new FastMzIdentMLUnmarshallerAdaptor(file);
        msDataAccessControllers = new HashMap<>();
        setName(file.getName());
        setType(Type.MZIDENTML);
        setContentCategories(ContentCategory.SPECTRUM);
        setCachingStrategy(new FastMzIdentMLCachingStrategy());
        populateCache();
    }

    /**
     * Get the FastMzIdentMLUnmarshallerAdaptor to be used by the CacheBuilder Implementation.
     *
     * @return MzIdentMLUnmarshallerAdaptor
     */
    public FastMzIdentMLUnmarshallerAdaptor getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * This is an initial scan through all the spectrumIdentificationLists and
     * spectrumIdentificationLists, and cross check if the spectra are available in the peak file.
     * Number of calculations such as Number of missing spectra, identified spectra will be performed.
     */
    public void doSpectraValidation() {
        missingIdentifiedSpectraIds = new HashSet<>();
        SpectrumIdentResultsGroupedBySpectraIDs = new Hashtable<>();

    /* Spectra details extracted from MzIdentML -> DataCollection -> Inputs
    eg:  <SpectraData location="file:///Carbamoyl-phosphate synthase small chain-47029-41-G2-4-biotools.mgf" id="SD_1"></SpectraData> */
        Map<Comparable, SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();
        List<SpectrumIdentificationList> spectrumIdentificationLists =
                unmarshaller.getSpectrumIdentificationList();
        for (SpectrumIdentificationList spectrumIdentificationList : spectrumIdentificationLists) {
            // eg: <SpectrumIdentificationResult id="SIR_12" spectrumID="index=35"
            // spectraData_ref="SD_1">...</SpectrumIdentificationResult>
            for (SpectrumIdentificationResult spectrumIdentificationResult :
                    spectrumIdentificationList.getSpectrumIdentificationResult()) {
                numberOfIdentifiedSpectra++;
                String spectrumDataRef =
                        spectrumIdentificationResult.getSpectraDataRef(); // eg: spectraData_ref="SD_1"
                String spectrumID =
                        spectrumIdentificationResult.getSpectrumID(); // eg: spectrumID="index=35"
                SpectraData spectraData =
                        spectraDataIds.get(spectrumDataRef); // eg: mgf file location, file format etc
                String formattedSpectrumID =
                        MzIdentMLUtils.getSpectrumId(
                                SimpleToJmzIdentMLTransformer.convertSpectraDataToJmzidml(spectraData),
                                spectrumID); // eg: 35
                spectrumIdentificationResult.setFormattedSpectrumID(formattedSpectrumID);
                SpectrumIdentResultsGroupedBySpectraIDs.computeIfAbsent(
                        spectrumDataRef, value -> new ArrayList<>())
                        .add(spectrumIdentificationResult);
                // check the spectra referenced in the mzIdentML also available in the peak files
                dataAccessController = msDataAccessControllers.get(spectrumDataRef);
                if (!isSpectraInPeakFile(dataAccessController, formattedSpectrumID)) {
                    missingIdentifiedSpectraIds.add(formattedSpectrumID);
                }
            }
        }
    }

    /**
     * Get the number of Proteins reported in the MzIdentML file
     *
     * @return Number of Proteins
     */
    @Override
    public int getNumberOfProteins() {
        return unmarshaller.getProteinIds().size();
    }

    /**
     * Get the total number of Peptide Sequences identified in the MzIdentML. In other words,
     * it counts how many <Peptide> elements are there in the MzIdentML file
     *
     * @return Number of Peptides
     */
    @Override
    public int getNumberOfPeptides() {
        return unmarshaller.getPeptideIds().size();
    }

    /**
     * Get PeptidoForms. PeptidoForms are different peptide forms that a peptide can have due to various modifications.
     * and various position(s) of the modification(s).
     *
     * @return Number of PeptidoForms
     */
    public int getNumberOfPeptidoForms() {
        return unmarshaller.getNumberOfPeptidoForms();
    }


    /**
     * Get the total number of unique Peptides  identified in the MzIdentML.
     *
     * @return Number of Peptides
     */
    public int getNumberOfUniquePeptides() {
        return unmarshaller.getUniquePeptideIds().size();
    }

    /**
     * Get the number of Spectra in the DataAccessController
     *
     * @return The number of Spectra for DataAccessController
     */
    @Override
    public int getNumberOfSpectra() {
        return super.getNumberOfSpectra();
    }

    /**
     * Get number of Spectra referenced in the MzIdentML, but actually not available in the
     * Spectra/Peak list File
     *
     * @return Number of missing spectra
     */
    @Override
    public int getNumberOfMissingSpectra() {
        return missingIdentifiedSpectraIds.size();
    }

    /**
     * Get the number of MS/MS with at least one peptide identification in the MzIdentML file This is
     * equivalent to number of SpectrumIdentificationList elements in the mzIdentML
     *
     * @return Number of Spectra reported in the MzIdentML file
     */
    @Override
    public int getNumberOfIdentifiedSpectra() {
        return numberOfIdentifiedSpectra;
    }

    /**
     * Get list of Spectra Ids which have been referenced in the MzIdentML, but actually not available
     * in the Spectra/Peak list File
     *
     * @return List of Spectra IDs
     */
    public Collection<CvParam> getIdentifiedUniquePTMs() {
        return unmarshaller.getIdentifiedUniquePTMs();
    }

    /**
     * Get all the Search modifications used as search parameter(s) for the identification, they may
     * differ than the identified peptide modifications
     *
     * @return Set of unique Search Modifications
     */
    public Collection<CvParam> getSearchModifications() {
        return unmarshaller.getSearchModifications();
    }

    /**
     * Get IDs of Spectra referenced in the MzIdentML, which are actually not available in the
     * Spectra/Peak list File
     *
     * @return List of Spectra Ids
     */
    public Set<Comparable> getMissingIdentifiedSpectraIds() {
        return missingIdentifiedSpectraIds;
    }

    /**
     * Get the List of File Spectra that the MzIdentML use to identified peptides
     *
     * @return List of SpectraData Files associated with MzIdentML.
     */
    @Override
    public List<uk.ac.ebi.pride.utilities.data.core.SpectraData> getSpectraDataFiles() {
        return null;
    }

    /**
     * Get anchor protein Ids from the Protein Ambiguity Groups
     *
     * @return collection of anchor protein Ids
     */
    public Collection<Comparable> getAnchorProteinIds() {
        Set<Comparable> anchorProteins = new HashSet<>();
        if (hasProteinAmbiguityGroup()) {
            for (ProteinAmbiguityGroup proteinAmbiguityGroup : unmarshaller.getProteinAmbiguityGroups()) {
                anchorProteins.add(getAnchorProteinId(proteinAmbiguityGroup));
            }
        }
        return anchorProteins;
    }

    /**
     * This method finds the anchor protein from the Protein Ambiguity groups by comparing the CV term
     * and returns the Id of the anchor protein
     *
     * @param proteinAmbiguityGroup Protein Ambiguity group
     * @return anchor protein Id.
     */
    private String getAnchorProteinId(ProteinAmbiguityGroup proteinAmbiguityGroup) {
        ProteinDetectionHypothesis anchorPDH = null;

        for (ProteinDetectionHypothesis proteinDetectionHypothesis :
                proteinAmbiguityGroup.getProteinDetectionHypothesis()) {
            for (CvParam cvParam : proteinDetectionHypothesis.getCvParam()) {
                if (cvParam.getAccession().equals(Constants.ANCHOR_PROTEIN)) {
                    anchorPDH = proteinDetectionHypothesis;
                }
            }
        }
        return (anchorPDH != null) ? anchorPDH.getDBSequenceRef() : null;
    }

    /**
     * This method randomly selects PSM(but covers different peak files as much as possible), and
     * calculates the delta mass error rate. For more information about PSM random selection, please
     * refer {@link #getRandomPSMs}
     *
     * @param numberOfChecks non negative integer
     * @param deltaThreshold double value
     * @return boolean value. If the delta mass error rate within the threshold, it returns true,
     * otherwise false
     */
    public double getSampleDeltaMzErrorRate(final int numberOfChecks, final Double deltaThreshold) {
        if (numberOfChecks > 0) {
            List<SpectrumIdentificationItem> PSMList = getRandomPSMs(numberOfChecks);
            int errorPSMCount = 0;

            for (SpectrumIdentificationItem SpectrumIdentificationItem : PSMList) {
                logger.debug(
                        "SpectrumIdentificationItem  - "
                                + SpectrumIdentificationItem.getId()
                                + "has been selected for random checkup");
                Boolean result = checkDeltaMassThreshold(SpectrumIdentificationItem, deltaThreshold);
                if (!result) errorPSMCount++;
            }
            deltaMzErrorRate =
                    new BigDecimal(((double) errorPSMCount / numberOfChecks))
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
        }
        return deltaMzErrorRate;
    }

    /**
     * This method randomly collects a single PSM from each Peak/spectra which were identified, and
     * repeatedly collecting PSM until it reaches the numberOfChecks limit. However, if number of
     * randomly selected PSMs getting exceeded the total number of PSMs reported in the MzIdentML,
     * then the collection process will be stopped and it returns the collected PSMs with a warning.
     *
     * @param numberOfChecks non negative integer
     * @return randomly selected SpectrumIdentificationItem list
     */
    @SuppressWarnings("unchecked")
    private List<SpectrumIdentificationItem> getRandomPSMs(final int numberOfChecks) {
        Random random = new Random();
        List<SpectrumIdentificationItem> selectedPSMs = new ArrayList<>(numberOfChecks);

        while (selectedPSMs.size() < numberOfChecks) {
            for (Map.Entry PSMList : SpectrumIdentResultsGroupedBySpectraIDs.entrySet()) {
                List<SpectrumIdentificationResult> psms =
                        (List<SpectrumIdentificationResult>) PSMList.getValue();
                if (psms != null && !psms.isEmpty()) {
                    SpectrumIdentificationResult SpectrumIdentificationResult =
                            psms.get(random.nextInt(psms.size()));
                    SpectrumIdentificationItem psm =
                            SpectrumIdentificationResult.getSpectrumIdentificationItem().get(0);
                    psm.setFormattedSpectrumID(SpectrumIdentificationResult.getFormattedSpectrumID());
                    selectedPSMs.add(psm);
                }
            }
            if (selectedPSMs.size() >= numberOfIdentifiedSpectra) {
                logger.warn(
                        "Number of checks specified is higher than the number of PSMs! Only "
                                + numberOfIdentifiedSpectra
                                + " will be performed!");
                break;
            }
        }
        return selectedPSMs;
    }

    /**
     * Check if the Delta Mass is within the threshold value passed as a parameter. Peptide
     * modifications are also included for the calculations.
     *
     * @param spectrumIdentificationItem SpectrumIdentificationItem object from mzIdentML
     * @param deltaThreshold             Non-negative Double value(eg: 4.0)
     * @return boolean
     */
    private boolean checkDeltaMassThreshold(
            SpectrumIdentificationItem spectrumIdentificationItem, Double deltaThreshold) {
        boolean isDeltaMassThresholdPassed = true;
        Integer charge = spectrumIdentificationItem.getChargeState();
        double mz = spectrumIdentificationItem.getExperimentalMassToCharge();
        String peptideRef = spectrumIdentificationItem.getPeptideRef();
        Peptide peptide = unmarshaller.getPeptideById(peptideRef);
        if (peptide != null) {
            List<Double> ptmMasses = unmarshaller.getPTMMassesFromPeptide(peptide);
            if (mz == -1) {
                Spectrum spectrum =
                        dataAccessController.getSpectrumById(
                                spectrumIdentificationItem.getFormattedSpectrumID());
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
                Double deltaMass =
                        MoleculeUtilities.calculateDeltaMz(peptide.getPeptideSequence(), mz, charge, ptmMasses);
                if (deltaMass == null || Math.abs(deltaMass) > deltaThreshold) {
                    isDeltaMassThresholdPassed = false;
                }
            }
        } else {
            logger.error("Random peptide is null! peptideRef:" + peptideRef);
            isDeltaMassThresholdPassed = false;
        }
        return isDeltaMassThresholdPassed;
    }

    /**
     * Checks if the spectra available in the peak list. If it is not available, missing spectrumIDs
     * will be collected to the assayFileValidationSummary.
     *
     * @param dataAccessController DataAccessController
     * @param formattedSpectrumID  SpectrumID formatted based on the peak list file type
     * @return boolean value, false - if spectra cannot be found in the peak file
     */
    private boolean isSpectraInPeakFile(
            DataAccessController dataAccessController, String formattedSpectrumID) {
        boolean spectraFound = true;
        if (dataAccessController != null) {
            spectraFound = dataAccessController.getSpectrumIds().contains(formattedSpectrumID);
            if (!dataAccessController.getSpectrumIds().contains(formattedSpectrumID)) {
                spectraFound = false;
            }
        }
        return spectraFound;
    }

    /**
     * Check of the mzIdentML object contains any protein ambiguity groups and return a boolean value
     * //TODO: check if the object exists
     *
     * @return boolean value
     */
    @Override
    public boolean hasProteinAmbiguityGroup() {
        return unmarshaller
                .getMzIdentML()
                .getDataCollection()
                .getAnalysisData()
                .getProteinDetectionList()
                .getProteinAmbiguityGroup().size() > 0;
    }
}
