package uk.ac.ebi.pride.utilities.data.utils;


import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.*;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineParam;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineScoreParam;
import uk.ac.ebi.pride.tools.ErrorHandlerIface;
import uk.ac.ebi.pride.tools.GenericSchemaValidator;
import uk.ac.ebi.pride.tools.ValidationErrorHandler;
//Todo: check this library
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * MzIdentML utilities class. It contains all functions related with mzidentMl validation
 * file format,
 * <p/>
 * @author ypriverol
 * @author rwang
 */
public final class MzIdentMLUtils {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MzIdentMLUtils.class);

    /**
     * This parameter is a hack to allow in mzIdentML version 1.1 to convert the modification unambiguity position from
     * proteome discoverer values.
     */

    private MzIdentMLUtils() {
    }

    public static Constants.SpecIdFormat getSpectraDataIdFormat(uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData) {
        uk.ac.ebi.pride.utilities.data.core.CvParam specIdFormat = spectraData.getSpectrumIdFormat();
        return getSpectraDataIdFormat(specIdFormat.getAccession());
    }

    public static Constants.SpecIdFormat getSpectraDataIdFormat(uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraData) {
        uk.ac.ebi.jmzidml.model.mzidml.CvParam specIdFormat = spectraData.getSpectrumIDFormat().getCvParam();
        return getSpectraDataIdFormat(specIdFormat.getAccession());
    }

    public static Constants.SpecFileFormat getSpectraDataFormat(uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData) {
        uk.ac.ebi.pride.utilities.data.core.CvParam specFileFormat = spectraData.getFileFormat();
        if (specFileFormat != null) {
            if (specFileFormat.getAccession().equals("MS:1000613"))
                return Constants.SpecFileFormat.DTA;
            if (specFileFormat.getAccession().equals("MS:1001062"))
                return Constants.SpecFileFormat.MGF;
            if (specFileFormat.getAccession().equals("MS:1000565"))
                return Constants.SpecFileFormat.PKL;
            if (specFileFormat.getAccession().equals("MS:1000584") || specFileFormat.getAccession().equals("MS:1000562"))
                return Constants.SpecFileFormat.MZML;
            if (specFileFormat.getAccession().equals("MS:1000566"))
                return Constants.SpecFileFormat.MZXML;
        }
        return Constants.SpecFileFormat.NONE;
    }

    private static Constants.SpecIdFormat getSpectraDataIdFormat(String accession) {
        if (accession.equals("MS:1001528"))
            return Constants.SpecIdFormat.MASCOT_QUERY_NUM;
        if (accession.equals("MS:1000774"))
            return Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID;
        if (accession.equals("MS:1000775"))
            return Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID;
        if (accession.equals("MS:1001530"))
            return Constants.SpecIdFormat.MZML_ID;
        if (accession.equals("MS:1000776"))
            return Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID;
        if (accession.equals("MS:1000770"))
            return Constants.SpecIdFormat.WIFF_NATIVE_ID;
        if (accession.equals("MS:1000777"))
            return Constants.SpecIdFormat.MZDATA_ID;
        if(accession.equals(("MS:1000768")))
            return Constants.SpecIdFormat.SPECTRUM_NATIVE_ID;
        return Constants.SpecIdFormat.NONE;
    }

    public static String getSpectrumId(uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraData, String spectrumID) {
        Constants.SpecIdFormat fileIdFormat = getSpectraDataIdFormat(spectraData);

        if (fileIdFormat == Constants.SpecIdFormat.MASCOT_QUERY_NUM) {
            String rValueStr = spectrumID.replaceAll("query=", "");
            String id = null;
            if(rValueStr.matches(Constants.INTEGER)){
                id = Integer.toString(Integer.parseInt(rValueStr) + 1);
            }
            return id;
        } else if (fileIdFormat == Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID) {
            String rValueStr = spectrumID.replaceAll("index=", "");
            String id = null;
            if(rValueStr.matches(Constants.INTEGER)){
                id = Integer.toString(Integer.parseInt(rValueStr) + 1);
            }
            return id;
        } else if (fileIdFormat == Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID) {
            return spectrumID.replaceAll("file=", "");
        } else if (fileIdFormat == Constants.SpecIdFormat.MZML_ID) {
            return spectrumID.replaceAll("mzMLid=", "");
        } else if (fileIdFormat == Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID) {
            return spectrumID.replaceAll("scan=", "");
        } else {
            return spectrumID;
        }
    }

    public static List<Constants.SpecFileFormat> getFileTypeSupported(SpectraData spectraData) {
        List<Constants.SpecFileFormat> fileFormats = new ArrayList<Constants.SpecFileFormat>();

        Constants.SpecFileFormat spectraDataFormat = MzIdentMLUtils.getSpectraDataFormat(spectraData);

        if (spectraDataFormat == Constants.SpecFileFormat.NONE) {
            Constants.SpecIdFormat spectIdFormat = MzIdentMLUtils.getSpectraDataIdFormat(spectraData);
            if (spectIdFormat == Constants.SpecIdFormat.MASCOT_QUERY_NUM) {
                fileFormats.add(Constants.SpecFileFormat.MGF);
            } else if (spectIdFormat == Constants.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID || spectIdFormat == Constants.SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID) {
                spectraDataFormat = MzIdentMLUtils.getDataFormatFromFileExtension(spectraData);
                fileFormats.add(spectraDataFormat);
                if(spectraDataFormat != Constants.SpecFileFormat.DTA)  fileFormats.add(Constants.SpecFileFormat.DTA);
                if(spectraDataFormat != Constants.SpecFileFormat.MGF)  fileFormats.add(Constants.SpecFileFormat.MGF);
                if(spectraDataFormat != Constants.SpecFileFormat.PKL)  fileFormats.add(Constants.SpecFileFormat.PKL);
                if(spectraDataFormat != Constants.SpecFileFormat.NONE) fileFormats.add(Constants.SpecFileFormat.NONE);
            }else if (spectIdFormat == Constants.SpecIdFormat.MZML_ID) {
                fileFormats.add(Constants.SpecFileFormat.MZML);
            } else if (spectIdFormat == Constants.SpecIdFormat.SCAN_NUMBER_NATIVE_ID) {
                fileFormats.add(Constants.SpecFileFormat.MZXML);
            } else if (spectIdFormat == Constants.SpecIdFormat.MZDATA_ID) {
                fileFormats.add(Constants.SpecFileFormat.MZDATA);
            }
        } else {
            fileFormats.add(spectraDataFormat);
        }
        return fileFormats;
    }

    /**
     * Check the file type
     *
     * @param file input file
     * @return Class    the class type of the data access controller
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

    public static Constants.SpecFileFormat getDataFormatFromFileExtension(SpectraData spectradata){
        Constants.SpecFileFormat fileFormat = Constants.SpecFileFormat.NONE;
        if(spectradata.getLocation() !=null){
            fileFormat = Constants.getSpecFileFormatFromLocation(spectradata.getLocation());
        }else if(spectradata.getName() != null){
            fileFormat = Constants.getSpecFileFormatFromLocation(spectradata.getName());
        }
        return fileFormat;
    }

    public static List<String> validateMzIdentMLSchema(File resultFile) throws SAXException, FileNotFoundException, URISyntaxException, MalformedURLException {
        GenericSchemaValidator genericValidator = new GenericSchemaValidator();
        URI url = MzIdentMLUtils.class.getClassLoader().getResource("mzIdentML1.1.0.xsd").toURI();
        genericValidator.setSchema(url);
        List<String> errorMsgs;

        logger.info("XML schema validation on " + resultFile.getName());
        ErrorHandlerIface handler = new ValidationErrorHandler();
        genericValidator.setErrorHandler(handler);
        BufferedReader br = new BufferedReader(new FileReader(resultFile));
        genericValidator.validate(br);

        //noinspection unchecked
        errorMsgs = handler.getErrorMessages(); // ToDo: make ErrorHandlerIface type safe


        return errorMsgs;
    }

    /**
     * Search and find a list of search engine types from input parameter group.
     *
     * @return List<SearchEngineType>  a list of search engine
     */
    public static List<SearchEngineParam> getSearchEngineTypes(List<CvParam> cvParams) {
        if (cvParams == null) {
            throw new IllegalArgumentException("Input argument for getSearchEngineScoreTypes can not be null");
        }
        List<SearchEngineParam> searchEngines = new ArrayList<SearchEngineParam>();
        for(CvParam param: cvParams)
            if(SearchEngineScoreParam.getSearchEngineScoreParamByAccession(param.getAccession()) != null){
                SearchEngineScoreParam searchEngineScoreParam = SearchEngineScoreParam.getSearchEngineScoreParamByAccession(param.getAccession());
                SearchEngineParam seachEngine = searchEngineScoreParam.getSearchEngineParam();
                searchEngines.add(seachEngine);
            }

        return searchEngines;
    }



}
