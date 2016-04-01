package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Yasset Perez-Riverol
 */
public class MzIdentMlMSControllerImplTest {

    private MzIdentMLControllerImpl mzIdentMlController = null;


    @Before
    public void setUp() throws Exception {
        URL url = MzIdentMlControllerImplTest.class.getClassLoader().getResource("small.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        mzIdentMlController = new MzIdentMLControllerImpl(inputFile, true);
        url = MzIdentMlControllerImplTest.class.getClassLoader().getResource("small.mgf");
        File filems = new File(url != null ? url.getFile() : null != null ? url != null ? url.getFile() : null : null);
        List<File> fileList = new ArrayList<File>();
        fileList.add(filems);
        mzIdentMlController.addMSController(fileList);

    }

    @After
    public void tearDown() throws Exception {
        mzIdentMlController.close();
    }


    @Test
    public void addMSController() throws Exception {
        Spectrum spectrum = mzIdentMlController.getSpectrumById("730!SD_1");
        assertTrue("There should be 60 peaks", spectrum.getIntensityBinaryDataArray().getDoubleArray().length == 60);
    }

    @Test
    public void checkReferencedSpectra(){
        boolean status = mzIdentMlController.checkRandomSpectraByDeltaMassThreshold(10, 2.0);
        Assert.assertTrue(status);
    }



}
