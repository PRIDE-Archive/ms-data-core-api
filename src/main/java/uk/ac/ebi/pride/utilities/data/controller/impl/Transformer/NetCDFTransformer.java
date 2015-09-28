package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.netCDF.core.Metadata;
import uk.ac.ebi.pride.utilities.netCDF.core.MsScan;
import uk.ac.ebi.pride.utilities.netCDF.utils.NetCDFConstants;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 28/09/15
 */
public class NetCDFTransformer {

    /**
     * Convert spectrum
     *
     * @return Spectrum    spectrum
     */
    public static Spectrum transformSpectrum(MsScan scan) {
        Spectrum newSpec = null;
        if (scan != null) {

            String specId = scan.getId();
            int index = -1; //spectrum.getIndex().intValue();
            String spotId = null; //spectrum.getSpotID();
            DataProcessing dataProcessing = null;  //transformDataProcessing(spectrum.getDataProcessing());
            int arrLen = -1; // spectrum.getDefaultArrayLength();
            SourceFile sourceFile = null; //transformSourceFile(spectrum.getSourceFile());
            ScanList scans = null; //transformScanList(spectrum.getScanList());
            List<ParamGroup> products = null; //transformProductList(spectrum.getProductList());
            List<Precursor> precursors = null;
            List<BinaryDataArray> binaryArray = transformBinaryDataArrayList(scan.getDataPoints());


            ParamGroup paramGroup = new ParamGroup();

            newSpec = new Spectrum(paramGroup, specId, null, index, dataProcessing, arrLen,
                    binaryArray, spotId, sourceFile, scans, precursors, products);
        }
        return newSpec;
    }

    private static List<BinaryDataArray> transformBinaryDataArrayList(Map<Float, Float> peakList) {
        List<BinaryDataArray> binaryDataArrays = new ArrayList<BinaryDataArray>();
        uk.ac.ebi.pride.utilities.term.CvTermReference cvRefMz = CvTermReference.MZ_ARRAY;
        CvParam cvParamMz = new CvParam(cvRefMz.getAccession(), cvRefMz.getName(), cvRefMz.getCvLabel(), "", cvRefMz.getAccession(), cvRefMz.getName(), cvRefMz.getCvLabel());
        ParamGroup mzParam = new ParamGroup(cvParamMz, null);

        uk.ac.ebi.pride.utilities.term.CvTermReference cvRefInt = CvTermReference.INTENSITY_ARRAY;
        CvParam cvParam = new CvParam(cvRefInt.getAccession(), cvRefInt.getName(), cvRefInt.getCvLabel(), "", cvRefInt.getAccession(), cvRefInt.getName(), cvRefInt.getCvLabel());
        ParamGroup intParam = new ParamGroup(cvParam, null);
        double[] intArray = new double[0];
        double[] mzArray = new double[0];

        if (peakList != null && peakList.size() > 0) {
            intArray = new double[peakList.keySet().size()];
            mzArray = new double[peakList.keySet().size()];
            int i = 0;

            Iterator iterator = peakList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                mzArray[i] = new Double((Float) mapEntry.getKey());
                intArray[i] = new Double((Float) mapEntry.getValue());
                i++;
            }
        }

        //Todo: How you can know if the intensity correspond with the mz value?

        BinaryDataArray intBinaryArr = new BinaryDataArray(null, intArray, intParam);
        binaryDataArrays.add(intBinaryArr);
        BinaryDataArray mzBinaryArr = new BinaryDataArray(null, mzArray, mzParam);
        binaryDataArrays.add(mzBinaryArr);

