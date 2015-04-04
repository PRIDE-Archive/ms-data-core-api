package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test PrideXmlControllerImpl
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class PrideXmlControllerImplTest {

    private PrideXmlControllerImpl prideController = null;

    @Before
    public void setUp() throws Exception {
        URL url = PrideXmlControllerImplTest.class.getClassLoader().getResource("test-pride.xml");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        prideController = new PrideXmlControllerImpl(inputFile);
    }

    @After
    public void tearDown() throws Exception {
        prideController.close();
    }

    @Test
    public void testGetCvLookups() throws Exception {
        List<CVLookup> cvs = prideController.getCvLookups();
        assertTrue("There should be only one cv lookup", cvs.size()==1);
        assertEquals("CV label should be PSI", cvs.get(0).getCvLabel(), "PSI");
    }

    @Test
    public void testGetFileDescription() throws Exception {
        //FileDescription fileDesc = prideController.getFileDescription();
        //assertEquals("File content", fileDesc.getFileContent().getCvParams().get(0).getAccession(), "MS:1000294");
    }

    @Test
    public void testGetSamples() throws Exception {
        List<Sample> samples = prideController.getSamples();
        assertTrue("There should be only one sample", samples.size() == 1);
        assertEquals("Sample ID should always be sample1", samples.get(0).getId(), "sample1");
        assertEquals("Sample cv param should be lung", samples.get(0).getCvParams().get(0).getName(), "lung");
    }

    @Test
    public void testGetSoftware() throws Exception {
        List<Software> software = prideController.getSoftwares();
        assertTrue("There should be only one software", software.size() == 1);
        assertEquals("Software ID should be Xcalibur", software.get(0).getName(), "Xcalibur");
        assertEquals("Software version should be 1.2 SP1", software.get(0).getVersion(), "1.2 SP1");
    }

    @Test
    public void testGetInstruments() throws Exception {
        List<InstrumentConfiguration> instrumentConfigurations = prideController.getInstrumentConfigurations();
        assertTrue("There should be only one instrument configuration", instrumentConfigurations.size() == 1);
        assertEquals("Source should contain Electrospray Ionization", instrumentConfigurations.get(0).getSource().get(0).getCvParams().get(0).getName(), "Electrospray Ionization");
        assertEquals("Analyzer should contain Ion Trap", instrumentConfigurations.get(0).getAnalyzer().get(0).getCvParams().get(0).getName(), "Ion Trap");
        assertEquals("Detector should contain Electron Multiplier Tube", instrumentConfigurations.get(0).getDetector().get(0).getCvParams().get(0).getName(), "Electron Multiplier Tube");
    }

    @Test
    public void testGetDataProcessings() throws Exception {
        List<DataProcessing> dataProcs = prideController.getDataProcessings();
        assertTrue("There should be only one data processing", dataProcs.size() == 1);
        assertEquals("Auto-generated data processing id should be dataprocess1", dataProcs.get(0).getId(), "dataprocessing1");
        assertTrue("There should be only on processing method", dataProcs.get(0).getProcessingMethods().size() == 1);
        assertEquals("Processing method's software id should Xcalibur", dataProcs.get(0).getProcessingMethods().get(0).getSoftware().getName(), "Xcalibur");
        assertEquals("Processing method should contain cv term PSI:1000035", dataProcs.get(0).getProcessingMethods().get(0).getCvParams().get(0).getAccession(), "PSI:1000035");
    }

    @Test
    public void testGetMetaData() throws Exception {
        ExperimentMetaData experiment = prideController.getExperimentMetaData();

        // test additional param
        List<CvParam> additional = experiment.getCvParams();
        assertTrue("There should be only two additional cv parameters", additional.size()==2);
        assertEquals("XML generation software accession should be PRIDE:0000175", additional.get(0).getAccession(), "PRIDE:0000175");

        // test references
        List<Reference> references = experiment.getReferences();
        assertTrue("There should be only one reference", references.size()==2);
        assertEquals("PubMed number should be 16038019", references.get(0).getCvParams().get(0).getAccession(), "16038019");

        // test protocol
        ExperimentProtocol protocol = experiment.getProtocol();
        assertEquals("Protocol name is In Gel Protein Digestion", protocol.getName(), "In Gel Protein Digestion");
        assertEquals("First protocol step is reduction", protocol.getProtocolSteps().get(0).getCvParams().get(0).getName(), "Reduction");

        // test version
        assertEquals("Version should be 2.1", experiment.getVersion(), "2.1");
    }

    @Test
    public void testGetSpectrumIds() throws Exception {
        assertTrue("The number of spectrum should be 100", prideController.getSpectrumIds().size() == 100);
    }

    @Test
    public void testGetSpectrumById() throws Exception {
        Spectrum spectrum = prideController.getSpectrumById("2");

        // test spectrum index
        //assertEquals("Spectrum index should be 1", spectrum.getIndex(), 1);

        // test param group
        assertEquals("MS level should be 0", spectrum.getCvParams().get(0).getValue(), "0");
        assertEquals("Spectrum type", spectrum.getCvParams().get(1).getAccession(), "MS:1000294");
        assertEquals("Spectrum representation", spectrum.getCvParams().get(2).getAccession(), "MS:1000127");
        assertEquals("Scan mode", spectrum.getCvParams().get(3).getAccession(), "PSI:1000036");

        // test scan list
        ScanList scanList = spectrum.getScanList();
        // check param group
        assertEquals("Method of combination", scanList.getCvParams().get(0).getAccession(), "MS:1000571");
        //
        // check scans
        assertTrue("There should be two scans", scanList.getScans().size() == 2);
        assertEquals("ScanWindow upper limit", scanList.getScans().get(0).getScanWindows().get(0).getCvParams().get(1).getValue(), "123.45");
        assertEquals("Scan param group", scanList.getScans().get(0).getCvParams().get(0).getValue(), "Zero Value");

        // test precursor
        assertTrue("There should be only one precursor", spectrum.getPrecursors().size() == 1);
        assertEquals("Precursor spectrum ref", spectrum.getPrecursors().get(0).getSpectrum().getId().toString(), "0");
        assertEquals("Precursor ion selection", spectrum.getPrecursors().get(0).getSelectedIons().get(0).getCvParams().get(0).getAccession(), "PSI:1000041");

        // test binary array
        assertEquals("Mz Binary array precision", spectrum.getMzBinaryDataArray().getCvParams().get(0).getAccession(), "MS:1000523");
        assertEquals("Mz binary array compression", spectrum.getMzBinaryDataArray().getCvParams().get(1).getAccession(), "MS:1000576");
        prideController.close();
        
    }
}
