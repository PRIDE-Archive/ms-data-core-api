package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.*;

public class NetCDFControllerImplTest {

    File sourcefile = null;

    private NetCDFControllerImpl cdfFile;


    @Before
    public void setUp() throws Exception {

        try {
            URL testFile = getClass().getClassLoader().getResource("SBEP_Microbiome_025.CDF");
            assertNotNull("Error loading netCDF test file", testFile);
            sourcefile = new File(testFile.toURI());

            if (sourcefile != null)
                cdfFile = new NetCDFControllerImpl(sourcefile);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    }

    @After
    public void tearDown() throws Exception {
        cdfFile.close();

    }

    @Test
    public void testGetSpectrumById() throws Exception {
        Collection<Comparable> ids = cdfFile.getSpectrumIds();
        for(Comparable id: ids){
            Spectrum spectrum = cdfFile.getSpectrumById(id);
            double totalIntensity = 0.0f;
            for(int i = 0; i < spectrum.getMassIntensityMap().length; i++)
                totalIntensity += spectrum.getMassIntensityMap()[i][1];
            System.out.println("Scan: " + id + " TIC: " + totalIntensity);
        }

    }

    @Test
    public void testGetSpectrumIds() throws Exception {
        assertTrue(cdfFile.getNumberOfSpectra() == 5020);

    }
}