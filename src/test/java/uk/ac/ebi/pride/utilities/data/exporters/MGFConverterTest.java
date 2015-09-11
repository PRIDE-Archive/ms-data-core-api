package uk.ac.ebi.pride.utilities.data.exporters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.io.File;
import java.net.URL;

/**
 * Test the MGF exporter
 */
public class MGFConverterTest {

    private PrideXmlControllerImpl prideController = null;

    @Before
    public void setUp() throws Exception {
        URL url = MGFConverterTest.class.getClassLoader().getResource("test-pride.xml");
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
    public void testConvert() throws Exception {
        MGFConverter converter = new MGFConverter(prideController, "temp.mgf");
        converter.convert();
        File tempFile = new File("temp.mgf");
        if(tempFile.exists()){
            tempFile.delete();
        }
    }
}