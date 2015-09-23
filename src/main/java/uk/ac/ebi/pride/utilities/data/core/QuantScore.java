package uk.ac.ebi.pride.utilities.data.core;

import java.util.Map;

/**
 * The QuantScore is the List of StudyVariable Scores and the Abundance by Assays. The abundance by assays is
 * independently of the number of Study variable and should be reported if quantitation is present.
 */

public class QuantScore extends IdentifiableParamGroup{

    //For each Study variable we should keep the value of the score
    Map<Comparable, Double> studyVariableScores;

    // For each assay we should kep the Total Abundance
    Map<Comparable, Double> assayAbundance;

    /**
     * Default Constructor for QuantScore.
     * @param id Id of the score
     * @param name name of the score
     * @param studyVariableScores Map of the study variables related with this Score
     * @param assayAbundance  Map of the abundances related with this score.
     */
    public QuantScore(Comparable id, String name, Map<Comparable, Double> studyVariableScores, Map<Comparable, Double> assayAbundance) {
        super(id, name);
        this.studyVariableScores = studyVariableScores;
        this.assayAbundance = assayAbundance;
    }

    /**
     * Default Constructor for QuantScore.
     * @param params CvParams related with this score
     * @param id Id of the score
     * @param name name of the score
     * @param studyVariableScores Map of the study variables related with this Score
     * @param assayAbundance  Map of the abundances related with this score.
     */
    public QuantScore(ParamGroup params, Comparable id, String name, Map<Comparable, Double> studyVariableScores, Map<Comparable, Double> assayAbundance) {
        super(params, id, name);
        this.studyVariableScores = studyVariableScores;
        this.assayAbundance = assayAbundance;
    }

    /**
     * Default Constructor for QuantScore without Cv Params
     *
     * @param studyVariableScores Map of the study variables related with this Score
     * @param assayAbundance  Map of the abundances related with this score.
     */
    public QuantScore(Map<Comparable, Double> studyVariableScores, Map<Comparable, Double> assayAbundance) {
        super(null, null, null);
        this.studyVariableScores = studyVariableScores;
        this.assayAbundance = assayAbundance;
    }

    /**
     * Return Study variables map
     * @return Abundance Map
     */
    public Map<Comparable, Double> getStudyVariableScores() {
        return studyVariableScores;
    }

    /**
     * Set abundance Study Variable Map
     * @param studyVariableScores   Study Variable Scores to be set.
     */
    public void setStudyVariableScores(Map<Comparable, Double> studyVariableScores) {
        this.studyVariableScores = studyVariableScores;
    }



    /**
     * Returns the Assay Abundance
     * @return  Return a Map where the key is the AssayAbundance Variable and Double is the Value.
     */
    public Map<Comparable, Double> getAssayAbundance() {
        return assayAbundance;
    }

    /**
     * Return the Assay Abundance
     * @param assayAbundance  Set eh Map of AssayAbundance as a Key (AssayAbundanceVariable) and Double the value.
     */
    public void setAssayAbundance(Map<Comparable, Double> assayAbundance) {
        this.assayAbundance = assayAbundance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        QuantScore that = (QuantScore) o;

        return !(assayAbundance != null ? !assayAbundance.equals(that.assayAbundance) : that.assayAbundance != null) && !(studyVariableScores != null ? !studyVariableScores.equals(that.studyVariableScores) : that.studyVariableScores != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (studyVariableScores != null ? studyVariableScores.hashCode() : 0);
        result = 31 * result + (assayAbundance != null ? assayAbundance.hashCode() : 0);
        return result;
    }
}
