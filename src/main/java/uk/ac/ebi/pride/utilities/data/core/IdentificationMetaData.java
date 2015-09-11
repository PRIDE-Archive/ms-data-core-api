package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * This class is used to manage and store the information of the metadata for protein and spectrum
 * identifications.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 19/08/11
 * Time: 11:56
 */
public class IdentificationMetaData extends IdentifiableParamGroup {

    /**
     * The parameters and settings of a ProteinDetection process.
     */
    private Protocol proteinDetectionProtocol;

    /**
     * List of database for searching mass spectra. Examples include a set of amino acid sequence entries, or annotated spectra libraries.
     */
    private final List<SearchDataBase> searchDataBases;

    /**
     * List of the parameters and settings of a SpectrumIdentification analysis.
     */
    private final List<SpectrumIdentificationProtocol> spectrumIdentificationProtocols;

    public IdentificationMetaData(Comparable id, String name,
                                  List<SpectrumIdentificationProtocol> spectrumIdentificationProtocols,
                                  Protocol proteinDetectionProtocol,
                                  List<SearchDataBase> searchDataBases) {
        super(id, name);
        this.spectrumIdentificationProtocols = CollectionUtils.createListFromList(spectrumIdentificationProtocols);
        this.searchDataBases = CollectionUtils.createListFromList(searchDataBases);
        this.proteinDetectionProtocol = proteinDetectionProtocol;
    }

    public IdentificationMetaData(ParamGroup params, Comparable id, String name,
                                  List<SpectrumIdentificationProtocol> spectrumIdentificationProtocols,
                                  Protocol proteinDetectionProtocol,
                                  List<SearchDataBase> searchDataBases) {
        super(params, id, name);
        this.spectrumIdentificationProtocols = CollectionUtils.createListFromList(spectrumIdentificationProtocols);
        this.searchDataBases = CollectionUtils.createListFromList(searchDataBases);
        this.proteinDetectionProtocol = proteinDetectionProtocol;
    }

    public List<SpectrumIdentificationProtocol> getSpectrumIdentificationProtocols() {
        return spectrumIdentificationProtocols;
    }

    public void setSpectrumIdentificationProtocols(List<SpectrumIdentificationProtocol> spectrumIdentificationProtocols) {
        CollectionUtils.replaceValuesInCollection(spectrumIdentificationProtocols, this.spectrumIdentificationProtocols);
    }

    public Protocol getProteinDetectionProtocol() {
        return proteinDetectionProtocol;
    }

    public void setProteinDetectionProtocol(Protocol proteinDetectionProtocol) {
        this.proteinDetectionProtocol = proteinDetectionProtocol;
    }

    public List<SearchDataBase> getSearchDataBases() {
        return searchDataBases;
    }

    public void setSearchDataBases(List<SearchDataBase> searchDataBases) {
        CollectionUtils.replaceValuesInCollection(searchDataBases, this.searchDataBases);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentificationMetaData)) return false;
        if (!super.equals(o)) return false;

        IdentificationMetaData that = (IdentificationMetaData) o;

        return !(proteinDetectionProtocol != null ? !proteinDetectionProtocol.equals(that.proteinDetectionProtocol) : that.proteinDetectionProtocol != null) && searchDataBases.equals(that.searchDataBases) && spectrumIdentificationProtocols.equals(that.spectrumIdentificationProtocols);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (proteinDetectionProtocol != null ? proteinDetectionProtocol.hashCode() : 0);
        result = 31 * result + searchDataBases.hashCode();
        result = 31 * result + spectrumIdentificationProtocols.hashCode();
        return result;
    }
}



