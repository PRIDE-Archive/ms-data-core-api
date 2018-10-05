package uk.ac.ebi.pride.utilities.data.controller.tools;

import org.junit.Test;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * This class contains unit tests for file format conversion.
 *
 * @author Tobias Ternent
 */

public class ConverterTest {
  /**
   * This test converts an example mzIdentML file into an mzTab file.
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testConvertMzidToMztab() throws Exception{
    URL url = ConverterTest.class.getClassLoader().getResource("test.mzid");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputMzidFile = new File(url.toURI());
    File outputFile = File.createTempFile("test", ".mztab");
    String[] args = new String[]{
            "-" + ARG_CONVERSION,
            "-" + ARG_INPUTFILE, inputMzidFile.getPath(),
            "-" + ARG_OUTPUTFILE, outputFile.getPath()};
    Converter.startConversion(PGConverter.parseArgs(args));
    MzTabControllerImpl mzTabController = new MzTabControllerImpl(outputFile);
    assertTrue("Total number of Protein IDs should be correct.", 797==mzTabController.getProteinIds().size());
    mzTabController.close();
    assertTrue("No errors reported during the conversion from  mzIdentML to MzTab", outputFile.exists());
  }

  /**
   * This test converts an example PRIDE XML file into an mzTab file.
   *
   * @throws Exception if there are problems opening the example file.
   */
  @Test
  public void testConvertPridexmlToMztab() throws Exception{
    URL url = ConverterTest.class.getClassLoader().getResource("test.xml");
    if (url == null) {
      throw new IllegalStateException("no file for input found!");
    }
    File inputPridexmlFile = new File(url.toURI());
    File outputFile = new File("C:\\test\\test.mztab");
    String[] args = new String[]{
            "-" + ARG_CONVERSION,
            "-" + ARG_INPUTFILE, inputPridexmlFile.getPath(),
            "-" + ARG_OUTPUTFILE, outputFile.getPath()};
    Converter.startConversion(PGConverter.parseArgs(args));
    MzTabControllerImpl mzTabController = new MzTabControllerImpl(outputFile);
    assertTrue("Total number of Protein IDs should be correct.", 269==mzTabController.getProteinIds().size());
    mzTabController.close();
    assertTrue("No errors reported during the conversion from  mzIdentML to MzTab", outputFile.exists());
  }
  //TODO mzTab to proBed conversion? mzIdentML to proBed validation?
}