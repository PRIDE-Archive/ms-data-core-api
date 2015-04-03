package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * DataBaseTranslation control and storage the information of Nucleotide Databases.
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class DataBaseTranslation {

    private final List<Integer> allowedFrames;

    private final List<IdentifiableParamGroup> translationTables;

    public DataBaseTranslation(List<Integer> allowedFrames, List<IdentifiableParamGroup> translationTables) {
        this.allowedFrames = CollectionUtils.createListFromList(allowedFrames);
        this.translationTables = CollectionUtils.createListFromList(translationTables);
    }

    public List<Integer> getAllowedFrames() {
        return allowedFrames;
    }

    public void setAllowedFrames(List<Integer> allowedFrames) {
        CollectionUtils.replaceValuesInCollection(allowedFrames, this.allowedFrames);
    }

    public List<IdentifiableParamGroup> getTranslationTables() {
        return translationTables;
    }

    public void setTranslationTables(List<IdentifiableParamGroup> translationTables) {
        CollectionUtils.replaceValuesInCollection(translationTables, this.translationTables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataBaseTranslation)) return false;

        DataBaseTranslation that = (DataBaseTranslation) o;

        return allowedFrames.equals(that.allowedFrames) && translationTables.equals(that.translationTables);

    }

    @Override
    public int hashCode() {
        int result = allowedFrames.hashCode();
        result = 31 * result + translationTables.hashCode();
        return result;
    }
}



