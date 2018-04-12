package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.mol.MoleculeUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 *
 * Testing all the components of the mzTab Controller for the pipeline.
 */

@Ignore
public class MzTabControllerImplIterativeTest {

    private MzTabControllerImpl mzTabController = null;
    private PeakControllerImpl peakController = null;


    public static final double MZ_OUTLIER = 4;


    public static final String PSI_MOD = "MOD";
    public static final String MS = "MS";
    public static final String UNIMOD = "UNIMOD";

    @Before
    public void setUp() throws Exception {
        URL url = MzTabControllerImplIterativeTest.class.getClassLoader().getResource("small-silac.mztab");
        URL urlMgf = MzTabControllerImplIterativeTest.class.getClassLoader().getResource("small-silac.mgf");

        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        mzTabController = new MzTabControllerImpl(inputFile);
        peakController = new PeakControllerImpl(new File(urlMgf != null ? urlMgf.toURI() : null));

        List<File> files = new ArrayList<File>();
        files.add(new File(urlMgf.toURI()));
        mzTabController.addMSController(files);
    }

    @After
    public void tearDown() throws Exception {
        mzTabController.close();
    }

    @Test
    @Ignore
    public void scanMetadata() throws IOException {
        long start = System.currentTimeMillis();
        scanForGeneralMetadata(mzTabController);
        scanForInstrument(peakController);
        scanForSoftware(mzTabController);
        scanForSearchDetails(mzTabController);
        scanEntryByEntry(mzTabController);
        scanSpectraAndPeakData(mzTabController);
        System.out.println("Final time in miliseconds: " + (System.currentTimeMillis() - start));
    }

    private void scanSpectraAndPeakData(MzTabControllerImpl dataAccessController) throws IOException {
        // spectra and peak list file
        List<uk.ac.ebi.pride.utilities.data.core.SpectraData> spectraDataFiles = dataAccessController.getSpectraDataFiles();
        for (SpectraData spectraDataFile : spectraDataFiles) {
            String location = spectraDataFile.getLocation();


            System.out.println("Searching for peak list file: " + location);

            Integer numberOfSpectrabySpectraData = dataAccessController.getNumberOfSpectrabySpectraData(spectraDataFile);

            System.out.println("Number of Spectra presents in the file: " + numberOfSpectrabySpectraData);
        }
    }

    private void scanForGeneralMetadata( DataAccessController dataAccessController) {

        ExperimentMetaData experimentMetaData = dataAccessController.getExperimentMetaData();

        // file id
        System.out.println("File Id: " + experimentMetaData.getId());

        // set assay title
        System.out.println("Experiment Name: " + experimentMetaData.getName());

        // set short label
        System.out.println("Short Label: " + experimentMetaData.getShortLabel());

        // protein count
        System.out.println("Protein Counts: " + dataAccessController.getNumberOfProteins());

        // peptide count
        System.out.println("Peptide Counts: " + dataAccessController.getNumberOfPeptides());

        // total spectrum count
        System.out.println("Spectrum Count: " + dataAccessController.getNumberOfSpectra());

        //contact
        System.out.println("Contact Person: " + experimentMetaData.getPersons().toString());

        // sample
        System.out.println("Samples: " + experimentMetaData.getSamples().toString());

        //additional params
        ParamGroup additional = dataAccessController.getExperimentMetaData().getAdditional();
        System.out.println("Additional Params: " + additional.getCvParams().toString());

    }

    private void scanForSoftware(DataAccessController dataAccessController) {

        ExperimentMetaData experimentMetaData = dataAccessController.getExperimentMetaData();

        Set<Software> softwares = new HashSet<Software>();
        //todo - dataProcessing params are not captured as software params
        //todo - there is a 1-1 mapping for pride XML, but how to deal with mzTab?
        //todo - will need to call getspectrumprotocol and getproteinprotocol on dataaccesscontroller to get params
        softwares.addAll(experimentMetaData.getSoftwares());

        System.out.println("Softwares: " + softwares);
    }

