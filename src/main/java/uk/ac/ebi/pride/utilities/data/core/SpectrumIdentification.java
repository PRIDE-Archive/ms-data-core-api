package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * PSM Spectrum identification match. The relation between an Spectrum and a PeptideEvidence.
 * It contains a number of Scores from different search Engines.
 * @author Yasset Perez-Riverol
 * Date: 05/08/11
 * Time: 15:49
 */
public class SpectrumIdentification extends IdentifiableParamGroup {

    /**
     * The theoretical mass-to-charge value calculated for the peptide in Daltons / charge.
     */
    private double calculatedMassToCharge;

    /**
     * The calculated isoelectric point of the (poly)peptide, with relevant
     * modifications included. Do not supply this value if the PI cannot be
     * calculated properly.
     */
    private double calculatedPI;

    /**
     * The charge state of the identified peptide.
     */
    private int chargeState;

    /**
     * The mass-to-charge value measured in the experiment in Daltons / charge.
     */
    private double experimentalMassToCharge;

    /**
     * The product ions identified in this result.
     */
    private final List<FragmentIon> fragmentation;

    /**
     * A reference should be given to the MassTable used to calculate the
     * sequenceMass only if more than one MassTable has been given.
     */
    private MassTable massTable;

    /**
     * Reference to the PeptideEvidence element identified. If a specific
     * sequence can be assigned to multiple proteins and or positions in a
     * protein all possible PeptideEvidence elements should be referenced here.
     */
    private final List<PeptideEvidence> peptideEvidenceList;

    /**
     * Score stores a number of peptide scores for a list of search engines.
     */
    private Score score;

    /**
     * A reference to the identified (poly)peptide sequence in the Peptide element.
     */
    private PeptideSequence peptideSequence;

    /**
     * For an MS/MS result set, this is the rank of the identification quality as
     * scored by the search engine. 1 is the top rank. If multiple identifications
     * have the same top score, they should all be assigned rank =1. For PMF data, the
     * rank attribute may be meaningless and values of rank = 0 should be given.
     */
    private int rank;

    /**
     * Set to true if the producers of the file has deemed that the identification
     * has passed a given threshold or been validated as correct.
     * If no such threshold has been set, value of true should be given for all
     * results.
     */
    private boolean passThreshold;

    /**
     * A reference should be provided to link the SpectrumIdentificationItem
     * to a Sample if more than one sample has been described in the
     * AnalysisSampleCollection.
     */
    private Sample sample;

    /**
     * A reference to a spectra data set (e.g. a spectra file).
     */
    private SpectraData spectraData;

    /**
     * The locally unique id for the spectrum in the spectra data set specified
     * by SpectraData_ref. External guidelines are provided on the use of
     * consistent identifiers for spectra in different external formats.
     */
    private Spectrum spectrum;

    /**
     * Add retention time to the SpectrumIdentification Item.
     */
    private String retentionTime;

    /**
     * Spectrum Identification Item
     * @param id                        Id
     * @param name                      name
     * @param chargeState               Charge state
     * @param experimentalMassToCharge  Experimental Charge
     * @param calculatedMassToCharge    Calculated Mass
     * @param calculatedPI              Calculated PI
     * @param peptideSequence           Peptide Sequence
     * @param rank                      Peptide Rank
     * @param passThreshold             If Pass the threshold
     * @param massTable                 Mass table used to identified the peptide
     * @param sample                    Sample in which this peptide appear
     * @param peptideEvidenceList       List of peptide evidences.
     * @param fragmentation             Fragmentation assigned
     * @param score                     Identification Score
     * @param spectrum                  Spectrum use to identified the peptide
     * @param spectraData               The reference to SpectraData
     */
    public SpectrumIdentification(Comparable id, String name, int chargeState, double experimentalMassToCharge,
                                  double calculatedMassToCharge, double calculatedPI, PeptideSequence peptideSequence, int rank,
                                  boolean passThreshold, MassTable massTable, Sample sample,
                                  List<PeptideEvidence> peptideEvidenceList, List<FragmentIon> fragmentation,
                                  Score score, Spectrum spectrum, SpectraData spectraData) {
        this(null, id, name, chargeState, experimentalMassToCharge, calculatedMassToCharge, calculatedPI,
                peptideSequence, rank, passThreshold, massTable, sample, peptideEvidenceList, fragmentation,
                score, spectrum, spectraData);
    }

    /**
     * Spectrum Identification Item
     * @param params                    CvParams associated with the score
     * @param id                        Id
     * @param name                      name
     * @param chargeState               Charge state
     * @param experimentalMassToCharge  Experimental Charge
     * @param calculatedMassToCharge    Calculated Mass
     * @param calculatedPI              Calculated PI
     * @param peptideSequence           Peptide Sequence
     * @param rank                      Peptide Rank
     * @param passThreshold             If Pass the threshold
     * @param massTable                 Mass table used to identified the peptide
     * @param sample                    Sample in which this peptide appear
     * @param peptideEvidenceList       List of peptide evidences.
     * @param fragmentation             Fragmentation assigned
     * @param score                     Identification Score
     * @param spectrum                  Spectrum use to identified the peptide
     * @param spectraData               The reference to SpectraData
     */
    public SpectrumIdentification(ParamGroup params, Comparable id, String name, int chargeState, double experimentalMassToCharge,
                                  double calculatedMassToCharge, double calculatedPI, PeptideSequence peptideSequence, int rank,
                                  boolean passThreshold, MassTable massTable, Sample sample,
                                  List<PeptideEvidence> peptideEvidenceList, List<FragmentIon> fragmentation,
                                  Score score, Spectrum spectrum, SpectraData spectraData) {
        super(params, id, name);
        this.chargeState = chargeState;
        this.experimentalMassToCharge = experimentalMassToCharge;
        this.calculatedMassToCharge = calculatedMassToCharge;
        this.calculatedPI = calculatedPI;
        this.peptideSequence = peptideSequence;
        this.rank = rank;
        this.passThreshold = passThreshold;
        this.massTable = massTable;
        this.sample = sample;
        this.peptideEvidenceList = CollectionUtils.createListFromList(peptideEvidenceList);
        this.fragmentation = CollectionUtils.createListFromList(fragmentation);
        this.score = score;
        this.spectrum = spectrum;
        this.spectraData = spectraData;
    }

