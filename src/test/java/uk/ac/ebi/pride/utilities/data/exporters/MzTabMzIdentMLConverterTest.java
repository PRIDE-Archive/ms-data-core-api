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
 * MzTab Converter of mzIdentML files. The class allow to convert mzIdentML
 * files to mzTab including the protein groups and PSMs.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class MzTabMzIdentMLConverterTest {

    private MzIdentMLControllerImpl mzIdentMLController = null;


    @Before
    public void setUp() throws Exception {
        URL url = MzTabPRIDEConverterTest.class.getClassLoader().getResource("55merge_mascot_full.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        mzIdentMLController = new MzIdentMLControllerImpl(inputFile);
    }

    @Test
    public void convertToMzTab() throws IOException {
       AbstractMzTabConverter mzTabconverter = new MzIdentMLMzTabConverter(mzIdentMLController);
       MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
       MZTabFileConverter checker = new MZTabFileConverter();
       checker.check(mzTabFile);
       assertTrue("No errors reported during the conversion from MzIdentML to MzTab", checker.getErrorList().size() == 0);
    }

    @After
    public void tearDown() throws Exception {

    }
}
