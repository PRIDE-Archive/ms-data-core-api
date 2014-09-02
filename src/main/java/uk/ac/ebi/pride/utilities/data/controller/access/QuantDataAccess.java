package uk.ac.ebi.pride.utilities.data.controller.access;

import uk.ac.ebi.pride.utilities.data.core.Quantification;
import uk.ac.ebi.pride.utilities.data.core.QuantitativeSample;
import uk.ac.ebi.pride.utilities.data.utils.QuantCvTermReference;

import java.util.Collection;

/**
 * QuantDataAccess defines methods for accessing quantitative proteomics data
 * <p/>
 * @author rwang
 * @author ypriverol
 *
 */
public interface QuantDataAccess {

    /**
     * Check whether the experiment contains quantitative data
     *
     * @return boolean true means quantitative data exists
     */
    public boolean hasQuantData();

    /**
     * Check whether the experiment contains quantitative data at the protein level
     *
     * @return boolean true means quantitative data available
     */
    public boolean hasProteinQuantData();

    /**
     * Check whether the experiment contains total intensities at the protein level
     *
     * @return boolean  true means there are total intensities
     */
    public boolean hasProteinTotalIntensities();

    /**
     * Check whether the experiment contains quantitative data at the peptide level
     *
     * @return boolean true means quantitative data available
     */
    public boolean hasPeptideQuantData();

    /**
     * Check whether the experiment contains total intensities at the protein level
     *
     * @return boolean true means quantitative data available
     */
    public boolean hasPeptideTotalIntensities();

    /**
     * Check whether the experiment contains quantitative data using label free methods
     *
     * @return boolean true means label free methods (e.g. TIC, emPAI) have been used
     */
    public boolean hasLabelFreeQuantMethods();

    /**
     * Get all the label free methods used
     *
     * @return Collection<QuantCvTermReference>    a collection of label free methods
     */
    public Collection<QuantCvTermReference> getLabelFreeQuantMethods();

    /**
     * Get the label free methods at the protein identification level
     *
     * @return Collection<QuantCvTermReference>    a collection of label free methods
     */
    public Collection<QuantCvTermReference> getProteinLabelFreeQuantMethods();

    /**
     * Get the label free methods at the peptide identification level
     *
     * @return Collection<QuantCvTermReference>    a collection of label free methods
     */
    public Collection<QuantCvTermReference> getPeptideLabelFreeQuantMethods();

    /**
     * Check whether the experiment contains quantitative data using isotope labelling methods
     *
     * @return boolean true means isotope labelling methods have been used
     */
    public boolean hasIsotopeLabellingQuantMethods();

    /**
     * Get the isotope labelling methods at the protein level
     *
     * @return Collection<QuantCvTermReference>    a collection of isotope labelling methods
     */
    public Collection<QuantCvTermReference> getProteinIsotopeLabellingQuantMethods();

    /**
     * Get the isotope labelling methods at the peptide level
     *
     * @return Collection<QuantCvTermReference>    a collection of isotope labelling methods
     */
    public Collection<QuantCvTermReference> getPeptideIsotopeLabellingQuantMethods();

    /**
     * Get all the isotope labelling methods
     *
     * @return Collection<QuantCvTermReference>    a collection of isotope labelling methods
     */
    public Collection<QuantCvTermReference> getIsotopeLabellingQuantMethods();

    /**
     * Get quantitative method type
     *
     * @return Collection<QuantCvTermReference> a list of quantitative methods
     */
    public Collection<QuantCvTermReference> getQuantMethods();

    /**
     * Get the number of reagents
     *
     * @return int the number of reagents
     */
    public int getNumberOfReagents();

    /**
     * Get reference reagent's sub sample index
     *
     * @return int the index of a reference sub sample reagent
     */
    public int getReferenceSubSampleIndex();

    /**
     * Get the mapping between sub samples and reagents
     *
     * @return QuantitativeSample quantitative sample description
     */
    public QuantitativeSample getQuantSample();

    /**
     * Get the unit for protein identifications
     *
     * @return QuantCvTermReference    unit's cv term
     */
    public QuantCvTermReference getProteinQuantUnit();

    /**
     * Get the unit for peptide identification
     *
     * @return QuantCvTermReference    unit's cv term
     */
    public QuantCvTermReference getPeptideQuantUnit();

    /**
     * Get quantitative data related to a given protein
     *
     * @param proteinId protein identification id
     * @return Quantification   quantitative data
     */
    public Quantification getProteinQuantData(Comparable proteinId);

    /**
     * Get quantitative data related to a given peptide
     *
     * @param proteinId   protein identification id
     * @param peptideId peptide id
     * @return Quantification   quantitative data
     */
    public Quantification getPeptideQuantData(Comparable proteinId, Comparable peptideId);
}



