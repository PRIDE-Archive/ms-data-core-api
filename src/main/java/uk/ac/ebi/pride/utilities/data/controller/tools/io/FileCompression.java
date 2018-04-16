package uk.ac.ebi.pride.utilities.data.controller.tools.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class FileCompression {

  private static final Logger log = LoggerFactory.getLogger(FileCompression.class);

  /**
   * This method extracts an input list of files.
   *
   * @param files a list of input zip files to extract.
   * @return a list of extracted files.
   */
  public static List<File> extractZipFiles(List<File> files) {
    List<File> zippedFiles = findZippedFiles(files);
    if (zippedFiles.size()>0) {
      files.removeAll(zippedFiles);
      files.addAll(unzipFiles(zippedFiles, zippedFiles.get(0).getParentFile().getAbsoluteFile()));
    }
    return files.stream().distinct().collect(Collectors.toList());
  }

  /**
   * ~This method identifies any gzipped files.
   * @param files a list if input files.
   * @return a list of files that are gzipped.
   */
  private static List<File> findZippedFiles(List<File> files) {
    return files.stream().filter(file -> file.getName().endsWith(".gz")).collect(Collectors.toList());
  }

  /**
   * This method extracts a list of iniput gzipped files to an output directory.
   * @param zippedFiles a list of input files to extract.
   * @param outputFolder the output directory.
   * @return a list of files that have been extracted.
   */
  private static List<File> unzipFiles(List<File> zippedFiles, File outputFolder) {
    List<File> unzippedFiles = new ArrayList<>();
    zippedFiles.parallelStream().forEach(inputFile -> {
      try {
        log.info("Unzipping file: " + inputFile.getAbsolutePath());
        FileInputStream fis = null;
        GZIPInputStream gs = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
          fis = new FileInputStream(inputFile);
          gs = new GZIPInputStream(fis);
          String outputFile = outputFolder + File.separator + inputFile.getName().replace(".gz", "");
          fos = new FileOutputStream(outputFile);
          bos = new BufferedOutputStream(fos, 2048);
          byte data[] = new byte[2048];
          int count;
          while ((count = gs.read(data, 0, 2048)) != -1) {
            bos.write(data, 0, count);
          }
          bos.flush();
          bos.close();
          unzippedFiles.add(new File(outputFile));
          log.info("Unzipped file: " + outputFile);
        } finally {
          if (fis != null) {
            fis.close();
          }
          if (gs != null) {
            gs.close();
          }
          if (fos != null) {
            fos.close();
          }
          if (bos != null) {
            bos.close();
          }
        }
      } catch (IOException ioe) {
        log.error("IOException when unzipping files.", ioe);
      }
    });
    return unzippedFiles;
  }
}