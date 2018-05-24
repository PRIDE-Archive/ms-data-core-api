package uk.ac.ebi.pride.utilities.data.io.file;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * @author Suresh Hewapathirana
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

//    @Test
//    public void doUnmarshell() {
//            Monitor monitor= MonitorFactory.start("uk.ac.ebi.pride.utilities.data.io.file.doUnmarshell");
//            this.fastValidateMzIdentMLUnmarshaller = new FastMzIdentMLUnmarshallerAdaptor(new File("<File full path here>"));
//            monitor.stop();
//            System.out.println("Performance INFO: --------------  " + monitor);
//    }

  @Test
  public void isSpectraDataReferencedByTitle() {
    Assert.assertFalse("Spectra of this MzIdentML is not referred by title", fastValidateMzIdentMLUnmarshaller.isSpectraDataReferencedByTitle(fastValidateMzIdentMLUnmarshaller.getSpectraData().get(0)));
  }

  @Test
  public void getVersion() {
    Assert.assertEquals("MzIdentML Version should be 1.1.0","1.1.0", fastValidateMzIdentMLUnmarshaller.getVersion());
  }


  @Test
  public void getCvList() {
    Assert.assertEquals("small.mzid file first CV in the CvList should be PSI-MS","PSI-MS", fastValidateMzIdentMLUnmarshaller.getCvList().get(0).getId());
  }

  @After
  public void tearDown() throws Exception {
    fastValidateMzIdentMLUnmarshaller.close();
  }
}