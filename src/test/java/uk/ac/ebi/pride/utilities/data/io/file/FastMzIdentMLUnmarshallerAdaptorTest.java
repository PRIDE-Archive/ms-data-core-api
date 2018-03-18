package uk.ac.ebi.pride.utilities.data.io.file;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;

/**
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLUnmarshallerAdaptorTest {

    FastMzIdentMLUnmarshallerAdaptor fastValidateMzIdentMLUnmarshaller;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void doUnmarshell() {
            Monitor monitor= MonitorFactory.start("uk.ac.ebi.pride.utilities.data.io.file.doUnmarshell");
            this.fastValidateMzIdentMLUnmarshaller = new FastMzIdentMLUnmarshallerAdaptor(new File("/Users/hewapathirana/Downloads/F238646.mzid"));
            monitor.stop();
            System.out.println("Performance INFO: --------------  " + monitor);
    }

    @After
    public void tearDown() throws Exception {
    }
}