    public int getChargeState() {
        return chargeState;
    }

    public void setChargeState(int chargeState) {
        this.chargeState = chargeState;
    }

    public double getExperimentalMassToCharge() {
        return experimentalMassToCharge;
    }

    public void setExperimentalMassToCharge(double experimentalMassToCharge) {
        this.experimentalMassToCharge = experimentalMassToCharge;
    }

    public double getCalculatedMassToCharge() {
        return calculatedMassToCharge;
    }

    public void setCalculatedMassToCharge(double calculatedMassToCharge) {
        this.calculatedMassToCharge = calculatedMassToCharge;
    }

    public double getCalculatedPI() {
        return calculatedPI;
    }

    public void setCalculatedPI(double calculatedPI) {
        this.calculatedPI = calculatedPI;
    }

    public PeptideSequence getPeptideSequence() {
        return peptideSequence;
    }

    public void setPeptideSequence(PeptideSequence peptideSequence) {
        this.peptideSequence = peptideSequence;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isPassThreshold() {
        return passThreshold;
    }

    public void setPassThreshold(boolean passThreshold) {
        this.passThreshold = passThreshold;
    }

    public MassTable getMassTable() {
        return massTable;
    }

    public void setMassTable(MassTable massTable) {
        this.massTable = massTable;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public List<PeptideEvidence> getPeptideEvidenceList() {
        return peptideEvidenceList;
    }

    public void setPeptideEvidenceList(List<PeptideEvidence> peptideEvidenceList) {
        CollectionUtils.replaceValuesInCollection(peptideEvidenceList, this.peptideEvidenceList);
    }

    public List<FragmentIon> getFragmentation() {
        return fragmentation;
    }

    public void setFragmentation(List<FragmentIon> fragmentation) {
        CollectionUtils.replaceValuesInCollection(fragmentation, this.fragmentation);
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    public SpectraData getSpectraData() {
        return spectraData;
    }

    public void setSpectraData(SpectraData spectraData) {
        this.spectraData = spectraData;
    }

    public boolean hasModification() {
        return (!(getPeptideSequence().getModifications().isEmpty()));
    }

    public int getSequenceLength() {
        return getPeptideSequence().getSequence().length();
    }

    public List<Modification> getModifications() {
        return getPeptideSequence().getModifications();
    }

    public String getSequence() {
        return getPeptideSequence().getSequence();
    }

    public String getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(String retentionTime) {
        this.retentionTime = retentionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SpectrumIdentification that = (SpectrumIdentification) o;

        if (Double.compare(that.calculatedMassToCharge, calculatedMassToCharge) != 0) return false;
        if (Double.compare(that.calculatedPI, calculatedPI) != 0) return false;
        if (chargeState != that.chargeState) return false;
        if (Double.compare(that.experimentalMassToCharge, experimentalMassToCharge) != 0) return false;
        if (passThreshold != that.passThreshold) return false;
        if (rank != that.rank) return false;
        if (fragmentation != null ? !fragmentation.equals(that.fragmentation) : that.fragmentation != null)
            return false;
        if (massTable != null ? !massTable.equals(that.massTable) : that.massTable != null) return false;
        if (peptideEvidenceList != null ? !peptideEvidenceList.equals(that.peptideEvidenceList) : that.peptideEvidenceList != null)
            return false;
        if (peptideSequence != null ? !peptideSequence.equals(that.peptideSequence) : that.peptideSequence != null)
            return false;
        if (sample != null ? !sample.equals(that.sample) : that.sample != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;
        return !(spectraData != null ? !spectraData.equals(that.spectraData) : that.spectraData != null) && !(spectrum != null ? !spectrum.equals(that.spectrum) : that.spectrum != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(calculatedMassToCharge);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(calculatedPI);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + chargeState;
        temp = Double.doubleToLongBits(experimentalMassToCharge);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (fragmentation != null ? fragmentation.hashCode() : 0);
        result = 31 * result + (massTable != null ? massTable.hashCode() : 0);
        result = 31 * result + (peptideEvidenceList != null ? peptideEvidenceList.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (peptideSequence != null ? peptideSequence.hashCode() : 0);
        result = 31 * result + rank;
        result = 31 * result + (passThreshold ? 1 : 0);
        result = 31 * result + (sample != null ? sample.hashCode() : 0);
        result = 31 * result + (spectraData != null ? spectraData.hashCode() : 0);
        result = 31 * result + (spectrum != null ? spectrum.hashCode() : 0);
        return result;
    }
}



