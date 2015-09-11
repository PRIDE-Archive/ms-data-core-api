package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * A molecule modification specification. If n modifications have been found on a peptide,
 * there should be n instances of Modification. If multiple modifications are provided as cvParams,
 * it is assumed that the modification is ambiguous i.e. one modification or another.
 * A ParamGroup must be provided with the identification of the modification sourced from a
 * suitable CV e.g. UNIMOD. If the modification is not present in the CV (and this will be checked
 * by the semantic validator within a given tolerance window), there is a unknown modification
 * CV term that must be used instead. A neutral loss should be defined as an additional CVParam
 * within Modification. If more complex information should be given about neutral losses
 * (such as presence/absence on particular product ions), this can additionally be encoded within
 * the FragmentationArray.
 * <p>
 * The modification for PRIDE Model and MZIdentMl model have the following features:
 * - id: Modification identifier
 * - name: Modification Name.
 * - location: Location of the modification within the peptide - position in peptide sequence.
 * - residues: Specification of the residue (amino acid) on which the modification occurs.
 * - Average Mass Delta: Atomic mass delta considering the natural distribution of isotopes in Daltons.
 * - Average MonoIsotopic Mass Delta: Atomic mass delta when assuming only the most common isotope of elements in Daltons.
 * - Modification database where accession is from (used for PRIDE Objects)
 * - Modification database version is (used for PRIDE Objects)
 * </p>
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 04/08/11
 * Time: 14:11
 */
public class Modification extends IdentifiableParamGroup {

    /**
     * In the new validation approach for pride modification objects, just one Average Mass Delta could be associated
     * to a Modification. In the MzIdentMl Modification object only one Average Mass Delta is annotated.
     */
    private final List<Double> avgMassDelta;

    /**
     * Location of the modification within the peptide - position in peptide sequence, counted from
     * the N-terminus residue, starting at position 1. Specific modifications to the N-terminus should be
     * given the location 0. Modification to the C-terminus should be given as peptide length + 1.
     * MzIdentMl and PrideXML
     */

    private int location;

    /**
     * modification database where accession is from (used for PRIDE Objects)
     */
    private String modDatabase;

    /**
     * modification database version is (used for PRIDE Objects)
     */
    private String modDatabaseVersion;

    /**
     * In the new validation approach for pride modification objects, just one MonoIsotopic Mass Delta could be associated
     * to a Modification. In the MzIdentMl Modification object only one MonoIsotopic Mass Delta is annotated.
     */
    private final List<Double> monoisotopicMassDelta;

    /**
     * Possible Residues for this modification. In the PRIDE Object this attribute do not exist but in the
     * pride modification validator One modification can be related with more than one specificity. In MzIdentML
     * Object the Modification is related with more than one specificity.
     */
    private final List<String> residues;

    /**
     * Constructor for PRIDE Modification Object
     *
     * @param id                    ID
     * @param name                  Name
     * @param location              Location
     * @param residues              List of the possible residues where the modification is present
     * @param avgMassDelta          List of Possible Average Mass Delta
     * @param monoisotopicMassDelta List of Possible MonoIsotopic Mass Delta
     * @param modDatabase           DataBase Name
     * @param modDatabaseVersion    DataBase Version
     */
    public Modification(String id, String name, int location,
                        List<String> residues, List<Double> avgMassDelta,
                        List<Double> monoisotopicMassDelta, String modDatabase,
                        String modDatabaseVersion) {
        this(null, id, name, location, residues, avgMassDelta, monoisotopicMassDelta, modDatabase, modDatabaseVersion);
    }

