package uk.ac.ebi.pride.utilities.data.utils;


import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineParam;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineScoreParam;
import uk.ac.ebi.pride.tools.ErrorHandlerIface;
import uk.ac.ebi.pride.tools.GenericSchemaValidator;
import uk.ac.ebi.pride.tools.ValidationErrorHandler;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * MzIdentML utilities class. It contains all functions related with mzidentMl validation
 * file format,
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public final class MzIdentMLUtils {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MzIdentMLUtils.class);
    private static CvParam spectrumIdFormatMGFTitle;

    /**
     * This parameter is a hack to allow in mzIdentML version 1.1 to convert the modification unambiguity position from
     * proteome discoverer values.
     */

    private MzIdentMLUtils() {
    }

    public static Constants.SpecIdFormat getSpectraDataIdFormat(uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData) {
        uk.ac.ebi.pride.utilities.data.core.CvParam specIdFormat = spectraData.getSpectrumIdFormat();
        return Constants.getSpectraDataIdFormat(specIdFormat.getAccession());
    }

    public static Constants.SpecIdFormat getSpectraDataIdFormat(uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraData) {
        uk.ac.ebi.jmzidml.model.mzidml.CvParam specIdFormat = spectraData.getSpectrumIDFormat().getCvParam();
        return Constants.getSpectraDataIdFormat(specIdFormat.getAccession());

    } public static Constants.SpecIdFormat getSpectraDataIdFormat( uk.ac.ebi.pride.utilities.data.lightModel.SpectraData spectraData) {
        uk.ac.ebi.pride.utilities.data.lightModel.CvParam specIdFormat = spectraData.getSpectrumIDFormat().getCvParam();
        return Constants.getSpectraDataIdFormat(specIdFormat.getAccession());
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
            String id;
            if(rValueStr.matches(Constants.INTEGER)){
                id = Integer.toString(Integer.parseInt(rValueStr) + 1);
                return id;
            }
            return spectrumID;
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
// TODO: CODE GET DUPLICATED. NEED TO FIND HOW TO HAVE A COMMON METHOD
    public static String getSpectrumId( uk.ac.ebi.pride.utilities.data.lightModel.SpectraData spectraData, String spectrumID) {
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
            String id;
            if(rValueStr.matches(Constants.INTEGER)){
                id = Integer.toString(Integer.parseInt(rValueStr) + 1);
                return id;
            }
            return spectrumID;
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

    public static List<String> validateMzIdentMLSchema(File resultFile) {
        List<String> errorMessages = null;
        ErrorHandlerIface handler = new ValidationErrorHandler();
        String schemaFilename = getSchemaByVersion(resultFile);
        logger.info("Validating mzIdentML XML schema for: " + resultFile.getPath() + " using schema: " + schemaFilename);

        try{
        GenericSchemaValidator genericValidator = new GenericSchemaValidator();
        URI url = MzIdentMLUtils.class.getClassLoader().getResource(schemaFilename).toURI();
        genericValidator.setSchema(url);

            genericValidator.setErrorHandler(handler);
            BufferedReader br = new BufferedReader(new FileReader(resultFile));
            genericValidator.validate(br);
            logger.info("XML schema validation competed on " + resultFile.getName());
            //noinspection unchecked
            errorMessages = handler.getErrorMessages(); // ToDo: make ErrorHandlerIface type safe
        } catch (IOException | SAXException e) {
            logger.error("Problem reading or parsing the file: ", e);
        } catch (URISyntaxException usi) {
            logger.error("Unable to parse URI syntax: ", usi);
        }
        return errorMessages;
    }

    /**
     * Search and find a list of search engine types from input parameter group.
     *
     * @return List<SearchEngineCvTermReference>  a list of search engine
     */
    public static List<SearchEngineParam> getSearchEngineCvTermReferences(List<CvParam> cvParams) {
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

    public static boolean containMGFTitleCVterm(List<uk.ac.ebi.jmzidml.model.mzidml.CvParam> listCVParam){
        if(listCVParam != null && listCVParam.size() > 0){
            for(uk.ac.ebi.jmzidml.model.mzidml.CvParam cv: listCVParam){
                if(cv.getAccession().equalsIgnoreCase(CvTermReference.MS_MGF_TITLE_INDEX.getAccession()))
                    return true;
            }
        }
        return false;

    }


    public static Comparable MGFTitleCVtermValue(List<uk.ac.ebi.jmzidml.model.mzidml.CvParam> cvParams) {
        if(cvParams != null && cvParams.size() > 0){
            for(uk.ac.ebi.jmzidml.model.mzidml.CvParam cv: cvParams){
                if(cv.getAccession().equalsIgnoreCase(CvTermReference.MS_MGF_TITLE_INDEX.getAccession()))
                    return cv.getValue();
            }
        }
        return null;
    }

    public static boolean isSpectraDataReferencedByTitle(SpectraData spectraData) {
        return spectraData.getSpectrumIDFormat() != null &&
                spectraData.getSpectrumIDFormat().getCvParam().getAccession().equalsIgnoreCase(CvTermReference.MS_ID_FORMAT_WIFF.getAccession()) &&
                spectraData.getFileFormat() != null && spectraData.getFileFormat().getCvParam().getAccession().equalsIgnoreCase(CvTermReference.MS_FILE_FORMAT_WIFF.getAccession());
    }

    /**
     * This CvParam override the current File format CVparam for those files that are native wiff but are mgf using title reference.
     * @return Return a CvPAram for the WIFF File
     */
    public static CvParam getFileFormatMGFTitle() {
        return CvUtilities.getCVTermFromCvReference(CvTermReference.MS_MGF_FILE_FORMAT, null);
    }

    public static CvParam getSpectrumIdFormatMGFTitle() {
        return CvUtilities.getCVTermFromCvReference(CvTermReference.MS_MGF_IDFORMAT_TITLE, null);
    }

    public static CvParam newCvParam(CvParam cv, String value){
        return new CvParam(cv.getAccession(), cv.getName(), cv.getCvLookupID(), value, cv.getUnitAcc(), cv.getUnitName(), cv.getUnitCVLookupID());
    }

    /**
     * Returns the corresponding schema file name based on the mzIdentML version
     *
     * @param resultFile MzIdentML file
     * @return Schema file name which has to be validate with
     */
    private static String getSchemaByVersion(File resultFile){
        MzIdentMLControllerImpl mzIdentMlController = new MzIdentMLControllerImpl(resultFile, false);
        String version = mzIdentMlController.getExperimentMetaData().getVersion();
        String schema = "mzIdentML" + mzIdentMlController.getExperimentMetaData().getVersion() + ".xsd";
        logger.debug("Validating MzIdentML version " + version + " with " + schema );
        return  schema;
    }
}
