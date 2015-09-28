package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import javax.xml.datatype.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MZXml transformer provides the methods to convert MzXml objects to
 * ms-core-api objetcs.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 2/27/12
 * Time: 2:12 PM
 */
public final class MzXmlTransformer {

    /**
     * Private Constructor
     */
    private MzXmlTransformer() {

    }

    /**
     * Convert spectrum
     *
     * @param spectrum spectrum object
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

    public static List<Person> transformPersonContacts(List<uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Operator> operators) {
        List<Person> persons = null;
        if (operators != null) {
            persons = new ArrayList<Person>();
            for (uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Operator operator : operators) {
                if (operator != null) {
                    ParamGroup paramGroup = new ParamGroup();
                    CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
                    CvParam cvParam = new CvParam(contactTerm.getAccession(), contactTerm.getName(), contactTerm.getCvLabel(), operator.getFirst() + " " + operator.getLast(), null, null, null);
                    paramGroup.addCvParam(cvParam);
                    contactTerm = CvTermReference.CONTACT_EMAIL;
                    cvParam = new CvParam(contactTerm.getAccession(), contactTerm.getName(), contactTerm.getCvLabel(), operator.getEmail(), null, null, null);
                    paramGroup.addCvParam(cvParam);
                    Person person = new Person(paramGroup, null, operator.getFirst(), operator.getLast(), operator.getFirst(), null, null, operator.getEmail());
                    persons.add(person);
                }
            }
        }
        return persons;
    }

    public static List<SourceFile> transformFileSources(List<uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.ParentFile> rawParentFiles) {
        List<SourceFile> sourceFiles = null;
        if (rawParentFiles != null) {
            sourceFiles = new ArrayList<SourceFile>();
            for (uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.ParentFile parentFile : rawParentFiles) {
                ParamGroup paramGroup = null;
                CvParam fileFormat = null;
                if (parentFile.getFileType() != null) {
                    paramGroup = new ParamGroup();
                    CvTermReference cvReference = CvTermReference.MS_FILE_SPECTRUM;
                    // Kepp here the information of the file Type, is the only solution to convert mzXML attributes to CvTerms
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), parentFile.getFileType(), null, null, null);
                    paramGroup.addCvParam(cvParam);
                    fileFormat = cvParam;
                }
                SourceFile sourceFile = new SourceFile(paramGroup, null, parentFile.getFileName(), null, fileFormat, null);
                sourceFiles.add(sourceFile);
            }
        }
        return sourceFiles;
    }

    public static List<Software> transformSoftwares(List<uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Software> rawSoftwares) {
        List<Software> softwares = null;
        if (rawSoftwares != null) {
            softwares = new ArrayList<Software>();
            for (uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Software rawSoftware : rawSoftwares) {
                softwares.add(transformSoftware(rawSoftware));
            }
        }
        return softwares;
    }

    public static Software transformSoftware(uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Software rawSoftware) {
        Software software = null;
        if (rawSoftware != null) {
            software = new Software(null, null, rawSoftware.getName(), null, null, null, rawSoftware.getVersion());
            if (rawSoftware.getType() != null) {
                ParamGroup paramGroup = new ParamGroup();
                if (rawSoftware.getType().compareToIgnoreCase("conversion") == 0) {
                    CvTermReference cvReference = CvTermReference.MS_SOFTWARE_PROCESSING;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), rawSoftware.getName(), null, null, null);
                    paramGroup.addCvParam(cvParam);
                } else if (rawSoftware.getType().compareToIgnoreCase("acquisition") == 0) {
                    CvTermReference cvReference = CvTermReference.MS_SOFTWARE_ACQUISITION;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), rawSoftware.getName(), null, null, null);
                    paramGroup.addCvParam(cvParam);
                } else if (rawSoftware.getType().compareToIgnoreCase("processing") == 0) {
                    CvTermReference cvReference = CvTermReference.MS_SOFTWARE_PROCESSING;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), rawSoftware.getName(), null, null, null);
                    paramGroup.addCvParam(cvParam);
                }
                software.addCvParams(paramGroup.getCvParams());
            }

        }
        return software;
    }


    public static List<InstrumentConfiguration> transformMsInstrument(List<uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.MsInstrument> rawInstrumentList) {
        List<InstrumentConfiguration> instrumentConfigurations = null;
        if (rawInstrumentList != null) {
            instrumentConfigurations = new ArrayList<InstrumentConfiguration>();
            for (uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.MsInstrument msInstrument : rawInstrumentList) {
                Software software = transformSoftware(msInstrument.getSoftware());
                List<InstrumentComponent> source = new ArrayList<InstrumentComponent>();
                List<InstrumentComponent> analyzer = new ArrayList<InstrumentComponent>();
                List<InstrumentComponent> detector = new ArrayList<InstrumentComponent>();
                if (msInstrument.getMsIonisation() != null) {
                    CvTermReference cvReference = CvTermReference.MS_INSTRUMENT_SOURCE;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), msInstrument.getMsIonisation().getTheValue(), null, null, null);
                    ParamGroup paramGroup = new ParamGroup();
                    paramGroup.addCvParam(cvParam);
                    InstrumentComponent sourceInstr = new InstrumentComponent(0, paramGroup);
                    source.add(sourceInstr);
                }
                if (msInstrument.getMsDetector() != null) {
                    CvTermReference cvReference = CvTermReference.MS_INSTRUMENT_DETECTOR;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), msInstrument.getMsDetector().getTheValue(), null, null, null);
                    ParamGroup paramGroup = new ParamGroup();
                    paramGroup.addCvParam(cvParam);
                    InstrumentComponent detectorInstr = new InstrumentComponent(1, paramGroup);
                    detector.add(detectorInstr);
                }
                if (msInstrument.getMsMassAnalyzer() != null) {
                    CvTermReference cvReference = CvTermReference.MS_INSTRUMENT_ANALYZER;
                    CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), msInstrument.getMsMassAnalyzer().getTheValue(), null, null, null);
                    ParamGroup paramGroup = new ParamGroup();
                    paramGroup.addCvParam(cvParam);
                    InstrumentComponent analyzerInstr = new InstrumentComponent(2, paramGroup);
                    analyzer.add(analyzerInstr);
                }
                // The name of the instrument is the model of the msInstrument.
                InstrumentConfiguration instrumentConfiguration = new InstrumentConfiguration(msInstrument.getMsModel().getTheValue(), null, software, source, analyzer, detector, null);

                instrumentConfigurations.add(instrumentConfiguration);
            }
        }
        return instrumentConfigurations;
    }

    /**
     * Convert a list of data processings
     *
     * @param rawDataProcList mzXML data processing list
     * @return List<DataProcessing>    a list of data processings
     */
    public static List<DataProcessing> transformDataProcessingList(List<uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.DataProcessing> rawDataProcList) {
        List<DataProcessing> dataProcessings = null;

        if (rawDataProcList != null) {
            dataProcessings = new ArrayList<DataProcessing>();
            int i = 0;
            for (uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.DataProcessing oldDataProcessing : rawDataProcList) {
                dataProcessings.add(transformDataProcessing(i, oldDataProcessing));
                i++;
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
    public static DataProcessing transformDataProcessing(int order, uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.DataProcessing oldDataProcessing) {
        DataProcessing dataProcessing = null;
        if (oldDataProcessing != null) {
            List<CvParam> cvParams = new ArrayList<CvParam>();
            CvTermReference cvReference;
            if (oldDataProcessing.isCentroided() != null && oldDataProcessing.isCentroided()) {
                cvReference = CvTermReference.MS_DATAPROCESSING_CENTROID;
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), null, null, null, null);
                cvParams.add(cvParam);
            }
            if (oldDataProcessing.isChargeDeconvoluted() != null && oldDataProcessing.isChargeDeconvoluted()) {
                cvReference = CvTermReference.MS_DATAPROCESSING_DECONVOLUTION;
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), null, null, null, null);
                cvParams.add(cvParam);
            }
            if (oldDataProcessing.isDeisotoped() != null && oldDataProcessing.isDeisotoped()) {
                cvReference = CvTermReference.MS_DATAPROCESSING_DEISOTOPING;
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), null, null, null, null);
                cvParams.add(cvParam);
            }
            if (oldDataProcessing.getIntensityCutoff() != null) {
                cvReference = CvTermReference.MS_DATAPROCESSING_INTENSITY_THRESHOLD;
                CvParam cvParam = new CvParam(cvReference.getAccession(), cvReference.getName(), cvReference.getCvLabel(), oldDataProcessing.getIntensityCutoff().toString(), null, null, null);
                cvParams.add(cvParam);
            }
            List<ProcessingMethod> processingMethods = new ArrayList<ProcessingMethod>();
            ProcessingMethod processingMethod = new ProcessingMethod(order, transformSoftware(oldDataProcessing.getSoftware()), new ParamGroup(cvParams, null));
            processingMethods.add(processingMethod);
            dataProcessing = new DataProcessing(null, oldDataProcessing.getSoftware().getName(), processingMethods);
        }
        return dataProcessing;

    }


    public static CvParam transformDurationToCvParam(Duration startDate, CvTermReference msScanDate) {
        CvParam cvParam = null;
        if (startDate != null) {
            cvParam = new CvParam(msScanDate.getAccession(), msScanDate.getName(), msScanDate.getCvLabel(), startDate.toString(), null, null, null);
        }
        return cvParam;
    }
}
