package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.lightModel.SpectraData;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for converting light modules (that are used for fast validation) into other model objects or vice versa
 * <p>
 * Other Model objects used:
 * 1. uk.ac.ebi.jmzidml.model.mzidml
 * 2. uk.ac.ebi.pride.utilities.data.core
 * 3. uk.ac.ebi.pride.archive.repo.assay
 */
@Slf4j
public class LightModelsTransformer{

    // TODO: This method should be removed.
    public static uk.ac.ebi.jmzidml.model.mzidml.SpectraData transformSpectraDataToJmzidml(SpectraData spectraData) {

        try {
            uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraDataJmzidml = new uk.ac.ebi.jmzidml.model.mzidml.SpectraData();
            uk.ac.ebi.jmzidml.model.mzidml.SpectrumIDFormat spectrumIDFormatJmzidml = new uk.ac.ebi.jmzidml.model.mzidml.SpectrumIDFormat();
            uk.ac.ebi.jmzidml.model.mzidml.CvParam CVParamJmzidml = new uk.ac.ebi.jmzidml.model.mzidml.CvParam();

            CVParamJmzidml.setAccession(spectraData.getSpectrumIDFormat().getCvParam().getAccession());
            spectrumIDFormatJmzidml.setCvParam(CVParamJmzidml);

            spectraDataJmzidml.setSpectrumIDFormat(spectrumIDFormatJmzidml);
            spectraDataJmzidml.setLocation(spectraData.getLocation());
            spectraDataJmzidml.setId(spectraData.getId());

            return spectraDataJmzidml;
        } catch (Exception ex) {
            log.error("Error occurred while converting uk.ac.ebi.pride.utilities.data.lightModel.SpectraData " +
                    "to uk.ac.ebi.jmzidml.model.mzidml.SpectraData");
            return null;
        }
    }

    /**
     * This method converts the Software from uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware
     * to uk.ac.ebi.pride.utilities.data.core.Software object
     *
     * @param analysisSoftware uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware object
     * @return uk.ac.ebi.pride.utilities.data.core.Software object
     */
    public static Software transformToSoftware(uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware analysisSoftware) {
        try {
            Comparable id = analysisSoftware.getId();
            String name = analysisSoftware.getName();
            Contact contact = null;
            String customization = analysisSoftware.getCustomizations();
            String uri = analysisSoftware.getUri();
            String version = analysisSoftware.getVersion();
            return new Software(id, name, contact, customization, uri, version);
        } catch (Exception ex) {
            log.error("Error occurred while converting uk.ac.ebi.pride.utilities.data.lightModel.AnalysisSoftware " +
                    "to uk.ac.ebi.pride.utilities.data.core.Software");
            return null;
        }
    }

    /**
     * This method converts list of SourceFile from uk.ac.ebi.pride.utilities.data.lightModel.SourceFile
     * to uk.ac.ebi.pride.utilities.data.core.SourceFile objects
     *
     * @param sourceFilesLight List of uk.ac.ebi.pride.utilities.data.lightModel.SourceFile objects
     * @return List of uk.ac.ebi.pride.utilities.data.core.SourceFile objects
     */
    public static List<SourceFile> transformToSourceFiles(List<uk.ac.ebi.pride.utilities.data.lightModel.SourceFile> sourceFilesLight){
        List<SourceFile> sourceFiles = null;

        if (sourceFilesLight != null) {
            sourceFiles = new ArrayList<>();
            for (uk.ac.ebi.pride.utilities.data.lightModel.SourceFile sourceFileLight : sourceFilesLight) {
                String id = sourceFileLight.getId();
                String name = sourceFileLight.getName();
                String location = sourceFileLight.getLocation();
                uk.ac.ebi.pride.utilities.data.lightModel.CvParam fileFormat = (sourceFileLight.getFileFormat() != null) ? sourceFileLight.getFileFormat().getCvParam() : null;
                CvParam format = transformToCvParam(fileFormat);
                String formatDocumentation = sourceFileLight.getExternalFormatDocumentation();

                List<CvParam> cvParams = transformToCvParams(sourceFileLight.getCvParam());
                List<UserParam> userParams = transformToUserParams(sourceFileLight.getUserParam());
                sourceFiles.add(new SourceFile(new ParamGroup(cvParams, userParams), id, name, location, format, formatDocumentation));



//        ParamGroup params = sourceFileLight.getParamGroup();
//        List<CvParam> cvParams = new ArrayList<>();
//        for (uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParam: sourceFileLight.getCvParam()){
//            cvParams.add(transformToCvParam(cvParam));
//        }
//        CvParam fileFormat = ;
//                String externalFormatDocumentationURI = sourceFileLight.get;
//                sourceFiles.add(new SourceFile());
            }
        }/**/
        return sourceFiles;
    }