    private void scanForInstrument(DataAccessController dataAccessController) {

        //check to see if we have instrument configurations in the result file to scan
        //this isn't always present
        if (dataAccessController.getMzGraphMetaData() != null) {

            Collection<InstrumentConfiguration> instrumentConfigurations = dataAccessController.getMzGraphMetaData().getInstrumentConfigurations();
            for (InstrumentConfiguration instrumentConfiguration : instrumentConfigurations) {

                //set instrument cv param
                System.out.println("Instrument Configuration CvPrams: " + instrumentConfiguration.getCvParams().toString());

                //source
                for (InstrumentComponent source : instrumentConfiguration.getSource()) {
                    System.out.println("Source Instrument: " + source.getCvParams().toString());
                }
                //analyzer
                for (InstrumentComponent analyzer : instrumentConfiguration.getAnalyzer()) {
                    System.out.println("Analyzer Instrument: " + analyzer.getCvParams().toString());
                }

                //detector
                for (InstrumentComponent detector : instrumentConfiguration.getDetector()) {
                    System.out.println("Detector Instrument: " + detector.getCvParams().toString());
                }
            }
        }
    }

    private void scanForSearchDetails(DataAccessController dataAccessController) {
        // protein group

        Collection<Comparable> proteinIds = dataAccessController.getProteinIds();
        if (proteinIds != null && !proteinIds.isEmpty()) {
            Comparable firstProteinId = proteinIds.iterator().next();

            // protein accession
            String accession = dataAccessController.getProteinAccession(firstProteinId);
            System.out.println("First Protein: " + accession);

            // search database
            SearchDataBase searchDatabase = dataAccessController.getSearchDatabase(firstProteinId);
            if (searchDatabase != null) {
                System.out.println("Search Database: " + searchDatabase.getName());
            }
        }

    }

    private void scanEntryByEntry(DataAccessController dataAccessController) {

        Set<CvParam> ptms = new HashSet<CvParam>();
        Set<String> peptideSequences = new HashSet<String>();
        Set<Comparable> spectrumIds = new HashSet<Comparable>();
        double errorPSMCount = 0.0;
        double totalPSMCount = 0.0;
        long count = 0;

        // TODO - Address this random spectra test with better quality data
        //assertTrue("Check Random Spectra", ((ResultFileController) dataAccessController).checkRandomSpectraByDeltaMassThreshold(1, 4.0));
        Collection<Comparable> proteinIds = dataAccessController.getProteinIds();
        for (Comparable proteinId : proteinIds) {
            count ++;
            Collection<Comparable> peptideIds = dataAccessController.getPeptideIds(proteinId);
            for (Comparable peptideId : peptideIds) {
                totalPSMCount++;

                // peptide
                Peptide peptide = dataAccessController.getPeptideByIndex(proteinId, peptideId);
                PeptideSequence peptideSequence = peptide.getPeptideSequence();
                peptideSequences.add(peptideSequence.getSequence());

                // ptm
                List<Modification> modifications = new ArrayList<Modification>(dataAccessController.getPTMs(proteinId, peptideId));
                List<Double> ptmMasses = new ArrayList<Double>();
                for (Modification modification : modifications) {
                    // ptm mass
                    List<Double> monoMasses = modification.getMonoisotopicMassDelta();
                    if (monoMasses != null && !monoMasses.isEmpty()) {
                        ptmMasses.add(monoMasses.get(0));
                    }

                    // record ptm
                    List<CvParam> cvParams = modification.getCvParams();
                    for (CvParam cvParam : cvParams) {
                        if (cvParam.getCvLookupID().equalsIgnoreCase(PSI_MOD) || cvParam.getCvLookupID().equalsIgnoreCase(UNIMOD)) {
                            ptms.add(cvParam);
                        }
                    }
                }

                // precursor charge
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

                // delta mass
                if (charge == null) {
                    errorPSMCount++;
                } else {
                    Double deltaMass = MoleculeUtilities.calculateDeltaMz(peptideSequence.getSequence(), mz, charge, ptmMasses);
                    if (!isDeltaMzInRange(deltaMass)) {
                        errorPSMCount++;
                    }
                }

                // spectrum
                if (peptide.getSpectrumIdentification() != null && peptide.getSpectrumIdentification().getSpectrum() != null) {
                    Spectrum spectrum = peptide.getSpectrumIdentification().getSpectrum();
                    spectrumIds.add(spectrum.getId());
                }
            }

            if (count % 500 == 0) {
                System.out.println("Scanned " + count+ " entries of proteins from file : " + dataAccessController.getName());
            }
        }

        System.out.println("Peptide Sequences: " + peptideSequences.size());
        System.out.println("Number of Spectrums: " + spectrumIds.size());
        System.out.println("PTMs: " + ptms.toString());
        System.out.println("Delta Error Rate: " + (errorPSMCount / totalPSMCount));
    }

    protected boolean isDeltaMzInRange(Double deltaMz) {
        return deltaMz != null && (deltaMz >= -MZ_OUTLIER) && (deltaMz <= MZ_OUTLIER);
    }
}
