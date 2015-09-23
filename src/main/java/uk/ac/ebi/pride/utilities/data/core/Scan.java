package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Scan or acquisition from original raw file used to create this
 * peak list, as specified in sourceFile.
 * <p/>
 * In mzML 1.1.0.1, the following cv terms must be added:
 * <p/>
 * 1. Must include one or more child terms of "scan attribute"
 * (mass resolution, scan rate, scan start time, dwell time and et al).
 * <p/>
 * 2. May include only one child term of "scan direction" (decreasing m/z scan,
 * increasing m/z scan).
 * <p/>
 * 3. May include only one child term of "scan law" (exponential, linear and quadratic)
 * <p/>
 * @author Rui Wang
 * Date: 20-Feb-2010
 * Time: 18:58:53
 */
public class Scan extends ParamGroup {

    /**
     * external spectrum reference
     */
    private String externalSpecRef;

    /**
     * Instrument configuration
     */
    private InstrumentConfiguration instrumentConfiguration;

    /**
     * collection of scan windows
     * <p/>
     * Note: Scan window is a range of m/z values over which the instrument scans
     * and acquires a spectrum.
     * In mzML 1.1.0.1, the following cv terms must be added:
     * <p/>
     * 1. May include one or more child terms of "selection winidow attribute"
     * (scan window upper limit, scan window lower limit)
     * <p/>
     * 2. Must include only one "scan window upper limit".
     * <p/>
     * 3. Must include only one "scan window lower limit".
     */
    private final List<ParamGroup> scanWindows;

    /**
     * source file, it must refer to the file which contains externalSpecRef
     */
    private SourceFile sourceFile;

    /**
     * spectrum reference
     */
    private String spectrumRef;

    /**
     * Constructor
     *
     * @param spectrumRef             optional.
     * @param externalSpecRef         optional.
     * @param sourceFile              optional.
     * @param instrumentConfiguration optional.
     * @param scanWindows             optional.
     * @param params                  optional.
     */
    public Scan(String spectrumRef, String externalSpecRef, SourceFile sourceFile,
                InstrumentConfiguration instrumentConfiguration, List<ParamGroup> scanWindows, ParamGroup params) {
        super(params);
        this.spectrumRef = spectrumRef;
        this.externalSpecRef = externalSpecRef;
        this.sourceFile = sourceFile;
        this.instrumentConfiguration = instrumentConfiguration;
        this.scanWindows = CollectionUtils.createListFromList(scanWindows);
    }

    public String getExternalSpecRef() {
        return externalSpecRef;
    }

    public void setExternalSpecRef(String externalSpecRef) {
        this.externalSpecRef = externalSpecRef;
    }

    public InstrumentConfiguration getInstrumentConfiguration() {
        return instrumentConfiguration;
    }

    public void setInstrumentConfiguration(InstrumentConfiguration instrumentConfiguration) {
        this.instrumentConfiguration = instrumentConfiguration;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSpectrumRef() {
        return spectrumRef;
    }

    public void setSpectrumRef(String spectrumRef) {
        this.spectrumRef = spectrumRef;
    }

    public List<ParamGroup> getScanWindows() {
        return scanWindows;
    }

    public void setScanWindows(List<ParamGroup> scanWindows) {
        CollectionUtils.replaceValuesInCollection(scanWindows, this.scanWindows);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scan)) return false;
        if (!super.equals(o)) return false;

        Scan scan = (Scan) o;

        if (externalSpecRef != null ? !externalSpecRef.equals(scan.externalSpecRef) : scan.externalSpecRef != null)
            return false;
        if (instrumentConfiguration != null ? !instrumentConfiguration.equals(scan.instrumentConfiguration) : scan.instrumentConfiguration != null)
            return false;
        return scanWindows.equals(scan.scanWindows) && !(sourceFile != null ? !sourceFile.equals(scan.sourceFile) : scan.sourceFile != null) && !(spectrumRef != null ? !spectrumRef.equals(scan.spectrumRef) : scan.spectrumRef != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (externalSpecRef != null ? externalSpecRef.hashCode() : 0);
        result = 31 * result + (instrumentConfiguration != null ? instrumentConfiguration.hashCode() : 0);
        result = 31 * result + scanWindows.hashCode();
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0);
        result = 31 * result + (spectrumRef != null ? spectrumRef.hashCode() : 0);
        return result;
    }
}



