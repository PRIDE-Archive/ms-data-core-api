package uk.ac.ebi.pride.utilities.data.controller.access;

import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.term.QuantCvTermReference;
import uk.ac.ebi.pride.utilities.term.SearchEngineScoreCvTermReference;

import java.util.Collection;
import java.util.Map;

/**
 * QuantDataAccess defines methods for accessing quantitative proteomics data
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
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

    /**
     * In mzTab files the information of Quantitation is store in StudyVariables and not in Samples
     * @return Return a Map of Study Variables as mzTab Files where the key is the Variable identifier and the value is the Variable
     */
    public Map<Comparable, StudyVariable> getStudyVariables();

    /**
     * Return the Quantitation Score for Study variables approach.
     * @param proteinId Protein ID
     * @return Protein
     */
    public QuantScore getProteinQuantStudyData(Comparable proteinId);

    /**
     * Retrieve the Quantitation Score of peptides in Study Variables
     * @param proteinID Protein Identifier
     * @param peptideId Peptide Identifier
     * @return  A QuantScore representing all the studyvariables and abundance variables for the peptide.
     */
    public QuantScore getPeptideQuantStudyData(Comparable proteinID, Comparable peptideId);

    /**
     * Whether this controller contains peptides
     *
     * @return boolean  return true if peptide exists
     */
    public boolean hasQuantPeptide();

    /**
     * Get a collection of peptide ids belong to the identification.
     *
     * @param proteinId identification id
     * @return Collection<Comparable>   peptide ids
     */
    public Collection<Comparable> getQuantPeptideIds(Comparable proteinId);

    /**
     * This is convenient method for accessing peptide.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return Peptide  peptide.
     */
    public QuantPeptide getQuantPeptideByIndex(Comparable proteinId, Comparable peptideId);

    /**
     * This method is to get a list of redundant peptide sequences
     *
     * @param proteinId identification id
     * @return Collection<String>   return a list of peptide sequences.
     */
    public Collection<String> getQuantPeptideSequences(Comparable proteinId);

    /**
     * Get peptide sequence according to identification id and peptide id.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return String   peptide sequence
     */
    public String getQuantPeptideSequence(Comparable proteinId, Comparable peptideId);

    /**
     * Get peptide's spectrum id
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return Comparable   spectrum reference.
     */
    public Comparable getQuantPeptideSpectrumId(Comparable proteinId, Comparable peptideId);

    /**
     * Get the total number of peptides
     *
     * @return int  total number of peptides.
     */
    public int getNumberOfQuantPeptides();

    /**
     * Get the number peptides of a identification.
     *
     * @param proteinId identification id.
     * @return int  number of peptides.
     */
    public int getNumberOfQuantPeptides(Comparable proteinId);

    /**
     * Get the number of unique peptides of a identification.
     *
     * @param proteinId identification id.
     * @return int  number of unique peptides.
     */
    public int getNumberOfUniqueQuantPeptides(Comparable proteinId);

    /**
     * Get the number of ptms of a identification.
     * Note: this is not unique number of PTMs.
     *
     * @param proteinId identification id.
     * @return int  the number of PTMs.
     */
    public int getNumberOfQuantPTMs(Comparable proteinId);

    /**
     * Get the number of PTMs for a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id
     * @return int  number of ptms
     */
    public int getNumberOfQuantPTMs(Comparable proteinId, Comparable peptideId);

    /**
     * Get the ptms assigned to a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide
     * @return Collection<Modification> a collection of ptms
     */
    public Collection<Modification> getQuantPTMs(Comparable proteinId, Comparable peptideId);


     /**
     * Get peptide score generated by search engine.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return PeptideScore  peptide score
     */
    public Score getQuantPeptideScore(Comparable proteinId, Comparable peptideId);

    /**
     * Get peptide score generated by search engine.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return PeptideScore  peptide score
     */
    public QuantScore getQuantPeptideQuantScore(Comparable proteinId, Comparable peptideId);

    /**
     * Get all Peptide Evidence for a Peptide Identification
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return Collection<PeptideEvidence> collection of peptide Evidences.
     */
    public Collection<PeptideEvidence> getQuantPeptideEvidences(Comparable proteinId, Comparable peptideId);

    /**
     * Get a collection of peptide scores in cv term reference format
     *
     * @return a collection of cv term references
     */
    public Collection<SearchEngineScoreCvTermReference> getAvailableQuantPeptideLevelScores();

}



