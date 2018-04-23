package uk.ac.ebi.pride.utilities.data.utils;

import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.*;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Constants contain a set of functions for SpectraData validation and also the constants used by
 * mzTab and mzIdentML to reference the Ids and type file formats.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public final class Constants {

  /** algebraic sign */
  private static final String SIGN = "[+-]";

  public static final String TAB = "\t";

  public static final String LINE_SEPARATOR = "\n";

  /** integer expression */
  public static final String INTEGER = SIGN + "?\\d+";
  public static final String VERSION_NUMBER = "^(\\d+\\.)(\\d+\\.)(\\d+)$";
  public static final String MZIDENTML_ID_PATTERN = "^[a-zA-Z0-9.]*$";

  private Constants() {}

  public static final String NOT_AVAILABLE = "N/A";
  public static final String MGF_EXT = ".mgf";
  public static final String DTA_EXT = ".dta";
  public static final String MS2_EXT = ".ms2";
  public static final String PKL_EXT = ".pkl";
  public static final String MZXML_EXT = ".mzxml";
  public static final String APL_EXT = ".apl";
  public static final String XML_EXT = ".xml";
  public static final String MZML_EXT = ".mzML";
  public static final String MZTAB_EXT = ".mztab";
  public static final String WIFF_EXT = ".wiff";
  public static final String NETCDF_EXT = ".cdf";

  public static final String MZIDENTML_NAMESPACE = "http://psidev.info/psi/pi/mzIdentML/1.1";
  public static final Double DELTATHESHOLD = 4.0;
  public static final String PSI_MOD = "MOD";
  public static final String MS = "MS";
  public static final String UNIMOD = "UNIMOD";
  public static final String ANCHOR_PROTEIN = "MS:1001591";

  /** Supported id format used in the spectrum file. */
  public static enum SpecIdFormat {
    MASCOT_QUERY_NUM,
    MULTI_PEAK_LIST_NATIVE_ID,
    SINGLE_PEAK_LIST_NATIVE_ID,
    SCAN_NUMBER_NATIVE_ID,
    MZML_ID,
    MZDATA_ID,
    WIFF_NATIVE_ID,
    SPECTRUM_NATIVE_ID,
    WIFF_MGF_TITLE,
    NONE
  }

  /** An enum of the supported spectra file types */
  public static enum SpecFileFormat {
    MZML,
    PKL,
    DTA,
    MGF,
    MZXML,
    MZDATA,
    MS2,
    NONE
  }

  /**
   * Retrieve the Spectrum File format for an specific fileformat.
   *
   * @param fileFormat file format such mgf ms2, mzid or other file formats
   * @return the SpectrumFile format such as MZXML or PKL
   */
  public static SpecFileFormat getSpecFileFormat(String fileFormat) {
    if (fileFormat != null && fileFormat.length() > 0) {
      if (SpecFileFormat.MZXML.toString().equalsIgnoreCase(fileFormat)) return SpecFileFormat.MZXML;
      if (SpecFileFormat.DTA.toString().equalsIgnoreCase(fileFormat)) return SpecFileFormat.DTA;
      if (SpecFileFormat.MGF.toString().equalsIgnoreCase(fileFormat)) return SpecFileFormat.MGF;
      if (SpecFileFormat.MZDATA.toString().equalsIgnoreCase(fileFormat))
        return SpecFileFormat.MZDATA;
      if (SpecFileFormat.MZML.toString().equalsIgnoreCase(fileFormat)) return SpecFileFormat.MZML;
      if (SpecFileFormat.PKL.toString().equalsIgnoreCase(fileFormat)) return SpecFileFormat.PKL;
      if (SpecFileFormat.MS2.toString().equalsIgnoreCase(fileFormat)) return SpecFileFormat.MS2;
    }
    return SpecFileFormat.NONE;
  }

  /**
   * Return the SpectrumFile format for an specific path such as: /myppath/spectrum_file.mgf
   *
   * @param path the specific path
   * @return the SpectrumFile format such as MZXML or PKL
   */
  public static SpecFileFormat getSpecFileFormatFromLocation(String path) {
    if (path != null && path.length() > 0) {

      if (path.toUpperCase().contains(MZXML_EXT.toUpperCase())) return SpecFileFormat.MZXML;
      if (path.toUpperCase().contains(DTA_EXT.toUpperCase())) return SpecFileFormat.DTA;
      if (path.toUpperCase().contains(MGF_EXT.toUpperCase())) return SpecFileFormat.MGF;
      if (path.toUpperCase().contains(XML_EXT.toUpperCase())) return SpecFileFormat.MZDATA;
      if (path.toUpperCase().contains(MZML_EXT.toUpperCase())) return SpecFileFormat.MZML;
      if (path.toUpperCase().contains(PKL_EXT.toUpperCase())) return SpecFileFormat.PKL;
      if (path.toUpperCase().contains(PKL_EXT.toUpperCase())) return SpecFileFormat.PKL;
      if (path.toUpperCase().contains(MS2_EXT.toUpperCase())) return SpecFileFormat.MS2;
    }
    return SpecFileFormat.NONE;
  }

  /**
   * This function returns the Spectrum File format for an specific SpectraData ob object
   *
   * @param spectraData The SpectraData object
   * @return the Spectrum File format
   */
  public static Constants.SpecFileFormat getSpectraDataFormat(SpectraData spectraData) {
    uk.ac.ebi.pride.utilities.data.core.CvParam specFileFormat = spectraData.getFileFormat();
    if (specFileFormat != null) {
      if (specFileFormat.getAccession().equals("MS:1000613")) return Constants.SpecFileFormat.DTA;
      if (specFileFormat.getAccession().equals("MS:1001062")) return Constants.SpecFileFormat.MGF;
      if (specFileFormat.getAccession().equals("MS:1000565")) return Constants.SpecFileFormat.PKL;
      if (specFileFormat.getAccession().equals("MS:1000584")
          || specFileFormat.getAccession().equals("MS:1000562"))
        return Constants.SpecFileFormat.MZML;
      if (specFileFormat.getAccession().equals("MS:1000566")) return Constants.SpecFileFormat.MZXML;
      if (specFileFormat.getAccession().equals("MS:1001466")) return Constants.SpecFileFormat.MS2;
    }
    return getDataFormatFromFileExtension(spectraData);
  }

  /**
   * Spectrum Id format for an specific CVterm accession
   *
   * @param accession CvTerm Accession
   * @return Specific Spectrum Id Format
   */
  public static Constants.SpecIdFormat getSpectraDataIdFormat(String accession) {
    if (accession.equals("MS:1001528")) return Constants.SpecIdFormat.MASCOT_QUERY_NUM;
    if (accession.equals("MS:1000774")) return Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID;
    if (accession.equals("MS:1000775")) return Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID;
    if (accession.equals("MS:1001530")) return Constants.SpecIdFormat.MZML_ID;
    if (accession.equals("MS:1000776")) return Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID;
    if (accession.equals("MS:1000770")) return Constants.SpecIdFormat.WIFF_NATIVE_ID;
    if (accession.equals("MS:1000777")) return Constants.SpecIdFormat.MZDATA_ID;
    if (accession.equals(("MS:1000768"))) return Constants.SpecIdFormat.SPECTRUM_NATIVE_ID;
    if (accession.equals("MS:1000796")) return SpecIdFormat.WIFF_MGF_TITLE;
    return Constants.SpecIdFormat.NONE;
  }

  /**
   * Return the set of file format supported for an ambiguous SpectraData Object
   *
   * @param spectraData SpectraData Object
   * @return Set of file formats supported
   */
  public static List<SpecFileFormat> getFileTypeSupported(SpectraData spectraData) {
    List<Constants.SpecFileFormat> fileFormats = new ArrayList<SpecFileFormat>();

    Constants.SpecFileFormat spectraDataFormat = getSpectraDataFormat(spectraData);

    if (spectraDataFormat == Constants.SpecFileFormat.NONE) {
      Constants.SpecIdFormat spectIdFormat = MzIdentMLUtils.getSpectraDataIdFormat(spectraData);
      if (spectIdFormat == Constants.SpecIdFormat.MASCOT_QUERY_NUM) {
        fileFormats.add(Constants.SpecFileFormat.MGF);
      } else if (spectIdFormat == Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID
          || spectIdFormat == Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID) {
        spectraDataFormat = getDataFormatFromFileExtension(spectraData);
        fileFormats.add(spectraDataFormat);
        if (spectraDataFormat != Constants.SpecFileFormat.DTA)
          fileFormats.add(Constants.SpecFileFormat.DTA);
        if (spectraDataFormat != Constants.SpecFileFormat.MGF)
          fileFormats.add(Constants.SpecFileFormat.MGF);
        if (spectraDataFormat != Constants.SpecFileFormat.PKL)
          fileFormats.add(Constants.SpecFileFormat.PKL);
        if (spectraDataFormat != Constants.SpecFileFormat.NONE)
          fileFormats.add(Constants.SpecFileFormat.NONE);
        if (spectraDataFormat != SpecFileFormat.MS2) fileFormats.add(SpecFileFormat.MS2);
      } else if (spectIdFormat == Constants.SpecIdFormat.MZML_ID) {
        fileFormats.add(Constants.SpecFileFormat.MZML);
      } else if (spectIdFormat == Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID) {
        fileFormats.add(Constants.SpecFileFormat.MZXML);
      } else if (spectIdFormat == Constants.SpecIdFormat.MZDATA_ID) {
        fileFormats.add(Constants.SpecFileFormat.MZDATA);
      } else if (spectIdFormat == SpecIdFormat.WIFF_MGF_TITLE) {
        fileFormats.add(Constants.SpecFileFormat.MGF);
      }
    } else {
      fileFormats.add(spectraDataFormat);
    }
    return fileFormats;
  }

  /**
   * Return the Spectrum File format beased onf the SpectraData object name
   *
   * @param spectradata SpectraData Object
   * @return Spectrum File Format
   */
  public static Constants.SpecFileFormat getDataFormatFromFileExtension(SpectraData spectradata) {
    Constants.SpecFileFormat fileFormat = Constants.SpecFileFormat.NONE;
    if (spectradata.getLocation() != null) {
      fileFormat = Constants.getSpecFileFormatFromLocation(spectradata.getLocation());
    } else if (spectradata.getName() != null) {
      fileFormat = Constants.getSpecFileFormatFromLocation(spectradata.getName());
    }
    return fileFormat;
  }

  /**
   * Check the file type
   *
   * @param file input file
   * @return Class the class type of the data access controller
   */
  public static Class getFileType(File file) {
    Class classType = null;

    // check file type
    if (MzMLControllerImpl.isValidFormat(file)) {
      classType = MzMLControllerImpl.class;
    } else if (PrideXmlControllerImpl.isValidFormat(file)) {
      classType = PrideXmlControllerImpl.class;
    } else if (MzIdentMLControllerImpl.isValidFormat(file)) {
      classType = MzIdentMLControllerImpl.class;
    } else if (MzXmlControllerImpl.isValidFormat(file)) {
      classType = MzXmlControllerImpl.class;
    } else if (MzDataControllerImpl.isValidFormat(file)) {
      classType = MzDataControllerImpl.class;
    } else if (PeakControllerImpl.isValidFormat(file) != null) {
      classType = PeakControllerImpl.class;
    }
    return classType;
  }
}
