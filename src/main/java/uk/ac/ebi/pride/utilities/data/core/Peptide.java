package uk.ac.ebi.pride.utilities.data.core;

import java.util.List;

/**
 * The class Peptide Manage the information for peptide Identifications
 * @author Yasset Perez-Riverol
 * Date: 08/08/11
 * Time: 12:39
 */
public class Peptide {

    private PeptideEvidence peptideEvidence;

    private SpectrumIdentification spectrumIdentification;

    private int Id;


    public Peptide(PeptideEvidence peptideEvidence, SpectrumIdentification spectrumIdentification) {
        this.peptideEvidence = peptideEvidence;
        this.spectrumIdentification = spectrumIdentification;
    }

    public Peptide(PeptideEvidence peptideEvidence, SpectrumIdentification spectrumIdentification, int id) {
        this(peptideEvidence, spectrumIdentification);
        this.Id = id;
    }


    public PeptideEvidence getPeptideEvidence() {
        return peptideEvidence;
    }

    public void setPeptideEvidence(PeptideEvidence peptideEvidence) {
        this.peptideEvidence = peptideEvidence;
    }

    public SpectrumIdentification getSpectrumIdentification() {
        return spectrumIdentification;
    }

    public void setSpectrumIdentification(SpectrumIdentification spectrumIdentification) {
        this.spectrumIdentification = spectrumIdentification;
    }

    public PeptideSequence getPeptideSequence() {
        return this.getPeptideEvidence().getPeptideSequence();
    }

    public Spectrum getSpectrum() {
        return this.getSpectrumIdentification().getSpectrum();
    }

    public void setSpectrum(Spectrum spectrum) {
        this.getSpectrumIdentification().setSpectrum(spectrum);
    }

    public List<PeptideEvidence> getPeptideEvidenceList() {
        return this.getSpectrumIdentification().getPeptideEvidenceList();
    }

    public List<FragmentIon> getFragmentation() {
        return this.getSpectrumIdentification().getFragmentation();
    }

    public int getSequenceLength() {
        return this.getPeptideSequence().getSequence().length();
    }

    public List<Modification> getModifications() {
        return this.getPeptideEvidence().getPeptideSequence().getModifications();
    }

    public String getSequence() {
        return this.getPeptideSequence().getSequence();
    }

    public boolean hasModification() {
        return this.getPeptideSequence().getModifications().size() != 0;
    }

    public int getPrecursorCharge() {
        return this.getSpectrumIdentification().getChargeState();
    }

    public double getPrecursorMz() {
        return this.getSpectrumIdentification().getExperimentalMassToCharge();
    }

    public Score getScore() {
        return this.getSpectrumIdentification().getScore();
    }

    public QuantScore getQuantScore(){
        return this.getQuantScore();
    }

    public boolean isFragmentIonsChargeAnnotated() {
        boolean annotated = true;
        for (FragmentIon fragmentIon : getFragmentation()) {
            if (fragmentIon.getCharge() < 1) annotated = false;
        }

        return annotated;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peptide peptide = (Peptide) o;

        return !(peptideEvidence != null ? !peptideEvidence.equals(peptide.peptideEvidence) : peptide.peptideEvidence != null) && !(spectrumIdentification != null ? !spectrumIdentification.equals(peptide.spectrumIdentification) : peptide.spectrumIdentification != null);

    }

    @Override
    public int hashCode() {
        int result = peptideEvidence != null ? peptideEvidence.hashCode() : 0;
        result = 31 * result + (spectrumIdentification != null ? spectrumIdentification.hashCode() : 0);
        return result;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public ParamGroup getParamGroup() {
        ParamGroup paramGroup = new ParamGroup();
        if(peptideEvidence.getCvParams() != null)
            paramGroup.addCvParams(peptideEvidence.getCvParams());
        if(spectrumIdentification.getCvParams() != null)
            paramGroup.addCvParams(spectrumIdentification.getCvParams());

        if(peptideEvidence.getUserParams() != null)
            paramGroup.addUserParams(peptideEvidence.getUserParams());
        if(spectrumIdentification.getUserParams() != null)
            paramGroup.addUserParams(spectrumIdentification.getUserParams());

        return paramGroup;

    }
}



