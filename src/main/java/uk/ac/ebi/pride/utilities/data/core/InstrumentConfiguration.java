package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Description of a particular hardware configuration of a mass spectrometer.
 * each instrument can only contain three components : source, analyser, detector.
 * <p/>
 * In mzML 1.1.0.1, the following cv terms must be added:
 * 1. May include one or more child terms of "ion optics attribute". (field-free region,
 * accelerating voltage and et al)
 * <p/>
 * 2. May include only one child term of "ion optics type". (magnetic deflection,
 * delayed extraction, collision quadrupole and et al)
 * <p/>
 * 3. May include one or more child terms of "instrument attribute". (customization,
 * transmission, instrument serial number)
 * <p/>
 * 4. Must include only one "instrument mode" or any of its children. (4000 Q TRAP,
 * API 4000 and et al)
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class InstrumentConfiguration extends ParamGroup {

    /**
     * identifier of this instrument
     */
    private String id;

    /**
     * only one analyzer
     */
    private final List<InstrumentComponent> analyzer;

    /**
     * only one detector
     */
    private final List<InstrumentComponent> detector;

    /**
     * only one source
     */
    private final List<InstrumentComponent> source;

    /**
     * scan settings
     */
    private ScanSetting scanSetting;

    /**
     * software used
     */
    private Software software;

    /**
     * Constructor
     *
     * @param id          required.
     * @param scanSetting optional
     * @param software    optional.
     * @param source      required.
     * @param analyzer    required.
     * @param detector    required.
     * @param params      optional.
     */
    public InstrumentConfiguration(String id, ScanSetting scanSetting, Software software,
                                   List<InstrumentComponent> source, List<InstrumentComponent> analyzer,
                                   List<InstrumentComponent> detector, ParamGroup params) {
        super(params);
        this.id = id;
        this.scanSetting = scanSetting;
        this.software = software;
        this.source = CollectionUtils.createListFromList(source);
        this.analyzer = CollectionUtils.createListFromList(analyzer);
        this.detector = CollectionUtils.createListFromList(detector);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScanSetting getScanSetting() {
        return scanSetting;
    }

    public void setScanSetting(ScanSetting scanSetting) {
        this.scanSetting = scanSetting;
    }

    public Software getSoftware() {
        return software;
    }

    public void setSoftware(Software software) {
        this.software = software;
    }

    public List<InstrumentComponent> getSource() {
        return source;
    }

    public void setSource(List<InstrumentComponent> source) {
        CollectionUtils.replaceValuesInCollection(source, this.source);
    }

    public List<InstrumentComponent> getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(List<InstrumentComponent> analyzer) {
        CollectionUtils.replaceValuesInCollection(analyzer, this.analyzer);
    }

    public List<InstrumentComponent> getDetector() {
        return detector;
    }

    public void setDetector(List<InstrumentComponent> detector) {
        CollectionUtils.replaceValuesInCollection(detector, this.detector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstrumentConfiguration)) return false;
        if (!super.equals(o)) return false;

        InstrumentConfiguration that = (InstrumentConfiguration) o;

        if (!analyzer.equals(that.analyzer)) return false;
        if (!detector.equals(that.detector)) return false;
        return !(id != null ? !id.equals(that.id) : that.id != null) && !(scanSetting != null ? !scanSetting.equals(that.scanSetting) : that.scanSetting != null) && !(software != null ? !software.equals(that.software) : that.software != null) && source.equals(that.source);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + analyzer.hashCode();
        result = 31 * result + detector.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (scanSetting != null ? scanSetting.hashCode() : 0);
        result = 31 * result + (software != null ? software.hashCode() : 0);
        result = 31 * result + source.hashCode();
        return result;
    }
}



