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

    @Before
    public void setUp() throws Exception {
        monitor = MonitorFactory.start("uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.validateMzIdentML");
//        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("F238646.mzid");
//        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("mascot_daemon_merge.mgf");
        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("small.mzid");
        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mgf");
//        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("test.mzid");
//        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("test.mgf");
        if (url == null || urlMgf == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        fastMzIdentMLController = new FastMzIdentMLController(inputFile);

        // add spectra file(s) to the FastMzIdentMLController
        List<File> files = new ArrayList<>();
        files.add(new File(urlMgf.toURI()));
        fastMzIdentMLController.addMSController(files);
    }

    @Test
    public void getTotalNumberOfProteins() {
        assertTrue("Total number of proteins in the MzIdentML file", fastMzIdentMLController.getTotalNumberOfProteins() == 327);
    }

    @Test
    public void getTotalNumberOfPeptides() {
        System.out.println(fastMzIdentMLController.getTotalNumberOfPeptides());
        assertTrue("Total number of Peptides in the MzIdentML file", fastMzIdentMLController.getTotalNumberOfPeptides() == 327);
    }

    @Test
    public void getTotalNumberOfSpectra() {
    }

    @Test
    public void getTotalNumberOfUniquePeptides() {
    }

    @Test
    public void getTotalNumberOfIdentifiedSpectra() {
    }

    @Test
    public void getMissingIdentifiedSpectraIds() {
    }

    @Test
    public void getDeltaMzErrorRate() {
    }

    @After
    public void tearDown() {
        fastMzIdentMLController.close();
        monitor.stop();
        logger.info(monitor.toString());
    }
}