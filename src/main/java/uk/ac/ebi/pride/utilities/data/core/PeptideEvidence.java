package uk.ac.ebi.pride.utilities.data.core;

/**
 * PeptideEvidence links a specific Peptide Element to a specific position in a DBSequence.
 * There must only be one PeptideEvidence item per Peptide-to-DBSequence-position.
 * <p>
 * The Peptide Evidence Element contains the following information:
 * - start position: Start position of the peptide inside the protein sequence.
 * - end position: The index position of the last amino acid of the peptide inside the protein sequence.
 * - preResidue and posResidue: Previous and Post flanking residues.
 * - decoy: Set to true if the peptide is matched to a decoy sequence.
 * - peptide Reference: The reference to the Peptide Sequence in the Database.
 * - Protein Reference: The reference to the Protein or Nucleotide Sequence in the database.
 * - frame: The translation frame of this sequence if this is PeptideEvidence derived from nucleic acid sequence.
 * - Translation Table: A reference to the translation table used if this is PeptideEvidence derived from nucleic acid sequence
 * </p>
 * @author Yasset Perez-Riverol
 * Date: 04/08/11
 * Time: 14:48
 */
public class PeptideEvidence extends IdentifiableParamGroup {

    /**
     * A reference to the protein sequence in which the specified peptide has been linked.
     */
    private DBSequence dbSequence;

    /**
     * The index position of the last amino acid of the peptide inside the protein sequence,
     * where the first amino acid of the protein sequence is position 1.
     * Must be provided unless this is a de novo search.
     */
    private Integer endPosition;

    /**
     * The translation frame of this sequence if this is PeptideEvidence derived from nucleic acid sequence.
     */
    private Integer frame;

    /**
     * A reference to the identified (poly)peptide sequence in the Peptide element.
     */
    private PeptideSequence peptideSequence;

    /**
     * Post flanking residue. If the peptide is C-terminal, post="-" and not post="".
     * If for any reason it is unknown (e.g. denovo), post="?" should be used.
     */
    private char postResidue;

    /**
     * Previous flanking residue. If the peptide is N-terminal, pre="-" and not pre="".
     * If for any reason it is unknown (e.g. denovo), pre="?" should be used.
     */
    private char preResidue;

    /**
     * Start position of the peptide inside the protein sequence, where the first amino acid of the
     * protein sequence is position 1. Must be provided unless this is a de novo search.
     */
    private Integer startPosition;

    /**
     * The details specifying this translation table are captured as cvParams, e.g. translation table,
     * translation start codons and translation table description (see specification document and mapping file)
     */
    private IdentifiableParamGroup translationTable;

    /**
     * Set to true if the peptide is matched to a decoy sequence.
     */
    private Boolean decoy;

    public PeptideEvidence(String id, String name, Integer startPosition, Integer endPosition, Boolean decoy,
                           PeptideSequence peptideSequence, DBSequence dbSequence) {
        this(null, id, name, startPosition, endPosition, decoy, peptideSequence, dbSequence);
    }

    public PeptideEvidence(ParamGroup params, String id, String name, Integer startPosition, Integer endPosition,
                           boolean decoy, PeptideSequence peptideSequence, DBSequence dbSequence) {
        super(params, id, name);
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.frame = -1;
        this.translationTable = null;
        this.preResidue = '\u0000';
        this.postResidue = '\u0000';
        this.decoy = decoy;
        this.peptideSequence = peptideSequence;
        this.dbSequence = dbSequence;
    }

    public Integer getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public Integer getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public char getPreResidue() {
        return preResidue;
    }

    public void setPreResidue(char preResidue) {
        this.preResidue = preResidue;
    }

    public char getPostResidue() {
        return postResidue;
    }

    public void setPostResidue(char postResidue) {
        this.postResidue = postResidue;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public boolean isDecoy() {
        return decoy;
    }

    public void setDecoy(boolean decoy) {
        this.decoy = decoy;
    }

    public PeptideSequence getPeptideSequence() {
        return peptideSequence;
    }

    public void setPeptideSequence(PeptideSequence peptideSequence) {
        this.peptideSequence = peptideSequence;
    }

    public DBSequence getDbSequence() {
        return dbSequence;
    }

    public void setDbSequence(DBSequence dbSequence) {
        this.dbSequence = dbSequence;
    }

    public IdentifiableParamGroup getTranslationTable() {
        return translationTable;
    }

    public void setTranslationTable(IdentifiableParamGroup translationTable) {
        this.translationTable = translationTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PeptideEvidence that = (PeptideEvidence) o;

        if (postResidue != that.postResidue) return false;
        if (preResidue != that.preResidue) return false;
        if (dbSequence != null ? !dbSequence.equals(that.dbSequence) : that.dbSequence != null) return false;
        if (decoy != null ? !decoy.equals(that.decoy) : that.decoy != null) return false;
        if (endPosition != null ? !endPosition.equals(that.endPosition) : that.endPosition != null) return false;
        if (frame != null ? !frame.equals(that.frame) : that.frame != null) return false;
        return !(peptideSequence != null ? !peptideSequence.equals(that.peptideSequence) : that.peptideSequence != null) && !(startPosition != null ? !startPosition.equals(that.startPosition) : that.startPosition != null) && !(translationTable != null ? !translationTable.equals(that.translationTable) : that.translationTable != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dbSequence != null ? dbSequence.hashCode() : 0);
        result = 31 * result + (endPosition != null ? endPosition.hashCode() : 0);
        result = 31 * result + (frame != null ? frame.hashCode() : 0);
        result = 31 * result + peptideSequence.hashCode();
        result = 31 * result + (int) postResidue;
        result = 31 * result + (int) preResidue;
        result = 31 * result + (startPosition != null ? startPosition.hashCode() : 0);
        result = 31 * result + (translationTable != null ? translationTable.hashCode() : 0);
        result = 31 * result + (decoy != null ? decoy.hashCode() : 0);
        return result;
    }
}



