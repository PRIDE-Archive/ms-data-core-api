package uk.ac.ebi.pride.utilities.data.core;

//~--- non-JDK imports --------------------------------------------------------

import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * Chromatogram object.
 * <p/>
 * In mzML 1.1.0.1, the following cv terms must be added:
 * <p/>
 * 1. May include one or more child terms of "chromatogram attribute"
 * (highest observed m/z, highest observed wavelength and et al)
 * <p/>
 * 2. Must include only one child term of "chromatogram type"
 * (total ion current chromatogram, selected ion current chromatogram,
 * basepeak chromatogram)
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class Chromatogram extends MzGraph {

    public Chromatogram(Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                        int defaultArrayLength, List<BinaryDataArray> binaryDataArrays) {
        super(id, name, index, defaultDataProcessing, defaultArrayLength, binaryDataArrays);
    }

    public Chromatogram(ParamGroup params, Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                        int defaultArrayLength, List<BinaryDataArray> binaryDataArrays) {
        super(params, id, name, index, defaultDataProcessing, defaultArrayLength, binaryDataArrays);
    }

    public BinaryDataArray getIntensityArray() {
        return getBinaryDataArray(CvTermReference.INTENSITY_ARRAY.getAccession());
    }

    public BinaryDataArray getTimeArray() {
        return getBinaryDataArray(CvTermReference.TIME_ARRAY.getAccession());
    }


}



