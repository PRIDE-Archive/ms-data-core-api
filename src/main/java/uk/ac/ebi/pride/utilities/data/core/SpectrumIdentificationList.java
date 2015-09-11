package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * Represents the set of all search results from SpectrumIdentification.
 * @author Yasset Perez-Riverol
 * Date: 08/08/11
 * Time: 12:14
 */
public class SpectrumIdentificationList extends IdentifiableParamGroup {

    /**
     * Contains the types of measures that will be reported in generic arrays for
     * each SpectrumIdentificationItem e.g. product ion m/z, product ion
     * intensity, product ion m/z error. Fragmentation Table is used as
     */
    private final List<IdentifiableParamGroup> fragmentationTable;

    /**
     * The number of database sequences searched against. This value should
     * be provided unless a de novo search has been performed.
     */
    private int numSequenceSearched;

    /**
     * All identifications made from searching one spectrum.
     * For PMF data, all peptide identifications will be listed underneath as
     * SpectrumIdentificationItems. For MS/MS data, there will be ranked
     * SpectrumIdentificationItems corresponding to possible different
     * peptide IDs.
     */
    private final List<Peptide> spectrumIdentificationList;

    public SpectrumIdentificationList(Comparable id, String name, int numSequenceSearched,
                                      List<IdentifiableParamGroup> fragmentationTable,
                                      List<Peptide> spectrumIdentificationList) {
        this(null, id, name, numSequenceSearched, fragmentationTable, spectrumIdentificationList);
    }

    public SpectrumIdentificationList(ParamGroup params, Comparable id, String name, int numSequenceSearched,
                                      List<IdentifiableParamGroup> fragmentationTable,
                                      List<Peptide> spectrumIdentificationList) {
        super(params, id, name);
        this.numSequenceSearched = numSequenceSearched;
        this.fragmentationTable = CollectionUtils.createListFromList(fragmentationTable);
        this.spectrumIdentificationList = CollectionUtils.createListFromList(spectrumIdentificationList);
    }

    public int getNumSequenceSearched() {
        return numSequenceSearched;
    }

    public void setNumSequenceSearched(int numSequenceSearched) {
        this.numSequenceSearched = numSequenceSearched;
    }

    public List<IdentifiableParamGroup> getFragmentationTable() {
        return fragmentationTable;
    }

    public void setFragmentationTable(List<IdentifiableParamGroup> fragmentationTable) {
        CollectionUtils.replaceValuesInCollection(fragmentationTable, this.fragmentationTable);
    }

    public List<Peptide> getSpectrumIdentificationResultList() {
        return spectrumIdentificationList;
    }

    public void setSpectrumIdentificationResultList(List<Peptide> spectrumIdentificationItemList) {
        CollectionUtils.replaceValuesInCollection(spectrumIdentificationItemList, this.spectrumIdentificationList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpectrumIdentificationList)) return false;
        if (!super.equals(o)) return false;

        SpectrumIdentificationList that = (SpectrumIdentificationList) o;

        return numSequenceSearched == that.numSequenceSearched && fragmentationTable.equals(that.fragmentationTable) && spectrumIdentificationList.equals(that.spectrumIdentificationList);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + fragmentationTable.hashCode();
        result = 31 * result + numSequenceSearched;
        result = 31 * result + spectrumIdentificationList.hashCode();
        return result;
    }
}



