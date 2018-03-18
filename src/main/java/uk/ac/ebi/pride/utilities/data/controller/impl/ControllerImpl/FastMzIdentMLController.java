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
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;

import java.io.File;
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

    // spectra that are reported in the mzIdentML file, but not available in the peak list/Spectra file
    int numberOfMissingSpectra = 0;
    // spectra that are reported in the mzIdentML file
    int numberOfIdentifiedSpectra = 0;

    private AssayFileValidationSummary assayFileValidationSummary = new AssayFileValidationSummary();

    public FastMzIdentMLController(File inputFile) {
        super(inputFile, DataAccessMode.CACHE_AND_SOURCE);
        initialize();
    }

    protected void initialize() {

        // create pride access utils
        File file = (File) getSource();

        unmarshaller = new FastMzIdentMLUnmarshallerAdaptor(file);

        // init ms data accession controller map
        this.msDataAccessControllers = new HashMap<Comparable, DataAccessController>();

        // set data source description
        this.setName(file.getName());

        // set the type
        this.setType(Type.MZIDENTML);

        // set the content categories
        this.setContentCategories(
                ContentCategory.SPECTRUM
        );

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


    @Override
    public List<uk.ac.ebi.pride.utilities.data.core.SpectraData> getSpectraDataFiles() {
        return null;
    }

//    public void setNumberOfProteins() {
//        this.assayFileValidationSummary.setNumberOfProteins(unmarshaller.getProteinIds().size());
//    }
//
//    public int getNumberOfSpectra() {
//        return super.getNumberOfSpectra();
//    }

    public AssayFileValidationSummary validateMzIdentML() {

        spectraValidation(10);
        assayFileValidationSummary.toString();
        return assayFileValidationSummary;
    }

    public void spectraValidation(final int numberOfRandomChecks) {

        Map<Integer, Integer> randomlySelectedList = new HashMap<>(numberOfRandomChecks);
        Random random = new Random();
        int spectrumIdentificationListCount = 0;
        int spectrumIdentificationResultCount = 0;

        Double deltaThreshold = 4.0;


        // Spectra details extracted from MzIdentML -> DataCollection -> Inputs
        // eg:  <SpectraData location="file:///Carbamoyl-phosphate synthase small chain-47029-41-G2-4-biotools.mgf" id="SD_1"></SpectraData>
        Map<Comparable, SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();

        // get all the Spectrum Identification Lists from the mzIdentML object
        List<SpectrumIdentificationList> psmList = unmarshaller.getMzIdentML().getDataCollection().getAnalysisData().getSpectrumIdentificationList();

        // select random SpectrumIdentificationList for random validate checkup
        List<Integer> randomSpectrumIdentificationListIndexes = random.ints(0, psmList.size()).limit(numberOfRandomChecks).boxed().collect(Collectors.toList());
        for (Integer SpectrumIdentificationListIndex : randomSpectrumIdentificationListIndexes) {
            int spectrumIdentificationResultRange = psmList.get(SpectrumIdentificationListIndex).getSpectrumIdentificationResult().size();
            int spectrumIdentificationResult = random.ints(0, spectrumIdentificationResultRange).limit(numberOfRandomChecks).findFirst().getAsInt();
            randomlySelectedList.put(spectrumIdentificationResult, SpectrumIdentificationListIndex);
        }

        // Run through each SpectrumIdentificationList
        for (SpectrumIdentificationList spectrumIdentificationList : psmList) {
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

                DataAccessController dataAccessController = this.msDataAccessControllers.get(spectrumDataReference);

                crosscheckExternallyReferencedSpectra(dataAccessController, formattedSpectrumID);

                if (numberOfRandomChecks > 0 && randomlySelectedList.containsKey(spectrumIdentificationResultCount) && randomlySelectedList.containsValue(spectrumIdentificationListCount)) {
                    System.out.println(spectrumIdentificationResultCount + " - " + spectrumIdentificationListCount);
                    boolean result = true;
//                    Spectrum spectrum = dataAccessController.getSpectrumById(formattedSpectrumID);

                    int randomSpectrumIdentificationItemIndex = random.ints(0, spectrumIdentificationResult.getSpectrumIdentificationItem().size()).findFirst().getAsInt();
                    SpectrumIdentificationItem spectrumIdentificationItem = spectrumIdentificationResult.getSpectrumIdentificationItem().get(randomSpectrumIdentificationItemIndex);

                    Integer charge = spectrumIdentificationItem.getChargeState();
                    double mz = spectrumIdentificationItem.getExperimentalMassToCharge();

                    // find the peptide
                    String peptideRef = spectrumIdentificationItem.getPeptideRef();
                    Peptide peptide = unmarshaller.getPeptideById(peptideRef);

                    if (peptide == null) {
                        logger.error("Random peptide is null! peptideRef:" + peptideRef);
                        result = false;
                    } else {

                        List<Double> ptmMasses = new ArrayList<>();
                        for (Modification mod : peptide.getModification()) {
                            double monoMasses = mod.getMonoisotopicMassDelta();
                            ptmMasses.add(monoMasses);
                        }
//                        if ((charge == null || mz == -1)) {
//                            if (spectrum != null) {
//                                charge = dataAccessController.getSpectrumPrecursorCharge(spectrum.getId());
//                                mz = dataAccessController.getSpectrumPrecursorMz(spectrum.getId());
//                            } else {
//                                charge = null;
//                            }
//                            if (charge != null && charge == 0) {
//                                charge = null;
//                            }
//                        }
                        if (charge == null) {
                            result = false;
                        } else {
                            Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptide.getPeptideSequence(), mz, charge, ptmMasses);
                            if (deltaMass == null || Math.abs(deltaMass) > deltaThreshold) {
                                result = false;
                            }
                        }
                    }
                    System.out.println("result : " + result);
                }
                spectrumIdentificationResultCount++;
            }
            spectrumIdentificationListCount++;
        }
        System.out.println("Number of Identified Spectra: " + numberOfIdentifiedSpectra);
        System.out.println("Number of Missing Spectra: " + numberOfMissingSpectra);
        System.out.println("Number of Unidentified Spectra: " + (getNumberOfSpectra() - numberOfIdentifiedSpectra));
        System.out.println("Total Number of Spectra: " + getNumberOfSpectra());
    }

    private boolean crosscheckExternallyReferencedSpectra(DataAccessController dataAccessController, String formattedSpectrumID) {
        if (dataAccessController != null) {
            if (dataAccessController.getSpectrumIds().contains(formattedSpectrumID)) {
                Spectrum spectrum = dataAccessController.getSpectrumById(formattedSpectrumID);
                if (spectrum != null) {

                }
            } else {
                assayFileValidationSummary.addMissingIdentifiedSpectraId(formattedSpectrumID);
                logger.error("Missing spectra found: " + formattedSpectrumID);
                return false;
            }
        }
        return true;
    }

    private void checkSampleDeltaMzErrorRate() {

//        List<Boolean> randomChecks = new ArrayList<>(NUMBER_OF_CHECKS);
//
//        IntStream.range(1, NUMBER_OF_CHECKS).sequential().forEach(i -> randomChecks.add(checkRandomSpectraByDeltaMassThreshold(NUMBER_OF_CHECKS, 4.0)));
//        int checkFalseCounts = 0;
//        for (Boolean check : randomChecks) {
//            if (!check) {
//                checkFalseCounts++;
//            }
//        }
//        assayFileSummary.setDeltaMzErrorRate(new BigDecimal(((double) checkFalseCounts / (NUMBER_OF_CHECKS*NUMBER_OF_CHECKS))).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }


//    public boolean checkRandomSpectraByDeltaMassThreshold(int numberSpectra, Double deltaThreshold){
//        boolean result = false;
//
//        List<Comparable> listIds = new ArrayList<>(getProteinIds());
//
//        int randomProtIdNumb;
//        int randomPepIdNumb;
//        if(hasSpectrum()) {
//            for (int i=0; i < numberSpectra && result; i++){
//                randomProtIdNumb = r.ints(0, listIds.size()).findFirst().getAsInt();
//                Comparable proteinId = listIds.get(randomProtIdNumb);
//                Protein protein = getProteinById(proteinId);
//                randomPepIdNumb = r.ints(0, protein.getPeptides().size()).findFirst().getAsInt();
//                Peptide peptide = protein.getPeptides().get(randomPepIdNumb);
//
//                if (peptide==null) {
//                    logger.error("Random peptide is null! Index:" + randomPepIdNumb);
//                    result = false;
//                } else {
//                    Spectrum spectrum = getSpectrumById(peptide.getSpectrumIdentification().getId());
//                    Integer charge = getPeptidePrecursorCharge(proteinId, peptide.getId());
//                    double mz = getPeptidePrecursorMz(proteinId, peptide.getId());
//                    List<Double> ptmMasses = new ArrayList<>();
//                    for (Modification mod : peptide.getModifications()) {
//                        List<Double> monoMasses = mod.getMonoisotopicMassDelta();
//                        if (monoMasses != null && !monoMasses.isEmpty())
//                            ptmMasses.add(monoMasses.get(0));
//                    }
//                    if ((charge == null || mz == -1)) {
//                        if (spectrum!=null) {
//                            charge = getSpectrumPrecursorCharge(spectrum.getId());
//                            mz = getSpectrumPrecursorMz(spectrum.getId());
//                        } else {
//                            charge = null;
//                        }
//                        if (charge != null && charge == 0) {
//                            charge = null;
//                        }
//                    }
//                    if (charge == null) {
//                        result = false;
//                    } else {
//                        Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptide.getSequence(), mz, charge, ptmMasses);
//                        if (deltaMass == null || Math.abs(deltaMass) > deltaThreshold) {
//                            result = false;
//                        }
//                    }
//                }
//            }
//        } else {
//            result = false;
//        }
//        return result;
//    }
//    public void validateSpectra() {
//
//        boolean mgfTitleReference = false;
//
//        // Spectra details extracted from MzIdentML -> DataCollection -> Inputs
//        // eg:  <SpectraData location="file:///Carbamoyl-phosphate synthase small chain-47029-41-G2-4-biotools.mgf" id="SD_1"></SpectraData>
//        Map<Comparable, SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();
//
//        Map<Comparable, Tuple<String, String>> identSpectrumMap = new HashMap<Comparable, Tuple<String, String>>(INIT_BIG_HASH);
//        List<Tuple<String, String>> spectrumIdentified = new ArrayList<>(INIT_BIG_HASH);
//
//        Map<Comparable, List<Comparable>> spectraDataMap = new HashMap<Comparable, List<Comparable>>(spectraDataIds.size());
//
//        // get all the Spectrum Identifaction list from the mzIdentML object
//        List<SpectrumIdentificationList> psm = mzIdentML.getDataCollection().getAnalysisData().getSpectrumIdentificationList();
//
//        // Run through each SpectrumIdentificationList
//        for (SpectrumIdentificationList spectrumIdentificationList : psm) {
//            System.out.println("SpectrumIdentificationList size: " + spectrumIdentificationList.getSpectrumIdentificationResult().size());
//
//            // Run through each SpectrumIdentificationResult
//            // eg: <SpectrumIdentificationResult id="SIR_12" spectrumID="index=35" spectraData_ref="SD_1">...</SpectrumIdentificationResult>
//            for (SpectrumIdentificationResult spectrumIdentificationResult : spectrumIdentificationList.getSpectrumIdentificationResult()) {
//
//                String spectrumIdentResultId = spectrumIdentificationResult.getId();
//
//                // eg: spectraData_ref="SD_1"
//                String spectrumDataReference = spectrumIdentificationResult.getSpectraDataRef();
//                System.out.println("spectrumDataReference : " + spectrumDataReference);
//
//                // eg: id="SIR_12"
//                String spectrumID = spectrumIdentificationResult.getId();
//                System.out.println("spectrumID : " + spectrumID);
//
//                SpectraData spectraData = spectraDataIds.get(spectrumDataReference);
//
//                // fill the SpectraDataMap
//                // for the currently referenced spectra file, retrieve the List (if it exists already) that is to store all the spectra IDs
//                List<Comparable> spectrumIds = spectraDataMap.get(spectrumDataReference);
//
//                // if there is no spectra ID list for the spectrum file yet, then create one and add it to the map
//                if (spectrumIds == null) {
//                    spectrumIds = new ArrayList<Comparable>();
//                    spectraDataMap.put(spectrumDataReference, spectrumIds);
//                }
//                // add the spectrum ID to the list of spectrum IDs for the current spectrum file
//                spectrumIds.add(spectrumID);
//
//                for (SpectrumIdentificationItem spectrumIdentificationItem : spectrumIdentificationResult.getSpectrumIdentificationItem()) {
//
//                    String spectrumIdentItemId = spectrumIdentificationItem.getId();
//                    Tuple<String, String> spectrumFeatures = null;
//
//                    if (mgfTitleReference) {
////                    if (mgfTitleReference && possibleMGMTitleReferenced.contains(spectrumDataReference)) {
////                        Comparable title = unmarshaller.getMGFTitleReference(spectrumIdentResultId);
////                        if(title != null){
////                            spectrumFeatures = new Tuple<String, String>(title.toString(), spectrumDataReference);
////                            identSpectrumMap.put(spectrumIdentItemId, spectrumFeatures);
////                            mgfTitleReferenceMap.put(spectrumFeatures, unmarshaller.getMGFTitleReference(spectrumIdentResultId));
////                            if(!spectraDataToMGF.contains(spectrumDataReference))
////                                spectraDataToMGF.add(spectrumDataReference);
////                        }
//// ;
//                    } else {
//                        // extract the spectrum ID from the provided identifier
//
//                        String formattedSpectrumID = MzIdentMLUtils.getSpectrumId(SimpleToJmzIdentMLTransformer.convertSpectraDataToJmzidml(spectraData), spectrumID);
//                        System.out.println("---------------- formated ID: " + formattedSpectrumID);
//                        Spectrum spectrum = getSpectrumById(formattedSpectrumID);
//                        System.out.println("spectrum.getPrecursors()" + spectrum.getPrecursors());
//
//                        spectrumFeatures = new Tuple<String, String>(formattedSpectrumID, spectrumDataReference);
//                        identSpectrumMap.put(spectrumIdentItemId, spectrumFeatures);
//                    }
//                    if (spectrumFeatures != null)
//                        spectrumIdentified.add(spectrumFeatures);
//                }
//            }
//        }
//    }
}
