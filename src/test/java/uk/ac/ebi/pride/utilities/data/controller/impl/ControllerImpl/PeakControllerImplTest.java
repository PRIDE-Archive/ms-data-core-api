package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.Chromatogram;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

import java.io.File;
import java.net.URL;

public class PeakControllerImplTest{

    PeakControllerImpl peakController = null;

    @Before
    public void setUp() throws Exception {
        URL url = PeakControllerImplTest.class.getClassLoader().getResource("small.mgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        peakController = new PeakControllerImpl(inputFile);

    }

    @Test
    public void testGetChromatogramById() throws Exception {
        for(Comparable id: peakController.getChromatogramIds()){
            Chromatogram chromatogram = peakController.getChromatogramById(id);
            if(chromatogram != null)
                System.out.println(chromatogram.getIntensityArray().getDoubleArray().length);
        }
    }

    @Test
    public void testGetSpectrumIds() throws Exception {
        for(Comparable id: peakController.getSpectrumIds()){
            Spectrum spectrum = peakController.getSpectrumById(id);
            if(spectrum != null)
                System.out.println("Spectrum id: " + id + " Number of Peaks: " + spectrum.getMassIntensityMap().length);
        }
    }

    @After
    public void tearDown() throws Exception {

    }
}