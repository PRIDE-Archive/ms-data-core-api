package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */

public class MzIdentMLControllerIterativeTest {
    public static final Logger logger = LoggerFactory.getLogger(MzIdentMLControllerIterativeTest.class);
    
    private MzIdentMLControllerImpl mzIdentMLController = null;
    public static final double MZ_OUTLIER = 4;
    public static final String PSI_MOD = "MOD";
    public static final String MS = "MS";
    public static final String UNIMOD = "UNIMOD";


    @Before
    public void setUp() throws Exception {
        URL url = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mzid");
        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mgf");
        if (url == null || urlMgf == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        mzIdentMLController = new MzIdentMLControllerImpl(inputFile,true);
        List<File> files = new ArrayList<>();
        files.add(new File(urlMgf.toURI()));
        mzIdentMLController.addMSController(files);
    }

    @After
    public void tearDown() throws Exception {
        mzIdentMLController.close();
    }

    @Test
    public void scanMetadata() throws IOException {
        long start = System.currentTimeMillis();
        scanForGeneralMetadata(mzIdentMLController);
        scanForInstrument(mzIdentMLController);
        scanForSoftware(mzIdentMLController);
        scanForSearchDetails(mzIdentMLController);
        scanEntryByEntry(mzIdentMLController);
        scanMzIdentMLSpecificDetails(mzIdentMLController);
        logger.info("Final time in miliseconds: " + (System.currentTimeMillis() - start));
    }

    private void scanMzIdentMLSpecificDetails(MzIdentMLControllerImpl dataAccessController) throws IOException {
        List<SpectraData> spectraDataFiles = dataAccessController.getSpectraDataFiles();
        for (SpectraData spectraDataFile : spectraDataFiles) {
            String location = spectraDataFile.getLocation();
            logger.info("Searching for peak list file: " + location);
            Integer numberOfSpectrabySpectraData = dataAccessController.getNumberOfSpectrabySpectraData(spectraDataFile);
            logger.info("Number of Spectra presents in the file: " + numberOfSpectrabySpectraData);
        }
    }

    private void scanForGeneralMetadata( DataAccessController dataAccessController) {
        ExperimentMetaData experimentMetaData = dataAccessController.getExperimentMetaData();
        logger.info("Experiment Id: " + experimentMetaData.getId());
        logger.info("Experiment Name: " + experimentMetaData.getName());
        logger.info("Experiment Title: " + experimentMetaData.getShortLabel());
        logger.info("Protein Counts: " + dataAccessController.getNumberOfProteins());
        logger.info("Peptide Counts: " + dataAccessController.getNumberOfPeptides());
        logger.info("Spectrum Count: " + dataAccessController.getNumberOfSpectra());
        logger.info("Contact Person: " + experimentMetaData.getPersons().toString());
        logger.info("Experiment Id: " + experimentMetaData.getSamples().toString());
        ParamGroup additional = dataAccessController.getExperimentMetaData().getAdditional();
        logger.info("Experiment Id: " + additional.getCvParams().toString());
    }

    private void scanForSoftware(DataAccessController dataAccessController) {
        ExperimentMetaData experimentMetaData = dataAccessController.getExperimentMetaData();
        Set<Software> softwares = new HashSet<Software>();
        //todo - dataProcessing params are not captured as software params
        //todo - there is a 1-1 mapping for pride XML, but how to deal with mzidentml?
        //todo - will need to call getspectrumprotocol and getproteinprotocol on dataaccesscontroller to get params
        softwares.addAll(experimentMetaData.getSoftwares());
        logger.info("Softwares: " + softwares);
    }

    private void scanForInstrument(DataAccessController dataAccessController) {
        //check to see if we have instrument configurations in the result file to scan, this isn't always present
        if (dataAccessController.getMzGraphMetaData() != null) {
            Collection<InstrumentConfiguration> instrumentConfigurations = dataAccessController.getMzGraphMetaData().getInstrumentConfigurations();
            for (InstrumentConfiguration instrumentConfiguration : instrumentConfigurations) {
                logger.info("Instrument Configuration CvPrams: " + instrumentConfiguration.getCvParams().toString());
                for (InstrumentComponent source : instrumentConfiguration.getSource()) {
                    logger.info("Source Instrument: " + source.getCvParams().toString());
                }
                for (InstrumentComponent analyzer : instrumentConfiguration.getAnalyzer()) {
                    logger.info("Analyzer Instrument: " + analyzer.getCvParams().toString());
                }
                for (InstrumentComponent detector : instrumentConfiguration.getDetector()) {
                    logger.info("Detector Instrument: " + detector.getCvParams().toString());
                }
            }
        }
    }

    private void scanForSearchDetails(DataAccessController dataAccessController) {
        Collection<Comparable> proteinIds = dataAccessController.getProteinIds();
        if (proteinIds != null && !proteinIds.isEmpty()) {
            Comparable firstProteinId = proteinIds.iterator().next();
            String accession = dataAccessController.getProteinAccession(firstProteinId);
            logger.info("First Protein: " + accession);
            SearchDataBase searchDatabase = dataAccessController.getSearchDatabase(firstProteinId);
            if (searchDatabase != null) {
                logger.info("Search Database: " + searchDatabase.getName());
            }
        }
    }

    private void scanEntryByEntry(DataAccessController dataAccessController) {
        Set<CvParam> ptms = new HashSet<>();
        Set<String> peptideSequences = new HashSet<>();
        Set<Comparable> spectrumIds = new HashSet<>();
        double errorPSMCount = 0.0;
        double totalPSMCount = 0.0;
        long count = 0;

        // TODO - Address this random spectra test with better quality data
        //assertTrue("Check Random Spectra", ((ResultFileController) dataAccessController).checkRandomSpectraByDeltaMassThreshold(1, 4.0));
        Collection<Comparable> proteinIds = dataAccessController.getProteinIds();
        for (Comparable proteinId : proteinIds) {
            count ++;
            if (logger.isDebugEnabled() && ((int)((count * 100.0f) /  dataAccessController.getProteinIds().size())%10==0)) {
                logger.debug("\nPercent through total proteins, " + count + " / " +   dataAccessController.getProteinIds().size() + " : " + ((int)((count * 100.0f) /  dataAccessController.getProteinIds().size())) );
                calcCacheSizses((MzIdentMLControllerImpl) dataAccessController);
            }
            Collection<Comparable> peptideIds = dataAccessController.getPeptideIds(proteinId);
            for (Comparable peptideId : peptideIds) {
                totalPSMCount++;
                Peptide peptide = dataAccessController.getPeptideByIndex(proteinId, peptideId);
                PeptideSequence peptideSequence = peptide.getPeptideSequence();
                peptideSequences.add(peptideSequence.getSequence());
                List<Modification> modifications = new ArrayList<Modification>(dataAccessController.getPTMs(proteinId, peptideId));
                List<Double> ptmMasses = new ArrayList<Double>();
                for (Modification modification : modifications) {
                    List<Double> monoMasses = modification.getMonoisotopicMassDelta();
                    if (monoMasses != null && !monoMasses.isEmpty()) {
                        ptmMasses.add(monoMasses.get(0));
                    }
                    List<CvParam> cvParams = modification.getCvParams();
                    for (CvParam cvParam : cvParams) {
                        if (cvParam.getCvLookupID().equalsIgnoreCase(PSI_MOD) || cvParam.getCvLookupID().equalsIgnoreCase(UNIMOD)) {
                            ptms.add(cvParam);
                        }
                    }
                }
                Integer charge = dataAccessController.getPeptidePrecursorCharge(proteinId, peptideId);
                double mz = dataAccessController.getPeptidePrecursorMz(proteinId, peptideId);
                if ((charge == null || mz == -1)) {
                    Comparable specId = dataAccessController.getPeptideSpectrumId(proteinId, peptideId);
                    if(specId != null){
                        charge = dataAccessController.getSpectrumPrecursorCharge(specId);
                        mz = dataAccessController.getSpectrumPrecursorMz(specId);
                        if (charge == null || charge == 0) {
                            charge = null;
                        }
                    }
                }
                if (charge == null) {
                    errorPSMCount++;
                } else {
                    Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptideSequence.getSequence(), mz, charge, ptmMasses);
                    if (!isDeltaMzInRange(deltaMass)) {
                        errorPSMCount++;
                    }
                }
                if (peptide.getSpectrumIdentification() != null && peptide.getSpectrumIdentification().getSpectrum() != null) {
                    Spectrum spectrum = peptide.getSpectrumIdentification().getSpectrum();
                    spectrumIds.add(spectrum.getId());
                }
            }
            if (count % 500 == 0) {
                logger.info("Scanned " + count+ " entries of proteins from file : " + dataAccessController.getName());
            }
        }
        logger.info("Peptide Sequences: " + peptideSequences.size());
        logger.info("Number of Spectrums: " + spectrumIds.size());
        logger.info("PTMs: " + ptms.toString());
        logger.info("Delta Error Rate: " + (errorPSMCount / totalPSMCount));
    }

    private void calcCacheSizses(MzIdentMLControllerImpl mzIdentMLController) {
        Arrays.stream(CacheEntry.values()).forEach(cacheEntry -> logger.debug("Cache entry: " + cacheEntry.name() + " Size: " + (
            (mzIdentMLController.getCache().get(cacheEntry)==null?
                "null" :
            mzIdentMLController.getCache().get(cacheEntry) instanceof Map ?
                ((Map)mzIdentMLController.getCache().get(cacheEntry)).size() :
             mzIdentMLController.getCache().get(cacheEntry) instanceof Collection ?
                ((Collection)mzIdentMLController.getCache().get(cacheEntry)).size() :
             "null"))));
    }

    protected boolean isDeltaMzInRange(Double deltaMz) {
        return deltaMz != null && (deltaMz >= -MZ_OUTLIER) && (deltaMz <= MZ_OUTLIER);
    }
}