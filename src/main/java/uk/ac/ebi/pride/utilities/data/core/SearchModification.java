package uk.ac.ebi.pride.utilities.data.core;

//~--- JDK imports ------------------------------------------------------------

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Search Modification is the variable or fixed modification defined in the Identification Search
 * by the user. This modification is destroy related with the parameters of the Spectrum protocol
 * Identification.
 * <p/>
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 05/08/11
 * Time: 16:04
 */
public class SearchModification {

    private final List<CvParam> cvParams;

    private boolean fixedMod;

    private double massDelta;

    private final List<String> specificities;

    private final List<CvParam> specificityRules;

    public SearchModification(boolean fixedMod, double massDelta, List<String> specificities,
                              List<CvParam> specificityRules, List<CvParam> cvParams) {
        this.fixedMod = fixedMod;
        this.massDelta = massDelta;
        this.specificities = CollectionUtils.createListFromList(specificities);
        this.specificityRules = CollectionUtils.createListFromList(specificityRules);
        this.cvParams = CollectionUtils.createListFromList(cvParams);
    }

    public boolean isFixedMod() {
        return fixedMod;
    }

    public void setFixedMod(boolean fixedMod) {
        this.fixedMod = fixedMod;
    }

    public double getMassDelta() {
        return massDelta;
    }

    public void setMassDelta(double massDelta) {
        this.massDelta = massDelta;
    }

    public List<String> getSpecificities() {
        return specificities;
    }

    public void setSpecificities(List<String> specificities) {
        CollectionUtils.replaceValuesInCollection(specificities, this.specificities);
    }

    public List<CvParam> getSpecificityRules() {
        return specificityRules;
    }

    public void setSpecificityRules(List<CvParam> specificityRules) {
        CollectionUtils.replaceValuesInCollection(specificityRules, this.specificityRules);
    }

    public List<CvParam> getCvParams() {
        return cvParams;
    }

    public void setCvParams(List<CvParam> cvParams) {
        CollectionUtils.replaceValuesInCollection(cvParams, this.cvParams);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchModification)) return false;

        SearchModification that = (SearchModification) o;

        if (fixedMod != that.fixedMod) return false;
        return Double.compare(that.massDelta, massDelta) == 0 && cvParams.equals(that.cvParams) && specificities.equals(that.specificities) && specificityRules.equals(that.specificityRules);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = cvParams.hashCode();
        result = 31 * result + (fixedMod ? 1 : 0);
        temp = massDelta != +0.0d ? Double.doubleToLongBits(massDelta) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + specificities.hashCode();
        result = 31 * result + specificityRules.hashCode();
        return result;
    }
}



