package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;
import uk.ac.ebi.pride.utilities.data.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IonType defines the index of fragmentation ions being reported,
 * importing a CV term for the type of ion e.g. b ion. Example: if b3 b7 b8 and b10
 * have been identified, the index attribute will contain 3 7 8 10, and the
 * corresponding values will be reported in parallel arrays below.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 08/08/11
 * Time: 14:01
 */
public class IonType extends CvParam {

    /**
     * The charge of the identified fragmentation ions.
     */
    private int charge;

    /**
     * The index of ions identified as integers, following standard notation for
     * a-c, x-z e.g. if b3 b5 and b6 have been identified, the index would store
     * "3 5 6". For internal ions, the index contains pairs defining the start
     * and end point - see specification document for examples.
     * For immonium ions, the index is the position of the identified ion within
     * the peptide sequence - if the peptide contains the same amino acid in
     * multiple positions that cannot be distinguished, all positions should be
     * given.
     * <p/>
     */
    private final List<Integer> index;

    /**
     * An array of values for a given type of measure and for a particular ion
     * type, in parallel to the index of ions identified.
     * <p/>
     */
    private final Map<IdentifiableParamGroup, List<Integer>> measures;

    /**
     * Constructor
     *
     * @param accession      required.
     * @param name           required.
     * @param cvLookupID     required.
     * @param value          optional.
     * @param unitAcc        optional.
     * @param unitName       optional.
     * @param unitCVLookupID optional.
     */
    public IonType(String accession, String name,
                   String cvLookupID, String value,
                   String unitAcc, String unitName,
                   String unitCVLookupID) {
        super(accession, name, cvLookupID, value, unitAcc, unitName, unitCVLookupID);
        this.charge = -1;
        this.index = new ArrayList<Integer>();
        this.measures = new HashMap<IdentifiableParamGroup, List<Integer>>();
    }

    public List<Integer> getIndex() {
        return index;
    }

    public void setIndex(List<Integer> index) {
        CollectionUtils.replaceValuesInCollection(index, this.index);
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public Map<IdentifiableParamGroup, List<Integer>> getMeasures() {
        return measures;
    }

    public void setMeasures(Map<IdentifiableParamGroup, List<Integer>> measures) {
        MapUtils.replaceValuesInMap(measures, this.measures);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IonType)) return false;
        if (!super.equals(o)) return false;

        IonType ionType = (IonType) o;

        return charge == ionType.charge && index.equals(ionType.index) && measures.equals(ionType.measures);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + charge;
        result = 31 * result + index.hashCode();
        result = 31 * result + measures.hashCode();
        return result;
    }
}



