package uk.ac.ebi.pride.utilities.data.controller.tools.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.ac.ebi.pride.tools.cl.PrideXmlClValidator;
import uk.ac.ebi.pride.tools.cl.XMLValidationErrorHandler;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Suresh Hewapathirana
 */
public class SchemaValidator {

  private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);
  private static final  String SCHEMA_OK_MESSAGE = "XML schema validation OK on: ";

  /**
   * This method validates an input mzIdentML file according to the supplied schema, and returns the outcome.
   *
   * @param schemaLocation the location of the schema
   * @param pridexml       the input PRIDE XML file.
   * @return a list of two elements: the first element is a boolean (true or false) if the file passed validation. If false, the 2nd element in the list of the error messages.
   */
  public static SchemaCheckResult validatePridexmlSchema(String schemaLocation, File pridexml) {
    log.info("Validating PRIDE XML schema for: " + pridexml.getPath() + " using schema: " + schemaLocation);
    SchemaCheckResult result = new SchemaCheckResult(false, new ArrayList<>());
    try {
      PrideXmlClValidator validator = new PrideXmlClValidator();
      validator.setSchema(new URL(schemaLocation));
      BufferedReader br = new BufferedReader(new FileReader(pridexml));
      XMLValidationErrorHandler xveh = validator.validate(br);
      final String ERROR_MESSAGES = xveh.getErrorsFormattedAsPlainText();
      result.setValidAgainstSchema(StringUtils.isEmpty(ERROR_MESSAGES));
      if (StringUtils.isEmpty(ERROR_MESSAGES)) {
        log.info(SCHEMA_OK_MESSAGE + pridexml.getName());
      } else {
        log.error(ERROR_MESSAGES);
        result.setErrorMessages(xveh.getErrorsAsList());
      }
    } catch (Exception e) {
      log.error("Exception while validating PRIDE XML schema:", e);
    }
    return result;
  }

  /**
   * This method validates an input PRIDE XML file according to the supplied schema, and writes the output to a file.
   *
   * @param schemaLocation the location of the schema
   * @param pridexml       the input PRIDE XML file.
   * @param outputFile     the output log file with an OK message if there were no errors
   */
  public static void validatePridexmlSchema(String schemaLocation, File pridexml, File outputFile) {
    log.info("Validating PRIDE XML schema for: " + pridexml.getPath() + " using schema: " + schemaLocation);
    try {
      PrideXmlClValidator validator = new PrideXmlClValidator();
      validator.setSchema(new URL(schemaLocation));
      BufferedReader br = new BufferedReader(new FileReader(pridexml));
      XMLValidationErrorHandler xveh = validator.validate(br);
      final String ERROR_MESSAGES = xveh.getErrorsFormattedAsPlainText();
      if (StringUtils.isEmpty(ERROR_MESSAGES)) {
        log.info(SCHEMA_OK_MESSAGE + pridexml.getName());
        if (outputFile != null) {
          Files.write(outputFile.toPath(), SCHEMA_OK_MESSAGE.getBytes());
        }
      } else {
        log.error(ERROR_MESSAGES);
        if (outputFile != null) {
          Files.write(outputFile.toPath(), ERROR_MESSAGES.getBytes());
        }
      }
    } catch (IOException | SAXException e) {
      log.error("File Not Found or SAX Exception: ", e);
    } catch (Exception e) {
      log.error("Exception while validating PRIDE XML schema:", e);
    }
  }

  /**
   * This method validates an input mzIdentML file according to the right schema based on the version, and writes the output to a file.
   *
   * @param mzIdentML the input mzIdentML file.
   * @param outputFile the output log file with an OK message if there were no errors
   */
  public static SchemaCheckResult validateMzidSchema(File mzIdentML, File outputFile) {
    SchemaCheckResult result = new SchemaCheckResult(false, new ArrayList<>());
    try {
      List<String> errorMessages = MzIdentMLUtils.validateMzIdentMLSchema(mzIdentML);
      result.setValidAgainstSchema(errorMessages.size() < 1);
      result.setErrorMessages(errorMessages);
      if (outputFile != null) {
        Files.write(outputFile.toPath(), result.toString().getBytes());
      }
    } catch (IOException e) {
      log.error("File Not Found or SAX Exception: ", e);
    }
    return result;
  }

  /**
   * This method validates an input mzIdentML file according to the right schema based on the version, and returns the outcome.
   *
   * @param mzIdentML the input mzIdentML file.
   * @return a SchemaCheckResult - if the mzIdentML passed validation, validAgainstSchema will be true.
   * False otherwise, and contains a list of the error messages.
   */
  public static SchemaCheckResult validateMzidSchema(File mzIdentML) {
    SchemaCheckResult result = new SchemaCheckResult(false, new ArrayList<>());
    List<String> errorMessages = MzIdentMLUtils.validateMzIdentMLSchema(mzIdentML);
    result.setValidAgainstSchema(errorMessages.size() < 1);
    result.setErrorMessages(errorMessages);
    return result;
  }
}
