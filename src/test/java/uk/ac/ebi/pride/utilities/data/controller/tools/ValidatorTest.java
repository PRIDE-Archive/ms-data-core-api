package uk.ac.ebi.pride.utilities.data.controller.tools;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

import static org.junit.Assert.assertTrue;

/**
 * This class contains unit tests for file format validation.
 *
 * @author Tobias Ternent
 */

public class ValidatorTest {

  private static final Logger log = LoggerFactory.getLogger(ValidatorTest.class);

  /**
   * This test validates one example mzIdentML file which is related to a single peak .mgf file (without schema validation).
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
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_MZID, inputMzidFile.getPath(),
            "-" + ARG_PEAK, inputMgfFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()
    };
    Validator.startValidation(PGConverter.parseArgs(args));
    assertTrue("No errors reported during the validation of the mzIdentML file", reportStatus(reportFile));
  }

  /**
   * This test performs xml schema-only validation on one example mzIdentML file which is related to a single peak .mgf file.
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testMzidSchemaValidator() throws Exception {
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
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_SCHEMA_ONLY_VALIDATION,
            "-" + ARG_MZID, inputMzidFile.getPath(),
            "-" + ARG_PEAK, inputMgfFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()
    };
    Validator.startValidation(PGConverter.parseArgs(args));
    assertTrue("No errors reported during the validation of the mzIdentML file", reportStatus(reportFile));
    reportFile.deleteOnExit();

    url = ConverterTest.class.getClassLoader().getResource("mzidentml-example-bad.mzid");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    inputMzidFile = new File(url.toURI());
    reportFile = File.createTempFile("testMzid", ".log");
    args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_SCHEMA_ONLY_VALIDATION,
            "-" + ARG_MZID, inputMzidFile.getPath(),
            "-" + ARG_PEAK, inputMgfFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()
    };
    Validator.startValidation(PGConverter.parseArgs(args));
    try {
      reportStatus(reportFile);
    } catch (IOException ioe) {
      assertTrue("Errors reported during the validation of the mzIdentML file", ioe.toString().contains("ERROR"));
    }
  }

  /**
   * This test validates one example PRIDE XML file (without schema validation).
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testPridexmlValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.xml");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputPridexmlFile = new File(url.toURI());
    File reportFile = File.createTempFile("testPridexml", ".log");
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_PRIDEXML, inputPridexmlFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()};
    Validator.startValidation(PGConverter.parseArgs(args));
    assertTrue("No errors reported during the validation of the PRIDE XML", reportStatus(reportFile));
  }

  /**
   * This test performs xml schema-only validation on one example PRIDE XML file.
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testPridexmlSchemaValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.xml");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputPridexmlFile = new File(url.toURI());
    File reportFile = File.createTempFile("testPridexml", ".log");
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_SCHEMA_ONLY_VALIDATION,
            "-" + ARG_PRIDEXML, inputPridexmlFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()};
    Validator.startValidation(PGConverter.parseArgs(args));
    assertTrue("No errors reported during the validation of the PRIDE XML", reportStatus(reportFile));
  }

  /**
   * This test validates one example mzTab file.
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testMztabValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.mztab");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMztabFile = new File(url.toURI());
    url = ConverterTest.class.getClassLoader().getResource("test.mgf");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMgfFile = new File(url.toURI());
    File reportFile = File.createTempFile("testMztab", ".log");
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_MZTAB, inputMztabFile.getPath(),
            "-" + ARG_PEAK, inputMgfFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()};
    Validator.startValidation(PGConverter.parseArgs(args));
    assertTrue("No errors reported during the validation of the mzTab file", reportStatus(reportFile));
  }

  private boolean reportStatus(File report) throws Exception {
    boolean result = false;
    List<String> reportLines = new ArrayList<>();
    Stream<String> stream = Files.lines(report.toPath(), Charset.defaultCharset());
    stream.forEach(reportLines::add);
    for (String reportLine : reportLines) {
      String[] parts = reportLine.split(": ");
      if (parts.length > 0) {
        String key = parts[0];
        String content = parts.length > 1 ? parts[1] : "";
        switch (key) {
          case "Status":
            if (content.contains("ERROR")) {
              //result = false;
              throw new IOException(content);
            } else if (content.contains("OK")) {
              result = true;
            }
            break;
          default:
            break;
        }
      }
    }
    return result;
  }

  @Test
  public void testProbedValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.pro.bed");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputProbedFile = new File(url.toURI());
    File reportFile = File.createTempFile("testProbed", ".log");
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_PROBED, inputProbedFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()};
    Validator.startValidation(PGConverter.parseArgs(args));
    assertTrue("No errors reported during the validation of the mzTab file", reportStatus(reportFile));
  }

  /**
   * This test validates one example mzIdentML file which is related to a "purposefully bad" single peak .mgf file (without schema validation).
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testBadMissingPeaksMzidValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("test.mzid");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    url = ConverterTest.class.getClassLoader().getResource("missing-peaks.mgf");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMgfFile = new File(url.toURI());
    File reportFile = File.createTempFile("testMzid", ".log");
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_MZID, inputMzidFile.getPath(),
            "-" + ARG_PEAK, inputMgfFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()
    };
    Validator.startValidation(PGConverter.parseArgs(args));
    Exception e = null;
    try {
      log.info("Report OK? " + reportStatus(reportFile));
    } catch (IOException ioe) {
      e = ioe;
    }
    assertTrue("Errors correctly reported during the validation of the mzIdentML file", e != null);
  }

  /**
   * This test validates one "purposefully bad" example mzIdentML file with badly reported PTM CV Pararams,
   * which is related to a single peak .mgf file (without schema validation).
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testBadPtmCvparamsMzidValidator() throws Exception {
    URL url = ConverterTest.class.getClassLoader().getResource("bad-ptm-cvps.mzid");
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
    String[] args = new String[]{
            "-" + ARG_VALIDATION,
            "-" + ARG_MZID, inputMzidFile.getPath(),
            "-" + ARG_PEAK, inputMgfFile.getPath(),
            "-" + ARG_SKIP_SERIALIZATION,
            "-" + ARG_REPORTFILE, reportFile.getPath()
    };
    Validator.startValidation(PGConverter.parseArgs(args));
    Exception exception = null;
    try {
      log.info("Report OK? " + reportStatus(reportFile));
    } catch (IOException ioException) {
      exception = ioException;
    }
    assertTrue("Errors correctly reported during the validation of the mzIdentML file", exception != null);
  }
}

