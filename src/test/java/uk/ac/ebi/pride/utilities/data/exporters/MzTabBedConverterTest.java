package uk.ac.ebi.pride.utilities.data.exporters;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.PeptideEvidence;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.UserParam;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * MzTab to Bed Converter Test
 * This class tests converting an mzIdentML file with annotated chromosome information into mzTab,
 * and then testing the mzTab into the bed format.
 *
 * @author Tobias Ternent
 * @author Yasset Perez-Riverol
 */
public class MzTabBedConverterTest {

    private File mzTabFile = null;
    private File mzIdentMLFile = null;

    /**
     * Sets up the input annotated mzIdentML and previously generated mzTab files to test.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        URL url = MzTabBedConverterTest.class.getClassLoader().getResource("PXD000764_34937_combined_fdr.mztab");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        mzTabFile = new File(url.toURI());

        url = MzTabBedConverterTest.class.getClassLoader().getResource("PXD000764_34937_combined_fdr.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        mzIdentMLFile = new File(url.toURI());
    }

    /**
     * Converts the annotated mzIdentML into mzTab.
     *
     * @throws Exception
     */
    @Test
    public void convertAnnotatedMzIdentMLMzTab() throws Exception {
        MzIdentMLControllerImpl mzIdentMLController = new MzIdentMLControllerImpl(mzIdentMLFile);
        MzIdentMLMzTabConverter mzTabconverter = new MzIdentMLMzTabConverter(mzIdentMLController);
        MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
        MZTabFileConverter checker = new MZTabFileConverter();
        File temp = File.createTempFile("PXD000764_34937_mztab_test", ".tmp");
        TestCase.assertTrue("No errors reported during the conversion from mzIdentML to MzTab", checker.getErrorList().size() == 0);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(temp));
        mzTabFile.printMZTab(bufferedOutputStream);
        bufferedOutputStream.close();
        mzIdentMLController.close();

        boolean result = false;
        MzTabControllerImpl mzTabController = new MzTabControllerImpl(temp);
        chromCheck : {
            for (Comparable proteinID : mzTabController.getProteinIds()) {
                Protein protein = mzTabController.getProteinById(proteinID);
                ArrayList<PeptideEvidence> evidences = new ArrayList<>();
                for (Peptide peptide : protein.getPeptides()) {
                    for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                        if (!evidences.contains(peptide.getPeptideEvidence())) {
                            evidences.add(peptide.getPeptideEvidence());
                            for (UserParam userParam : peptideEvidence.getUserParams()) {
                                if (userParam.getName().equalsIgnoreCase("chr")) {
                                    result = true;
                                    break chromCheck;
                                }
                            }
                        }
                    }
                }
            }
        }
        mzTabController.close();
        assertTrue("No errors reported during the conversion from annotated mzIdentML to MzTab", result);
        temp.deleteOnExit();
    }

    /**
     * Converts the mzTab file with chromosome information into the bed format.
     *
     * @throws Exception
     */
    @Test
    public void convertMzTabBed() throws Exception{
        MzTabControllerImpl mzTabController = new MzTabControllerImpl(mzTabFile);
        MzTabBedConverter mzTabBedConverter = new MzTabBedConverter(mzTabController);
        File temp = File.createTempFile("PXD000764_34937_20150326_bed_test", ".tmp");
        mzTabBedConverter.convert(temp);
        mzTabController.close();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(temp));
        String firstLine = bufferedReader.readLine();
        final boolean test = firstLine != null && firstLine.startsWith("CHR_HSCHR6_MHC_QBL_CTG1\t31991945\t31991963");
        assertTrue("Chromosome information present in generated bed file", test);
        temp.deleteOnExit();
    }

    /**
     * Temporary files are already flagged to be deleted when finished, controllers and readers are already closed.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {

    }
}
