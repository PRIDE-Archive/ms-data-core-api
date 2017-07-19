package uk.ac.ebi.pride.utilities.data.exporters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;

import java.io.*;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 *  The filtering when exporting a mzIdentML to mzTab is done follows the next set of rules:
 *
 *      If there is not protein detection protocol in mzIdentML (e. g. no ambiguity groups provided) or there is not threshold define in the protein detection protocol:
 *          -The filtering can not be done at protein level directly. In this case is needed to look into the spectrum identification protocol.
 *              -If there is no threshold available at spectrum identification protocol
 *                  The spectra is filtered using rank information. Only spectrum with rank one pass the filter
 *              -If there is a threshold available at spectrum identification protocol
 *                  The spectra is filtered using using the provided threshold
 *          -Only the proteins whose spectra remain after the filtering will be kept.
 *      If there is protein detection protocol in mzIdentML the proteins and protein groups will be filtered according to threshold first.
 *               - After that the filtering by threshold at peptide level will be applied, because in the worst case scenario it will remove only proteins without spectra evidence that pass the filter.
 *               Before NoPeptideFilter was used to avoid inconsistencies with the protein filter, however was observed that some spectra evidences that did not pass the threshold were
 *               included because the threshold was provided but was incorrectly annotated in the file as NoThresholdAvailable. This option minimized the inclusion of spectra under the threshold.
 *      If there is no threshold information at protein or peptide level available
 *           -The spectra is filtered using rank information. Only spectrum with rank one pass the filter
 *           -Only the proteins whose spectra remain after the filtering will be kept.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 * @author Noemi del Toro
 */
public class HQMzIdentMLMzTabConverterTest {

    private MzIdentMLControllerImpl mzIdentMLController = null;


    @Before
    public void setUp() throws Exception {
        URL url = HQMzIdentMLMzTabConverterTest.class.getClassLoader().getResource("20110827_K1_A (K1A).mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
//        File inputFile = new File("/Users/ntoro/Desktop/mzTabs/OIS_3d_Protein_LABELSWAP.mzid-pride-filtered.xml");
        mzIdentMLController = new MzIdentMLControllerImpl(inputFile);
    }

    @Test
    public void convertToMzTab() throws IOException {
        AbstractMzTabConverter mzTabconverter = new HQMzIdentMLMzTabConverter(mzIdentMLController);
        MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
        MZTabFileConverter checker = new MZTabFileConverter();
        checker.check(mzTabFile);
        File tmpFile = File.createTempFile("tmeFile", "mztab");
        mzTabFile.printMZTab(new BufferedOutputStream(new FileOutputStream(tmpFile)));
        assertTrue("No errors reported during the conversion from MzIdentML to MzTab", checker.getErrorList().size() == 0);
        tmpFile.deleteOnExit();
    }

    @After
    public void tearDown() throws Exception {

    }
}
