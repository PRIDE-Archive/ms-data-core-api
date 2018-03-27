package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.lightModel.CvParam;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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

    @Before
    public void setUp() throws Exception {
        monitor = MonitorFactory.start("uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.validateMzIdentML");
        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("carb.mzid");
        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mgf");
//        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("small.mzid");
//        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mgf");

        if (url == null || urlMgf == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        fastMzIdentMLController = new FastMzIdentMLController(inputFile);

        // add spectra file(s) to the FastMzIdentMLController
        List<File> files = new ArrayList<>();
        files.add(new File(urlMgf.toURI()));
        fastMzIdentMLController.addMSController(files);

        fastMzIdentMLController.doSpectraValidation();
    }

    @Test
    public void runAllValidations(){
        logger.info("Protein Counts: " + fastMzIdentMLController.getNumberOfProteins());
        logger.info("Peptide Counts: " + fastMzIdentMLController.getNumberOfPeptides());
        logger.info("Spectrum Count: " + fastMzIdentMLController.getNumberOfSpectra());
        logger.info("Missing Spectrum Count: " + fastMzIdentMLController.getNumberOfMissingSpectra());
        logger.info("Missing Spectra ID List: " + fastMzIdentMLController.getMissingIdentifiedSpectraIds().toString());
        logger.info("Identified Spectrum Count: " + fastMzIdentMLController.getNumberOfIdentifiedSpectra());
        logger.info("DeltaMz Error Rate: " + fastMzIdentMLController.getSampleDeltaMzErrorRate(12, 4.0));
        logger.info("Identified Unique PTMs: " + fastMzIdentMLController.getIdentifiedUniquePTMs().toString());
        logger.info("Search Modifications: " + fastMzIdentMLController.getSearchMofifications().toString());
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

    @After
    public void tearDown() {
        fastMzIdentMLController.close();
        monitor.stop();
        logger.info(monitor.toString());
    }

    @Test
    public void getIdentifiedUniquePTMs() {
        assertTrue("Total number of identified unique PTMs should be 2", fastMzIdentMLController.getIdentifiedUniquePTMs().size() == 2);
        logger.info(fastMzIdentMLController.getIdentifiedUniquePTMs().toString());
    }

    @Test
    public void getSearchMofifications() {
        assertTrue("Total number of search modifications should be 1", fastMzIdentMLController.getSearchMofifications().size() == 1);
        logger.info(fastMzIdentMLController.getSearchMofifications().toString());
    }
}