package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.List;

/**
 * A peak list including the underlying acquisitions.
 * <p/>
 * In mzML 1.1.0.1, it should the following cv terms:
 * <p/>
 * 1. It may have only one "scan polarity" (negative scan, positive scan).
 * <p/>
 * 2. It must have only one "spectrum type" (charge inversion mass spectrum,
 * constant neutral gain spectrum and et al)
 * <p/>
 * 3. It must have only one "spectrum representation" or any of its children.
 * (centroid spectrum, profile spectrum)
 * <p/>
 * 4. It may have one or more "spectrum attribute" (total ion current,
 * zoom scan, base peak m/z, ms level, spectrum title and etc al)
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 * Date: 03-Feb-2010
 * Time: 08:46:05
 */
public class    Spectrum extends MzGraph implements Cloneable{

    /**
     * peptide associate with this spectrum
     */
    private List<Peptide> peptide;

    /**
     * list and descriptions of precursor isolations to this spectrum
     */
    private final List<Precursor> precursors;

    /**
     * list and descriptions of product isolations to this spectrum
     */
    private final List<ParamGroup> products;

    /**
     * list and descriptions of scans
     */
    private ScanList scanList;

    /**
     * source file
     */
    private SourceFile sourceFile;

    /*
     The retention time in which this spectrum was obtained
     */
    private String retentionTime;

    /**
     * the identifier for the spot on a MALDI or similar on
     */
    private String spotID;

    public Spectrum(Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                    int defaultArrayLength, List<BinaryDataArray> binaryDataArrays, String spotID,
                    SourceFile sourceFile, ScanList scanList, List<Precursor> precursors, List<ParamGroup> products,
                    List<Peptide> peptide) {
        this(null, id, name, index, defaultDataProcessing, defaultArrayLength, binaryDataArrays, spotID, sourceFile,
                scanList, precursors, products, peptide);
    }

    public Spectrum(ParamGroup params, Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                    int defaultArrayLength, List<BinaryDataArray> binaryDataArrays, String spotID,
                    SourceFile sourceFile, ScanList scanList, List<Precursor> precursors, List<ParamGroup> products) {
        this(params, id, name, index, defaultDataProcessing, defaultArrayLength, binaryDataArrays, spotID, sourceFile,
                scanList, precursors, products, null);
    }

    public Spectrum(ParamGroup params, Comparable id, String name, int index, DataProcessing defaultDataProcessing,
                    int defaultArrayLength, List<BinaryDataArray> binaryDataArrays, String spotID,
                    SourceFile sourceFile, ScanList scanList, List<Precursor> precursors, List<ParamGroup> products,
                    List<Peptide> peptide) {
        super(params, id, name, index, defaultDataProcessing, defaultArrayLength, binaryDataArrays);
        this.spotID = spotID;
        this.sourceFile = sourceFile;
        this.scanList = scanList;
        this.precursors = CollectionUtils.createListFromList(precursors);
        this.products = CollectionUtils.createListFromList(products);
        this.peptide = peptide;
    }

    public Spectrum(Spectrum spectrum){
        this(spectrum,spectrum.getId(),spectrum.getName(),spectrum.getIndex(),spectrum.getDataProcessing(),spectrum.getDefaultArrayLength(),
             spectrum.getBinaryDataArrays(),spectrum.getSpotID(),spectrum.getSourceFile(),spectrum.getScanList(),spectrum.getPrecursors(),
             spectrum.getProducts(),spectrum.getPeptide());
    }

    public String getSpotID() {
        return spotID;
    }

    public void setSpotID(String spotID) {
        this.spotID = spotID;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public ScanList getScanList() {
        return scanList;
    }

    public void setScanList(ScanList scanList) {
        this.scanList = scanList;
    }

    public List<Precursor> getPrecursors() {
        return precursors;
    }

    public void setPrecursors(List<Precursor> precursors) {
        CollectionUtils.replaceValuesInCollection(precursors, this.precursors);
    }

    public List<ParamGroup> getProducts() {
        return products;
    }

    public void setProducts(List<ParamGroup> products) {
        CollectionUtils.replaceValuesInCollection(products, this.products);
    }

    public BinaryDataArray getMzBinaryDataArray() {
        return getBinaryDataArray(CvTermReference.MZ_ARRAY.getAccession());
    }

    public BinaryDataArray getIntensityBinaryDataArray() {
        return getBinaryDataArray(CvTermReference.INTENSITY_ARRAY.getAccession());
    }

    public List<Peptide> getPeptide() {
        return peptide;
    }

    public void setPeptide(List<Peptide> peptide) {
        this.peptide = peptide;
    }

    public double[][] getMassIntensityMap() {

        double[] ionIntensities = getIntensityBinaryDataArray().getDoubleArray();
        double[] ionMasses = getMzBinaryDataArray().getDoubleArray();

        double[][] peakList = new double[ionIntensities.length][2];

        for (int i = 0; i < peakList.length; i++) {
            peakList[i][0] = ionMasses[i];
            peakList[i][1] = ionIntensities[i];

        }
        return peakList;
    }

    public void setMassIntensityMap(double[][] peakMap){

        double[] mzArr  = new double[peakMap.length];
        double[] intArr = new double[peakMap.length];

        for(int i = 0; i < peakMap.length; i++){
            mzArr[i]  = peakMap[i][0];
            intArr[i] = peakMap[i][1];
        }
        getIntensityBinaryDataArray().setDoubleArray(intArr);
        getMzBinaryDataArray().setDoubleArray(mzArr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Spectrum)) return false;
        if (!super.equals(o)) return false;

        Spectrum spectrum = (Spectrum) o;

        if (peptide != null ? !peptide.equals(spectrum.peptide) : spectrum.peptide != null) return false;
        if (!precursors.equals(spectrum.precursors)) return false;
        if (!products.equals(spectrum.products)) return false;
        return !(scanList != null ? !scanList.equals(spectrum.scanList) : spectrum.scanList != null) && !(sourceFile != null ? !sourceFile.equals(spectrum.sourceFile) : spectrum.sourceFile != null) && !(spotID != null ? !spotID.equals(spectrum.spotID) : spectrum.spotID != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (peptide != null ? peptide.hashCode() : 0);
        result = 31 * result + precursors.hashCode();
        result = 31 * result + products.hashCode();
        result = 31 * result + (scanList != null ? scanList.hashCode() : 0);
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0);
        result = 31 * result + (spotID != null ? spotID.hashCode() : 0);
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}



