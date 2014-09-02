package uk.ac.ebi.pride.utilities.data.controller.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideDBAccessControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * @author ypriverol
 * Date: 15/09/11
 * Time: 17:20
 */
public class PrideDataBaseControllerImplTest {

    private PrideDBAccessControllerImpl prideController = null;

    @Before
    public void setUp() throws Exception {
        prideController = new PrideDBAccessControllerImpl("10885");
    }

    @After
    public void tearDown() throws Exception {
        prideController.close();
    }

    @Test
    public void testGetCvLookups() throws Exception {
        List<CVLookup> cvs = prideController.getCvLookups();
        assertTrue("There should be only one cv lookup", cvs.size() == 1);
        assertEquals("CV label should be PSI", cvs.get(0).getCvLabel(), "PSI");
    }

    @Test
    public void testGetAdditionals() throws Exception {
        ParamGroup additionals = prideController.getAdditional();
        assertTrue("The number of CvTermns Should be 4:", additionals.getCvParams().size() == 4);
        assertEquals("The accession of the first CvTerm should be PRIDE:0000175", additionals.getCvParams().get(0).getAccession(), "PRIDE:0000175");
        assertEquals("The name of the four CvTerm should be Experiment description", additionals.getCvParams().get(3).getName(), "Experiment description");
    }

    @Test
    public void testGetSamples() throws Exception {
        List<Sample> samples = prideController.getSamples();
        assertTrue("There should be only one sample", samples.size() == 1);
        assertEquals("Sample ID should always be sample1", samples.get(0).getId(), "sample1");
        assertEquals("Sample cv param should be iTRAQ4plex-114 reporter+balance reagent derivatized residue", samples.get(0).getCvParams().get(0).getName(), "iTRAQ4plex-114 reporter+balance reagent derivatized residue");
    }

    @Test
    public void testGetSoftware() throws Exception {
        List<Software> software = prideController.getSoftwares();
        assertTrue("There should be only one software", software.size() == 1);
        assertEquals("Software ID should be MassLynx", software.get(0).getName(), "MassLynx");
        assertEquals("Software version should be 4.0", software.get(0).getVersion(), "4.0");
    }

    @Test
    public void testGetInstruments() throws Exception {
        List<InstrumentConfiguration> instrumentConfigurations = prideController.getInstrumentConfigurations();
        assertTrue("There should be only one instrument configuration", instrumentConfigurations.size() == 1);
        assertEquals("Source should contain Electrospray Ionization", instrumentConfigurations.get(0).getSource().get(0).getCvParams().get(0).getName(), "Electrospray Ionization");
        assertEquals("Detector should contain Microchannel Plate Detector", instrumentConfigurations.get(0).getDetector().get(0).getCvParams().get(0).getName(), "Microchannel Plate Detector");
        assertEquals("The Instrument Global Configuration QToF Global", instrumentConfigurations.get(0).getCvParams().get(0).getValue(), "QToF Global");
    }

    @Test
    public void testGetDataProcessings() throws Exception {
        List<DataProcessing> dataProcs = prideController.getDataProcessings();
        assertTrue("There should be only one data processing", dataProcs.size() == 1);
        assertEquals("Auto-generated data processing id should be dataprocess1", dataProcs.get(0).getId(), "dataprocessing1");
        assertTrue("There should be only on processing method", dataProcs.get(0).getProcessingMethods().size() == 1);
        assertEquals("Processing method's software id should MassLynx", dataProcs.get(0).getProcessingMethods().get(0).getSoftware().getName(), "MassLynx");
        assertEquals("Processing method should contain first Cv term Deisotoping", dataProcs.get(0).getProcessingMethods().get(0).getCvParams().get(0).getName(), "Deisotoping");
    }

    @Test
    public void testGetMetaData() throws Exception {
        ExperimentMetaData experiment = prideController.getExperimentMetaData();

        // test references
        //assertTrue("There should be only one reference", references.size()==0);
        //assertEquals("PubMed number should be 20213678", references.get(0).getCvParams().get(0).getAccession(), "20213678");

        // test protocol
        ExperimentProtocol protocol = experiment.getProtocol();
        assertEquals("Protocol name is iTRAQ", protocol.getName(), "iTRAQ");
        assertEquals("First protocol step is methyl methanethiosulfonate", protocol.getProtocolSteps().get(0).getCvParams().get(0).getName(), "methyl methanethiosulfonate");

        // test version
        assertEquals("Version should be 2.1", experiment.getVersion(), "2.1");
    }

    @Test
    public void testGetSpectrumIds() throws Exception {
        assertTrue("The number of spectrum should be 3099", prideController.getSpectrumIds().size() == 3099);
    }

//    @Test
//    public void testGetSpectrumById() throws Exception {
//
//        //119973254
//        Spectrum spectrum = prideController.getSpectrumById("1002");
//
//        // test spectrum index
//        //assertEquals("Spectrum index should be 1", spectrum.getIndex(), 1);
//
//        // test param group
//        assertEquals("MS level should be 2", spectrum.getCvParams().get(0).getValue(), "2");
//        assertEquals("Spectrum type", spectrum.getCvParams().get(1).getAccession(), "MS:1000294");
//
//
//        // test scan list
//        ScanList scanList = spectrum.getScanList();
//        // check param group
//        assertEquals("Method of combination MS:1000795", scanList.getCvParams().get(0).getAccession(), "MS:1000795");
//        //
//        // check scans
//        assertTrue("There should be two scans", scanList.getScans().size() == 1);
//        assertEquals("ScanWindow upper limit", scanList.getScans().get(0).getScanWindows().get(0).getCvParams().get(0).getValue(), "101.089700");
//
//
//        // test precursor
//        assertTrue("There should be only one precursor", spectrum.getPrecursors().size() == 1);
//        assertEquals("Precursor ion selection", spectrum.getPrecursors().get(0).getSelectedIons().get(0).getCvParams().get(0).getAccession(), "PSI:1000041");
//
//        // test binary array
//        assertEquals("Mz Binary array precision", spectrum.getMzBinaryDataArray().getCvParams().get(0).getAccession(), "MS:1000523");
//        assertEquals("Mz binary array compression", spectrum.getMzBinaryDataArray().getCvParams().get(1).getAccession(), "MS:1000576");
//
//    }


}
