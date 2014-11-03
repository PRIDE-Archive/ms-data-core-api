package uk.ac.ebi.pride.utilities.data.exporters;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;

import java.io.File;
import java.net.URL;

/**
 * MzTab Controller Test
 * @author ypriverol
 * @author rwang
 */
public class MzTabMzIdentMLConverterTest {

    private MzIdentMLControllerImpl prideController = null;


    @Before
    public void setUp() throws Exception {
        URL url = MzTabPRIDEConverterTest.class.getClassLoader().getResource("small.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        prideController = new MzIdentMLControllerImpl(inputFile);
    }

    @Test
    public void convertToMzTab(){
       // AbstractMzTabConverter mzTabconverter = new MzIdentMLMzTabConverter(prideController);
       // MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
       // MZTabFileConverter checker = new MZTabFileConverter();
       // checker.check(mzTabFile);
       // TestCase.assertTrue("No errors reported during the conversion from PRIDE XML to MzTab", checker.getErrorList().size() == 0);
    }

    @After
    public void tearDown() throws Exception {

    }
}
