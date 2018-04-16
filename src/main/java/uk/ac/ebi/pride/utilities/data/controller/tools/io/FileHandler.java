package uk.ac.ebi.pride.utilities.data.controller.tools.io;

import com.google.common.io.Files;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.ebi.pride.utilities.data.controller.tools.utils.Utility.*;

public class FileHandler {

  private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

  /**
   * This method gets all the input file ready for validation, if it is extracted.
   *
   * @param file the input file for validation.
   * @return List of extracted files for validation.
   */
  public static List<File> getFilesToValidate(File file) {
    List<File> filesToValidate = new ArrayList<>();
    if (file.isDirectory()) {
      log.error("Unable to validate against directory of mzid files.");
    } else {
      filesToValidate.add(file);
    }
    filesToValidate = FileCompression.extractZipFiles(filesToValidate);
    return filesToValidate;
  }

  /**
   * Creates temp data access controller files.
   *
   * @param dataAccessControllerFiles     the input data access controller files
   * @param tempDataAccessControllerFiles the temp data acceess controller files that get created
   * @return true if all the temp files were created OK, false otherwise
   */
  public static boolean createTempDataAccessControllerFiles(List<File> dataAccessControllerFiles, List<File> tempDataAccessControllerFiles) {
    boolean badtempDataAccessControllerFiles = true;
    if (CollectionUtils.isNotEmpty(dataAccessControllerFiles)) {
      for (File dataAccessControllerFile : dataAccessControllerFiles) {
        File tempDataAccessControllerFile = createNewTempFile(dataAccessControllerFile);
        if (tempDataAccessControllerFile != null && 0 < tempDataAccessControllerFile.length()) {
          tempDataAccessControllerFiles.add(tempDataAccessControllerFile);
        }
      }
      badtempDataAccessControllerFiles = CollectionUtils.isEmpty(tempDataAccessControllerFiles) ||
              tempDataAccessControllerFiles.size() != dataAccessControllerFiles.size();
    }
    return badtempDataAccessControllerFiles;
  }

  /**
   * Creates a new temporary file, as a copy of an input file. DeleteOnExit() is set.
   *
   * @param file the source input file to copy from.
   * @return the new temporary file. This may be null if it was not created successfully.
   */
  public static File createNewTempFile(File file) {
    File tempFile = null;
    try {
      tempFile = new File(Files.createTempDir(), file.getName());
      File tempParentFile = tempFile.getParentFile();
      tempFile.deleteOnExit();
      tempParentFile.deleteOnExit();
      FileUtils.copyFile(file, tempFile);
    } catch (IOException e) {
      log.error("Problem creating temp fle for: " + file.getPath());
      log.error("Deleting temp file " + tempFile.getName() + ": " + tempFile.delete());
      tempFile = null;
    }
    return tempFile;
  }

  /**
   * Deletes all the temporary files (assay file, data access controller files).
   *
   * @param tempAssayFile                 the temp assay file to be deleted
   * @param tempDataAccessControllerFiles the temp data access controller files to be deleted
   */
  public static void deleteAllTempFiles(File tempAssayFile, List<File> tempDataAccessControllerFiles) {
    deleteTempFile(tempAssayFile);
    if (CollectionUtils.isNotEmpty(tempDataAccessControllerFiles)) {
      for (File dataAccessControllerFile : tempDataAccessControllerFiles) {
        if (dataAccessControllerFile != null) {
          deleteTempFile(dataAccessControllerFile);
        }
      }
    }
  }

  /**
   * Deletes a temporary file
   *
   * @param tempFile the temp file to be deleted.
   */
  public static void deleteTempFile(File tempFile) {
    log.info("Deleting temp file " + tempFile.getName() + ": " + tempFile.delete());
  }

  /**
   * This method gets a list of provided peak files.
   *
   * @param cmd the command line arguments.
   * @return List of peak files.
   */
  public static List<File> getPeakFiles(CommandLine cmd) {
    List<File> peakFiles = new ArrayList<>();
    if (cmd.hasOption(ARG_PEAK) || cmd.hasOption(ARG_PEAKS)) {
      String[] peakFilesString = cmd.hasOption(ARG_PEAK) ? cmd.getOptionValues(ARG_PEAK)
              : cmd.hasOption(ARG_PEAKS) ? cmd.getOptionValue(ARG_PEAKS).split(STRING_SEPARATOR) : new String[0];
      for (String aPeakFilesString : peakFilesString) {
        File peakFile = new File(aPeakFilesString);
        if (peakFile.isDirectory()) {
          File[] listFiles = peakFile.listFiles(File::isFile);
          if (listFiles != null) {
            peakFiles.addAll(Arrays.asList(listFiles));
          }
        } else {
          peakFiles.add(peakFile);
          log.info("Added peak file: " + peakFile.getPath());
        }
      }
      peakFiles = FileCompression.extractZipFiles(peakFiles);
    } else {
      log.error("Peak file not supplied with mzIdentML file.");
    }
    return peakFiles;
  }

  /**
   * This method identifies a file's format extension type.
   *
   * @param file the input file.
   * @return the corresponding FileType.
   */
  public static FileType getFileType(File file) {
    FileType result;
    log.info("Checking file type for : " + file);
    if (PrideXmlControllerImpl.isValidFormat(file)) {
      result = FileType.PRIDEXML;
    } else if (MzIdentMLControllerImpl.isValidFormat(file)) {
      result = FileType.MZID;
    } else if (MzTabControllerImpl.isValidFormat(file)) {
      result = FileType.MZTAB;
    } else {
      log.error("Unrecognised file type: " + file);
      result = FileType.UNKNOWN;
    }
    return result;
  }
}