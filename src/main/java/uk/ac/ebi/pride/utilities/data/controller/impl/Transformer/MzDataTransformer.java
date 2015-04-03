package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provide the functions to convert and transform mzData objects
 * to ms-core-api objetcs.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 3/15/12
 * Time: 9:19 AM
 */
public final class MzDataTransformer {

    /**
     * Private Constructor
     */
    private MzDataTransformer() {

    }

    /**
     * Convert spectrum
     *
     * @param spectrum jmzML spectrum object
     * @return Spectrum    spectrum
     */
    public static Spectrum transformSpectrum(uk.ac.ebi.pride.tools.jmzreader.model.Spectrum spectrum) {
        Spectrum newSpec = null;
        if (spectrum != null) {

            String specId = spectrum.getId();
            int index = -1; //spectrum.getIndex().intValue();
            String spotId = null; //spectrum.getSpotID();
            DataProcessing dataProcessing = null;  //transformDataProcessing(spectrum.getDataProcessing());
            int arrLen = -1; // spectrum.getDefaultArrayLength();
            SourceFile sourceFile = null; //transformSourceFile(spectrum.getSourceFile());
            ScanList scans = null; //transformScanList(spectrum.getScanList());
            List<ParamGroup> products = null; //transformProductList(spectrum.getProductList());
            List<Precursor> precursors = null;
            List<BinaryDataArray> binaryArray = transformBinaryDataArrayList(spectrum.getPeakList());

            ParamGroup paramGroup = new ParamGroup();
            CvTermReference cvTerm = CvTermReference.MS_LEVEL;
            CvParam cvParam = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), spectrum.getMsLevel().toString(), null, null, null);
            paramGroup.addCvParam(cvParam);
            if (spectrum.getPrecursorMZ() != null || spectrum.getPrecursorIntensity() != null || spectrum.getPrecursorCharge() != null) {
                precursors = new ArrayList<Precursor>();
                ParamGroup ionSelected = new ParamGroup();
                if (spectrum.getPrecursorMZ() != null) {
                    cvTerm = CvTermReference.ION_SELECTION_MZ;
                    cvParam = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), spectrum.getPrecursorMZ().toString(), null, null, null);
                    ionSelected.addCvParam(cvParam);
                }
                if (spectrum.getPrecursorCharge() != null) {
                    cvTerm = CvTermReference.ION_SELECTION_CHARGE_STATE;
                    cvParam = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), spectrum.getPrecursorCharge().toString(), null, null, null);
                    ionSelected.addCvParam(cvParam);
                }
                if (spectrum.getPrecursorIntensity() != null) {
                    cvTerm = CvTermReference.ION_SELECTION_INTENSITY;
                    cvParam = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), spectrum.getPrecursorIntensity().toString(), null, null, null);
                    ionSelected.addCvParam(cvParam);
                }
                List<ParamGroup> listIons = new ArrayList<ParamGroup>();
                listIons.add(ionSelected);
                Precursor precursor = new Precursor(null, null, null, null, listIons, null);
                precursors.add(precursor);
            }

            newSpec = new Spectrum(paramGroup, specId, null, index, dataProcessing, arrLen,
                    binaryArray, spotId, sourceFile, scans, precursors, products);
        }
        return newSpec;
    }

    /**
     * Convert param group
     *
     * @param oldParam jmzML param group
     * @param <T>      any jmzml objects which extends param group
     * @return ParamGroup  param group
     */
    public static <T extends uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Param> ParamGroup transformParamGroup(T oldParam) {
        ParamGroup newParamGroup = null;

        if (oldParam != null) {
            List<CvParam> cvParams = new ArrayList<CvParam>();
            List<UserParam> userParams = new ArrayList<UserParam>();

            //transformReferenceableParamGroup(cvParams, userParams, paramGroup.getReferenceableParamGroupRef());
            transformCvParam(cvParams, oldParam.getCvParams());
            transformUserParam(userParams, oldParam.getUserParams());
            newParamGroup = new ParamGroup(cvParams, userParams);
        }

        return newParamGroup;
    }

    /**
     * Convert a list of param groups
     *
     * @param oldParamGroupList a list of jmzML param groups
     * @param <T>               any jmzML objects extends param group
     * @return ParamGroup  param group
     */
    public static <T extends uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Param> List<ParamGroup> transformParamGroupList(
            List<T> oldParamGroupList) {
        List<ParamGroup> newParamGroupList = null;

        if (oldParamGroupList != null) {
            newParamGroupList = new ArrayList<ParamGroup>();
            for (T oldParamGroup : oldParamGroupList) {
                newParamGroupList.add(transformParamGroup(oldParamGroup));
            }
        }
        return newParamGroupList;
    }

    /**
     * Convert a list of cv params
     *
     * @param newCvParams a list of new cv params, where the converted value should go
     * @param oldCvParams a list of jmzml cv params
     * @return List<CvParam>   a list of cv params
     */
    private static List<CvParam> transformCvParam(List<CvParam> newCvParams, List<uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.CvParam> oldCvParams) {

        if (oldCvParams != null) {
            for (uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.CvParam oldParam : oldCvParams) {
                CvParam newParam = new CvParam(oldParam.getAccession(), oldParam.getName(), oldParam.getCvLabel(),
                        oldParam.getValue(), null, null, null);
                newCvParams.add(newParam);
            }
        }
        return newCvParams;
    }

    /**
     * Convert a list of user params
     *
     * @param newUserParams a list of new user params, where the converted value should go
     * @param oldUserParams a list of jmzml user params
     * @return List<UserParam> a list of user params
     */
    private static List<UserParam> transformUserParam(List<UserParam> newUserParams, List<uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.UserParam> oldUserParams) {

        if (oldUserParams != null) {
            for (uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.UserParam oldParam : oldUserParams) {
                UserParam newParam = new UserParam(oldParam.getName(), null,
                        oldParam.getValue(), null, null, null);
                newUserParams.add(newParam);
            }
        }

        return newUserParams;
    }

    /**
     * Convert to a List of BinaryDataArray a Peak List Map.
     *
     * @param peakList Peak List as A Double Array of mz - intensity values
     * @return List<BinaryDataArray> List of compress BinaryDataArray
     */

    private static List<BinaryDataArray> transformBinaryDataArrayList(Map<Double, Double> peakList) {
        List<BinaryDataArray> binaryDataArrays = new ArrayList<BinaryDataArray>();
        uk.ac.ebi.pride.utilities.term.CvTermReference cvRefMz = CvTermReference.MZ_ARRAY;
        CvParam cvParamMz = new CvParam(cvRefMz.getAccession(), cvRefMz.getName(), cvRefMz.getCvLabel(), "", cvRefMz.getAccession(), cvRefMz.getName(), cvRefMz.getCvLabel());
        ParamGroup mzParam = new ParamGroup(cvParamMz, null);

        uk.ac.ebi.pride.utilities.term.CvTermReference cvRefInt = CvTermReference.INTENSITY_ARRAY;
        CvParam cvParam = new CvParam(cvRefInt.getAccession(), cvRefInt.getName(), cvRefInt.getCvLabel(), "", cvRefInt.getAccession(), cvRefInt.getName(), cvRefInt.getCvLabel());
        ParamGroup intParam = new ParamGroup(cvParam, null);

        double[] intArray = new double[peakList.keySet().size()];
        double[] mzArray = new double[peakList.keySet().size()];
        int i = 0;

        Iterator iterator = peakList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            mzArray[i] = (Double) mapEntry.getKey();
            intArray[i] = (Double) mapEntry.getValue();
            i++;
        }

        //Todo: How you can know if the intensity correspond with the mz value?

        BinaryDataArray intBinaryArr = new BinaryDataArray(null, intArray, intParam);
        binaryDataArrays.add(intBinaryArr);
        BinaryDataArray mzBinaryArr = new BinaryDataArray(null, mzArray, mzParam);
        binaryDataArrays.add(mzBinaryArr);

        return binaryDataArrays;


    }

    /**
     * Convert cv loopups
     *
     * @param oldCvList jmzml cv list
     * @return List<CvLookup>  a list of cv lookups
     */
    public static List<CVLookup> transformCVList(List<uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.CvLookup> oldCvList) {
        List<CVLookup> cvLookups = null;
        if (oldCvList != null) {
            cvLookups = new ArrayList<CVLookup>();
            //List<uk.ac.ebi.jmzml.model.mzml.CV> oldCvs = oldCvList.getCv();
            for (uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.CvLookup oldCvLookup : oldCvList) {
                cvLookups.add(transformCVLookup(oldCvLookup));
            }
        }
        return cvLookups;
    }

    /**
     * Convert cv lookups
     *
     * @param oldCvLookup jmzml cv lookups
     * @return CVLookup    cv lookup
     */
    public static CVLookup transformCVLookup(uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.CvLookup oldCvLookup) {
        CVLookup cvLookup = null;
        if (oldCvLookup != null) {
            cvLookup = new CVLookup(oldCvLookup.getCvLabel(), oldCvLookup.getFullName(),
                    oldCvLookup.getVersion(), oldCvLookup.getAddress());
        }
        return cvLookup;
    }

    /**
     * Convert sample list
     *
     * @param oldSampleDescription jmzml sample list
     * @return List<Sample>    a list of samples
     */
    public static List<Sample> transformSampleList(uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Admin oldSampleDescription) {
        List<Sample> samples = null;

        if (oldSampleDescription != null) {
            samples = new ArrayList<Sample>();
            String id = oldSampleDescription.getSampleName();
            String name = oldSampleDescription.getSampleName();
            ParamGroup paramGroup = transformParamGroup(oldSampleDescription.getSampleDescription());
            Sample newSample = new Sample(paramGroup, id, name);
            samples.add(newSample);
        }
        return samples;
    }

    /**
     * Convert software list
     *
     * @param oldSoftware a list of jmzml software
     * @return List<Software>  a list of sfotware
     */
    public static List<Software> transformSoftware(uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Software oldSoftware) {
        List<Software> softwares = null;
        if (oldSoftware != null) {
            softwares = new ArrayList<Software>();
            String id = oldSoftware.getName();
            String version = oldSoftware.getVersion();
            String name = oldSoftware.getName();
            Software newSoftware = new Software(null, id, name, null, null, null, version);
            //Todo: It is practically imposibble to match the data from this to ParamGroups
            // Todo: In the future will be interesting to show the attributes and not the ParamGroups
            softwares.add(newSoftware);
        }
        return softwares;
    }

    /**
     * Convert instrument configuration
     *
     * @param oldInstrument mzData instrument configuration
     * @return Instrumentconfiguration insturment configuration
     */
    public static List<InstrumentConfiguration> transformInstrumentConfiguration(uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.InstrumentDescription oldInstrument) {

        List<InstrumentConfiguration> instrumentConfigurations = null;

        if (oldInstrument != null) {
            instrumentConfigurations = new ArrayList<InstrumentConfiguration>();

            InstrumentConfiguration instrumentConfiguration;
            String id = oldInstrument.getInstrumentName();
            ScanSetting scanSetting = null;
            //Todo: It could be vry interesting to capture the information about the ScanSettings

            // convert software
            Software software = null;

            // convert component list
            List<InstrumentComponent> source = null;
            List<InstrumentComponent> detector = null;
            List<InstrumentComponent> analyzer = null;

            if (oldInstrument.getAnalyzerList() != null || oldInstrument.getDetector() != null || oldInstrument.getSource() != null) {
                int i = 0;
                if (oldInstrument.getSource() != null) {
                    source = new ArrayList<InstrumentComponent>();
                    InstrumentComponent newSource = transformInstrumentComponent(i, oldInstrument.getSource());
                    source.add(newSource);
                    i++;
                }
                if (oldInstrument.getDetector() != null) {
                    detector = new ArrayList<InstrumentComponent>();
                    InstrumentComponent newDetector = transformInstrumentComponent(i, oldInstrument.getSource());
                    detector.add(newDetector);
                    i++;
                }
                if (oldInstrument.getAnalyzerList() != null) {
                    analyzer = new ArrayList<InstrumentComponent>();
                    for (uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Param param : oldInstrument.getAnalyzerList().getAnalyzer()) {
                        InstrumentComponent newAnalyzer = transformInstrumentComponent(i, param);
                        i++;
                        analyzer.add(newAnalyzer);
                    }
                }
            }
            ParamGroup paramGroup = transformParamGroup(oldInstrument.getAdditional());
            instrumentConfiguration = new InstrumentConfiguration(id, scanSetting, software, source, analyzer, detector, paramGroup);
            instrumentConfigurations.add(instrumentConfiguration);
        }
        return instrumentConfigurations;
    }

    /**
     * Convert instrument component
     *
     * @param rawParamGroup jmzml instrument component
     * @return InstrumentComponent instrument component
     */
    private static InstrumentComponent transformInstrumentComponent(int order, uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Param rawParamGroup) {
        InstrumentComponent component = null;

        if (rawParamGroup != null) {
            component = new InstrumentComponent(order, transformParamGroup(rawParamGroup));
        }

        return component;
    }

    /**
     * Convert data processing
     *
     * @param oldDataProcessing jmzml data processing
     * @return DataProcessing  data processing
     */
    public static List<DataProcessing> transformDataProcessing(uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.DataProcessing oldDataProcessing) {
        List<DataProcessing> dataProcessings = null;
        if (oldDataProcessing != null) {
            dataProcessings = new ArrayList<DataProcessing>();
            List<ProcessingMethod> methods = null;

            if (oldDataProcessing.getSoftware() != null || oldDataProcessing.getProcessingMethod() != null) {
                methods = new ArrayList<ProcessingMethod>();
                Software software = null;
                ParamGroup paramGroup = null;
                if (oldDataProcessing.getSoftware() != null) {
                    List<Software> softwares = transformSoftware(oldDataProcessing.getSoftware());
                    software = softwares.get(0);
                }
                if (oldDataProcessing.getProcessingMethod() != null) {
                    paramGroup = transformParamGroup(oldDataProcessing.getProcessingMethod());
                }
                ProcessingMethod processingMethod = new ProcessingMethod(0, software, paramGroup);
                methods.add(processingMethod);
            }
            dataProcessings.add(new DataProcessing(null, methods));
        }
        return dataProcessings;
    }

    /**
     * Transform FileDescription object to List of SourceFile
     *
     * @param oldSourceFile mzData FileDescription Object
     * @return List<SourceFile> List of Source Files used in the MzMl
     */
    public static List<SourceFile> transformToFileSource(uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.SourceFile oldSourceFile) {
        List<SourceFile> sourceFiles = null;
        if (oldSourceFile != null) {
            sourceFiles = new ArrayList<SourceFile>();
            String name = oldSourceFile.getNameOfFile();
            String id = oldSourceFile.getNameOfFile();
            String path = oldSourceFile.getPathToFile();
            ParamGroup paramGroup = null;
            CvParam fileFormat = null;
            if (oldSourceFile.getFileType() != null) {
                paramGroup = new ParamGroup();
                CvTermReference cvReference = CvTermReference.MS_FILE_SPECTRUM;
                // Kepp here the information of the file Type, is the only solution to convert mzXML attributes to CvTerms
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), oldSourceFile.getFileType(), null, null, null);
                paramGroup.addCvParam(cvParam);
                fileFormat = cvParam;
            }
            SourceFile newSourceFile = new SourceFile(paramGroup, id, name, path, fileFormat, null);
            sourceFiles.add(newSourceFile);
        }
        return sourceFiles;
    }

    /**
     * Method to retrieve the Contact Persons From the FileDescription Object in the mzData Files
     *
     * @param oldPersons mzData Person Structure to convert to Person Contact
     * @return List<Person> List of Person Contacts
     */
    public static List<Person> transformToPerson(List<uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Person> oldPersons) {
        if (oldPersons != null) {
            List<Person> persons = new ArrayList<Person>();
            for (uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.Person contact : oldPersons) {
                ParamGroup paramGroup = new ParamGroup();
                CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
                CvParam cvParam = new CvParam(contactTerm.getAccession(), contactTerm.getName(), contactTerm.getCvLabel(), contact.getName(), null, null, null);
                paramGroup.addCvParam(cvParam);
                contactTerm = CvTermReference.CONTACT_EMAIL;
                cvParam = new CvParam(contactTerm.getAccession(), contactTerm.getName(), contactTerm.getCvLabel(), contact.getContactInfo(), null, null, null);
                paramGroup.addCvParam(cvParam);
                Person person = new Person(paramGroup, contact.getName(), contact.getContactInfo());
                persons.add(person);
            }
            return persons;
        }
        return null;
    }
}