    /**
     * Constructor for Modification Objects
     *
     * @param params                ParamGroup (CvTerms and User Params)
     * @param id                    ID
     * @param name                  Name
     * @param location              Location
     * @param residues              List of the possible residues where the modification is present
     * @param avgMassDelta          List of Possible Average Mass Delta
     * @param monoisotopicMassDelta List of Possible MonoIsotopic Mass Delta
     * @param modDatabase           DataBase Name
     * @param modDatabaseVersion    DataBase Version
     */
    public Modification(ParamGroup params, String id, String name, int location, List<String> residues,
                        List<Double> avgMassDelta, List<Double> monoisotopicMassDelta, String modDatabase,
                        String modDatabaseVersion) {
        super(params, id, name);
        this.location = location;
        this.residues = CollectionUtils.createListFromList(residues);
        this.avgMassDelta = CollectionUtils.createListFromList(avgMassDelta);
        this.monoisotopicMassDelta = CollectionUtils.createListFromList(monoisotopicMassDelta);
        this.modDatabase = modDatabase;
        this.modDatabaseVersion = modDatabaseVersion;
    }

    /**
     * Get Location of the Modification
     *
     * @return Location
     */
    public int getLocation() {
        return location;
    }

    /**
     * Set Location of the Modification
     *
     * @param location Location
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * Get the Amino Acids associated with this modification
     *
     * @return List of Residues (Amino Acids)
     */
    public List<String> getResidues() {
        return residues;
    }

    /**
     * Set the Amino Acids associated with this modification
     *
     * @param residues List of Residues (Amino Acids)
     */
    public void setResidues(List<String> residues) {
        CollectionUtils.replaceValuesInCollection(residues, this.residues);
    }

    /**
     * Get Modification DataBase Name
     *
     * @return DataBase Name
     */
    public String getModDatabase() {
        return modDatabase;
    }

    /**
     * Set Modification DataBase Name
     *
     * @param modDatabase DataBase Name
     */
    public void setModDatabase(String modDatabase) {
        this.modDatabase = modDatabase;
    }

    /**
     * Get Modification DataBase Version
     *
     * @return DataBase Version
     */
    public String getModDatabaseVersion() {
        return modDatabaseVersion;
    }

    /**
     * Set Modification DataBase Version
     *
     * @param modDatabaseVersion DataBase Version
     */
    public void setModDatabaseVersion(String modDatabaseVersion) {
        this.modDatabaseVersion = modDatabaseVersion;
    }

    /**
     * Get Average Mass Delta List
     *
     * @return Average Mass Delta List
     */
    public List<Double> getAvgMassDelta() {
        return avgMassDelta;
    }

    /**
     * Get Average Mass Delta List
     *
     * @param avgMassDelta Average Mass Delta List
     */
    public void setAvgMassDelta(List<Double> avgMassDelta) {
        CollectionUtils.replaceValuesInCollection(avgMassDelta, this.avgMassDelta);
    }

    /**
     * Get monoisotopic Mass Delta List
     *
     * @return monoisotopic Mass Delta List
     */
    public List<Double> getMonoisotopicMassDelta() {
        return monoisotopicMassDelta;
    }

    /**
     * Set monoisotopic mass delta List
     *
     * @param monoisotopicMassDelta monoisotopic mass delta List
     */
    public void setMonoisotopicMassDelta(List<Double> monoisotopicMassDelta) {
        CollectionUtils.replaceValuesInCollection(monoisotopicMassDelta, this.monoisotopicMassDelta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Modification)) return false;
        if (!super.equals(o)) return false;

        Modification that = (Modification) o;

        if (location != that.location) return false;
        if (!avgMassDelta.equals(that.avgMassDelta)) return false;
        if (modDatabase != null ? !modDatabase.equals(that.modDatabase) : that.modDatabase != null) return false;
        return !(modDatabaseVersion != null ? !modDatabaseVersion.equals(that.modDatabaseVersion) : that.modDatabaseVersion != null) && monoisotopicMassDelta.equals(that.monoisotopicMassDelta) && residues.equals(that.residues);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + avgMassDelta.hashCode();
        result = 31 * result + location;
        result = 31 * result + (modDatabase != null ? modDatabase.hashCode() : 0);
        result = 31 * result + (modDatabaseVersion != null ? modDatabaseVersion.hashCode() : 0);
        result = 31 * result + monoisotopicMassDelta.hashCode();
        result = 31 * result + residues.hashCode();
        return result;
    }
}



