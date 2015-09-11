package uk.ac.ebi.pride.utilities.data.core;


import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * The parameters and settings of a SpectrumIdentification analysis.
 * @author Yasset Perez-Riverol
 * Date: 05/08/11
 * Time: 15:56
 */
public class SpectrumIdentificationProtocol extends Protocol {

    /**
     * A specification of how a nucleic acid sequence database was translated for searching.
     */
    private DataBaseTranslation dataBaseTranslation;

    /**
     * The list of enzymes used in experiment
     */
    private final List<Enzyme> enzymes;

    /**
     * The specification of filters applied to the database searched.
     */
    private final List<Filter> filters;

    /**
     * The tolerance of the search given as a plus and minus value with units.
     */
    private final List<CvParam> fragmentTolerance;

    /**
     * The masses of residues used in the search.
     */
    private final List<MassTable> massTables;

    /**
     * The tolerance of the search given as a plus and minus value with units.
     */
    private final List<CvParam> parentTolerance;

    /**
     * The specification of static/variable modifications
     * (e.g. Oxidation of Methionine) that are to be considered in the spectra search.
     */
    private final List<SearchModification> searchModifications;

    /**
     * The type of search performed e.g. PMF, Tag searches, MS-MS
     */
    private ParamGroup searchType;

    /**
     * If there are multiple enzymes specified, this attribute is set to true if
     * cleavage with different enzymes is performed independently.
     */
    private boolean enzymeIndependent;

    public SpectrumIdentificationProtocol(Comparable id, String name, Software analysisSoftware,
                                          ParamGroup analysisParam, ParamGroup threshold, ParamGroup searchType,
                                          List<SearchModification> searchModifications, boolean enzymeIndependent, List<Enzyme> enzymes,
                                          List<MassTable> massTables, List<CvParam> fragmentTolerance, List<CvParam> parentTolerance,
                                          List<Filter> filters, DataBaseTranslation dataBaseTranslation) {
        this(analysisParam, id, name, analysisSoftware, threshold, searchType, searchModifications, enzymeIndependent,
                enzymes, massTables, fragmentTolerance, parentTolerance, filters, dataBaseTranslation);
    }


    public SpectrumIdentificationProtocol(ParamGroup analysisParam, Comparable id, String name,
                                          Software analysisSoftware, ParamGroup threshold, ParamGroup searchType,
                                          List<SearchModification> searchModifications, boolean enzymeIndependent, List<Enzyme> enzymes,
                                          List<MassTable> massTables, List<CvParam> fragmentTolerance, List<CvParam> parentTolerance,
                                          List<Filter> filters, DataBaseTranslation dataBaseTranslation) {
        super(analysisParam, id, name, analysisSoftware, threshold);
        this.searchType = searchType;
        this.searchModifications = CollectionUtils.createListFromList(searchModifications);
        this.enzymeIndependent = enzymeIndependent;
        this.enzymes = CollectionUtils.createListFromList(enzymes);
        this.massTables = CollectionUtils.createListFromList(massTables);
        this.fragmentTolerance = CollectionUtils.createListFromList(fragmentTolerance);
        this.parentTolerance = CollectionUtils.createListFromList(parentTolerance);
        this.filters = CollectionUtils.createListFromList(filters);
        this.dataBaseTranslation = dataBaseTranslation;
    }

    public ParamGroup getSearchType() {
        return searchType;
    }

    public void setSearchType(ParamGroup searchType) {
        this.searchType = searchType;
    }

    public List<SearchModification> getSearchModifications() {
        return searchModifications;
    }

    public void setSearchModifications(List<SearchModification> searchModifications) {
        CollectionUtils.replaceValuesInCollection(searchModifications, this.searchModifications);
    }

    public boolean isEnzymeIndependent() {
        return enzymeIndependent;
    }

    public void setEnzymeIndependent(boolean enzymeIndependent) {
        this.enzymeIndependent = enzymeIndependent;
    }

    public List<Enzyme> getEnzymes() {
        return enzymes;
    }

    public void setEnzymes(List<Enzyme> enzymes) {
        CollectionUtils.replaceValuesInCollection(enzymes, this.enzymes);
    }

    public List<MassTable> getMassTables() {
        return massTables;
    }

    public void setMassTables(List<MassTable> massTables) {
        CollectionUtils.replaceValuesInCollection(massTables, this.massTables);
    }

    public List<CvParam> getFragmentTolerance() {
        return fragmentTolerance;
    }

    public void setFragmentTolerance(List<CvParam> fragmentTolerance) {
        CollectionUtils.replaceValuesInCollection(fragmentTolerance, this.fragmentTolerance);
    }

    public List<CvParam> getParentTolerance() {
        return parentTolerance;
    }

    public void setParentTolerance(List<CvParam> parentTolerance) {
        CollectionUtils.replaceValuesInCollection(parentTolerance, this.parentTolerance);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        CollectionUtils.replaceValuesInCollection(filters, this.filters);
    }

    public DataBaseTranslation getDataBaseTranslation() {
        return dataBaseTranslation;
    }

    public void setDataBaseTranslation(DataBaseTranslation dataBaseTranslation) {
        this.dataBaseTranslation = dataBaseTranslation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpectrumIdentificationProtocol)) return false;
        if (!super.equals(o)) return false;

        SpectrumIdentificationProtocol that = (SpectrumIdentificationProtocol) o;

        if (enzymeIndependent != that.enzymeIndependent) return false;
        if (dataBaseTranslation != null ? !dataBaseTranslation.equals(that.dataBaseTranslation) : that.dataBaseTranslation != null)
            return false;
        if (!enzymes.equals(that.enzymes)) return false;
        if (!filters.equals(that.filters)) return false;
        if (!fragmentTolerance.equals(that.fragmentTolerance)) return false;
        return massTables.equals(that.massTables) && parentTolerance.equals(that.parentTolerance) && searchModifications.equals(that.searchModifications) && !(searchType != null ? !searchType.equals(that.searchType) : that.searchType != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dataBaseTranslation != null ? dataBaseTranslation.hashCode() : 0);
        result = 31 * result + enzymes.hashCode();
        result = 31 * result + filters.hashCode();
        result = 31 * result + fragmentTolerance.hashCode();
        result = 31 * result + massTables.hashCode();
        result = 31 * result + parentTolerance.hashCode();
        result = 31 * result + searchModifications.hashCode();
        result = 31 * result + (searchType != null ? searchType.hashCode() : 0);
        result = 31 * result + (enzymeIndependent ? 1 : 0);
        return result;
    }
}



