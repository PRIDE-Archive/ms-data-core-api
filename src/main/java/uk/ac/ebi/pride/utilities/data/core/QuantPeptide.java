package uk.ac.ebi.pride.utilities.data.core;



/**
 * Quantitative Peptide is a Peptide with Quantitation information and is used in mzTab file formats.
 */
public class QuantPeptide extends Peptide {

    /**
     * Score of quantitation by Study Variable
     */
    private QuantScore quantScore;

    public QuantPeptide(PeptideEvidence peptideEvidence, SpectrumIdentification spectrumIdentification,
                        QuantScore quantScore,
                        int index) {
        super(peptideEvidence, spectrumIdentification,index);
        this.quantScore = quantScore;
    }

    public QuantScore getQuantScore() {
        return quantScore;
    }

    public void setQuantScore(QuantScore quantScore) {
        this.quantScore = quantScore;
    }

}
