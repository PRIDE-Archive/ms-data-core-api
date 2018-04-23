package uk.ac.ebi.pride.utilities.data.io.file;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import uk.ac.ebi.jmzidml.xml.jaxb.unmarshaller.filters.MzIdentMLNamespaceFilter;
import uk.ac.ebi.pride.utilities.data.lightModel.MzIdentML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This class converts mzIdentML XML elements into Java objects(so-called unmarshalling) using Java
 * Architecture for XML Binding (JAXB). JAXB can be used with two approaches, which are namely DOM
 * and SAX parsers. Unlike DOM, SAX does not construct a tree of objects in the memory and SAX is
 * faster. For this implementation, SAX parser has been used.
 *
 * <p>This class designed according to Singleton design pattern. Therefore, mzIdentML object cannot
 * be directly created from the constructor, but with the getInstance public method.
 *
 * @author Suresh Hewapathirana
 */
@Slf4j
public class FastMzIdentMLUnmarshaller {

  private MzIdentML mzIdentML = null;
  private JAXBContext jaxbContext;
  private static volatile FastMzIdentMLUnmarshaller fastMzIdentMLUnmarshaller = null;

  @SuppressWarnings("unchecked")
  private FastMzIdentMLUnmarshaller(File mzIdentMLFile) {
    try {
      if (mzIdentMLFile.exists()) {
        if (mzIdentML == null) {
          InputSource inputSource = new InputSource(new FileInputStream(mzIdentMLFile));
          // required for the addition of namespaces to top-level objects
          MzIdentMLNamespaceFilter xmlFilter = new MzIdentMLNamespaceFilter();
          // Lazy caching of the JAXB Context.
          if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(MzIdentML.class.getPackage().getName());
          }
          // create unmarshaller(convert XML to Java objects)
          Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
          // Create an XMLReader to use with our filter
          XMLReader xmlReader = XMLReaderFactory.createXMLReader();
          xmlFilter.setParent(xmlReader);
          // Create a SAXSource specifying the filter
          SAXSource source = new SAXSource(xmlFilter, inputSource);
          // Do unmarshalling
          JAXBElement<MzIdentML> mzIdentMLJAXBElement =
              (JAXBElement<MzIdentML>) unmarshaller.unmarshal(source);
          // get mzIdentML Java object
          this.mzIdentML = mzIdentMLJAXBElement.getValue();
          log.debug("mzIdentML Unmarshalling completed!");
        }
      }
    } catch (JAXBException e) {
      log.error("UnmarshallerFactory.initializeUnmarshaller", e);
      throw new IllegalStateException("Could not initialize unmarshaller!");
    } catch (SAXException e) {
      log.error("UnmarshallerFactory.initializeUnmarshaller", e);
      throw new IllegalStateException("Could not initialize unmarshaller!");
    } catch (FileNotFoundException e) {
      log.error("UnmarshallerFactory.initializeUnmarshaller", e);
      throw new IllegalStateException("Input mzIdentML file reading error!");
    }
  }

  /**
   * This is the method should use to instantiate fastMzIdentMLUnmarshaller object. This method is
   * thread safe and also do a lazy loading because of two reasons: 1. XML parsing from mzIdentML
   * file is a memory intensive task 2. MzIdentML is an object with high memory consumption
   *
   * @param mzIdentMLFile Input MzIdentML file with .mzid extension
   * @return FastMzIdentMLUnmarshaller
   */
  public static FastMzIdentMLUnmarshaller getInstance(File mzIdentMLFile) {
    try {
      if (fastMzIdentMLUnmarshaller == null) {
        synchronized (FastMzIdentMLUnmarshaller.class) {
          if (fastMzIdentMLUnmarshaller == null) {
            fastMzIdentMLUnmarshaller = new FastMzIdentMLUnmarshaller(mzIdentMLFile);
          }
        }
      }
    } catch (Exception e) {
      log.error("mzIdentML file access error : " + e.getMessage());
    }
    return fastMzIdentMLUnmarshaller;
  }

  /**
   * Gets the value of the mzIdentML property.
   *
   * @return MzIdentML
   */
  public MzIdentML getMzIdentML() {
    return this.mzIdentML;
  }

  /** Close data access controller by clearing the entire mzIdentML Object */
  public void destroy() {
    this.mzIdentML = null;
  }
}
