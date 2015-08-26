package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.jmzml.model.mzml.*;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.BinaryDataArray;
import uk.ac.ebi.pride.utilities.data.core.Chromatogram;
import uk.ac.ebi.pride.utilities.data.core.DataProcessing;
import uk.ac.ebi.pride.utilities.data.core.InstrumentConfiguration;
import uk.ac.ebi.pride.utilities.data.core.ParamGroup;
import uk.ac.ebi.pride.utilities.data.core.Precursor;
import uk.ac.ebi.pride.utilities.data.core.ProcessingMethod;
import uk.ac.ebi.pride.utilities.data.core.ReferenceableParamGroup;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Scan;
import uk.ac.ebi.pride.utilities.data.core.ScanList;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.utils.BinaryDataUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.nio.ByteOrder;
import java.util.*;

/**
 * This class contains a set of static methods for converting jmzML object to the pride inspector core objects
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public final class MzMLTransformer {

    /**
     * Private Constructor
     */
    private MzMLTransformer() {

    }

    /**
     * Convert spectrum
     *
     * @param spectrum jmzML spectrum object
     * @return Spectrum spectrum
     */
    public static Spectrum transformSpectrum(uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum) {
        Spectrum newSpec = null;
        if (spectrum != null) {

            String specId = spectrum.getId();
            int index = spectrum.getIndex();
            String spotId = spectrum.getSpotID();
            DataProcessing dataProcessing = transformDataProcessing(spectrum.getDataProcessing());
            int arrLen = spectrum.getDefaultArrayLength();
            SourceFile sourceFile = transformSourceFile(spectrum.getSourceFile());
            ScanList scans = transformScanList(spectrum.getScanList());
            List<Precursor> precursors = transformPrecursorList(spectrum.getPrecursorList());
            List<ParamGroup> products = transformProductList(spectrum.getProductList());
            List<BinaryDataArray> binaryArray = transformBinaryDataArrayList(spectrum.getBinaryDataArrayList());
            ParamGroup paramGroup = transformParamGroup(spectrum);

            CvTermReference cvTerm = CvTermReference.ION_SELECTION_CHARGE_STATE;
            CvParam cvParam = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), getPrecursorIonCharge(spectrum), null, null, null);
            paramGroup.addCvParam(cvParam);


            newSpec = new Spectrum(paramGroup, specId, null, index, dataProcessing, arrLen,
                    binaryArray, spotId, sourceFile, scans, precursors, products);
        }
        return newSpec;
    }

    private static String getPrecursorIonCharge(uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum) {
        if(spectrum != null && spectrum.getPrecursorList() != null &&
                spectrum.getPrecursorList().getPrecursor() != null &&
                ! spectrum.getPrecursorList().getPrecursor().isEmpty()){
            for(uk.ac.ebi.jmzml.model.mzml.Precursor precursor: spectrum.getPrecursorList().getPrecursor()){
                if(precursor != null && precursor.getSelectedIonList() != null && precursor.getSelectedIonList().getSelectedIon() != null){
                    for(uk.ac.ebi.jmzml.model.mzml.ParamGroup term: precursor.getSelectedIonList().getSelectedIon()){
                        if(term != null && term.getCvParam() != null && ! term.getCvParam().isEmpty()){
                            for(CVParam param: term.getCvParam()){
                                if(param != null && param.getAccession() != null){
                                    if((param.getAccession().equalsIgnoreCase(CvTermReference.PSI_ION_SELECTION_CHARGE_STATE.getAccession()) ||
                                       param.getAccession().equalsIgnoreCase(CvTermReference.ION_SELECTION_CHARGE_STATE.getAccession())) &&
                                            param.getValue() != null && !param.getValue().isEmpty()){
                                        return param.getValue();
                                    }

                                }
                            }
                        }
                    }
                }

            }
        }
        return null;
    }

    private static String getPrecursorMSLevel(uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum) {
        if(spectrum != null && spectrum.getPrecursorList() != null &&
                spectrum.getPrecursorList().getPrecursor() != null &&
                ! spectrum.getPrecursorList().getPrecursor().isEmpty()){
            for(uk.ac.ebi.jmzml.model.mzml.Precursor precursor: spectrum.getPrecursorList().getPrecursor()){
                if(precursor != null && precursor.getSelectedIonList() != null && precursor.getSelectedIonList().getSelectedIon() != null){
                    for(uk.ac.ebi.jmzml.model.mzml.ParamGroup term: precursor.getSelectedIonList().getSelectedIon()){
                        if(term != null && term.getCvParam() != null && ! term.getCvParam().isEmpty()){
                            for(CVParam param: term.getCvParam()){
                                if(param != null && param.getAccession() != null){
                                    if((param.getAccession().equalsIgnoreCase(CvTermReference.MS_LEVEL.getAccession())) &&
                                            param.getValue() != null && !param.getValue().isEmpty()){
                                        return param.getValue();
                                    }

                                }
                            }
                        }
                    }
                }

            }
        }
        return null;
    }
    /**
     * Convert param group
     *
     * @param paramGroup jmzML param group
     * @param <T>        any jmzml objects which extends param group
     * @return ParamGroup  param group
     */
    public static <T extends uk.ac.ebi.jmzml.model.mzml.ParamGroup> ParamGroup transformParamGroup(T paramGroup) {
        ParamGroup newParamGroup = null;

        if (paramGroup != null) {
            List<CvParam> cvParams = new ArrayList<CvParam>();
            List<UserParam> userParams = new ArrayList<UserParam>();

            transformReferenceableParamGroup(cvParams, userParams, paramGroup.getReferenceableParamGroupRef());
            transformCvParam(cvParams, paramGroup.getCvParam());
            transformUserParam(userParams, paramGroup.getUserParam());
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
    public static <T extends uk.ac.ebi.jmzml.model.mzml.ParamGroup> List<ParamGroup> transformParamGroupList(
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
     * Convert referenceable param group
     *
     * @param oldRefParamGroupList jmzml referenceable param group
     * @return ReferenceableParamGroup param group
     */
    public static ReferenceableParamGroup transformReferenceableParamGroupList(uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroupList oldRefParamGroupList) {
        Map<String, ParamGroup> refMap = null;
        if (oldRefParamGroupList != null) {
            refMap = new HashMap<String, ParamGroup>();
            List<uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroup> oldRefParamGroups = oldRefParamGroupList.getReferenceableParamGroup();
            for (uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroup oldRefParamGroup : oldRefParamGroups) {
                String id = oldRefParamGroup.getId();
                List<CvParam> cvParams = new ArrayList<CvParam>();
                List<UserParam> userParams = new ArrayList<UserParam>();

                transformCvParam(cvParams, oldRefParamGroup.getCvParam());
                transformUserParam(userParams, oldRefParamGroup.getUserParam());
                ParamGroup newParamGroup = new ParamGroup(cvParams, userParams);
                refMap.put(id, newParamGroup);
            }
        }
        return new ReferenceableParamGroup(refMap);
    }

    /**
     * Convert a list of cv params
     *
     * @param newCvParams a list of new cv params, where the converted value should go
     * @param oldCvParams a list of jmzml cv params
     * @return List<CvParam>   a list of cv params
     */
    private static List<CvParam> transformCvParam(List<CvParam> newCvParams, List<uk.ac.ebi.jmzml.model.mzml.CVParam> oldCvParams) {

        if (oldCvParams != null) {
            for (uk.ac.ebi.jmzml.model.mzml.CVParam oldParam : oldCvParams) {
                String cvLookupID = null;
                uk.ac.ebi.jmzml.model.mzml.CV cv = oldParam.getCv();
                if (cv != null)
                    cvLookupID = cv.getId();
                String unitCVLookupID = null;
                cv = oldParam.getCv();
                if (cv != null)
                    unitCVLookupID = cv.getId();
                CvParam newParam = new CvParam(oldParam.getAccession(), oldParam.getName(), cvLookupID,
                        oldParam.getValue(), oldParam.getUnitAccession(),
                        oldParam.getUnitName(), unitCVLookupID);
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
    private static List<UserParam> transformUserParam(List<UserParam> newUserParams, List<uk.ac.ebi.jmzml.model.mzml.UserParam> oldUserParams) {

        if (oldUserParams != null) {
            for (uk.ac.ebi.jmzml.model.mzml.UserParam oldParam : oldUserParams) {
                String unitCVLookupID = null;
                uk.ac.ebi.jmzml.model.mzml.CV cv = oldParam.getUnitCv();
                if (cv != null)
                    unitCVLookupID = cv.getId();
                UserParam newParam = new UserParam(oldParam.getName(), oldParam.getType(),
                        oldParam.getValue(), oldParam.getUnitAccession(),
                        oldParam.getUnitName(), unitCVLookupID);
                newUserParams.add(newParam);
            }
        }

        return newUserParams;
    }

    /**
     * Convert a referenceable param group to a list of cv params and a list of user params
     *
     * @param cvParams   a list of new cv params
     * @param userParams a list of new user params
     * @param paramRefs  a list of jmzml referenceable paramgroups
     */
    private static void transformReferenceableParamGroup(List<CvParam> cvParams,
                                                         List<UserParam> userParams,
                                                         List<uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroupRef> paramRefs) {
        if (paramRefs != null) {
            for (uk.ac.ebi.jmzml.model.mzml.ReferenceableParamGroupRef ref : paramRefs) {
                transformCvParam(cvParams, ref.getReferenceableParamGroup().getCvParam());
                transformUserParam(userParams, ref.getReferenceableParamGroup().getUserParam());
            }
        }
    }

    /**
     * Convert a list of binary data array
     *
     * @param binaryDataArrayList jmzml binary data array list
     * @return List<BinaryDataArray>   a list of binary data array
     */
    private static List<BinaryDataArray> transformBinaryDataArrayList(uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList binaryDataArrayList) {
        List<BinaryDataArray> dataArrs = null;

        if (binaryDataArrayList != null) {
            dataArrs = new ArrayList<BinaryDataArray>();
            List<uk.ac.ebi.jmzml.model.mzml.BinaryDataArray> oldDataArrs = binaryDataArrayList.getBinaryDataArray();

            for (uk.ac.ebi.jmzml.model.mzml.BinaryDataArray oldBinaryArr : oldDataArrs) {
                dataArrs.add(transformBinaryDataArray(oldBinaryArr));
            }
        }

        return dataArrs;
    }

    /**
     * Convert binary data array
     *
     * @param oldBinaryArr jmzml binary data array
     * @return BinaryDataArray binary data array
     */
    private static BinaryDataArray transformBinaryDataArray(uk.ac.ebi.jmzml.model.mzml.BinaryDataArray oldBinaryArr) {
        BinaryDataArray newBinaryArr = null;

        if (oldBinaryArr != null) {

            byte[] binary = oldBinaryArr.getBinary();

            ParamGroup paramGroup = transformParamGroup(oldBinaryArr);

            CvTermReference binaryDataType = null;
            boolean isCompressed = false;
            List<CvParam> cvParams = paramGroup.getCvParams();
            for (CvParam cvParam : cvParams) {
                String acc = cvParam.getAccession();
                if (CvTermReference.isChild(CvTermReference.BINARY_DATA_TYPE.getAccession(), acc)) {
                    binaryDataType = CvTermReference.getCvRefByAccession(acc);
                } else if (CvTermReference.ZLIB_COMPRESSION.getAccession().equals(acc)) {
                    isCompressed = true;
                }
            }

            if (isCompressed) {
                binary = BinaryDataUtils.decompress(binary);
            }

            double[] binaryDoubleArr = BinaryDataUtils.toDoubleArray(binary, binaryDataType, ByteOrder.LITTLE_ENDIAN);
            DataProcessing dataProcessing = transformDataProcessing(oldBinaryArr.getDataProcessing());

            newBinaryArr = new BinaryDataArray(dataProcessing, binaryDoubleArr, paramGroup);
        }

        return newBinaryArr;
    }

    /**
     * Convert product list
     *
     * @param productList jmzml product list
     * @return List<ParamGroup>    a list of param group
     */
    private static List<ParamGroup> transformProductList(uk.ac.ebi.jmzml.model.mzml.ProductList productList) {
        List<ParamGroup> products = null;

        if (productList != null) {
            products = new ArrayList<ParamGroup>();
            List<uk.ac.ebi.jmzml.model.mzml.Product> oldProducts = productList.getProduct();

            for (uk.ac.ebi.jmzml.model.mzml.Product oldProduct : oldProducts) {
                uk.ac.ebi.jmzml.model.mzml.ParamGroup isowin = oldProduct.getIsolationWindow();
                ParamGroup newIsoWin = transformParamGroup(isowin);
                products.add(newIsoWin);
            }
        }

        return products;
    }

    /**
     * Convert precursor list
     *
     * @param precursorList jmzml precursor list
     * @return List<Precursor> a list of precursor
     */
    private static List<Precursor> transformPrecursorList(uk.ac.ebi.jmzml.model.mzml.PrecursorList precursorList) {
        List<Precursor> precursors = null;

        if (precursorList != null) {
            precursors = new ArrayList<Precursor>();
            List<uk.ac.ebi.jmzml.model.mzml.Precursor> oldPrecursors = precursorList.getPrecursor();
            for (uk.ac.ebi.jmzml.model.mzml.Precursor oldPrecursor : oldPrecursors) {
                precursors.add(transformPrecursor(oldPrecursor));
            }
        }

        return precursors;
    }

    /**
     * Convert precursor
     *
     * @param oldPrecursor jmzml precursor
     * @return Precursor   precursor
     */
    private static Precursor transformPrecursor(uk.ac.ebi.jmzml.model.mzml.Precursor oldPrecursor) {
        Precursor newPrecursor = null;

        if (oldPrecursor != null) {
            Spectrum parentSpectrum = transformSpectrum(oldPrecursor.getSpectrum());
            SourceFile sourceFile = transformSourceFile(oldPrecursor.getSourceFile());
            String externalSpectrumID = oldPrecursor.getExternalSpectrumID();
            ParamGroup isolationWindow = transformParamGroup(oldPrecursor.getIsolationWindow());
            uk.ac.ebi.jmzml.model.mzml.SelectedIonList oldSelectedIonList = oldPrecursor.getSelectedIonList();
            List<ParamGroup> selectedIon = null;
            if (oldSelectedIonList != null)
                selectedIon = transformParamGroupList(oldSelectedIonList.getSelectedIon());
            ParamGroup activation = transformParamGroup(oldPrecursor.getActivation());
            newPrecursor = new Precursor(parentSpectrum, sourceFile,
                    externalSpectrumID, isolationWindow,
                    selectedIon, activation);
        }

        return newPrecursor;
    }

    /**
     * Convert scan list
     *
     * @param scanList jmzml scan list
     * @return ScanList    scan list
     */
    private static ScanList transformScanList(uk.ac.ebi.jmzml.model.mzml.ScanList scanList) {
        ScanList newScanList = null;

        if (scanList != null) {
            List<Scan> scans = new ArrayList<Scan>();
            List<uk.ac.ebi.jmzml.model.mzml.Scan> oldScans = scanList.getScan();

            for (uk.ac.ebi.jmzml.model.mzml.Scan oldScan : oldScans) {
                scans.add(transformScan(oldScan));
            }
            ParamGroup paramGroup = transformParamGroup(scanList);
            newScanList = new ScanList(scans, paramGroup);
        }

        return newScanList;
    }

    /**
     * Convert scan
     *
     * @param oldScan jmzml scan
     * @return Scan    scan
     */
    private static Scan transformScan(uk.ac.ebi.jmzml.model.mzml.Scan oldScan) {
        Scan newScan = null;

        if (oldScan != null) {
            String spectrum = oldScan.getSpectrumRef();
            String externalSpecRef = oldScan.getExternalSpectrumID();
            SourceFile sourceFile = transformSourceFile(oldScan.getSourceFile());
            InstrumentConfiguration instrumentConfiguration = transformInstrumentConfiguration(oldScan.getInstrumentConfiguration());
            List<ParamGroup> scanWindows = null;
            uk.ac.ebi.jmzml.model.mzml.ScanWindowList oldScanWinList = oldScan.getScanWindowList();
            if (oldScanWinList != null) {
                scanWindows = transformParamGroupList(oldScanWinList.getScanWindow());
            }
            ParamGroup paramGroup = transformParamGroup(oldScan);
            newScan = new Scan(spectrum, externalSpecRef, sourceFile, instrumentConfiguration, scanWindows, paramGroup);
        }

        return newScan;
    }

    /**
     * Convert source file list
     *
     * @param oldSourceFileList jmzml source file list
     * @return List<SourceFile>    a list of source file
     */
    public static List<SourceFile> transformSourceFileList(uk.ac.ebi.jmzml.model.mzml.SourceFileList oldSourceFileList) {
        List<SourceFile> sourceFiles = null;
        if (oldSourceFileList != null) {
            sourceFiles = new ArrayList<SourceFile>();
            List<uk.ac.ebi.jmzml.model.mzml.SourceFile> oldSourceFiles = oldSourceFileList.getSourceFile();
            for (uk.ac.ebi.jmzml.model.mzml.SourceFile oldSourceFile : oldSourceFiles) {
                sourceFiles.add(transformSourceFile(oldSourceFile));
            }
        }
        return sourceFiles;
    }

    /**
     * Convert source file
     *
     * @param oldSourceFile jmzml source file
     * @return SourceFile  source file
     */
    private static SourceFile transformSourceFile(uk.ac.ebi.jmzml.model.mzml.SourceFile oldSourceFile) {
        SourceFile newSourceFile = null;

        if (oldSourceFile != null) {
            String name = oldSourceFile.getName();
            String id = oldSourceFile.getId();
            String path = oldSourceFile.getLocation();
            ParamGroup paramGroup = transformParamGroup(oldSourceFile);
            newSourceFile = new SourceFile(paramGroup, id, name, path);
        }

        return newSourceFile;
    }

    /**
     * Convert scan setting
     *
     * @param oldScanSettings jmzml scan settings
     * @return ScanSetting scan setting
     */
    private static ScanSetting transformScanSettings(uk.ac.ebi.jmzml.model.mzml.ScanSettings oldScanSettings) {
        ScanSetting newScanSetting = null;

        if (oldScanSettings != null) {
            String id = oldScanSettings.getId();
            List<SourceFile> sourceFile = new ArrayList<SourceFile>();
            // ToDo: this might need to improve
            List<uk.ac.ebi.jmzml.model.mzml.SourceFileRef> oldSourceFileRefs = oldScanSettings.getSourceFileRefList().getSourceFileRef();
            for (uk.ac.ebi.jmzml.model.mzml.SourceFileRef oldSourceFileRef : oldSourceFileRefs) {
                sourceFile.add(transformSourceFile(oldSourceFileRef.getSourceFile()));
            }
            List<ParamGroup> targets = transformParamGroupList(oldScanSettings.getTargetList().getTarget());
            ParamGroup paramGroup = transformParamGroup(oldScanSettings);
            newScanSetting = new ScanSetting(id, sourceFile, targets, paramGroup);
        }
        return newScanSetting;
    }

    /**
     * Convert processsing method
     *
     * @param oldProcMethod jmzml processing method
     * @return ProcessingMethod    processing method
     */
    private static ProcessingMethod transformProcessingMethod(uk.ac.ebi.jmzml.model.mzml.ProcessingMethod oldProcMethod) {
        ProcessingMethod newProcessingMethod = null;

        if (oldProcMethod != null) {
            int order = oldProcMethod.getOrder();
            Software software = transformSoftware(oldProcMethod.getSoftware());
            ParamGroup paramGroup = transformParamGroup(oldProcMethod);
            newProcessingMethod = new ProcessingMethod(order, software, paramGroup);
        }
        return newProcessingMethod;
    }

    /**
     * Convert chromatogram
     *
     * @param chroma jmzml chromatogram
     * @return Chromatogram    chromatogram
     */
    public static Chromatogram transformChromatogram(uk.ac.ebi.jmzml.model.mzml.Chromatogram chroma) {
        Chromatogram newChroma = null;

        if (chroma != null) {
            String id = chroma.getId();
            int index = chroma.getIndex();
            DataProcessing dataProcessing = transformDataProcessing(chroma.getDataProcessing());
            int arrLength = chroma.getDefaultArrayLength();
            List<BinaryDataArray> binaryArr = transformBinaryDataArrayList(chroma.getBinaryDataArrayList());
            ParamGroup paramGroup = transformParamGroup(chroma);
            newChroma = new Chromatogram(paramGroup, id, null, index, dataProcessing, arrLength, binaryArr);
        }

        return newChroma;
    }

    /**
     * Convert cv loopups
     *
     * @param oldCvList jmzml cv list
     * @return List<CvLookup>  a list of cv lookups
     */
    public static List<CVLookup> transformCVList(uk.ac.ebi.jmzml.model.mzml.CVList oldCvList) {
        List<CVLookup> cvLookups = null;
        if (oldCvList != null) {
            cvLookups = new ArrayList<CVLookup>();
            List<uk.ac.ebi.jmzml.model.mzml.CV> oldCvs = oldCvList.getCv();
            for (uk.ac.ebi.jmzml.model.mzml.CV oldCV : oldCvs) {
                cvLookups.add(transformCVLookup(oldCV));
            }
        }
        return cvLookups;
    }

    /**
     * Convert cv lookups
     *
     * @param oldCv jmzml cv lookups
     * @return CVLookup    cv lookup
     */
    public static CVLookup transformCVLookup(uk.ac.ebi.jmzml.model.mzml.CV oldCv) {
        CVLookup cvLookup = null;
        if (oldCv != null) {
            cvLookup = new CVLookup(oldCv.getId(), oldCv.getFullName(),
                    oldCv.getVersion(), oldCv.getURI());
        }
        return cvLookup;
    }

    /**
     * Convert sample list
     *
     * @param oldSampleList jmzml sample list
     * @return List<Sample>    a list of samples
     */
    public static List<Sample> transformSampleList(uk.ac.ebi.jmzml.model.mzml.SampleList oldSampleList) {
        List<Sample> samples = null;

        if (oldSampleList != null) {
            samples = new ArrayList<Sample>();
            List<uk.ac.ebi.jmzml.model.mzml.Sample> oldSamples = oldSampleList.getSample();
            for (uk.ac.ebi.jmzml.model.mzml.Sample oldSample : oldSamples) {
                samples.add(transformSample(oldSample));
            }
        }
        return samples;
    }

    /**
     * Convert sample
     *
     * @param oldSample jmzml sample
     * @return Sample  sample
     */
    public static Sample transformSample(uk.ac.ebi.jmzml.model.mzml.Sample oldSample) {
        Sample newSample = null;

        if (oldSample != null) {
            String id = oldSample.getId();
            String name = oldSample.getName();
            ParamGroup paramGroup = transformParamGroup(oldSample);
            newSample = new Sample(paramGroup, id, name);
        }

        return newSample;
    }

    /**
     * Convert software list
     *
     * @param oldSoftwareList a list of jmzml software
     * @return List<Software>  a list of sfotware
     */
    public static List<Software> transformSoftwareList(uk.ac.ebi.jmzml.model.mzml.SoftwareList oldSoftwareList) {
        List<Software> softwares = null;

        if (oldSoftwareList != null) {
            softwares = new ArrayList<Software>();
            List<uk.ac.ebi.jmzml.model.mzml.Software> oldSoftwares = oldSoftwareList.getSoftware();
            for (uk.ac.ebi.jmzml.model.mzml.Software oldSoftware : oldSoftwares) {
                softwares.add(transformSoftware(oldSoftware));
            }
        }
        return softwares;
    }

    /**
     * Convert software
     *
     * @param oldSoftware jmzml softare
     * @return Software    software
     */
    public static Software transformSoftware(uk.ac.ebi.jmzml.model.mzml.Software oldSoftware) {
        Software newSoftware = null;

        if (oldSoftware != null) {
            String id = oldSoftware.getId();
            String version = oldSoftware.getVersion();
            String name = null;
            if (oldSoftware.getCvParam().size() > 0) {
                name = oldSoftware.getCvParam().get(0).getName();
            }
            ParamGroup paramGroup = transformParamGroup(oldSoftware);
            newSoftware = new Software(paramGroup, id, name, null, null, null, version);
        }
        return newSoftware;
    }

    /**
     * Convert scan settings
     *
     * @param oldScanSettingsList scan setting list
     * @return List<ScanSetting>   a list of scan settings
     */
    public static List<ScanSetting> transformScanSettingList(uk.ac.ebi.jmzml.model.mzml.ScanSettingsList oldScanSettingsList) {
        List<ScanSetting> scanSettings = null;

        if (oldScanSettingsList != null) {
            scanSettings = new ArrayList<ScanSetting>();
            List<uk.ac.ebi.jmzml.model.mzml.ScanSettings> oldScanSettings = oldScanSettingsList.getScanSettings();
            for (uk.ac.ebi.jmzml.model.mzml.ScanSettings oldScanSetting : oldScanSettings) {
                scanSettings.add(transformScanSetting(oldScanSetting));
            }
        }

        return scanSettings;
    }

    /**
     * Convert scan setting
     *
     * @param oldScanSetting jmzml scan setting
     * @return ScanSetting scan setting
     */
    public static ScanSetting transformScanSetting(uk.ac.ebi.jmzml.model.mzml.ScanSettings oldScanSetting) {
        ScanSetting scanSetting = null;

        if (oldScanSetting != null) {
            String id = oldScanSetting.getId();
            List<SourceFile> sourceFiles = new ArrayList<SourceFile>();
            List<uk.ac.ebi.jmzml.model.mzml.SourceFileRef> oldSourceFileRefs = oldScanSetting.getSourceFileRefList().getSourceFileRef();
            for (uk.ac.ebi.jmzml.model.mzml.SourceFileRef oldSourceFileRef : oldSourceFileRefs) {
                sourceFiles.add(transformSourceFile(oldSourceFileRef.getSourceFile()));
            }
            List<ParamGroup> targets = transformParamGroupList(oldScanSetting.getTargetList().getTarget());
            ParamGroup paramGroup = transformParamGroup(oldScanSetting);
            scanSetting = new ScanSetting(id, sourceFiles, targets, paramGroup);
        }

        return scanSetting;
    }

    /**
     * Convert instrument configuration list
     *
     * @param oldInstrumentList jmzml instrument configuration list
     * @return List<InstrumentConfiguration>   a list of instrument configurations
     */
    public static List<InstrumentConfiguration> transformInstrumentConfigurationList(uk.ac.ebi.jmzml.model.mzml.InstrumentConfigurationList oldInstrumentList) {
        List<InstrumentConfiguration> instrumentConfigurations = null;

        if (oldInstrumentList != null) {
            instrumentConfigurations = new ArrayList<InstrumentConfiguration>();
            List<uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration> oldInstruments = oldInstrumentList.getInstrumentConfiguration();
            for (uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration oldInstrument : oldInstruments) {
                instrumentConfigurations.add(transformInstrumentConfiguration(oldInstrument));
            }
        }

        return instrumentConfigurations;
    }

    /**
     * Convert instrument configuration
     *
     * @param oldInstrument jmzml instrument configuration
     * @return Instrumentconfiguration insturment configuration
     */
    public static InstrumentConfiguration transformInstrumentConfiguration(uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration oldInstrument) {
        InstrumentConfiguration instrumentConfiguration = null;

        if (oldInstrument != null) {
            String id = oldInstrument.getId();
            ScanSetting scanSetting = transformScanSetting(oldInstrument.getScanSettings());

            // convert software
            uk.ac.ebi.jmzml.model.mzml.Software oldSoftware = oldInstrument.getSoftware();
            Software software = null;
            if (oldSoftware != null) {
                software = transformSoftware(oldSoftware);
            }
            // convert component list
            ComponentList componentList = oldInstrument.getComponentList();
            List<InstrumentComponent> source = new ArrayList<InstrumentComponent>();
            List<InstrumentComponent> analyzer = new ArrayList<InstrumentComponent>();
            List<InstrumentComponent> detector = new ArrayList<InstrumentComponent>();
            if (componentList != null) {
                for (uk.ac.ebi.jmzml.model.mzml.SourceComponent oldSource : componentList.getSource()) {
                    InstrumentComponent newSource = transformInstrumentComponent(oldSource);
                    source.add(newSource);
                }
                for (uk.ac.ebi.jmzml.model.mzml.AnalyzerComponent oldAnalyzer : componentList.getAnalyzer()) {
                    InstrumentComponent newAnalyzer = transformInstrumentComponent(oldAnalyzer);
                    analyzer.add(newAnalyzer);
                }
                for (uk.ac.ebi.jmzml.model.mzml.DetectorComponent oldDetector : componentList.getDetector()) {
                    InstrumentComponent newDetector = transformInstrumentComponent(oldDetector);
                    detector.add(newDetector);
                }
            }
            ParamGroup paramGroup = transformParamGroup(oldInstrument);
            instrumentConfiguration = new InstrumentConfiguration(id, scanSetting, software, source, analyzer, detector, paramGroup);
        }

        return instrumentConfiguration;
    }

    /**
     * Convert instrument component
     *
     * @param rawComponent jmzml instrument component
     * @return InstrumentComponent instrument component
     */
    private static InstrumentComponent transformInstrumentComponent(uk.ac.ebi.jmzml.model.mzml.Component rawComponent) {
        InstrumentComponent component = null;

        if (rawComponent != null) {
            component = new InstrumentComponent(rawComponent.getOrder(), transformParamGroup(rawComponent));
        }

        return component;
    }

    /**
     * Convert a list of data prcessings
     *
     * @param oldDataProcessingList jmzml data processing list
     * @return List<DataProcessing>    a list of data processings
     */
    public static List<DataProcessing> transformDataProcessingList(uk.ac.ebi.jmzml.model.mzml.DataProcessingList oldDataProcessingList) {
        List<DataProcessing> dataProcessings = null;

        if (oldDataProcessingList != null) {
            dataProcessings = new ArrayList<DataProcessing>();
            List<uk.ac.ebi.jmzml.model.mzml.DataProcessing> oldDataProcessings = oldDataProcessingList.getDataProcessing();
            for (uk.ac.ebi.jmzml.model.mzml.DataProcessing oldDataProcessing : oldDataProcessings) {
                dataProcessings.add(transformDataProcessing(oldDataProcessing));
            }
        }

        return dataProcessings;
    }

    /**
     * Convert data processing
     *
     * @param oldDataProcessing jmzml data processing
     * @return DataProcessing  data processing
     */
    public static DataProcessing transformDataProcessing(uk.ac.ebi.jmzml.model.mzml.DataProcessing oldDataProcessing) {
        DataProcessing newDataProcessing = null;
        if (oldDataProcessing != null) {
            String id = oldDataProcessing.getId();
            List<ProcessingMethod> methods = new ArrayList<ProcessingMethod>();
            List<uk.ac.ebi.jmzml.model.mzml.ProcessingMethod> oldProcMethods = oldDataProcessing.getProcessingMethod();
            for (uk.ac.ebi.jmzml.model.mzml.ProcessingMethod oldProcMethod : oldProcMethods) {
                methods.add(transformProcessingMethod(oldProcMethod));
            }
            newDataProcessing = new DataProcessing(id, methods);
        }

        return newDataProcessing;
    }

    /**
     * Retrieve the file content
     *
     * @param rawFileDescription The raw file description objects.
     * @return ParamGroup
     */
    public static ParamGroup transformFileDescriptionToFileContent(FileDescription rawFileDescription) {
        if (rawFileDescription != null) {
            return transformParamGroup(rawFileDescription.getFileContent());
        }
        return null;
    }

    /**
     * Transform FileDescription object to List of SourceFile
     *
     * @param rawFileDescription jmzml FileDescription Object
     * @return List<SourceFile> List of Source Files used in the MzMl
     */
    public static List<SourceFile> transformFileDescriptionToFileSource(FileDescription rawFileDescription) {
        if (rawFileDescription != null) {
            uk.ac.ebi.jmzml.model.mzml.SourceFileList rawSourceFileList = rawFileDescription.getSourceFileList();
            return transformSourceFileList(rawSourceFileList);
        }
        return null;
    }

    /**
     * Method to retrieve the Contact Persons From the FileDescription Object in the MzMl Files
     *
     * @param rawFileDescription raw file description
     * @return List<Person> List of Person Contacts
     */
    public static List<Person> transformFileDescriptionToPerson(FileDescription rawFileDescription) {
        if (rawFileDescription != null && rawFileDescription.getContact() != null) {
            List<ParamGroup> contacts = transformParamGroupList(rawFileDescription.getContact());
            List<Person> persons = new ArrayList<Person>();
            for (ParamGroup contact : contacts) {
                CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
                List<CvParam> contactsValues = DataAccessUtilities.getCvParam(contact, contactTerm.getCvLabel(), contactTerm.getAccession());
                String name = null;
                if (!contactsValues.isEmpty()) {
                    name = contactsValues.get(0).getValue();
                }
                contactTerm = CvTermReference.CONTACT_EMAIL;
                contactsValues = DataAccessUtilities.getCvParam(contact, contactTerm.getCvLabel(), contactTerm.getAccession());
                String mail = null;
                if (!contactsValues.isEmpty()) {
                    mail = contactsValues.get(0).getValue();
                }
                Person contactPerson = new Person(contact, name, mail);
                persons.add(contactPerson);
            }
            return persons;
        }
        return null;
    }

    /**
     * Method to transform FileDescription in the MzMl file to an Organization Objet List
     *
     * @param rawFileDescription raw file description
     * @return List<Organization> Organization List
     */
    public static List<Organization> transformFileDescriptionOrganization(FileDescription rawFileDescription) {
        if (rawFileDescription != null) {
            List<ParamGroup> contacts = transformParamGroupList(rawFileDescription.getContact());
            List<Organization> organizations = new ArrayList<Organization>();
            for (ParamGroup contact : contacts) {
                CvTermReference contactTerm = CvTermReference.CONTACT_ORG;
                List<CvParam> contactsValues = DataAccessUtilities.getCvParam(contact, contactTerm.getCvLabel(), contactTerm.getAccession());
                String name = null;
                if (!contactsValues.isEmpty()) {
                    name = contactsValues.get(0).getValue();
                }
                contactTerm = CvTermReference.CONTACT_EMAIL;
                contactsValues = DataAccessUtilities.getCvParam(contact, contactTerm.getCvLabel(), contactTerm.getAccession());
                String mail = null;
                if (!contactsValues.isEmpty()) {
                    mail = contactsValues.get(0).getValue();
                }
                Organization contactOrganization = new Organization(contact, name, mail);
                organizations.add(contactOrganization);
            }
            return organizations;
        }
        return null;
    }

    public static CvParam transformDateToCvParam(Date creationDate) {
        CvTermReference cvTerm = CvTermReference.MS_SCAN_DATE;
        return new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), creationDate.toString(), null, null, null);
    }
}
