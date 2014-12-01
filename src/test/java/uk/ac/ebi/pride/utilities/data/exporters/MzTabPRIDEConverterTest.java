package uk.ac.ebi.pride.utilities.data.exporters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;


import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class MzTabPRIDEConverterTest {

    private PrideXmlControllerImpl prideController = null;


    @Before
    public void setUp() throws Exception {
        URL url = MzTabPRIDEConverterTest.class.getClassLoader().getResource("quant-pride.xml");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        prideController = new PrideXmlControllerImpl(inputFile);
    }

    @Test
    public void convertToMzTab(){
//       AbstractMzTabConverter mzTabconverter = new PRIDEMzTabConverter(prideController);
//       MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
//       MZTabFileConverter checker = new MZTabFileConverter();
//       checker.check(mzTabFile);
//       assertTrue("No errors reported during the conversion from PRIDE XML to MzTab", checker.getErrorList().size() == 0);
    }

    @After
    public void tearDown() throws Exception {

    }
}