package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * List and descriptions of scans.
 * <p/>
 * In mzML 1.1.0.1, the following cv terms must be added:
 * <p/>
 * 1. Must include only one child term of "spectra combination".
 * (sum of spectra, no combination)
 * <p/>
 * @author Rui Wang
 * Date: 20-Feb-2010
 * Time: 18:57:52
 */
public class ScanList extends ParamGroup {
    private final List<Scan> scans;

    /**
     * Constructor
     *
     * @param scans  required.
     * @param params optional.
     */
    public ScanList(List<Scan> scans, ParamGroup params) {
        super(params);
        this.scans = CollectionUtils.createListFromList(scans);
    }

    public List<Scan> getScans() {
        return scans;
    }

    public void setScans(List<Scan> scans) {
        CollectionUtils.replaceValuesInCollection(scans, this.scans);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanList)) return false;
        if (!super.equals(o)) return false;

        ScanList scanList = (ScanList) o;

        return scans.equals(scanList.scans);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + scans.hashCode();
        return result;
    }
}



