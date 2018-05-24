package uk.ac.ebi.pride.utilities.data.io.file;

import org.junit.*;

import java.io.File;
import java.net.URL;

/**
 * @author Suresh Hewapathirana
 */

/**
 * This class only do few testing to verify that mzIdentML unmarshalling works fine.
 * Rest of the other methods are tested by FastMzIdentMLControllerTest class.
 */
public class FastMzIdentMLUnmarshallerAdaptorTest {

    FastMzIdentMLUnmarshallerAdaptor fastValidateMzIdentMLUnmarshaller;

    @Before
    public void setUp() throws Exception {
      URL url = FastMzIdentMLUnmarshallerAdaptorTest.class.getClassLoader().getResource("small.mzid");
      URL urlMgf = FastMzIdentMLUnmarshallerAdaptorTest.class.getClassLoader().getResource("small.mgf");

      if (url == null || urlMgf == null) {
        throw new IllegalStateException("no file for input found!");
      }
      File inputFile = new File(url.toURI());
      this.fastValidateMzIdentMLUnmarshaller = new FastMzIdentMLUnmarshallerAdaptor(inputFile);
    }

  /**
   * Test few functions to see unmarshalling works fine
   */
  @Test
  public void testFastMzIdentMLUnmarshalling() {
    Assert.assertEquals("MzIdentML Version should be 1.1.0","1.1.0", fastValidateMzIdentMLUnmarshaller.getVersion());
    Assert.assertEquals("small.mzid file first CV in the CvList should be PSI-MS","PSI-MS", fastValidateMzIdentMLUnmarshaller.getCvList().get(0).getId());
    Assert.assertFalse("Spectra of this MzIdentML is not referred by title", fastValidateMzIdentMLUnmarshaller.isSpectraDataReferencedByTitle(fastValidateMzIdentMLUnmarshaller.getSpectraData().get(0)));
  }

  @After
  public void tearDown() throws Exception {
    fastValidateMzIdentMLUnmarshaller.close();
  }
}