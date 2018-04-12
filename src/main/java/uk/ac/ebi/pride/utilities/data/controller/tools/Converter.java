package uk.ac.ebi.pride.utilities.data.controller.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;
import uk.ac.ebi.pride.utilities.data.exporters.AbstractMzTabConverter;
import uk.ac.ebi.pride.utilities.data.exporters.HQMzIdentMLMzTabConverter;
import uk.ac.ebi.pride.utilities.data.exporters.MzTabBedConverter;
import uk.ac.ebi.pride.utilities.data.exporters.PRIDEMzTabConverter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

/**
 * This class converts from one input file to another output format, according to the provided cmommand line arguments.
 *
 * @author Tobias Ternent
 */
public class Converter {

  private static final Logger log = LoggerFactory.getLogger(Converter.class);

  /**
   * This method begins the conversion from one input file to another output format.
   *
   * @param cmd command line arguments.
   * @throws IOException if there are problems reading or writing to the file system.
   */
  public static void startConversion(CommandLine cmd) throws IOException {
    log.info("Starting conversion...");
    File inputFile;
    String inputFileType = null;
    if (cmd.hasOption(ARG_INPUTFILE)) {
      inputFile = new File(cmd.getOptionValue(ARG_INPUTFILE));
    } else {
      inputFile = cmd.hasOption(ARG_MZID) ? new File(cmd.getOptionValue(ARG_MZID))
              : cmd.hasOption(ARG_PRIDEXML) ? new File(cmd.getOptionValue(ARG_PRIDEXML))
              : cmd.hasOption(ARG_MZTAB) ? new File(cmd.getOptionValue(ARG_MZTAB))
              : null;
    }
    if (inputFile == null || inputFile.isDirectory()) {
      log.error("Unable to convert whole directory.");
    } else {
      inputFileType = FilenameUtils.getExtension(inputFile.getAbsolutePath()).toLowerCase();
      if (inputFileType.equals("xml")) {
        inputFileType = ARG_PRIDEXML;
      }
    }
    File outputFile = null;
    String outputFormat = null;
    if (cmd.hasOption(ARG_OUTPUTFILE)) {
      outputFile = new File(cmd.getOptionValue(ARG_OUTPUTFILE));
      outputFormat = FilenameUtils.getExtension(outputFile.getAbsolutePath()).toLowerCase();
    } else if (cmd.hasOption(ARG_OUTPUTTFORMAT)) {
      outputFormat = cmd.getOptionValue(ARG_OUTPUTTFORMAT).toLowerCase();
      if (inputFile != null) {
        if (outputFormat.equalsIgnoreCase(ARG_PROBED)) {
          outputFile = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.PROBED.toString().toLowerCase());
        } else if (outputFormat.equalsIgnoreCase(ARG_BIGBED)) {
          outputFile = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.BIGBED.toString().toLowerCase());
        } else {
          outputFile = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + outputFormat);
        }
      }
    } else {
      log.error("No output file or output format specified.");
    }
    if (inputFile != null && outputFile != null) {
      switch (inputFileType != null ? inputFileType : "") {
        case ARG_MZID:
        case ARG_PRIDEXML:
          if (outputFormat.equals(ARG_MZTAB)) {
            convertToMztab(inputFile, outputFile, inputFileType);
          } else {
            if ((inputFileType != null && inputFileType.equals(ARG_MZID)) && outputFormat.equals(ARG_PROBED)) {
              File intermediateMztab = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.MZTAB.toString().toLowerCase());
              convertToMztab(inputFile, intermediateMztab, inputFileType);
              startMztabToProbed(intermediateMztab, outputFile, cmd);
            } else {
              if ((inputFileType != null && inputFileType.equals(ARG_MZID)) && outputFormat.equals(ARG_BIGBED)) {
                File intermediateMztab = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.MZTAB.toString().toLowerCase());
                convertToMztab(inputFile, intermediateMztab, inputFileType);
                File intermediateProbed = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.PROBED.toString().toLowerCase());
                startMztabToProbed(intermediateMztab, intermediateProbed, cmd);
                startProbedToBigbed(intermediateProbed, cmd);
              } else {
                log.error("Unable to convert input mzid/pride xml file into the target output format.");
              }
            }
          }
          break;
        case ARG_MZTAB:
          switch (outputFormat) {
            case ARG_PROBED:
              startMztabToProbed(inputFile, outputFile, cmd);
              break;
            case ARG_BIGBED:
              File intermediateProbed = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.PROBED.toString().toLowerCase());
              startMztabToProbed(inputFile, intermediateProbed, cmd);
              startProbedToBigbed(intermediateProbed, cmd);
              break;
            default:
              log.error("Unable to convert input mztab into the target output format.");
              break;
          }
          break;
        case ARG_PROBED:
          if (outputFormat.equals(ARG_BIGBED)) {
            startProbedToBigbed(inputFile, cmd);
          } else {
            log.error("Unable to convert input probed into the target output format.");
          }
          break;
      }
    } else {
      log.error("No output file or format defined.");
    }
  }

  /**
   * This methid begins the conversion from an input mzTab file to proBed.
   *
   * @param inputFile  the input mzTab file.
   * @param outputFile the output proBed file.
   * @param cmd        command line arguments
   * @throws IOException if there are problems reading or writing to the file system.
   */
  private static void startMztabToProbed(File inputFile, File outputFile, CommandLine cmd) throws IOException {
    convertMztabToProbed(inputFile, outputFile);
    if (cmd.hasOption(ARG_CHROMSIZES)) {
      log.info("Sorting and filtering proBed file according to chrom sizes file: " + cmd.getOptionValue(ARG_CHROMSIZES));
      try {
        FileUtils.copyFile(outputFile, new File(outputFile.getPath() + "_unsorted"));
        MzTabBedConverter.sortProBed(outputFile, new File(cmd.getOptionValue(ARG_CHROMSIZES)));
      } catch (InterruptedException ie) {
        log.error("Interrupted Exception: ", ie);
        throw new IOException(ie);
      }
    } else {
      log.info("Chromosome sizes file was not provided, therefore not sorting BED file.");
    }
  }

  /**
   * This method converts an input proBed file to bigBed.
   *
   * @param inputFile the input proBed file to convert.
   * @param cmd       command line arguments.
   * @throws IOException if there are problems reading or writing to the file system.
   */
  private static void startProbedToBigbed(File inputFile, CommandLine cmd) throws IOException {
    File aSQL = null;
    File chromSizes = null;
    File bigBedConverter = null;
    if (cmd.hasOption(ARG_ASQLFILE)) {
      aSQL = new File(cmd.getOptionValue(ARG_ASQLFILE));
    } else if (cmd.hasOption(ARG_ASQLNAME)) {
      aSQL = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "." + FileType.ASQL.toString().toLowerCase());
      MzTabBedConverter.createAsql(cmd.getOptionValue(ARG_ASQLNAME), aSQL.getAbsolutePath());
    }
    if (cmd.hasOption(ARG_CHROMSIZES)) {
      chromSizes = new File(cmd.getOptionValue(ARG_CHROMSIZES));
    }
    if (cmd.hasOption(ARG_BIGBEDCONVERTER)) {
      bigBedConverter = new File(cmd.getOptionValue(ARG_BIGBEDCONVERTER));
    }
    if (aSQL != null && chromSizes != null && bigBedConverter != null) {
      convertProbedToBigbed(inputFile, aSQL, chromSizes, bigBedConverter);
    } else {
      log.error("All supporting files have not been set correctly. Please double check the following have been provided properly:\n" +
              "aSQL file, chromosome sizes file, bigBedConverter tool.");
    }
  }

  /**
   * This method converts an input mzIdentML or PRIDE XML file to mzTab.
   *
   * @param inputFile       the input file.
   * @param outputMztabFile the output mzTab file.
   * @param inputFormat     the input file format.
   * @throws IOException if there are problems reading or writing to the file system.
   */
  private static void convertToMztab(File inputFile, File outputMztabFile, String inputFormat) throws IOException {
    log.info("About to convert input file: " + inputFile.getAbsolutePath() + " to: " + outputMztabFile.getAbsolutePath());
    List<File> filesToConvert = new ArrayList<>();
    filesToConvert.add(inputFile);
    filesToConvert = Validator.extractZipFiles(filesToConvert);
    filesToConvert.forEach(file -> {
      try {
        AbstractMzTabConverter mzTabconverter = null;
        if (inputFormat.equals(FileType.MZID.toString())) {
          MzIdentMLControllerImpl mzIdentMLController = new MzIdentMLControllerImpl(inputFile);
          mzTabconverter = new HQMzIdentMLMzTabConverter(mzIdentMLController);
        } else if (inputFormat.equals(FileType.PRIDEXML.toString()) || inputFormat.equals(ARG_PRIDEXML)) {
          PrideXmlControllerImpl prideXmlController = new PrideXmlControllerImpl(inputFile);
          mzTabconverter = new PRIDEMzTabConverter(prideXmlController);
        }
        if (mzTabconverter != null) {
          MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
          BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(outputMztabFile));
          mzTabFile.printMZTab(writer);
          writer.close();
          log.info("Successfully written to mzTab file: " + outputMztabFile.getAbsolutePath());
        } else {
          throw new IOException("Unable to parse input file format correctly");
        }
      } catch (IOException ioe) {
        log.error("IOException: ", ioe);
      }
    });
  }

  /**
   * This method converts an input mzTab file (.mztab) to proBed (.pro.bed).
   *
   * @param inputFile  the input mzTab file.
   * @param outputFile the output proBed file.
   * @throws IOException if there are problems reading or writing to the file system.
   */
  private static void convertMztabToProbed(File inputFile, File outputFile) throws IOException {
    try {
      log.info("Converting to bed: " + inputFile.getAbsolutePath());
      MzTabControllerImpl mzTabController = new MzTabControllerImpl(inputFile);
      MzTabBedConverter mzTabBedConverter = new MzTabBedConverter(mzTabController);
      log.info("New proBed file path: " + outputFile.getAbsolutePath());
      boolean createdDirs = outputFile.getParentFile().mkdirs();
      boolean cratedNewFile = outputFile.createNewFile();
      if (createdDirs || cratedNewFile) {
        log.info("Crated new directory and/or file.");
      } else {
        log.info("Unable to create new directory and file, either the directory and/or file already exists or no insufficient permissions to access them.");
      }
      mzTabBedConverter.convert(outputFile);
      mzTabController.close();
      log.info("Finished processing " + outputFile.getAbsolutePath());
      File mzTabDirectory = inputFile.getParentFile();
      if (mzTabDirectory != null) {
        File[] files = mzTabDirectory.listFiles();
        if (files != null) {
          for (File file : files) {
            if (file.getName().contains("pride.mztaberrors.out")) {
              boolean deleted = file.delete();
              log.info("Deleted file: " + deleted);
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception when converting mztab to probed: ", e);
      throw new IOException(e);
    }
  }

  /**
   * This method converts a proBed file to bigBed using the UCSC converter tool.
   *
   * @param proBed          the input proBed file to be converted (.pro.bed).
   * @param aSQL            the supporting aSQL file (.as).
   * @param chromSizes      the supporting chromosome sizes text file.
   * @param bigBedConverter the UCSC bedToBigBed tool.
   */
  private static void convertProbedToBigbed(File proBed, File aSQL, File chromSizes, File bigBedConverter) {
    try {
      File outputBigBed = MzTabBedConverter.convertProBedToBigBed(
              aSQL,
              "bed12+13",
              proBed,
              chromSizes,
              bigBedConverter
      );
      log.info("Generated output bigBed file:" + outputBigBed.toPath());
    } catch (IOException | URISyntaxException | InterruptedException e) {
      log.error("Error when converting to bigBed: ", e);
    }
  }
}
