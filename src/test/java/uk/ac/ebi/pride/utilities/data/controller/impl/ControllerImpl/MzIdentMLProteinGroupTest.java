package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.ProteinGroup;

import java.io.File;
import java.net.URL;

/**
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class MzIdentMLProteinGroupTest {

    private MzIdentMLControllerImpl mzIdentMlController = null;

    @Before
    public void setUp() throws Exception {
        URL url = MzIdentMlControllerImplTest.class.getClassLoader().getResource("small.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        mzIdentMlController = new MzIdentMLControllerImpl(inputFile, true);
    }

    @After
    public void tearDown() throws Exception {
        mzIdentMlController.close();
    }

    @Test
    public void testProteinGroup() throws Exception {
        ProteinGroup proteinGroup = mzIdentMlController.getProteinAmbiguityGroupById("PAG_hit_306");

        Assert.assertEquals(proteinGroup.getProteinDetectionHypothesis().size(), 1);
        Protein protein = proteinGroup.getProteinDetectionHypothesis().get(0);
        Assert.assertEquals(protein.getId(), "PDH_SPTN2_HUMAN_0");
        Assert.assertEquals(protein.getPeptides().size(), 2);

    }
}
