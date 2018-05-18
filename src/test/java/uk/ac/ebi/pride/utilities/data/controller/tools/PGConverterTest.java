package uk.ac.ebi.pride.utilities.data.controller.tools;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

/**
 * This class tests few possible command-line commands just to make sure integration of PGConverter
 * and MzTabConverterTool works fine
 *
 * @author Suresh Hewapathirana
 */
@Slf4j
public class PGConverterTest {

  /**
   * Test printing error code used in the API
   *
   * <p>This should print "Cause: A URI pointing to the unit's source data, (e.g. a PRIDE experiment
   * or a PeptideAtlas build). For example, "MTD PRIDE_1234-uri
   * http://www.ebi.ac.uk/pride/url/to/experiment".
   */
  @Test
  public void PrintErrorCodeTest() {

    String[] args = new String[] {"-" + ARG_ERROR_CODE, "-" + ARG_CODE, "1015"};
    PGConverter.main(args);
  }

  /**
   * This method should print the Help menu. This can be used to quickly check the commands and also
   * check the formatting of the help menu
   */
  @Test
  public void PrintHelpTest() {
    String[] args = new String[] {"-h"};
    PGConverter.main(args);
  }

  /**
   * This test validates one example mzIdentML file which is related to a single peak .mgf file
   * (without schema validation).
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testMzidValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.mzid");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    url = ConverterTest.class.getClassLoader().getResource("test.mgf");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMgfFile = new File(url.toURI());
    File reportFile = File.createTempFile("testMzid", ".log");
    String[] args =
        new String[] {
          "-" + ARG_VALIDATION,
          "-" + ARG_MZID,
          inputMzidFile.getPath(),
          "-" + ARG_PEAK,
          inputMgfFile.getPath(),
          "-" + ARG_SKIP_SERIALIZATION,
//          "-" + ARG_SCHEMA_VALIDATION,
          "-" + ARG_REPORTFILE,
          reportFile.getPath()
        };
    PGConverter.main(args);
  }

  /**
   * This test validates one example MzTab file which is related to a single peak .mgf file
   * (without schema validation).
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testMzTabValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.mztab");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    url = ConverterTest.class.getClassLoader().getResource("test.mgf");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMgfFile = new File(url.toURI());
    File reportFile = File.createTempFile("testMzid", ".log");
    String[] args =
            new String[] {
                    "-" + ARG_VALIDATION,
                    "-" + ARG_MZTAB,
                    inputMzidFile.getPath(),
                    "-" + ARG_PEAK,
                    inputMgfFile.getPath(),
                    "-" + ARG_SKIP_SERIALIZATION,
//          "-" + ARG_SCHEMA_VALIDATION,
                    "-" + ARG_REPORTFILE,
                    reportFile.getPath()
            };
    PGConverter.main(args);
  }



  /**
   * This test validates one example mzIdentML file which is related to a single peak .mgf file
   * with FastMzIdentMLValidation approach (instead of random access file by xxindex)
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testMzidFastValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("small.mzid");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    url = ConverterTest.class.getClassLoader().getResource("small.mgf");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMgfFile = new File(url.toURI());
    File reportFile = File.createTempFile("testMzid", ".log");
    String[] args =
            new String[] {
                    "-" + ARG_VALIDATION,
                    "-" + ARG_MZID,
                    inputMzidFile.getPath(),
                    "-" + ARG_PEAK,
                    inputMgfFile.getPath(),
//                    "-" + ARG_SCHEMA_VALIDATION,
                    "-" + ARG_SKIP_SERIALIZATION,
                    "-" + ARG_FAST_VALIDATION,
                    "-" + ARG_REPORTFILE,
                    reportFile.getPath()
            };
    PGConverter.main(args);
  }

  /**
   * Check the validity of the mzTab file
   *
   * @throws Exception
   */
  @Test
  public void CheckTest() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.mztab");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    String[] args = new String[] {"-" + ARG_CHECK, "-" + ARG_INPUTFILE, inputMzidFile.getPath()};
    PGConverter.main(args);
  }

  /**
   * Convert the MzIdentML to MzTab format using MzTabConverterTool implementation Here, no High
   * Quality Filters will be applied while converting the input result file
   *
   * @throws Exception
   */
  @Test
  public void ConvertTest() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("carb.mzid");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    String[] args =
        new String[] {
          "-" + ARG_CONVERT,
          "-" + ARG_INPUTFILE,
          inputMzidFile.getPath(),
          "-" + ARG_FORMAT,
          "MZIDENTML"
        };
    PGConverter.main(args);
  }

  @Test
  public void testPrideXMLValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test-pride.xml");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }

    File inputPrideXMLFile = new File(url.toURI());
    File reportFile = File.createTempFile("testpridexml", ".log");
    String[] args =
        new String[] {
          "-" + ARG_VALIDATION,
          "-" + ARG_PRIDEXML,
          inputPrideXMLFile.getPath(),
          "-" + ARG_SKIP_SERIALIZATION,
          "-" + ARG_REPORTFILE,
          reportFile.getPath()
        };
    PGConverter.main(args);
  }
}