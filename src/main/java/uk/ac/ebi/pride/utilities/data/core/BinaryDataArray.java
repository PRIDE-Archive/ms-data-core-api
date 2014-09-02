package uk.ac.ebi.pride.utilities.data.core;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;

/**
 * BinaryDataArray is a slim down version of binaryDataArray in mzML
 * ParamGroup of this object must have the followings in mzML 1.1.0.1 definition:
 * <p/>
 * For Chromatograms (once each):
 * 1. a child term of binary data compression type (zlib compression, no compression)
 * 2. a child term of binary data array (m/z array, intensity array, charge array,
 * signal to noise array, time array, wavelength array, non-standard data array,
 * flow rate array, pressure array and temperature array)
 * 3. a child term of binary data type (32-bit float or 64-bit float)
 * <p/>
 * For Spectrum (once each):
 * 1. a child term of binary data compression type (zlib compression, no compression)
 * 2. a child term of binary data array (m/z array, intensity array, charge array and signal to noise array)
 * 3. a child term of binary data type (32-bit float or 64-bit float)
 * <p/>
 * Note: arrayLength is ignored
 * Note: encodedLength is ignored
 * Note: In mzML, binary is always base64 encoded and is always "little endian".
 * <p/>
 * @author rwang
 * @author ypriverol
 */
public class BinaryDataArray extends ParamGroup {

    private double[] binaryDoubleArray;

    private DataProcessing dataProcessing;

    /**
     * Constructor
     *
     * @param dataProcessing  optional.
     * @param binaryDoubleArr required.
     * @param params          required, but there is no way of enforce/check it.
     */
    public BinaryDataArray(DataProcessing dataProcessing, double[] binaryDoubleArr, ParamGroup params) {
        super(params);
        setDataProcessing(dataProcessing);
        setDoubleArray(binaryDoubleArr);
    }

    public double[] getDoubleArray() {
        return Arrays.copyOf(binaryDoubleArray, binaryDoubleArray.length);
    }

    public void setDoubleArray(double[] binaryDoubleArr) {
        this.binaryDoubleArray = Arrays.copyOf(binaryDoubleArr, binaryDoubleArr.length);
    }

    public DataProcessing getDataProcessing() {
        return dataProcessing;
    }

    public void setDataProcessing(DataProcessing dataProcessing) {
        this.dataProcessing = dataProcessing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BinaryDataArray that = (BinaryDataArray) o;

        return Arrays.equals(binaryDoubleArray, that.binaryDoubleArray) && !(dataProcessing != null ? !dataProcessing.equals(that.dataProcessing) : that.dataProcessing != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (binaryDoubleArray != null ? Arrays.hashCode(binaryDoubleArray) : 0);
        result = 31 * result + (dataProcessing != null ? dataProcessing.hashCode() : 0);
        return result;
    }
}



