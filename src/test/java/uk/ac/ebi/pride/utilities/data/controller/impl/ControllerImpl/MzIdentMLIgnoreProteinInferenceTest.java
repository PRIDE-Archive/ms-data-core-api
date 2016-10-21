package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tesing Protein Inference for ms-data-core-api
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class MzIdentMLIgnoreProteinInferenceTest {
    private MzIdentMLControllerImpl mzIdentMlController = null;

    @Before
    public void setUp() throws Exception {
        URL url = MzIdentMlControllerImplTest.class.getClassLoader().getResource("55merge_mascot_full.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        mzIdentMlController = new MzIdentMLControllerImpl(inputFile, true,true);
    }

    @After
    public void tearDown() throws Exception {
        mzIdentMlController.close();
    }


    @Test
    public void testGetSamples() throws Exception {
        List<Sample> samples = mzIdentMlController.getSamples();
        assertTrue("There should be only one sample", samples.size() == 2);
        assertEquals("Sample ID should always be sample1", samples.get(0).getId(), "sample1");
        assertEquals("Sample cv param should be ", samples.get(0).getCvParams().get(0).getName(), "name31");
    }

    @Test
    public void testGetSoftware() throws Exception {
        List<Software> software = mzIdentMlController.getSoftwares();
        assertTrue("There should be only one software", software.size() == 2);
        assertEquals("Software ID should be Mascot Server", software.get(0).getName(), "Mascot Server");
        assertEquals("Software version should be 2.3.3.0 for the second software", software.get(1).getVersion(), "2.3.3.0");
    }

    @Test
    public void testGetMetaData() throws Exception {
        ExperimentMetaData experiment = mzIdentMlController.getExperimentMetaData();

        // test references
        List<Reference> references = experiment.getReferences();
        assertTrue("There should be only one reference", references.size()==1);
        assertEquals("PubMed number should be 16038019", references.get(0).getDoi(), "10.1002/(SICI)1522-2683(19991201)20:18<3551::AID-ELPS3551>3.0.CO;2-2");

        // test version
        assertEquals("Version should be 1.1.0", experiment.getVersion(), "1.1.0");
        // test name
        assertEquals("The name of the file should be PSI Example File", experiment.getName(), "PSI Example File");

        // test the Provider of the File
        assertEquals("The id of the Provider should be person2",(experiment.getProvider().getContact()).getId(),"person2");
        assertEquals("The role of the Provider should be researcher",experiment.getProvider().getRole().getName(),"researcher");

    }

    @Test
    public void testGetPersonContacts() throws Exception{
        List<Person> persons = mzIdentMlController.getPersonContacts();
        assertTrue("There should be only two persons", persons.size() == 2);
        assertEquals("Person one ID should be person1", persons.get(0).getId(), "person1");
        assertEquals("Person two last Name should be Perez-Riverol", persons.get(1).getLastname(), "Perez-Riverol");
        assertEquals("Affiliation for Person two should be Matrix Science Limited", persons.get(1).getAffiliation().get(0).getName(),"Matrix Science Limited");
    }

    @Test
    public void testGetOrganizationContacts() throws Exception{
        List<Organization> organizations = mzIdentMlController.getOrganizationContacts();
        assertTrue("There should be only two organizations", organizations.size() == 2);
        assertEquals("Organization one ID should be ORG_MSL", organizations.get(0).getId(), "ORG_MSL");
        assertEquals("Organization two Parent Organization Name should be Matrix Science Limited", organizations.get(1).getParentOrganization().getName(), "Matrix Science Limited");
    }

    @Test
    public void testGetIdentificationMetaData() throws Exception {
        IdentificationMetaData experiment = mzIdentMlController.getIdentificationMetaData();

        // test SearchDatabase
        List<SearchDataBase> databases = experiment.getSearchDataBases();
        assertTrue("There should be only one database", databases.size()==1);
        assertEquals("The name of hte database should be", databases.get(0).getName(), "NeoProt_tripledecoy");
        assertEquals("The CvTerm Name for the File format should be", databases.get(0).getFileFormat().getName(),"FASTA format");
        assertEquals("The name of the File in user Params should be", databases.get(0).getNameDatabase().getUserParams().get(0).getName(),"Neo_rndTryp_3times.fasta");
        assertTrue("The number of sequences should be 22348",databases.get(0).getNumDatabaseSequence()==22348);

        // test SpectrumIdentificationProtocol
        List<SpectrumIdentificationProtocol> spectrumIdentificationProtocol = experiment.getSpectrumIdentificationProtocols();

        assertEquals("The Enzyme Name should be", spectrumIdentificationProtocol.get(0).getEnzymes().get(0).getEnzymeName().getCvParams().get(0).getName(), "Trypsin");
        assertEquals("The mass for the Lysine Residue should be", spectrumIdentificationProtocol.get(0).getMassTables().get(0).getResidues().get("K"), new Float(128.09496));
        assertEquals("The Filter Used in the Search Process should be DB filter taxonomy",spectrumIdentificationProtocol.get(0).getFilters().get(0).getFilterType().getCvParams().get(0).getName(),"DB filter taxonomy");

        Protocol proteinDetectionProtocol = experiment.getProteinDetectionProtocol();

        // test Protein Detection Protocol
        assertEquals("The name of the software used should be Mascot Parser",proteinDetectionProtocol.getAnalysisSoftware().getName(), "Mascot Parser");
        //assertEquals("The role of the Provider should be researcher",experiment.getProvider().getRole().getName(),"researcher");
        assertEquals("Protein Detection Protocol Id should be", proteinDetectionProtocol.getId(), "PDP_MascotParser_1");

    }

    @Test
    public void testGetIdentificationIDs() throws DataAccessException {
        List<Comparable> identifications = new ArrayList<Comparable>(mzIdentMlController.getProteinIds());
        assertTrue("The numer of Identification should be 2", identifications.size()==2044);
    }

}
