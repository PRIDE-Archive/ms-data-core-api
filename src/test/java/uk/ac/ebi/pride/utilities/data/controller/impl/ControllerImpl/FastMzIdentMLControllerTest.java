package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(FastMzIdentMLControllerTest.class);
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
        logger.info("Protein Counts: " + fastMzIdentMLController.getNumberOfProteins());
        logger.info("Peptide Counts: " + fastMzIdentMLController.getNumberOfPeptides());
        logger.info("PeptidoForms Counts: " + fastMzIdentMLController.getNumberOfPeptidoForms());
        logger.info("Unique Peptide Counts: " + fastMzIdentMLController.getNumberOfUniquePeptides());
        logger.info("Spectrum Count: " + fastMzIdentMLController.getNumberOfSpectra());
        logger.info("Missing Spectrum Count: " + fastMzIdentMLController.getNumberOfMissingSpectra());
        logger.info("Missing Spectra ID List: " + fastMzIdentMLController.getMissingIdentifiedSpectraIds().toString());
        logger.info("Identified Spectrum Count: " + fastMzIdentMLController.getNumberOfIdentifiedSpectra());
        logger.info("DeltaMz Error Rate: " + fastMzIdentMLController.getSampleDeltaMzErrorRate(12, 4.0));
        logger.info("Identified Unique PTMs: " + fastMzIdentMLController.getIdentifiedUniquePTMs().toString());
        logger.info("Search Modifications: " + fastMzIdentMLController.getSearchModifications().toString());
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
        logger.info(fastMzIdentMLController.getIdentifiedUniquePTMs().toString());
    }

    @Test
    public void getSearchModifications() {
        assertTrue("Total number of search modifications should be 2", fastMzIdentMLController.getSearchModifications().size() == 2);
        logger.info(fastMzIdentMLController.getSearchModifications().toString());
    }

    @Test
    public void getAnchorProteinIds() {
        assertTrue("Total number of protein ambiguity groups  should be 1", fastMzIdentMLController.getAnchorProteinIds().size() == 1);
    }

    @After
    public void tearDown() {
        fastMzIdentMLController.close();
        monitor.stop();
        logger.info(monitor.toString());
    }
}