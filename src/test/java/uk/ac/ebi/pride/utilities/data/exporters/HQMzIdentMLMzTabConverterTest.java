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
 * MzTab Controller Test
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 * @author Noemi del Toro
 */
public class HQMzIdentMLMzTabConverterTest {

    private MzIdentMLControllerImpl mzIdentMLController = null;


    @Before
    public void setUp() throws Exception {
        URL url = HQMzIdentMLMzTabConverterTest.class.getClassLoader().getResource("55merge_mascot_full.mzid");
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
        assertTrue("No errors reported during the conversion from MzIdentML to MzTab", checker.getErrorList().size() == 0);
    }

    @After
    public void tearDown() throws Exception {

    }
}