    /**
     * This method converts list of CvParams from uk.ac.ebi.pride.utilities.data.lightModel.CvParam
     * to uk.ac.ebi.pride.utilities.data.core.CvParam object
     *
     * @param cvParamsLight List of uk.ac.ebi.pride.utilities.data.lightModel.CvParam objects
     * @return List of uk.ac.ebi.pride.utilities.data.core.CvParam objects
     */
    public static List<CvParam> transformToCvParams(List<uk.ac.ebi.pride.utilities.data.lightModel.CvParam> cvParamsLight){
        List<CvParam> cvParams = null;
        if(cvParams != null) {
            for (uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight : cvParamsLight) {
                cvParams.add(transformToCvParam(cvParamLight));
            }
        }
        return cvParams;
    }

    /**
     * This method converts uk.ac.ebi.pride.utilities.data.lightModel.CvParam object
     * to uk.ac.ebi.pride.utilities.data.core.CvParam object
     *
     * @param cvParamLight uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight
     * @return uk.ac.ebi.pride.utilities.data.core.CvParam object
     */
    public static CvParam transformToCvParam(uk.ac.ebi.pride.utilities.data.lightModel.CvParam cvParamLight){
        CvParam newParam = null;
        if(cvParamLight != null) {
            String cvLookupID = null;
            String unitCVLookupID = null;
            uk.ac.ebi.pride.utilities.data.lightModel.Cv cv = cvParamLight.getUnitCv();
            if (cv != null) {
                cvLookupID = cv.getId();
            }
            newParam = new CvParam(
                    cvParamLight.getAccession(),
                    cvParamLight.getName(),
                    cvLookupID,
                    cvParamLight.getValue(),
                    cvParamLight.getUnitAccession(),
                    cvParamLight.getUnitName(),
                    unitCVLookupID); //TODO: do we need this?
        }
        return newParam;
    }

    /**
     * This method converts list of UserParam from uk.ac.ebi.pride.utilities.data.lightModel.UserParam
     * to uk.ac.ebi.pride.utilities.data.core.UserParam object
     *
     * @param userParamsLight List of uk.ac.ebi.pride.utilities.data.lightModel.UserParam objects
     * @return List of uk.ac.ebi.pride.utilities.data.core.UserParam objects
     */
    private static List<UserParam> transformToUserParams(List<uk.ac.ebi.pride.utilities.data.lightModel.UserParam> userParamsLight) {
        List<UserParam> userParams = null;
        if (userParamsLight != null) {
            userParams = new ArrayList<UserParam>();
            for (uk.ac.ebi.pride.utilities.data.lightModel.UserParam userParam : userParamsLight) {
                userParams.add(transformToUserParam(userParam));
            }
        }
        return userParams;
    }

    /**
     * This method converts uk.ac.ebi.pride.utilities.data.lightModel.UserParam object
     * to uk.ac.ebi.pride.utilities.data.core.UserParam object
     *
     * @param userParam uk.ac.ebi.pride.utilities.data.lightModel.UserParam object
     * @return uk.ac.ebi.pride.utilities.data.core.UserParam object
     */
    private static UserParam transformToUserParam(uk.ac.ebi.pride.utilities.data.lightModel.UserParam userParam) {
        UserParam newParam = null;
        if (userParam != null) {
            String unitCVLookupID = null;
            uk.ac.ebi.pride.utilities.data.lightModel.Cv cv = userParam.getUnitCv();
            if (cv != null) unitCVLookupID = cv.getId();
            newParam = new UserParam(userParam.getName(),
                    userParam.getType(),
                    userParam.getValue(),
                    userParam.getUnitAccession(),
                    userParam.getUnitName(),
                    unitCVLookupID);
        }
        return newParam;
    }
}
