package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Most of the public methods of FastMzIdentMLController required for the validation are tested by FastMzIdentMLControllerTest class.
 *
 * @author Suresh Hewapathirana
 */
@Slf4j
public class FastMzIdentMLControllerTest {

    private FastMzIdentMLController fastMzIdentMLController;
    Monitor monitor;

    /**
     * This is the initial method which instantiates FastMzIdentMLController and spectra file(s) to the controller.
     * This will make sure to populate any cache data that are required for the fast validation.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        monitor = MonitorFactory.start("uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.validateMzIdentML");
        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("small.mzid");
        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mgf");

        if (url == null || urlMgf == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        fastMzIdentMLController = new FastMzIdentMLController(inputFile);

        // add spectra file(s) to the FastMzIdentMLController
        List<File> files = new ArrayList<>();
        files.add(new File(urlMgf.toURI()));
        fastMzIdentMLController.addMSController(files);
        // run initial inspection
        fastMzIdentMLController.doSpectraValidation();
    }

    /**
     * A method to print validation results. This provides fundamental statistics about the MzIdentML file.
     */
    @Test
    public void printValidationsResults() {
        log.info("Protein Counts: " + fastMzIdentMLController.getNumberOfProteins());
        log.info("Peptide Counts: " + fastMzIdentMLController.getNumberOfPeptides());
        log.info("PeptidoForms Counts: " + fastMzIdentMLController.getNumberOfPeptidoForms());
        log.info("Unique Peptide Counts: " + fastMzIdentMLController.getNumberOfUniquePeptides());
        log.info("Spectrum Count: " + fastMzIdentMLController.getNumberOfSpectra());
        log.info("Missing Spectrum Count: " + fastMzIdentMLController.getNumberOfMissingSpectra());
        log.info("Missing Spectra ID List: " + fastMzIdentMLController.getMissingIdentifiedSpectraIds().toString());
        log.info("Identified Spectrum Count: " + fastMzIdentMLController.getNumberOfIdentifiedSpectra());
        log.info("DeltaMz Error Rate: " + fastMzIdentMLController.getSampleDeltaMzErrorRate(100, 4.0));
        log.info("Identified Unique PTMs: " + fastMzIdentMLController.getIdentifiedUniquePTMs().toString());
        log.info("Search Modifications: " + fastMzIdentMLController.getSearchModifications().toString());
    }

    @Test
    public void getNumberOfProteins() {
        assertTrue("Total number of proteins in the MzIdentML file should be 327", fastMzIdentMLController.getNumberOfProteins() == 327);
    }

    @Test
    public void getNumberOfPeptides() {
        assertTrue("Total number of Peptide in the MzIdentML file should be 1956", fastMzIdentMLController.getNumberOfPeptides() == 1956);
    }

    @Test
    public void getNumberOfPeptidoForms() {
        assertTrue("Total number of PeptidoForms in the MzIdentML file should be 3757", fastMzIdentMLController.getNumberOfPeptidoForms() == 3757);
        assertTrue("Total number of PeptidoForms should be greater than or equal to number of peptides", fastMzIdentMLController.getNumberOfPeptides() >= fastMzIdentMLController.getNumberOfPeptides());
    }

    @Test
    public void getNumberOfUniquePeptides() {
        assertTrue("Total number of Peptide in the MzIdentML file should be 1956", fastMzIdentMLController.getNumberOfUniquePeptides() == 1956);
    }

    @Test
    public void getNumberOfSpectra() {
        assertTrue("Total number of Spectra in the MzIdentML file should be 1001", fastMzIdentMLController.getNumberOfSpectra() == 1001);
    }

    @Test
    public void getNumberOfMissingSpectra() {
        assertTrue("Total number of missing spectra in the MzIdentML file should be 0", fastMzIdentMLController.getNumberOfMissingSpectra() == 0);
    }

    @Test
    public void getNumberOfIdentifiedSpectra() {
        assertTrue("Total number of identified spectra in the MzIdentML file should be 851", fastMzIdentMLController.getNumberOfIdentifiedSpectra() == 851);
    }

    @Test
    public void checkRandomSpectraByDeltaMassThreshold() {
        final double DELTA_MZ = 4.0;
        assertTrue("DeltaMz Error Rate should be less than " + DELTA_MZ, fastMzIdentMLController.getSampleDeltaMzErrorRate(10, DELTA_MZ) < DELTA_MZ);
    }

    @Test
    public void getMissingIdentifiedSpectraIds() {
        assertTrue("Total number of missing spectra in the MzIdentML file should be 0", fastMzIdentMLController.getMissingIdentifiedSpectraIds().size() == 0);
    }

    @Test
    public void getIdentifiedUniquePTMs() {
        assertTrue("Total number of identified unique PTMs should be 3", fastMzIdentMLController.getIdentifiedUniquePTMs().size() == 3);
        log.info(fastMzIdentMLController.getIdentifiedUniquePTMs().toString());
    }

    @Test
    public void getSearchModifications() {
        assertTrue("Total number of search modifications should be 2", fastMzIdentMLController.getSearchModifications().size() == 2);
        log.info(fastMzIdentMLController.getSearchModifications().toString());
    }

    @Test
    public void getAnchorProteinIds() {
        assertTrue("Total number of protein ambiguity groups  should be 1", fastMzIdentMLController.getAnchorProteinIds().size() == 1);
    }

    @Test
    public void getExperimentMetaData() {
        fastMzIdentMLController.getExperimentMetaData();
    }

    /**
     * clear the MzIdentML object which holds all the data in memory
     */
    @After
    public void tearDown() {
        fastMzIdentMLController.close();
        monitor.stop();
        log.info(monitor.toString());
    }
}