        return binaryDataArrays;

    }

    public static MzGraphMetaData transfromMzGraphMetadata(Metadata rawMetadata) {
        ParamGroup paramGroup = new ParamGroup();
        Map<String, String> attr = rawMetadata.getAttributes();
        CvParam cv = null;
        if(attr.containsKey(NetCDFConstants.IONIZATION_MODE)){
            CvTermReference cvTerm = CvTermReference.MS_IONIZATION_MODE;
            cv = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), attr.get(NetCDFConstants.IONIZATION_MODE), null, null, null);
            paramGroup.addCvParam(cv);
        }
        List<ScanSetting> scanList = new ArrayList<ScanSetting>();
                scanList.add(new ScanSetting(null, null, null, paramGroup));

        return new MzGraphMetaData(null,null, scanList, transformMsInstrument(attr), transformDataProcessingList(attr));

    }

    public static ExperimentMetaData transfromExperimentMetaData(Metadata rawMetadata) {
        ParamGroup paramGroup = null;

        Date creationDate = null;

        String title = null;

        String description = null;

        Map<String, String> attr = rawMetadata.getAttributes();

        if(attr.containsKey(NetCDFConstants.EXPERIMENT_DATE))
            creationDate = parseDate(attr.get(NetCDFConstants.EXPERIMENT_DATE));


        if(attr.containsKey(NetCDFConstants.EXPERIMENT_TITLE))
            title = attr.get(NetCDFConstants.EXPERIMENT_TITLE);

        if(attr.containsKey(NetCDFConstants.ADMINISTRTATIVE_COMMENTS))
            description = attr.get(NetCDFConstants.ADMINISTRTATIVE_COMMENTS);

        return new ExperimentMetaData(paramGroup,null,title,null,description,null,null,transformPersonContacts(attr),transformFileSources(attr),null,null,null,creationDate,null,null,null);
    }

    private static Date parseDate(String s) {
        String subChar = s.substring(0,8);
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            return (Date)format.parse(subChar);
        } catch (NumberFormatException|ParseException e) {

        }
        return null;
    }

    public static List<Person> transformPersonContacts(Map<String, String> attr) {
        List<Person> persons = null;

        if (attr.containsKey(NetCDFConstants.DATASET_OWNER) || attr.containsKey(NetCDFConstants.OPERATOR_NAME)) {
            persons = new ArrayList<Person>();
            String operator = (attr.get(NetCDFConstants.DATASET_OWNER) == null)? attr.get(NetCDFConstants.OPERATOR_NAME): attr.get(NetCDFConstants.DATASET_OWNER);
            if (operator != null) {
                    ParamGroup paramGroup = new ParamGroup();
                    CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
                    CvParam cvParam = new CvParam(contactTerm.getAccession(), contactTerm.getName(), contactTerm.getCvLabel(), operator, null, null, null);
                    paramGroup.addCvParam(cvParam);
                    Person person = new Person(paramGroup, operator, null);
                    persons.add(person);
                }
            }
        return persons;
    }

    public static List<SourceFile> transformFileSources(Map<String, String> attr) {
        List<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        for (String key : attr.keySet()) {
            ParamGroup paramGroup = null;
            CvParam fileFormat = null;
            if (key.contains(NetCDFConstants.SOURCES)){
                paramGroup = new ParamGroup();
                CvTermReference cvReference = CvTermReference.MS_FILE_SPECTRUM;
                // Kepp here the information of the file Type, is the only solution to convert mzXML attributes to CvTerms
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), attr.get(key), null, null, null);
                paramGroup.addCvParam(cvParam);
                fileFormat = cvParam;
            }
            SourceFile sourceFile = new SourceFile(paramGroup, null, attr.get(key), null, fileFormat, null);
            sourceFiles.add(sourceFile);
        }
        return sourceFiles;
    }

    public static List<InstrumentConfiguration> transformMsInstrument(Map<String, String> attr) {
            List<InstrumentConfiguration> instrumentConfigurations = new ArrayList<InstrumentConfiguration>();
            List<InstrumentComponent> source = new ArrayList<InstrumentComponent>();
            List<InstrumentComponent> analyzer = new ArrayList<InstrumentComponent>();
            List<InstrumentComponent> detector = new ArrayList<InstrumentComponent>();
            String model = null;
            for (String key : attr.keySet()) {
                if (attr.containsKey(NetCDFConstants.IONIZATION_MODE)) {
                    CvTermReference cvReference = CvTermReference.MS_INSTRUMENT_SOURCE;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), attr.get(NetCDFConstants.IONIZATION_MODE), null, null, null);
                    ParamGroup paramGroup = new ParamGroup();
                    paramGroup.addCvParam(cvParam);
                    InstrumentComponent sourceInstr = new InstrumentComponent(0, paramGroup);
                    source.add(sourceInstr);
                }
                if (attr.containsKey(NetCDFConstants.DETECTOR_TYPE)) {
                    CvTermReference cvReference = CvTermReference.MS_INSTRUMENT_DETECTOR;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), attr.get(NetCDFConstants.DETECTOR_TYPE), null, null, null);
                    ParamGroup paramGroup = new ParamGroup();
                    paramGroup.addCvParam(cvParam);
                    InstrumentComponent detectorInstr = new InstrumentComponent(1, paramGroup);
                    detector.add(detectorInstr);
                }

                if(attr.containsKey(NetCDFConstants.INSTRUMENT_MODEL) || attr.containsKey(NetCDFConstants.INSTRUMENT_MFR)){
                    model = attr.containsKey(NetCDFConstants.INSTRUMENT_MODEL)?attr.get(NetCDFConstants.INSTRUMENT_MODEL):attr.get(NetCDFConstants.INSTRUMENT_MFR);
                }

            }
        // The name of the instrument is the model of the msInstrument.
        InstrumentConfiguration instrumentConfiguration = new InstrumentConfiguration(model, null, null, source, analyzer, detector, null);
        instrumentConfigurations.add(instrumentConfiguration);

        return instrumentConfigurations;
    }

    /**
     * Convert a list of data processings
     *
     * @return List<DataProcessing>    a list of data processings
     */
    public static List<DataProcessing> transformDataProcessingList(Map<String, String> attr) {
        List<DataProcessing>  dataProcessings = new ArrayList<DataProcessing>();
        int i = 0;
        for (String oldDataProcessing : attr.keySet()) {
            if(oldDataProcessing.equalsIgnoreCase(NetCDFConstants.EXPERIMENT_TYPE)){
                ParamGroup paramGroup = new ParamGroup();
                CvTermReference cvReference = CvTermReference.MS_GENERAL_SPECTRUM_REPRESENTATION;
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), attr.get(NetCDFConstants.EXPERIMENT_TYPE), null, null, null);
                paramGroup.addCvParam(cvParam);
                ProcessingMethod processingMEthod = new ProcessingMethod(i, null, paramGroup);
                List<ProcessingMethod> methods = new ArrayList<ProcessingMethod>();
                methods.add(processingMEthod);
                DataProcessing processing = new DataProcessing(oldDataProcessing, methods);
                dataProcessings.add(processing);
            }
            i++;
        }
        return dataProcessings;
    }


}
