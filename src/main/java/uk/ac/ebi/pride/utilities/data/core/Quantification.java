package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;

import java.util.*;

/**
 * Quantitation object is a pseudo object which contains all the cv params related to quantitative data for PRIDE XMLs.
 * <p/>
 * It also provides a set of methods for accessing these information
 * <p/>
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */

public class Quantification {

    /**
     * Isotope labelling method standard deviations
     */
    private final Double[] isotopeLabellingDeviations;

    /**
     * Isotope labelling method standard error
     */
    private final Double[] isotopeLabellingErrors;

    /**
     * Isotope labelling method
     */
    private QuantCvTermReference isotopeLabellingMethod;

    /**
     * Isotope labelling method results
     */
    private final Double[] isotopeLabellingResults;

    /**
     * Label free method results
     */
    private final Map<QuantCvTermReference, Double> labelFreeResults;

    /**
     * The type of the identification
     */
    private Type type;

    /**
     * quantification unit
     */
    private QuantCvTermReference unit;

    /**
     * The type of the identification;
     */
    public enum Type { PROTEIN, PEPTIDE }

    public Quantification(Type type, List<CvParam> cvParamList) {
        this.type                       = type;
        this.isotopeLabellingResults    = new Double[QuantitativeSample.MAX_SUB_SAMPLE_SIZE];
        this.isotopeLabellingDeviations = new Double[QuantitativeSample.MAX_SUB_SAMPLE_SIZE];
        this.isotopeLabellingErrors     = new Double[QuantitativeSample.MAX_SUB_SAMPLE_SIZE];
        this.labelFreeResults           = new HashMap<QuantCvTermReference, Double>();

        if (cvParamList != null) {
            init(cvParamList);
        }
    }

