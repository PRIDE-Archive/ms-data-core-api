package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract object can be extended by both Spectrum and Chromatogram.
 * This is a General Class representation of Spectrums and Chromatograms.
 * <p/>
 * <p/>
 * @author Rui Wang
 * Date: 05-Feb-2010
 * Time: 15:41:15
 */
public abstract class MzGraph extends IdentifiableParamGroup {

    /**
     * list of binary data arrays
     */
    private final List<BinaryDataArray> binaryDataArrays;

    /**
     * default length of binary data arrays
     */
    private int defaultArrayLength;

    /**
     * appropriate data processing method
     */
    private DataProcessing defaultDataProcessing;

    /**
     * zero-based, consecutive index
     */
    private int index;

    /**
     * @param id                    Generic Id for MzGraph
     * @param name                  Generic Name for MzGraph
     * @param index                 Consecutive Index zero-based
     * @param defaultDataProcessing Appropriate data processing method
     * @param defaultArrayLength    Default length of binary data arrays
     * @param binaryDataArrays      List of binary data arrays
     */
    protected MzGraph(Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                      int defaultArrayLength, List<BinaryDataArray> binaryDataArrays) {
        this(null, id, name, index, defaultDataProcessing, defaultArrayLength, binaryDataArrays);
    }

    /**
     * @param params                CvParams of MzGraph
     * @param id                    Generic Id for MzGraph
     * @param name                  Generic Name for MzGraph
     * @param index                 Consecutive Index zero-based
     * @param defaultDataProcessing Appropriate data processing method
     * @param defaultArrayLength    Default length of binary data arrays
     * @param binaryDataArrays      List of binary data arrays
     */
    protected MzGraph(ParamGroup params, Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                      int defaultArrayLength, List<BinaryDataArray> binaryDataArrays) {
        super(params, id, name);
        this.index = index;
        this.defaultDataProcessing = defaultDataProcessing;
        this.defaultArrayLength = defaultArrayLength;
        this.binaryDataArrays = new ArrayList<BinaryDataArray>();
        if(binaryDataArrays != null && binaryDataArrays.size() > 0){
            for(BinaryDataArray binaryDataArray: binaryDataArrays){
                BinaryDataArray bin = new BinaryDataArray(binaryDataArray);
                this.binaryDataArrays.add(bin);
            }
        }
    }


    public List<BinaryDataArray> getBinaryDataArrays() {
        return binaryDataArrays;
    }

    public void setBinaryDataArrays(List<BinaryDataArray> binaryDataArrays) {
        CollectionUtils.replaceValuesInCollection(binaryDataArrays, this.binaryDataArrays);
    }


    /**
     * Get either m/z array or intensity array
     *
     * @param cvAcc Controlled vocabulary's accession number.
     * @return BinaryDataArray  data array.
     */
    public BinaryDataArray getBinaryDataArray(String cvAcc) {
        BinaryDataArray arr = null;
        List<BinaryDataArray> binaries = getBinaryDataArrays();

        if (binaries != null) {
            for (BinaryDataArray binary : binaries) {
                List<CvParam> cvParams = binary.getCvParams();

                for (CvParam cvParam : cvParams) {
                    String accession = cvParam.getAccession();

                    if (cvAcc.equals(accession)) {
                        arr = binary;
                    }
                }
            }
        }

        return arr;
    }

    public DataProcessing getDataProcessing() {
        return this.defaultDataProcessing;
    }

    public void setDataProcessing(DataProcessing dataProcessing) {
        this.defaultDataProcessing = dataProcessing;
    }

    public int getDefaultArrayLength() {
        return defaultArrayLength;
    }

    public void setDefaultArrayLength(int defaultArrayLength) {
        this.defaultArrayLength = defaultArrayLength;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MzGraph)) return false;
        if (!super.equals(o)) return false;

        MzGraph mzGraph = (MzGraph) o;

        return defaultArrayLength == mzGraph.defaultArrayLength && index == mzGraph.index && binaryDataArrays.equals(mzGraph.binaryDataArrays) && !(defaultDataProcessing != null ? !defaultDataProcessing.equals(mzGraph.defaultDataProcessing) : mzGraph.defaultDataProcessing != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + binaryDataArrays.hashCode();
        result = 31 * result + defaultArrayLength;
        result = 31 * result + (defaultDataProcessing != null ? defaultDataProcessing.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }
}



