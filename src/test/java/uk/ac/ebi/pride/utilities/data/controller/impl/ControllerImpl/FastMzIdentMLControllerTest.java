package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLControllerTest {

    public static final Logger logger = LoggerFactory.getLogger(FastMzIdentMLControllerTest.class);

    private FastMzIdentMLController fastMzIdentMLController;

    @Before
    public void setUp() throws Exception {
//        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("F238646.mzid");
//        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("mascot_daemon_merge.mgf");
//        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("small.mzid");
//        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("small.mgf");
        URL url = FastMzIdentMLControllerTest.class.getClassLoader().getResource("test.mzid");
        URL urlMgf = MzIdentMLControllerIterativeTest.class.getClassLoader().getResource("test.mgf");
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
    public void Validate() {
        fastMzIdentMLController.spectraValidation(10);
    }

    @Test
    public void getNumberOfProteins() {
        assertEquals(327, fastMzIdentMLController.getNumberOfProteins());
    }

    @Test
    public void getNumberOfSpectra() {
        assertEquals(1001, fastMzIdentMLController.getNumberOfSpectra());
    }

    @Test
    public void getNumberOfMissingSpectra() {
        assertEquals(1001, fastMzIdentMLController.getNumberOfMissingSpectra());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Test
    public void validateMzIdentML() {
        Monitor monitor= MonitorFactory.start("uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.validateMzIdentML");
        fastMzIdentMLController.validateMzIdentML();
        monitor.stop();
        System.out.println("Performance INFO: --------------  " + monitor);
    }

    @After
    public void tearDown() {
        fastMzIdentMLController.close();
    }
}