    private void init(List<CvParam> cvParamList) {
        for (CvParam cvParam : cvParamList) {

            // check intensities
            if (QuantCvTermReference.isIntensityParam(cvParam.getAccession())) {
                int index = QuantCvTermReference.getIntensityParamIndex(cvParam.getAccession());

                isotopeLabellingResults[index - 1] = Double.parseDouble(cvParam.getValue());
            }

            // check standard deviation
            else if (QuantCvTermReference.isStandardDeviationParam(cvParam.getAccession())) {
                int index = QuantCvTermReference.getStandardDeviationParamIndex(cvParam.getAccession());

                isotopeLabellingDeviations[index - 1] = Double.parseDouble(cvParam.getValue());
            }

            // check standard error
            else if (QuantCvTermReference.isStandardErrorParam(cvParam.getAccession())) {
                int index = QuantCvTermReference.getStandardErrorParamIndex(cvParam.getAccession());

                isotopeLabellingErrors[index - 1] = Double.parseDouble(cvParam.getValue());
            }

            // check unit
            else if (QuantCvTermReference.isUnit(cvParam.getAccession())) {
                unit = QuantCvTermReference.getUnit(cvParam.getAccession());
            }

            // check isotope labelling
            else if (QuantCvTermReference.isIsotopeLabellingMethodParam(cvParam.getAccession())) {
                isotopeLabellingMethod = QuantCvTermReference.getIsotopeLabellingMethodParam(cvParam.getAccession());
            }

            // check label free
            else if (QuantCvTermReference.isLabelFreeMethod(cvParam.getAccession())) {
                QuantCvTermReference method = QuantCvTermReference.getLabelFreeMethod(cvParam.getAccession());
                Double               value  = Double.parseDouble(cvParam.getValue());

                labelFreeResults.put(method, value);
            }
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Check whether the results of label free quantification methods are present
     *
     * @return boolean true means label free methods exist
     */
    public boolean hasLabelFreeMethod() {
        return (labelFreeResults.size() != 0);
    }

    /**
     * Get a list of label free methods which are present
     *
     * @return List<QuanCvTermReference>   a list of label free methods
     */
    public List<QuantCvTermReference> getLabelFreeMethods() {
        if (labelFreeResults != null) {
            return new ArrayList<QuantCvTermReference>(labelFreeResults.keySet());
        }

        return null;
    }

    /**
     * Get the results of label free quantification methods
     *
     * @param types the types of methods, the result will be ordered according the the input types
     * @return List<CvParam>   label free method results
     */
    public List<Double> getLabelFreeResults(Collection<QuantCvTermReference> types) {
        List<Double> results = new ArrayList<Double>();

        if (labelFreeResults != null) {
            for (QuantCvTermReference quantCvTermReference : types) {
                Double val = labelFreeResults.get(quantCvTermReference);

                results.add(val);
            }
        }

        return results;
    }

    /**
     * Check whether the results of isotope labelling methods are present
     *
     * @return boolean true means isotope labelling methods exist
     */
    public boolean hasIsotopeLabellingMethod() {
        return (isotopeLabellingMethod != null) && (isotopeLabellingResults != null);
    }

    /**
     * Get the isotope labelling method
     *
     * @return QuantCvTermReference    isotope labelling method
     */
    public QuantCvTermReference getIsotopeLabellingMethod() {
        return isotopeLabellingMethod;
    }

    /**
     * Get a list of results of isotope labelling
     *
     * @return List<CvParam>   a list of cv params
     */
    public List<Double> getIsotopeLabellingResults() {
        if (isotopeLabellingResults != null) {
            return Arrays.asList(isotopeLabellingResults);
        }

        return null;
    }

    /**
     * Get the result of isotope labelling according to a given index
     *
     * @param index index of the sub sample
     * @return CvParam the result of a sub sample
     */
    public Double getIsotopeLabellingResult(int index) {
        if ((isotopeLabellingResults != null) && (index > 0) && (index <= QuantitativeSample.MAX_SUB_SAMPLE_SIZE)) {
            return isotopeLabellingResults[index - 1];
        }

        return null;
    }

    /**
     * Get a list of standard deviations of isotope labelling
     *
     * @return List<Double>    a list of deviation
     */
    public List<Double> getIsotopeLabellingDeviation() {
        if (isotopeLabellingDeviations != null) {
            return Arrays.asList(isotopeLabellingDeviations);
        }

        return null;
    }

    /**
     * Get the standard deviation of a given sub sample
     *
     * @param index index of the sub sample
     * @return Double  standard deviation
     */
    public Double getIsotopeLabellingDeviation(int index) {
        if ((isotopeLabellingDeviations != null) && (index > 0) && (index <= QuantitativeSample.MAX_SUB_SAMPLE_SIZE)) {
            return isotopeLabellingDeviations[index - 1];
        }

        return null;
    }

    /**
     * Get a list of standard errors of isotope labelling
     *
     * @return List<Double>    a list of standard errors
     */
    public List<Double> getIsotopeLabellingError() {
        if (isotopeLabellingErrors != null) {
            return Arrays.asList(isotopeLabellingErrors);
        }

        return null;
    }

    /**
     * Get the standard error of a given sub sample
     *
     * @param index index of the sub sample
     * @return Double  standard error
     */
    public Double getIsotopeLabellingError(int index) {
        if ((isotopeLabellingErrors != null) && (index > 0) && (index <= QuantitativeSample.MAX_SUB_SAMPLE_SIZE)) {
            return isotopeLabellingErrors[index - 1];
        }

        return null;
    }

    /**
     * Convenient method to check whether the reported value is total intensity
     * These values can be used for calculating ratios.
     *
     * @return boolean     true means total intensities are present
     */
    public boolean hasTotalIntensities() {
        return hasIsotopeLabellingMethod() && (getUnit() == null);
    }

    /**
     * Get the unit of the quantification
     *
     * @return QuantCvTermReference    the term describes the unit
     */
    public QuantCvTermReference getUnit() {
        return unit;
    }

    /**
     * Get the index of reference sub sample
     *
     * @return int index of a reference sub sample, if two intensities are 1.0, then -1 index is returned, because
     *         it cannot be decided
     */
    public int getReferenceSubSampleIndex() {
        int index = -1;
        int cnt   = 0;

        for (int i = 0; i < isotopeLabellingResults.length; i++) {
            Double isotopeLabellingResult = isotopeLabellingResults[i];

            if ((isotopeLabellingResult != null) && (isotopeLabellingResult == 1.0)) {
                index = i + 1;
                cnt++;
            }
        }

        return (cnt == 1)
               ? index
               : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quantification that = (Quantification) o;

        return Arrays.equals(isotopeLabellingDeviations, that.isotopeLabellingDeviations) && Arrays.equals(isotopeLabellingErrors, that.isotopeLabellingErrors) && isotopeLabellingMethod == that.isotopeLabellingMethod && Arrays.equals(isotopeLabellingResults, that.isotopeLabellingResults) && !(labelFreeResults != null ? !labelFreeResults.equals(that.labelFreeResults) : that.labelFreeResults != null) && type == that.type && unit == that.unit;

    }

    @Override
    public int hashCode() {
        int result = isotopeLabellingDeviations != null ? Arrays.hashCode(isotopeLabellingDeviations) : 0;
        result = 31 * result + (isotopeLabellingErrors != null ? Arrays.hashCode(isotopeLabellingErrors) : 0);
        result = 31 * result + (isotopeLabellingMethod != null ? isotopeLabellingMethod.hashCode() : 0);
        result = 31 * result + (isotopeLabellingResults != null ? Arrays.hashCode(isotopeLabellingResults) : 0);
        result = 31 * result + (labelFreeResults != null ? labelFreeResults.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}



