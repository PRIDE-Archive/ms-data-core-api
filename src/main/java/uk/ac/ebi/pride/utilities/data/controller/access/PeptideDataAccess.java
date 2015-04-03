package uk.ac.ebi.pride.utilities.data.controller.access;

import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.term.SearchEngineScoreCvTermReference;

import java.util.Collection;

/**
 * PeptideDataAccess defines methods for accessing peptide related information.
 * <p/>
 * You may find the concept of peptide id alien to PRIDE XML files, the index of peptide in
 * the identification should be used in this case.
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */

public interface PeptideDataAccess {

    /**
     * Whether this controller contains peptides
     *
     * @return boolean  return true if peptide exists
     */
    public boolean hasPeptide();

    /**
     * Get a collection of peptide ids belong to the identification.
     *
     * @param proteinId identification id
     * @return Collection<Comparable>   peptide ids
     */
    public Collection<Comparable> getPeptideIds(Comparable proteinId);

    /**
     * This is convenient method for accessing peptide.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return Peptide  peptide.
     */
    public Peptide getPeptideByIndex(Comparable proteinId, Comparable peptideId);

    /**
     * Convenient method for accessing the rank of the peptide
     *
     * @param proteinId protein id
     * @param peptideId peptide id
     * @return rank in integer
     */
    public int getPeptideRank(Comparable proteinId, Comparable peptideId);

    /**
     * This method is to get a list of redundant peptide sequences
     *
     * @param proteinId identification id
     * @return Collection<String>   return a list of peptide sequences.
     */
    public Collection<String> getPeptideSequences(Comparable proteinId);

    /**
     * Get peptide sequence according to identification id and peptide id.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return String   peptide sequence
     */
    public String getPeptideSequence(Comparable proteinId, Comparable peptideId);

    /**
     * Get peptide sequence start
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return int  start position for the peptide
     */
    public int getPeptideSequenceStart(Comparable proteinId, Comparable peptideId);

    /**
     * Get peptide sequence stop
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return int  stop position for the peptide
     */
    public int getPeptideSequenceEnd(Comparable proteinId, Comparable peptideId);

    /**
     * Get peptide's spectrum id
     *
     * @param proteinId identification id
     * @param peptideId peptide id, this can be the index of the peptide as well.
     * @return Comparable   spectrum reference.
     */
    public Comparable getPeptideSpectrumId(Comparable proteinId, Comparable peptideId);

    /**
     * Get the total number of peptides
     *
     * @return int  total number of peptides.
     */
    public int getNumberOfPeptides();

    /**
     * Get the total number of peptides of a given Rank
     *
     * @return int  total number of peptides.
     */
    public int getNumberOfPeptidesByRank(int rank);

    /**
     * Get the number peptides of a identification.
     *
     * @param proteinId identification id.
     * @return int  number of peptides.
     */
    public int getNumberOfPeptides(Comparable proteinId);

    /**
     * Get the number of unique peptides of a identification.
     *
     * @param proteinId identification id.
     * @return int  number of unique peptides.
     */
    public int getNumberOfUniquePeptides(Comparable proteinId);

    /**
     * Get the number of ptms of a identification.
     * Note: this is not unique number of PTMs.
     *
     * @param proteinId identification id.
     * @return int  the number of PTMs.
     */
    public int getNumberOfPTMs(Comparable proteinId);

    /**
     * Get the number of PTMs for a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id
     * @return int  number of ptms
     */
    public int getNumberOfPTMs(Comparable proteinId, Comparable peptideId);

    /**
     * Get the ptms assigned to a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide
     * @return Collection<Modification> a collection of ptms
     */
    public Collection<Modification> getPTMs(Comparable proteinId, Comparable peptideId);

    /**
     * Get the number of Substitution ptms of a identification.
     * Note: this is not unique number of PTMs.
     *
     * @param proteinId identification id.
     * @return int  the number of PTMs.
     */
    public int getNumberOfSubstitutionPTMs(Comparable proteinId);

    /**
     * Get the number of Substitution PTMs for a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id
     * @return int  number of ptms
     */
    public int getNumberOfSubstitutionPTMs(Comparable proteinId, Comparable peptideId);

    /**
     * Get the ptms assigned to a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide
     * @return Collection<Modification> a collection of ptms
     */
    public Collection<SubstitutionModification> getSubstitutionPTMs(Comparable proteinId, Comparable peptideId);

    /**
     * Get the number of fragment ions for a peptide
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return int  number of fragment ions
     */
    public int getNumberOfFragmentIons(Comparable proteinId, Comparable peptideId);

    /**
     * Get the fragment ions assigned to the peptide.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return Collection<FragmentIon>  a collection of fragment ions.
     */
    public Collection<FragmentIon> getFragmentIons(Comparable proteinId, Comparable peptideId);

    /**
     * Get peptide score generated by search engine.
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return PeptideScore  peptide score
     */
    public Score getPeptideScore(Comparable proteinId, Comparable peptideId);

    /**
     * Get all Peptide Evidence for a Peptide Identification
     *
     * @param proteinId identification id
     * @param peptideId peptide id, can be the index of the peptide as well.
     * @return Collection<PeptideEvidence> collection of peptide Evidences.
     */
    public Collection<PeptideEvidence> getPeptideEvidences(Comparable proteinId, Comparable peptideId);

    /**
     * Get a collection of peptide scores in cv term reference format
     *
     * @return a collection of cv term references
     */
    public Collection<SearchEngineScoreCvTermReference> getAvailablePeptideLevelScores();

    /**
     * Get precursor charge on peptide level
     * Note: sometimes, precursor charge at the peptide level is different from the precursor charge at the spectrum level
     * As the peptide-level precursor charge is often assigned by search engine rather than ms instrument
     *
     * @param identId   identification id
     * @param peptideId peptid eid, can be the index of the peptide as well.
     * @return precursor charge, null should be returned if not available
     */
    public Integer getPeptidePrecursorCharge(Comparable identId, Comparable peptideId);

    /**
     * Get precursor m/z on peptide level
     * Note: sometimes, precursor m/z at the peptide level is different from the precursor charge at the spectrum level
     * As the peptide-level precursor charge is often assigned by search engine rather than ms instrument
     *
     * @param proteinId identification id
     * @param peptideId peptid eid, can be the index of the peptide as well.
     * @return precursor charge, null should be returned if not available
     */public double getPeptidePrecursorMz(Comparable proteinId, Comparable peptideId);

    /**
     * Get The Peptide Theoretical Mz from the file
     * Note: sometimes, precursor m/z at the peptide level is different from the precursor charge at the spectrum level
     * As the peptide-level precursor charge is often assigned by search engine rather than ms instrument
     *
     * @param proteinId identification id
     * @param peptideId peptid eid, can be the index of the peptide as well.
     * @return precursor charge, null should be returned if not available
     */public double getPeptideTheoreticalMz(Comparable proteinId, Comparable peptideId);


}



