package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * PeakTransformer provides the functionality to convert peak objects to ms-core-api
 * objects.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 3/15/12
 * Time: 10:49 PM
 */
public final class PeakTransformer {

    /**
     * Private Constructor
     */
    private PeakTransformer() {

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

            cvTerm = CvTermReference.ION_SELECTION_CHARGE_STATE;
            cvParam = new CvParam(cvTerm.getAccession(), cvTerm.getName(), cvTerm.getCvLabel(), (spectrum.getPrecursorCharge() != null) ? spectrum.getPrecursorCharge().toString() : null, null, null, null);
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
        double[] intArray = new double[0];
        double[] mzArray = new double[0];

        if (peakList != null && peakList.size() > 0) {
            intArray = new double[peakList.keySet().size()];
            mzArray = new double[peakList.keySet().size()];
            int i = 0;

            Iterator iterator = peakList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                mzArray[i] = (Double) mapEntry.getKey();
                intArray[i] = (Double) mapEntry.getValue();
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
}
