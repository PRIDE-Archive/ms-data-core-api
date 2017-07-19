package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for a MzML file
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class MzMLControllerImplTest {

    private MzMLControllerImpl mzMLController = null;

    @Before
    public void setUp() throws Exception {
        URL url = MzMLControllerImplTest.class.getClassLoader().getResource("tiny.pwiz.1.1.mzML");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        mzMLController = new MzMLControllerImpl(inputFile);
    }

    @After
    public void tearDown() throws Exception {
        mzMLController.close();
    }

    @Test
    public void testGetExperimentMetaData() throws Exception {
        ExperimentMetaData metada = mzMLController.getExperimentMetaData();
        assertTrue("mzML file ID should always be", metada.getId().toString().equalsIgnoreCase("urn:lsid:psidev.info:mzML.instanceDocuments.tiny.pwiz"));
        assertTrue("mzML file Version should always be", metada.getVersion().equalsIgnoreCase("1.1.0"));
    }

    @Test
    public void testGetFileDescription() throws Exception {
        ExperimentMetaData metada = mzMLController.getExperimentMetaData();
        assertTrue("mzML file Content should be", metada.getFileContent().getCvParams().get(0).getName().equalsIgnoreCase("MSn spectrum"));
        assertTrue("mzML file Content should be", metada.getFileContent().getCvParams().get(1).getName().equalsIgnoreCase("centroid spectrum"));
    }

    @Test
    public void testGetSamples() throws Exception {
        List<Sample> samples = mzMLController.getSamples();
        assertTrue("There should be only one sample", samples.size() == 1);
        assertEquals("Sample ID should always be sample1", samples.get(0).getId(), "_x0032_0090101_x0020_-_x0020_Sample_x0020_1");
        assertEquals("Sample cv param should be lung", samples.get(0).getName(), "Sample 1");
    }

    @Test
    public void testGetSoftware() throws Exception {
        List<Software> softwares = mzMLController.getSoftwares();
        assertTrue("There should be only three softwares", softwares.size() == 3);
        assertEquals("Software Name should always be software1", softwares.get(0).getId(), "Bioworks");
        assertEquals("Software Name should always be software2", softwares.get(1).getId(), "pwiz");
        assertEquals("Software version should always be software3", softwares.get(2).getVersion(), "2.0.5");
    }

    @Test
    public void testGetScanSettings() throws Exception {
       List<ScanSetting> scanSettings = mzMLController.getScanSettings();
       assertTrue("There should be only one ScanSetting", scanSettings.size() == 1);
       assertEquals("ScanSetting ID should be always Scanning 1", scanSettings.get(0).getId(), "tiny_x0020_scan_x0020_settings");
       assertTrue("Number of Source Files for ScanSetting should always one", scanSettings.get(0).getSourceFile().size() == 1);
    }

    @Test
    public void testGetInstrumentConfigurations() throws Exception {
        List<InstrumentConfiguration> instrumentConfigurations = mzMLController.getInstrumentConfigurations();
        assertTrue("There should be only one InstrumentConfiguration", instrumentConfigurations.size() ==1);
        assertEquals("The software used should be", instrumentConfigurations.get(0).getSoftware().getId(),"CompassXtract");
        assertTrue("The source used should be", instrumentConfigurations.get(0).getSource().get(0).getOrder() == 1);
    }

    @Test
    public void testGetDataProcessings() throws Exception {
        List<DataProcessing> dataProcessings = mzMLController.getDataProcessings();
        assertTrue("There should be only two DataProcessing Objects", dataProcessings.size()==2);
        assertEquals("The software used one Procesing Step is", dataProcessings.get(0).getProcessingMethods().get(0).getSoftware().getId(),"CompassXtract");
        assertEquals("CvParams associated with the second Processing Object is", dataProcessings.get(1).getProcessingMethods().get(0).getCvParams().get(0).getName(),"Conversion to mzML");

    }

    @Test
    public void testGetSpectrumIds() throws Exception {
        List<Comparable> ids = new ArrayList<Comparable>(mzMLController.getSpectrumIds());
        assertTrue("There should be four Spectras", ids.size()==4);
        assertTrue("The id of the first spectra should be", ids.contains("scan=19"));
        assertTrue("The id of the Four spectra should be", ids.contains("sample=1 period=1 cycle=22 experiment=1"));
    }

    @Test
    public void testGetSpectrumById() throws Exception {
        Spectrum spectrum = mzMLController.getSpectrumById("scan=20");
        assertTrue("The id of the spectra should be", spectrum.getId().equals("scan=20"));
        assertEquals("CvTerm Scan List Scan Windows", spectrum.getScanList().getScans().get(0).getScanWindows().get(0).getCvParams().get(0).getName(),"scan window lower limit");
    }

    @Test
    public void testGetChromatogramIds() throws Exception {
        List<Comparable> ids = new ArrayList<Comparable>(mzMLController.getChromatogramIds());
        assertTrue("There should be four Chromatograms", ids.size()==2);
        assertTrue("The id of the first spectra should be", ids.contains("tic"));
        assertTrue("The id of the Four spectra should be", ids.contains("sic"));
    }

    @Test
    public void testGetChromatogramById() throws Exception {
        Chromatogram chromatogram = mzMLController.getChromatogramById("tic");
        assertTrue("The id of the chromatogram should be", chromatogram.getId().equals("tic"));
        assertEquals("CvTerm Scan List Scan Windows", chromatogram.getBinaryDataArrays().get(0).getDataProcessing().getProcessingMethods().get(0).getCvParams().get(0).getName(),"Conversion to mzML");
        mzMLController.close();
    }
}
